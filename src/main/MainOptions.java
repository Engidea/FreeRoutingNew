package main;

import java.util.Locale;

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

/**
 * Parse main options and provide clean results
 * 
 * @author damiano
 *
 */
public class MainOptions
   {
   String design_file_name = null;
   String design_dir_name = null;
   Locale options_locale = null;

   public MainOptions(String p_args[])
      {
      for (int i = 0; i < p_args.length; ++i)
         {
         if (p_args[i].startsWith("-de"))
            {
            // the design file is provided
            if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-"))
               {
               design_file_name = p_args[i + 1];
               }
            }
         else if (p_args[i].startsWith("-di"))
            {
            // the design directory is provided
            if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-"))
               {
               design_dir_name = p_args[i + 1];
               }
            }
         else if (p_args[i].startsWith("-l"))
            {
            // the locale is provided
            if (p_args.length > i + 1 && p_args[i + 1].startsWith("d"))
               {
               options_locale = java.util.Locale.GERMAN;
               }
            }
         else if (p_args[i].startsWith("-s"))
            {
            //session_file_option = true;
            }
         }
      }
   }
