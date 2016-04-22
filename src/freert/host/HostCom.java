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
import specctra.DsnCoordinateTransform;
import specctra.DsnParserInfo;
import board.varie.IdGenerator;
import datastructures.UnitMeasure;

/**
 * Communication information to host systems or host design formats.
 * What is it , really ?
 *
 * @author alfons
 */
public final class HostCom implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // For coordinate transforms to a Specctra dsn file for example
   public final DsnCoordinateTransform coordinate_transform;

   public final UnitMeasure unit;

   // The resolution (1 / unit_factor) of the coordinate system, which is imported from the host system.
   public final int resolution;

   public final DsnParserInfo specctra_parser_info;

   public final IdGenerator id_no_generator;
   
   public HostCom(UnitMeasure p_unit, int p_resolution, DsnParserInfo p_specctra_parser_info, DsnCoordinateTransform p_coordinate_transform, IdGenerator p_id_no_generator)
      {
      coordinate_transform = p_coordinate_transform;
      unit = p_unit;
      resolution = p_resolution;
      specctra_parser_info = p_specctra_parser_info;
      id_no_generator = p_id_no_generator;
      }

/*
   private boolean host_cad_is_eagle()
      {
      return specctra_parser_info != null && specctra_parser_info.host_cad != null && specctra_parser_info.host_cad.equalsIgnoreCase("CadSoft");
      }
*/
   
   public boolean host_cad_exists()
      {
      return specctra_parser_info != null && specctra_parser_info.host_cad != null;
      }

   /**
    * Returns the resolution scaled to the input unit
    */
   public double get_resolution(UnitMeasure p_unit)
      {
      return UnitMeasure.scale(resolution, p_unit, unit);
      }

   private void readObject( ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      p_stream.defaultReadObject();
      }

   }
