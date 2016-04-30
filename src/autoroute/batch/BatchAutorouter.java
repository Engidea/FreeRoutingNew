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
import freert.planar.PlaSegmentFloat;
import freert.rules.RuleNet;
import freert.varie.TimeLimitStoppable;
import freert.varie.UndoableObjectNode;
import gui.varie.GuiResources;
import gui.varie.UndoableObjectStorable;
import interactive.IteraBoard;
import interactive.BrdActionThread;
import interactive.IteraSettings;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import main.Ldbg;
import main.Mdbg;
import autoroute.ArtControl;
import autoroute.ArtEngine;
import autoroute.expand.ExpandCostFactor;
import autoroute.varie.ArtResult;
import board.BrdConnectable;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.varie.BrdStopConnection;

/**
 * Handles the sequencing of the batch autoroute passes.
 * 
 * @author Alfons Wirtz
 */
public class BatchAutorouter
   {
   private static final String classname = "BatchAutorouter.";
   
   private final BrdActionThread s_thread;
   private final IteraBoard hdlg;
   private final RoutingBoard routing_board;
   private final IteraSettings itera_settings;
   private final ExpandCostFactor[] trace_cost_arr;
   private final int start_ripup_costs;
   private final GuiResources resources;

   // Used to draw the airline of the current routed incomplete
   private PlaSegmentFloat air_line = null;
   

   /**
    * Creates a new batch auto router.
    */
   public BatchAutorouter(BrdActionThread p_thread, boolean p_with_preferred_directions, int p_start_ripup_costs)
      {
      s_thread = p_thread;
      hdlg     = p_thread.hdlg;
      routing_board  = hdlg.get_routing_board();
      itera_settings = hdlg.itera_settings;
      
      resources = hdlg.newGuiResources("interactive.resources.InteractiveState");
      
      if (p_with_preferred_directions)
         {
         trace_cost_arr = itera_settings.autoroute_settings.get_trace_cost_arr();
         }
      else
         {
         // remove preferred direction
         trace_cost_arr = new ExpandCostFactor[this.routing_board.get_layer_count()];
         for (int i = 0; i < trace_cost_arr.length; ++i)
            {
            double curr_min_cost = itera_settings.autoroute_settings.get_preferred_direction_trace_costs(i);
            this.trace_cost_arr[i] = new ExpandCostFactor(curr_min_cost, curr_min_cost);
            }
         }

      this.start_ripup_costs = p_start_ripup_costs;
      }


   public void remove_tails ( )
      {
      routing_board.start_marking_changed_area();
      
      routing_board.remove_trace_tails(-1, BrdStopConnection.NONE);
      
      TimeLimitStoppable time_limit = new TimeLimitStoppable(10, s_thread);
      
      routing_board.optimize_changed_area(new int[0], null, itera_settings.trace_pull_tight_accuracy, trace_cost_arr, time_limit, null);
      }
   
   
   private void autoroute_remove_tails_try ()
      {
      if ( s_thread.is_stop_requested() ) return;
      
      if (itera_settings.autoroute_settings.stop_remove_fanout_vias) return;
      
      // clean up the route if the board is completed and if fanout is used
      remove_tails();
      }

   /**
    * Autoroute passes until the board is completed or the autoroute is stopped by the user. 
    */
   public void autoroute_loop()
      {
      int previous_unrouted_count=0;
      int unrouted_count=0;
      
      while ( ! s_thread.is_stop_requested())
         {
         int curr_pass_no = itera_settings.autoroute_settings.pass_no_get();
         
         String message = resources.getString("batch_autorouter") + " " + resources.getString("stop_message") + "        " + resources.getString("pass") + " " + curr_pass_no + ": ";
         hdlg.screen_messages.set_status_message(message);
         hdlg.userPrintln(message);
         
         unrouted_count = autoroute_pass(curr_pass_no);
         
         // no more traces to route
         if ( unrouted_count <= 0 ) break;
         
         // avoid spinning forever trying to route the same traces
         if ( unrouted_count == previous_unrouted_count ) break;
         
         previous_unrouted_count = unrouted_count;
         
         itera_settings.autoroute_settings.pass_no_inc();
         }
      
      autoroute_remove_tails_try();
      }

   private void autoroute_info_show (int items_to_go, int routed, int ripped, int failed)
      {
      hdlg.screen_messages.set_batch_autoroute_info(items_to_go,routed,ripped,failed);
      
      if ( hdlg.debug(Mdbg.MAZE, Ldbg.TRACE))
         hdlg.userPrintln("to_go="+items_to_go+" routed="+routed+" ripped="+ripped+" failed="+failed);
      }
   
   /**
    * Really, what is the point of going trough all nets ? If I just route the ones that are incomplete...
    * @param ar_pass_no
    * @param p_with_screen_message
    * @return the number of incomplete on the board
    */
   private int autoroute_pass_try(int ar_pass_no )
      {
      Collection<BrdItem> autoroute_item_list = new LinkedList<BrdItem>();
      
      Set<BrdItem> handeled_items = new TreeSet<BrdItem>();

      Iterator<UndoableObjectNode> iter = routing_board.item_list.start_read_object();
      
      for (;;)
         {
         UndoableObjectStorable curr_ob = routing_board.item_list.read_object(iter);

         if (curr_ob == null) break;
         
         // skip objects that are not board items
         if ( ! (curr_ob instanceof BrdItem) ) continue;
         
         // skip objects that are not Connectable or board items
         if ( ! ( curr_ob instanceof BrdConnectable ) ) continue;
         
         BrdItem curr_item = (BrdItem) curr_ob;

         // this is really not clear.... yet
         if ( curr_item.is_route()) continue;
        
         // if current item is in the ones already handled skip it
         if ( handeled_items.contains(curr_item)) continue;
         
         for (int idx = 0; idx < curr_item.net_count(); ++idx)
            {
            // Go over all nets of the item
            int curr_net_no = curr_item.get_net_no(idx);
         
            // Get all connected of the current item
            Set<BrdItem> connected_set = curr_item.get_connected_set(curr_net_no);
            
            for (BrdItem curr_connected_item : connected_set)
               {
               if (curr_connected_item.net_count() <= 1)
                  {
                  handeled_items.add(curr_connected_item);
                  }
               }
            
            int net_item_count = routing_board.connectable_item_count(curr_net_no);
            
            if (connected_set.size() < net_item_count)
               {
               autoroute_item_list.add(curr_item);
               }
            }
         }
      
      if (autoroute_item_list.isEmpty())
         {
         air_line = null;
         return 0;
         }
      
      int items_to_go_count = autoroute_item_list.size();
      int ripped_item_count = 0;
      int failed_count = 0;
      int routed_count = 0;
      
      autoroute_info_show(items_to_go_count, routed_count, ripped_item_count, failed_count);
      
      for (BrdItem curr_item : autoroute_item_list)
         {
         if (s_thread.is_stop_requested()) break;
         
         for (int index = 0; index < curr_item.net_count(); index++)
            {
            int r_net_no = curr_item.get_net_no(index);

            SortedSet<BrdItem> ripped_item_list = new TreeSet<BrdItem>();
            
            routing_board.start_marking_changed_area();
            
            if (autoroute_item(curr_item, r_net_no, ripped_item_list, ar_pass_no))
               {
               routed_count++;
               hdlg.repaint();
               }
            else
               {
               failed_count++;
               }
            
            --items_to_go_count;
            
            ripped_item_count += ripped_item_list.size();
            
            autoroute_info_show(items_to_go_count, routed_count, ripped_item_count, failed_count);
            }
         }
      
      air_line = null;
      
      return failed_count;
      }

   /**
    * Autoroute one pass of all items of the board. 
    * @return the number of failed items in the board
    */
   public int autoroute_pass(int p_pass_no )
      {
      try
         {
         return autoroute_pass_try(p_pass_no );
         }
      catch (Exception exc)
         {
         hdlg.userPrintln(classname+"autoroute_pass", exc);
         air_line = null;
         // consider the job done, there is really no point to retry
         return 0;
         }
      }


   /**
    * Attempt to autoroute one item and a specific net of that item
    * @param p_item
    * @param p_route_net_no
    * @param p_ripped_item_list
    * @param ar_pass_no
    * @return true if the routing is successful
    */
   private boolean autoroute_item(BrdItem p_item, int p_route_net_no, SortedSet<BrdItem> p_ripped_item_list, int ar_pass_no)
      {
      boolean contains_plane = false;
      
      RuleNet route_net = routing_board.brd_rules.nets.get(p_route_net_no);
      
      if (route_net != null)
         {
         hdlg.userPrintln(classname+"autoroute_item_try: net "+route_net.name);
         contains_plane = route_net.contains_plane();
         }
      else
         {
         hdlg.userPrintln(classname+"autoroute_item_try: item "+p_item+" NO net ??");
         }
      
      int curr_via_costs;

      if (contains_plane)
         {
         curr_via_costs = itera_settings.autoroute_settings.get_plane_via_costs();
         }
      else
         {
         curr_via_costs = itera_settings.autoroute_settings.get_via_costs();
         }
      
      ArtControl autoroute_control = new ArtControl( routing_board, p_route_net_no, itera_settings, curr_via_costs, trace_cost_arr);
      
      autoroute_control.ripup_costs = start_ripup_costs * ar_pass_no;

      Set<BrdItem> unconnected_set = p_item.get_unconnected_set(p_route_net_no);

      // p_item is already routed.
      if (unconnected_set.size() == 0) return true; 
      
      Set<BrdItem> connected_set = p_item.get_connected_set(p_route_net_no);
      Set<BrdItem> route_start_set;
      Set<BrdItem> route_dest_set;
      
      if (contains_plane)
         {
         for (BrdItem curr_item : connected_set)
            {
            if (curr_item instanceof BrdAreaConduction)
               {
               return true; // already connected to plane
               }
            }
         }
      
      if (contains_plane)
         {
         route_start_set = connected_set;
         route_dest_set = unconnected_set;
         }
      else
         {
         route_start_set = unconnected_set;
         route_dest_set = connected_set;
         }

      calc_airline(route_start_set, route_dest_set);

      TimeLimitStoppable time_limit = new TimeLimitStoppable(10 + ar_pass_no, s_thread);

      ArtEngine autoroute_engine = new ArtEngine(routing_board, p_route_net_no, autoroute_control.trace_clearance_class_no, time_limit);
      
      ArtResult aresult = autoroute_engine.autoroute_connection(route_start_set, route_dest_set, autoroute_control, p_ripped_item_list);
      
      if (aresult == ArtResult.ALREADY_CONNECTED)
         {
         routing_board.userPrintln("how can it be already connected ?");
         return true;
         }
      
      return aresult == ArtResult.ROUTED;
      }

   /**
    * @return the airline of the current autoroute connection or null
    */
   public PlaSegmentFloat get_air_line()
      {
      return air_line;
      }

   private void calc_airline(Collection<BrdItem> p_from_items, Collection<BrdItem> p_to_items)
      {
      PlaPointFloat from_corner = null;
      PlaPointFloat to_corner = null;
      
      double min_distance = Double.MAX_VALUE;

      for (BrdItem curr_from_item : p_from_items)
         {
         if (!(curr_from_item instanceof BrdAbit)) continue;

         PlaPointFloat curr_from_corner = ((BrdAbit) curr_from_item).get_center().to_float();

         if ( curr_from_corner == null ) continue;
         
         for (BrdItem curr_to_item : p_to_items)
            {
            if (!(curr_to_item instanceof BrdAbit)) continue;
               
            PlaPointFloat curr_to_corner = ((BrdAbit) curr_to_item).get_center().to_float();
            
            if ( curr_to_corner == null ) continue;
            
            double curr_distance = curr_from_corner.length_square(curr_to_corner);
            
            if (curr_distance < min_distance)
               {
               min_distance = curr_distance;
               from_corner = curr_from_corner;
               to_corner = curr_to_corner;
               }
            }
         }
      
      if ( from_corner == null || to_corner == null ) return;
      
      air_line = new PlaSegmentFloat(from_corner, to_corner);
      }
   }
