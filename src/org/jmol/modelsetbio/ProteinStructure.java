/* $RCSfile$
 * $Author: nicove $
 * $Date: 2007-03-25 06:44:28 -0500 (Sun, 25 Mar 2007) $
 * $Revision: 7224 $
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

import java.util.Hashtable;
import java.util.Map;


import javajs.util.AU;
import javajs.util.P3;
import javajs.util.V3;

import org.jmol.c.STR;
import org.jmol.util.Logger;

public abstract class ProteinStructure {

  STR type;
  STR subtype;
  String structureID;
  int strucNo;
  int serialID;
  int strandCount;

  public int nRes;
  protected AlphaPolymer apolymer;
  protected int monomerIndexFirst;
  protected P3 axisA, axisB;
  protected V3 axisUnitVector;
  protected V3 vectorProjection;

  private static int globalStrucNo = 1000;
  private int monomerIndexLast;
  private P3[] segments;
  
  /**
   * 
   * @param apolymer
   * @param type
   * @param monomerIndex
   * @param monomerCount
   */
  protected void setupPS(AlphaPolymer apolymer, STR type,
                       int monomerIndex, int monomerCount) {
    strucNo = ++globalStrucNo;
    this.apolymer = apolymer;
    this.type = type;
    vectorProjection = new V3();
    monomerIndexFirst = monomerIndex;
    addMonomer(monomerIndex + monomerCount - 1);
    if(Logger.debugging)
      Logger.info(
          "Creating ProteinStructure " + strucNo 
          + " " + type.getBioStructureTypeName(false) 
          + " from " + monomerIndexFirst + " through "+ monomerIndexLast
          + " in polymer " + apolymer);
  }

  /**
   * Note that this method does not check to see 
   * that there are no overlapping protein structures.
   *  
   * @param index
   */
  void addMonomer(int index) {
    resMap = null;
    resetAxes();
    monomerIndexFirst = Math.min(monomerIndexFirst, index);
    monomerIndexLast = Math.max(monomerIndexLast, index);
    nRes = monomerIndexLast - monomerIndexFirst + 1;
    //System.out.println("addMonomer First = " + monomerIndexFirst);
  }

  /**
   * should be OK here to remove the first -- we just get a monomerCount of 0;
   * but we don't remove monomers that aren't part of this structure.
   * 
   * @param index
   */
  void removeMonomer(int index) {
    resMap = null;
    resetAxes();
    if (index > monomerIndexLast || index < monomerIndexFirst)
      return;
    if (index == monomerIndexFirst) {
      monomerIndexFirst++;
      nRes--;
    } else if (index == monomerIndexLast) {
      monomerIndexLast--;
      nRes--;
    } else {
      int n = monomerIndexLast - index;
      monomerIndexLast = index - 1;
      nRes = index - monomerIndexFirst;
      Monomer[] monomers = apolymer.monomers;
      //System.out.println("bp removing protein structure " + index + " " + n);
      STR type = monomers[++index].getProteinStructureType();
      int mLast = -1;
      for (int i = 0, pt = index; i < n; i++, pt++) {
        ((AlphaMonomer)monomers[pt]).setStructure(null);//, false);
        //System.out.println("bp monomer=" + pt + " " + monomers[pt] + " " + type);
        mLast = monomers[pt].setProteinStructureType(type, mLast);
      }
    }
    //System.out.println("remMonomer First = " + monomerIndexFirst + " last="
      //  + monomerIndexLast);
  }

  public void calcAxis() {
    // implemented in helix and sheet
  }

  void calcSegments() {
    if (segments != null)
      return;
    calcAxis();
    segments = new P3[nRes + 1];
    segments[nRes] = axisB;
    segments[0] = axisA;
    V3 axis = V3.newV(axisUnitVector);
    axis.scale(axisB.distance(axisA) / nRes);
    for (int i = 1; i < nRes; i++) {
      P3 point = segments[i] = new P3();
      point.add2(segments[i - 1], axis);
      //now it's just a constant-distance segmentation. 
      //there isn't anything significant about seeing the
      //amino colors in different-sized slices, and (IMHO)
      //it looks better this way anyway. RMH 11/2006
      
      //apolymer.getLeadMidPoint(monomerIndex + i, point);
      //projectOntoAxis(point);
    }
  }

  public boolean isWithin(int monomerIndex) {
    return (monomerIndex > monomerIndexFirst 
        && monomerIndex < monomerIndexLast);
  }

  public int getMonomerIndex() {
    return monomerIndexFirst;
  }

  private Map<Monomer, Integer> resMap;
  public int getIndex(Monomer monomer) {
    if (resMap == null) {
      resMap = new Hashtable<Monomer, Integer>();
      for (int i = nRes; --i >= 0; )
        resMap.put(apolymer.monomers[monomerIndexFirst + i], Integer.valueOf(i));
    }
    Integer ii = resMap.get(monomer);
    return (ii == null ? -1 : ii.intValue());
  }

  public P3[] getSegments() {
    if (segments == null)
      calcSegments();
    return segments;
  }

  public P3 getAxisStartPoint() {
    calcAxis();
    return axisA;
  }

  public P3 getAxisEndPoint() {
    calcAxis();
    return axisB;
  }

  P3 getStructureMidPoint(int index) {
    if (segments == null)
      calcSegments();
    return segments[index];
  }

  public void getInfo(Map<String, Object> info) {
    info.put("type", type.getBioStructureTypeName(false));
    int[] leadAtomIndices = apolymer.getLeadAtomIndices();
    int[] iArray = AU.arrayCopyRangeI(leadAtomIndices, monomerIndexFirst, monomerIndexFirst + nRes);
    info.put("leadAtomIndices", iArray);
    calcAxis();
    if (axisA == null)
      return;
    info.put("axisA", axisA);
    info.put("axisB", axisB);
    info.put("axisUnitVector", axisUnitVector);
  }

  void resetAxes() {
    axisA = null;
    segments = null;
  }
}
