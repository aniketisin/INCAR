/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-10-07 20:10:15 -0500 (Sun, 07 Oct 2007) $
 * $Revision: 8384 $
 *
 * Copyright (C) 2003-2006  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.export;

import java.util.Map;

import org.jmol.api.Interface;
import org.jmol.api.JmolRendererInterface;
import org.jmol.g3d.HermiteRenderer;
import org.jmol.modelset.Atom;
import org.jmol.script.T;
import org.jmol.util.GData;
import org.jmol.util.MeshSurface;

import javajs.awt.Font;
import javajs.util.M3;
import javajs.util.M4;
import javajs.util.P3;
import javajs.util.P3i;
import org.jmol.viewer.Viewer;

public class Export3D implements JmolRendererInterface {

  protected ___Exporter exporter;

  private double privateKey;

  private GData gdata;
  private short colix;
  private HermiteRenderer hermite3d;
  private int width;
  private int height;
  private int slab;

  String exportName;

  private boolean isWebGL;

  public Export3D() {
    // by reflection
  }

  @Override
  public Object initializeExporter(Viewer vwr, double privateKey,
                                   GData gdata, Map<String, Object> params) {
    exportName = (String) params.get("type");
    isWebGL = exportName.equals("JS");
    if ((exporter = (___Exporter) Interface.getOption("export." + (isWebGL ? "" : "_") + exportName + "Exporter", vwr, "export")) == null)
      return null;
    this.gdata = gdata;
    exporter.setRenderer(this);
    gdata.setNewWindowParametersForExport();
    slab = gdata.slab;
    width = gdata.width;
    height = gdata.height;
    this.privateKey = privateKey;
    return (initializeOutput(vwr, privateKey, params) ? exporter : null);
  }

  @Override
  public boolean initializeOutput(Viewer vwr, double privateKey,
                                  Map<String, Object> params) {
    return exporter.initializeOutput(vwr, privateKey, gdata, params);
  }

  @Override
  public int getExportType() {
    return exporter.exportType;
  }

  @Override
  public String getExportName() {
    return exportName;
  }

  @Override
  public String finalizeOutput() {
    return exporter.finalizeOutput();
  }

  @Override
  public void setSlab(int slabValue) {
    gdata.setSlab(slabValue);
    slab = gdata.slab;
  }

  @Override
  public void setDepth(int depthValue) {
    // no equivalent in exporters?
    gdata.setDepth(depthValue);
  }

  @Override
  public void renderBackground(JmolRendererInterface me) {
    if (exporter.exportType == GData.EXPORT_RAYTRACER)
      gdata.renderBackground(me);
  }

  @Override
  public void drawAtom(Atom atom) {
    exporter.drawAtom(atom);
  }

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
  @Override
  public void drawRect(int x, int y, int z, int zSlab, int rWidth, int rHeight) {
    if (isWebGL) {
      //TODO
      return;
    }
    // labels (and rubberband, not implemented) and navigation cursor
    if (zSlab != 0 && gdata.isClippedZ(zSlab))
      return;
    int w = rWidth - 1;
    int h = rHeight - 1;
    int xRight = x + w;
    int yBottom = y + h;
    if (y >= 0 && y < height)
      drawHLine(x, y, z, w);
    if (yBottom >= 0 && yBottom < height)
      drawHLine(x, yBottom, z, w);
    if (x >= 0 && x < width)
      drawVLine(x, y, z, h);
    if (xRight >= 0 && xRight < width)
      drawVLine(xRight, y, z, h);
  }

  /**
   * @param x
   * @param y
   * @param z
   * @param w
   */

  private void drawHLine(int x, int y, int z, int w) {
    // hover, labels only
    //OVERRIDDED IN JS
    int argbCurrent = gdata.getColorArgbOrGray(colix);
    if (w < 0) {
      x += w;
      w = -w;
    }
    for (int i = 0; i <= w; i++) {
      exporter.drawTextPixel(argbCurrent, x + i, y, z);
    }
  }

  /**
   * @param x
   * @param y
   * @param z
   * @param h
   */
  private void drawVLine(int x, int y, int z, int h) {
    // hover, labels only
    // OVERRIDDED IN JS
    int argbCurrent = gdata.getColorArgbOrGray(colix);
    if (h < 0) {
      y += h;
      h = -h;
    }
    for (int i = 0; i <= h; i++) {
      exporter.drawTextPixel(argbCurrent, x, y + i, z);
    }
  }

