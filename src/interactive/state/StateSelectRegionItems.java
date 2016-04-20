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
 * SelectItemsInRegionState.java
 *
 * Created on 9. November 2003, 12:02
 */
package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import planar.PlaPointFloat;
import planar.ShapeTileBox;
import planar.PlaPointInt;
import board.items.BrdItem;

/**
 * Interactive state for selecting all items in a rectangle.
 *
 * @author Alfons Wirtz
 */
public class StateSelectRegionItems extends StateSelectRegion
   {
   public static StateSelectRegionItems get_instance(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      return get_instance(null, p_parent_state, p_board_handling, p_logfile);
      }

   /**
    * Returns a new instance of this class with first point p_location.
    */
   public static StateSelectRegionItems get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      p_board_handling.display_layer_messsage();
      StateSelectRegionItems new_instance = new StateSelectRegionItems(p_parent_state, p_board_handling, p_logfile);
      new_instance.corner1 = p_location;
      if (new_instance.actlog != null)
         {
         new_instance.actlog.add_corner(p_location);
         }
      new_instance.i_brd.screen_messages.set_status_message(new_instance.resources.getString("drag_left_mouse_button_to_selects_items_in_region"));
      return new_instance;
      }

   /** Creates a new instance of SelectItemsInRegionState */
   private StateSelectRegionItems(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      if (actlog != null)
         {
         actlog.start_scope(LogfileScope.SELECT_REGION);
         }
      }

   @Override
   public StateInteractive complete()
      {
      if (!i_brd.is_board_read_only())
         {
         i_brd.screen_messages.set_status_message("");
         corner2 = i_brd.get_current_mouse_position();
         if (actlog != null)
            {
            actlog.add_corner(corner2);
            }
         this.select_all_in_region();
         }
      return this.return_state;
      }

   /**
    * Selects all items in the rectangle defined by corner1 and corner2.
    */
   private void select_all_in_region()
      {
      PlaPointInt p1 = this.corner1.round();
      PlaPointInt p2 = this.corner2.round();

      ShapeTileBox b = new ShapeTileBox(Math.min(p1.v_x, p2.v_x), Math.min(p1.v_y, p2.v_y), Math.max(p1.v_x, p2.v_x), Math.max(p1.v_y, p2.v_y));
      int select_layer;
      if (i_brd.itera_settings.select_on_all_visible_layers)
         {
         select_layer = -1;
         }
      else
         {
         select_layer = i_brd.itera_settings.layer_no;
         }
      Set<BrdItem> found_items = i_brd.itera_settings.item_selection_filter.filter(i_brd.get_routing_board().overlapping_items(b, select_layer));
      if (i_brd.itera_settings.select_on_all_visible_layers)
         {
         // remove items, which are not visible
         Set<BrdItem> visible_items = new TreeSet<BrdItem>();
         Iterator<BrdItem> it = found_items.iterator();
         while (it.hasNext())
            {
            BrdItem curr_item = it.next();
            for (int i = curr_item.first_layer(); i <= curr_item.last_layer(); ++i)
               {
               if (i_brd.gdi_context.get_layer_visibility(i) > 0)
                  {
                  visible_items.add(curr_item);
                  break;
                  }
               }
            }
         found_items = visible_items;
         }
      boolean something_found = (found_items.size() > 0);
      
      if (something_found)
         {
         if ( return_state instanceof StateSelectedItem)
            {
            ((StateSelectedItem) return_state).get_item_list().addAll(found_items);
            }
         else
            {
            return_state = StateSelectedItem.get_instance(found_items, this.return_state, i_brd, actlog);
            }
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("nothing_selected"));
         }
      }
   }
