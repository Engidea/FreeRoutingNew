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
 * BoardStatusPanel.java
 *
 * Created on 16. Februar 2005, 08:11
 */

package gui;

import freert.main.Stat;
import gui.varie.GuiResources;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel at the lower border of the board frame containing amongst others the message line and the current layer and cursor
 * position.
 *
 * @author Alfons Wirtz
 */
public class BoardPanelStatus extends JPanel
   {
   private static final long serialVersionUID = 1L;

   public final JLabel status_message;
   public final JLabel add_message;
   public final JLabel current_layer;
   public final JLabel mouse_position;
   
   BoardPanelStatus(Stat p_stat)
      {
      GuiResources resources = new GuiResources(p_stat,"gui.resources.BoardPanelStatus");
      setLayout(new java.awt.BorderLayout());
      setPreferredSize(new java.awt.Dimension(300, 20));

      JPanel left_message_panel = new JPanel();
      left_message_panel.setLayout(new java.awt.BorderLayout());

      status_message = resources.newJLabel("status_line");
      status_message.setHorizontalAlignment(SwingConstants.CENTER);
      left_message_panel.add(status_message, java.awt.BorderLayout.CENTER);

      add_message = resources.newJLabel("additional_text_field");
      add_message.setMaximumSize(new java.awt.Dimension(300, 14));
      add_message.setMinimumSize(new java.awt.Dimension(140, 14));
      add_message.setPreferredSize(new java.awt.Dimension(180, 14));
      left_message_panel.add(add_message, java.awt.BorderLayout.EAST);

      this.add(left_message_panel, java.awt.BorderLayout.CENTER);

      JPanel right_message_panel = new JPanel();
      right_message_panel.setLayout(new java.awt.BorderLayout());

      right_message_panel.setMinimumSize(new java.awt.Dimension(200, 20));
      right_message_panel.setOpaque(false);
      right_message_panel.setPreferredSize(new java.awt.Dimension(450, 20));

      current_layer = resources.newJLabel("current_layer");
      right_message_panel.add(current_layer, java.awt.BorderLayout.CENTER);

      JPanel cursor_panel = new JPanel();
      cursor_panel.setLayout(new java.awt.BorderLayout());
      cursor_panel.setMinimumSize(new java.awt.Dimension(220, 14));
      cursor_panel.setPreferredSize(new java.awt.Dimension(220, 14));

      JLabel cursor = resources.newJLabel("cursor");
      cursor.setHorizontalAlignment(SwingConstants.CENTER);
      cursor.setMaximumSize(new java.awt.Dimension(100, 14));
      cursor.setMinimumSize(new java.awt.Dimension(50, 14));
      cursor.setPreferredSize(new java.awt.Dimension(50, 14));
      cursor_panel.add(cursor, java.awt.BorderLayout.WEST);

      mouse_position = resources.newJLabel("(0,0)");
      mouse_position.setMaximumSize(new java.awt.Dimension(170, 14));
      mouse_position.setPreferredSize(new java.awt.Dimension(170, 14));
      cursor_panel.add(mouse_position, java.awt.BorderLayout.EAST);

      right_message_panel.add(cursor_panel, java.awt.BorderLayout.EAST);

      this.add(right_message_panel, java.awt.BorderLayout.EAST);
      }
   }
