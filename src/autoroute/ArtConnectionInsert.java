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
 * Created on 23. Februar 2004, 08:18
 */
package autoroute;

import java.util.Set;
import autoroute.varie.ArtLocateResult;
import board.RoutingBoard;
import board.infos.BrdViaInfo;
import board.items.BrdAbitPin;
import board.items.BrdItem;
import board.items.BrdTracep;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.library.LibPadstack;
import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;
import freert.planar.Polyline;
import freert.varie.NetNosList;

/**
 * Inserts the traces and vias of the connection found by the autoroute algorithm.
 *
 * @author Alfons Wirtz
 */
public final class ArtConnectionInsert
   {
   private static final String classname="ArtInsertConnection.";
   
   private final RoutingBoard r_board;
   private final ArtControl ctrl;
   
   private PlaPointInt last_corner = null;
   private PlaPointInt first_corner = null;

   public ArtConnectionInsert( RoutingBoard p_board, ArtControl p_ctrl )
      {
      r_board = p_board;
      ctrl = p_ctrl;
      }

   /**
    * Actually try to insert the given connection
    * @param p_connection
    * @return true if it does it or false if it fails
    */
   public boolean insert (ArtConnectionLocate p_connection)
      {
      if (p_connection == null )
         {
         r_board.userPrintln(classname+"insert: p_connection == null");
         return false;
         }
      
      if ( p_connection.connection_items == null)
         {
         r_board.userPrintln(classname+"insert: connection_items == null");
         return false;
         }

      int curr_layer = p_connection.target_layer;
      
      for ( ArtLocateResult curr_new_item : p_connection.connection_items )
         {
         
         if (! insert_via_done(curr_new_item.corner_first(), curr_layer, curr_new_item.layer))
            {
            r_board.userPrintln(classname+"insert_via FAIL");
            return false;
            }
         
         curr_layer = curr_new_item.layer;
         
         if (! insert_trace_done(curr_new_item))
            {
            r_board.userPrintln(classname+"insert trace failed for net "+ctrl.net_no);
            return false;
            }
         }
      
      if (! insert_via_done(last_corner, curr_layer, p_connection.start_layer))
         {
         r_board.userPrintln(classname+"insert_via on last corner FAIL");
         return false;
         }
      
      if (p_connection.target_item instanceof BrdTracep)
         {
         BrdTracep to_trace = (BrdTracep) p_connection.target_item;
         r_board.connect_to_trace(first_corner, to_trace, ctrl.trace_half_width[p_connection.start_layer], ctrl.trace_clearance_idx);
         }
      
      if (p_connection.start_item instanceof BrdTracep)
         {
         BrdTracep to_trace = (BrdTracep) p_connection.start_item;
         r_board.connect_to_trace(last_corner, to_trace, ctrl.trace_half_width[p_connection.target_layer], ctrl.trace_clearance_idx);
         }
      
      r_board.normalize_traces(ctrl.net_no);
      
      return true;
      }


   /**
    * Inserts the trace by shoving aside obstacle traces and vias.
    * @return true if all is fine, false if that was not possible for the whole trace.
    */
   private boolean insert_trace_done(ArtLocateResult p_locate)
      {
      if (p_locate.size() == 1)
         {
         last_corner = p_locate.corner(0);
         return true;
         }
      
      boolean result = true;

      // switch off correcting connection to pin because it may get wrong in inserting the polygon line for line.
      double saved_edge_to_turn_dist = r_board.brd_rules.set_pin_edge_to_turn_dist(-1);

      // Look for pins at the start and the end of p_trace in case that neckdown is necessary
      BrdAbitPin start_pin = null;
      BrdAbitPin end_pin = null;
      
      if (ctrl.with_neckdown)
         {
         ItemSelectionFilter item_filter = new ItemSelectionFilter(ItemSelectionChoice.PINS);
         PlaPointInt curr_end_corner = p_locate.corner_first();
         for (int index = 0; index < 2; ++index)
            {
            Set<BrdItem> picked_items = r_board.pick_items(curr_end_corner, p_locate.layer, item_filter);
            
            for (BrdItem curr_item : picked_items)
               {
               BrdAbitPin curr_pin = (BrdAbitPin) curr_item;
               if (curr_pin.contains_net(ctrl.net_no) && curr_pin.center_get().equals(curr_end_corner))
                  {
                  if (index == 0)
                     {
                     start_pin = curr_pin;
                     }
                  else
                     {
                     end_pin = curr_pin;
                     }
                  }
               }
            curr_end_corner = p_locate.corner(p_locate.size(-1) );
            }
         }
      
      NetNosList net_no_arr = new NetNosList( ctrl.net_no );

      int from_corner_no = 0;
      
      for (int index = 1; index < p_locate.size(); ++index)
         {
         // no need to be stingy with the ArrayList allocation
         PlaPointIntAlist curr_corner_arr = new PlaPointIntAlist(p_locate.size());
         
         for (int jndex = from_corner_no; jndex <= index; ++jndex)
            curr_corner_arr.add( p_locate.corner(jndex));
         
         Polyline insert_polyline = new Polyline(curr_corner_arr);
         
         PlaPointInt ok_point = r_board.insert_trace(
               insert_polyline, 
               ctrl.trace_half_width[p_locate.layer], 
               p_locate.layer, 
               net_no_arr, 
               ctrl.trace_clearance_idx,
               ctrl.max_shove_trace_recursion_depth, 
               ctrl.max_shove_via_recursion_depth, 
               ctrl.max_spring_over_recursion_depth, 
               Integer.MAX_VALUE, 
               ctrl.pull_tight_accuracy, 
               true, 
               null);
         
         boolean neckdown_inserted = false;
         
         if ( ok_point == null )
            {
            result = false;
            break;
            }
         
         if ( ok_point != insert_polyline.corner_last() && ctrl.with_neckdown && curr_corner_arr.size() == 2)
            {
            neckdown_inserted = insert_neckdown(ok_point, curr_corner_arr.get(1), p_locate.layer, start_pin, end_pin);
            }
         if (ok_point == insert_polyline.corner_last() || neckdown_inserted)
            {
            from_corner_no = index;
            }
         else if (ok_point == insert_polyline.corner_first() && index != p_locate.size(-1))
            {
            // if ok_point == insert_polyline.first_corner() the spring over may have failed.
            // Spring over may correct the situation because an insertion, which is ok with clearance compensation
            // may cause violations without clearance compensation.
            // In this case repeating the insertion with more distant corners may allow the spring_over to correct the situation.
            if (from_corner_no > 0)
               {
               // p_trace.corners[i] may be inside the offset for the substitute trace around a spring_over obstacle (if clearance compensation is off).
               if (curr_corner_arr.size() < 3)
                  {
                  // first correction
                  --from_corner_no;
                  }
               }

            System.out.println("InsertFoundConnectionAlgo: violation corrected");
            }
         else
            {
            result = false;
            break;
            }
         }
      
            
      if ( ! r_board.debug(Mdbg.AUTORT, Ldbg.SPC_C) )
         {
         // the idea is that this code is always executed, unless you are debugging autoroute special C
         for (int index = 0; index < p_locate.size(-1); ++index)
            {
            BrdTracep trace_stub = r_board.get_trace_tail(p_locate.corner(index), p_locate.layer, net_no_arr);

            if (trace_stub == null) continue;

            r_board.remove_item(trace_stub);
            }
         }
      
      r_board.brd_rules.set_pin_edge_to_turn_dist(saved_edge_to_turn_dist);
      
      if ( first_corner == null) first_corner = p_locate.corner_first();
      
      last_corner = p_locate.corner_last();
      
      return result;
      }

   /**
    * 
    */
   private boolean insert_neckdown(PlaPointInt p_from_corner, PlaPointInt p_to_corner, int p_layer, BrdAbitPin p_start_pin, BrdAbitPin p_end_pin)
      {
      if (p_start_pin != null)
         {
         PlaPointInt ok_point = try_neck_down(p_to_corner, p_from_corner, p_layer, p_start_pin, true);

         if (ok_point == p_from_corner)  return true;
         }
      
      if (p_end_pin != null)
         {
         PlaPointInt ok_point = try_neck_down(p_from_corner, p_to_corner, p_layer, p_end_pin, false);

         if (ok_point == p_to_corner) return true;
         }
      
      return false;
      }

   private PlaPointInt try_neck_down(PlaPointInt p_from_corner, PlaPointInt p_to_corner, int p_layer, BrdAbitPin p_pin, boolean p_at_start)
      {
      if (!p_pin.is_on_layer(p_layer)) return null;

      PlaPointFloat pin_center = p_pin.center_get().to_float();
      double curr_clearance = r_board.brd_rules.clearance_matrix.value_at(ctrl.trace_clearance_idx, p_pin.clearance_idx(), p_layer);
      double pin_neck_down_distance = 2 * (0.5 * p_pin.get_max_width(p_layer) + curr_clearance);

      if (pin_center.distance(p_to_corner.to_float()) >= pin_neck_down_distance) return null;

      int neck_down_halfwidth = p_pin.get_trace_neckdown_halfwidth(p_layer);

      if (neck_down_halfwidth >= ctrl.trace_half_width[p_layer]) return null;

      PlaPointFloat float_from_corner = p_from_corner.to_float();
      PlaPointFloat float_to_corner = p_to_corner.to_float();

      final int TOLERANCE = 2;

      NetNosList net_no_arr = new NetNosList(ctrl.net_no);

      double ok_length = r_board.check_trace(p_from_corner, p_to_corner, p_layer, net_no_arr, ctrl.trace_half_width[p_layer], ctrl.trace_clearance_idx, true);

      if (ok_length >= Integer.MAX_VALUE) return p_from_corner;
      
      ok_length -= TOLERANCE;
      PlaPointInt neck_down_end_point;
      if (ok_length <= TOLERANCE)
         {
         neck_down_end_point = p_from_corner;
         }
      else
         {
         PlaPointFloat float_neck_down_end_point = float_from_corner.change_length(float_to_corner, ok_length);
         neck_down_end_point = float_neck_down_end_point.round();
         // add a corner in case neck_down_end_point is not exactly on the line from p_from_corner to p_to_corner
         boolean horizontal_first = Math.abs(float_from_corner.v_x - float_neck_down_end_point.v_x) >= Math.abs(float_from_corner.v_y - float_neck_down_end_point.v_y);
         PlaPointInt add_corner = ArtConnectionLocate.calculate_additional_corner(float_from_corner, float_neck_down_end_point, horizontal_first, r_board.brd_rules.get_trace_snap_angle()).round();

         PlaPointInt curr_ok_point = r_board.insert_trace(
               p_from_corner, 
               add_corner, 
               ctrl.trace_half_width[p_layer], 
               p_layer, 
               net_no_arr, 
               ctrl.trace_clearance_idx,
               ctrl.max_shove_trace_recursion_depth, 
               ctrl.max_shove_via_recursion_depth, 
               ctrl.max_spring_over_recursion_depth, 
               Integer.MAX_VALUE, 
               ctrl.pull_tight_accuracy, 
               true, 
               null);

         if (curr_ok_point != add_corner) return p_from_corner;

         curr_ok_point = r_board.insert_trace (
               add_corner, 
               neck_down_end_point, 
               ctrl.trace_half_width[p_layer], 
               p_layer, 
               net_no_arr, 
               ctrl.trace_clearance_idx,
               ctrl.max_shove_trace_recursion_depth, 
               ctrl.max_shove_via_recursion_depth, 
               ctrl.max_spring_over_recursion_depth, 
               Integer.MAX_VALUE, 
               ctrl.pull_tight_accuracy, 
               true, null);

         if (curr_ok_point != neck_down_end_point) return p_from_corner;

         add_corner = ArtConnectionLocate.calculate_additional_corner(float_neck_down_end_point, float_to_corner, !horizontal_first, r_board.brd_rules.get_trace_snap_angle()).round();
         if (!add_corner.equals(p_to_corner))
            {
            curr_ok_point = r_board.insert_trace (
                  neck_down_end_point, 
                  add_corner, 
                  ctrl.trace_half_width[p_layer], 
                  p_layer, 
                  net_no_arr, 
                  ctrl.trace_clearance_idx,
                  ctrl.max_shove_trace_recursion_depth, 
                  ctrl.max_shove_via_recursion_depth, 
                  ctrl.max_spring_over_recursion_depth, 
                  Integer.MAX_VALUE, 
                  ctrl.pull_tight_accuracy, 
                  true, null);

            if (curr_ok_point != add_corner) return p_from_corner;

            neck_down_end_point = add_corner;
            }
         }

      PlaPointInt ok_point = r_board.insert_trace (
            neck_down_end_point, 
            p_to_corner, 
            neck_down_halfwidth, 
            p_layer, net_no_arr, 
            ctrl.trace_clearance_idx,
            ctrl.max_shove_trace_recursion_depth, 
            ctrl.max_shove_via_recursion_depth, 
            ctrl.max_spring_over_recursion_depth, 
            Integer.MAX_VALUE, 
            ctrl.pull_tight_accuracy, 
            true, null);
      
      return ok_point;
      }

   /**
    * Search the cheapest via masks containing p_from_layer and p_to_layer, so that a forced via is possible at p_location with
    * this mask and inserts the via. 
    * @return false, if no suitable via mask was found or if the algorithm failed.
    */
   private boolean insert_via_done(PlaPointInt p_location, int p_from_layer, int p_to_layer)
      {
      // no via necessary
      if (p_from_layer == p_to_layer) return true; 
      
      int from_layer;
      int to_layer;
      
      if (p_from_layer < p_to_layer)
         {
         // sort the input layers
         from_layer = p_from_layer;
         to_layer = p_to_layer;
         }
      else
         {
         from_layer = p_to_layer;
         to_layer = p_from_layer;
         }
      
      NetNosList net_no_arr = new NetNosList(ctrl.net_no);
      BrdViaInfo via_info = null;
      
      for (int index = 0; index < ctrl.via_rule.via_count(); ++index)
         {
         BrdViaInfo curr_via_info = ctrl.via_rule.get_via(index);
         LibPadstack curr_via_padstack = curr_via_info.get_padstack();

         if (curr_via_padstack.from_layer() > from_layer || curr_via_padstack.to_layer() < to_layer) continue;

         if (r_board.shove_via_algo.check(curr_via_info, p_location, net_no_arr, ctrl.max_shove_trace_recursion_depth, ctrl.max_shove_via_recursion_depth ))
            {
            via_info = curr_via_info;
            break;
            }
         }

      if (via_info == null)
         {
         System.out.print("InsertFoundConnectionAlgo: via mask not found for net ");
         System.out.println(ctrl.net_no);
         return false;
         }
      
      // insert the via
      if (! r_board.shove_via_algo.shove_via_insert(
            via_info, 
            p_location, 
            net_no_arr, 
            ctrl.trace_clearance_idx, 
            ctrl.trace_half_width, 
            ctrl.max_shove_trace_recursion_depth,
            ctrl.max_shove_via_recursion_depth ))
         {
         System.out.print("InsertFoundConnectionAlgo: forced via failed for net "+ctrl.net_no);
         return false;
         }
      
      return true;
      }
   }
