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
 * DragState.java
 *
 * Created on 10. Dezember 2003, 09:08
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import board.items.BrdAbit;
import board.items.BrdItem;
import board.items.BrdTracep;
import freert.planar.PlaPointFloat;

/**
 * Class implementing functionality when the mouse is dragged on a routing board
 *
 * @author Alfons Wirtz
 */
public abstract class StateDrag extends StateInteractive
   {
   protected PlaPointFloat previous_location;
   protected boolean something_dragged = false;
   protected boolean observers_activated = false;
   
   /**
    * Returns a new instance of this state, if a item to drag was found at the input location 
    * null otherwise.
    */
   public static StateDrag get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      p_board_handling.display_layer_messsage();
      BrdItem item_to_move = null;
      int try_count = 1;
      if (p_board_handling.itera_settings.select_on_all_visible_layers)
         {
         try_count += p_board_handling.get_layer_count();
         }
      int curr_layer = p_board_handling.itera_settings.layer_no;
      int pick_layer = curr_layer;
      boolean item_found = false;

      for (int i = 0; i < try_count; ++i)
         {
         if (i == 0 || pick_layer != curr_layer && (p_board_handling.gdi_context.get_layer_visibility(pick_layer)) > 0)
            {
            Collection<BrdItem> found_items = p_board_handling.get_routing_board().pick_items(p_location.round(), pick_layer, p_board_handling.itera_settings.item_selection_filter);
            Iterator<BrdItem> it = found_items.iterator();
            while (it.hasNext())
               {
               item_found = true;
               BrdItem curr_item = it.next();
               if (curr_item instanceof BrdTracep)
                  {
                  continue; // traces are not moved
                  }
               if (!p_board_handling.itera_settings.drag_components_enabled && curr_item.get_component_no() != 0)
                  {
                  continue;
                  }
               item_to_move = curr_item;
               if (curr_item instanceof BrdAbit)
                  {
                  break; // drill items are preferred
                  }
               }
            if (item_to_move != null)
               {
               break;
               }
            }
         // nothing found on settings.layer, try all visible layers
         pick_layer = i;
         }
      
      StateDrag result;
      if (item_to_move != null)
         {
         result = new StateDragItem(item_to_move, p_location, p_parent_state, p_board_handling, p_logfile);
         }
      else if (!item_found)
         {
         result = new StateDragMakeSpace(p_location, p_parent_state, p_board_handling, p_logfile);
         }
      else
         {
         result = null;
         }
      if (result != null)
         {
         p_board_handling.hide_ratsnest();
         }
      return result;
      }

   protected StateDrag(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      previous_location = p_location;
      }

   public abstract StateInteractive move_to(PlaPointFloat p_to_location);

   @Override
   public final StateInteractive mouse_dragged(PlaPointFloat p_point)
      {
      StateInteractive result = move_to(p_point);
      
      if (result != this)
         {
         // an error occurred
         Set<Integer> changed_nets = new TreeSet<Integer>();
         
         r_brd.undo(changed_nets);
         
         for (Integer changed_net : changed_nets)
            {
            i_brd.update_ratsnest(changed_net);
            }
         }
      
      if (something_dragged) actlog_add_corner(p_point);

      return result;
      }

   @Override
   public StateInteractive complete()
      {
      return button_released();
      }

   @Override
   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      return move_to(p_point);
      }
   }