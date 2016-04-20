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
 * SelectMenuState.java
 *
 * Created on 28. November 2003, 10:13
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import planar.PlaPointFloat;

/**
 * Class implementing the different functionality in the select menu, especially the different behaviour of the mouse button 1.
 *
 * @author Alfons Wirtz
 */
public class StateMenuSelect extends StateMenu
   {
   public StateMenuSelect(IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_board_handling, p_logfile);
      }

   @Override
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      StateInteractive result = select_items(p_location);
      return result;
      }

   public StateInteractive mouse_dragged(PlaPointFloat p_point)
      {
      return StateSelectRegionItems.get_instance(i_brd.get_current_mouse_position(), this, i_brd, actlog);
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("in_select_menu"));
      }

   public String get_help_id()
      {
      return "MenuState_SelectMenuState";
      }
   }
