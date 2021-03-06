/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2004-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.modelsetbio;

import java.util.Arrays;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;


import org.jmol.java.BS;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelLoader;
import org.jmol.modelset.ModelSet;
import org.jmol.script.SV;
import org.jmol.script.T;
import org.jmol.util.BSUtil;
import org.jmol.util.Edge;
import org.jmol.util.Logger;

import javajs.util.Measure;
import javajs.util.PT;
import javajs.util.SB;
import javajs.util.P3;
import javajs.util.P4;
import javajs.util.V3;
import org.jmol.viewer.JC;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolAdapterAtomIterator;
import org.jmol.api.JmolAdapterStructureIterator;
import org.jmol.api.JmolBioResolver;
import org.jmol.c.STR;

/**
 * a class used by ModelLoader only to handle all loading
 * of operations specific to PDB/mmCIF files. By loading
 * only by class name, only loaded if PDB file is called. 
 * 
 */
public final class Resolver implements JmolBioResolver {

  public Resolver() {
    // only implemented via reflection, and only for PDB/mmCIF files
  }
  
  @Override
  public Model getBioModel(int modelIndex,
                        int trajectoryBaseIndex, String jmolData,
                        Properties modelProperties,
                        Map<String, Object> modelAuxiliaryInfo) {
    return new BioModel(ms, modelIndex, trajectoryBaseIndex,
        jmolData, modelProperties, modelAuxiliaryInfo);
  }

  @Override
  public Group distinguishAndPropagateGroup(Chain chain, String group3,
                                            int seqcode, int firstAtomIndex,
                                            int maxAtomIndex, int modelIndex,
                                            int[] specialAtomIndexes,
                                            Atom[] atoms) {
    /*
     * called by finalizeGroupBuild()
     * 
     * first: build array of special atom names, for example "CA" for the alpha
     * carbon is assigned #2 see JmolConstants.specialAtomNames[] the special
     * atoms all have IDs based on Atom.lookupSpecialAtomID(atomName) these will
     * be the same for each conformation
     * 
     * second: creates the monomers themselves based on this information thus
     * building the byte offsets[] array for each monomer, indicating which
     * position relative to the first atom in the group is which atom. Each
     * monomer.offsets[i] then points to the specific atom of that type these
     * will NOT be the same for each conformation
     */

    int lastAtomIndex = maxAtomIndex - 1;

    int distinguishingBits = 0;

    // clear previous specialAtomIndexes
    for (int i = JC.ATOMID_MAX; --i >= 0;)
      specialAtomIndexes[i] = Integer.MIN_VALUE;

    // go last to first so that FIRST confirmation is default
    for (int i = maxAtomIndex; --i >= firstAtomIndex;) {
      int specialAtomID = atoms[i].getAtomID();
      if (specialAtomID <= 0)
        continue;
      if (specialAtomID < JC.ATOMID_DISTINGUISHING_ATOM_MAX) {
        /*
         * save for future option -- turns out the 1jsa bug was in relation to
         * an author using the same group number for two different groups
         * 
         * if ((distinguishingBits & (1 << specialAtomID) != 0) {
         * 
         * //bh 9/21/2006: //
         * "if the group has two of the same, that cannot be right." // Thus,
         * for example, two C's doth not make a protein "carbonyl C"
         * distinguishingBits = 0; break; }
         */
        distinguishingBits |= (1 << specialAtomID);
      }
      specialAtomIndexes[specialAtomID] = i;
    }

    if (lastAtomIndex < firstAtomIndex)
      throw new NullPointerException();

    Monomer m = null;
    if ((distinguishingBits & JC.ATOMID_PROTEIN_MASK) == JC.ATOMID_PROTEIN_MASK)
      m = AminoMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes, atoms);
    else if (distinguishingBits == JC.ATOMID_ALPHA_ONLY_MASK)
      m = AlphaMonomer.validateAndAllocateA(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (((distinguishingBits & JC.ATOMID_NUCLEIC_MASK) == JC.ATOMID_NUCLEIC_MASK))
      m = NucleicMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (distinguishingBits == JC.ATOMID_PHOSPHORUS_ONLY_MASK)
      m = PhosphorusMonomer.validateAndAllocateP(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (JC.checkCarbohydrate(group3))
      m = CarbohydrateMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex);
    return ( m != null && m.leadAtomIndex >= 0 ? m : null);
  }   
  
