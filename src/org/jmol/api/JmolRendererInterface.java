package org.jmol.api;


import java.util.Map;

import org.jmol.modelset.Atom;
import org.jmol.util.GData;
import org.jmol.util.MeshSurface;

import javajs.awt.Font;
import javajs.util.M3;
import javajs.util.M4;
import javajs.util.P3;
import javajs.util.P3i;
import org.jmol.viewer.Viewer;

public interface JmolRendererInterface extends JmolGraphicsInterface {

  // exporting  

  public abstract void addRenderer(int tok);

  public abstract boolean checkTranslucent(boolean isAlphaTranslucent);

  public abstract void drawAtom(Atom atom);

  public abstract void drawBond(P3 atomA, P3 atomB, short colixA,
                                short colixB, byte endcaps, short mad, int bondOrder);

  public abstract void drawDashedLine(int run, int rise, P3i pointA,
                                      P3i pointB);

  public abstract void drawDottedLine(P3i pointA, P3i pointB);

  public abstract boolean drawEllipse(P3 ptAtom, P3 ptX, P3 ptY,
                                      boolean fillArc, boolean wireframeOnly);

  /**
   * draws a ring and filled circle (halos, draw CIRCLE, draw handles)
   * 
   * @param colixRing
   * @param colixFill
   * @param diameter
   * @param x
   *        center x
   * @param y
   *        center y
   * @param z
   *        center z
   */
  public abstract void drawFilledCircle(short colixRing, short colixFill,
                                        int diameter, int x, int y, int z);

  public abstract void drawHermite4(int tension, P3i s0, P3i s1,
                                   P3i s2, P3i s3);

  public abstract void drawHermite7(boolean fill, boolean border, int tension,
                                   P3i s0, P3i s1, P3i s2,
                                   P3i s3, P3i s4, P3i s5,
                                   P3i s6, P3i s7, int aspectRatio, short colixBack);

  public abstract void drawImage(Object image, int x, int y, int z, int zslab,
                                 short bgcolix, int width, int height);

  public abstract void drawLine(short colixA, short colixB, int x1, int y1,
                                int z1, int x2, int y2, int z2);

  public abstract void drawLineAB(P3i pointA, P3i pointB);

  public abstract void drawLineXYZ(int x1, int y1, int z1, int x2, int y2, int z2);

  public abstract void drawPixel(int x, int y, int z);

  public abstract void drawPoints(int count, int[] coordinates, int scale);

  public abstract void drawQuadrilateral(short colix, P3i screenA,
                                         P3i screenB, P3i screenC,
                                         P3i screenD);

  /**
   * draws a rectangle
   * 
   * @param x
   *        upper left x
   * @param y
   *        upper left y
   * @param z
   *        upper left z
   * @param zSlab
   *        z for slab check (for set labelsFront)
   * @param rWidth
   *        pixel count
   * @param rHeight
   *        pixel count
   */
  public abstract void drawRect(int x, int y, int z, int zSlab, int rWidth,
                                int rHeight);

  /**
   * draws the specified string in the current font. no line wrapping -- axis,
   * labels, measures
   * 
   * @param str
   *        the String
   * @param font3d
   *        the Font3D
   * @param xBaseline
   *        baseline x
   * @param yBaseline
   *        baseline y
   * @param z
   *        baseline z
   * @param zSlab
   *        z for slab calculation
   * @param bgColix TODO
   */

  public abstract void drawString(String str, Font font3d, int xBaseline,
                                  int yBaseline, int z, int zSlab, short bgColix);

  /**
   * draws the specified string in the current font. no line wrapping -- echo,
   * frank, hover, molecularOrbital, uccage
   * 
   * @param str
   *        the String
   * @param font3d
   *        the Font3D
   * @param xBaseline
   *        baseline x
   * @param yBaseline
   *        baseline y
   * @param z
   *        baseline z
   * @param bgColix TODO
   */

  public abstract void drawStringNoSlab(String str, Font font3d,
                                        int xBaseline, int yBaseline, int z, short bgColix);

  public abstract void drawSurface(MeshSurface meshSurface, short colix);

 public abstract void drawTriangle3C(P3i screenA, short colixA,
                                    P3i screenB, short colixB,
                                    P3i screenC, short colixC, int check);

  public abstract void fillConeSceen3f(byte endcap, int screenDiameter,
                                     P3 screenBase, P3 screenTip);

  public abstract void fillConeScreen(byte endcap, int screenDiameter,
                                      P3i screenBase, P3i screenTip,
                                      boolean isBarb);

