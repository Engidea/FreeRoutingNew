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
 * Created on 9. November 2003, 13:05
 */

package interactive.state;

import freert.planar.PlaPointFloat;
import interactive.Actlog;
import interactive.IteraBoard;
import java.awt.geom.Point2D;

/**
 * Class for interactive zooming to a rectangle.
 *
 * @author Alfons Wirtz
 */
public final class StateSelectRegionZoom extends StateSelectRegion
   {
   public StateSelectRegionZoom(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile, PlaPointFloat p_location)
      {
      super(p_parent_state, p_board_handling, p_logfile);

      if (actlog != null) actlog.start_scope(interactive.LogfileScope.ZOOM_FRAME);

      corner1 = p_location;
      
      i_brd.screen_messages.set_status_message(resources.getString("drag_left_mouse_button_to_create_region_to_display"));
      }

   @Override
   public StateInteractive complete()
      {
      corner2 = i_brd.get_current_mouse_position();
      
      zoom_region();

      if ( actlog != null) actlog.add_corner(corner2);

      return this.return_state;
      }

   private void zoom_region()
      {
      if (corner1 == null || corner2 == null)
         {
         return;
         }
      Point2D sc_corner1 = i_brd.gdi_context.coordinate_transform.board_to_screen(corner1);
      Point2D sc_corner2 = i_brd.gdi_context.coordinate_transform.board_to_screen(corner2);
      i_brd.get_panel().zoom_frame(sc_corner1, sc_corner2);
      }
   }
