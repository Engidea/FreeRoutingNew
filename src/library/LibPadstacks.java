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
 * Padstacks.java
 *
 * Created on 3. Juni 2004, 09:42
 */

package library;

import java.util.Vector;
import planar.ShapeConvex;
import board.BrdLayerStructure;

/**
 * Describes a library of padstacks for pins or vias.
 *
 * @author alfons
 */
public final class LibPadstacks implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final Vector<LibPadstack> padstack_list = new Vector<LibPadstack>();
   // The layer structure of each padstack
   public final BrdLayerStructure board_layer_structure;
   
   public LibPadstacks(BrdLayerStructure p_layer_structure)
      {
      board_layer_structure = p_layer_structure;
      }

   /**
    * Returns the padstack with the input name or null, if no such padstack exists.
    */
   public LibPadstack get(String p_name)
      {
      if ( p_name == null ) return null;
      
      for ( LibPadstack curr_padstack : padstack_list )
         {
         if ( curr_padstack.pads_name.compareToIgnoreCase(p_name) == 0) return curr_padstack;
         }

      return null;
      }

   /**
    * Returns the count of Padstacks in this object.
    */
   public int count()
      {
      return padstack_list.size();
      }

   /**
    * Returns the padstack with index p_padstack_no for 1 <= p_padstack_no <= padstack_count
    */
   public LibPadstack get(int p_padstack_no)
      {
      if (p_padstack_no <= 0 || p_padstack_no > padstack_list.size())
         {
         System.err.println("Padstacks.get: 1 <= p_padstack_no <= " + count() + " expected");
         return null;
         }

      LibPadstack result = padstack_list.elementAt(p_padstack_no - 1);

      if ( result.pads_no != p_padstack_no)
         throw new IllegalArgumentException("Padstacks.get: inconsistent padstack number");

      return result;
      }

   /**
    * Appends a new padstack with the input shapes to this padstacks. p_shapes is an array of dimension board layer_count.
    * p_attach_allowed indicates if vias of the own net are allowed to overlap with this padstack 
    * If p_placed_absolute is false, the layers of the padstack are mirrored, if it is placed on the back side.
    */
   public LibPadstack add(String p_name, ShapeConvex[] p_shapes, boolean p_attach_allowed, boolean p_placed_absolute)
      {
      int pad_no = count()+1;
      
      LibPadstack new_padstack = new LibPadstack(p_name, pad_no, p_shapes, p_attach_allowed, p_placed_absolute);
      
      padstack_list.add(new_padstack);
      
      return new_padstack;
      }

   /**
    * Appends a new padstack with the input shapes to this padstacks. p_shapes is an array of dimension board layer_count. The
    * padatack name is generated internally.
    */
   public LibPadstack add(ShapeConvex[] p_shapes)
      {
      int pad_no = count()+1;

      String new_name = "padstack#" + pad_no;
      
      return add(new_name, p_shapes, false, false);
      }

   /**
    * Appends a new padstack withe the input shape from p_from_layer to p_to_layer and null on the other layers. 
    * The padatack name is generated internally.
    * in this case the number of layers is the same as the number of shapes
    */
   public LibPadstack add(ShapeConvex p_shape, int p_from_layer, int p_to_layer)
      {
      ShapeConvex[] shape_arr = new ShapeConvex[board_layer_structure.size()];
      
      int from_layer = Math.max(p_from_layer, 0);
      int to_layer = Math.min(p_to_layer, board_layer_structure.size() - 1);

      for (int index = from_layer; index <= to_layer; ++index)
         {
         shape_arr[index] = p_shape;
         }
      
      return add(shape_arr);
      }
   }
