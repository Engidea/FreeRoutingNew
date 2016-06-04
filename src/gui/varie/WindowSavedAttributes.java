package gui.varie;

import java.awt.Rectangle;

/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

/**
 * Type for attributes of this class, which are saved to an Objectstream.
 */
public class WindowSavedAttributes implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final Rectangle bounds;
   public final boolean is_visible;
   
   public WindowSavedAttributes(java.awt.Rectangle p_bounds, boolean p_is_visible)
      {
      bounds = p_bounds;
      is_visible = p_is_visible;
      }
   }
