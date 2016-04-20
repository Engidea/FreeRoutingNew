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
 * BoardWindowsMenu.java
 *
 * Created on 12. Februar 2005, 06:08
 */

package gui.menu;

import gui.BoardFrame;
import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Creates the parameter menu of a board frame.
 * @author Alfons Wirtz
 */
public final class BoardMenuParameter extends JMenu
   {
   private static final long serialVersionUID = 1L;

   private final MenuHandler menuHandler = new MenuHandler();

   private final BoardFrame board_frame;
   private final GuiResources resources;
   
   private JMenuItem selectwindow;
   private JMenuItem routewindow;
   private JMenuItem autoroutewindow;
   private JMenuItem movewindow;
   private JMenuItem unitwindow;

   private JMenuItem newMenu(String key)
      {
      JMenuItem item = new JMenuItem();
      item.setText(resources.getString(key));
      item.addActionListener(menuHandler);
      add(item);
      return item;
      }

   public BoardMenuParameter(BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;
      resources = new GuiResources(board_frame.stat, "gui.resources.BoardMenuParameter");

      setText(resources.getString("parameter"));

      selectwindow = newMenu("select");
      routewindow = newMenu("route");
      autoroutewindow = newMenu("autoroute");
      movewindow = newMenu("move");
      unitwindow = newMenu("UnitMeasure");
      }


   private final class MenuHandler implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         Object source = evt.getSource();

         if (source == selectwindow)
            board_frame.select_parameter_window.setVisible(true);
         else if (source == routewindow)
            board_frame.route_parameter_window.setVisible(true);
         else if (source == autoroutewindow)
            board_frame.autoroute_parameter_window.setVisible(true);
         else if (source == movewindow)
            board_frame.move_parameter_window.setVisible(true);
         else if (source == unitwindow)
            board_frame.unit_parameter_window.setVisible(true);
         }
      }

   }
