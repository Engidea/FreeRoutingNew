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
 * ChangeLayerMenu.java
 *
 * Created on 17. Februar 2005, 08:58
 */

package gui.menu;

import gui.BoardFrame;
import gui.varie.GuiResources;
import javax.swing.JMenu;
import board.BrdLayerStructure;

/**
 * Used as submenu in a popup menu for change layer actions.
 *
 * @author Alfons Wirtz
 */
public final class PopupMenuChangeLayer extends JMenu
   {
   private static final long serialVersionUID = 1L;

   private final BoardFrame board_frame;

   private final PopupLayerMenuItem[] item_arr;

   public PopupMenuChangeLayer(BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;
      GuiResources resources = board_frame.newGuiResources("gui.resources.Default");

      BrdLayerStructure layer_structure = board_frame.board_panel.board_handling.get_routing_board().layer_structure;
      item_arr = new PopupLayerMenuItem[layer_structure.signal_layer_count()];

      setText(resources.getString("change_layer"));
      setToolTipText(resources.getString("change_layer_tooltip"));
      int curr_signal_layer_no = 0;
      
      for (int index = 0; index < layer_structure.size(); ++index)
         {
         if (layer_structure.is_signal(index))
            {
            item_arr[curr_signal_layer_no] = new PopupLayerMenuItem(board_frame, index);
            item_arr[curr_signal_layer_no].setText(layer_structure.get_name(index));
            add(item_arr[curr_signal_layer_no]);
            ++curr_signal_layer_no;
            }
         }
      }

   /**
    * Disables the item with index p_no and enables all other items.
    */
   public void disable_item(int p_no)
      {
      for (int i = 0; i < item_arr.length; ++i)
         {
         if (i == p_no)
            {
            item_arr[i].setEnabled(false);
            }
         else
            {
            item_arr[i].setEnabled(true);
            }
         }
      }
   }
