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
 * SelectedItemPopupMenu.java
 *
 * Created on 17. Februar 2005, 07:47
 */

package gui.menu;

import gui.BoardFrame;
import javax.swing.JMenuItem;
import main.Stat;

/**
 * Popup menu used in the interactive selected item state..
 *
 * @author Alfons Wirtz
 */
public class PopupMenuSelectedItems extends PopupMenuDisplay
   {
   private static final long serialVersionUID = 1L;

   public PopupMenuSelectedItems(Stat stat, BoardFrame p_board_frame)
      {
      super(stat, p_board_frame);
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
      JMenuItem copy_item = new JMenuItem();
      copy_item.setText(resources.getString("copy"));
      copy_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.copy_selected_items(board_panel.right_button_click_location);
               }
         });

      if (board_panel.board_handling.get_routing_board().get_test_level() != board.varie.TestLevel.RELEASE_VERSION)
         {
         this.add(copy_item);
         }

      JMenuItem move_item = new JMenuItem();
      move_item.setText(resources.getString("move"));
      move_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.move_selected_items(board_panel.right_button_click_location);
               }
         });

      this.add(move_item, 0);
      }
   }
