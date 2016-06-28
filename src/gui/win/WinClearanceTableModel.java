package gui.win;

import freert.rules.ClearanceMatrix;
import freert.varie.UndoObjectNode;
import freert.varie.UndoObjects;
import gui.ComboBoxLayer;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

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
 * Table model of the clearance matrix.
 */
public class WinClearanceTableModel extends AbstractTableModel implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   private final IteraBoard itera_board;
   private final GuiResources resources;
   private final ComboBoxLayer layer_combo_box;
   
   public WinClearanceTableModel(IteraBoard p_board_handling, ComboBoxLayer p_layer_combo_box)
      {
      itera_board = p_board_handling;
      layer_combo_box = p_layer_combo_box;
      
      ClearanceMatrix clearance_matrix = itera_board.get_routing_board().brd_rules.clearance_matrix;
      
      resources = itera_board.newGuiResources("gui.resources.WindowClearanceMatrix");


      column_names = new String[clearance_matrix.get_class_count() + 1];
      column_names[0] = resources.getString("class");

      data = new Object[clearance_matrix.get_class_count()][];
      for (int i = 0; i < clearance_matrix.get_class_count(); ++i)
         {
         column_names[i + 1] = clearance_matrix.get_name(i);
         data[i] = new Object[clearance_matrix.get_class_count() + 1];
         data[i][0] = clearance_matrix.get_name(i);
         }
      set_values(0);
      }

   public String getColumnName(int p_col)
      {
      return column_names[p_col];
      }

   public int getRowCount()
      {
      return data.length;
      }

   public int getColumnCount()
      {
      return column_names.length;
      }

   public Object getValueAt(int p_row, int p_col)
      {
      return data[p_row][p_col];
      }

   public void setValueAt(Object p_value, int p_row, int p_col)
      {
      Number number_value = null;
      if (p_value instanceof Number)
         {
         // does ot work because of a localisation Bug in Java
         number_value = (Number) p_value;
         }
      else
         {
         // Workaround because of a localisation Bug in Java
         // The numbers are always displayed in the English Format.
         if (!(p_value instanceof String))
            {
            return;
            }
         try
            {
            number_value = Float.parseFloat((String) p_value);
            }
         catch (Exception e)
            {
            return;
            }
         }
      int curr_row = p_row;
      int curr_column = p_col - 1;

      // check, if there are items on the board assigned to clearance class i or j.
      UndoObjects item_list = itera_board.get_routing_board().undo_items;
      boolean items_already_assigned_row = false;
      boolean items_already_assigned_column = false;
      java.util.Iterator<UndoObjectNode> it = item_list.start_read_object();
      for (;;)
         {
         board.items.BrdItem curr_item = (board.items.BrdItem) item_list.read_next(it);
         if (curr_item == null)
            {
            break;
            }
         int curr_item_class_no = curr_item.clearance_idx();
         if (curr_item_class_no == curr_row)
            {
            items_already_assigned_row = true;
            }
         if (curr_item_class_no == curr_column)
            {
            items_already_assigned_column = true;
            }

         }
      ClearanceMatrix clearance_matrix = itera_board.get_routing_board().brd_rules.clearance_matrix;
      boolean items_already_assigned = items_already_assigned_row && items_already_assigned_column;
      if (items_already_assigned)
         {
         String message = resources.getString("already_assigned") + " ";
         if (curr_row == curr_column)
            {
            message += resources.getString("the_class") + " " + clearance_matrix.get_name(curr_row);
            }
         else
            {
            message += resources.getString("the_classes") + " " + clearance_matrix.get_name(curr_row) + " " + resources.getString("and") + " " + clearance_matrix.get_name(curr_column);
            }
         message += resources.getString("change_anyway");
         int selected_option = itera_board.get_panel().board_frame.showConfirmDialog( message, null);
         if (selected_option != JOptionPane.YES_OPTION)
            {
            return;
            }
         }

      data[p_row][p_col] = number_value;
      data[p_col - 1][p_row + 1] = number_value;
      fireTableCellUpdated(p_row, p_col);
      fireTableCellUpdated(p_col - 1, p_row + 1);

      int board_value = (int) Math.round(itera_board.coordinate_transform.user_to_board((number_value).doubleValue()));
      int layer_no = layer_combo_box.get_selected_layer().index;
      if (layer_no == ComboBoxLayer.ALL_LAYER_INDEX)
         {
         // change the clearance on all layers
         clearance_matrix.set_value(curr_row, curr_column, board_value);
         clearance_matrix.set_value(curr_column, curr_row, board_value);
         }
      else if (layer_no == ComboBoxLayer.INNER_LAYER_INDEX)
         {
         // change the clearance on all inner layers
         clearance_matrix.set_inner_value(curr_row, curr_column, board_value);
         clearance_matrix.set_inner_value(curr_column, curr_row, board_value);
         }
      else
         {
         // change the clearance on layer with index layer_no
         clearance_matrix.set_value(curr_row, curr_column, layer_no, board_value);
         clearance_matrix.set_value(curr_column, curr_row, layer_no, board_value);
         }
      if (items_already_assigned)
         {
         // force reinserting all item into the search tree, because their tree shapes may have changed
         itera_board.get_routing_board().search_tree_manager.clearance_value_changed();
         }
      }

   public boolean isCellEditable(int p_row, int p_col)
      {
      return p_row > 0 && p_col > 1;
      }

   public Class<?> getColumnClass(int p_col)
      {
      if (p_col == 0)
         {
         return String.class;
         }
      return String.class;
      // Should be Number.class or Float.class. But that does not work because of a localisation bug in Java.
      }

   /**
    * Sets the values of this clearance table to the values of the clearance matrix on the input layer.
    */
   public void set_values(int p_layer)
      {
      ClearanceMatrix clearance_matrix = itera_board.get_routing_board().brd_rules.clearance_matrix;

      for (int i = 0; i < clearance_matrix.get_class_count(); ++i)
         {
         for (int j = 0; j < clearance_matrix.get_class_count(); ++j)
            {
            if (p_layer == ComboBoxLayer.ALL_LAYER_INDEX)
               {
               // all layers

               if (clearance_matrix.is_layer_dependent(i, j))
                  {
                  data[i][j + 1] = -1;
                  }
               else
                  {
                  Float curr_table_value = (float) itera_board.coordinate_transform.board_to_user(clearance_matrix.value_at(i, j, 0));
                  data[i][j + 1] = curr_table_value;
                  }
               }
            else if (p_layer == ComboBoxLayer.INNER_LAYER_INDEX)
               {
               // all layers

               if (clearance_matrix.is_inner_layer_dependent(i, j))
                  {
                  data[i][j + 1] = -1;
                  }
               else
                  {
                  Float curr_table_value = (float) itera_board.coordinate_transform.board_to_user(clearance_matrix.value_at(i, j, 1));
                  data[i][j + 1] = curr_table_value;
                  }
               }
            else
               {
               Float curr_table_value = (float) itera_board.coordinate_transform.board_to_user(clearance_matrix.value_at(i, j, p_layer));
               data[i][j + 1] = curr_table_value;
               }
            }
         }
      }

   private Object[][] data = null;
   private String[] column_names = null;
   }
