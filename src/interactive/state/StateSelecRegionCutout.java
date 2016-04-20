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
 * CutoutRouteState.java
 *
 * Created on 5. Juni 2005, 07:13
 *
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import planar.PlaPointFloat;
import planar.ShapeTileBox;
import planar.PlaPointInt;
import board.items.BrdItem;
import board.items.BrdTracePolyline;

/**
 *
 * @author Alfons Wirtz
 */
public class StateSelecRegionCutout extends StateSelectRegion
   {
   public static StateSelecRegionCutout get_instance(Collection<BrdItem> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      return get_instance(p_item_list, null, p_parent_state, p_board_handling, p_logfile);
      }

   public static StateSelecRegionCutout get_instance(Collection<BrdItem> p_item_list, PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      p_board_handling.display_layer_messsage();
      // filter items, whichh cannnot be cutout
      Collection<BrdTracePolyline> item_list = new LinkedList<BrdTracePolyline>();

      for (BrdItem curr_item : p_item_list)
         {
         if (!curr_item.is_user_fixed() && curr_item instanceof BrdTracePolyline)
            {
            item_list.add((BrdTracePolyline) curr_item);
            }
         }

      StateSelecRegionCutout new_instance = new StateSelecRegionCutout(item_list, p_parent_state, p_board_handling, p_logfile);
      new_instance.corner1 = p_location;
      if (p_location != null && new_instance.actlog != null)
         {
         new_instance.actlog.add_corner(p_location);
         }
      new_instance.i_brd.screen_messages.set_status_message(new_instance.resources.getString("drag_left_mouse_button_to_select_cutout_rectangle"));
      return new_instance;
      }

   private StateSelecRegionCutout(Collection<BrdTracePolyline> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.CUTOUT_ROUTE);
         }
      this.trace_list = p_item_list;
      }
   @Override
   public StateInteractive complete()
      {
      i_brd.screen_messages.set_status_message("");
      corner2 = i_brd.get_current_mouse_position();
      if (actlog != null)
         {
         actlog.add_corner(corner2);
         }
      this.cutout_route();
      return this.return_state;
      }

   /**
    * Selects all items in the rectangle defined by corner1 and corner2.
    */
   private void cutout_route()
      {
      if (this.corner1 == null || this.corner2 == null)
         {
         return;
         }

      i_brd.get_routing_board().generate_snapshot();

      PlaPointInt p1 = this.corner1.round();
      PlaPointInt p2 = this.corner2.round();

      ShapeTileBox cut_box = new ShapeTileBox(Math.min(p1.v_x, p2.v_x), Math.min(p1.v_y, p2.v_y), Math.max(p1.v_x, p2.v_x), Math.max(p1.v_y, p2.v_y));

      Set<Integer> changed_nets = new TreeSet<Integer>();

      for (BrdTracePolyline curr_trace : this.trace_list)
         {
         board.shape.ShapeTraceEntries.cutout_trace(curr_trace, cut_box, 0);
         for (int i = 0; i < curr_trace.net_count(); ++i)
            {
            changed_nets.add(curr_trace.get_net_no(i));
            }
         }

      for (Integer changed_net : changed_nets)
         {
         i_brd.update_ratsnest(changed_net);
         }
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      if (trace_list == null)
         {
         return;
         }

      for (BrdTracePolyline curr_trace : this.trace_list)
         {

         curr_trace.draw(p_graphics, i_brd.gdi_context, i_brd.gdi_context.get_hilight_color(), i_brd.gdi_context.get_hilight_color_intensity());
         }
      super.draw(p_graphics);
      }

   private final Collection<BrdTracePolyline> trace_list;
   }
