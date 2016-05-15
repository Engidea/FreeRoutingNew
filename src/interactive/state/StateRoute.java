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
 * RouteState.java
 *
 * Created on 8. November 2003, 08:22
 */
package interactive.state;

import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.rules.RuleNet;
import freert.varie.NetNosList;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.IteraRoute;
import interactive.LogfileScope;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import board.BrdLayerStructure;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.items.BrdTrace;
import board.items.BrdTracePolyline;
import board.varie.BrdStopConnection;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Interactive routing state.
 *
 * @author Alfons Wirtz
 */
public class StateRoute extends StateInteractive
   {
   protected IteraRoute route = null;
   private Set<BrdItem> routing_target_set = null;
   protected boolean observers_activated = false;
   
   /**
    * Creates a new instance of RouteState If p_logfile != null, the creation of the route is stored in the logfile.
    */
   protected StateRoute(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      }
   
   /**
    * Returns a new instance of this class or null, if starting a new route was not possible at p_location. 
    * If p_actlog != null the creation of the route is stored in the logfile.
    **/
   public static StateRoute get_instance(PlaPointFloat p_location, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_actlog)
      {
      if (!(p_parent_state instanceof StateMenu))
         {
         System.err.println("RouteState.get_instance: unexpected parent state");
         return null;
         }
      
      p_board_handling.display_layer_messsage();
      
      PlaPointInt location = p_location.round();
      
      BrdItem picked_item = pick_start_item(location, p_board_handling);
      
      if (picked_item == null) return null;
      
      int net_count = picked_item.net_count();

      if (net_count <= 0) return null;

      NetNosList route_net_no_arr;
      
      if (picked_item instanceof BrdAbitPin && net_count > 1)
         {
         // tie pin, remove nets, which are already conneccted to this pin on the current layer.
         route_net_no_arr = new NetNosList ( get_route_net_numbers_at_tie_pin((BrdAbitPin) picked_item, p_board_handling.itera_settings.layer_no) );
         }
      else
         {
         int [] a_net_no_arr = new int[net_count];
      
         for (int index = 0; index < net_count; ++index) a_net_no_arr[index] = picked_item.get_net_no(index);
         
         route_net_no_arr = new NetNosList(a_net_no_arr);
         }
      
      if (route_net_no_arr.is_empty() ) return null;
      
      // uff, this static stuff is really annoying....
      RoutingBoard routing_board = p_board_handling.get_routing_board();
      
      int[] trace_half_widths = new int[routing_board.get_layer_count()];
      boolean[] layer_active_arr = new boolean[trace_half_widths.length];
      
      for (int index = 0; index < trace_half_widths.length; ++index)
         {
         trace_half_widths[index] = p_board_handling.get_trace_halfwidth(route_net_no_arr.first(), index);
         
         layer_active_arr[index] = false;
         
         for (int route_net_no : route_net_no_arr )
            {
            if ( p_board_handling.is_active_routing_layer(route_net_no, index))
               {
               layer_active_arr[index] = true;
               }
            }
         }

      int trace_clearance_class = p_board_handling.get_trace_clearance_class(route_net_no_arr.first() );
      
      boolean start_ok = true;
      
      if (picked_item instanceof BrdTracePolyline)
         {
         BrdTracePolyline picked_trace = (BrdTracePolyline) picked_item;
         
         PlaPoint picked_corner = picked_trace.nearest_end_point(location);
         
         if (picked_corner instanceof PlaPointInt && p_location.distance(picked_corner.to_float()) < 5 * picked_trace.get_half_width())
            {
            // Why should it fail if it is a rational ? pippo
            location = (PlaPointInt) picked_corner;
            }
         else
            {
            PlaPointFloat nearest_point = picked_trace.polyline().nearest_point_approx(p_location);

            location = nearest_point.round();
            
            if ( ! routing_board.connect_to_trace(location, picked_trace, picked_trace.get_half_width(), picked_trace.clearance_class_no()))
               {
               start_ok = false;
               }
            }
         
         if (start_ok && !p_board_handling.itera_settings.manual_rule_selection)
            {
            // Pick up the half with and the clearance class of the found trace.
            int[] new_trace_half_widths = new int[trace_half_widths.length];
            System.arraycopy(trace_half_widths, 0, new_trace_half_widths, 0, trace_half_widths.length);
            new_trace_half_widths[picked_trace.get_layer()] = picked_trace.get_half_width();
            trace_half_widths = new_trace_half_widths;
            trace_clearance_class = picked_trace.clearance_class_no();
            }
         }
      else if (picked_item instanceof BrdAbit)
         {
         BrdAbit drill_item = (BrdAbit) picked_item;

         location = drill_item.center_get();
         }

      if (!start_ok) return null;

      RuleNet curr_net = routing_board.brd_rules.nets.get(route_net_no_arr.first());

      if (curr_net == null)  return null;

      // Switch to stitch mode for nets, which are shove fixed.
      boolean is_stitch_route = p_board_handling.itera_settings.is_stitch_route || curr_net.get_class().is_shove_fixed() || !curr_net.get_class().can_pull_tight();
      routing_board.generate_snapshot();
      
      StateRoute new_instance;

      if (is_stitch_route)
         {
         new_instance = new StateRouteStitch(p_parent_state, p_board_handling, p_actlog);
         }
      else
         {
         new_instance = new StateRouteDynamic(p_parent_state, p_board_handling, p_actlog);
         }
      
      new_instance.routing_target_set = picked_item.get_unconnected_set(-1);

      new_instance.route = new IteraRoute(location, 
            p_board_handling.itera_settings.layer_no, 
            trace_half_widths, 
            layer_active_arr, 
            route_net_no_arr, 
            trace_clearance_class,
            p_board_handling.get_via_rule(route_net_no_arr.first()), 
            p_board_handling.itera_settings.push_enabled, 
            picked_item, 
            new_instance.
            routing_target_set, 
            routing_board, 
            is_stitch_route, 
            p_board_handling.itera_settings.is_via_snap_to_smd_center(), 
            p_board_handling.itera_settings);
      
      new_instance.observers_activated = !routing_board.observers_active();
      
      if (new_instance.observers_activated)
         {
         routing_board.start_notify_observers();
         }
      
      p_board_handling.repaint();

      if (new_instance.actlog != null)
         {
         new_instance.actlog.start_scope(LogfileScope.CREATING_TRACE, p_location);
         p_board_handling.hide_ratsnest();
         }

      new_instance.display_default_message();

      return new_instance;
      }


   /**
    * Checks starting an interactive route at p_location. 
    * @return the picked start item of the routing at p_location, or null, if no such item was found.
    */
   private static BrdItem pick_start_item(PlaPointInt p_location, IteraBoard p_hdlg)
      {
      RoutingBoard routing_board = p_hdlg.get_routing_board();

      // look if an already exististing trace ends at p_start_corner and pick it up in this case.
      BrdItem picked_item = routing_board.pick_nearest_routing_item(p_location, p_hdlg.itera_settings.layer_no, null);
      
      if ( picked_item != null ) return picked_item;
      
      // if we are not selecting on all layers just get out, nothing found
      if ( ! p_hdlg.itera_settings.select_on_all_visible_layers ) return null;

      // Nothing found on preferred layer, try the other visible layers. Prefer the outer layers.

      int layer_count = routing_board.get_layer_count();
      
      picked_item = pick_routing_item(p_location, 0, p_hdlg);

      if (picked_item != null) return picked_item;
      
      picked_item = pick_routing_item(p_location, layer_count - 1, p_hdlg);
      
      if (picked_item != null) return picked_item;

      // prefer signal layers
      for (int index = 1; index < layer_count - 1; ++index)
         {
         if ( ! routing_board.layer_structure.is_signal(index)) continue;

         picked_item = pick_routing_item(p_location, index, p_hdlg);

         if (picked_item != null) return picked_item;
         }

      for (int index = 1; index < layer_count - 1; ++index)
         {
         if ( routing_board.layer_structure.is_signal(index)) continue;

         picked_item = pick_routing_item(p_location, index, p_hdlg);

         if (picked_item != null) return picked_item;
         }

      return picked_item;
      }

   private static BrdItem pick_routing_item(PlaPointInt p_location, int p_layer_no, IteraBoard p_hdlg)
      {
      if (p_layer_no == p_hdlg.itera_settings.layer_no || (p_hdlg.gdi_context.get_layer_visibility(p_layer_no) <= 0))
         {
         return null;
         }

      BrdItem picked_item = p_hdlg.get_routing_board().pick_nearest_routing_item(p_location, p_layer_no, null);
      
      if (picked_item != null)
         {
         p_hdlg.set_layer(picked_item.first_layer());
         }
      
      return picked_item;
      }

   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      return add_corner(p_point);
      }

   /**
    * Action to be taken when a key is pressed (Shortcut).
    */
   public StateInteractive key_typed(char p_key_char)
      {
      StateInteractive curr_return_state = this;
      
      if (Character.isDigit(p_key_char))
         {
         // change to the p_key_char-the signal layer
         board.BrdLayerStructure layer_structure = r_brd.layer_structure;
         int d = Character.digit(p_key_char, 10);
         d = Math.min(d, layer_structure.signal_layer_count());
         // Board layers start at 0, keyboard input for layers starts at 1.
         d = Math.max(d - 1, 0);
         board.BrdLayer new_layer = layer_structure.get_signal_layer(d);
         d = layer_structure.get_no(new_layer);

         if (d >= 0)
            {
            change_layer_action(d);
            }
         }
      else if (p_key_char == '+')
         {
         // change to the next signal layer
         BrdLayerStructure layer_structure = r_brd.layer_structure;
         int current_layer_no = i_brd.itera_settings.layer_no;
         for (;;)
            {
            ++current_layer_no;
            if (current_layer_no >= layer_structure.size() || layer_structure.is_signal(current_layer_no))
               {
               break;
               }
            }
         if (current_layer_no < layer_structure.size())
            {
            change_layer_action(current_layer_no);
            }
         }
      else if (p_key_char == '-')
         {
         // change to the to the previous signal layer
         board.BrdLayerStructure layer_structure = r_brd.layer_structure;
         int current_layer_no = i_brd.itera_settings.layer_no;
         for (;;)
            {
            --current_layer_no;
            if (current_layer_no < 0 || layer_structure.is_signal(current_layer_no))
               {
               break;
               }
            }
         if (current_layer_no >= 0)
            {
            change_layer_action(current_layer_no);
            }
         }
      else
         {
         curr_return_state = super.key_typed(p_key_char);
         }
      return curr_return_state;
      }

   /**
    * Append a line to p_location to the trace routed so far. 
    * @return from state, if the route is completed by connecting to a target.
    */
   public StateInteractive add_corner(PlaPointFloat p_location)
      {
      boolean route_completed = route.next_corner(p_location);
      
      String layer_string = r_brd.layer_structure.get_name(route.nearest_target_layer());
      
      i_brd.screen_messages.set_target_layer(layer_string);
      
      if (actlog != null) actlog.add_corner(p_location);
      
      // assume I stay in this state
      StateInteractive result = this;

      if (route_completed)
         {
         result = return_state;

         if (observers_activated)
            {
            r_brd.end_notify_observers();
            observers_activated = false;
            }

         i_brd.screen_messages.clear();

         for (int curr_net_no : route.net_nos )
            {
            i_brd.update_ratsnest(curr_net_no);
            }
         }
      
      i_brd.recalculate_length_violations();
      i_brd.repaint(i_brd.get_graphics_update_rectangle());
     
      return result;
      }

   public StateInteractive cancel()
      {
      BrdTrace tail = r_brd.get_trace_tail(route.get_last_corner(), i_brd.itera_settings.layer_no, route.net_nos);
      
      if (tail != null)
         {
         Collection<BrdItem> remove_items = tail.get_connection_items(BrdStopConnection.VIA);
         if (i_brd.itera_settings.push_enabled)
            {
            r_brd.remove_items_and_pull_tight(remove_items, i_brd.itera_settings.trace_pull_tight_region_width, i_brd.itera_settings.trace_pull_tight_accuracy, false);
            }
         else
            {
            r_brd.remove_items_unfixed(remove_items);
            }
         }
      if (observers_activated)
         {
         r_brd.end_notify_observers();
         observers_activated = false;
         }

      actlog_start_scope(LogfileScope.CANCEL_SCOPE);

      i_brd.screen_messages.clear();
      
      for (int curr_net_no : route.net_nos )
         {
         i_brd.update_ratsnest(curr_net_no);
         }
      
      return return_state;
      }

   public boolean change_layer_action(int p_new_layer)
      {
      boolean result = true;
      if (p_new_layer >= 0 && p_new_layer < r_brd.get_layer_count())
         {
         if (route != null && !route.is_layer_active(p_new_layer))
            {
            String layer_name = r_brd.layer_structure.get_name(p_new_layer);
            i_brd.screen_messages.set_status_message(resources.getString("layer_not_changed_because_layer") + " " + layer_name + " " + resources.getString("is_not_active_for_the_current_net"));
            }
         
         boolean change_layer_succeeded = route.change_layer(p_new_layer);
         if (change_layer_succeeded)
            {
            boolean connected_to_plane = false;
            // check, if the layer change resulted in a connection to a power plane.
            int old_layer = i_brd.itera_settings.get_layer_no();
            ItemSelectionFilter selection_filter = new ItemSelectionFilter(ItemSelectionChoice.VIAS);
            Collection<BrdItem> picked_items = r_brd.pick_items(route.get_last_corner(), old_layer, selection_filter);
            BrdAbitVia new_via = null;
            for (BrdItem curr_via : picked_items)
               {
               if (curr_via.shares_net_no(route.net_nos))
                  {
                  new_via = (BrdAbitVia) curr_via;
                  break;
                  }
               }
            if (new_via != null)
               {
               int from_layer;
               int to_layer;
               if (old_layer < p_new_layer)
                  {
                  from_layer = old_layer + 1;
                  to_layer = p_new_layer;
                  }
               else
                  {
                  from_layer = p_new_layer;
                  to_layer = old_layer - 1;
                  }
               Collection<BrdItem> contacts = new_via.get_normal_contacts();
               for (BrdItem curr_item : contacts)
                  {
                  if (curr_item instanceof BrdAreaConduction)
                     {
                     BrdAreaConduction curr_area = (BrdAreaConduction) curr_item;
                     if (curr_area.get_layer() >= from_layer && curr_area.get_layer() <= to_layer)
                        {
                        connected_to_plane = true;
                        break;
                        }
                     }
                  }
               }

            if (connected_to_plane)
               {
               i_brd.set_interactive_state(return_state);
               
               for (int curr_net_no : route.net_nos ) i_brd.update_ratsnest(curr_net_no);
               }
            else
               {
               p_new_layer = i_brd.set_layer(p_new_layer);
               String layer_name = r_brd.layer_structure.get_name(p_new_layer);
               i_brd.screen_messages.set_status_message(resources.getString("layer_changed_to") + " " + layer_name);
               // make the current situation restorable by undo
               r_brd.generate_snapshot();
               }
            if (actlog != null)
               {
               actlog.start_scope(LogfileScope.CHANGE_LAYER, p_new_layer);
               }
            }
         else
            {
            int shove_failing_layer = r_brd.get_shove_failing_layer();
            if (shove_failing_layer >= 0)
               {
               String layer_name = r_brd.layer_structure.get_name(r_brd.get_shove_failing_layer());
               i_brd.screen_messages.set_status_message(resources.getString("layer_not_changed_because_of_obstacle_on_layer") + " " + layer_name);
               }
            else
               {
               System.out.println("RouteState.change_layer_action: shove_failing_layer not set");
               }
            result = false;
            }
         i_brd.repaint();
         }
      return result;
      }

   /**
    * get nets of p_tie_pin except nets of traces, which are already conneccted to this pin on p_layer.
    */
   static int[] get_route_net_numbers_at_tie_pin(BrdAbitPin p_pin, int p_layer)
      {
      Set<Integer> net_number_list = new TreeSet<Integer>();
      for (int i = 0; i < p_pin.net_count(); ++i)
         {
         net_number_list.add(p_pin.get_net_no(i));
         }
      
      Set<BrdItem> contacts = p_pin.get_normal_contacts();
      for (BrdItem curr_contact : contacts)
         {
         if (curr_contact.first_layer() <= p_layer && curr_contact.last_layer() >= p_layer)
            {
            for (int i = 0; i < curr_contact.net_count(); ++i)
               {
               net_number_list.remove(curr_contact.get_net_no(i));
               }
            }
         }
      
      int[] result = new int[net_number_list.size()];
      int curr_ind = 0;
      for (Integer curr_net_number : net_number_list)
         {
         result[curr_ind] = curr_net_number;
         ++curr_ind;
         }
      return result;
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      if (route == null) return;

      route.draw(p_graphics, i_brd.gdi_context);
      }

   public void display_default_message()
      {
      if (route == null) return;
      
      RuleNet curr_net = r_brd.brd_rules.nets.get(route.net_nos.first());
      
      i_brd.screen_messages.set_status_message(resources.getString("routing_net") + " " + curr_net.name);
      }
   }
