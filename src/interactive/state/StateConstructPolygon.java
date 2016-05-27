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
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;
import freert.planar.ShapePolygon;
import freert.rules.BoardRules;
import freert.varie.NetNosList;

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

      actlog_start_scope(LogfileScope.CREATING_POLYGONSHAPE);

      add_corner(p_location);
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
         PlaPointIntAlist corner_arr = new PlaPointIntAlist(corner_count);
         
         for ( PlaPointInt a_point : corner_list ) corner_arr.add(a_point);
         
         ShapePolygon obstacle_shape = new ShapePolygon(corner_arr);
         
         int cl_class = BoardRules.clearance_null_idx;
         if (obstacle_shape.split_to_convex() == null)
            {
            // shape is invalid, maybe it has selfintersections
            construction_succeeded = false;
            }
         else
            {
            construction_succeeded = r_brd.check_shape(obstacle_shape, i_brd.itera_settings.layer_no, NetNosList.EMPTY, cl_class);
            }
         if (construction_succeeded)
            {

            r_brd.start_notify_observers();

            r_brd.generate_snapshot();
            r_brd.insert_obstacle(obstacle_shape, i_brd.itera_settings.layer_no, cl_class, board.varie.ItemFixState.UNFIXED);
            r_brd.end_notify_observers();

            r_brd.end_notify_observers();
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

      actlog_start_scope(LogfileScope.COMPLETE_SCOPE);

      return return_state;
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("creating_polygonshape"));
      }

   }