  //////////// ADDITION OF HYDROGEN ATOMS /////////////
  // Bob Hanson and Erik Wyatt, Jmol 12.1.51, 7/1/2011
  
  /*
   * for each group, as it is finished in the file reading:
   * 
   * 1) get and store atom/bond information for group type
   * 2) add placeholder (deleted) hydrogen atoms to a group
   * 
   * in the end:
   * 
   * 3) set multiple bonding and charges
   * 4) determine actual number of required hydrogen atoms
   * 5) set hydrogen atom names, atom numbers, and positions 
   * 6) undelete those atoms  
   * 
   */
  private ModelLoader ml;
  private ModelSet ms;
  private BS bsAddedHydrogens;
  private BS bsAddedMask;
  private BS bsAtomsForHs;
  private Map<String, String>htBondMap;
  private Map<String, Boolean>htGroupBonds;
  private String[] hNames;
  private int lastSetH = Integer.MIN_VALUE;
  private int maxSerial = 0;
  private int baseBondIndex = 0;
  private boolean haveHsAlready;
  
  @Override
  public void setHaveHsAlready(boolean b) {
    haveHsAlready = b;
  }

  private V3 vAB;
  private V3 vAC;
  private V3 vNorm;
  private P4 plane;

  @Override
  public void initialize(ModelLoader modelLoader) {
    this.ml = modelLoader;
    this.ms = modelLoader.ms;
  }
  
  @Override
  public void initializeHydrogenAddition() {
    baseBondIndex = ml.ms.bondCount;
    bsAddedHydrogens = new BS();
    bsAtomsForHs = new BS();
    htBondMap = new Hashtable<String, String>();
    htGroupBonds = new Hashtable<String, Boolean>();
    hNames = new String[3];
    vAB = new V3();
    vAC = new V3();
    vNorm = new V3();
    plane = new P4();
  }
  
  @Override
  public void addImplicitHydrogenAtoms(JmolAdapter adapter, int iGroup, int nH) {
    String group3 = ml.getGroup3(iGroup);
    int nH1;
    if (haveHsAlready || group3 == null
        || (nH1 = JC.getStandardPdbHydrogenCount(group3)) == 0)
      return;
    nH = (nH1 < 0 ? -1 : nH1 + nH);
    Object model = null;
    int iFirst = ml.getFirstAtomIndex(iGroup);
    int ac = ms.getAtomCount();
    if (nH < 0) {
      if (ac - iFirst == 1) // CA or P-only, or simple metals, also HOH, DOD
        return;
      model = ms.vwr.getLigandModel(group3, "ligand_", "_data", null);
      if (model == null)
        return;
      nH = adapter.getHydrogenAtomCount(model);
      if (nH < 1)
        return;
    }
    getBondInfo(adapter, group3, model);
    ms.am[ms.at[iFirst].mi].isPdbWithMultipleBonds = true;
    bsAtomsForHs.setBits(iFirst, ac);
    bsAddedHydrogens.setBits(ac, ac + nH);
    boolean isHetero = ms.at[iFirst].isHetero();
    P3 xyz = P3.new3(Float.NaN, Float.NaN, Float.NaN);
    Atom a = ms.at[iFirst];
    for (int i = 0; i < nH; i++)
      ms.addAtom(a.mi, a.group, 1, "H", 0, a.getSeqID(), 0, xyz,
          Float.NaN, null, 0, 0, 1, 0, null, isHetero, (byte) 0, null)
          .deleteBonds(null);
  }

