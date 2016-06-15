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
 * MazeSearchAlgo.java
 *
 * Created on 25. Januar 2004, 13:24
 */
package autoroute.maze;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import autoroute.ArtConnection;
import autoroute.ArtControl;
import autoroute.ArtEngine;
import autoroute.ArtItem;
import autoroute.expand.ExpandDestinationDistance;
import autoroute.expand.ExpandDoor;
import autoroute.expand.ExpandDoorItem;
import autoroute.expand.ExpandDrill;
import autoroute.expand.ExpandDrillPage;
import autoroute.expand.ExpandObject;
import autoroute.expand.ExpandRoomComplete;
import autoroute.expand.ExpandRoomFreespace;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import autoroute.expand.ExpandRoomObstacle;
import autoroute.varie.ArtViaMask;
import board.BrdConnectable;
import board.RoutingBoard;
import board.awtree.AwtreeObject;
import board.awtree.AwtreeShapeSearch;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdItem;
import board.items.BrdTracep;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import board.varie.ShoveDrillResult;
import board.varie.TraceAngleRestriction;
import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.planar.PlaLineInt;
import freert.planar.PlaLineIntAlist;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;
import freert.planar.PlaSegmentFloat;
import freert.planar.Polyline;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.varie.NetNosList;

/**
 * Class for autorouting an incomplete connection via a maze search algorithm.
 * @author Alfons Wirtz
 */
