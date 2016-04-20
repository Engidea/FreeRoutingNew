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
 * OrthogonalAutorouteEngine.java
 *
 * Created on 24. Mai 2007, 07:51
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
import planar.ShapeTile;
import planar.ShapeTileBox;
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
public final class SortedRooms_90_Degree
   {
   private final ExpandRoomComplete completed_room;
   private final SortedSet<SortedRoom_90_Degree> sorted_neighbours;
   
   private final ExpandRoom from_room;
   private final boolean is_obstacle_expansion_room;
   private final ShapeTileBox room_shape;
   private final boolean[] edge_interiour_touches_obstacle = new boolean[4];
   
   private SortedRooms_90_Degree(ExpandRoom p_from_room, ExpandRoomComplete p_completed_room)
      {
      from_room = p_from_room;
      completed_room = p_completed_room;
      is_obstacle_expansion_room = p_from_room instanceof ExpandRoomObstacle;
      room_shape = (ShapeTileBox) p_completed_room.get_shape();
      sorted_neighbours = new TreeSet<SortedRoom_90_Degree>();

      for (int index = 0; index < edge_interiour_touches_obstacle.length; ++index)
         {
         edge_interiour_touches_obstacle[index] = false;
         }
      }

   
   public static ExpandRoomComplete calculate(ExpandRoom p_room, ArtEngine p_engine)
      {
      int net_no = p_engine.get_net_no();
      
      SortedRooms_90_Degree room_neighbours = calculate_neighbours(p_room, net_no, p_engine.autoroute_search_tree, p_engine.new_room_id_no());
      
      if (room_neighbours == null) return null;

      // Check, that each side of the romm shape has at least one touching neighbour.
      // Otherwise improve the room shape by enlarging.
      boolean edge_removed = room_neighbours.try_remove_edge(net_no, p_engine.autoroute_search_tree);
      
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
            calculate_incomplete_rooms_with_empty_neighbours((ExpandRoomObstacle) p_room, p_engine);
            }
         }
      else
         {
         room_neighbours.calculate_new_incomplete_rooms(p_engine);
         }
      return result;
      }

   private static void calculate_incomplete_rooms_with_empty_neighbours(ExpandRoomObstacle p_room, ArtEngine p_autoroute_engine)
      {
      ShapeTile room_shape = p_room.get_shape();
      if (!(room_shape instanceof ShapeTileBox))
         {
         System.out.println("SortedOrthoganelRoomNeighbours.calculate_incomplete_rooms_with_empty_neighbours: IntBox expected for room_shape");
         return;
         }
      
      
      ShapeTileBox room_box = (ShapeTileBox) room_shape;
      ShapeTileBox bounding_box = p_autoroute_engine.r_board.get_bounding_box();
      for (int i = 0; i < 4; ++i)
         {
         ShapeTileBox new_room_box;
         if (i == 0)
            {
            new_room_box = new ShapeTileBox(bounding_box.box_ll.v_x, bounding_box.box_ll.v_y, bounding_box.box_ur.v_x, room_box.box_ll.v_y);
            }
         else if (i == 1)
            {
            new_room_box = new ShapeTileBox(room_box.box_ur.v_x, bounding_box.box_ll.v_y, bounding_box.box_ur.v_x, bounding_box.box_ur.v_y);
            }
         else if (i == 2)
            {
            new_room_box = new ShapeTileBox(bounding_box.box_ll.v_x, room_box.box_ur.v_y, bounding_box.box_ur.v_x, bounding_box.box_ur.v_y);
            }
         else if (i == 3)
            {
            new_room_box = new ShapeTileBox(bounding_box.box_ll.v_x, bounding_box.box_ll.v_y, room_box.box_ll.v_x, bounding_box.box_ur.v_y);
            }
         else
            {
            System.out.println("SortedOrthoganelRoomNeighbours.calculate_incomplete_rooms_with_empty_neighbours: illegal index i");
            return;
            }
         
         ShapeTileBox new_contained_box = room_box.intersection(new_room_box);
         ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_room_box, p_room.get_layer(), new_contained_box);
         ExpandDoor new_door = new ExpandDoor(p_room, new_room, PlaDimension.LINE);
         p_room.add_door(new_door);
         new_room.add_door(new_door);
         }
      }

   /**
    * Calculates all touching neighbours of p_room and sorts them in counterclock sense around the boundary of the room shape.
    */
   private static SortedRooms_90_Degree calculate_neighbours(ExpandRoom p_room, int p_net_no, ShapeSearchTree p_autoroute_search_tree, int p_room_id_no)
      {
      ShapeTile room_shape = p_room.get_shape();
      if (!(room_shape instanceof ShapeTileBox))
         {
         System.out.println("SortedOrthogonalRoomNeighbours.calculate: IntBox expected for room_shape");
         return null;
         }
      ShapeTileBox room_box = (ShapeTileBox) room_shape;
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
         System.out.println("SortedOrthogonalRoomNeighbours.calculate: unexpected expansion room type");
         return null;
         }
      SortedRooms_90_Degree result = new SortedRooms_90_Degree(p_room, completed_room);
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
         if (!(curr_shape instanceof ShapeTileBox))
            {
            System.out.println("OrthogonalAutorouteEngine:calculate_sorted_neighbours: IntBox expected for curr_shape");
            return null;
            }
         ShapeTileBox curr_box = (ShapeTileBox) curr_shape;
         ShapeTileBox intersection = room_box.intersection(curr_box);
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
            System.out.println("AutorouteEngine.calculate_doors: dimension >= 0 expected");
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

   private void calculate_new_incomplete_rooms(ArtEngine p_autoroute_engine)
      {
      ShapeTileBox board_bounds = p_autoroute_engine.r_board.bounding_box;
      SortedRoom_90_Degree prev_neighbour = this.sorted_neighbours.last();
      Iterator<SortedRoom_90_Degree> it = this.sorted_neighbours.iterator();

      while (it.hasNext())
         {
         SortedRoom_90_Degree next_neighbour = it.next();

         if (!next_neighbour.intersection.intersects(prev_neighbour.intersection))
            {
            // create a door to a new incomplete expansion room between
            // the last corner of the previous neighbour and the first corner of the
            // current neighbour.
            if (next_neighbour.first_touching_side == 0)
               {
               if (prev_neighbour.last_touching_side == 0)
                  {
                  if (prev_neighbour.intersection.box_ur.v_x < next_neighbour.intersection.box_ll.v_x)
                     {
                     insert_incomplete_room(p_autoroute_engine, prev_neighbour.intersection.box_ur.v_x, board_bounds.box_ll.v_y, next_neighbour.intersection.box_ll.v_x, this.room_shape.box_ll.v_y);
                     }
                  }
               else
                  {
                  if (prev_neighbour.intersection.box_ll.v_y > this.room_shape.box_ll.v_y || next_neighbour.intersection.box_ll.v_x > this.room_shape.box_ll.v_x)
                     {
                     if (is_obstacle_expansion_room)
                        {
                        // no 2-dim doors between obstacle_expansion_rooms and free space rooms allowed.
                        if (prev_neighbour.last_touching_side == 3)
                           {
                           insert_incomplete_room(p_autoroute_engine, board_bounds.box_ll.v_x, room_shape.box_ll.v_y, room_shape.box_ll.v_x, prev_neighbour.intersection.box_ll.v_y);
                           }
                        insert_incomplete_room(p_autoroute_engine, room_shape.box_ll.v_x, board_bounds.box_ll.v_y, next_neighbour.intersection.box_ll.v_x, room_shape.box_ll.v_y);
                        }
                     else
                        {
                        insert_incomplete_room(p_autoroute_engine, board_bounds.box_ll.v_x, board_bounds.box_ll.v_y, next_neighbour.intersection.box_ll.v_x, prev_neighbour.intersection.box_ll.v_y);
                        }
                     }
                  }
               }
            else if (next_neighbour.first_touching_side == 1)
               {
               if (prev_neighbour.last_touching_side == 1)
                  {
                  if (prev_neighbour.intersection.box_ur.v_y < next_neighbour.intersection.box_ll.v_y)
                     {
                     insert_incomplete_room(p_autoroute_engine, this.room_shape.box_ur.v_x, prev_neighbour.intersection.box_ur.v_y, board_bounds.box_ur.v_x, next_neighbour.intersection.box_ll.v_y);
                     }
                  }
               else
                  {
                  if (prev_neighbour.intersection.box_ur.v_x < this.room_shape.box_ur.v_x || next_neighbour.intersection.box_ll.v_y > this.room_shape.box_ll.v_y)
                     {
                     if (is_obstacle_expansion_room)
                        {
                        // no 2-dim doors between obstacle_expansion_rooms and free space rooms allowed.
                        if (prev_neighbour.last_touching_side == 0)
                           {
                           insert_incomplete_room(p_autoroute_engine, prev_neighbour.intersection.box_ur.v_x, board_bounds.box_ll.v_y, room_shape.box_ur.v_x, room_shape.box_ll.v_y);
                           }
                        insert_incomplete_room(p_autoroute_engine, room_shape.box_ur.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, next_neighbour.intersection.box_ll.v_y);
                        }
                     else
                        {
                        insert_incomplete_room(p_autoroute_engine, prev_neighbour.intersection.box_ur.v_x, board_bounds.box_ll.v_y, board_bounds.box_ur.v_x, next_neighbour.intersection.box_ll.v_y);
                        }
                     }
                  }
               }
            else if (next_neighbour.first_touching_side == 2)
               {
               if (prev_neighbour.last_touching_side == 2)
                  {
                  if (prev_neighbour.intersection.box_ll.v_x > next_neighbour.intersection.box_ur.v_x)
                     {
                     insert_incomplete_room(p_autoroute_engine, next_neighbour.intersection.box_ur.v_x, this.room_shape.box_ur.v_y, prev_neighbour.intersection.box_ll.v_x, board_bounds.box_ur.v_y);
                     }
                  }
               else
                  {
                  if (prev_neighbour.intersection.box_ur.v_y < this.room_shape.box_ur.v_y || next_neighbour.intersection.box_ur.v_x < this.room_shape.box_ur.v_x)
                     {
                     if (is_obstacle_expansion_room)
                        {
                        // no 2-dim doors between obstacle_expansion_rooms and free space rooms allowed.
                        if (prev_neighbour.last_touching_side == 1)
                           {
                           insert_incomplete_room(p_autoroute_engine, room_shape.box_ur.v_x, prev_neighbour.intersection.box_ur.v_y, board_bounds.box_ur.v_x, room_shape.box_ur.v_y);
                           }
                        insert_incomplete_room(p_autoroute_engine, next_neighbour.intersection.box_ur.v_x, room_shape.box_ur.v_y, room_shape.box_ur.v_x, board_bounds.box_ur.v_y);
                        }
                     else
                        {
                        insert_incomplete_room(p_autoroute_engine, next_neighbour.intersection.box_ur.v_x, prev_neighbour.intersection.box_ur.v_y, board_bounds.box_ur.v_x, board_bounds.box_ur.v_y);
                        }
                     }
                  }
               }
            else if (next_neighbour.first_touching_side == 3)
               {
               if (prev_neighbour.last_touching_side == 3)
                  {
                  if (prev_neighbour.intersection.box_ll.v_y > next_neighbour.intersection.box_ur.v_y)
                     {
                     insert_incomplete_room(p_autoroute_engine, board_bounds.box_ll.v_x, next_neighbour.intersection.box_ur.v_y, this.room_shape.box_ll.v_x, prev_neighbour.intersection.box_ll.v_y);
                     }
                  }
               else
                  {
                  if (next_neighbour.intersection.box_ur.v_y < this.room_shape.box_ur.v_y || prev_neighbour.intersection.box_ll.v_x > this.room_shape.box_ll.v_x)
                     {
                     if (is_obstacle_expansion_room)
                        {
                        // no 2-dim doors between obstacle_expansion_rooms and free space rooms allowed.
                        if (prev_neighbour.last_touching_side == 2)
                           {
                           insert_incomplete_room(p_autoroute_engine, room_shape.box_ll.v_x, room_shape.box_ur.v_y, prev_neighbour.intersection.box_ll.v_x, board_bounds.box_ur.v_y);
                           }
                        insert_incomplete_room(p_autoroute_engine, board_bounds.box_ll.v_x, next_neighbour.intersection.box_ur.v_y, room_shape.box_ll.v_x, room_shape.box_ur.v_y);
                        }
                     else
                        {
                        insert_incomplete_room(p_autoroute_engine, board_bounds.box_ll.v_x, next_neighbour.intersection.box_ur.v_y, prev_neighbour.intersection.box_ll.v_x, board_bounds.box_ur.v_y);
                        }
                     }
                  }
               }
            else
               {
               System.out.println("SortedOrthogonalRoomNeighbour.calculate_new_incomplete: illegal touching side");
               }
            }
         prev_neighbour = next_neighbour;
         }
      }

   private void insert_incomplete_room(ArtEngine p_autoroute_engine, int p_ll_x, int p_ll_y, int p_ur_x, int p_ur_y)
      {
      ShapeTileBox new_incomplete_room_shape = new ShapeTileBox(p_ll_x, p_ll_y, p_ur_x, p_ur_y);

      if ( ! new_incomplete_room_shape.dimension().is_area() ) return;

      ShapeTileBox new_contained_shape = room_shape.intersection(new_incomplete_room_shape);
      if ( new_contained_shape.is_empty()) return;
      
      // WARNING the check was for > 0
      PlaDimension door_dimension = new_incomplete_room_shape.intersection(room_shape).dimension();
      if (! door_dimension.is_empty() )
         {
         ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_incomplete_room_shape, from_room.get_layer(), new_contained_shape);
         ExpandDoor new_door = new ExpandDoor(this.completed_room, new_room, door_dimension);
         this.completed_room.add_door(new_door);
         new_room.add_door(new_door);
         }
      }


   /**
    * Check, that each side of the romm shape has at least one touching neighbour. Otherwise the room shape will be improved the by
    * enlarging. Returns true, if the room shape was changed.
    */
   private boolean try_remove_edge(int p_net_no, ShapeSearchTree p_autoroute_search_tree)
      {
      if (!(this.from_room instanceof ExpandRoomFreespaceIncomplete))
         {
         return false;
         }
      ExpandRoomFreespaceIncomplete curr_incomplete_room = (ExpandRoomFreespaceIncomplete) this.from_room;
      if (!(curr_incomplete_room.get_shape() instanceof ShapeTileBox))
         {
         System.out.println("SortedOrthogonalRoomNeighbours.try_remove_edge: IntBox expected for room_shape type");
         return false;
         }
      ShapeTileBox room_box = (ShapeTileBox) curr_incomplete_room.get_shape();
      double room_area = room_box.area();

      int remove_edge_no = -1;
      for (int i = 0; i < 4; ++i)
         {
         if (!this.edge_interiour_touches_obstacle[i])
            {
            remove_edge_no = i;
            break;
            }
         }

      if (remove_edge_no >= 0)
         {
         // Touching neighbour missing at the edge side with index remove_edge_no
         // Remove the edge line and restart the algorithm.
         ShapeTileBox enlarged_box = remove_border_line(room_box, remove_edge_no);
         Collection<ExpandDoor> door_list = this.completed_room.get_doors();
         ShapeTile ignore_shape = null;
         ShapeTreeObject ignore_object = null;
         double max_door_area = 0;
         for (ExpandDoor curr_door : door_list)
            {
            // insert the overlapping doors with CompleteFreeSpaceExpansionRooms
            // for the information in complete_shape about the objects to ignore.
            if ( ! curr_door.dimension.is_area() ) continue;

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
         ExpandRoomFreespaceIncomplete enlarged_room = new ExpandRoomFreespaceIncomplete(enlarged_box, curr_incomplete_room.get_layer(), curr_incomplete_room.get_contained_shape());
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

   private static ShapeTileBox remove_border_line(ShapeTileBox p_room_box, int p_remove_edge_no)
      {
      ShapeTileBox result;
      if (p_remove_edge_no == 0)
         {
         result = new ShapeTileBox(p_room_box.box_ll.v_x, -PlaLimits.CRIT_INT, p_room_box.box_ur.v_x, p_room_box.box_ur.v_y);
         }
      else if (p_remove_edge_no == 1)
         {
         result = new ShapeTileBox(p_room_box.box_ll.v_x, p_room_box.box_ll.v_y, PlaLimits.CRIT_INT, p_room_box.box_ur.v_y);
         }
      else if (p_remove_edge_no == 2)
         {
         result = new ShapeTileBox(p_room_box.box_ll.v_x, p_room_box.box_ll.v_y, p_room_box.box_ur.v_x, PlaLimits.CRIT_INT);
         }
      else if (p_remove_edge_no == 3)
         {
         result = new ShapeTileBox(-PlaLimits.CRIT_INT, p_room_box.box_ll.v_y, p_room_box.box_ur.v_x, p_room_box.box_ur.v_y);
         }
      else
         {
         System.out.println("SortedOrthogonalRoomNeighbours.remove_border_line: illegal p_remove_edge_no");
         result = null;
         }
      return result;
      }

   private void add_sorted_neighbour( ShapeTileBox p_intersection)
      {
      SortedRoom_90_Degree new_neighbour = new SortedRoom_90_Degree(room_shape,  edge_interiour_touches_obstacle,  p_intersection);
      sorted_neighbours.add(new_neighbour);
      }
   }
