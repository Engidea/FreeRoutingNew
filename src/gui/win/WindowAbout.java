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
 * WindowAbout.java
 *
 * Created on 7. Juli 2005, 07:24
 *
 */

package gui.win;

import freert.main.MainApplication;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays general information about the freeroute software.
 *
 * @author Alfons Wirtz
 */
public class WindowAbout extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   public WindowAbout(BoardFrame p_board_frame)
      {
      super (p_board_frame);
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.WindowAbout");
      
      setTitle(resources.getString("title"));

      JPanel window_panel = new JPanel();
      add(window_panel);

      // Initialize gridbag layout.

      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      window_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.insets = new java.awt.Insets(5, 10, 5, 10);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;

      JLabel description_label = new JLabel(resources.getString("description"));
      gridbag.setConstraints(description_label, gridbag_constraints);
      window_panel.add(description_label, gridbag_constraints);

      String version_string = resources.getString("version") + " " + MainApplication.VERSION_NUMBER_STRING;
      JLabel version_label = new JLabel(version_string);
      gridbag.setConstraints(version_label, gridbag_constraints);
      window_panel.add(version_label, gridbag_constraints);

      JLabel warrenty_label = new JLabel(resources.getString("warranty"));
      gridbag.setConstraints(warrenty_label, gridbag_constraints);
      window_panel.add(warrenty_label, gridbag_constraints);

      JLabel homepage_label = new JLabel(resources.getString("homepage"));
      gridbag.setConstraints(homepage_label, gridbag_constraints);
      window_panel.add(homepage_label, gridbag_constraints);

      JLabel support_label = new JLabel(resources.getString("support"));
      gridbag.setConstraints(support_label, gridbag_constraints);
      window_panel.add(support_label, gridbag_constraints);

      add(window_panel);
      pack();
      }
   }
