package autoroute.sorted;

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
 * Created on 28. Mai 2007, 07:27
 *
 */

import java.util.Collection;
import autoroute.ArtEngine;
import autoroute.ArtItem;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandDoorItem;
import autoroute.expand.ExpandRoom;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomFreespace;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import autoroute.expand.ExpandRoomObstacle;
import board.BrdConnectable;
import board.awtree.AwtreeEntry;
import board.awtree.AwtreeObject;
import board.awtree.AwtreeShapeSearch;
import board.items.BrdItem;
import board.items.BrdTracep;
import freert.planar.PlaDimension;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileSimplex;

public final class SortedRoomsTop
   {
   private final ArtEngine r_engine;
   
   public SortedRoomsTop (ArtEngine p_engine)
      {
      r_engine = p_engine;
      }

   /**
    * To calculate the neigbour rooms of an expansion room. The neighbour rooms will be sorted in counterclock sense around the
    * border of the shape of p_room. Overlapping neighbours containing an item may be stored in an unordered list.
    */
   public ExpandRoomComplete calculate(ExpandRoom p_room)
      {
      int net_no = r_engine.get_net_no();

      SortedRoomsList room_neighbours = calculate_neighbours(p_room, net_no, r_engine.art_search_tree, r_engine.new_room_id_no() );

      if (room_neighbours == null) return null;

      // Check, that each side of the romm shape has at least one touching neighbour.
      // Otherwise improve the room shape by enlarging.

      boolean edge_removed = room_neighbours.try_remove_edge(net_no, r_engine.art_search_tree );
      
      ExpandRoomComplete result = room_neighbours.completed_room;
      
      if (edge_removed)
         {
         r_engine.remove_all_doors(result);
         
         return calculate(p_room);
         }

      // Now calculate the new incomplete rooms together with the doors between this room and the sorted neighbours.
      if (room_neighbours.sorted_neighbours.isEmpty())
         {
         if (result instanceof ExpandRoomObstacle)
            {
            calculate_incomplete_rooms_with_empty_neighbours((ExpandRoomObstacle) p_room );
            }
         }
      else
         {
         room_neighbours.calculate_new_incomplete_rooms(r_engine);
         if ( ! result.get_shape().dimension().is_area() )
            {
            System.out.println("AutorouteEngine.calculate_new_incomplete_rooms_with_mmore_than_1_neighbour: unexpected dimension for smoothened_shape");
            }
         }

      if (result instanceof ExpandRoomFreespaceComplete)
         {
         calculate_target_doors((ExpandRoomFreespaceComplete) result, room_neighbours.own_net_objects, r_engine);
         }
      return result;
      }

   private void calculate_incomplete_rooms_with_empty_neighbours(ExpandRoomObstacle p_room )
      {
      ShapeTile room_shape = p_room.get_shape();

      for (int index = 0; index < room_shape.border_line_count(); ++index)
         {
         PlaLineInt curr_line = room_shape.border_line(index);
         
         if ( ! insert_door_ok_test(p_room, curr_line)) continue;

         ShapeTile new_room_shape = new ShapeTileSimplex(curr_line.opposite());
         
         ShapeTile new_contained_shape = room_shape.intersection(new_room_shape);
         ExpandRoomFreespace new_room = r_engine.add_incomplete_expansion_room(new_room_shape, p_room.get_layer(), new_contained_shape);
         ExpandDoor new_door = new ExpandDoor(p_room, new_room, PlaDimension.LINE);
         p_room.add_door(new_door);
         new_room.add_door(new_door);

         }
      }

   
   private void calculate_target_doors(ExpandRoomFreespaceComplete p_room, Collection<AwtreeEntry> p_own_net_objects, ArtEngine p_autoroute_engine)
      {
      if (!p_own_net_objects.isEmpty())
         {
         p_room.set_net_dependent();
         }
      
      for (AwtreeEntry curr_entry : p_own_net_objects)
         {
         if ( ! (curr_entry.object instanceof BrdItem)) continue;

         BrdItem an_item = (BrdItem) curr_entry.object;
         
         if ( ! an_item.contains_net(p_autoroute_engine.get_net_no())) continue;

         if ( ! (an_item instanceof BrdConnectable)) continue;
         
         BrdConnectable a_conn = (BrdConnectable) an_item;
         
         ShapeTile curr_connection_shape = a_conn.get_trace_connection_shape(p_autoroute_engine.art_search_tree, curr_entry.shape_index_in_object);

         if (curr_connection_shape != null && p_room.get_shape().intersects(curr_connection_shape))
            {
            ExpandDoorItem new_target_door = new ExpandDoorItem(an_item, curr_entry.shape_index_in_object, p_room, p_autoroute_engine.art_search_tree);
            p_room.add_target_door(new_target_door);
            }
         }
      }

   
   private SortedRoomsList calculate_neighbours(ExpandRoom p_room, int p_net_no, AwtreeShapeSearch p_autoroute_search_tree, int p_room_id_no )
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
         System.out.println("SortedRoomNeighbours.calculate: unexpected expansion room type");
         return null;
         }
      
      SortedRoomsList result = new SortedRoomsList(p_room, completed_room);
      
      Collection<AwtreeEntry> overlapping_objects = p_autoroute_search_tree.find_overlap_tree_entries(room_shape, p_room.get_layer());

      // Calculate the touching neigbour objects and sort them in counterclock sence around the border of the room shape.
      
      for (AwtreeEntry curr_entry : overlapping_objects)
         {
         AwtreeObject curr_object = curr_entry.object;
      
         if (curr_object == p_room) continue;

         if ((p_room instanceof ExpandRoomFreespaceIncomplete) && !curr_object.is_trace_obstacle(p_net_no))
            {
            // delay processing the target doors until the room shape will not change any more
            result.own_net_objects.add(curr_entry);
            continue;
            }
      
         ShapeTile curr_shape = curr_object.get_tree_shape(p_autoroute_search_tree, curr_entry.shape_index_in_object);
         ShapeTile shape_intersect = room_shape.intersection(curr_shape);
         PlaDimension dimension = shape_intersect.dimension();

         if (dimension.is_empty())
            {
            System.out.println("SortedRoomNeighbours.calculate: dimension >= 0 expected"); // pippone
            continue;
            }
         
         if (dimension.is_area() )
            {
            if (completed_room instanceof ExpandRoomObstacle && curr_object instanceof BrdItem)
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
            else 
               {
               System.out.println("SortedRoomNeighbours.calculate: unexpected area overlap of free space expansion room");
               }
            continue;
            }
         
         if (dimension.is_line() )
            {
            int[] touching_sides = room_shape.touching_sides(curr_shape);
            if (touching_sides.length != 2)
               {
               System.out.println("SortedRoomNeighbours.calculate: touching_sides length 2 expected");
               continue;
               }

            result.add_sorted_neighbour(curr_shape, touching_sides[0], touching_sides[1], false, false);
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
            
            if (neighbour_room == null) continue;

            if (insert_door_ok_test(completed_room, neighbour_room, shape_intersect))
               {
               ExpandDoor new_door = new ExpandDoor(completed_room, neighbour_room, PlaDimension.LINE);
               neighbour_room.add_door(new_door);
               completed_room.add_door(new_door);
               }

            continue;
            }

/*         
 * Hmmmm, apparently handling the point case produce quite worse results...
 * I do not have enough insignt at the moment to guess why handling a single point would be "good" or not, at the moment this code is commented out
 * 
         if (dimension.is_point() )
            {
            PlaPointInt touching_point = shape_intersect.corner(0);
            int room_corner_no = room_shape.equals_corner(touching_point);
            boolean room_touch_is_corner;
            int touching_side_no_of_room;
            if (room_corner_no >= 0)
               {
               room_touch_is_corner = true;
               touching_side_no_of_room = room_corner_no;
               }
            else
               {
               room_touch_is_corner = false;
               touching_side_no_of_room = room_shape.contains_on_border_line_no(touching_point);
               if (touching_side_no_of_room < 0 )
                  {
                  System.out.println("SortedRoomNeighbours.calculate: touching_side_no_of_room >= 0 expected");
                  }
               }
            int neighbour_room_corner_no = curr_shape.equals_corner(touching_point);
            boolean neighbour_room_touch_is_corner;
            int touching_side_no_of_neighbour_room;
            if (neighbour_room_corner_no >= 0)
               {
               neighbour_room_touch_is_corner = true;
               // The previous border line is preferred to make the shape of the incomplete room as big as possible
               touching_side_no_of_neighbour_room = curr_shape.prev_no(neighbour_room_corner_no);
               }
            else
               {
               neighbour_room_touch_is_corner = false;
               touching_side_no_of_neighbour_room = curr_shape.contains_on_border_line_no(touching_point);
               if (touching_side_no_of_neighbour_room < 0 )
                  {
                  System.out.println("AutorouteEngine.SortedRoomNeighbours.calculate: touching_side_no_of_neighbour_room >= 0 expected");
                  }
               }
            result.add_sorted_neighbour(curr_shape, touching_side_no_of_room, touching_side_no_of_neighbour_room, room_touch_is_corner, neighbour_room_touch_is_corner);
            }
  */       
         }
      return result;
      }
   
   /**
    * p_door_shape is expected to bave dimension 1.
    * @return true if the door can be added successfully
    */
   private boolean insert_door_ok_test(ExpandRoom p_room_1, ExpandRoom p_room_2, ShapeTile p_door_shape)
      {
      // there is already a door to room_2
      if (p_room_1.door_exists(p_room_2)) return false;

      if (p_room_1 instanceof ExpandRoomObstacle && p_room_2 instanceof ExpandRoomObstacle)
         {
         BrdItem first_item = ((ExpandRoomObstacle) p_room_1).get_item();
         BrdItem second_item = ((ExpandRoomObstacle) p_room_2).get_item();
         // insert only overlap_doors between items of the same net for performance reasons.
         return first_item.shares_net(second_item);
         }
      
      
      if (!(p_room_1 instanceof ExpandRoomObstacle) && !(p_room_2 instanceof ExpandRoomObstacle))
         {
         return true;
         }
      
      
      // Insert 1 dimensional doors of trace rooms only, if they are parallel to the trace line.
      // Otherwise there may be check ripup problems with entering at the wrong side at a fork.
      
      PlaLineInt door_line = null;
      PlaPointInt prev_corner = p_door_shape.corner(0);
      int corner_count = p_door_shape.border_line_count();
      
      for (int index = 1; index < corner_count; ++index)
         {
         PlaPointInt curr_corner = p_door_shape.corner(index);
         if (!curr_corner.equals(prev_corner))
            {
            door_line = p_door_shape.border_line(index - 1);
            break;
            }
         prev_corner = curr_corner;
         }
      
      // if there is no door line
      if ( door_line == null ) return false;
      
      if (p_room_1 instanceof ExpandRoomObstacle)
         {
         if (!insert_door_ok_test((ExpandRoomObstacle) p_room_1, door_line)) return false;
         }

      if (p_room_2 instanceof ExpandRoomObstacle)
         {
         if (!insert_door_ok_test((ExpandRoomObstacle) p_room_2, door_line)) return false;
         }
      
      return true;
      }

   
   /**
    * test if Insert 1 dimensional doors for the first and the last room of a trace rooms only, if they are parallel to the trace line.
    * Otherwise there may be check ripup problems with entering at the wrong side at a fork.
    */
   private boolean insert_door_ok_test(ExpandRoomObstacle p_room, PlaLineInt p_door_line)
      {
      if (p_door_line == null)
         {
         System.err.println("SortedRoomNeighbours.insert_door_ok_test: p_door_line is null");
         return false;
         }
      
      BrdItem curr_item = p_room.get_item();

      // the test is only for the traces
      if ( ! (curr_item instanceof BrdTracep)) return true;
      
      int room_index = p_room.get_index_in_item();
      BrdTracep curr_trace = (BrdTracep) curr_item;

      // the test is only for the firat and last room of a trace, all the rest is fine
      if ( room_index != 0 && room_index != curr_trace.tile_shape_count() - 1) return true;

      PlaLineInt curr_trace_line = curr_trace.polyline().plaline(room_index + 1);

      // insert is ok only if both lines are aprallel
      return curr_trace_line.is_parallel(p_door_line);
      }
   
   
   
   
   
   
   
   
   
   
   
   }
