/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2014-11-07 04:05:13 +0530 (Fri, 07 Nov 2014) $
 * $Revision: 20090 $
 *
 * Copyright (C) 2002-2006  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.render;

import org.jmol.c.AXES;
import org.jmol.script.T;
import org.jmol.shape.Axes;
import org.jmol.util.GData;
import org.jmol.util.Point3fi;

import javajs.awt.Font;
import javajs.util.P3;

import org.jmol.viewer.StateManager;


public class AxesRenderer extends FontLineShapeRenderer {

  private final static String[] axisLabels = { "+X", "+Y", "+Z", null, null, null, 
                                  "a", "b", "c", 
                                  "X", "Y", "Z", null, null, null,
                                  "X", null, "Z", null, "(Y)", null};

  private final P3[] screens = new P3[6];
  {
    for (int i = 6; --i >= 0; )
      screens[i] = new P3();
  }
  private final P3 originScreen = new P3();
  
  private short[] colixes = new short[3];

  private final static String[] axesTypes = {"a", "b", "c"};

  @Override
  protected void initRenderer() {
    endcap = GData.ENDCAPS_FLAT; 
    draw000 = false;
  }

  @Override
  protected boolean render() {
    Axes axes = (Axes) shape;
    int mad = vwr.getObjectMad(StateManager.OBJ_AXIS1);
    // no translucent axes
    if (mad == 0 || !g3d.checkTranslucent(false))
      return false;
    boolean isXY = (axes.axisXY.z != 0);
    if (!isXY && tm.isNavigating() && vwr.getBoolean(T.navigationperiodic))
      return false;
    AXES axesMode = vwr.g.axesMode;
    imageFontScaling = vwr.getImageFontScaling();
    if (vwr.areAxesTainted()) {
      Font f = axes.font3d;
      axes.initShape();
      if (f != null)
        axes.font3d = f;
    }
    font3d = vwr.gdata.getFont3DScaled(axes.font3d, imageFontScaling);

    int modelIndex = vwr.am.cmi;
    // includes check here for background model present
    boolean isUnitCell = (axesMode == AXES.UNITCELL);
    if (vwr.ms.isJmolDataFrameForModel(modelIndex)
        && !vwr.ms.getJmolFrameType(modelIndex).equals(
            "plot data"))
      return false;
    if (isUnitCell && modelIndex < 0) {
      if (vwr.getCurrentUnitCell() == null)
        return false;
    }
    int nPoints = 6;
    int labelPtr = 0;
    if (isUnitCell && ms.unitCells != null) {
      nPoints = 3;
      labelPtr = 6;
    } else if (isXY) {
      nPoints = 3;
      labelPtr = 9;
    } else if (axesMode == AXES.BOUNDBOX) {
      nPoints = 6;
      labelPtr = (vwr.getBoolean(T.axesorientationrasmol) ? 15 : 9);
    }
    if (axes.labels != null) {
      if (nPoints != 3)
        nPoints = (axes.labels.length < 6 ? 3 : 6);
      labelPtr = -1;
    }
    boolean isDataFrame = vwr.isJmolDataFrame();

    int slab = vwr.gdata.slab;
    int diameter = mad;
    boolean drawTicks = false;
    if (isXY) {
      if (exportType == GData.EXPORT_CARTESIAN)
        return false;
      if (mad >= 20) {
        // width given in angstroms as mAng.
        // max out at 500
        diameter = (mad > 500 ? 5 : mad / 100);
        if (diameter == 0)
          diameter = 2;
      } else {
        if (g3d.isAntialiased())
          diameter += diameter;
      }
      g3d.setSlab(0);
      float z = axes.axisXY.z;
      pt0i.setT(z == Float.MAX_VALUE || z == -Float.MAX_VALUE ? tm.transformPt2D(axes.axisXY): tm.transformPt(axes.axisXY));
      originScreen.set(pt0i.x, pt0i.y, pt0i.z);
      float zoomDimension = vwr.getScreenDim();
      float scaleFactor = zoomDimension / 10f * axes.scale;
      if (g3d.isAntialiased())
        scaleFactor *= 2;
      for (int i = 0; i < 3; i++) {
        tm.rotatePoint(axes.getAxisPoint(i, false), screens[i]);
        screens[i].z *= -1;
        screens[i].scaleAdd2(scaleFactor, screens[i], originScreen);
      }
    } else {
      drawTicks = (axes.tickInfos != null);
      if (drawTicks) {
        if (atomA == null) {
          atomA = new Point3fi();
          atomB = new Point3fi();
        }
        atomA.setT(axes.getOriginPoint(isDataFrame));
      }
      tm.transformPtNoClip(axes.getOriginPoint(isDataFrame), originScreen);
      diameter = getDiameter((int) originScreen.z, mad);
      for (int i = nPoints; --i >= 0;)
        tm.transformPtNoClip(axes.getAxisPoint(i, isDataFrame), screens[i]);
    }
    float xCenter = originScreen.x;
    float yCenter = originScreen.y;
    colixes[0] = vwr.getObjectColix(StateManager.OBJ_AXIS1);
    colixes[1] = vwr.getObjectColix(StateManager.OBJ_AXIS2);
    colixes[2] = vwr.getObjectColix(StateManager.OBJ_AXIS3);
    for (int i = nPoints; --i >= 0;) {
      if (isXY && axes.axisType != null && !axes.axisType.contains(axesTypes [i]))
        continue;
      colix = colixes[i % 3];
      g3d.setC(colix);
      String label = (axes.labels == null ? axisLabels[i + labelPtr]
          : i < axes.labels.length ? axes.labels[i] : null);
      if (label != null && label.length() > 0)
        renderLabel(label, screens[i].x, screens[i].y, screens[i].z, xCenter,
            yCenter);
      if (drawTicks) {
        tickInfo = axes.tickInfos[(i % 3) + 1];
        if (tickInfo == null)
          tickInfo = axes.tickInfos[0];
        atomB.setT(axes.getAxisPoint(i, isDataFrame));
        if (tickInfo != null) {
          tickInfo.first = 0;
          tickInfo.signFactor = (i % 6 >= 3 ? -1 : 1);
        }
      }
      renderLine(originScreen, screens[i], diameter, pt0i, pt1i, drawTicks
          && tickInfo != null);
    }
    if (nPoints == 3 && !isXY) { // a b c [orig]
      String label0 = (axes.labels == null || axes.labels.length == 3 || axes.labels[3] == null ? "0"
          : axes.labels[3]);
      if (label0 != null && label0.length() != 0) {
        colix = vwr.getColixBackgroundContrast();
        g3d.setC(colix);
        renderLabel(label0, originScreen.x, originScreen.y, originScreen.z,
            xCenter, yCenter);
      }
    }
    if (isXY)
      g3d.setSlab(slab);
    return false;
  }
  
  private void renderLabel(String str, float x, float y, float z, float xCenter, float yCenter) {
    int strAscent = font3d.getAscent();
    int strWidth = font3d.stringWidth(str);
    float dx = x - xCenter;
    float dy = y - yCenter;
    if ((dx != 0 || dy != 0)) {
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      dx = (strWidth * 0.75f * dx / dist);
      dy = (strAscent * 0.75f * dy / dist);
      x += dx;
      y += dy;
    }
    double xStrBaseline = Math.floor(x - strWidth / 2f);
    double yStrBaseline = Math.floor(y + strAscent / 2f);
    g3d.drawString(str, font3d, (int) xStrBaseline, (int) yStrBaseline, (int) z, (int) z, (short) 0);
  }
}
