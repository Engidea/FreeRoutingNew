package interactive.state;

/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.rules.RuleNets;
import freert.rules.RuleViaInfoList;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.IteraClearanceViolations;
import interactive.IteraRoute;
import interactive.LogfileScope;
import java.util.Iterator;
import java.util.Set;
import main.Ldbg;
import main.Mdbg;
import board.infos.BrdItemViolation;
import board.items.BrdItem;

/**
 * 
 * @author damiano
 *
 */
public final class StateRepairClearanceViolation extends StateInteractive
   {
   private static final String classname="StateRepairClearanceViolation.";
   
   private static final int SHOVE_TRACE_WIDTH=7;

   
   StateRepairClearanceViolation (  StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super( p_parent_state, p_board_handling, p_logfile);
      }
   
   
   private IteraRoute newIteraRoute (PlaPointInt start_point, int layer_no)
      {
      int[] shove_trace_width_arr = new int[r_brd.get_layer_count()];
      boolean[] layer_active_arr = new boolean[shove_trace_width_arr.length];
      
      for (int index = 0; index < shove_trace_width_arr.length; ++index)
         {
         shove_trace_width_arr[index] = SHOVE_TRACE_WIDTH;
         layer_active_arr[index] = true;
         }
      
      int[] route_net_no_arr = new int[1];
      route_net_no_arr[0] = RuleNets.HIDDEN_NET_NO;

      return new IteraRoute(start_point, 
            layer_no, 
            shove_trace_width_arr, 
            layer_active_arr, 
            route_net_no_arr, 
            0, 
            RuleViaInfoList.EMPTY, 
            true,
            null, 
            null, 
            r_brd, 
            false, 
            false, 
            i_brd.itera_settings);
      }
   
   
   private void repair ( BrdItemViolation a_viol )
      {
      PlaPointFloat start_float = a_viol.shape.centre_of_gravity();
      int layer_no = a_viol.layer_no;
      
      IteraRoute router = newIteraRoute(start_float.round(), layer_no);
      
      // make the situation restorable by undo
//      r_board.generate_snapshot();
      
      // Delayed till here because otherwise the mouse might have been only clicked for selecting and not pressed for moving.
      actlog_start_scope(LogfileScope.MAKING_SPACE, start_float);
      
      PlaPointFloat end_float = start_float.add(new PlaPointFloat(10,10));
      
      router.next_corner(end_float);

      PlaPoint route_end = router.get_last_corner();
      
      i_brd.move_mouse(route_end.to_float());
      
      i_brd.recalculate_length_violations();
      
      i_brd.repaint();

      r_brd.remove_items_unfixed(r_brd.get_connectable_items(RuleNets.HIDDEN_NET_NO));
      }
   
   /**
    * Not as easy as it seems...
    * Beside actually shoving you need to remember that the violations change once you shove one
    * So, you have to recalculate them amd pick up the first that has not one of the items shoved before
    * @param p_items_list
    */
   public void repair ( Set<BrdItem> p_items_list )
      {
      IteraClearanceViolations clearance_violations = new IteraClearanceViolations(p_items_list);

      
      Iterator<BrdItemViolation> iter = clearance_violations.violation_list.iterator();
      
      if ( ! iter.hasNext() ) return;

      int violation_count = clearance_violations.violation_list.size();
      
      if ( debug(Mdbg.CLRVIOL, Ldbg.DEBUG)) i_brd.userPrintln(classname+"repair: violation_count="+violation_count);

      BrdItemViolation a_viol = iter.next();
      
      if ( debug(Mdbg.CLRVIOL, Ldbg.FINE)) 
         {
         StringBuilder builder = new StringBuilder(500);
         builder.append("violation center"+a_viol.shape.centre_of_gravity());
         builder.append(" min_radius="+a_viol.shape.min_width());
         
         i_brd.userPrintln(builder.toString());
         }

      repair (a_viol);
      }
   
   
   }
