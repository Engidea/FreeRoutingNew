package freert.rules;
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

import board.BrdLayerStructure;


/**
 * a single entry of the clearance matrix
 */
public class ClearanceMatrixEntry implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final BrdLayerStructure layer_structure;
   
   final int[] layer;

   public ClearanceMatrixEntry(BrdLayerStructure p_layer_structure)
      {
      layer_structure = p_layer_structure;
      
      layer = new int[layer_structure.size()];
      
      for (int i = 0; i < layer_structure.size(); ++i)
         {
         layer[i] = 0;
         }
      }

   /**
    * Returns thrue of all clearances values of this and p_other are equal.
    */
   boolean equals(ClearanceMatrixEntry p_other)
      {
      for (int i = 0; i < layer_structure.size(); ++i)
         {
         if (layer[i] != p_other.layer[i])
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Return true, if not all layer values are equal.
    */
   boolean is_layer_dependent()
      {
      int compare_value = layer[0];
      for (int index = 1; index < layer_structure.size(); ++index)
         {
         if (layer[index] != compare_value)
            {
            return true;
            }
         }
      return false;
      }
   }