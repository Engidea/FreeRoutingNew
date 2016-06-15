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
 * RouteParameterWindow.java
 *
 * Created on 17. November 2004, 07:11
 */

package gui.win;

import freert.main.Stat;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiPanelVertical;
import gui.varie.GuiResources;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import board.items.BrdOutline;
import board.items.BrdTracep;
import board.varie.TraceAngleRestriction;

/**
 * Window handling parameters of the interactive routing.
 *
 * @author Alfons Wirtz
 */
public class WindowRouteParameter extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowRouteParameter.";
   
   private final RouteParameterActionListener actionListener = new RouteParameterActionListener();

   private final Stat stat;
   private final interactive.IteraBoard i_board;
   private final GuiResources resources;
   private final JSlider pullt_min_move_slider;
   private final JFormattedTextField pullt_min_move_field;
   private final JFormattedTextField edge_to_turn_dist_field;

   private final JRadioButton snap_angle_45_button;
   private final JRadioButton snap_angle_none_button;
   
   private final JRadioButton dynamic_button;
   private final JRadioButton stitch_button;
   private final JRadioButton automatic_button;
   private final JRadioButton manual_button;
   
   private final JCheckBox shove_check;
   private final JCheckBox drag_component_check;
   private final JCheckBox ignore_conduction_check;
   private final JCheckBox via_snap_to_smd_center_check;
   private final JCheckBox hilight_routing_obstacle_check;
   private final JCheckBox neckdown_check;
   private final JCheckBox restrict_pin_exit_directions_check;
   private final JCheckBox clearance_compensation_check;
   private final JCheckBox outline_keepout_check;

   private boolean key_input_completed = true;
   
   private static final int c_accuracy_max_slider_value = 100;
   private static final int c_accuracy_scale_factor = 20;

   public  final WindowManualRules manual_rule_window;
   
   public WindowRouteParameter(Stat p_stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      stat = p_stat;
      i_board = p_board_frame.board_panel.itera_board;
      resources = new GuiResources(p_stat, "gui.resources.WindowRouteParameter");

      manual_rule_window = new WindowManualRules(p_board_frame);

      setTitle(resources.getString("title"));

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      GuiPanelVertical main_panel = new GuiPanelVertical(new Insets(3,3,3,3));

      snap_angle_45_button = resources.newJRadioButton("45_degree",actionListener);
      snap_angle_none_button = resources.newJRadioButton("none",actionListener);

      main_panel.add(newSnapAnglePanel());
      
      dynamic_button = resources.newJRadioButton("dynamic",actionListener);
      stitch_button = resources.newJRadioButton("stitching",actionListener);

      main_panel.add(newRouteModePanel());

      automatic_button = resources.newJRadioButton("automatic",new AutomaticTraceWidthListener());
      manual_button = resources.newJRadioButton("manual",new ManualTraceWidthListener());

      main_panel.add(newRuleSelectionPanel());

      shove_check = resources.newJCheckBox("push&shove_enabled","push&shove_enabled_tooltip",new ShoveListener());
      drag_component_check = resources.newJCheckBox("drag_components_enabled","drag_components_enabled_tooltip",new DragComponentListener());
      via_snap_to_smd_center_check = resources.newJCheckBox("via_snap_to_smd_center","via_snap_to_smd_center_tooltip",new ViaSnapToSMDCenterListener());
      hilight_routing_obstacle_check = resources.newJCheckBox("hilight_routing_obstacle","hilight_routing_obstacle_tooltip",new HilightObstacleListener());
      ignore_conduction_check = resources.newJCheckBox("ignore_conduction_areas","ignore_conduction_areas_tooltip",new IgnoreConductionListener());
      neckdown_check = resources.newJCheckBox("automatic_neckdown","automatic_neckdown_tooltip",actionListener);
      clearance_compensation_check = resources.newJCheckBox("clearance_compensation","clearance_compensation_tooltip",actionListener);
      outline_keepout_check = resources.newJCheckBox("keepout_outside_outline","keepout_outside_outline_tooltip",actionListener);
      
      main_panel.add(newCheckboxesPanel());

      restrict_pin_exit_directions_check = resources.newJCheckBox("restrict_pin_exit_directions","restrict_pin_exit_directions_tooltip",new RestrictPinExitDirectionsListener());

      NumberFormat number_format = NumberFormat.getInstance(p_board_frame.get_locale());
      number_format.setGroupingUsed(false);
      
      edge_to_turn_dist_field = newJNumber(number_format,5,true);
      edge_to_turn_dist_field.addKeyListener(new EdgeToTurnDistFieldKeyListener());
      edge_to_turn_dist_field.addFocusListener(new EdgeToTurnDistFieldFocusListener());

      main_panel.add(newPinExitPanel());

      pullt_min_move_field = newJNumber(number_format,5,false);
      
      pullt_min_move_slider = new JSlider();
      pullt_min_move_slider.setMaximum(c_accuracy_max_slider_value);
      pullt_min_move_slider.addChangeListener(new PullTightAccuracyChangeListener());
      
      main_panel.add(newPullTightPanel());

      add(main_panel.getJPanel());
      
      p_board_frame.set_context_sensitive_help(this, "WindowRouteParameter");

      refresh();
      pack();
      }

   private JFormattedTextField newJNumber (NumberFormat number_format, int cols, boolean editable)
      {
      JFormattedTextField risul = new JFormattedTextField(number_format);
      risul.setColumns(cols);
      risul.setEditable(editable);
      
      return risul;
      }
   
   private JPanel newPullTightPanel ()
      {
      GuiPanelVertical risul = new GuiPanelVertical(new Insets(2,2,2,2));

      risul.setBorder( resources.newTitledBorder("pull_tight_parameter"));
      risul.setToolTipText(resources.getString("pull_tight_parameters_tooltip"));  

      JPanel inner = new JPanel();
      inner.add(resources.newJLabel("pull_tight_min_move","pull_tight_min_move_tooltip"));
      inner.add(pullt_min_move_field);
      
      risul.add(inner);
      risul.add(pullt_min_move_slider);
      
      return risul.getJPanel();
      }
   
   private JPanel newPinExitPanel ()
      {
      GuiPanelVertical risul = new GuiPanelVertical(new Insets(2,2,2,2));
      
      risul.setBorder(resources.newTitledBorder("pin_exit_options"));
      risul.setToolTipText(resources.getString("pin_exit_options_tooltip"));  
      
      risul.add(restrict_pin_exit_directions_check);

      JPanel inner = new JPanel();
      inner.add(resources.newJLabel("pin_pad_to_turn_gap","pin_pad_to_turn_gap_tooltip"));
      inner.add(edge_to_turn_dist_field);

      risul.add(inner);
      
      return risul.getJPanel();
      }
   
   
   private JPanel newCheckboxesPanel ()
      {
      JPanel risul = new JPanel();
      risul.setLayout(new BoxLayout(risul, BoxLayout.Y_AXIS));
      risul.setBorder(resources.newTitledBorder("route_options"));
      risul.setToolTipText(resources.getString("route_options_tooltip"));
      
      risul.add(shove_check);
      risul.add(drag_component_check);
      risul.add(via_snap_to_smd_center_check);
      risul.add(hilight_routing_obstacle_check);
      risul.add(ignore_conduction_check);
      risul.add(neckdown_check);
      risul.add(clearance_compensation_check);
      risul.add(outline_keepout_check);
      
      return risul;
      }
   
   
   private JPanel newRuleSelectionPanel ()
      {
      ButtonGroup trace_widths_button_group = new ButtonGroup();
      trace_widths_button_group.add(automatic_button);
      trace_widths_button_group.add(manual_button);
      automatic_button.setSelected(true);

      JPanel risul = new JPanel();
      risul.setBorder(resources.newTitledBorder("rule_selection"));
      risul.setToolTipText(resources.getString("rule_selection_tooltip"));
      
      risul.add(automatic_button);
      risul.add(manual_button);
      
      return risul;
      }

   
   private JPanel newRouteModePanel ()
      {
      ButtonGroup group = new ButtonGroup();
      group.add(dynamic_button);
      group.add(stitch_button);
      dynamic_button.setSelected(true);

      JPanel risul = new JPanel();
      risul.setBorder(resources.newTitledBorder("route_mode"));
      risul.setToolTipText(resources.getString("route_mode_tooltip"));
      
      risul.add(dynamic_button);
      risul.add(stitch_button);
      
      return risul;
      }
   

   private JPanel newSnapAnglePanel ()
      {
      ButtonGroup group = new ButtonGroup();
      group.add(snap_angle_45_button);
      group.add(snap_angle_none_button);
      snap_angle_none_button.setSelected(true);

      JPanel risul = new JPanel();
      risul.setBorder(resources.newTitledBorder("snap_angle"));
      risul.setToolTipText(resources.getString("snap_angle_tooltip"));

      risul.add(snap_angle_45_button);
      risul.add(snap_angle_none_button);
      
      return risul;
      }

   public void dispose()
      {
      manual_rule_window.dispose();
      
      super.dispose();
      }

   /**
    * Reads the data of this frame from disk. 
    * @returns false, if the reading failed.
    */
   @Override
   public boolean read(java.io.ObjectInputStream p_object_stream)
      {
      if ( ! super.read(p_object_stream) )
         stat.userPrintln(classname+"read ERROR a");
      
      if ( ! manual_rule_window.read(p_object_stream) )
         stat.userPrintln(classname+"read ERROR b");

      refresh();
      return true;
      }

   /**
    * Saves this frame to disk.
    */
   @Override
   public void save(java.io.ObjectOutputStream p_object_stream)
      {
      super.save(p_object_stream);
      manual_rule_window.save(p_object_stream);
      }

   /**
    * Recalculates all displayed values
    */
   @Override
   public void refresh()
      {
      TraceAngleRestriction snap_angle = i_board.get_routing_board().brd_rules.get_trace_snap_angle();

      if (snap_angle.is_limit_45())
         snap_angle_45_button.setSelected(true);
      else
         snap_angle_none_button.setSelected(true);

      if (i_board.itera_settings.is_stitch_route())
         stitch_button.setSelected(true);
      else
         dynamic_button.setSelected(true);

      if (i_board.itera_settings.get_manual_rule_selection())
         {
         manual_button.setSelected(true);
         manual_rule_window.setVisible(true);
         }
      else
         {
         automatic_button.setSelected(true);
         }

      shove_check.setSelected(i_board.itera_settings.is_push_enabled());
      drag_component_check.setSelected(i_board.itera_settings.get_drag_components_enabled());
      via_snap_to_smd_center_check.setSelected(i_board.itera_settings.is_via_snap_to_smd_center());
      ignore_conduction_check.setSelected(i_board.get_routing_board().brd_rules.get_ignore_conduction());
      hilight_routing_obstacle_check.setSelected(i_board.itera_settings.is_hilight_routing_obstacle());
      neckdown_check.setSelected(i_board.itera_settings.is_automatic_neckdown());
      clearance_compensation_check.setSelected(i_board.get_routing_board().search_tree_manager.is_clearance_compensation_used());
      
      BrdOutline outline = i_board.get_routing_board().get_outline();
      if (outline != null)
         {
         outline_keepout_check.setSelected(outline.keepout_outside_outline_generated());
         }

      double edge_to_turn_dist = i_board.get_routing_board().brd_rules.get_pin_edge_to_turn_dist();
      edge_to_turn_dist = i_board.coordinate_transform.board_to_user(edge_to_turn_dist);

      edge_to_turn_dist_field.setValue(edge_to_turn_dist);
      restrict_pin_exit_directions_check.setSelected(edge_to_turn_dist > 0);

      int min_move_slider_value = i_board.itera_settings.trace_pullt_min_move / c_accuracy_scale_factor;
      min_move_slider_value = Math.min(min_move_slider_value, c_accuracy_max_slider_value);
      pullt_min_move_slider.setValue(min_move_slider_value);
  
      manual_rule_window.refresh();
      }

   @Override
   public void parent_iconified()
      {
      manual_rule_window.parent_iconified();

      super.parent_iconified();
      }

   @Override
   public void parent_deiconified()
      {
      manual_rule_window.parent_deiconified();

      super.parent_deiconified();
      }
   
   private boolean has_free_angle_traces ()
      {
      Collection<BrdTracep> trace_list = i_board.get_routing_board().get_traces();
     
      for (BrdTracep curr_trace : trace_list)
         {
         if ( ! (curr_trace instanceof BrdTracep) ) continue;
         
         BrdTracep atrace = (BrdTracep) curr_trace;
         
         if (! atrace.polyline().is_orthogonal() )
            {
            return true;
            }
         }
      
      return false;
      }

   public void snap_angle_45_button_fun()
      {
      if (i_board.get_routing_board().brd_rules.is_trace_snap_45())
         {
         // this just means that current value is the same as what I want
         return;
         }
      
      if (has_free_angle_traces())
         {
         String curr_message = resources.getString("change_snap_angle_45");
         if (!WindowMessage.confirm(curr_message))
            {
            refresh();
            return;
            }
         }

      stat.userPrintln(classname+" req TraceAngleRestriction.FORTYFIVE_DEGREE");
      i_board.set_trace_snap_angle(TraceAngleRestriction.FORTYFIVE);
      }

   public void snap_angle_none_button_fun()
      {
      stat.userPrintln(classname+" req AngleRestriction.NONE");
      i_board.set_trace_snap_angle(TraceAngleRestriction.NONE);
      }

   public void dynamic_button_fun()
      {
      stat.userPrintln(classname+" req set_stitch_route(false)");
      i_board.itera_settings.set_stitch_route(false);
      }

   public void stitch_button_fun()
      {
      stat.userPrintln(classname+" req set_stitch_route(true)");
      i_board.itera_settings.set_stitch_route(true);
      }

   private void clearance_compensation_fun ()
      {
      boolean want = clearance_compensation_check.isSelected();
      stat.userPrintln(classname+" req clearance_compensation_fun want="+want);
      i_board.set_clearance_compensation(want);      
      }
   
   private void outline_keepout_fun ()
      {
      if (i_board.is_board_read_only())
         {
         stat.userPrintln(classname+" outline_keepout_fun board is read only");
         return;
         }
      
      BrdOutline outline = i_board.get_routing_board().get_outline();
      if (outline == null)
         {
         stat.userPrintln(classname+" outline_keepout_fun no outline, yet");
         return;
         }
      
      boolean want = outline_keepout_check.isSelected();
      stat.userPrintln(classname+" req outline_keepout_fun want="+want);
      outline.generate_keepout_outside(want);
      }
   
   
   
   private class AutomaticTraceWidthListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         manual_rule_window.setVisible(false);
         i_board.itera_settings.set_manual_tracewidth_selection(false);
         }
      }

   private class ManualTraceWidthListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (first_time)
            {
            java.awt.Point location = getLocation();
            manual_rule_window.setLocation((int) location.getX() + 200, (int) location.getY() + 200);
            first_time = false;
            }
         manual_rule_window.setVisible(true);
         i_board.itera_settings.set_manual_tracewidth_selection(true);
         }

      boolean first_time = true;
      }

   private class ShoveListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_push_enabled(shove_check.isSelected());
         refresh();
         }
      }

   private class ViaSnapToSMDCenterListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_via_snap_to_smd_center(via_snap_to_smd_center_check.isSelected());
         }
      }

   private class IgnoreConductionListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.set_ignore_conduction(ignore_conduction_check.isSelected());
         }
      }

   private class HilightObstacleListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_hilight_routing_obstacle(hilight_routing_obstacle_check.isSelected());
         }
      }

   private class DragComponentListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         i_board.itera_settings.set_drag_components_enabled(drag_component_check.isSelected());
         refresh();
         }
      }


   private class RestrictPinExitDirectionsListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (restrict_pin_exit_directions_check.isSelected())
            {
            freert.rules.BoardRules board_rules = i_board.get_routing_board().brd_rules;
            double edge_to_turn_dist = i_board.coordinate_transform.board_to_user(board_rules.get_min_trace_half_width());
            i_board.set_pin_edge_to_turn_dist(edge_to_turn_dist);
            }
         else
            {
            i_board.set_pin_edge_to_turn_dist(0);
            }
         refresh();
         }
      }

   private class EdgeToTurnDistFieldKeyListener extends java.awt.event.KeyAdapter
      {
      public void keyTyped(java.awt.event.KeyEvent p_evt)
         {
         if (p_evt.getKeyChar() == '\n')
            {
            key_input_completed = true;
            Object input = edge_to_turn_dist_field.getValue();
            if (!(input instanceof Number))
               {
               return;
               }
            float input_value = ((Number) input).floatValue();
            
            i_board.set_pin_edge_to_turn_dist(input_value);
            
            stat.userPrintln(classname+"set pin turn dist "+input_value);
            
            restrict_pin_exit_directions_check.setSelected(input_value > 0);
            
            refresh();
            }
         else
            {
            key_input_completed = false;
            }
         }
      }

   private class EdgeToTurnDistFieldFocusListener implements java.awt.event.FocusListener
      {
      public void focusLost(java.awt.event.FocusEvent p_evt)
         {
         if (!key_input_completed)
            {
            // restore the text field.
            double edge_to_turn_dist = i_board.get_routing_board().brd_rules.get_pin_edge_to_turn_dist();
            edge_to_turn_dist = i_board.coordinate_transform.board_to_user(edge_to_turn_dist);
            
            stat.userPrintln(classname+"restore pin turn dist "+edge_to_turn_dist);
            
            edge_to_turn_dist_field.setValue(edge_to_turn_dist);
            key_input_completed = true;
            }
         }

      public void focusGained(java.awt.event.FocusEvent p_evt)
         {
         }
      }
   
   private void set_pull_tight_accuracy(int p_slider_value)
      {
      int slider_value = Math.max(p_slider_value, 0);
      
      slider_value = Math.min(p_slider_value, c_accuracy_max_slider_value);
      
      int new_accurracy = slider_value + 1 * c_accuracy_scale_factor;

      pullt_min_move_field.setValue(new_accurracy);

      pullt_min_move_slider.setValue(slider_value);
      
      i_board.itera_settings.pull_tight_accuracy_set(new_accurracy);
      }

   
   private class PullTightAccuracyChangeListener implements ChangeListener
      {
      public void stateChanged(ChangeEvent evt)
         {
         set_pull_tight_accuracy(pullt_min_move_slider.getValue());
         }
      }

   

   private void neckdown_fun ()
      {
      boolean want = neckdown_check.isSelected();
      stat.userPrintln(classname+"neckdown_fun want="+want);
      i_board.itera_settings.set_automatic_neckdown(want);      
      }

   private class RouteParameterActionListener implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         Object src = p_evt.getSource();
         
         if ( src == snap_angle_45_button )
            snap_angle_45_button_fun();
         else if ( src == snap_angle_none_button )
            snap_angle_none_button_fun();
         else if ( src == dynamic_button )
            dynamic_button_fun();
         else if ( src == stitch_button )
            stitch_button_fun();
         else if ( src == clearance_compensation_check )
            clearance_compensation_fun();
         else if ( src == outline_keepout_check )
            outline_keepout_fun();
         else if ( src == neckdown_check )
            neckdown_fun();

         }
      }

   }
