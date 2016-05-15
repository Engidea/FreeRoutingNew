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
 * SelectedItemState.java
 *
 * Created on 10. November 2003, 08:02
 */
package interactive.state;

import freert.library.LibPackage;
import freert.library.LibPackagePin;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.rules.RuleNet;
import freert.varie.NetNosList;
import freert.varie.ThreadStoppable;
import freert.varie.TimeLimitStoppable;
import gui.win.WindowObjectInfo;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.IteraClearanceViolations;
import interactive.LogfileScope;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JPopupMenu;
import main.Ldbg;
import main.Mdbg;
import autoroute.varie.ArtResult;
import board.BrdConnectable;
import board.algo.AlgoOptimizeVia;
import board.infos.BrdComponent;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdArea;
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.varie.ItemFixState;

/**
 * Class implementing actions on the currently selected items.
 *
 * @author Alfons Wirtz
 */
public final class StateSelectedItem extends StateInteractive
   {
   private static final String classname = "SelectedItemState.";

   private Set<BrdItem> items_list;
   
   private IteraClearanceViolations clearance_violations = null;
   
   /**
    * Creates a new SelectedItemState with with the items in p_item_list selected. 
    * @return null, if p_item_list is empty.
    */
   public static StateSelectedItem get_instance(Set<BrdItem> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      if (p_item_list.isEmpty()) return null;

      StateSelectedItem new_state = new StateSelectedItem(p_item_list, p_parent_state, p_board_handling, p_logfile);

      return new_state;
      }

   private StateSelectedItem(Set<BrdItem> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      
      items_list = p_item_list;
      }

   /**
    * Gets the list of the currently selected items.
    */
   public Collection<BrdItem> get_item_list()
      {
      return items_list;
      }

   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return toggle_select(p_location);
      }

   public StateInteractive mouse_dragged(PlaPointFloat p_point)
      {
      return StateSelectRegionItems.get_instance(i_brd.get_current_mouse_position(), this, i_brd, actlog);
      }

   /**
    * Action to be taken when a key is pressed (Shortcut).
    */
   public StateInteractive key_typed(char p_key_char)
      {
      StateInteractive result = this;

      if (p_key_char == 'a')
         {
         i_brd.autoroute_selected_items();
         }
      else if (p_key_char == 'b')
         {
         extent_to_whole_components();
         }
      else if (p_key_char == 'd')
         {
         result = cutout_items();
         }
      else if (p_key_char == 'e')
         {
         result = extent_to_whole_connections();
         }
      else if (p_key_char == 'f')
         {
         fix_items();
         }
      else if (p_key_char == 'i')
         {
         result = info_display();
         }
      else if (p_key_char == 'm')
         {
         result = StateMoveItem.get_instance(i_brd.get_current_mouse_position(), items_list, return_state, i_brd, actlog);
         }
      else if (p_key_char == 'n')
         {
         extent_to_whole_nets();
         }
      else if (p_key_char == 'p')
         {
         i_brd.optimize_selected_items();
         }
      else if (p_key_char == 'r')
         {
         result = new StateSelectRegionZoom( this, i_brd, actlog, i_brd.get_current_mouse_position());
         }
      else if (p_key_char == 's')
         {
         result = extent_to_whole_connected_sets();
         }
      else if (p_key_char == 'u')
         {
         unfix_items();
         }
      else if (p_key_char == 'v')
         {
         toggle_clearance_violations();
         }
      else if (p_key_char == 'V')
         {
         repair_clearance_violations();
         }
      else if (p_key_char == 'w')
         {
         i_brd.zoom_selection();
         }
      else if (p_key_char == KeyEvent.VK_DELETE)
         {
         result = delete_items();
         }
      else
         {
         result = super.key_typed(p_key_char);
         }
      return result;
      }

   /**
    * fixes all items in this selected set
    */
   public void fix_items()
      {
      for ( BrdItem curr_ob : items_list )
         {
         if (curr_ob.get_fixed_state().ordinal() < ItemFixState.USER_FIXED.ordinal())
            {
            curr_ob.set_fixed_state(ItemFixState.USER_FIXED);
            }
         }
      
      actlog_start_scope(LogfileScope.FIX_SELECTED_ITEMS);
      }

   /**
    * unfixes all items in this selected set
    */
   public void unfix_items()
      {
      for ( BrdItem curr_ob : items_list ) curr_ob.unfix();
      
      actlog_start_scope(LogfileScope.UNFIX_SELECTED_ITEMS);
      }


   /**
    * Repair clearance violations onthe selected items
    */
   public void repair_clearance_violations()
      {
      if ( debug(Mdbg.CLRVIOL, Ldbg.TRACE)) i_brd.userPrintln(classname+"repair_clearance_violations: start");
      
      StateRepairClearanceViolation repair = new StateRepairClearanceViolation(this, i_brd, actlog);

      repair.repair(items_list);
      }

   /**
    * Makes all items in this selected_set connectable and assigns them to a new net.
    */
   public StateInteractive assign_items_to_new_net()
      {
      // make the situation restorable by undo
      r_brd.generate_snapshot();
      
      boolean items_already_connected = false;
      
      RuleNet new_net = r_brd.brd_rules.nets.new_net();

      for ( BrdItem curr_item : items_list)
         {
         if (curr_item instanceof BrdArea)
            {
            r_brd.make_conductive((BrdArea) curr_item, new_net.net_number);
            }
         else if (curr_item instanceof BrdAbit)
            {
            if (curr_item.is_connected())
               {
               items_already_connected = true;
               }
            else
               {
               curr_item.set_net_no(new_net.net_number);
               }
            }
         }
      if (items_already_connected)
         {
         i_brd.screen_messages.set_status_message(resources.getString("some_items_are_not_changed_because_they_are_already_connected"));
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("new_net_created_from_selected_items"));
         }

      actlog_start_scope(LogfileScope.ASSIGN_SELECTED_TO_NEW_NET);

      i_brd.update_ratsnest();
      i_brd.repaint();
      return return_state;
      }

   /**
    * Assigns all items in this selected_set to a new group( new component for example)
    */
   public StateInteractive assign_items_to_new_group()
      {
      r_brd.generate_snapshot();
      // Take the gravity point of all item centers for the location of the new component.
      double gravity_x = 0;
      double gravity_y = 0;
      int pin_count = 0;
      
      Iterator<BrdItem> it = items_list.iterator();
      
      while (it.hasNext())
         {
         BrdItem curr_ob = it.next();
         if (curr_ob instanceof BrdAbitVia)
            {
            PlaPointFloat curr_center = ((BrdAbit) curr_ob).center_get().to_float();
            gravity_x += curr_center.v_x;
            gravity_y += curr_center.v_y;
            ++pin_count;
            }
         else
            {
            // currently only Vias can be aasigned to a new component
            it.remove();
            }
         }
      
      if (pin_count == 0) return return_state;
      
      gravity_x /= pin_count;
      gravity_y /= pin_count;
      PlaPointInt gravity_point = new PlaPointInt(Math.round(gravity_x), Math.round(gravity_y));
      // create a new package
      LibPackagePin[] pin_arr = new LibPackagePin[items_list.size()];
      it = items_list.iterator();
      
      for (int index = 0; index < pin_arr.length; ++index)
         {
         BrdAbitVia curr_via = (BrdAbitVia) it.next();
         
         PlaVectorInt rel_coor = curr_via.center_get().difference_by(gravity_point);
         
         String pin_name = Integer.toString(index + 1);
         
         pin_arr[index] = new LibPackagePin(pin_name, curr_via.get_padstack().pads_no, rel_coor, 0);
         }
      
      LibPackage new_package = r_brd.library.packages.add(pin_arr);
      BrdComponent new_component = r_brd.brd_components.add(gravity_point, 0, true, new_package);
      
      it = items_list.iterator();
      
      for (int index = 0; index < pin_arr.length; ++index)
         {
         BrdAbitVia curr_via = (BrdAbitVia) it.next();
         
         r_brd.remove_item(curr_via);
         
         NetNosList net_no_arr = curr_via.net_nos.copy();
         
/*         
         int[] net_no_arr = new int[curr_via.net_count()];
         for (int jndex = 0; jndex < net_no_arr.length; ++jndex)
            {
            net_no_arr[jndex] = curr_via.get_net_no(jndex);
            }
*/         
         r_brd.insert_pin(new_component.id_no, index, net_no_arr, curr_via.clearance_class_no(), curr_via.get_fixed_state());
         }

      actlog_start_scope(LogfileScope.ASSIGN_SELECTED_TO_NEW_GROUP);

      i_brd.repaint();
      return return_state;
      }

   /**
    * Deletes all unfixed items in this selected set and pulls tight the neighbor traces.
    */
   public StateInteractive delete_items()
      {
      r_brd.generate_snapshot();

      // calculate the changed nets for updating the ratsnest
      Set<Integer> changed_nets = new TreeSet<Integer>();
      Iterator<BrdItem> it = items_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item instanceof BrdConnectable)
            {
            for (int i = 0; i < curr_item.net_count(); ++i)
               {
               changed_nets.add(curr_item.get_net_no(i));
               }
            }
         }
      
      
      // you are llow to delete fixed items when NOT in release mode
      boolean with_delete_fixed = ! i_brd.debug(Mdbg.GUI, Ldbg.RELEASE);
      
      boolean all_items_removed;
      
      if (i_brd.itera_settings.push_enabled)
         {
         all_items_removed = r_brd.remove_items_and_pull_tight(items_list, i_brd.itera_settings.trace_pull_tight_region_width, i_brd.itera_settings.trace_pull_tight_accuracy, with_delete_fixed);
         }
      else
         {
         all_items_removed = r_brd.remove_items(items_list, with_delete_fixed);
         }
      
      if (!all_items_removed)
         {
         i_brd.screen_messages.set_status_message(resources.getString("some_items_are_fixed_and_could_therefore_not_be_removed"));
         }

      actlog_start_scope(LogfileScope.DELETE_SELECTED);

      for (Integer curr_net_no : changed_nets)
         {
         i_brd.update_ratsnest(curr_net_no.intValue());
         }
      i_brd.repaint();
      
      return return_state;
      }

   /**
    * Deletes all unfixed items in this selected set inside a rectangle
    */
   public StateInteractive cutout_items()
      {
      return StateSelecRegionCutout.get_instance(items_list, return_state, i_brd, actlog);
      }
   
   /**
    * Autoroute the selected items. 
    * If p_stoppable_thread != null, the algorithm can be requested to terminate.
    * This is some kind of duplicate of the Batch routing, it is not easy to merge them...
    * 
    */
   public StateInteractive autoroute(ThreadStoppable p_thread)
      {
      boolean saved_board_read_only = i_brd.set_board_read_only(true);
      
      String start_message = resources.getString("autoroute") + " " + resources.getString("stop_message");
      i_brd.screen_messages.set_status_message(start_message);
      
      Integer not_found_count = 0;
      Integer found_count = 0;

      Collection<BrdItem> autoroute_item_list = new LinkedList<BrdItem>();

      for (BrdItem curr_item : items_list)
         {
         if ( ! (curr_item instanceof BrdConnectable)) continue;
         
         for (int index = 0; index < curr_item.net_count(); ++index)
            {
            if ( curr_item.get_unconnected_set(curr_item.get_net_no(index)).isEmpty()) continue;

            autoroute_item_list.add(curr_item);
            }
         }

      int items_to_go_count = autoroute_item_list.size();
      
      i_brd.screen_messages.set_interactive_autoroute_info(found_count, not_found_count, items_to_go_count);
      
      // Empty this.item_list to avoid displaying the selected items.
      items_list.clear();

      boolean ratsnest_hidden_before = i_brd.get_ratsnest().hide();

      for (BrdItem curr_item : autoroute_item_list)
         {
         if ( is_stop_requested(p_thread))  break;

         // can only route with a net count of one
         if (curr_item.net_count() != 1) continue;
         
         boolean contains_plane = false;
         
         RuleNet route_net = r_brd.brd_rules.nets.get(curr_item.get_net_no(0));
         
         if (route_net != null)
            {
            contains_plane = route_net.contains_plane();
            i_brd.userPrintln(classname+"autoroute: net "+route_net.name);
            }
         
         int via_costs;
         if (contains_plane)
            via_costs = i_brd.itera_settings.autoroute_settings.get_plane_via_costs();
         else
            via_costs = i_brd.itera_settings.autoroute_settings.get_via_costs();
         
         r_brd.start_marking_changed_area();
         
         ArtResult autoroute_result;
         
         try
            {
            autoroute_result = r_brd.autoroute(curr_item, i_brd.itera_settings, via_costs, p_thread );   
            }
         catch ( Exception exc )
            {
            autoroute_result = ArtResult.EXCEPTION;
            i_brd.userPrintln(classname+"autoroute", exc);
            }
         
         if (autoroute_result == ArtResult.ROUTED)
            {
            ++found_count;
            i_brd.repaint();
            }
         else if (autoroute_result != ArtResult.ALREADY_CONNECTED)
            {
            ++not_found_count;
            }
         --items_to_go_count;
         i_brd.screen_messages.set_interactive_autoroute_info(found_count, not_found_count, items_to_go_count);
         }

      i_brd.screen_messages.clear();
      String curr_message;
      if (is_stop_requested(p_thread))
         {
         curr_message = resources.getString("interrupted");
         }
      else
         {
         curr_message = resources.getString("completed");
         }
      String end_message = resources.getString("autoroute") + " " + curr_message + ": " + found_count.toString() + " " + resources.getString("connections_found") + ", "
            + not_found_count.toString() + " " + resources.getString("connections_not_found");
      i_brd.screen_messages.set_status_message(end_message);

      i_brd.set_board_read_only(saved_board_read_only);

      actlog_start_scope(LogfileScope.AUTOROUTE_SELECTED);
      
      i_brd.update_ratsnest();
      
      if (!ratsnest_hidden_before)
         {
         i_brd.get_ratsnest().show();
         }
      
      return return_state;
      }

   /**
    * Fanouts the pins contained in the selected items. If p_stoppable_thread != null, the algorithm can be requestet to terminate.
    */
   public StateInteractive fanout(ThreadStoppable p_stoppable_thread)
      {
      boolean saved_board_read_only = i_brd.is_board_read_only();
      i_brd.set_board_read_only(true);
      if (p_stoppable_thread != null)
         {
         String start_message = resources.getString("fanout") + " " + resources.getString("stop_message");
         i_brd.screen_messages.set_status_message(start_message);
         }
      Integer not_found_count = 0;
      Integer found_count = 0;

      boolean interrupted = false;
      Collection<BrdAbitPin> fanout_list = new java.util.LinkedList<BrdAbitPin>();
      for (BrdItem curr_item : items_list)
         {
         if (curr_item instanceof BrdAbitPin)
            {
            fanout_list.add((BrdAbitPin) curr_item);
            }
         }
      int items_to_go_count = fanout_list.size();
      i_brd.screen_messages.set_interactive_autoroute_info(found_count, not_found_count, items_to_go_count);

      // Empty this.item_list to avoid displaying the seected items.
      items_list.clear();

      boolean ratsnest_hidden_before = i_brd.get_ratsnest().hide();

      for (BrdAbitPin curr_pin : fanout_list)
         {
         if (p_stoppable_thread != null && p_stoppable_thread.is_stop_requested())
            {
            interrupted = true;
            break;
            }
         r_brd.start_marking_changed_area();
         ArtResult autoroute_result = r_brd.fanout(curr_pin, i_brd.itera_settings, -1, p_stoppable_thread );
         if (autoroute_result == ArtResult.ROUTED)
            {
            ++found_count;
            i_brd.repaint();
            }
         else if (autoroute_result != ArtResult.ALREADY_CONNECTED)
            {
            ++not_found_count;
            }

         --items_to_go_count;
         i_brd.screen_messages.set_interactive_autoroute_info(found_count, not_found_count, items_to_go_count);
         }
      if (p_stoppable_thread != null)
         {
         i_brd.screen_messages.clear();
         String curr_message;
         if (interrupted)
            {
            curr_message = resources.getString("interrupted");
            }
         else
            {
            curr_message = resources.getString("completed");
            }
         String end_message = resources.getString("fanout") + " " + curr_message + ": " + found_count.toString() + " " + resources.getString("connections_found") + ", " + not_found_count.toString()
               + " " + resources.getString("connections_not_found");
         i_brd.screen_messages.set_status_message(end_message);
         }
      i_brd.set_board_read_only(saved_board_read_only);

      actlog_start_scope(LogfileScope.FANOUT_SELECTED);

      i_brd.update_ratsnest();

      if (!ratsnest_hidden_before)
         {
         i_brd.get_ratsnest().show();
         }
      
      return return_state;
      }

   private boolean is_stop_requested ( ThreadStoppable p_thread )
      {
      if ( p_thread== null) return false;
      
      return p_thread.is_stop_requested();
      }

   
   /**
    * Optimizes the selected items. If p_thread != null, the algorithm can be requested to terminate.
    * Always show messages, even if thread is null
    */
   public StateInteractive pull_tight(ThreadStoppable p_thread)
      {
      boolean saved_board_read_only = i_brd.set_board_read_only(true);

      String start_message = resources.getString("pull_tight") + " " + resources.getString("stop_message");
      i_brd.screen_messages.set_status_message(start_message);
      
      r_brd.start_marking_changed_area();
      
      AlgoOptimizeVia optimize_via = new AlgoOptimizeVia(r_brd);

      for (BrdItem curr_item : items_list)
         {
         if (is_stop_requested(p_thread)) break;
         
         if (curr_item.net_count() != 1)continue;
         
         i_brd.userPrintln("pull_tight: item "+curr_item);
         
         if (curr_item instanceof BrdTracePolyline)
            {
            BrdTracePolyline curr_trace = (BrdTracePolyline) curr_item;
            boolean something_changed = curr_trace.pull_tight(!i_brd.itera_settings.push_enabled, i_brd.itera_settings.trace_pull_tight_accuracy, p_thread);
            if (!something_changed)
               {
               curr_trace.smoothen_end_corners_fork(!i_brd.itera_settings.push_enabled, i_brd.itera_settings.trace_pull_tight_accuracy, p_thread);
               }
            }
         else if (curr_item instanceof BrdAbitVia)
            {
            optimize_via.optimize_via_location((BrdAbitVia) curr_item, null, i_brd.itera_settings.trace_pull_tight_accuracy, 10);
            }
         }
      
      String curr_message;

      if (! is_stop_requested(p_thread) && i_brd.itera_settings.push_enabled )
         {
         i_brd.userPrintln("pull_tight: optimize_changed_area ");

         TimeLimitStoppable t_limit = new TimeLimitStoppable(10, p_thread);

         r_brd.optimize_changed_area(NetNosList.EMPTY, null, i_brd.itera_settings.trace_pull_tight_accuracy, null, t_limit, null);
         }

      if (is_stop_requested(p_thread))
         {
         curr_message = resources.getString("interrupted");
         }
      else
         {
         curr_message = resources.getString("completed");
         }
      String end_message = resources.getString("pull_tight") + " " + curr_message;
      i_brd.screen_messages.set_status_message(end_message);
      
      i_brd.set_board_read_only(saved_board_read_only);
      
      actlog_start_scope(LogfileScope.OPTIMIZE_SELECTED);
      
      i_brd.update_ratsnest();
      
      return return_state;
      }

   /**
    * Assigns the input clearance class to the selected items.
    */
   public StateInteractive assign_clearance_class(int p_cl_class_index)
      {
      if (p_cl_class_index < 0 || p_cl_class_index >= r_brd.brd_rules.clearance_matrix.get_class_count())
         {
         return return_state;
         }
      
      actlog_start_scope(LogfileScope.ASSIGN_CLEARANCE_CLASS,p_cl_class_index);

      // make the situation restorable by undo
      r_brd.generate_snapshot();
      for (BrdItem curr_item : items_list)
         {
         if (curr_item.clearance_class_no() == p_cl_class_index)
            {
            continue;
            }
         curr_item.change_clearance_class(p_cl_class_index);
         }
      return return_state;
      }

   /**
    * Select also all items belonging to any net of the current selected items.
    */
   public StateInteractive extent_to_whole_nets()
      {
      // collect all net numbers of the selected items
      Set<Integer> curr_net_no_set = new TreeSet<Integer>();
      
      for (BrdItem curr_item : items_list )
         {
         if ( ! (curr_item instanceof BrdConnectable)) continue;
         
         for (int index = 0; index < curr_item.net_count(); ++index)
            {
            curr_net_no_set.add(curr_item.get_net_no(index));
            }
         }

      Set<BrdItem> new_selected_items = new TreeSet<BrdItem>();
      
      for ( Integer an_int : curr_net_no_set )
         {
         int curr_net_no = an_int.intValue();
         
         new_selected_items.addAll(r_brd.get_connectable_items(curr_net_no));
         }
      
      items_list = new_selected_items;

      if (new_selected_items.isEmpty())
         {
         return return_state;
         }
      
      actlog_start_scope(LogfileScope.EXTEND_TO_WHOLE_NETS);

      filter();
      i_brd.repaint();
      return this;
      }

   /**
    * Select also all items belonging to any group of the current selected items.
    */
   public StateInteractive extent_to_whole_components()
      {

      // collect all group numbers of the selected items
      Set<Integer> curr_group_no_set = new TreeSet<Integer>();
      Iterator<BrdItem> it = items_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item.get_component_no() > 0)
            {
            curr_group_no_set.add(curr_item.get_component_no());
            }
         }
      Set<BrdItem> new_selected_items = new TreeSet<BrdItem>();
      new_selected_items.addAll(items_list);
      Iterator<Integer> it2 = curr_group_no_set.iterator();
      while (it2.hasNext())
         {
         int curr_group_no = it2.next();
         new_selected_items.addAll(r_brd.get_component_items(curr_group_no));
         }
      if (new_selected_items.isEmpty())
         {
         return return_state;
         }
      items_list = new_selected_items;

      actlog_start_scope(LogfileScope.EXTEND_TO_WHOLE_COMPONENTS);

      i_brd.repaint();
      return this;
      }

   /**
    * Select also all items belonging to any connected set of the current selected items.
    */
   public StateInteractive extent_to_whole_connected_sets()
      {
      Set<BrdItem> new_selected_items = new TreeSet<BrdItem>();
      
      for (BrdItem curr_item : items_list )
         {
         if (curr_item instanceof BrdConnectable)
            {
            new_selected_items.addAll(curr_item.get_connected_set(-1));
            }
         }
      
      if (new_selected_items.isEmpty()) return return_state;

      items_list = new_selected_items;

      actlog_start_scope(LogfileScope.EXTEND_TO_WHOLE_CONNECTED_SETS);

      filter();
      i_brd.repaint();
      return this;
      }

   /**
    * Select also all items belonging to any connection of the current selected items.
    */
   public StateInteractive extent_to_whole_connections()
      {
      Set<BrdItem> new_selected_items = new TreeSet<BrdItem>();
      Iterator<BrdItem> it = items_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item instanceof BrdConnectable)
            {
            new_selected_items.addAll(curr_item.get_connection_items());
            }
         }
      if (new_selected_items.isEmpty())
         {
         return return_state;
         }
      
      items_list = new_selected_items;

      actlog_start_scope(LogfileScope.EXTEND_TO_WHOLE_CONNECTIONS);

      filter();
      i_brd.repaint();
      return this;
      }

   /**
    * Picks item at p_point. 
    * Removes it from the selected_items list, if it is already in there, otherwise adds it to the list.
    * Returns true (to change to the return_state) if nothing was picked.
    */
   public StateInteractive toggle_select(PlaPointFloat p_point)
      {
      Collection<BrdItem> picked_items = i_brd.pick_items(p_point);
      
      boolean state_ended = picked_items.isEmpty();
      
      if (picked_items.size() == 1)
         {
         BrdItem picked_item = picked_items.iterator().next();
         if ( items_list.contains(picked_item))
            {
            items_list.remove(picked_item);
            
            if ( items_list.isEmpty())
               {
               state_ended = true;
               }
            }
         else
            {
            items_list.add(picked_item);
            }
         }
      
      i_brd.repaint();
      StateInteractive result;
      
      if (state_ended)
         {
         result = return_state;
         }
      else
         {
         result = this;
         }
      
      actlog_start_scope(LogfileScope.TOGGLE_SELECT, p_point);

      return result;
      }

   /**
    * Shows or hides the clearance violations of the selected items..
    */
   public void toggle_clearance_violations()
      {
      if (clearance_violations == null)
         {
         clearance_violations = new IteraClearanceViolations(items_list);
         int violation_count = clearance_violations.violation_list.size();
         String curr_message = violation_count  + " " + resources.getString("clearance_violations_found");
         i_brd.screen_messages.set_status_message(curr_message);
         }
      else
         {
         clearance_violations = null;
         i_brd.screen_messages.set_status_message("");
         }
      i_brd.repaint();
      }

   /**
    * Removes items not selected by the current interactive filter from the selected item list.
    */
   public StateInteractive filter()
      {
      StateInteractive result = this;

      items_list = i_brd.itera_settings.item_selection_filter.filter(items_list);
      
      if (items_list.isEmpty()) result = return_state;
      
      i_brd.repaint();

      return result;
      }

   /**
    * Prints information about the selected item into a graphical text window.
    */
   public StateSelectedItem info_display()
      {
      WindowObjectInfo.display(items_list, i_brd.get_panel().board_frame, i_brd.coordinate_transform, new Point(100, 100));
      return this;
      }

   @Override
   public String get_help_id()
      {
      return "SelectedItemState";
      }

   @Override
   public void draw(java.awt.Graphics p_graphics)
      {
      if (items_list == null)  return;
     
      for ( BrdItem curr_item : items_list)
         {
         curr_item.draw(p_graphics, i_brd.gdi_context, i_brd.gdi_context.get_hilight_color(), i_brd.gdi_context.get_hilight_color_intensity());
         }
      
      if (clearance_violations != null)
         {
         clearance_violations.draw(p_graphics, i_brd.gdi_context);
         }
      }

   @Override
   public JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_select;
      }

   @Override
   public void set_toolbar()
      {
      i_brd.get_panel().board_frame.set_select_toolbar();
      }

   @Override
   public void display_default_message()
      {
      i_brd.screen_messages.set_status_message(resources.getString("in_select_item_mode"));
      }
   }
