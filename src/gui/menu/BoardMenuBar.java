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
 * BoardMenuBar.java
 *
 * Created on 11. Februar 2005, 10:17
 */

package gui.menu;

import gui.BoardFrame;
import javax.swing.JMenu;
import main.Stat;

/**
 * Creates the menu bar of a board frame together with its menu items.
 *
 * @author Alfons Wirtz
 */
public final class BoardMenuBar extends javax.swing.JMenuBar
   {
   private static final long serialVersionUID = 1L;

   private final BoardMenuFile file_menu;

   public BoardMenuBar (Stat stat, BoardFrame p_board_frame)
      {
      file_menu = new BoardMenuFile(stat, p_board_frame);
      add(file_menu);
      
      JMenu display_menu = new BoardMenuDisplay(stat, p_board_frame);
      add(display_menu);
      
      javax.swing.JMenu parameter_menu = new BoardMenuParameter(p_board_frame);
      add(parameter_menu);
      javax.swing.JMenu rules_menu = BoardMenuRules.get_instance(p_board_frame);
      add(rules_menu);
      javax.swing.JMenu info_menu = BoardMenuInfo.get_instance(p_board_frame);
      add(info_menu);
      javax.swing.JMenu other_menu = BoardMenuOther.get_instance(p_board_frame);
      add(other_menu);
      javax.swing.JMenu help_menu = new BoardMenuHelpReduced(p_board_frame);
      add(help_menu);
      }

   public void add_design_dependent_items()
      {
      file_menu.add_design_dependent_items();
      }

   }
