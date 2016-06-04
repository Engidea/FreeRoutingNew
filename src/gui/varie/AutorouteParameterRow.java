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
 * TODO merge this info into the BrdLayer
 * @author damiano
 *
 */
public class AutorouteParameterRow
   {
   public static final int PFDIR_horizontal=1;
   public static final int PFDIR_vertical=2;
   
   public final int signal_layer_no;

   public String signal_layer_name;
   public int signal_layer_pfdir;
   public boolean signal_layer_active;
   
   public AutorouteParameterRow(int p_layer_no)
      {
      signal_layer_no = p_layer_no;
      }
   
   public boolean isHorizontal ()
      {
      return signal_layer_pfdir == PFDIR_horizontal;
      }
   }
