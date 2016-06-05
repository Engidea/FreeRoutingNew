package autoroute.varie;

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
 * Autoroute needs to hold some info on layers too...
 * @author damiano
 *
 */
public final class ArtLayer
   {
   public final int layer_no;

   public boolean art_layer_active;      // holds if the layer is active in autoroute mode, was in ArtSettings   
   public boolean preferred_horizontal;  // if true the preferred direction is horizonatal
   public double  preferred_direction_trace_cost;
   public double  against_direction_trace_cost;

   public ArtLayer ( int p_layer_no )
      {
      layer_no = p_layer_no;
      }
   
   public void add_cost ( double a_value )
      {
      preferred_direction_trace_cost += a_value;
      against_direction_trace_cost += a_value;
      }
   
   }
