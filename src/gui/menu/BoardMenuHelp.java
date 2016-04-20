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
 * BoardMenuHelpReduced.java
 *
 * Created on 21. Oktober 2005, 09:06
 *
 */

package gui.menu;

import gui.BoardFrame;
import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Alfons Wirtz
 */
public final class BoardMenuHelp extends JMenu
   {
   private static final long serialVersionUID = 1L;

   protected final BoardFrame board_frame;

   public BoardMenuHelp(BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.BoardMenuHelp");
      
      setText(resources.getString("help"));

      JMenuItem about_window = resources.newJMenuItem("about",null,new PrivateListener());

      add(about_window);
      }
   
   private class PrivateListener implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         board_frame.about_window.setVisible(true);
         }
      }
   }
