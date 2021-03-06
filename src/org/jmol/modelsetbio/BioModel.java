/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-08-05 21:10:46 -0500 (Fri, 05 Aug 2011) $
 * $Revision: 15943 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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

import javajs.util.AU;
import javajs.util.OC;
import javajs.util.Lst;
import javajs.util.PT;
import javajs.util.SB;

import java.util.Hashtable;

import java.util.Map;
import java.util.Properties;


import org.jmol.api.DSSPInterface;
import org.jmol.api.Interface;
import org.jmol.c.STR;
import org.jmol.java.BS;
import org.jmol.modelset.Atom;
import org.jmol.modelset.AtomCollection;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;
import org.jmol.modelset.HBond;
import org.jmol.modelset.LabelToken;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.jmol.script.T;
import org.jmol.util.BSUtil;
import org.jmol.util.Escape;
import org.jmol.util.Edge;
import javajs.util.P3;


import org.jmol.viewer.JC;
import org.jmol.viewer.Viewer;


public final class BioModel extends Model{

  /*
   *   
   * Note that "monomer" extends group. A group only becomes a 
   * monomer if it can be identified as one of the following 
   * PDB/mmCIF types:
   * 
   *   amino  -- has an N, a C, and a CA
   *   alpha  -- has just a CA
   *   nucleic -- has C1',C2',C3',C4',C5',O3', and O5'
   *   phosphorus -- has P
   *   
   * The term "conformation" is a bit loose. It means "what you get
   * when you go with one or another set of alternative locations.
   *
   *  
   */
  
  private int bioPolymerCount = 0;
  private BioPolymer[] bioPolymers;

  BioModel(ModelSet modelSet, int modelIndex, int trajectoryBaseIndex, 
      String jmolData, Properties properties, Map<String, Object> auxiliaryInfo) {
    set(modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo);
    isBioModel = true;
    clearBioPolymers();
  }

  @Override
  public void freeze() {
    freezeM();
    bioPolymers = (BioPolymer[])AU.arrayCopyObject(bioPolymers, bioPolymerCount);
  }
  
  public void addSecondaryStructure(STR type, String structureID,
                                    int serialID, int strandCount,
                                    int startChainID, int startSeqcode,
                                    int endChainID, int endSeqcode, int istart,
                                    int iend, BS bsAssigned) {
    for (int i = bioPolymerCount; --i >= 0;)
      if (bioPolymers[i] instanceof AlphaPolymer)
        ((AlphaPolymer) bioPolymers[i]).addStructure(type, structureID,
            serialID, strandCount, startChainID, startSeqcode, endChainID,
            endSeqcode, istart, iend, bsAssigned);
  }

  @Override
  public String calculateStructures(boolean asDSSP, boolean doReport,
                                    boolean dsspIgnoreHydrogen,
                                    boolean setStructure, boolean includeAlpha) {
    if (bioPolymerCount == 0 || !setStructure && !asDSSP)
      return "";
    ms.proteinStructureTainted = structureTainted = true;
    if (setStructure)
      for (int i = bioPolymerCount; --i >= 0;)
        if (!asDSSP || bioPolymers[i].getGroups()[0].getNitrogenAtom() != null)
          bioPolymers[i].clearStructures();
    if (!asDSSP || includeAlpha)
      for (int i = bioPolymerCount; --i >= 0;)
        if (bioPolymers[i] instanceof AlphaPolymer)
          ((AlphaPolymer) bioPolymers[i]).calculateStructures(includeAlpha);
    return (asDSSP ? calculateDssx(null, doReport, dsspIgnoreHydrogen, setStructure) : "");
  }
  
  private String calculateDssx(Lst<Bond> vHBonds, boolean doReport,
                               boolean dsspIgnoreHydrogen, boolean setStructure) {
    boolean haveProt = false;
    boolean haveNucl = false;
    for (int i = 0; i < bioPolymerCount && !(haveProt && haveNucl); i++) {
      if (bioPolymers[i].isNucleic())
        haveNucl = true;
      else if (bioPolymers[i] instanceof AminoPolymer)
        haveProt = true;
    }
    
    String s = "";
    if (haveProt)
      s += ((DSSPInterface) Interface.getOption("dssx.DSSP", ms.vwr, "ms"))
        .calculateDssp(bioPolymers, bioPolymerCount, vHBonds, doReport,
            dsspIgnoreHydrogen, setStructure);
    if (haveNucl && auxiliaryInfo.containsKey("dssr") && vHBonds != null)
      s += ms.vwr.getAnnotationParser().getHBonds(ms, modelIndex, vHBonds, doReport);
    return s;
  }

