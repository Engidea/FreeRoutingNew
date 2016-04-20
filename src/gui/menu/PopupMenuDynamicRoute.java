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
 * RoutePopupMenu.java
 *
 * Created on 17. Februar 2005, 07:08
 */

package gui.menu;

import gui.BoardFrame;
import main.Stat;
import board.BrdLayer;

/**
 * Popup menu used in the interactive route state.
 *
 * @author Alfons Wirtz
 */
public class PopupMenuDynamicRoute extends PopupMenuDisplay
   {
   private static final long serialVersionUID = 1L;

   public PopupMenuDynamicRoute(Stat stat, BoardFrame p_board_frame)
      {
      super(stat, p_board_frame);

      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_board_frame.get_locale());
      board.BrdLayerStructure layer_structure = board_panel.board_handling.get_routing_board().layer_structure;

      javax.swing.JMenuItem end_route_item = new javax.swing.JMenuItem();
      end_route_item.setText(resources.getString("end_route"));
      end_route_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.return_from_state();
               }
         });

      add(end_route_item, 0);

      javax.swing.JMenuItem cancel_item = new javax.swing.JMenuItem();
      cancel_item.setText(resources.getString("cancel_route"));
      cancel_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.cancel_state();
               }
         });

      add(cancel_item, 1);

      javax.swing.JMenuItem snapshot_item = new javax.swing.JMenuItem();
      snapshot_item.setText(resources.getString("generate_snapshot"));
      snapshot_item.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_panel.board_handling.generate_snapshot();
               }
         });

      add(snapshot_item, 2);

      if (layer_structure.size() > 0)
         {
         change_layer_menu = new PopupMenuChangeLayer(p_board_frame);
         add(change_layer_menu, 0);
         }
      else
         {
         change_layer_menu = null;
         }

      int want_no = board_panel.board_handling.itera_settings.get_layer();
      
      BrdLayer curr_layer = layer_structure.get(want_no);
      
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

   private final PopupMenuChangeLayer change_layer_menu;
   }
