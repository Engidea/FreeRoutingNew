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
 * SelectRegionState.java
 *
 * Created on 9. November 2003, 11:34
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import planar.PlaPointFloat;

/**
 * Common base class for interactive selection of a rectangle.
 *
 * @author Alfons Wirtz
 */
public class StateSelectRegion extends StateInteractive
   {
   protected StateSelectRegion(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      }

   public StateInteractive button_released()
      {
      i_brd.screen_messages.set_status_message("");
      return complete();
      }

   public StateInteractive mouse_dragged(PlaPointFloat p_point)
      {
      if (corner1 == null)
         {
         corner1 = p_point;
         if (actlog != null)
            {
            actlog.add_corner(corner1);
            }
         }
      i_brd.repaint();
      return this;
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      this.return_state.draw(p_graphics);
      PlaPointFloat current_mouse_position = i_brd.get_current_mouse_position();
      if (corner1 == null || current_mouse_position == null)
         {
         return;
         }
      corner2 = current_mouse_position;
      i_brd.gdi_context.draw_rectangle(corner1, corner2, 1, java.awt.Color.white, p_graphics, 1);
      }

   protected PlaPointFloat corner1 = null;
   protected PlaPointFloat corner2 = null;
   }
