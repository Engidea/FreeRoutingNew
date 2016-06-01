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
 * RouteDetailWindow.java
 *
 * Created on 18. November 2004, 07:31
 */
package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import board.items.BrdOutline;

/**
 * To be delete, paramters ahave been incorporated into window route
 *
 * @author Alfons Wirtz
 */
public class WindowRouteDetail extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private final interactive.IteraBoard board_handling;
   private final JSlider pullt_min_move_slider;
   private final JRadioButton on_button;
   private final JRadioButton off_button;
   private final JCheckBox outline_keepout_check_box;
   private static final int c_max_slider_value = 100;
   private static final int c_accuracy_scale_factor = 20;

   private WindowRouteDetail(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      this.board_handling = p_board_frame.board_panel.itera_board;
      GuiResources resources = board_frame.newGuiResources("gui.resources.WindowRouteDetail");
      
      this.setTitle(resources.getString("title"));

      // create main panel

      final JPanel main_panel = new JPanel();
      add(main_panel);
      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      main_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.anchor = java.awt.GridBagConstraints.WEST;
      gridbag_constraints.insets = new java.awt.Insets(5, 10, 5, 10);

      // add label and button group for the clearance compensation.

      JLabel clearance_compensation_label = new JLabel(resources.getString("clearance_compensation"));
      clearance_compensation_label.setToolTipText(resources.getString("clearance_compensation_tooltip"));

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      gridbag_constraints.gridheight = 2;
      gridbag.setConstraints(clearance_compensation_label, gridbag_constraints);
      main_panel.add(clearance_compensation_label);

      on_button = new JRadioButton(resources.getString("on"));
      off_button = new JRadioButton(resources.getString("off"));

      on_button.addActionListener(new CompensationOnListener());
      off_button.addActionListener(new CompensationOffListener());

      ButtonGroup clearance_compensation_button_group = new ButtonGroup();
      clearance_compensation_button_group.add(on_button);
      clearance_compensation_button_group.add(off_button);
      off_button.setSelected(true);

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag_constraints.gridheight = 1;
      gridbag.setConstraints(on_button, gridbag_constraints);
      main_panel.add(on_button, gridbag_constraints);
      gridbag.setConstraints(off_button, gridbag_constraints);
      main_panel.add(off_button, gridbag_constraints);

      JLabel separator = new JLabel("  ----------------------------------------  ");
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      // add label and slider for the pull tight accuracy.

      JLabel pull_tight_accuracy_label = new JLabel(resources.getString("pull_tight_accuracy"));
      pull_tight_accuracy_label.setToolTipText(resources.getString("pull_tight_accuracy_tooltip"));
      gridbag_constraints.insets = new java.awt.Insets(5, 10, 5, 10);
      gridbag.setConstraints(pull_tight_accuracy_label, gridbag_constraints);
      main_panel.add(pull_tight_accuracy_label);

      this.pullt_min_move_slider = new JSlider();
      pullt_min_move_slider.setMaximum(c_max_slider_value);
      pullt_min_move_slider.addChangeListener(new SliderChangeListener());
      gridbag.setConstraints(pullt_min_move_slider, gridbag_constraints);
      main_panel.add(pullt_min_move_slider);

      separator = new JLabel("  ----------------------------------------  ");
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      // add switch to define, if keepout is generated outside the outline.

      this.outline_keepout_check_box = new JCheckBox(resources.getString("keepout_outside_outline"));
      this.outline_keepout_check_box.setSelected(false);
      this.outline_keepout_check_box.addActionListener(new OutLineKeepoutListener());
      gridbag.setConstraints(outline_keepout_check_box, gridbag_constraints);
      this.outline_keepout_check_box.setToolTipText(resources.getString("keepout_outside_outline_tooltip"));
      main_panel.add(outline_keepout_check_box, gridbag_constraints);

      separator = new JLabel();
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      refresh();
      pack();
      setLocationRelativeTo(null);
      }

   /**
    * Recalculates all displayed values
    */
   public void refresh()
      {
      if (this.board_handling.get_routing_board().search_tree_manager.is_clearance_compensation_used())
         {
         on_button.setSelected(true);
         }
      else
         {
         off_button.setSelected(true);
         }
      BrdOutline outline = board_handling.get_routing_board().get_outline();
      if (outline != null)
         {
         outline_keepout_check_box.setSelected(outline.keepout_outside_outline_generated());
         }
      
      int accuracy_slider_value = c_max_slider_value - board_handling.itera_settings.trace_pullt_min_move / c_accuracy_scale_factor + 1;
      pullt_min_move_slider.setValue(accuracy_slider_value);
      }


   private class CompensationOnListener implements java.awt.event.ActionListener
      {

      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         board_handling.set_clearance_compensation(true);
         }
      }

   private class CompensationOffListener implements java.awt.event.ActionListener
      {

      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         board_handling.set_clearance_compensation(false);
         }
      }

   private class SliderChangeListener implements ChangeListener
      {

      public void stateChanged(ChangeEvent evt)
         {
         int new_accurracy = (c_max_slider_value - pullt_min_move_slider.getValue() + 1) * c_accuracy_scale_factor;
         board_handling.itera_settings.pull_tight_accuracy_set(new_accurracy);
         }
      }

   private class OutLineKeepoutListener implements java.awt.event.ActionListener
      {

      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (board_handling.is_board_read_only())
            {
            return;
            }
         BrdOutline outline = board_handling.get_routing_board().get_outline();
         if (outline != null)
            {
            outline.generate_keepout_outside(outline_keepout_check_box.isSelected());
            }
         }
      }
   }
