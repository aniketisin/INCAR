/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
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

package org.jmol.symmetry;

import javajs.util.M3;
import javajs.util.M4;
import javajs.util.Matrix;
import javajs.util.P3;
import javajs.util.PT;
import javajs.util.SB;
import javajs.util.T3;
import javajs.util.V3;

import org.jmol.util.Logger;
import org.jmol.util.Parser;

/*
 * Bob Hanson 4/2006
 * 
 * references: International Tables for Crystallography Vol. A. (2002) 
 *
 * http://www.iucr.org/iucr-top/cif/cifdic_html/1/cif_core.dic/Ispace_group_symop_operation_xyz.html
 * http://www.iucr.org/iucr-top/cif/cifdic_html/1/cif_core.dic/Isymmetry_equiv_pos_as_xyz.html
 *
 * LATT : http://macxray.chem.upenn.edu/LATT.pdf thank you, Patrick Carroll
 * 
 * NEVER ACCESS THESE METHODS DIRECTLY! ONLY THROUGH CLASS Symmetry
 */


class SymmetryOperation extends M4 {
  String xyzOriginal;
  String xyz;
  private boolean doNormalize = true;
  boolean isFinalized;
  private int opId;
  V3 centering;

  private P3 atomTest;

  private String[] myLabels;
  int modDim;

// rsvs:
//    [ [(3+modDim)*x + 1]    
//      [(3+modDim)*x + 1]     [ Gamma_R   [0x0]   | Gamma_S
//      [(3+modDim)*x + 1]  ==    [0x0]    Gamma_e | Gamma_d 
//      ...                       [0]       [0]    |   1     ]
//      [0 0 0 0 0...   1] ]
  
  float[] linearRotTrans;
  
  Matrix rsvs;
  boolean isBio;
  private Matrix sigma;
  int index;
  String subsystemCode;
  public int timeReversal;
  
  void setSigma(String subsystemCode, Matrix sigma) {
    this.subsystemCode = subsystemCode;
    this.sigma = sigma;
  }

  /**
   * @j2sIgnoreSuperConstructor
   * @j2sOverride
   * 
   * @param op
   * @param atoms
   * @param atomIndex
   * @param countOrId
   * @param doNormalize
   */
  SymmetryOperation(SymmetryOperation op, P3[] atoms,
                           int atomIndex, int countOrId, boolean doNormalize) {
    this.doNormalize = doNormalize;
    if (op == null) {
      opId = countOrId;
      return;
    }
    /*
     * externalizes and transforms an operation for use in atom reader
     * 
     */
    xyzOriginal = op.xyzOriginal;
    xyz = op.xyz;
    opId = op.opId;
    modDim = op.modDim;
    myLabels = op.myLabels;
    index = op.index;
    linearRotTrans = op.linearRotTrans;
    sigma = op.sigma;
    subsystemCode = op.subsystemCode;
    timeReversal = op.timeReversal;
    setMatrix(false);
    if (!op.isFinalized)
      doFinalize();
    if (doNormalize && sigma == null)
      setOffset(atoms, atomIndex, countOrId);
  }

  

