package freert.spectra.varie;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 */

import freert.planar.PlaArea;

/** 
 * Deescribes a named keepout belonging to a package, 
 */
public final class DsnPackageKeepout implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String name;
   public final PlaArea area;
   public final int layer;

   public DsnPackageKeepout(String p_name, PlaArea p_area, int p_layer)
      {
      name = p_name;
      area = p_area;
      layer = p_layer;
      }
   }
