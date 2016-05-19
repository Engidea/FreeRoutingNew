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
 * CircleConstructionState.java
 *
 * Created on 6. November 2003, 09:37
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import board.varie.ItemFixState;
import board.varie.TraceAngleRestriction;
import freert.planar.ShapeCircle;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.ShapeConvex;
import freert.rules.BoardRules;
import freert.varie.NetNosList;

/**
 * Interactive creation of a circle obstacle
 *
 * @author Alfons Wirtz
 */
public class StateCircleConstrut extends StateInteractive
   {
   /**
    * Returns a new instance of this class. If p_logfile != null; the creation of this item is stored in a logfile
    */
   public static StateCircleConstrut get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      p_board_handling.remove_ratsnest(); // inserting a circle may change the connectivity.
      return new StateCircleConstrut(p_location, p_parent_state, p_board_handling, p_logfile);
      }

   /** Creates a new instance of CircleConstructionState */
   private StateCircleConstrut(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      circle_center = p_location;

      actlog_start_scope(LogfileScope.CREATING_CIRCLE, p_location);
      }

   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      if (actlog != null)
         {
         actlog.add_corner(p_location);
         }
      return complete();
      }

   public StateInteractive mouse_moved()
      {
      super.mouse_moved();
      i_brd.repaint();
      return this;
      }

   /**
    * completes the circle construction state
    */
   @Override
   public StateInteractive complete()
      {
      PlaPointInt center = circle_center.round();
      int radius = (int) Math.round(circle_radius);
      int layer = i_brd.itera_settings.layer_no;
      int cl_class;

      cl_class = BoardRules.clearance_null_idx;
      boolean construction_succeeded = (circle_radius > 0);
      ShapeConvex obstacle_shape = null;
      
      if (construction_succeeded)
         {
         obstacle_shape = new ShapeCircle(center, radius);
         if (r_brd.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
            {
            obstacle_shape = obstacle_shape.bounding_box();
            }
         else if (r_brd.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.FORTYFIVE_DEGREE)
            {
            obstacle_shape = obstacle_shape.bounding_octagon();
            }
         construction_succeeded = r_brd.check_shape(obstacle_shape, layer, NetNosList.EMPTY, cl_class);
         }

      if (construction_succeeded)
         {
         i_brd.screen_messages.set_status_message(resources.getString("keepout_successful_completed"));

         // insert the new shape as keepout
         observers_activated = r_brd.observers_active();
         if (observers_activated)
            {
            r_brd.start_notify_observers();
            }
         r_brd.generate_snapshot();
         r_brd.insert_obstacle(obstacle_shape, layer, cl_class, ItemFixState.UNFIXED);
         if (observers_activated)
            {
            r_brd.end_notify_observers();
            observers_activated = false;
            }
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("keepout_cancelled_because_of_overlaps"));
         }
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.COMPLETE_SCOPE);
         }
      i_brd.repaint();
      return return_state;
      }

   /**
    * Used when reading the next point from a logfile. Calls complete, because only 1 additional point is stored in the logfile.
    */
   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      circle_radius = circle_center.distance(p_point);
      return this;
      }

   /**
    * draws the graphic construction aid for the circle
    */
   public void draw(java.awt.Graphics p_graphics)
      {
      PlaPointFloat current_mouse_position = i_brd.get_current_mouse_position();
      if (current_mouse_position == null)
         {
         return;
         }
      circle_radius = circle_center.distance(current_mouse_position);
      i_brd.gdi_context.draw_circle(circle_center, circle_radius, 300, java.awt.Color.white, p_graphics, 1);
      }

   public javax.swing.JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_insert_cancel;
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("creating_circle"));
      }

   private final PlaPointFloat circle_center;
   private double circle_radius = 0;

   private boolean observers_activated = false;
   }
