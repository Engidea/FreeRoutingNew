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
 * ClearanceMatrixWindow.java
 *
 * Created on 20. Februar 2005, 06:09
 */

package gui.win;

import freert.rules.ClearanceMatrix;
import gui.BoardFrame;
import gui.ComboBoxLayer;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import board.RoutingBoard;

/**
 * Window for interactive editing of the clearance Matrix.
 *
 * @author Alfons Wirtz
 */
public final class WindowClearanceMatrix extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private final JPanel main_panel;
   private JPanel center_panel;
   private final ComboBoxLayer layer_combo_box;
   private JTable clearance_table;
   private WinClearanceTableModel clearance_table_model;
   private final GuiResources resources;

   // Characters, which are not allowed in the name of a clearance class.
   private static final String[] reserved_name_chars = { "(", ")", " ", "_" };
   
   public WindowClearanceMatrix(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      resources = board_frame.newGuiResources("gui.resources.WindowClearanceMatrix");

      setTitle(resources.getString("title"));

      main_panel = new JPanel();
      main_panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      main_panel.setLayout(new java.awt.BorderLayout());

      // Add the layer combo box.

      JPanel north_panel = new JPanel();
      north_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
      JLabel layer_label = new JLabel(resources.getString("layer") + " ");
      layer_label.setToolTipText(resources.getString("layer_tooltip"));
      north_panel.add(layer_label);

      IteraBoard board_handling = board_frame.board_panel.itera_board;
      layer_combo_box = new ComboBoxLayer(board_handling.get_routing_board().layer_structure, p_board_frame.get_locale());
      north_panel.add(layer_combo_box);
      layer_combo_box.addActionListener(new ComboBoxListener());

      main_panel.add(north_panel, java.awt.BorderLayout.NORTH);

      // Add the clearance table.

      center_panel = add_clearance_table(p_board_frame);

      main_panel.add(center_panel, java.awt.BorderLayout.CENTER);

      // Add panel with buttons.

      JPanel south_panel = new JPanel();
      south_panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      south_panel.setLayout(new java.awt.BorderLayout());
      add(south_panel);

      JButton add_class_button = resources.newJButton("add_class","add_class_tooltip",new AddClassListener());
      south_panel.add(add_class_button, java.awt.BorderLayout.WEST);

      final JButton prune_button = new JButton(resources.getString("prune"));
      prune_button.setToolTipText(resources.getString("prune_tooltip"));
      prune_button.addActionListener(new PruneListener());
      south_panel.add(prune_button, java.awt.BorderLayout.EAST);

      main_panel.add(south_panel, java.awt.BorderLayout.SOUTH);

      p_board_frame.set_context_sensitive_help(this, "WindowClearanceMatrix");

      add(main_panel);
      pack();
      }

   /**
    * Recalculates all displayed values
    */
   public void refresh()
      {
      RoutingBoard routing_board = board_frame.board_panel.itera_board.get_routing_board();
      if (clearance_table_model.getRowCount() != routing_board.brd_rules.clearance_matrix.get_class_count())
         {
         adjust_clearance_table();
         }
      clearance_table_model.set_values(layer_combo_box.get_selected_layer().index);
      repaint();
      }

   private JPanel add_clearance_table(BoardFrame p_board_frame)
      {
      clearance_table_model = new WinClearanceTableModel(p_board_frame.board_panel.itera_board,layer_combo_box);
      clearance_table = new JTable(clearance_table_model);

      // Put the clearance table into a scroll pane.
      final int textfield_height = 16;
      final int textfield_width = Math.max(6 * max_name_length(), 100);
      int table_height = textfield_height * (clearance_table_model.getRowCount());
      int table_width = textfield_width * clearance_table_model.getColumnCount();
      clearance_table.setPreferredSize(new java.awt.Dimension(table_width, table_height));
      clearance_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      // Put a panel around the table and the header before putting the table into the scroll pane,
      // because otherwise there seems to be a redisplay bug in horizontal scrolling.
      JPanel scroll_panel = new JPanel();
      scroll_panel.setLayout(new java.awt.BorderLayout());
      scroll_panel.add(clearance_table.getTableHeader(), java.awt.BorderLayout.NORTH);
      scroll_panel.add(clearance_table, java.awt.BorderLayout.CENTER);
      JScrollPane scroll_pane = new JScrollPane(scroll_panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      final int scroll_bar_width = 20;
      final int scroll_pane_height = textfield_height * clearance_table_model.getRowCount() + scroll_bar_width;
      final int scroll_pane_width = Math.min(table_width + scroll_bar_width, 1200);
      scroll_pane.setPreferredSize(new java.awt.Dimension(scroll_pane_width, scroll_pane_height));
      // Change the background color of the header and the first column of the table.
      java.awt.Color header_background_color = new java.awt.Color(220, 220, 255);
      JTableHeader table_header = clearance_table.getTableHeader();
      table_header.setBackground(header_background_color);

      TableColumn first_column = clearance_table.getColumnModel().getColumn(0);
      DefaultTableCellRenderer first_colunn_renderer = new DefaultTableCellRenderer();
      first_colunn_renderer.setBackground(header_background_color);
      first_column.setCellRenderer(first_colunn_renderer);

      final JPanel result = new JPanel();
      result.setLayout(new java.awt.BorderLayout());

      result.add(scroll_pane, java.awt.BorderLayout.CENTER);

      // add message for german localisation bug
      if (p_board_frame.get_locale().getLanguage().equalsIgnoreCase("de"))
         {
         JLabel bug_label = new JLabel("Wegen eines Java-System-Bugs muss das Dezimalkomma in dieser Tabelle als Punkt eingegeben werden!");
         result.add(bug_label, java.awt.BorderLayout.SOUTH);
         }
      return result;
      }

   /**
    * Adds a new class to the clearance matrix.
    */
   private void add_class()
      {
      String new_name = null;
      // Ask for the name of the new class.
      for (;;)
         {
         new_name = JOptionPane.showInputDialog(resources.getString("new_name"));

         if (new_name == null) return;

         new_name = new_name.trim();

         if (is_legal_class_name(new_name)) break;
         }

      final RoutingBoard routing_board = board_frame.board_panel.itera_board.get_routing_board();
      final ClearanceMatrix clearance_matrix = routing_board.brd_rules.clearance_matrix;

      // Check, if the name exists already.
      boolean name_exists = false;
      
      for (int i = 0; i < clearance_matrix.get_class_count(); ++i)
         {
         if (new_name.equals(clearance_matrix.get_name(i)))
            {
            name_exists = true;
            break;
            }
         }
      
      if (name_exists) return;
 
      clearance_matrix.append_class(new_name);
      
      adjust_clearance_table();
      }

   /**
    * Removes clearance classs, whose clearance values are all equal to a previous class.
    */
   private void prune_clearance_matrix()
      {
      final RoutingBoard routing_board = board_frame.board_panel.itera_board.get_routing_board();
      ClearanceMatrix clearance_matrix = routing_board.brd_rules.clearance_matrix;
      for (int i = clearance_matrix.get_class_count() - 1; i >= 2; --i)
         {
         for (int j = clearance_matrix.get_class_count() - 1; j >= 0; --j)
            {
            if (i == j)
               {
               continue;
               }
            if (clearance_matrix.is_equal(i, j))
               {
               String message = resources.getString("confirm_remove") + " " + clearance_matrix.get_name(i);
               int selected_option = JOptionPane.showConfirmDialog(getJFrame(), message, null, JOptionPane.YES_NO_OPTION);
               if (selected_option == JOptionPane.YES_OPTION)
                  {
                  java.util.Collection<board.items.BrdItem> board_items = routing_board.get_items();
                  routing_board.brd_rules.change_clearance_class_no(i, j, board_items);
                  if (!routing_board.brd_rules.remove_clearance_class(i, board_items))
                     {
                     System.out.println("WindowClearanceMatrix.prune_clearance_matrix error removing clearance class");
                     return;
                     }
                  routing_board.search_tree_manager.clearance_class_removed(i);
                  adjust_clearance_table();
                  }
               break;
               }
            }
         }
      }

   /**
    * Adjusts the displayed window with the clearance table after the size of the clearance matrix has changed.
    */
   private void adjust_clearance_table()
      {
      clearance_table_model = new WinClearanceTableModel(board_frame.board_panel.itera_board,layer_combo_box);
      clearance_table = new JTable(clearance_table_model);
      main_panel.remove(center_panel);
      center_panel = add_clearance_table(board_frame);
      main_panel.add(center_panel, java.awt.BorderLayout.CENTER);
      pack();
      board_frame.refresh_windows();
      }

   /**
    * Returns true, if p_string is a legal class name.
    */
   private boolean is_legal_class_name(String p_string)
      {
      if (p_string.equals("")) return false;

      for (int i = 0; i < reserved_name_chars.length; ++i)
         {
         if (p_string.contains(reserved_name_chars[i]))
            {
            return false;
            }
         }
      return true;
      }

   private int max_name_length()
      {
      int result = 1;
      freert.rules.ClearanceMatrix clearance_matrix = board_frame.board_panel.itera_board.get_routing_board().brd_rules.clearance_matrix;
      for (int i = 0; i < clearance_matrix.get_class_count(); ++i)
         {
         result = Math.max(result, clearance_matrix.get_name(i).length());
         }
      return result;
      }

   private class ComboBoxListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         refresh();
         }
      }

   private class AddClassListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         add_class();
         }
      }

   private class PruneListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         prune_clearance_matrix();
         }
      }

   }
