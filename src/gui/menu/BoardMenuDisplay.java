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
 * BoardDisplayMenu.java
 *
 * Created on 12. Februar 2005, 05:42
 */

package gui.menu;

import gui.BoardFrame;
import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import main.Stat;

/**
 * Creates the display menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public final class BoardMenuDisplay extends JMenu
   {
   private static final long serialVersionUID = 1L;

   private final MenuHandler listener = new MenuHandler();

   private final BoardFrame board_frame;

   private JMenuItem itemvisibility, layervisibility, colors, miscellanious, debug_config;

   public BoardMenuDisplay(Stat stat, BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;

      GuiResources resources = new GuiResources(stat, "gui.resources.BoardMenuDisplay");

      setText(resources.getString("display"));

      itemvisibility = resources.newJMenuItem("object_visibility", "object_visibility_tooltip", listener);
      add(itemvisibility);

      layervisibility = resources.newJMenuItem("layer_visibility", "layer_visibility_tooltip", listener);
      add(layervisibility);

      colors = resources.newJMenuItem("colors", "colors_tooltip", listener);
      add(colors);

      miscellanious =resources.newJMenuItem("miscellaneous", "miscellaneous_tooltip", listener);
      add(miscellanious);

      debug_config = resources.newJMenuItem( "debug_config", "debug_config_tooltip", listener);
      add(debug_config);
      }

   private final class MenuHandler implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         Object source = evt.getSource();

         if (source == itemvisibility)
            board_frame.object_visibility_window.setVisible(true);
         else if (source == layervisibility)
            board_frame.layer_visibility_window.setVisible(true);
         else if (source == colors)
            board_frame.color_manager.setVisible(true);
         else if (source == miscellanious)
            board_frame.display_misc_window.setVisible(true);
         else if (source == debug_config)
            board_frame.debug_config.setVisible(true);
         }
      }
   }
