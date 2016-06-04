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
 * Layers of the board layer structure plus layer "all". 
 * Index is the layer number in the board layer structure or -1 for layer "all".
 */
public class GuiLayer
   {
   public final String name;

   // The index in the board layer_structure, -1 for the layers with name "all" or "inner"
   public final int index;

   public GuiLayer(String p_name, int p_index)
      {
      name = p_name;
      index = p_index;
      }

   public String toString()
      {
      return name;
      }
   }
