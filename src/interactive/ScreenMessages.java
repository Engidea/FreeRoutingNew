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

import java.text.NumberFormat;
import board.BrdLayer;
import freert.main.Stat;
import freert.planar.PlaPointFloat;
import gui.BoardPanelStatus;
import gui.varie.GuiResources;

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

   private final BoardPanelStatus statusPanel;
   
   private String prev_target_layer_name = EMPTY;
   private boolean write_protected = false;
   
   // The number format for displaying the trace length 
   private final NumberFormat number_format;
   
   public ScreenMessages( BoardPanelStatus p_panel, Stat p_stat)
      {
      stat = p_stat;
      
      statusPanel = p_panel;

      resources = new GuiResources(p_stat,"interactive.resources.ScreenMessages");
      active_layer_string = resources.getString("current_layer") + " ";
      target_layer_string = resources.getString("target_layer") + " ";

      number_format = java.text.NumberFormat.getInstance(p_stat.locale);
      number_format.setMaximumFractionDigits(4);
      }

   /**
    * Sets the message in the status field.
    */
   public void set_status_message(String p_message)
      {
      if ( write_protected)  return;

      statusPanel.status_message.setText(p_message);
      }

   /**
    * Sets the displayed layer number on the screen.
    */
   public void show_layer_name(BrdLayer p_layer)
      {
      if ( write_protected ) return;

      statusPanel.current_layer.setText(active_layer_string + p_layer.name);
      }

   public void set_interactive_autoroute_info(int p_found, int p_not_found, int p_items_to_go)
      {
      statusPanel.add_message.setText(resources.getString("to_route") + " " + p_items_to_go);
      statusPanel.current_layer.setText(resources.getString("found") + " " + p_found + ", " + resources.getString("failed") + " " + p_not_found);
      }

   public void set_batch_autoroute_info(int items_to_go, int routed, int ripped, int failed)
      {
      statusPanel.add_message.setText(resources.getString("to_route") + " " + items_to_go + ", " + resources.getString("routed") + " " + routed + ", ");
      statusPanel.current_layer.setText(resources.getString("ripped") + " " + ripped + ", " + resources.getString("failed") + " " + failed);
      }

   public void set_batch_fanout_info(int p_pass_no, int p_components_to_go)
      {
      statusPanel.add_message.setText(resources.getString("fanout_pass") + " " + p_pass_no + ": ");
      statusPanel.current_layer.setText(resources.getString("still") + " " + p_components_to_go + " " + resources.getString("components"));
      }

   public void set_post_route_info(int p_via_count, double p_trace_length)
      {
      statusPanel.add_message.setText(resources.getString("via_count") + " " + p_via_count);
      statusPanel.current_layer.setText(resources.getString("trace_length") + " " + number_format.format(p_trace_length));
      }

   /**
    * Sets the displayed layer of the nearest target item in interactive routing.
    */
   public void set_target_layer(String p_layer_name)
      {
      if ( write_protected ) return;
      
      // tiny optimization, avoid updating the field if the value is the same..
      if ( p_layer_name.equals(prev_target_layer_name) ) return;
      
      statusPanel.add_message.setText(target_layer_string + p_layer_name);
      
      prev_target_layer_name = p_layer_name;
      }

   public void set_mouse_position(PlaPointFloat p_pos)
      {
      if ( write_protected) return;
      
      statusPanel.mouse_position.setText(p_pos.to_string(stat.locale));
      }

   /**
    * Clears the additional field, which is among others used to display the layer of the nearest target item.
    */
   public void clear_add_field()
      {
      if ( write_protected ) return;

      statusPanel.add_message.setText(EMPTY);
      
      prev_target_layer_name = EMPTY;
      }

   /**
    * Clears the status field and the additional field.
    */
   public void clear()
      {
      if ( write_protected ) return;
      
      statusPanel.add_message.setText(EMPTY);
      clear_add_field();
      statusPanel.current_layer.setText(EMPTY);
      }

   /**
    * As long as write_protected is set to true, the set functions in this class will do nothing.
    */
   public void set_write_protected(boolean p_value)
      {
      write_protected = p_value;
      }
   }
