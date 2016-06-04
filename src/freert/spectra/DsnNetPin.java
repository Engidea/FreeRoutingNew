package freert.spectra;

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
 * Sorted tuple of component name and pin name.
 */
public class DsnNetPin implements Comparable<DsnNetPin>
   {
   public DsnNetPin(String p_component_name, String p_pin_name)
      {
      component_name = p_component_name;
      pin_name = p_pin_name;
      }

   public int compareTo(DsnNetPin p_other)
      {
      int result = this.component_name.compareTo(p_other.component_name);
      if (result == 0)
         {
         result = this.pin_name.compareTo(p_other.pin_name);
         }
      return result;
      }

   public final String component_name;
   public final String pin_name;
   }