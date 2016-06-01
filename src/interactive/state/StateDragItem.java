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
 * DragItemState.java
 *
 * Created on 9. November 2003, 08:13
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import board.items.BrdItem;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;

/**
 * Class for interactive dragging items with the mouse on a routing board
 *
 * @author Alfons Wirtz
 */
public class StateDragItem extends StateDrag
   {
   private final BrdItem item_to_move;
   
   protected StateDragItem(BrdItem p_item_to_move, PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_location, p_parent_state, p_board_handling, p_logfile);
      item_to_move = p_item_to_move;
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("dragging_item"));
      }

   /**
    * Moves the items of the group to p_to_location. 
    * @return return_state, if an error accrued while moving, so that an undo may be necessary.
    */
   public StateInteractive move_to(PlaPointFloat p_to_location)
      {
      PlaPointInt to_location = p_to_location.round();
      
      PlaPointInt from_location = previous_location.round();
      
      if (r_brd.brd_rules.is_trace_snap_90())
         {
         to_location = to_location.orthogonal_projection(from_location);
         }
      else if (r_brd.brd_rules.is_trace_snap_45())
         {
         to_location = to_location.fortyfive_degree_projection(from_location);
         }
      
      if (to_location.equals(from_location)) return this;
      
      if (item_to_move.is_user_fixed())
         {
         i_brd.screen_messages.set_status_message("Please unfix item before dragging");
         return this;
         }

      StateDragMoveComponent move_component = null;
      PlaVectorInt rel_coor = to_location.difference_by(from_location);
      
      double length = rel_coor.distance();
      
      boolean shove_ok = false;
      
      for (int index = 0; index < 2; ++index)
         {
         move_component = new StateDragMoveComponent(item_to_move, rel_coor, 99, 5);
         
         if (move_component.check_move())
            {
            shove_ok = true;
            break;
            }
         
         if (index == 0)
            {
            // reduce evtl. the shove distance to make the check shove function work properly, if more than 1 trace have to be shoved.
            double sample_width = 2 * r_brd.get_min_trace_half_width();
            if (length > sample_width)
               {
               rel_coor = rel_coor.change_length_approx(sample_width);
               }
            }
         }

      if ( shove_ok)
         {
         if (!something_dragged)
            {
            // Initializations for the first time dragging
            r_brd.start_notify_observers();

            // make the situation restorable by undo
            r_brd.generate_snapshot();
      
            // Delayed till here because otherwise the mouse might have been only clicked for selecting and not pressed for moving.
            actlog_start_scope(LogfileScope.DRAGGING_ITEMS, previous_location);
            
            something_dragged = true;
            }
         
         if (!move_component.drag_move(i_brd.itera_settings.trace_pull_tight_region_width, i_brd.itera_settings.trace_pullt_min_move))
            {
            // an insert error occurred, end the drag state
            return return_state;
            }
         
         i_brd.repaint();
         }
      
      previous_location = p_to_location;
      return this;
      }

   public StateInteractive button_released()
      {

      r_brd.end_notify_observers();
      
      if (something_dragged)
         {
         actlog_start_scope(LogfileScope.COMPLETE_SCOPE);
         
         // Update the incomplete for the nets of the moved items.
         if (item_to_move.get_component_no() == 0)
            {
            for (int i = 0; i < item_to_move.net_count(); ++i)
               {
               i_brd.update_ratsnest(item_to_move.get_net_no(i));
               }
            }
         else
            {
            Collection<BrdItem> moved_items = r_brd.get_component_items(item_to_move.get_component_no());
            Set<Integer> changed_nets = new TreeSet<Integer>();
            Iterator<BrdItem> it = moved_items.iterator();
            while (it.hasNext())
               {
               BrdItem curr_moved_item = it.next();
               for (int i = 0; i < curr_moved_item.net_count(); ++i)
                  {
                  changed_nets.add(new Integer(curr_moved_item.get_net_no(i)));
                  }
               }
            for (Integer curr_net_no : changed_nets)
               {
               i_brd.update_ratsnest(curr_net_no.intValue());
               }
            }
         }
      else
         {
         i_brd.show_ratsnest();
         }
      
      i_brd.screen_messages.set_status_message("");
      
      return return_state;
      }
   }