  public void getBondInfo(JmolAdapter adapter, String group3, Object model) {
    if (htGroupBonds.get(group3) != null)
      return;
    String[][] bondInfo;
    if (model == null) {
      bondInfo = ms.vwr.getPdbBondInfo(group3);
    } else {
      bondInfo = getLigandBondInfo(adapter, model, group3);
    }
    if (bondInfo == null)
      return;
    htGroupBonds.put(group3, Boolean.TRUE);
    for (int i = 0; i < bondInfo.length; i++) {
      if (bondInfo[i] == null)
        continue;
      if (bondInfo[i][1].charAt(0) == 'H')
        htBondMap.put(group3 + "." + bondInfo[i][0], bondInfo[i][1]);
      else
        htBondMap.put(group3 + ":" + bondInfo[i][0] + ":" + bondInfo[i][1], bondInfo[i][2]);
    }
  }

  /**
   * reads PDB ligand CIF info and creates a bondInfo object.
   * 
   * @param adapter
   * @param model
   * @param group3 
   * @return      [[atom1, atom2, order]...]
   */
  private String[][] getLigandBondInfo(JmolAdapter adapter, Object model, String group3) {
    String[][] dataIn = adapter.getBondList(model);
    Map<String, P3> htAtoms = new Hashtable<String, P3>();
    JmolAdapterAtomIterator iterAtom = adapter.getAtomIterator(model);
    while (iterAtom.hasNext())
      htAtoms.put(iterAtom.getAtomName(), iterAtom.getXYZ());      
    String[][] bondInfo = new String[dataIn.length * 2][];
    int n = 0;
    for (int i = 0; i < dataIn.length; i++) {
      String[] b = dataIn[i];
      if (b[0].charAt(0) != 'H')
        bondInfo[n++] = new String[] { b[0], b[1], b[2],
            b[1].startsWith("H") ? "0" : "1" };
      if (b[1].charAt(0) != 'H')
        bondInfo[n++] = new String[] { b[1], b[0], b[2],
            b[0].startsWith("H") ? "0" : "1" };
    }
    Arrays.sort(bondInfo, new BondSorter());
    // now look for 
    String[] t;
    for (int i = 0; i < n;) {
      t = bondInfo[i];
      String a1 = t[0];
      int nH = 0;
      int nC = 0;
      for (; i < n && (t = bondInfo[i])[0].equals(a1); i++) {
        if (t[3].equals("0")) {
          nH++;
          continue;
        }
        if (t[3].equals("1"))
          nC++;
      }
      int pt = i - nH - nC;
      if (nH == 1)
        continue;
      switch (nC) {
      case 1:
        char sep = (nH == 2 ? '@' : '|');
        for (int j = 1; j < nH; j++) {
          bondInfo[pt][1] += sep + bondInfo[pt + j][1];
          bondInfo[pt + j] = null;
        }
        continue;
      case 2:
        if (nH != 2)
          continue;
        String name = bondInfo[pt][0];
        String name1 = bondInfo[pt + nH][1];
        String name2 = bondInfo[pt + nH + 1][1];
        int factor = name1.compareTo(name2);
        Measure.getPlaneThroughPoints(htAtoms.get(name1), htAtoms.get(name), htAtoms.get(name2), vNorm, vAB, vAC,
            plane);
        float d = Measure.distanceToPlane(plane, htAtoms.get(bondInfo[pt][1])) * factor;
        bondInfo[pt][1] = (d > 0 ? bondInfo[pt][1] + "@" + bondInfo[pt + 1][1]
            :  bondInfo[pt + 1][1] + "@" + bondInfo[pt][1]);
        bondInfo[pt + 1] = null;
      }
    }
    for (int i = 0; i < n; i++) {
      if ((t = bondInfo[i]) != null && t[1].charAt(0) != 'H' && t[0].compareTo(t[1]) > 0) {
        bondInfo[i] = null;
        continue;
      }
      if (t != null)
        Logger.info(" ligand " + group3 + ": " + bondInfo[i][0] + " - " + bondInfo[i][1] + " order " + bondInfo[i][2]);
    }
    return bondInfo;
  }
  