  @Override
  public void setConformation(BS bsConformation) {
    if (nAltLocs > 0)
      for (int i = bioPolymerCount; --i >= 0; )
        bioPolymers[i].setConformation(bsConformation);
  }

  @Override
  public boolean getPdbConformation(BS bsConformation, int conformationIndex) {
    if (nAltLocs > 0)
      for (int i = bioPolymerCount; --i >= 0;)
        bioPolymers[i].getConformation(bsConformation, conformationIndex);
    return true;
  }

  @Override
  public int getBioPolymerCount() {
    return bioPolymerCount;
  }

  @Override
  public void calcSelectedMonomersCount(BS bsSelected) {
    for (int i = bioPolymerCount; --i >= 0; )
      bioPolymers[i].calcSelectedMonomersCount(bsSelected);
  }

  public BioPolymer getBioPolymer(int polymerIndex) {
    return bioPolymers[polymerIndex];
  }

  @Override
  public void getDefaultLargePDBRendering(SB sb, int maxAtoms) {
    BS bs = new BS();
    if (getBondCount() == 0)
      bs = bsAtoms;
    // all biopolymer atoms...
    if (bs != bsAtoms)
      for (int i = 0; i < bioPolymerCount; i++)
        bioPolymers[i].getRange(bs);
    if (bs.nextSetBit(0) < 0)
      return;
    // ...and not connected to backbone:
    BS bs2 = new BS();
    if (bs == bsAtoms) {
      bs2 = bs;
    } else {
      for (int i = 0; i < bioPolymerCount; i++)
        if (bioPolymers[i].getType() == BioPolymer.TYPE_NOBONDING)
          bioPolymers[i].getRange(bs2);
    }
    if (bs2.nextSetBit(0) >= 0)
      sb.append("select ").append(Escape.eBS(bs2)).append(";backbone only;");
    if (ac <= maxAtoms)
      return;
    // ...and it's a large model, to wireframe:
      sb.append("select ").append(Escape.eBS(bs)).append(" & connected; wireframe only;");
    // ... and all non-biopolymer and not connected to stars...
    if (bs != bsAtoms) {
      bs2.clearAll();
      bs2.or(bsAtoms);
      bs2.andNot(bs);
      if (bs2.nextSetBit(0) >= 0)
        sb.append("select " + Escape.eBS(bs2) + " & !connected;stars 0.5;spacefill off;");
    }
  }
  
  @Override
  public void fixIndices(int modelIndex, int nAtomsDeleted, BS bsDeleted) {
    fixIndicesM(modelIndex, nAtomsDeleted, bsDeleted);
    for (int i = 0; i < bioPolymerCount; i++)
      bioPolymers[i].recalculateLeadMidpointsAndWingVectors();
  }

  @Override
  public int calculateStruts(ModelSet modelSet, BS bs1, BS bs2) {

    // only check the atoms in THIS model
    Lst<Atom> vCA = new  Lst<Atom>();
    Atom a1 = null;
    BS bsCheck;
    if (bs1.equals(bs2)) {
      bsCheck = bs1;
    } else {
      bsCheck = BSUtil.copy(bs1);
      bsCheck.or(bs2);
    }
    Atom[] atoms = modelSet.at;
    Viewer vwr = modelSet.vwr;
    bsCheck.and(vwr.getModelUndeletedAtomsBitSet(modelIndex));
    for (int i = bsCheck.nextSetBit(0); i >= 0; i = bsCheck.nextSetBit(i + 1))
      if (atoms[i].checkVisible()
          && atoms[i].atomID == JC.ATOMID_ALPHA_CARBON
          && atoms[i].getGroupID() != JC.GROUPID_CYSTEINE)
        vCA.addLast((a1 = atoms[i]));
    if (vCA.size() == 0)
      return 0;    
    float thresh = vwr.getFloat(T.strutlengthmaximum);
    short mad = (short) (vwr.getFloat(T.strutdefaultradius) * 2000);
    int delta = vwr.getInt(T.strutspacing);
    boolean strutsMultiple = vwr.getBoolean(T.strutsmultiple);
    Lst<Atom[]> struts = getBioPolymer(a1.getPolymerIndexInModel())
        .calculateStruts(modelSet, bs1, bs2, vCA, thresh, delta, strutsMultiple);
    for (int i = 0; i < struts.size(); i++) {
      Atom[] o = struts.get(i);
      modelSet.bondAtoms(o[0], o[1], Edge.BOND_STRUT, mad, null, 0, false, true);
    }
    return struts.size(); 
  }
  
