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
 * InputDsnFile.java
 *
 * Created on 10. Mai 2004, 07:43
 */
package freert.spectra.varie;

import java.util.Collection;
import java.util.LinkedList;
import freert.spectra.DsnFileScanner;
import freert.spectra.DsnKeyword;
import freert.spectra.DsnKeywordScope;
import freert.spectra.JflexScanner;

/**
 * @author alfons
 */
public class DsnReadUtils
   {
   public static final String CLASS_CLEARANCE_SEPARATOR = "-";



   public static boolean read_on_off_scope(JflexScanner p_scanner)
      {
      try
         {
         Object next_token = p_scanner.next_token();
         boolean result = false;
         if (next_token == DsnKeyword.ON)
            {
            result = true;
            }
         else if (next_token != DsnKeyword.OFF)
            {
            System.out.println("DsnFile.read_boolean: Keyword.OFF expected");
            }
         DsnKeywordScope.skip_scope(p_scanner);
         return result;
         }
      catch (java.io.IOException e)
         {
         System.out.println("DsnFile.read_boolean: IO error scanning file");
         return false;
         }
      }

   public static int read_integer_scope(JflexScanner p_scanner)
      {
      try
         {
         int value;
         Object next_token = p_scanner.next_token();
         if (next_token instanceof Integer)
            {
            value = ((Integer) next_token).intValue();
            }
         else
            {
            System.err.println("DsnFile.read_integer_scope: number expected");
            return 0;
            }
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.err.println("DsnFile.read_integer_scope: closing bracket expected");
            return 0;
            }
         return value;
         }
      catch (java.io.IOException e)
         {
         System.err.println("DsnFile.read_integer_scope: IO error scanning file");
         return 0;
         }
      }

   public static double read_float_scope(JflexScanner p_scanner)
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
            System.err.println("DsnFile.read_float_scope: number expected");
            return 0;
            }
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.err.println("DsnFile.read_float_scope: closing bracket expected");
            return 0;
            }
         return value;
         }
      catch (java.io.IOException e)
         {
         System.err.println("DsnFile.read_float_scope: IO error scanning file");
         return 0;
         }
      }

   public static String read_string_scope(JflexScanner p_scanner)
      {
      try
         {
         p_scanner.yybegin(DsnFileScanner.NAME);
         Object next_token = p_scanner.next_token();
         if (!(next_token instanceof String))
            {
            System.err.println("DsnFile:read_string_scope: String expected");
            return null;
            }
         String result = (String) next_token;
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.err.println("DsnFile.read_string_scope: closing bracket expected");
            }
         return result;
         }
      catch (java.io.IOException e)
         {
         System.err.println("DsnFile.read_string_scope: IO error scanning file");
         return null;
         }
      }

   public static Collection<String> read_string_list_scope(JflexScanner p_scanner)
      {
      Collection<String> result = new LinkedList<String>();
      try
         {
         for (;;)
            {
            p_scanner.yybegin(DsnFileScanner.NAME);
            Object next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            if (!(next_token instanceof String))
               {
               System.out.println("DsnFileread_string_list_scope: string expected");
               return null;
               }
            result.add((String) next_token);
            }
         }
      catch (java.io.IOException e)
         {
         System.out.println("DsnFile.read_string_list_scope: IO error scanning file");
         }
      return result;
      }

   }
