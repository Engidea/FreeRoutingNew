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
 * LayerComboBox.java
 *
 * Created on 20. Februar 2005, 08:14
 */

package gui;

import gui.varie.GuiLayer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import board.BrdLayer;
import board.BrdLayerStructure;

/**
 * A Combo Box with items for individuell board layers plus an additional item for all layers.
 *
 * @author Alfons Wirtz
 */
public class ComboBoxLayer extends JComboBox<GuiLayer>
   {
   private static final long serialVersionUID = 1L;

   // The layer index, when all layers are selected
   public final static int ALL_LAYER_INDEX = -1;
   // The layer index, when all inner layers ar selected
   public final static int INNER_LAYER_INDEX = -2;

   private final GuiLayer[] layer_arr;
   
   public ComboBoxLayer(BrdLayerStructure p_layer_structure, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_locale);
      int signal_layer_count = p_layer_structure.signal_layer_count();
      int item_count = signal_layer_count + 1;
      boolean add_inner_layer_item = signal_layer_count > 2;
      if (add_inner_layer_item)
         {
         ++item_count;
         }
      layer_arr = new GuiLayer[item_count];
      layer_arr[0] = new GuiLayer(resources.getString("all"), ALL_LAYER_INDEX);
      int curr_layer_no = 0;
      if (add_inner_layer_item)
         {
         layer_arr[1] = new GuiLayer(resources.getString("inner"), INNER_LAYER_INDEX);
         ++curr_layer_no;
         }
      for (int index = 0; index < signal_layer_count; ++index)
         {
         ++curr_layer_no;
         BrdLayer curr_signal_layer = p_layer_structure.get_signal_layer(index);
         layer_arr[curr_layer_no] = new GuiLayer(curr_signal_layer.name, p_layer_structure.get_no(curr_signal_layer));
         }
      setModel(new DefaultComboBoxModel<GuiLayer>(layer_arr));
      setSelectedIndex(0);
      }

   public GuiLayer get_selected_layer()
      {
      return (GuiLayer) getSelectedItem();
      }


   }
