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
 * MakeSpaceState.java
 *
 * Created on 10. Dezember 2003, 10:53
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.IteraRoute;
import interactive.LogfileScope;
import java.awt.Graphics;
import planar.PlaPoint;
import planar.PlaPointFloat;
import rules.RuleNets;
import rules.RuleViaInfoList;
import board.RoutingBoard;
import board.varie.TraceAngleRestriction;

/**
 * Class for shoving items out of a region to make space to insert something else. 
 * For that purpose traces of an invisible net are created temporary for shoving.
 *
 * @author Alfons Wirtz
 */
public final class StateDragMakeSpace extends StateDrag
   {
   private static final int SHOVE_TRACE_WIDTH=200;

   private final RoutingBoard r_board;
   private final IteraRoute itera_route;
   
   public StateDragMakeSpace(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_location, p_parent_state, p_board_handling, p_logfile);
      r_board = i_brd.get_routing_board();

      int[] shove_trace_width_arr = new int[r_board.get_layer_count()];
      boolean[] layer_active_arr = new boolean[shove_trace_width_arr.length];
      
      for (int index = 0; index < shove_trace_width_arr.length; ++index)
         {
         shove_trace_width_arr[index] = SHOVE_TRACE_WIDTH;
         layer_active_arr[index] = true;
         }
      
      int[] route_net_no_arr = new int[1];
      route_net_no_arr[0] = RuleNets.HIDDEN_NET_NO;

      itera_route = new IteraRoute(p_location.round(), 
            i_brd.itera_settings.layer_no, 
            shove_trace_width_arr, 
            layer_active_arr, 
            route_net_no_arr, 
            0, 
            RuleViaInfoList.EMPTY, 
            true,
            null, 
            null, 
            r_board, 
            false, 
            false, 
            i_brd.itera_settings);
      }

   @Override
   public StateInteractive move_to(PlaPointFloat p_to_location)
      {
      if (! something_dragged)
         {
         // initialisitions for the first time dragging
         observers_activated = !r_board.observers_active();

         if (observers_activated) r_board.start_notify_observers();
         
         // make the situation restorable by undo
         r_board.generate_snapshot();
         
         // Delayed till here because otherwise the mouse might have been only clicked for selecting and not pressed for moving.
         actlog_start_scope(LogfileScope.MAKING_SPACE, previous_location);
         
         something_dragged = true;
         }
      
      itera_route.next_corner(p_to_location);

      PlaPoint route_end = itera_route.get_last_corner();
      
      if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NONE && ! route_end.equals(p_to_location.round()))
         {
         i_brd.move_mouse(route_end.to_float());
         }
      
      i_brd.recalculate_length_violations();
      
      i_brd.repaint();
      
      return this;
      }

   @Override   
   public StateInteractive button_released()
      {
      r_board.remove_items_unfixed(r_board.get_connectable_items(RuleNets.HIDDEN_NET_NO));
      
      if (observers_activated)
         {
         r_board.end_notify_observers();
         observers_activated = false;
         }
      
      if (something_dragged)
         {
         actlog_start_scope(LogfileScope.COMPLETE_SCOPE);
         }
      
      i_brd.show_ratsnest();
      
      return return_state;
      }

   @Override   
   public void draw(Graphics p_graphics)
      {
      itera_route.draw(p_graphics, i_brd.gdi_context);
      }

   }
