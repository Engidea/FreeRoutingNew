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
 * LocationAndVisibilitySavableWindow.java
 *
 * Created on 20. Dezember 2004, 09:03
 */

package gui;

import gui.varie.WindowSavedAttributes;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Subwindow of the board frame, whose location and visibility can be saved and read from disc.
 * @author Alfons Wirtz
 */
public class GuiSubWindowSavable extends GuiSubWindow implements Serializable
   {
   private static final long serialVersionUID = 1L;

   protected final BoardFrame board_frame;

   public GuiSubWindowSavable ( BoardFrame p_board_frame )
      {
      board_frame = p_board_frame;
      }
   
   
   public final boolean debug ( int mask, int level )
      {
      return board_frame.debug(mask,level);
      }

   public final void userPrintln ( String message )
      {
      board_frame.userPrintln(message);
      }
   
   /**
    * Reads the data of this frame from disc. Returns false, if the reading failed.
    */
   public boolean read(ObjectInputStream p_object_stream)
      {
      try
         {
         WindowSavedAttributes saved_attributes = (WindowSavedAttributes) p_object_stream.readObject();
         setBounds(saved_attributes.bounds);
         setVisible(saved_attributes.is_visible);
         return true;
         }
      catch (Exception e)
         {
         System.out.println("SelectParameterWindow.read: read failed");
         return false;
         }
      }

   /**
    * Saves this frame to disk.
    */
   public void save(ObjectOutputStream p_object_stream)
      {
      try
         {
         WindowSavedAttributes saved_attributes = new WindowSavedAttributes(this.getBounds(), this.isVisible());
         p_object_stream.writeObject(saved_attributes);
         }
      catch (java.io.IOException e)
         {
         System.err.println("BoardSubWindow.save: save failed");
         }
      }

   /**
    * Refresh the displayed values in this window. 
    * To be overwritten where needed in derived classes.
    */
   public void refresh()
      {
      }
   }
