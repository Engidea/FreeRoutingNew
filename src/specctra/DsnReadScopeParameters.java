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
 * ReadScopeParameter.java
 *
 * Created on 21. Juni 2004, 08:28
 */

package specctra;

import freert.varie.UnitMeasure;
import interactive.IteraBoard;
import java.util.Collection;
import java.util.LinkedList;
import specctra.varie.DsnWriteResolution;
import autoroute.ArtSettings;
import board.varie.IdGenerator;
import board.varie.TraceAngleRestriction;

/**
 * Default parameter type used while reading a Specctra dsn-file.
 * @author alfons
 */
public final class DsnReadScopeParameters
   {
   public final JflexScanner scanner;

   final IteraBoard i_board;
   final DsnNetList netlist = new DsnNetList();

   final IdGenerator item_id_no_generator;

   // The plane cannot be inserted directly into the boards, because the layers may not be read completely.
   final Collection<DsnPlaneInfo> plane_list = new LinkedList<DsnPlaneInfo>();
   final Collection<String[]> constants = new LinkedList<String[]>();

   // Component placement information. 
   // It is filled while reading the placement scope and can be evaluated after reading the library and network scope.
   final Collection<DsnComponentPlacement> placement_list = new LinkedList<DsnComponentPlacement>();

   // The names of the via padstacks filled while reading the structure scope and evaluated after reading the library scope.
   Collection<String> via_padstack_names = null;

   boolean via_at_smd_allowed = false;        // damiano: should really pick up this from kicad proper
   TraceAngleRestriction snap_angle = null;

   // The logical parts are used for pin and gate swaw 
   Collection<DsnLogicalPartMapping> logical_part_mappings = new java.util.LinkedList<DsnLogicalPartMapping>();
   Collection<DsnLogicalPart> logical_parts = new java.util.LinkedList<DsnLogicalPart>();

   // The following objects are from the parser scope
   String string_quote = "\"";
   String host_cad = null;
   String host_version = null;

   boolean dsn_file_generated_by_host = true;

   boolean board_outline_ok = true;

   DsnWriteResolution write_resolution = null;

   // The following objects will be initialized when the structure scope is read
   DsnCoordinateTransform coordinate_transform = null;
   DsnLayerStructure layer_structure = null;
   
   ArtSettings autoroute_settings = null;

   // The following two are read from dsn and then used to create the one used by freeroute
   // note that the two values should be overwritten on import..
   // ideally, I wish UM and a subdivision of 10
   UnitMeasure dsn_unit_meas;
   // how much to further divide the unit measure, ideally 10 parts if Unit is UM
   int dsn_resolution;

   DsnReadScopeParameters(JflexScanner p_scanner, IteraBoard p_itera_board,  IdGenerator p_item_id_no_generator )
      {
      scanner = p_scanner;
      i_board = p_itera_board;
      item_id_no_generator = p_item_id_no_generator;
      }

   }
