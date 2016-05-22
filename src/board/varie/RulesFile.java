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
 * RulesFile.java
 *
 * Created on 18. Juli 2005, 07:07
 *
 */

package board.varie;

import freert.spectra.DsnCoordinateTransform;
import freert.spectra.DsnFileScanner;
import freert.spectra.DsnKeyword;
import freert.spectra.DsnKeywordLibrary;
import freert.spectra.DsnKeywordNetwork;
import freert.spectra.DsnKeywordScope;
import freert.spectra.DsnKeywordStructure;
import freert.spectra.DsnLayerStructure;
import freert.spectra.DsnNetClass;
import freert.spectra.DsnRule;
import freert.spectra.DsnRuleClearance;
import freert.spectra.DsnRuleWidth;
import freert.spectra.DsnWriteScopeParameter;
import freert.spectra.JflexScanner;
import freert.spectra.varie.DsnKeywordAutoroute;
import gui.varie.IndentFileWriter;
import java.io.InputStreamReader;
import board.RoutingBoard;

/**
 * File for saving the board rules, so that they can be restored after the Board
 * is creates anew from the host system.
 *
 * @author Alfons Wirtz
 */
public class RulesFile
   {

   public static void write(interactive.IteraBoard p_board_handling, java.io.OutputStream p_output_stream, String p_design_name)
      {
      IndentFileWriter output_file = new IndentFileWriter(p_output_stream);
      RoutingBoard routing_board = p_board_handling.get_routing_board();
      DsnWriteScopeParameter write_scope_parameter = new DsnWriteScopeParameter(routing_board, p_board_handling.itera_settings.autoroute_settings, output_file,
            routing_board.host_com.specctra_parser_info.string_quote, routing_board.host_com.coordinate_transform, false);
      try
         {
         write_rules(write_scope_parameter, p_design_name);
         }
      catch (java.io.IOException e)
         {
         System.out.println("unable to write rules to file");
         }
      try
         {
         output_file.close();
         }
      catch (java.io.IOException e)
         {
         System.out.println("unable to close rules file");
         }
      }

   public static boolean read(java.io.InputStream p_input_stream, String p_design_name, interactive.IteraBoard p_board_handling)
      {
      RoutingBoard routing_board = p_board_handling.get_routing_board();
      JflexScanner scanner = new DsnFileScanner(new InputStreamReader(p_input_stream));
      try
         {
         Object curr_token = scanner.next_token();
         if (curr_token != DsnKeyword.OPEN_BRACKET)
            {
            System.out.println("RulesFile.read: open bracket expected");
            return false;
            }
         curr_token = scanner.next_token();
         if (curr_token != DsnKeyword.RULES)
            {
            System.out.println("RulesFile.read: keyword rules expected");
            return false;
            }
         curr_token = scanner.next_token();
         if (curr_token != DsnKeyword.PCB_SCOPE)
            {
            System.out.println("RulesFile.read: keyword pcb expected");
            return false;
            }
         scanner.yybegin(DsnFileScanner.NAME);
         curr_token = scanner.next_token();
         if (!(curr_token instanceof String) || !((String) curr_token).equals(p_design_name))
            {
            System.out.println("RulesFile.read: design_name not matching");
            return false;
            }
         }
      catch (java.io.IOException e)
         {
         System.out.println("RulesFile.read: IO error scanning file");
         return false;
         }
      DsnLayerStructure layer_structure = new DsnLayerStructure(routing_board.layer_structure);
      DsnCoordinateTransform coordinate_transform = routing_board.host_com.coordinate_transform;
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("RulesFile.read: IO error scanning file");
            return false;
            }
         if (next_token == null)
            {
            System.out.println("Structure.read_scope: unexpected end of file");
            return false;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         boolean read_ok = true;
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.RULE)
               {
               add_rules(DsnRule.read_scope(scanner), routing_board, null);
               }
            else if (next_token == DsnKeyword.LAYER)
               {
               add_layer_rules(scanner, routing_board);
               }
            else if (next_token == DsnKeyword.PADSTACK)
               {
               DsnKeywordLibrary.read_padstack_scope(scanner, layer_structure, coordinate_transform, routing_board.brd_library.padstacks);
               }
            else if (next_token == DsnKeyword.VIA)
               {
               read_via_info(scanner, routing_board);
               }
            else if (next_token == DsnKeyword.VIA_RULE)
               {
               read_via_rule(scanner, routing_board);
               }
            else if (next_token == DsnKeyword.CLASS)
               {
               read_net_class(scanner, layer_structure, routing_board);
               }
            else if (next_token == DsnKeyword.SNAP_ANGLE)
               {
               TraceAngleRestriction snap_angle = DsnKeywordStructure.read_snap_angle(scanner);
               if (snap_angle != null)
                  {
                  routing_board.brd_rules.set_trace_snap_angle(snap_angle);
                  }
               }
            else if (next_token == DsnKeyword.AUTOROUTE_SETTINGS)
               {
               autoroute.ArtSettings autoroute_settings = DsnKeywordAutoroute.read_scope(scanner, layer_structure);
               if (autoroute_settings != null)
                  {
                  p_board_handling.itera_settings.autoroute_settings = autoroute_settings;
                  }
               }
            else
               {
               DsnKeywordScope.skip_scope(scanner);
               }
            }
         if (!read_ok)
            {
            return false;
            }
         }
      return true;
      }

   private static void write_rules(DsnWriteScopeParameter p_par, String p_design_name) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("rules PCB ");
      p_par.file.write(p_design_name);
      DsnKeywordStructure.write_snap_angle(p_par.file, p_par.board.brd_rules.get_trace_snap_angle());
      DsnKeywordAutoroute.write_scope(p_par.file, p_par.autoroute_settings, p_par.board.layer_structure, p_par.identifier_type);
      // write the default rule using 0 as default layer.
      DsnRule.write_default_rule(p_par, 0);
      // write the via padstacks
      for (int i = 1; i <= p_par.board.brd_library.padstacks.count(); ++i)
         {
         freert.library.LibPadstack curr_padstack = p_par.board.brd_library.padstacks.get(i);
         if (p_par.board.brd_library.get_via_padstack(curr_padstack.pads_name) != null)
            {
            DsnKeywordLibrary.write_padstack_scope(p_par, curr_padstack);
            }
         }
      DsnKeywordNetwork.write_via_infos(p_par.board.brd_rules, p_par.file, p_par.identifier_type);
      DsnKeywordNetwork.write_via_rules(p_par.board.brd_rules, p_par.file, p_par.identifier_type);
      DsnKeywordNetwork.write_net_classes(p_par);
      p_par.file.end_scope();
      }

   private static void add_rules(java.util.Collection<DsnRule> p_rules, RoutingBoard p_board, String p_layer_name)
      {
      int layer_no = -1;
      if (p_layer_name != null)
         {
         layer_no = p_board.layer_structure.get_no(p_layer_name);
         if (layer_no < 0)
            {
            System.out.println("RulesFile.add_rules: layer not found");
            }
         }
      DsnCoordinateTransform coordinate_transform = p_board.host_com.coordinate_transform;
      String string_quote = p_board.host_com.specctra_parser_info.string_quote;
      for (DsnRule curr_rule : p_rules)
         {
         if (curr_rule instanceof DsnRuleWidth)
            {
            double wire_width = ((DsnRuleWidth) curr_rule).value;
            int trace_halfwidth = (int) Math.round(coordinate_transform.dsn_to_board(wire_width) / 2);
            if (layer_no < 0)
               {
               p_board.brd_rules.set_default_trace_half_widths(trace_halfwidth);
               }
            else
               {
               p_board.brd_rules.set_default_trace_half_width(layer_no, trace_halfwidth);
               }
            }
         else if (curr_rule instanceof DsnRuleClearance)
            {
            DsnKeywordStructure.set_clearance_rule((DsnRuleClearance) curr_rule, layer_no, coordinate_transform, p_board.brd_rules, string_quote);
            }
         }
      }

   private static boolean add_layer_rules(JflexScanner p_scanner, RoutingBoard p_board)
      {
      try
         {
         Object next_token = p_scanner.next_token();
         if (!(next_token instanceof String))
            {
            System.out.println("RulesFile.add_layer_rules: String expected");
            return false;
            }
         String layer_string = (String) next_token;
         next_token = p_scanner.next_token();
         while (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            if (next_token != DsnKeyword.OPEN_BRACKET)
               {
               System.out.println("RulesFile.add_layer_rules: ( expected");
               return false;
               }
            next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.RULE)
               {
               java.util.Collection<DsnRule> curr_rules = DsnRule.read_scope(p_scanner);
               add_rules(curr_rules, p_board, layer_string);
               }
            else
               {
               DsnKeywordScope.skip_scope(p_scanner);
               }
            next_token = p_scanner.next_token();
            }
         return true;
         }
      catch (java.io.IOException e)
         {
         System.out.println("RulesFile.add_layer_rules: IO error scanning file");
         return false;
         }
      }

   private static boolean read_via_info(JflexScanner p_scanner, RoutingBoard p_board)
      {
      board.infos.BrdViaInfo curr_via_info = DsnKeywordNetwork.read_via_info(p_scanner, p_board);
      if (curr_via_info == null)
         {
         return false;
         }
      board.infos.BrdViaInfo existing_via = p_board.brd_rules.via_infos.get(curr_via_info.get_name());
      if (existing_via != null)
         {
         // replace existing via info
         p_board.brd_rules.via_infos.remove(existing_via);
         }
      p_board.brd_rules.via_infos.add(curr_via_info);
      return true;
      }

   private static boolean read_via_rule(JflexScanner p_scanner, RoutingBoard p_board)
      {
      java.util.Collection<String> via_rule = DsnKeywordNetwork.read_via_rule(p_scanner, p_board);
      if (via_rule == null)
         {
         return false;
         }
      DsnKeywordNetwork.add_via_rule(via_rule, p_board);
      return true;
      }

   private static boolean read_net_class(JflexScanner p_scanner, DsnLayerStructure p_layer_structure, RoutingBoard p_board)
      {
      DsnNetClass curr_class = DsnNetClass.read_scope(p_scanner);
      if (curr_class == null)
         {
         return false;
         }
      DsnKeywordNetwork.insert_net_class(curr_class, p_layer_structure, p_board, p_board.host_com.coordinate_transform, false);
      return true;
      }
   }
