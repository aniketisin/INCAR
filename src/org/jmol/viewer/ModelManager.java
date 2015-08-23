/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2014-04-04 21:58:30 +0530 (Fri, 04 Apr 2014) $
 * $Revision: 19601 $

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
package org.jmol.viewer;


import org.jmol.java.BS;
import org.jmol.modelset.ModelLoader;
import org.jmol.modelset.ModelSet;

import javajs.util.SB;

class ModelManager {

  private final Viewer vwr;
  ModelSet modelSet;

  private String modelSetPathName;
  private String fileName;

  ModelManager(Viewer vwr) {
    this.vwr = vwr;
  }

  void zap() {
    modelSetPathName = fileName = null;
    new ModelLoader(vwr, vwr.getZapName(), null, null, null, null);
  }
  
  String getModelSetFileName() {
    return (fileName != null ? fileName : vwr.getZapName());
  }

  String getModelSetPathName() {
    return modelSetPathName;
  }
  
  void createModelSet(String fullPathName, String fileName,
                          SB loadScript, Object atomSetCollection,
                          BS bsNew, boolean isAppend) {
    String modelSetName = null;
    if (isAppend) {
      modelSetName = modelSet.modelSetName;
      if (modelSetName.equals("zapped"))
        modelSetName = null;
      else if (modelSetName.indexOf(" (modified)") < 0)
        modelSetName += " (modified)";
    } else if (atomSetCollection == null) {
      zap();
    } else {
      this.modelSetPathName = fullPathName;
      this.fileName = fileName;
    }
    if (atomSetCollection != null) {
      if (modelSetName == null) {
        modelSetName = vwr.getModelAdapter().getAtomSetCollectionName(
            atomSetCollection);
        if (modelSetName != null) {
          modelSetName = modelSetName.trim();
          if (modelSetName.length() == 0)
            modelSetName = null;
        }
        if (modelSetName == null)
          modelSetName = reduceFilename(fileName);
      }
      new ModelLoader(vwr, modelSetName, loadScript,
          atomSetCollection, (isAppend ? modelSet : null), bsNew);
    }
    if (modelSet.getAtomCount() == 0 && !modelSet.getMSInfoB("isPyMOL"))
      zap();
  }

  private static String reduceFilename(String fileName) {
    if (fileName == null)
      return null;
    int ichDot = fileName.indexOf('.');
    if (ichDot > 0)
      fileName = fileName.substring(0, ichDot);
    if (fileName.length() > 24)
      fileName = fileName.substring(0, 20) + " ...";
    return fileName;
  }

  String createAtomDataSet(Object atomSetCollection, int tokType) {
    return ModelLoader.createAtomDataSet(vwr, modelSet, tokType, atomSetCollection,
    vwr.bsA());
  }

}
