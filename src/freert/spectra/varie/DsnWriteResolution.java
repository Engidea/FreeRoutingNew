package freert.spectra.varie;
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

public class DsnWriteResolution implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String char_name;
   public final int positive_int;

   public DsnWriteResolution(String p_char_name, int p_positive_int)
      {
      char_name = p_char_name;
      positive_int = p_positive_int;
      }

   }
