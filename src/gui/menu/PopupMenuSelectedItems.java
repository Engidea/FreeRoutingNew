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
 * SelectedItemPopupMenu.java
 *
 * Created on 17. Februar 2005, 07:47
 */

package gui.menu;

import gui.BoardFrame;
import gui.varie.GuiResources;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import main.Ldbg;
import main.Mdbg;
import main.Stat;

/**
 * Popup menu used in the interactive selected item state..
 *
 * @author Alfons Wirtz
 */
public class PopupMenuSelectedItems extends PopupMenuDisplay
   {
   private static final long serialVersionUID = 1L;
   private final MenuAction menuAction = new MenuAction();
   
   private final JMenuItem copy_item,move_item;
   
   public PopupMenuSelectedItems(Stat stat, BoardFrame p_board_frame)
      {
      super(stat, p_board_frame);

      GuiResources resources = p_board_frame.newGuiResources("gui.resources.Default");
      
      copy_item = resources.newJMenuItem("copy",null,menuAction);

      if ( ! board_panel.debug(Mdbg.GUI_MENU, Ldbg.RELEASE))
         {
         // copy is added only when NOT in release mode
         add(copy_item);
         }

      move_item = resources.newJMenuItem("move",null,menuAction);

      add(move_item);
      }
   
private class MenuAction implements ActionListener
   {
   @Override
   public void actionPerformed(ActionEvent event)
      {
      Object a_menu = event.getSource();
      
      if ( a_menu == copy_item )
         board_panel.itera_board.copy_selected_items(board_panel.right_button_click_location);
      else if ( a_menu == move_item )
         board_panel.itera_board.move_selected_items(board_panel.right_button_click_location);
      }
   }
   
   }
