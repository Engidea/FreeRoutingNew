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

import java.util.SortedSet;

public class DsnLogicalPartMapping
   {
   // The name of the mapping
   public final String name;

   // The components belonging to the mapping
   public final SortedSet<String> components;

   public DsnLogicalPartMapping(String p_name, SortedSet<String> p_components)
      {
      name = p_name;
      components = p_components;
      }

   }