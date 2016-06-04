package freert.spectra;
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

import freert.spectra.varie.DsnWriteResolution;

/**
 * Information from the parser scope in a Specctra-dsn-file. The fields are optional and may be null.
 */
public final class DsnParserInfo implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // Character for quoting strings in a dsn-File. 
   public final String string_quote;
   public final String host_cad;
   public final String host_version;
   public final java.util.Collection<String[]> constants;
   public final DsnWriteResolution write_resolution;
   public final boolean dsn_file_generated_by_host;

   public DsnParserInfo ( DsnReadScopeParameters p_par )
      {
      string_quote = p_par.string_quote;
      host_cad = p_par.host_cad;
      host_version = p_par.host_version;
      constants = p_par.constants;
      write_resolution = p_par.write_resolution;
      dsn_file_generated_by_host = p_par.dsn_file_generated_by_host;
      }
   }
