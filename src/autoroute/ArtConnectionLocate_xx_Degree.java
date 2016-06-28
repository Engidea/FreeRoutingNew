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
 * LocateFoundConnectionAlgo.java
 *
 * Created on 14. Februar 2004, 07:55
 */

package autoroute;

import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import autoroute.expand.ExpandRoomComplete;
import autoroute.maze.MazeSearchResult;
import autoroute.varie.ArtBacktrackElement;
import board.awtree.AwtreeShapeSearch;
import board.items.BrdItem;
import board.varie.TraceAngleRestriction;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentFloat;
import freert.planar.PlaSide;
import freert.planar.ShapeTile;

/**
 * Calculates from the backtrack list the location of the traces and vias, which realize a connection found by the maze search
 * algorithm.
 *
 * @author Alfons Wirtz
 */
final class ArtConnectionLocate_xx_Degree extends ArtConnectionLocate
   {
   static private final double c_tolerance = 1.0;
   
   protected ArtConnectionLocate_xx_Degree(MazeSearchResult p_maze_search_result, ArtControl p_ctrl, AwtreeShapeSearch p_search_tree, TraceAngleRestriction p_angle_restriction, SortedSet<BrdItem> p_ripped_item_list )
      {
      super(p_maze_search_result, p_ctrl, p_search_tree, p_angle_restriction, p_ripped_item_list );
      }

   /**
    * Calculates a list with the next point of the trace under construction. 
    * If the trace is completed, the result list will be empty.
    */
   protected Collection<PlaPointFloat> calculate_next_trace_corners()
      {
      Collection<PlaPointFloat> result = new LinkedList<PlaPointFloat>();
      
      if (current_to_door_index >= current_target_door_index)
         {
         if (current_to_door_index == current_target_door_index)
            {
            PlaPointFloat nearest_point = current_target_shape.nearest_point_approx(current_from_point.round());
            ++current_to_door_index;
            result.add(nearest_point);
            }
         return result;
         }

      double trace_halfwidth_exact = art_ctrl.compensated_trace_half_width[current_trace_layer];
      double trace_halfwidth_max = trace_halfwidth_exact + ArtEngine.TRACE_WIDTH_TOLERANCE;
      double trace_halfwidth_middle = trace_halfwidth_exact + c_tolerance;

      ArtBacktrackElement curr_to_info = backtrack_array[current_to_door_index];
      
      PlaPointFloat door_left_corner = calc_door_left_corner(curr_to_info);
      
      if ( door_left_corner == null ) return result;
      
      PlaPointFloat door_right_corner = calc_door_right_corner(curr_to_info);

      if ( door_right_corner == null ) return result;
 
      if (current_from_point.side_of(door_left_corner, door_right_corner) != PlaSide.ON_THE_LEFT)
         {
         // the door is already crossed at this.from_point
         
         if (current_from_point.scalar_product(previous_from_point, door_left_corner) >= 0)
            {
            // Also the left corner of the door is passed.
            // That may not be the case if the door line is crossed almost parallel.
            door_left_corner = null;
            }
         
         if (current_from_point.scalar_product(previous_from_point, door_right_corner) >= 0)
            {
            // Also the right corner of the door is passed.
            door_right_corner = null;
            }
      
         if (door_left_corner == null && door_right_corner == null)
            {
            // The door is completely passed.
            current_to_door_index++;
            result.add(current_from_point);
            return result;
            }
         }

      // Calculate the visibility range for a trace line from current_from_point
      // through the interval from left_most_visible_point to right_most_visible_point,
      // by advancing the door index as far as possible, so that still somthing is visible.

      boolean end_of_trace = false;
      PlaPointFloat left_tangent_point = null;
      PlaPointFloat right_tangent_point = null;
      int new_door_ind = current_to_door_index;
      int left_ind = new_door_ind;
      int right_ind = new_door_ind;
      int curr_door_ind = current_to_door_index + 1;
      PlaPointFloat result_corner = null;

      // construct a maximum length straight line through the doors

      for (;;)
         {
         left_tangent_point = current_from_point.right_tangential_point(door_left_corner, trace_halfwidth_max);
         if (door_left_corner != null && left_tangent_point == null)
            {
            System.out.println("LocateFoundConnectionAlgo.calculate_next_trace_corner: left tangent point is null");
            left_tangent_point = door_left_corner;
            }
         right_tangent_point = current_from_point.left_tangential_point(door_right_corner, trace_halfwidth_max);
         if (door_right_corner != null && right_tangent_point == null)
            {
            System.out.println("LocateFoundConnectionAlgo.calculate_next_trace_corner: right tangent point is null");
            right_tangent_point = door_right_corner;
            }
         if (left_tangent_point != null && right_tangent_point != null && right_tangent_point.side_of(current_from_point, left_tangent_point) != PlaSide.ON_THE_RIGHT)
            {
            // The gap between left_most_visible_point and right_most_visible_point ist to small
            // for a trace with the current half width.

            double left_corner_distance = door_left_corner.distance(current_from_point);
            double right_corner_distance = door_right_corner.distance(current_from_point);
            if (left_corner_distance <= right_corner_distance)
               {
               new_door_ind = left_ind;
               result_corner = left_turn_next_corner(current_from_point, trace_halfwidth_max, door_left_corner, door_right_corner);
               }
            else
               {
               new_door_ind = right_ind;
               result_corner = right_turn_next_corner(current_from_point, trace_halfwidth_max, door_right_corner, door_left_corner);
               }
            break;
            }
         if (curr_door_ind >= current_target_door_index)
            {
            end_of_trace = true;
            break;
            }
         ArtBacktrackElement next_to_info = backtrack_array[curr_door_ind];
         PlaPointFloat next_left_corner = calc_door_left_corner(next_to_info);
         PlaPointFloat next_right_corner = calc_door_right_corner(next_to_info);
         if (current_from_point.side_of(next_left_corner, next_right_corner) != PlaSide.ON_THE_RIGHT)
            {
            // the door may be already crossed at this.from_point
            if (door_left_corner == null && current_from_point.scalar_product(previous_from_point, next_left_corner) >= 0)
               {
               // Also the left corner of the door is passed.
               // That may not be the case if the door line is crossed almost parallel.
               next_left_corner = null;
               }
            if (door_right_corner == null && current_from_point.scalar_product(previous_from_point, next_right_corner) >= 0)
               {
               // Also the right corner of the door is passed.
               next_right_corner = null;
               }
            if (next_left_corner == null && next_right_corner == null)
               {
               // The door is completely passed.
               // Should not happen because the previous door was not passed completely.
               System.out.println("LocateFoundConnectionAlgo.calculate_next_trace_corner: next door passed unexpected");
               ++current_to_door_index;
               result.add(current_from_point);
               return result;
               }
            }
         if (door_left_corner != null && door_right_corner != null)
            {
            // otherwise the following side_of conditions may not be correct
            // even if all parameter points are defined
            if (next_left_corner.side_of(current_from_point, door_right_corner) == PlaSide.ON_THE_RIGHT)
               {
               // bend to the right
               new_door_ind = right_ind + 1;
               result_corner = right_turn_next_corner(current_from_point, trace_halfwidth_max, door_right_corner, next_left_corner);
               break;
               }

            if (next_right_corner.side_of(current_from_point, door_left_corner) == PlaSide.ON_THE_LEFT)
               {
               // bend to the left
               new_door_ind = left_ind + 1;
               result_corner = left_turn_next_corner(current_from_point, trace_halfwidth_max, door_left_corner, next_right_corner);
               break;
               }
            }
         boolean visability_range_gets_smaller_on_the_right_side = (door_right_corner == null);
         if (door_right_corner != null && next_right_corner.side_of(current_from_point, door_right_corner) != PlaSide.ON_THE_RIGHT)
            {
            PlaPointFloat curr_tangential_point = current_from_point.left_tangential_point(next_right_corner, trace_halfwidth_max);
            if (curr_tangential_point != null)
               {
               PlaSegmentFloat check_line = new PlaSegmentFloat(current_from_point, curr_tangential_point);
               if (check_line.segment_distance(door_right_corner) >= trace_halfwidth_max)
                  {
                  visability_range_gets_smaller_on_the_right_side = true;
                  }
               }
            }
         if (visability_range_gets_smaller_on_the_right_side)
            {
            // The visibility range gets smaller on the right side.
            door_right_corner = next_right_corner;
            right_ind = curr_door_ind;
            }
         boolean visability_range_gets_smaller_on_the_left_side = (door_left_corner == null);
         if (door_left_corner != null && next_left_corner.side_of(current_from_point, door_left_corner) != PlaSide.ON_THE_LEFT)
            {
            PlaPointFloat curr_tangential_point = current_from_point.right_tangential_point(next_left_corner, trace_halfwidth_max);
            if (curr_tangential_point != null)
               {
               PlaSegmentFloat check_line = new PlaSegmentFloat(current_from_point, curr_tangential_point);
               if (check_line.segment_distance(door_left_corner) >= trace_halfwidth_max)
                  {
                  visability_range_gets_smaller_on_the_left_side = true;
                  }
               }
            }
         if (visability_range_gets_smaller_on_the_left_side)
            {
            // The visibility range gets smaller on the left side.
            door_left_corner = next_left_corner;
            left_ind = curr_door_ind;
            }
         ++curr_door_ind;
         }

      if (end_of_trace)
         {
         PlaPointFloat nearest_point = current_target_shape.nearest_point_approx(current_from_point);
         result_corner = nearest_point;
         if (left_tangent_point != null && nearest_point.side_of(current_from_point, left_tangent_point) == PlaSide.ON_THE_LEFT)
            {
            // The nearest target point is to the left of the visible range, add another corner
            new_door_ind = left_ind + 1;
            PlaPointFloat target_right_corner = current_target_shape.corner_approx(current_target_shape.index_of_right_most_corner(current_from_point));
            PlaPointFloat curr_corner = right_left_tangential_point(current_from_point, target_right_corner, door_left_corner, trace_halfwidth_max);
            if (curr_corner != null)
               {
               result_corner = curr_corner;
               end_of_trace = false;
               }
            }
         else if (right_tangent_point != null && nearest_point.side_of(current_from_point, right_tangent_point) == PlaSide.ON_THE_RIGHT)
            {
            // The nearest target point is to the right of the visible range, add another corner
            PlaPointFloat target_left_corner = current_target_shape.corner_approx(current_target_shape.index_of_left_most_corner(current_from_point));
            new_door_ind = right_ind + 1;
            PlaPointFloat curr_corner = left_right_tangential_point(current_from_point, target_left_corner, door_right_corner, trace_halfwidth_max);
            if (curr_corner != null)
               {
               result_corner = curr_corner;
               end_of_trace = false;
               }
            }
         }
      
      if (end_of_trace)
         {
         new_door_ind = current_target_door_index;
         }

      // Check clearance violation with the previous door shapes and correct them in this case.

      PlaSegmentFloat check_line = new PlaSegmentFloat(current_from_point, result_corner);
      int check_from_door_index = Math.max(current_to_door_index - 5, current_from_door_index + 1);
      PlaPointFloat corrected_result = null;
      int corrected_door_ind = 0;
      for (int index = check_from_door_index; index < new_door_ind; ++index)
         {
         PlaPointFloat curr_left_corner = calc_door_left_corner(backtrack_array[index]);
         double curr_dist = check_line.segment_distance(curr_left_corner);
         if (Math.abs(curr_dist) < trace_halfwidth_middle)
            {
            PlaPointFloat curr_corrected_result = right_left_tangential_point(check_line.point_a, check_line.point_b, curr_left_corner, trace_halfwidth_max);
            if (curr_corrected_result != null)
               {
               if (corrected_result == null || curr_corrected_result.side_of(current_from_point, corrected_result) == PlaSide.ON_THE_RIGHT)
                  {
                  corrected_door_ind = index; 
                  corrected_result = curr_corrected_result;
                  }
               }
            }
         PlaPointFloat curr_right_corner = calc_door_right_corner(backtrack_array[index]);
         curr_dist = check_line.segment_distance(curr_right_corner);
         if (Math.abs(curr_dist) < trace_halfwidth_middle)
            {
            PlaPointFloat curr_corrected_result = left_right_tangential_point(check_line.point_a, check_line.point_b, curr_right_corner, trace_halfwidth_max);
            if (curr_corrected_result != null)
               {
               if (corrected_result == null || curr_corrected_result.side_of(current_from_point, corrected_result) == PlaSide.ON_THE_LEFT)
                  {
                  corrected_door_ind = index;
                  corrected_result = curr_corrected_result;
                  }
               }
            }
         }
      if (corrected_result != null)
         {
         result_corner = corrected_result;
         new_door_ind = Math.max(corrected_door_ind, current_to_door_index);
         }

      current_to_door_index = new_door_ind;
      if (result_corner != null && result_corner != current_from_point)
         {
         result.add(result_corner);
         }
      return result;
      }

   /**
    * Calculates the left most corner of the shape of p_to_info.door seen from the center of the common room with the previous door.
    */
   private PlaPointFloat calc_door_left_corner(ArtBacktrackElement p_to_info)
      {
      ExpandRoomComplete from_room = p_to_info.door.other_room(p_to_info.next_room);
      
      if ( from_room == null ) return null;
      
      PlaPointFloat pole = from_room.get_shape().centre_of_gravity();
      ShapeTile curr_to_door_shape = p_to_info.door.get_shape();
      int left_most_corner_no = curr_to_door_shape.index_of_left_most_corner(pole);
      return curr_to_door_shape.corner_approx(left_most_corner_no);
      }

   /**
    * Calculates the right most corner of the shape of p_to_info.door seen from the center of the common room with the previous
    * door.
    */
   private PlaPointFloat calc_door_right_corner(ArtBacktrackElement p_to_info)
      {
      ExpandRoomComplete from_room = p_to_info.door.other_room(p_to_info.next_room);
      
      if ( from_room == null ) return null;
      
      PlaPointFloat pole = from_room.get_shape().centre_of_gravity();
      ShapeTile curr_to_door_shape = p_to_info.door.get_shape();
      int right_most_corner_no = curr_to_door_shape.index_of_right_most_corner(pole);
      return curr_to_door_shape.corner_approx(right_most_corner_no);
      }

   /**
    * Calculates as first line the left side tangent from p_from_corner to the circle with center p_to_corner and radius p_dist. As
    * second line the right side tangent from p_to_corner to the circle with center p_next_corner and radius 2 * p_dist is
    * constructed. The second line is than translated by the distance p_dist to the left. Returned is the intersection of the first
    * and the second line.
    */
   private PlaPointFloat right_turn_next_corner(PlaPointFloat p_from_corner, double p_dist, PlaPointFloat p_to_corner, PlaPointFloat p_next_corner)
      {
      PlaPointFloat curr_tangential_point = p_from_corner.left_tangential_point(p_to_corner, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo.right_turn_next_corner: left tangential point is null");
         return p_from_corner;
         }
      PlaSegmentFloat first_line = new PlaSegmentFloat(p_from_corner, curr_tangential_point);
      curr_tangential_point = p_to_corner.right_tangential_point(p_next_corner, 2 * p_dist + c_tolerance);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo.right_turn_next_corner: right tangential point is null");
         return p_from_corner;
         }
      PlaSegmentFloat second_line = new PlaSegmentFloat(p_to_corner, curr_tangential_point);
      second_line = second_line.translate(p_dist);
      return first_line.intersection(second_line);
      }

   /**
    * Calculates as first line the right side tangent from p_from_corner to the circle with center p_to_corner and radius p_dist. As
    * second line the left side tangent from p_to_corner to the circle with center p_next_corner and radius 2 * p_dist is
    * constructed. The second line is than translated by the distance p_dist to the right. Returned is the intersection of the first
    * and the second line.
    */
   private PlaPointFloat left_turn_next_corner(PlaPointFloat p_from_corner, double p_dist, PlaPointFloat p_to_corner, PlaPointFloat p_next_corner)
      {
      PlaPointFloat curr_tangential_point = p_from_corner.right_tangential_point(p_to_corner, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo.left_turn_next_corner: right tangential point is null");
         return p_from_corner;
         }
      PlaSegmentFloat first_line = new PlaSegmentFloat(p_from_corner, curr_tangential_point);
      curr_tangential_point = p_to_corner.left_tangential_point(p_next_corner, 2 * p_dist + c_tolerance);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo.left_turn_next_corner: left tangential point is null");
         return p_from_corner;
         }
      PlaSegmentFloat second_line = new PlaSegmentFloat(p_to_corner, curr_tangential_point);
      second_line = second_line.translate(-p_dist);
      return first_line.intersection(second_line);
      }

   /**
    * Calculates the right tangential line from p_from_point and the left tangential line from p_to_point to the circle with center
    * p_center and radius p_dist. Returns the intersection of the 2 lines.
    */
   private PlaPointFloat right_left_tangential_point(PlaPointFloat p_from_point, PlaPointFloat p_to_point, PlaPointFloat p_center, double p_dist)
      {
      PlaPointFloat curr_tangential_point = p_from_point.right_tangential_point(p_center, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo. right_left_tangential_point: right tangential point is null");
         return null;
         }
      PlaSegmentFloat first_line = new PlaSegmentFloat(p_from_point, curr_tangential_point);
      curr_tangential_point = p_to_point.left_tangential_point(p_center, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo. right_left_tangential_point: left tangential point is null");
         return null;
         }
      PlaSegmentFloat second_line = new PlaSegmentFloat(p_to_point, curr_tangential_point);
      return first_line.intersection(second_line);
      }

   /**
    * Calculates the left tangential line from p_from_point and the right tangential line from p_to_point to the circle with center
    * p_center and radius p_dist. Returns the intersection of the 2 lines.
    */
   private PlaPointFloat left_right_tangential_point(PlaPointFloat p_from_point, PlaPointFloat p_to_point, PlaPointFloat p_center, double p_dist)
      {
      PlaPointFloat curr_tangential_point = p_from_point.left_tangential_point(p_center, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo. left_right_tangential_point: left tangential point is null");
         return null;
         }
      PlaSegmentFloat first_line = new PlaSegmentFloat(p_from_point, curr_tangential_point);
      curr_tangential_point = p_to_point.right_tangential_point(p_center, p_dist);
      if (curr_tangential_point == null)
         {
         System.out.println("LocateFoundConnectionAlgo. left_right_tangential_point: right tangential point is null");
         return null;
         }
      PlaSegmentFloat second_line = new PlaSegmentFloat(p_to_point, curr_tangential_point);
      return first_line.intersection(second_line);
      }

   }
