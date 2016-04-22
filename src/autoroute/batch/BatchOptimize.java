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
 */
package autoroute.batch;

import freert.planar.PlaPointFloat;
import gui.varie.UndoableObjectStorable;
import interactive.BrdActionThread;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import rules.BoardRules;
import board.RoutingBoard;
import board.items.BrdItem;
import board.items.BrdTrace;
import board.varie.BrdStopConnection;
import board.varie.ItemFixState;
import datastructures.UndoableObjectNode;

/**
 * To optimize the vias and traces after the batch autorouter has completed the board.
 * 
 * @author Alfons Wirtz
 */
public class BatchOptimize
   {
   private final BrdActionThread batch_thread;
   private final RoutingBoard r_board;
   private final BatchSortedRouteItems sorted_route_items;

   private boolean use_increased_ripup_costs; // in the first passes the ripup costs are icreased for better performance.
   private double min_cumulative_trace_length_before = 0;
   private static int MAX_AUTOROUTE_PASSES = 6;
   private static int ADDITIONAL_RIPUP_COST_FACTOR_AT_START = 10;

   /**
    * To optimize the route on the board after the autoroute task is finished.
    */
   public BatchOptimize(BrdActionThread p_thread)
      {
      batch_thread = p_thread;
      r_board = p_thread.hdlg.get_routing_board();
      sorted_route_items = new BatchSortedRouteItems(r_board);
      }

   /**
    * Optimize the route on the board.
    */
   public void optimize_board()
      {
      r_board.userPrintln("Before optimize: Via count: " + r_board.get_vias().size() + ", trace length: " + Math.round(r_board.cumulative_trace_length()));
      
      boolean route_improved = true;
      int curr_pass_no = 0;
      use_increased_ripup_costs = true;

      while (route_improved)
         {
         if ( batch_thread.is_stop_requested()) break;

         curr_pass_no++;
         
         boolean with_prefered_directions = (curr_pass_no % 2 != 0); // to create more variations
         
         route_improved = optimize_route_pass(curr_pass_no, with_prefered_directions);
         }
      }

   /**
    * Pass to reduce the number of vias an to shorten the trace length a completely routed board. 
    * @return true, if the route was improved.
    */
   private boolean optimize_route_pass(int p_pass_no, boolean p_with_prefered_directions)
      {
      boolean route_improved = false;
      
      int via_count_before = r_board.get_vias().size();

      double trace_length_before = batch_thread.hdlg.coordinate_transform.board_to_user(r_board.cumulative_trace_length());
      
      batch_thread.hdlg.screen_messages.set_post_route_info(via_count_before, trace_length_before);

      min_cumulative_trace_length_before = calc_weighted_trace_length(r_board);

      while ( ! batch_thread.is_stop_requested() )
         {
         BrdItem curr_item = sorted_route_items.next();

         if (curr_item == null) break;

         if (optimize_item_route(curr_item, p_pass_no, p_with_prefered_directions))
            {
            route_improved = true;
            }
         }
      
      sorted_route_items.clear();
      
      if ( use_increased_ripup_costs && !route_improved)
         {
         use_increased_ripup_costs = false;
         route_improved = true; // to keep the optimizer going with lower ripup costs
         }
      
      return route_improved;
      }

   /**
    * Autoroute ripup passes until the board is completed or the autoroute is stopped by the user, or if p_max_pass_count is
    * exceeded. Is currently used in the optimize via batch pass. Returns the number of passes to complete the board or
    * p_max_pass_count + 1, if the board is not completed.
    */
   private int optimize_item_autoroute(BrdActionThread p_thread, int p_max_pass_count, int p_ripup_costs, boolean p_with_prefered_directions)
      {
      BatchAutorouter router_instance = new BatchAutorouter(p_thread, p_with_prefered_directions, p_ripup_costs);

      int curr_pass_no = 1;

      while ( ! p_thread.is_stop_requested() && curr_pass_no <= p_max_pass_count)
         {
         int unrouted_count = router_instance.autoroute_pass(curr_pass_no);
      
         if ( unrouted_count <= 0 ) break;
         
         p_thread.hdlg.itera_settings.autoroute_settings.pass_no_inc();

         ++curr_pass_no;
         }
      
      router_instance.remove_tails();
            
      return curr_pass_no;
      }
   
   
   
   
   
   /**
    * Tries to improve the route by rerouting the connections containing p_item.
    */
   private boolean optimize_item_route(BrdItem p_item, int p_pass_no, boolean p_with_prefered_directions)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("interactive.resources.InteractiveState", batch_thread.hdlg.get_locale());
      String start_message = resources.getString("batch_optimizer") + " " + resources.getString("stop_message") + "        " + resources.getString("pass") + " " + (new Integer(p_pass_no)).toString()
            + ": ";
      batch_thread.hdlg.screen_messages.set_status_message(start_message);
      batch_thread.hdlg.remove_ratsnest();
      
      int incomplete_count_before = batch_thread.hdlg.get_ratsnest().incomplete_count();
      int via_count_before = r_board.get_vias().size();
      
      Set<BrdItem> ripped_items = new TreeSet<BrdItem>();
      ripped_items.add(p_item);
      
      if (p_item instanceof BrdTrace)
         {
         // add also the fork items, especially because not all fork items may be
         // returned by ReadSortedRouteItems because of matching end points.
         BrdTrace curr_trace = (BrdTrace) p_item;
         Set<BrdItem> curr_contact_list = curr_trace.get_start_contacts();
         for (int i = 0; i < 2; ++i)
            {
            if (contains_only_unfixed_traces(curr_contact_list))
               {
               ripped_items.addAll(curr_contact_list);
               }
            curr_contact_list = curr_trace.get_end_contacts();
            }
         }
      
      Set<BrdItem> ripped_connections = new TreeSet<BrdItem>();
      for (BrdItem curr_item : ripped_items)
         {
         ripped_connections.addAll(curr_item.get_connection_items(BrdStopConnection.NONE));
         }
      
      for (BrdItem curr_item : ripped_connections)
         {
         if (curr_item.is_user_fixed())
            {
            return false;
            }
         }
      
      r_board.generate_snapshot();
      
      r_board.remove_items_unfixed(ripped_connections);
      
      for (int i = 0; i < p_item.net_count(); ++i)
         {
         r_board.combine_traces(p_item.get_net_no(i));
         }
      
      int ripup_costs = batch_thread.hdlg.itera_settings.autoroute_settings.get_start_ripup_costs();
      if (use_increased_ripup_costs)
         {
         ripup_costs *= ADDITIONAL_RIPUP_COST_FACTOR_AT_START;
         }
      if (p_item instanceof BrdTrace)
         {
         // taking less ripup costs seems to produce better results
         ripup_costs = (int) Math.round(0.6 * (double) ripup_costs);
         }
      
      optimize_item_autoroute(batch_thread, MAX_AUTOROUTE_PASSES, ripup_costs, p_with_prefered_directions);
      
      batch_thread.hdlg.remove_ratsnest();
      int incomplete_count_after = batch_thread.hdlg.get_ratsnest().incomplete_count();
      int via_count_after = r_board.get_vias().size();
      double trace_length_after = calc_weighted_trace_length(r_board);
      
      boolean route_improved = !batch_thread.is_stop_requested()
            && (incomplete_count_after < incomplete_count_before || incomplete_count_after == incomplete_count_before
                  && (via_count_after < via_count_before || via_count_after == via_count_before && min_cumulative_trace_length_before > trace_length_after));
      
      if (route_improved)
         {
         if (incomplete_count_after < incomplete_count_before || incomplete_count_after == incomplete_count_before && via_count_after < via_count_before)
            {
            min_cumulative_trace_length_before = trace_length_after;
            }
         else
            {
            // Only cumulative trace length shortened.
            // Catch unexpected increase of cumulative trace length somewhere for examole by removing acid trapsw.
            min_cumulative_trace_length_before = Math.min(min_cumulative_trace_length_before, trace_length_after);
            }
         r_board.pop_snapshot();
         double new_trace_length = batch_thread.hdlg.coordinate_transform.board_to_user(r_board.cumulative_trace_length());
         batch_thread.hdlg.screen_messages.set_post_route_info(via_count_after, new_trace_length);
         }
      else
         {
         r_board.undo(null);
         }
      
      return route_improved;
      }

   static boolean contains_only_unfixed_traces(Collection<BrdItem> p_item_list)
      {
      for (BrdItem curr_item : p_item_list)
         {
         if (curr_item.is_user_fixed() || !(curr_item instanceof BrdTrace))
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Calculates the cumulative trace lengths multiplied by the trace radius of all traces on the board, which are not shove_fixed.
    */
   private double calc_weighted_trace_length(RoutingBoard p_board)
      {
      double result = 0;
      
      Iterator<UndoableObjectNode> iter = p_board.item_list.start_read_object();
      
      for (;;)
         {
         UndoableObjectStorable curr_item = p_board.item_list.read_object(iter);
         
         if (curr_item == null)  break;

         if ( ! ( curr_item instanceof BrdTrace) ) continue;
         
         BrdTrace curr_trace = (BrdTrace) curr_item;
         
         ItemFixState fixed_state = curr_trace.get_fixed_state();

         if ( ! (fixed_state == ItemFixState.UNFIXED || fixed_state == ItemFixState.SHOVE_FIXED) ) continue;

         int clearance_value = p_board.get_clearance(curr_trace.clearance_class_no(), BoardRules.default_clearance_class, curr_trace.get_layer());
         
         int half_width = curr_trace.get_half_width();

         double trace_len = curr_trace.get_length();
         
         double weighted_trace_length = trace_len * (half_width + clearance_value);
         
         if (fixed_state == ItemFixState.SHOVE_FIXED)
            {
            // to produce less violations with pin exit directions.
            weighted_trace_length /= 2;
            }
         
         result += weighted_trace_length;
         }

      return result;
      }

   /**
    * @return the current position of the item, which will be rerouted or null, if the optimizer is not active.
    */
   public PlaPointFloat get_current_position()
      {
      if (sorted_route_items == null) return null;

      return sorted_route_items.get_current_position();
      }


   }
