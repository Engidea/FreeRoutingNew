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
 * ViolationsWindow.java
 *
 * Created on 22. Maerz 2005, 05:40
 */

package gui.win;

import freert.main.Ldbg;
import freert.main.Mdbg;
import gui.BoardFrame;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import interactive.IteraClearanceViolations;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import board.infos.BrdItemViolation;
import board.infos.BrdViolation;

/**
 *
 * @author Alfons Wirtz
 */
public final class WindowClearanceViolations extends WindowObjectListWithFilter
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowClearanceViolations.";

   private final GuiResources resources;
   
   public WindowClearanceViolations(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      resources = board_frame.newGuiResources("gui.resources.WindowClearanceViolations");
      
      setTitle(resources.getString("title"));
      list_empty_message.setText(resources.getString("list_empty_message"));
      board_frame.set_context_sensitive_help(this, "WindowObjectList_ClearanceViolations");
      }

   @Override
   protected void fill_list()
      {
      if ( debug (Mdbg.CLRVIOL, Ldbg.TRACE )) userPrintln(classname+"toggle_clearance_violations: start");
      
      IteraBoard board_handling = board_frame.board_panel.itera_board;

      IteraClearanceViolations clearance_violations = new IteraClearanceViolations(board_handling.get_routing_board().get_items());

      SortedSet<BrdViolation> sorted_set = new TreeSet<BrdViolation>();

      for (BrdItemViolation curr_violation : clearance_violations.violation_list)
         {
         sorted_set.add(new BrdViolation(board_frame, resources, curr_violation));
         }
      
      for (BrdViolation curr_violation : sorted_set)
         {
         add_to_list(curr_violation);
         }
      
      gui_list.setVisibleRowCount(Math.min(sorted_set.size(), DEFAULT_TABLE_SIZE));
      }

   protected void select_instances()
      {
      @SuppressWarnings("deprecation")
      Object[] selected_violations = gui_list.getSelectedValues();
      if (selected_violations.length <= 0)
         {
         return;
         }
      
      Set<board.items.BrdItem> selected_items = new TreeSet<board.items.BrdItem>();
      
      for (int i = 0; i < selected_violations.length; ++i)
         {
         BrdItemViolation curr_violation = ((BrdViolation) selected_violations[i]).violation;
         selected_items.add(curr_violation.first_item);
         selected_items.add(curr_violation.second_item);
         }

      interactive.IteraBoard board_handling = board_frame.board_panel.itera_board;
      board_handling.select_items(selected_items);
      board_handling.toggle_selected_item_violations();
      board_handling.zoom_selection();
      }
   }