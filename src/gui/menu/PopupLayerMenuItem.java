package gui.menu;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

import gui.BoardFrame;
import gui.BoardPanel;
import gui.varie.GuiResources;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

public final class PopupLayerMenuItem extends JMenuItem
   {
   private static final long serialVersionUID = 1L;
   
   private final LocalActionListener listener = new LocalActionListener();
   
   private final BoardFrame board_frame;
   private final int layer_no;
   private final String message1;
   
   PopupLayerMenuItem(BoardFrame p_board_frame, int p_layer_no)
      {
      board_frame = p_board_frame;
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.Default");
      message1 = resources.getString("layer_changed_to") + " ";
      layer_no = p_layer_no;
      
      addActionListener(listener);
      }

private final class LocalActionListener implements ActionListener
   {
   public void actionPerformed(ActionEvent evt)
      {
      BoardPanel board_panel = board_frame.board_panel;
      
      if (board_panel.itera_board.change_layer_action(layer_no))
         {
         String layer_name = board_panel.itera_board.get_routing_board().layer_structure.get_name(layer_no);
         board_panel.screen_messages.set_status_message(message1 + layer_name);
         }
      // If change_layer failed the status message is set inside change_layer_action
      // because the information of the cause of the failing is missing here.
      board_panel.move_mouse(board_panel.right_button_click_location);
      }
   } 
   
   }
