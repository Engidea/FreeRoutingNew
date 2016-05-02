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
 * Wiring.java
 *
 * Created on 24. Mai 2004, 07:20
 */

package specctra;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import specctra.varie.DsnReadUtils;
import board.RoutingBoard;
import board.items.BrdAbitVia;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.items.BrdTrace;
import board.items.BrdTracePolyline;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.Polyline;
import freert.planar.ShapeTileBox;
import freert.rules.NetClass;
import freert.rules.RuleNet;
import freert.varie.ItemClass;
import freert.varie.UndoableObjectNode;
import gui.varie.IndentFileWriter;

/**
 * Class for reading and writing wiring scopes from dsn-files.
 *
 * @author Alfons Wirtz
 */
final class DsnKeywordWiring extends DsnKeywordScope
   {
   private static final String classname="DsnKeywordWiring.";
   
   public DsnKeywordWiring()
      {
      super("wiring");
      }

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
            System.out.println("Wiring.read_scope: IO error scanning file");
            return false;
            }
         if (next_token == null)
            {
            System.out.println("Wiring.read_scope: unexpected end of file");
            return false;
            }
         if (next_token == CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         boolean read_ok = true;
         if (prev_token == OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.WIRE)
               {
               read_wire_scope(p_par);
               }
            else if (next_token == DsnKeyword.VIA)
               {
               read_ok = read_via_scope(p_par);
               }
            else
               {
               skip_scope(p_par.scanner);
               }
            }
         if (!read_ok)
            {
            return false;
            }
         }
      RoutingBoard board = p_par.i_board.get_routing_board();
      for (int i = 1; i <= board.brd_rules.nets.max_net_no(); ++i)
         {
         board.normalize_traces(i);
         }
      return true;
      }

   public static void write_scope(DsnWriteScopeParameter p_par) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("wiring");
      // write the wires
      Collection<BrdTrace> board_wires = p_par.board.get_traces();
      Iterator<BrdTrace> it = board_wires.iterator();
      while (it.hasNext())
         {
         write_wire_scope(p_par, it.next());
         }
      Collection<BrdAbitVia> board_vias = p_par.board.get_vias();
      for (BrdAbitVia curr_via : board_vias)
         {
         write_via_scope(p_par, curr_via);
         }
      // write the conduction areas
      Iterator<UndoableObjectNode> it2 = p_par.board.item_list.start_read_object();
      for (;;)
         {
         Object curr_ob = p_par.board.item_list.read_object(it2);
         if (curr_ob == null) break;

         if (!(curr_ob instanceof BrdAreaConduction)) continue;

         BrdAreaConduction curr_area = (BrdAreaConduction) curr_ob;

         if (!(p_par.board.layer_structure.is_signal(curr_area.get_layer())))
            {
            // This conduction areas arw written in the structure scope.
            continue;
            }
         
         write_conduction_area_scope(p_par, curr_area);
         }
      p_par.file.end_scope();
      }

   private static void write_via_scope(DsnWriteScopeParameter p_par, BrdAbitVia p_via) throws java.io.IOException
      {
      library.LibPadstack via_padstack = p_via.get_padstack();
      PlaPointFloat via_location = p_via.get_center().to_float();
      double[] via_coor = p_par.coordinate_transform.board_to_dsn(via_location);
      int net_no;
      freert.rules.RuleNet via_net;
      if (p_via.net_count() > 0)
         {
         net_no = p_via.get_net_no(0);
         via_net = p_par.board.brd_rules.nets.get(net_no);
         }
      else
         {
         net_no = 0;
         via_net = null;
         }
      p_par.file.start_scope();
      p_par.file.write("via ");
      p_par.identifier_type.write(via_padstack.pads_name, p_par.file);
      for (int i = 0; i < via_coor.length; ++i)
         {
         p_par.file.write(" ");
         p_par.file.write((new Double(via_coor[i])).toString());
         }
      if (via_net != null)
         {
         write_net(via_net, p_par.file, p_par.identifier_type);
         }
      DsnRule.write_item_clearance_class(p_par.board.brd_rules.clearance_matrix.get_name(p_via.clearance_class_no()), p_par.file, p_par.identifier_type);
      write_fixed_state(p_par.file, p_via.get_fixed_state());
      p_par.file.end_scope();
      }

   private static void write_wire_scope(DsnWriteScopeParameter p_par, BrdTrace p_wire) throws java.io.IOException
      {
      if (!(p_wire instanceof BrdTracePolyline))
         {
         System.out.println("Wiring.write_wire_scope: trace type not yet implemented");
         return;
         }
      BrdTracePolyline curr_wire = (BrdTracePolyline) p_wire;
      int layer_no = curr_wire.get_layer();
      board.BrdLayer board_layer = p_par.board.layer_structure.get(layer_no);
      DsnLayer curr_layer = new DsnLayer(board_layer.name, layer_no, board_layer.is_signal);
      double wire_width = p_par.coordinate_transform.board_to_dsn(2 * curr_wire.get_half_width());
      freert.rules.RuleNet wire_net = null;
      if (curr_wire.net_count() > 0)
         {
         wire_net = p_par.board.brd_rules.nets.get(curr_wire.get_net_no(0));
         }
      if (wire_net == null)
         {
         System.out.println("Wiring.write_wire_scope: net not found");
         return;
         }
      p_par.file.start_scope();
      p_par.file.write("wire");

      if (p_par.compat_mode)
         {
         PlaPoint[] corner_arr = curr_wire.polyline().corner_arr();
         PlaPointFloat[] float_corner_arr = new PlaPointFloat[corner_arr.length];
         for (int i = 0; i < corner_arr.length; ++i)
            {
            float_corner_arr[i] = corner_arr[i].to_float();
            }
         double[] coors = p_par.coordinate_transform.board_to_dsn(float_corner_arr);
         DsnPolygonPath curr_path = new DsnPolygonPath(curr_layer, wire_width, coors);
         curr_path.write_scope(p_par.file, p_par.identifier_type);
         }
      else
         {
         double[] coors = p_par.coordinate_transform.board_to_dsn(curr_wire.polyline());
         DsnPolylinePath curr_path = new DsnPolylinePath(curr_layer, wire_width, coors);
         curr_path.write_scope(p_par.file, p_par.identifier_type);
         }
      write_net(wire_net, p_par.file, p_par.identifier_type);
      DsnRule.write_item_clearance_class(p_par.board.brd_rules.clearance_matrix.get_name(p_wire.clearance_class_no()), p_par.file, p_par.identifier_type);
      write_fixed_state(p_par.file, curr_wire.get_fixed_state());
      p_par.file.end_scope();
      }

   private static void write_conduction_area_scope(DsnWriteScopeParameter p_par, BrdAreaConduction p_conduction_area) throws java.io.IOException
      {
      int net_count = p_conduction_area.net_count();
      if (net_count <= 0 || net_count > 1)
         {
         System.out.println("Plane.write_scope: unexpected net count");
         return;
         }
      freert.rules.RuleNet curr_net = p_par.board.brd_rules.nets.get(p_conduction_area.get_net_no(0));
      freert.planar.PlaArea curr_area = p_conduction_area.get_area();
      int layer_no = p_conduction_area.get_layer();
      board.BrdLayer board_layer = p_par.board.layer_structure.get(layer_no);
      DsnLayer conduction_layer = new DsnLayer(board_layer.name, layer_no, board_layer.is_signal);
      freert.planar.PlaShape boundary_shape;
      freert.planar.PlaShape[] holes;
      if (curr_area instanceof freert.planar.PlaShape)
         {
         boundary_shape = (freert.planar.PlaShape) curr_area;
         holes = new freert.planar.PlaShape[0];
         }
      else
         {
         boundary_shape = curr_area.get_border();
         holes = curr_area.get_holes();
         }
      p_par.file.start_scope();
      p_par.file.write("wire ");
      DsnShape dsn_shape = p_par.coordinate_transform.board_to_dsn(boundary_shape, conduction_layer);
      if (dsn_shape != null)
         {
         dsn_shape.write_scope(p_par.file, p_par.identifier_type);
         }
      for (int i = 0; i < holes.length; ++i)
         {
         DsnShape dsn_hole = p_par.coordinate_transform.board_to_dsn(holes[i], conduction_layer);
         dsn_hole.write_hole_scope(p_par.file, p_par.identifier_type);
         }
      write_net(curr_net, p_par.file, p_par.identifier_type);
      DsnRule.write_item_clearance_class(p_par.board.brd_rules.clearance_matrix.get_name(p_conduction_area.clearance_class_no()), p_par.file, p_par.identifier_type);
      p_par.file.end_scope();
      }

   static private void write_net(freert.rules.RuleNet p_net, IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.new_line();
      p_file.write("(");
      DsnNet.write_net_id(p_net, p_file, p_identifier_type);
      p_file.write(")");
      }

   static private void write_fixed_state(IndentFileWriter p_file, ItemFixState p_fixed_state) throws java.io.IOException
      {
      if (p_fixed_state == ItemFixState.UNFIXED)
         {
         return;
         }
      p_file.new_line();
      p_file.write("(type ");
      if (p_fixed_state == ItemFixState.SHOVE_FIXED)
         {
         p_file.write("shove_fixed)");
         }
      else if (p_fixed_state == ItemFixState.SYSTEM_FIXED)
         {
         p_file.write("fix)");
         }
      else
         {
         p_file.write("protect)");
         }
      }

   private void read_wire_scope(DsnReadScopeParameters p_par)
      {
      DsnNetId net_id = null;
      String clearance_class_name = null;
      board.varie.ItemFixState fixed = board.varie.ItemFixState.UNFIXED;
      DsnPath path = null; // Used, if a trace is read.
      DsnShape border_shape = null; // Used, if a conduction area is read.
      Collection<DsnShape> hole_list = new LinkedList<DsnShape>();
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
            System.out.println("Wiring.read_wire_scope: IO error scanning file");
            return;
            }
         if (next_token == null)
            {
            System.out.println("Wiring.read_wire_scope: unexpected end of file");
            return;
            }
         if (next_token == CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.POLYGON_PATH)
               {
               path = DsnShape.read_polygon_path_scope(p_par.scanner, p_par.layer_structure);
               }
            else if (next_token == DsnKeyword.POLYLINE_PATH)
               {
               path = DsnShape.read_polyline_path_scope(p_par.scanner, p_par.layer_structure);
               }
            else if (next_token == DsnKeyword.RECTANGLE)
               {

               border_shape = DsnShape.read_rectangle_scope(p_par.scanner, p_par.layer_structure);
               }
            else if (next_token == DsnKeyword.POLYGON)
               {

               border_shape = DsnShape.read_polygon_scope(p_par.scanner, p_par.layer_structure);
               }
            else if (next_token == DsnKeyword.CIRCLE)
               {

               border_shape = DsnShape.read_circle_scope(p_par.scanner, p_par.layer_structure);
               }
            else if (next_token == DsnKeyword.WINDOW)
               {
               DsnShape hole_shape = DsnShape.read_scope(p_par.scanner, p_par.layer_structure);
               hole_list.add(hole_shape);
               // overread the closing bracket
               try
                  {
                  next_token = p_par.scanner.next_token();
                  }
               catch (java.io.IOException e)
                  {
                  System.out.println("Wiring.read_wire_scope: IO error scanning file");
                  return;
                  }
               if (next_token != DsnKeyword.CLOSED_BRACKET)
                  {
                  System.out.println("Wiring.read_wire_scope: closing bracket expected");
                  return;
                  }
               }
            else if (next_token == DsnKeyword.NET)
               {
               net_id = read_net_id(p_par.scanner);
               }
            else if (next_token == DsnKeyword.CLEARANCE_CLASS)
               {
               clearance_class_name = DsnReadUtils.read_string_scope(p_par.scanner);
               }
            else if (next_token == DsnKeyword.TYPE)
               {
               fixed = calc_fixed(p_par.scanner);
               }
            else
               {
               skip_scope(p_par.scanner);
               }
            }
         }
      if (path == null && border_shape == null)
         {
         System.out.println("Wiring.read_wire_scope: shape missing");
         return;
         }
      RoutingBoard board = p_par.i_board.get_routing_board();

      NetClass net_class = board.brd_rules.get_default_net_class();
      
      Collection<RuleNet> found_nets = get_subnets(net_id, board.brd_rules);
      
      int[] net_no_arr = new int[found_nets.size()];
      
      if ( found_nets.size() > 1 )
         board.userPrintln(classname+"weird net_size="+found_nets.size());
      
      int curr_index = 0;
      for (RuleNet curr_net : found_nets)
         {
         net_no_arr[curr_index] = curr_net.net_number;
         net_class = curr_net.get_class();
         ++curr_index;
         }
      
      int clearance_class_no = -1;
      if (clearance_class_name != null)
         {
         clearance_class_no = board.brd_rules.clearance_matrix.get_no(clearance_class_name);
         }
      
      int layer_no;
      int half_width;
      if (path != null)
         {
         layer_no = path.layer.layer_no;
         half_width = (int) Math.round(p_par.coordinate_transform.dsn_to_board(path.width / 2));
         }
      else
         {
         layer_no = border_shape.layer.layer_no;
         half_width = 0;
         }
      if (layer_no < 0 || layer_no >= board.get_layer_count())
         {
         System.out.print("Wiring.read_wire_scope: unexpected layer ");
         if (path != null)
            {
            System.out.println(path.layer.name);
            }
         else
            {
            System.out.println(border_shape.layer.name);
            }
         return;
         }

      ShapeTileBox bounding_box = board.get_bounding_box();

      BrdItem result = null;
      if (border_shape != null)
         {
         if (clearance_class_no < 0)
            {
            clearance_class_no = net_class.default_item_clearance_classes.get(ItemClass.AREA);
            }
         Collection<DsnShape> area = new LinkedList<DsnShape>();
         area.add(border_shape);
         area.addAll(hole_list);
         freert.planar.PlaArea conduction_area = DsnShape.transform_area_to_board(area, p_par.coordinate_transform);
         result = board.insert_conduction_area(conduction_area, layer_no, net_no_arr, clearance_class_no, false, fixed);
         }
      else if (path instanceof DsnPolygonPath)
         {
         if (clearance_class_no < 0)
            {
            clearance_class_no = net_class.default_item_clearance_classes.get(ItemClass.TRACE);
            }
         
         PlaPointInt[] corner_arr = new PlaPointInt[path.coordinate_arr.length / 2];
         double[] curr_point = new double[2];
         for (int i = 0; i < corner_arr.length; ++i)
            {
            curr_point[0] = path.coordinate_arr[2 * i];
            curr_point[1] = path.coordinate_arr[2 * i + 1];
            PlaPointFloat curr_corner = p_par.coordinate_transform.dsn_to_board(curr_point);
            if (!bounding_box.contains(curr_corner))
               {
               System.out.println("Wiring.read_wire_scope: wire corner outside board");
               return;
               }
            corner_arr[i] = curr_corner.round();
            }
         
         try
            {
            Polyline trace_polyline = new Polyline(corner_arr);
            // Traces are not yet normalized here because cycles may be removed premature.
            result = board.insert_trace_without_cleaning(trace_polyline, layer_no, half_width, net_no_arr, clearance_class_no, fixed);
            }
         catch ( Exception exc )
            {
            System.out.println("Wiring.read_wire_scope: polyline too short");
            }
         }
      else if (path instanceof DsnPolylinePath)
         {
         if (clearance_class_no < 0)
            {
            clearance_class_no = net_class.default_item_clearance_classes.get(ItemClass.TRACE);
            }
         PlaLineInt[] line_arr = new PlaLineInt[path.coordinate_arr.length / 4];
         double[] curr_point = new double[2];
         for (int i = 0; i < line_arr.length; ++i)
            {
            curr_point[0] = path.coordinate_arr[4 * i];
            curr_point[1] = path.coordinate_arr[4 * i + 1];
            PlaPointFloat curr_a = p_par.coordinate_transform.dsn_to_board(curr_point);
            curr_point[0] = path.coordinate_arr[4 * i + 2];
            curr_point[1] = path.coordinate_arr[4 * i + 3];
            PlaPointFloat curr_b = p_par.coordinate_transform.dsn_to_board(curr_point);
            line_arr[i] = new PlaLineInt(curr_a.round(), curr_b.round());
            }
         Polyline trace_polyline = new Polyline(line_arr);
         result = board.insert_trace_without_cleaning(trace_polyline, layer_no, half_width, net_no_arr, clearance_class_no, fixed);
         }
      else
         {
         System.out.println("Wiring.read_wire_scope: unexpected Path subclass");
         return;
         }
      if (result != null && result.net_count() == 0)
         {
         try_correct_net(result);
         }
      }

   /**
    * Maybe trace of type turret without net in Mentor design. Try to assig the net by calculating the overlaps.
    */
   private void try_correct_net(BrdItem p_item)
      {
      if (!(p_item instanceof BrdTrace))
         {
         return;
         }
      BrdTrace curr_trace = (BrdTrace) p_item;
      java.util.Set<BrdItem> contacts = curr_trace.get_normal_contacts(curr_trace.first_corner(), true);
      contacts.addAll(curr_trace.get_normal_contacts(curr_trace.last_corner(), true));
      int corrected_net_no = 0;
      for (BrdItem curr_contact : contacts)
         {
         if (curr_contact.net_count() == 1)
            {
            corrected_net_no = curr_contact.get_net_no(0);
            break;
            }
         }
      if (corrected_net_no != 0)
         {
         p_item.set_net_no(corrected_net_no);
         }
      }

   private static Collection<freert.rules.RuleNet> get_subnets(DsnNetId p_net_id, freert.rules.BoardRules p_rules)
      {
      Collection<freert.rules.RuleNet> found_nets = new LinkedList<freert.rules.RuleNet>();
      if (p_net_id != null)
         {
         if (p_net_id.subnet_number > 0)
            {
            freert.rules.RuleNet found_net = p_rules.nets.get(p_net_id.name, p_net_id.subnet_number);
            if (found_net != null)
               {
               found_nets.add(found_net);
               }
            }
         else
            {
            found_nets = p_rules.nets.get(p_net_id.name);
            }
         }
      return found_nets;
      }

   private boolean read_via_scope(DsnReadScopeParameters p_par)
      {
      try
         {
         board.varie.ItemFixState fixed = board.varie.ItemFixState.UNFIXED;
         // read the padstack name
         Object next_token = p_par.scanner.next_token();
         if (!(next_token instanceof String))
            {
            System.out.println("Wiring.read_via_scope: padstack name expected");
            return false;
            }
         String padstack_name = (String) next_token;
         // read the location
         double[] location = new double[2];
         for (int i = 0; i < 2; ++i)
            {
            next_token = p_par.scanner.next_token();
            if (next_token instanceof Double)
               {
               location[i] = ((Double) next_token).doubleValue();
               }
            else if (next_token instanceof Integer)
               {
               location[i] = ((Integer) next_token).intValue();
               }
            else
               {
               System.out.println("Wiring.read_via_scope: number expected");
               return false;
               }
            }
         DsnNetId net_id = null;
         String clearance_class_name = null;
         for (;;)
            {
            Object prev_token = next_token;
            next_token = p_par.scanner.next_token();
            if (next_token == null)
               {
               System.out.println("Wiring.read_via_scope: unexpected end of file");
               return false;
               }
            if (next_token == CLOSED_BRACKET)
               {
               // end of scope
               break;
               }
            if (prev_token == OPEN_BRACKET)
               {
               if (next_token == DsnKeyword.NET)
                  {
                  net_id = read_net_id(p_par.scanner);
                  }
               else if (next_token == DsnKeyword.CLEARANCE_CLASS)
                  {
                  clearance_class_name = DsnReadUtils.read_string_scope(p_par.scanner);
                  }
               else if (next_token == DsnKeyword.TYPE)
                  {
                  fixed = calc_fixed(p_par.scanner);
                  }
               else
                  {
                  skip_scope(p_par.scanner);
                  }
               }
            }
         RoutingBoard board = p_par.i_board.get_routing_board();
         library.LibPadstack curr_padstack = board.library.padstacks.get(padstack_name);
         if (curr_padstack == null)
            {
            System.out.println("Wiring.read_via_scope: via padstack not found");
            return false;
            }
         freert.rules.NetClass net_class = board.brd_rules.get_default_net_class();
         Collection<freert.rules.RuleNet> found_nets = get_subnets(net_id, board.brd_rules);
         if (net_id != null && found_nets.isEmpty())
            {
            System.out.print("Wiring.read_via_scope: net with name ");
            System.out.print(net_id.name);
            System.out.println(" not found");
            }
         int[] net_no_arr = new int[found_nets.size()];
         int curr_index = 0;
         for (freert.rules.RuleNet curr_net : found_nets)
            {
            net_no_arr[curr_index] = curr_net.net_number;
            net_class = curr_net.get_class();
            }
         int clearance_class_no = -1;
         if (clearance_class_name != null)
            {
            clearance_class_no = board.brd_rules.clearance_matrix.get_no(clearance_class_name);
            }
         if (clearance_class_no < 0)
            {
            clearance_class_no = net_class.default_item_clearance_classes.get(ItemClass.VIA);
            }
         PlaPointInt board_location = p_par.coordinate_transform.dsn_to_board(location).round();
         if (via_exists(board_location, curr_padstack, net_no_arr, board))
            {
            System.out.print("Multiple via skipped at (");
            System.out.println(board_location.v_x + ", " + board_location.v_y + ")");
            }
         else
            {
            boolean attach_allowed = p_par.via_at_smd_allowed && curr_padstack.attach_allowed;
            board.insert_via(curr_padstack, board_location, net_no_arr, clearance_class_no, fixed, attach_allowed);
            }
         return true;
         }
      catch (java.io.IOException e)
         {
         System.out.println("Wiring.read_via_scope: IO error scanning file");
         return false;
         }
      }

   private static boolean via_exists(PlaPointInt p_location, library.LibPadstack p_padstack, int[] p_net_no_arr, RoutingBoard p_board)
      {
      ItemSelectionFilter filter = new ItemSelectionFilter(ItemSelectionChoice.VIAS);
      int from_layer = p_padstack.from_layer();
      int to_layer = p_padstack.to_layer();
      Collection<BrdItem> picked_items = p_board.pick_items(p_location, p_padstack.from_layer(), filter);
      for (BrdItem curr_item : picked_items)
         {
         BrdAbitVia curr_via = (BrdAbitVia) curr_item;
         if (curr_via.nets_equal(p_net_no_arr) && curr_via.get_center().equals(p_location) && curr_via.first_layer() == from_layer && curr_via.last_layer() == to_layer)
            {
            return true;
            }
         }
      return false;
      }

   static board.varie.ItemFixState calc_fixed(JflexScanner p_scanner)
      {
      try
         {
         // assume wire state is unfixed
         board.varie.ItemFixState result = board.varie.ItemFixState.UNFIXED;
         
         // handling the type of the wiring
         Object next_token = p_scanner.next_token();
         
         if (next_token == DsnKeyword.SHOVE_FIXED)
            {
            result = board.varie.ItemFixState.SHOVE_FIXED;
            }
         else if (next_token == DsnKeyword.FIX)
            {
            result = board.varie.ItemFixState.SYSTEM_FIXED;
            }
         else if (next_token == DsnKeyword.PROTECT)
            {
            // damiano, I have to find out how to ask Kicad NOT to protect traces...
            result = board.varie.ItemFixState.USER_FIXED;
            }
         
         next_token = p_scanner.next_token();
         
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.out.println("Wiring.is_fixed: ) expected");
            return board.varie.ItemFixState.UNFIXED;
            }
         return result;
         }
      catch (java.io.IOException e)
         {
         System.out.println("Wiring.is_fixed: IO error scanning file");
         return board.varie.ItemFixState.UNFIXED;
         }
      }

   /**
    * Reads a net_id. The subnet_number of the net_id will be 0, if no subneet_number was found.
    */
   private static DsnNetId read_net_id(JflexScanner p_scanner)
      {
      try
         {
         int subnet_number = 0;
         p_scanner.yybegin(DsnFileScanner.NAME);
         Object next_token = p_scanner.next_token();
         if (!(next_token instanceof String))
            {
            System.out.println("Wiring:read_net_id: String expected");
            return null;
            }
         String net_name = (String) next_token;
         next_token = p_scanner.next_token();
         if (next_token instanceof Integer)
            {
            subnet_number = (Integer) next_token;
            next_token = p_scanner.next_token();
            }
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.out.println("Wiring.read_net_id: closing bracket expected");
            }
         return new DsnNetId(net_name, subnet_number);
         }
      catch (java.io.IOException e)
         {
         System.out.println("DsnFile.read_string_scope: IO error scanning file");
         return null;
         }
      }
   }
