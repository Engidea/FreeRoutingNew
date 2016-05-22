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
 * Rule.java
 *
 * Created on 1. Juni 2004, 09:27
 */

package freert.spectra;

import java.util.Collection;
import java.util.LinkedList;
import freert.rules.BoardRules;
import board.BrdLayer;

/**
 * Class for reading and writing rule scopes from dsn-files.
 *
 * @author Alfons Wirtz
 */
public abstract class DsnRule
   {
   /**
    * Returns a collection of objects of class Rule.
    */
   public static Collection<DsnRule> read_scope(JflexScanner p_scanner)
      {
      Collection<DsnRule> result = new LinkedList<DsnRule>();
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("Rule.read_scope: IO error scanning file");
            System.out.println(e);
            return null;
            }
         if (next_token == null)
            {
            System.out.println("Rule.read_scope: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            DsnRule curr_rule = null;
            if (next_token == DsnKeyword.WIDTH)
               {
               curr_rule = read_width_rule(p_scanner);
               }
            else if (next_token == DsnKeyword.CLEARANCE)
               {
               curr_rule = read_clearance_rule(p_scanner);
               }
            else
               {
               DsnKeywordScope.skip_scope(p_scanner);
               }
            if (curr_rule != null)
               {
               result.add(curr_rule);
               }

            }
         }
      return result;
      }

   /**
    * Reads a LayerRule from dsn-file.
    */
   public static DsnRuleLayer read_layer_rule_scope(JflexScanner p_scanner)
      {
      try
         {
         Collection<String> layer_names = new LinkedList<String>();
         Collection<DsnRule> rule_list = new LinkedList<DsnRule>();
         for (;;)
            {
            p_scanner.yybegin(DsnFileScanner.LAYER_NAME);
            Object next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.OPEN_BRACKET)
               {
               break;
               }
            if (!(next_token instanceof String))
               {

               System.out.println("Rule.read_layer_rule_scope: string expected");
               return null;
               }
            layer_names.add((String) next_token);
            }
         for (;;)
            {
            Object next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            if (next_token != DsnKeyword.RULE)
               {

               System.out.println("Rule.read_layer_rule_scope: rule expected");
               return null;
               }
            rule_list.addAll(read_scope(p_scanner));
            }
         return new DsnRuleLayer(layer_names, rule_list);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Rule.read_layer_rule_scope: IO error scanning file");
         return null;
         }
      }

   public static DsnRuleWidth read_width_rule(JflexScanner p_scanner)
      {
      try
         {
         double value;
         Object next_token = p_scanner.next_token();
         if (next_token instanceof Double)
            {
            value = ((Double) next_token).doubleValue();
            }
         else if (next_token instanceof Integer)
            {
            value = ((Integer) next_token).doubleValue();
            }
         else
            {
            System.out.println("Rule.read_width_rule: number expected");
            return null;
            }
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.out.println("Rule.read_width_rule: closing bracket expected");
            return null;
            }
         return new DsnRuleWidth(value);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Rule.read_width_rule: IO error scanning file");
         return null;
         }
      }

   public static void write_scope(freert.rules.NetClass p_net_class, DsnWriteScopeParameter p_par) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("rule");

      // write the trace width
      int default_trace_half_width = p_net_class.get_trace_half_width(0);
      double trace_width = 2 * p_par.coordinate_transform.board_to_dsn(default_trace_half_width);
      p_par.file.new_line();
      p_par.file.write("(width ");
      p_par.file.write((new Double(trace_width)).toString());
      p_par.file.write(")");
      p_par.file.end_scope();
      for (int index = 1; index < p_par.board.layer_structure.size(); ++index)
         {
         if (p_net_class.get_trace_half_width(index) != default_trace_half_width)
            {
            write_layer_rule(p_net_class, index, p_par);
            }
         }
      }

   private static void write_layer_rule(freert.rules.NetClass p_net_class, int p_layer_no, DsnWriteScopeParameter p_par) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("layer_rule ");

      BrdLayer curr_board_layer = p_par.board.layer_structure.get(p_layer_no);

      p_par.file.write(curr_board_layer.name);
      p_par.file.start_scope();
      p_par.file.write("rule ");

      int curr_trace_half_width = p_net_class.get_trace_half_width(p_layer_no);

      // write the trace width
      double trace_width = 2 * p_par.coordinate_transform.board_to_dsn(curr_trace_half_width);
      p_par.file.new_line();
      p_par.file.write("(width ");
      p_par.file.write((new Double(trace_width)).toString());
      p_par.file.write(") ");
      p_par.file.end_scope();
      p_par.file.end_scope();
      }

   /**
    * Writes the default rule as a scope to an output dsn-file.
    */
   public static void write_default_rule(DsnWriteScopeParameter p_par, int p_layer) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("rule");
      // write the trace width
      double trace_width = 2 * p_par.coordinate_transform.board_to_dsn(p_par.board.brd_rules.get_default_net_class().get_trace_half_width(0));
      p_par.file.new_line();
      p_par.file.write("(width ");
      p_par.file.write((new Double(trace_width)).toString());
      p_par.file.write(")");
      // write the default clearance rule
      int default_cl_no = BoardRules.clearance_default_idx;
      int default_board_clearance = p_par.board.brd_rules.clearance_matrix.value_at(default_cl_no, default_cl_no, p_layer);
      double default_clearance = p_par.coordinate_transform.board_to_dsn(default_board_clearance);
      p_par.file.new_line();
      p_par.file.write("(clear ");
      p_par.file.write((new Double(default_clearance)).toString());
      p_par.file.write(")");
      // write the Smd_to_turn_gap
      Double smd_to_turn_dist = p_par.coordinate_transform.board_to_dsn(p_par.board.brd_rules.get_pin_edge_to_turn_dist());
      p_par.file.new_line();
      p_par.file.write("(clear ");
      p_par.file.write(smd_to_turn_dist.toString());
      p_par.file.write(" (type smd_to_turn_gap))");
      int cl_count = p_par.board.brd_rules.clearance_matrix.get_class_count();
      for (int i = 1; i <= cl_count; ++i)
         {
         write_clearance_rules(p_par, p_layer, i, cl_count, default_board_clearance);
         }
      p_par.file.end_scope();
      }

   /**
    * Write the clearance rules, which are different from the default clearance.
    */
   private static void write_clearance_rules(DsnWriteScopeParameter p_par, int p_layer, int p_cl_class, int p_max_cl_class, int p_default_clearance) throws java.io.IOException
      {
      freert.rules.ClearanceMatrix cl_matrix = p_par.board.brd_rules.clearance_matrix;
      for (int i = p_cl_class; i < p_max_cl_class; ++i)
         {
         int curr_board_clearance = cl_matrix.value_at(p_cl_class, i, p_layer);
         if (curr_board_clearance == p_default_clearance)
            {
            continue;
            }
         double curr_clearance = p_par.coordinate_transform.board_to_dsn(curr_board_clearance);
         p_par.file.new_line();
         p_par.file.write("(clear ");
         p_par.file.write((new Double(curr_clearance)).toString());
         p_par.file.write(" (type ");
         p_par.identifier_type.write(cl_matrix.get_name(p_cl_class), p_par.file);
         p_par.file.write("_");
         p_par.identifier_type.write(cl_matrix.get_name(i), p_par.file);
         p_par.file.write("))");
         }
      }

   public static DsnRuleClearance read_clearance_rule(JflexScanner p_scanner)
      {
      try
         {
         double value;
         Object next_token = p_scanner.next_token();
         if (next_token instanceof Double)
            {
            value = ((Double) next_token).doubleValue();
            }
         else if (next_token instanceof Integer)
            {
            value = ((Integer) next_token).doubleValue();
            }
         else
            {
            System.err.println("Rule.read_clearance_rule: number expected");
            return null;
            }
         Collection<String> class_pairs = new LinkedList<String>();
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            if (next_token != DsnKeyword.OPEN_BRACKET)
               {
               System.out.println("Rule.read_clearance_rule: ( expected");
               return null;
               }
            next_token = p_scanner.next_token();
            if (next_token != DsnKeyword.TYPE)
               {
               System.out.println("Rule.read_clearance_rule: type expected");
               return null;
               }
            for (;;)
               {
               p_scanner.yybegin(DsnFileScanner.IGNORE_QUOTE);
               next_token = p_scanner.next_token();
               if (next_token == DsnKeyword.CLOSED_BRACKET)
                  {
                  break;
                  }
               if (!(next_token instanceof String))
                  {
                  System.out.println("Rule.read_clearance_rule: string expected");
                  return null;
                  }
               class_pairs.add((String) next_token);
               }
            next_token = p_scanner.next_token();
            if (next_token != DsnKeyword.CLOSED_BRACKET)
               {
               System.out.println("Rule.read_clearance_rule: closing bracket expected");
               return null;
               }
            }
         return new DsnRuleClearance(value, class_pairs);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Rule.read_clearance_rule: IO error scanning file");
         return null;
         }

      }

   static public void write_item_clearance_class(String p_name, gui.varie.IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.new_line();
      p_file.write("(clearance_class ");
      p_identifier_type.write(p_name, p_file);
      p_file.write(")");
      }



   }
