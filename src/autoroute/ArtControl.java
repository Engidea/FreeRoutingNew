/*
 *  Copyright (C) 2014  Alfons Wirtz
 *  website www.freerouting.net
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
 * AutorouteControl.java
 *
 * Created on 25. Januar 2004, 09:38
 */
package autoroute;

import freert.planar.ShapeConvex;
import freert.rules.RuleViaInfoList;
import interactive.IteraSettings;
import autoroute.expand.ExpandCostFactor;
import autoroute.varie.ArtViaCost;
import autoroute.varie.ArtViaMask;
import board.RoutingBoard;
import board.infos.BrdViaInfo;

/**
 * Structure for controlling the autoroute algorithm.
 * It is kind of Settings but applied to a single live situation
 * @author Alfons Wirtz
 */
public final class ArtControl
   {
   public final int layer_count;
   // The horizontal and vertical trace costs on each layer 
   public final ExpandCostFactor[] trace_costs;
   // Defines for each layer, if it may used for routing
   public final boolean[] layer_active;
   // The currently used net number in the autoroute algorithm 
   public final int net_no;
   // The currently used trace half widths in the autoroute algorithm on each layer
   public final int[] trace_half_width;
   // The currently used compensated trace half widths in the autoroute algorithm on each layer. 
   // Equal to trace_half_width if no clearance compensation is used.
   public final int[] compensated_trace_half_width;

   public final double[] via_radius_arr;
   double via_radius_max;
     
   // The currently used clearance class for traces in the autoroute algorithm 
   public int trace_clearance_class_no;
   // The currently used clearance class for vias in the autoroute algorithm 
   public int via_clearance_class;
   // The possible (partial) vias, which can be used by the autoroute 
   public RuleViaInfoList via_rule;
   // The array of possible via ranges used by the autorouter 
   public ArtViaMask[] via_info_arr;
   // The lower bound for the first layer of vias 
   public int via_lower_bound;
   // The upper bound for the last layer of vias 
   public int via_upper_bound;
   // The width of the region around changed traces, where traces are pulled tight 
   public int tidy_region_width;
   // The pull tight accuracy of traces 
   public int pull_tight_accuracy;  // interesting
   // The maximum recursion depth for shoving traces 
   public int max_shove_trace_recursion_depth;
   // The maximum recursion depth for shoving obstacles 
   public int max_shove_via_recursion_depth;
   // The maximum recursion depth for traces springing over obstacles
   public int max_spring_over_recursion_depth;
   // True, if layer change by inserting of vias is allowed 
   public boolean vias_allowed;
   // True, if vias may drill to the pad of SMD pins 
   public boolean attach_smd_allowed;
   // the additional costs to min_normal via_cost for inserting a via between 2 layers 
   public final ArtViaCost[] add_via_costs;
   // The minimum cost valua of all normal vias 
   public double min_normal_via_cost;
   // The minimal cost value of all cheap vias 
   public double min_cheap_via_cost;
   public boolean ripup_allowed;
   public int ripup_costs;
   public int ripup_pass_no;
   public final boolean with_neckdown;
   // If true, the autoroute algorithm completes after the first drill 
   public boolean is_fanout;

   // request to stop remove fanout vias, they are classified as "tails" and would normally be removed
   // Normally true, if the autoroute contains no fanout pass
   public boolean stop_remove_fanout_vias;

   public ArtControl(RoutingBoard p_board, int p_net_no, IteraSettings p_settings)
      {
      this(p_board, p_net_no, p_settings, p_settings.autoroute_settings.get_trace_cost_arr());
      
      net_init( p_board, p_settings.autoroute_settings.get_via_costs());
      }


   public ArtControl(RoutingBoard p_board, int p_net_no, IteraSettings p_settings, int p_via_costs, ExpandCostFactor[] p_trace_cost_arr)
      {
      this(p_board, p_net_no, p_settings, p_trace_cost_arr);
      
      net_init( p_board, p_via_costs);
      
      // ripping when finishing up a board is normally quite bad, so make it a configuration option      
      ripup_allowed           = p_settings.autoroute_settings.no_ripup;  
      stop_remove_fanout_vias = p_settings.autoroute_settings.stop_remove_fanout_vias;
      }

   private ArtControl(RoutingBoard p_board, int p_net_no, IteraSettings p_settings, ExpandCostFactor[] p_trace_costs_arr)
      {
      net_no = p_net_no;

      layer_count = p_board.get_layer_count();
      trace_half_width = new int[layer_count];
      compensated_trace_half_width = new int[layer_count];
      layer_active = new boolean[layer_count];
      vias_allowed = p_settings.autoroute_settings.vias_allowed;
      via_radius_arr = new double[layer_count];
      add_via_costs = new ArtViaCost[layer_count];

      for (int i = 0; i < layer_count; ++i)
         {
         add_via_costs[i] = new ArtViaCost(layer_count);
         layer_active[i] = p_settings.autoroute_settings.get_layer_active(i);
         }
      
      is_fanout = false;
      stop_remove_fanout_vias = true;
      with_neckdown = p_settings.is_automatic_neckdown();
      tidy_region_width = Integer.MAX_VALUE;
      pull_tight_accuracy = 500;
      max_shove_trace_recursion_depth = 20;
      max_shove_via_recursion_depth = 5;
      max_spring_over_recursion_depth = 5;
      for (int i = 0; i < layer_count; ++i)
         {
         for (int j = 0; j < layer_count; ++j)
            {
            add_via_costs[i].to_layer[j] = 0;
            }
         }
      trace_costs = p_trace_costs_arr;
      attach_smd_allowed = false;
      via_lower_bound = 0;
      via_upper_bound = layer_count;

      ripup_allowed = false;
      ripup_costs = 1000;
      ripup_pass_no = 1;
      }

   /**
    * Apparently it is kind of possible to have a non present net ???
    * @param p_net_no
    * @param p_board
    * @param p_via_costs
    */
   private void net_init( RoutingBoard p_board, int p_via_costs)
      {
      freert.rules.RuleNet curr_net = p_board.brd_rules.nets.get(net_no);
      freert.rules.NetClass curr_net_class;
      
      if (curr_net != null)
         {
         curr_net_class = curr_net.get_class();
         trace_clearance_class_no = curr_net_class.get_trace_clearance_class();
         via_rule = curr_net_class.get_via_rule();
         }
      else
         {
         trace_clearance_class_no = 1;
         via_rule = p_board.brd_rules.via_rules.firstElement();
         curr_net_class = null;
         }
      
      for (int index = 0; index < layer_count; ++index)
         {
         if (net_no > 0)
            {
            trace_half_width[index] = p_board.brd_rules.get_trace_half_width(net_no, index);
            }
         else
            {
            trace_half_width[index] = p_board.brd_rules.get_trace_half_width(1, index);
            }
         
         compensated_trace_half_width[index] = trace_half_width[index] + p_board.brd_rules.clearance_matrix.clearance_compensation_value(trace_clearance_class_no, index);
         if (curr_net_class != null && !curr_net_class.is_active_routing_layer(index))
            {
            layer_active[index] = false;
            }
         }
      
      if (via_rule.via_count() > 0)
         {
         via_clearance_class = via_rule.get_via(0).get_clearance_class();
         }
      else
         {
         via_clearance_class = 1;
         }
      
      via_info_arr = new ArtViaMask[via_rule.via_count()];
      for (int i = 0; i < via_rule.via_count(); ++i)
         {
         BrdViaInfo curr_via = via_rule.get_via(i);
         if (curr_via.attach_smd_allowed())
            {
            attach_smd_allowed = true;
            }
         library.LibPadstack curr_via_padstack = curr_via.get_padstack();
         int from_layer = curr_via_padstack.from_layer();
         int to_layer = curr_via_padstack.to_layer();
         for (int j = from_layer; j <= to_layer; ++j)
            {
            ShapeConvex curr_shape = curr_via_padstack.get_shape(j);
            double curr_radius;
            if (curr_shape != null)
               {
               curr_radius = 0.5 * curr_shape.max_width();
               }
            else
               {
               curr_radius = 0;
               }
            via_radius_arr[j] = Math.max(via_radius_arr[j], curr_radius);
            }
         via_info_arr[i] = new ArtViaMask(from_layer, to_layer, curr_via.attach_smd_allowed());
         }
      for (int j = 0; j < layer_count; ++j)
         {
         via_radius_arr[j] = Math.max(via_radius_arr[j], trace_half_width[j]);
         via_radius_max = Math.max(via_radius_max, via_radius_arr[j]);
         }
      double via_cost_factor = via_radius_max;
      via_cost_factor = Math.max(via_cost_factor, 1);
      min_normal_via_cost = p_via_costs * via_cost_factor;
      min_cheap_via_cost = 0.8 * min_normal_via_cost;
      }




   }
