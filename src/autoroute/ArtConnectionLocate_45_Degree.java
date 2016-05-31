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
 * LocateFoundConnectionAlgo45Degree.java
 *
 * Created on 1. Februar 2006, 07:43
 *
 */

package autoroute;

import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandObject;
import autoroute.expand.ExpandRoomObstacle;
import autoroute.maze.MazeSearchResult;
import autoroute.varie.ArtBacktrackElement;
import board.items.BrdItem;
import board.shape.ShapeSearchTree;
import board.varie.TraceAngleRestriction;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentFloat;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileSimplex;
import freert.varie.Signum;

/**
 *
 * @author Alfons Wirtz
 */
final class ArtConnectionLocate_45_Degree extends ArtConnectionLocate
   {
   public ArtConnectionLocate_45_Degree(MazeSearchResult p_maze_search_result, ArtControl p_ctrl, ShapeSearchTree p_search_tree, TraceAngleRestriction p_angle_restriction, SortedSet<BrdItem> p_ripped_item_list )
      {
      super(p_maze_search_result, p_ctrl, p_search_tree, p_angle_restriction, p_ripped_item_list );
      }

   protected Collection<PlaPointFloat> calculate_next_trace_corners()
      {
      Collection<PlaPointFloat> result = new LinkedList<PlaPointFloat>();

      if (current_to_door_index > current_target_door_index)
         {
         return result;
         } 

      ArtBacktrackElement curr_from_info = backtrack_array[current_to_door_index - 1];

      if (curr_from_info.next_room == null)
         {
         System.out.println("LocateFoundConnectionAlgo45Degree.calculate_next_trace_corners: next_room is null");
         return result;
         }

      ShapeTile room_shape = curr_from_info.next_room.get_shape();

      int trace_halfwidth = art_ctrl.compensated_trace_half_width[current_trace_layer];
      int trace_halfwidth_add = trace_halfwidth + ArtEngine.TRACE_WIDTH_TOLERANCE; // add some tolerance for free space
                                                                                         // expansion rooms.
      int shrink_offset;
      if (curr_from_info.next_room instanceof ExpandRoomObstacle)
         {

         shrink_offset = trace_halfwidth;
         }
      else
         {
         shrink_offset = trace_halfwidth_add;
         }

      ShapeTile shrinked_room_shape = room_shape.offset(-shrink_offset);
      if (!shrinked_room_shape.is_empty())
         {
         // enter the shrinked room shape by a 45 degree angle first
         PlaPointFloat nearest_room_point = shrinked_room_shape.nearest_point_approx(current_from_point);
         boolean horizontal_first = calc_horizontal_first_from_door(curr_from_info.door, current_from_point, nearest_room_point);
         nearest_room_point = round_to_integer(nearest_room_point);
         result.add(calculate_additional_corner(current_from_point, nearest_room_point, horizontal_first, angle_restriction));
         result.add(nearest_room_point);
         current_from_point = nearest_room_point;
         }
      else
         {
         shrinked_room_shape = room_shape;
         }

      if (current_to_door_index == current_target_door_index)
         {
         PlaPointFloat nearest_point = current_target_shape.nearest_point_approx(current_from_point);
         nearest_point = round_to_integer(nearest_point);
         PlaPointFloat add_corner = calculate_additional_corner(current_from_point, nearest_point, true, angle_restriction);
         if (!shrinked_room_shape.contains(add_corner))
            {
            add_corner = calculate_additional_corner(current_from_point, nearest_point, false, angle_restriction);
            }
         result.add(add_corner);
         result.add(nearest_point);
         ++current_to_door_index;
         return result;
         }

      ArtBacktrackElement curr_to_info = backtrack_array[current_to_door_index];
      if (!(curr_to_info.door instanceof ExpandDoor))
         {
         System.out.println("LocateFoundConnectionAlgo45Degree.calculate_next_trace_corners: ExpansionDoor expected");
         return result;
         }
      ExpandDoor curr_to_door = (ExpandDoor) curr_to_info.door;

      PlaPointFloat nearest_to_door_point;
      if ( curr_to_door.dimension.is_area() )
         {
         // May not happen in free angle routing mode because then corners are cut off.
         ShapeTile to_door_shape = curr_to_door.get_shape();

         ShapeTile shrinked_to_door_shape = to_door_shape.shrink(shrink_offset);
         nearest_to_door_point = shrinked_to_door_shape.nearest_point_approx(current_from_point);
         nearest_to_door_point = round_to_integer(nearest_to_door_point);
         }
      else
         {
         PlaSegmentFloat[] line_sections = curr_to_door.get_section_segments(trace_halfwidth);
         if (curr_to_info.section_no_of_door >= line_sections.length)
            {
            System.out.println("LocateFoundConnectionAlgo45Degree.calculate_next_trace_corners: line_sections inconsistent");
            return result;
            }
         PlaSegmentFloat curr_line_section = line_sections[curr_to_info.section_no_of_door];
         nearest_to_door_point = curr_line_section.nearest_segment_point(current_from_point);

         boolean nearest_to_door_point_ok = true;
         if (curr_to_info.next_room != null)
            {
            ShapeTileSimplex next_room_shape = curr_to_info.next_room.get_shape().to_Simplex();
            // with IntBox or IntOctagon the next calculation will not work, because they have
            // border lines of lenght 0.
            PlaPointFloat[] nearest_points = next_room_shape.nearest_border_points_approx(nearest_to_door_point, 2);
            if (nearest_points.length >= 2)
               {
               nearest_to_door_point_ok = nearest_points[1].distance(nearest_to_door_point) >= trace_halfwidth_add;
               }
            }
         if (!nearest_to_door_point_ok)
            {
            // may be the room has an acute (45 degree) angle at a corner of the door
            nearest_to_door_point = curr_line_section.point_a.middle_point(curr_line_section.point_b);
            }
         }
      nearest_to_door_point = round_to_integer(nearest_to_door_point);
      boolean horizontal_first = calc_horizontal_first_to_door(curr_to_info.door, current_from_point, nearest_to_door_point);
      result.add(calculate_additional_corner(current_from_point, nearest_to_door_point, horizontal_first, angle_restriction));
      result.add(nearest_to_door_point);
      ++current_to_door_index;
      return result;
      }

   private PlaPointFloat round_to_integer(PlaPointFloat p_point)
      {
      return p_point.round().to_float();
      }

   /**
    * Calculates, if the next 45-degree angle should be horizontal first when coming fromm p_from_point on p_from_door.
    */
   private static boolean calc_horizontal_first_from_door(ExpandObject p_from_door, PlaPointFloat p_from_point, PlaPointFloat p_to_point)
      {
      ShapeTile door_shape = p_from_door.get_shape();
      ShapeTileBox from_door_box = door_shape.bounding_box();

      if ( p_from_door.get_dimension().is_area() )
         {
         return from_door_box.height() >= from_door_box.width();
         }

      PlaSegmentFloat door_line_segment = door_shape.diagonal_corner_segment();
      PlaPointFloat left_corner;
      PlaPointFloat right_corner;
      if (door_line_segment.point_a.v_x < door_line_segment.point_b.v_x || door_line_segment.point_a.v_x == door_line_segment.point_b.v_x && door_line_segment.point_a.v_y <= door_line_segment.point_b.v_y)
         {
         left_corner = door_line_segment.point_a;
         right_corner = door_line_segment.point_b;
         }
      else
         {
         left_corner = door_line_segment.point_b;
         right_corner = door_line_segment.point_a;
         }
      double door_dx = right_corner.v_x - left_corner.v_x;
      double door_dy = right_corner.v_y - left_corner.v_y;
      double abs_door_dy = Math.abs(door_dy);
      double door_max_width = Math.max(door_dx, abs_door_dy);
      boolean result;
      double door_half_max_width = 0.5 * door_max_width;
      if (from_door_box.width() <= door_half_max_width)
         {
         // door is about vertical
         result = true;
         }
      else if (from_door_box.height() <= door_half_max_width)
         {
         // door is about horizontal
         result = false;
         }
      else
         {
         double dx = p_to_point.v_x - p_from_point.v_x;
         double dy = p_to_point.v_y - p_from_point.v_y;
         if (left_corner.v_y < right_corner.v_y)
            {
            // door is about right diagonal
            if (Signum.of(dx) == Signum.of(dy))
               {
               result = Math.abs(dx) > Math.abs(dy);
               }
            else
               {
               result = Math.abs(dx) < Math.abs(dy);
               }

            }
         else
            {
            // door is about left diagonal
            if (Signum.of(dx) == Signum.of(dy))
               {
               result = Math.abs(dx) < Math.abs(dy);
               }
            else
               {
               result = Math.abs(dx) > Math.abs(dy);
               }
            }
         }
      return result;
      }

   /**
    * Calculates, if the 45-degree angle to the next door shape should be horizontal first when coming fromm p_from_point.
    */
   private boolean calc_horizontal_first_to_door(ExpandObject p_to_door, PlaPointFloat p_from_point, PlaPointFloat p_to_point)
      {
      ShapeTile door_shape = p_to_door.get_shape();
      ShapeTileBox from_door_box = door_shape.bounding_box();

      if (p_to_door.get_dimension().is_area())
         {
         return from_door_box.height() <= from_door_box.width();
         }
      
      PlaSegmentFloat door_line_segment = door_shape.diagonal_corner_segment();
      PlaPointFloat left_corner;
      PlaPointFloat right_corner;
      if (door_line_segment.point_a.v_x < door_line_segment.point_b.v_x || door_line_segment.point_a.v_x == door_line_segment.point_b.v_x && door_line_segment.point_a.v_y <= door_line_segment.point_b.v_y)
         {
         left_corner = door_line_segment.point_a;
         right_corner = door_line_segment.point_b;
         }
      else
         {
         left_corner = door_line_segment.point_b;
         right_corner = door_line_segment.point_a;
         }
      double door_dx = right_corner.v_x - left_corner.v_x;
      double door_dy = right_corner.v_y - left_corner.v_y;
      double abs_door_dy = Math.abs(door_dy);
      double door_max_width = Math.max(door_dx, abs_door_dy);
      boolean result;
      double door_half_max_width = 0.5 * door_max_width;
      if (from_door_box.width() <= door_half_max_width)
         {
         // door is about vertical
         result = false;
         }
      else if (from_door_box.height() <= door_half_max_width)
         {
         // door is about horizontal
         result = true;
         }
      else
         {
         double dx = p_to_point.v_x - p_from_point.v_x;
         double dy = p_to_point.v_y - p_from_point.v_y;
         if (left_corner.v_y < right_corner.v_y)
            {
            // door is about right diagonal
            if (Signum.of(dx) == Signum.of(dy))
               {
               result = Math.abs(dx) < Math.abs(dy);
               }
            else
               {
               result = Math.abs(dx) > Math.abs(dy);
               }

            }
         else
            {
            // door is about left diagonal
            if (Signum.of(dx) == Signum.of(dy))
               {
               result = Math.abs(dx) > Math.abs(dy);
               }
            else
               {
               result = Math.abs(dx) < Math.abs(dy);
               }
            }
         }
      return result;
      }

   }
