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
 * TileConstructionState.java
 *
 * Created on 6. November 2003, 14:46
 */

package interactive.state;

import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSide;
import freert.planar.ShapeTile;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Iterator;
import rules.BoardRules;
import board.RoutingBoard;
import board.varie.ItemFixState;
import board.varie.TraceAngleRestriction;

/**
 * Class for interactive construction of a tile shaped obstacle
 *
 * @author Alfons Wirtz
 */
public class StateConstuctTile extends StateConstructCorner
   {
   /**
    * Returns a new instance of this class If p_logfile != null; the creation of this item is stored in a logfile
    */
   public StateConstuctTile(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      if (this.actlog != null)
         {
         actlog.start_scope(LogfileScope.CREATING_TILE);
         }
      this.add_corner(p_location);
      }

   /**
    * adds a corner to the tile under construction
    */
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      super.left_button_clicked(p_location);
      remove_concave_corners();
      i_brd.repaint();
      return this;
      }

   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      return left_button_clicked(p_point);
      }

   @Override
   public StateInteractive complete()
      {
      remove_concave_corners_at_close();
      int corner_count = corner_list.size();
      boolean construction_succeeded = corner_count > 2;
      if (construction_succeeded)
         {
         // create the edgelines of the new tile
         PlaLineInt[] edge_lines = new PlaLineInt[corner_count];
         Iterator<PlaPointInt> it = corner_list.iterator();
         PlaPointInt first_corner = it.next();
         PlaPointInt prev_corner = first_corner;
         for (int i = 0; i < corner_count - 1; ++i)
            {
            PlaPointInt next_corner = it.next();
            edge_lines[i] = new PlaLineInt(prev_corner, next_corner);
            prev_corner = next_corner;
            }
         edge_lines[corner_count - 1] = new PlaLineInt(prev_corner, first_corner);
         ShapeTile obstacle_shape = ShapeTile.get_instance(edge_lines);
         RoutingBoard board = i_brd.get_routing_board();
         int layer = i_brd.itera_settings.layer_no;
         int cl_class = BoardRules.clearance_class_none;

         construction_succeeded = board.check_shape(obstacle_shape, layer, new int[0], cl_class);
         if (construction_succeeded)
            {
            // insert the new shape as keepout
            this.observers_activated = !i_brd.get_routing_board().observers_active();
            if (this.observers_activated)
               {
               i_brd.get_routing_board().start_notify_observers();
               }
            board.generate_snapshot();
            board.insert_obstacle(obstacle_shape, layer, cl_class, ItemFixState.UNFIXED);
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

   /**
    * skips concave corners at the end of the corner_list.
    **/
   private void remove_concave_corners()
      {
      PlaPointInt[] corner_arr = new PlaPointInt[corner_list.size()];
      Iterator<PlaPointInt> it = corner_list.iterator();
      for (int i = 0; i < corner_arr.length; ++i)
         {
         corner_arr[i] = it.next();
         }

      int new_length = corner_arr.length;
      if (new_length < 3)
         {
         return;
         }
      PlaPointInt last_corner = corner_arr[new_length - 1];
      PlaPointInt curr_corner = corner_arr[new_length - 2];
      while (new_length > 2)
         {
         PlaPointInt prev_corner = corner_arr[new_length - 3];
         PlaSide last_corner_side = last_corner.side_of(prev_corner, curr_corner);
         if (last_corner_side == PlaSide.ON_THE_LEFT)
            {
            // side is ok, nothing to skip
            break;
            }
         if (this.i_brd.get_routing_board().brd_rules.get_trace_snap_angle() != TraceAngleRestriction.FORTYFIVE_DEGREE)
            {
            // skip concave corner
            corner_arr[new_length - 2] = last_corner;
            }
         --new_length;
         // In 45 degree case just skip last corner as nothing like the following
         // calculation for the 90 degree case to keep
         // the angle restrictions is implemented.
         if (this.i_brd.get_routing_board().brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
            {
            // prevent generating a non orthogonal line by changing the previous corner
            PlaPointInt prev_prev_corner = null;
            if (new_length >= 3)
               {
               prev_prev_corner = corner_arr[new_length - 3];
               }
            if (prev_prev_corner != null && prev_prev_corner.v_x == prev_corner.v_x)
               {
               corner_arr[new_length - 2] = new PlaPointInt(prev_corner.v_x, last_corner.v_y);
               }
            else
               {
               corner_arr[new_length - 2] = new PlaPointInt(last_corner.v_x, prev_corner.v_y);
               }
            }
         curr_corner = prev_corner;
         }
      if (new_length < corner_arr.length)
         {
         // somthing skipped, update corner_list
         corner_list = new java.util.LinkedList<PlaPointInt>();
         for (int i = 0; i < new_length; ++i)
            {
            corner_list.add(corner_arr[i]);
            }
         }
      }

   /**
    * removes as many corners at the end of the corner list, so that closing the polygon will not create a concave corner
    */
   private void remove_concave_corners_at_close()
      {
      add_corner_for_snap_angle();
      if (corner_list.size() < 4)
         {
         return;
         }
      PlaPointInt[] corner_arr = new PlaPointInt[corner_list.size()];
      Iterator<PlaPointInt> it = corner_list.iterator();
      for (int i = 0; i < corner_arr.length; ++i)
         {
         corner_arr[i] = it.next();
         }
      int new_length = corner_arr.length;

      PlaPointInt first_corner = corner_arr[0];
      PlaPointInt second_corner = corner_arr[1];
      while (new_length > 3)
         {
         PlaPointInt last_corner = corner_arr[new_length - 1];
         if (last_corner.side_of(second_corner, first_corner) != PlaSide.ON_THE_LEFT)
            {
            break;
            }
         --new_length;
         }

      if (new_length != corner_arr.length)
         {
         // recalculate the corner_list
         corner_list = new java.util.LinkedList<PlaPointInt>();
         for (int i = 0; i < new_length; ++i)
            {
            corner_list.add(corner_arr[i]);
            }
         add_corner_for_snap_angle();
         }
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("creatig_tile"));
      }
   }
