/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2014-07-23 20:11:12 +0530 (Wed, 23 Jul 2014) $
 * $Revision: 19878 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.adapter.smarter;

import org.jmol.c.STR;

public class Structure {
  public STR structureType;
  public STR substructureType;
  public String structureID;
  public int serialID;
  public int strandCount;

  public int startSequenceNumber;
  public int startChainID;
  public String startChainStr;  
  public char startInsertionCode = '\0';
  
  public int endSequenceNumber;
  public int endChainID;
  public String endChainStr;
  public char endInsertionCode = '\0';
  
  public int[] atomStartEnd = new int[2];
  public int[] modelStartEnd = new int[] {-1, -1};
  

  public static STR getHelixType(int type) {
    switch (type) {
    case 1:
      return STR.HELIXALPHA;
    case 3:
      return STR.HELIXPI;
    case 5:
      return STR.HELIX310;
    }
    return STR.HELIX;
  }

  public Structure(int modelIndex, STR structureType,
      STR substructureType, String structureID, int serialID,
      int strandCount) {
    this.structureType = structureType;
    this.substructureType = substructureType;
    if (structureID == null)
      return;
    setModels(modelIndex, 0);
    this.structureID = structureID;
    this.strandCount = strandCount; // 1 for sheet initially; 0 for helix or turn
    this.serialID = serialID;
  }

  public void set(int startChainID, int startSequenceNumber,
                  char startInsertionCode, int endChainID,
                  int endSequenceNumber, char endInsertionCode, int istart,
                  int iend) {
    this.startChainID = startChainID;
    this.startSequenceNumber = startSequenceNumber;
    this.startInsertionCode = startInsertionCode;
    this.endChainID = endChainID;
    this.endSequenceNumber = endSequenceNumber;
    this.endInsertionCode = endInsertionCode;
    atomStartEnd[0] = istart;
    atomStartEnd[1] = iend;
  }
  
  public void setModels(int model1, int model2) {
    modelStartEnd[0] = model1;
    modelStartEnd[1] = (model2 == 0 ? model1 : model2);
  }

}
