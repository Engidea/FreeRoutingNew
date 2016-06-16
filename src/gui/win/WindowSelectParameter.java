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
 * SelectParameterWindow.java
 *
 * Created on 19. November 2004, 11:12
 */

package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiPanelVertical;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import board.BrdLayer;
import board.BrdLayerStructure;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Window for the handling of the interactive selection parameters,
 *
 * @author Alfons Wirtz
 */
public class WindowSelectParameter extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private final IteraBoard i_board;
   private final GuiResources resources;

   private final ItemSelectionChoice[] filter_values = ItemSelectionChoice.values();
   private final JCheckBox[] item_selection_choices; 
   
   private final JRadioButton[] layer_name_arr;
   private final JRadioButton all_visible_button;
   private final JRadioButton current_only_button;

   
   public WindowSelectParameter(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      i_board = p_board_frame.board_panel.itera_board;

      resources = board_frame.newGuiResources("gui.resources.WindowSelectParameter");
      
      setTitle(resources.getString("title"));

      // create main panel

      GuiPanelVertical main_panel = new GuiPanelVertical(new Insets(3,3,3,3));

      all_visible_button = resources.newJRadioButton("all_visible","all_visible_tooltip",new AllVisibleListener());
      current_only_button = resources.newJRadioButton("current_only","current_only_tooltip",new CurrentOnlyListener());

      main_panel.add(newSelectPanel());
      
      
      item_selection_choices = new JCheckBox[filter_values.length];
      
      main_panel.add(newSelectablePanel());

      
      // Create buttongroup for the current layer:

      BrdLayerStructure layer_structure = i_board.get_routing_board().layer_structure;

      layer_name_arr = new JRadioButton[layer_structure.signal_layer_count()];

      main_panel.add(newLayersPanel(layer_structure));

      
      add(main_panel.getJPanel());
      
      p_board_frame.set_context_sensitive_help(this, "WindowSelectParameter");
      
      refresh();
      pack();
      }

   private JPanel newLayersPanel (BrdLayerStructure layer_structure)
      {
      JPanel risul = new JPanel();
      risul.setLayout(new BoxLayout(risul, BoxLayout.Y_AXIS));
      
      risul.setBorder(resources.newTitledBorder("current_layer"));
      risul.setToolTipText(resources.getString("current_layer_tooltip"));
      
      ButtonGroup current_layer_button_group = new ButtonGroup();
      
      for (int index = 0; index < layer_name_arr.length; ++index)
         {
         BrdLayer curr_signal_layer = layer_structure.get_signal_layer(index);
         int layer_no = layer_structure.get_no(curr_signal_layer);

         layer_name_arr[index] = resources.newJRadioButton(curr_signal_layer.name,new CurrentLayerListener(index, layer_no));

         current_layer_button_group.add(layer_name_arr[index]);

         risul.add(layer_name_arr[index]);
         }
      
      return risul;
      }
   
   private JPanel newSelectPanel ()
      {
      ButtonGroup group = new ButtonGroup();
      group.add(all_visible_button);
      group.add(current_only_button);
      all_visible_button.setSelected(true);

      JPanel risul = new JPanel();
      risul.setBorder(resources.newTitledBorder("selection_layers"));
      risul.setToolTipText(resources.getString("selection_layers_tooltip"));

      risul.add(all_visible_button);
      risul.add(current_only_button);
      
      return risul;
      }


   private JPanel newSelectablePanel ()
      {
      JPanel risul = new JPanel();
      risul.setLayout(new BoxLayout(risul, BoxLayout.Y_AXIS));
      
      risul.setBorder(resources.newTitledBorder("selectable_items"));
      risul.setToolTipText(resources.getString("selectable_items_tooltip"));

      for (int index = 0; index < filter_values.length; ++index)
         {
         item_selection_choices[index] = resources.newJCheckBox(filter_values[index].toString(),new ItemSelectionListener(index));
         risul.add(item_selection_choices[index]);
         }
      
      return risul;
      }
   
   /**
    * Refreshs the displayed values in this window.
    */
   public void refresh()
      {
      if (i_board.itera_settings.get_select_on_all_visible_layers())
         {
         all_visible_button.setSelected(true);
         }
      else
         {
         current_only_button.setSelected(true);
         }
      ItemSelectionFilter item_selection_filter = i_board.itera_settings.get_item_selection_filter();
      if (item_selection_filter == null)
         {
         System.out.println("SelectParameterWindow.refresh: item_selection_filter is null");
         }
      else
         {
         final ItemSelectionChoice[] filter_values = ItemSelectionChoice.values();
         for (int i = 0; i < filter_values.length; ++i)
            {
            item_selection_choices[i].setSelected(item_selection_filter.is_selected(filter_values[i]));
            }
         }
      BrdLayerStructure layer_structure = i_board.get_routing_board().layer_structure;
      BrdLayer current_layer = layer_structure.get(i_board.itera_settings.get_layer_no());
      layer_name_arr[layer_structure.get_signal_layer_no(current_layer)].setSelected(true);
      }

   /**
    * Selects the layer with the input signal number.
    */
   public void select_signal_layer(int p_signal_layer_no)
      {
      layer_name_arr[p_signal_layer_no].setSelected(true);
      }


   private class CurrentLayerListener implements ActionListener
      {
      public CurrentLayerListener(int p_signal_layer_no, int p_layer_no)
         {
         signal_layer_no = p_signal_layer_no;
         layer_no = p_layer_no;
         }

      public void actionPerformed(ActionEvent p_evt)
         {
         i_board.set_current_layer(layer_no);
         }

      public final int signal_layer_no;
      public final int layer_no;
      }

   private class AllVisibleListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_select_on_all_visible_layers(true);
         }
      }

   private class CurrentOnlyListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_select_on_all_visible_layers(false);
         }
      }

   private class ItemSelectionListener implements java.awt.event.ActionListener
      {
      public ItemSelectionListener(int p_item_no)
         {
         item_no = p_item_no;
         }

      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         boolean is_selected = item_selection_choices[item_no].isSelected();

         ItemSelectionChoice item_type = ItemSelectionChoice.values()[item_no];

         i_board.set_selectable(item_type, is_selected);

         // make shure that from fixed and unfixed items at least one type is selected.
         if (item_type == ItemSelectionChoice.FIXED)
            {
            int unfixed_no = ItemSelectionChoice.UNFIXED.ordinal();
            if (!is_selected && !item_selection_choices[unfixed_no].isSelected())
               {
               item_selection_choices[unfixed_no].setSelected(true);
               i_board.set_selectable(ItemSelectionChoice.UNFIXED, true);
               }
            }
         else if (item_type == ItemSelectionChoice.UNFIXED)
            {
            int fixed_no = ItemSelectionChoice.FIXED.ordinal();
            if (!is_selected && !item_selection_choices[fixed_no].isSelected())
               {
               item_selection_choices[fixed_no].setSelected(true);
               i_board.set_selectable(ItemSelectionChoice.FIXED, true);
               }
            }
         }

      private final int item_no;
      }
   }