  /**
   * rsvs is the superspace group rotation-translation matrix.
   * It is a (3 + modDim + 1) x (3 + modDim + 1) matrix from 
   * which we can extract all necessary parts;
   * @param isReverse 
   * 
   */
  private void setGamma(boolean isReverse) {
  // standard M4 (this)
  //
  //  [ [rot]   | [trans] 
  //     [0]    |   1     ]
  //
  // becomes for a superspace group
  //
  //  rows\cols    (3)    (modDim)    (1)
  // (3)        [ Gamma_R   [0x0]   | Gamma_S
  // (modDim)       m*      Gamma_e | Gamma_d 
  // (1)           [0]       [0]    |   1     ]
    
    int n = 3 + modDim;
    double[][] a = (rsvs = new Matrix(null, n + 1, n + 1)).getArray();
    double[] t = new double[n];
    int pt = 0;
    // first retrieve all n x n values from linearRotTrans
    // and get the translation as well
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++)
        a[i][j] = linearRotTrans[pt++];
      t[i] = (isReverse ? -1 : 1) * linearRotTrans[pt++];
    }
    a[n][n] = 1;
    if (isReverse)
      rsvs = rsvs.inverse();
    // t is already reversed; set it now.
    for (int i = 0; i < n; i++)
      a[i][n] = t[i];
    // then set this operation matrix as {R|t}
    a = rsvs.getSubmatrix(0,  0,  3,  3).getArray();
    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 4; j++)
        setElement(i,  j, (float) (j < 3 ? a[i][j] : t[i]));
    setElement(3,3,1);
  }

  void doFinalize() {
    m03 /= 12;
    m13 /= 12;
    m23 /= 12;
    if (modDim > 0) {
      double[][] a = rsvs.getArray();
      for (int i = a.length - 1; --i >= 0;)
        a[i][3 + modDim] /= 12;
    }
    isFinalized = true;
  }
  
  String getXyz(boolean normalized) {
    return (normalized && modDim == 0 || xyzOriginal == null ? xyz : xyzOriginal);
  }

  void newPoint(P3 atom1, P3 atom2, int x, int y, int z) {
    rotTrans2(atom1, atom2);
    atom2.add3(x,  y,  z);
  }

  String dumpInfo() {
    return "\n" + xyz + "\ninternal matrix representation:\n"
        + toString();
  }

  final static String dumpSeitz(M4 s, boolean isCanonical) {
    SB sb = new SB();
    float[] r = new float[4];
    for (int i = 0; i < 3; i++) {
      s.getRow(i,r);
      sb.append("[\t");
      for (int j = 0; j < 3; j++)
        sb.appendI((int) r[j]).append("\t");
      sb.append(twelfthsOf(isCanonical ? ((int)r[3] + 12)%12 : (int) r[3])).append("\t]\n");
    }
    return sb.toString();
  }
  
  boolean setMatrixFromXYZ(String xyz, int modDim, boolean allowScaling) {
    /*
     * sets symmetry based on an operator string "x,-y,z+1/2", for example
     * 
     */
    if (xyz == null)
      return false;
    xyzOriginal = xyz;
    xyz = xyz.toLowerCase();
    int n = (modDim + 4) * (modDim + 4);
    this.modDim = modDim;
    if (modDim > 0)
      myLabels = labelsXn;
    linearRotTrans = new float[n];
    boolean isReverse = (xyz.startsWith("!"));
    if (isReverse)
      xyz = xyz.substring(1);
    if (xyz.indexOf("xyz matrix:") == 0) {
      /* note: these terms must in unit cell fractional coordinates!
       * CASTEP CML matrix is in fractional coordinates, but do not take into account
       * hexagonal systems. Thus, in wurtzite.cml, for P 6c 2'c:
       *
       * "transform3": 
       * 
       * -5.000000000000e-1  8.660254037844e-1  0.000000000000e0   0.000000000000e0 
       * -8.660254037844e-1 -5.000000000000e-1  0.000000000000e0   0.000000000000e0 
       *  0.000000000000e0   0.000000000000e0   1.000000000000e0   0.000000000000e0 
       *  0.000000000000e0   0.000000000000e0   0.000000000000e0   1.000000000000e0
       *
       * These are transformations of the STANDARD xyz axes, not the unit cell. 
       * But, then, what coordinate would you feed this? Fractional coordinates of what?
       * The real transform is something like x-y,x,z here.
       * 
       */
      this.xyz = xyz;
      Parser.parseStringInfestedFloatArray(xyz, null, linearRotTrans);        
      return setFromMatrix(null, isReverse);
    }
    if (xyz.indexOf("[[") == 0) {
      xyz = xyz.replace('[',' ').replace(']',' ').replace(',',' ');
      Parser.parseStringInfestedFloatArray(xyz, null, linearRotTrans);
      for (int i = 0; i < n; i++) {
        float v = linearRotTrans[i];
        if (Float.isNaN(v))
          return false;
      }
      setMatrix(isReverse);
      isFinalized = true;
      isBio = (xyz.indexOf("bio") >= 0);
      this.xyz = (isBio ? toString() : getXYZFromMatrix(this, false, false, false));
      return true;
    }
    if (xyz.endsWith("m")) {
      timeReversal = (xyz.indexOf("-m") >= 0 ? -1 : 1);
      allowScaling = true;
    }
    String strOut = getMatrixFromString(this, xyz, linearRotTrans, allowScaling);
    if (strOut == null)
      return false;
    setMatrix(isReverse);
    this.xyz = (isReverse ? getXYZFromMatrix(this, true, false, false) : strOut);
    if (timeReversal != 0)
      this.xyz += (timeReversal == 1 ? ",m" : ",-m");
    //System.out.println("testing " + xyz +  " == " + this.xyz + " " + this + "\n" + Escape.eAF(linearRotTrans));
    if (Logger.debugging)
      Logger.debug("" + this);
    return true;
  }


  private void setMatrix(boolean isReverse) {
    if (linearRotTrans.length > 16) {
      setGamma(isReverse);
    } else {
      setA(linearRotTrans);
      if (isReverse) {
        P3 p3 = P3.new3(m03,  m13,  m23);
        invertM(this);
        rotate(p3);
        p3.scale(-1);
        setTranslation(p3);
      }
    }
  }

  boolean setFromMatrix(float[] offset, boolean isReverse) {
    float v = 0;
    int pt = 0;
    myLabels = (modDim == 0 ? labelsXYZ : labelsXn);
    int rowPt = 0;
    int n = 3 + modDim;
    for (int i = 0; rowPt < n; i++) {
      if (Float.isNaN(linearRotTrans[i]))
        return false;
      v = linearRotTrans[i];
      if (Math.abs(v) < 0.00001f)
        v = 0;
      boolean isTrans = ((i + 1) % (n + 1) == 0);
      if (isTrans) {
        if (offset != null) {
          v /= 12;
          if (pt < offset.length)
            v += offset[pt++];
        }
        v = normalizeTwelfths((v < 0 ? -1 : 1) * Math.abs(v * 12)
            / 12f, doNormalize);
        rowPt++;
      }
      linearRotTrans[i] = v;
    }
    linearRotTrans[linearRotTrans.length - 1] = 1;
    setMatrix(isReverse);
    isFinalized = (offset == null);
    xyz = getXYZFromMatrix(this, true, false, false);
    //System.out.println("testing " + xyz + " " + this + "\n" + Escape.eAF(linearRotTrans));
    return true;
  }

  /**
   * Convert the Jones-Faithful notation 
   *   "x, -z+1/2, y"  or "x1, x3-1/2, x2, x5+1/2, -x6+1/2, x7..."
   * to a linear array
   * 
   * Also allows a-b,-5a-5b,-c;0,0,0  format
   * 
   * @param op
   * @param xyz
   * @param linearRotTrans
   * @param allowScaling
   * @return canonized Jones-Faithful string
   */
  static String getMatrixFromString(SymmetryOperation op, String xyz,
                                    float[] linearRotTrans, boolean allowScaling) {
    boolean isDenominator = false;
    boolean isDecimal = false;
    boolean isNegative = false;
    int modDim = (op == null ? 0 : op.modDim);
    int nRows = 4 + modDim;
    boolean doNormalize = (op != null && op.doNormalize);
    int dimOffset = (modDim > 0 ? 3 : 0); // allow a b c to represent x y z
    linearRotTrans[linearRotTrans.length - 1] = 1;
    // may be a-b,-5a-5b,-c;0,0,0 form
    int transPt = xyz.indexOf(';') + 1;
    if (transPt != 0) {
      allowScaling = true;
      if (transPt == xyz.length())
        xyz += "0,0,0";
    }
    int rotPt = -1;
    String[] myLabels = (op == null || modDim == 0 ? null : op.myLabels);
    if (myLabels == null)
      myLabels = labelsXYZ;
    xyz = xyz.toLowerCase() + ",";
    if (modDim > 0)
      xyz = replaceXn(xyz, modDim + 3);
    int xpt = 0;
    int tpt0 = 0;
    int rowPt = 0;
    char ch;
    float iValue = 0;
    float decimalMultiplier = 1f;
    String strT = "";
    String strOut = "";
    for (int i = 0; i < xyz.length(); i++) {
      switch (ch = xyz.charAt(i)) {
      case ';':
        break;
      case '\'':
      case ' ':
      case '{':
      case '}':
      case '!':
        continue;
      case '-':
        isNegative = true;
        continue;
      case '+':
        isNegative = false;
        continue;
      case '/':
        isDenominator = true;
        continue;
      case 'x':
      case 'y':
      case 'z':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'g':
      case 'h':
        tpt0 = rowPt * nRows;
        int ipt = (ch >= 'x' ? ch - 'x' :ch - 'a' + dimOffset);
        xpt = tpt0 + ipt;
        int val = (isNegative ? -1 : 1);
        if (allowScaling && iValue != 0) {
          linearRotTrans[xpt] = iValue; 
          val = (int) iValue;
          iValue = 0;
        } else {
          linearRotTrans[xpt] = val; 
        }
        strT += plusMinus(strT, val, myLabels[ipt]);
        break;
      case ',':
        if (transPt != 0) {
          if (transPt > 0) {
            // now read translation
            rotPt = i;
            i = transPt - 1;
            transPt = -i;
            iValue = 0;
            continue;
          }
          transPt = i + 1;
          i = rotPt;
        }
        // add translation in 12ths
        iValue = normalizeTwelfths(iValue, doNormalize);
        linearRotTrans[tpt0 + nRows - 1] = iValue;
        strT += xyzFraction(iValue, false, true);
        strOut += (strOut == "" ? "" : ",") + strT;
        if (rowPt == nRows - 2)
          return strOut;
        iValue = 0;
        strT = "";
        if (rowPt++ > 2 && modDim == 0) {
          Logger.warn("Symmetry Operation? " + xyz);
          return null;
        }
        break;
      case '.':
        isDecimal = true;
        decimalMultiplier = 1f;
        continue;
      case '0':
        if (!isDecimal && (isDenominator || !allowScaling))
          continue;
        //$FALL-THROUGH$
      default:
        //Logger.debug(isDecimal + " " + ch + " " + iValue);
        int ich = ch - '0';
        if (isDecimal && ich >= 0 && ich <= 9) {
          decimalMultiplier /= 10f;
          if (iValue < 0)
            isNegative = true;
          iValue += decimalMultiplier * ich * (isNegative ? -1 : 1);
          continue;
        }
        if (ich >= 0 && ich <= 9) {
          if (isDenominator) {
            if (iValue == 0) {
              // a/2,....
              linearRotTrans[xpt] /= ich;
            } else {
              iValue /= ich;
            }
          } else {
            iValue = iValue * 10 + (isNegative ? -1 : 1) * ich;
            isNegative = false;
          }
        } else {
          Logger.warn("symmetry character?" + ch);
        }
      }
      isDecimal = isDenominator = isNegative = false;
    }
    return null;
  }

  static String replaceXn(String xyz, int n) {
    for (int i = n; --i >= 0;)
      xyz = PT.rep(xyz, labelsXn[i], labelsXnSub[i]);
    return xyz;
  }

  private final static String xyzFraction(float n12ths, boolean allPositive, boolean halfOrLess) {
    float n = n12ths;
    if (allPositive) {
      while (n < 0)
        n += 12f;
    } else if (halfOrLess) {
      while (n > 6f)
        n -= 12f;
      while (n < -6f)
        n += 12f;
    }
    String s = twelfthsOf(n);
    return (s.charAt(0) == '0' ? "" : n > 0 ? "+" + s : s);
  }

  private final static String twelfthsOf(float n12ths) {
    String str = "";
    if (n12ths < 0) {
      n12ths = -n12ths;
      str = "-";
    }
    int m = 12;
    int n = Math.round(n12ths);
    if (Math.abs(n - n12ths) > 0.01f) {
      // fifths? sevenths? eigths? ninths? sixteenths?
      // Juan Manuel suggests 10 is large enough here 
      float f = n12ths / 12;
      int max = 20;
      for (m = 5; m < max; m++) {
        float fm = f * m;
        n = Math.round(fm);
        if (Math.abs(n - fm) < 0.01f)
          break;
      }
      if (m == max)
        return str + f;
    } else {
      if (n == 12)
        return str + "1";
      if (n < 12)
        return str + twelfths[n % 12];
      switch (n % 12) {
      case 0:
        return "" + n / 12;
      case 2:
      case 10:
        m = 6;
        break;
      case 3:
      case 9:
        m = 4;
        break;
      case 4:
      case 8:
        m = 3;
        break;
      case 6:
        m = 2;
        break;
      default:
        break;
      }
      n = (n * m / 12);
    }
    return str + n + "/" + m;
  }

  private final static String[] twelfths = { "0", "1/12", "1/6", "1/4", "1/3",
  "5/12", "1/2", "7/12", "2/3", "3/4", "5/6", "11/12" };

  private static String plusMinus(String strT, float x, String sx) {
    return (x == 0 ? "" : (x < 0 ? "-" : strT.length() == 0 ? "" : "+") + (x == 1 || x == -1 ? "" : "" + (int) Math.abs(x)) + sx);
  }

  private static float normalizeTwelfths(float iValue, boolean doNormalize) {
    iValue *= 12f;
    if (doNormalize) {
      while (iValue > 6)
        iValue -= 12;
      while (iValue <= -6)
        iValue += 12;
    }
    return iValue;
  }

  final static String[] labelsXYZ = new String[] {"x", "y", "z"};
  final static String[] labelsXn = new String[] {"x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8", "x9", "x10", "x11", "x12", "x13"};
  final static String[] labelsXnSub = new String[] {"x", "y", "z", "a",  "b",  "c",  "d",  "e",  "f",  "g",   "h",   "i",   "j"};

  final static String getXYZFromMatrix(M4 mat, boolean is12ths,
                                       boolean allPositive, boolean halfOrLess) {
    String str = "";
    SymmetryOperation op = (mat instanceof SymmetryOperation ? (SymmetryOperation) mat
        : null);
    if (op != null && op.modDim > 0)
      return getXYZFromRsVs(op.rsvs.getRotation(), op.rsvs.getTranslation(), is12ths);
    float[] row = new float[4];
    for (int i = 0; i < 3; i++) {
      int lpt = (i < 3 ? 0 : 3);
      mat.getRow(i, row);
      String term = "";
      for (int j = 0; j < 3; j++)
        if (row[j] != 0)
          term += plusMinus(term, row[j], labelsXYZ[j + lpt]);
      term += xyzFraction((is12ths ? row[3] : row[3] * 12), allPositive,
          halfOrLess);
      str += "," + term;
    }
    return str.substring(1);
  }

  private void setOffset(P3[] atoms, int atomIndex, int count) {
    /*
     * the center of mass of the full set of atoms is moved into the cell with this
     *  
     */
    int i1 = atomIndex;
    int i2 = i1 + count;
    float x = 0;
    float y = 0;
    float z = 0;
    if (atomTest == null)
      atomTest = new P3();
    for (int i = i1; i < i2; i++) {
      newPoint(atoms[i], atomTest, 0, 0, 0);
      x += atomTest.x;
      y += atomTest.y;
      z += atomTest.z;
    }
    
    while (x < -0.001 || x >= count + 0.001) {
      m03 += (x < 0 ? 1 : -1);
      x += (x < 0 ? count : -count);
    }
    while (y < -0.001 || y >= count + 0.001) {
      m13 += (y < 0 ? 1 : -1);
      y += (y < 0 ? count : -count);
    }
    while (z < -0.001 || z >= count + 0.001) {
      m23 += (z < 0 ? 1 : -1);
      z += (z < 0 ? count : -count);
    }
  }