  @Override
  public void setStructureList(Map<STR, float[]> structureList) {
    bioPolymers = (BioPolymer[])AU.arrayCopyObject(bioPolymers, bioPolymerCount);
    for (int i = bioPolymerCount; --i >= 0; )
      bioPolymers[i].setStructureList(structureList);
  }

  @Override
  public void calculateStraightness(Viewer vwr, char ctype, char qtype,
                                    int mStep) {
    P3 ptTemp = new P3();
    for (int p = 0; p < bioPolymerCount; p++)
      bioPolymers[p].getPdbData(vwr, ctype, qtype, mStep, 2, null, 
          null, false, false, false, null, null, null, new BS(), ptTemp);
  }
  
  
  @Override
  public void getPolymerPointsAndVectors(BS bs, Lst<P3[]> vList,
                                         boolean isTraceAlpha,
                                         float sheetSmoothing) {
    int last = Integer.MAX_VALUE - 1;
    for (int ip = 0; ip < bioPolymerCount; ip++)
      last = bioPolymers[ip]
          .getPolymerPointsAndVectors(last, bs, vList, isTraceAlpha, sheetSmoothing);
  }

  @Override
  public P3[] getPolymerLeadMidPoints(int iPolymer) {
    return bioPolymers[iPolymer].getLeadMidpoints();
  }

  @Override
  public void recalculateLeadMidpointsAndWingVectors() {
    for (int ip = 0; ip < bioPolymerCount; ip++)
      bioPolymers[ip].recalculateLeadMidpointsAndWingVectors();
  }

  
  @Override
  public Lst<BS> getBioBranches(Lst<BS> biobranches) {
    // scan through biopolymers quickly -- 
    BS bsBranch;
    for (int j = 0; j < bioPolymerCount; j++) {
      bsBranch = new BS();
      bioPolymers[j].getRange(bsBranch);
      int iAtom = bsBranch.nextSetBit(0);
      if (iAtom >= 0) {
        if (biobranches == null)
          biobranches = new  Lst<BS>();
        biobranches.addLast(bsBranch);
      }
    }
    return biobranches;
  }

  @Override
  public void getGroupsWithin(int nResidues, BS bs, BS bsResult) {
    for (int i = bioPolymerCount; --i >= 0;)
      bioPolymers[i].getRangeGroups(nResidues, bs, bsResult);
  }
  
  @Override
  public void getSequenceBits(String specInfo, BS bs, BS bsResult) {
    int lenInfo = specInfo.length();
    for (int ip = 0; ip < bioPolymerCount; ip++) {
      String sequence = bioPolymers[ip].getSequence();
      int j = -1;
      while ((j = sequence.indexOf(specInfo, ++j)) >=0)
        bioPolymers[ip].getPolymerSequenceAtoms(j, lenInfo, bs, bsResult);
    }
  }

  @Override
  public void selectSeqcodeRange(int seqcodeA, int seqcodeB, int chainID,
                                 BS bs, boolean caseSensitive) {
    int id;
    for (int i = chainCount; --i >= 0;) {
      Chain chain = chains[i];
      if (chainID == -1 
          || chainID == (id = chain.chainID) 
          || !caseSensitive && id > 0 && id < 300 && chainID == AtomCollection.chainToUpper(id))
        for (int index = 0; index >= 0;)
          index = chains[i].selectSeqcodeRange(index, seqcodeA, seqcodeB, bs);
    }
  }

