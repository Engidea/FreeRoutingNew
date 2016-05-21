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

import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.rules.RuleNets;
import freert.rules.RuleViaInfoList;
import freert.varie.NetNosList;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.IteraRoute;
import interactive.LogfileScope;
import java.awt.Graphics;

/**
 * Class for shoving items out of a region to make space to insert something else. 
 * For that purpose traces of an invisible net are created temporary for shoving.
 *
 * @author Alfons Wirtz
 */
public final class StateDragMakeSpace extends StateDrag
   {
   private static final int SHOVE_TRACE_WIDTH=200;

   private final IteraRoute itera_route;
   
   public StateDragMakeSpace(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_location, p_parent_state, p_board_handling, p_logfile);

      int[] shove_trace_width_arr = new int[r_brd.get_layer_count()];
      boolean[] layer_active_arr = new boolean[shove_trace_width_arr.length];
      
      for (int index = 0; index < shove_trace_width_arr.length; ++index)
         {
         shove_trace_width_arr[index] = SHOVE_TRACE_WIDTH;
         layer_active_arr[index] = true;
         }
      
      NetNosList route_net_no_arr = new NetNosList(RuleNets.HIDDEN_NET_NO);

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
            r_brd, 
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
         observers_activated = !r_brd.observers_active();

         if (observers_activated) r_brd.start_notify_observers();
         
         // make the situation restorable by undo
         r_brd.generate_snapshot();
         
         // Delayed till here because otherwise the mouse might have been only clicked for selecting and not pressed for moving.
         actlog_start_scope(LogfileScope.MAKING_SPACE, previous_location);
         
         something_dragged = true;
         }
      
      itera_route.next_corner(p_to_location);

      PlaPointInt route_end = itera_route.get_last_corner();
      
      if (r_brd.brd_rules.is_trace_snap_none() && ! route_end.equals(p_to_location.round()))
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
      r_brd.remove_items_unfixed(r_brd.get_connectable_items(RuleNets.HIDDEN_NET_NO));
      
      if (observers_activated)
         {
         r_brd.end_notify_observers();
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
