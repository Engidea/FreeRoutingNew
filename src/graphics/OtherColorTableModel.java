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
 * OtherColorTableModel.java
 *
 * Created on 5. August 2003, 07:39
 */

package graphics;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import main.Stat;

/**
 * Stores the colors used for the background and highlighting.
 *
 * @author Alfons Wirtz
 */
public final class OtherColorTableModel extends ColorTableModel implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   public OtherColorTableModel(Stat p_stat)
      {
      super(1, p_stat);
      
      table_data[0] = new Color[OtherColorName.values().length];
      Object[] curr_row = table_data[0];
      curr_row[OtherColorName.BACKGROUND.ordinal()] = new Color(70, 70, 70);
      curr_row[OtherColorName.HIGHLIGHT.ordinal()] = Color.white;
      curr_row[OtherColorName.INCOMPLETES.ordinal()] = Color.white;
      curr_row[OtherColorName.OUTLINE.ordinal()] = new Color(100, 150, 255);
      curr_row[OtherColorName.VIOLATIONS.ordinal()] = Color.magenta;
      curr_row[OtherColorName.COMPONENT_FRONT.ordinal()] = Color.blue;
      curr_row[OtherColorName.COMPONENT_BACK.ordinal()] = Color.red;
      curr_row[OtherColorName.LENGTH_MATCHING_AREA.ordinal()] = Color.green;
      }

   public OtherColorTableModel( ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      super(p_stream);
      }

   /**
    * Copy construcror.
    */
   public OtherColorTableModel(OtherColorTableModel p_item_color_model)
      {
      super(p_item_color_model.table_data.length, p_item_color_model.stat);
      for (int i = 0; i < this.table_data.length; ++i)
         {
         table_data[i] = new Object[p_item_color_model.table_data[i].length];
         System.arraycopy(p_item_color_model.table_data[i], 0, this.table_data[i], 0, this.table_data[i].length);
         }
      }

   public int getColumnCount()
      {
      return OtherColorName.values().length;
      }

   public String getColumnName(int p_col)
      {
      return resources.getString(OtherColorName.values()[p_col].toString());
      }

   public boolean isCellEditable(int p_row, int p_col)
      {
      return true;
      }

   public Color get_background_color()
      {
      return (Color) (table_data[0][OtherColorName.BACKGROUND.ordinal()]);
      }

   public Color get_hilight_color()
      {
      return (Color) (table_data[0][OtherColorName.HIGHLIGHT.ordinal()]);
      }

   public Color get_incomplete_color()
      {
      return (Color) (table_data[0][OtherColorName.INCOMPLETES.ordinal()]);
      }

   public Color get_outline_color()
      {
      return (Color) (table_data[0][OtherColorName.OUTLINE.ordinal()]);
      }

   public Color get_violations_color()
      {
      return (Color) (table_data[0][OtherColorName.VIOLATIONS.ordinal()]);
      }

   public Color get_component_color(boolean p_front)
      {
      Color result;
      if (p_front)
         {
         result = (Color) (table_data[0][OtherColorName.COMPONENT_FRONT.ordinal()]);
         }
      else
         {
         result = (Color) (table_data[0][OtherColorName.COMPONENT_BACK.ordinal()]);
         }
      return result;
      }

   public Color get_length_matching_area_color()
      {
      return (Color) (table_data[0][OtherColorName.LENGTH_MATCHING_AREA.ordinal()]);
      }

   public void set_background_color(Color p_color)
      {
      table_data[0][OtherColorName.BACKGROUND.ordinal()] = p_color;
      }

   public void set_hilight_color(Color p_color)
      {
      table_data[0][OtherColorName.HIGHLIGHT.ordinal()] = p_color;
      }

   public void set_incomplete_color(Color p_color)
      {
      table_data[0][OtherColorName.INCOMPLETES.ordinal()] = p_color;
      }

   public void set_violations_color(Color p_color)
      {
      table_data[0][OtherColorName.VIOLATIONS.ordinal()] = p_color;
      }

   public void set_outline_color(Color p_color)
      {
      table_data[0][OtherColorName.OUTLINE.ordinal()] = p_color;
      }

   public void set_component_color(Color p_color, boolean p_front)
      {
      if (p_front)
         {
         table_data[0][OtherColorName.COMPONENT_FRONT.ordinal()] = p_color;
         }
      else
         {
         table_data[0][OtherColorName.COMPONENT_BACK.ordinal()] = p_color;
         }
      }

   public void set_length_matching_area_color(Color p_color)
      {
      table_data[0][OtherColorName.LENGTH_MATCHING_AREA.ordinal()] = p_color;
      }
   }
