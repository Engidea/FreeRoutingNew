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

import freert.main.Stat;
import gui.BoardFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 * Creates the menu bar of a board frame together with its menu items.
 *
 * @author Alfons Wirtz
 */
public final class BoardMenuBar extends JMenuBar
   {
   private static final long serialVersionUID = 1L;

   public BoardMenuBar (Stat stat, BoardFrame p_board_frame)
      {
      add(new BoardMenuFile(stat, p_board_frame));
      
      add(new BoardMenuDisplay(stat, p_board_frame));
      
      add(new BoardMenuParameter(p_board_frame));
      
      JMenu rules_menu = BoardMenuRules.get_instance(p_board_frame);
      add(rules_menu);
      JMenu info_menu = BoardMenuInfo.get_instance(p_board_frame);
      add(info_menu);

      add(new BoardMenuOther(p_board_frame));
      
      JMenu help_menu = new BoardMenuHelp(p_board_frame);

      p_board_frame.gui_help.add_menu_items(help_menu);

      add(help_menu);
      }

   }