  /**
   * draws a screened circle ... every other dot is turned on
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

  @Override
  public void drawFilledCircle(short colixRing, short colixFill, int diameter,
                               int x, int y, int z) {
    // halos, draw
    if (!gdata.isClippedZ(z))
      exporter.drawFilledCircle(colixRing, colixFill, diameter, x, y, z);
  }

  /**
   * draws a simple circle (draw circle)
   * 
   * @param colix
   *        the color index
   * @param diameter
   *        the pixel diameter
   * @param x
   *        center x
   * @param y
   *        center y
   * @param z
   *        center z
   * @param doFill
   *        (not implemented in exporters)
   */

  public void drawCircle(short colix, int diameter, int x, int y, int z,
                         boolean doFill) {
    // halos, draw
    if (!gdata.isClippedZ(z))
      exporter.drawCircle(x, y, z, diameter, colix, doFill);
  }

  private P3 ptA = new P3();
  private P3 ptB = new P3();
  private P3 ptC = new P3();
  private P3 ptD = new P3();
  /*
   * private Point3f ptE = new Point3f(); private Point3f ptF = new Point3f();
   * private Point3f ptG = new Point3f(); private Point3f ptH = new Point3f();
   */
  private P3i ptAi = new P3i();
  private P3i ptBi = new P3i();

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
  @Override
  public void fillSphereXYZ(int diameter, int x, int y, int z) {
    ptA.set(x, y, z);
    fillSphere(diameter, ptA);
  }

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *        pixel count
   * @param center
   *        javax.vecmath.Point3i defining the center
   */

  @Override
  public void fillSphereI(int diameter, P3i center) {
    // dashed line; mesh line; render mesh points; lone pair; renderTriangles
    ptA.set(center.x, center.y, center.z);
    fillSphere(diameter, ptA);
  }

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *        pixel count
   * @param center
   *        a javax.vecmath.Point3f ... floats are casted to ints
   */
  @Override
  public void fillSphere(int diameter, P3 center) {
    if (diameter == 0)
      return;
    exporter.fillSphere(colix, diameter, center);
  }

  /**
   * fills background rectangle for label
   * <p>
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
  @Override
  public void fillRect(int x, int y, int z, int zSlab, int widthFill,
                       int heightFill) {
    // hover and labels only -- slab at atom or front -- simple Z/window clip
    if (gdata.isClippedZ(zSlab))
      return;
    ptA.set(x, y, z);
    ptB.set(x + widthFill, y, z);
    ptC.set(x + widthFill, y + heightFill, z);
    ptD.set(x, y + heightFill, z);
    fillQuadrilateral(ptA, ptB, ptC, ptD);
  }

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
   * @param bgcolix
   */

  @Override
  public void drawString(String str, Font font3d, int xBaseline, int yBaseline,
                         int z, int zSlab, short bgcolix) {
    // axis, labels, measures
    if (str != null && !gdata.isClippedZ(zSlab))
      drawStringNoSlab(str, font3d, xBaseline, yBaseline, z, bgcolix);
  }

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
   * @param bgcolix
   */

  @Override
  public void drawStringNoSlab(String str, Font font3d, int xBaseline,
                               int yBaseline, int z, short bgcolix) {
    // echo, frank, hover, molecularOrbital, uccage
    if (str == null)
      return;
    z = Math.max(slab, z);
    if (font3d == null)
      font3d = gdata.getFont3DCurrent();
    else
      gdata.setFont(font3d);
    exporter.plotText(xBaseline, yBaseline, z, colix, str, font3d);
  }

  @Override
  public void drawImage(Object objImage, int x, int y, int z, int zSlab,
                        short bgcolix, int width, int height) {
    if (objImage == null || width == 0 || height == 0)
      return;
    if (gdata.isClippedZ(zSlab))
      return;
    z = Math.max(slab, z);
    exporter.plotImage(x, y, z, objImage, bgcolix, width, height);
  }

  // mostly public drawing methods -- add "public" if you need to

  /*
   * *************************************************************** points
   * **************************************************************
   */

  @Override
  public void drawPixel(int x, int y, int z) {
    // measures - render angle
    plotPixelClipped(x, y, z);
  }

