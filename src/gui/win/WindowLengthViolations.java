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
 * WindowLengthViolations.java
 *
 * Created on 1. Juni 2005, 06:52
 *
 */

package gui.win;

import board.infos.BrdLengthViolation;
import freert.rules.RuleNets;
import gui.BoardFrame;
import gui.varie.GuiResources;
import interactive.RatsNest;
import main.Stat;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowLengthViolations extends WindowObjectListWithFilter
   {
   private static final long serialVersionUID = 1L;

   private final GuiResources resources;
   
   public WindowLengthViolations(Stat stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);

      resources = new GuiResources(stat,"gui.resources.WindowLengthViolations");
      
      setTitle(resources.getString("title"));
      list_empty_message.setText(resources.getString("list_empty"));
      p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LengthViolations");
      }

   protected void fill_list()
      {
      RatsNest ratsnest = this.board_frame.board_panel.board_handling.get_ratsnest();
      RuleNets net_list = this.board_frame.board_panel.board_handling.get_routing_board().brd_rules.nets;
      java.util.SortedSet<BrdLengthViolation> length_violations = new java.util.TreeSet<BrdLengthViolation>();
      for (int net_index = 1; net_index <= net_list.max_net_no(); ++net_index)
         {
         double curr_violation_length = ratsnest.get_length_violation(net_index);
         if (curr_violation_length != 0)
            {
            BrdLengthViolation curr_length_violation = new BrdLengthViolation(resources,board_frame,net_list.get(net_index), curr_violation_length);
            length_violations.add(curr_length_violation);
            }
         }

      for (BrdLengthViolation curr_violation : length_violations)
         {
         this.add_to_list(curr_violation);
         }
      this.gui_list.setVisibleRowCount(Math.min(length_violations.size(), DEFAULT_TABLE_SIZE));
      }

   protected void select_instances()
      {
      @SuppressWarnings("deprecation")
      Object[] selected_violations = gui_list.getSelectedValues();
      if (selected_violations.length <= 0)
         {
         return;
         }
      java.util.Set<board.items.BrdItem> selected_items = new java.util.TreeSet<board.items.BrdItem>();
      for (int i = 0; i < selected_violations.length; ++i)
         {
         BrdLengthViolation curr_violation = ((BrdLengthViolation) selected_violations[i]);
         selected_items.addAll(curr_violation.net.get_items());
         }
      interactive.IteraBoard board_handling = board_frame.board_panel.board_handling;
      board_handling.select_items(selected_items);
      board_handling.zoom_selection();
      }

   }
