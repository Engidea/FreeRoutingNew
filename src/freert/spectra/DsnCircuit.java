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
 * Circuit.java
 *
 * Created on 30. Mai 2005, 06:30
 *
 */

package freert.spectra;

import freert.spectra.varie.DsnReadUtils;

/**
 *
 * @author Alfons Wirtz
 */
public class DsnCircuit
   {
   /**
    * Currently only the length matching rule is read from a circuit scope. If the scope does not contain a length matching rule,
    * nulll is returned.
    */
   public static DsnCircuitReadScopeResult read_scope(JflexScanner p_scanner)
      {
      Object next_token = null;
      double min_trace_length = 0;
      double max_trace_length = 0;
      java.util.Collection<String> use_via = new java.util.LinkedList<String>();
      java.util.Collection<String> use_layer = new java.util.LinkedList<String>();
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("Circuit.read_scope: IO error scanning file");
            return null;
            }
         if (next_token == null)
            {
            System.out.println("Circuit.read_scope: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.LENGTH)
               {
               DsnCircuitLengthMatch length_rule = read_length_scope(p_scanner);
               if (length_rule != null)
                  {
                  min_trace_length = length_rule.min_length;
                  max_trace_length = length_rule.max_length;
                  }
               }
            else if (next_token == DsnKeyword.USE_VIA)
               {
               use_via.addAll(DsnKeywordStructure.read_via_padstacks(p_scanner));
               }
            else if (next_token == DsnKeyword.USE_LAYER)
               {
               use_layer.addAll(DsnReadUtils.read_string_list_scope(p_scanner));
               }
            else
               {
               DsnKeywordScope.skip_scope(p_scanner);
               }
            }
         }
      return new DsnCircuitReadScopeResult(max_trace_length, min_trace_length, use_via, use_layer);
      }

   static DsnCircuitLengthMatch read_length_scope(JflexScanner p_scanner)
      {
      DsnCircuitLengthMatch result = null;
      double[] length_arr = new double[2];
      Object next_token = null;
      for (int i = 0; i < 2; ++i)
         {
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("Circuit.read_length_scope: IO error scanning file");
            return null;
            }
         if (next_token instanceof Double)
            {
            length_arr[i] = ((Double) next_token).doubleValue();
            }
         else if (next_token instanceof Integer)
            {
            length_arr[i] = ((Integer) next_token).intValue();
            }
         else
            {
            System.out.println("Circuit.read_length_scope: number expected");
            return null;
            }
         }
      result = new DsnCircuitLengthMatch(length_arr[0], length_arr[1]);
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("Circuit.read_length_scope: IO error scanning file");
            return null;
            }
         if (next_token == null)
            {
            System.out.println("Circuit.read_length_scope: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            DsnKeywordScope.skip_scope(p_scanner);
            }
         }
      return result;
      }
   }
