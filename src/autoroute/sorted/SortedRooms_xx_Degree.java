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
import planar.PlaDimension;
import planar.PlaDirection;
import planar.PlaLineInt;
import planar.PlaPoint;
import planar.PlaPointInt;
import planar.PlaSide;
import planar.ShapeTile;
import planar.ShapeTileSimplex;
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
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeEntry;
import board.shape.ShapeTreeObject;
import board.varie.TestLevel;

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
   private final ExpandRoomComplete completed_room;
   private final ShapeTile room_shape;
   private final SortedSet<SortedRoom_xx_Degree> sorted_neighbours;
   private final Collection<ShapeTreeEntry> own_net_objects;

   private SortedRooms_xx_Degree(ExpandRoom p_from_room, ExpandRoomComplete p_completed_room)
      {
      from_room = p_from_room;
      completed_room = p_completed_room;
      room_shape = p_completed_room.get_shape();
      sorted_neighbours = new TreeSet<SortedRoom_xx_Degree>();
      own_net_objects = new LinkedList<ShapeTreeEntry>();
      }
   
   /**
    * To calculate the neigbour rooms of an expansion room. The neighbour rooms will be sorted in counterclock sense around the
    * border of the shape of p_room. Overlapping neighbours containing an item may be stored in an unordered list.
    */
   public static ExpandRoomComplete calculate(ExpandRoom p_room, ArtEngine p_engine)
      {
      int net_no = p_engine.get_net_no();
      TestLevel test_level = p_engine.r_board.get_test_level();

      SortedRooms_xx_Degree room_neighbours = calculate_neighbours(p_room, net_no, p_engine.autoroute_search_tree, p_engine.new_room_id_no(), test_level);

      if (room_neighbours == null) return null;

      // Check, that each side of the romm shape has at least one touching neighbour.
      // Otherwise improve the room shape by enlarging.

      boolean edge_removed = room_neighbours.try_remove_edge(net_no, p_engine.autoroute_search_tree, test_level);
      
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
         if ( ! result.get_shape().dimension().is_area() )
            {
            System.out.println("AutorouteEngine.calculate_new_incomplete_rooms_with_mmore_than_1_neighbour: unexpected dimension for smoothened_shape");
            }
         }

      if (result instanceof ExpandRoomFreespaceComplete)
         {
         calculate_target_doors((ExpandRoomFreespaceComplete) result, room_neighbours.own_net_objects, p_engine);
         }
      return result;
      }

   private static void calculate_incomplete_rooms_with_empty_neighbours(ExpandRoomObstacle p_room, ArtEngine p_autoroute_engine)
      {
      ShapeTile room_shape = p_room.get_shape();
      for (int i = 0; i < room_shape.border_line_count(); ++i)
         {
         PlaLineInt curr_line = room_shape.border_line(i);
         if (insert_door_ok(p_room, curr_line))
            {
            PlaLineInt[] shape_line = new PlaLineInt[1];
            shape_line[0] = curr_line.opposite();
            ShapeTile new_room_shape = new ShapeTileSimplex(shape_line);
            ShapeTile new_contained_shape = room_shape.intersection(new_room_shape);
            ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_room_shape, p_room.get_layer(), new_contained_shape);
            ExpandDoor new_door = new ExpandDoor(p_room, new_room, PlaDimension.LINE);
            p_room.add_door(new_door);
            new_room.add_door(new_door);
            }
         }
      }

   private static void calculate_target_doors(ExpandRoomFreespaceComplete p_room, Collection<ShapeTreeEntry> p_own_net_objects, ArtEngine p_autoroute_engine)
      {
      if (!p_own_net_objects.isEmpty())
         {
         p_room.set_net_dependent();
         }
      
      for (ShapeTreeEntry curr_entry : p_own_net_objects)
         {
         if ( ! (curr_entry.object instanceof BrdConnectable)) continue;

         BrdConnectable curr_object = (BrdConnectable) curr_entry.object;
         if ( ! curr_object.contains_net(p_autoroute_engine.get_net_no())) continue;

         ShapeTile curr_connection_shape = curr_object.get_trace_connection_shape(p_autoroute_engine.autoroute_search_tree, curr_entry.shape_index_in_object);

         if (curr_connection_shape != null && p_room.get_shape().intersects(curr_connection_shape))
            {
            BrdItem curr_item = (BrdItem) curr_object;
            ExpandDoorItem new_target_door = new ExpandDoorItem(curr_item, curr_entry.shape_index_in_object, p_room, p_autoroute_engine.autoroute_search_tree);
            p_room.add_target_door(new_target_door);
            }
         }
      }

   private static SortedRooms_xx_Degree calculate_neighbours(ExpandRoom p_room, int p_net_no, ShapeSearchTree p_autoroute_search_tree, int p_room_id_no, TestLevel p_test_level)
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
      
      SortedRooms_xx_Degree result = new SortedRooms_xx_Degree(p_room, completed_room);
      Collection<ShapeTreeEntry> overlapping_objects = new LinkedList<ShapeTreeEntry>();
      p_autoroute_search_tree.calc_overlapping_tree_entries(room_shape, p_room.get_layer(), overlapping_objects);

      // Calculate the touching neigbour objects and sort them in counterclock sence around the border of the room shape.
      
      for (ShapeTreeEntry curr_entry : overlapping_objects)
         {
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_entry.object;
      
         if (curr_object == p_room) continue;

         if ((p_room instanceof ExpandRoomFreespaceIncomplete) && !curr_object.is_trace_obstacle(p_net_no))
            {
            // delay processing the target doors until the room shape will not change any more
            result.own_net_objects.add(curr_entry);
            continue;
            }
      
         ShapeTile curr_shape = curr_object.get_tree_shape(p_autoroute_search_tree, curr_entry.shape_index_in_object);
         ShapeTile intersection = room_shape.intersection(curr_shape);
         PlaDimension dimension = intersection.dimension();
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
         if (dimension.is_empty())
            {
            System.out.println("SortedRoomNeighbours.calculate: dimension >= 0 expected");
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
            if (neighbour_room != null)
               {
               if (SortedRooms_xx_Degree.insert_door_ok(completed_room, neighbour_room, intersection))
                  {
                  ExpandDoor new_door = new ExpandDoor(completed_room, neighbour_room, PlaDimension.LINE);
                  neighbour_room.add_door(new_door);
                  completed_room.add_door(new_door);
                  }
               }
            }
         else
            // dimensin = 0
            {
            PlaPoint touching_point = intersection.corner(0);
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
         }
      return result;
      }


   private void add_sorted_neighbour(ShapeTile p_neighbour_shape, int p_touching_side_no_of_room, int p_touching_side_no_of_neighbour_room, boolean p_room_touch_is_corner,
         boolean p_neighbour_room_touch_is_corner)
      {
      SortedRoom_xx_Degree new_neighbour = new SortedRoom_xx_Degree(room_shape, p_neighbour_shape, p_touching_side_no_of_room, p_touching_side_no_of_neighbour_room, p_room_touch_is_corner,
            p_neighbour_room_touch_is_corner);
      sorted_neighbours.add(new_neighbour);
      }

   /**
    * Check, that each side of the romm shape has at least one touching neighbour. Otherwise the room shape will be improved the by
    * enlarging. Returns true, if the room shape was changed.
    */
   private boolean try_remove_edge(int p_net_no, ShapeSearchTree p_autoroute_search_tree, TestLevel p_test_level)
      {
      if (!(from_room instanceof ExpandRoomFreespaceIncomplete)) return false;

      ExpandRoomFreespaceIncomplete curr_incomplete_room = (ExpandRoomFreespaceIncomplete) this.from_room;
      Iterator<SortedRoom_xx_Degree> it = sorted_neighbours.iterator();
      int remove_edge_no = -1;
      ShapeTileSimplex room_simplex = curr_incomplete_room.get_shape().to_Simplex();
      double room_shape_area = room_simplex.area();

      int prev_edge_no = -1;
      int curr_edge_no = 0;
      while (it.hasNext())
         {
         SortedRoom_xx_Degree next_neighbour = it.next();

         if (next_neighbour.touching_side_no_of_room == prev_edge_no)
            {
            continue;
            }
         if (next_neighbour.touching_side_no_of_room == curr_edge_no)
            {
            prev_edge_no = curr_edge_no;
            ++curr_edge_no;
            }
         else
            {
            // On the edge side with index curr_edge_no is no touching
            // neighbour.
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
         // Touching neighbour missing at the edge side with index remove_edge_no
         // Remove the edge line and restart the algorithm.
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
                     ((ExpandRoomFreespaceComplete) this.completed_room).set_shape(this.completed_room.get_shape().intersection(cut_half_plane));
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

               boolean last_time = curr_touching_side_no == first_touching_side_no && !(prev_neighbour == this.sorted_neighbours.last() && first_time)
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

                  ShapeTile new_contained_shape = this.completed_room.get_shape().intersection(new_room_shape);
                  if (!new_contained_shape.is_empty())
                     {
                     ExpandRoomFreespace new_room = p_autoroute_engine.add_incomplete_expansion_room(new_room_shape, this.from_room.get_layer(), new_contained_shape);
                     ExpandDoor new_door = new ExpandDoor(this.completed_room, new_room, PlaDimension.LINE);
                     this.completed_room.add_door(new_door);
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

   /**
    * p_door_shape is expected to bave dimension 1.
    */
   static boolean insert_door_ok(ExpandRoom p_room_1, ExpandRoom p_room_2, ShapeTile p_door_shape)
      {
      if (p_room_1.door_exists(p_room_2))
         {
         return false;
         }
      if (p_room_1 instanceof ExpandRoomObstacle && p_room_2 instanceof ExpandRoomObstacle)
         {
         BrdItem first_item = ((ExpandRoomObstacle) p_room_1).get_item();
         BrdItem second_item = ((ExpandRoomObstacle) p_room_2).get_item();
         // insert only overlap_doors between items of the same net for performance reasons.
         return (first_item.shares_net(second_item));
         }
      if (!(p_room_1 instanceof ExpandRoomObstacle) && !(p_room_2 instanceof ExpandRoomObstacle))
         {
         return true;
         }
      // Insert 1 dimensional doors of trace rooms only, if they are parallel to the trace line.
      // Otherwise there may be check ripup problems with entering at the wrong side at a fork.
      PlaLineInt door_line = null;
      PlaPoint prev_corner = p_door_shape.corner(0);
      int corner_count = p_door_shape.border_line_count();
      for (int i = 1; i < corner_count; ++i)
         {
         PlaPoint curr_corner = p_door_shape.corner(i);
         if (!curr_corner.equals(prev_corner))
            {
            door_line = p_door_shape.border_line(i - 1);
            break;
            }
         prev_corner = curr_corner;
         }
      if (p_room_1 instanceof ExpandRoomObstacle)
         {
         if (!insert_door_ok((ExpandRoomObstacle) p_room_1, door_line))
            {
            return false;
            }
         }
      if (p_room_2 instanceof ExpandRoomObstacle)
         {
         if (!insert_door_ok((ExpandRoomObstacle) p_room_2, door_line))
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Insert 1 dimensional doors for the first and the last room of a trace rooms only, if they are parallel to the trace line.
    * Otherwise there may be check ripup problems with entering at the wrong side at a fork.
    */
   private static boolean insert_door_ok(ExpandRoomObstacle p_room, PlaLineInt p_door_line)
      {
      if (p_door_line == null)
         {
         System.err.println("SortedRoomNeighbours.insert_door_ok: p_door_line is null");
         return false;
         }
      
      BrdItem curr_item = p_room.get_item();
      if (curr_item instanceof BrdTracePolyline)
         {
         int room_index = p_room.get_index_in_item();
         BrdTracePolyline curr_trace = (BrdTracePolyline) curr_item;
         if (room_index == 0 || room_index == curr_trace.tile_shape_count() - 1)
            {
            PlaLineInt curr_trace_line = curr_trace.polyline().lines_arr[room_index + 1];
            if (!curr_trace_line.is_parallel(p_door_line))
               {
               return false;
               }
            }
         }
      return true;
      }
   }