  void plotPixelClipped(int x, int y, int z) {
    // circle3D, drawPixel, plotPixelClipped(point3)
    if (isClipped(x, y, z))
      return;
    exporter.drawPixel(colix, x, y, z, 1);
  }

  @Override
  public void plotPixelClippedP3i(P3i screen) {
    if (isClipped(screen.x, screen.y, screen.z))
      return;
    // circle3D, drawPixel, plotPixelClipped(point3)
    exporter.drawPixel(colix, screen.x, screen.y, screen.z, 1);
  }

  @Override
  public void drawPoints(int count, int[] coordinates, int scale) {
    for (int i = count * 3; i > 0;) {
      int z = coordinates[--i];
      int y = coordinates[--i];
      int x = coordinates[--i];
      if (isClipped(x, y, z))
        continue;
      exporter.drawPixel(colix, x, y, z, scale);
    }
  }

  /*
   * *************************************************************** lines and
   * cylinders **************************************************************
   */

  @Override
  public void drawDashedLine(int run, int rise, P3i pointA, P3i pointB) {
    // axes and such -- ignored dashed for exporters
    drawLineAB(pointA, pointB);
    // ptA.set(pointA.x, pointA.y, pointA.z);
    // ptB.set(pointB.x, pointB.y, pointB.z);
    // exporter.drawDashedLine(colix, run, rise, ptA, ptB);
  }

  @Override
  public void drawDottedLine(P3i pointA, P3i pointB) {
    // TODO
    // axes, bbcage only
    drawLineAB(pointA, pointB); // Temporary only
    // ptA.set(pointA.x, pointA.y, pointA.z);
    // ptB.set(pointB.x, pointB.y, pointB.z);
    // exporter.drawDashedLine(colix, 2, 1, ptA, ptB);
  }

  @Override
  public void drawLineXYZ(int x1, int y1, int z1, int x2, int y2, int z2) {
    // stars
    ptAi.set(x1, y1, z1);
    ptBi.set(x2, y2, z2);
    drawLineAB(ptAi, ptBi);
  }

  @Override
  public void drawLine(short colixA, short colixB, int xA, int yA, int zA,
                       int xB, int yB, int zB) {
    // line bonds, line backbone, drawTriangle
    fillCylinderXYZ(colixA, colixB, GData.ENDCAPS_FLAT, exporter.lineWidthMad,
        xA, yA, zA, xB, yB, zB);
  }

  @Override
  public void drawLineAB(P3i pointA, P3i pointB) {
    // draw quadrilateral and hermite, stars
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    exporter.fillCylinderScreenMad(colix, GData.ENDCAPS_FLAT,
        exporter.lineWidthMad, ptA, ptB);
  }

  @Override
  public void drawBond(P3 atomA, P3 atomB, short colixA, short colixB,
                       byte endcaps, short mad, int bondOrder) {
    // from SticksRenderer to allow for a direct
    // writing of single bonds -- just for efficiency here 
    // bondOrder == -1 indicates we have cartesian coordinates and we want to draw endcaps
    if (mad == 1)
      mad = exporter.lineWidthMad;
    exporter
        .drawCylinder(atomA, atomB, colixA, colixB, endcaps, mad, bondOrder);
  }

  @Override
  public void fillCylinderXYZ(short colixA, short colixB, byte endcaps,
                              int mad, int xA, int yA, int zA, int xB, int yB,
                              int zB) {
    /*
     * from drawLine, Sticks, fillCylinder, backbone
     * 
     */
    ptA.set(xA, yA, zA);
    ptB.set(xB, yB, zB);
    // bond order 1 here indicates that we have screen coordinates
    exporter.drawCylinder(ptA, ptB, colixA, colixB, endcaps, mad, 1);
  }

  @Override
  public void fillCylinderScreen(byte endcaps, int screenDiameter, int xA,
                                 int yA, int zA, int xB, int yB, int zB) {
    // vectors, polyhedra
    ptA.set(xA, yA, zA);
    ptB.set(xB, yB, zB);
    exporter.fillCylinderScreen(colix, endcaps, screenDiameter, ptA, ptB, null,
        null, 0);
  }

