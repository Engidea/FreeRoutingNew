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
 * LayerVisibilityFrame.java
 *
 * Created on 5. November 2004, 11:29
 */

package gui.win;

import gui.BoardFrame;
import gui.BoardPanel;
import gui.varie.GuiResources;
import board.BrdLayerStructure;

/**
 * Interactive Frame to adjust the visibility of the individual board layers
 *
 * @author alfons
 */
public final class WindowLayerVisibility extends WindowVisibility
   {
   private static final long serialVersionUID = 1L;

   public static WindowLayerVisibility get_instance(BoardFrame p_board_frame)
      {
      BoardPanel board_panel = p_board_frame.board_panel;
      GuiResources resources = p_board_frame.newGuiResources("gui.resources.Default");
      String title = resources.getString("layer_visibility");
      String header_message = resources.getString("layer_visibility_header");
      BrdLayerStructure layer_structure = board_panel.board_handling.get_routing_board().layer_structure;
      String[] message_arr = new String[layer_structure.size()];

      for (int index = 0; index < message_arr.length; ++index)
         {
         message_arr[index] = layer_structure.get_name(index);
         }
      
      WindowLayerVisibility result = new WindowLayerVisibility(p_board_frame, title, header_message, message_arr);
      for (int i = 0; i < message_arr.length; ++i)
         {
         result.set_slider_value(i, board_panel.board_handling.gdi_context.get_raw_layer_visibility(i));
         }
      p_board_frame.set_context_sensitive_help(result, "WindowDisplay_LayerVisibility");
      return result;
      }

   private WindowLayerVisibility(BoardFrame p_board_frame, String p_title, String p_header_message, String[] p_message_arr)
      {
      super(p_board_frame, p_title, p_header_message, p_message_arr);
      }

   protected void set_changed_value(int p_layer_no, double p_value)
      {
      p_layer_no = get_board_handling().set_layer_visibility(p_layer_no, p_value);
      
      get_board_handling().set_layer(p_layer_no);
      }

   protected void set_all_minimum()
      {
      int layer_count = get_board_handling().gdi_context.layer_count();
      for (int i = 0; i < layer_count; ++i)
         {
         if (i != get_board_handling().itera_settings.get_layer())
            {
            set_slider_value(i, 0);
            set_changed_value(i, 0);
            }
         }
      }

   /**
    * Refreshs the displayed values in this window.
    */
   public void refresh()
      {
      graphics.GdiContext graphics_context = get_board_handling().gdi_context;
      for (int i = 0; i < graphics_context.layer_count(); ++i)
         {
         set_slider_value(i, graphics_context.get_raw_layer_visibility(i));
         }
      }
   }
