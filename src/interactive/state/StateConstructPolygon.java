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
 * PolygonShapeConstructionState.java
 *
 * Created on 7. November 2003, 17:19
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Iterator;
import planar.PlaPointFloat;
import planar.PlaPointInt;
import planar.ShapePolygon;
import rules.BoardRules;

/**
 * Interactive state for constructing an obstacle with a polygon shape.
 *
 * @author Alfons Wirtz
 */
public class StateConstructPolygon extends StateConstructCorner
   {
   /**
    * Returns a new instance of this class If p_logfile != null; the creation of this item is stored in a logfile
    */
   public static StateConstructPolygon get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      return new StateConstructPolygon(p_location, p_parent_state, p_board_handling, p_logfile);
      }

   private StateConstructPolygon(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);

      if (actlog != null) actlog.start_scope(LogfileScope.CREATING_POLYGONSHAPE);

      this.add_corner(p_location);
      }

   /**
    * Inserts the polygon shape item into the board, if possible and returns to the main state
    */
   @Override
   public StateInteractive complete()
      {
      add_corner_for_snap_angle();
      int corner_count = corner_list.size();
      boolean construction_succeeded = (corner_count > 2);
      if (construction_succeeded)
         {
         PlaPointInt[] corner_arr = new PlaPointInt[corner_count];
         Iterator<PlaPointInt> it = corner_list.iterator();
         for (int i = 0; i < corner_count; ++i)
            {
            corner_arr[i] = it.next();
            }
         ShapePolygon obstacle_shape = new ShapePolygon(corner_arr);
         int cl_class = BoardRules.clearance_class_none;
         if (obstacle_shape.split_to_convex() == null)
            {
            // shape is invalid, maybe it has selfintersections
            construction_succeeded = false;
            }
         else
            {
            construction_succeeded = i_brd.get_routing_board().check_shape(obstacle_shape, i_brd.itera_settings.layer_no, new int[0], cl_class);
            }
         if (construction_succeeded)
            {
            this.observers_activated = !i_brd.get_routing_board().observers_active();
            if (this.observers_activated)
               {
               i_brd.get_routing_board().start_notify_observers();
               }
            i_brd.get_routing_board().generate_snapshot();
            i_brd.get_routing_board().insert_obstacle(obstacle_shape, i_brd.itera_settings.layer_no, cl_class, board.varie.ItemFixState.UNFIXED);
            i_brd.get_routing_board().end_notify_observers();
            if (this.observers_activated)
               {
               i_brd.get_routing_board().end_notify_observers();
               this.observers_activated = false;
               }
            }
         }
      if (construction_succeeded)
         {
         i_brd.screen_messages.set_status_message(resources.getString("keepout_successful_completed"));
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("keepout_cancelled_because_of_overlaps"));
         }
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.COMPLETE_SCOPE);
         }
      return this.return_state;
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("creating_polygonshape"));
      }

   }
