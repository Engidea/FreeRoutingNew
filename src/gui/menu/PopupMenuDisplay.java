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
 * PopupMenuDisplay.java
 *
 * Created on 22. Mai 2005, 09:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gui.menu;

import freert.main.Stat;
import gui.BoardFrame;
import gui.BoardPanel;
import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Alfons Wirtz
 */
public class PopupMenuDisplay extends JPopupMenu
   {
   private static final long serialVersionUID = 1L;

   private final MenuHandler handler = new MenuHandler();

   protected final Stat stat;

   protected final BoardPanel board_panel;

   private JMenuItem center_display_item;
   private JMenuItem zoom_in_item;
   private JMenuItem zoom_out_item;

   public PopupMenuDisplay(Stat p_stat, BoardFrame p_board_frame)
      {
      stat = p_stat;
      board_panel = p_board_frame.board_panel;
      GuiResources resources = new GuiResources(p_stat, "gui.resources.Default");

      center_display_item = new javax.swing.JMenuItem();
      center_display_item.setText(resources.getString("center_display"));
      center_display_item.addActionListener(handler);
      this.add(center_display_item);

      javax.swing.JMenu zoom_menu = new javax.swing.JMenu();
      zoom_menu.setText(resources.getString("zoom"));

      zoom_in_item = new javax.swing.JMenuItem();
      zoom_in_item.setText(resources.getString("zoom_in"));
      center_display_item.addActionListener(handler);
      zoom_menu.add(zoom_in_item);

      zoom_out_item = new javax.swing.JMenuItem();
      zoom_out_item.setText(resources.getString("zoom_out"));
      center_display_item.addActionListener(handler);

      zoom_menu.add(zoom_out_item);

      this.add(zoom_menu);
      }

   private final class MenuHandler implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         Object source = evt.getSource();

         if (source == center_display_item)
            board_panel.center_display(board_panel.right_button_click_location);
         else if (source == zoom_in_item)
            board_panel.zoom_in(board_panel.right_button_click_location);
         else if (source == zoom_out_item)
            board_panel.zoom_out(board_panel.right_button_click_location);

         }
      }
   }
