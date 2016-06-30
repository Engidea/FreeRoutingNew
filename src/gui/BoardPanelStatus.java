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

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import freert.main.Stat;
import gui.varie.GuiResources;

/**
 * Panel at the lower border of the board frame containing amongst others the message line and the current layer and cursor
 * position.
 *
 * @author Alfons Wirtz
 */
public final class BoardPanelStatus extends JPanel
   {
   private static final long serialVersionUID = 1L;

   public final JLabel status_message;
   public final JLabel add_message;
   public final JLabel current_layer;
   public final JLabel mouse_position;
   
   BoardPanelStatus(Stat p_stat)
      {
      GuiResources resources = new GuiResources(p_stat,"gui.resources.BoardPanelStatus");
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(300, 20));

      JPanel left_message_panel = new JPanel();
      left_message_panel.setLayout(new BorderLayout());

      status_message = resources.newJLabel("status_line");
      status_message.setHorizontalAlignment(SwingConstants.CENTER);
      left_message_panel.add(status_message, BorderLayout.CENTER);

      add_message = resources.newJLabel("additional_text_field");
      add_message.setMaximumSize(new Dimension(300, 14));
      add_message.setMinimumSize(new Dimension(140, 14));
      add_message.setPreferredSize(new Dimension(180, 14));
      left_message_panel.add(add_message, BorderLayout.EAST);

      add(left_message_panel, BorderLayout.CENTER);

      JPanel right_message_panel = new JPanel();
      right_message_panel.setLayout(new BorderLayout());

      right_message_panel.setMinimumSize(new Dimension(200, 20));
      right_message_panel.setOpaque(false);
      right_message_panel.setPreferredSize(new Dimension(450, 20));

      current_layer = resources.newJLabel("current_layer");
      right_message_panel.add(current_layer, BorderLayout.CENTER);

      JPanel cursor_panel = new JPanel();
      cursor_panel.setLayout(new BorderLayout());
      cursor_panel.setMinimumSize(new Dimension(220, 14));
      cursor_panel.setPreferredSize(new Dimension(220, 14));

      JLabel cursor = resources.newJLabel("cursor");
      cursor.setHorizontalAlignment(SwingConstants.CENTER);
      cursor.setMaximumSize(new Dimension(100, 14));
      cursor.setMinimumSize(new Dimension(50, 14));
      cursor.setPreferredSize(new Dimension(50, 14));
      cursor_panel.add(cursor, BorderLayout.WEST);

      mouse_position = resources.newJLabel("(0,0)");
      mouse_position.setMaximumSize(new Dimension(170, 14));
      mouse_position.setPreferredSize(new Dimension(170, 14));
      cursor_panel.add(mouse_position, BorderLayout.EAST);

      right_message_panel.add(cursor_panel, BorderLayout.EAST);

      add(right_message_panel, BorderLayout.EAST);
      }
   }
