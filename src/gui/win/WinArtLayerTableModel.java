package gui.win;

import gui.varie.IntKeyStringValue;
import gui.varie.IntKeyStringValueAlist;
import interactive.IteraBoard;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import autoroute.varie.ArtLayer;

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
 * Actually, this could be merged into ArtSettings...
 * @author damiano
 *
 */

public final class WinArtLayerTableModel extends AbstractTableModel implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname = "WinLayerTableModel.";

   private static final int COL_layer_idx=0;
   private static final int COL_layer_name=1;
   private static final int COL_layer_active=2;
   private static final int COL_layer_pfdir=3;
   private static final int COL_layer_count=4;

   private final ArrayList<ArtLayer> layer_list;
   private final IntKeyStringValueAlist pfdir_choices;
   private final IteraBoard i_board ;
   
   public WinArtLayerTableModel ( IteraBoard p_itera_board )
      {
      i_board = p_itera_board;
      
      // pick up a direct reference to the storage
      layer_list  = i_board.itera_settings.autoroute_settings.art_layer_list();
      
      pfdir_choices = new IntKeyStringValueAlist(2);
      pfdir_choices.add(ArtLayer.PFDIR_horizontal,"Horizontal");
      pfdir_choices.add(ArtLayer.PFDIR_vertical,"Vertical");
      }

   public void fireTableChanged ()
      {
      super.fireTableChanged(new TableModelEvent(this));
      }
   
   public void adjustTableClumns ( JTable p_table )
      {
      p_table.setPreferredScrollableViewportSize(new Dimension(300,100));
      p_table.setFillsViewportHeight(false);
      
      TableColumnModel tc_model = p_table.getColumnModel();
      
      tc_model.getColumn(COL_layer_idx).setMaxWidth(30);
      tc_model.getColumn(COL_layer_active).setMaxWidth(100);

      JComboBox<IntKeyStringValue> a_combo = new JComboBox<IntKeyStringValue>();
      for ( IntKeyStringValue a_row : pfdir_choices ) a_combo.addItem(a_row);
      
      tc_model.getColumn(COL_layer_pfdir).setCellEditor(new DefaultCellEditor(a_combo));
      }
   
   @Override
   public boolean isCellEditable(int p_row, int p_col) 
      {
      switch ( p_col )
         {
         case COL_layer_idx: 
         case COL_layer_name:
            return false;
         
         case COL_layer_pfdir:
         case COL_layer_active: 
            return true;
            
         default: 
            return false;
         }
      }
   
   @Override
   public String getColumnName ( int p_col )
      {
      switch ( p_col )
         {
         case COL_layer_idx: return "Idx";
         
         case COL_layer_pfdir: return "Preferred Dir";
   
         case COL_layer_name: return "Name";
            
         case COL_layer_active: return "Active";
            
         default: return "????";
         }
      }
   
   @Override
   public int getColumnCount()
      {
      return COL_layer_count;
      }

   @Override
   public int getRowCount()
      {
      return layer_list.size();
      }

   @Override
   public Class<?> getColumnClass(int p_col)
      {
      switch ( p_col )
         {
         case COL_layer_idx:
         case COL_layer_pfdir:
            return Integer.class;
   
         case COL_layer_name:
            return String.class;
            
         case COL_layer_active:
            return Boolean.class;
            
         default: 
            return Object.class;
         }
      }
   
   @Override
   public Object getValueAt(int p_row, int p_col)
      {
      ArtLayer a_row = layer_list.get(p_row);
      
      switch ( p_col )
         {
         case COL_layer_idx:
            return a_row.layer_no;

         case COL_layer_name:
            return a_row.layer_name;
            
         case COL_layer_active:
            return a_row.art_layer_active;

         case COL_layer_pfdir:
            return pfdir_choices.get(a_row.layer_pfdir);
            
         default: 
            return "BAD col="+p_col;
         }
      }

   @Override
   public void setValueAt(Object a_val, int p_row, int p_col)
      {
      if ( a_val == null ) return;
      
      ArtLayer a_row = layer_list.get(p_row);
      
      switch ( p_col )
         {
         case COL_layer_active:
            if ( a_val instanceof Boolean )
               {
               Boolean a_bool = (Boolean)a_val;
               a_row.art_layer_active = a_bool.booleanValue();
               i_board.userPrintln(classname+"signal_layer_active="+a_row.art_layer_active);
               }
            break;

         case COL_layer_pfdir:
            if ( a_val instanceof IntKeyStringValue )
               {
               IntKeyStringValue a_kv = (IntKeyStringValue)a_val;
               a_row.layer_pfdir = a_kv.key;
               i_board.userPrintln(classname+"signal_layer_pfdir="+a_row.layer_pfdir);
               }
            break;
         }
      }
   }