  protected class BondSorter implements Comparator<String[]>{
    @Override
    public int compare(String[] a, String[] b) {
      return (b == null ? (a == null ? 0 : -1) : a == null ? 1 : a[0]
          .compareTo(b[0]) < 0 ? -1 : a[0].compareTo(b[0]) > 0 ? 1 : a[3]
          .compareTo(b[3]) < 0 ? -1 : a[3].compareTo(b[3]) > 0 ? 1 : a[1]
          .compareTo(b[1]) < 0 ? -1 : a[1].compareTo(b[1]) > 0 ? 1 : 0);
    }
  }
  
  @Override
  public void finalizeHydrogens() {
    ms.vwr.getLigandModel(null, null, null, null);
    finalizePdbMultipleBonds();
    addHydrogens();
  }

  private void addHydrogens() {
    if (bsAddedHydrogens.nextSetBit(0) < 0)
      return;
    bsAddedMask = BSUtil.copy(bsAddedHydrogens);
    finalizePdbCharges();
    int[] nTotal = new int[1];
    P3[][] pts = ms.calculateHydrogens(bsAtomsForHs, nTotal, true, false,
        null);
    Group groupLast = null;
    int ipt = 0;
    for (int i = 0; i < pts.length; i++) {
      if (pts[i] == null)
        continue;
      Atom atom = ms.at[i];
      Group g = atom.group;
      if (g != groupLast) {
        groupLast = g;
        ipt = g.lastAtomIndex;
        while (bsAddedHydrogens.get(ipt))
          ipt--;
      }
      String gName = atom.getGroup3(false);
      String aName = atom.getAtomName();
      String hName = htBondMap.get(gName + "." + aName);
      if (hName == null)
        continue;
      boolean isChiral = hName.contains("@");
      boolean isMethyl = (hName.endsWith("?") || hName.indexOf("|") >= 0);
      int n = pts[i].length;
      if (n == 3 && !isMethyl && hName.equals("H@H2")) {
        hName = "H|H2|H3";
        isMethyl = true;
        isChiral = false;
      }
      if (isChiral && n == 3 || isMethyl != (n == 3)) {
        Logger.info("Error adding H atoms to " + gName + g.getResno() + ": "
            + pts[i].length + " atoms should not be added to " + aName);
        continue;
      }
      int pt = hName.indexOf("@");
      switch (pts[i].length) {
      case 1:
        if (pt > 0)
          hName = hName.substring(0, pt);
        setHydrogen(i, ++ipt, hName, pts[i][0]);
        break;
      case 2:
        String hName1,
        hName2;
        float d = -1;
        Bond[] bonds = atom.getBonds();
        if (bonds != null)
          switch (bonds.length) {
          case 2:
            // could be nitrogen?
            Atom atom1 = bonds[0].getOtherAtom(atom);
            Atom atom2 = bonds[1].getOtherAtom(atom);
            int factor = atom1.getAtomName().compareTo(atom2.getAtomName());
            Measure.getPlaneThroughPoints(atom1, atom, atom2, vNorm, vAB, vAC,
                plane);
            d = Measure.distanceToPlane(plane, pts[i][0]) * factor;
            break;
          }
        if (pt < 0) {
          Logger.info("Error adding H atoms to " + gName + g.getResno()
              + ": expected to only need 1 H but needed 2");
          hName1 = hName2 = "H";
        } else if (d < 0) {
          hName2 = hName.substring(0, pt);
          hName1 = hName.substring(pt + 1);
        } else {
          hName1 = hName.substring(0, pt);
          hName2 = hName.substring(pt + 1);
        }
        setHydrogen(i, ++ipt, hName1, pts[i][0]);
        setHydrogen(i, ++ipt, hName2, pts[i][1]);
        break;
      case 3:
        int pt1 = hName.indexOf('|');
        if (pt1 >= 0) {
          int pt2 = hName.lastIndexOf('|');
          hNames[0] = hName.substring(0, pt1);
          hNames[1] = hName.substring(pt1 + 1, pt2);
          hNames[2] = hName.substring(pt2 + 1);
        } else {
          hNames[0] = hName.replace('?', '1');
          hNames[1] = hName.replace('?', '2');
          hNames[2] = hName.replace('?', '3');
        }
        //          Measure.getPlaneThroughPoints(pts[i][0], pts[i][1], pts[i][2], vNorm, vAB,
        //            vAC, plane);
        //      d = Measure.distanceToPlane(plane, atom);
        //    int hpt = (d < 0 ? 1 : 2);
        setHydrogen(i, ++ipt, hNames[0], pts[i][0]);
        setHydrogen(i, ++ipt, hNames[1], pts[i][2]);
        setHydrogen(i, ++ipt, hNames[2], pts[i][1]);
        break;
      }
    }
    deleteUnneededAtoms();
    ms.fixFormalCharges(BSUtil.newBitSet2(ml.baseAtomIndex, ml.ms.ac));
  }

