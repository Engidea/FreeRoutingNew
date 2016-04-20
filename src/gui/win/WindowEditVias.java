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
 * ViaTablePanel.java
 *
 * Created on 4. April 2005, 07:05
 */

package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import main.Stat;
import rules.BoardRules;
import rules.ViaTableColumnName;
import rules.ViaTableModel;
import board.infos.BrdViaInfo;
import board.infos.BrdViaInfoList;
import datastructures.ItemClass;

/**
 * Edit window for the table of available vias.
 *
 * @author Alfons Wirtz
 */
public class WindowEditVias extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private final BoardFrame board_frame;

   private final JPanel main_panel;

   private JScrollPane scroll_pane;
   private JTable table;
   private ViaTableModel table_model;

   private final JComboBox<String> cl_class_combo_box;
   private final JComboBox<String> padstack_combo_box;

   private final GuiResources resources;

   private static final int TEXTFIELD_HEIGHT = 16;
   private static final int TEXTFIELD_WIDTH = 100;
   
   public WindowEditVias(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      resources = new GuiResources(stat, "gui.resources.WindowEditVias");
      
      setTitle(resources.getString("title"));

      board_frame = p_board_frame;

      this.main_panel = new JPanel();
      this.main_panel.setLayout(new java.awt.BorderLayout());

      this.cl_class_combo_box = new JComboBox<String>();
      this.padstack_combo_box = new JComboBox<String>();
      add_combobox_items();

      add_table();

      JPanel via_info_button_panel = new JPanel();
      via_info_button_panel.setLayout(new java.awt.FlowLayout());
      this.main_panel.add(via_info_button_panel, java.awt.BorderLayout.SOUTH);
      final JButton add_via_button = new JButton(resources.getString("add"));
      add_via_button.setToolTipText(resources.getString("add_tooltip"));
      add_via_button.addActionListener(new AddViaListener());
      via_info_button_panel.add(add_via_button);
      final JButton remove_via_button = new JButton(resources.getString("remove"));
      remove_via_button.setToolTipText(resources.getString("remove_tooltip"));
      remove_via_button.addActionListener(new RemoveViaListener());
      via_info_button_panel.add(remove_via_button);

      p_board_frame.set_context_sensitive_help(this, "WindowVia_EditVia");

      this.add(main_panel);
      this.pack();
      }

   /**
    * Recalculates all values displayed in the parent window
    */
   public void refresh()
      {
      this.padstack_combo_box.removeAllItems();
      this.cl_class_combo_box.removeAllItems();
      this.add_combobox_items();
      this.table_model.set_values();
      }

   private void add_table()
      {
      this.table_model = new ViaTableModel(board_frame,resources);
      this.table = new  JTable(this.table_model);
      this.scroll_pane = new JScrollPane(this.table);
      int table_height = TEXTFIELD_HEIGHT * this.table_model.getRowCount();
      int table_width = TEXTFIELD_WIDTH * this.table_model.getColumnCount();
      this.table.setPreferredScrollableViewportSize(new java.awt.Dimension(table_width, table_height));
      this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.main_panel.add(scroll_pane, java.awt.BorderLayout.CENTER);

      this.table.getColumnModel().getColumn(ViaTableColumnName.CLEARANCE_CLASS.ordinal()).setCellEditor(new DefaultCellEditor(cl_class_combo_box));

      this.table.getColumnModel().getColumn(ViaTableColumnName.PADSTACK.ordinal()).setCellEditor(new DefaultCellEditor(padstack_combo_box));
      }

   private void add_combobox_items()
      {
      board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
      for (int i = 0; i < routing_board.brd_rules.clearance_matrix.get_class_count(); ++i)
         {
         cl_class_combo_box.addItem(routing_board.brd_rules.clearance_matrix.get_name(i));
         }
      for (int i = 0; i < routing_board.library.via_padstack_count(); ++i)
         {
         padstack_combo_box.addItem(routing_board.library.get_via_padstack(i).pads_name);
         }
      }

   /**
    * Adjusts the displayed window with the via table after the size of the table has been changed.
    */
   private void adjust_table()
      {
      table_model = new ViaTableModel(board_frame,resources);
      table = new JTable(table_model);
      main_panel.remove(scroll_pane);
      add_table();
      pack();
      board_frame.refresh_windows();
      }

   private class AddViaListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
         BrdViaInfoList via_infos = routing_board.brd_rules.via_infos;
         Integer no = 1;
         String new_name = null;
         final String name_start = resources.getString("new_via");
         for (;;)
            {
            new_name = name_start + no.toString();
            if (!via_infos.name_exists(new_name))
               {
               break;
               }
            ++no;
            }
         rules.NetClass default_net_class = routing_board.brd_rules.get_default_net_class();
         BrdViaInfo new_via = new BrdViaInfo(new_name, routing_board.library.get_via_padstack(0), default_net_class.default_item_clearance_classes.get(ItemClass.VIA),
               false, routing_board.brd_rules);
         via_infos.add(new_via);
         adjust_table();
         }
      }

   private class RemoveViaListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (table_model.getRowCount() <= 1)
            {
            board_frame.screen_messages.set_status_message(resources.getString("message_1"));
            return;
            }
         int selected_row = table.getSelectedRow();
         if (selected_row < 0)
            {
            return;
            }
         Object via_name = table_model.getValueAt(selected_row, ViaTableColumnName.NAME.ordinal());
         if (!(via_name instanceof String))
            {
            return;
            }
         BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().brd_rules;
         BrdViaInfo via_info = board_rules.via_infos.get((String) via_name);
         // Check, if via_info is used in a via rule.
         for (rules.RuleViaInfoList curr_rule : board_rules.via_rules)
            {
            if (curr_rule.contains(via_info))
               {
               String message = resources.getString("message_2") + " " + curr_rule.rule_name;
               board_frame.screen_messages.set_status_message(message);
               return;
               }
            }
         if (board_rules.via_infos.remove(via_info))
            {
            adjust_table();
            String message = resources.getString("via") + "via " + via_info.get_name() + " " + resources.getString("removed");
            board_frame.screen_messages.set_status_message(message);
            }
         }
      }

   

   }