  @Override
  public void fillCylinderScreen3I(byte endcaps, int diameter, P3i pointA,
                                   P3i pointB, P3 pt0f, P3 pt1f, float radius) {
    // from Draw arrow and NucleicMonomer
    if (diameter <= 0)
      return;
    if (!exporter.isCartesian) {
      ptA.set(pointA.x, pointA.y, pointA.z);
      ptB.set(pointB.x, pointB.y, pointB.z);
    }
    exporter.fillCylinderScreen(colix, endcaps, diameter, ptA, ptB, pt0f, pt1f,
        radius);
  }

  /*
   * *************************************************************** triangles
   * **************************************************************
   */

  @Override
  public void fillCylinder(byte endcaps, int diameter, P3i pointA, P3i pointB) {
    if (diameter <= 0)
      return;
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    exporter.fillCylinderScreenMad(colix, endcaps, diameter, ptA, ptB);
  }

  @Override
  public void fillCylinderBits(byte endcaps, int diameter, P3 pointA, P3 pointB) {
    if (diameter <= 0)
      return;
    exporter.fillCylinderScreenMad(colix, endcaps, diameter, pointA, pointB);
  }

  @Override
  public void fillConeScreen(byte endcap, int screenDiameter, P3i pointBase,
                             P3i screenTip, boolean isBarb) {
    // dipole, vector, draw arrow/vector
    ptA.set(pointBase.x, pointBase.y, pointBase.z);
    ptB.set(screenTip.x, screenTip.y, screenTip.z);
    exporter.fillConeScreen(colix, endcap, screenDiameter, ptA, ptB, isBarb);
  }

  @Override
  public void fillConeSceen3f(byte endcap, int screenDiameter, P3 pointBase,
                              P3 screenTip) {
    // cartoons, rockets
    exporter.fillConeScreen(colix, endcap, screenDiameter, pointBase,
        screenTip, false);
  }

  @Override
  public void drawHermite4(int tension, P3i s0, P3i s1, P3i s2, P3i s3) {
    // strands
    hermite3d.renderHermiteRope(false, tension, 0, 0, 0, s0, s1, s2, s3);
  }

  @Override
  public void fillHermite(int tension, int diameterBeg, int diameterMid,
                          int diameterEnd, P3i s0, P3i s1, P3i s2, P3i s3) {
    hermite3d.renderHermiteRope(true, tension, diameterBeg, diameterMid,
        diameterEnd, s0, s1, s2, s3);
  }

  @Override
  public void drawTriangle3C(P3i screenA, short colixA, P3i screenB,
                             short colixB, P3i screenC, short colixC, int check) {
    // primary method for mapped Mesh
    if ((check & 1) == 1)
      drawLine(colixA, colixB, screenA.x, screenA.y, screenA.z, screenB.x,
          screenB.y, screenB.z);
    if ((check & 2) == 2)
      drawLine(colixB, colixC, screenB.x, screenB.y, screenB.z, screenC.x,
          screenC.y, screenC.z);
    if ((check & 4) == 4)
      drawLine(colixA, colixC, screenA.x, screenA.y, screenA.z, screenC.x,
          screenC.y, screenC.z);
  }

//  @Override
//  public void drawTriangle3I(P3i screenA, P3i screenB, P3i screenC, int check) {
//    // primary method for mapped Mesh
//    if ((check & 1) == 1)
//      drawLine(colix, colix, screenA.x, screenA.y, screenA.z, screenB.x,
//          screenB.y, screenB.z);
//    if ((check & 2) == 2)
//      drawLine(colix, colix, screenB.x, screenB.y, screenB.z, screenC.x,
//          screenC.y, screenC.z);
//    if ((check & 4) == 4)
//      drawLine(colix, colix, screenA.x, screenA.y, screenA.z, screenC.x,
//          screenC.y, screenC.z);
//  }

  /*
   * public void drawfillTriangle(int xA, int yA, int zA, int xB, int yB, int
   * zB, int xC, int yC, int zC) { ptA.set(xA, yA, zA); ptB.set(xB, yB, zB);
   * ptC.set(xC, yC, zC); fillTriangle(ptA, ptB, ptC); }
   */

