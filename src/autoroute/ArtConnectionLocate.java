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
 * Created on 31. Januar 2006, 08:20
 *
 */
package autoroute;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import autoroute.expand.ExpandDoorItem;
import autoroute.expand.ExpandDrill;
import autoroute.expand.ExpandObject;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomObstacle;
import autoroute.maze.MazeSearchElement;
import autoroute.maze.MazeSearchResult;
import autoroute.varie.ArtBacktrackElement;
import autoroute.varie.ArtLocateResult;
import board.BrdConnectable;
import board.items.BrdItem;
import board.shape.ShapeSearchTree;
import board.varie.TraceAngleRestriction;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 *
 * @author Alfons Wirtz
 */
public abstract class ArtConnectionLocate
   {
   // The new items implementing the found connection
   public final Collection<ArtLocateResult> connection_items = new LinkedList<ArtLocateResult>();
   // The start item of the new routed connection 
   public final BrdItem start_item;
   // The layer of the connection to the start item 
   public final int start_layer;
   // The destination item of the new routed connection 
   public final BrdItem target_item;
   // The layer of the connection to the target item 
   public final int target_layer;
   // The array of backtrack doors from the destination to the start of a found connection of the maze search algorithm.
   protected final ArtBacktrackElement[] backtrack_array;
   protected final ArtControl art_ctrl;
   protected final TraceAngleRestriction angle_restriction;
   protected final ExpandDoorItem start_door;
   
   
   protected PlaPointFloat current_from_point;
   protected PlaPointFloat previous_from_point;
   protected int current_trace_layer;
   protected int current_from_door_index;
   protected int current_to_door_index;
   protected int current_target_door_index;
   protected ShapeTile current_target_shape;

   private boolean is_initialized = false;

   /**
    * Returns a new Instance or exception
    */
   public static ArtConnectionLocate get_instance(MazeSearchResult p_maze_search, ArtControl p_ctrl, ShapeSearchTree p_search_tree, TraceAngleRestriction p_angle_restriction, SortedSet<BrdItem> p_ripped_list )
      {
      if (p_angle_restriction.is_no_restriction() )
         return new ArtConnectionLocate_xx_Degree(p_maze_search, p_ctrl, p_search_tree, p_angle_restriction, p_ripped_list );
      else
         return new ArtConnectionLocate_45_Degree(p_maze_search, p_ctrl, p_search_tree, p_angle_restriction, p_ripped_list );
      }

   protected ArtConnectionLocate(MazeSearchResult p_maze_search_result, ArtControl p_ctrl, ShapeSearchTree p_search_tree, TraceAngleRestriction p_angle_restriction, SortedSet<BrdItem> p_ripped_list )
      {
      art_ctrl = p_ctrl;
      angle_restriction = p_angle_restriction;

      Collection<ArtBacktrackElement> backtrack_list = new_backtrack(p_maze_search_result, p_ripped_list);
      backtrack_array = new ArtBacktrackElement[backtrack_list.size()];
      
      Iterator<ArtBacktrackElement> it = backtrack_list.iterator();
      for (int i = 0; i < backtrack_array.length; ++i)
         {
         backtrack_array[i] = it.next();
         }

      ArtBacktrackElement start_info = backtrack_array[backtrack_array.length - 1];
      
      if (!(start_info.door instanceof ExpandDoorItem))
         {
         System.err.println("LocateFoundConnectionAlgo: ItemExpansionDoor expected for start_info.door");
         start_item = null;
         start_layer = 0;
         target_item = null;
         target_layer = 0;
         start_door = null;         
         return;
         }
      
      is_initialized = true;
      
      start_door = (ExpandDoorItem) start_info.door;
      start_item = start_door.item;
      start_layer = start_door.room.get_layer();
      current_from_door_index = 0;
      boolean at_fanout_end = false;
      
      if (p_maze_search_result.destination_door instanceof ExpandDoorItem)
         {
         ExpandDoorItem curr_destination_door = (ExpandDoorItem) p_maze_search_result.destination_door;
         target_item = curr_destination_door.item;
         target_layer = curr_destination_door.room.get_layer();
         current_from_point = calculate_starting_point(curr_destination_door, p_search_tree);
         }
      else if (p_maze_search_result.destination_door instanceof ExpandDrill)
         {
         // may happen only in case of fanout
         target_item = null;
         ExpandDrill curr_drill = (ExpandDrill) p_maze_search_result.destination_door;
         current_from_point = curr_drill.location.to_float();
         target_layer = curr_drill.first_layer_no + p_maze_search_result.section_no_of_door;
         at_fanout_end = true;
         }
      else
         {
         System.out.println("LocateFoundConnectionAlgo: unexpected type of destination_door");
         target_item = null;
         target_layer = 0;
         is_initialized = false;
         return;
         }
      
      current_trace_layer = target_layer;
      previous_from_point = current_from_point;

      boolean connection_done = false;
      
      while (!connection_done)
         {
         boolean layer_changed = false;
      
         if (at_fanout_end)
            {
            // do not increase current_target_door_index
            layer_changed = true;
            }
         else
            {
            current_target_door_index = current_from_door_index + 1;
            while (current_target_door_index < backtrack_array.length && !layer_changed)
               {
               if ( backtrack_array [current_target_door_index].door instanceof ExpandDrill)
                  {
                  layer_changed = true;
                  }
               else
                  {
                  ++current_target_door_index;
                  }
               }
            }
         if (layer_changed)
            {
            // the next trace leads to a via
            ExpandDrill current_target_drill = (ExpandDrill)  backtrack_array[ current_target_door_index].door;
            current_target_shape = new ShapeTileBox(current_target_drill.location);
            }
         else
            {
            // the next trace leads to the final target
            connection_done = true;
            current_target_door_index = backtrack_array.length - 1;
            ShapeTile target_shape = ((BrdConnectable) start_item).get_trace_connection_shape(p_search_tree, start_door.tree_entry_no);
            current_target_shape = target_shape.intersection(start_door.room.get_shape());

            if ( current_target_shape.dimension().is_area() )
               {
               // the target is a conduction area, make a save connection by shrinking the shape by the trace halfwidth.
               double trace_half_width = art_ctrl.compensated_trace_half_width[start_door.room.get_layer()];
               ShapeTile shrinked_shape = (ShapeTile) current_target_shape.offset(-trace_half_width);
               if (!shrinked_shape.is_empty())
                  {
                  current_target_shape = shrinked_shape;
                  }
               }
            }
         current_to_door_index = current_from_door_index + 1;
         ArtLocateResult next_trace = calculate_next_trace(layer_changed, at_fanout_end);
         at_fanout_end = false;
         connection_items.add(next_trace);
         }
      }

   
   public boolean is_initialized ()
      {
      return is_initialized;
      }
   
   /**
    * Calculates the next trace trace of the connection under construction. 
    * @return null, if all traces are returned.
    */
   private ArtLocateResult calculate_next_trace(boolean p_layer_changed, boolean p_at_fanout_end)
      {
      Collection<PlaPointFloat> corner_list = new LinkedList<PlaPointFloat>();
      corner_list.add(current_from_point);

      if (!p_at_fanout_end)
         {
         PlaPointFloat adjusted_start_corner = adjust_start_corner();
         if (adjusted_start_corner != current_from_point)
            {
            PlaPointFloat add_corner = calculate_additional_corner(current_from_point, adjusted_start_corner, true, angle_restriction);
            corner_list.add(add_corner);
            corner_list.add(adjusted_start_corner);
            previous_from_point = current_from_point;
            current_from_point = adjusted_start_corner;
            }
         }
      
      PlaPointFloat prev_corner = current_from_point;
      for (;;)
         {
         Collection<PlaPointFloat> next_corners = calculate_next_trace_corners();
         if (next_corners.isEmpty())
            {
            break;
            }
         Iterator<PlaPointFloat> it = next_corners.iterator();
         while (it.hasNext())
            {
            PlaPointFloat curr_next_corner = it.next();
            if (curr_next_corner != prev_corner)
               {
               corner_list.add(curr_next_corner);
               previous_from_point = current_from_point;
               current_from_point = curr_next_corner;
               prev_corner = curr_next_corner;
               }
            }
         }

      int next_layer = current_trace_layer;
      if (p_layer_changed)
         {
         current_from_door_index = current_target_door_index + 1;
         ExpandRoomComplete next_room = backtrack_array[current_from_door_index].next_room;
         if (next_room != null)
            {
            next_layer = next_room.get_layer();
            }
         }

      // Round the new trace corners to Integer.
      Collection<PlaPointInt> rounded_corner_list = new LinkedList<PlaPointInt>();
      Iterator<PlaPointFloat> it = corner_list.iterator();
      PlaPointInt prev_point = null;
      while (it.hasNext())
         {
         PlaPointInt curr_point = (it.next()).round();
         if (!curr_point.equals(prev_point))
            {
            rounded_corner_list.add(curr_point);
            prev_point = curr_point;
            }
         }

      // Construct the result item
      PlaPointInt[] corner_arr = new PlaPointInt[rounded_corner_list.size()];
      Iterator<PlaPointInt> it2 = rounded_corner_list.iterator();
      for (int i = 0; i < corner_arr.length; ++i)
         {
         corner_arr[i] = it2.next();
         }
      
      ArtLocateResult result = new ArtLocateResult(corner_arr, current_trace_layer);
      
      current_trace_layer = next_layer;
      
      return result;
      }

   /**
    * Returns the next list of corners for the construction of the trace in calculate_next_trace. If the result is emppty, the trace
    * is already completed.
    */
   protected abstract Collection<PlaPointFloat> calculate_next_trace_corners();

   /** 
    * Test display of the baktrack rooms
    */
   public void draw(java.awt.Graphics p_graphics, freert.graphics.GdiContext p_graphics_context)
      {
      for (int i = 0; i < backtrack_array.length; ++i)
         {
         ExpandRoomComplete next_room = backtrack_array[i].next_room;
         if (next_room != null)
            {
            next_room.draw(p_graphics, p_graphics_context, 0.2);
            }
         ExpandObject next_door = backtrack_array[i].door;
         if (next_door instanceof ExpandDrill)
            {
            ((ExpandDrill) next_door).draw(p_graphics, p_graphics_context, 0.2);
            }
         }
      }

   /**
    * Calculates the starting point of the next trace on p_from_door.item. 
    * The implementation is not yet optimal for starting points on traces or areas.
    */
   private PlaPointFloat calculate_starting_point(ExpandDoorItem p_from_door, ShapeSearchTree p_search_tree)
      {
      ShapeTile connection_shape = ((BrdConnectable) p_from_door.item).get_trace_connection_shape(p_search_tree, p_from_door.tree_entry_no);
      connection_shape = connection_shape.intersection(p_from_door.room.get_shape());
      return connection_shape.centre_of_gravity().round().to_float();
      }

   /**
    * Creates a list of doors by backtracking from p_destination_door to the start door. 
    * @throw  IllegalArgumentException if p_maze_search_result is null
    */
   private Collection<ArtBacktrackElement> new_backtrack(MazeSearchResult p_maze_search_result, SortedSet<BrdItem> p_ripped_item_list)
      {
      if (p_maze_search_result == null)
         throw new IllegalArgumentException ("p_maze_search_result is null");
      
      Collection<ArtBacktrackElement> result = new LinkedList<ArtBacktrackElement>();
      ExpandRoomComplete curr_next_room = null;
      ExpandObject curr_backtrack_door = p_maze_search_result.destination_door;
      MazeSearchElement curr_maze_search_element = curr_backtrack_door.get_maze_search_element(p_maze_search_result.section_no_of_door);
      if (curr_backtrack_door instanceof ExpandDoorItem)
         {
         curr_next_room = ((ExpandDoorItem) curr_backtrack_door).room;
         }
      else if (curr_backtrack_door instanceof ExpandDrill)
         {
         ExpandDrill curr_drill = (ExpandDrill) curr_backtrack_door;
         curr_next_room = curr_drill.room_arr[curr_drill.first_layer_no + p_maze_search_result.section_no_of_door];
         if (curr_maze_search_element.room_ripped)
            {
            for (ExpandRoomComplete tmp_room : curr_drill.room_arr)
               {
               if (tmp_room instanceof ExpandRoomObstacle)
                  {
                  p_ripped_item_list.add(((ExpandRoomObstacle) tmp_room).get_item());
                  }
               }
            }
         }
      ArtBacktrackElement curr_backtrack_element = new ArtBacktrackElement(curr_backtrack_door, p_maze_search_result.section_no_of_door, curr_next_room);
      for (;;)
         {
         result.add(curr_backtrack_element);
         curr_backtrack_door = curr_maze_search_element.backtrack_door;
         if (curr_backtrack_door == null)
            {
            break;
            }
         int curr_section_no = curr_maze_search_element.section_no_of_backtrack_door;
         if (curr_section_no >= curr_backtrack_door.maze_search_element_count())
            {
            System.out.println("LocateFoundConnectionAlgo: curr_section_no to big");
            curr_section_no = curr_backtrack_door.maze_search_element_count() - 1;
            }
         if (curr_backtrack_door instanceof ExpandDrill)
            {
            ExpandDrill curr_drill = (ExpandDrill) curr_backtrack_door;
            curr_next_room = curr_drill.room_arr[curr_section_no];
            }
         else
            {
            curr_next_room = curr_backtrack_door.other_room(curr_next_room);
            }
         curr_maze_search_element = curr_backtrack_door.get_maze_search_element(curr_section_no);
         curr_backtrack_element = new ArtBacktrackElement(curr_backtrack_door, curr_section_no, curr_next_room);
         if (curr_maze_search_element.room_ripped)
            {
            if (curr_next_room instanceof ExpandRoomObstacle)
               {
               p_ripped_item_list.add(((ExpandRoomObstacle) curr_next_room).get_item());
               }
            }
         }
      return result;
      }

   /**
    * Adjusts the start corner, so that a trace starting at this corner is completely contained in the start room.
    */
   private final PlaPointFloat adjust_start_corner()
      {
      if (current_from_door_index < 0)
         {
         return current_from_point;
         }
      
      ArtBacktrackElement curr_from_info = backtrack_array[current_from_door_index];
      
      if (curr_from_info.next_room == null)
         {
         return current_from_point;
         }
      
      double trace_half_width = art_ctrl.compensated_trace_half_width[current_trace_layer];
      ShapeTile shrinked_room_shape = (ShapeTile) curr_from_info.next_room.get_shape().offset(-trace_half_width);
      if (shrinked_room_shape.is_empty() || shrinked_room_shape.contains(current_from_point))
         {
         return current_from_point;
         }
      
      return shrinked_room_shape.nearest_point_approx(current_from_point).round().to_float();
      }

   private static PlaPointFloat ninety_degree_corner(PlaPointFloat p_from_point, PlaPointFloat p_to_point, boolean p_horizontal_first)
      {
      double x;
      double y;
      if (p_horizontal_first)
         {
         x = p_to_point.v_x;
         y = p_from_point.v_y;
         }
      else
         {
         x = p_from_point.v_x;
         y = p_to_point.v_y;
         }
      return new PlaPointFloat(x, y);
      }

   private static PlaPointFloat fortyfive_degree_corner(PlaPointFloat p_from_point, PlaPointFloat p_to_point, boolean p_horizontal_first)
      {
      double abs_dx = Math.abs(p_to_point.v_x - p_from_point.v_x);
      double abs_dy = Math.abs(p_to_point.v_y - p_from_point.v_y);
      double x;
      double y;

      if (abs_dx <= abs_dy)
         {
         if (p_horizontal_first)
            {
            x = p_to_point.v_x;
            if (p_to_point.v_y >= p_from_point.v_y)
               {
               y = p_from_point.v_y + abs_dx;
               }
            else
               {
               y = p_from_point.v_y - abs_dx;
               }
            }
         else
            {
            x = p_from_point.v_x;
            if (p_to_point.v_y > p_from_point.v_y)
               {
               y = p_to_point.v_y - abs_dx;
               }
            else
               {
               y = p_to_point.v_y + abs_dx;
               }
            }
         }
      else
         {
         if (p_horizontal_first)
            {
            y = p_from_point.v_y;
            if (p_to_point.v_x > p_from_point.v_x)
               {
               x = p_to_point.v_x - abs_dy;
               }
            else
               {
               x = p_to_point.v_x + abs_dy;
               }
            }
         else
            {
            y = p_to_point.v_y;
            if (p_to_point.v_x > p_from_point.v_x)
               {
               x = p_from_point.v_x + abs_dy;
               }
            else
               {
               x = p_from_point.v_x - abs_dy;
               }
            }
         }
      return new PlaPointFloat(x, y);
      }

   /**
    * Calculates an additional corner, so that for the lines from p_from_point to the result corner and from the result corner to
    * p_to_point p_angle_restriction is fulfilled.
    */
   static PlaPointFloat calculate_additional_corner(PlaPointFloat p_from_point, PlaPointFloat p_to_point, boolean p_horizontal_first, TraceAngleRestriction p_angle_restriction)
      {
      if (p_angle_restriction == TraceAngleRestriction.NINETY_DEGREE)
         return  ninety_degree_corner(p_from_point, p_to_point, p_horizontal_first);
      else if (p_angle_restriction == TraceAngleRestriction.FORTYFIVE_DEGREE)
         return fortyfive_degree_corner(p_from_point, p_to_point, p_horizontal_first);
      else
         return p_to_point;
      }
   }