  /**
   * Delete hydrogen atoms that are still in bsAddedHydrogens, 
   * because they were not actually added.
   * Also delete ligand hydrogen atoms from CO2- and PO3(2-)
   * 
   * Note that we do this AFTER all atoms have been added. That means that
   * this operation will not mess up atom indexing
   * 
   */
  private void deleteUnneededAtoms() {
    BS bsBondsDeleted = new BS();
    for (int i = bsAtomsForHs.nextSetBit(0); i >= 0; i = bsAtomsForHs
        .nextSetBit(i + 1)) {
      Atom atom = ms.at[i];
      // specifically look for neutral HETATM O with a bond count of 2: 
      if (!atom.isHetero() || atom.getElementNumber() != 8 || atom.getFormalCharge() != 0
          || atom.getCovalentBondCount() != 2)
        continue;
      Bond[] bonds = atom.getBonds();
      Atom atom1 = bonds[0].getOtherAtom(atom);
      Atom atomH = bonds[1].getOtherAtom(atom);
      if (atom1.getElementNumber() == 1) {
        Atom a = atom1;
        atom1 = atomH;
        atomH = a;
      }
      
      // Does it have an H attached?
      if (atomH.getElementNumber() != 1)
        continue;
      // If so, does it have an attached atom that is doubly bonded to O?
      // so this could be RSO4H or RPO3H2 or RCO2H
      Bond[] bonds1 = atom1.getBonds();
      for (int j = 0; j < bonds1.length; j++) {
        if (bonds1[j].order == 2) {
          Atom atomO = bonds1[j].getOtherAtom(atom1);
          if (atomO.getElementNumber() == 8) {
            bsAddedHydrogens.set(atomH.i);
            atomH.deleteBonds(bsBondsDeleted);
            break;
          }
        }

      }
    }
    ms.deleteBonds(bsBondsDeleted, true);
    deleteAtoms(bsAddedHydrogens);
  }
  
