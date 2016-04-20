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
 * LayerStructure.java
 *
 * Created on 26. Mai 2004, 06:37
 */

package board;

/**
 * Describes the layer structure of the board.
 *
 * @author alfons
 */
public final class BrdLayerStructure implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   private final BrdLayer[] arr;

   public BrdLayerStructure(BrdLayer[] p_layer_arr)
      {
      arr = p_layer_arr;
      
      for (int index=0; index<arr.length; index++)
         {
         arr[index].layer_no = index;
         }
      
      }

   public int size()
      {
      return arr.length;
      }

   public BrdLayer get ( int layer_no )
      {
      return arr[layer_no];
      }

   
   public int get_first_signal_layer_no ( )
      {
      for (int index=0; index<arr.length; index++)
         {
         BrdLayer layer = get(index);
         
         if ( layer.is_signal ) return index;
         }
      
      return -1;
      }
   
   /**
    * Returns the index of the layer with the name p_name in the array arr, -1, if arr contains no layer with name p_name.
    */
   public int get_no(String p_name)
      {
      for (int index = 0; index < arr.length; ++index)
         {
         if (p_name.equals(arr[index].name))
            {
            return index;
            }
         }
      return -1;
      }

   /**
    * Returns the index of p_layer in the array arr, or -1, if arr does not contain p_layer.
    */
   public int get_no(BrdLayer p_layer)
      {
      for (int index = 0; index < arr.length; ++index)
         {
         if (p_layer == arr[index]) return index;
         }
      return -1;
      }

   public boolean is_signal ( int layer_no )
      {
      return arr[layer_no].is_signal;
      }
   
   
   public String get_name ( int layer_no )
      {
      return arr[layer_no].name;
      }
   
   /**
    * The next ones are used to fix the suboptimal handling of combo selection in the GUI
    */
   
   
   /**
    * Returns the count of signal layers of this layer_structure.
    */
   public int signal_layer_count()
      {
      int found_signal_layers = 0;

      for (int index = 0; index < arr.length; ++index)
         {
         if (arr[index].is_signal) found_signal_layers++;
         }

      return found_signal_layers;
      }

   /**
    * Gets the p_no-th signal layer of this layer structure.
    */
   public BrdLayer get_signal_layer(int p_no)
      {
      int found_signal_layers = 0;

      for (int index = 0; index < arr.length; ++index)
         {
         if (arr[index].is_signal)
            {
            if (p_no == found_signal_layers)
               {
               return arr[index];
               }
            ++found_signal_layers;
            }
         }
      return arr[arr.length - 1];
      }

   /**
    * Returns the count of signal layers with a smaller number than p_layer
    */
   public int get_signal_layer_no(BrdLayer p_layer)
      {
      int found_signal_layers = 0;
      
      for (int index = 0; index < arr.length; ++index)
         {
         if (arr[index] == p_layer)
            {
            return found_signal_layers;
            }
         
         if (arr[index].is_signal)
            {
            ++found_signal_layers;
            }
         }
      
      return -1;
      }

   /**
    * Gets the layer number of the p_signal_layer_no-th signal layer in this layer structure
    */
   public int get_layer_no(int p_signal_layer_no)
      {
      BrdLayer curr_signal_layer = get_signal_layer(p_signal_layer_no);
      
      return get_no(curr_signal_layer);
      }
   }
