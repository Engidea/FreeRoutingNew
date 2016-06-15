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
 */

package freert.rules;

import board.BrdLayerStructure;

/**
 *
 * NxN Matrix describing the spacing restrictions between N clearance classes on a fixed set of layers.
 * This really needs to be made clearer, using arrays is just.... weird...
 * It is easy to use a List, so, you do not need to reallocate bits and pieces when one item is added or removed
 * However, the question is, is the clearance index used around in the system ? if so, does it need to retain the index ?
 * It seems so... therefore, it is ok to add a class at the end, since previous numbers are retained BUT to delete you 
 * cannot just delete rows, you should make them null, so indices are retained TODO
 * @author Alfons Wirtz
 */

public final class ClearanceMatrix implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // count of clearance classes, should be the same as size of clearance_row
   private int class_count;

   private final BrdLayerStructure layer_structure;
   private ClearanceMatrixRow[] clearance_rows;  // vector of class_count rows of the clearance matrix
   private int[] max_value_on_layer;       // maximum clearance value for each layer
   
   /**
    * Creates a new instance with the 2 clearance classes "none" and "default" ans initializes it with p_default_value.
    */
   public static ClearanceMatrix get_default_instance(BrdLayerStructure p_layer_structure, int p_default_value)
      {
      String[] name_arr = new String[2];
      name_arr[0] = "null";
      name_arr[1] = "default";
      ClearanceMatrix result = new ClearanceMatrix(2, p_layer_structure, name_arr);
      result.set_default_value(p_default_value);
      return result;
      }

   final BrdLayerStructure get_layer_structure ()
      {
      return layer_structure;
      }
   
   /**
    * Creates a new instance for p_class_count clearance classes on p_layer_count layers. p_names is an array of dimension
    * p_class_count;
    */
   public ClearanceMatrix(int p_class_count, BrdLayerStructure p_layer_structure, String[] p_name_arr)
      {
      class_count = Math.max(p_class_count, 1);
      layer_structure = p_layer_structure;
      clearance_rows = new ClearanceMatrixRow[class_count];
      
      for (int index = 0; index < class_count; ++index)
         {
         clearance_rows[index] = new ClearanceMatrixRow(this, p_name_arr[index]);
         }
      
      max_value_on_layer = new int[layer_structure.size()];
      }

   /**
    * Returns the number of the clearance class with the input name, or -1, if no such clearance class exists.
    */
   public int get_no(String p_name)
      {
      for (int i = 0; i < class_count; ++i)
         {
         if (clearance_rows[i].name.compareToIgnoreCase(p_name) == 0)
            {
            return i;
            }
         }
      return -1;
      }

   /**
    * Gets the name of the clearance class with the input number.
    */
   public String get_name(int p_cl_class)
      {
      if (p_cl_class < 0 || p_cl_class >= clearance_rows.length)
         {
         System.out.println("CleatranceMatrix.get_name: p_cl_class out of range");
         return null;
         }
      return clearance_rows[p_cl_class].name;
      }

   /**
    * Sets the value of all clearance classes with number >= 1 to p_value on all layers.
    */
   public void set_default_value(int p_value)
      {
      for (int index = 0; index < layer_structure.size(); ++index)
         {
         set_default_value(index, p_value);
         }
      }

   /**
    * Sets the value of all clearance classes with number >= 1 to p_value on p_layer.
    */
   public void set_default_value(int p_layer, int p_value)
      {
      for (int index = 1; index < class_count; ++index)
         {
         for (int jndex = 1; jndex < class_count; ++jndex)
            {

            set_value(index, jndex, p_layer, p_value);
            }
         }
      }

   /**
    * Sets the value of an entry in the clearance matrix to p_value on all layers.
    */
   public void set_value(int p_i, int p_j, int p_value)
      {
      for (int layer = 0; layer < layer_structure.size(); ++layer)
         {
         set_value(p_i, p_j, layer, p_value);
         }
      }

   /**
    * Sets the value of an entry in the clearance matrix to p_value on all inner layers.
    */
   public void set_inner_value(int p_i, int p_j, int p_value)
      {
      for (int layer = 1; layer < layer_structure.size() - 1; ++layer)
         {
         set_value(p_i, p_j, layer, p_value);
         }
      }

   /**
    * Sets the value of an entry in the clearance matrix to p_value.
    * damiano Why does clearance needs to be even ? and what happens if I set it trough the GUI ?
    */
   public void set_value(int p_i, int p_j, int p_layer, int p_value)
      {
      ClearanceMatrixRow curr_row = clearance_rows[p_j];
      ClearanceMatrixEntry curr_entry = curr_row.column[p_i];
      // assure, that the clearance value is even
      int value = Math.max(p_value, 0);
      value += value % 2;
      
      curr_entry.layer[p_layer] = value;
      curr_row.max_value[p_layer] = Math.max(curr_row.max_value[p_layer], p_value);
      max_value_on_layer[p_layer] = Math.max(max_value_on_layer[p_layer], p_value);
      }

   /**
    * This value will be always an even integer.
    * @return the required spacing of clearance classes with index p_i and p_j on p_layer. 
    */
   public final int value_at(int class_i, int class_j, int p_layer)
      {
      try
         {
         return clearance_rows[class_j].column[class_i].layer[p_layer];
         }
      catch ( Exception exc )
         {
         return 0;
         }
      }

   /**
    * Returns the maximal required spacing of clearance class with index p_i to all other clearance classes on layer p_layer.
    */
   public int max_value(int p_i, int p_layer)
      {
      int i = Math.max(p_i, 0);
      i = Math.min(i, class_count - 1);
      int layer = Math.max(p_layer, 0);
      layer = Math.min(layer, layer_structure.size() - 1);
      return clearance_rows[i].max_value[layer];
      }

   public int max_value(int p_layer)
      {
      int layer = Math.max(p_layer, 0);
      layer = Math.min(layer, layer_structure.size() - 1);
      return max_value_on_layer[layer];
      }

   /**
    * Returns true, if the values of the clearance matrix in the p_i-th column and the p_j-th row are not equal on all layers.
    */
   public boolean is_layer_dependent(int p_i, int p_j)
      {
      int compare_value = clearance_rows[p_j].column[p_i].layer[0];
      for (int l = 1; l < layer_structure.size(); ++l)
         {
         if (clearance_rows[p_j].column[p_i].layer[l] != compare_value)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * Returns true, if the values of the clearance matrix in the p_i-th column and the p_j-th row are not equal on all inner layers.
    */
   public boolean is_inner_layer_dependent(int p_i, int p_j)
      {
      if (layer_structure.size() <= 2)
         {
         return false; // no inner layers
         }
      int compare_value = clearance_rows[p_j].column[p_i].layer[1];
      for (int l = 2; l < layer_structure.size() - 1; ++l)
         {
         if (clearance_rows[p_j].column[p_i].layer[l] != compare_value)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * Returns the row with index p_no
    */
   public ClearanceMatrixRow get_row(int p_no)
      {
      if (p_no < 0 || p_no >= clearance_rows.length)
         {
         System.out.println("ClearanceMatrix.get_row: p_no out of range");
         return null;
         }
      
      return clearance_rows[p_no];
      }

   public int get_class_count()
      {
      return class_count;
      }

   /**
    * @return the layer count of this clearance matrix
    */
   public int get_layer_count()
      {
      return layer_structure.size();
      }

   /**
    * Return the clearance compensation value of p_clearance_class_no on layer p_layer.
    */
   public int clearance_compensation_value(int p_clearance_class_no, int p_layer)
      {
      return ( value_at(p_clearance_class_no, p_clearance_class_no, p_layer) + 1) / 2;
      }

   /**
    * Appends a new clearance class to the clearance matrix and initializes it with the values of the default class. 
    * @returns false, if a clearance class with name p_class_name is already existing.
    */
   public boolean append_class(String p_class_name)
      {
      if (get_no(p_class_name) >= 0) return false;
   
      int old_class_count = class_count;
      
      ++class_count;

      ClearanceMatrixRow[] new_row = new ClearanceMatrixRow[class_count];

      // append a matrix entry to each old row
      for (int i = 0; i < old_class_count; ++i)
         {
         ClearanceMatrixRow curr_old_row = clearance_rows[i];
         new_row[i] = new ClearanceMatrixRow(this, curr_old_row.name);
         ClearanceMatrixRow curr_new_row = new_row[i];
         curr_new_row.max_value = curr_old_row.max_value;
         for (int j = 0; j < old_class_count; ++j)
            {
            curr_new_row.column[j] = curr_old_row.column[j];
            }

         curr_new_row.column[old_class_count] = new ClearanceMatrixEntry(layer_structure);
         }

      // append the new row

      new_row[old_class_count] = new ClearanceMatrixRow(this, p_class_name);

      clearance_rows = new_row;

      // Set the new matrix elements to default values.

      for (int index = 0; index < old_class_count; ++index)
         {
         for (int j = 0; j < layer_structure.size(); ++j)
            {
            int default_value = value_at(1, index, j);
            set_value(old_class_count, index, j, default_value);
            set_value(index, old_class_count, j, default_value);
            }
         }

      for (int j = 0; j < layer_structure.size(); ++j)
         {
         int default_value = value_at(1, 1, j);
         set_value(old_class_count, old_class_count, j, default_value);
         }
      return true;
      }

   /**
    * Removes the class with index p_index from the clearance matrix.
    */
   void remove_class(int p_index)
      {
      int old_class_count = class_count;
      --class_count;

      ClearanceMatrixRow[] new_row = new ClearanceMatrixRow[class_count];

      // remove the matrix entry with inded p_index in to each old row
      int new_row_index = 0;
      for (int i = 0; i < old_class_count; ++i)
         {
         if (i == p_index)
            {
            continue;
            }
         ClearanceMatrixRow curr_old_row = clearance_rows[i];
         new_row[new_row_index] = new ClearanceMatrixRow(this, curr_old_row.name);
         ClearanceMatrixRow curr_new_row = new_row[new_row_index];

         int new_column_index = 0;
         for (int j = 0; j < old_class_count; ++j)
            {
            if (j == p_index)
               {
               continue;
               }
            curr_new_row.column[new_column_index] = curr_old_row.column[j];
            ++new_column_index;
            }
         ++new_row_index;
         }
      clearance_rows = new_row;
      }

   /**
    * Returns true, if all clearance values of the class with index p_1 are equal to the clearance values of index p_2.
    */
   public boolean is_equal(int p_1, int p_2)
      {
      if (p_1 == p_2)
         {
         return true;
         }
      if (p_1 < 0 || p_2 < 0 || p_1 >= class_count || p_2 >= class_count)
         {
         return false;
         }
      ClearanceMatrixRow row_1 = clearance_rows[p_1];
      ClearanceMatrixRow row_2 = clearance_rows[p_2];
      for (int i = 1; i < class_count; ++i)
         {
         if (!row_1.column[i].equals(row_2.column[i]))
            {
            return false;
            }
         }
      return true;
      }


   }