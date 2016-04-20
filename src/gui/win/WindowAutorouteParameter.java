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
 * WindowAutorouteParameter.java
 *
 * Created on 24. Juli 2006, 07:20
 *
 */
package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.AutorouteParameterRow;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import main.Stat;
import autoroute.ArtSettings;
import board.BrdLayerStructure;

/**
 * Window handling parameters of the automatic routing
 * @author Alfons Wirtz
 */
public class WindowAutorouteParameter extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowAutorouteParameter.";
   
   private final AutoParamsListener actionListener = new AutoParamsListener();
   private final ArrayList<AutorouteParameterRow> param_list = new ArrayList<AutorouteParameterRow>();

   private final IteraBoard board_handling;
   private final JCheckBox vias_allowed,no_ripup,vias_remove_uconn;
   private final JCheckBox fanout_pass_button;
   private final JCheckBox autoroute_pass_button;
   private final JCheckBox postroute_pass_button;
   private final JButton detail_button;
   private final WindowAutorouteParameterDetail detail_window;
   private final String horizontal;
   private final String vertical;
   
   public WindowAutorouteParameter(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      board_handling = p_board_frame.board_panel.board_handling;
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.WindowAutorouteParameter");

      setTitle(resources.getString("title"));
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      // create main panel

      final JPanel main_panel = new JPanel();
      add(main_panel);
      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      main_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.anchor = java.awt.GridBagConstraints.WEST;
      gridbag_constraints.insets = new java.awt.Insets(1, 10, 1, 10);

      gridbag_constraints.gridwidth = 3;
      JLabel layer_label = resources.newJLabel("layer");
      gridbag.setConstraints(layer_label, gridbag_constraints);
      main_panel.add(layer_label);

      JLabel active_label = resources.newJLabel("active");
      gridbag.setConstraints(active_label, gridbag_constraints);
      main_panel.add(active_label);

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      JLabel preferred_direction_label = resources.newJLabel("preferred_direction");
      gridbag.setConstraints(preferred_direction_label, gridbag_constraints);
      main_panel.add(preferred_direction_label);

      this.horizontal = resources.getString("horizontal");
      this.vertical = resources.getString("vertical");

      board.BrdLayerStructure layer_structure = board_handling.get_routing_board().layer_structure;
      int signal_layer_count = layer_structure.signal_layer_count();

      for (int layer_no = 0; layer_no < signal_layer_count; ++layer_no)
         {
         AutorouteParameterRow arow = new AutorouteParameterRow(layer_no);
         board.BrdLayer curr_signal_layer = layer_structure.get_signal_layer(layer_no);
         arow.setName(curr_signal_layer.name);
         
         gridbag_constraints.gridwidth = 3;
         gridbag.setConstraints(arow.signal_layer_name,gridbag_constraints);
         main_panel.add(arow.signal_layer_name);

         arow.signal_layer_active.addActionListener(new LayerActiveListener(arow));
         gridbag.setConstraints(arow.signal_layer_active,gridbag_constraints);
         main_panel.add(arow.signal_layer_active);
         arow.signal_layer_combo.addItem(this.horizontal);
         arow.signal_layer_combo.addItem(this.vertical);
         arow.signal_layer_combo.addActionListener(new PreferredDirectionListener(arow));
         
         gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridbag.setConstraints(arow.signal_layer_combo, gridbag_constraints);
         main_panel.add(arow.signal_layer_combo);
         
         param_list.add(arow);
         }

      JLabel separator = new JLabel("----------------------------------------  ");

      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      JLabel alabel = resources.newJLabel("vias_allowed");
      gridbag.setConstraints(alabel, gridbag_constraints);
      main_panel.add(alabel);

      vias_allowed = new JCheckBox();
      vias_allowed.addActionListener(actionListener);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag.setConstraints(vias_allowed, gridbag_constraints);
      main_panel.add(vias_allowed);
      
      // ---------------------------------------------------------------------------
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      alabel = resources.newJLabel("no_ripup");
      gridbag.setConstraints(alabel, gridbag_constraints);
      main_panel.add(alabel);

      no_ripup = new JCheckBox();
      no_ripup.addActionListener(actionListener);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag.setConstraints(no_ripup, gridbag_constraints);
      main_panel.add(no_ripup);

      // ---------------------------------------------------------------------------
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      alabel = resources.newJLabel("vias_rem_unconn");
      gridbag.setConstraints(alabel, gridbag_constraints);
      main_panel.add(alabel);

      vias_remove_uconn = new JCheckBox();
      vias_remove_uconn.addActionListener(actionListener);
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag.setConstraints(vias_remove_uconn, gridbag_constraints);
      main_panel.add(vias_remove_uconn);
      
      // ---------------------------------------------------------------------------

      separator = new JLabel("----------------------------------------  ");
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      JLabel passes_label = resources.newJLabel("passes");

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      gridbag_constraints.gridheight = 3;
      gridbag.setConstraints(passes_label, gridbag_constraints);
      main_panel.add(passes_label);

      fanout_pass_button    = resources.newJCheckBox("fanout",actionListener);
      autoroute_pass_button = resources.newJCheckBox("autoroute",actionListener);
      postroute_pass_button = resources.newJCheckBox("postroute",actionListener);

      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridbag_constraints.gridheight = 1;
      
      gridbag.setConstraints(fanout_pass_button, gridbag_constraints);
      main_panel.add(fanout_pass_button, gridbag_constraints);

      gridbag.setConstraints(autoroute_pass_button, gridbag_constraints);
      main_panel.add(autoroute_pass_button, gridbag_constraints);
      
      gridbag.setConstraints(postroute_pass_button, gridbag_constraints);
      main_panel.add(postroute_pass_button, gridbag_constraints);

      separator = new JLabel("----------------------------------------  ");
      gridbag.setConstraints(separator, gridbag_constraints);
      main_panel.add(separator, gridbag_constraints);

      detail_window = new WindowAutorouteParameterDetail(stat, p_board_frame);

      detail_button = resources.newJButton("detail_parameter",null,actionListener);
      gridbag.setConstraints(detail_button, gridbag_constraints);
      main_panel.add(detail_button);

      p_board_frame.set_context_sensitive_help(this, "WindowAutorouteParameter");

      this.refresh();
      this.pack();
      }

   /**
    * Recalculates all displayed values
    */
   public void refresh()
      {
      ArtSettings settings = board_handling.itera_settings.autoroute_settings;
      BrdLayerStructure layer_structure = board_handling.get_routing_board().layer_structure;

      vias_allowed.setSelected(settings.vias_allowed);
      vias_remove_uconn.setSelected(settings.stop_remove_fanout_vias);
      no_ripup.setSelected(settings.no_ripup);
      fanout_pass_button.setSelected(settings.get_with_fanout());
      autoroute_pass_button.setSelected(settings.get_with_autoroute());
      postroute_pass_button.setSelected(settings.get_with_postroute());

      for (int i = 0; i < param_list.size(); ++i)
         {
         AutorouteParameterRow arow = param_list.get(i);
         arow.signal_layer_active.setSelected(settings.get_layer_active(layer_structure.get_layer_no(i)));

         if (settings.get_preferred_direction_is_horizontal(layer_structure.get_layer_no(i)))
            {
            arow.signal_layer_combo.setSelectedItem(this.horizontal);
            }
         else
            {
            arow.signal_layer_combo.setSelectedItem(this.vertical);
            }
         }
      this.detail_window.refresh();
      }

   public void dispose()
      {
      detail_window.dispose();
      super.dispose();
      }

   @Override
   public void parent_iconified()
      {
      detail_window.parent_iconified();
      super.parent_iconified();
      }

   @Override
   public void parent_deiconified()
      {
      detail_window.parent_deiconified();
      super.parent_deiconified();
      }


   private class LayerActiveListener implements ActionListener
      {
      private final AutorouteParameterRow param;

      public LayerActiveListener(AutorouteParameterRow p_param)
         {
         param = p_param;
         }

      public void actionPerformed(ActionEvent p_evt)
         {
         boolean selected = param.signal_layer_active.isSelected();
         int curr_layer_no = board_handling.get_routing_board().layer_structure.get_layer_no(param.signal_layer_no);
         board_handling.itera_settings.autoroute_settings.set_layer_active(curr_layer_no, selected);
         }
      }

   private class PreferredDirectionListener implements ActionListener
      {
      private final AutorouteParameterRow param;

      public PreferredDirectionListener(AutorouteParameterRow p_param)
         {
         param = p_param;
         }

      public void actionPerformed(ActionEvent p_evt)
         {
         int curr_layer_no = board_handling.get_routing_board().layer_structure.get_layer_no(param.signal_layer_no);
         board_handling.itera_settings.autoroute_settings.set_preferred_direction_is_horizontal(curr_layer_no, param.signal_layer_combo.getSelectedItem() == horizontal);
         }
      }



   
   private class AutoParamsListener implements ActionListener
      {
      public void actionPerformed(ActionEvent p_evt)
         {
         ArtSettings asettings = board_handling.itera_settings.autoroute_settings;

         Object source = p_evt.getSource();
         
         if ( source == fanout_pass_button )
            {
            asettings.set_with_fanout(fanout_pass_button.isSelected());
            asettings.pass_no_set(1);
            }
         else if ( source == postroute_pass_button )
            {
            asettings.set_with_postroute(postroute_pass_button.isSelected());
            asettings.pass_no_set(1);
            }
         else if ( source == autoroute_pass_button )
            {
            asettings.set_with_autoroute(autoroute_pass_button.isSelected());
            asettings.pass_no_set(1);
            }
         else if ( source == vias_allowed)
            {
            asettings.vias_allowed = vias_allowed.isSelected();
            board_handling.userPrintln(classname+"vias_allowed="+asettings.vias_allowed);
            }
         else if ( source == vias_remove_uconn)
            {
            asettings.stop_remove_fanout_vias = vias_remove_uconn.isSelected();
            board_handling.userPrintln(classname+"vias_remove_uconn="+asettings.stop_remove_fanout_vias);
            }
         else if ( source == no_ripup)
            {
            asettings.no_ripup = no_ripup.isSelected();
            board_handling.userPrintln(classname+"no_ripup="+asettings.no_ripup);
            }
         else if ( source == detail_button )
            {
            detail_window.setVisible(true);
            }
         }
      }
   }