//  // action of this method depends upon setting of unitcell
//  private void transformCartesian(UnitCell unitcell, P3 pt) {
//    unitcell.toFractional(pt, false);
//    transform(pt);
//    unitcell.toCartesian(pt, false);
//
//  }
  
  V3[] rotateAxes(V3[] vectors, UnitCell unitcell, P3 ptTemp, M3 mTemp) {
    V3[] vRot = new V3[3];
    getRotationScale(mTemp);    
    for (int i = vectors.length; --i >=0;) {
      ptTemp.setT(vectors[i]);
      unitcell.toFractional(ptTemp, true);
      mTemp.rotate(ptTemp);
      unitcell.toCartesian(ptTemp, true);
      vRot[i] = V3.newV(ptTemp);
    }
    return vRot;
  }
  
  static String fcoord(T3 p) {
    return fc(p.x) + " " + fc(p.y) + " " + fc(p.z);
  }

  private static String fc(float x) {
    float xabs = Math.abs(x);
    int x24 = (int) approxF(xabs * 24);
    String m = (x < 0 ? "-" : "");
    if (x24%8 != 0)
      return m + twelfthsOf(x24 >> 1);
    return (x24 == 0 ? "0" : x24 == 24 ? m + "1" : m + (x24/8) + "/3");
  }

  static float approxF(float f) {
    return PT.approx(f, 100);
  }

  public static void normalizeTranslation(M4 operation) {
    operation.m03 = ((int)operation.m03 + 12) % 12;
    operation.m13 = ((int)operation.m13 + 12) % 12;
    operation.m23 = ((int)operation.m23 + 12) % 12;    
  }

  public static String getXYZFromRsVs(Matrix rs, Matrix vs, boolean is12ths) {
    double[][] ra = rs.getArray();
    double[][] va = vs.getArray();
    int d = ra.length;
    String s = "";
    for (int i = 0; i < d; i++) {
      s += ",";
      for (int j = 0; j < d; j++) {
        double r = ra[i][j];
        if (r != 0) {
          s += (r < 0 ? "-" : s.endsWith(",") ? "" : "+") + (Math.abs(r) == 1 ? "" : "" + (int) Math.abs(r)) + "x" + (j + 1);
        }
      }
      s += xyzFraction((int) (va[i][0] * (is12ths ? 1 : 12)), false, true);
    }
    return PT.rep(s.substring(1), ",+", ",");
  }

  @Override
  public String toString() {
    return (rsvs == null ? super.toString() : super.toString() + " " + rsvs.toString());
  }

  float magOp = Float.MAX_VALUE;
  boolean isCenteringOp;
  private boolean unCentered;
  public float getSpinOp() {
    if (magOp == Float.MAX_VALUE)
      magOp = determinant3() * timeReversal;
    //System.out.println("sym op " + index + " " + xyz + " has tr " + timeReversal + " and magop " + magOp);
    return magOp;
  }

  public void setTimeReversal(int magRev) {
    timeReversal = magRev;
    if (xyz.indexOf("m") >= 0)
      xyz = xyz.substring(0, xyz.indexOf("m"));
    xyz += (magRev == 1 ? ",m" : magRev == -1 ? ",-m" : "");
  }

  public static String cleanMatrix(M4 m4) {
    SB sb = new SB();
    sb.append("[ ");
    float[] row = new float[4];
    for (int i = 0; i < 3; i++) {
      m4.getRow(i, row);
      sb.append("[ ")
        .appendI((int)row[0]).append(" ")
        .appendI((int)row[1]).append(" ")
        .appendI((int)row[2]).append(" ");      
      sb.append(twelfthsOf(row[3]*12)).append(" ]");
    }
    return sb.append(" ]").toString();
  }

  /**
   * assumption here is that these are in order of sets, as in ITA
   * 
   * @param c
   * @param isFinal
   *        TODO
   * @return centering
   */
  public V3 setCentering(V3 c, boolean isFinal) {
    if (centering == null && !unCentered) {
      if (modDim == 0 && index > 1 && m00 == 1 && m11 == 1 && m22 == 1
          && m01 == 0 && m02 == 0 && m10 == 0 && m12 == 0 && m20 == 0
          && m21 == 0) {
        centering = V3.new3(m03, m13, m23);
        if (centering.lengthSquared() == 0) {
          unCentered = true;
          centering = null;
        } else if (!isFinal)
          centering.scale(1 / 12f);
        isCenteringOp = true;
      } else {
        centering = c;
      }
    }
    return centering;
  }
  
}
