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
 */

package board.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import board.BrdFromSide;
import board.RoutingBoard;
import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdAreaConduction;
import board.items.BrdAreaObstacleComp;
import board.items.BrdAreaObstacleVia;
import board.items.BrdItem;
import board.items.BrdTracep;
import board.varie.ItemFixState;
import board.varie.ShapeTraceEntryPoint;
import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.Polyline;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTile;
import freert.varie.NetNosList;

/**
 * Auxiliary class used by the shove functions
 *
 * @author Alfons Wirtz
 */

public final class ShapeTraceEntries
   {
   public final Collection<BrdAbitVia> shove_via_list;
   private final ShapeTile shape;
   private final int layer;
   private final NetNosList own_net_nos;
   private final int cl_class;
   private BrdFromSide from_side;
   private final RoutingBoard board;
   private ShapeTraceEntryPoint list_anchor;
   private int trace_piece_count;
   private int max_stack_level;
   private boolean shape_contains_trace_tails = false;
   private BrdItem found_obstacle = null;
   private static final double c_offset_add = 1;
   
   /**
    * Used for shoving traces and vias out of the input shape. p_from_side.no is the side of p_shape, from where the shove comes. if
    * p_from_side.no < 0, it will be calculated internally.
    */
   public ShapeTraceEntries(ShapeTile p_shape, int p_layer, NetNosList p_own_net_nos, int p_cl_type, BrdFromSide p_from_side, RoutingBoard p_board)
      {
      shape = p_shape;
      layer = p_layer;
      own_net_nos = p_own_net_nos;
      cl_class = p_cl_type;
      from_side = p_from_side;
      board = p_board;
      list_anchor = null;
      trace_piece_count = 0;
      max_stack_level = 0;
      shove_via_list = new java.util.LinkedList<BrdAbitVia>();
      }

   /**
    * Stores traces and vias in p_item_list. Returns false, if p_item_list contains obstacles, which cannot be shoved aside. If
    * p_is_pad_check. the check is for vias, otherwise it is for traces. If p_copper_sharing_allowed, overlaps with traces or pads
    * of the own net are allowed.
    */
   public boolean store_items(Collection<BrdItem> p_item_list, boolean p_is_pad_check, boolean p_copper_sharing_allowed)
      {
      Iterator<BrdItem> it = p_item_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();

         if (!p_is_pad_check && curr_item instanceof BrdAreaObstacleVia || curr_item instanceof BrdAreaObstacleComp)
            {
            continue;
            }
         boolean contains_own_net = curr_item.shares_net_no(own_net_nos);
         if (curr_item instanceof BrdAreaConduction && (contains_own_net || !((BrdAreaConduction) curr_item).get_is_obstacle()))
            {
            continue;
            }
         if (curr_item.is_shove_fixed() && !contains_own_net)
            {
            found_obstacle = curr_item;
            return false;
            }
         if (curr_item instanceof BrdAbitVia)
            {
            if (p_is_pad_check || !contains_own_net)
               {
               shove_via_list.add((BrdAbitVia) curr_item);
               }
            }
         else if (curr_item instanceof BrdTracep)
            {
            BrdTracep curr_trace = (BrdTracep) curr_item;

            if (!store_trace(curr_trace))
               {
               return false;
               }
            }
         else
            {
            if (contains_own_net)
               {
               if (!p_copper_sharing_allowed)
                  {
                  found_obstacle = curr_item;
                  return false;
                  }
               if (p_is_pad_check && !((curr_item instanceof BrdAbitPin) && ((BrdAbitPin) curr_item).drill_allowed()))
                  {
                  found_obstacle = curr_item;
                  return false;
                  }
               }
            else
               {
               found_obstacle = curr_item;
               return false;
               }
            }
         }
      search_from_side();
      resort();
      if (!calculate_stack_levels())
         {
         return false;
         }
      return true;
      }

   /**
    * calculates the next substitute trace piece. 
    * @return null at he end of the substitute trace list.
    */
   public BrdTracep next_substitute_trace_piece()
      {
      ShapeTraceEntryPoint[] entries = pop_piece();

      if (entries == null) return null;

      BrdTracep curr_trace = entries[0].trace;
      ShapeTile offset_shape;
      ShapeSearchTree search_tree = board.search_tree_manager.get_default_tree();
      if (search_tree.is_clearance_compensation_used())
         {
         double curr_offset = curr_trace.get_compensated_half_width(search_tree) + c_offset_add;
         offset_shape = (ShapeTile) shape.offset(curr_offset);
         }
      else
         {
         // enlarge the shape in 2 steps for symmetry reasons
         offset_shape = (ShapeTile) shape.offset(curr_trace.get_half_width());
         double cl_offset = board.get_clearance(curr_trace.clearance_idx(), cl_class, layer) + c_offset_add;
         offset_shape = (ShapeTile) offset_shape.offset(cl_offset);
         }
      int edge_count = shape.border_line_count();
      int edge_diff = entries[1].edge_no - entries[0].edge_no;

      // calculate the polyline of the substitute trace

      PlaLineInt[] piece_lines = new PlaLineInt[edge_diff + 3];
      // start with the intersecting line of the trace at the start entry.
      piece_lines[0] = entries[0].trace.polyline().plaline(entries[0].trace_line_no);
      // end with the intersecting line of the trace at the end entry
      piece_lines[piece_lines.length - 1] = entries[1].trace.polyline().plaline(entries[1].trace_line_no);
      // fill the interiour lines of piece_lines with the appropriate edge lines of the offset shape
      int curr_edge_no = entries[0].edge_no % edge_count;
      
      for (int index = 1; index < piece_lines.length - 1; ++index)
         {
         piece_lines[index] = offset_shape.border_line(curr_edge_no);
         if (curr_edge_no == edge_count - 1)
            {
            curr_edge_no = 0;
            }
         else
            {
            ++curr_edge_no;
            }
         }
      
      try
         {
         Polyline piece_polyline = new Polyline(piece_lines);
         
         return new BrdTracep(
               piece_polyline, 
               layer, 
               curr_trace.get_half_width(), 
               curr_trace.net_nos,
               curr_trace.clearance_idx(), 
               0, 0, ItemFixState.UNFIXED, board);
         }
      catch ( Exception exc )
         {
         System.err.println("Check this out");
         // no valid trace piece, return the next one
         return next_substitute_trace_piece();
         }
      
      }

   /**
    * returns the maximum recursion depth for shoving the obstacle traces
    */
   public int stack_depth()
      {
      return max_stack_level;
      }

   /**
    * returns the number of substitute trace pieces.
    */
   public int substitute_trace_count()
      {
      return trace_piece_count;
      }

   /**
    * Looks if an unconnected endpoint of a trace of a foreign net is contained in the interiour of the shape.
    */
   public boolean trace_tails_in_shape()
      {
      return shape_contains_trace_tails;
      }

   /**
    * Cuts out all traces in p_item_list out of the stored shape. Traces with net number p_except_net_no are ignored
    */
   public void cutout_traces(Collection<BrdItem> p_item_list)
      {
      Iterator<BrdItem> it = p_item_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item instanceof BrdTracep && !curr_item.shares_net_no(own_net_nos))
            {
            cutout_trace((BrdTracep) curr_item, shape, cl_class);
            }
         }
      }

   /**
    * Returns the item responsible for the failing, if the shove algorithm failed.
    */
   public BrdItem get_found_obstacle()
      {
      return found_obstacle;
      }

   public static void cutout_trace(BrdTracep p_trace, ShapeConvex p_shape, int p_cl_class)
      {
      if (!p_trace.is_on_the_board())
         {
         System.err.println("ShapeTraceEntries.cutout_trace : trace is deleted");
         return;
         }
      
      ShapeConvex offset_shape;
      RoutingBoard board = p_trace.r_board;
      ShapeSearchTree search_tree = board.search_tree_manager.get_default_tree();
      if (search_tree.is_clearance_compensation_used())
         {
         double curr_offset = p_trace.get_compensated_half_width(search_tree) + c_offset_add;
         offset_shape = p_shape.offset(curr_offset);
         }
      else
         {
         // enlarge the shape in 2 steps for symmetry reasons
         double cl_offset = board.get_clearance(p_trace.clearance_idx(), p_cl_class, p_trace.get_layer()) + c_offset_add;
         offset_shape = p_shape.offset(p_trace.get_half_width());
         offset_shape = offset_shape.offset(cl_offset);
         }
      Polyline trace_lines = p_trace.polyline();
      ArrayList<Polyline> pieces = offset_shape.cutout(trace_lines);
      if (pieces.size() == 1 && pieces.get(0) == trace_lines)
         {
         // nothing cut off
         return;
         }
      
      if (pieces.size() == 2 && offset_shape.is_outside(pieces.get(0).corner_first()) && offset_shape.is_outside(pieces.get(1).corner_last()))
         {
         fcutout_trace_fast(p_trace, pieces.get(0), pieces.get(1));
         }
      else
         {
         board.remove_item(p_trace);
         for (int index = 0; index < pieces.size(); ++index)
            {
            board.insert_trace_without_cleaning(
                  pieces.get(index), 
                  p_trace.get_layer(), 
                  p_trace.get_half_width(), 
                  p_trace.net_nos, 
                  p_trace.clearance_idx(), 
                  ItemFixState.UNFIXED);
            }
         }
      }

   /** 
    * Optimized function handling the performance critical standard cutout case 
    */
   private static void fcutout_trace_fast(BrdTracep p_trace, Polyline p_start_piece, Polyline p_end_piece)
      {
      RoutingBoard board = p_trace.r_board;
      
      // clear up autoroute info of this item
      p_trace.art_item_clear();
      
      board.item_list.save_for_undo(p_trace);
      BrdTracep start_piece = new BrdTracep(
            p_start_piece, 
            p_trace.get_layer(), 
            p_trace.get_half_width(), 
            p_trace.net_nos, 
            p_trace.clearance_idx(), 0, 0, ItemFixState.UNFIXED,
            board);

      board.item_list.insert(start_piece);
      start_piece.set_on_the_board(true);

      BrdTracep end_piece = new BrdTracep(
            p_end_piece, 
            p_trace.get_layer(), 
            p_trace.get_half_width(), 
            p_trace.net_nos, 
            p_trace.clearance_idx(), 0, 0, ItemFixState.UNFIXED, board);

      board.item_list.insert(end_piece);
      end_piece.set_on_the_board(true);

      board.search_tree_manager.reuse_entries_after_cutout(p_trace, start_piece, end_piece);
      board.remove_item(p_trace);

      board.observers.notify_new(start_piece);
      board.observers.notify_new(end_piece);
      }

   /**
    * Stores all intersection points of p_trace with the border of the internal shape enlarged by the half width and the clearance
    * of the corresponding trace pen.
    */
   private boolean store_trace(BrdTracep p_trace)
      {
      ShapeSearchTree search_tree = board.search_tree_manager.get_default_tree();
      ShapeTile offset_shape;
      if (search_tree.is_clearance_compensation_used())
         {
         double curr_offset = p_trace.get_compensated_half_width(search_tree) + c_offset_add;
         offset_shape = (ShapeTile) shape.offset(curr_offset);
         }
      else
         {
         // enlarge the shape in 2 steps for symmetry reasons
         double cl_offset = board.get_clearance(p_trace.clearance_idx(), cl_class, p_trace.get_layer()) + c_offset_add;
         offset_shape = (ShapeTile) shape.offset(p_trace.get_half_width());
         offset_shape = (ShapeTile) offset_shape.offset(cl_offset);
         }

      // using enlarge here instead offset causes problems because of a
      // comparison in the constructor of class EntryPoint
      int[][] entries = offset_shape.entrance_points(p_trace.polyline());
      for (int i = 0; i < entries.length; ++i)
         {
         int[] entry_tuple = entries[i];
         PlaPointFloat entry_approx = p_trace.polyline().plaline(entry_tuple[0]).intersection_approx(offset_shape.border_line(entry_tuple[1]));
         
         if ( entry_approx.is_NaN() ) System.err.println("store_trace entry_approx.isNaN, fix it !");
         
         insert_entry_point(p_trace, entry_tuple[0], entry_tuple[1], entry_approx);
         }

      // Look, if an end point of the trace lies in the interiour of the shape. This may be the case, if a via touches the shape

      if ( p_trace.shares_net_no(own_net_nos) )
         {
         found_obstacle = p_trace;
         return true;
         }

      if ( ! p_trace.is_nets_normal() ) return false;

      PlaPoint end_corner = p_trace.corner_first();
      
      Collection<BrdItem> contact_list;
      for (int index = 0; index < 2; ++index)
         {
         if (offset_shape.contains(end_corner))
            {
            if (index == 0)
               {
               contact_list = p_trace.get_start_contacts();
               }
            else
               {
               contact_list = p_trace.get_end_contacts();
               }
            int contact_count = 0;
            boolean store_end_corner = true;

            // check for contact object, which is not shovable
            Iterator<BrdItem> it = contact_list.iterator();
            while (it.hasNext())
               {
               BrdItem contact_item = it.next();
               if (!contact_item.is_route())
                  {
                  found_obstacle = contact_item;
                  return false;
                  }
               
               if (contact_item instanceof BrdTracep)
                  {

                  if (contact_item.is_shove_fixed() || ((BrdTracep) contact_item).get_half_width() != p_trace.get_half_width() || contact_item.clearance_idx() != p_trace.clearance_idx())
                     {
                     if (offset_shape.contains_inside(end_corner))
                        {
                        found_obstacle = contact_item;
                        return false;
                        }
                     }
                  }
               else if (contact_item instanceof BrdAbitVia)
                  {
                  ShapeTile via_shape = ((BrdAbitVia) contact_item).get_tile_shape_on_layer(layer);

                  double via_trace_diff = via_shape.smallest_radius() - p_trace.get_compensated_half_width(search_tree);
                  if (!search_tree.is_clearance_compensation_used())
                     {
                     int via_clearance = board.get_clearance(contact_item.clearance_idx(), cl_class, layer);
                     int trace_clearance = board.get_clearance(p_trace.clearance_idx(), cl_class, layer);
                     if (trace_clearance > via_clearance)
                        {
                        via_trace_diff += via_clearance - trace_clearance;
                        }
                     }
                  if (via_trace_diff < 0)
                     {
                     // the via is smaller than the trace
                     found_obstacle = contact_item;
                     return false;
                     }

                  if (via_trace_diff == 0 && !offset_shape.contains_inside(end_corner))
                     {
                     // the via need not to be shoved
                     store_end_corner = false;
                     }
                  }
               ++contact_count;
               }
            
            if (contact_count == 1 && store_end_corner)
               {
               PlaPoint projection = offset_shape.nearest_border_point(end_corner);
               int projection_side = offset_shape.contains_on_border_line_no(projection);
               int trace_line_segment_no;
               // the following may not be correct because the trace may not conntain a suitable
               // line for the construction oof the end line of the substitute trace.
               if (index == 0)
                  {
                  trace_line_segment_no = 0;
                  }
               else
                  {
                  trace_line_segment_no = p_trace.polyline().plalinelen(-1);
                  }

               if (projection_side >= 0)
                  {
                  insert_entry_point(p_trace, trace_line_segment_no, projection_side, projection.to_float());
                  }
               }
            else if (contact_count == 0 && offset_shape.contains_inside(end_corner))
               {
               shape_contains_trace_tails = true;
               }
            }
         end_corner = p_trace.corner_last();
         }

      found_obstacle = p_trace;
      return true;
      }

   private void search_from_side()
      {
      if (from_side != null && from_side.side_no >= 0)
         {
         return; // from side is already legal
         }
      ShapeTraceEntryPoint curr_node = list_anchor;
      int curr_fromside_no = 0;
      PlaPointFloat curr_entry_approx = null;
      while (curr_node != null)
         {
         if (curr_node.trace.shares_net_no(own_net_nos))
            {
            curr_fromside_no = curr_node.edge_no;
            curr_entry_approx = curr_node.entry_approx;
            break;
            }
         curr_node = curr_node.next;
         }
      from_side = new BrdFromSide(curr_fromside_no, curr_entry_approx);
      }

   /**
    * resorts the intersection points according to from_side_no and removes redundant points
    */
   private void resort()
      {
      int edge_count = shape.border_line_count();
      if (from_side.side_no < 0 || from_side.side_no >= edge_count)
         {
         System.out.println("ShapeTraceEntries.resort: from side not calculated");
         return;
         }
      // resort the intersection points, so that they start in the
      // middle of from_side.
      PlaPointFloat compare_corner_1 = shape.corner_approx(from_side.side_no);
      PlaPointFloat compare_corner_2;
      if (from_side.side_no == edge_count - 1)
         {
         compare_corner_2 = shape.corner_approx(0);
         }
      else
         {
         compare_corner_2 = shape.corner_approx(from_side.side_no + 1);
         }
      double from_point_dist = 0;
      PlaPointFloat from_point_projection = null;
      if (from_side.border_intersection != null)
         {
         from_point_projection = from_side.border_intersection.projection_approx(shape.border_line(from_side.side_no));
         from_point_dist = from_point_projection.dustance_square(compare_corner_1);
         if (from_point_dist >= compare_corner_1.dustance_square(compare_corner_2))
            {
            from_side = new BrdFromSide(from_side.side_no, null);
            }
         }
      // search the first intersection point between the side middle
      // and compare_corner_2
      ShapeTraceEntryPoint curr = list_anchor;
      ShapeTraceEntryPoint prev = null;

      while (curr != null)
         {
         if (curr.edge_no > from_side.side_no)
            {
            break;
            }
         if (curr.edge_no == from_side.side_no)
            {
            if (from_side.border_intersection != null)
               {
               PlaPointFloat curr_projection = curr.entry_approx.projection_approx(shape.border_line(from_side.side_no));
               if (curr_projection.dustance_square(compare_corner_1) >= from_point_dist && curr_projection.dustance_square(from_point_projection) <= curr_projection.dustance_square(compare_corner_1))
                  {
                  break;
                  }
               }
            else
               {
               if (curr.entry_approx.dustance_square(compare_corner_2) <= curr.entry_approx.dustance_square(compare_corner_1))
                  {
                  break;
                  }
               }
            }
         prev = curr;
         curr = prev.next;
         }
      if (curr != null && curr != list_anchor)
         {
         ShapeTraceEntryPoint new_anchor = curr;

         while (curr != null)
            {
            prev = curr;
            curr = prev.next;
            }
         prev.next = list_anchor;
         curr = list_anchor;
         while (curr != new_anchor)
            {
            // add edge_count to curr.side to differentiate points
            // before and after the middle of from_side
            curr.edge_no += edge_count;
            prev = curr;
            curr = prev.next;
            }
         prev.next = null;
         list_anchor = new_anchor;
         }
      // remove intersections between two other intersections of the same
      // connected set, so that only first and last intersection is kept.
      if (list_anchor == null)
         {
         return;
         }
      prev = list_anchor;
      NetNosList prev_net_nos = prev.trace.net_nos;

      curr = list_anchor.next;
      NetNosList curr_net_nos;
      ShapeTraceEntryPoint next;

      if (curr != null)
         {
         curr_net_nos = curr.trace.net_nos;
         next = curr.next;
         }
      else
         {
         next = null;
         curr_net_nos = NetNosList.EMPTY;
         }
      ShapeTraceEntryPoint before_prev = null;
      while (next != null)
         {
         NetNosList next_net_nos = next.trace.net_nos;
         if ( prev_net_nos.net_nos_equal(curr_net_nos) && curr_net_nos.net_nos_equal(next_net_nos))
            {
            prev.next = next;
            }
         else
            {
            before_prev = prev;
            prev = curr;
            prev_net_nos = curr_net_nos;
            }
         curr_net_nos = next_net_nos;
         curr = next;
         next = curr.next;
         }

      // remove nodes of own net at start and end of the list
      if (curr != null && curr_net_nos.net_nos_equal(own_net_nos))
         {
         prev.next = null;
         if ( prev_net_nos.net_nos_equal(own_net_nos))
            {
            if (before_prev != null)
               {
               before_prev.next = null;
               }
            else
               {
               list_anchor = null;
               }
            }
         }

      if (list_anchor != null && list_anchor.trace.nets_equal(own_net_nos))
         {
         list_anchor = list_anchor.next;

         if (list_anchor != null && list_anchor.trace.nets_equal(own_net_nos))
            {
            list_anchor = list_anchor.next;
            }
         }
      }

   private boolean calculate_stack_levels()
      {
      if (list_anchor == null)
         {
         return true;
         }
      ShapeTraceEntryPoint curr_entry = list_anchor;
      NetNosList curr_net_nos = curr_entry.trace.net_nos;
      int curr_level;
      if ( curr_net_nos.net_nos_equal(own_net_nos))
         {
         // ignore own net when calculating the stack level
         curr_level = 0;
         }
      else
         {
         curr_level = 1;
         }

      while (curr_entry != null)
         {
         if (curr_entry.stack_level < 0) // not yet calculated
            {
            ++trace_piece_count;
            curr_entry.stack_level = curr_level;
            if (curr_level > max_stack_level)
               {
               if (max_stack_level > 1)
                  {
                  found_obstacle = curr_entry.trace;
                  }
               max_stack_level = curr_level;
               }
            }

         // set stack level for all entries of the current net;
         ShapeTraceEntryPoint check_entry = curr_entry.next;
         int index_of_next_foreign_set = 0;
         int index_of_last_occurance_of_set = 0;
         int next_index = 0;
         ShapeTraceEntryPoint last_own_entry = null;
         ShapeTraceEntryPoint first_foreign_entry = null;

         while (check_entry != null)
            {
            ++next_index;
            NetNosList check_net_nos = check_entry.trace.net_nos;
            if ( check_net_nos.net_nos_equal(curr_net_nos))
               {
               index_of_last_occurance_of_set = next_index;
               last_own_entry = check_entry;
               check_entry.stack_level = curr_entry.stack_level;
               }

            else if (index_of_next_foreign_set == 0)
               {
               // first occurance of a foreign connected set
               index_of_next_foreign_set = next_index;
               first_foreign_entry = check_entry;
               }
            check_entry = check_entry.next;
            }
         ShapeTraceEntryPoint next_entry = null;

         if (next_index != 0)
            {
            if (index_of_next_foreign_set != 0 && index_of_next_foreign_set < index_of_last_occurance_of_set)
            // raise level
               {
               next_entry = first_foreign_entry;
               if (next_entry.stack_level >= 0) // already calculated
                  {
                  // stack property failes
                  return false;
                  }
               ++curr_level;
               }
            else
               {
               if (index_of_last_occurance_of_set != 0)
                  {
                  next_entry = last_own_entry;
                  }
               else
                  {
                  next_entry = first_foreign_entry;
                  if (next_entry.stack_level >= 0) // already calculated
                     {
                     --curr_level;
                     if (next_entry.stack_level != curr_level)
                        {
                        return false;
                        }
                     }
                  }
               }
            curr_net_nos = next_entry.trace.net_nos;
            // remove all entries between curr_entry and next_entry, because
            // they are irrelevant;
            check_entry = curr_entry.next;
            while (check_entry != next_entry)
               {
               check_entry = check_entry.next;
               }
            curr_entry.next = next_entry;
            curr_entry = next_entry;
            }
         else
            {
            curr_entry = null;
            }
         }
      if (curr_level != 1)
         {
         System.out.println("ShapeTraceEntries.calculate_stack_levels: curr_level inconsistent");
         return false;
         }
      return true;
      }

   /**
    * Pops the next piece with minimal level from the imtersection list Returns null, if the stack is empty. The returned array has
    * 2 elements. The first is the first entry point, and the second is the last entry point of the minimal level.
    */
   private ShapeTraceEntryPoint[] pop_piece()
      {
      if (list_anchor == null)
         {
         if (trace_piece_count != 0)
            {
            System.out.println("ShapeTraceEntries: trace_piece_count is inconsistent");
            }
         return null;
         }
      ShapeTraceEntryPoint first = list_anchor;
      ShapeTraceEntryPoint prev_first = null;

      while (first != null)
         {
         if (first.stack_level == max_stack_level)
            {
            break;
            }
         prev_first = first;
         first = first.next;
         }
      if (first == null)
         {
         System.out.println("ShapeTraceEntries: max_stack_level not found");
         return null;
         }
      ShapeTraceEntryPoint[] result = new ShapeTraceEntryPoint[2];
      result[0] = first;
      ShapeTraceEntryPoint last = first;
      ShapeTraceEntryPoint after_last = first.next;

      while (after_last != null)
         {
         if (after_last.stack_level != max_stack_level || !after_last.trace.nets_equal(first.trace))
            {
            break;
            }
         last = after_last;
         after_last = last.next;
         }
      result[1] = last;

      // remove the nodes from first to last inclusive

      if (prev_first != null)
         {
         prev_first.next = after_last;
         }
      else
         {
         list_anchor = after_last;
         }

      // recalculate max_stack_level;
      max_stack_level = 0;
      ShapeTraceEntryPoint curr = list_anchor;
      while (curr != null)
         {
         if (curr.stack_level > max_stack_level)
            {
            max_stack_level = curr.stack_level;
            }
         curr = curr.next;
         }
      --trace_piece_count;
      
      if (first.trace.nets_equal(own_net_nos))
         {
         // own net is ignored and nay occur only at the lowest level
         result = pop_piece();
         }
      
      return result;
      }

   private void insert_entry_point(BrdTracep p_trace, int p_trace_line_no, int p_edge_no, PlaPointFloat p_entry_approx)
      {
      ShapeTraceEntryPoint new_entry = new ShapeTraceEntryPoint(p_trace, p_trace_line_no, p_edge_no, p_entry_approx);
      ShapeTraceEntryPoint curr_prev = null;
      ShapeTraceEntryPoint curr_next = list_anchor;

      // insert the new entry into the sorted list
      while (curr_next != null)
         {
         if (curr_next.edge_no > new_entry.edge_no)  break;
         
         if (curr_next.edge_no == new_entry.edge_no)
            {
            PlaPointFloat prev_corner = shape.corner_approx(p_edge_no);
            PlaPointFloat next_corner;
            if (p_edge_no == shape.border_line_count() - 1)
               {
               next_corner = shape.corner_approx(0);
               }
            else
               {
               next_corner = shape.corner_approx(new_entry.edge_no + 1);
               }
            
            if (prev_corner.scalar_product(p_entry_approx, next_corner) <= prev_corner.scalar_product(curr_next.entry_approx, next_corner))
            // the projection of the line from prev_corner to p_entry_approx
            // onto the line from prev_corner to next_corner is smaller
            // than the projection of the line from prev_corner to
            // next.entry_approx onto the same line.
               {
               break;
               }
            }
         curr_prev = curr_next;
         curr_next = curr_next.next;
         }
      
      new_entry.next = curr_next;
      
      if (curr_prev != null)
         {
         curr_prev.next = new_entry;
         }
      else
         {
         list_anchor = new_entry;
         }
      }
   

   }