  @Override
  public void fillTriangle3CN(P3i pointA, short colixA, short normixA,
                              P3i pointB, short colixB, short normixB,
                              P3i pointC, short colixC, short normixC) {
    // mesh, isosurface
    if (colixA != colixB || colixB != colixC) {
      // shouldn't be here, because that uses renderIsosurface
      return;
    }
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    ptC.set(pointC.x, pointC.y, pointC.z);
    exporter.fillTriangle(colixA, ptA, ptB, ptC, false, false);
  }

  @Override
  public void fillTriangleTwoSided(short normix, int xpointA, int ypointA,
                                   int zpointA, int xpointB, int ypointB,
                                   int zpointB, int xpointC, int ypointC,
                                   int zpointC) {
    // polyhedra
    ptA.set(xpointA, ypointA, zpointA);
    ptB.set(xpointB, ypointB, zpointB);
    ptC.set(xpointC, ypointC, zpointC);
    exporter.fillTriangle(colix, ptA, ptB, ptC, true, false);
  }

  @Override
  public void fillTriangle3f(P3 pointA, P3 pointB, P3 pointC, boolean setNoisy) {
    // rockets
    exporter.fillTriangle(colix, pointA, pointB, pointC, false, false);
  }

  @Override
  public void fillTriangle3i(P3i screenA, P3i screenB, P3i screenC, P3 ptA0,
                             P3 ptB0, P3 ptC0) {
    // cartoon only, for nucleic acid bases
    if (exporter.isCartesian) {
      exporter.fillTriangle(colix, ptA0, ptB0, ptC0, true, true);
    } else {
      ptA.set(screenA.x, screenA.y, screenA.z);
      ptB.set(screenB.x, screenB.y, screenB.z);
      ptC.set(screenC.x, screenC.y, screenC.z);
      exporter.fillTriangle(colix, ptA, ptB, ptC, true, false);
    }
  }

  // was in js:

  //  @Override
  //  public void fillTriangle3i(P3i pointA, P3i pointB, P3i pointC, P3 ptA,
  //                             P3 ptB, P3 ptC) {
  //    // cartoon only, for nucleic acid bases
  //    exporter.fillTriangle(colix, ptA, ptB, ptC, true, true);
  //  }

  @Override
  public void fillTriangle(P3i pointA, short colixA, short normixA, P3i pointB,
                           short colixB, short normixB, P3i pointC,
                           short colixC, short normixC, float factor) {
    fillTriangle3CN(pointA, colixA, normixA, pointB, colixB, normixB, pointC,
        colixC, normixC);
  }

  /*
   * ***************************************************************
   * quadrilaterals
   * **************************************************************
   */

  @Override
  public void drawQuadrilateral(short colix, P3i pointA, P3i pointB,
                                P3i pointC, P3i screenD) {
    // mesh only -- translucency has been checked
    setC(colix);
    drawLineAB(pointA, pointB);
    drawLineAB(pointB, pointC);
    drawLineAB(pointC, screenD);
    drawLineAB(screenD, pointA);
  }

  @Override
  public void fillQuadrilateral(P3 pointA, P3 pointB, P3 pointC, P3 pointD) {
    // hermite, rockets, cartoons
    exporter.fillTriangle(colix, pointA, pointB, pointC, false, false);
    exporter.fillTriangle(colix, pointA, pointC, pointD, false, false);
  }

  @Override
  public void fillQuadrilateral3i(P3i pointA, short colixA, short normixA,
                                  P3i pointB, short colixB, short normixB,
                                  P3i pointC, short colixC, short normixC,
                                  P3i screenD, short colixD, short normixD) {
    // mesh
    fillTriangle3CN(pointA, colixA, normixA, pointB, colixB, normixB, pointC,
        colixC, normixC);
    fillTriangle3CN(pointA, colixA, normixA, pointC, colixC, normixC, screenD,
        colixD, normixD);
  }

  @Override
  public void drawSurface(MeshSurface meshSurface, short colix) {
    exporter.drawSurface(meshSurface, colix);
  }

//  @Override
//  public short[] getBgColixes(short[] bgcolixes) {
//    // 3D exporters cannot do background labels
//    return exporter.exportType == GData.EXPORT_CARTESIAN ? null : bgcolixes;
//  }

  @Override
  public void fillEllipsoid(P3 center, P3[] points, int x, int y, int z,
                            int diameter, M3 mToEllipsoidal, double[] coef,
                            M4 mDeriv, int selectedOctant, P3i[] octantPoints) {
    exporter.fillEllipsoid(center, points, colix, x, y, z, diameter,
        mToEllipsoidal, coef, mDeriv, octantPoints);
  }

