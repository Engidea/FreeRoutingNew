package gui.varie;

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
 * Information to be stored in a SnapShot.
 */
public final class SnapSelection implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String filter;
   public final int[] selected_indices;

   public SnapSelection(String p_filter, int[] p_selected_indices)
      {
      filter = p_filter;
      selected_indices = p_selected_indices;
      }

   }
