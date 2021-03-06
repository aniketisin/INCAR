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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.shape;


import org.jmol.java.BS;
import org.jmol.viewer.JC;
import org.jmol.viewer.StateManager;

public class Bbcage extends FontLineShape {

  @Override
  public void setProperty(String propertyName, Object value, BS bs) {
    setPropFLS(propertyName, value);
  }
  
  @Override
  public void initShape() {
    super.initShape();
    font3d = vwr.gdata.getFont3D(JC.AXES_DEFAULT_FONTSIZE);
    myType = "boundBox";
  }

  public boolean isVisible;
  
  @Override
  public void setModelVisibilityFlags(BS bs) {
    BS bboxModels;
    isVisible = ((mad = (short) vwr.getObjectMad(StateManager.OBJ_BOUNDBOX)) != 0 
        && ((bboxModels = vwr.ms.getBoundBoxModels()) == null 
          || bs.intersects(bboxModels)));
  }
  
}
