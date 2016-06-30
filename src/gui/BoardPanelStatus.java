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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

      status_message = resources.newJLabel("status_line");
      status_message.setForeground(Color.BLUE);

      add(status_message,BorderLayout.LINE_START);
      
      JPanel center_panel=new JPanel(new FlowLayout(FlowLayout.LEFT,20,0));
      
      add_message = resources.newJLabel("additional_text_field");
      add_message.setMaximumSize(new Dimension(300, 14));

      center_panel.add(add_message);

      current_layer = resources.newJLabel("current_layer");

      center_panel.add(current_layer);
      
      add(center_panel,BorderLayout.CENTER);
      
      mouse_position = resources.newJLabel("(0,0)");
      mouse_position.setMaximumSize(new Dimension(170, 14));
      
      add(mouse_position,BorderLayout.LINE_END);
      }
   }
