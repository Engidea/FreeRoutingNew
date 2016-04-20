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
 * MenuState.java
 *
 * Created on 28. November 2003, 10:04
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Collection;
import java.util.Set;
import planar.PlaPointFloat;
import board.items.BrdItem;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Common base class for the main menus, which can be selected in the tool bar.
 *
 * @author Alfons Wirtz
 */
public class StateMenu extends StateInteractive
   {
   public StateMenu(IteraBoard p_board_handle, Actlog p_logfile)
      {
      super(null, p_board_handle, p_logfile);
      this.return_state = this;
      }

   public javax.swing.JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_main;
      }

   /**
    * Selects items at p_location. Returns a new instance of SelectedItemState with the selected items, if something was selected.
    */
   public StateInteractive select_items(PlaPointFloat p_location)
      {
      i_brd.display_layer_messsage();
      Set<BrdItem> picked_items = i_brd.pick_items(p_location);
      boolean something_found = (picked_items.size() > 0);
      StateInteractive result;
      if (something_found)
         {
         result = StateSelectedItem.get_instance(picked_items, this, i_brd, this.actlog);
         i_brd.screen_messages.set_status_message(resources.getString("in_select_mode"));
         actlog_start_scope(LogfileScope.START_SELECT, p_location);
         }
      else
         {
         result = this;
         }
      
      i_brd.repaint();
      return result;
      }

   public StateInteractive swap_pin(PlaPointFloat p_location)
      {
      ItemSelectionFilter selection_filter = new ItemSelectionFilter(ItemSelectionChoice.PINS);
      Collection<BrdItem> picked_items = i_brd.pick_items(p_location, selection_filter);
      StateInteractive result = this;
      if (picked_items.size() > 0)
         {
         BrdItem first_item = picked_items.iterator().next();
         if (!(first_item instanceof board.items.BrdAbitPin))
            {
            System.out.println("MenuState.swap_pin: Pin expected");
            return this;
            }
         board.items.BrdAbitPin selected_pin = (board.items.BrdAbitPin) first_item;
         result = StatePinSwap.get_instance(selected_pin, this, i_brd, this.actlog);
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("no_pin_selected"));
         }
      i_brd.repaint();
      return result;
      }

   /**
    * Action to be taken when a key shortcut is pressed.
    */
   public StateInteractive key_typed(char p_key_char)
      {
      StateInteractive curr_return_state = this;
      if (p_key_char == 'b')
         {
         i_brd.redo();
         }
      else if (p_key_char == 'd')
         {
         curr_return_state = new StateMenuDrag(i_brd, actlog);
         }
      else if (p_key_char == 'e')
         {
         // It seems to me that this is quite useful to understand what is happening... damiano
         curr_return_state = new StateExpandTest(i_brd.get_current_mouse_position(), this, i_brd);
         }
      else if (p_key_char == 'g')
         {
         i_brd.toggle_ratsnest();
         }
      else if (p_key_char == 'i')
         {
         curr_return_state = select_items(i_brd.get_current_mouse_position());
         }
      else if (p_key_char == 'p')
         {
         i_brd.itera_settings.set_push_enabled(!i_brd.itera_settings.push_enabled);
         i_brd.get_panel().board_frame.refresh_windows();
         }
      else if (p_key_char == 'r')
         {
         curr_return_state = new StateMenuRoute(i_brd, actlog);
         }
      else if (p_key_char == 's')
         {
         curr_return_state = new StateMenuSelect(i_brd, actlog);
         }
      else if (p_key_char == 't')
         {
         curr_return_state = StateRoute.get_instance(i_brd.get_current_mouse_position(), this, i_brd, actlog);
         }
      else if (p_key_char == 'u')
         {
         i_brd.undo();
         }
      else if (p_key_char == 'v')
         {
         i_brd.toggle_clearance_violations();
         }
      else if (p_key_char == 'w')
         {
         curr_return_state = swap_pin(i_brd.get_current_mouse_position());
         }
      else if (p_key_char == '+')
         {
         // increase the current layer to the next signal layer
         board.BrdLayerStructure layer_structure = i_brd.get_routing_board().layer_structure;
         int current_layer_no = i_brd.itera_settings.layer_no;
         for (;;)
            {
            ++current_layer_no;
            if (current_layer_no >= layer_structure.size() || layer_structure.is_signal(current_layer_no))
               {
               break;
               }
            }
         if (current_layer_no < layer_structure.size())
            {
            i_brd.set_current_layer(current_layer_no);
            }
         }
      else if (p_key_char == '-')
         {
         // decrease the current layer to the previous signal layer
         board.BrdLayerStructure layer_structure = i_brd.get_routing_board().layer_structure;
         int current_layer_no = i_brd.itera_settings.layer_no;
         for (;;)
            {
            --current_layer_no;
            if (current_layer_no < 0 || layer_structure.is_signal(current_layer_no))
               {
               break;
               }
            }
         if (current_layer_no >= 0)
            {
            i_brd.set_current_layer(current_layer_no);
            }

         }
      else
         {
         curr_return_state = super.key_typed(p_key_char);
         }
      return curr_return_state;
      }

   /**
    * Do nothing on complete.
    */
   @Override
   public StateInteractive complete()
      {
      return this;
      }

   /**
    * Do nothing on cancel.
    */
   @Override
   public StateInteractive cancel()
      {
      return this;
      }

   @Override
   public void set_toolbar()
      {
      i_brd.get_panel().board_frame.set_menu_toolbar();
      }
   }
