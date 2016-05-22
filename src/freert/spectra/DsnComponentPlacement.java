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
 * ComponentPlacement.java
 *
 * Created on 20. Mai 2004, 07:43
 */

package freert.spectra;

import java.util.Collection;
import java.util.LinkedList;


/**
 * Describes the placement data of a library component
 *
 * @author alfons
 */
public class DsnComponentPlacement
   {
   // The name of the corresponding library component 
   public final String lib_name;

   // The list of ComponentLocations of the library component on the board. 
   public final Collection<DsnComponentLocation> locations;

   public DsnComponentPlacement(String p_lib_name)
      {
      lib_name = p_lib_name;
      locations = new LinkedList<DsnComponentLocation>();
      }

   }
