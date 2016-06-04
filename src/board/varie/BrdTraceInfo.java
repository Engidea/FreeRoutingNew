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

/**
 * Auxiliary class used in the method move_by
 */
public class BrdTraceInfo implements Comparable<BrdTraceInfo>
   {
   public final int layer;
   public final int half_width;
   public final int clearance_type;

   public BrdTraceInfo(int p_layer, int p_half_width, int p_clearance_type)
      {
      layer = p_layer;
      half_width = p_half_width;
      clearance_type = p_clearance_type;
      }

   /**
    * Implements the comparable interface.
    */
   public int compareTo(BrdTraceInfo p_other)
      {
      return p_other.layer - layer;
      }

   }
