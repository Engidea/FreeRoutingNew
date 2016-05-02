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
 * ColorTableModel.java
 *
 * Created on 5. August 2003, 10:18
 */
package freert.graphics;

import gui.varie.GuiResources;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.swing.table.AbstractTableModel;
import main.Stat;

/**
 * Abstract class to store colors used for drawing the board.
 *
 * @author Alfons Wirtz
 */
public abstract class ColorTableModel extends AbstractTableModel
   {
   private static final long serialVersionUID = 1L;

   protected final Object[][] table_data;
   
   protected transient Stat stat;    // need to rebind it on object restore
   protected transient GuiResources resources;

   protected ColorTableModel(int p_row_count, Stat p_stat)
      {
      table_data = new Object[p_row_count][];
      stat = p_stat;
      resources = newResources();
      }

   protected ColorTableModel( ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      table_data = (Object[][]) p_stream.readObject();
      }

   private GuiResources newResources ()
      {
      return new GuiResources(stat, "freert.graphics.resources.ColorTableModel");
      }
   
   protected void write_object(java.io.ObjectOutputStream p_stream) throws java.io.IOException
      {
      p_stream.writeObject(table_data);
      }

   protected void transient_update ( Stat p_stat )
      {
      stat = p_stat;
      resources = newResources();
      }
   
   public int getRowCount()
      {
      return table_data.length;
      }

   public Object getValueAt(int p_row, int p_col)
      {
      return table_data[p_row][p_col];
      }

   public void setValueAt(Object p_value, int p_row, int p_col)
      {
      table_data[p_row][p_col] = p_value;
      fireTableCellUpdated(p_row, p_col);
      }

   /**
    * JTable uses this method to determine the default renderer/ editor for each cell. If we didn't implement this method, then the
    * last column would contain text ("true"/"false"), rather than a check box.
    */
   public Class<?> getColumnClass(int p_c)
      {
      return getValueAt(0, p_c).getClass();
      }

   }
