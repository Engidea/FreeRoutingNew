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

import freert.main.Stat;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.AutorouteParameterRow;
import gui.varie.GuiPanelVertical;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

   private final IteraBoard i_board;
   private final JCheckBox vias_allowed,no_ripup,vias_remove_uconn;
   private final JCheckBox fanout_pass_button;
   private final JCheckBox autoroute_pass_button;
   private final JCheckBox postroute_pass_button;
   private final JButton detail_button;
   private final WindowAutorouteParameterDetail detail_window;

   private final WinLayerTableModel layer_table_model;
   private final JTable layer_table;
   
   private final GuiResources resources;
   
   
   public WindowAutorouteParameter(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      i_board = p_board_frame.board_panel.itera_board;
      
      resources = board_frame.newGuiResources("gui.resources.WindowAutorouteParameter");

      setTitle(resources.getString("title"));
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      // create main panel
      GuiPanelVertical main_panel = new GuiPanelVertical(new Insets(3,3,3,3));
      
      layer_table_model = new WinLayerTableModel(i_board);
      layer_table = new JTable(layer_table_model);
      layer_table_model.adjustTableClumns(layer_table);
      
      main_panel.add(newLayersPanel());

      vias_allowed = resources.newJCheckBox("vias_allowed",actionListener);
      no_ripup     = resources.newJCheckBox("no_ripup",actionListener);
      vias_remove_uconn = resources.newJCheckBox("vias_rem_unconn",actionListener);
      
      main_panel.add(newOptionsPanel());

      fanout_pass_button    = resources.newJCheckBox("fanout",actionListener);
      autoroute_pass_button = resources.newJCheckBox("autoroute",actionListener);
      postroute_pass_button = resources.newJCheckBox("postroute",actionListener);

      main_panel.add(newPassesPanel());

      detail_window = new WindowAutorouteParameterDetail(stat, p_board_frame);

      detail_button = resources.newJButton("detail_parameter",null,actionListener);

      main_panel.add(detail_button);

      add(main_panel.getJPanel());

      p_board_frame.set_context_sensitive_help(this, "WindowAutorouteParameter");

      refresh();
      pack();
      }

   private JPanel newPassesPanel ()
      {
      JPanel risul = new JPanel();
      
      risul.setLayout(new BoxLayout(risul, BoxLayout.Y_AXIS));
      risul.setBorder(resources.newTitledBorder("passes"));
      risul.setToolTipText(resources.getString("autor_options_tooltip"));

      risul.add(fanout_pass_button);
      risul.add(autoroute_pass_button);
      risul.add(postroute_pass_button);
      
      return risul;
      }
   
   private JPanel newOptionsPanel ()
      {
      JPanel risul = new JPanel();
      
      risul.setLayout(new BoxLayout(risul, BoxLayout.Y_AXIS));
      risul.setBorder(resources.newTitledBorder("autor_options"));
      risul.setToolTipText(resources.getString("autor_options_tooltip"));
      
      risul.add(vias_allowed);
      risul.add(no_ripup);
      risul.add(vias_remove_uconn);

      return risul;
      }
   
   private JPanel newLayersPanel ()
      {
      JScrollPane a_scroll = new JScrollPane(layer_table);

      JPanel risul = new JPanel(new BorderLayout());

      risul.add(a_scroll,BorderLayout.CENTER);
      risul.setBorder(resources.newTitledBorder("layer_setup"));
      risul.setToolTipText(resources.getString("layer_setup_tooltip"));
      
      return risul;
      }

   
   
   /**
    * Recalculates all displayed values
    */
   public void refresh()
      {
      ArtSettings settings = i_board.itera_settings.autoroute_settings;
      BrdLayerStructure layer_structure = i_board.get_routing_board().layer_structure;

      vias_allowed.setSelected(settings.vias_allowed);
      vias_remove_uconn.setSelected(settings.stop_remove_fanout_vias);
      no_ripup.setSelected(settings.no_ripup);
      fanout_pass_button.setSelected(settings.get_with_fanout());
      autoroute_pass_button.setSelected(settings.get_with_autoroute());
      postroute_pass_button.setSelected(settings.get_with_postroute());

      int signal_layer_count = layer_structure.signal_layer_count();

      for (int layer_no = 0; layer_no < signal_layer_count; ++layer_no)
         {
         AutorouteParameterRow arow = layer_table_model.get_layer(layer_no);

         arow.signal_layer_name = layer_structure.get_name(layer_no);

         arow.signal_layer_active = settings.get_layer_active(layer_structure.get_layer_no(layer_no));

         if (settings.get_preferred_direction_is_horizontal(layer_structure.get_layer_no(layer_no)))
            {
            arow.signal_layer_pfdir = AutorouteParameterRow.PFDIR_horizontal;
            }
         else
            {
            arow.signal_layer_pfdir = AutorouteParameterRow.PFDIR_vertical;
            }
         }
      
      detail_window.refresh();
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
   
   private class AutoParamsListener implements ActionListener
      {
      public void actionPerformed(ActionEvent p_evt)
         {
         ArtSettings asettings = i_board.itera_settings.autoroute_settings;

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
            i_board.userPrintln(classname+"vias_allowed="+asettings.vias_allowed);
            }
         else if ( source == vias_remove_uconn)
            {
            asettings.stop_remove_fanout_vias = vias_remove_uconn.isSelected();
            i_board.userPrintln(classname+"vias_remove_uconn="+asettings.stop_remove_fanout_vias);
            }
         else if ( source == no_ripup)
            {
            asettings.no_ripup = no_ripup.isSelected();
            i_board.userPrintln(classname+"no_ripup="+asettings.no_ripup);
            }
         else if ( source == detail_button )
            {
            detail_window.setVisible(true);
            }
         }
      }
   }