  @Override
  public boolean drawEllipse(P3 ptAtom, P3 ptX, P3 ptY, boolean fillArc,
                             boolean wireframeOnly) {
    return exporter.drawEllipse(ptAtom, ptX, ptY, colix, fillArc);
  }

  /*
   * *************************************************************** g3d-relayed
   * info specifically needed for the renderers
   * **************************************************************
   */

  /**
   * is full scene / oversampling antialiasing in effect
   * 
   * @return the answer
   */
  @Override
  public boolean isAntialiased() {
    return false;
  }

  @Override
  public boolean checkTranslucent(boolean isAlphaTranslucent) {
    return true;
  }

  @Override
  public boolean haveTranslucentObjects() {
    return true;
  }

  /**
   * sets current color from colix color index
   * 
   * @param colix
   *        the color index
   * @return true or false if this is the right pass
   */
  @Override
  public boolean setC(short colix) {
    this.colix = colix;
    gdata.setC(colix);
    return true;
  }

  @Override
  public boolean isInDisplayRange(int x, int y) {
    return (exporter.exportType == GData.EXPORT_CARTESIAN || gdata.isInDisplayRange(x, y));
  }

  public int clipCode(int x, int y, int z) {
    return (exporter.exportType == GData.EXPORT_CARTESIAN ? gdata.clipCode(z)
        : gdata.clipCode3(x, y, z));
  }

  @Override
  public boolean isClippedXY(int diameter, int x, int y) {
    return (exporter.exportType != GData.EXPORT_CARTESIAN && gdata.isClippedXY(diameter, x, y));
  }

  public boolean isClipped(int x, int y, int z) {
    return (gdata.isClippedZ(z) || isClipped(x, y));
  }

  protected boolean isClipped(int x, int y) {
    return (exporter.exportType != GData.EXPORT_CARTESIAN && gdata.isClipped(x, y));
  }

  public double getPrivateKey() {
    return privateKey;
  }

  @Override
  public void volumeRender4(int diam, int x, int y, int z) {
    fillSphereXYZ(diam, x, y, z);
  }

  @Override
  public void renderCrossHairs(int[] minMax, int screenWidth, int screenHeight,
                               P3 navigationOffset, float navigationDepthPercent) {
  }

  @Override
  public void volumeRender(boolean TF) {
    // TODO

  }

  @Override
  public void addRenderer(int tok) {
    if (tok == T.hermitelevel)
      hermite3d = (HermiteRenderer) new HermiteRenderer().set(this, gdata);
  }

  @Override
  public void plotImagePixel(int argb, int x, int y, int z, int shade,
                             int bgargb) {
    // from Text3D
    if (isWebGL)
      return;
    z = Math.max(slab, z);
    if (shade != 0) {
      // so shade 1 ==> 0xEE (almost opaque)
      //    shade 7 ==> 0x11 (almost transparent)
      int a = (shade == 8 ? 0xFF : ((8 - shade) << 4) + (8 - shade));
      argb = (argb & 0xFFFFFF) | (a << 24);
    }
    exporter.drawTextPixel(argb, x, y, z);
  }

  @Override
  public void drawHermite7(boolean fill, boolean border, int tension, P3i s0,
                           P3i s1, P3i s2, P3i s3, P3i s4, P3i s5, P3i s6,
                           P3i s7, int aspectRatio, short colixBack) {
    if (colixBack == 0 || isWebGL) {
      hermite3d.renderHermiteRibbon(fill, border, tension, s0, s1, s2, s3, s4,
          s5, s6, s7, aspectRatio, 0);
      return;
    }
    hermite3d.renderHermiteRibbon(fill, border, tension, s0, s1, s2, s3, s4,
        s5, s6, s7, aspectRatio, 1);
    short colix = this.colix;
    setC(colixBack);
    hermite3d.renderHermiteRibbon(fill, border, tension, s0, s1, s2, s3, s4,
        s5, s6, s7, aspectRatio, -1);
    setC(colix);
  }

  @Override
  public void renderAllStrings(Object jr) {
    if (isWebGL) {
      // TODO
      return;
    }
    gdata.renderAllStrings(this);
  }

}
