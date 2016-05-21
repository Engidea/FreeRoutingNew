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
 * PackagesWindow.java
 *
 * Created on 7. Maerz 2005, 09:14
 */

package gui.win;

import freert.library.LibPackage;
import freert.library.LibPackages;
import gui.BoardFrame;

/**
 * Window displaying the library packagess.
 *
 * @author Alfons Wirtz
 */
public class WindowPackages extends WindowObjectListWithFilter
   {
   private static final long serialVersionUID = 1L;

   public WindowPackages(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
      this.setTitle(resources.getString("packages"));
      p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LibraryPackages");
      }

   /**
    * Fills the list with the library packages.
    */
   protected void fill_list()
      {
      LibPackages packages = this.board_frame.board_panel.board_handling.get_routing_board().brd_library.packages;
      LibPackage[] sorted_arr = new LibPackage[packages.pkg_count()];
      for (int i = 0; i < sorted_arr.length; ++i)
         {
         sorted_arr[i] = packages.pkg_get(i + 1);
         }
      java.util.Arrays.sort(sorted_arr);
      for (int i = 0; i < sorted_arr.length; ++i)
         {
         this.add_to_list(sorted_arr[i]);
         }
      this.gui_list.setVisibleRowCount(Math.min(packages.pkg_count(), DEFAULT_TABLE_SIZE));
      }

   protected void select_instances()
      {
      @SuppressWarnings("deprecation")
      Object[] selected_packages = gui_list.getSelectedValues();
      if (selected_packages.length <= 0)
         {
         return;
         }
      board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
      java.util.Set<board.items.BrdItem> board_instances = new java.util.TreeSet<board.items.BrdItem>();
      java.util.Collection<board.items.BrdItem> board_items = routing_board.get_items();
      for (board.items.BrdItem curr_item : board_items)
         {
         if (curr_item.get_component_no() > 0)
            {
            board.infos.BrdComponent curr_component = routing_board.brd_components.get(curr_item.get_component_no());
            LibPackage curr_package = curr_component.get_package();
            boolean package_matches = false;
            for (int i = 0; i < selected_packages.length; ++i)
               {
               if (curr_package == selected_packages[i])
                  {
                  package_matches = true;
                  break;
                  }
               }
            if (package_matches)
               {
               board_instances.add(curr_item);
               }
            }
         }
      board_frame.board_panel.board_handling.select_items(board_instances);
      board_frame.board_panel.board_handling.zoom_selection();
      }
   }
