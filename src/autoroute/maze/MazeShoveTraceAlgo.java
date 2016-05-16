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
 * Created on 10. Mai 2006, 06:41
 *
 */

package autoroute.maze;

import java.util.Collection;
import main.Ldbg;
import main.Mdbg;
import autoroute.ArtControl;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomObstacle;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdItem;
import board.items.BrdTracep;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentFloat;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaSide;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.varie.NetNosList;

/**
 * Auxiliary functions used in MazeSearchAlgo.
 * Do NOT confuse with ShoveTraeAlgo
 * @author Alfons Wirtz
 */
public final class MazeShoveTraceAlgo
   {
   private static final String classname="MazeShoveTraceAlgo.";
   
   private final RoutingBoard r_board;
   private final ArtControl art_ctrl;
   
   public MazeShoveTraceAlgo (RoutingBoard p_board,ArtControl p_ctrl )
      {
      r_board = p_board;
      art_ctrl = p_ctrl;
      }
   
   /**
    * Returns false if the algorithm did not succeed and trying to shove from another door section may be more successful.
    */
   boolean check_shove_trace_line(MazeListElement p_list_element, ExpandRoomObstacle p_obstacle_room,  boolean p_shove_to_the_left, Collection<MazeDoorSection> p_to_door_list)
      {
      if (!(p_list_element.door instanceof ExpandDoor)) return true;
      
      ExpandDoor from_door = (ExpandDoor) p_list_element.door;
      
      BrdItem an_item = p_obstacle_room.get_item();
      
      if (!(an_item instanceof BrdTracep))  return true;
      
      BrdTracep obstacle_trace = (BrdTracep)an_item;

      int trace_layer = p_obstacle_room.get_layer();
      
      // only traces with the same half width and the same clearance class can be shoved.
      if (obstacle_trace.get_half_width() != art_ctrl.trace_half_width[trace_layer] )
         {
         r_board.userPrintln("cannot shove != width "+obstacle_trace);
         return true;
         }
         
      if ( obstacle_trace.clearance_idx() != art_ctrl.trace_clearance_idx)
         {
         r_board.userPrintln("cannot shove != clearance "+obstacle_trace);
         return true;
         }
      
      double compensated_trace_half_width = art_ctrl.compensated_trace_half_width[trace_layer];
      ShapeTile from_door_shape = from_door.get_shape();
      if (from_door_shape.max_width() < 2 * compensated_trace_half_width)
         {
         r_board.userPrintln("cannot shove != 2*compe "+obstacle_trace);
         return true;
         }
      
      int trace_corner_no = p_obstacle_room.get_index_in_item();
      Polyline trace_polyline = obstacle_trace.polyline();

      if (trace_corner_no >= trace_polyline.corner_count() )
         {
         r_board.userPrintln("cannot shove trace_corner_no to big "+obstacle_trace);
         return false;
         }
      
      Collection<ExpandDoor> room_doors = p_obstacle_room.get_doors();
      // The side of the trace line seen from the doors to expand.
      // Used to determine, if a door is on the right side to put it into the p_door_list.
      PlaSegmentInt shove_line_segment;
      
      if (from_door.dimension.is_area())
         {
         // shove from a link door into the direction of the other link door.
         ExpandRoomComplete other_room = from_door.other_room(p_obstacle_room);

         if (!(other_room instanceof ExpandRoomObstacle)) return false;
         
         if (!end_points_matching(obstacle_trace, ((ExpandRoomObstacle) other_room).get_item())) return false;

         PlaPointFloat door_center = from_door_shape.centre_of_gravity();
         PlaPointFloat corner_1 = trace_polyline.corner_approx(trace_corner_no);
         PlaPointFloat corner_2 = trace_polyline.corner_approx(trace_corner_no + 1);

         if (corner_1.length_square(corner_2) < 1)
            {
            // shove_line_segment may be reduced to a point
            return false;
            }
         
         boolean shove_into_direction_of_trace_start = door_center.length_square(corner_2) < door_center.length_square(corner_1);
         
         shove_line_segment = trace_polyline.segment_get(trace_corner_no + 1);

         if (shove_into_direction_of_trace_start)
            {
            // shove from the end point to the start point of the line segment
            shove_line_segment = shove_line_segment.opposite();
            }
         }
      else
         {
         ExpandRoomComplete from_room = from_door.other_room(p_obstacle_room);
         PlaPointFloat from_point = from_room.get_shape().centre_of_gravity();
         PlaLineInt shove_trace_line = trace_polyline.plaline(trace_corner_no + 1);
         PlaSegmentFloat door_line_segment = from_door_shape.diagonal_corner_segment();
         
         PlaSide side_of_trace_line = shove_trace_line.side_of(door_line_segment.point_a, 0);

         PlaSegmentFloat polar_line_segment = from_door_shape.polar_line_segment(from_point);

         boolean door_line_swapped = polar_line_segment.point_b.length_square(door_line_segment.point_a) < polar_line_segment.point_a.length_square(door_line_segment.point_a);

         boolean section_ok;
         // shove only from the right most section to the right or from the left most section to the left.

         double shape_entry_check_distance = compensated_trace_half_width + 5;
         double check_dist_square = shape_entry_check_distance * shape_entry_check_distance;

         if (p_shove_to_the_left && !door_line_swapped || !p_shove_to_the_left && door_line_swapped)
            {
            section_ok = p_list_element.section_no_of_door == p_list_element.door.maze_search_element_count() - 1
                  && (p_list_element.shape_entry.point_a.length_square(door_line_segment.point_b) <= check_dist_square || p_list_element.shape_entry.point_b.length_square(door_line_segment.point_b) <= check_dist_square);
            }
         else
            {
            section_ok = p_list_element.section_no_of_door == 0
                  && (p_list_element.shape_entry.point_a.length_square(door_line_segment.point_a) <= check_dist_square || p_list_element.shape_entry.point_b.length_square(door_line_segment.point_a) <= check_dist_square);
            }
         
         if (!section_ok)
            {
            return false;
            }

         // create the line segment for shoving

         PlaSegmentFloat shrinked_line_segment = polar_line_segment.shrink_segment(compensated_trace_half_width);
         PlaDirection perpendicular_direction = shove_trace_line.direction().turn_45_degree(2);
         if (side_of_trace_line == PlaSide.ON_THE_LEFT)
            {
            if (p_shove_to_the_left)
               {
               PlaLineInt start_closing_line = new PlaLineInt(shrinked_line_segment.point_b.round(), perpendicular_direction);
               shove_line_segment = new PlaSegmentInt(start_closing_line, trace_polyline.plaline(trace_corner_no + 1), trace_polyline.plaline(trace_corner_no + 2));
               }
            else
               {
               PlaLineInt start_closing_line = new PlaLineInt(shrinked_line_segment.point_a.round(), perpendicular_direction);
               shove_line_segment = new PlaSegmentInt(start_closing_line, trace_polyline.plaline(trace_corner_no + 1).opposite(), trace_polyline.plaline(trace_corner_no).opposite());
               }
            }
         else
            {
            if (p_shove_to_the_left)
               {
               PlaLineInt start_closing_line = new PlaLineInt(shrinked_line_segment.point_b.round(), perpendicular_direction);
               shove_line_segment = new PlaSegmentInt(start_closing_line, trace_polyline.plaline(trace_corner_no + 1).opposite(), trace_polyline.plaline(trace_corner_no).opposite());
               }
            else
               {
               PlaLineInt start_closing_line = new PlaLineInt(shrinked_line_segment.point_a.round(), perpendicular_direction);
               shove_line_segment = new PlaSegmentInt(start_closing_line, trace_polyline.plaline(trace_corner_no + 1), trace_polyline.plaline(trace_corner_no + 2));
               }
            }
         }
      
      int trace_half_width = art_ctrl.trace_half_width[trace_layer];

      NetNosList net_no_arr = new NetNosList(art_ctrl.net_no);

      double shove_width = r_board.check_trace_segment(shove_line_segment, trace_layer, net_no_arr, trace_half_width, art_ctrl.trace_clearance_idx, true);
      boolean segment_shortened = false;
      
      if (shove_width < Integer.MAX_VALUE)
         {
         // shorten shove_line_segment
         shove_width = shove_width - 1;
      
         if (shove_width <= 0) return true;
         
         shove_line_segment = shove_line_segment.change_length_approx(shove_width);
         segment_shortened = true;
         }

      PlaPointFloat from_corner = shove_line_segment.start_point_approx();
      PlaPointFloat to_corner = shove_line_segment.end_point_approx();
      boolean segment_ist_point = from_corner.length_square(to_corner) < 0.1;

      if ( ! segment_ist_point)
         {
         shove_width = r_board.shove_trace_algo.shove_trace_check(
               shove_line_segment, 
               p_shove_to_the_left, 
               trace_layer, 
               net_no_arr, 
               trace_half_width, 
               art_ctrl.trace_clearance_idx,
               art_ctrl.max_shove_trace_recursion_depth, 
               art_ctrl.max_shove_via_recursion_depth);

         if (shove_width <= 0) return true;
         }

      // Put the doors on this side of the room into p_to_door_list with
      if (segment_shortened)
         {
         shove_width = Math.min(shove_width, from_corner.distance(to_corner));
         }

      PlaLineInt shove_line = shove_line_segment.get_line();

      // From_door_compare_distance is used to check, that a door is between from_door and the end point of the shove line.
      double from_door_compare_distance;
      if (from_door.dimension.is_area() || segment_ist_point)
         {
         from_door_compare_distance = Double.MAX_VALUE;
         }
      else
         {
         from_door_compare_distance = to_corner.length_square(from_door_shape.corner_approx(0));
         }

      for (ExpandDoor curr_door : room_doors)
         {
         if (curr_door == from_door) continue;

         if (curr_door.first_room instanceof ExpandRoomObstacle && curr_door.second_room instanceof ExpandRoomObstacle)
            {
            BrdItem first_room_item = ((ExpandRoomObstacle) curr_door.first_room).get_item();
            BrdItem second_room_item = ((ExpandRoomObstacle) curr_door.second_room).get_item();
           
            if (first_room_item != second_room_item)
               {
               // there may be topological problems at a trace fork
               continue;
               }
            }
         
         ShapeTile curr_door_shape = curr_door.get_shape();
         if (curr_door.dimension.is_area() && shove_width >= Integer.MAX_VALUE)
            {
            boolean add_link_door = curr_door_shape.contains(to_corner);

            if (add_link_door)
               {
               PlaSegmentFloat[] line_sections = curr_door.get_section_segments(compensated_trace_half_width);
               p_to_door_list.add(new MazeDoorSection(curr_door, 0, line_sections[0]));
               }
            continue;
            }
         else if (!segment_ist_point)
            {
            // now curr_door is 1-dimensional

            // check, that curr_door is on the same border_line as p_from_door.
            PlaSegmentFloat curr_door_segment = curr_door_shape.diagonal_corner_segment();
            if (curr_door_segment == null)
               {
               if (r_board.debug(Mdbg.MAZE, Ldbg.SPC_C))
                  System.out.println(classname+"check_shove_trace_line: door shape is empty");

               continue;
               }
            PlaSide start_corner_side_of_trace_line = shove_line.side_of(curr_door_segment.point_a, 0);
            PlaSide end_corner_side_of_trace_line = shove_line.side_of(curr_door_segment.point_b, 0);
            if (p_shove_to_the_left)
               {
               if (start_corner_side_of_trace_line != PlaSide.ON_THE_LEFT || end_corner_side_of_trace_line != PlaSide.ON_THE_LEFT)
                  {
                  continue;
                  }
               }
            else
               {
               if (start_corner_side_of_trace_line != PlaSide.ON_THE_RIGHT || end_corner_side_of_trace_line != PlaSide.ON_THE_RIGHT)
                  {
                  continue;
                  }
               }
            PlaSegmentFloat curr_door_line = curr_door_shape.polar_line_segment(from_corner);
            PlaPointFloat curr_door_nearest_corner;
            if (curr_door_line.point_a.length_square(from_corner) <= curr_door_line.point_b.length_square(from_corner))
               {
               curr_door_nearest_corner = curr_door_line.point_a;
               }
            else
               {
               curr_door_nearest_corner = curr_door_line.point_b;
               }
            if (to_corner.length_square(curr_door_nearest_corner) >= from_door_compare_distance)
               {
               // curr_door is not located into the direction of to_corner.
               continue;
               }
            PlaPointFloat curr_door_projection = curr_door_nearest_corner.projection_approx(shove_line);

            if (curr_door_projection.distance(from_corner) + compensated_trace_half_width <= shove_width)
               {
               PlaSegmentFloat[] line_sections = curr_door.get_section_segments(compensated_trace_half_width);
               for (int i = 0; i < line_sections.length; ++i)
                  {
                  PlaSegmentFloat curr_line_section = line_sections[i];
                  PlaPointFloat curr_section_nearest_corner;
                  if (curr_line_section.point_a.length_square(from_corner) <= curr_line_section.point_b.length_square(from_corner))
                     {
                     curr_section_nearest_corner = curr_line_section.point_a;
                     }
                  else
                     {
                     curr_section_nearest_corner = curr_line_section.point_b;
                     }
                  PlaPointFloat curr_section_projection = curr_section_nearest_corner.projection_approx(shove_line);
                  if (curr_section_projection.distance(from_corner) <= shove_width)
                     {
                     p_to_door_list.add(new MazeDoorSection(curr_door, i, curr_line_section));
                     }
                  }
               }
            }
         }
      
      return true;
      }

   /**
    * Check if the endpoints of p_trace and p_from_item are matching, so that the shove can continue through a link door.
    */
   private boolean end_points_matching(BrdTracep p_trace, BrdItem p_from_item)
      {
      if (p_from_item == p_trace) return true;

      if ( ! p_trace.shares_net(p_from_item)) return false;
      
      if (p_from_item instanceof BrdAbit)
         {
         PlaPointInt from_center = ((BrdAbit) p_from_item).center_get();
         
         if ( from_center.equals(p_trace.corner_first()) ) return true;
         
         if ( from_center.equals(p_trace.corner_last()) ) return true;
         }
      else if (p_from_item instanceof BrdTracep)
         {
         BrdTracep from_trace = (BrdTracep) p_from_item;
         
         if ( p_trace.corner_first().equals(from_trace.corner_first()) ) return true;
         
         if (  p_trace.corner_first().equals(from_trace.corner_last()) ) return true;
         
         if ( p_trace.corner_last().equals(from_trace.corner_first()) ) return true;
         
         if ( p_trace.corner_last().equals(from_trace.corner_last()) ) return true;
         }

      return false;
      }

   }
