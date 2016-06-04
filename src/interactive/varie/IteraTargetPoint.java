package interactive.varie;

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

import board.items.BrdItem;
import freert.planar.PlaPointFloat;

public class IteraTargetPoint
   {
   public final PlaPointFloat location;
   public final BrdItem item;

   public IteraTargetPoint(PlaPointFloat p_location, BrdItem p_item)
      {
      location = p_location;
      item = p_item;
      }
   }
