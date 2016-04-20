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
 * Sorted45DegreeRoomNeighbours.java
 *
 * Created on 6. Juli 2007, 07:28
 *
 */

package autoroute.sorted;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import planar.PlaDimension;
import planar.PlaLimits;
import planar.PlaPointFloat;
import planar.PlaPointInt;
import planar.ShapeTile;
import planar.ShapeTileOctagon;
import autoroute.ArtEngine;
import autoroute.ArtItem;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandRoom;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomFreespace;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import autoroute.expand.ExpandRoomObstacle;
import board.items.BrdItem;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeEntry;
import board.shape.ShapeTreeObject;

/**
 *
 * @author Alfons Wirtz
 */
public final class SortedRooms_45_Degree
   {
   private final ExpandRoomComplete completed_room;
   private final SortedSet<SortedRoom_45_Degree> sorted_neighbours;
   private final ExpandRoom from_room;
   private final ShapeTileOctagon room_shape;
   private final boolean[] edge_interiour_touches_obstacle = new boolean[8];

   private SortedRooms_45_Degree(ExpandRoom p_from_room, ExpandRoomComplete p_completed_room)
      {
      from_room = p_from_room;
      completed_room = p_completed_room;
      room_shape = p_completed_room.get_shape().bounding_octagon();
      sorted_neighbours = new TreeSet<SortedRoom_45_Degree>();

      for (int index = 0; index < edge_interiour_touches_obstacle.length; ++index)
         {
         edge_interiour_touches_obstacle[index] = false;
         }
      }
   
   public static ExpandRoomComplete calculate(ExpandRoom p_room, ArtEngine p_engine)
      {
      int net_no = p_engine.get_net_no();
      SortedRooms_45_Degree room_neighbours = calculate_neighbours(p_room, net_no, p_engine.autoroute_search_tree, p_engine.new_room_id_no());

      if (room_neighbours == null) return null;

      // Check, that each side of the romm shape has at least one touching neighbour.
      // Otherwise improve the room shape by enlarging.
      boolean edge_removed = room_neighbours.try_remove_edge_line(net_no, p_engine.autoroute_search_tree);

      ExpandRoomComplete result = room_neighbours.completed_room;
      
      if (edge_removed)
         {
         p_engine.remove_all_doors(result);
         return calculate(p_room, p_engine);
         }

      // Now calculate the new incomplete rooms together with the doors
      // between this room and the sorted neighbours.

      if (room_neighbours.sorted_neighbours.isEmpty())
         {
         if (result instanceof ExpandRoomObstacle)
            {
            room_neighbours.calculate_edge_incomplete_rooms_of_obstacle_expansion_room(0, 7, p_engine);
            }
         }
      else
         {
         room_neighbours.calculate_new_incomplete_rooms(p_engine);
         }
      return result;
      }

   /**
    * Calculates all touching neighbours of p_room and sorts them in counterclock sense around the boundary of the room shape.
    */
   private static SortedRooms_45_Degree calculate_neighbours(ExpandRoom p_room, int p_net_no, ShapeSearchTree p_autoroute_search_tree, int p_room_id_no)
      {
      ShapeTile room_shape = p_room.get_shape();
      ExpandRoomComplete completed_room;
      if (p_room instanceof ExpandRoomFreespaceIncomplete)
         {
         completed_room = new ExpandRoomFreespaceComplete(room_shape, p_room.get_layer(), p_room_id_no);
         }
      else if (p_room instanceof ExpandRoomObstacle)
         {
         completed_room = (ExpandRoomObstacle) p_room;
         }
      else
         {
         System.out.println("Sorted45DegreeRoomNeighbours.calculate_neighbours: unexpected expansion room type");
         return null;
         }
      
      
      ShapeTileOctagon room_oct = room_shape.bounding_octagon();
      SortedRooms_45_Degree result = new SortedRooms_45_Degree(p_room, completed_room);
      Collection<ShapeTreeEntry> overlapping_objects = new LinkedList<ShapeTreeEntry>();
      p_autoroute_search_tree.calc_overlapping_tree_entries(room_shape, p_room.get_layer(), overlapping_objects);
      // Calculate the touching neigbour objects and sort them in counterclock sence
      // around the border of the room shape.
      for (ShapeTreeEntry curr_entry : overlapping_objects)
         {
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_entry.object;
         if (curr_object == p_room)
            {
            continue;
            }
         if ((completed_room instanceof ExpandRoomFreespaceComplete) && !curr_object.is_trace_obstacle(p_net_no))
            {
            ((ExpandRoomFreespaceComplete) completed_room).calculate_target_doors(curr_entry, p_net_no, p_autoroute_search_tree);
            continue;
            }
         
         
         ShapeTile curr_shape = curr_object.get_tree_shape(p_autoroute_search_tree, curr_entry.shape_index_in_object);
         ShapeTileOctagon curr_oct = curr_shape.bounding_octagon();
         ShapeTileOctagon intersection = room_oct.intersection(curr_oct);

         PlaDimension dimension = intersection.dimension();
         
         if (dimension.is_area() && completed_room instanceof ExpandRoomObstacle)
            {
            if (curr_object instanceof BrdItem)
               {
               // only Obstacle expansion roos may have a 2-dim overlap
               BrdItem curr_item = (BrdItem) curr_object;
               if (curr_item.is_route())
                  {
                  ArtItem item_info = curr_item.art_item_get();
                  ExpandRoomObstacle curr_overlap_room = item_info.get_expansion_room(curr_entry.shape_index_in_object, p_autoroute_search_tree);
                  ((ExpandRoomObstacle) completed_room).create_overlap_door(curr_overlap_room);
                  }
               }
            continue;
            }
         
         if (dimension.is_empty())
            {
            // may happen at a corner from 2 diagonal lines with non integer coordinates (--.5, ---.5).
            continue;
            }
         
         result.add_sorted_neighbour(intersection);
         if (dimension.is_line() )
            {
            // make shure, that there is a door to the neighbour room.
            ExpandRoom neighbour_room = null;
            if (curr_object instanceof ExpandRoom)
               {
               neighbour_room = (ExpandRoom) curr_object;
               }
            else if (curr_object instanceof BrdItem)
               {
               BrdItem curr_item = (BrdItem) curr_object;
               if (curr_item.is_route())
                  {
                  // expand the item for ripup and pushing purposes
                  ArtItem item_info = curr_item.art_item_get();
                  neighbour_room = item_info.get_expansion_room(curr_entry.shape_index_in_object, p_autoroute_search_tree);
                  }
               }
            if (neighbour_room != null)
               {
               if (SortedRooms_xx_Degree.insert_door_ok(completed_room, neighbour_room, intersection))
                  {
                  ExpandDoor new_door = new ExpandDoor(completed_room, neighbour_room);
                  neighbour_room.add_door(new_door);
                  completed_room.add_door(new_door);
                  }
               }
            }
         }
      return result;
      }


   private void add_sorted_neighbour( ShapeTileOctagon p_intersection)
      {
      SortedRoom_45_Degree new_neighbour = new SortedRoom_45_Degree(room_shape, edge_interiour_touches_obstacle, p_intersection);

      if (new_neighbour.last_touching_side >= 0)
         {
         sorted_neighbours.add(new_neighbour);
         }
      }

   /**
    * Calculates an incomplete room for each edge side from p_from_side_no to p_to_side_no.
    */
   private void calculate_edge_incomplete_rooms_of_obstacle_expansion_room(int p_from_side_no, int p_to_side_no, ArtEngine p_autoroute_engine)
      {
      if (!(this.from_room instanceof ExpandRoomObstacle))
         {
         System.out.println("Sorted45DegreeRoomNeighbours.calculate_side_incomplete_rooms_of_obstacle_expansion_room: ObstacleExpansionRoom expected for this.from_room");
         return;
         }
      ShapeTileOctagon board_bounding_oct = p_autoroute_engine.r_board.get_bounding_box().bounding_octagon();
      PlaPointInt curr_corner = this.room_shape.corner(p_from_side_no);
      int curr_side_no = p_from_side_no;
      for (;;)
         {
         int next_side_no = (curr_side_no + 1) % 8;
         PlaPointInt next_corner = this.room_shape.corner(next_side_no);
         if (!curr_corner.equals(next_corner))
            {
            int lx = board_bounding_oct.oct_lx;
            int ly = board_bounding_oct.oct_ly;
            int rx = board_bounding_oct.oct_rx;
            int uy = board_bounding_oct.oct_uy;
            int ulx = board_bounding_oct.oct_ulx;
            int lrx = board_bounding_oct.oct_lrx;
            int llx = board_bounding_oct.oct_llx;
            int urx = board_bounding_oct.oct_urx;
            if (curr_side_no == 0)
               {
               uy = this.room_shape.oct_ly;
               }
            else if (curr_side_no == 1)
               {
               ulx = this.room_shape.oct_lrx;
               }
            else if (curr_side_no == 2)
               {
               lx = this.room_shape.oct_rx;
               }
            else if (curr_side_no == 3)
               {
               llx = this.room_shape.oct_urx;
               }
            else if (curr_side_no == 4)
               {
               ly = this.room_shape.oct_uy;
               }
            else if (curr_side_no == 5)
               {
               lrx = this.room_shape.oct_ulx;
               }
            else if (curr_side_no == 6)
               {
               rx = this.room_shape.oct_lx;
               }
            else if (curr_side_no == 7)
               {
               urx = this.room_shape.oct_llx;
               }
            else
               {
               System.out.println("SortedOrthoganelRoomNeighbours.calculate_edge_incomplete_rooms_of_obstacle_expansion_room: curr_side_no illegal");
               return;
               }
            insert_incomplete_room(p_autoroute_engine, lx, ly, rx, uy, ulx, lrx, llx, urx);
            }
         if (curr_side_no == p_to_side_no)
            {
            break;
            }
         curr_side_no = next_side_no;
         }
      }

   private static ShapeTileOctagon remove_not_touching_border_lines(ShapeTileOctagon p_room_oct, boolean[] p_edge_interiour_touches_obstacle)
      {
      int lx;
      if (p_edge_interiour_touches_obstacle[6])
         {
         lx = p_room_oct.oct_lx;
         }
      else
         {
         lx = -PlaLimits.CRIT_INT;
         }

      int ly;
      if (p_edge_interiour_touches_obstacle[0])
         {
         ly = p_room_oct.oct_ly;
         }
      else
         {
         ly = -PlaLimits.CRIT_INT;
         }

      int rx;
      if (p_edge_interiour_touches_obstacle[2])
         {
         rx = p_room_oct.oct_rx;
         }
      else
         {
         rx = PlaLimits.CRIT_INT;
         }

      int uy;
      if (p_edge_interiour_touches_obstacle[4])
         {
         uy = p_room_oct.oct_uy;
         }
      else
         {
         uy = PlaLimits.CRIT_INT;
         }

      int ulx;
      if (p_edge_interiour_touches_obstacle[5])
         {
         ulx = p_room_oct.oct_ulx;
         }
      else
         {
         ulx = -PlaLimits.CRIT_INT;
         }

      int lrx;
      if (p_edge_interiour_touches_obstacle[1])
         {
         lrx = p_room_oct.oct_lrx;
         }
      else
         {
         lrx = PlaLimits.CRIT_INT;
         }

      int llx;
      if (p_edge_interiour_touches_obstacle[7])
         {
         llx = p_room_oct.oct_llx;
         }
      else
         {
         llx = -PlaLimits.CRIT_INT;
         }

      int urx;
      if (p_edge_interiour_touches_obstacle[3])
         {
         urx = p_room_oct.oct_urx;
         }
      else
         {
         urx = PlaLimits.CRIT_INT;
         }

      ShapeTileOctagon result = new ShapeTileOctagon(lx, ly, rx, uy, ulx, lrx, llx, urx);
      return result.normalize();
      }

   /**
    * Check, that each side of the romm shape has at least one touching neighbour. Otherwise the room shape will be improved the by
    * enlarging. Returns true, if the room shape was changed.
    */
   private boolean try_remove_edge_line(int p_net_no, ShapeSearchTree p_autoroute_search_tree)
      {
      if (!(this.from_room instanceof ExpandRoomFreespaceIncomplete))
         {
         return false;
         }
      ExpandRoomFreespaceIncomplete curr_incomplete_room = (ExpandRoomFreespaceIncomplete) this.from_room;
      if (!(curr_incomplete_room.get_shape() instanceof ShapeTileOctagon))
         {
         System.out.println("Sorted45DegreeRoomNeighbours.try_remove_edge_line: IntOctagon expected for room_shape type");
         return false;
         }
      ShapeTileOctagon room_oct = (ShapeTileOctagon) curr_incomplete_room.get_shape();
      double room_area = room_oct.area();

      boolean try_remove_edge_lines = false;
      for (int i = 0; i < 8; ++i)
         {
         if (!this.edge_interiour_touches_obstacle[i])
            {
            PlaPointFloat prev_corner = this.room_shape.corner_approx(i);
            PlaPointFloat next_corner = this.room_shape.corner_approx(this.room_shape.next_no(i));
            if (prev_corner.distance_square(next_corner) > 1)
               {
               try_remove_edge_lines = true;
               break;
               }
            }
         }

      if (try_remove_edge_lines)
         {
         // Touching neighbour missing at the edge side with index remove_edge_no
         // Remove the edge line and restart the algorithm.

         ShapeTileOctagon enlarged_oct = remove_not_touching_border_lines(room_oct, this.edge_interiour_touches_obstacle);

         Collection<ExpandDoor> door_list = this.completed_room.get_doors();
         ShapeTile ignore_shape = null;
         ShapeTreeObject ignore_object = null;
         double max_door_area = 0;
         for (ExpandDoor curr_door : door_list)
            {
            // insert the overlapping doors with CompleteFreeSpaceExpansionRooms for the information in complete_shape about the objects to ignore.
            if ( ! curr_door.dimension.is_area()) continue;

            ExpandRoomComplete other_room = curr_door.other_room(this.completed_room);
               {
               if (other_room instanceof ExpandRoomFreespaceComplete)
                  {
                  ShapeTile curr_door_shape = curr_door.get_shape();
                  double curr_door_area = curr_door_shape.area();
                  if (curr_door_area > max_door_area)
                     {
                     max_door_area = curr_door_area;
                     ignore_shape = curr_door_shape;
                     ignore_object = (ExpandRoomFreespaceComplete) other_room;
                     }
                  }
               }
            }
         ExpandRoomFreespaceIncomplete enlarged_room = new ExpandRoomFreespaceIncomplete(enlarged_oct, curr_incomplete_room.get_layer(), curr_incomplete_room.get_contained_shape());
         Collection<ExpandRoomFreespaceIncomplete> new_rooms = p_autoroute_search_tree.complete_shape(enlarged_room, p_net_no, ignore_object, ignore_shape);
         if (new_rooms.size() == 1)
            {
            // Check, that the area increases to prevent endless loop.
            ExpandRoomFreespaceIncomplete new_room = new_rooms.iterator().next();
            if (new_room.get_shape().area() > room_area)
               {
               curr_incomplete_room.set_shape(new_room.get_shape());
               curr_incomplete_room.set_contained_shape(new_room.get_contained_shape());
               return true;
               }
            }
         }
      return false;
      }

   /**
    * Inserts a new incomplete room with an octagon shape.
    */
   private void insert_incomplete_room(ArtEngine p_autoroute_engine, int p_lx, int p_ly, int p_rx, int p_uy, int p_ulx, int p_lrx, int p_llx, int p_urx)
      {
      ShapeTileOctagon new_incomplete_room_shape = new ShapeTileOctagon(p_lx, p_ly, p_rx, p_uy, p_ulx, p_lrx, p_llx, p_urx);
      new_incomplete_room_shape = new_incomplete_room_shape.normalize();
      if ( new_incomplete_room_shape.dimension().is_area() )
         {
         ShapeTileOctagon new_contained_shape = this.room_shape.intersection(new_incomplete_room_shape);
         if (!new_contained_shape.is_empty())
            {
            PlaDimension door_dimension = new_contained_shape.dimension();
            if ( ! door_dimension.is_empty() )
               {
               ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_incomplete_room_shape, this.from_room.get_layer(), new_contained_shape);
               ExpandDoor new_door = new ExpandDoor(this.completed_room, new_room, door_dimension);
               this.completed_room.add_door(new_door);
               new_room.add_door(new_door);
               }
            }
         }
      }

   private void calculate_new_incomplete_rooms_for_obstacle_expansion_room(SortedRoom_45_Degree p_prev_neighbour, SortedRoom_45_Degree p_next_neighbour, ArtEngine p_autoroute_engine)
      {
      int from_side_no = p_prev_neighbour.last_touching_side;
      int to_side_no = p_next_neighbour.first_touching_side;
      if (from_side_no == to_side_no && p_prev_neighbour != p_next_neighbour)
         {
         // no return in case of only 1 neighbour.
         return;
         }
      ShapeTileOctagon board_bounding_oct = p_autoroute_engine.r_board.bounding_box.bounding_octagon();

      // insert the new incomplete room from p_prev_neighbour to the next corner of the room shape.

      int lx = board_bounding_oct.oct_lx;
      int ly = board_bounding_oct.oct_ly;
      int rx = board_bounding_oct.oct_rx;
      int uy = board_bounding_oct.oct_uy;
      int ulx = board_bounding_oct.oct_ulx;
      int lrx = board_bounding_oct.oct_lrx;
      int llx = board_bounding_oct.oct_llx;
      int urx = board_bounding_oct.oct_urx;
      if (from_side_no == 0)
         {
         uy = this.room_shape.oct_ly;
         ulx = p_prev_neighbour.intersection.oct_lrx;
         }
      else if (from_side_no == 1)
         {
         ulx = this.room_shape.oct_lrx;
         lx = p_prev_neighbour.intersection.oct_rx;
         }
      else if (from_side_no == 2)
         {
         lx = this.room_shape.oct_rx;
         llx = p_prev_neighbour.intersection.oct_urx;
         }
      else if (from_side_no == 3)
         {
         llx = this.room_shape.oct_urx;
         ly = p_prev_neighbour.intersection.oct_uy;
         }
      else if (from_side_no == 4)
         {
         ly = this.room_shape.oct_uy;
         lrx = p_prev_neighbour.intersection.oct_ulx;
         }
      else if (from_side_no == 5)
         {
         lrx = this.room_shape.oct_ulx;
         rx = p_prev_neighbour.intersection.oct_lx;
         }
      else if (from_side_no == 6)
         {
         rx = this.room_shape.oct_lx;
         urx = p_prev_neighbour.intersection.oct_llx;
         }
      else if (from_side_no == 7)
         {
         urx = this.room_shape.oct_llx;
         uy = p_prev_neighbour.intersection.oct_ly;
         }
      insert_incomplete_room(p_autoroute_engine, lx, ly, rx, uy, ulx, lrx, llx, urx);

      // insert the new incomplete room from p_prev_neighbour to the next corner of the room shape.

      lx = board_bounding_oct.oct_lx;
      ly = board_bounding_oct.oct_ly;
      rx = board_bounding_oct.oct_rx;
      uy = board_bounding_oct.oct_uy;
      ulx = board_bounding_oct.oct_ulx;
      lrx = board_bounding_oct.oct_lrx;
      llx = board_bounding_oct.oct_llx;
      urx = board_bounding_oct.oct_urx;

      if (to_side_no == 0)
         {
         uy = this.room_shape.oct_ly;
         urx = p_next_neighbour.intersection.oct_llx;
         }
      else if (to_side_no == 1)
         {
         ulx = this.room_shape.oct_lrx;
         uy = p_next_neighbour.intersection.oct_ly;
         }
      else if (to_side_no == 2)
         {
         lx = this.room_shape.oct_rx;
         ulx = p_next_neighbour.intersection.oct_lrx;
         }
      else if (to_side_no == 3)
         {
         llx = this.room_shape.oct_urx;
         lx = p_next_neighbour.intersection.oct_rx;
         }
      else if (to_side_no == 4)
         {
         ly = this.room_shape.oct_uy;
         llx = p_next_neighbour.intersection.oct_urx;
         }
      else if (to_side_no == 5)
         {
         lrx = this.room_shape.oct_ulx;
         ly = p_next_neighbour.intersection.oct_uy;
         }
      else if (to_side_no == 6)
         {
         rx = this.room_shape.oct_lx;
         lrx = p_next_neighbour.intersection.oct_ulx;
         }
      else if (to_side_no == 7)
         {
         urx = this.room_shape.oct_llx;
         rx = p_next_neighbour.intersection.oct_lx;
         }
      insert_incomplete_room(p_autoroute_engine, lx, ly, rx, uy, ulx, lrx, llx, urx);

      // Insert the new incomplete rooms on the intermediate free sides of the obstacle expansion room.
      int curr_from_side_no = (from_side_no + 1) % 8;
      if (curr_from_side_no == to_side_no)
         {
         return;
         }
      int curr_to_side_no = (to_side_no + 7) % 8;
      this.calculate_edge_incomplete_rooms_of_obstacle_expansion_room(curr_from_side_no, curr_to_side_no, p_autoroute_engine);
      }

   private void calculate_new_incomplete_rooms(ArtEngine p_autoroute_engine)
      {
      ShapeTileOctagon board_bounding_oct = p_autoroute_engine.r_board.bounding_box.bounding_octagon();
      SortedRoom_45_Degree prev_neighbour = this.sorted_neighbours.last();
      if (this.from_room instanceof ExpandRoomObstacle && this.sorted_neighbours.size() == 1)
         {
         // ObstacleExpansionRoom has only only 1 neighbour
         calculate_new_incomplete_rooms_for_obstacle_expansion_room(prev_neighbour, prev_neighbour, p_autoroute_engine);
         return;
         }
      Iterator<SortedRoom_45_Degree> it = this.sorted_neighbours.iterator();

      while (it.hasNext())
         {
         SortedRoom_45_Degree next_neighbour = it.next();

         boolean insert_incomplete_room;

         if (this.completed_room instanceof ExpandRoomObstacle && this.sorted_neighbours.size() == 2)
            {
            // check, if this site is touching or open.
            ShapeTile intersection = next_neighbour.intersection.intersection(prev_neighbour.intersection);
            if (intersection.is_empty())
               {
               insert_incomplete_room = true;
               }
            else if ( ! intersection.dimension().is_empty() )
               {
               insert_incomplete_room = false;
               }
            else
               // dimension = 1
               {
               if (prev_neighbour.last_touching_side == next_neighbour.first_touching_side)
                  {
                  // touch along the side of the room shape
                  insert_incomplete_room = false;
                  }
               else if (prev_neighbour.last_touching_side == (next_neighbour.first_touching_side + 1) % 8)
                  {
                  // touch at a corner of the room shape
                  insert_incomplete_room = false;
                  }
               else
                  {
                  insert_incomplete_room = true;
                  }
               }
            }
         else
            {
            // the 2 neigbours do not touch
            insert_incomplete_room = !next_neighbour.intersection.intersects(prev_neighbour.intersection);
            }

         if (insert_incomplete_room)
            {
            // create a door to a new incomplete expansion room between
            // the last corner of the previous neighbour and the first corner of the
            // current neighbour

            if (this.from_room instanceof ExpandRoomObstacle && next_neighbour.first_touching_side != prev_neighbour.last_touching_side)
               {
               calculate_new_incomplete_rooms_for_obstacle_expansion_room(prev_neighbour, next_neighbour, p_autoroute_engine);
               }
            else
               {
               int lx = board_bounding_oct.oct_lx;
               int ly = board_bounding_oct.oct_ly;
               int rx = board_bounding_oct.oct_rx;
               int uy = board_bounding_oct.oct_uy;
               int ulx = board_bounding_oct.oct_ulx;
               int lrx = board_bounding_oct.oct_lrx;
               int llx = board_bounding_oct.oct_llx;
               int urx = board_bounding_oct.oct_urx;

               if (next_neighbour.first_touching_side == 0)
                  {
                  if (prev_neighbour.intersection.oct_llx < next_neighbour.intersection.oct_llx)
                     {
                     urx = next_neighbour.intersection.oct_llx;
                     uy = prev_neighbour.intersection.oct_ly;
                     if (prev_neighbour.last_touching_side == 0)
                        {
                        ulx = prev_neighbour.intersection.oct_lrx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_llx > next_neighbour.intersection.oct_llx)
                     {
                     rx = next_neighbour.intersection.oct_lx;
                     urx = prev_neighbour.intersection.oct_llx;
                     }
                  else
                     // prev_neighbour.intersection.llx == next_neighbour.intersection.llx
                     {
                     urx = next_neighbour.intersection.oct_llx;
                     }
                  }
               else if (next_neighbour.first_touching_side == 1)
                  {
                  if (prev_neighbour.intersection.oct_ly < next_neighbour.intersection.oct_ly)
                     {
                     uy = next_neighbour.intersection.oct_ly;
                     ulx = prev_neighbour.intersection.oct_lrx;
                     if (prev_neighbour.last_touching_side == 1)
                        {
                        lx = prev_neighbour.intersection.oct_rx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_ly > next_neighbour.intersection.oct_ly)
                     {
                     uy = prev_neighbour.intersection.oct_ly;
                     urx = next_neighbour.intersection.oct_llx;
                     }
                  else
                     // prev_neighbour.intersection.ly == next_neighbour.intersection.ly
                     {
                     uy = next_neighbour.intersection.oct_ly;
                     }
                  }
               else if (next_neighbour.first_touching_side == 2)
                  {
                  if (prev_neighbour.intersection.oct_lrx > next_neighbour.intersection.oct_lrx)
                     {
                     ulx = next_neighbour.intersection.oct_lrx;
                     lx = prev_neighbour.intersection.oct_rx;
                     if (prev_neighbour.last_touching_side == 2)
                        {
                        llx = prev_neighbour.intersection.oct_urx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_lrx < next_neighbour.intersection.oct_lrx)
                     {
                     uy = next_neighbour.intersection.oct_ly;
                     ulx = prev_neighbour.intersection.oct_lrx;
                     }
                  else
                     // prev_neighbour.intersection.lrx == next_neighbour.intersection.lrx
                     {
                     ulx = next_neighbour.intersection.oct_lrx;
                     }
                  }
               else if (next_neighbour.first_touching_side == 3)
                  {
                  if (prev_neighbour.intersection.oct_rx > next_neighbour.intersection.oct_rx)
                     {
                     lx = next_neighbour.intersection.oct_rx;
                     llx = prev_neighbour.intersection.oct_urx;
                     if (prev_neighbour.last_touching_side == 3)
                        {
                        ly = prev_neighbour.intersection.oct_uy;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_rx < next_neighbour.intersection.oct_rx)
                     {
                     lx = prev_neighbour.intersection.oct_rx;
                     ulx = next_neighbour.intersection.oct_lrx;
                     }
                  else
                     // prev_neighbour.intersection.ry == next_neighbour.intersection.ry
                     {
                     lx = next_neighbour.intersection.oct_rx;
                     }
                  }
               else if (next_neighbour.first_touching_side == 4)
                  {
                  if (prev_neighbour.intersection.oct_urx > next_neighbour.intersection.oct_urx)
                     {
                     llx = next_neighbour.intersection.oct_urx;
                     ly = prev_neighbour.intersection.oct_uy;
                     if (prev_neighbour.last_touching_side == 4)
                        {
                        lrx = prev_neighbour.intersection.oct_ulx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_urx < next_neighbour.intersection.oct_urx)
                     {
                     lx = next_neighbour.intersection.oct_rx;
                     llx = prev_neighbour.intersection.oct_urx;
                     }
                  else
                     // prev_neighbour.intersection.urx == next_neighbour.intersection.urx
                     {
                     llx = next_neighbour.intersection.oct_urx;
                     }
                  }
               else if (next_neighbour.first_touching_side == 5)
                  {
                  if (prev_neighbour.intersection.oct_uy > next_neighbour.intersection.oct_uy)
                     {
                     ly = next_neighbour.intersection.oct_uy;
                     lrx = prev_neighbour.intersection.oct_ulx;
                     if (prev_neighbour.last_touching_side == 5)
                        {
                        rx = prev_neighbour.intersection.oct_lx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_uy < next_neighbour.intersection.oct_uy)
                     {
                     ly = prev_neighbour.intersection.oct_uy;
                     llx = next_neighbour.intersection.oct_urx;
                     }
                  else
                     // prev_neighbour.intersection.uy == next_neighbour.intersection.uy
                     {
                     ly = next_neighbour.intersection.oct_uy;
                     }
                  }
               else if (next_neighbour.first_touching_side == 6)
                  {
                  if (prev_neighbour.intersection.oct_ulx < next_neighbour.intersection.oct_ulx)
                     {
                     lrx = next_neighbour.intersection.oct_ulx;
                     rx = prev_neighbour.intersection.oct_lx;
                     if (prev_neighbour.last_touching_side == 6)
                        {
                        urx = prev_neighbour.intersection.oct_llx;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_ulx > next_neighbour.intersection.oct_ulx)
                     {
                     ly = next_neighbour.intersection.oct_uy;
                     lrx = prev_neighbour.intersection.oct_ulx;
                     }
                  else
                     // prev_neighbour.intersection.ulx == next_neighbour.intersection.ulx
                     {
                     lrx = next_neighbour.intersection.oct_ulx;
                     }
                  }
               else if (next_neighbour.first_touching_side == 7)
                  {
                  if (prev_neighbour.intersection.oct_lx < next_neighbour.intersection.oct_lx)
                     {
                     rx = next_neighbour.intersection.oct_lx;
                     urx = prev_neighbour.intersection.oct_llx;
                     if (prev_neighbour.last_touching_side == 7)
                        {
                        uy = prev_neighbour.intersection.oct_ly;
                        }
                     }
                  else if (prev_neighbour.intersection.oct_lx > next_neighbour.intersection.oct_lx)
                     {
                     rx = prev_neighbour.intersection.oct_lx;
                     lrx = next_neighbour.intersection.oct_ulx;
                     }
                  else
                     // prev_neighbour.intersection.lx == next_neighbour.intersection.lx
                     {
                     rx = next_neighbour.intersection.oct_lx;
                     }
                  }
               else
                  {
                  System.out.println("Sorted45DegreeRoomNeighbour.calculate_new_incomplete: illegal touching side");
                  }
               insert_incomplete_room(p_autoroute_engine, lx, ly, rx, uy, ulx, lrx, llx, urx);
               }
            }
         prev_neighbour = next_neighbour;
         }
      }
   }