  /**
   * called from org.jmol.modelsetbio.resolver when adding hydrogens.
   * 
   * @param bsDeletedAtoms
   */
  private void deleteAtoms(BS bsDeletedAtoms) {
    // get map
    int[] mapOldToNew = new int[ms.ac];
    int[] mapNewToOld = new int[ms.ac - bsDeletedAtoms.cardinality()];
    int n = ml.baseAtomIndex;
    Model[] models = ms.am;
    Atom[] atoms = ms.at;
    for (int i = ml.baseAtomIndex; i < ms.ac; i++) {
      models[atoms[i].mi].bsAtoms.clear(i);
      models[atoms[i].mi].bsAtomsDeleted.clear(i);
      if (bsDeletedAtoms.get(i)) {
        mapOldToNew[i] = n - 1;
        models[atoms[i].mi].ac--;
      } else {
        mapNewToOld[n] = i;
        mapOldToNew[i] = n++;
      }
    }
    ms.setMSInfo("bsDeletedAtoms", bsDeletedAtoms);
    // adjust group pointers
    for (int i = ml.baseGroupIndex; i < ml.groups.length; i++) {
      Group g = ml.groups[i];
      if (g.firstAtomIndex >= ml.baseAtomIndex) {
        g.firstAtomIndex = mapOldToNew[g.firstAtomIndex];
        g.lastAtomIndex = mapOldToNew[g.lastAtomIndex];
        if (g.leadAtomIndex >= 0)
          g.leadAtomIndex = mapOldToNew[g.leadAtomIndex];
      }
    }
    // adjust atom arrays
    ms.adjustAtomArrays(mapNewToOld, ml.baseAtomIndex, n);
    ms.calcBoundBoxDimensions(null, 1);
    ms.resetMolecules();
    ms.validateBspf(false);
    bsAddedMask = BSUtil.deleteBits(bsAddedMask, bsDeletedAtoms);
    System.out.println("res bsAddedMask = " + bsAddedMask);
    for (int i = ml.baseModelIndex; i < ml.ms.mc; i++) {
      fixAnnotations(i, "domains", T.domains);
      fixAnnotations(i, "validation", T.validation);
    }
  }

  private void fixAnnotations(int i, String name, int type) {
    Object o = ml.ms.getInfo(i, name);
    if (o != null) {
      Object dbObj = ml.ms.getCachedAnnotationMap(i, name, o);
      if (dbObj != null)
        ml.ms.vwr.getAnnotationParser().fixAtoms(i, (SV) dbObj, bsAddedMask, type, 20);
    }
  }

  private void finalizePdbCharges() {
    Atom[] atoms = ms.at;
    // fix terminal N groups as +1
    for (int i = bsAtomsForHs.nextSetBit(0); i >= 0; i = bsAtomsForHs.nextSetBit(i + 1)) {
      Atom a = atoms[i];
      if (a.group.getNitrogenAtom() == a && a.getCovalentBondCount() == 1)
        a.setFormalCharge(1);
      if ((i = bsAtomsForHs.nextClearBit(i + 1)) < 0)
        break;
    }
  }
  
  private void finalizePdbMultipleBonds() {
    Map<String, Boolean> htKeysUsed = new Hashtable<String, Boolean>();
    int bondCount = ms.bondCount;
    Bond[] bonds = ms.bo;
    for (int i = baseBondIndex; i < bondCount; i++) {
      Atom a1 = bonds[i].getAtom1();
      Atom a2 = bonds[i].getAtom2();
      Group g = a1.group;
      if (g != a2.group)
        continue;
      SB key = new SB().append(g.getGroup3());
      key.append(":");
      String n1 = a1.getAtomName();
      String n2 = a2.getAtomName();
      if (n1.compareTo(n2) > 0)
        key.append(n2).append(":").append(n1);
      else
        key.append(n1).append(":").append(n2);
      String skey = key.toString();
      String type = htBondMap.get(skey);
      if (type == null)
        continue;
      htKeysUsed.put(skey, Boolean.TRUE);
      bonds[i].setOrder(PT.parseInt(type));
    }

    for (String key : htBondMap.keySet()) {
      if (htKeysUsed.get(key) != null)
        continue;
      if (key.indexOf(":") < 0) {
        htKeysUsed.put(key, Boolean.TRUE);
        continue;
      }
      String value = htBondMap.get(key);
      Logger.info("bond " + key + " was not used; order=" + value);
      if (htBondMap.get(key).equals("1")) {
        htKeysUsed.put(key, Boolean.TRUE);
        continue; // that's ok
      }
    }
    Map<String, String> htKeysBad = new Hashtable<String, String>();
    for (String key : htBondMap.keySet()) {
      if (htKeysUsed.get(key) != null)
        continue;
      htKeysBad.put(key.substring(0, key.lastIndexOf(":")), htBondMap.get(key));
    }
    if (htKeysBad.isEmpty())
      return;
    for (int i = 0; i < bondCount; i++) {
      Atom a1 = bonds[i].getAtom1();
      Atom a2 = bonds[i].getAtom2();
      if (a1.group == a2.group)
        continue;
      String value;
      if ((value = htKeysBad.get(a1.getGroup3(false) + ":" + a1.getAtomName())) == null
          && ((value = htKeysBad.get(a2.getGroup3(false) + ":" + a2.getAtomName())) == null))
        continue;
      bonds[i].setOrder(PT.parseInt(value));
      Logger.info("assigning order " + bonds[i].order + " to bond " + bonds[i]);
    }
  }

