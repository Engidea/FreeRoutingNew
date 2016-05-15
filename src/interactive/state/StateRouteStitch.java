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
 * StichRouteState.java
 *
 * Created on 8. Dezember 2003, 08:05
 */

package interactive.state;

import freert.planar.PlaPointFloat;
import freert.varie.ItemClass;
import interactive.Actlog;
import interactive.IteraBoard;

/**
 * State for interactive routing by adding corners with the left mouse button.
 *
 * @author Alfons Wirtz
 */
public class StateRouteStitch extends StateRoute
   {
   protected StateRouteStitch(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      }

   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return add_corner(p_location);
      }

   public StateInteractive add_corner(PlaPointFloat p_location)
      {
      // make the current situation restorable by undo
      r_brd.generate_snapshot();
      return super.add_corner(p_location);
      }

   public StateInteractive mouse_moved()
      {
      super.mouse_moved();
      route.calc_nearest_target_point(i_brd.get_current_mouse_position());
      i_brd.repaint();
      return this;
      }

   public javax.swing.JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_stitch_route;
      }

   public String get_help_id()
      {
      return "RouteState_StitchingRouteState";
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      super.draw(p_graphics);
      if (route == null)
         {
         return;
         }
      // draw a line from the routing end point to the cursor
      PlaPointFloat[] draw_points = new PlaPointFloat[2];
      draw_points[0] = route.get_last_corner().to_float();
      draw_points[1] = i_brd.get_current_mouse_position();
      java.awt.Color draw_color = i_brd.gdi_context.get_hilight_color();
      double display_width = i_brd.get_trace_halfwidth(route.net_nos.first(), i_brd.itera_settings.layer_no);
      int clearance_draw_width = 50;
      double radius_with_clearance = display_width;
      freert.rules.NetClass default_net_class = r_brd.brd_rules.get_default_net_class();
      int cl_class = default_net_class.default_item_clearance_classes.get(ItemClass.TRACE);
      radius_with_clearance += r_brd.get_clearance(cl_class, cl_class, i_brd.itera_settings.layer_no);
      i_brd.gdi_context.draw(draw_points, display_width, draw_color, p_graphics, 0.5);
      // draw the clearance boundary around the end point
      i_brd.gdi_context.draw_circle(draw_points[1], radius_with_clearance, clearance_draw_width, draw_color, p_graphics, 0.5);
      }
   }