  @Override
  public void getRasmolHydrogenBonds(BS bsA, BS bsB,
                                     Lst<Bond> vHBonds, boolean nucleicOnly,
                                     int nMax, boolean dsspIgnoreHydrogens,
                                     BS bsHBonds) {    
    boolean doAdd = (vHBonds == null);
    if (doAdd)
      vHBonds = new  Lst<Bond>();
    if (nMax < 0)
      nMax = Integer.MAX_VALUE;
    boolean asDSSX = (bsB == null);
    BioPolymer bp, bp1;
    if (asDSSX && bioPolymerCount > 0) {
      
      calculateDssx(vHBonds, false, dsspIgnoreHydrogens, false);
      
    } else {
      for (int i = bioPolymerCount; --i >= 0;) {
        bp = bioPolymers[i];
        int type = bp.getType();
        if ((nucleicOnly || type != BioPolymer.TYPE_AMINO)
            && type != BioPolymer.TYPE_NUCLEIC)
          continue;
        boolean isRNA = bp.isRna();
        boolean isAmino = (type == BioPolymer.TYPE_AMINO);
        if (isAmino)
          bp.calcRasmolHydrogenBonds(null, bsA, bsB, vHBonds, nMax, null, true,
              false);
        for (int j = bioPolymerCount; --j >= 0;) {
          if ((bp1 = bioPolymers[j]) != null && (isRNA || i != j)
              && type == bp1.getType()) {
            bp1.calcRasmolHydrogenBonds(bp, bsA, bsB, vHBonds, nMax, null,
                true, false);
          }
        }
      }
    }
    
    if (vHBonds.size() == 0 || !doAdd)
      return;
    hasRasmolHBonds = true;
    for (int i = 0; i < vHBonds.size(); i++) {
      HBond bond = (HBond) vHBonds.get(i);
      Atom atom1 = bond.getAtom1();
      Atom atom2 = bond.getAtom2();
      if (atom1.isBonded(atom2))
        continue;
      int index = ms.addHBond(atom1, atom2, bond.order, bond.getEnergy());
      if (bsHBonds != null)
        bsHBonds.set(index);
    }
  }

  @Override
  public void clearRasmolHydrogenBonds(BS bsAtoms) {
    //called by calcRasmolHydrogenBonds (bsAtoms not null) from autoHBond
    //      and setAtomPositions (bsAtoms null)
    BS bsDelete = new BS();
    hasRasmolHBonds = false;
    Model[] models = ms.am;
    Bond[] bonds = ms.bo;
    for (int i = ms.bondCount; --i >= 0;) {
      Bond bond = bonds[i];
      Atom atom1 = bond.getAtom1();
      Model m = models[atom1.mi];
      if (!m.isBioModel || m.trajectoryBaseIndex != modelIndex
          || (bond.order & Edge.BOND_H_CALC_MASK) == 0)
        continue;
      if (bsAtoms != null && !bsAtoms.get(atom1.i)) {
        hasRasmolHBonds = true;
        continue;
      }
      bsDelete.set(i);
    }
    if (bsDelete.nextSetBit(0) >= 0)
      ms.deleteBonds(bsDelete, false);
  }

  @Override
  public void calculatePolymers(Group[] groups, int groupCount,
                                int baseGroupIndex, BS modelsExcluded, 
                                boolean checkConnections) {
    if (groups == null) {
      groups = ms.getGroups();
      groupCount = groups.length;
    }
    if (modelsExcluded != null)
      for (int i = 0; i < groupCount; ++i) {
        Group group = groups[i];
        if (group instanceof Monomer) {
          Monomer monomer = (Monomer) group;
          if (monomer.bioPolymer != null
              && (!modelsExcluded.get(monomer.getModelIndex())))
            monomer.setBioPolymer(null, -1);
        }
      }
    for (int i = baseGroupIndex; i < groupCount; ++i) {
      Group g = groups[i];
      Model model = g.getModel();
      if (!model.isBioModel || ! (g instanceof Monomer))
        continue;
      boolean doCheck = checkConnections 
        && !ms.isJmolDataFrameForModel(ms.at[g.firstAtomIndex].mi);
      BioPolymer bp = (((Monomer) g).bioPolymer == null ?
          Resolver.allocateBioPolymer(groups, i, doCheck) : null);
      if (bp == null || bp.monomerCount == 0)
        continue;
      ((BioModel) model).addBioPolymer(bp);
      i += bp.monomerCount - 1;
    }
  }  

