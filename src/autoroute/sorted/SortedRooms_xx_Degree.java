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
 * SortedRoomNeighbours.java
 *
 * Created on 28. Mai 2007, 07:27
 *
 */

package autoroute.sorted;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import autoroute.ArtEngine;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandRoom;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomFreespace;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeEntry;
import freert.planar.PlaDimension;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointInt;
import freert.planar.PlaSide;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileSimplex;

/**
 * To calculate the neighbors rooms of an expansion room. 
 * The neighbors rooms will be sorted in counterclock sense around the border of the shape of p_room. 
 * Overlapping neighbors containing an item may be stored in an unordered list.
 *
 * @author Alfons Wirtz
 */
public final class SortedRooms_xx_Degree
   {
   private final ExpandRoom from_room;
   final ExpandRoomComplete completed_room;
   private final ShapeTile room_shape;
   final SortedSet<SortedRoom_xx_Degree> sorted_neighbours;
   final Collection<ShapeTreeEntry> own_net_objects;

   SortedRooms_xx_Degree(ExpandRoom p_from_room, ExpandRoomComplete p_completed_room)
      {
      from_room = p_from_room;
      completed_room = p_completed_room;
      room_shape = p_completed_room.get_shape();
      sorted_neighbours = new TreeSet<SortedRoom_xx_Degree>();
      own_net_objects = new LinkedList<ShapeTreeEntry>();
      }


   void add_sorted_neighbour(
         ShapeTile p_neighbour_shape, 
         int p_touching_side_no_of_room, 
         int p_touching_side_no_of_neighbour_room, 
         boolean p_room_touch_is_corner,
         boolean p_neighbour_room_touch_is_corner)
      {
      sorted_neighbours.add( new SortedRoom_xx_Degree(
            room_shape, 
            p_neighbour_shape, 
            p_touching_side_no_of_room, 
            p_touching_side_no_of_neighbour_room, 
            p_room_touch_is_corner,
            p_neighbour_room_touch_is_corner));
      }

   /**
    * Check, that each side of the romm shape has at least one touching neighbour. 
    * Otherwise the room shape will be improved the by enlarging. 
    * @return true, if the room shape was changed.
    */
   boolean try_remove_edge(int p_net_no, ShapeSearchTree p_autoroute_search_tree )
      {
      if (!(from_room instanceof ExpandRoomFreespaceIncomplete)) return false;

      ExpandRoomFreespaceIncomplete curr_incomplete_room = (ExpandRoomFreespaceIncomplete) from_room;
      int remove_edge_no = -1;
      ShapeTileSimplex room_simplex = curr_incomplete_room.get_shape().to_Simplex();
      double room_shape_area = room_simplex.area();

      int prev_edge_no = -1;
      int curr_edge_no = 0;

      for ( SortedRoom_xx_Degree next_neighbour : sorted_neighbours )
         {
         if (next_neighbour.touching_side_no_of_room == prev_edge_no) continue;

         if (next_neighbour.touching_side_no_of_room == curr_edge_no)
            {
            prev_edge_no = curr_edge_no;
            ++curr_edge_no;
            }
         else
            {
            // On the edge side with index curr_edge_no is no touching neighbour.
            remove_edge_no = curr_edge_no;
            break;
            }
         }

      if (remove_edge_no < 0 && curr_edge_no < room_simplex.border_line_count())
         {
         // missing touching neighbour at the last edge side.
         remove_edge_no = curr_edge_no;
         }

      if (remove_edge_no >= 0)
         {
         // Touching neighbour missing at the edge side with index remove_edge_no Remove the edge line and restart the algorithm.
         ShapeTileSimplex enlarged_shape = room_simplex.remove_border_line(remove_edge_no);
         ExpandRoomFreespaceIncomplete enlarged_room = new ExpandRoomFreespaceIncomplete(enlarged_shape, curr_incomplete_room.get_layer(), curr_incomplete_room.get_contained_shape());
         Collection<ExpandRoomFreespaceIncomplete> new_rooms = p_autoroute_search_tree.complete_shape(enlarged_room, p_net_no, null, null);
         
         if (new_rooms.size() != 1)
            {
            System.out.println("AutorouteEngine.calculate_doors: 1 completed shape expected");
            return false;
            }
         
         boolean remove_edge = false;
         if (new_rooms.size() == 1)
            {
            // Check, that the area increases to prevent endless loop.
            ExpandRoomFreespaceIncomplete new_shape = new_rooms.iterator().next();
            if (new_shape.get_shape().area() > room_shape_area)
               {
               remove_edge = true;
               }
            }
         
         if (remove_edge)
            {
            Iterator<ExpandRoomFreespaceIncomplete> it2 = new_rooms.iterator();
            ExpandRoomFreespaceIncomplete new_room = it2.next();
            curr_incomplete_room.set_shape(new_room.get_shape());
            curr_incomplete_room.set_contained_shape(new_room.get_contained_shape());
            return true;
            }
         }
      return false;
      }

   /**
    * Called from calculate_doors(). The shape of the room p_result may change inside this function.
    * Wow.... pippo interesting....
    */
   public void calculate_new_incomplete_rooms(ArtEngine p_autoroute_engine)
      {
      SortedRoom_xx_Degree prev_neighbour = sorted_neighbours.last();
      Iterator<SortedRoom_xx_Degree> it = sorted_neighbours.iterator();
      ShapeTileSimplex room_simplex = from_room.get_shape().to_Simplex();

      while (it.hasNext())
         {
         SortedRoom_xx_Degree next_neighbour = it.next();
         int first_touching_side_no = prev_neighbour.touching_side_no_of_room;
         int last_touching_side_no = next_neighbour.touching_side_no_of_room;

         int curr_next_no = room_simplex.next_no(first_touching_side_no);
         boolean intersection_with_prev_neighbour_ends_at_corner = (first_touching_side_no != last_touching_side_no || prev_neighbour == sorted_neighbours.last())
               && prev_neighbour.last_corner().equals(room_simplex.corner(curr_next_no));
         boolean intersection_with_next_neighbour_starts_at_corner = (first_touching_side_no != last_touching_side_no || prev_neighbour == sorted_neighbours.last())
               && next_neighbour.first_corner().equals(room_simplex.corner(last_touching_side_no));

         if (intersection_with_prev_neighbour_ends_at_corner)
            {
            first_touching_side_no = curr_next_no;
            }

         if (intersection_with_next_neighbour_starts_at_corner)
            {
            last_touching_side_no = room_simplex.prev_no(last_touching_side_no);
            }
         boolean neighbours_touch = false;

         if (sorted_neighbours.size() > 1)
            {
            neighbours_touch = prev_neighbour.last_corner().equals(next_neighbour.first_corner());
            }

         if (!neighbours_touch)
            {
            // create a door to a new incomplete expansion room between
            // the last corner of the previous neighbour and the first corner of the
            // current neighbour.
            int last_bounding_line_no = prev_neighbour.touching_side_no_of_neighbour_room;
            if (!(intersection_with_prev_neighbour_ends_at_corner || prev_neighbour.room_touch_is_corner))
               {
               last_bounding_line_no = prev_neighbour.neighbour_shape.prev_no(last_bounding_line_no);
               }

            int first_bounding_line_no = next_neighbour.touching_side_no_of_neighbour_room;
            if (!(intersection_with_next_neighbour_starts_at_corner || next_neighbour.neighbour_room_touch_is_corner))
               {
               first_bounding_line_no = next_neighbour.neighbour_shape.next_no(first_bounding_line_no);
               }
            PlaLineInt start_edge_line = next_neighbour.neighbour_shape.border_line(first_bounding_line_no).opposite();
            // start_edge_line is only used for the first new incomplete room.
            PlaLineInt middle_edge_line = null;
            int curr_touching_side_no = last_touching_side_no;
            boolean first_time = true;
          
            // The loop goes backwards fromm the edge line of next_neigbour to the edge line of prev_neigbour.
            for (;;)
               {
               boolean corner_cut_off = false;
               if (from_room instanceof ExpandRoomFreespaceIncomplete)
                  {
                  ExpandRoomFreespaceIncomplete incomplete_room = (ExpandRoomFreespaceIncomplete) from_room;
                  if (curr_touching_side_no == last_touching_side_no && first_touching_side_no != last_touching_side_no)
                     {
                     // Create a new line approximately from the last corner of the previous
                     // neighbour to the first corner of the next neighbour to cut off
                     // the outstanding corners of the room shape in the empty space.
                     // That is only tried in the first pass of the loop.
                     PlaPointInt cut_line_start = prev_neighbour.last_corner().to_float().round();
                     PlaPointInt cut_line_end = next_neighbour.first_corner().to_float().round();
                     PlaLineInt cut_line = new PlaLineInt(cut_line_start, cut_line_end);
                     ShapeTile cut_half_plane = ShapeTile.get_instance(cut_line);
                     ((ExpandRoomFreespaceComplete) completed_room).set_shape(completed_room.get_shape().intersection(cut_half_plane));
                     corner_cut_off = true;
                     if (incomplete_room.get_contained_shape().side_of(cut_line) != PlaSide.ON_THE_LEFT)
                        {
                        // Otherwise p_room.contained_shape would no longer be contained
                        // in the shape after cutting of the corner.
                        corner_cut_off = false;
                        }
                     if (corner_cut_off)
                        {
                        middle_edge_line = cut_line.opposite();
                        }
                     }
                  }
               int next_touching_side_no = room_simplex.prev_no(curr_touching_side_no);

               if (!corner_cut_off)
                  {
                  middle_edge_line = room_simplex.border_line(curr_touching_side_no).opposite();
                  }

               PlaDirection middle_line_dir = middle_edge_line.direction();

               boolean last_time = curr_touching_side_no == first_touching_side_no && !(prev_neighbour == sorted_neighbours.last() && first_time)
               // The expression above handles the case, when all neigbours are on 1 edge line.
                     || corner_cut_off;

               PlaLineInt end_edge_line;
               // end_edge_line is only used for the last new incomplete room.
               if (last_time)
                  {
                  end_edge_line = prev_neighbour.neighbour_shape.border_line(last_bounding_line_no).opposite();
                  if (end_edge_line.direction().side_of(middle_line_dir) != PlaSide.ON_THE_LEFT)
                     {
                     // Concave corner between the middle and the last line.
                     // May be there is a 1 point touch.
                     end_edge_line = null;
                     }
                  }
               else
                  {
                  end_edge_line = null;
                  }

               if (start_edge_line != null && middle_line_dir.side_of(start_edge_line.direction()) != PlaSide.ON_THE_LEFT)
                  {
                  // concave corner between the first and the middle line
                  // May be there is a 1 point touch.
                  start_edge_line = null;
                  }
               int new_edge_line_count = 1;
               if (start_edge_line != null)
                  {
                  ++new_edge_line_count;
                  }
               if (end_edge_line != null)
                  {
                  ++new_edge_line_count;
                  }
               PlaLineInt[] new_edge_lines = new PlaLineInt[new_edge_line_count];
               int curr_index = 0;
               if (start_edge_line != null)
                  {
                  new_edge_lines[curr_index] = start_edge_line;
                  ++curr_index;
                  }
               new_edge_lines[curr_index] = middle_edge_line;
               if (end_edge_line != null)
                  {
                  ++curr_index;
                  new_edge_lines[curr_index] = end_edge_line;
                  }
               ShapeTileSimplex new_room_shape = ShapeTileSimplex.get_instance(new_edge_lines);
               if (!new_room_shape.is_empty())
                  {

                  ShapeTile new_contained_shape = completed_room.get_shape().intersection(new_room_shape);
                  if (!new_contained_shape.is_empty())
                     {
                     ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_room_shape, from_room.get_layer(), new_contained_shape);
                     ExpandDoor new_door = new ExpandDoor(completed_room, new_room, PlaDimension.LINE);
                     completed_room.add_door(new_door);
                     new_room.add_door(new_door);
                     }
                  }
               if (last_time)
                  {
                  break;
                  }
               curr_touching_side_no = next_touching_side_no;
               start_edge_line = null;
               first_time = false;
               }
            }
         prev_neighbour = next_neighbour;
         }
      }

   }
