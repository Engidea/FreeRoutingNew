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
package board.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import main.Ldbg;
import main.Mdbg;
import board.BrdFromSide;
import board.BrdShapeAndFromSide;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdArea;
import board.items.BrdAreaConduction;
import board.items.BrdAreaObstacleComp;
import board.items.BrdAreaObstacleVia;
import board.items.BrdItem;
import board.items.BrdOutline;
import board.items.BrdTracep;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTraceEntries;
import board.shape.ShapeTreeObject;
import board.varie.BrdStopConnection;
import board.varie.TraceAngleRestriction;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaVectorInt;
import freert.planar.Polyline;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import freert.varie.TimeLimit;

/**
 * Contains internal auxiliary functions of class RoutingBoard for shoving traces
 *
 * @author Alfons Wirtz
 */
public final class AlgoShoveTrace
   {
   private static final String classname="AlgoShoveTrace.";
   
   private final RoutingBoard r_board;
   
   public AlgoShoveTrace(RoutingBoard p_board)
      {
      r_board = p_board;
      }

   /**
    * Checks if a shove with the input parameters is possible without clearance violations 
    * p_dir is used internally to prevent the check from bouncing back. 
    * @return false, if the shove failed.
    */
   public boolean shove_trace_check(
         ShapeTile p_trace_shape, 
         BrdFromSide p_from_side, 
         PlaDirection p_dir, 
         int p_layer, 
         NetNosList p_net_no_arr, 
         int p_cl_type, 
         int p_max_recursion_depth,
         int p_max_via_recursion_depth, 
         int p_max_spring_over_recursion_depth, 
         TimeLimit p_time_limit)
      {
      if ( is_stop_requested(p_time_limit) )
         {
         if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: stop_requested");

         return false;
         }

      if (p_trace_shape.is_empty())
         {
         if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: p_trace_shape is empty");
         
         return true;
         }

      if (!p_trace_shape.is_contained_in(r_board.get_bounding_box()))
         {
         r_board.set_shove_failing_obstacle(r_board.get_outline());
         return false;
         }
      
      ShapeTraceEntries shape_entries = new ShapeTraceEntries(p_trace_shape, p_layer, p_net_no_arr, p_cl_type, p_from_side, r_board);
      ShapeSearchTree search_tree = r_board.search_tree_manager.get_default_tree();
      Collection<BrdItem> obstacles = search_tree.find_overlap_items_with_clearance(p_trace_shape, p_layer, NetNosList.EMPTY, p_cl_type);
      obstacles.removeAll(get_ignore_items_at_tie_pins(p_trace_shape, p_layer, p_net_no_arr));
      boolean obstacles_shovable = shape_entries.store_items(obstacles, false, true);
      
      if (!obstacles_shovable)
         {
         BrdItem obstacle = shape_entries.get_found_obstacle();
         
         if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: !obstacles_shovable "+obstacle);
         
         r_board.set_shove_failing_obstacle(obstacle);
         return false;
         }
      
      int trace_piece_count = shape_entries.substitute_trace_count();

      if (shape_entries.stack_depth() > 1)
         {
         BrdItem obstacle = shape_entries.get_found_obstacle();

         if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: !obstacles_shovable "+obstacle);

         r_board.set_shove_failing_obstacle(obstacle);
         
         return false;
         }
      
      double shape_radius = 0.5 * p_trace_shape.bounding_box().min_width();

      // check, if the obstacle vias can be shoved

      for (BrdAbitVia curr_shove_via : shape_entries.shove_via_list)
         {
         if (curr_shove_via.shares_net_no(p_net_no_arr))  continue;

         if (p_max_via_recursion_depth <= 0)
            {
            if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: !p_max_via_recursion_depth <= 0 "+curr_shove_via);

            r_board.set_shove_failing_obstacle(curr_shove_via);
            return false;
            }

         PlaPointFloat curr_shove_via_center = curr_shove_via.center_get().to_float();
         PlaPointInt[] try_via_centers = r_board.move_drill_algo.try_shove_via_points(p_trace_shape, p_layer, curr_shove_via, p_cl_type, true);

         double max_dist = 0.5 * curr_shove_via.get_shape_on_layer(p_layer).bounding_box().max_width() + shape_radius;
         double max_dist_square = max_dist * max_dist;
         boolean shove_via_ok = false;
         
         for (int index = 0; index < try_via_centers.length; ++index)
            {
            if ( index == 0 || curr_shove_via_center.dustance_square(try_via_centers[index].to_float()) <= max_dist_square)
               {
               PlaVectorInt delta = try_via_centers[index].difference_by(curr_shove_via.center_get());
               Collection<BrdItem> ignore_items = new LinkedList<BrdItem>();
               if (r_board.move_drill_algo.check(curr_shove_via, delta, p_max_recursion_depth, p_max_via_recursion_depth - 1, ignore_items,  p_time_limit))
                  {
                  shove_via_ok = true;
                  break;
                  }
               }
            }
         
         if (!shove_via_ok)
            {
            return false;
            }
         }

      if (trace_piece_count == 0) return true;

      if (p_max_recursion_depth <= 0)
         {
         BrdItem obstacle = shape_entries.get_found_obstacle();

         if ( r_board.debug(Mdbg.PUSH_TRACE, Ldbg.SPC_A)) r_board.userPrintln(classname+"check: p_max_recursion_depth <= 0 "+obstacle);

         r_board.set_shove_failing_obstacle(obstacle);
         return false;
         }

      boolean is_orthogonal_mode = p_trace_shape instanceof ShapeTileBox;
      
      for (;;)
         {
         BrdTracep curr_substitute_trace = shape_entries.next_substitute_trace_piece();
      
         if (curr_substitute_trace == null) break;

         if (p_max_spring_over_recursion_depth > 0)
            {
            Polyline new_polyline = spring_over(
                  curr_substitute_trace.polyline(), 
                  curr_substitute_trace.get_compensated_half_width(search_tree), 
                  p_layer, 
                  curr_substitute_trace.net_nos,
                  curr_substitute_trace.clearance_idx(), 
                  false, p_max_spring_over_recursion_depth, null);

            // spring_over did not work
            if (new_polyline == null) return false;
            
            if (new_polyline != curr_substitute_trace.polyline())
               {
               // spring_over changed something
               --p_max_spring_over_recursion_depth;
               curr_substitute_trace.change(new_polyline);
               }
            }
         
         for (int index = 0; index < curr_substitute_trace.tile_shape_count(); ++index)
            {
            PlaDirection curr_dir = curr_substitute_trace.polyline().plaline(index + 1).direction();
            boolean is_in_front = p_dir == null || p_dir.equals(curr_dir);
            if (is_in_front)
               {
               BrdShapeAndFromSide curr = new BrdShapeAndFromSide(curr_substitute_trace, index, is_orthogonal_mode, true);
               if (!shove_trace_check(curr.shape, 
                     curr.from_side, 
                     curr_dir, 
                     p_layer, 
                     curr_substitute_trace.net_nos, 
                     curr_substitute_trace.clearance_idx(), 
                     p_max_recursion_depth - 1,
                     p_max_via_recursion_depth, 
                     p_max_spring_over_recursion_depth, 
                     p_time_limit))
                  {
                  return false;
                  }
               }
            }
         }
      
      return true;
      }

   private boolean is_stop_requested ( TimeLimit t_limit )
      {
      if (t_limit == null ) return false;
      
      return t_limit.is_stop_requested();
      }
   
   
   /**
    * Checks if a shove with the input parameters is possible without clearance violations 
    * The result is the maximum lenght of a trace from the start of the line segment to the end of the line segment, for wich the algoritm succeedes. 
    * If the algorithm succeedes completely, the result will be equal to Integer.MAX_VALUE.
    */
   public final double shove_trace_check( 
         PlaSegmentInt p_line_segment, 
         boolean p_shove_to_the_left, 
         int p_layer, 
         NetNosList p_net_no_arr, 
         int p_trace_half_width, 
         int p_cl_type,
         int p_max_recursion_depth, 
         int p_max_via_recursion_depth)
      {
      ShapeSearchTree search_tree = r_board.search_tree_manager.get_default_tree();
      
      if (search_tree.is_clearance_compensation_used())
         {
         p_trace_half_width += search_tree.get_clearance_compensation(p_cl_type, p_layer);
         }
      
      ArrayList<ShapeTile> trace_shapes = p_line_segment.to_polyline().offset_shapes(p_trace_half_width);
      
      if (trace_shapes.size() != 1)
         {
         System.out.println("ShoveTraceAlgo.check: trace_shape count 1 expected");
         return 0;
         }

      ShapeTile trace_shape = trace_shapes.get(0);
      if (trace_shape.is_empty())
         {
         System.out.println("ShoveTraceAlgo.check: trace_shape is empty");
         return 0;
         }
      
      // catches weird situations, I suppose...
      if (!trace_shape.is_contained_in(r_board.get_bounding_box())) return 0;
      
      BrdFromSide from_side = new BrdFromSide(p_line_segment, trace_shape, p_shove_to_the_left);
      ShapeTraceEntries shape_entries = new ShapeTraceEntries(trace_shape, p_layer, p_net_no_arr, p_cl_type, from_side, r_board);
      Collection<BrdItem> obstacles = search_tree.find_overlap_items_with_clearance(trace_shape, p_layer, NetNosList.EMPTY, p_cl_type);
      boolean obstacles_shovable = shape_entries.store_items(obstacles, false, true);

      if (!obstacles_shovable || shape_entries.trace_tails_in_shape())
         {
         return 0;
         }
      
      int trace_piece_count = shape_entries.substitute_trace_count();

      if (shape_entries.stack_depth() > 1)
         {
         return 0;
         }

      PlaPointFloat start_corner_appprox = p_line_segment.start_point_approx();
      PlaPointFloat end_corner_appprox = p_line_segment.end_point_approx();
      double segment_length = end_corner_appprox.distance(start_corner_appprox);

      freert.rules.ClearanceMatrix cl_matrix = r_board.brd_rules.clearance_matrix;

      double result = Integer.MAX_VALUE;

      // check, if the obstacle vias can be shoved

      for (BrdAbitVia curr_shove_via : shape_entries.shove_via_list)
         {
         if (curr_shove_via.shares_net_no(p_net_no_arr))
            {
            continue;
            }
         boolean shove_via_ok = false;
         if (p_max_via_recursion_depth > 0)
            {

            PlaPointInt[] new_via_center = r_board.move_drill_algo.try_shove_via_points(trace_shape, p_layer, curr_shove_via, p_cl_type, false);

            if (new_via_center.length <= 0)
               {
               return 0;
               }
            PlaVectorInt delta = new_via_center[0].difference_by(curr_shove_via.center_get());
            Collection<BrdItem> ignore_items = new LinkedList<BrdItem>();
            shove_via_ok = r_board.move_drill_algo.check(curr_shove_via, delta, p_max_recursion_depth, p_max_via_recursion_depth - 1, ignore_items, null);
            }

         if (!shove_via_ok)
            {
            PlaPointFloat via_center_appprox = curr_shove_via.center_get().to_float();
            double projection = start_corner_appprox.scalar_product(end_corner_appprox, via_center_appprox);
            projection /= segment_length;
            ShapeTileBox via_box = curr_shove_via.get_tree_shape_on_layer(search_tree, p_layer).bounding_box();
            double via_radius = 0.5 * via_box.max_width();
            double curr_ok_lenght = projection - via_radius - p_trace_half_width;
            if (!search_tree.is_clearance_compensation_used())
               {
               curr_ok_lenght -= cl_matrix.value_at(p_cl_type, curr_shove_via.clearance_idx(), p_layer);
               }
            if (curr_ok_lenght <= 0)
               {
               return 0;
               }
            result = Math.min(result, curr_ok_lenght);
            }
         }
      if (trace_piece_count == 0)
         {
         return result;
         }
      if (p_max_recursion_depth <= 0)
         {
         return 0;
         }

      PlaDirection line_direction = p_line_segment.get_line().direction();
      for (;;)
         {
         BrdTracep curr_substitute_trace = shape_entries.next_substitute_trace_piece();
         if (curr_substitute_trace == null)
            {
            break;
            }
         for (int index = 0; index < curr_substitute_trace.tile_shape_count(); ++index)
            {
            Polyline a_poly = curr_substitute_trace.polyline();

            PlaSegmentInt curr_line_segment = a_poly.segment_get(index + 1);
            
            if (p_shove_to_the_left)
               {
               // swap the line segmment to get the corredct shove length
               // in case it is smmaller than the length of the whole line segmment.
               curr_line_segment = curr_line_segment.opposite();
               }
            boolean is_in_front = curr_line_segment.get_line().direction().equals(line_direction);
            
            if (is_in_front)
               {
               double shove_ok_length = shove_trace_check(
                     curr_line_segment, 
                     p_shove_to_the_left, 
                     p_layer, 
                     curr_substitute_trace.net_nos, 
                     curr_substitute_trace.get_half_width(),
                     curr_substitute_trace.clearance_idx(), 
                     p_max_recursion_depth - 1,
                     p_max_via_recursion_depth);
               if (shove_ok_length < Integer.MAX_VALUE)
                  {
                  if (shove_ok_length <= 0)
                     {
                     return 0;
                     }
                  double projection = Math.min(start_corner_appprox.scalar_product(end_corner_appprox, curr_line_segment.start_point_approx()),
                        start_corner_appprox.scalar_product(end_corner_appprox, curr_line_segment.end_point_approx()));
                  projection /= segment_length;
                  double curr_ok_length = shove_ok_length + projection - p_trace_half_width - curr_substitute_trace.get_half_width();
                  if (search_tree.is_clearance_compensation_used())
                     {
                     curr_ok_length -= search_tree.get_clearance_compensation(curr_substitute_trace.clearance_idx(), p_layer);
                     }
                  else
                     {
                     curr_ok_length -= cl_matrix.value_at(p_cl_type, curr_substitute_trace.clearance_idx(), p_layer);
                     }
                  if (curr_ok_length <= 0)
                     {
                     return 0;
                     }
                  result = Math.min(curr_ok_length, result);
                  }
               break;
               }
            }
         }
      return result;
      }

   /**
    * Puts in a trace segment with the input parameters and shoves obstacles out of the way. 
    * If the shove does not work, the database may be damaged. To prevent this, call check first.
    */
   public boolean shove_trace_insert(
         ShapeTile p_trace_shape, 
         BrdFromSide p_from_side, 
         int p_layer, 
         NetNosList p_net_no_arr, 
         int p_cl_type,
         Collection<BrdItem> p_ignore_items, 
         int p_max_recursion_depth,
         int p_max_via_recursion_depth, 
         int p_max_spring_over_recursion_depth)
      {
      
      if (p_trace_shape.is_empty())
         {
         System.out.println("ShoveTraceAux.insert: p_trace_shape is empty");
         return true;
         }
      
      if (!p_trace_shape.is_contained_in(r_board.get_bounding_box()))
         {
         r_board.set_shove_failing_obstacle(r_board.get_outline());
         return false;
         }
  
      if ( ! r_board.move_drill_algo.shove_vias(
            p_trace_shape, 
            p_from_side, 
            p_layer, 
            p_net_no_arr, 
            p_cl_type, 
            p_ignore_items, 
            p_max_recursion_depth, 
            p_max_via_recursion_depth, 
            true))
         {
         return false;
         }
      
      ShapeTraceEntries shape_entries = new ShapeTraceEntries(p_trace_shape, p_layer, p_net_no_arr, p_cl_type, p_from_side, r_board);
      ShapeSearchTree search_tree = r_board.search_tree_manager.get_default_tree();
      Collection<BrdItem> obstacles = search_tree.find_overlap_items_with_clearance(p_trace_shape, p_layer, NetNosList.EMPTY, p_cl_type);
      obstacles.removeAll(get_ignore_items_at_tie_pins(p_trace_shape, p_layer, p_net_no_arr));
      boolean obstacles_shovable = shape_entries.store_items(obstacles, false, true);

      if (!shape_entries.shove_via_list.isEmpty())
         {
         obstacles_shovable = false;
         r_board.set_shove_failing_obstacle(shape_entries.shove_via_list.iterator().next());
         return false;
         }
      
      if (!obstacles_shovable)
         {
         r_board.set_shove_failing_obstacle(shape_entries.get_found_obstacle());
         return false;
         }
      
      int trace_piece_count = shape_entries.substitute_trace_count();
      
      if (trace_piece_count == 0) return true;

      if (p_max_recursion_depth <= 0)
         {
         r_board.set_shove_failing_obstacle(shape_entries.get_found_obstacle());
         return false;
         }

      boolean tails_exist_before = r_board.contains_trace_tails(obstacles, p_net_no_arr);
      shape_entries.cutout_traces(obstacles);
      boolean is_orthogonal_mode = p_trace_shape instanceof ShapeTileBox;
      for (;;)
         {
         BrdTracep curr_substitute_trace = shape_entries.next_substitute_trace_piece();
      
         if (curr_substitute_trace == null)  break;

         if (curr_substitute_trace.corner_first().equals(curr_substitute_trace.corner_last()))
            {
            continue;
            }
         if (p_max_spring_over_recursion_depth > 0)
            {
            Polyline new_polyline = spring_over(
                  curr_substitute_trace.polyline(), 
                  curr_substitute_trace.get_compensated_half_width(search_tree), 
                  p_layer, curr_substitute_trace.net_nos,
                  curr_substitute_trace.clearance_idx(), 
                  false, p_max_spring_over_recursion_depth, null);

            if (new_polyline == null)
               {
               // spring_over did not work
               return false;
               }
            if (new_polyline != curr_substitute_trace.polyline())
               {
               // spring_over changed something
               --p_max_spring_over_recursion_depth;
               curr_substitute_trace.change(new_polyline);
               }
            }
         NetNosList curr_net_no_arr = curr_substitute_trace.net_nos;
         for (int i = 0; i < curr_substitute_trace.tile_shape_count(); ++i)
            {
            BrdShapeAndFromSide curr = new BrdShapeAndFromSide(curr_substitute_trace, i, is_orthogonal_mode, false);
            if ( !  shove_trace_insert(
                  curr.shape, 
                  curr.from_side, 
                  p_layer, 
                  curr_net_no_arr, 
                  curr_substitute_trace.clearance_idx(), 
                  p_ignore_items, p_max_recursion_depth - 1, 
                  p_max_via_recursion_depth,
                  p_max_spring_over_recursion_depth))
               {
               return false;
               }
            }
         
         for (int i = 0; i < curr_substitute_trace.corner_count(); ++i)
            {
            r_board.join_changed_area(curr_substitute_trace.polyline().corner_approx(i), p_layer);
            }
         PlaPoint[] end_corners = null;
         if (!tails_exist_before)
            {
            end_corners = new PlaPoint[2];
            end_corners[0] = curr_substitute_trace.corner_first();
            end_corners[1] = curr_substitute_trace.corner_last();
            }
         
         r_board.insert_item(curr_substitute_trace);
         
         curr_substitute_trace.normalize(r_board.changed_area.get_area(p_layer));

         if ( ! tails_exist_before)  // TODO
            {
            for (int i = 0; i < 2; ++i)
               {
               BrdTracep tail = r_board.get_trace_tail(end_corners[i], p_layer, curr_net_no_arr);

               if (tail != null)
                  {
                  r_board.remove_items_unfixed(tail.get_connection_items(BrdStopConnection.VIA));
                  for (int curr_net_no : curr_net_no_arr)
                     {
                     r_board.combine_traces(curr_net_no);
                     }
                  }
               }
            }
         }
      return true;
      }

   Collection<BrdItem> get_ignore_items_at_tie_pins(ShapeTile p_trace_shape, int p_layer, NetNosList p_net_no_arr)
      {
      Collection<ShapeTreeObject> overlaps = r_board.overlapping_objects(p_trace_shape, p_layer);

      Set<BrdItem> result = new TreeSet<BrdItem>();

      for (ShapeTreeObject curr_object : overlaps)
         {
         if ( ! (curr_object instanceof BrdAbitPin) ) continue;
         
         BrdAbitPin curr_pin = (BrdAbitPin) curr_object;
         
         if (curr_pin.shares_net_no(p_net_no_arr))
            {
            result.addAll(curr_pin.get_all_contacts(p_layer));
            }
         }
      return result;
      }

   /**
    * Checks, if there are obstacle in the way of p_polyline and tries to wrap the polyline trace around these obstacles in counter clock sense. 
    * Returns null, if that is not possible. 
    * Returns p_polyline, if there were no obstacles 
    * If p_contact_pins != null, all pins not contained in p_contact_pins are regarded as obstacles, even if they are of the own net.
    */
   private final Polyline spring_over(
         Polyline p_polyline, 
         int p_half_width, 
         int p_layer, 
         NetNosList p_net_no_arr, 
         int p_cl_type, 
         boolean p_over_connected_pins, 
         int p_recursion_depth,
         Set<BrdAbitPin> p_contact_pins)
      {
      BrdItem found_obstacle = null;
      ShapeTileBox found_obstacle_bounding_box = null;
      ShapeSearchTree search_tree = r_board.search_tree_manager.get_default_tree();
      NetNosList check_net_no_arr;
      
      if (p_contact_pins == null)
         check_net_no_arr = p_net_no_arr;
      else
         check_net_no_arr = NetNosList.EMPTY;
      
      for (int index = 0; index < p_polyline.plalinelen(-2); ++index)
         {
         ShapeTile curr_shape = p_polyline.offset_shape(p_half_width, index);
         
         Collection<BrdItem> obstacles = search_tree.find_overlap_items_with_clearance(curr_shape, p_layer, check_net_no_arr, p_cl_type);
         
         for ( BrdItem curr_item : obstacles )
            {
            boolean is_obstacle;
         
            if (curr_item.shares_net_no(p_net_no_arr))
               {
               // to avoid acid traps
               is_obstacle = curr_item instanceof BrdAbitPin && p_contact_pins != null && !p_contact_pins.contains(curr_item);
               }
            else if (curr_item instanceof BrdAreaConduction)
               {
               is_obstacle = ((BrdAreaConduction) curr_item).get_is_obstacle();
               }
            else if (curr_item instanceof BrdAreaObstacleVia || curr_item instanceof BrdAreaObstacleComp)
               {
               is_obstacle = false;
               }
            else if (curr_item instanceof BrdTracep)
               {
               if (curr_item.is_shove_fixed())
                  {
                  is_obstacle = true;
                  
                  if (curr_item instanceof BrdTracep)
                     {
                     // check for a shove fixed trace exit stub, which has to be be ignored at a tie pin.
                     Collection<BrdItem> curr_contacts = curr_item.get_normal_contacts();
                     for (BrdItem curr_contact : curr_contacts)
                        {
                        if (curr_contact.shares_net_no(p_net_no_arr))
                           {
                           is_obstacle = false;
                           }
                        }
                     }
                  }
               else
                  {
                  // a unfixed trace can be pushed aside eventually
                  is_obstacle = false;
                  }
               }
            else
               {
               // a unfixed via can be pushed aside eventually
               is_obstacle = !curr_item.is_route();
               }

            if (is_obstacle)
               {
               if (found_obstacle == null)
                  {
                  found_obstacle = curr_item;
                  found_obstacle_bounding_box = curr_item.bounding_box();
                  }
               else if (found_obstacle != curr_item)
                  {
                  // check, if 1 obstacle is contained in the other obstacle and take the bigger obstacle in this case.
                  // That may happen in case of fixed vias inside of pins.
                  ShapeTileBox curr_item_bounding_box = curr_item.bounding_box();
                  if (found_obstacle_bounding_box.intersects(curr_item_bounding_box))
                     {
                     if (curr_item_bounding_box.contains(found_obstacle_bounding_box))
                        {
                        found_obstacle = curr_item;
                        found_obstacle_bounding_box = curr_item_bounding_box;
                        }
                     else if (!found_obstacle_bounding_box.contains(curr_item_bounding_box))
                        {
                        return null;
                        }
                     }
                  }
               }
            }
         
         if (found_obstacle != null)  break;
         }
      
      if (found_obstacle == null)
         {
         // no obstacle in the way, nothing to do
         return p_polyline;
         }

      if (p_recursion_depth <= 0 || found_obstacle instanceof BrdOutline || (found_obstacle instanceof BrdTracep && !found_obstacle.is_shove_fixed()))
         {
         r_board.set_shove_failing_obstacle(found_obstacle);
         return null;
         }
      
      boolean try_spring_over = true;
      if (!p_over_connected_pins)
         {
         // Check if the obstacle has a trace contact on p_layer
         Collection<BrdItem> contacts_on_layer = found_obstacle.get_all_contacts(p_layer);
         for (BrdItem curr_contact : contacts_on_layer)
            {
            if (curr_contact instanceof BrdTracep)
               {
               try_spring_over = false;
               break;
               }
            }
         }
      ShapeConvex obstacle_shape = null;
      if (try_spring_over)
         {
         if (found_obstacle instanceof BrdArea || found_obstacle instanceof BrdTracep)
            {
            if (found_obstacle.tree_shape_count(search_tree) == 1)
               {
               obstacle_shape = found_obstacle.get_tree_shape(search_tree, 0);
               }
            else
               {
               try_spring_over = false;
               }
            }
         else if (found_obstacle instanceof BrdAbit)
            {
            BrdAbit found_drill_item = (BrdAbit) found_obstacle;
            obstacle_shape = (found_drill_item.get_tree_shape_on_layer(search_tree, p_layer));
            }
         }
      
      
      if (!try_spring_over)
         {
         r_board.set_shove_failing_obstacle(found_obstacle);
         return null;
         }
      
      ShapeTile offset_shape;
      if (search_tree.is_clearance_compensation_used())
         {
         int offset = p_half_width + 1;
         offset_shape = (ShapeTile) obstacle_shape.enlarge(offset);
         }
      else
         {
         // enlarge the shape in 2 steps for symmetry reasons
         int offset = p_half_width + 1;
         double half_cl_offset = 0.5 * r_board.get_clearance(found_obstacle.clearance_idx(), p_cl_type, p_layer);
         offset_shape = (ShapeTile) obstacle_shape.enlarge(offset + half_cl_offset);
         offset_shape = (ShapeTile) offset_shape.enlarge(half_cl_offset);
         }
      
      if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
         {
         offset_shape = offset_shape.bounding_box();
         }
      
      else if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.FORTYFIVE_DEGREE)
         {
         offset_shape = offset_shape.bounding_octagon();
         }

      if (offset_shape.contains_inside(p_polyline.corner_first()) || offset_shape.contains_inside(p_polyline.corner_last()))
         {
         // can happen with clearance compensation off because of asymmetry in calculations with the offset shapes
         r_board.set_shove_failing_obstacle(found_obstacle);
         return null;
         }
      
      int[][] entries = offset_shape.entrance_points(p_polyline);
      if (entries.length == 0)
         {
         return p_polyline; // no obstacle
         }

      if (entries.length < 2)
         {
         r_board.set_shove_failing_obstacle(found_obstacle);
         return null;
         }
      
      ArrayList<Polyline> pieces = offset_shape.cutout(p_polyline); 
      
      // build a circuit around the offset_shape in counter clock sense
      // from the first intersection point to the second intersection point
      int first_intersection_side_no = entries[0][1];
      int last_intersection_side_no = entries[entries.length - 1][1];
      int first_intersection_line_no = entries[0][0];
      int last_intersection_line_no = entries[entries.length - 1][0];
      int side_diff = last_intersection_side_no - first_intersection_side_no;
      if (side_diff < 0)
         {
         side_diff += offset_shape.border_line_count();
         }
      else if (side_diff == 0)
         {
         PlaPointFloat compare_corner = offset_shape.corner_approx(first_intersection_side_no);
         PlaPointFloat first_intersection = p_polyline.plaline(first_intersection_line_no).intersection_approx(offset_shape.border_line(first_intersection_side_no));
         PlaPointFloat second_intersection = p_polyline.plaline(last_intersection_line_no).intersection_approx(offset_shape.border_line(last_intersection_side_no));
         if (compare_corner.distance(second_intersection) < compare_corner.distance(first_intersection))
            {
            side_diff += offset_shape.border_line_count();
            }
         }
      PlaLineInt[] substitute_lines = new PlaLineInt[side_diff + 3];
      substitute_lines[0] = p_polyline.plaline(first_intersection_line_no);
      int curr_edge_line_no = first_intersection_side_no;

      for (int index = 1; index <= side_diff + 1; ++index)
         {
         substitute_lines[index] = offset_shape.border_line(curr_edge_line_no);
         if (curr_edge_line_no == offset_shape.border_line_count() - 1)
            {
            curr_edge_line_no = 0;
            }
         else
            {
            ++curr_edge_line_no;
            }
         }
      substitute_lines[side_diff + 2] = p_polyline.plaline(last_intersection_line_no);
      Polyline substitute_polyline = new Polyline(substitute_lines);
      Polyline result = substitute_polyline;

      if (pieces.size() > 0)
         {
         result = pieces.get(0).combine(substitute_polyline);
         }
      
      if (pieces.size() > 1)
         {
         result = result.combine(pieces.get(1));
         }
      
      return spring_over(result, p_half_width, p_layer, p_net_no_arr, p_cl_type, p_over_connected_pins, p_recursion_depth - 1, p_contact_pins);
      }

   /**
    * damiano, can this be used to push violations ?
    * Checks, if there are obstacle in the way of p_polyline and tries to wrap the polyline trace around these obstacles. 
    * Returns null, if that is not possible. 
    * Returns p_polyline, if there were no obstacles 
    * This function looks contrary to the previous function for the shortest way around the obstacles. 
    * If p_contact_pins != null, all pins not contained in p_contact_pins are regarded as obstacles, even if they are of the own net.
    */
   public Polyline spring_over_obstacles(Polyline p_polyline, int p_half_width, int p_layer, NetNosList p_net_no_arr, int p_cl_type, Set<BrdAbitPin> p_contact_pins)
      {
      final int c_max_spring_over_recursion_depth = 20;
      
      Polyline counter_clock_wise_result = spring_over(p_polyline, p_half_width, p_layer, p_net_no_arr, p_cl_type, true, c_max_spring_over_recursion_depth, p_contact_pins);

      if (counter_clock_wise_result == p_polyline)
         {
         return p_polyline; // no obstacle
         }

      Polyline clock_wise_result = spring_over(p_polyline.reverse(), p_half_width, p_layer, p_net_no_arr, p_cl_type, true, c_max_spring_over_recursion_depth, p_contact_pins);
      Polyline result = null;
      if (clock_wise_result != null && counter_clock_wise_result != null)
         {
         if (clock_wise_result.length_approx() <= counter_clock_wise_result.length_approx())
            {
            result = clock_wise_result.reverse();
            }
         else
            {
            result = counter_clock_wise_result;
            }

         }
      else if (clock_wise_result != null)
         {
         result = clock_wise_result.reverse();
         }
      else if (counter_clock_wise_result != null)
         {
         result = counter_clock_wise_result;
         }

      return result;
      }
   }