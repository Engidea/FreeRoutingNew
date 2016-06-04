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

/** A max_length of -1 indicates, tha no maximum length is defined. */
public class DsnCircuitReadScopeResult
   {
   public DsnCircuitReadScopeResult(double p_max_length, double p_min_length, java.util.Collection<String> p_use_via, java.util.Collection<String> p_use_layer)
      {
      max_length = p_max_length;
      min_length = p_min_length;
      use_via = p_use_via;
      use_layer = p_use_layer;
      }

   public final double max_length;
   public final double min_length;
   public final java.util.Collection<String> use_via;
   public final java.util.Collection<String> use_layer;
   }
