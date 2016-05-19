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
 * ForcedViaAlgo.java
 *
 * Created on 25. April 2004, 09:55
 */

package board.algo;

import board.BrdFromSide;
import board.RoutingBoard;
import board.infos.BrdViaInfo;
import board.varie.ItemFixState;
import board.varie.ShoveDrillResult;
import board.varie.TraceAngleRestriction;
import freert.library.LibPadstack;
import freert.planar.ShapeCircle;
import freert.planar.PlaLimits;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileSimplex;
import freert.varie.NetNosList;

/**
 * Class with static functions for checking and inserting forced vias.
 *
 * @author alfons
 */
public final class AlgoShoveVia
   {
   private final RoutingBoard r_board;
   
   public AlgoShoveVia ( RoutingBoard p_board )
      {
      r_board = p_board;
      }
   
   /**
    * Checks, if a Via is possible at the input layer after evtl. shoving aside obstacle traces. p_room_shape is used for
    * calculating the from_side.
    */
   public ShoveDrillResult check_layer(
         double p_via_radius, 
         int p_cl_class, 
         boolean p_attach_smd_allowed, 
         ShapeTile p_room_shape, 
         PlaPoint p_location, 
         int p_layer,
         NetNosList p_net_no_arr, 
         int p_max_recursion_depth, 
         int p_max_via_recursion_depth)
      {
      if (p_via_radius <= 0)
         {
         return ShoveDrillResult.DRILLABLE;
         }
      if (!(p_location instanceof PlaPointInt))
         {
         return ShoveDrillResult.NOT_DRILLABLE;
         }
      ShapeConvex via_shape = new ShapeCircle((PlaPointInt) p_location, (int) Math.ceil(p_via_radius));

      double check_radius = p_via_radius + 0.5 * r_board.get_clearance(p_cl_class, p_cl_class, p_layer) + r_board.get_min_trace_half_width();

      ShapeTile tile_shape;
      boolean is_90_degree;
      if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
         {
         tile_shape = via_shape.bounding_box();
         is_90_degree = true;
         }
      else
         {
         tile_shape = via_shape.bounding_octagon();
         is_90_degree = false;
         }

      BrdFromSide from_side = calculate_from_side(p_location.to_float(), tile_shape, p_room_shape.to_Simplex(), check_radius, is_90_degree);

      if (from_side == null) return ShoveDrillResult.NOT_DRILLABLE;

      ShoveDrillResult result = r_board.shove_pad_algo.check_forced_pad(
            tile_shape, 
            from_side, 
            p_layer, 
            p_net_no_arr, 
            p_cl_class, 
            p_attach_smd_allowed, 
            null, 
            p_max_recursion_depth,
            p_max_via_recursion_depth, 
            false, 
            null);
      return result;
      }

   /**
    * Checks, if a Via is possible with the input parameter after evtl. shoving aside obstacle traces.
    */
   public boolean check(
         BrdViaInfo p_via_info, 
         PlaPointInt p_location, 
         NetNosList p_net_no_arr, 
         int p_max_recursion_depth, 
         int p_max_via_recursion_depth )
      {
      PlaVectorInt translate_vector = p_location.difference_by(PlaPointInt.ZERO);
      
      int calc_from_side_offset = r_board.get_min_trace_half_width();

      LibPadstack via_padstack = p_via_info.get_padstack();
      for (int index = via_padstack.from_layer(); index <= via_padstack.to_layer(); ++index)
         {
         PlaShape curr_pad_shape = via_padstack.get_shape(index);

         if (curr_pad_shape == null) continue;

         curr_pad_shape = (PlaShape) curr_pad_shape.translate_by(translate_vector);
         ShapeTile tile_shape;
         
         if (r_board.brd_rules.is_trace_snap_90())
            {
            // this is understandable
            tile_shape = curr_pad_shape.bounding_box();
            }
         else
            {
            // this is understandable, 
            tile_shape = curr_pad_shape.bounding_octagon();
            }
         
         BrdFromSide from_side = r_board.shove_pad_algo.calc_from_side(tile_shape, p_location, index, calc_from_side_offset, p_via_info.get_clearance_class());
         if ( r_board.shove_pad_algo.check_forced_pad(
               tile_shape, 
               from_side, 
               index, 
               p_net_no_arr, 
               p_via_info.get_clearance_class(), 
               p_via_info.attach_smd_allowed(), 
               null, 
               p_max_recursion_depth,
               p_max_via_recursion_depth, 
               false, 
               null) == ShoveDrillResult.NOT_DRILLABLE)
            {
            r_board.set_shove_failing_layer(index);
            return false;
            }
         }
      return true;
      }

   /**
    * Shoves aside traces, so that a via with the input parameters can be inserted without clearance violations. If the shove
    * failed, the database may be damaged, so that an undo becomes necessesary. p_trace_clearance_class_no and
    * p_trace_pen_halfwidth_arr is provided to make space for starting a trace in case the trace width is bigger than the via shape.
    * Returns false, if the forced via failed.
    */
   public boolean shove_via_insert(
         BrdViaInfo p_via_info, 
         PlaPointInt p_location, 
         NetNosList p_net_no_arr, 
         int p_trace_clearance_class_no, 
         int[] p_trace_pen_halfwidth_arr, 
         int p_max_recursion_depth,
         int p_max_via_recursion_depth )
      {
      PlaVectorInt translate_vector = p_location.difference_by(PlaPointInt.ZERO);
      
      int calc_from_side_offset = r_board.get_min_trace_half_width();

      LibPadstack via_padstack = p_via_info.get_padstack();
      for (int index = via_padstack.from_layer(); index <= via_padstack.to_layer(); ++index)
         {
         PlaShape curr_pad_shape = via_padstack.get_shape(index);
         
         if (curr_pad_shape == null) continue;
         
         curr_pad_shape = (PlaShape) curr_pad_shape.translate_by(translate_vector);
         ShapeTile tile_shape;
         ShapeCircle start_trace_circle;
         
         if (p_trace_pen_halfwidth_arr[index] > 0 && p_location instanceof PlaPointInt)
            {
            start_trace_circle = new ShapeCircle((PlaPointInt) p_location, p_trace_pen_halfwidth_arr[index]);
            }
         else
            {
            // ACK, this will fail miserably....
            start_trace_circle = null;
            }
         
         ShapeTile start_trace_shape = null;
         if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
            {
            tile_shape = curr_pad_shape.bounding_box();
            if (start_trace_circle != null)
               {
               start_trace_shape = start_trace_circle.bounding_box();
               }
            }
         else
            {
            tile_shape = curr_pad_shape.bounding_octagon();
            if (start_trace_circle != null)
               {
               start_trace_shape = start_trace_circle.bounding_octagon();
               }
            }
         
         BrdFromSide from_side = r_board.shove_pad_algo.calc_from_side(
               tile_shape, 
               p_location, 
               index, 
               calc_from_side_offset, 
               p_via_info.get_clearance_class());
         
         if (!r_board.shove_pad_algo.forced_pad(
               tile_shape, 
               from_side, 
               index, 
               p_net_no_arr, 
               p_via_info.get_clearance_class(), 
               p_via_info.attach_smd_allowed(), 
               null, 
               p_max_recursion_depth,
               p_max_via_recursion_depth))
            {
            r_board.set_shove_failing_layer(index);
            return false;
            }
         if (start_trace_shape != null)
            {
            // necessesary in case strart_trace_shape is bigger than tile_shape
            if ( ! r_board.shove_pad_algo.forced_pad(
                  start_trace_shape, 
                  from_side, 
                  index, 
                  p_net_no_arr, 
                  p_trace_clearance_class_no, 
                  true, 
                  null, 
                  p_max_recursion_depth, 
                  p_max_via_recursion_depth))
               {
               r_board.set_shove_failing_layer(index);
               return false;
               }
            }
         }
      r_board.insert_via(
            via_padstack, 
            p_location, 
            p_net_no_arr, 
            p_via_info.get_clearance_class(), 
            ItemFixState.UNFIXED, p_via_info.attach_smd_allowed());
      return true;
      }

   private BrdFromSide calculate_from_side(PlaPointFloat p_via_location, ShapeTile p_via_shape, ShapeTileSimplex p_room_shape, double p_dist, boolean is_90_degree)
      {
      ShapeTileBox via_box = p_via_shape.bounding_box();
      for (int index = 0; index < 4; ++index)
         {
         PlaPointFloat check_point;
         double border_x;
         double border_y;
         if (index == 0)
            {
            check_point = new PlaPointFloat(p_via_location.v_x, p_via_location.v_y - p_dist);
            border_x = p_via_location.v_x;
            border_y = via_box.box_ll.v_y;
            }
         else if (index == 1)
            {
            check_point = new PlaPointFloat(p_via_location.v_x + p_dist, p_via_location.v_y);
            border_x = via_box.box_ur.v_x;
            border_y = p_via_location.v_y;
            }
         else if (index == 2)
            {
            check_point = new PlaPointFloat(p_via_location.v_x, p_via_location.v_y + p_dist);
            border_x = p_via_location.v_x;
            border_y = via_box.box_ur.v_y;
            }
         else
            // i == 3
            {
            check_point = new PlaPointFloat(p_via_location.v_x - p_dist, p_via_location.v_y);
            border_x = via_box.box_ll.v_x;
            border_y = p_via_location.v_y;
            }
         if (p_room_shape.contains(check_point))
            {
            int from_side_no;
            if (is_90_degree)
               {
               from_side_no = index;
               }
            else
               {
               from_side_no = 2 * index;
               }
            PlaPointFloat curr_border_point = new PlaPointFloat(border_x, border_y);
            return new BrdFromSide(from_side_no, curr_border_point);
            }
         }
      if (is_90_degree)
         {
         return null;
         }
      // try the diagonal drections
      double dist = p_dist / PlaLimits.sqrt2;
      double border_dist = via_box.max_width() / (2 * PlaLimits.sqrt2);
      for (int index = 0; index < 4; ++index)
         {
         PlaPointFloat check_point;
         double border_x;
         double border_y;
         if (index == 0)
            {
            check_point = new PlaPointFloat(p_via_location.v_x + dist, p_via_location.v_y - dist);
            border_x = p_via_location.v_x + border_dist;
            border_y = p_via_location.v_y - border_dist;
            }
         else if (index == 1)
            {
            check_point = new PlaPointFloat(p_via_location.v_x + dist, p_via_location.v_y + dist);
            border_x = p_via_location.v_x + border_dist;
            border_y = p_via_location.v_y + border_dist;
            }
         else if (index == 2)
            {
            check_point = new PlaPointFloat(p_via_location.v_x - dist, p_via_location.v_y + dist);
            border_x = p_via_location.v_x - border_dist;
            border_y = p_via_location.v_y + border_dist;
            }
         else
            // i == 3
            {
            check_point = new PlaPointFloat(p_via_location.v_x - dist, p_via_location.v_y - dist);
            border_x = p_via_location.v_x - border_dist;
            border_y = p_via_location.v_y - border_dist;
            }
         if (p_room_shape.contains(check_point))
            {

            int from_side_no = 2 * index + 1;
            PlaPointFloat curr_border_point = new PlaPointFloat(border_x, border_y);
            return new BrdFromSide(from_side_no, curr_border_point);
            }
         }
      return null;
      }
   }
