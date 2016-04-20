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
 * ScopeKeyword.java
 *
 * Created on 12. Mai 2004, 08:47
 */

package specctra;

/**
 * Keywords defining a scope object
 * @author  alfons
 */

public class DsnKeywordScope extends DsnKeyword
   {
   /**
    * Subclasses may create this
    * @param p_name
    */
   protected DsnKeywordScope(String p_name)
      {
      super(p_name);
      }

   /**
    * Sips the current scope while reading a dsn file. Returns false, if no legal scope was found.
    */
   public static boolean skip_scope(JflexScanner p_scanner)
      {
      int open_bracked_count = 1;
      while (open_bracked_count > 0)
         {
         p_scanner.yybegin(DsnFileScanner.NAME);
         Object curr_token = null;
         try
            {
            curr_token = p_scanner.next_token();
            }
         catch (Exception e)
            {
            System.err.println("ScopeKeyword.skip_scope: Error while scanning file");
            System.err.println(e);
            return false;
            }
         if (curr_token == null)
            {
            return false; // end of file
            }
         if (curr_token == DsnKeyword.OPEN_BRACKET)
            {
            ++open_bracked_count;
            }
         else if (curr_token == DsnKeyword.CLOSED_BRACKET)
            {
            --open_bracked_count;
            }
         }
      return true;
      }

   /**
    * Reads the next scope of this keyword from dsn file.
    */
   public boolean read_scope(DsnReadScopeParameters p_par)
      {
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_par.scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("ScopeKeyword.read_scope: IO error scanning file");
            System.out.println(e);
            return false;
            }
         if (next_token == null)
            {
            // end of file
            return true;
            }
         if (next_token == CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == OPEN_BRACKET)
            {
            DsnKeywordScope next_scope;
            // a new scope is expected
            if (next_token instanceof DsnKeywordScope)
               {
               next_scope = (DsnKeywordScope) next_token;
               if (!next_scope.read_scope(p_par))
                  {
                  return false;
                  }

               }
            else
               {
               // skip unknown scope
               skip_scope(p_par.scanner);
               }

            }
         }
      return true;
      }
   }