  private void addBioPolymer(BioPolymer polymer) {
    if (bioPolymers.length == 0)
      clearBioPolymers();
    if (bioPolymerCount == bioPolymers.length)
      bioPolymers = (BioPolymer[])AU.doubleLength(bioPolymers);
    polymer.bioPolymerIndexInModel = bioPolymerCount;
    bioPolymers[bioPolymerCount++] = polymer;
  }

  @Override
  public void clearBioPolymers() {
    bioPolymers = new BioPolymer[8];
    bioPolymerCount = 0;
  }

  @Override
  public void getAllPolymerInfo(
                                BS bs,
                                Map<String, Lst<Map<String, Object>>> finalInfo,
                                Lst<Map<String, Object>> modelVector) {
    Map<String, Object> modelInfo = new Hashtable<String, Object>();
    Lst<Map<String, Object>> info = new  Lst<Map<String, Object>>();
    for (int ip = 0; ip < bioPolymerCount; ip++) {
      Map<String, Object> polyInfo = bioPolymers[ip].getPolymerInfo(bs); 
      if (!polyInfo.isEmpty())
        info.addLast(polyInfo);
    }
    if (info.size() > 0) {
      modelInfo.put("modelIndex", Integer.valueOf(modelIndex));
      modelInfo.put("polymers", info);
      modelVector.addLast(modelInfo);
    }
  }
  
