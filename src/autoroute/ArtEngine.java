/*
 *  Copyright (C) 2014  Alfons Wirtz
 *  website www.freerouting.net
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
 * AutorouteEngine.java
 *
 * Created on 11. Januar 2004, 11:14
 */
package autoroute;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandDoorItem;
import autoroute.expand.ExpandRoom;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import autoroute.expand.ExpandRoomObstacle;
import autoroute.maze.MazeSearch;
import autoroute.maze.MazeSearchResult;
import autoroute.sorted.SortedRooms_xx_Degree;
import autoroute.varie.ArtResult;
import board.RoutingBoard;
import board.items.BrdItem;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeObject;
import board.varie.BrdStopConnection;
import board.varie.IdGenerator;
import freert.planar.PlaDimension;
import freert.planar.PlaLineInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileSimplex;
import freert.varie.ThreadStoppable;
import freert.varie.UndoableObjectNode;

/**
 * Temporary autoroute data stored on the RoutingBoard.
 * @author Alfons Wirtz
 */
public final class ArtEngine
   {
   private static final String classname="ArtEngine.";
   public static final int TRACE_WIDTH_TOLERANCE = 2;
   
   // The PCB-board of this autoroute algorithm
   public final RoutingBoard r_board;
   // The current search tree used in autoroute. It depends on the trace clearance class used in the autoroute algorithm.
   public final ShapeSearchTree autoroute_search_tree;
   // The net number used for routing in this autoroute algorithm.
   private final int route_net_no;
   // The 2-dimensional array of rectangular pages of ExpansionDrills
   public final DrillPageArray drill_page_array;
   // To be able to stop the expansion algorithm.
   final ThreadStoppable stoppable;
   // The list of incomplete expansion rooms on the routing board
   private final List<ExpandRoomFreespaceIncomplete> incomplete_expansion_rooms = new LinkedList<ExpandRoomFreespaceIncomplete>();
   // The list of complete expansion rooms on the routing board
   private final List<ExpandRoomFreespaceComplete> complete_expansion_rooms = new LinkedList<ExpandRoomFreespaceComplete>();;
   // The count of expansion rooms created so far
   private IdGenerator expansion_room_instance_count = new IdGenerator();

   public ArtEngine(RoutingBoard p_board, int p_net_no, int p_trace_clearance_class_no, ThreadStoppable p_stoppable )
      {
      r_board = p_board;
      route_net_no = p_net_no;
      stoppable = p_stoppable;

      autoroute_search_tree = r_board.search_tree_manager.get_autoroute_tree(p_trace_clearance_class_no);

      double max_drill_page_width = 5 * r_board.brd_rules.get_default_via_diameter();
      max_drill_page_width = Math.max(max_drill_page_width, 10000);

      drill_page_array = new DrillPageArray(r_board, max_drill_page_width);
      }

   /*
    * Autoroute a connection between p_start_set and p_dest_set. Returns ALREADY_CONNECTED, ROUTED, NOT_ROUTED, or INSERT_ERROR.
    */
   public ArtResult autoroute_connection(Set<BrdItem> p_start_set, Set<BrdItem> p_dest_set, ArtControl p_ctrl, SortedSet<BrdItem> p_ripped_item_list)
      {
      MazeSearch maze_search = new MazeSearch ( this, p_ctrl, p_start_set, p_dest_set);

      if ( ! maze_search.is_initialized() )
         {
         r_board.userPrintln(classname+"autoroute_connection: MazeSearchAlgo not initialized");
         return ArtResult.EXCEPTION;
         }

      MazeSearchResult search_result = maze_search.find_connection();
      if ( search_result == null )
         {
         r_board.userPrintln(classname+"autoroute_connection: search_result NULL");
         return ArtResult.NOT_ROUTED;
         }

      ArtConnectionLocate locate_connection = ArtConnectionLocate.get_instance(search_result, p_ctrl, autoroute_search_tree, r_board.brd_rules.get_trace_snap_angle(), p_ripped_item_list);
      if ( ! locate_connection.is_initialized() )
         {
         r_board.userPrintln(classname+"autoroute_connection: ! is_initialized");
         return ArtResult.NOT_ROUTED;
         }

      autoroute_clear();

      // Delete the ripped connections.
      SortedSet<BrdItem> ripped_connections = new TreeSet<BrdItem>();
      Set<Integer> changed_nets = new TreeSet<Integer>();
      BrdStopConnection stop_connection_option;

      if (p_ctrl.stop_remove_fanout_vias)
         {
         stop_connection_option = BrdStopConnection.NONE;
         }
      else
         {
         stop_connection_option = BrdStopConnection.FANOUT_VIA;
         }

      for (BrdItem curr_ripped_item : p_ripped_item_list)
         {
         ripped_connections.addAll(curr_ripped_item.get_connection_items(stop_connection_option));
         for (int i = 0; i < curr_ripped_item.net_count(); ++i)
            {
            changed_nets.add(curr_ripped_item.get_net_no(i));
            }
         }
      
      // let the observers know the changes in the board database.
      boolean observers_activated = !r_board.observers_active();

      if (observers_activated) r_board.start_notify_observers();

      r_board.remove_items_unfixed(ripped_connections);

      for (int curr_net_no : changed_nets)
         {
         r_board.remove_trace_tails(curr_net_no, stop_connection_option);
         }
      
      ArtConnectionInsert insert_algo = new ArtConnectionInsert( r_board, p_ctrl);
      
      boolean inserted = insert_algo.insert(locate_connection);
      
      if (observers_activated) r_board.end_notify_observers();
      
      return inserted ? ArtResult.ROUTED : ArtResult.INSERT_ERROR;
      }

   /**
    * Returns the net number of the current connection to route.
    */
   public int get_net_no()
      {
      return route_net_no;
      }

   /**
    * Returns autoroute needs to stop
    */
   public boolean is_stop_requested()
      {
      if ( stoppable == null)  return false;
      
      return stoppable.is_stop_requested();
      }

   private void autoroute_clear_items()
      {
      Iterator<UndoableObjectNode> iter = r_board.item_list.start_read_object();

      for (;;)
         {
         BrdItem curr_item = (BrdItem) r_board.item_list.read_object(iter);

         if (curr_item == null) break;

         curr_item.art_item_clear();
         }
      }
   
   /**
    * Clears all temporary data
    */
   public void autoroute_clear()
      {
      for (ExpandRoomFreespaceComplete curr_room : complete_expansion_rooms)
         curr_room.remove_from_tree(autoroute_search_tree);
      
      complete_expansion_rooms.clear();
      
      incomplete_expansion_rooms.clear();
      
      expansion_room_instance_count.clear();
      
      autoroute_clear_items();
      }

   /**
    * Draws the shapes of the expansion rooms created so far.
    */
   public void draw(java.awt.Graphics p_graphics, graphics.GdiContext p_graphics_context, double p_intensity)
      {
      for (ExpandRoomFreespaceComplete curr_room : complete_expansion_rooms)
         curr_room.draw(p_graphics, p_graphics_context, p_intensity);

      Collection<BrdItem> item_list = r_board.get_items();
     
      for (BrdItem curr_item : item_list)
         {
         ArtItem autoroute_info = curr_item.art_item_get();
         
         if (autoroute_info == null) continue;

         autoroute_info.draw(p_graphics, p_graphics_context, p_intensity);
         }

      // this.drill_page_array.draw(p_graphics, p_graphics_context, p_intensity);
      }

   /**
    * Creates a new FreeSpaceExpansionRoom and adds it to the room list. 
    * Its shape is normally unbounded at construction time of the room. 
    * The final (completed) shape will be a subshape of the start shape, which does not overlap with any obstacle, and it is
    * as big as possible. p_contained_points will remain contained in the shape, after it is completed.
    */
   public ExpandRoomFreespaceIncomplete add_incomplete_expansion_room(ShapeTile p_shape, int p_layer, ShapeTile p_contained_shape)
      {
      ExpandRoomFreespaceIncomplete new_room = new ExpandRoomFreespaceIncomplete(p_shape, p_layer, p_contained_shape);

      incomplete_expansion_rooms.add(new_room);
      
      return new_room;
      }

   /**
    * Returns the first element in the list of incomplete expansion rooms or null, if the list is empty.
    */
   public ExpandRoomFreespaceIncomplete get_first_incomplete_expansion_room()
      {
      if (incomplete_expansion_rooms.isEmpty()) return null;

      Iterator<ExpandRoomFreespaceIncomplete> it = incomplete_expansion_rooms.iterator();

      return it.next();
      }

   /**
    * Removes an incomplete room from the database.
    */
   private void remove_incomplete_expansion_room(ExpandRoomFreespaceIncomplete p_room)
      {
      if ( p_room == null ) return;
      
      remove_all_doors(p_room);
      
      incomplete_expansion_rooms.remove(p_room);
      }

   /**
    * Removes a complete expansion room from the database and creates new incomplete expansion rooms for the neighbours.
    */
   public void remove_complete_expansion_room(ExpandRoomFreespaceComplete p_room)
      {
      if ( p_room == null ) return;
      
      // create new incomplete expansion rooms for all neighbors
      ShapeTile room_shape = p_room.get_shape();
      int room_layer = p_room.get_layer();
      Collection<ExpandDoor> room_doors = p_room.get_doors();
      
      for (ExpandDoor curr_door : room_doors)
         {
         ExpandRoom curr_neighbour = curr_door.other_room(p_room);
      
         if (curr_neighbour == null) continue;
         
         curr_neighbour.remove_door(curr_door);

         ShapeTile neighbour_shape = curr_neighbour.get_shape();
         
         ShapeTile intersection = room_shape.intersection(neighbour_shape);
         
         if (intersection.dimension() != PlaDimension.LINE) continue;

         // add a new incomplete room to curr_neighbour.
         int[] touching_sides = room_shape.touching_sides(neighbour_shape);
         PlaLineInt[] line_arr = new PlaLineInt[1];
         line_arr[0] = neighbour_shape.border_line(touching_sides[1]).opposite();
         ShapeTileSimplex new_incomplete_room_shape = ShapeTileSimplex.get_instance(line_arr);
         ExpandRoomFreespaceIncomplete new_incomplete_room = add_incomplete_expansion_room(new_incomplete_room_shape, room_layer, intersection);

         ExpandDoor new_door = new ExpandDoor(curr_neighbour, new_incomplete_room, PlaDimension.LINE);
         
         curr_neighbour.add_door(new_door);
         new_incomplete_room.add_door(new_door);
         }

      remove_all_doors(p_room);
      
      p_room.remove_from_tree(autoroute_search_tree);
      
      complete_expansion_rooms.remove(p_room);
      
      drill_page_array.invalidate(room_shape);
      }

   /**
    * Completes the shape of p_room. 
    * Returns the resulting rooms after completing the shape. p_room will no more exist after this function.
    */
   public Collection<ExpandRoomFreespaceComplete> complete_expansion_room(ExpandRoomFreespaceIncomplete p_room)
      {
      try
         {
         Collection<ExpandRoomFreespaceComplete> result = new LinkedList<ExpandRoomFreespaceComplete>();
         ShapeTile from_door_shape = null;
         ShapeTreeObject ignore_object = null;
         Collection<ExpandDoor> room_doors = p_room.get_doors();
         for (ExpandDoor curr_door : room_doors)
            {
            ExpandRoom other_room = curr_door.other_room(p_room);
            if (other_room instanceof ExpandRoomFreespaceComplete && curr_door.dimension.is_area() )
               {
               from_door_shape = curr_door.get_shape();
               ignore_object = (ExpandRoomFreespaceComplete) other_room;
               break;
               }
            }
         Collection<ExpandRoomFreespaceIncomplete> completed_shapes = autoroute_search_tree.complete_shape(p_room, route_net_no, ignore_object, from_door_shape);
         
         remove_incomplete_expansion_room(p_room);
         
         Iterator<ExpandRoomFreespaceIncomplete> it = completed_shapes.iterator();
         boolean is_first_completed_room = true;
         while (it.hasNext())
            {
            ExpandRoomFreespaceIncomplete curr_incomplete_room = it.next();
         
            if (curr_incomplete_room.get_shape().dimension() != PlaDimension.AREA )  continue;

            if (is_first_completed_room)
               {
               is_first_completed_room = false;
               ExpandRoomFreespaceComplete completed_room = add_complete_room(curr_incomplete_room);
               if (completed_room != null)
                  {
                  result.add(completed_room);
                  }
               }
            else
               {
               // the shape of the first completed room may have changed and may
               // intersect now with the other shapes. Therefore the completed shapes
               // have to be recalculated.
               Collection<ExpandRoomFreespaceIncomplete> curr_completed_shapes = autoroute_search_tree.complete_shape(curr_incomplete_room, route_net_no, ignore_object, from_door_shape);
               Iterator<ExpandRoomFreespaceIncomplete> it2 = curr_completed_shapes.iterator();
               while (it2.hasNext())
                  {
                  ExpandRoomFreespaceIncomplete tmp_room = it2.next();
                  ExpandRoomFreespaceComplete completed_room = this.add_complete_room(tmp_room);
                  if (completed_room != null)
                     {
                     result.add(completed_room);
                     }
                  }
               }
            }
         return result;
         }
      catch (Exception e)
         {
         System.out.print("AutorouteEngine.complete_expansion_room: ");
         System.out.println(e);
         return new LinkedList<ExpandRoomFreespaceComplete>();
         }
      }

   /**
    * Calculates the doors and adds the completed room to the room database.
    */
   private ExpandRoomFreespaceComplete add_complete_room(ExpandRoomFreespaceIncomplete p_room)
      {
      ExpandRoomFreespaceComplete completed_room = (ExpandRoomFreespaceComplete) calculate_doors(p_room);
      
      if ( completed_room == null ) return null;

      if ( completed_room.get_shape().dimension() != PlaDimension.AREA) return null;
      
      complete_expansion_rooms.add(completed_room);
      
      autoroute_search_tree.insert(completed_room);
      
      return completed_room;
      }

   /**
    * Calculates the neighbours of p_room and inserts doors to the new created neighbour rooms. 
    * The shape of the result room may be different to the shape of p_room
    */
   private ExpandRoomComplete calculate_doors(ExpandRoom p_room)
      {
/*      
      if (autoroute_search_tree instanceof ShapeSearchTree90Degree)
         return SortedRooms_90_Degree.calculate(p_room, this);
      else if (autoroute_search_tree instanceof ShapeSearchTree45Degree)
         return SortedRooms_45_Degree.calculate(p_room, this);
      else
*/      
         return SortedRooms_xx_Degree.calculate(p_room, this);
      }

   /**
    * Completes the shapes of the neigbour rooms of p_room, so that the doors of p_room will not change later on.
    */
   public void complete_neigbour_rooms(ExpandRoomComplete p_room)
      {
      if (p_room.get_doors() == null) return;

      Iterator<ExpandDoor> iter = p_room.get_doors().iterator();
      while (iter.hasNext())
         {
         ExpandDoor curr_door = iter.next();
         // cast to ExpansionRoom becaus ExpansionDoor.other_room works differently with
         // parameter type CompleteExpansionRoom.
         ExpandRoom neighbour_room = curr_door.other_room((ExpandRoom) p_room);

         if (neighbour_room == null) continue;
         
         if (neighbour_room instanceof ExpandRoomFreespaceIncomplete)
            {
            complete_expansion_room((ExpandRoomFreespaceIncomplete) neighbour_room);
            // restart reading because the doors have changed
            iter = p_room.get_doors().iterator();
            }
         else if (neighbour_room instanceof ExpandRoomObstacle)
            {
            ExpandRoomObstacle obstacle_neighbour_room = (ExpandRoomObstacle) neighbour_room;
            if (!obstacle_neighbour_room.all_doors_calculated())
               {
               calculate_doors(obstacle_neighbour_room);
               obstacle_neighbour_room.set_doors_calculated(true);
               }
            }
         }
      }

   /**
    * Invalidates all drill pages intersecting with p_shape, so the they must be recalculated at the next call of get_ddrills()
    */
   public void invalidate_drill_pages(ShapeTile p_shape)
      {
      this.drill_page_array.invalidate(p_shape);
      }

   /**
    * Removes all doors from p_room
    */
   public void remove_all_doors(ExpandRoom p_room)
      {
      Iterator<ExpandDoor> it = p_room.get_doors().iterator();
      while (it.hasNext())
         {
         ExpandDoor curr_door = it.next();

         ExpandRoom other_room = curr_door.other_room(p_room);
         if (other_room == null) continue;
         
         other_room.remove_door(curr_door);
      
         if (other_room instanceof ExpandRoomFreespaceIncomplete)
            {
            remove_incomplete_expansion_room((ExpandRoomFreespaceIncomplete) other_room);
            }
         }

      p_room.clear_doors();
      }

   /**
    * Returns all complete free space expansion rooms with a target door to an item in the set p_items.
    */
   Set<ExpandRoomFreespaceComplete> get_rooms_with_target_items(Set<BrdItem> p_items)
      {
      Set<ExpandRoomFreespaceComplete> result = new TreeSet<ExpandRoomFreespaceComplete>();
      if (this.complete_expansion_rooms != null)
         {
         for (ExpandRoomFreespaceComplete curr_room : this.complete_expansion_rooms)
            {
            Collection<ExpandDoorItem> target_door_list = curr_room.get_target_doors();
            for (ExpandDoorItem curr_target_door : target_door_list)
               {
               BrdItem curr_target_item = curr_target_door.item;
               if (p_items.contains(curr_target_item))
                  {
                  result.add(curr_room);
                  }
               }
            }
         }
      return result;
      }

   /**
    * Checks, if the internal datastructure is valid.
    * @return true if they are valid
    */
   public boolean validate()
      {
      if (complete_expansion_rooms == null) return true;

      boolean result = true;

      for (ExpandRoomFreespaceComplete curr_room : complete_expansion_rooms)
         {
         if (!curr_room.validate(this))
            {
            result = false;
            }
         }
      return result;
      }

   public int new_room_id_no()
      {
      return expansion_room_instance_count.new_no();
      }

   }