  private void setHydrogen(int iTo, int iAtom, String name, P3 pt) {
    if (!bsAddedHydrogens.get(iAtom))
      return;
    Atom[] atoms = ms.at;
    if (lastSetH == Integer.MIN_VALUE || atoms[iAtom].mi != atoms[lastSetH].mi) 
      maxSerial = ((int[]) ms.getInfo(atoms[lastSetH = iAtom].mi, "PDB_CONECT_firstAtom_count_max"))[2];
    bsAddedHydrogens.clear(iAtom);
    ms.setAtomName(iAtom, name);
    atoms[iAtom].setT(pt);
    ms.setAtomNumber(iAtom, ++maxSerial);
    atoms[iAtom].setAtomSymmetry(atoms[iTo].getAtomSymmetry());
    ml.undeleteAtom(iAtom);

    ms.bondAtoms(atoms[iTo], atoms[iAtom], Edge.BOND_COVALENT_SINGLE, 
        ms.getDefaultMadFromOrder(Edge.BOND_COVALENT_SINGLE), null, 0, true, false);
  }

  @Override
  public Object fixPropertyValue(BS bsAtoms, Object data, boolean toHydrogens) {
    Atom[] atoms = ms.at;
    // we aren't doing this anymore
    // it was for TLS groups
//    if (data instanceof String) {
//      String[] sData = PT.split((String) data, "\n");
//      String[] newData = new String[bsAtoms.cardinality()];
//      String lastData = "";
//      for (int pt = 0, iAtom = 0, i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms
//          .nextSetBit(i + 1), iAtom++) {
//        if (atoms[i].getElementNumber() == 1) {
//          if (!toHydrogens)
//            continue;
//        } else {
//          lastData = sData[pt++];
//        }
//        newData[iAtom] = lastData;
//      }
//      return PT.join(newData, '\n', 0);
//    }
    // already float data
    float[] fData = (float[]) data;
    float[] newData = new float[bsAtoms.cardinality()];
    float lastData = 0;
    for (int pt = 0, iAtom = 0, i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms
        .nextSetBit(i + 1), iAtom++) {
      if (atoms[i].getElementNumber() == 1) {
        if (!toHydrogens)
          continue;
      } else {
        lastData = fData[pt++];
      }
      newData[iAtom] = lastData;
    }
    return newData;
  }

  static BioPolymer allocateBioPolymer(Group[] groups, int firstGroupIndex,
                                       boolean checkConnections) {
    Monomer previous = null;
    int count = 0;
    for (int i = firstGroupIndex; i < groups.length; ++i) {
      Group group = groups[i];
      Monomer current;
      if (!(group instanceof Monomer)
          || (current = (Monomer) group).bioPolymer != null || previous != null
          && previous.getClass() != current.getClass() || checkConnections
          && !current.isConnectedAfter(previous))
        break;
      previous = current;
      count++;
    }
    if (count == 0)
      return null;
    Monomer[] monomers = new Monomer[count];
    for (int j = 0; j < count; ++j)
      monomers[j] = (Monomer) groups[firstGroupIndex + j];
    if (previous instanceof AminoMonomer)
      return new AminoPolymer(monomers);
    if (previous instanceof AlphaMonomer)
      return new AlphaPolymer(monomers);
    if (previous instanceof NucleicMonomer)
      return new NucleicPolymer(monomers);
    if (previous instanceof PhosphorusMonomer)
      return new PhosphorusPolymer(monomers);
    if (previous instanceof CarbohydrateMonomer)
      return new CarbohydratePolymer(monomers);
    Logger
        .error("Polymer.allocatePolymer() ... no matching polymer for monomor "
            + previous);
    throw new NullPointerException();
  }
  
