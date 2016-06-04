package board.varie;
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

import freert.planar.PlaPointInt;

public final class BrdKeepPoint
   {
   // the point to keep 
   public final PlaPointInt keep_point;
   // keep point layer....
   public final int on_layer;

   /**
    * If you use it then the values MUST be non null
    * @param p_keep_point
    * @param p_on_layer
    */
   public BrdKeepPoint ( PlaPointInt p_keep_point, int p_on_layer )
      {
      if ( p_keep_point == null )
         throw new IllegalArgumentException("p_keep_point == null");

      keep_point = p_keep_point;
      on_layer = p_on_layer;
      }
   }
