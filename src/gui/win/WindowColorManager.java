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
 * ColorManager.java
 *
 * Created on 3. August 2003, 11:16
 */

package gui.win;

import graphics.GdiContext;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import gui.varie.TableColorEditor;
import gui.varie.TableColorRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Window for changing the colors of board objects.
 *
 * @author Alfons Wirtz
 */
public class WindowColorManager extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   public WindowColorManager(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      GdiContext graphics_context = board_frame.board_panel.board_handling.gdi_context;
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.Default");
      
      setTitle(resources.getString("color_manager"));

      JPanel panel = new JPanel();
      int textfield_height = 17;
      int table_width = 1100;
      int item_color_table_height = graphics_context.item_color_table.getRowCount() * textfield_height;
      
      panel.setPreferredSize(new Dimension(10 + table_width, 70 + item_color_table_height));

      item_color_table = new JTable(graphics_context.item_color_table);
      item_color_table.setPreferredScrollableViewportSize(new Dimension(table_width, item_color_table_height));
      JScrollPane item_scroll_pane = init_color_table(item_color_table, p_board_frame.get_locale());
      panel.add(item_scroll_pane, BorderLayout.NORTH);

      other_color_table = new JTable(graphics_context.other_color_table);
      other_color_table.setPreferredScrollableViewportSize(new Dimension(table_width, textfield_height));
      JScrollPane other_scroll_pane = init_color_table(other_color_table, p_board_frame.get_locale());
      panel.add(other_scroll_pane, BorderLayout.SOUTH);
      add(panel);
      p_board_frame.set_context_sensitive_help(this, "WindowDisplay_Colors");
      pack();
      }

   /**
    * Reassigns the table model variables because they may have changed in p_graphics_context.
    */
   public void set_table_models(GdiContext p_graphics_context)
      {
      item_color_table.setModel(p_graphics_context.item_color_table);
      other_color_table.setModel(p_graphics_context.other_color_table);
      }

   /**
    * Initializes p_color_table and return the created scroll_pane of the color table.
    */
   private static JScrollPane init_color_table(JTable p_color_table, java.util.Locale p_locale)
      {
      // Create the scroll pane and add the table to it.
      JScrollPane scroll_pane = new JScrollPane(p_color_table);
      // Set up renderer and editor for the Color columns.
      p_color_table.setDefaultRenderer(Color.class, new TableColorRenderer(true));

      setUpColorEditor(p_color_table, p_locale);
      return scroll_pane;
      }

   // Set up the editor for the Color cells.
   private static void setUpColorEditor(JTable p_table, java.util.Locale p_locale)
      {
      // First, set up the button that brings up the dialog.
      final JButton button = new JButton("")
         {
         private static final long serialVersionUID = 1L;

         public void setText(String s)
            {
            // Button never shows text -- only color.
            }
         };
      button.setBackground(Color.white);
      button.setBorderPainted(false);
      button.setMargin(new Insets(0, 0, 0, 0));

      // Now create an editor to encapsulate the button, and
      // set it up as the editor for all Color cells.
      final TableColorEditor colorEditor = new TableColorEditor(button);
      p_table.setDefaultEditor(Color.class, colorEditor);

      // Set up the dialog that the button brings up.
      final JColorChooser colorChooser = new JColorChooser();
      ActionListener okListener = new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
               {
               colorEditor.currentColor = colorChooser.getColor();
               }
         };
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.Default", p_locale);
      final JDialog dialog = JColorChooser.createDialog(button, resources.getString("pick_a_color"), true, colorChooser, okListener, null);

      // Here's the code that brings up the dialog.
      button.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
               {
               button.setBackground(colorEditor.currentColor);
               colorChooser.setColor(colorEditor.currentColor);
               // Without the following line, the dialog comes up
               // in the middle of the screen.
               // dialog.setLocationRelativeTo(button);
               dialog.setVisible(true);
               }
         });
      }

   private final JTable item_color_table;
   private final JTable other_color_table;


   }