public final class MazeSearch
   {
   private static final String classname="MazeSearch.";
   
   private static final int ALREADY_RIPPED_COSTS = 1;

   private final java.util.Random random_generator = new java.util.Random();
   
   // The autoroute engine of this expansion algorithm.
   private final ArtEngine art_engine;
   // for simplicity I also store the routing board
   private final RoutingBoard r_board;
   // Control parameters for the engine
   private final ArtControl art_ctrl;
   // The search tree for expanding. It is the tree compensated for the current net.
   private final AwtreeShapeSearch search_tree;
   // The queue of of expanded elements used in this search algorithm.
   private final SortedSet<MazeListElement> maze_expansion_list = new TreeSet<MazeListElement>();
   // Used for calculating of a good lower bound for the distance between a new MazeExpansionElement and the destination set of the expansion.
   private final ExpandDestinationDistance destination_distance;
   // The destination door found by the expanding algorithm.
   private ExpandObject destination_door = null;
   
   private int section_no_of_destination_door = 0;
   
   private boolean is_initialized;  // true when the maze is initialized correctly
   
   private final MazeShoveTraceAlgo maze_shove_trace;
   
   /**
    * Initializes a new instance of MazeSearchAlgo for searching a connection between p_start_items and p_destination_items. 
    * new instance, initialization may fail due to incomplete params
    */
   public MazeSearch(ArtEngine p_art_engine, ArtControl p_ctrl, Set<BrdItem> p_start_items, Set<BrdItem> p_destination_items )
      {
      art_engine = p_art_engine;
      r_board    = art_engine.r_board;
      art_ctrl   = p_ctrl;
      random_generator.setSeed(p_ctrl.ripup_costs); // To get reproducible random numbers in the ripup algorithm.
      search_tree = p_art_engine.art_search_tree;
      destination_distance = new ExpandDestinationDistance(art_ctrl.trace_costs, art_ctrl.layer_active, art_ctrl.min_normal_via_cost, art_ctrl.min_cheap_via_cost);
      maze_shove_trace = new MazeShoveTraceAlgo(r_board, art_ctrl);

      is_initialized = false; // assume not initialized
      
      reduce_trace_shapes_at_tie_pins(p_start_items, art_ctrl.net_no,  search_tree);
      reduce_trace_shapes_at_tie_pins(p_destination_items, art_ctrl.net_no, search_tree);

      for ( BrdItem curr_item : p_destination_items )
         {
         ArtItem curr_info = curr_item.art_item_get();

         curr_info.set_start_info(false);
         
         for (int index = 0; index < curr_item.tree_shape_count(search_tree); ++index)
            {
            ShapeTile curr_tree_shape = curr_item.get_tree_shape(search_tree, index);
            
            if (curr_tree_shape != null)
               {
               destination_distance.join(curr_tree_shape.bounding_box(), curr_item.shape_layer(index));
               }
            }
         }

      if ( art_ctrl.is_fanout)
         {
         // destination set is not needed for fanout
         ShapeTileBox board_bounding_box = r_board.bounding_box;
         destination_distance.join(board_bounding_box, 0);
         destination_distance.join(board_bounding_box, art_ctrl.layer_count - 1);
         }
      
      // process the start items
      LinkedList<ExpandRoomFreespaceIncomplete> start_rooms = new LinkedList<ExpandRoomFreespaceIncomplete>();

      for ( BrdItem curr_item : p_start_items )
         {
         ArtItem curr_info = curr_item.art_item_get();

         curr_info.set_start_info(true);
         
         if ( ! (curr_item instanceof BrdConnectable)) continue;
         
         BrdConnectable a_conn = (BrdConnectable)curr_item;

         for (int index = 0; index < curr_item.tree_shape_count(search_tree); ++index)
            {
            ShapeTile contained_shape = a_conn.get_trace_connection_shape(search_tree, index);
            
            ExpandRoomFreespaceIncomplete new_start_room = art_engine.add_incomplete_expansion_room(null, curr_item.shape_layer(index), contained_shape);

            start_rooms.add(new_start_room);
            }
         }

      // complete the start rooms
      LinkedList<ExpandRoomFreespaceComplete> completed_start_rooms = new LinkedList<ExpandRoomFreespaceComplete>();

      for (ExpandRoomFreespaceIncomplete curr_room : start_rooms)
         {
         Collection<ExpandRoomFreespaceComplete> curr_completed_rooms = art_engine.complete_expansion_room(curr_room);
         
         completed_start_rooms.addAll(curr_completed_rooms);
         }

      // Put the ItemExpansionDoors of the completed start rooms into the maze_expansion_list.

      for (ExpandRoomFreespaceComplete curr_room : completed_start_rooms)
         {
         for ( ExpandDoorItem curr_door : curr_room.get_target_doors() )
            {
            if (curr_door.is_destination_door())  continue;
            
            ShapeTile connection_shape = ((BrdConnectable)curr_door.item).get_trace_connection_shape(search_tree, curr_door.tree_entry_no);
            
            connection_shape = connection_shape.intersection(curr_door.room.get_shape());
            
            PlaPointFloat curr_center = connection_shape.centre_of_gravity();
            
            PlaSegmentFloat shape_entry = new PlaSegmentFloat(curr_center, curr_center);
            
            double sorting_value = destination_distance.calculate(curr_center, curr_room.get_layer());
            
            MazeListElement new_list_element = new MazeListElement(curr_door, 0, null, 0, 0, sorting_value, curr_room, shape_entry, false, MazeAdjustment.NONE, false);
            
            maze_expansion_list.add(new_list_element);
            
            is_initialized = true;
            }
         }
      }
   
   
   public boolean is_initialized()
      {
      return is_initialized;
      }
   

   /**
    * Does a maze search to find a connection route between the start and the destination items.
    * If the algorithm succeeds, the ExpansionDoor and its section number of the found destination is returned, from where the whole found connection can be
    * backtracked. Otherwise the return value will be null.
    */
   public MazeSearchResult find_connection()
      {
      while (occupy_next_element())
         {
         }
      
      if ( destination_door == null) return null;
      
      return new MazeSearchResult( destination_door,  section_no_of_destination_door);
      }

   /**
    * Expands the next element in the maze expansion list. 
    * @return false, if the expansion list is exhausted or the destination is reached.
    */
   public boolean occupy_next_element()
      {
      if ( destination_door != null) return false; // destination already reached
      
      MazeListElement list_element = null;
      MazeSearchElement curr_door_section = null;
      
      // Search the next element, which is not yet expanded.
      boolean next_element_found = false;
      
      while ( ! maze_expansion_list.isEmpty())
         {
         if (art_engine.is_stop_requested()) return false;
         
         Iterator<MazeListElement> iter = maze_expansion_list.iterator();
         
         list_element = iter.next();

         int curr_section_no = list_element.section_no_of_door;
        
         curr_door_section = list_element.door.get_maze_search_element(curr_section_no);
         
         iter.remove();
         
         if ( ! curr_door_section.is_occupied )
            {
            next_element_found = true;
            break;
            }
         }
      
      if ( ! next_element_found) return false;

      curr_door_section.backtrack_door = list_element.backtrack_door;
      curr_door_section.section_no_of_backtrack_door = list_element.section_no_of_backtrack_door;
      curr_door_section.room_ripped = list_element.room_ripped;
      curr_door_section.adjustment = list_element.adjustment;

      if (list_element.door instanceof ExpandDrillPage)
         {
         expand_to_drills_of_page(list_element);
         return true;
         }

      if (list_element.door instanceof ExpandDoorItem)
         {
         ExpandDoorItem curr_door = (ExpandDoorItem) list_element.door;
         if (curr_door.is_destination_door())
            {
            // The destination is reached.
            destination_door = curr_door;
            section_no_of_destination_door = list_element.section_no_of_door;
            return false;
            }
         }

      if (art_ctrl.is_fanout && list_element.door instanceof ExpandDrill && list_element.backtrack_door instanceof ExpandDrill)
         {
         // algorithm completed after the first drill;
         destination_door = list_element.door;
         section_no_of_destination_door = list_element.section_no_of_door;
         return false;
         }

      if (art_ctrl.vias_allowed && list_element.door instanceof ExpandDrill && !(list_element.backtrack_door instanceof ExpandDrill))
         {
         expand_to_other_layers(list_element);
         }

      if (list_element.next_room != null)
         {
         if (!expand_to_room_doors(list_element))
            {
            return true; // occupation by ripup is delayed or nothing was expanded
            // In case nothing was expanded allow the section to be occupied from
            // somewhere else, if the next room is thin.
            }
         }
      
      curr_door_section.is_occupied = true;
      
      return true;
      }

   /**
    * Expands the other door section of the room. 
    * Returns true, if the from door section has to be occupied, and false, if the occupation for is delayed.
    */
   private boolean expand_to_room_doors(MazeListElement p_element)
      {
      // Complete the neigbour rooms to make shure, that the doors of this room will not change later on.
      int layer_no = p_element.next_room.get_layer();

      boolean layer_active = art_ctrl.layer_active[layer_no];

      if (!layer_active)
         {
         if (r_board.layer_structure.is_signal(layer_no))
            {
            return true;
            }
         }

      double half_width = art_ctrl.compensated_trace_half_width[layer_no];
      boolean curr_door_is_small = false;
      if (p_element.door instanceof ExpandDoor)
         {
         double half_width_add = half_width + ArtEngine.TRACE_WIDTH_TOLERANCE;
         ExpandDoor curr_door = (ExpandDoor) p_element.door;
         if (art_ctrl.with_neckdown)
            {
            // try evtl. neckdown at a destination pin
            double neck_down_half_width = check_neck_down_at_dest_pin(p_element.next_room);
            if (neck_down_half_width > 0)
               {
               half_width_add = Math.min(half_width_add, neck_down_half_width);
               half_width = half_width_add;
               }
            }
         curr_door_is_small = door_is_small(curr_door, 2 * half_width_add);
         }

      art_engine.complete_neigbour_rooms(p_element.next_room);

      PlaPointFloat shape_entry_middle = p_element.shape_entry.point_a.middle_point(p_element.shape_entry.point_b);

      if (art_ctrl.with_neckdown && p_element.door instanceof ExpandDoorItem)
         {
         // try evtl. neckdown at a start pin
         BrdItem start_item = ((ExpandDoorItem) p_element.door).item;
         if (start_item instanceof board.items.BrdAbitPin)
            {
            double neckdown_half_width = ((board.items.BrdAbitPin) start_item).get_trace_neckdown_halfwidth(layer_no);
            if (neckdown_half_width > 0)
               {
               half_width = Math.min(half_width, neckdown_half_width);
               }
            }

         }

      boolean next_room_is_thick = true;
      if (p_element.next_room instanceof ExpandRoomObstacle)
         {
         next_room_is_thick = room_shape_is_thick((ExpandRoomObstacle) p_element.next_room);
         }
      else
         {
         ShapeTile next_room_shape = p_element.next_room.get_shape();
         if (next_room_shape.min_width() < 2 * half_width)
            {
            next_room_is_thick = false; // to prevent probles with the opposite side
            }
         else if (!p_element.already_checked && p_element.door.get_dimension().is_line() && !curr_door_is_small)
            {
            // The algorithm below works only, if p_location is on the border of p_room_shape.
            // That is only the case for 1 dimensional doors.
            // For small doors the check is done in check_leaving_via below.

            PlaPointFloat[] nearest_points = next_room_shape.nearest_border_points_approx(shape_entry_middle, 2);
            if (nearest_points.length < 2)
               {
               System.out.println("MazeSearchAlgo.expand_to_room_doors: nearest_points.length == 2 expected");
               next_room_is_thick = false;
               }
            else
               {
               double curr_dist = nearest_points[1].distance(shape_entry_middle);
               next_room_is_thick = (curr_dist > half_width + 1);
               }
            }
         }
      if (!layer_active && p_element.door instanceof ExpandDrill)
         {
         // check for drill to a foreign conduction area on split plane.
         PlaPointInt drill_location = ((ExpandDrill) p_element.door).location;
         ItemSelectionFilter filter = new ItemSelectionFilter(ItemSelectionChoice.CONDUCTION);
         Set<BrdItem> picked_items = r_board.pick_items(drill_location, layer_no, filter);

         for (BrdItem curr_item : picked_items)
            {
            if (!curr_item.contains_net(art_ctrl.net_no)) return true;
            }
         
         }
      boolean something_expanded = false;
      if (expand_to_target_doors(p_element, next_room_is_thick, curr_door_is_small, shape_entry_middle))
         {
         something_expanded = true;
         }

      if (!layer_active)
         {
         return true;
         }

      int ripup_costs = 0;

      if (p_element.next_room instanceof ExpandRoomFreespace)
         {
         if (!p_element.already_checked)
            {
            if (curr_door_is_small)
               {
               boolean enter_through_small_door = false;
               if (next_room_is_thick)
                  {
                  // check to enter the thick room from a ripped item through a small door (after ripup)
                  enter_through_small_door = check_leaving_ripped_item(p_element);
                  }
               if (!enter_through_small_door)
                  {
                  return something_expanded;
                  }
               }

            }
         }
      else if (p_element.next_room instanceof ExpandRoomObstacle)
         {
         ExpandRoomObstacle obstacle_room = (ExpandRoomObstacle) p_element.next_room;

         if (!p_element.already_checked)
            {
            boolean room_rippable = false;
            if ( art_ctrl.ripup_allowed)
               {
               ripup_costs = check_ripup(p_element, obstacle_room.get_item(), curr_door_is_small);
               room_rippable = (ripup_costs >= 0);
               }

            if (ripup_costs != ALREADY_RIPPED_COSTS && next_room_is_thick)
               {
               BrdItem obstacle_item = obstacle_room.get_item();
               if (!curr_door_is_small && art_ctrl.max_shove_trace_recursion_depth > 0 && obstacle_item instanceof board.items.BrdTracep)
                  {
                  if (!shove_trace_room(p_element, obstacle_room))
                     {
                     if (ripup_costs > 0)
                        {
                        // delay the occupation by ripup to allow shoving the room by another door sections.
                        MazeListElement new_element = new MazeListElement(p_element.door, p_element.section_no_of_door, p_element.backtrack_door,
                              p_element.section_no_of_backtrack_door, p_element.expansion_value + ripup_costs, p_element.sorting_value + ripup_costs, p_element.next_room,
                              p_element.shape_entry, true, p_element.adjustment, true);
                        maze_expansion_list.add(new_element);
                        }
                     return something_expanded;
                     }
                  }
               }
            if (!room_rippable)
               {
               return true;
               }
            }
         }

      for (ExpandDoor to_door : p_element.next_room.get_doors())
         {
         if (to_door == p_element.door)
            {
            continue;
            }
         if (expand_to_door(to_door, p_element, ripup_costs, next_room_is_thick, MazeAdjustment.NONE))
            {
            something_expanded = true;
            }
         }

      // Expand also the drill pages intersecting the room.
      if (art_ctrl.vias_allowed && !(p_element.door instanceof ExpandDrill))
         {
         if ((something_expanded || next_room_is_thick) && p_element.next_room instanceof ExpandRoomFreespaceComplete)
            {
            // avoid setting something_expanded to true when next_room is thin to allow occupying by different sections of the door
            Collection<ExpandDrillPage> overlapping_drill_pages = art_engine.drill_page_array.overlapping_pages(p_element.next_room.get_shape());
               {
               for (ExpandDrillPage to_drill_page : overlapping_drill_pages)
                  {
                  expand_to_drill_page(to_drill_page, p_element);
                  something_expanded = true;
                  }
               }
            }
         else if (p_element.next_room instanceof ExpandRoomObstacle)
            {
            BrdItem curr_obstacle_item = ((ExpandRoomObstacle) p_element.next_room).get_item();
            if (curr_obstacle_item instanceof board.items.BrdAbitVia)
               {
               board.items.BrdAbitVia curr_via = (board.items.BrdAbitVia) curr_obstacle_item;
               ExpandDrill via_drill_info = curr_via.get_autoroute_drill_info(art_engine.art_search_tree);
               expand_to_drill(via_drill_info, p_element, ripup_costs);
               }
            }
         }

      return something_expanded;
      }

   /** Expand the target doors of the room. 
    * Returns true, if at leat 1 target door was expanded 
    */
   private boolean expand_to_target_doors(MazeListElement p_list_element, boolean p_next_room_is_thick, boolean p_curr_door_is_small, PlaPointFloat p_shape_entry_middle)
      {
      if (p_curr_door_is_small)
         {
         boolean enter_through_small_door = false;
         if (p_list_element.door instanceof ExpandDoor)
            {
            ExpandRoomComplete from_room = ((ExpandDoor) p_list_element.door).other_room(p_list_element.next_room);
            if (from_room instanceof ExpandRoomObstacle)
               {
               // otherwise entering through the small door may fail, because it was not checked.
               enter_through_small_door = true;
               }
            }
      
         if ( ! enter_through_small_door ) return false;
         }
    
      boolean result = false;
      
      for (ExpandDoorItem to_door : p_list_element.next_room.get_target_doors())
         {
         if (to_door == p_list_element.door) continue;

         ShapeTile target_shape = ((BrdConnectable) to_door.item).get_trace_connection_shape(art_engine.art_search_tree, to_door.tree_entry_no);

         PlaPointFloat connection_point = target_shape.nearest_point_approx(p_shape_entry_middle);
         
         if (!p_next_room_is_thick)
            {
            // check the line from p_shape_entry_middle to the nearest point.
            NetNosList curr_net_no_arr =new NetNosList(art_ctrl.net_no);
            
            int curr_layer = p_list_element.next_room.get_layer();
            
            PlaPointIntAlist check_points = new PlaPointIntAlist(2);
            check_points.add( p_shape_entry_middle.round() );
            check_points.add( connection_point.round() );

            if ( check_points.get(0).equals(check_points.get(1)) ) continue;
            
            Polyline check_polyline = new Polyline(check_points);

            boolean check_ok = r_board.check_trace(
                  check_polyline, 
                  art_ctrl.trace_half_width[curr_layer], 
                  curr_layer, 
                  curr_net_no_arr, 
                  art_ctrl.trace_clearance_idx,
                  art_ctrl.max_shove_trace_recursion_depth, 
                  art_ctrl.max_shove_via_recursion_depth, 
                  art_ctrl.max_spring_over_recursion_depth);
               
            if ( ! check_ok)  continue;
            }

         // Wondering if this should be done in any case even if previous fails ..... TODO
         PlaSegmentFloat new_shape_entry = new PlaSegmentFloat(connection_point, connection_point);

         if (expand_to_door_section(to_door, 0, new_shape_entry, p_list_element, 0, MazeAdjustment.NONE)) result = true;
         }
      
      return result;
      }

   /**
    * Return true, if at least 1 door ection was expanded.
    */
   private boolean expand_to_door(ExpandDoor p_to_door, MazeListElement p_list_element, int p_add_costs, boolean p_next_room_is_thick, MazeAdjustment p_adjustment)
      {
      double half_width = art_ctrl.compensated_trace_half_width[p_list_element.next_room.get_layer()];
      boolean something_expanded = false;
      PlaSegmentFloat[] line_sections = p_to_door.get_section_segments(half_width);

      for (int i = 0; i < line_sections.length; ++i)
         {
         MazeSearchElement mz_el = p_to_door.get_maze_search_element(i);

         if (mz_el.is_occupied)
            {
            continue;
            }
         
         PlaSegmentFloat new_shape_entry;
         if (p_next_room_is_thick)
            {
            new_shape_entry = line_sections[i];
            if (p_to_door.dimension.is_line() && line_sections.length == 1 && p_to_door.first_room instanceof ExpandRoomFreespaceComplete
                  && p_to_door.second_room instanceof ExpandRoomFreespaceComplete)
               {
               // check entering the p_to_door at an acute corner of the shape of p_list_element.next_room
               PlaPointFloat shape_entry_middle = new_shape_entry.point_a.middle_point(new_shape_entry.point_b);
               ShapeTile room_shape = p_list_element.next_room.get_shape();
               if (room_shape.min_width() < 2 * half_width)
                  {
                  return false;
                  }
               PlaPointFloat[] nearest_points = room_shape.nearest_border_points_approx(shape_entry_middle, 2);
               if (nearest_points.length < 2 || nearest_points[1].distance(shape_entry_middle) <= half_width + 1)
                  {
                  return false;
                  }
               }
            }
         else
            {
            // expand only doors on the opposite side of the room from the shape_entry.
            if (p_to_door.dimension.is_line() && i == 0 && line_sections[0].point_b.dustance_square(line_sections[0].point_a) < 1)
               {
               // p_to_door is small belonging to a via or thin room
               continue;
               }
            new_shape_entry = segment_projection(p_list_element.shape_entry, line_sections[i]);
            if (new_shape_entry == null)
               {
               continue;
               }
            }

         if (expand_to_door_section(p_to_door, i, new_shape_entry, p_list_element, p_add_costs, p_adjustment))
            {
            something_expanded = true;
            }
         }
      return something_expanded;
      }

   /**
    * Checks, if the width p_door is big enough for a trace with width p_trace_width.
    */
   private boolean door_is_small(ExpandDoor p_door, double p_trace_width)
      {
      if ( ! p_door.dimension.is_line() ) return false;
      
      if ( ! ( p_door.first_room instanceof ExpandRoomFreespaceComplete && p_door.second_room instanceof ExpandRoomFreespaceComplete ) ) return false;

      ShapeTile door_shape = p_door.get_shape();
      
      if (door_shape.is_empty())
         {
         if (r_board.debug(Mdbg.MAZE, Ldbg.DEBUG))
            System.out.println(classname+"door_is_small door_shape is empty");

         return true;
         }

      double door_length;
      
      TraceAngleRestriction angle_restriction = r_board.brd_rules.get_trace_snap_angle();
      
      if (angle_restriction.is_limit_45() )
         {
         ShapeTileOctagon door_oct = door_shape.bounding_octagon();
         door_length = door_oct.max_width();
         }
      else
         {
         PlaSegmentFloat door_line_segment = door_shape.diagonal_corner_segment();
         door_length = door_line_segment.point_b.distance(door_line_segment.point_a);
         }

      return door_length < p_trace_width;
      }

   /**
    * Return true, if the door section was successfully expanded.
    */
   private boolean expand_to_door_section(ExpandObject p_door, int p_section_no, PlaSegmentFloat p_shape_entry, MazeListElement p_from_element, int p_add_costs, MazeAdjustment p_adjustment)
      {
      if (p_door.get_maze_search_element(p_section_no).is_occupied || p_shape_entry == null)
         {
         return false;
         }
      ExpandRoomComplete next_room = p_door.other_room(p_from_element.next_room);
      int layer = p_from_element.next_room.get_layer();
      PlaPointFloat shape_entry_middle = p_shape_entry.point_a.middle_point(p_shape_entry.point_b);
      double expansion_value = p_from_element.expansion_value + p_add_costs
            + shape_entry_middle.distance_weighted(p_from_element.shape_entry.point_a.middle_point(p_from_element.shape_entry.point_b), art_ctrl.trace_costs[layer].horizontal, art_ctrl.trace_costs[layer].vertical);
      double sorting_value = expansion_value + destination_distance.calculate(shape_entry_middle, layer);
      boolean room_ripped = p_add_costs > 0 && p_adjustment == MazeAdjustment.NONE || p_from_element.already_checked && p_from_element.room_ripped;

      MazeListElement new_element = new MazeListElement(p_door, p_section_no, p_from_element.door, p_from_element.section_no_of_door, expansion_value, sorting_value, next_room, p_shape_entry,
            room_ripped, p_adjustment, false);
      maze_expansion_list.add(new_element);
      return true;
      }

   private void expand_to_drill(ExpandDrill p_drill, MazeListElement p_from_element, int p_add_costs)
      {
      int layer = p_from_element.next_room.get_layer();
      int trace_half_width = art_ctrl.compensated_trace_half_width[layer];
      boolean room_shape_is_thin = p_from_element.next_room.get_shape().min_width() < 2 * trace_half_width;

      if (room_shape_is_thin)
         {
         // expand only drills intersecting the backtrack door
         if (p_from_element.backtrack_door == null || !p_drill.get_shape().intersects(p_from_element.backtrack_door.get_shape()))
            {
            return;
            }
         }

      double via_radius = art_ctrl.via_radius_arr[layer];
      ShapeConvex shrinked_drill_shape = p_drill.get_shape().shrink(via_radius);
      PlaPointFloat compare_corner = p_from_element.shape_entry.point_a.middle_point(p_from_element.shape_entry.point_b);
      if (p_from_element.door instanceof ExpandDrillPage && p_from_element.backtrack_door instanceof ExpandDoorItem)
         {
         // If expansion comes from a pin with trace exit directions the eapansion_value is calculated
         // from the nearest trace exit point instead from the center olf the pin.
         BrdItem from_item = ((ExpandDoorItem) p_from_element.backtrack_door).item;
         if (from_item instanceof BrdAbitPin)
            {
            PlaPointFloat nearest_exit_corner = ((BrdAbitPin) from_item).nearest_trace_exit_corner(p_drill.location.to_float(), trace_half_width, layer);
            if (nearest_exit_corner != null)
               {
               compare_corner = nearest_exit_corner;
               }
            }
         }
      PlaPointFloat nearest_point = shrinked_drill_shape.nearest_point_approx(compare_corner);
      PlaSegmentFloat shape_entry = new PlaSegmentFloat(nearest_point, nearest_point);
      int section_no = layer - p_drill.first_layer_no;
      double expansion_value = p_from_element.expansion_value + p_add_costs + nearest_point.distance_weighted(compare_corner, art_ctrl.trace_costs[layer].horizontal, art_ctrl.trace_costs[layer].vertical);
      ExpandObject new_backtrack_door;
      int new_section_no_of_backtrack_door;
      if (p_from_element.door instanceof ExpandDrillPage)
         {
         new_backtrack_door = p_from_element.backtrack_door;
         new_section_no_of_backtrack_door = p_from_element.section_no_of_backtrack_door;
         }
      else
         {
         // Expanded directly through already existing via
         // The step expand_to_drill_page is skipped
         new_backtrack_door = p_from_element.door;
         new_section_no_of_backtrack_door = p_from_element.section_no_of_door;
         expansion_value += art_ctrl.min_normal_via_cost;
         }
      double sorting_value = expansion_value + destination_distance.calculate(nearest_point, layer);
      MazeListElement new_element = new MazeListElement(p_drill, section_no, new_backtrack_door, new_section_no_of_backtrack_door, expansion_value, sorting_value, null, shape_entry,
            p_from_element.room_ripped, MazeAdjustment.NONE, false);
      maze_expansion_list.add(new_element);
      }

   /**
    * A drill page is inserted between an expansion roomm and the drill to expand in order to prevent performance problems with
    * rooms with big shapes containing many drills.
    */
   private void expand_to_drill_page(ExpandDrillPage p_drill_page, MazeListElement p_from_element)
      {

      int layer = p_from_element.next_room.get_layer();
      PlaPointFloat from_element_shape_entry_middle = p_from_element.shape_entry.point_a.middle_point(p_from_element.shape_entry.point_b);
      PlaPointFloat nearest_point = p_drill_page.page_shape.nearest_point(from_element_shape_entry_middle);
      double expansion_value = p_from_element.expansion_value + art_ctrl.min_normal_via_cost;
      double sorting_value = expansion_value + nearest_point.distance_weighted(from_element_shape_entry_middle, art_ctrl.trace_costs[layer].horizontal, art_ctrl.trace_costs[layer].vertical)
            + destination_distance.calculate(nearest_point, layer);
      MazeListElement new_element = new MazeListElement(p_drill_page, layer, p_from_element.door, p_from_element.section_no_of_door, expansion_value, sorting_value, p_from_element.next_room,
            p_from_element.shape_entry, p_from_element.room_ripped, MazeAdjustment.NONE, false);
      maze_expansion_list.add(new_element);
      }

   private void expand_to_drills_of_page(MazeListElement p_from_element)
      {
      int from_room_layer = p_from_element.section_no_of_door;
      ExpandDrillPage drill_page = (ExpandDrillPage) p_from_element.door;
      Collection<ExpandDrill> drill_list = drill_page.get_drills(art_engine, art_ctrl.attach_smd_allowed);
      for (ExpandDrill curr_drill : drill_list)
         {
         int section_no = from_room_layer - curr_drill.first_layer_no;
         if (section_no < 0 || section_no >= curr_drill.room_arr.length)
            {
            continue;
            }
         if (curr_drill.room_arr[section_no] == p_from_element.next_room && !curr_drill.get_maze_search_element(section_no).is_occupied)
            {
            expand_to_drill(curr_drill, p_from_element, 0);
            }
         }
      }

   /**
    * Tries to expand other layers by inserting a via.
    */
   private void expand_to_other_layers(MazeListElement p_list_element)
      {
      int via_lower_bound = 0;
      int via_upper_bound = -1;
      ExpandDrill curr_drill = (ExpandDrill) p_list_element.door;
      int from_layer = curr_drill.first_layer_no + p_list_element.section_no_of_door;
      boolean smd_attached_on_component_side = false;
      boolean smd_attached_on_solder_side = false;
      boolean room_ripped;
      if (curr_drill.room_arr[p_list_element.section_no_of_door] instanceof ExpandRoomObstacle)
         {
         // check ripup of an existing via
         if (! art_ctrl.ripup_allowed)
            {
            return;
            }
         BrdItem curr_obstacle_item = ((ExpandRoomObstacle) curr_drill.room_arr[p_list_element.section_no_of_door]).get_item();
         if (!(curr_obstacle_item instanceof board.items.BrdAbitVia))
            {
            return;
            }
         freert.library.LibPadstack curr_obstacle_padstack = ((board.items.BrdAbitVia) curr_obstacle_item).get_padstack();
         if (! art_ctrl.via_rule.contains_padstack(curr_obstacle_padstack) || curr_obstacle_item.clearance_idx() != art_ctrl.via_clearance_idx)
            {
            return;
            }
         via_lower_bound = curr_obstacle_padstack.from_layer();
         via_upper_bound = curr_obstacle_padstack.to_layer();
         room_ripped = true;
         }
      else
         {
         NetNosList net_no_arr = new NetNosList(art_ctrl.net_no);

         room_ripped = false;
         int via_lower_limit = Math.max(curr_drill.first_layer_no, art_ctrl.via_lower_bound);
         int via_upper_limit = Math.min(curr_drill.last_layer_no, art_ctrl.via_upper_bound);
         // Calculate the lower bound of possible vias.
         int curr_layer = from_layer;
         for (;;)
            {
            ShapeTile curr_room_shape = curr_drill.room_arr[curr_layer - curr_drill.first_layer_no].get_shape();
            
            ShoveDrillResult drill_result = r_board.shove_via_algo.check_layer(
                  art_ctrl.via_radius_arr[curr_layer], 
                  art_ctrl.via_clearance_idx, 
                  art_ctrl.attach_smd_allowed, 
                  curr_room_shape,
                  curr_drill.location, 
                  curr_layer, 
                  net_no_arr, 
                  art_ctrl.max_shove_trace_recursion_depth, 
                  0);

            if (drill_result == ShoveDrillResult.NOT_DRILLABLE)
               {
               via_lower_bound = curr_layer + 1;
               break;
               }
            else if (drill_result == ShoveDrillResult.DRILLABLE_WITH_ATTACH_SMD)
               {
               if (curr_layer == 0)
                  {
                  smd_attached_on_component_side = true;
                  }
               else if (curr_layer == art_ctrl.layer_count - 1)
                  {
                  smd_attached_on_solder_side = true;
                  }
               }
            if (curr_layer <= via_lower_limit)
               {
               via_lower_bound = via_lower_limit;
               break;
               }
            --curr_layer;
            }
         if (via_lower_bound > curr_drill.first_layer_no)
            {
            return;
            }
         curr_layer = from_layer + 1;
         for (;;)
            {
            if (curr_layer > via_upper_limit)
               {
               via_upper_bound = via_upper_limit;
               break;
               }
            ShapeTile curr_room_shape = curr_drill.room_arr[curr_layer - curr_drill.first_layer_no].get_shape();
            ShoveDrillResult drill_result = r_board.shove_via_algo.check_layer(
                  art_ctrl.via_radius_arr[curr_layer], 
                  art_ctrl.via_clearance_idx, 
                  art_ctrl.attach_smd_allowed, 
                  curr_room_shape,
                  curr_drill.location, 
                  curr_layer, net_no_arr, 
                  art_ctrl.max_shove_trace_recursion_depth, 
                  0);

            if (drill_result == ShoveDrillResult.NOT_DRILLABLE)
               {
               via_upper_bound = curr_layer - 1;
               break;
               }
            else if (drill_result == ShoveDrillResult.DRILLABLE_WITH_ATTACH_SMD)
               {
               if (curr_layer == art_ctrl.layer_count - 1)
                  {
                  smd_attached_on_solder_side = true;
                  }
               }
            ++curr_layer;
            }
         if (via_upper_bound < curr_drill.last_layer_no)
            {
            return;
            }
         }

      for (int to_layer = via_lower_bound; to_layer <= via_upper_bound; ++to_layer)
         {
         if (to_layer == from_layer) continue;

         // check, there there is a fitting via mask.
         int curr_first_layer;
         int curr_last_layer;
         if (to_layer < from_layer)
            {
            curr_first_layer = to_layer;
            curr_last_layer = from_layer;
            }
         else
            {
            curr_first_layer = from_layer;
            curr_last_layer = to_layer;
            }
         boolean mask_found = false;
         for (int i = 0; i < art_ctrl.via_info_arr.length; ++i)
            {
            ArtViaMask curr_via_info = art_ctrl.via_info_arr[i];
            if (curr_first_layer >= curr_via_info.from_layer && curr_last_layer <= curr_via_info.to_layer && curr_via_info.from_layer >= via_lower_bound && curr_via_info.to_layer <= via_upper_bound)
               {
               boolean mask_ok = true;
               if (curr_via_info.from_layer == 0 && smd_attached_on_component_side || curr_via_info.to_layer == art_ctrl.layer_count - 1 && smd_attached_on_solder_side)
                  {
                  mask_ok = curr_via_info.attach_smd_allowed;
                  }
               if (mask_ok)
                  {
                  mask_found = true;
                  break;
                  }
               }
            }

         if (!mask_found) continue;

         MazeSearchElement curr_drill_layer_info = curr_drill.get_maze_search_element(to_layer - curr_drill.first_layer_no);

         if (curr_drill_layer_info.is_occupied) continue;

         double expansion_value = p_list_element.expansion_value + art_ctrl.add_via_costs[from_layer].to_layer[to_layer];
         PlaPointFloat shape_entry_middle = p_list_element.shape_entry.point_a.middle_point(p_list_element.shape_entry.point_b);
         double sorting_value = expansion_value + destination_distance.calculate(shape_entry_middle, to_layer);
         int curr_room_index = to_layer - curr_drill.first_layer_no;
         
         MazeListElement new_element = new MazeListElement(
               curr_drill, 
               curr_room_index, 
               curr_drill, 
               p_list_element.section_no_of_door, 
               expansion_value, sorting_value,
               curr_drill.room_arr[curr_room_index], 
               p_list_element.shape_entry, 
               room_ripped, 
               MazeAdjustment.NONE, false);

         maze_expansion_list.add(new_element);
         }
      }

   /**
    * Looks for pins with more than 1 nets and reduces shapes of traces of foreign nets, which are already connected to such a pin,
    * so that the pin center is not blocked for connection.
    */
   private void reduce_trace_shapes_at_tie_pins(Collection<BrdItem> p_item_list, int p_own_net_no, AwtreeShapeSearch p_autoroute_tree)
      {
      for (BrdItem curr_item : p_item_list)
         {
         if ( ! (curr_item instanceof BrdAbitPin) ) continue;

         BrdAbitPin curr_tie_pin = (BrdAbitPin) curr_item;

         if (  curr_tie_pin.net_count() <= 1) continue;
         
         Collection<BrdItem> pin_contacts = curr_item.get_normal_contacts();
         
         for (BrdItem curr_contact : pin_contacts)
            {
            if (!(curr_contact instanceof BrdTracep) ) continue;
            
            if ( curr_contact.contains_net(p_own_net_no) ) continue;
            
            p_autoroute_tree.reduce_trace_shape_at_tie_pin(curr_tie_pin, (BrdTracep) curr_contact);
            }
         }
      }

   private boolean room_shape_is_thick(ExpandRoomObstacle p_obstacle_room)
      {
      BrdItem obstacle_item = p_obstacle_room.get_item();
      int layer = p_obstacle_room.get_layer();
      double obstacle_half_width;
      if (obstacle_item instanceof BrdTracep)
         {
         obstacle_half_width = ((BrdTracep) obstacle_item).get_half_width() + search_tree.get_clearance_compensation(obstacle_item.clearance_idx(), layer);
         }
      else if (obstacle_item instanceof BrdAbitVia)
         {
         ShapeTile via_shape = ((board.items.BrdAbitVia) obstacle_item).get_tree_shape_on_layer(search_tree, layer);
         obstacle_half_width = 0.5 * via_shape.max_width();
         }
      else
         {
         System.out.println("MazeSearchAlgo. room_shape_is_thick: unexpected obstacle item");
         obstacle_half_width = 0;
         }
      
      return obstacle_half_width >= art_ctrl.compensated_trace_half_width[layer];
      }

   /**
    * Checks, if the room can be ripped and returns the rip up costs, which are > 0, if the room is ripped and -1, if no ripup was
    * possible. If the previous room was also ripped and contained the same item or an item of the same connection, the result will
    * be equal to ALREADY_RIPPED_COSTS
    */
   private int check_ripup(MazeListElement p_list_element, BrdItem p_obstacle_item, boolean p_door_is_small)
      {
      if (!p_obstacle_item.is_route())
         {
         return -1;
         }
      if (p_door_is_small)
         {
         // allow entering a via or trace, if its corresponding border segment is smaller than the current trace width

         if (!enter_through_small_door(p_list_element, p_obstacle_item))
            {
            return -1;
            }

         }
      
      ExpandRoomComplete previous_room = p_list_element.door.other_room(p_list_element.next_room);
      boolean room_was_shoved = p_list_element.adjustment != MazeAdjustment.NONE;
      BrdItem previous_item = null;
      if (previous_room != null && previous_room instanceof ExpandRoomObstacle)
         {
         previous_item = ((ExpandRoomObstacle) previous_room).get_item();
         }
      if (room_was_shoved)
         {
         if (previous_item != null && previous_item != p_obstacle_item && previous_item.shares_net(p_obstacle_item))
            {
            // The ripped trace may start at a fork.
            return -1;
            }
         }
      else if (previous_item == p_obstacle_item)
         {
         return ALREADY_RIPPED_COSTS;
         }

      double fanout_via_cost_factor = 1.0;
      double cost_factor = 1;
      if (p_obstacle_item instanceof BrdTracep)
         {
         BrdTracep obstacle_trace = (BrdTracep) p_obstacle_item;
         cost_factor = obstacle_trace.get_half_width();
         if (!art_ctrl.stop_remove_fanout_vias)
            {
            // protect traces between SMD-pins and fanout vias
            fanout_via_cost_factor = calc_fanout_via_ripup_cost_factor(obstacle_trace);
            }
         }
      else if (p_obstacle_item instanceof board.items.BrdAbitVia)
         {
         boolean look_if_fanout_via = !art_ctrl.stop_remove_fanout_vias;
         Collection<BrdItem> contact_list = p_obstacle_item.get_normal_contacts();
         int contact_count = 0;
         for (BrdItem curr_contact : contact_list)
            {
            if (!(curr_contact instanceof BrdTracep) || curr_contact.is_user_fixed())
               {
               return -1;
               }
            ++contact_count;
            BrdTracep obstacle_trace = (BrdTracep) curr_contact;
            cost_factor = Math.max(cost_factor, obstacle_trace.get_half_width());
            if (look_if_fanout_via)
               {
               double curr_fanout_via_cost_factor = calc_fanout_via_ripup_cost_factor(obstacle_trace);
               if (curr_fanout_via_cost_factor > 1)
                  {
                  fanout_via_cost_factor = curr_fanout_via_cost_factor;
                  look_if_fanout_via = false;
                  }
               }
            }
         if (fanout_via_cost_factor <= 1)
            {
            // not a fanout via
            cost_factor *= 0.5 * Math.max(contact_count - 1, 0);
            }
         }

      double ripup_cost = art_ctrl.ripup_costs * cost_factor;
      double detour = 1;
      
      if (fanout_via_cost_factor <= 1) // p_obstacle_item does not belong to a fanout
         {
         ArtConnection obstacle_connection = ArtConnection.get(p_obstacle_item);
         if (obstacle_connection != null)
            {
            detour = obstacle_connection.get_detour();
            }
         }
      
      boolean randomize = art_ctrl.ripup_pass_no >= 4 && art_ctrl.ripup_pass_no % 3 != 0;
      if (randomize)
         {
         // shuffle the result to avoid repetitive loops
         double random_number = random_generator.nextDouble();
         double random_factor = 0.5 + random_number * random_number;
         detour *= random_factor;
         }
      ripup_cost /= detour;

      ripup_cost *= fanout_via_cost_factor;
      int result = Math.max((int) ripup_cost, 1);
      final int MAX_RIPUP_COSTS = Integer.MAX_VALUE / 100;
      return Math.min(result, MAX_RIPUP_COSTS);
      }

   /**
    * Return the additional cost factor for ripping the trace, if it is connected to a fanout via or 1, if no fanout via was found.
    */
   private static double calc_fanout_via_ripup_cost_factor(BrdTracep p_trace)
      {
      final double FANOUT_COST_CONST = 20000;
      Collection<BrdItem> curr_end_contacts;
      for (int i = 0; i < 2; ++i)
         {
         if (i == 0)
            {
            curr_end_contacts = p_trace.get_start_contacts();
            }
         else
            {
            curr_end_contacts = p_trace.get_end_contacts();
            }
         if (curr_end_contacts.size() != 1)
            {
            continue;
            }
         BrdItem curr_trace_contact = curr_end_contacts.iterator().next();
         boolean protect_fanout_via = false;
         if (curr_trace_contact instanceof board.items.BrdAbitPin && curr_trace_contact.first_layer() == curr_trace_contact.last_layer())
            {
            protect_fanout_via = true;
            }
         else if (curr_trace_contact instanceof BrdTracep && curr_trace_contact.get_fixed_state() == ItemFixState.SHOVE_FIXED)
            {
            // look for shove fixed exit traces of SMD-pins
            BrdTracep contact_trace = (BrdTracep) curr_trace_contact;
            if (contact_trace.corner_count() == 2)
               {
               protect_fanout_via = true;
               }
            }

         if (protect_fanout_via)
            {
            double fanout_via_cost_factor = p_trace.get_half_width() / p_trace.get_length();
            fanout_via_cost_factor *= fanout_via_cost_factor;
            fanout_via_cost_factor *= FANOUT_COST_CONST;
            fanout_via_cost_factor = Math.max(fanout_via_cost_factor, 1);
            return fanout_via_cost_factor;
            }
         }
      return 1;
      }

   /**
    * Shoves a trace room and expands the corresponding doors. 
    * Return false, if no door was expanded. In this case occupation of the
    * door_section by ripup can be delayed to allow shoving the room from a different door section
    */
   private boolean shove_trace_room(MazeListElement p_element, ExpandRoomObstacle p_obstacle_room)
      {
      if (p_element.section_no_of_door != 0 && p_element.section_no_of_door != p_element.door.maze_search_element_count() - 1)
         {
         // No delay of occupation necessary because inner sections of a door are currently not shoved.
         return true;
         }
      
      boolean result = false;
      
      if (p_element.adjustment != MazeAdjustment.RIGHT)
         {
         Collection<MazeDoorSection> left_to_door_section_list = new LinkedList<MazeDoorSection>();

         if (maze_shove_trace.check_shove_trace_line(p_element, p_obstacle_room,  false, left_to_door_section_list))
            {
            result = true;
            }

         for (MazeDoorSection curr_left_door_section : left_to_door_section_list)
            {
            MazeAdjustment curr_adjustment;
            if (curr_left_door_section.door.dimension.is_area() )
               {
               // the door is the link door to the next room
               curr_adjustment = MazeAdjustment.LEFT;
               }
            else
               {
               curr_adjustment = MazeAdjustment.NONE;
               }

            expand_to_door_section(curr_left_door_section.door, curr_left_door_section.section_no, curr_left_door_section.section_line, p_element, 0, curr_adjustment);
            }
         }

      if (p_element.adjustment != MazeAdjustment.LEFT)
         {
         Collection<MazeDoorSection> right_to_door_section_list = new java.util.LinkedList<MazeDoorSection>();

         if (maze_shove_trace.check_shove_trace_line(p_element, p_obstacle_room, true, right_to_door_section_list))
            {
            result = true;
            }
         for (MazeDoorSection curr_right_door_section : right_to_door_section_list)
            {
            MazeAdjustment curr_adjustment;
            if (curr_right_door_section.door.dimension.is_area() )
               {
               // the door is the link door to the next room
               curr_adjustment = MazeAdjustment.RIGHT;
               }
            else
               {
               curr_adjustment = MazeAdjustment.NONE;
               }
            expand_to_door_section(curr_right_door_section.door, curr_right_door_section.section_no, curr_right_door_section.section_line, p_element, 0, curr_adjustment);
            }
         }
      return result;
      }

   /**
    * Checks, if the next roomm contains a destination pin, where evtl. neckdown is necessary. Return the neck down width in this
    * case, or 0, if no such pin waas found,
    */
   private double check_neck_down_at_dest_pin(ExpandRoomComplete p_room)
      {
      Collection<ExpandDoorItem> target_doors = p_room.get_target_doors();
      for (ExpandDoorItem curr_target_door : target_doors)
         {
         if (curr_target_door.item instanceof board.items.BrdAbitPin)
            {
            return ((board.items.BrdAbitPin) curr_target_door.item).get_trace_neckdown_halfwidth(p_room.get_layer());
            }
         }
      return 0;
      }

   /**
    * Returns the perpendicular projection of p_from_segment onto p_to_segment. Returns null, if the projection is empty.
    */
   private PlaSegmentFloat segment_projection(PlaSegmentFloat p_from_segment, PlaSegmentFloat p_to_segment)
      {
      PlaSegmentFloat check_segment = p_from_segment.adjust_direction(p_to_segment);
      PlaSegmentFloat first_projection = p_to_segment.segment_projection(check_segment);
      PlaSegmentFloat second_projection = p_to_segment.segment_projection_2(check_segment);
      PlaSegmentFloat result;
      if (first_projection != null && second_projection != null)
         {
         PlaPointFloat result_a;
         if (first_projection.point_a == p_to_segment.point_a || second_projection.point_a == p_to_segment.point_a)
            {
            result_a = p_to_segment.point_a;
            }
         else if (first_projection.point_a.dustance_square(p_to_segment.point_a) <= second_projection.point_a.dustance_square(p_to_segment.point_a))
            {
            result_a = first_projection.point_a;
            }
         else
            {
            result_a = second_projection.point_a;
            }
         PlaPointFloat result_b;
         if (first_projection.point_b == p_to_segment.point_b || second_projection.point_b == p_to_segment.point_b)
            {
            result_b = p_to_segment.point_b;
            }
         else if (first_projection.point_b.dustance_square(p_to_segment.point_b) <= second_projection.point_b.dustance_square(p_to_segment.point_b))
            {
            result_b = first_projection.point_b;
            }
         else
            {
            result_b = second_projection.point_b;
            }
         result = new PlaSegmentFloat(result_a, result_b);
         }
      else if (first_projection != null)
         {
         result = first_projection;
         }
      else
         {
         result = second_projection;
         }
      return result;
      }

   /**
    * Checks, if the next room can be entered if the door of p_list_element is small. 
    * If p_ignore_item != null, p_ignore_item and
    * all other items directly connected to p_ignore_item are ignored in the check.
    */
   private boolean enter_through_small_door(MazeListElement p_list_element, BrdItem p_ignore_item)
      {
      if ( p_list_element.door.get_dimension().is_area() ) return false;

      ShapeTile door_shape = p_list_element.door.get_shape();

      // Get the line of the 1 dimensional door.
      PlaLineInt door_line = null;
      PlaPointFloat prev_corner = door_shape.corner_approx(0);
      int corner_count = door_shape.border_line_count();
      for (int i = 1; i < corner_count; ++i)
         {
         // skip lines of lenghth 0
         PlaPointFloat next_corner = door_shape.corner_approx(i);
         if (next_corner.dustance_square(prev_corner) > 1)
            {
            door_line = door_shape.border_line(i - 1);
            break;
            }
         prev_corner = next_corner;
         }
      if (door_line == null)
         {
         return false;
         }

      PlaPointInt door_center = door_shape.centre_of_gravity().round();
      int curr_layer = p_list_element.next_room.get_layer();
      int check_radius = art_ctrl.compensated_trace_half_width[curr_layer] + ArtEngine.TRACE_WIDTH_TOLERANCE;
      
      // create a perpendicular line segment of length 2 * check_radius through the door center
      
      PlaLineIntAlist line_arr = new PlaLineIntAlist(3);
      
      line_arr.add( door_line.translate(check_radius));
      line_arr.add( new PlaLineInt(door_center, door_line.direction().rotate_45_deg(2)) );
      line_arr.add( door_line.translate(-check_radius) );

      Polyline check_polyline = new Polyline(line_arr);
      
      ShapeTile check_shape = check_polyline.offset_shape(check_radius, 0);

      NetNosList ignore_net_nos = new NetNosList(art_ctrl.net_no);

      Set<AwtreeObject> overlapping_objects = art_engine.art_search_tree.find_overlap_objects(check_shape, curr_layer, ignore_net_nos );

      for (AwtreeObject curr_object : overlapping_objects)
         {
         if (!(curr_object instanceof BrdItem) || curr_object == p_ignore_item)
            {
            continue;
            }
         
         BrdItem curr_item = (BrdItem) curr_object;
         if (!curr_item.shares_net(p_ignore_item))
            {
            return false;
            }
         Set<BrdItem> curr_contacts = curr_item.get_normal_contacts();
         if (!curr_contacts.contains(p_ignore_item))
            {
            return false;
            }
         }
      return true;
      }

   /** 
    * Checks entering a thick room from a via or trace through a small door (after ripup) 
    */
   private boolean check_leaving_ripped_item(MazeListElement p_list_element)
      {
      if (!(p_list_element.door instanceof ExpandDoor)) return false;
    
      ExpandDoor curr_door = (ExpandDoor) p_list_element.door;
      ExpandRoomComplete from_room = curr_door.other_room(p_list_element.next_room);

      if (!(from_room instanceof ExpandRoomObstacle)) return false;

      BrdItem curr_item = ((ExpandRoomObstacle) from_room).get_item();

      if (!curr_item.is_route()) return false;

      return enter_through_small_door(p_list_element, curr_item);
      }
   }