  @SuppressWarnings("incomplete-switch")
  @Override
  public void getChimeInfo(SB sb, int nHetero) {
    int n = 0;
    Model[] models = ms.am;
    int modelCount = ms.mc;
    int ac = ms.getAtomCount();
    Atom[] atoms = ms.at;
    sb.append("\nMolecule name ....... "
        + ms.getInfoM("COMPND"));
    sb.append("\nSecondary Structure . PDB Data Records");
    sb.append("\nBrookhaven Code ..... " + ms.modelSetName);
    for (int i = modelCount; --i >= 0;)
      n += models[i].getChainCount(false);
    sb.append("\nNumber of Chains .... " + n);
    n = 0;
    for (int i = modelCount; --i >= 0;)
      n += models[i].getGroupCountHetero(false);
    nHetero = 0;
    for (int i = modelCount; --i >= 0;)
      nHetero += models[i].getGroupCountHetero(true);
    sb.append("\nNumber of Groups .... " + n);
    if (nHetero > 0)
      sb.append(" (" + nHetero + ")");
    for (int i = ac; --i >= 0;)
      if (atoms[i].isHetero())
        nHetero++;
    getChimeInfoM(sb, nHetero);
    int nH = 0;
    int nS = 0;
    int nT = 0;
    int id;
    int lastid = -1;
    for (int i = 0; i < ac; i++) {
      if (atoms[i].mi != 0)
        break;
      if ((id = atoms[i].getStrucNo()) != lastid && id != 0) {
        lastid = id;
        switch (atoms[i].getProteinStructureType()) {
        case HELIX:
          nH++;
          break;
        case SHEET:
          nS++;
          break;
        case TURN:
          nT++;
          break;
        }
      }
    }
    sb.append("\nNumber of Helices ... " + nH);
    sb.append("\nNumber of Strands ... " + nS);
    sb.append("\nNumber of Turns ..... " + nT);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public String getProteinStructureState(BS bsAtoms, boolean taintedOnly,
                                         boolean needPhiPsi, int mode) {
    boolean showMode = (mode == 3);
    boolean pdbFileMode = (mode == 1);
    boolean scriptMode = (mode == 0);
    BS bs = null;
    SB cmd = new SB();
    SB sbTurn = new SB();
    SB sbHelix = new SB();
    SB sbSheet = new SB();
    STR type = STR.NONE;
    STR subtype = STR.NONE;
    int id = 0;
    int iLastAtom = 0;
    int iLastModel = -1;
    int lastId = -1;
    int res1 = 0;
    int res2 = 0;
    String sid = "";
    String group1 = "";
    String group2 = "";
    String chain1 = "";
    String chain2 = "";
    int n = 0;
    int nHelix = 0;
    int nTurn = 0;
    int nSheet = 0;
    BS bsTainted = null;
    Model[] models = ms.am;
    Atom[] atoms = ms.at;
    int ac = ms.getAtomCount();
    
    if (taintedOnly) {
      if (!ms.proteinStructureTainted)
        return "";
      bsTainted = new BS();
      for (int i = firstAtomIndex; i < ac; i++)
        if (models[atoms[i].mi].isStructureTainted())
          bsTainted.set(i);
      bsTainted.set(ac);
    }
    for (int i = 0; i <= ac; i++)
      if (i == ac || bsAtoms == null || bsAtoms.get(i)) {
        if (taintedOnly && !bsTainted.get(i))
          continue;
        id = 0;
        if (i == ac || (id = atoms[i].getStrucNo()) != lastId) {
          if (bs != null) {
            switch  (type) {
            case HELIX:
            case TURN:
            case SHEET:
              n++;
              if (scriptMode) {
                int iModel = atoms[iLastAtom].mi;
                String comment = "    \t# model="
                    + ms.getModelNumberDotted(iModel);
                if (iLastModel != iModel) {
                  iLastModel = iModel;
                    cmd.append("  structure none ").append(
                        Escape.eBS(ms.getModelAtomBitSetIncludingDeleted(
                            iModel, false))).append(comment).append(";\n");
                }
                comment += " & (" + res1 + " - " + res2 + ")";
                String stype = subtype.getBioStructureTypeName(false);
                  cmd.append("  structure ").append(stype).append(" ").append(
                      Escape.eBS(bs)).append(comment).append(";\n");
              } else {
                String str;
                int nx;
                SB sb;
                // NNN III GGG C RRRR GGG C RRRR
                // HELIX 99 99 LYS F 281 LEU F 293 1
                // NNN III 2 GGG CRRRR GGG CRRRR
                // SHEET 1 A 8 ILE A 43 ASP A 45 0
                // NNN III GGG CRRRR GGG CRRRR
                // TURN 1 T1 PRO A 41 TYR A 44
                switch (type) {
                case HELIX:
                  nx = ++nHelix;
                  if (sid == null || pdbFileMode)
                    sid = PT.formatStringI("%3N %3N", "N", nx);
                  str = "HELIX  %ID %3GROUPA %1CA %4RESA  %3GROUPB %1CB %4RESB";
                  sb = sbHelix;
                  String stype = null;
                  switch (subtype) {
                  case HELIX:
                  case HELIXALPHA:
                    stype = "  1";
                    break;
                  case HELIX310:
                    stype = "  5";
                    break;
                  case HELIXPI:
                    stype = "  3";
                    break;
                  }
                  if (stype != null)
                    str += stype;
                  break;
                case SHEET:
                  nx = ++nSheet;
                  if (sid == null || pdbFileMode) {
                    sid = PT.formatStringI("%3N %3A 0", "N", nx);
                    sid = PT.formatStringS(sid, "A", "S" + nx);
                  }
                  str = "SHEET  %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
                  sb = sbSheet;
                  break;
                case TURN:
                default:
                  nx = ++nTurn;
                  if (sid == null || pdbFileMode)
                    sid = PT.formatStringI("%3N %3N", "N", nx);
                  str = "TURN   %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
                  sb = sbTurn;
                  break;
                }
                str = PT.formatStringS(str, "ID", sid);
                str = PT.formatStringS(str, "GROUPA", group1);
                str = PT.formatStringS(str, "CA", chain1);
                str = PT.formatStringI(str, "RESA", res1);
                str = PT.formatStringS(str, "GROUPB", group2);
                str = PT.formatStringS(str, "CB", chain2);
                str = PT.formatStringI(str, "RESB", res2);
                sb.append(str);
                if (showMode)
                  sb.append(" strucno= ").appendI(lastId);
                sb.append("\n");

                /*
                 * HELIX 1 H1 ILE 7 PRO 19 1 3/10 CONFORMATION RES 17,19 1CRN 55
                 * HELIX 2 H2 GLU 23 THR 30 1 DISTORTED 3/10 AT RES 30 1CRN 56
                 * SHEET 1 S1 2 THR 1 CYS 4 0 1CRNA 4 SHEET 2 S1 2 CYS 32 ILE 35
                 */
              }
            }
            bs = null;
          }
          if (id == 0
              || bsAtoms != null
              && needPhiPsi
              && (Float.isNaN(atoms[i].getGroupParameter(T.phi)) || Float
                  .isNaN(atoms[i].getGroupParameter(T.psi))))
            continue;
        }
        String ch = atoms[i].getChainIDStr();
        if (bs == null) {
          bs = new BS();
          res1 = atoms[i].getResno();
          group1 = atoms[i].getGroup3(false);
          chain1 = ch;
        }
        type = atoms[i].getProteinStructureType();
        subtype = atoms[i].getProteinStructureSubType();
        sid = atoms[i].getProteinStructureTag();
        bs.set(i);
        lastId = id;
        res2 = atoms[i].getResno();
        group2 = atoms[i].getGroup3(false);
        chain2 = ch;
        iLastAtom = i;
      }
    if (n > 0)
      cmd.append("\n");
    return (scriptMode ? cmd.toString() : sbHelix.appendSB(sbSheet).appendSB(
        sbTurn).appendSB(cmd).toString());
  }

  private final static String[] pdbRecords = { "ATOM  ", "MODEL ", "HETATM" };

  @Override
  public String getFullPDBHeader() {
    if (modelIndex < 0)
      return "";
    String info = (String) auxiliaryInfo.get("fileHeader");
    if (info != null)
      return info;
    info = ms.vwr.getCurrentFileAsString("biomodel");
    int ichMin = info.length();
    for (int i = pdbRecords.length; --i >= 0;) {
      int ichFound;
      String strRecord = pdbRecords[i];
      switch (ichFound = (info.startsWith(strRecord) ? 0 : info.indexOf("\n"
          + strRecord))) {
      case -1:
        continue;
      case 0:
        auxiliaryInfo.put("fileHeader", "");
        return "";
      default:
        if (ichFound < ichMin)
          ichMin = ++ichFound;
      }
    }
    info = info.substring(0, ichMin);
    auxiliaryInfo.put("fileHeader", info);
    return info;
  }

  @Override
  public void getPdbData(Viewer vwr, String type, char ctype,
                         boolean isDraw, BS bsSelected,
                         OC out, LabelToken[] tokens, SB pdbCONECT, BS bsWritten) {
    boolean bothEnds = false;
    char qtype = (ctype != 'R' ? 'r' : type.length() > 13
        && type.indexOf("ramachandran ") >= 0 ? type.charAt(13) : 'R');
    if (qtype == 'r')
      qtype = vwr.getQuaternionFrame();
    int mStep = vwr.getInt(T.helixstep);
    int derivType = (type.indexOf("diff") < 0 ? 0 : type.indexOf("2") < 0 ? 1
        : 2);
    if (!isDraw) {
      out.append("REMARK   6 Jmol PDB-encoded data: " + type + ";");
      if (ctype != 'R') {
        out.append("  quaternionFrame = \"" + qtype + "\"");
        bothEnds = true; //???
      }
      out.append("\nREMARK   6 Jmol Version ").append(Viewer.getJmolVersion())
          .append("\n");
      if (ctype == 'R')
        out
            .append("REMARK   6 Jmol data min = {-180 -180 -180} max = {180 180 180} "
                + "unScaledXyz = xyz * {1 1 1} + {0 0 0} plotScale = {100 100 100}\n");
      else
        out
            .append("REMARK   6 Jmol data min = {-1 -1 -1} max = {1 1 1} "
                + "unScaledXyz = xyz * {0.1 0.1 0.1} + {0 0 0} plotScale = {100 100 100}\n");
    }
    
    P3 ptTemp = new P3();
    for (int p = 0; p < bioPolymerCount; p++)
      bioPolymers[p].getPdbData(vwr, ctype, qtype, mStep, derivType,
          bsAtoms, bsSelected, bothEnds, isDraw, p == 0, tokens, out, 
          pdbCONECT, bsWritten, ptTemp);
  }

}
