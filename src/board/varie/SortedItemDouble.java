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
import freert.varie.Signum;

/**
 * Used to sort the group items in the direction of translate_vector, so that the front items can be moved first.
 * It is kind of generic, when you want to sort Items by some kind of value
 */
public final class SortedItemDouble implements Comparable<SortedItemDouble>
   {
   public final BrdItem item;
   public final double projection;

   public SortedItemDouble(BrdItem p_item, double p_projection)
      {
      item = p_item;
      projection = p_projection;
      }

   @Override
   public int compareTo(SortedItemDouble p_other)
      {
      return Signum.as_int(projection - p_other.projection);
      }
   }
