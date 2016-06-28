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
 * Structure.java
 *
 * Created on 13. Mai 2004, 09:57
 */
package freert.spectra;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import board.BrdLayerStructure;
import board.RoutingBoard;
import board.items.BrdAreaConduction;
import board.varie.ItemFixState;
import board.varie.TraceAngleRestriction;
import freert.host.HostCom;
import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.planar.ShapeSegments;
import freert.planar.ShapeTileBox;
import freert.rules.BoardRules;
import freert.spectra.varie.DsnKeywordAutoroute;
import freert.spectra.varie.DsnReadUtils;
import freert.varie.ItemClass;
import freert.varie.NetNosList;
import freert.varie.UndoObjectNode;
import freert.varie.UndoObjectStorable;
import gui.varie.IndentFileWriter;

/**
 * Class for reading and writing structure scopes from dsn-files.
 *
 * @author Alfons Wirtz
 */
public final class DsnKeywordStructure extends DsnKeywordScope
   {
   private static final String classname = "DsnKeywordStructure.";
         
   public DsnKeywordStructure()
      {
      super("structure");
      }

   @Override
   public boolean read_scope(DsnReadScopeParameters p_par)
      {
      DsnBoardConstruction board_construction_info = new DsnBoardConstruction();

      // If true, components on the back side are rotated before mirroring The correct location is the scope PlaceControl, but Electra writes it here.
      boolean flip_style_rotate_first = false;

      Collection<DsnScopeArea> keepout_list = new LinkedList<DsnScopeArea>();
      Collection<DsnScopeArea> via_keepout_list = new LinkedList<DsnScopeArea>();
      Collection<DsnScopeArea> place_keepout_list = new LinkedList<DsnScopeArea>();

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
            System.out.println("Structure.read_scope: IO error scanning file");
            System.out.println(e);
            return false;
            }
         if (next_token == null)
            {
            System.out.println("Structure.read_scope: unexpected end of file");
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
            if (next_token == DsnKeyword.BOUNDARY)
               {
               read_boundary_scope(p_par.scanner, board_construction_info);
               }
            else if (next_token == DsnKeyword.LAYER)
               {
               read_ok = read_layer_scope(p_par.scanner, board_construction_info, p_par.string_quote);
               if (p_par.layer_structure != null)
                  {
                  // correct the layer_structure because another layer is read
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  System.err.println("Overwriting p_par.layer_structure");
                  }
               }
            else if (next_token == DsnKeyword.VIA)
               {
               p_par.via_padstack_names = read_via_padstacks(p_par.scanner);
               }
            else if (next_token == DsnKeyword.RULE)
               {
               board_construction_info.default_rules.addAll(DsnRule.read_scope(p_par.scanner));
               }
            else if (next_token == DsnKeyword.KEEPOUT)
               {
               if (p_par.layer_structure == null)
                  {
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  }
               keepout_list.add(DsnShape.read_area_scope(p_par.scanner, p_par.layer_structure, false));
               }
            else if (next_token == DsnKeyword.VIA_KEEPOUT)
               {
               if (p_par.layer_structure == null)
                  {
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  }
               via_keepout_list.add(DsnShape.read_area_scope(p_par.scanner, p_par.layer_structure, false));
               }
            else if (next_token == DsnKeyword.PLACE_KEEPOUT)
               {
               if (p_par.layer_structure == null)
                  {
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  }
               place_keepout_list.add(DsnShape.read_area_scope(p_par.scanner, p_par.layer_structure, false));
               }
            else if (next_token == DsnKeyword.PLANE_SCOPE)
               {
               if (p_par.layer_structure == null)
                  {
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  }
               DsnKeyword.PLANE_SCOPE.read_scope(p_par);
               }
            else if (next_token == DsnKeyword.AUTOROUTE_SETTINGS)
               {
               if (p_par.layer_structure == null)
                  {
                  p_par.layer_structure = new DsnLayerStructure(board_construction_info.layer_info);
                  p_par.autoroute_settings = DsnKeywordAutoroute.read_scope(p_par.scanner, p_par.layer_structure);
                  }
               }
            else if (next_token == DsnKeyword.CONTROL)
               {
               read_ok = read_control_scope(p_par);
               }
            else if (next_token == DsnKeyword.FLIP_STYLE)
               {
               flip_style_rotate_first = DsnKeywordPlaceControl.read_flip_style_rotate_first(p_par.scanner);
               }
            else if (next_token == DsnKeyword.SNAP_ANGLE)
               {
               // it is OK if it is null, it just means that default will be used
               p_par.snap_angle = read_snap_angle(p_par.scanner);
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

      boolean result = true;
      if (p_par.i_board.get_routing_board() == null)
         {
         result = create_board(p_par, board_construction_info);
         }
      
      RoutingBoard board = p_par.i_board.get_routing_board();
      if (board == null)
         {
         return false;
         }
      
      if (flip_style_rotate_first)
         {
         board.brd_components.set_flip_style_rotate_first(true);
         }
      ItemFixState fixed_state;
      
      if ( board.debug(Mdbg.DSN, Ldbg.RELEASE) )
         {
         fixed_state = ItemFixState.SYSTEM_FIXED;
         }
      else
         {
         fixed_state = ItemFixState.USER_FIXED;
         }
      
      // insert the keepouts
      for (DsnScopeArea curr_area : keepout_list)
         {
         if (!insert_keepout(curr_area, p_par, KeepoutType.keepout, fixed_state))
            {
            return false;
            }
         }

      for (DsnScopeArea curr_area : via_keepout_list)
         {
         if (!insert_keepout(curr_area, p_par, KeepoutType.via_keepout, ItemFixState.SYSTEM_FIXED))
            {
            return false;
            }
         }

      for (DsnScopeArea curr_area : place_keepout_list)
         {
         if (!insert_keepout(curr_area, p_par, KeepoutType.place_keepout, ItemFixState.SYSTEM_FIXED))
            {
            return false;
            }
         }

      // insert the planes.
      Iterator<DsnPlaneInfo> it = p_par.plane_list.iterator();
      while (it.hasNext())
         {
         DsnPlaneInfo plane_info = it.next();
         DsnNetId net_id = new DsnNetId(plane_info.net_name, 1);
         if (!p_par.netlist.contains(net_id))
            {
            DsnNet new_net = p_par.netlist.add_net(net_id);
            if (new_net != null)
               {
               board.brd_rules.nets.add(new_net.id.name, new_net.id.subnet_number, true);
               }
            }
         freert.rules.RuleNet curr_net = board.brd_rules.nets.get(plane_info.net_name, 1);
         if (curr_net == null)
            {
            System.out.println("Plane.read_scope: net not found");
            continue;
            }
         freert.planar.PlaArea plane_area = DsnShape.transform_area_to_board(plane_info.area.shape_list, p_par.coordinate_transform);
         DsnLayer curr_layer = (plane_info.area.shape_list.iterator().next()).layer;
         if (curr_layer.layer_no >= 0)
            {
            int clearance_class_no;
            if (plane_info.area.clearance_class_name != null)
               {
               clearance_class_no = board.brd_rules.clearance_matrix.get_no(plane_info.area.clearance_class_name);
               if (clearance_class_no < 0)
                  {
                  System.out.println("Structure.read_scope: clearance class not found");
                  clearance_class_no = BoardRules.clearance_null_idx;
                  }
               }
            else
               {
               clearance_class_no = curr_net.get_class().default_item_clearance_classes.get(ItemClass.AREA);
               }
            NetNosList net_numbers = new NetNosList( curr_net.net_number);
            
            board.insert_conduction_area(plane_area, curr_layer.layer_no, net_numbers, clearance_class_no, false, ItemFixState.SYSTEM_FIXED);
            }
         else
            {
            System.out.println("Plane.read_scope: unexpected layer name");
            return false;
            }
         }
      insert_missing_power_planes(board_construction_info.layer_info, p_par.netlist, board);

      p_par.i_board.initialize_manual_trace_half_widths();
      if (p_par.autoroute_settings != null)
         {
         p_par.i_board.itera_settings.autoroute_settings = p_par.autoroute_settings;
         }
      return result;
      }

   public static void write_scope(DsnWriteScopeParameter p_par) throws java.io.IOException
      {
      p_par.file.start_scope();
      p_par.file.write("structure");
      // write the bounding box
      p_par.file.start_scope();
      p_par.file.write("boundary");
      ShapeTileBox bounds = p_par.board.get_bounding_box();
      double[] rect_coor = p_par.coordinate_transform.board_to_dsn(bounds);
      DsnRectangle bounding_rectangle = new DsnRectangle(DsnLayer.PCB, rect_coor);
      bounding_rectangle.write_scope(p_par.file, p_par.identifier_type);
      p_par.file.end_scope();
      // lookup the outline in the board
      UndoObjectStorable curr_ob = null;
      Iterator<UndoObjectNode> it = p_par.board.undo_items.start_read_object();
      for (;;)
         {
         curr_ob = p_par.board.undo_items.read_next(it);
         if (curr_ob == null)
            {
            break;
            }
         if (curr_ob instanceof board.items.BrdOutline)
            {
            break;
            }
         }
      if (curr_ob == null)
         {
         System.out.println("Structure.write_scope; outline not found");
         return;
         }
      board.items.BrdOutline outline = (board.items.BrdOutline) curr_ob;

      // write the outline
      for (int i = 0; i < outline.shape_count(); ++i)
         {
         DsnShape outline_shape = p_par.coordinate_transform.board_to_dsn(outline.get_shape(i), DsnLayer.SIGNAL);
         p_par.file.start_scope();
         p_par.file.write("boundary");
         outline_shape.write_scope(p_par.file, p_par.identifier_type);
         p_par.file.end_scope();
         }

      write_snap_angle(p_par.file, p_par.board.brd_rules.get_trace_snap_angle());

      // write the routing vias
      write_via_padstacks(p_par.board.brd_library, p_par.file, p_par.identifier_type);

      // write the control scope
      write_control_scope(p_par.board.brd_rules, p_par.file);

      write_default_rules(p_par);

      // write the autoroute settings
      DsnKeywordAutoroute.write_scope(p_par.file, p_par.autoroute_settings, p_par.board.layer_structure, p_par.identifier_type);

      // write the keepouts
      it = p_par.board.undo_items.start_read_object();
      for (;;)
         {
         curr_ob = p_par.board.undo_items.read_next(it);
         if (curr_ob == null)
            {
            break;
            }
         if (!(curr_ob instanceof board.items.BrdArea))
            {
            continue;
            }
         board.items.BrdArea curr_keepout = (board.items.BrdArea) curr_ob;
         if (curr_keepout.get_component_no() != 0)
            {
            // keepouts belonging to a component are not written individually.
            continue;
            }
         if (curr_keepout instanceof board.items.BrdAreaConduction)
            {
            // conduction area will be written later.
            continue;
            }
         write_keepout_scope(p_par, curr_keepout);
         }

      // write the conduction areas
      it = p_par.board.undo_items.start_read_object();
      for (;;)
         {
         curr_ob = p_par.board.undo_items.read_next(it);
         if (curr_ob == null)  break;

         if (!(curr_ob instanceof board.items.BrdAreaConduction)) continue;

         BrdAreaConduction curr_area = (BrdAreaConduction) curr_ob;
         
         if (p_par.board.layer_structure.is_signal(curr_area.get_layer()))
            {
            // These conduction areas are written in the wiring scope.
            continue;
            }
         DsnKeywordPlane.write_scope(p_par, (board.items.BrdAreaConduction) curr_ob);
         }
      p_par.file.end_scope();
      }

   static void write_default_rules(DsnWriteScopeParameter p_par) throws java.io.IOException
      {
      // write the default rule using 0 as default layer.
      DsnRule.write_default_rule(p_par, 0);

      // write the layer structure
      for (int index = 0; index < p_par.board.layer_structure.size(); ++index)
         {
         boolean write_layer_rule = p_par.board.brd_rules.get_default_net_class().get_trace_half_width(index) != p_par.board.brd_rules.get_default_net_class().get_trace_half_width(0)
               || !clearance_equals(p_par.board.brd_rules.clearance_matrix, index, 0);
         DsnLayer.write_scope(p_par, index, write_layer_rule);
         }
      }

   private static void write_via_padstacks(freert.library.BrdLibrary p_library, IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.new_line();
      p_file.write("(via");
      for (int i = 0; i < p_library.via_padstack_count(); ++i)
         {
         freert.library.LibPadstack curr_padstack = p_library.get_via_padstack(i);
         if (curr_padstack != null)
            {
            p_file.write(" ");
            p_identifier_type.write(curr_padstack.pads_name, p_file);
            }
         else
            {
            System.out.println("Structure.write_via_padstacks: padstack is null");
            }
         }
      p_file.write(")");
      }

   private static void write_control_scope(freert.rules.BoardRules p_rules, IndentFileWriter p_file) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("control");
      p_file.new_line();
      p_file.write("(via_at_smd ");
      boolean via_at_smd_allowed = false;
      for (int i = 0; i < p_rules.via_infos.count(); ++i)
         {
         if (p_rules.via_infos.get(i).attach_smd_allowed())
            {
            via_at_smd_allowed = true;
            break;
            }
         }
      if (via_at_smd_allowed)
         {
         p_file.write("on)");
         }
      else
         {
         p_file.write("off)");
         }
      p_file.end_scope();
      }

   private static void write_keepout_scope(DsnWriteScopeParameter p_par, board.items.BrdArea p_keepout) throws java.io.IOException
      {
      freert.planar.PlaArea keepout_area = p_keepout.get_area();
      int layer_no = p_keepout.get_layer();
      board.BrdLayer board_layer = p_par.board.layer_structure.get(layer_no);
      DsnLayer keepout_layer = new DsnLayer(board_layer.name, layer_no, board_layer.is_signal);
      freert.planar.PlaShape boundary_shape;
      freert.planar.PlaShape[] holes;
      if (keepout_area instanceof freert.planar.PlaShape)
         {
         boundary_shape = (freert.planar.PlaShape) keepout_area;
         holes = new freert.planar.PlaShape[0];
         }
      else
         {
         boundary_shape = keepout_area.get_border();
         holes = keepout_area.get_holes();
         }
      p_par.file.start_scope();
      if (p_keepout instanceof board.items.BrdAreaObstacleVia)
         {
         p_par.file.write("via_keepout");
         }
      else
         {
         p_par.file.write("keepout");
         }
      DsnShape dsn_shape = p_par.coordinate_transform.board_to_dsn(boundary_shape, keepout_layer);
      if (dsn_shape != null)
         {
         dsn_shape.write_scope(p_par.file, p_par.identifier_type);
         }
      for (int i = 0; i < holes.length; ++i)
         {
         DsnShape dsn_hole = p_par.coordinate_transform.board_to_dsn(holes[i], keepout_layer);
         dsn_hole.write_hole_scope(p_par.file, p_par.identifier_type);
         }
      if (p_keepout.clearance_idx() > 0)
         {
         DsnRule.write_item_clearance_class(p_par.board.brd_rules.clearance_matrix.get_name(p_keepout.clearance_idx()), p_par.file, p_par.identifier_type);
         }
      p_par.file.end_scope();
      }

   private static boolean read_boundary_scope(JflexScanner p_scanner, DsnBoardConstruction p_board_construction_info)
      {
      DsnShape curr_shape = DsnShape.read_scope(p_scanner, null);
      // overread the closing bracket.
      try
         {
         Object prev_token = null;
         for (;;)
            {
            Object next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            if (prev_token == DsnKeyword.OPEN_BRACKET)
               {
               if (next_token == DsnKeyword.CLEARANCE_CLASS)
                  {
                  p_board_construction_info.outline_clearance_class_name = DsnReadUtils.read_string_scope(p_scanner);
                  }
               }
            prev_token = next_token;
            }
         }
      catch (java.io.IOException e)
         {
         System.out.println("Structure.read_boundary_scope: IO error scanning file");
         return false;
         }
      if (curr_shape == null)
         {
         System.out.println("Structure.read_boundary_scope: shape is null");
         return true;
         }
      if (curr_shape.layer == DsnLayer.PCB)
         {
         if (p_board_construction_info.bounding_shape == null)
            {
            p_board_construction_info.bounding_shape = curr_shape;
            }
         else
            {
            System.out.println("Structure.read_boundary_scope: exact 1 bounding_shape expected");
            }
         }
      else if (curr_shape.layer == DsnLayer.SIGNAL)
         {
         p_board_construction_info.outline_shapes.add(curr_shape);
         }
      else
         {
         System.out.println("Structure.read_boundary_scope: unexpected layer");
         }
      return true;
      }

   static boolean read_layer_scope(JflexScanner p_scanner, DsnBoardConstruction p_board_construction_info, String p_string_quote)
      {
      try
         {
         boolean layer_ok = true;
         boolean is_signal = true;
         Object next_token = p_scanner.next_token();
         if (!(next_token instanceof String))
            {
            System.out.println("Structure.read_layer_scope: String expected");
            return false;
            }
         Collection<String> net_names = new LinkedList<String>();
         String layer_string = (String) next_token;
         next_token = p_scanner.next_token();
         while (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            if (next_token != DsnKeyword.OPEN_BRACKET)
               {
               System.out.println("Structure.read_layer_scope: ( expected");
               return false;
               }
            next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.TYPE)
               {
               next_token = p_scanner.next_token();
               if (next_token == DsnKeyword.POWER)
                  {
                  is_signal = false;
                  }
               else if (next_token != DsnKeyword.SIGNAL)
                  {
                  System.out.print("Structure.read_layer_scope: unknown layer type ");
                  if (next_token instanceof String)
                     {
                     System.out.print((String) next_token);
                     }
                  System.out.println();
                  layer_ok = false;
                  }
               next_token = p_scanner.next_token();
               if (next_token != DsnKeyword.CLOSED_BRACKET)
                  {
                  System.out.println("Structure.read_layer_scope: ) expected");
                  return false;
                  }
               }
            else if (next_token == DsnKeyword.RULE)
               {
               Collection<DsnRule> curr_rules = DsnRule.read_scope(p_scanner);
               p_board_construction_info.layer_dependent_rules.add(new DsnLayerRule(layer_string, curr_rules));
               }
            else if (next_token == DsnKeyword.USE_NET)
               {
               for (;;)
                  {
                  p_scanner.yybegin(DsnFileScanner.NAME);
                  next_token = p_scanner.next_token();
                  if (next_token == DsnKeyword.CLOSED_BRACKET)
                     {
                     break;
                     }
                  if (next_token instanceof String)
                     {
                     net_names.add((String) next_token);
                     }
                  else
                     {
                     System.out.println("Structure.read_layer_scope: string expected");
                     }
                  }
               }
            else
               {
               skip_scope(p_scanner);
               }
            next_token = p_scanner.next_token();
            }
         if (layer_ok)
            {
            // NOTE that the layer_no is calculated here and guarantee linearly increasing
            DsnLayer curr_layer = new DsnLayer(layer_string, p_board_construction_info.found_layer_count++, is_signal, net_names);
            p_board_construction_info.layer_info.add(curr_layer);
            }
         }
      catch (java.io.IOException e)
         {
         System.out.println("Layer.read_scope: IO error scanning file");
         System.out.println(e);
         return false;
         }
      return true;

      }

   static Collection<String> read_via_padstacks(JflexScanner p_scanner)
      {
      try
         {
         Collection<String> normal_vias = new LinkedList<String>();
         Collection<String> spare_vias = new LinkedList<String>();
         for (;;)
            {
            Object next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            if (next_token == DsnKeyword.OPEN_BRACKET)
               {
               next_token = p_scanner.next_token();
               if (next_token == DsnKeyword.SPARE)
                  {
                  spare_vias = read_via_padstacks(p_scanner);
                  }
               else
                  {
                  skip_scope(p_scanner);
                  }
               }
            else if (next_token instanceof String)
               {
               normal_vias.add((String) next_token);
               }
            else
               {
               System.out.println("Structure.read_via_padstack: String expected");
               return null;
               }
            }
         // add the spare vias to the end of the list
         normal_vias.addAll(spare_vias);
         return normal_vias;
         }
      catch (java.io.IOException e)
         {
         System.out.println("Structure.read_via_padstack: IO error scanning file");
         return null;
         }
      }

   private static boolean read_control_scope(DsnReadScopeParameters p_par)
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
            System.out.println("Structure.read_control_scope: IO error scanning file");
            return false;
            }
         if (next_token == null)
            {
            System.out.println("Structure.read_control_scope: unexpected end of file");
            return false;
            }
         if (next_token == CLOSED_BRACKET)
            {
            // end of scope
            break;
            }
         if (prev_token == OPEN_BRACKET)
            {
            if (next_token == DsnKeyword.VIA_AT_SMD)
               {
               p_par.via_at_smd_allowed = DsnReadUtils.read_on_off_scope(p_par.scanner);
               }
            else
               {
               skip_scope(p_par.scanner);
               }
            }
         }
      return true;
      }

   public static TraceAngleRestriction read_snap_angle(JflexScanner p_scanner)
      {
      try
         {
         Object next_token = p_scanner.next_token();
         
         TraceAngleRestriction snap_angle = TraceAngleRestriction.NONE;
         
         if (next_token == DsnKeyword.FORTYFIVE_DEGREE)
            {
            snap_angle = TraceAngleRestriction.FORTYFIVE;
            }
         else if (next_token == DsnKeyword.NONE)
            {
            snap_angle = TraceAngleRestriction.NONE;
            }
         else
            {
            System.out.println("Structure.read_snap_angle_scope: unexpected token");
            return null;
            }
         
         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.out.println("Structure.read_selection_layer_scop: closing bracket expected");
            return null;
            }
         return snap_angle;
         }
      catch (java.io.IOException e)
         {
         System.out.println("Structure.read_snap_angl: IO error scanning file");
         return null;
         }
      }

   public static void write_snap_angle(IndentFileWriter p_file, board.varie.TraceAngleRestriction p_angle_restriction) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("snap_angle ");
      p_file.new_line();

      if (p_angle_restriction.is_limit_45())
         {
         p_file.write("fortyfive_degree");
         }
      else
         {
         p_file.write("none");
         }
      p_file.end_scope();
      }

   private boolean calc_board_outline (DsnReadScopeParameters p_par, DsnBoardConstruction p_board_construction_info)
      {
      // we already have a board outline
      if (p_board_construction_info.bounding_shape != null) return true;
   
      if (p_board_construction_info.outline_shapes.isEmpty())
         {
         // happens if the boundary shape with layer PCB is missing
         System.err.println("Structure.create_board: outline missing");
         p_par.board_outline_ok = false;
         return false;
         }
      
      DsnRectangle bounding_box = null;

      for ( DsnShape a_shape : p_board_construction_info.outline_shapes )
         {
         DsnRectangle a_boc = a_shape.bounding_box();
         
         if ( bounding_box == null ) 
            bounding_box = a_boc;
         else
            bounding_box = bounding_box.union(a_boc);
         }
      
      p_board_construction_info.bounding_shape = bounding_box;

      return true;
      }

   private BrdLayerStructure calc_board_layers (DsnReadScopeParameters p_par, DsnBoardConstruction p_board_construction_info)
      {
      int layer_count = p_board_construction_info.layer_info.size();

      if (layer_count <= 0)
         {
         System.err.println(classname+"calc_board_layers: layers missing in structure scope");
         return null;
         }

      return new BrdLayerStructure(p_board_construction_info.layer_info);
      }

   private boolean create_board(DsnReadScopeParameters p_par, DsnBoardConstruction p_board_construction_info)
      {
      if ( ! calc_board_outline(p_par, p_board_construction_info)) return false;

      BrdLayerStructure board_layer_structure = calc_board_layers(p_par, p_board_construction_info);
      
      if ( board_layer_structure == null ) return false;      

      p_par.layer_structure = new DsnLayerStructure(p_board_construction_info.layer_info);
      
      DsnRectangle bounding_box = p_board_construction_info.bounding_shape.bounding_box();
      
      // Calculate an appropriate scaling between dsn coordinates and board coordinates.
      int scale_factor = Math.max(p_par.dsn_resolution, 1);

      double max_coor = 0;
      for (int i = 0; i < 4; ++i)
         {
         max_coor = Math.max(max_coor, Math.abs(bounding_box.coor[i] * p_par.dsn_resolution));
         }
      if (max_coor == 0)
         {
         p_par.board_outline_ok = false;
         return false;
         }
      
      // make scale factor smaller, if there is a danger of integer overflow.
      while (5 * max_coor >= freert.planar.PlaLimits.CRIT_INT)
         {
         scale_factor /= 10;
         max_coor /= 10;
         }

      // Qui ci sarebbe da vedere se si riesce ad avere una scala al decimo di micrometro.... damiano pippo
      p_par.coordinate_transform = new DsnCoordinateTransform(scale_factor, 0, 0);

      ShapeTileBox bounds = bounding_box.transform_to_board(p_par.coordinate_transform);
      bounds = bounds.offset(1000);

      Collection<ShapeSegments> board_outline_shapes = new LinkedList<ShapeSegments>();
      for (DsnShape curr_shape : p_board_construction_info.outline_shapes)
         {
         if (curr_shape instanceof DsnPolygonPath)
            {
            DsnPolygonPath curr_path = (DsnPolygonPath) curr_shape;
            if (curr_path.width != 0)
               {
               // set the width to 0, because the offset function used in transform_to_board is not implemented for shapes, which are not convex.
               curr_shape = new DsnPolygonPath(curr_path.layer, 0, curr_path.coordinate_arr);
               }
            }
         ShapeSegments curr_board_shape = (ShapeSegments) curr_shape.transform_to_board(p_par.coordinate_transform);

         if ( ! curr_board_shape.dimension().is_lt_point() )
            {
            board_outline_shapes.add(curr_board_shape);
            }
         }
      
      if (board_outline_shapes.isEmpty())
         {
         // construct an outline from the bounding_shape, if the outline is missing.
         ShapeSegments curr_board_shape = (ShapeSegments) p_board_construction_info.bounding_shape.transform_to_board(p_par.coordinate_transform);
         board_outline_shapes.add(curr_board_shape);
         }
      
      Collection<ShapeSegments> hole_shapes = separate_holes(board_outline_shapes);
      
      BoardRules board_rules = new BoardRules(p_par.i_board, board_layer_structure );
      
      DsnParserInfo specctra_parser_info = new DsnParserInfo(p_par);
      
      HostCom board_communication = new HostCom(p_par.dsn_unit_meas, p_par.dsn_resolution, specctra_parser_info, p_par.coordinate_transform, p_par.item_id_no_generator);

      ShapeSegments[] outline_shape_arr = new ShapeSegments[board_outline_shapes.size()];
      Iterator<ShapeSegments> it2 = board_outline_shapes.iterator();
      for (int i = 0; i < outline_shape_arr.length; ++i)
         {
         outline_shape_arr[i] = it2.next();
         }
      update_board_rules(p_par, p_board_construction_info, board_rules);
      board_rules.set_trace_snap_angle(p_par.snap_angle);

      p_par.i_board.create_board(bounds, board_layer_structure, outline_shape_arr, p_board_construction_info.outline_clearance_class_name, board_rules, board_communication );

      RoutingBoard board = p_par.i_board.get_routing_board();

      // Insert the holes in the board outline as keepouts.
      for (ShapeSegments curr_outline_hole : hole_shapes)
         {
         for (int i = 0; i < board_layer_structure.size(); ++i)
            {
            board.insert_obstacle(curr_outline_hole, i, 0, ItemFixState.SYSTEM_FIXED);
            }
         }

      return true;
      }

   // Check, if a conduction area is inserted on each plane,
   // and insert evtl. a conduction area

   private static void insert_missing_power_planes(Collection<DsnLayer> p_layer_info, DsnNetList p_netlist, RoutingBoard p_board)
      {
      Collection<board.items.BrdAreaConduction> conduction_areas = p_board.get_conduction_areas();
      for (DsnLayer curr_layer : p_layer_info)
         {
         if (curr_layer.is_signal)
            {
            continue;
            }
         boolean conduction_area_found = false;
         for (board.items.BrdAreaConduction curr_conduction_area : conduction_areas)
            {
            if (curr_conduction_area.get_layer() == curr_layer.layer_no)
               {
               conduction_area_found = true;
               break;
               }
            }
         if (!conduction_area_found && !curr_layer.net_names.isEmpty())
            {
            String curr_net_name = curr_layer.net_names.iterator().next();
            DsnNetId curr_net_id = new DsnNetId(curr_net_name, 1);
            if (!p_netlist.contains(curr_net_id))
               {
               DsnNet new_net = p_netlist.add_net(curr_net_id);
               if (new_net != null)
                  {
                  p_board.brd_rules.nets.add(new_net.id.name, new_net.id.subnet_number, true);
                  }
               }
            freert.rules.RuleNet curr_net = p_board.brd_rules.nets.get(curr_net_id.name, curr_net_id.subnet_number);
               {
               if (curr_net == null)
                  {
                  System.out.println("Structure.insert_missing_power_planes: net not found");
                  continue;
                  }
               }
            NetNosList net_numbers = new NetNosList(curr_net.net_number);
            
            p_board.insert_conduction_area(p_board.bounding_box, curr_layer.layer_no, net_numbers, BoardRules.clearance_null_idx, false, ItemFixState.SYSTEM_FIXED);
            }
         }
      }

   /**
    * Calculates shapes in p_outline_shapes, which are holes in the outline and
    * returns them in the result list.
    */
   private static Collection<ShapeSegments> separate_holes(Collection<ShapeSegments> p_outline_shapes)
      {
      DsnStructureOutlineShape shape_arr[] = new DsnStructureOutlineShape[p_outline_shapes.size()];
      Iterator<ShapeSegments> it = p_outline_shapes.iterator();
      for (int i = 0; i < shape_arr.length; ++i)
         {
         shape_arr[i] = new DsnStructureOutlineShape(it.next());
         }
      for (int i = 0; i < shape_arr.length; ++i)
         {
         DsnStructureOutlineShape curr_shape = shape_arr[i];
         for (int j = 0; j < shape_arr.length; ++j)
            {
            // check if shape_arr[j] may be contained in shape_arr[i]
            DsnStructureOutlineShape other_shape = shape_arr[j];
            if (i == j || other_shape.is_hole)
               {
               continue;
               }
            if (!other_shape.bounding_box.contains(curr_shape.bounding_box))
               {
               continue;
               }
            curr_shape.is_hole = other_shape.contains_all_corners(curr_shape);
            }
         }
      Collection<ShapeSegments> hole_list = new LinkedList<ShapeSegments>();
      for (int i = 0; i < shape_arr.length; ++i)
         {
         if (shape_arr[i].is_hole)
            {
            p_outline_shapes.remove(shape_arr[i].shape);
            hole_list.add(shape_arr[i].shape);
            }
         }
      return hole_list;
      }

   /**
    * Updates the board rules from the rules read from the dsn file.
    */
   private static void update_board_rules(DsnReadScopeParameters p_par, DsnBoardConstruction p_board_construction_info, BoardRules p_board_rules)
      {
      boolean smd_to_turn_gap_found = false;
      // update the clearance matrix
      Iterator<DsnRule> it = p_board_construction_info.default_rules.iterator();
      while (it.hasNext())
         {
         DsnRule curr_ob = it.next();
         if (curr_ob instanceof DsnRuleClearance)
            {
            DsnRuleClearance curr_rule = (DsnRuleClearance) curr_ob;
            if (set_clearance_rule(curr_rule, -1, p_par.coordinate_transform, p_board_rules, p_par.string_quote))
               {
               smd_to_turn_gap_found = true;
               }
            }
         }
      // update width rules
      it = p_board_construction_info.default_rules.iterator();
      while (it.hasNext())
         {
         Object curr_ob = it.next();
         if (curr_ob instanceof DsnRuleWidth)
            {
            double wire_width = ((DsnRuleWidth) curr_ob).value;
            int trace_halfwidth = (int) Math.round(p_par.coordinate_transform.dsn_to_board(wire_width) / 2);
            p_board_rules.set_default_trace_half_widths(trace_halfwidth);
            }
         }
      Iterator<DsnLayerRule> it3 = p_board_construction_info.layer_dependent_rules.iterator();
      while (it3.hasNext())
         {
         DsnLayerRule layer_rule = it3.next();
         int layer_no = p_par.layer_structure.get_no(layer_rule.layer_name);
         if (layer_no < 0)
            {
            continue;
            }
         Iterator<DsnRule> it2 = layer_rule.rule.iterator();
         while (it2.hasNext())
            {
            DsnRule curr_ob = it2.next();
            if (curr_ob instanceof DsnRuleWidth)
               {
               double wire_width = ((DsnRuleWidth) curr_ob).value;
               int trace_halfwidth = (int) Math.round(p_par.coordinate_transform.dsn_to_board(wire_width) / 2);
               p_board_rules.set_default_trace_half_width(layer_no, trace_halfwidth);
               }
            else if (curr_ob instanceof DsnRuleClearance)
               {
               DsnRuleClearance curr_rule = (DsnRuleClearance) curr_ob;
               set_clearance_rule(curr_rule, layer_no, p_par.coordinate_transform, p_board_rules, p_par.string_quote);
               }
            }
         }
      if (!smd_to_turn_gap_found)
         {
         p_board_rules.set_pin_edge_to_turn_dist(p_board_rules.get_min_trace_half_width());
         }
      }

   /**
    * Converts a dsn clearance rule into a board clearance rule.
    * If p_layer_no < 0, the rule is set on all layers. 
    * @return true, if the string smd_to_turn_gap was found.
    */
   public static boolean set_clearance_rule(DsnRuleClearance p_rule, int p_layer_no, DsnCoordinateTransform p_coordinate_transform, BoardRules p_board_rules, String p_string_quote)
      {
      boolean result = false;
      int curr_clearance = (int) Math.round(p_coordinate_transform.dsn_to_board(p_rule.value));
      if (p_rule.clearance_class_pairs.isEmpty())
         {
         if (p_layer_no < 0)
            {
            p_board_rules.clearance_matrix.set_default_value(curr_clearance);
            }
         else
            {
            p_board_rules.clearance_matrix.set_default_value(p_layer_no, curr_clearance);
            }
         return result;
         }
      if (contains_wire_clearance_pair(p_rule.clearance_class_pairs))
         {
         create_default_clearance_classes(p_board_rules);
         }
      Iterator<String> it = p_rule.clearance_class_pairs.iterator();
      while (it.hasNext())
         {
         String curr_string = it.next();
         if (curr_string.equalsIgnoreCase("smd_to_turn_gap"))
            {
            p_board_rules.set_pin_edge_to_turn_dist(curr_clearance);
            result = true;
            continue;
            }
         String[] curr_pair;
         if (curr_string.startsWith(p_string_quote))
            {
            // split at the second occurance of p_string_quote
            curr_string = curr_string.substring(p_string_quote.length());
            curr_pair = curr_string.split(p_string_quote, 2);
            if (curr_pair.length != 2 || !curr_pair[1].startsWith("_"))
               {
               System.out.println("Structure.set_clearance_rule: '_' exprcted");
               continue;
               }
            curr_pair[1] = curr_pair[1].substring(1);
            }
         else
            {
            curr_pair = curr_string.split("_", 2);
            if (curr_pair.length != 2)
               {
               // pairs with more than 1 underline like smd_via_same_net are not
               // implemented
               continue;
               }
            }

         if (curr_pair[1].startsWith(p_string_quote) && curr_pair[1].endsWith(p_string_quote))
            {
            // remove the quotes
            curr_pair[1] = curr_pair[1].substring(1, curr_pair[1].length() - 1);
            }
         else
            {
            String[] tmp_pair = curr_pair[1].split("_", 2);
            if (tmp_pair.length != 1)
               {
               // pairs with more than 1 underline like smd_via_same_net are not
               // implemented
               continue;
               }
            }

         int first_class_no;
         if (curr_pair[0].equals("wire"))
            {
            first_class_no = 1; // default class
            }
         else
            {
            first_class_no = p_board_rules.clearance_matrix.get_no(curr_pair[0]);
            }
         if (first_class_no < 0)
            {
            first_class_no = append_clearance_class(p_board_rules, curr_pair[0]);
            }
         int second_class_no;
         if (curr_pair[1].equals("wire"))
            {
            second_class_no = 1; // default class
            }
         else
            {
            second_class_no = p_board_rules.clearance_matrix.get_no(curr_pair[1]);
            }
         if (second_class_no < 0)
            {
            second_class_no = append_clearance_class(p_board_rules, curr_pair[1]);
            }
         if (p_layer_no < 0)
            {
            p_board_rules.clearance_matrix.set_value(first_class_no, second_class_no, curr_clearance);
            p_board_rules.clearance_matrix.set_value(second_class_no, first_class_no, curr_clearance);
            }
         else
            {
            p_board_rules.clearance_matrix.set_value(first_class_no, second_class_no, p_layer_no, curr_clearance);
            p_board_rules.clearance_matrix.set_value(second_class_no, first_class_no, p_layer_no, curr_clearance);
            }
         }
      return result;
      }

   static boolean contains_wire_clearance_pair(Collection<String> p_clearance_pairs)
      {
      for (String curr_pair : p_clearance_pairs)
         {
         if (curr_pair.startsWith("wire_") || curr_pair.endsWith("_wire"))
            {
            return true;
            }
         }
      return false;
      }

   static private void create_default_clearance_classes(BoardRules p_board_rules)
      {
      append_clearance_class(p_board_rules, "via");
      append_clearance_class(p_board_rules, "smd");
      append_clearance_class(p_board_rules, "pin");
      append_clearance_class(p_board_rules, "area");
      }

   static private int append_clearance_class(BoardRules p_board_rules, String p_name)
      {
      p_board_rules.clearance_matrix.append_class(p_name);
      int result = p_board_rules.clearance_matrix.get_no(p_name);
      freert.rules.NetClass default_net_class = p_board_rules.get_default_net_class();
      if (p_name.equals("via"))
         {
         default_net_class.default_item_clearance_classes.set(ItemClass.VIA, result);
         }
      else if (p_name.equals("pin"))
         {
         default_net_class.default_item_clearance_classes.set(ItemClass.PIN, result);
         }
      else if (p_name.equals("smd"))
         {
         default_net_class.default_item_clearance_classes.set(ItemClass.SMD, result);
         }
      else if (p_name.equals("area"))
         {
         default_net_class.default_item_clearance_classes.set(ItemClass.AREA, result);
         }
      return result;
      }

   /**
    * Returns true, if all clearance values on the 2 input layers are equal.
    */
   private static boolean clearance_equals(freert.rules.ClearanceMatrix p_cl_matrix, int p_layer_1, int p_layer_2)
      {
      if (p_layer_1 == p_layer_2)
         {
         return true;
         }
      for (int i = 1; i < p_cl_matrix.get_class_count(); ++i)
         {
         for (int j = i; j < p_cl_matrix.get_class_count(); ++j)
            {
            if (p_cl_matrix.value_at(i, j, p_layer_1) != p_cl_matrix.value_at(i, j, p_layer_2))
               {
               return false;
               }
            }
         }
      return true;
      }

   private static boolean insert_keepout(DsnScopeArea p_area, DsnReadScopeParameters p_par, KeepoutType p_keepout_type, ItemFixState p_fixed_state)
      {
      freert.planar.PlaArea keepout_area = DsnShape.transform_area_to_board(p_area.shape_list, p_par.coordinate_transform);
      if ( ! keepout_area.dimension().is_area() )
         {
         System.out.println("Structure.insert_keepout: keepout is not an area");
         return true;
         }
      RoutingBoard board = p_par.i_board.get_routing_board();
      if (board == null)
         {
         System.out.println("Structure.insert_keepout: board not initialized");
         return false;
         }
      DsnLayer curr_layer = (p_area.shape_list.iterator().next()).layer;
      if (curr_layer == DsnLayer.SIGNAL)
         {
         for (int i = 0; i < board.get_layer_count(); ++i)
            {
            if (p_par.layer_structure.arr[i].is_signal)
               {
               insert_keepout(board, keepout_area, i, p_area.clearance_class_name, p_keepout_type, p_fixed_state);
               }
            }
         }
      else if (curr_layer.layer_no >= 0)
         {
         insert_keepout(board, keepout_area, curr_layer.layer_no, p_area.clearance_class_name, p_keepout_type, p_fixed_state);
         }
      else
         {
         System.out.println("Structure.insert_keepout: unknown layer name");
         return false;
         }

      return true;
      }

   private static void insert_keepout(RoutingBoard p_board, freert.planar.PlaArea p_area, int p_layer, String p_clearance_class_name, KeepoutType p_keepout_type, ItemFixState p_fixed_state)
      {
      int clearance_class_no;
      if (p_clearance_class_name == null)
         {
         clearance_class_no = p_board.brd_rules.get_default_net_class().default_item_clearance_classes.get(ItemClass.AREA);
         }
      else
         {
         clearance_class_no = p_board.brd_rules.clearance_matrix.get_no(p_clearance_class_name);
         if (clearance_class_no < 0)
            {
            System.out.println("Keepout.insert_leepout: clearance class not found");
            clearance_class_no = BoardRules.clearance_null_idx;
            }
         }
      if (p_keepout_type == KeepoutType.via_keepout)
         {
         p_board.insert_via_obstacle(p_area, p_layer, clearance_class_no, p_fixed_state);
         }
      else if (p_keepout_type == KeepoutType.place_keepout)
         {
         p_board.insert_component_obstacle(p_area, p_layer, clearance_class_no, p_fixed_state);
         }
      else
         {
         p_board.insert_obstacle(p_area, p_layer, clearance_class_no, p_fixed_state);
         }
      }

   enum KeepoutType
      {
      keepout, via_keepout, place_keepout
      }

   }
