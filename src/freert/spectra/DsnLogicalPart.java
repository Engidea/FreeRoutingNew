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

public class DsnLogicalPart
   {
   public DsnLogicalPart(String p_name, java.util.Collection<DsnPartPin> p_part_pins)
      {
      name = p_name;
      part_pins = p_part_pins;
      }

   /** The name of the maopping. */
   public final String name;

   /** The pins of this logical part */
   public final java.util.Collection<DsnPartPin> part_pins;
   }
