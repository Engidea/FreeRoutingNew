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
 * Communication.java
 *
 * Created on 5. Juli 2004, 07:31
 */

package freert.host;


import java.io.IOException;
import java.io.ObjectInputStream;
import board.varie.IdGenerator;
import freert.spectra.DsnCoordinateTransform;
import freert.spectra.DsnParserInfo;
import freert.varie.UnitMeasure;

/**
 * Communication information to host systems or host design formats.
 * What is it , really ?
 *
 * @author alfons
 */
public final class HostCom implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final IdGenerator id_no_generator;
   
   // For coordinate transforms to a Specctra dsn file for example
   public final DsnCoordinateTransform coordinate_transform;

   // the original host unit measure, whatever that is
   public final UnitMeasure host_unit;

   // The resolution (1 / unit_factor) of the coordinate system, which is imported from the host system.
   public final int host_resolution;

   public final DsnParserInfo specctra_parser_info;

   
   public HostCom(UnitMeasure p_unit, int p_resolution, DsnParserInfo p_specctra_parser_info, DsnCoordinateTransform p_coordinate_transform, IdGenerator p_id_no_generator)
      {
      coordinate_transform = p_coordinate_transform;
      host_unit = p_unit;
      host_resolution = p_resolution;
      specctra_parser_info = p_specctra_parser_info;
      id_no_generator = p_id_no_generator;
      }

   public int new_id_no ()
      {
      return id_no_generator.new_no();
      }
   
   public boolean host_cad_exists()
      {
      return specctra_parser_info != null && specctra_parser_info.host_cad != null;
      }

   /**
    * Returns the resolution scaled to the input unit
    */
   public double get_resolution(UnitMeasure p_unit)
      {
      return UnitMeasure.scale(host_resolution, p_unit, host_unit);
      }

   private void readObject( ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      p_stream.defaultReadObject();
      }

   }
