package freert.main;

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
   boolean system_plaf=true;

   public MainOptions(String p_args[])
      {
      for (int index = 0; index < p_args.length; ++index)
         {
         if (p_args[index].startsWith("-de"))
            {
            // the design file is provided
            if (p_args.length > index + 1 && !p_args[index + 1].startsWith("-"))
               {
               design_file_name = p_args[index + 1];
               }
            }
         else if (p_args[index].startsWith("-di"))
            {
            // the design directory is provided
            if (p_args.length > index + 1 && !p_args[index + 1].startsWith("-"))
               {
               design_dir_name = p_args[index + 1];
               }
            }
         else if (p_args[index].startsWith("-l"))
            {
            // the locale is provided
            if (p_args.length > index + 1 && p_args[index + 1].startsWith("d"))
               {
               options_locale = java.util.Locale.GERMAN;
               index++;
               }
            }
         else if (p_args[index].startsWith("-nosysplaf"))
            {
            system_plaf=false;
            }
         }
      }
   }
