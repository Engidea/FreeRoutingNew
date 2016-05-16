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
 * WindowUnconnectedRoute.java
 *
 * Created on 16. Februar 2006, 06:20
 *
 */

package gui.win;

import gui.BoardFrame;
import gui.varie.GuiResources;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import main.Stat;
import board.RoutingBoard;
import board.infos.BrdUnconnectedRoute;
import board.items.BrdAbitVia;
import board.items.BrdItem;
import board.items.BrdTracep;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowUnconnectedRoute extends WindowObjectListWithFilter
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowUnconnectedRoute.";

   private final Stat stat;
   private final GuiResources resources;
   private int max_unconnected_route_info_id_no = 0;
   
   public WindowUnconnectedRoute(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      this.stat = stat;
      this.resources = new GuiResources(stat,"gui.resources.CleanupWindows");
      this.setTitle(resources.getString("unconnected_route"));
      this.list_empty_message.setText(resources.getString("no_unconnected_route_found"));
      p_board_frame.set_context_sensitive_help(this, "WindowObjectList_UnconnectedRoute");
      }

   @Override
   protected void fill_list()
      {
      RoutingBoard routing_board = this.board_frame.board_panel.board_handling.get_routing_board();

      Set<BrdItem> handled_items = new java.util.TreeSet<BrdItem>();

      SortedSet<BrdUnconnectedRoute> unconnected_route_info_set = new java.util.TreeSet<BrdUnconnectedRoute>();

      Collection<BrdItem> board_items = routing_board.get_items();
      for (BrdItem curr_item : board_items)
         {
         if (!(curr_item instanceof BrdTracep || curr_item instanceof BrdAbitVia))
            {
            // Skip what is not a trace or a via
            continue;
            }
         
         if (handled_items.contains(curr_item))
            {
            // skip what already checked
            continue;
            }
         
         Collection<BrdItem> curr_connected_set = curr_item.get_connected_set(-1);
         boolean terminal_item_found = false;
         
         for (BrdItem curr_connnected_item : curr_connected_set)
            {
            handled_items.add(curr_connnected_item);
            if (!(curr_connnected_item instanceof BrdTracep || curr_connnected_item instanceof board.items.BrdAbitVia))
               {
               terminal_item_found = true;
               }
            }
         
         if (!terminal_item_found)
            {
            // We have found unconnected route
            if (curr_item.net_count() == 1)
               {
               freert.rules.RuleNet curr_net = routing_board.brd_rules.nets.get(curr_item.get_net_no(0));
               if (curr_net != null)
                  {
                  BrdUnconnectedRoute curr_unconnected_route_info = new BrdUnconnectedRoute(resources, curr_net, curr_connected_set,max_unconnected_route_info_id_no++);
                  unconnected_route_info_set.add(curr_unconnected_route_info);
                  }
               }
            else
               {
               stat.userPrintln(classname+"fill_list: net_count 1 expected");
               }
            }
         }

      for (BrdUnconnectedRoute curr_info : unconnected_route_info_set)
         {
         this.add_to_list(curr_info);
         }
      
      this.gui_list.setVisibleRowCount(Math.min(unconnected_route_info_set.size(), DEFAULT_TABLE_SIZE));
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
         selected_items.addAll(((BrdUnconnectedRoute) selected_list_values[i]).item_list);
         }
      interactive.IteraBoard board_handling = board_frame.board_panel.board_handling;
      board_handling.select_items(selected_items);
      board_handling.zoom_selection();
      }
   }
