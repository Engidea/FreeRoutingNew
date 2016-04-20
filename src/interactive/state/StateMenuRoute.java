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
 * RouteMenuState.java
 *
 * Created on 29. November 2003, 07:50
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import planar.PlaPointFloat;

/**
 * Class implementing the different functionality in the route menu, especially the different behavior of the mouse button 1.
 *
 * @author Alfons Wirtz
 */
public class StateMenuRoute extends StateMenu
   {
   public StateMenuRoute(IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_board_handling, p_logfile);
      }

   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return StateRoute.get_instance(p_location, this, i_brd, actlog);
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(" in route menu");
      }

   public String get_help_id()
      {
      return "MenuState_RouteMenuState";
      }

   }
