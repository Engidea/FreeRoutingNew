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
 * BatchAutorouterThread.java
 *
 * Created on 25. April 2006, 07:58
 *
 */
package autoroute.batch;

import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentFloat;
import freert.varie.UnitMeasure;
import gui.varie.GuiResources;
import interactive.BrdActionThread;
import interactive.IteraBoard;
import java.awt.Color;
import autoroute.ArtSettings;
import board.varie.BrdValidate;

/**
 * Thread for the batch autoroute.
 * Possibly to be moved under the autoroute package
 * @author Alfons Wirtz
 */
public final class BatchAutorouteThread extends BrdActionThread
   {
   private static final String classname="BatchAutorouteThread.";
   
   private final BatchAutorouter batch_autorouter;
   private final BatchOptimize batch_optimize;
   private final GuiResources resources;
   
   
   public BatchAutorouteThread(IteraBoard p_board_handling)
      {
      super(p_board_handling,classname);
      
      ArtSettings autoroute_settings = p_board_handling.itera_settings.autoroute_settings;

      batch_autorouter = new BatchAutorouter(this, !autoroute_settings.get_with_fanout(), autoroute_settings.get_start_ripup_costs());
      batch_optimize = new BatchOptimize(this);
      
      resources = hdlg.newGuiResources("interactive.resources.InteractiveState");
      }

   
   private void postroute_try ()
      {
      if ( is_stop_requested() ) return;
      
      if ( hdlg.itera_settings.autoroute_settings.get_with_postroute() )
         {
         String opt_message = resources.getString("batch_optimizer") + " " + resources.getString("stop_message");
         hdlg.screen_messages.set_status_message(opt_message);

         batch_optimize.optimize_board();
         
         String curr_message = is_stop_requested() ? resources.getString("interrupted") : resources.getString("completed");

         String end_message = resources.getString("postroute") + " " + curr_message;
         hdlg.screen_messages.set_status_message(end_message);
         }
      }
   
   private void fanout_try ()
      {
      if ( ! hdlg.itera_settings.autoroute_settings.get_with_fanout() ) return;
      
      if ( hdlg.itera_settings.autoroute_settings.pass_no_get() > 1 ) return;
      
      BatchFanout fanout = new BatchFanout(this);
      
      fanout.fanout_board();
      }
   
   private void autoroute_try ()
      {
      if ( is_stop_requested() ) return;
      
      if ( ! hdlg.itera_settings.autoroute_settings.get_with_autoroute() ) return;
      
      batch_autorouter.autoroute_loop();
      }
   
   
   private void validate_try ()
      {
      // the idea is to sip the test on release version
      if ( hdlg.debug(Mdbg.AUTORT, Ldbg.RELEASE) ) return;

      if ( ! hdlg.get_routing_board().brd_rules.is_trace_snap_45() ) return;
   
      BrdValidate.multiple_of_45_degree("after autoroute: ", hdlg.get_routing_board());
      }
   
   @Override
   protected void thread_action()
      {
      try
         {
         boolean board_read_only_before = hdlg.set_board_read_only(true);
         boolean ratsnest_hidden_before = hdlg.get_ratsnest().hide();
         
         String start_message = resources.getString("batch_autorouter") + " " + resources.getString("stop_message");
         hdlg.screen_messages.set_status_message(start_message);

         fanout_try();
         
         autoroute_try();
         
         postroute_try();
         
         hdlg.set_board_read_only(board_read_only_before);
         hdlg.update_ratsnest();
         
         hdlg.screen_messages.clear();
         
         String curr_message = is_stop_requested() ? resources.getString("interrupted") : resources.getString("completed");
         Integer incomplete_count = hdlg.get_ratsnest().incomplete_count();
         String end_message = resources.getString("autoroute") + " " + curr_message + ", " + incomplete_count.toString() + " " + resources.getString("connections_not_found");
         hdlg.screen_messages.set_status_message(end_message);
         
         if (!ratsnest_hidden_before)
            {
            hdlg.get_ratsnest().show();
            }

         hdlg.get_panel().board_frame.refresh_windows();
         
         validate_try();
         }
      catch (Exception exc)
         {
         hdlg.userPrintln(classname+"thread_action exc ",exc);
         }
      }

   public void draw(java.awt.Graphics p_graphics)
      {
      PlaSegmentFloat curr_air_line = batch_autorouter.get_air_line();

      if (curr_air_line != null)
         {
         PlaPointFloat[] draw_line = new PlaPointFloat[2];
         draw_line[0] = curr_air_line.point_a;
         draw_line[1] = curr_air_line.point_b;
         
         // draw the incomplete
         Color draw_color = hdlg.gdi_context.get_incomplete_color();
         
         // problem with low resolution on Kicad 300;
         double draw_width = Math.min(hdlg.get_routing_board().host_com.get_resolution(UnitMeasure.UM) * 10, 300); 
         
         hdlg.gdi_context.draw(draw_line, draw_width, draw_color, p_graphics, 1);
         }
      
      PlaPointFloat current_opt_position = batch_optimize.get_current_position();

      if (current_opt_position != null)
         {
         final int draw_width = 1;
         int radius = 10 * hdlg.get_routing_board().brd_rules.get_default_trace_half_width(0);
         java.awt.Color draw_color = this.hdlg.gdi_context.get_incomplete_color();
         PlaPointFloat[] draw_points = new PlaPointFloat[2];
         draw_points[0] = new PlaPointFloat(current_opt_position.v_x - radius, current_opt_position.v_y - radius);
         draw_points[1] = new PlaPointFloat(current_opt_position.v_x + radius, current_opt_position.v_y + radius);
         this.hdlg.gdi_context.draw(draw_points, draw_width, draw_color, p_graphics, 1);
         draw_points[0] = new PlaPointFloat(current_opt_position.v_x + radius, current_opt_position.v_y - radius);
         draw_points[1] = new PlaPointFloat(current_opt_position.v_x - radius, current_opt_position.v_y + radius);
         this.hdlg.gdi_context.draw(draw_points, draw_width, draw_color, p_graphics, 1);
         this.hdlg.gdi_context.draw_circle(current_opt_position, radius, draw_width, draw_color, p_graphics, 1);
         }
      }

   }
