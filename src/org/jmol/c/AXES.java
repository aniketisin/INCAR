/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2011  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.c;

/**
 * Enum for axes mode.
 */
public enum AXES {

  BOUNDBOX(0),
  MOLECULAR(1),
  UNITCELL(2);

  /**
   * Code of axes mode.
   */
  private final int code;

  /**
   * @param code Code of axes mode.
   */
  private AXES(int code) {
    this.code = code;
  }

  /**
   * @return Code of axes mode.
   */
  public int getCode() {
    return code;
  }

  /**
   * @param code Code of axes mode.
   * @return Axes mode.
   */
  public static AXES getAxesMode(int code) {
    for (AXES mode : values()) {
      if (mode.getCode() == code) {
        return mode;
      }
    }
    return null;
  }
}
