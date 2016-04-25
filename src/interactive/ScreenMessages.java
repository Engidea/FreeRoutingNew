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
 * ScreenMessageFields.java
 *
 * Created on 8. August 2003, 19:10
 */

package interactive;

import freert.planar.PlaPointFloat;
import gui.BoardPanelStatus;
import gui.varie.GuiResources;
import javax.swing.JLabel;
import main.Stat;
import board.BrdLayer;

/**
 * Text fields to display messages on the screen.
 *
 * @author arbeit
 */
public final class ScreenMessages
   {
   private static final String EMPTY = "            ";

   private final Stat stat;
   private final GuiResources resources;
   private final String active_layer_string;
   private final String target_layer_string;

   private final JLabel add_field;
   private final JLabel status_field;
   private final JLabel layer_field;
   private final JLabel mouse_position;
   
   private String prev_target_layer_name = EMPTY;
   private boolean write_protected = false;
   
   // The number format for displaying the trace length 
   private final java.text.NumberFormat number_format;
   
   public ScreenMessages( BoardPanelStatus p_panel, Stat p_stat)
      {
      stat = p_stat;

      status_field  = p_panel.status_message;
      add_field     = p_panel.add_message;
      layer_field   =  p_panel.current_layer;
      mouse_position =  p_panel.mouse_position;

      resources = new GuiResources(p_stat,"interactive.resources.ScreenMessages");
      active_layer_string = resources.getString("current_layer") + " ";
      target_layer_string = resources.getString("target_layer") + " ";
      
      add_field.setText(EMPTY);

      number_format = java.text.NumberFormat.getInstance(p_stat.locale);
      number_format.setMaximumFractionDigits(4);
      }

   /**
    * Sets the message in the status field.
    */
   public void set_status_message(String p_message)
      {
      if ( write_protected)  return;

      status_field.setText(p_message);
      }

   /**
    * Sets the displayed layer number on the screen.
    */
   public void show_layer_name(BrdLayer p_layer)
      {
      if ( write_protected ) return;

      layer_field.setText(active_layer_string + p_layer.name);
      }

   public void set_interactive_autoroute_info(int p_found, int p_not_found, int p_items_to_go)
      {
      add_field.setText(resources.getString("to_route") + " " + p_items_to_go);
      layer_field.setText(resources.getString("found") + " " + p_found + ", " + resources.getString("failed") + " " + p_not_found);
      }

   public void set_batch_autoroute_info(int items_to_go, int routed, int ripped, int failed)
      {
      add_field.setText(resources.getString("to_route") + " " + items_to_go + ", " + resources.getString("routed") + " " + routed + ", ");
      layer_field.setText(resources.getString("ripped") + " " + ripped + ", " + resources.getString("failed") + " " + failed);
      }

   public void set_batch_fanout_info(int p_pass_no, int p_components_to_go)
      {
      add_field.setText(resources.getString("fanout_pass") + " " + p_pass_no + ": ");
      layer_field.setText(resources.getString("still") + " " + p_components_to_go + " " + resources.getString("components"));
      }

   public void set_post_route_info(int p_via_count, double p_trace_length)
      {
      add_field.setText(resources.getString("via_count") + " " + p_via_count);
      layer_field.setText(resources.getString("trace_length") + " " + this.number_format.format(p_trace_length));
      }

   /**
    * Sets the displayed layer of the nearest target item in interactive routing.
    */
   public void set_target_layer(String p_layer_name)
      {
      if ( write_protected ) return;
      
      // tiny optimization, avoid updating the field if the value is the same..
      if ( p_layer_name.equals(prev_target_layer_name) ) return;
      
      add_field.setText(target_layer_string + p_layer_name);
      
      prev_target_layer_name = p_layer_name;
      }

   public void set_mouse_position(PlaPointFloat p_pos)
      {
      if ( write_protected) return;
      
      mouse_position.setText(p_pos.to_string(stat.locale));
      }

   /**
    * Clears the additional field, which is among others used to display the layer of the nearest target item.
    */
   public void clear_add_field()
      {
      if ( write_protected ) return;

      add_field.setText(EMPTY);
      
      prev_target_layer_name = EMPTY;
      }

   /**
    * Clears the status field and the additional field.
    */
   public void clear()
      {
      if ( write_protected ) return;
      
      status_field.setText(EMPTY);
      clear_add_field();
      layer_field.setText(EMPTY);
      }

   /**
    * As long as write_protected is set to true, the set functions in this class will do nothing.
    */
   public void set_write_protected(boolean p_value)
      {
      write_protected = p_value;
      }
   }
