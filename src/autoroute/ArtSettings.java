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
 * AutorouteSettings.java
 *
 * Created on 27. Juli 2006, 09:16
 *
 */
package autoroute;

import java.util.ArrayList;
import autoroute.expand.ExpandCostFactor;
import autoroute.varie.ArtLayer;
import board.BrdLayer;
import board.RoutingBoard;

/**
 * This is really the autoroute settings
 * @author Alfons Wirtz
 */
public final class ArtSettings implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public boolean no_ripup;      // do not rip current traces
   public boolean vias_allowed;
   public boolean stop_remove_fanout_vias;  // damiano, was in batch autorouter
   
   private boolean with_fanout;
   private boolean with_autoroute;
   private boolean with_postroute;
   private int via_costs;
   private int plane_via_costs;
   private int start_ripup_costs;
   private int autoroute_pass_no;

   private final ArrayList<ArtLayer> layers_list;
   
   public ArtSettings(int p_layer_count)
      {
      layers_list = new ArrayList<ArtLayer>(p_layer_count);
      
      for ( int index=0; index<p_layer_count; index++ ) layers_list.add( new ArtLayer(index));

      // set default values
      start_ripup_costs = 100;
      autoroute_pass_no = 1;
      vias_allowed = true;
      stop_remove_fanout_vias = true;
      with_fanout = false;
      with_autoroute = true;
      with_postroute = true;
      via_costs = 50;
      plane_via_costs = 1;
      stop_remove_fanout_vias = true;
      }

   public ArtSettings(RoutingBoard p_board)
      {
      this(p_board.get_layer_count());

      double horizontal_width = p_board.bounding_box.width();
      double vertical_width = p_board.bounding_box.height();

      int layer_count = p_board.get_layer_count();

      // additional costs against preferred direction with 1 digit behind the decimal point.
      double horizontal_add_costs_against_preferred_dir = 0.1 * Math.round(10 * horizontal_width / vertical_width);

      double vertical_add_costs_against_preferred_dir = 0.1 * Math.round(10 * vertical_width / horizontal_width);

      // make more horizontal preferred direction, if the board is horizontal.

      boolean preferred_is_horizontal = horizontal_width < vertical_width;
      
      for (int index = 0; index < layer_count; ++index)
         {
         ArtLayer a_layer = art_layer_get(index);

         BrdLayer b_layer = p_board.layer_structure.get(index);
         
         a_layer.layer_name       = b_layer.name;
         a_layer.art_layer_active = b_layer.is_signal;
         
         if (a_layer.art_layer_active)
            {
            preferred_is_horizontal = ! preferred_is_horizontal;
            }
         
         a_layer.set_pfdir_horizontal(preferred_is_horizontal);

         if (preferred_is_horizontal)
            {
            a_layer.against_direction_trace_cost += horizontal_add_costs_against_preferred_dir;
            }
         else
            {
            a_layer.against_direction_trace_cost += vertical_add_costs_against_preferred_dir;
            }
         }

      int signal_layer_count = p_board.layer_structure.signal_layer_count();
      
      if (signal_layer_count > 2)
         {
         double outer_add_costs = 0.2 * signal_layer_count;
         
         // increase costs on the outer layers.
         art_layer_get(0).add_cost(outer_add_costs);

         art_layer_get(layer_count - 1).add_cost(outer_add_costs);
         }
      }
   
   /**
    * Return the direct reference to the layer list that baks the config
    * This can be used in a table model, until the table model is merged here...
    * @return
    */
   public ArrayList<ArtLayer>art_layer_list ()
      {
      return layers_list;
      }

   private ArtLayer art_layer_get ( int p_no )
      {
      try
         {
         return layers_list.get(p_no);
         }
      catch ( Exception exc )
         {
         exc.printStackTrace();
         return null;
         }
      }
   
   public void set_start_ripup_costs(int p_value)
      {
      start_ripup_costs = Math.max(p_value, 1);
      }

   public int get_start_ripup_costs()
      {
      return start_ripup_costs;
      }

   public void pass_no_set(int p_value)
      {
      if ( p_value < 1 ) 
         autoroute_pass_no = 1;
      else if ( p_value > 99 )
         autoroute_pass_no = 99;
      else
         autoroute_pass_no = p_value;
      }

   public int pass_no_get()
      {
      return autoroute_pass_no;
      }

   public void pass_no_inc()
      {
      autoroute_pass_no++;
      }

   public void set_with_fanout(boolean p_value)
      {
      with_fanout = p_value;
      }

   public boolean get_with_fanout()
      {
      return with_fanout;
      }

   public void set_with_autoroute(boolean p_value)
      {
      with_autoroute = p_value;
      }

   public boolean get_with_autoroute()
      {
      return with_autoroute;
      }

   public void set_with_postroute(boolean p_value)
      {
      with_postroute = p_value;
      }

   public boolean get_with_postroute()
      {
      return with_postroute;
      }

   public void set_via_costs(int p_value)
      {
      via_costs = Math.max(p_value, 1);
      }

   public int get_via_costs()
      {
      return via_costs;
      }

   public void set_plane_via_costs(int p_value)
      {
      plane_via_costs = Math.max(p_value, 1);
      }

   public int get_plane_via_costs()
      {
      return plane_via_costs;
      }

   public void set_layer_active(int p_layer, boolean p_value)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return;
      
      a_layer.art_layer_active = p_value;
      }

   public boolean get_layer_active(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return false;

      return a_layer.art_layer_active;
      }

   public void set_preferred_direction_horizontal(int p_layer, boolean p_value)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return;
      
      a_layer.set_pfdir_horizontal(p_value);
      }

   public boolean is_preferred_direction_horizontal(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return false;

      return a_layer.is_pfdir_horizontal();
      }

   public void set_preferred_direction_trace_costs(int p_layer, double p_value)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return;

      a_layer.preferred_direction_trace_cost = Math.max(p_value, 0.1);
      }

   public double get_preferred_direction_trace_costs(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return 1;

      return a_layer.preferred_direction_trace_cost;
      }

   public void set_against_preferred_direction_trace_costs(int p_layer, double p_value)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return;

      a_layer.against_direction_trace_cost = Math.max(p_value, 0.1);
      }
   
   public double get_against_preferred_direction_trace_costs(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return 1;

      return a_layer.against_direction_trace_cost;
      }

   
   public double get_horizontal_trace_costs(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return 1;

      return a_layer.is_pfdir_horizontal() ? a_layer.preferred_direction_trace_cost : a_layer.against_direction_trace_cost;
      }


   public double get_vertical_trace_costs(int p_layer)
      {
      ArtLayer a_layer = art_layer_get(p_layer);

      if ( a_layer == null ) return 1;

      return a_layer.is_pfdir_horizontal() ? a_layer.against_direction_trace_cost : a_layer.preferred_direction_trace_cost;
      }

   public ExpandCostFactor[] get_trace_cost_arr()
      {
      ExpandCostFactor[] result = new ExpandCostFactor[layers_list.size()];
      
      for (int index = 0; index < result.length; ++index)
         {
         result[index] = new ExpandCostFactor(get_horizontal_trace_costs(index), get_vertical_trace_costs(index));
         }
      
      return result;
      }
   }
