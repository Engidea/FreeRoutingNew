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

public class DsnNetId implements Comparable<DsnNetId>
   {
   public DsnNetId(String p_name, int p_subnet_number)
      {
      name = p_name;
      subnet_number = p_subnet_number;
      }

   public int compareTo(DsnNetId p_other)
      {
      int result = this.name.compareTo(p_other.name);
      if (result == 0)
         {
         result = this.subnet_number - p_other.subnet_number;
         }
      return result;
      }

   public final String name;
   public final int subnet_number;
   }
