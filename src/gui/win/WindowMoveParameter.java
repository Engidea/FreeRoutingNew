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
 * WindowMoveParameter.java
 *
 * Created on 16. September 2005, 06:53
 *
 */

package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import main.Stat;

/**
 * Window with the parameters for moving components.
 *
 * @author Alfons Wirtz
 */
public class WindowMoveParameter extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowMoveParameter.";

   private final Stat stat;
   private final interactive.IteraBoard board_handling;
   private final JFormattedTextField horizontal_grid_field;
   private final JFormattedTextField vertical_grid_field;
   private boolean key_input_completed = true;
   private final javax.swing.JRadioButton zoom_button;
   private final javax.swing.JRadioButton rotate_button;
   
   public WindowMoveParameter(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      this.stat = stat;
      this.board_handling = p_board_frame.board_panel.itera_board;
      
      GuiResources resources = new GuiResources(stat,"gui.resources.WindowMoveParameter");
      
      setTitle(resources.getString("title"));

      // create main panel

      final javax.swing.JPanel main_panel = new javax.swing.JPanel();
      this.add(main_panel);
      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      main_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.anchor = java.awt.GridBagConstraints.WEST;
      gridbag_constraints.insets = new java.awt.Insets(1, 10, 1, 10);

      // Create label and number field for the horizontal and verical component grid

      gridbag_constraints.gridwidth = 2;
      JLabel horizontal_grid_label = resources.newJLabel("horizontal_component_grid");
      gridbag.setConstraints(horizontal_grid_label, gridbag_constraints);
      main_panel.add(horizontal_grid_label);

      java.text.NumberFormat number_format = java.text.NumberFormat.getInstance(p_board_frame.get_locale());
      number_format.setMaximumFractionDigits(7);
      this.horizontal_grid_field = new JFormattedTextField(number_format);
      this.horizontal_grid_field.setColumns(5);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag.setConstraints(horizontal_grid_field, gridbag_constraints);
      main_panel.add(horizontal_grid_field);
      horizontal_grid_field_sync();
      horizontal_grid_field.addKeyListener(new HorizontalGridFieldKeyListener());
      horizontal_grid_field.addFocusListener(new HorizontalGridFieldFocusListener());

      gridbag_constraints.gridwidth = 2;
      JLabel vertical_grid_label = resources.newJLabel("vertical_component_grid");
      gridbag.setConstraints(vertical_grid_label, gridbag_constraints);
      main_panel.add(vertical_grid_label);

      this.vertical_grid_field = new javax.swing.JFormattedTextField(number_format);
      this.vertical_grid_field.setColumns(5);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag.setConstraints(vertical_grid_field, gridbag_constraints);
      main_panel.add(vertical_grid_field);
      vertical_grid_field_sync();
      vertical_grid_field.addKeyListener(new VerticalGridFieldKeyListener());
      vertical_grid_field.addFocusListener(new VerticalGridFieldFocusListener());

      JLabel separator = new JLabel("  -----------------------------------------------  ");
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      // add label and button group for the wheel function.

      JLabel wheel_function_label = resources.newJLabel("wheel_function");
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      gridbag_constraints.gridheight = 2;
      gridbag.setConstraints(wheel_function_label, gridbag_constraints);
      main_panel.add(wheel_function_label);
      wheel_function_label.setToolTipText(resources.getString("wheel_function_tooltip"));

      this.zoom_button = new javax.swing.JRadioButton(resources.getString("zoom"));
      this.rotate_button = new javax.swing.JRadioButton(resources.getString("rotate"));

      zoom_button.addActionListener(new ZoomButtonListener());
      rotate_button.addActionListener(new RotateButtonListener());

      javax.swing.ButtonGroup button_group = new javax.swing.ButtonGroup();
      button_group.add(zoom_button);
      button_group.add(rotate_button);
      if (this.board_handling.itera_settings.get_zoom_with_wheel())
         {
         zoom_button.setSelected(true);
         }
      else
         {
         rotate_button.setSelected(true);
         }

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag_constraints.gridheight = 1;
      gridbag.setConstraints(zoom_button, gridbag_constraints);
      main_panel.add(zoom_button, gridbag_constraints);
      gridbag.setConstraints(rotate_button, gridbag_constraints);
      main_panel.add(rotate_button, gridbag_constraints);

      p_board_frame.set_context_sensitive_help(this, "WindowMoveParameter");

      refresh();
      pack();
      }

   private void horizontal_grid_field_sync()
      {
      double p_value = board_handling.itera_settings.get_horizontal_component_grid();
      
      if (p_value <= 0)
         {
         this.horizontal_grid_field.setValue(0);
         }
      else
         {
         Float grid_width = (float) board_handling.coordinate_transform.board_to_user(p_value);
         this.horizontal_grid_field.setValue(grid_width);
         }
      }

   private void vertical_grid_field_sync()
      {
      double p_value = board_handling.itera_settings.get_vertical_component_grid();
      if (p_value <= 0)
         {
         this.vertical_grid_field.setValue(0);
         }
      else
         {
         Float grid_width = (float) board_handling.coordinate_transform.board_to_user(p_value);
         vertical_grid_field.setValue(grid_width);
         }
      }

   private double clean_input_value ( JFormattedTextField afield )
      {
      if ( afield == null ) return 0;
      
      Object input = afield.getValue();

      if (!(input instanceof Number)) return 0;

      Number anumber = (Number) input;
      
      double adouble = anumber.doubleValue();

      if (adouble < 0) return 0;

      return adouble;
      }

   private class HorizontalGridFieldKeyListener extends java.awt.event.KeyAdapter
      {
      public void keyTyped(java.awt.event.KeyEvent p_evt)
         {
         if (p_evt.getKeyChar() == '\n')
            {
            key_input_completed = true;

            double input_value = clean_input_value(horizontal_grid_field);

            stat.userPrintln(classname+" horizontal "+input_value);
            
            board_handling.itera_settings.set_horizontal_component_grid((int) Math.round(board_handling.coordinate_transform.user_to_board(input_value)));

            horizontal_grid_field_sync();
            }
         else
            {
            key_input_completed = false;
            }
         }
      }

   private class HorizontalGridFieldFocusListener implements java.awt.event.FocusListener
      {
      public void focusLost(java.awt.event.FocusEvent p_evt)
         {
         if (!key_input_completed)
            {
            // restore the text field.
            horizontal_grid_field_sync();
            key_input_completed = true;
            }
         }

      public void focusGained(java.awt.event.FocusEvent p_evt)
         {
         }
      }

   private class VerticalGridFieldKeyListener extends java.awt.event.KeyAdapter
      {
      public void keyTyped(java.awt.event.KeyEvent p_evt)
         {
         if (p_evt.getKeyChar() == '\n')
            {
            key_input_completed = true;

            double input_value = clean_input_value(vertical_grid_field);

            stat.userPrintln(classname+" vertical "+input_value);

            board_handling.itera_settings.set_vertical_component_grid((int) Math.round(board_handling.coordinate_transform.user_to_board(input_value)));

            vertical_grid_field_sync();
            }
         else
            {
            key_input_completed = false;
            }
         }
      }

   private class VerticalGridFieldFocusListener implements java.awt.event.FocusListener
      {
      public void focusLost(java.awt.event.FocusEvent p_evt)
         {
         if (!key_input_completed)
            {
            // restore the text field.
            vertical_grid_field_sync();
            key_input_completed = true;
            }
         }

      public void focusGained(java.awt.event.FocusEvent p_evt)
         {
         }
      }

   private class ZoomButtonListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         board_handling.itera_settings.set_zoom_with_wheel(true);
         }
      }

   private class RotateButtonListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         board_handling.itera_settings.set_zoom_with_wheel(false);
         }
      }

   }
