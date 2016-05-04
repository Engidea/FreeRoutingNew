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
 * WindowRouteStubs.java
 *
 * Created on 17. Februar 2006, 07:16
 *
 */

package gui.win;

import freert.planar.PlaPointFloat;
import gui.BoardFrame;
import gui.varie.GuiResources;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import main.Stat;
import board.RoutingBoard;
import board.infos.BrdRouteStub;
import board.items.BrdItem;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowRouteStubs extends WindowObjectListWithFilter
   {
   private static final long serialVersionUID = 1L;
   
   private final Stat stat;

   private final GuiResources resources;
   
   public WindowRouteStubs(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      this.stat = stat;
      
      this.resources = new GuiResources(stat,"gui.resources.CleanupWindows");
      this.setTitle(resources.getString("route_stubs"));
      
      this.list_empty_message.setText(resources.getString("no_route_stubs_found"));
      p_board_frame.set_context_sensitive_help(this, "WindowObjectList_RouteStubs");
      }

   @Override
   protected void fill_list()
      {
      RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();

      SortedSet<BrdRouteStub> route_stub_info_set = new java.util.TreeSet<BrdRouteStub>();

      Collection<BrdItem> board_items = routing_board.get_items();
      for (BrdItem curr_item : board_items)
         {
         if (!(curr_item instanceof board.items.BrdTrace || curr_item instanceof board.items.BrdAbitVia))
            {
            continue;
            }
         if (curr_item.net_count() != 1)
            {
            continue;
            }

         PlaPointFloat stub_location;
         int stub_layer;
         if (curr_item instanceof board.items.BrdAbitVia)
            {
            Collection<BrdItem> contact_list = curr_item.get_all_contacts();
            if (contact_list.isEmpty())
               {
               stub_layer = curr_item.first_layer();
               }
            else
               {
               Iterator<BrdItem> it = contact_list.iterator();
               BrdItem curr_contact_item = it.next();
               int first_contact_first_layer = curr_contact_item.first_layer();
               int first_contact_last_layer = curr_contact_item.last_layer();
               boolean all_contacts_on_one_layer = true;
               while (it.hasNext())
                  {
                  curr_contact_item = it.next();
                  if (curr_contact_item.first_layer() != first_contact_first_layer || curr_contact_item.last_layer() != first_contact_last_layer)
                     {
                     all_contacts_on_one_layer = false;
                     break;
                     }
                  }
               if (!all_contacts_on_one_layer)
                  {
                  continue;
                  }
               if (curr_item.first_layer() >= first_contact_first_layer && curr_item.last_layer() <= first_contact_first_layer)
                  {
                  stub_layer = first_contact_first_layer;
                  }
               else
                  {
                  stub_layer = first_contact_last_layer;
                  }
               }
            stub_location = ((board.items.BrdAbitVia) curr_item).center_get().to_float();
            }
         else
            {
            board.items.BrdTrace curr_trace = (board.items.BrdTrace) curr_item;
            if (curr_trace.get_start_contacts().isEmpty())
               {
               stub_location = curr_trace.first_corner().to_float();
               }
            else if (curr_trace.get_end_contacts().isEmpty())
               {
               stub_location = curr_trace.corner_last().to_float();
               }
            else
               {
               continue;
               }
            stub_layer = curr_trace.get_layer();
            }
         BrdRouteStub curr_route_stub_info = new BrdRouteStub(stat,board_frame.board_panel.board_handling, curr_item, stub_location, stub_layer);
         route_stub_info_set.add(curr_route_stub_info);
         }

      for (BrdRouteStub curr_info : route_stub_info_set)
         {
         this.add_to_list(curr_info);
         }
      this.gui_list.setVisibleRowCount(Math.min(route_stub_info_set.size(), DEFAULT_TABLE_SIZE));
      }

   protected void select_instances()
      {
      @SuppressWarnings("deprecation")
      Object[] selected_list_values = gui_list.getSelectedValues();
      if (selected_list_values.length <= 0)
         {
         return;
         }
      Set<board.items.BrdItem> selected_items = new java.util.TreeSet<board.items.BrdItem>();
      for (int i = 0; i < selected_list_values.length; ++i)
         {
         selected_items.add(((BrdRouteStub) selected_list_values[i]).stub_item);
         }
      interactive.IteraBoard board_handling = board_frame.board_panel.board_handling;
      board_handling.select_items(selected_items);
      board_handling.zoom_selection();
      }
   }
