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

import java.util.ArrayList;
import java.util.Collection;
import board.items.BrdItem;
import freert.planar.PlaPointInt;
import freert.varie.PlaDelTriStorable;

public final class IteraNetItem implements PlaDelTriStorable
   {
   public final BrdItem item;
   public Collection<BrdItem> connected_set;


   public IteraNetItem(BrdItem p_item, Collection<BrdItem> p_connected_set)
      {
      item = p_item;
      connected_set = p_connected_set;
      }

   @Override
   public ArrayList<PlaPointInt> get_triangulation_corners()
      {
      return item.get_ratsnest_corners();
      }

   }
