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
 * WriteScopeParameter.java
 *
 * Created on 21. Juni 2004, 08:37
 */

package freert.spectra;

import gui.varie.IndentFileWriter;
import autoroute.ArtSettings;
import board.RoutingBoard;

/**
 * Default parameter type used while writing a Specctra dsn-file.
 *
 * @author alfons
 */
public class DsnWriteScopeParameter
   {
   public final RoutingBoard board;
   public final ArtSettings autoroute_settings;
   public final IndentFileWriter file;
   public final DsnCoordinateTransform coordinate_transform;
   public final boolean compat_mode;
   public final DsnIdentifier identifier_type;

   /**
    * Creates a new instance of WriteScopeParameter. If p_compat_mode is true, only standard speecctra dsb scopes are written, so
    * that any host system with an specctra interface can read them.
    */
   public DsnWriteScopeParameter(RoutingBoard p_board, autoroute.ArtSettings p_autoroute_settings, IndentFileWriter p_file, String p_string_quote, DsnCoordinateTransform p_coordinate_transform,
         boolean p_compat_mode)
      {
      board = p_board;
      autoroute_settings = p_autoroute_settings;
      file = p_file;
      coordinate_transform = p_coordinate_transform;
      compat_mode = p_compat_mode;
      String[] reserved_chars = { "(", ")", " ", ";", "-", "_" };
      identifier_type = new DsnIdentifier(reserved_chars, p_string_quote);
      }

   }
