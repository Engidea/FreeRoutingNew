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
 * NOTE that preferred direction is now quite basic, may become more interesting in the future
 * @author damiano
 *
 */
public final class ArtLayer
   {
   public static final int PFDIR_horizontal=1;  // preferred direction is horizontal
   public static final int PFDIR_vertical=2;    // vertical
   
   public final int layer_no;            // duplicate from BrdLayer

   public String  layer_name;            // duplicate from BrdLayer
   public boolean art_layer_active;      // holds if the layer is active in autoroute mode, was in ArtSettings   
   public int     layer_pfdir;           // Layer preferred direction for autoroute
   public double  preferred_direction_trace_cost;
   public double  against_direction_trace_cost;

   public ArtLayer ( int p_layer_no )
      {
      layer_no = p_layer_no;
      preferred_direction_trace_cost = 1;
      against_direction_trace_cost = 1;
      }
   
   public void add_cost ( double a_value )
      {
      preferred_direction_trace_cost += a_value;
      against_direction_trace_cost += a_value;
      }
   
   public boolean is_pfdir_horizontal()
      {
      return layer_pfdir == PFDIR_horizontal;
      }
   
   public void set_pfdir_horizontal ( boolean p_enbale )
      {
      layer_pfdir = p_enbale ? PFDIR_horizontal : PFDIR_vertical;
      }
   
   }
