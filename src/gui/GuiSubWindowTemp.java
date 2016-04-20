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
 * BoardTemporarySubWindow.java
 *
 * Created on 20. Juni 2005, 08:19
 *
 */

package gui;

import java.awt.event.WindowAdapter;

/**
 * Class for temporary sub windows of the board frame
 * This one is NOT stored on object stream
 * @author Alfons Wirtz
 */
public class GuiSubWindowTemp extends GuiSubWindow
   {
   protected final BoardFrame board_frame;

   public GuiSubWindowTemp(BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;
      
      p_board_frame.add_subwindow(this);

      addWindowListener(new WindowAdapter()
         {
         public void windowClosing(java.awt.event.WindowEvent evt)
            {
            dispose();
            }
         });
      }

   /** 
    * Used, when the board frame with all the subwindows is disposed
    * You should not mess up with the list of subwindows
    */
   public void board_frame_disposed()
      {
      super.dispose();
      }

   @Override
   public void dispose()
      {
      board_frame.remove_subwindow(this);
      super.dispose();
      }

   }
