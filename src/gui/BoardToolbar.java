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
 * BoardToolbarPanel.java
 *
 * Created on 15. Februar 2005, 09:44
 */

package gui;

import gui.varie.GuiResources;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import main.Stat;

/**
 * Implements the toolbar panel of the board frame.
 *
 * @author Alfons Wirtz
 */
class BoardToolbar extends JPanel
   {
   private static final long serialVersionUID = 1L;

   private final BoardActionListener listener = new BoardActionListener();
   
   private final BoardFrame board_frame;
   private final JToggleButton select_button,route_button,drag_button;
   private final JButton autoroute_button;
   private final JButton undo_button,redo_button;
   private final JButton incompletes_button,violation_button;
   private final JButton display_all_button,display_region_button;
   
   BoardToolbar(Stat stat, BoardFrame p_board_frame)
      {
      board_frame = p_board_frame;

      GuiResources resources = new GuiResources(stat,"gui.resources.BoardToolbar");

      setLayout(new java.awt.BorderLayout());

      // create the left toolbar

      final JToolBar left_toolbar = new JToolBar();
      final ButtonGroup toolbar_button_group = new ButtonGroup();
      select_button = resources.newJToggleButton("select_button", "select_button_tooltip", listener);
      route_button = resources.newJToggleButton("route_button", "route_button_tooltip", listener);
      drag_button = resources.newJToggleButton("drag_button", "drag_button_tooltip", listener);

      left_toolbar.setMaximumSize(new java.awt.Dimension(1200, 23));
      toolbar_button_group.add(select_button);
      select_button.setSelected(true);

      left_toolbar.add(select_button);

      toolbar_button_group.add(route_button);

      left_toolbar.add(route_button);

      toolbar_button_group.add(drag_button);

      left_toolbar.add(drag_button);

      JLabel jLabel1 = new JLabel();
      jLabel1.setMaximumSize(new java.awt.Dimension(30, 10));
      jLabel1.setMinimumSize(new java.awt.Dimension(3, 10));
      jLabel1.setPreferredSize(new java.awt.Dimension(30, 10));
      left_toolbar.add(jLabel1);

      add(left_toolbar, java.awt.BorderLayout.WEST);

      // create the middle toolbar

      final JToolBar middle_toolbar = new JToolBar();

      autoroute_button = resources.newJButton("autoroute_button", "autoroute_button_tooltip", listener);

      middle_toolbar.add(autoroute_button);

      JLabel separator_2 = new JLabel();
      separator_2.setMaximumSize(new java.awt.Dimension(10, 10));
      separator_2.setPreferredSize(new java.awt.Dimension(10, 10));
      separator_2.setRequestFocusEnabled(false);
      middle_toolbar.add(separator_2);

      undo_button = resources.newJButton("undo_button", "undo_button_tooltip", listener);

      middle_toolbar.add(undo_button);

      redo_button = resources.newJButton("redo_button", "redo_button_tooltip", listener);

      middle_toolbar.add(redo_button);

      JLabel separator_1 = new JLabel();
      separator_1.setMaximumSize(new java.awt.Dimension(10, 10));
      separator_1.setPreferredSize(new java.awt.Dimension(10, 10));
      middle_toolbar.add(separator_1);

      incompletes_button = resources.newJButton("incompletes_button", "incompletes_button_tooltip", listener);

      middle_toolbar.add(incompletes_button);

      violation_button = resources.newJButton("violations_button", "violations_button_tooltip", listener);

      middle_toolbar.add(violation_button);

      JLabel separator_3 = new JLabel();
      separator_3.setMaximumSize(new java.awt.Dimension(10, 10));
      separator_3.setPreferredSize(new java.awt.Dimension(10, 10));
      separator_3.setRequestFocusEnabled(false);
      middle_toolbar.add(separator_3);

      display_all_button = resources.newJButton("display_all_button", "display_all_button_tooltip", listener);

      middle_toolbar.add(display_all_button);

      display_region_button = resources.newJButton("display_region_button", "display_region_button_tooltip", listener);

      middle_toolbar.add(display_region_button);

      add(middle_toolbar, java.awt.BorderLayout.CENTER);
      }

   /**
    * Sets the selected button in the menu button button group
    */
   void hilight_selected_button()
      {
      interactive.state.StateInteractive interactive_state = board_frame.board_panel.board_handling.get_interactive_state();
      if (interactive_state instanceof interactive.state.StateMenuRoute)
         {
         route_button.setSelected(true);
         }
      else if (interactive_state instanceof interactive.state.StateMenuDrag)
         {
         drag_button.setSelected(true);
         }
      else if (interactive_state instanceof interactive.state.StateMenuSelect)
         {
         select_button.setSelected(true);
         }
      }
   
   private final class BoardActionListener implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent evt)
         {
         Object source = evt.getSource();
   
         if (source == select_button )
            board_frame.board_panel.board_handling.set_select_menu_state();
         else if ( source == route_button )
            board_frame.board_panel.board_handling.set_route_menu_state();
         else if ( source == drag_button )
            board_frame.board_panel.board_handling.set_drag_menu_state();
         else if ( source == autoroute_button )
            board_frame.board_panel.board_handling.start_batch_autorouter();
         else if ( source == undo_button )
            {
            board_frame.board_panel.board_handling.cancel_state();
            board_frame.board_panel.board_handling.undo();
            board_frame.refresh_windows();
            }
         else if ( source == redo_button )
            board_frame.board_panel.board_handling.redo();
         else if ( source == incompletes_button )
            board_frame.board_panel.board_handling.toggle_ratsnest();
         else if ( source == violation_button )
            board_frame.board_panel.board_handling.toggle_clearance_violations();
         else if ( source == display_all_button )
            board_frame.board_panel.zoom_all();
         else if ( source == display_region_button )
            board_frame.board_panel.board_handling.zoom_region();
         }
      }
   }
