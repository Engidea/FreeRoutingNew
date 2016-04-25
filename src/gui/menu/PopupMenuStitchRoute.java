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
 * PopupMenuStitchRoute.java
 *
 * Created on 7. Februar 2006, 08:10
 *
 */

package gui.menu;

import gui.BoardFrame;
import main.Stat;
import board.BrdLayer;

/**
 *
 * @author Alfons Wirtz
 */
public class PopupMenuStitchRoute extends PopupMenuDisplay
   {
   private static final long serialVersionUID = 1L;

   private final PopupMenuChangeLayer change_layer_menu;
   
   public PopupMenuStitchRoute(Stat stat, BoardFrame p_board_frame)
      {
      super(stat, p_board_frame);
      board.BrdLayerStructure layer_structure = board_panel.board_handling.get_routing_board().layer_structure;

      if (layer_structure.size() > 0)
         {
         change_layer_menu = new PopupMenuChangeLayer(p_board_frame);
         add(change_layer_menu, 0);
         }
      else
         {
         change_layer_menu = null;
         }
      
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
      javax.swing.JMenuItem insert_item = new javax.swing.JMenuItem();
      insert_item.setText(resources.getString("insert"));
      insert_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.left_button_clicked(board_panel.right_button_click_location);
               }
         });

      add(insert_item, 0);

      javax.swing.JMenuItem done_item = new javax.swing.JMenuItem();
      done_item.setText(resources.getString("done"));
      done_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.return_from_state();
               }
         });

      add(done_item, 1);

      javax.swing.JMenuItem cancel_item = new javax.swing.JMenuItem();
      cancel_item.setText(resources.getString("cancel"));
      cancel_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.cancel_state();
               }
         });

      add(cancel_item, 2);

      BrdLayer curr_layer = layer_structure.get(board_panel.board_handling.itera_settings.get_layer_no());
      disable_layer_item(layer_structure.get_signal_layer_no(curr_layer));
      }

   /**
    * Disables the p_no-th item in the change_layer_menu.
    */
   public void disable_layer_item(int p_no)
      {
      if (change_layer_menu != null)
         {
         change_layer_menu.disable_item(p_no);
         }
      }
   }
