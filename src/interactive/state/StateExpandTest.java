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
 * ExpandTestState.java
 *
 * Created on 23. Dezember 2003, 07:56
 */
package interactive.state;

import interactive.IteraBoard;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import autoroute.ArtConnectionInsert;
import autoroute.ArtConnectionLocate;
import autoroute.ArtControl;
import autoroute.ArtEngine;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import autoroute.maze.MazeSearch;
import autoroute.maze.MazeSearchResult;
import board.BrdConnectable;
import board.RoutingBoard;
import board.items.BrdItem;
import board.varie.BrdStopConnection;
import freert.planar.PlaPointFloat;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 * State for testing the expanding algorithm of the autorouter.
 * @author Alfons Wirtz
 */
public final class StateExpandTest extends StateInteractive
   {
   private MazeSearch maze_search_algo = null;
   private ArtConnectionLocate locate_connection = null;
   private ArtControl control_settings;
   private ArtEngine autoroute_engine;

   public StateExpandTest(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
      {
      super(p_return_state, p_board_handling, null);
      expand_test_init(p_location);
      }

   private void expand_test_init(PlaPointFloat p_location)
      {
      // look if an autoroute can be started at the input location
      RoutingBoard board = i_brd.get_routing_board();
      int layer = i_brd.itera_settings.layer_no;
      Collection<BrdItem> found_items = board.pick_items(p_location.round(), layer);
      BrdItem route_item = null;
      int route_net_no = 0;

      for ( BrdItem cur_item : found_items )
         {
         if ( ! ( cur_item instanceof BrdConnectable) ) continue;
         
         // I want an item with just one net count
         if (cur_item.net_count() != 1 ) continue;
         
         if ( cur_item.get_net_no(0) > 0)
            {
            route_item = cur_item;
            route_net_no = cur_item.get_net_no(0);
            break;
            }
         }
      
      control_settings = new ArtControl(i_brd.get_routing_board(), route_net_no, i_brd.itera_settings);
      control_settings.ripup_pass_no = i_brd.itera_settings.autoroute_settings.pass_no_get();
      control_settings.ripup_costs = control_settings.ripup_pass_no * i_brd.itera_settings.autoroute_settings.get_start_ripup_costs();
      control_settings.vias_allowed = false;

      autoroute_engine = new ArtEngine(board, route_net_no, control_settings.trace_clearance_class_no, null );
      
      if (route_item == null)
         {
         // create an expansion room in the empty space
         ShapeTile contained_shape = new ShapeTileBox(p_location.round());
         ExpandRoomFreespaceIncomplete expansion_room = autoroute_engine.add_incomplete_expansion_room(null, layer, contained_shape);
         i_brd.userPrintln("expansion test started route_item==null");
         complete_expansion_room(expansion_room);
         return;
         }
      
      Set<BrdItem> route_start_set = route_item.get_connected_set(route_net_no);
      Set<BrdItem> route_dest_set = route_item.get_unconnected_set(route_net_no);
      
      if (route_dest_set.size() > 0)
         {
         i_brd.userPrintln("expansion test started route_dest_set size > 0 ");
         maze_search_algo = new MazeSearch ( autoroute_engine, control_settings, route_start_set, route_dest_set );
         
         if ( ! maze_search_algo.is_initialized() )
            {
            i_brd.userPrintln("maze_search_algo.is_initialized() NOT ");
            maze_search_algo = null;
            }
         }
      }
   
   
   private boolean in_autoroute ()
      {
      return maze_search_algo != null;
      }
   
   private void key_n_typed ()
      {
      if (in_autoroute())
         {
         if (! maze_search_algo.occupy_next_element())
            {
            // to display the backtrack rooms
            complete_autoroute();
            i_brd.userPrintln("expansion completed");
            }
         }
      else
         {
         boolean completing_succeeded = false;
      
         while (!completing_succeeded)
            {
            ExpandRoomFreespaceIncomplete next_room = this.autoroute_engine.get_first_incomplete_expansion_room();
            if (next_room == null)
               {
               i_brd.userPrintln("expansion completed");
               break;
               }
            completing_succeeded = complete_expansion_room(next_room);
            }
         }
      }
   
   
   private void key_a_typed ()
      {
      if (in_autoroute())
         {
         complete_autoroute();
         }
      else
         {
         ExpandRoomFreespaceIncomplete next_room = autoroute_engine.get_first_incomplete_expansion_room();
         while (next_room != null)
            {
            complete_expansion_room(next_room);
            next_room = autoroute_engine.get_first_incomplete_expansion_room();
            }
         }
   
      }

   private void key_digit_typed (char p_key_char )
      {
      // next 10^p_key_char expansions
      int d = Character.digit(p_key_char, 10);
      
      final int max_count = (int) Math.pow(10, d);
      
      if (in_autoroute())
         {
         for (int i = 0; i < max_count; ++i)
            {
            if ( maze_search_algo.occupy_next_element())
               {
               // to display the backtack rooms
               complete_autoroute();
               i_brd.userPrintln("expansion completed");
               break;
               }
            }
         }
      else
         {
         int curr_count = 0;
         ExpandRoomFreespaceIncomplete next_room = autoroute_engine.get_first_incomplete_expansion_room();
         while (next_room != null && curr_count < max_count)
            {
            complete_expansion_room(next_room);
            next_room = autoroute_engine.get_first_incomplete_expansion_room();
            ++curr_count;
            }
         }
      }
   
   
   public StateInteractive key_typed(char p_key_char)
      {
      StateInteractive result = this;
      
      if (p_key_char == 'n')
         {
         key_n_typed();
         }
      else if (p_key_char == 'a')
         {
         key_a_typed();
         }
      else if (Character.isDigit(p_key_char))
         {
         key_digit_typed(p_key_char);
         }
      else
         {
         autoroute_engine.autoroute_clear();
         result = super.key_typed(p_key_char);
         }
      
      i_brd.repaint();
      return result;
      }

   @Override
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return cancel();
      }

   @Override
   public StateInteractive cancel()
      {
      autoroute_engine.autoroute_clear();
      return return_state;
      }
   
   @Override
   public StateInteractive complete()
      {
      return cancel();
      }

   @Override
   public void draw( Graphics p_graphics)
      {
      autoroute_engine.draw(p_graphics, i_brd.gdi_context, 0.1);

      if ( locate_connection != null)
         {
         locate_connection.draw(p_graphics, i_brd.gdi_context);
         }
      }


   private void complete_autoroute()
      {
      MazeSearchResult search_result = maze_search_algo.find_connection();

      if (search_result == null) return;
      
      SortedSet<BrdItem> ripped_item_list = new TreeSet<BrdItem>();
      locate_connection = ArtConnectionLocate.get_instance(search_result, control_settings, autoroute_engine.autoroute_search_tree,
            i_brd.get_routing_board().brd_rules.get_trace_snap_angle(), ripped_item_list );
      i_brd.get_routing_board().generate_snapshot();
      
      SortedSet<BrdItem> ripped_connections = new TreeSet<BrdItem>();
      for (BrdItem curr_ripped_item : ripped_item_list)
         {
         ripped_connections.addAll(curr_ripped_item.get_connection_items(BrdStopConnection.VIA));
         }
      
      i_brd.get_routing_board().remove_items_unfixed(ripped_connections);
      
      ArtConnectionInsert insert_algo = new ArtConnectionInsert( i_brd.get_routing_board(), control_settings);
      
      insert_algo.insert(locate_connection);
      }

   /**
    * Returns true, if the completion succeeded.
    */
   private boolean complete_expansion_room(ExpandRoomFreespaceIncomplete p_incomplete_room)
      {
      Collection<ExpandRoomFreespaceComplete> completed_rooms = autoroute_engine.complete_expansion_room(p_incomplete_room);
      return (completed_rooms.size() > 0);
      }
   }
