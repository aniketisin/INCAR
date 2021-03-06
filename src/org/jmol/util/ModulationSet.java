package org.jmol.util;

import java.util.Hashtable;
import java.util.Map;

import org.jmol.api.JmolModulationSet;
import org.jmol.api.SymmetryInterface;

import javajs.util.Lst;
import javajs.util.M3;
import javajs.util.Matrix;
import javajs.util.P3;
import javajs.util.PT;
import javajs.util.T3;
import javajs.util.V3;

/**
 * A class to group a set of modulations for an atom as a "vibration" Extends V3
 * so that it will be a displacement, and its value will be an occupancy
 * 
 * @author Bob Hanson hansonr@stolaf.edu 8/9/2013
 * 
 */

public class ModulationSet extends Vibration implements JmolModulationSet {

  public float vOcc = Float.NaN;
  public Map<String, Float> htUij;
  public float vOcc0;

  String id;

  private Lst<Modulation> mods;
  private int iop;
  private P3 r0;
  /**
   * vib is a spin vector when the model has modulation; otherwise an
   * unmodulated vibration.
   * 
   */
  public Vibration vib;
  public V3 mxyz;

  private SymmetryInterface symmetry;
  private M3 gammaE;
  private Matrix gammaIinv;
  private Matrix sigma;
  private Matrix tau;

  private boolean enabled;
  private float scale = 1;

  private P3 qtOffset = new P3();
  private boolean isQ;

  private Matrix rI;

  private ModulationSet modTemp;
  private String strop;
  private boolean isSubsystem;

  private Matrix tFactorInv;
  private Matrix rsvs;

