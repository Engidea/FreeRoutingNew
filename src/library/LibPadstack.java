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
 * Padstack.java
 *
 * Created on 27. Mai 2004, 06:35
 */

package library;

import java.util.Collection;
import java.util.LinkedList;
import freert.planar.PlaDirection;
import freert.planar.PlaDirectionLong;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import board.infos.PrintableInfo;

/**
 * Describes padstack masks for pins or vias located at the origin.
 *
 * @author alfons
 */
public final class LibPadstack implements Comparable<LibPadstack>, PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
 
   public  final int pads_no;
   public  final String pads_name;
   private final ShapeConvex[] shapes;

   // true, if vias of the own net are allowed to overlap with this padstack 
   public final boolean attach_allowed;
   // If false, the layers of the padstack are mirrored, if it is placed on the back side. The default is false.
   public final boolean placed_absolute;
   
   /**
    * Creates a new Padstack with shape p_shapes[i] on layer i (0 <= i < p_shapes.length). 
    * p_attach_allowed indicates, if vias of the own net are allowed to overlap with this padstack 
    * If p_placed_absolute is false, the layers of the padstack are mirrored, if
    * it is placed on the back side. p_padstack_list is the list, where this padstack belongs to.
    */
   LibPadstack(String p_name, int p_no, ShapeConvex[] p_shapes, boolean p_attach_allowed, boolean p_placed_absolute )
      {
      pads_no = p_no;
      pads_name = p_name;
      shapes = p_shapes;
      attach_allowed = p_attach_allowed;
      placed_absolute = p_placed_absolute;
      }

   /**
    * Compares 2 padstacks by name. Useful for example to display padstacks in alphabetic order.
    */
   public int compareTo(LibPadstack p_other)
      {
      if ( p_other == null ) return 0;
      
      return pads_name.compareToIgnoreCase(p_other.pads_name);
      }

   /**
    * Gets the shape of this padstack on layer p_layer
    */
   public ShapeConvex get_shape(int p_layer)
      {
      if (p_layer < 0 || p_layer >= shapes.length)
         {
         System.out.println("Padstack.get_layer p_layer out of range");
         return null;
         }
      
      return shapes[p_layer];
      }

   /**
    * Returns the first layer of this padstack with a shape != null.
    */
   public int from_layer()
      {
      int result = 0;
      while (result < shapes.length && shapes[result] == null)
         {
         ++result;
         }
      return result;
      }

   /**
    * Returns the last layer of this padstack with a shape != null.
    */
   public int to_layer()
      {
      int result = shapes.length - 1;
      while (result >= 0 && shapes[result] == null)
         {
         --result;
         }
      return result;
      }

   /** Returns the layer ciount of the board of this padstack. */
   public int board_layer_count()
      {
      return shapes.length;
      }

   public String toString()
      {
      return pads_name;
      }

   /**
    * Calculates the allowed trace exit directions of the shape of this padstack on layer p_layer. If the length of the pad is
    * smaller than p_factor times the height of the pad, connection also to the long side is allowed.
    */
   public Collection<PlaDirectionLong> get_trace_exit_directions(int p_layer, double p_factor)
      {
      Collection<PlaDirectionLong> result = new LinkedList<PlaDirectionLong>();
      
      if (p_layer < 0 || p_layer >= shapes.length) return result;
      
      ShapeConvex curr_shape = shapes[p_layer];

      if (curr_shape == null) return result;
      
      if (!(curr_shape instanceof ShapeTileBox || curr_shape instanceof ShapeTileOctagon)) return result;

      ShapeTileBox curr_box = curr_shape.bounding_box();

      boolean all_dirs = false;
      if (Math.max(curr_box.width(), curr_box.height()) < p_factor * Math.min(curr_box.width(), curr_box.height()))
         {
         all_dirs = true;
         }

      if (all_dirs || curr_box.width() >= curr_box.height())
         {
         result.add(PlaDirection.RIGHT);
         result.add(PlaDirection.LEFT);
         }
      if (all_dirs || curr_box.width() <= curr_box.height())
         {
         result.add(PlaDirection.UP);
         result.add(PlaDirection.DOWN);
         }
      return result;
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("padstack") + " ");
      p_window.append_bold(pads_name);
      
      for (int index = 0; index < shapes.length; ++index)
         {
         if (shapes[index] != null)
            {
            p_window.newline();
            p_window.indent();
            p_window.append(shapes[index], p_locale);
//            p_window.append(" " + resources.getString("on_layer") + " ");
//            p_window.append(padstack_list.board_layer_structure.arr[index].name);
            }
         }
      p_window.newline();
      }
   }
