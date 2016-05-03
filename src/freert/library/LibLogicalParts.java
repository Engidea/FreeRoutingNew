/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
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
 * LogicalParts.java
 *
 * Created on 26. Maerz 2005, 06:08
 */

package freert.library;

import java.util.Vector;

/**
 * The logical parts contain information for gate swap and pin swap.
 *
 * @author Alfons Wirtz
 */
public final class LibLogicalParts implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   private final Vector<LibLogicalPart> part_arr = new Vector<LibLogicalPart>();

   public LibLogicalPart add(String p_name, LibLogicalPin[] p_part_pin_arr)
      {
      java.util.Arrays.sort(p_part_pin_arr);
      LibLogicalPart new_part = new LibLogicalPart(p_name, part_arr.size() + 1, p_part_pin_arr);
      part_arr.add(new_part);
      return new_part;
      }

   /**
    * Returns the logical part with the input name or null, if no such package exists.
    */
   public LibLogicalPart get(String p_name)
      {
      for (LibLogicalPart curr_part : this.part_arr)
         {
         if (curr_part != null && curr_part.name.compareToIgnoreCase(p_name) == 0)
            {
            return curr_part;
            }
         }
      return null;
      }

   /**
    * Returns the logical part with index p_part_no. Part numbers are from 1 to part count.
    */
   public LibLogicalPart get(int p_part_no)
      {
      LibLogicalPart result = part_arr.elementAt(p_part_no - 1);
      if (result != null && result.part_no != p_part_no)
         {
         System.out.println("LogicalParts.get: inconsistent part number");
         }
      return result;
      }

   /**
    * Returns the count of logical parts.
    */
   public int count()
      {
      return part_arr.size();
      }

   }
