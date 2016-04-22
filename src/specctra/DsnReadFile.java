package specctra;

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
 * Created on 10. Mai 2004, 07:43
 */

import interactive.IteraBoard;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import specctra.varie.DsnReadResult;
import board.BrdLayer;
import board.BrdLayerStructure;
import board.RoutingBoard;
import board.items.BrdAreaConduction;
import board.items.BrdItem;
import board.items.BrdOutline;
import board.items.BrdTrace;
import board.varie.IdGenerator;
import board.varie.TestLevel;

public final class DsnReadFile
   {
   private static final String classname="DsnReadFile.";
   
   private final InputStream input_stream;
   private final IteraBoard board_handling;
   
   public DsnReadFile (IteraBoard p_board_handling,  InputStream p_input_stream)
      {
      board_handling = p_board_handling;
      input_stream = p_input_stream;
      }
   
   /**
    * Creates a routing board from a Specctra dns file. 
    * The parameters p_item_observers and p_item_id_no_generator are used, in case the board is embedded into a host system
    * @throws IOException 
    */
   public DsnReadResult read( IdGenerator p_item_id_no_generator, TestLevel p_test_level) throws IOException
      {
      JflexScanner scanner = new DsnFileScanner(new InputStreamReader(input_stream));
      Object curr_token = null;
      for (int i = 0; i < 3; ++i)
         {
         curr_token = scanner.next_token();

         boolean keyword_ok = true;
         
         if (i == 0)
            {
            keyword_ok = (curr_token == DsnKeyword.OPEN_BRACKET);
            }
         else if (i == 1)
            {
            keyword_ok = (curr_token == DsnKeyword.PCB_SCOPE);
            scanner.yybegin(DsnFileScanner.NAME); // to overread the name of the pcb for i = 2
            }
         if (!keyword_ok)
            {
            throw new IOException(classname+"read: specctra dsn file format expected");
            }
         }
      
      DsnReadScopeParameters read_scope_par = new DsnReadScopeParameters(scanner, board_handling, p_item_id_no_generator, p_test_level);
      boolean read_ok = DsnKeyword.PCB_SCOPE.read_scope(read_scope_par);
      DsnReadResult result;
      if (read_ok)
         {
         result = DsnReadResult.OK;
         if (read_scope_par.autoroute_settings == null)
            {
            // look for power planes with incorrect layer type and adjust autoroute parameters
            adjust_plane_autoroute_settings();
            }
         }
      else if (!read_scope_par.board_outline_ok)
         {
         result = DsnReadResult.OUTLINE_MISSING;
         }
      else
         {
         result = DsnReadResult.ERROR;
         }
      // tests.Validate.check("after reading dsn", read_scope_par.board_handling.get_routing_board());
      return result;
      }

   /**
    * Sets contains_plane to true for nets with a conduction_area covering a large part of a signal layer, if that layer does not
    * contain any traces This is useful in case the layer type was not set correctly to plane in the dsn-file. Returns true, if
    * something was changed.
    */
   private boolean adjust_plane_autoroute_settings()
      {
      RoutingBoard routing_board = board_handling.get_routing_board();
      
      BrdLayerStructure board_layer_structure = routing_board.layer_structure;
      
      if (board_layer_structure.size() <= 2) return false;
      
      // there should be at least one signal layer, right ?
      if ( board_layer_structure.get_first_signal_layer_no() < 0 ) return false;
      
      boolean[] layer_contains_wires_arr = new boolean[board_layer_structure.size()];
      boolean[] changed_layer_arr = new boolean[board_layer_structure.size()];
      
      for (int index = 0; index < layer_contains_wires_arr.length; ++index)
         {
         layer_contains_wires_arr[index] = false;
         changed_layer_arr[index] = false;
         }
      
      Collection<BrdAreaConduction> conduction_area_list = new LinkedList<BrdAreaConduction>();
      Collection<BrdItem> item_list = routing_board.get_items();
      for (BrdItem curr_item : item_list)
         {
         if (curr_item instanceof BrdTrace)
            {
            int curr_layer = ((BrdTrace) curr_item).get_layer();
            layer_contains_wires_arr[curr_layer] = true;
            }
         else if (curr_item instanceof BrdAreaConduction)
            {
            conduction_area_list.add((BrdAreaConduction) curr_item);
            }
         }
      boolean nothing_changed = true;

      BrdOutline board_outline = routing_board.get_outline();
      double board_area = 0;
      for (int i = 0; i < board_outline.shape_count(); ++i)
         {
         freert.planar.ShapeTile[] curr_piece_arr = board_outline.get_shape(i).split_to_convex();
         if (curr_piece_arr != null)
            {
            for (freert.planar.ShapeTile curr_piece : curr_piece_arr)
               {
               board_area += curr_piece.area();
               }
            }
         }
      for (BrdAreaConduction curr_conduction_area : conduction_area_list)
         {
         int layer_no = curr_conduction_area.get_layer();
         if (layer_contains_wires_arr[layer_no])
            {
            continue;
            }
         BrdLayer curr_layer = routing_board.layer_structure.get(layer_no);
 
         if (!curr_layer.is_signal || layer_no == 0 || layer_no == board_layer_structure.size() - 1)
            {
            continue;
            }
         
         freert.planar.ShapeTile[] convex_pieces = curr_conduction_area.get_area().split_to_convex();
         double curr_area = 0;
         for (freert.planar.ShapeTile curr_piece : convex_pieces)
            {
            curr_area += curr_piece.area();
            }
         if (curr_area < 0.5 * board_area)
            {
            // skip conduction areas not covering most of the board
            continue;
            }

         for (int i = 0; i < curr_conduction_area.net_count(); ++i)
            {
            rules.RuleNet curr_net = routing_board.brd_rules.nets.get(curr_conduction_area.get_net_no(i));
            curr_net.set_contains_plane(true);
            nothing_changed = false;
            }

         changed_layer_arr[layer_no] = true;
         if (curr_conduction_area.get_fixed_state().ordinal() < board.varie.ItemFixState.USER_FIXED.ordinal())
            {
            curr_conduction_area.set_fixed_state(board.varie.ItemFixState.USER_FIXED);
            }
         }
      if (nothing_changed)
         {
         return false;
         }
      // Adjust the layer preferred directions in the autoroute settings and deactivate the changed layers.
      autoroute.ArtSettings autoroute_settings = board_handling.itera_settings.autoroute_settings;
      int layer_count = routing_board.get_layer_count();
      boolean curr_preferred_direction_is_horizontal = autoroute_settings.get_preferred_direction_is_horizontal(0);
      for (int i = 0; i < layer_count; ++i)
         {
         if (changed_layer_arr[i])
            {
            autoroute_settings.set_layer_active(i, false);
            }
         else if (autoroute_settings.get_layer_active(i))
            {
            autoroute_settings.set_preferred_direction_is_horizontal(i, curr_preferred_direction_is_horizontal);
            curr_preferred_direction_is_horizontal = !curr_preferred_direction_is_horizontal;
            }
         }
      return true;
      }
   
   
   }
