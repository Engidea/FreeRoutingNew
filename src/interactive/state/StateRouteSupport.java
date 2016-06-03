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
 */
package interactive.state;

import freert.graphics.GdiContext;
import freert.library.LibPadstack;
import freert.planar.PlaArea;
import freert.planar.PlaEllipse;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.planar.Polyline;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.rules.RuleNet;
import freert.rules.RuleViaInfoList;
import freert.varie.NetNosList;
import freert.varie.TimeLimit;
import freert.varie.TimeLimitStoppable;
import freert.varie.UnitMeasure;
import interactive.IteraSettings;
import interactive.NetIncompletes;
import interactive.varie.IteraTargetPoint;
import interactive.varie.PinSwappable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import board.RoutingBoard;
import board.infos.BrdViaInfo;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.items.BrdTracep;
import board.varie.BrdKeepPoint;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import board.varie.TraceAngleRestriction;

/**
 * Functionality for interactive routing
 * This is used by StateRoute and StateDragMakeSpace for additional routing suport
 * @author Alfons Wirtz
 */
public final class StateRouteSupport
   {
   private static final int s_CHECK_FORCED_TRACE_TIME_MAX = 3;
   private static final int s_PULL_TIGHT_TIME_MAX = 2;
//   private static final String classname="IteraRoute.";

   private final RoutingBoard r_board;
   private final IteraSettings itera_settings;
   private final BrdItem start_item;
   private final Set<BrdItem> target_set;
   // Pins, which can be reached by a pin swap by a target pin
   private final Set<PinSwappable> swap_pin_infos;
   
   private final int[] pen_half_width_arr;
   private final boolean[] layer_active_arr;
   private final int clearance_class;
   private final RuleViaInfoList via_rule;
   private final int max_shove_trace_recursion_depth;
   private final int max_shove_via_recursion_depth;
   private final int max_spring_over_recursion_depth;
   private final boolean is_stitch_mode;
   private final boolean via_snap_to_smd_center;

   private Collection<IteraTargetPoint> target_points;  // from drill_items
   private Collection<BrdItem> target_traces_and_areas; // from traces and conduction areas
   private PlaPointFloat nearest_target_point;
   private BrdItem nearest_target_item;
   private BrdItem shove_failing_obstacle = null;
   private PlaPointInt prev_corner;
   private int layer_active_no;

   public  final NetNosList net_nos;  // Net numbers to use for routing

   
   /**
    * Starts routing a connection. 
    * p_pen_half_width_arr is provided because it may be different from the half width array in p_board.rules.
    */
   public StateRouteSupport(
         PlaPointInt p_start_corner, 
         int p_layer, 
         int[] p_pen_half_width_arr, 
         boolean[] p_layer_active_arr, 
         NetNosList p_net_no_arr, 
         int p_clearance_class, 
         RuleViaInfoList p_via_rule, 
         boolean p_push_enabled,
         BrdItem p_start_item, 
         Set<BrdItem> p_target_set,
         RoutingBoard p_board, 
         boolean p_is_stitch_mode, 
         boolean p_via_snap_to_smd_center, 
         IteraSettings p_itera_settings)
      {
      r_board = p_board;
      itera_settings = p_itera_settings;
      layer_active_no = p_layer;
      
      if (p_push_enabled)
         {
         max_shove_trace_recursion_depth = 20;
         max_shove_via_recursion_depth = 8;
         max_spring_over_recursion_depth = 5;
         }
      else
         {
         max_shove_trace_recursion_depth = 0;
         max_shove_via_recursion_depth = 0;
         max_spring_over_recursion_depth = 0;
         }

      prev_corner = p_start_corner;
      net_nos = p_net_no_arr;
      pen_half_width_arr = p_pen_half_width_arr;
      layer_active_arr = p_layer_active_arr;
      clearance_class = p_clearance_class;
      via_rule = p_via_rule;
      start_item = p_start_item;
      target_set = p_target_set;
      is_stitch_mode = p_is_stitch_mode;
      via_snap_to_smd_center = p_via_snap_to_smd_center;
      calculate_target_points_and_areas();
      swap_pin_infos = calculate_swap_pin_infos();
      }

   /**
    * Append a line to the trace routed so far. 
    * @return true, if the route is completed by connecting to a target.
    */
   public boolean route_to(PlaPointFloat p_corner)
      {
      if (! layer_active_arr[layer_active_no]) return false;
      
      // ok, notice how cur corner is now a normal int
      PlaPointInt curr_corner = p_corner.round();

      if ( curr_corner.equals(prev_corner)) return false;

      if ( ! r_board.contains(prev_corner) ) return false;
      
      if ( !  r_board.contains(curr_corner) ) return false;
      
      if ( ! r_board.layer_structure.is_signal(layer_active_no) ) return false;
      
      if (nearest_target_item instanceof BrdAbit)
         {
         BrdAbit target = (BrdAbit) nearest_target_item;
         
         // connection already completed at prev_corner.         
         if ( prev_corner.equals(target.center_get())) return true; 
         }
      
      shove_failing_obstacle = null;
      
/*  why does it have to be an ortho projection or 45 degrees at start ?
 *  really, the rest of the system should take care of optimizing or something    
 *  damiano avoiding snap angles here shows better what happens in the optimization
 *  
      if (angle_restriction == TraceAngleRestriction.NINETY_DEGREE)
         {
         curr_corner = curr_corner.orthogonal_projection((PlaPointInt) prev_corner);
         }
      else if (angle_restriction == TraceAngleRestriction.FORTYFIVE_DEGREE)
         {
         curr_corner = curr_corner.fortyfive_degree_projection((PlaPointInt) prev_corner);
         }
*/      
      
      BrdItem end_routing_item = r_board.pick_nearest_routing_item(prev_corner, layer_active_no, null);

      // look for a nearby item of this net, which is not connected to end_routing_item.
      nearest_target_item = r_board.pick_nearest_routing_item(curr_corner, layer_active_no, end_routing_item);

      TimeLimitStoppable t_limit = new TimeLimitStoppable(s_CHECK_FORCED_TRACE_TIME_MAX);

      // tests.Validate.check("before insert", board);
      
      PlaPointInt ok_point = r_board.insert_trace (
            prev_corner, 
            curr_corner, 
            pen_half_width_arr[layer_active_no], 
            layer_active_no, 
            net_nos, 
            clearance_class, 
            max_shove_trace_recursion_depth,
            max_shove_via_recursion_depth, 
            max_spring_over_recursion_depth, 
            itera_settings.trace_pull_tight_region_width, 
            itera_settings.trace_pullt_min_move, 
            ! is_stitch_mode, 
            t_limit);
      
      // tests.Validate.check("after insert", board);
      
      if (ok_point == prev_corner && itera_settings.is_automatic_neckdown())
         {
         ok_point = try_neckdown_at_start(curr_corner);
         }
      
      if (ok_point == prev_corner && itera_settings.is_automatic_neckdown() )
         {
         ok_point = try_neckdown_at_end(prev_corner, curr_corner);
         }
      
      if (ok_point == null)
         {
         // database may be damaged, restore previous situation
         r_board.undo(null);
         // end routing in case it is dynamic
         return  ! is_stitch_mode ;
         }

      if (ok_point == prev_corner)
         {
         set_shove_failing_obstacle(r_board.shove_fail_obstacle_get());
      
         return false;
         }
      
      prev_corner = ok_point;
      
      // check, if a target is reached
      boolean route_completed = false;
      
      if (ok_point == curr_corner)
         {
         route_completed = connect_to_target(curr_corner);
         }

      ShapeTileOctagon tidy_clip_shape;
      
      if (itera_settings.trace_pull_tight_region_width == Integer.MAX_VALUE)
         {
         tidy_clip_shape = null;
         }
      else if (itera_settings.trace_pull_tight_region_width == 0)
         {
         tidy_clip_shape = ShapeTileOctagon.EMPTY;
         }
      else
         {
         // this is probably the idea of it, however, is it really useful ?
         tidy_clip_shape = new ShapeTileOctagon(ok_point).enlarge(itera_settings.trace_pull_tight_region_width);
         }
      
      NetNosList opt_net_no_arr = max_shove_trace_recursion_depth <= 0 ? net_nos : NetNosList.EMPTY;
      
      if (route_completed)
         {
         r_board.reduce_nets_of_route_items();

         for ( int curr_net_no : net_nos ) r_board.combine_traces(curr_net_no);
         }
      else
         {
         update_nearest_target_point(prev_corner.to_float());
         }
      
      t_limit = new TimeLimitStoppable(s_PULL_TIGHT_TIME_MAX);
      
      r_board.changed_area_optimize(
            opt_net_no_arr, 
            tidy_clip_shape, 
            itera_settings.trace_pullt_min_move, 
            null, 
            t_limit, 
            new BrdKeepPoint(ok_point, layer_active_no) );
      
      return route_completed;
      }

   /**
    * Changing the layer in interactive route and inserting a via. 
    * @returns false, if changing the layer was not possible.
    */
   public boolean change_layer(int p_to_layer)
      {
      if (layer_active_no == p_to_layer) return true;

      if (p_to_layer < 0 || p_to_layer >= layer_active_arr.length)
         {
         System.out.println("Route.change_layer: p_to_layer out of range");
         return false;
         }

      if ( ! layer_active_arr[p_to_layer]) return false;

      if (via_rule == null) return false;

      shove_failing_obstacle = null;

      if ( via_snap_to_smd_center )
         {
         boolean snapped_to_smd_center = via_try_snap_smd_center(p_to_layer);
         
         if (!snapped_to_smd_center)
            {
            via_try_snap_smd_center(layer_active_no);
            }
         }
      
      PlaPointInt a_corner = prev_corner; 
      
      boolean result = true;
      int min_layer = Math.min(layer_active_no, p_to_layer);
      int max_layer = Math.max(layer_active_no, p_to_layer);
      boolean via_found = false;
      
      for (int index = 0; index < via_rule.via_count(); ++index)
         {
         BrdViaInfo curr_via_info = via_rule.get_via(index);
         LibPadstack curr_via_padstack = curr_via_info.get_padstack();
         
         if (min_layer < curr_via_padstack.from_layer() || max_layer > curr_via_padstack.to_layer())
            {
            continue;
            }
         
         // make the current situation restorable by undo
         r_board.generate_snapshot();
         
         result = r_board.insert_via(
               curr_via_info, 
               a_corner, 
               net_nos, 
               clearance_class, 
               pen_half_width_arr, 
               max_shove_trace_recursion_depth, 
               0, 
               itera_settings.trace_pull_tight_region_width,
               itera_settings.trace_pullt_min_move, 
               s_PULL_TIGHT_TIME_MAX);
         
         if (result)
            {
            via_found = true;
            break;
            }
         
         set_shove_failing_obstacle(r_board.shove_fail_obstacle_get());
      
         r_board.undo(null);
         }
      
      if (via_found)
         {
         layer_active_no = p_to_layer;
         }
      
      return result;
      }

   /**
    * Snaps to the center of an smd pin, if the location location on p_layer is inside an smd pin of the own net,
    */
   private boolean via_try_snap_smd_center(int p_layer)
      {
      ItemSelectionFilter selection_filter = new ItemSelectionFilter(ItemSelectionChoice.PINS);
      Collection<BrdItem> picked_items = r_board.pick_items(prev_corner, p_layer, selection_filter);
      
      BrdAbitPin found_smd_pin = null;
      
      for (BrdItem curr_item : picked_items)
         {
         if ( ! ( curr_item instanceof BrdAbitPin ) ) continue; 
         
         BrdAbitPin a_pin = (BrdAbitPin)curr_item;
         
         if ( ! a_pin.shares_net_no(net_nos) ) continue;
         
         if ( a_pin.first_layer() == p_layer && a_pin.last_layer() == p_layer)
            {
            found_smd_pin = a_pin;
            break;
            }
         }
      
      if (found_smd_pin == null) return false;
      
      PlaPointInt pin_center = found_smd_pin.center_get();

      if (itera_connect(prev_corner, pin_center))
         {
         // if connections successfult update the prev_corner
         prev_corner = pin_center;
         }
      
      return true;
      }

   /**
    * If p_from_point is already on a target item, a connection to the target is made and true returned.
    */
   private boolean connect_to_target(PlaPointInt p_from_point)
      {
      if (nearest_target_item != null && target_set != null && !target_set.contains(nearest_target_item))
         {
         nearest_target_item = null;
         }
      
      if (nearest_target_item == null || !nearest_target_item.shares_net_no(net_nos))
         {
         return false;
         }
      
      boolean route_completed = false;
      PlaPointInt connection_point = null;
      if (nearest_target_item instanceof BrdAbit)
         {
         BrdAbit target = (BrdAbit) nearest_target_item;
         connection_point = target.center_get();
         }
      else if (nearest_target_item instanceof BrdTracep)
         {
         return r_board.connect_to_trace(p_from_point, (BrdTracep) nearest_target_item, pen_half_width_arr[layer_active_no], clearance_class);
         }
      else if (nearest_target_item instanceof BrdAreaConduction)
         {
         connection_point = p_from_point;
         }
      
      if (connection_point != null )
         {
         route_completed = itera_connect(p_from_point, connection_point);
         }
      
      return route_completed;
      }

   /**
    * Tries to make a trace connection from p_from_point to p_to_point according to the angle restriction. 
    * @returns true, if the connection succeeded.
    */
   private boolean itera_connect(PlaPointInt p_from_point, PlaPointInt p_to_point)
      {
      ArrayList<PlaPointInt> corners = angled_connection(p_from_point, p_to_point);
      
      boolean connection_succeeded = true;
      
      for (int index = 1; index < corners.size(); ++index)
         {
         PlaPointInt from_corner = corners.get(index - 1);
         PlaPointInt to_corner = corners.get(index);
         
         TimeLimit time_limit = new TimeLimit(s_CHECK_FORCED_TRACE_TIME_MAX);

         while (!from_corner.equals(to_corner))
            {
            PlaPointInt curr_ok_point = r_board.insert_trace (
                  from_corner, 
                  to_corner, 
                  pen_half_width_arr[layer_active_no], 
                  layer_active_no, 
                  net_nos, 
                  clearance_class, 
                  max_shove_trace_recursion_depth,
                  max_shove_via_recursion_depth, 
                  max_spring_over_recursion_depth, 
                  itera_settings.trace_pull_tight_region_width, 
                  itera_settings.trace_pullt_min_move, 
                  ! is_stitch_mode, 
                  time_limit);
            
            if (curr_ok_point == null)
               {
               // database may be damaged, restore previous situation
               r_board.undo(null);
               return true;
               }
            
            if (curr_ok_point.equals(from_corner) && itera_settings.is_automatic_neckdown())
               {
               curr_ok_point = try_neckdown_at_end(from_corner, to_corner);
               }
            
            if (curr_ok_point.equals(from_corner))
               {
               prev_corner = from_corner;
               connection_succeeded = false;
               break;
               }
            
            from_corner = curr_ok_point;
            }
         }
      
      return connection_succeeded;
      }

   /**
    * Calculates the nearest layer of the nearest target item to this.layer.
    */
   public int nearest_target_layer()
      {
      if (nearest_target_item == null) return layer_active_no;

      int first_layer = nearest_target_item.first_layer();
      int last_layer = nearest_target_item.last_layer();

      if (layer_active_no < first_layer)
         {
         return first_layer;
         }
      else if (layer_active_no > last_layer)
         {
         return last_layer;
         }
      else
         {
         return layer_active_no;
         }
      }

   /**
    * Returns all pins, which can be reached by a pin swap from a start or target pin.
    */
   private Set<PinSwappable> calculate_swap_pin_infos()
      {
      TreeSet<PinSwappable> result = new TreeSet<PinSwappable>();

      if (target_set == null) return result;

      for (BrdItem curr_item : target_set)
         {
         if ( ! ( curr_item instanceof BrdAbitPin) ) continue;

         BrdAbitPin a_pin = (BrdAbitPin)curr_item;
         Collection<BrdAbitPin> curr_swapppable_pins = a_pin.get_swappable_pins();
         for (BrdAbitPin curr_swappable_pin : curr_swapppable_pins)
            {
            result.add(new PinSwappable(r_board, curr_swappable_pin));
            }
         }
      
      // add the from item, if it is a pin
      ItemSelectionFilter selection_filter = new ItemSelectionFilter(ItemSelectionChoice.PINS);
      Collection<BrdItem> picked_items = r_board.pick_items(prev_corner, layer_active_no, selection_filter);

      for (BrdItem curr_item : picked_items)
         {
         if ( ! (curr_item instanceof BrdAbitPin) ) continue;

         BrdAbitPin a_pin = (BrdAbitPin)curr_item;
         Collection<BrdAbitPin> curr_swapppable_pins = a_pin.get_swappable_pins();
         for (BrdAbitPin curr_swappable_pin : curr_swapppable_pins)
            {
            result.add(new PinSwappable(r_board,curr_swappable_pin));
            }
         }
      
      return result;
      }

   /**
    * Highlights the targets and draws the incomplete.
    */
   public void draw(Graphics p_graphics, GdiContext p_graphics_context)
      {
      if ( itera_settings.is_hilight_routing_obstacle() &&  shove_failing_obstacle != null)
         {
         shove_failing_obstacle.draw(p_graphics, p_graphics_context, p_graphics_context.get_violations_color(), 1);
         }
      
      if (target_set == null || net_nos.is_empty() ) return;

      RuleNet curr_net = r_board.brd_rules.nets.get(net_nos.first());

      if (curr_net == null) return;

      Color highlight_color = p_graphics_context.get_hilight_color();
      double highligt_color_intensity = p_graphics_context.get_hilight_color_intensity();

      // Highlight the swapppable pins and their incomplete
      for (PinSwappable curr_info : swap_pin_infos)
         {
         curr_info.my_pin.draw(p_graphics, p_graphics_context, highlight_color, 0.3 * highligt_color_intensity);
         if (curr_info.incomplete != null)
            {
            // draw the swap pin incomplete
            PlaPointFloat[] draw_points = new PlaPointFloat[2];
            draw_points[0] = curr_info.incomplete.point_a;
            draw_points[1] = curr_info.incomplete.point_b;
            Color draw_color = p_graphics_context.get_incomplete_color();
            p_graphics_context.draw(draw_points, 1, draw_color, p_graphics, highligt_color_intensity);
            }
         }

      // Highlight the target set
      for (BrdItem curr_item : target_set)
         {
         if ( curr_item instanceof BrdAreaConduction ) continue;

         curr_item.draw(p_graphics, p_graphics_context, highlight_color, highligt_color_intensity);
         }
      
      
      if ( nearest_target_point == null || prev_corner == null) return;
      
      PlaPointFloat from_corner = prev_corner.to_float();

      boolean curr_length_matching_ok = true; // used for drawing the incomplete as violation
      double max_trace_length = curr_net.get_class().get_maximum_trace_length();
      double min_trace_length = curr_net.get_class().get_minimum_trace_length();
      double length_matching_color_intensity = p_graphics_context.get_length_matching_area_color_intensity();

      if (max_trace_length > 0 || min_trace_length > 0 && length_matching_color_intensity > 0)
         {
         // draw the length matching area
         double trace_length_add = from_corner.distance(prev_corner.to_float());
         // trace_length_add is != 0 only in stitching mode.
         if (max_trace_length <= 0)
            {
            // max_trace_length not provided. Create an ellipse containing the whole board.
            max_trace_length = 0.3 * freert.planar.PlaLimits.CRIT_INT;
            }
         
         double curr_max_trace_length = max_trace_length - (curr_net.get_trace_length() + trace_length_add);
         double curr_min_trace_length = min_trace_length - (curr_net.get_trace_length() + trace_length_add);
         double incomplete_length = nearest_target_point.distance(from_corner);

         if (incomplete_length < curr_max_trace_length && min_trace_length <= max_trace_length)
            {
            PlaVectorInt delta = nearest_target_point.round().difference_by(prev_corner);
            double rotation = delta.angle_approx();
            PlaPointFloat center = from_corner.middle_point(nearest_target_point);
            double bigger_radius = 0.5 * curr_max_trace_length;
            // dist_focus_to_center^2 = bigger_radius^2 - smaller_radius^2
            double smaller_radius = 0.5 * Math.sqrt(curr_max_trace_length * curr_max_trace_length - incomplete_length * incomplete_length);
            int ellipse_count;
            if (min_trace_length <= 0 || incomplete_length >= curr_min_trace_length)
               {
               ellipse_count = 1;
               }
            else
               {
               // display an ellipse ring.
               ellipse_count = 2;
               }
            PlaEllipse[] ellipse_arr = new PlaEllipse[ellipse_count];
            ellipse_arr[0] = new PlaEllipse(center, rotation, bigger_radius, smaller_radius);
            ShapeTileBox bounding_box = new ShapeTileBox(prev_corner.to_float().round(), nearest_target_point.round());
            bounding_box = bounding_box.offset(curr_max_trace_length - incomplete_length);
            r_board.gdi_update_join(bounding_box);
            if (ellipse_count == 2)
               {
               bigger_radius = 0.5 * curr_min_trace_length;
               smaller_radius = 0.5 * Math.sqrt(curr_min_trace_length * curr_min_trace_length - incomplete_length * incomplete_length);
               ellipse_arr[1] = new PlaEllipse(center, rotation, bigger_radius, smaller_radius);
               }
            p_graphics_context.fill_ellipse_arr(ellipse_arr, p_graphics, p_graphics_context.get_length_matching_area_color(), length_matching_color_intensity);
            }
         else
            {
            curr_length_matching_ok = false;
            }
         }

      // draw the incomplete
      PlaPointFloat[] draw_points = new PlaPointFloat[2];
      draw_points[0] = from_corner;
      draw_points[1] = nearest_target_point;
      Color draw_color = p_graphics_context.get_incomplete_color();
      double draw_width = Math.min(r_board.host_com.get_resolution(UnitMeasure.MIL), 100); // problem with low resolution on
                                                                                            // Kicad
      if (!curr_length_matching_ok)
         {
         draw_color = p_graphics_context.get_violations_color();
         draw_width *= 3;
         }
      
      p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, highligt_color_intensity);
      if (nearest_target_item != null && !nearest_target_item.is_on_layer(layer_active_no))
         {
         // draw a marker to indicate the layer change.
         NetIncompletes.draw_layer_change_marker(draw_points[0], 4 * pen_half_width_arr[0], p_graphics, p_graphics_context);
         }
      }

   /**
    * Makes a connection polygon from p_from_point to p_to_point whose lines fulfill the angle restriction.
    */
   private ArrayList<PlaPointInt> angled_connection(PlaPointInt p_from_point, PlaPointInt p_to_point)
      {
      TraceAngleRestriction angle_restriction = r_board.brd_rules.get_trace_snap_angle();
      ArrayList<PlaPointInt> result = new ArrayList<PlaPointInt>(3);
      
      result.add(p_from_point);
 
      if (angle_restriction.is_limit_90() )
         {
         PlaPointInt extra = p_from_point.ninety_degree_corner(p_to_point, true);
         if ( extra != null ) result.add(extra);
         }
      else if (angle_restriction.is_limit_45() )
         {
         PlaPointInt extra = p_from_point.fortyfive_degree_corner(p_to_point, true);
         if ( extra != null ) result.add(extra);
         }
      
      result.add(p_to_point);
      
      return result;
      }

   /**
    * Calculates a list of the center points of DrillItems, end points of traces and areas of ConductionAreas in the target set.
    */
   private void calculate_target_points_and_areas()
      {
      target_points = new LinkedList<IteraTargetPoint>();
      target_traces_and_areas = new LinkedList<BrdItem>();
      if (target_set == null)
         {
         return;
         }
      Iterator<BrdItem> it = target_set.iterator();
      while (it.hasNext())
         {
         BrdItem curr_ob = it.next();
         if (curr_ob instanceof BrdAbit)
            {
            PlaPointInt curr_point = ((BrdAbit) curr_ob).center_get();
            target_points.add(new IteraTargetPoint(curr_point.to_float(), curr_ob));
            }
         else if (curr_ob instanceof BrdTracep || curr_ob instanceof BrdAreaConduction)
            {
            target_traces_and_areas.add(curr_ob);
            }
         }
      }

   public PlaPointInt get_last_corner()
      {
      return prev_corner;
      }

   public boolean is_layer_active(int p_layer)
      {
      if (p_layer < 0 || p_layer >= layer_active_arr.length) return false;

      return layer_active_arr[p_layer];
      }

   /**
    * The nearest point is used for drawing the incomplete
    * This functionupdates the info on this class given the input value
    */
   public void update_nearest_target_point(PlaPointFloat p_from_point)
      {
      double min_dist = Double.MAX_VALUE;
      PlaPointFloat nearest_point = null;
      BrdItem nearest_item = null;
      for (IteraTargetPoint curr_target_point : target_points)
         {
         double curr_dist = p_from_point.distance(curr_target_point.location);
         if (curr_dist < min_dist)
            {
            min_dist = curr_dist;
            nearest_point = curr_target_point.location;
            nearest_item = curr_target_point.item;
            }
         }
      
      for ( BrdItem curr_item : target_traces_and_areas )
         {
         if (curr_item instanceof BrdTracep)
            {
            BrdTracep curr_trace = (BrdTracep) curr_item;
            Polyline curr_polyline = curr_trace.polyline();
            
            if (curr_polyline.bounding_box().distance(p_from_point) < min_dist)
               {
               PlaPointFloat curr_nearest_point = curr_polyline.nearest_point_approx(p_from_point);
               double curr_dist = p_from_point.distance(curr_nearest_point);
               if (curr_dist < min_dist)
                  {
                  min_dist = curr_dist;
                  nearest_point = curr_nearest_point;
                  nearest_item = curr_trace;
                  }
               }
            }
         else if (curr_item instanceof BrdAreaConduction && curr_item.tile_shape_count() > 0)
            {
            BrdAreaConduction curr_conduction_area = (BrdAreaConduction) curr_item;
            PlaArea curr_area = curr_conduction_area.get_area();
            if (curr_area.bounding_box().distance(p_from_point) < min_dist)
               {
               PlaPointFloat curr_nearest_point = curr_area.nearest_point_approx(p_from_point);
               double curr_dist = p_from_point.distance(curr_nearest_point);
               if (curr_dist < min_dist)
                  {
                  min_dist = curr_dist;
                  nearest_point = curr_nearest_point;
                  nearest_item = curr_conduction_area;
                  }
               }
            }
         }
      
      // target set is empty, no target point
      if (nearest_point == null) return; 
      
      nearest_target_point = nearest_point;
      nearest_target_item = nearest_item;
      
      // join the graphics update box by the nearest item, so that the incomplete is completely displayed.
      r_board.gdi_update_join(nearest_item.bounding_box());
      }

   /**
    * May be called with null value to "clear" a previous info
    * @param p_item
    */
   private void set_shove_failing_obstacle(BrdItem p_item)
      {
      shove_failing_obstacle = p_item;
      
      if (p_item != null)
         {
         r_board.gdi_update_join(p_item.bounding_box());
         }
      }

   /**
    * If the routed starts at a pin and the route failed with the normal trace width, another try with the smallest pin width is
    * done. Returns the ok_point of the try, which is this.prev_point, if the try failed.
    */
   private PlaPointInt try_neckdown_at_start(PlaPointInt p_to_corner)
      {
      if (!(start_item instanceof BrdAbitPin)) return prev_corner;

      BrdAbitPin start_pin = (BrdAbitPin) start_item;

      if (!start_pin.is_on_layer(layer_active_no)) return prev_corner;

      PlaPointFloat pin_center = start_pin.center_get().to_float();
      double curr_clearance = r_board.brd_rules.clearance_matrix.value_at(clearance_class, start_pin.clearance_idx(), layer_active_no);
      double pin_neck_down_distance = 2 * (0.5 * start_pin.get_max_width(layer_active_no) + curr_clearance);

      if (pin_center.distance(prev_corner.to_float()) >= pin_neck_down_distance) return prev_corner;

      int neck_down_halfwidth = start_pin.get_trace_neckdown_halfwidth(layer_active_no);

      if (neck_down_halfwidth >= pen_half_width_arr[layer_active_no]) return prev_corner;

      // check, that the neck_down started inside the pin shape
      if (!prev_corner.equals(start_pin.center_get()))
         {
         BrdItem picked_item = r_board.pick_nearest_routing_item(prev_corner, layer_active_no, null);
         if (picked_item instanceof BrdTracep)
            {
            if (((BrdTracep) picked_item).get_half_width() > neck_down_halfwidth)
               {
               return prev_corner;
               }
            }
         }
      
      TimeLimit time_limit = new TimeLimit(s_CHECK_FORCED_TRACE_TIME_MAX);
      
      PlaPointInt ok_point = r_board.insert_trace (
            prev_corner, 
            p_to_corner, 
            neck_down_halfwidth, 
            layer_active_no, 
            net_nos, 
            clearance_class, 
            max_shove_trace_recursion_depth,
            max_shove_via_recursion_depth, 
            max_spring_over_recursion_depth, 
            itera_settings.trace_pull_tight_region_width, 
            itera_settings.trace_pullt_min_move, 
            ! is_stitch_mode, 
            time_limit);
      
      return ok_point;
      }

   /**
    * If the routed ends at a pin and the route failed with the normal trace width, another try with the smalllest pin width is
    * done. Returns the ok_point of the try, which is p_from_corner, if the try failed.
    */
   private PlaPointInt try_neckdown_at_end(PlaPointInt p_from_corner, PlaPointInt p_to_corner)
      {
      if (!(nearest_target_item instanceof BrdAbitPin)) return p_from_corner;

      BrdAbitPin target_pin = (BrdAbitPin) nearest_target_item;

      if (!target_pin.is_on_layer(layer_active_no)) return p_from_corner;

      PlaPointFloat pin_center = target_pin.center_get().to_float();
      double curr_clearance = r_board.brd_rules.clearance_matrix.value_at(clearance_class, target_pin.clearance_idx(), layer_active_no);
      double pin_neck_down_distance = 2 * (0.5 * target_pin.get_max_width(layer_active_no) + curr_clearance);

      if (pin_center.distance(p_from_corner.to_float()) >= pin_neck_down_distance)
         {
         return p_from_corner;
         }
      
      int neck_down_halfwidth = target_pin.get_trace_neckdown_halfwidth(layer_active_no);
      
      if (neck_down_halfwidth >= pen_half_width_arr[layer_active_no])
         {
         return p_from_corner;
         }
      
      TimeLimit time_limit = new TimeLimit(s_CHECK_FORCED_TRACE_TIME_MAX);
      
      PlaPointInt ok_point = r_board.insert_trace (
            p_from_corner, 
            p_to_corner, 
            neck_down_halfwidth, 
            layer_active_no, 
            net_nos, 
            clearance_class, 
            max_shove_trace_recursion_depth,
            max_shove_via_recursion_depth, 
            max_spring_over_recursion_depth, 
            itera_settings.trace_pull_tight_region_width, 
            itera_settings.trace_pullt_min_move, 
            !is_stitch_mode, time_limit);
      
      return ok_point;
      }




   }