  @Override
  public float getScale() {
    return scale;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public ModulationSet() {

  }

  /**
   * A collection of modulations for a specific atom.
   * 
   * 
   * @param id
   * @param r00
   *        originating atom position prior to application of symmetry
   * @param r0
   *        unmodulated (average) position after application of symmetry
   * @param d
   * @param mods
   * @param gammaE
   * @param factors
   *        including sigma and tFactor
   * @param iop
   * @param symmetry
   * @param v
   *        TODO
   * @return this
   * 
   * 
   */

  public ModulationSet setMod(String id, P3 r00, P3 r0, int d,
                              Lst<Modulation> mods, M3 gammaE,
                              Matrix[] factors, int iop,
                              SymmetryInterface symmetry, Vibration v) {

    // The superspace operation matrix is (3+d+1)x(3+d+1) rotation/translation matrix
    // that can be blocked as follows:
    // 
    //              rotational part     | translational part
    //          
    //             Gamma_E     0        |  S_E + n
    // {Gamma|S} =                        
    //             Gamma_M   Gamma_I    |  S_I
    //             
    //               0         0        |   1
    // 
    // where Gamma_E is the "external" 3x3 R3 point group operation 
    //       Gamma_I is the "intermal" dxd point group operation
    //       Gamma_M is the dx3 "mixing" component that adds an
    //               external effect to tbe rotation of internal coordinates.
    //       3x1 S_E and 3x1 S_I are the external and internal translations, respectively
    //       n is the R3 part of the lattice translation that is part of this particular operation
    //       
    // Note that all elements of Gamma are 0, 1, or -1 -- "epsilons"
    //
    // Likewise, the 3+d coordinate vector that Gamma|S is being operated upon is a (3+d x 1) column vector
    // that can be considered to be an external ("R3") component and an internal ("d-space") component:
    // 
    //        r_E (3 x 1)
    //   r = 
    //        r_I (d x 1)
    //
    // Thus, we have:
    //
    //   r' = Gamma * r + S + n 
    //
    // with components
    //
    //   r'_E = Gamma_E * r_E + S_E + n    (just as for standard structures)
    //   r'_I = Gamma_M * r_E + Gamma_I * r_I + S_I 
    //
    // These equations are not actually used here.
    //
    // The set of cell wave vectors form the sigma (d x 3) array, one vector per row.
    // Multiplying sigma by the atom vector r'_E and adding a zero-point offset
    // in internal d-space, tuv, gives us r'_I
    //
    //   r'_I = sigma * r'_E + tuv
    //
    // However, this coordinate is not in the "space" that our modulation functions
    // are defined for. In order to apply those functions, we must back-transform this
    // point into the space of the asymmetric unit. We do that inverting our function 
    // 
    //   X'_I = Gamma_M * X_E + Gamma_I * X_I + S_I
    //
    // to give:
    //
    //   X_I = (Gamma_I^-1)(X'_I - Gamma_M * X_E - S_I)
    //
    // The parameters to this Java function r0 and r00 provide values for r'_E and r_E, 
    // respectively. Substituting r'_I for X'_I and r_E for X_E, we get:
    //
    //   r_I = (Gamma_I^-1)(sigma * r'_E + tuv - Gamma_M * r_E - S_I)
    //
    // In the code below, we precalculate all except the zero-point offset as "tau":
    //
    //   tau = gammaIinv.mul(sigma.mul(vR0).sub(gammaM.mul(vR00)).sub(sI));
    //
    // and then, in calculate(), we add in the tuv part and sum all the modulations:
    //
    //   rI = tau.add(gammaIinv.mul(tuv)).toArray();
    //   for (int i = mods.size(); --i >= 0;)
    //     mods.get(i).apply(this, rI);
    // 
    // We can think of tau as an operator leading to a point in the "internal" d-space, 
    // as in a t-plot (van Smaalen, Fig. 2.6, p. 35) but for all internal coordinates together.
    //
    //
    //// Note that Gamma_M is not necessarily all zeros. For example, in 
    //// SSG 67.1.16.12  Acmm(1/2,0,g)0s0 we have an operator
    ////  
    ////  (x1,x2,-x3,x1-x4+1/2) 
    ////  
    //// [http://stokes.byu.edu/iso/ssginfo.php?label=67.1.16.12&notation=x1x2x3x4]
    //// 
    //// Prior to Jmol 14.[2/3].7 10/11/2014 this was not being considered.
    ////
    //// Noting that we have 
    ////
    ////   Gamma_M = sigma * Gamma_E - Gamma_I * sigma
    ////
    //// and
    ////
    ////   X'_I = sigma * X'_E = sigma * (Gamma_E * X_E + S_E)
    ////
    //// we can, with some work, rewrite tau as:
    //// 
    ////   tau = X_I = sigma * X_E + (Gamma_I^-1)(sigma * S_E - S_I)
    //// 
    //// This relationship is used in Jana2006 but not here, because it 
    //// also necessitates adding in the final lattice shift, and that is not
    //// as easy as it sounds. It is easier just to use Gamma_M * X_E.
    ////
    //// Aside: In the case of subsystems, Gamma and S are extracted from:
    //// 
    ////   {Gamma | S} = {Rs_nu | Vs_nu} = W_nu {Rs|Vs} W_nu^-1
    ////
    //// For subsystem nu, we need to use t_nu, which will be
    //// 
    ////   t_nu = (W_dd - sigma W_3d) * tuv   (van Smaalen, p. 101)
    //// 
    ////   t_nu = tFactor * tuv
    //// 
    //// so this becomes
    //// 
    ////   X_I = tau + (Gamma_I^-1)(tFactor^-1 * tuv)
    //// 
    //// Thus we have two subsystem-dependent modulation factors we
    //// need to bring in, sigma and tFactor, and two we need to compute,
    //// GammaIinv and tau.

    this.id = id + "_" + symmetry.getSpaceGroupName();
    strop = symmetry.getSpaceGroupXyz(iop, false);
    this.r0 = r0;
    modDim = d;
    rI = new Matrix(null, d, 1);
    this.mods = mods;
    this.iop = iop;
    this.symmetry = symmetry;
    this.gammaE = gammaE; // gammaE_nu, R, the real 3x3 rotation matrix, as M3
    sigma = factors[0];
    if (factors[1] != null) {
      isSubsystem = true;
      tFactorInv = factors[1].inverse();
    }
    if (v != null) {
      // An atom's modulation will take the place of its vibration, if it
      // has one, so we have to create a field here to hang onto that. 
      // It could be a magnetic moment being modulated, or it may be
      // just a simple vibration that just needs a place to be.
      vib = v;
      vib.modScale = 1;
      mxyz = new V3(); // modulations of spin
    }
    Matrix vR00 = Matrix.newT(r00, true);
    Matrix vR0 = Matrix.newT(r0, true);

    rsvs = symmetry.getOperationRsVs(iop);
    gammaIinv = rsvs.getSubmatrix(3, 3, d, d).inverse();
    Matrix gammaM = rsvs.getSubmatrix(3, 0, d, 3);
    Matrix sI = rsvs.getSubmatrix(3, 3 + d, d, 1);

    tau = gammaIinv.mul(sigma.mul(vR0).sub(gammaM.mul(vR00)).sub(sI));

    if (Logger.debuggingHigh)
      Logger.debug("MODSET create " + id + " r0=" + Escape.eP(r0) + " tau="
          + tau);

    return this;
  }

  @Override
  public SymmetryInterface getSubSystemUnitCell() {
    return (isSubsystem ? symmetry : null);
  }

  /**
   * Calculate  r_I internal d-space coordinate of an atom.
   *  
   * @param tuv
   * @param isQ
   * @return this ModulationSet, with this.rI set to the coordinate
   */

  public synchronized ModulationSet calculate(T3 tuv, boolean isQ) {
    // initialize modulation components
    x = y = z = 0;
    htUij = null;
    vOcc = Float.NaN;
    if (mxyz != null)
      mxyz.set(0, 0, 0);
    double[][] a;
    if (isQ && qtOffset != null) {
      // basically moving whole unit cells here
      // applied to all cell wave vectors
      Matrix q = new Matrix(null, 3, 1);
      a = q.getArray();
      a[0][0] = qtOffset.x;
      a[1][0] = qtOffset.y;
      a[2][0] = qtOffset.z;
      a = (rI = sigma.mul(q)).getArray();
    } else {
      // initialize to 0 0 0
      a = rI.getArray();
      for (int i = 0; i < modDim; i++)
        a[i][0] = 0;
    }
    if (tuv != null) {
      // add in specified x4,x5,x6 offset:
      switch (modDim) {
      default:
        a[2][0] += tuv.z;
        //$FALL-THROUGH$
      case 2:
        a[1][0] += tuv.y;
        //$FALL-THROUGH$
      case 1:
        a[0][0] += tuv.x;
        break;
      }
    }
    if (isSubsystem) {
      // apply subsystem scaling adjustment
      rI = tFactorInv.mul(rI);
    }
    // add in precalculated part
    rI = tau.add(gammaIinv.mul(rI));
    // modulate
    double[][] arI = rI.getArray();
    for (int i = mods.size(); --i >= 0;)
      mods.get(i).apply(this, arI);
    // rotate by R3 rotation
    gammaE.rotate(this);
    if (mxyz != null)
      gammaE.rotate(mxyz);
    return this;
  }

  public void addUTens(String utens, float v) {
    if (htUij == null)
      htUij = new Hashtable<String, Float>();
    Float f = htUij.get(utens);
    if (Logger.debuggingHigh)
      Logger.debug("MODSET " + id + " utens=" + utens + " f=" + f + " v=" + v);
    if (f != null)
      v += f.floatValue();
    htUij.put(utens, Float.valueOf(v));
  }

  /**
   * Set modulation "t" value, which sets which unit cell in sequence we are
   * looking at; d=1 only.
   * 
   * @param isOn
   * @param qtOffset
   * @param isQ
   * @param scale
   * 
   */
  @Override
  public synchronized void setModTQ(T3 a, boolean isOn, T3 qtOffset,
                                    boolean isQ, float scale) {
    if (enabled)
      addTo(a, Float.NaN);
    enabled = false;
    this.scale = scale;
    if (qtOffset != null) {
      this.qtOffset.setT(qtOffset);
      this.isQ = isQ;
      if (isQ)
        qtOffset = null;
      calculate(qtOffset, isQ);
    }
    if (isOn) {
      addTo(a, 1);
      enabled = true;
    }
  }

  @Override
  public void addTo(T3 a, float scale) {
    boolean isReset = (Float.isNaN(scale));
    if (isReset)
      scale = -1;
    ptTemp.setT(this);
    ptTemp.scale(this.scale * scale);
    if (a != null) {
      //if (!isReset)
      //System.out.println(a + " ms " + ptTemp);
      symmetry.toCartesian(ptTemp, true);
      a.add(ptTemp);
    }
    // magnetic moment part
    if (mxyz != null)
      setVib(isReset);
  }

  private void setVib(boolean isReset) {
    vib.setT(v0);
    if (isReset)
      return;
    ptTemp.setT(mxyz);
    ptTemp.scale(this.scale * scale);
    symmetry.toCartesian(ptTemp, true);
    PT.fixPtFloats(ptTemp, PT.CARTESIAN_PRECISION);
    ptTemp.scale(vib.modScale);
    vib.add(ptTemp);
  }

  @Override
  public String getState() {
    String s = "";
    if (qtOffset != null && qtOffset.length() > 0)
      s += "; modulation " + Escape.eP(qtOffset) + " " + isQ + ";\n";
    s += "modulation {selected} " + (enabled ? "ON" : "OFF");
    return s;
  }

  @Override
  public T3 getModPoint(boolean asEnabled) {
    return (asEnabled ? this : r0);
  }

  @Override
  public Object getModulation(char type, T3 tuv) {
    getModTemp();
    switch (type) {
    case 'D':
      // return r0 if t456 is null, otherwise calculate dx,dy,dz for a given t4,5,6
      return P3.newP(tuv == null ? r0 : modTemp.calculate(tuv, false));
    case 'M':
      // return r0 if t456 is null, otherwise calculate dx,dy,dz for a given t4,5,6
      return P3.newP(tuv == null ? v0 : modTemp.calculate(tuv, false).mxyz);
    case 'T':
      modTemp.calculate(tuv, false);
      double[][] ta = modTemp.rI.getArray();
      return P3.new3((float) ta[0][0], (modDim > 1 ? (float) ta[1][0] : 0),
          (modDim > 2 ? (float) ta[2][0] : 0));
    case 'O':
      // return vOcc current or calculated
      return Float.valueOf((tuv == null ? vOcc : modTemp.calculate(tuv, false).vOcc) * 100);
    }
    return null;
  }

  P3 ptTemp = new P3();
  private V3 v0;

  @Override
  public void setTempPoint(T3 a, T3 t456, float vibScale, float scale) {
    if (!enabled)
      return;
    getModTemp();
    addTo(a, Float.NaN);
    modTemp.calculate(t456, false).addTo(a, scale);
  }

  private void getModTemp() {
    if (modTemp == null) {
      modTemp = new ModulationSet();
      modTemp.id = id;
      modTemp.tau = tau;
      modTemp.mods = mods;
      modTemp.gammaE = gammaE;
      modTemp.modDim = modDim;
      modTemp.gammaIinv = gammaIinv;
      modTemp.sigma = sigma;
      modTemp.r0 = r0;
      modTemp.v0 = v0;
      modTemp.vib = vib;
      modTemp.symmetry = symmetry;
      modTemp.rI = rI;
      if (mxyz != null) {
        modTemp.mxyz = new V3();
      }
    }
  }

  @Override
  public void getInfo(Map<String, Object> info) {
    Hashtable<String, Object> modInfo = new Hashtable<String, Object>();
    modInfo.put("id", id);
    modInfo.put("r0", r0);
    modInfo.put("tau", tau.getArray());
    modInfo.put("modDim", Integer.valueOf(modDim));
    modInfo.put("rsvs", rsvs);
    modInfo.put("sigma", sigma.getArray());
    modInfo.put("symop", Integer.valueOf(iop + 1));
    modInfo.put("strop", strop);
    modInfo.put("unitcell", symmetry.getUnitCellInfo());

    Lst<Hashtable<String, Object>> mInfo = new Lst<Hashtable<String, Object>>();
    for (int i = 0; i < mods.size(); i++)
      mInfo.addLast(mods.get(i).getInfo());
    modInfo.put("mods", mInfo);
    info.put("modulation", modInfo);
  }

  @Override
  public void setXYZ(T3 v) {
    // we do not allow setting of the modulation vector,
    // but if there is an associated magnetic spin "vibration"
    // or an associated simple vibration,
    // then we allow setting of that.
    // but this is temporary, since really we set these from v0.
    if (vib == null)
      return;
    if (vib.modDim == Vibration.TYPE_SPIN) {
      if (v.x == PT.FLOAT_MIN_SAFE && v.y == PT.FLOAT_MIN_SAFE) {
        // written by StateCreator -- for modulated magnetic moments
        // 957 Fe Fe_1_#957 1.4E-45 1.4E-45 0.3734652 ;
        vib.modScale = v.z;
        return;
      }
    }
    vib.setT(v);
  }

  @Override
  public Vibration getVibration(boolean forceNew) {
    // ModulationSets can be place holders for standard vibrations
    if (vib == null && forceNew)
      vib = new Vibration();
    return vib;
  }

  @Override
  public V3 getV3() {
    return (mxyz == null ? this : mxyz);
  }

  @Override
  public void scaleVibration(float m) {
    if (vib != null)
      vib.scale(m);
    vib.modScale *= m;
  }

  @Override
  public void setMoment() {
    if (mxyz == null)
      return;
    symmetry.toCartesian(vib, true);
    v0 = V3.newV(vib);
  }

  @Override
  public boolean isNonzero() {
    return x != 0 || y != 0 || z != 0 || mxyz != null
        && (mxyz.x != 0 || mxyz.y != 0 || mxyz.z != 0);
  }

  private float[] axesLengths;

  float[] getAxesLengths() {
    return (axesLengths == null ? (axesLengths = symmetry.getNotionalUnitCell())
        : axesLengths);
  }

}
