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
 * SessionFile.java
 *
 * Created on 29. Oktober 2004, 08:01
 */

package specctra;

import gui.varie.IndentFileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import main.Stat;
import planar.PlaPoint;
import planar.PlaPointFloat;
import board.BrdLayer;
import board.RoutingBoard;
import board.infos.BrdComponent;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.varie.ItemFixState;

/**
 * Methods to handle a Specctra session file.
 *
 * @author alfons
 */
public final class SpectraWriteSesFile
   {
   private static final String classname="SpectraWriteSesFile.";
   
   private final Stat stat;
   private final RoutingBoard r_board;
   private final IndentFileWriter output_file;

   
   public SpectraWriteSesFile ( Stat p_stat, RoutingBoard p_board, OutputStream p_output_stream )
      {
      stat = p_stat;
      r_board = p_board;
      output_file = new IndentFileWriter(p_output_stream);
      }
   
   /**
    * Creates a Specctra session file to update the host system from the RoutingBooard
    */
   public boolean write( String p_design_name)
      {
      String session_name = p_design_name.replace(".dsn", ".ses");
      
      try
         {
         String[] reserved_chars = { "(", ")", " ", "-" };
         DsnIdentifier identifier_type = new DsnIdentifier(reserved_chars, r_board.communication.specctra_parser_info.string_quote);
         write_session_scope(identifier_type, session_name, p_design_name);
         output_file.close();
         stat.userPrintln(classname+"write DONE");
         }
      catch ( Exception exc)
         {
         stat.userPrintln("unable to write session file",exc);
         return false;
         }

      return true;
      }

   private void write_session_scope(DsnIdentifier p_identifier_type, String p_session_name, String p_design_name) throws IOException
      {
      double scale_factor = r_board.communication.coordinate_transform.dsn_to_board(1) / r_board.communication.resolution;
      DsnCoordinateTransform coordinate_transform = new DsnCoordinateTransform(scale_factor, 0, 0);
      output_file.start_scope();
      output_file.write("session ");
      p_identifier_type.write(p_session_name, output_file);
      output_file.new_line();
      output_file.write("(base_design ");
      p_identifier_type.write(p_design_name, output_file);
      output_file.write(")");
      write_placement( p_identifier_type, coordinate_transform );
      write_was_is( p_identifier_type);
      write_routes( p_identifier_type, coordinate_transform );
      output_file.end_scope();
      }

   private void write_placement( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      output_file.start_scope();
      output_file.write("placement");
      DsnKeywordResolution.write_scope(output_file, r_board.communication);

      for (int i = 1; i <= r_board.library.packages.count(); ++i)
         {
         write_components( p_identifier_type, p_coordinate_transform, r_board.library.packages.get(i));
         }
      output_file.end_scope();
      }

   /**
    * Writes all components with the package p_package to the session file.
    */
   private void write_components( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform, library.LibPackage p_package) throws IOException
      {
      Collection<BrdItem> board_items = r_board.get_items();
      boolean component_found = false;
      for (int i = 1; i <= r_board.brd_components.count(); ++i)
         {
         board.infos.BrdComponent curr_component = r_board.brd_components.get(i);
         if (curr_component.get_package() == p_package)
            {
            // check, if not all items of the component are deleted
            boolean undeleted_item_found = false;
            Iterator<BrdItem> it = board_items.iterator();
            while (it.hasNext())
               {
               BrdItem curr_item = it.next();
               if (curr_item.get_component_no() == curr_component.id_no)
                  {
                  undeleted_item_found = true;
                  break;
                  }
               }
            if (undeleted_item_found)
               {
               if (!component_found)
                  {
                  // write the scope header
                  output_file.start_scope();
                  output_file.write("component ");
                  p_identifier_type.write(p_package.pkg_name, output_file);
                  component_found = true;
                  }
               write_component( p_identifier_type, p_coordinate_transform, curr_component);
               }
            }
         }
      if (component_found)
         {
         output_file.end_scope();
         }
      }

   private void write_component( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform, BrdComponent p_component)
         throws IOException
      {
      output_file.new_line();
      output_file.write("(place ");
      p_identifier_type.write(p_component.name, output_file);
      double[] location = p_coordinate_transform.board_to_dsn(p_component.get_location().to_float());
      Integer x_coor = (int) Math.round(location[0]);
      Integer y_coor = (int) Math.round(location[1]);
      output_file.write(" ");
      output_file.write(x_coor.toString());
      output_file.write(" ");
      output_file.write(y_coor.toString());
      if (p_component.is_on_front())
         {
         output_file.write(" front ");
         }
      else
         {
         output_file.write(" back ");
         }
      int rotation = (int) Math.round(p_component.get_rotation_in_degree());
      output_file.write((new Integer(rotation).toString()));
      if (p_component.position_fixed)
         {
         output_file.new_line();
         output_file.write(" (lock_type position)");
         }
      output_file.write(")");
      }

   private void write_was_is( DsnIdentifier p_identifier_type ) throws IOException
      {
      output_file.start_scope();
      output_file.write("was_is");
      Collection<BrdAbitPin> board_pins = r_board.get_pins();
      for (BrdAbitPin curr_pin : board_pins)
         {
         BrdAbitPin swapped_with = curr_pin.get_changed_to();
         if (curr_pin.get_changed_to() != curr_pin)
            {
            output_file.new_line();
            output_file.write("(pins ");
            board.infos.BrdComponent curr_cmp = r_board.brd_components.get(curr_pin.get_component_no());
            if (curr_cmp != null)
               {
               p_identifier_type.write(curr_cmp.name, output_file);
               output_file.write("-");
               library.LibPackagePin package_pin = curr_cmp.get_package().get_pin(curr_pin.get_index_in_package());
               p_identifier_type.write(package_pin.name, output_file);
               }
            else
               {
               System.out.println("SessionFile.write_was_is: component not found");
               }
            output_file.write(" ");
            board.infos.BrdComponent swap_cmp = r_board.brd_components.get(swapped_with.get_component_no());
            if (swap_cmp != null)
               {
               p_identifier_type.write(swap_cmp.name, output_file);
               output_file.write("-");
               library.LibPackagePin package_pin = swap_cmp.get_package().get_pin(swapped_with.get_index_in_package());
               p_identifier_type.write(package_pin.name, output_file);
               }
            else
               {
               System.out.println("SessionFile.write_was_is: component not found");
               }
            output_file.write(")");
            }
         }
      output_file.end_scope();
      }

   private void write_routes( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      output_file.start_scope();
      output_file.write("routes ");
      DsnKeywordResolution.write_scope(output_file, r_board.communication);
      DsnKeywordParser.write_scope(output_file, r_board.communication.specctra_parser_info, p_identifier_type, true);
      write_library( p_identifier_type, p_coordinate_transform);
      write_network( p_identifier_type, p_coordinate_transform);
      output_file.end_scope();
      }

   private void write_library( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      output_file.start_scope();
      output_file.write("library_out ");
      for (int i = 0; i < r_board.library.via_padstack_count(); ++i)
         {
         write_padstack(r_board.library.get_via_padstack(i), p_identifier_type, p_coordinate_transform );
         }
      output_file.end_scope();
      }

   private void write_padstack(library.LibPadstack p_padstack, DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      // search the layer range of the padstack
      int first_layer_no = 0;
      while (first_layer_no < r_board.get_layer_count())
         {
         if (p_padstack.get_shape(first_layer_no) != null)
            {
            break;
            }
         ++first_layer_no;
         }
      int last_layer_no = r_board.get_layer_count() - 1;
      while (last_layer_no >= 0)
         {
         if (p_padstack.get_shape(last_layer_no) != null)
            {
            break;
            }
         --last_layer_no;
         }
      if (first_layer_no >= r_board.get_layer_count() || last_layer_no < 0)
         {
         System.out.println("SessionFile.write_padstack: padstack shape not found");
         return;
         }

      output_file.start_scope();
      output_file.write("padstack ");
      p_identifier_type.write(p_padstack.pads_name, output_file);
      for (int index = first_layer_no; index <= last_layer_no; ++index)
         {
         planar.PlaShape curr_board_shape = p_padstack.get_shape(index);
         
         if (curr_board_shape == null) continue;
         
         BrdLayer board_layer = r_board.layer_structure.get(index);
         DsnLayer curr_layer = new DsnLayer(board_layer.name, index, board_layer.is_signal);
         DsnShape curr_shape = p_coordinate_transform.board_to_dsn_rel(curr_board_shape, curr_layer);
         output_file.start_scope();
         output_file.write("shape");
         curr_shape.write_scope_int(output_file, p_identifier_type);
         output_file.end_scope();
         }
      if (!p_padstack.attach_allowed)
         {
         output_file.new_line();
         output_file.write("(attach off)");
         }
      output_file.end_scope();
      }

   private void write_network( DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      output_file.start_scope();
      output_file.write("network_out ");
      for (int i = 1; i <= r_board.brd_rules.nets.max_net_no(); ++i)
         {
         write_net(i, p_identifier_type, p_coordinate_transform );
         }
      output_file.end_scope();
      }

   private void write_net(int p_net_no, DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      Collection<BrdItem> net_items = r_board.get_connectable_items(p_net_no);
      boolean header_written = false;
      Iterator<BrdItem> it = net_items.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item.get_fixed_state() == board.varie.ItemFixState.SYSTEM_FIXED)
            {
            continue;
            }
         
         boolean is_wire = curr_item instanceof BrdTracePolyline;
         boolean is_via = curr_item instanceof BrdAbitVia;
         boolean is_conduction_area = curr_item instanceof BrdAreaConduction && r_board.layer_structure.is_signal(curr_item.first_layer());
         
         if (!header_written && (is_wire || is_via || is_conduction_area))
            {
            output_file.start_scope();
            output_file.write("net ");
            rules.RuleNet curr_net = r_board.brd_rules.nets.get(p_net_no);
            if (curr_net == null)
               {
               System.out.println("SessionFile.write_net: net not found");
               }
            else
               {
               p_identifier_type.write(curr_net.name, output_file);
               }
            header_written = true;
            }
         if (is_wire)
            {
            write_wire((BrdTracePolyline) curr_item, p_identifier_type, p_coordinate_transform );
            }
         else if (is_via)
            {
            write_via((BrdAbitVia) curr_item, p_identifier_type, p_coordinate_transform );
            }
         else if (is_conduction_area)
            {
            write_conduction_area((BrdAreaConduction) curr_item, p_identifier_type, p_coordinate_transform);
            }
         }
      if (header_written)
         {
         output_file.end_scope();
         }
      }

   private void write_wire(BrdTracePolyline p_wire,  DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      int layer_no = p_wire.get_layer();
      board.BrdLayer board_layer = r_board.layer_structure.get(layer_no);
      int wire_width = (int) Math.round(p_coordinate_transform.board_to_dsn(2 * p_wire.get_half_width()));
      output_file.start_scope();
      output_file.write("wire");
      PlaPoint[] corner_arr = p_wire.polyline().corner_arr();
      int[] coors = new int[2 * corner_arr.length];
      int corner_index = 0;
      int[] prev_coors = null;
      for (int i = 0; i < corner_arr.length; ++i)
         {
         double[] curr_float_coors = p_coordinate_transform.board_to_dsn(corner_arr[i].to_float());
         int[] curr_coors = new int[2];
         curr_coors[0] = (int) Math.round(curr_float_coors[0]);
         curr_coors[1] = (int) Math.round(curr_float_coors[1]);
         if (i == 0 || (curr_coors[0] != prev_coors[0] || curr_coors[1] != prev_coors[1]))
            {
            coors[corner_index] = curr_coors[0];
            ++corner_index;
            coors[corner_index] = curr_coors[1];
            ++corner_index;
            prev_coors = curr_coors;

            }
         }
      if (corner_index < coors.length)
         {
         int[] adjusted_coors = new int[corner_index];
         for (int i = 0; i < adjusted_coors.length; ++i)
            {
            adjusted_coors[i] = coors[i];
            }
         coors = adjusted_coors;
         }
      write_path(board_layer.name, wire_width, coors, p_identifier_type );
      write_fixed_state(p_wire.get_fixed_state());
      output_file.end_scope();
      }

   private void write_via(BrdAbitVia p_via, DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform ) throws IOException
      {
      library.LibPadstack via_padstack = p_via.get_padstack();
      PlaPointFloat via_location = p_via.get_center().to_float();
      output_file.start_scope();
      output_file.write("via ");
      p_identifier_type.write(via_padstack.pads_name, output_file);
      output_file.write(" ");
      double[] location = p_coordinate_transform.board_to_dsn(via_location);
      Integer x_coor = (int) Math.round(location[0]);
      output_file.write(x_coor.toString());
      output_file.write(" ");
      Integer y_coor = (int) Math.round(location[1]);
      output_file.write(y_coor.toString());
      write_fixed_state(p_via.get_fixed_state());
      output_file.end_scope();
      }

   private void write_fixed_state( ItemFixState p_fixed_state) throws IOException
      {
      if (p_fixed_state.ordinal() <= ItemFixState.SHOVE_FIXED.ordinal())
         {
         return;
         }
      output_file.new_line();
      output_file.write("(type ");
      if (p_fixed_state == board.varie.ItemFixState.SYSTEM_FIXED)
         {
         output_file.write("fix)");
         }
      else
         {
         output_file.write("protect)");
         }
      }

   private void write_path(String p_layer_name, int p_width, int[] p_coors, DsnIdentifier p_identifier_type ) throws IOException
      {
      output_file.start_scope();
      output_file.write("path ");
      p_identifier_type.write(p_layer_name, output_file);
      output_file.write(" ");
      output_file.write((new Integer(p_width)).toString());
      int corner_count = p_coors.length / 2;
      for (int i = 0; i < corner_count; ++i)
         {
         output_file.new_line();
         output_file.write(new Integer(p_coors[2 * i]).toString());
         output_file.write(" ");
         output_file.write(new Integer(p_coors[2 * i + 1]).toString());
         }
      output_file.end_scope();
      }

   private void write_conduction_area(BrdAreaConduction p_conduction_area, DsnIdentifier p_identifier_type, DsnCoordinateTransform p_coordinate_transform) throws IOException
      {
      int net_count = p_conduction_area.net_count();
      if (net_count <= 0 || net_count > 1)
         {
         System.out.println("SessionFile.write_conduction_area: unexpected net count");
         return;
         }
      planar.PlaArea curr_area = p_conduction_area.get_area();
      int layer_no = p_conduction_area.get_layer();
      BrdLayer board_layer = r_board.layer_structure.get(layer_no);
      DsnLayer conduction_layer = new DsnLayer(board_layer.name, layer_no, board_layer.is_signal);
      planar.PlaShape boundary_shape;
      planar.PlaShape[] holes;
      if (curr_area instanceof planar.PlaShape)
         {
         boundary_shape = (planar.PlaShape) curr_area;
         holes = new planar.PlaShape[0];
         }
      else
         {
         boundary_shape = curr_area.get_border();
         holes = curr_area.get_holes();
         }
      output_file.start_scope();
      output_file.write("wire ");
      DsnShape dsn_shape = p_coordinate_transform.board_to_dsn(boundary_shape, conduction_layer);
      if (dsn_shape != null)
         {
         dsn_shape.write_scope_int(output_file, p_identifier_type);
         }
      for (int i = 0; i < holes.length; ++i)
         {
         DsnShape dsn_hole = p_coordinate_transform.board_to_dsn(holes[i], conduction_layer);
         dsn_hole.write_hole_scope(output_file, p_identifier_type);
         }
      output_file.end_scope();
      }
   }