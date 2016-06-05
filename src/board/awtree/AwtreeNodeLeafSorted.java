package board.awtree;

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
 * created for sorting Items according to their clearance to p_cl_type on layer p_layer
 */
public final class AwtreeNodeLeafSorted implements Comparable<AwtreeNodeLeafSorted>
   {
   static private int last_generated_id_no = 1;

   private final int entry_id_no;

   public final AwtreeNodeLeaf leaf;
   public final int clearance;
   
   
   public AwtreeNodeLeafSorted(AwtreeNodeLeaf p_leaf, int p_clearance)
      {
      leaf = p_leaf;
      clearance = p_clearance;
      entry_id_no = last_generated_id_no++;

      // I wonder what happens when eventually it wraps araound...
      if (last_generated_id_no >= Integer.MAX_VALUE) last_generated_id_no = 1;
      }

   public int compareTo(AwtreeNodeLeafSorted p_other)
      {
      if (clearance != p_other.clearance) return clearance - p_other.clearance;

      return entry_id_no - p_other.entry_id_no;
      }
   }
