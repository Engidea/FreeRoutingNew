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
 * SnapshotFrame.java
 *
 * Created on 9. November 2004, 09:42
 */

package gui.win;

import freert.graphics.GdiCoordinateTransform;
import freert.graphics.ItemColorTableModel;
import freert.graphics.OtherColorTableModel;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import gui.varie.SnapSavedAttributes;
import interactive.IteraBoard;
import interactive.SnapShot;
import java.awt.Dimension;
import java.awt.Point;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Window handling snapshots of the interactive situation.
 *
 * @author Alfons Wirtz
 */
public final class WindowSnapshot extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private DefaultListModel<SnapShot> list_model = new DefaultListModel<SnapShot>();
   private final JList<SnapShot> gui_list;
   private final javax.swing.JTextField name_field;
   final WindowSnapshotSettings settings_window;
   private int snapshot_count = 0;
   private final GuiResources resources;
   
   public WindowSnapshot(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      settings_window = new WindowSnapshotSettings(p_board_frame);
      resources = board_frame.newGuiResources("gui.resources.WindowSnapshot");
      setTitle(resources.getString("title"));

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      // create main panel
      final JPanel main_panel = new JPanel();
      add(main_panel);
      main_panel.setLayout(new java.awt.BorderLayout());

      // create goto button
      JButton goto_button = new JButton(resources.getString("goto_snapshot"));
      goto_button.setToolTipText(resources.getString("goto_tooltip"));
      GotoListener goto_listener = new GotoListener();
      goto_button.addActionListener(goto_listener);
      main_panel.add(goto_button, java.awt.BorderLayout.NORTH);

      // create snapshot list
      gui_list = new JList<SnapShot>(list_model);
      gui_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      gui_list.setSelectedIndex(0);
      gui_list.setVisibleRowCount(5);
      gui_list.addMouseListener(new java.awt.event.MouseAdapter()
         {
            public void mouseClicked(java.awt.event.MouseEvent evt)
               {
               if (evt.getClickCount() > 1)
                  {
                  goto_selected();
                  }
               }
         });

      JScrollPane list_scroll_pane = new JScrollPane(gui_list);
      list_scroll_pane.setPreferredSize(new java.awt.Dimension(200, 100));
      main_panel.add(list_scroll_pane, java.awt.BorderLayout.CENTER);

      // create the south panel
      final JPanel south_panel = new JPanel();
      main_panel.add(south_panel, java.awt.BorderLayout.SOUTH);
      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      south_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;

      // create panel to add a new snapshot
      final JPanel add_panel = new JPanel();
      gridbag.setConstraints(add_panel, gridbag_constraints);
      add_panel.setLayout(new java.awt.BorderLayout());
      south_panel.add(add_panel);

      JButton add_button = new JButton(resources.getString("create"));
      AddListener add_listener = new AddListener();
      add_button.addActionListener(add_listener);
      add_panel.add(add_button, java.awt.BorderLayout.WEST);

      name_field = new JTextField(10);
      name_field.setText(resources.getString("snapshot") + " 1");
      add_panel.add(name_field, java.awt.BorderLayout.EAST);

      // create delete buttons
      JButton delete_button = new JButton(resources.getString("remove"));
      DeleteListener delete_listener = new DeleteListener();
      delete_button.addActionListener(delete_listener);
      gridbag.setConstraints(delete_button, gridbag_constraints);
      south_panel.add(delete_button);

      JButton delete_all_button = new JButton(resources.getString("remove_all"));
      DeleteAllListener delete_all_listener = new DeleteAllListener();
      delete_all_button.addActionListener(delete_all_listener);
      gridbag.setConstraints(delete_all_button, gridbag_constraints);
      south_panel.add(delete_all_button);

      // create button for the snapshot settings
      JButton settings_button = new JButton(resources.getString("settings"));
      settings_button.setToolTipText(resources.getString("settings_tooltip"));
      SettingsListener settings_listener = new SettingsListener();
      settings_button.addActionListener(settings_listener);
      gridbag.setConstraints(delete_all_button, gridbag_constraints);
      south_panel.add(settings_button);

      p_board_frame.set_context_sensitive_help(this, "WindowSnapshots");

      pack();
      }

   public void dispose()
      {
      settings_window.dispose();
      super.dispose();
      }

   @Override
   public void parent_iconified()
      {
      settings_window.parent_iconified();
      super.parent_iconified();
      }

   @Override
   public void parent_deiconified()
      {
      settings_window.parent_deiconified();
      super.parent_deiconified();
      }

   /**
    * Reads the data of this frame from disk. Returns false, if the reading failed.
    */
   @Override
   public boolean read(ObjectInputStream p_object_stream)
      {
      try
         {
         SnapSavedAttributes saved_attributes = (SnapSavedAttributes) p_object_stream.readObject();
         snapshot_count = saved_attributes.snapshot_count;
         list_model = saved_attributes.list_model;
         gui_list.setModel(list_model);
         String next_default_name = "snapshot " + (new Integer(snapshot_count + 1)).toString();
         name_field.setText(next_default_name);
         setLocation(saved_attributes.location);
         setVisible(saved_attributes.is_visible);
         settings_window.read(p_object_stream);
         return true;
         }
      catch (Exception e)
         {
         System.out.println("VisibilityFrame.read_attriutes: read failed");
         return false;
         }
      }

   /**
    * Saves this frame to disk.
    */
   @Override
   public void save(ObjectOutputStream p_object_stream)
      {
      SnapSavedAttributes saved_attributes = new SnapSavedAttributes(list_model, snapshot_count, getLocation(), isVisible());
      try
         {
         p_object_stream.writeObject(saved_attributes);
         }
      catch (java.io.IOException e)
         {
         System.out.println("VisibilityFrame.save_attriutes: save failed");
         }
      settings_window.save(p_object_stream);
      }

   public void goto_selected()
      {
      int index = gui_list.getSelectedIndex();
      if (index >= 0 && list_model.getSize() > index)
         {
         IteraBoard board_handling = board_frame.board_panel.board_handling;
         interactive.SnapShot curr_snapshot = (SnapShot) list_model.elementAt(index);

         curr_snapshot.go_to(board_handling);

         if (curr_snapshot.settings.get_snapshot_attributes().object_colors)
            {
            board_handling.gdi_context.item_color_table = new ItemColorTableModel(curr_snapshot.graphics_context.item_color_table);
            board_handling.gdi_context.other_color_table = new OtherColorTableModel(curr_snapshot.graphics_context.other_color_table);

            board_frame.color_manager.set_table_models(board_handling.gdi_context);
            }

         if (curr_snapshot.settings.get_snapshot_attributes().display_region)
            {
            Point viewport_position = curr_snapshot.copy_viewport_position();
            
            if (viewport_position != null)
               {
               board_handling.gdi_context.coordinate_transform = new GdiCoordinateTransform(curr_snapshot.graphics_context.coordinate_transform);
               Dimension panel_size = board_handling.gdi_context.get_panel_size();
               board_frame.board_panel.setSize(panel_size);
               board_frame.board_panel.setPreferredSize(panel_size);
               board_frame.set_viewport_position(viewport_position);
               }
            }

         board_frame.refresh_windows();
         board_frame.hilight_selected_button();
         board_frame.setVisible(true);
         board_frame.repaint();
         }
      }

   /**
    * Refreshs the displayed values in this window.
    */
   public void refresh()
      {
      settings_window.refresh();
      }


   private class AddListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         SnapShot new_snapshot = SnapShot.get_instance(name_field.getText(), board_frame.board_panel.board_handling);
         if (new_snapshot != null)
            {
            ++snapshot_count;
            list_model.addElement(new_snapshot);
            String next_default_name = resources.getString("snapshot") + " " + (new Integer(snapshot_count + 1)).toString();
            name_field.setText(next_default_name);
            }
         }
      }

   /**
    * Selects the item, which is previous to the current selected item in the list. The current selected item is then no more
    * selected.
    */
   public void select_previous_item()
      {
      if (!isVisible()) return;

      int selected_index = gui_list.getSelectedIndex();
      if (selected_index <= 0)
         {
         return;
         }
      gui_list.setSelectedIndex(selected_index - 1);
      }

   /**
    * Selects the item, which is next to the current selected item in the list. The current selected item is then no more selected.
    */
   public void select_next_item()
      {
      if (!isVisible())         return;
      
      int selected_index = gui_list.getSelectedIndex();
      if (selected_index < 0 || selected_index >= list_model.getSize() - 1)
         {
         return;
         }

      gui_list.setSelectedIndex(selected_index + 1);
      }

   private class DeleteListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         SnapShot selected_snapshot = gui_list.getSelectedValue();
         if (selected_snapshot != null)
            {
            list_model.removeElement(selected_snapshot);
            }
         }
      }

   private class DeleteAllListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         list_model.removeAllElements();
         }
      }

   private class GotoListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         goto_selected();
         }
      }

   private class SettingsListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (first_time)
            {
            Point location = getLocation();
            settings_window.setLocation((int) location.getX() + 200, (int) location.getY());
            first_time = false;
            }
         settings_window.setVisible(true);
         }

      boolean first_time = true;
      }

   }
