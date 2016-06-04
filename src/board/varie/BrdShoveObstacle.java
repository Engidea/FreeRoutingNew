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

import board.items.BrdItem;

public final class BrdShoveObstacle
   {
   public BrdItem brd_item = null;
   public int on_layer = -1;

   public BrdShoveObstacle (  )
      {
      }

   public BrdShoveObstacle ( BrdItem p_item, int p_on_layer )
      {
      brd_item = p_item;
      on_layer = p_on_layer;
      }
   
   public void clear ()
      {
      brd_item = null;
      on_layer = -1;
      }
   }