  public abstract void fillCylinder(byte endcaps, int diameter,
                                    P3i screenA, P3i screenB);

  public abstract void fillCylinderBits(byte endcaps, int diameter,
                                        P3 screenA, P3 screenB);

  public abstract void fillCylinderScreen(byte endcaps, int diameter, int xA,
                                          int yA, int zA, int xB, int yB, int zB);

  public abstract void fillCylinderScreen3I(byte endcapsOpenend, int diameter,
                                          P3i pt0i, P3i pt1i, P3 pt0f, P3 pt1f, float radius);

  public abstract void fillCylinderXYZ(short colixA, short colixB, byte endcaps,
                                    int diameter, int xA, int yA, int zA,
                                    int xB, int yB, int zB);

  public abstract void fillEllipsoid(P3 center, P3[] points, int x,
                                     int y, int z, int diameter,
                                     M3 mToEllipsoidal, double[] coef,
                                     M4 mDeriv, int selectedOctant,
                                     P3i[] octantPoints);

  public abstract void fillHermite(int tension, int diameterBeg,
                                   int diameterMid, int diameterEnd,
                                   P3i s0, P3i s1, P3i s2,
                                   P3i s3);

  public abstract void fillQuadrilateral(P3 screenA, P3 screenB,
                                         P3 screenC, P3 screenD);

  public abstract void fillQuadrilateral3i(P3i screenA, short colixA,
                                         short normixA, P3i screenB,
                                         short colixB, short normixB,
                                         P3i screenC, short colixC,
                                         short normixC, P3i screenD,
                                         short colixD, short normixD);

  /**
   * fills background rectangle for label
   *<p>
   * 
   * @param x
   *        upper left x
   * @param y
   *        upper left y
   * @param z
   *        upper left z
   * @param zSlab
   *        z value for slabbing
   * @param widthFill
   *        pixel count
   * @param heightFill
   *        pixel count
   */
  public abstract void fillRect(int x, int y, int z, int zSlab, int widthFill,
                                int heightFill);

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *        pixel count
   * @param center
   *        a javax.vecmath.Point3f ... floats are casted to ints
   */
  public abstract void fillSphere(int diameter, P3 center);

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *        pixel count
   * @param center
   *        javax.vecmath.Point3i defining the center
   */

  public abstract void fillSphereI(int diameter, P3i center);

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *        pixel count
   * @param x
   *        center x
   * @param y
   *        center y
   * @param z
   *        center z
   */
  public abstract void fillSphereXYZ(int diameter, int x, int y, int z);

  public abstract void fillTriangle(P3i screenA, short colixA,
                                    short normixA, P3i screenB,
                                    short colixB, short normixB,
                                    P3i screenC, short colixC,
                                    short normixC, float factor);

  public abstract void fillTriangle3CN(P3i screenA, short colixA,
                                    short normixA, P3i screenB,
                                    short colixB, short normixB,
                                    P3i screenC, short colixC, short normixC);

  public abstract void fillTriangle3f(P3 screenA, P3 screenB,
                                    P3 screenC, boolean setNoisy);

  public abstract void fillTriangle3i(P3i screenA, P3i screenB,
                                    P3i screenC, P3 ptA, P3 ptB, P3 ptC);

  public abstract void fillTriangleTwoSided(short normix, int xScreenA,
                                            int yScreenA, int zScreenA,
                                            int xScreenB, int yScreenB,
                                            int zScreenB, int xScreenC,
                                            int yScreenC, int zScreenC);

  public abstract String finalizeOutput();

  public abstract String getExportName();

  public abstract int getExportType();

  public abstract boolean haveTranslucentObjects();

  public abstract Object initializeExporter(Viewer vwr,
                                             double privateKey, GData gdata,
                                             Map<String, Object> params);

  public abstract boolean initializeOutput(Viewer vwr,
                                        double privateKey,
                                        Map<String, Object> params);

  public abstract void plotImagePixel(int argb, int x, int y, int z, int shade, int bgargb);

  public abstract void plotPixelClippedP3i(P3i a);

  public abstract void renderBackground(JmolRendererInterface jre);

  public abstract void renderCrossHairs(int[] minMax, int screenWidth,
                                        int screenHeight,
                                        P3 navigationOffset,
                                        float navigationDepthPercent);

  /**
   * sets current color from colix color index
   * 
   * @param colix
   *        the color index
   * @return true or false if this is the right pass
   */
  public abstract boolean setC(short colix);

  public abstract void volumeRender(boolean TF);

  public abstract void volumeRender4(int diam, int x, int y, int z);

}
