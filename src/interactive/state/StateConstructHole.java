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
 * HoleConstructionState.java
 *
 * Created on 7. November 2003, 18:40
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Collection;
import java.util.Iterator;
import board.items.BrdArea;
import board.items.BrdItem;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.planar.PlaArea;
import freert.planar.PlaCircle;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PolylineArea;
import freert.planar.ShapePolygon;
import freert.planar.ShapePolyline;

/**
 * Interactive cutting a hole into an obstacle shape
 *
 * @author Alfons Wirtz
 */
public class StateConstructHole extends StateConstructCorner
   {
   /**
    * Returns a new instance of this class or null, if that was not possible with the input parameters. If p_logfile != null, the
    * construction of this hole is stored in a logfile.
    */
   public static StateConstructHole get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      StateConstructHole new_instance = new StateConstructHole(p_parent_state, p_board_handling, p_logfile);
      if (!new_instance.start_ok(p_location))
         {
         new_instance = null;
         }
      return new_instance;
      }

   private StateConstructHole(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      }

   /**
    * Looks for an obstacle area to modify Returns false, if it cannot find one.
    */
   private boolean start_ok(PlaPointFloat p_location)
      {
      PlaPointInt pick_location = p_location.round();
      ItemSelectionChoice[] selectable_choices = { ItemSelectionChoice.KEEPOUT, ItemSelectionChoice.VIA_KEEPOUT, ItemSelectionChoice.CONDUCTION };
      ItemSelectionFilter selection_filter = new ItemSelectionFilter(selectable_choices);
      Collection<BrdItem> found_items = r_brd.pick_items(pick_location, i_brd.itera_settings.layer_no, selection_filter);
      if (found_items.size() != 1)
         {
         i_brd.screen_messages.set_status_message(resources.getString("no_item_found_for_adding_hole"));
         return false;
         }
      board.items.BrdItem found_item = found_items.iterator().next();
      if (!(found_item instanceof BrdArea))
         {
         i_brd.screen_messages.set_status_message(resources.getString("no_obstacle_area_found_for_adding_hole"));
         return false;
         }
      item_to_modify = (BrdArea) found_item;
      if (item_to_modify.get_area() instanceof PlaCircle)
         {
         i_brd.screen_messages.set_status_message(resources.getString("adding_hole_to_circle_not_yet_implemented"));
         return false;
         }
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.ADDING_HOLE);
         }
      add_corner(p_location);
      return true;
      }

   /**
    * Adds a corner to the polygon of the the hole under construction.
    */
   public StateInteractive left_button_clicked(PlaPointFloat p_next_corner)
      {
      if (item_to_modify == null)
         {
         return return_state;
         }
      if (item_to_modify.get_area().contains(p_next_corner))
         {
         super.add_corner(p_next_corner);
         i_brd.repaint();
         }
      return this;
      }

   /**
    * adds the just constructed hole to the item under modification, if that is possible without clearance violations
    */
   @Override
   public StateInteractive complete()
      {
      if (item_to_modify == null)
         {
         return return_state;
         }
      add_corner_for_snap_angle();
      int corner_count = corner_list.size();
      boolean construction_succeeded = (corner_count > 2);
      ShapePolyline[] new_holes = null;
      ShapePolyline new_border = null;
      if (construction_succeeded)
         {
         PlaArea obs_area = item_to_modify.get_area();
         PlaShape[] old_holes = obs_area.get_holes();
         new_border = (ShapePolyline) obs_area.get_border();
         if (new_border == null)
            {
            construction_succeeded = false;
            }
         else
            {
            new_holes = new ShapePolyline[old_holes.length + 1];
            for (int i = 0; i < old_holes.length; ++i)
               {
               new_holes[i] = (ShapePolyline) old_holes[i];
               if (new_holes[i] == null)
                  {
                  construction_succeeded = false;
                  break;
                  }
               }
            }
         }
      if (construction_succeeded)
         {
         PlaPointInt[] new_hole_corners = new PlaPointInt[corner_count];
         Iterator<PlaPointInt> it = corner_list.iterator();
         for (int i = 0; i < corner_count; ++i)
            {
            new_hole_corners[i] = it.next();
            }
         new_holes[new_holes.length - 1] = new ShapePolygon(new_hole_corners);
         PolylineArea new_obs_area = new PolylineArea(new_border, new_holes);

         if (new_obs_area.split_to_convex() == null)
            {
            // shape is invalid, maybe it has selfintersections
            construction_succeeded = false;
            }
         else
            {
            observers_activated = !r_brd.observers_active();
            if (observers_activated)
               {
               r_brd.start_notify_observers();
               }
            r_brd.generate_snapshot();
            r_brd.remove_item(item_to_modify);
            r_brd.insert_obstacle(new_obs_area, item_to_modify.get_layer(), item_to_modify.clearance_class_no(), board.varie.ItemFixState.UNFIXED);
            if (observers_activated)
               {
               r_brd.end_notify_observers();
               observers_activated = false;
               }
            }
         }
      if (construction_succeeded)
         {
         i_brd.screen_messages.set_status_message(resources.getString("adding_hole_completed"));
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("adding_hole_failed"));
         }
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.COMPLETE_SCOPE);
         }
      return return_state;
      }

   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("adding_hole_to_obstacle_area"));
      }

   private BrdArea item_to_modify = null;
   }
