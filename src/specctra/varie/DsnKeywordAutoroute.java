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
 * AutorouteSettings.java
 *
 * Created on 1. Maerz 2007, 07:10
 *
 */
package specctra.varie;

import gui.varie.IndentFileWriter;
import specctra.DsnFileScanner;
import specctra.DsnIdentifier;
import specctra.DsnKeyword;
import specctra.DsnKeywordScope;
import specctra.DsnLayerStructure;
import specctra.JflexScanner;
import autoroute.ArtSettings;

/**
 * Hey, this has nothing to do with Dsn (spectra) it is bound to the rules parsing only !!
 * TODO Move it away from here, in a separate package
 * @author Alfons Wirtz
 */
public class DsnKeywordAutoroute
   {
   public static ArtSettings read_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      ArtSettings result = new ArtSettings(p_layer_structure.arr.length);
      boolean with_fanout = false;
      boolean with_autoroute = true;
      boolean with_postroute = true;
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
            System.out.println("DsnKeywordAutoroute.read_scope: IO error scanning file");
            return null;
            }
         if (next_token == null)
            {
            System.out.println("DsnKeywordAutoroute.read_scope: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.FANOUT)
               {
               with_fanout = DsnReadUtils.read_on_off_scope(p_scanner);
               }
            else if (next_token == DsnKeyword.AUTOROUTE)
               {
               with_autoroute = DsnReadUtils.read_on_off_scope(p_scanner);
               }
            else if (next_token == DsnKeyword.POSTROUTE)
               {
               with_postroute = DsnReadUtils.read_on_off_scope(p_scanner);
               }
            else if (next_token == DsnKeyword.VIAS)
               {
               result.vias_allowed = DsnReadUtils.read_on_off_scope(p_scanner);
               }
            else if (next_token == DsnKeyword.VIA_COSTS)
               {
               result.set_via_costs(DsnReadUtils.read_integer_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.PLANE_VIA_COSTS)
               {
               result.set_plane_via_costs(DsnReadUtils.read_integer_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.START_RIPUP_COSTS)
               {
               result.set_start_ripup_costs(DsnReadUtils.read_integer_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.START_PASS_NO)
               {
               result.pass_no_set(DsnReadUtils.read_integer_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.LAYER_RULE)
               {
               result = read_layer_rule(p_scanner, p_layer_structure, result);
               if (result == null)
                  {
                  return null;
                  }
               }
            else
               {
               DsnKeywordScope.skip_scope(p_scanner);
               }
            }
         }
      result.set_with_fanout(with_fanout);
      result.set_with_autoroute(with_autoroute);
      result.set_with_postroute(with_postroute);
      return result;
      }

   static ArtSettings read_layer_rule(JflexScanner p_scanner, DsnLayerStructure p_layer_structure, autoroute.ArtSettings p_settings)
      {
      p_scanner.yybegin(DsnFileScanner.NAME);
      Object next_token;
      try
         {
         next_token = p_scanner.next_token();
         }
      catch (java.io.IOException e)
         {
         System.out.println("DsnKeywordAutoroute.read_layer_rule: IO error scanning file");
         return null;
         }
      if (!(next_token instanceof String))
         {
         System.out.println("DsnKeywordAutoroute.read_layer_rule: String expected");
         return null;
         }
      int layer_no = p_layer_structure.get_no((String) next_token);
      if (layer_no < 0)
         {
         System.out.println("DsnKeywordAutoroute.read_layer_rule: layer not found");
         return null;
         }
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("DsnKeywordAutoroute.read_layer_rule: IO error scanning file");
            return null;
            }
         if (next_token == null)
            {
            System.out.println("DsnKeywordAutoroute.read_layer_rule: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.ACTIVE)
               {
               p_settings.set_layer_active(layer_no, DsnReadUtils.read_on_off_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.PREFERRED_DIRECTION)
               {
               try
                  {
                  boolean pref_dir_is_horizontal = true;
                  next_token = p_scanner.next_token();
                  if (next_token == DsnKeyword.VERTICAL)
                     {
                     pref_dir_is_horizontal = false;
                     }
                  else if (next_token != DsnKeyword.HORIZONTAL)
                     {
                     System.out.println("DsnKeywordAutoroute.read_layer_rule: unexpected key word");
                     return null;
                     }
                  p_settings.set_preferred_direction_is_horizontal(layer_no, pref_dir_is_horizontal);
                  next_token = p_scanner.next_token();
                  if (next_token != DsnKeyword.CLOSED_BRACKET)
                     {
                     System.out.println("DsnKeywordAutoroute.read_layer_rule: uclosing bracket expected");
                     return null;
                     }
                  }
               catch (java.io.IOException e)
                  {
                  System.out.println("DsnKeywordAutoroute.read_layer_rule: IO error scanning file");
                  return null;
                  }
               }
            else if (next_token == DsnKeyword.PREFERRED_DIRECTION_TRACE_COSTS)
               {
               p_settings.set_preferred_direction_trace_costs(layer_no, DsnReadUtils.read_float_scope(p_scanner));
               }
            else if (next_token == DsnKeyword.AGAINST_PREFERRED_DIRECTION_TRACE_COSTS)
               {
               p_settings.set_against_preferred_direction_trace_costs(layer_no, DsnReadUtils.read_float_scope(p_scanner));
               }
            else
               {
               DsnKeywordScope.skip_scope(p_scanner);
               }
            }
         }
      return p_settings;
      }

   public static void write_scope(IndentFileWriter p_file, ArtSettings p_settings, board.BrdLayerStructure p_layer_structure, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("autoroute_settings");
      p_file.new_line();
      p_file.write("(fanout ");
      if (p_settings.get_with_fanout())
         {
         p_file.write("on)");
         }
      else
         {
         p_file.write("off)");
         }
      p_file.new_line();
      p_file.write("(autoroute ");
      if (p_settings.get_with_autoroute())
         {
         p_file.write("on)");
         }
      else
         {
         p_file.write("off)");
         }
      p_file.new_line();
      p_file.write("(postroute ");
      if (p_settings.get_with_postroute())
         {
         p_file.write("on)");
         }
      else
         {
         p_file.write("off)");
         }
      p_file.new_line();
      p_file.write("(vias ");
      if (p_settings.vias_allowed)
         {
         p_file.write("on)");
         }
      else
         {
         p_file.write("off)");
         }
      p_file.new_line();
      p_file.write("(via_costs ");
         {
         Integer via_costs = p_settings.get_via_costs();
         p_file.write(via_costs.toString());
         }
      p_file.write(")");
      p_file.new_line();
      p_file.write("(plane_via_costs ");
         {
         Integer via_costs = p_settings.get_plane_via_costs();
         p_file.write(via_costs.toString());
         }
      p_file.write(")");
      p_file.new_line();
      p_file.write("(start_ripup_costs ");
         {
         Integer ripup_costs = p_settings.get_start_ripup_costs();
         p_file.write(ripup_costs.toString());
         }
      p_file.write(")");
      p_file.new_line();
      p_file.write("(start_pass_no ");
         {
         Integer pass_no = p_settings.pass_no_get();
         p_file.write(pass_no.toString());
         }
      p_file.write(")");
      for (int layer_idx = 0; layer_idx < p_layer_structure.size(); ++layer_idx)
         {
         board.BrdLayer curr_layer = p_layer_structure.get(layer_idx);
         p_file.start_scope();
         p_file.write("layer_rule ");
         p_identifier_type.write(curr_layer.name, p_file);
         p_file.new_line();
         p_file.write("(active ");
         if (p_settings.get_layer_active(layer_idx))
            {
            p_file.write("on)");
            }
         else
            {
            p_file.write("off)");
            }
         p_file.new_line();
         p_file.write("(preferred_direction ");
         if (p_settings.get_preferred_direction_is_horizontal(layer_idx))
            {
            p_file.write("horizontal)");
            }
         else
            {
            p_file.write("vertical)");
            }
         p_file.new_line();
         p_file.write("(preferred_direction_trace_costs ");
         Float trace_costs = (float) p_settings.get_preferred_direction_trace_costs(layer_idx);
         p_file.write(trace_costs.toString());
         p_file.write(")");
         p_file.new_line();
         p_file.write("(against_preferred_direction_trace_costs ");
         trace_costs = (float) p_settings.get_against_preferred_direction_trace_costs(layer_idx);
         p_file.write(trace_costs.toString());
         p_file.write(")");
         p_file.end_scope();
         }
      p_file.end_scope();
      }
   }
