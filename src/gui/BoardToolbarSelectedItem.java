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
 * BoardSelectedItemToolbar.java
 *
 * Created on 16. Februar 2005, 05:59
 */

package gui;

import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import main.Stat;

/**
 * Describes the toolbar of the board frame, when it is in the selected item state.
 *
 * @author Alfons Wirtz
 */
class BoardToolbarSelectedItem extends JToolBar
   {
   private static final long serialVersionUID = 1L;
   
   private final ItemActionListener action_listener = new ItemActionListener();

   private final BoardFrame board_frame;
   private final GuiResources resources;
   
   private final JButton tidy_button;

   /**
    * Creates a new instance of BoardSelectedItemToolbar. If p_extended, some additional buttons are generated.
    */
   BoardToolbarSelectedItem(Stat stat, BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;
      
      boolean p_extended = false; // Damiano, you may move the two menu somewhere else

      resources = new GuiResources(stat,"gui.resources.BoardToolbarSelectedItem");
 
      JButton cancel_button = new JButton();
      cancel_button.setText(resources.getString("cancel"));
      cancel_button.setToolTipText(resources.getString("cancel_tooltip"));
      cancel_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.cancel_state();
               }
         });

      add(cancel_button);

      JButton info_button = new JButton();
      info_button.setText(resources.getString("info"));
      info_button.setToolTipText(resources.getString("info_tooltip"));
      info_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.display_selected_item_info();
               }
         });

      add(info_button);

      JButton delete_button = new JButton();
      delete_button.setText(resources.getString("delete"));
      delete_button.setToolTipText(resources.getString("delete_tooltip"));
      delete_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.delete_selected_items();
               }
         });

      add(delete_button);

      JButton cutout_button = new JButton();
      cutout_button.setText(resources.getString("cutout"));
      cutout_button.setToolTipText(resources.getString("cutout_tooltip"));
      cutout_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.cutout_selected_items();
               }
         });

      add(cutout_button);

      JButton fix_button = new JButton();
      fix_button.setText(resources.getString("fix"));
      fix_button.setToolTipText(resources.getString("fix_tooltip"));
      fix_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.fix_selected_items();
               }
         });

      add(fix_button);

      JButton unfix_button = new JButton();
      unfix_button.setText(resources.getString("unfix"));
      unfix_button.setToolTipText(resources.getString("unfix_tooltip"));
      unfix_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.unfix_selected_items();
               }
         });

      add(unfix_button);

      JButton autoroute_button = new JButton();
      autoroute_button.setText(resources.getString("autoroute"));
      autoroute_button.setToolTipText(resources.getString("autoroute_tooltip"));
      autoroute_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.autoroute_selected_items();
               }
         });
      add(autoroute_button);

      tidy_button = resources.newJButton("pull_tight","pull_tight_tooltip",action_listener);
      add(tidy_button);

      JButton clearance_class_button = new JButton();
      clearance_class_button.setText(resources.getString("spacing"));
      clearance_class_button.setToolTipText(resources.getString("spacing_tooltip"));
      clearance_class_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               assign_clearance_class();
               }
         });

      JButton fanout_button = new JButton();
      fanout_button.setText(resources.getString("fanout"));
      fanout_button.setToolTipText(resources.getString("fanout_tooltip"));
      fanout_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.fanout_selected_items();
               }
         });
      add(fanout_button);

      add(clearance_class_button);

      JLabel jLabel5 = new JLabel();
      jLabel5.setMaximumSize(new java.awt.Dimension(10, 10));
      jLabel5.setPreferredSize(new java.awt.Dimension(10, 10));
      add(jLabel5);

      JButton whole_nets_button = new JButton();
      whole_nets_button.setText(resources.getString("nets"));
      whole_nets_button.setToolTipText(resources.getString("nets_tooltip"));
      whole_nets_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.extend_selection_to_whole_nets();
               }
         });

      add(whole_nets_button);

      JButton whole_connected_sets_button = new JButton();
      whole_connected_sets_button.setText(resources.getString("conn_sets"));
      whole_connected_sets_button.setToolTipText(resources.getString("conn_sets_tooltip"));
      whole_connected_sets_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.extend_selection_to_whole_connected_sets();
               }
         });

      add(whole_connected_sets_button);

      JButton whole_connections_button = new JButton();
      whole_connections_button.setText(resources.getString("connections"));
      whole_connections_button.setToolTipText(resources.getString("connections_tooltip"));
      whole_connections_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.extend_selection_to_whole_connections();
               }
         });

      add(whole_connections_button);

      JButton whole_groups_button = new JButton();
      whole_groups_button.setText(resources.getString("components"));
      whole_groups_button.setToolTipText(resources.getString("components_tooltip"));
      whole_groups_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.extend_selection_to_whole_components();
               }
         });

      add(whole_groups_button);

      if (p_extended)
         {
         JButton new_net_button = new JButton();
         new_net_button.setText(resources.getString("new_net"));
         new_net_button.setToolTipText(resources.getString("new_net_tooltip"));
         new_net_button.addActionListener(new java.awt.event.ActionListener()
            {
               public void actionPerformed(java.awt.event.ActionEvent evt)
                  {
                  board_frame.board_panel.board_handling.assign_selected_to_new_net();
                  }
            });

         add(new_net_button);

         JButton new_group_button = new JButton();
         new_group_button.setText(resources.getString("new_component"));
         new_group_button.setToolTipText(resources.getString("new_component_tooltip"));
         new_group_button.addActionListener(new java.awt.event.ActionListener()
            {
               public void actionPerformed(java.awt.event.ActionEvent evt)
                  {
                  board_frame.board_panel.board_handling.assign_selected_to_new_group();
                  }
            });

         add(new_group_button);
         }

      JLabel jLabel6 = new JLabel();
      jLabel6.setMaximumSize(new java.awt.Dimension(10, 10));
      jLabel6.setPreferredSize(new java.awt.Dimension(10, 10));
      add(jLabel6);

      JButton violation_button = new JButton();
      violation_button.setText(resources.getString("violations"));
      violation_button.setToolTipText(resources.getString("violations_tooltip"));
      violation_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.toggle_selected_item_violations();
               }
         });

      add(violation_button);

      JLabel jLabel7 = new JLabel();
      jLabel7.setMaximumSize(new java.awt.Dimension(10, 10));
      jLabel7.setPreferredSize(new java.awt.Dimension(10, 10));
      add(jLabel7);

      JButton display_selection_button = new JButton();
      display_selection_button.setText(resources.getString("zoom_selection"));
      display_selection_button.setToolTipText(resources.getString("zoom_selection_tooltip"));
      display_selection_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.zoom_selection();

               }
         });
      add(display_selection_button);

      JButton display_all_button = new JButton();
      display_all_button.setText(resources.getString("zoom_all"));
      display_all_button.setToolTipText(resources.getString("zoom_all_tooltip"));
      display_all_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.zoom_all();
               }
         });
      add(display_all_button);

      JButton display_region_button = new JButton();
      display_region_button.setText(resources.getString("zoom_region"));
      display_region_button.setToolTipText(resources.getString("zoom_region_tooltip"));
      display_region_button.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               board_frame.board_panel.board_handling.zoom_region();
               }
         });

      add(display_region_button);
      }

   private void assign_clearance_class()
      {
      if (board_frame.board_panel.board_handling.is_board_read_only())
         {
         return;
         }
      freert.rules.ClearanceMatrix clearance_matrix = board_frame.board_panel.board_handling.get_routing_board().brd_rules.clearance_matrix;
      Object[] class_name_arr = new Object[clearance_matrix.get_class_count()];
      for (int i = 0; i < class_name_arr.length; ++i)
         {
         class_name_arr[i] = clearance_matrix.get_name(i);
         }
      Object selected_value = JOptionPane.showInputDialog(null, resources.getString("select_clearance_class"), resources.getString("assign_clearance_class"),
            JOptionPane.INFORMATION_MESSAGE, null, class_name_arr, class_name_arr[0]);
      if (selected_value == null || !(selected_value instanceof String))
         {
         return;
         }
      int class_index = clearance_matrix.get_no((String) selected_value);
      if (class_index < 0)
         {
         return;
         }
      board_frame.board_panel.board_handling.assign_clearance_classs_to_selected_items(class_index);
      }

   private final class ItemActionListener implements ActionListener
   {
   public void actionPerformed(java.awt.event.ActionEvent evt)
      {
      Object source = evt.getSource();

      if (source == tidy_button )
         board_frame.board_panel.board_handling.optimize_selected_items();

      }
   }
   
   
   }