  private BS bsAssigned;

  /**
   * Pull in all spans of helix, etc. in the file(s)
   * 
   * We do turn first, because sometimes a group is defined twice, and this way
   * it gets marked as helix or sheet if it is both one of those and turn.
   * 
   * Jmol 14.3 - adds sequence ANNOTATION
   *  
   * @param adapter
   * @param atomSetCollection
   */
  @Override
  public void iterateOverAllNewStructures(JmolAdapter adapter,
                                          Object atomSetCollection) {
    JmolAdapterStructureIterator iterStructure = adapter
        .getStructureIterator(atomSetCollection);
    if (iterStructure == null)
      return;
    BS bs = iterStructure.getStructuredModels();
    if (bs != null)
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
        ml.structuresDefinedInFile.set(ml.baseModelIndex + i);
    while (iterStructure.hasNext())
      if (iterStructure.getStructureType() != STR.TURN)
        setStructure(iterStructure);

    // define turns LAST. (pulled by the iterator first)
    // so that if they overlap they get overwritten:

    iterStructure = adapter.getStructureIterator(atomSetCollection);
    while (iterStructure.hasNext())
      if (iterStructure.getStructureType() == STR.TURN)
        setStructure(iterStructure);
  }

  /**
   * note that istart and iend will be adjusted. 
   * 
   * @param iterStructure
   */
  private void setStructure(JmolAdapterStructureIterator iterStructure) {
    STR t = iterStructure.getSubstructureType();
    String id = iterStructure.getStructureID();
    int serID = iterStructure.getSerialID();
    int count = iterStructure.getStrandCount();
    int[] atomRange = iterStructure.getAtomIndices();
    int[] modelRange = iterStructure.getModelIndices();
    if (bsAssigned == null)
      bsAssigned = new BS();
    defineStructure(t, id, serID, count, iterStructure.getStartChainID(),
          iterStructure.getStartSequenceNumber(), iterStructure
              .getStartInsertionCode(), iterStructure.getEndChainID(),
          iterStructure.getEndSequenceNumber(), iterStructure
              .getEndInsertionCode(), atomRange, modelRange, bsAssigned);
  }

  private void defineStructure(STR subType, String structureID,
                               int serialID, int strandCount, int startChainID,
                               int startSequenceNumber,
                               char startInsertionCode, int endChainID,
                               int endSequenceNumber, char endInsertionCode,
                               int[] atomRange, int[] modelRange, BS bsAssigned) {
    STR type = (subType == STR.NOT ? STR.NONE
        : subType);
    int startSeqCode = Group.getSeqcodeFor(startSequenceNumber,
        startInsertionCode);
    int endSeqCode = Group.getSeqcodeFor(endSequenceNumber, endInsertionCode);
    Model[] models = ms.am;
    if (ml.isTrajectory) { //from PDB file
      modelRange[1] = modelRange[0];
    } else {
      modelRange[0] += ml.baseModelIndex;
      modelRange[1] += ml.baseModelIndex;
    }
    ml.structuresDefinedInFile.setBits(modelRange[0],
        modelRange[1] + 1);
    for (int i = modelRange[0]; i <= modelRange[1]; i++) {
      int i0 = models[i].firstAtomIndex;
      if (models[i] instanceof BioModel)
        ((BioModel) models[i]).addSecondaryStructure(type, structureID,
            serialID, strandCount, startChainID, startSeqCode, endChainID,
            endSeqCode, i0 + atomRange[0], i0 + atomRange[1], bsAssigned);
    }
  }
}


