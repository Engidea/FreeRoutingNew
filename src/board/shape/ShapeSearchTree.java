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
 * Created on 1. September 2004, 10:13
 */
package board.shape;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import main.Ldbg;
import main.Mdbg;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdArea;
import board.items.BrdItem;
import board.items.BrdOutline;
import board.items.BrdTracePolyline;
import board.varie.TraceAngleRestriction;
import freert.planar.PlaDimension;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaShape;
import freert.planar.PlaSide;
import freert.planar.Polyline;
import freert.planar.ShapeBounding;
import freert.planar.ShapeConvex;
import freert.planar.ShapePolyline;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.planar.ShapeTileRegular;
import freert.planar.ShapeTileSimplex;
import freert.rules.ClearanceMatrix;
import freert.varie.UnitMeasure;

/**
 * Elementary geometric search functions making direct use of the MinAreaTree in the package datastructures.
 * this is used are the search tree for free angle routing !!
 * @author Alfons Wirtz
 */
public class ShapeSearchTree extends ShapeTreeMinArea
   {
   protected final RoutingBoard r_board;

   // The clearance class number for which the shapes of this tree is compensated. 
   // If compensated_clearance_class_no = 0, the shapes are not compensated.
   public final int compensated_clearance_class_no;

   /**
    * Creates a new ShapeSearchTree. 
    * p_compensated_clearance_class_no is the clearance class number for which the shapes of this tree is compensated. 
    * If p_compensated_clearance_class_no = 0, the shapes are not compensated
    * Note that for the free angle routing this is the actual class that is created
    */
   public ShapeSearchTree(ShapeBounding p_directions, RoutingBoard p_board, int p_compensated_clearance_class_no)
      {
      super(p_directions);

      r_board = p_board;
      compensated_clearance_class_no = p_compensated_clearance_class_no;
      }

   /**
    * Returns, if for the shapes stored in this tree clearance compensation is used.
    */
   public final boolean is_clearance_compensation_used()
      {
      return compensated_clearance_class_no > 0;
      }

   /**
    * Return the clearance compensation value of p_clearance_class_no to the clearance compensation class of this search tree with
    * on layer p_layer. 
    * @return 0, if no clearance compensation is used for this tree.
    */
   public final int get_clearance_compensation(int p_clearance_class_no, int p_layer)
      {
      if (p_clearance_class_no <= 0) return 0;

      int result = r_board.brd_rules.clearance_matrix.value_at(p_clearance_class_no, compensated_clearance_class_no, p_layer);
      
      result -= r_board.brd_rules.clearance_matrix.clearance_compensation_value(compensated_clearance_class_no, p_layer);
      
      if ( result < 0 ) result = 0;
      
      return result;
      }

   /**
    * Changes the tree entries from p_keep_at_start_count + 1 to new_shape_count - 1 - keep_at_end_count to p_changed_entries.
    * Special implementation for change_trace for performance reasons
    */
   public final void change_entries(BrdTracePolyline p_obj, Polyline p_new_polyline, int p_keep_at_start_count, int p_keep_at_end_count)
      {
      // calculate the shapes of p_new_polyline from keep_at_start_count to
      // new_shape_count - keep_at_end_count - 1;
      int compensated_half_width = p_obj.get_half_width() + get_clearance_compensation(p_obj.clearance_class_no(), p_obj.get_layer());
      ShapeTile[] changed_shapes = offset_shapes(p_new_polyline, compensated_half_width, p_keep_at_start_count, p_new_polyline.plalinelen(-1) - p_keep_at_end_count);
      int old_shape_count = p_obj.tree_shape_count(this);
      int new_shape_count = changed_shapes.length + p_keep_at_start_count + p_keep_at_end_count;
      ShapeTreeLeaf[] new_leaf_arr = new ShapeTreeLeaf[new_shape_count];
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[new_shape_count];
      ShapeTreeLeaf[] old_entries = p_obj.get_search_tree_entries(this);
      for (int i = 0; i < p_keep_at_start_count; ++i)
         {
         new_leaf_arr[i] = old_entries[i];
         new_precalculated_tree_shapes[i] = p_obj.get_tree_shape(this, i);
         }
      for (int i = p_keep_at_start_count; i < old_shape_count - p_keep_at_end_count; ++i)
         {
         remove_leaf(old_entries[i]);
         }
      for (int i = 0; i < p_keep_at_end_count; ++i)
         {
         int new_index = new_shape_count - p_keep_at_end_count + i;
         int old_index = old_shape_count - p_keep_at_end_count + i;

         new_leaf_arr[new_index] = old_entries[old_index];
         new_leaf_arr[new_index].shape_index_in_object = new_index;
         new_precalculated_tree_shapes[new_index] = p_obj.get_tree_shape(this, old_index);
         }

      // correct the precalculated tree shapes first, because it is used in this.insert
      for (int i = p_keep_at_start_count; i < new_shape_count - p_keep_at_end_count; ++i)
         {
         new_precalculated_tree_shapes[i] = changed_shapes[i - p_keep_at_start_count];
         }
      p_obj.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      for (int i = p_keep_at_start_count; i < new_shape_count - p_keep_at_end_count; ++i)
         {
         new_leaf_arr[i] = insert(p_obj, i);
         }
      p_obj.set_search_tree_entries(new_leaf_arr, this);
      }

   /**
    * Merges the tree entries from p_from_trace in front of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public final void merge_entries_in_front(BrdTracePolyline p_from_trace, BrdTracePolyline p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      int compensated_half_width = p_to_trace.get_half_width() + get_clearance_compensation(p_to_trace.clearance_class_no(), p_to_trace.get_layer());
      ShapeTile[] link_shapes = offset_shapes(p_joined_polyline, compensated_half_width, p_from_entry_no, p_to_entry_no);
      boolean change_order = p_from_trace.first_corner().equals(p_to_trace.first_corner());
      // remove the last or first tree entry from p_from_trace and the
      // first tree entry from p_to_trace, because they will be replaced by
      // the new link entries.
      int from_shape_count_minus_1 = p_from_trace.tile_shape_count() - 1;
      int remove_no;
      if (change_order)
         {
         remove_no = 0;
         }
      else
         {
         remove_no = from_shape_count_minus_1;
         }
      ShapeTreeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
      ShapeTreeLeaf[] to_trace_entries = p_to_trace.get_search_tree_entries(this);
      remove_leaf(from_trace_entries[remove_no]);
      remove_leaf(to_trace_entries[0]);
      int new_shape_count = from_trace_entries.length + link_shapes.length + to_trace_entries.length - 2;
      ShapeTreeLeaf[] new_leaf_arr = new ShapeTreeLeaf[new_shape_count];
      int old_to_shape_count = to_trace_entries.length;
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[new_shape_count];
      // transfer the tree entries except the last or first from p_from_trace to p_to_trace
      for (int i = 0; i < from_shape_count_minus_1; ++i)
         {
         int from_no;
         if (change_order)
            {
            from_no = from_shape_count_minus_1 - i;
            }
         else
            {
            from_no = i;
            }
         new_precalculated_tree_shapes[i] = p_from_trace.get_tree_shape(this, from_no);
         new_leaf_arr[i] = from_trace_entries[from_no];
         new_leaf_arr[i].object = p_to_trace;
         new_leaf_arr[i].shape_index_in_object = i;
         }
      for (int i = 1; i < old_to_shape_count; ++i)
         {
         int curr_ind = from_shape_count_minus_1 + link_shapes.length + i - 1;
         new_precalculated_tree_shapes[curr_ind] = p_to_trace.get_tree_shape(this, i);
         new_leaf_arr[curr_ind] = to_trace_entries[i];
         new_leaf_arr[curr_ind].shape_index_in_object = curr_ind;
         }

      // correct the precalculated tree shapes first, because it is used in this.insert
      for (int i = 0; i < link_shapes.length; ++i)
         {
         int curr_ind = from_shape_count_minus_1 + i;
         new_precalculated_tree_shapes[curr_ind] = link_shapes[i];
         }
      p_to_trace.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      // create the new link entries
      for (int i = 0; i < link_shapes.length; ++i)
         {
         int curr_ind = from_shape_count_minus_1 + i;
         new_leaf_arr[curr_ind] = insert(p_to_trace, curr_ind);
         }

      p_to_trace.set_search_tree_entries(new_leaf_arr, this);
      }

   /**
    * Merges the tree entries from p_from_trace to the end of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public final void merge_entries_at_end(BrdTracePolyline p_from_trace, BrdTracePolyline p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      int compensated_half_width = p_to_trace.get_half_width() + get_clearance_compensation(p_to_trace.clearance_class_no(), p_to_trace.get_layer());
      ShapeTile[] link_shapes = offset_shapes(p_joined_polyline, compensated_half_width, p_from_entry_no, p_to_entry_no);
      boolean change_order = p_from_trace.corner_last().equals(p_to_trace.corner_last());
      ShapeTreeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
      ShapeTreeLeaf[] to_trace_entries = p_to_trace.get_search_tree_entries(this);
      // remove the last or first tree entry from p_from_trace and the
      // last tree entry from p_to_trace, because they will be replaced by
      // the new link entries.
      int to_shape_count_minus_1 = p_to_trace.tile_shape_count() - 1;
      remove_leaf(to_trace_entries[to_shape_count_minus_1]);
      int remove_no;
      if (change_order)
         {
         remove_no = p_from_trace.tile_shape_count() - 1;
         }
      else
         {
         remove_no = 0;
         }
      remove_leaf(from_trace_entries[remove_no]);
      int new_shape_count = from_trace_entries.length + link_shapes.length + to_trace_entries.length - 2;
      ShapeTreeLeaf[] new_leaf_arr = new ShapeTreeLeaf[new_shape_count];
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[new_shape_count];
      // transfer the tree entries except the last from the old shapes
      // of p_to_trace to the new shapes of p_to_trace
      for (int i = 0; i < to_shape_count_minus_1; ++i)
         {
         new_precalculated_tree_shapes[i] = p_to_trace.get_tree_shape(this, i);
         new_leaf_arr[i] = to_trace_entries[i];
         }

      for (int i = 1; i < from_trace_entries.length; ++i)
         {
         int curr_ind = to_shape_count_minus_1 + link_shapes.length + i - 1;
         int from_no;
         if (change_order)
            {
            from_no = from_trace_entries.length - i - 1;
            }
         else
            {
            from_no = i;
            }
         new_precalculated_tree_shapes[curr_ind] = p_from_trace.get_tree_shape(this, from_no);
         new_leaf_arr[curr_ind] = from_trace_entries[from_no];
         new_leaf_arr[curr_ind].object = p_to_trace;
         new_leaf_arr[curr_ind].shape_index_in_object = curr_ind;
         }

      // correct the precalculated tree shapes first, because it is used in this.insert
      for (int i = 0; i < link_shapes.length; ++i)
         {
         int curr_ind = to_shape_count_minus_1 + i;
         new_precalculated_tree_shapes[curr_ind] = link_shapes[i];
         }
      p_to_trace.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      // create the new link entries
      for (int i = 0; i < link_shapes.length; ++i)
         {
         int curr_ind = to_shape_count_minus_1 + i;
         new_leaf_arr[curr_ind] = insert(p_to_trace, curr_ind);
         }
      p_to_trace.set_search_tree_entries(new_leaf_arr, this);
      }

   /**
    * Trannsfers tree entries from p_from_trace to p_start and p_end_piece after a moddle piece was cut out. 
    * Special implementation for ShapeTraceEntries.fast_cutout_trace for performance reasoms.
    */
   public final void reuse_entries_after_cutout(BrdTracePolyline p_from_trace, BrdTracePolyline p_start_piece, BrdTracePolyline p_end_piece)
      {
      ShapeTreeLeaf[] start_piece_leaf_arr = new ShapeTreeLeaf[p_start_piece.polyline().plalinelen(-2)];
      ShapeTreeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
      // transfer the entries at the start of p_from_trace to p_start_piece.
      for (int i = 0; i < start_piece_leaf_arr.length - 1; ++i)
         {
         start_piece_leaf_arr[i] = from_trace_entries[i];
         start_piece_leaf_arr[i].object = p_start_piece;
         start_piece_leaf_arr[i].shape_index_in_object = i;
         from_trace_entries[i] = null;
         }
      start_piece_leaf_arr[start_piece_leaf_arr.length - 1] = insert(p_start_piece, start_piece_leaf_arr.length - 1);

      // create the last tree entry of the start piece.

      ShapeTreeLeaf[] end_piece_leaf_arr = new ShapeTreeLeaf[p_end_piece.polyline().plalinelen(-2)];

      // create the first tree entry of the end piece.
      end_piece_leaf_arr[0] = insert(p_end_piece, 0);

      for (int i = 1; i < end_piece_leaf_arr.length; ++i)
         {
         int from_index = from_trace_entries.length - end_piece_leaf_arr.length + i;
         end_piece_leaf_arr[i] = from_trace_entries[from_index];
         end_piece_leaf_arr[i].object = p_end_piece;
         end_piece_leaf_arr[i].shape_index_in_object = i;
         from_trace_entries[from_index] = null;
         }

      p_start_piece.set_search_tree_entries(start_piece_leaf_arr, this);
      p_end_piece.set_search_tree_entries(end_piece_leaf_arr, this);
      }

   /**
    * Puts all items in the tree overlapping with p_shape on layer p_layer into p_obstacles. 
    * If p_layer < 0, the layer is ignored.
    */
   public final TreeSet<ShapeTreeObject> find_overlap_objects(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos)
      {
      TreeSet<ShapeTreeObject> risul_obstacles = new TreeSet<ShapeTreeObject>();

      Collection<ShapeTreeEntry> tree_entries = new LinkedList<ShapeTreeEntry>();

      calc_overlapping_tree_entries(p_shape, p_layer, p_ignore_net_nos, tree_entries);

      Iterator<ShapeTreeEntry> it = tree_entries.iterator();
      
      while (it.hasNext())
         {
         ShapeTreeEntry curr_entry = it.next();

         risul_obstacles.add((ShapeTreeObject) curr_entry.object);
         }
      
      return risul_obstacles;
      }

   /**
    * Returns all SearchTreeObjects on layer p_layer, which overlap with p_shape. If p_layer < 0, the layer is ignored
    */
   public final Set<ShapeTreeObject> find_overlap_objects(ShapeConvex p_shape, int p_layer)
      {
      return find_overlap_objects(p_shape, p_layer, new int[0] );
      }

   /**
    * Puts all tree entries overlapping with p_shape on layer p_layer into the list p_obstacles. 
    * If p_layer < 0, the layer is ignored.
    */
   public final void calc_overlapping_tree_entries(ShapeConvex p_shape, int p_layer, Collection<ShapeTreeEntry> p_risul_tree)
      {
      calc_overlapping_tree_entries(p_shape, p_layer, new int[0], p_risul_tree);
      }

   /**
    * Puts all tree entries overlapping with p_shape on layer p_layer into the list p_obstacles. 
    * If p_layer < 0, the layer is ignored. 
    * tree_entries with object containing a net number of p_ignore_net_nos are ignored.
    */
   public final void calc_overlapping_tree_entries(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos, Collection<ShapeTreeEntry> p_risul_tree)
      {
      if (p_shape == null) return;
      
      if (p_risul_tree == null)
         {
         System.err.println("calc_overlapping_tree_entries p_risul_tree is null");
         return;
         }
      
      ShapeTileRegular bounds = p_shape.bounding_shape(bounding_directions);
      if (bounds == null)
         {
         System.err.println("board.ShapeSearchTree.overlaps: p_shape not bounded");
         return;
         }
      
      Collection<ShapeTreeLeaf> tmp_list = get_overlaps(bounds);
      Iterator<ShapeTreeLeaf> it = tmp_list.iterator();

      boolean is_45_degree = p_shape instanceof ShapeTileOctagon;

      while (it.hasNext())
         {
         ShapeTreeLeaf curr_leaf = it.next();
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_leaf.object;
         int shape_index = curr_leaf.shape_index_in_object;
         boolean ignore_object = p_layer >= 0 && curr_object.shape_layer(shape_index) != p_layer;
         if (!ignore_object)
            {
            for (int i = 0; i < p_ignore_net_nos.length; ++i)
               {
               if (!curr_object.is_obstacle(p_ignore_net_nos[i]))
                  {
                  ignore_object = true;
                  }
               }
            }
         
         if (!ignore_object)
            {
            ShapeTile curr_shape = curr_object.get_tree_shape(this, curr_leaf.shape_index_in_object);
            boolean add_item;
            if (is_45_degree && curr_shape instanceof ShapeTileOctagon)
            // in this case the check for intersection is redundant and
            // therefore skipped for performance reasons
               {
               add_item = true;
               }
            else
               {
               add_item = curr_shape.intersects(p_shape);
               }
            if (add_item)
               {
               ShapeTreeEntry new_entry = new ShapeTreeEntry(curr_object, shape_index);
               p_risul_tree.add(new_entry);
               }
            }
         }
      }

   /**
    * Looks up all entries in the search tree, so that inserting an item with shape p_shape, net number p_net_no, clearance type
    * p_cl_type and layer p_layer whould produce a clearance violation, and puts them into the set p_obstacle_entries. 
    * The elements in p_obstacle_entries are of type TreeEntry. 
    * if p_layer < 0, the layer is ignored. Used only internally, because the clearance compensation is not taken iinnto account.
    */
   public final void find_overlap_tree_entries_with_clearance(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos, int p_cl_type, Collection<ShapeTreeEntry> p_result)
      {
      if (p_shape == null) return;

      if (p_result == null) return;

      ClearanceMatrix cl_matrix = r_board.brd_rules.clearance_matrix;
      
      ShapeTileRegular bounds = p_shape.bounding_shape(bounding_directions);

      if (bounds == null)
         {
         System.out.println("find_overlap_tree_entries_with_clearance: p_shape is not bounded");
         bounds = r_board.get_bounding_box();
         }
      
      int max_clearance = (int) (1.2 * cl_matrix.max_value(p_cl_type, p_layer));
      // search with the bounds enlarged by the maximum clearance to
      // get all candidates for overlap
      // a factor less than sqr2 has evtl. be added because
      // enlarging is not symmetric.
      ShapeTileRegular offset_bounds = (ShapeTileRegular) bounds.offset(max_clearance);
      Collection<ShapeTreeLeaf> tmp_list = get_overlaps(offset_bounds);
      Iterator<ShapeTreeLeaf> it1 = tmp_list.iterator();
      // sort the found items by its clearances tp p_cl_type on layer p_layer
      Set<ShapeSearchTreeEntry> sorted_items = new TreeSet<ShapeSearchTreeEntry>();

      while (it1.hasNext())
         {
         ShapeTreeLeaf curr_leaf = it1.next();
         BrdItem curr_item = (BrdItem) curr_leaf.object;
         int shape_index = curr_leaf.shape_index_in_object;
         boolean ignore_item = p_layer >= 0 && curr_item.shape_layer(shape_index) != p_layer;
         if (!ignore_item)
            {
            for (int i = 0; i < p_ignore_net_nos.length; ++i)
               {
               if (!curr_item.is_obstacle(p_ignore_net_nos[i]))
                  {
                  ignore_item = true;
                  }
               }
            }
         if (!ignore_item)
            {
            int curr_clearance = cl_matrix.value_at(p_cl_type, curr_item.clearance_class_no(), p_layer);
            ShapeSearchTreeEntry sorted_ob = new ShapeSearchTreeEntry(curr_leaf, curr_clearance);
            sorted_items.add(sorted_ob);
            }
         }
      Iterator<ShapeSearchTreeEntry> it = sorted_items.iterator();
      int curr_half_clearance = 0;
      ShapeConvex curr_offset_shape = p_shape;
      while (it.hasNext())
         {
         ShapeSearchTreeEntry tmp_entry = it.next();
         int tmp_half_clearance = tmp_entry.clearance / 2;
         if (tmp_half_clearance != curr_half_clearance)
            {
            curr_half_clearance = tmp_half_clearance;
            curr_offset_shape = (ShapeTile) p_shape.enlarge(curr_half_clearance);
            }
         ShapeTile tmp_shape = tmp_entry.leaf.object.get_tree_shape(this, tmp_entry.leaf.shape_index_in_object);
         // enlarge both item shapes by the half clearance to create
         // symmetry.
         ShapeConvex tmp_offset_shape = (ShapeConvex) tmp_shape.enlarge(curr_half_clearance);
         if (curr_offset_shape.intersects(tmp_offset_shape))
            {
            p_result.add(new ShapeTreeEntry(tmp_entry.leaf.object, tmp_entry.leaf.shape_index_in_object));
            }
         }
      }

   /**
    * Puts all items in the tree overlapping with p_shape on layer p_layer into p_result
    * If p_layer < 0 the layer is ignored.
    * @param p_result a non null Set
    */
   public final void find_overlap_objects_with_clearance(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos, int p_cl_type, Set<ShapeTreeObject> p_result)
      {
      Collection<ShapeTreeEntry> res_tree_entries = new LinkedList<ShapeTreeEntry>();
      
      if (is_clearance_compensation_used())
         {
         calc_overlapping_tree_entries(p_shape, p_layer, p_ignore_net_nos, res_tree_entries);
         }
      else
         {
         find_overlap_tree_entries_with_clearance(p_shape, p_layer, p_ignore_net_nos, p_cl_type, res_tree_entries);
         }
      
      for (ShapeTreeEntry curr_entry : res_tree_entries )
         {
         p_result.add((ShapeTreeObject) curr_entry.object);
         }
      }

   /**
    * Returns items, which overlap with p_shape on layer p_layer inclusive clearance. 
    * p_clearance_class is the index in the clearance matrix, which describes the required clearance restrictions to other items. 
    * The function may also return items which are nearly overlapping, but do not overlap with exact calculation. 
    * If p_layer < 0, the layer is ignored.
    */
   public final Set<BrdItem> find_overlap_items_with_clearance(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos, int p_clearance_class)
      {
      Set<ShapeTreeObject> overlaps = new TreeSet<ShapeTreeObject>();

      find_overlap_objects_with_clearance(p_shape, p_layer, p_ignore_net_nos, p_clearance_class, overlaps);
      
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      for (ShapeTreeObject curr_object : overlaps)
         {
         if ( ! (curr_object instanceof BrdItem) ) continue;

         result.add((BrdItem) curr_object);
         }

      return result;
      }

   /**
    * Returns all objects of class TreeEntry, which overlap with p_shape on layer p_layer inclusive clearance
    *  p_clearance_class is the index in the clearance matrix, which describes the required clearance restrictions to other items. 
    *  If p_layer < 0, the layer is ignored.
    *  This seems one of the main point for the logic, finding out if something overlaps, damiano
    */
   public final Collection<ShapeTreeEntry> find_overlap_tree_entries_with_clearance(ShapeConvex p_shape, int p_layer, int[] p_ignore_net_nos, int p_clearance_class)
      {
      Collection<ShapeTreeEntry> result = new LinkedList<ShapeTreeEntry>();
      
      if ( is_clearance_compensation_used())
         {
         calc_overlapping_tree_entries(p_shape, p_layer, p_ignore_net_nos, result);
         }
      else
         {
         find_overlap_tree_entries_with_clearance(p_shape, p_layer, p_ignore_net_nos, p_clearance_class, result);
         }
      
      return result;
      }

   /**
    * Calculates a new incomplete room with a maximal TileShape contained in the shape of p_room, which may overlap only with items
    * of the input net on the input layer. p_room.get_contained_shape() will be contained in the shape of the result room. If that
    * is not possible, several rooms are returned with shapes, which intersect with p_room.get_contained_shape(). The result room is
    * not yet complete, because its doors are not yet calculated. 
    * If p_ignore_shape != null, objects of type CompleteFreeSpaceExpansionRoom, whose intersection with the shape of p_room is containes in p_ignore_shape, are ignored.
    * Note that this is override in subclasses...
    */
   public Collection<ExpandRoomFreespaceIncomplete> complete_shape(ExpandRoomFreespaceIncomplete p_room, int p_net_no, ShapeTreeObject p_ignore_object, ShapeTile p_ignore_shape)
      {
      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();

      if (p_room.get_contained_shape() == null)
         {
         System.out.println("ShapeSearchTree.complete_shape: p_shape_to_be_contained != null expected");
         return result;
         }
      
      if ( root_node == null) return result;

      ShapeTile start_shape = r_board.get_bounding_box();
      if (p_room.get_shape() != null)
         {
         start_shape = start_shape.intersection(p_room.get_shape());
         }

      ShapeTileRegular bounding_shape = start_shape.bounding_shape(bounding_directions);
      if (start_shape.dimension() == PlaDimension.AREA)
         {
         ExpandRoomFreespaceIncomplete new_room = new ExpandRoomFreespaceIncomplete(start_shape, p_room.get_layer(), p_room.get_contained_shape());
         result.add(new_room);
         }
      
      node_stack.reset();
      node_stack.push(root_node);
      ShapeTreeNode curr_node;
      int room_layer = p_room.get_layer();

      for (;;)
         {
         curr_node = node_stack.pop();
         
         if (curr_node == null) break;
         
         if ( ! curr_node.bounding_shape.intersects(bounding_shape)) continue;
         
         if ( ! (curr_node instanceof ShapeTreeLeaf) )
            {
            node_stack.push(((ShapeTreeNodeInner) curr_node).first_child);
            node_stack.push(((ShapeTreeNodeInner) curr_node).second_child);
            continue;
            }

         ShapeTreeLeaf curr_leaf = (ShapeTreeLeaf) curr_node;
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_leaf.object;
         int shape_index = curr_leaf.shape_index_in_object;
      
         if ( ! (curr_object.is_trace_obstacle(p_net_no) && curr_object.shape_layer(shape_index) == room_layer && curr_object != p_ignore_object)) continue;
         
         ShapeTile curr_object_shape = curr_object.get_tree_shape(this, shape_index);
         LinkedList<ExpandRoomFreespaceIncomplete> new_result = new LinkedList<ExpandRoomFreespaceIncomplete>();
         ShapeTileRegular new_bounding_shape = ShapeTileOctagon.EMPTY;

         for (ExpandRoomFreespaceIncomplete curr_incomplete_room : result)
            {
            boolean something_changed = false;
            ShapeTile intersection = curr_incomplete_room.get_shape().intersection(curr_object_shape);
            if (intersection.dimension() == PlaDimension.AREA)
               {
               boolean ignore_expansion_room = curr_object instanceof ExpandRoomFreespaceComplete && p_ignore_shape != null && p_ignore_shape.contains(intersection);
               // cannot happen in free angle roouting, because then expansion_rooms
               // may not overlap. Therefore that can be removed as soon as special
               // function for 45-degree routing is used.
               if (!ignore_expansion_room)
                  {
                  something_changed = true;
                  new_result.addAll(restrain_shape(curr_incomplete_room, curr_object_shape));
                  for (ExpandRoomFreespaceIncomplete tmp_room : new_result)
                     {
                     new_bounding_shape = new_bounding_shape.union(tmp_room.get_shape().bounding_shape(bounding_directions));
                     }
                  }
               }

            if (!something_changed)
               {
               new_result.add(curr_incomplete_room);
               new_bounding_shape = new_bounding_shape.union(curr_incomplete_room.get_shape().bounding_shape(bounding_directions));
               }
            }
         result = new_result;
         bounding_shape = new_bounding_shape;
         }

      result = divide_large_room(result, r_board.get_bounding_box());
      
      return result;
      }

   /**
    * Restrains the shape of p_incomplete_room to a TileShape, which does not intersect with the interiour of p_obstacle_shape.
    * p_incomplete_room.get_contained_shape() must be contained in the shape of the result room. If that is not possible, several
    * rooms are returned with shapes, which intersect with p_incomplete_room.get_contained_shape().
    */
   private Collection<ExpandRoomFreespaceIncomplete> restrain_shape(ExpandRoomFreespaceIncomplete p_incomplete_room, ShapeTile p_obstacle_shape)
      {
      // Search the edge line of p_obstacle_shape, so that p_shape_to_be_contained are on the right side of this line, and that the line segment
      // intersects with the interiour of p_shape.
      // If there are more than 1 such lines take the line which is furthest away from p_points_to_be_con.tained
      // Then insersect p_shape with the halfplane defined by the opposite of this line.
      
      // otherwise border_lines of lenth 0 for octagons may not be handeled correctly      
      ShapeTileSimplex obstacle_simplex = p_obstacle_shape.to_Simplex();

      // There may be a performance problem, if a point shape is represented as an octagon      
      ShapeTile shape_to_be_contained = p_incomplete_room.get_contained_shape().to_Simplex();
      
      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();
      ShapeTile room_shape = p_incomplete_room.get_shape();
      int layer = p_incomplete_room.get_layer();
      
      if (shape_to_be_contained.is_empty())
         {
         if (r_board.debug(Mdbg.SHAPE, Ldbg.SPC_C) )
            System.out.println("ShapeSearchTree.restrain_shape: p_shape_to_be_contained is empty");

         return result;
         }
      PlaLineInt cut_line = null;
      double cut_line_distance = -1;
      for (int i = 0; i < obstacle_simplex.border_line_count(); ++i)
         {
         PlaSegmentInt curr_line_segment = new PlaSegmentInt(obstacle_simplex, i);
         if (room_shape.is_intersected_interiour_by(curr_line_segment))
            {
            // otherwise curr_object may not touch the intersection of p_shape with the half_plane defined by the cut_line.
            // That may lead to problems when creating the ExpansionRooms.
            PlaLineInt curr_line = obstacle_simplex.border_line(i);

            double curr_min_distance = shape_to_be_contained.distance_to_the_left(curr_line);

            if (curr_min_distance > cut_line_distance)
               {
               cut_line_distance = curr_min_distance;
               cut_line = curr_line.opposite();
               }
            }
         }
      if (cut_line != null)
         {
         ShapeTile result_piece = ShapeTile.get_instance(cut_line);
         if (room_shape != null)
            {
            result_piece = room_shape.intersection(result_piece);
            }
         
         if (result_piece.dimension().is_area() )
            {
            result.add(new ExpandRoomFreespaceIncomplete(result_piece, layer, shape_to_be_contained));
            }
         }
      else
         {
         // There is no cut line, so that all p_shape_to_be_contained is completely on the right side of that line. 
         // Search a cut line, so that at least part of p_shape_to_be_contained is on the right side.
         if (shape_to_be_contained.dimension().is_empty() )
            {
            // There is already a completed expansion room around p_shape_to_be_contained.
            return result;
            }

         for (int i = 0; i < obstacle_simplex.border_line_count(); ++i)
            {
            PlaSegmentInt curr_line_segment = new PlaSegmentInt(obstacle_simplex, i);
            if (room_shape.is_intersected_interiour_by(curr_line_segment))
               {
               PlaLineInt curr_line = obstacle_simplex.border_line(i);
               if (shape_to_be_contained.side_of(curr_line) == PlaSide.COLLINEAR)
                  {
                  // curr_line intersects with the interiour of p_shape_to_be_contained
                  cut_line = curr_line.opposite();
                  break;
                  }
               }
            }

         if (cut_line == null)
            {
            // cut line not found, parts or the whole of p_shape may be already
            // occupied from somewhere else.
            return result;
            }
         // Calculate the new shape to be contained in the result shape.
         ShapeTile cut_half_plane = ShapeTile.get_instance(cut_line);
         ShapeTile new_shape_to_be_contained = shape_to_be_contained.intersection(cut_half_plane);

         ShapeTile result_piece;
         if (room_shape == null)
            {
            result_piece = cut_half_plane;
            }
         else
            {
            result_piece = room_shape.intersection(cut_half_plane);
            }
         if (result_piece.dimension().is_area() )
            {
            result.add(new ExpandRoomFreespaceIncomplete(result_piece, layer, new_shape_to_be_contained));
            }
         ShapeTile opposite_half_plane = ShapeTile.get_instance(cut_line.opposite());
         ShapeTile rest_piece;
         if (room_shape == null)
            {
            rest_piece = opposite_half_plane;
            }
         else
            {
            rest_piece = room_shape.intersection(opposite_half_plane);
            }
         if (rest_piece.dimension().is_area() )
            {
            ShapeTile rest_shape_to_be_contained = shape_to_be_contained.intersection(opposite_half_plane);
            ExpandRoomFreespaceIncomplete rest_incomplete_room = new ExpandRoomFreespaceIncomplete(rest_piece, layer, rest_shape_to_be_contained);
            result.addAll(restrain_shape(rest_incomplete_room, obstacle_simplex));
            }
         }
      return result;
      }

   /**
    * Reduces the first or last shape of p_trace at a tie pin, so that the autoroute algorithm can find a connection for a
    * different net.
    */
   public final void reduce_trace_shape_at_tie_pin(BrdAbitPin p_tie_pin, BrdTracePolyline p_trace)
      {
      ShapeTile pin_shape = p_tie_pin.get_tree_shape_on_layer(this, p_trace.get_layer());
      PlaPointFloat compare_corner;
      int trace_shape_no;
      if (p_trace.first_corner().equals(p_tie_pin.get_center()))
         {
         trace_shape_no = 0;
         compare_corner = p_trace.polyline().corner_approx(1);

         }
      else if (p_trace.corner_last().equals(p_tie_pin.get_center()))
         {
         trace_shape_no = p_trace.corner_count() - 2;
         compare_corner = p_trace.polyline().corner_approx(p_trace.corner_count() - 2);
         }
      else
         {
         return;
         }
      ShapeTile trace_shape = p_trace.get_tree_shape(this, trace_shape_no);
      ShapeTile intersection = trace_shape.intersection(pin_shape);
      
      
      if ( ! intersection.dimension().is_area() )
         {
         return;
         }
      
      
      ShapeTile[] shape_pieces = trace_shape.cutout(pin_shape);
      ShapeTile new_trace_shape = ShapeTileSimplex.EMPTY;
      for (int i = 0; i < shape_pieces.length; ++i)
         {
         if (shape_pieces[i].dimension() == PlaDimension.AREA)
            {
            if (new_trace_shape == ShapeTileSimplex.EMPTY || shape_pieces[i].contains(compare_corner))
               {
               new_trace_shape = shape_pieces[i];
               }
            }
         }
      change_item_shape(p_trace, trace_shape_no, new_trace_shape);
      }

   /**
    * Changes the shape with index p_shape_no of this item to p_new_shape and updates the entry in the tree.
    */
   void change_item_shape(BrdItem p_item, int p_shape_no, ShapeTile p_new_shape)
      {
      ShapeTreeLeaf[] old_entries = p_item.get_search_tree_entries(this);
      ShapeTreeLeaf[] new_leaf_arr = new ShapeTreeLeaf[old_entries.length];
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[old_entries.length];
      remove_leaf(old_entries[p_shape_no]);
      for (int i = 0; i < new_precalculated_tree_shapes.length; ++i)
         {
         if (i == p_shape_no)
            {
            new_precalculated_tree_shapes[i] = p_new_shape;

            }
         else
            {
            new_precalculated_tree_shapes[i] = p_item.get_tree_shape(this, i);
            new_leaf_arr[i] = old_entries[i];
            }
         }
      p_item.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);
      new_leaf_arr[p_shape_no] = insert(p_item, p_shape_no);
      p_item.set_search_tree_entries(new_leaf_arr, this);
      }

   /**
    * This is for a polyline
    */
   public final ShapeTile[] calculate_tree_shapes(BrdTracePolyline p_trace)
      {
      int offset_width = p_trace.get_half_width() + get_clearance_compensation(p_trace.clearance_class_no(), p_trace.get_layer());
      
      ShapeTile[] result = new ShapeTile[p_trace.tile_shape_count()];

      for (int index = 0; index < result.length; ++index)
         {
         result[index] = offset_shape(p_trace.polyline(), offset_width, index);
         }
      return result;
      }

   /**
    * WIll be overrideen under...
    * @param p_drill_item
    * @return
    */
   public ShapeTile[] calculate_tree_shapes(BrdAbit p_drill_item)
      {
      ShapeTile[] result = new ShapeTile[p_drill_item.tile_shape_count()];

      for (int index = 0; index < result.length; ++index)
         {
         PlaShape curr_shape = p_drill_item.get_shape(index);
         if (curr_shape == null)
            {
            result[index] = null;
            }
         else
            {
            ShapeTile curr_tile_shape;
            if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.NINETY_DEGREE)
               {
               curr_tile_shape = curr_shape.bounding_box();
               }
            else if (r_board.brd_rules.get_trace_snap_angle() == TraceAngleRestriction.FORTYFIVE_DEGREE)
               {
               curr_tile_shape = curr_shape.bounding_octagon();
               }
            else
               {
               curr_tile_shape = curr_shape.bounding_tile();
               }
            int offset_width = get_clearance_compensation(p_drill_item.clearance_class_no(), p_drill_item.shape_layer(index));
            if (curr_tile_shape == null)
               {
               System.out.println("ShapeSearchTree.calculate_tree_shapes: shape is null");
               }
            else
               {
               curr_tile_shape = (ShapeTile) curr_tile_shape.enlarge(offset_width);
               }
            result[index] = curr_tile_shape;
            }
         }
      return result;
      }

   /**
    * Careful, overide in subclass, this is for BoardArea
    */
   public ShapeTile[] calculate_tree_shapes(BrdArea p_obstacle_area)
      {
      ShapeTile[] convex_shapes = p_obstacle_area.split_to_convex();

      if (convex_shapes == null) return new ShapeTile[0];
      
      double max_tree_shape_width = 50000;
      
      if (r_board.host_com.host_cad_exists())
         {
         max_tree_shape_width = Math.min(500 * r_board.host_com.get_resolution(UnitMeasure.MIL), max_tree_shape_width);
         // Problem with low resolution on Kicad.
         // Called only for designs from host cad systems because otherwise the old sample.dsn gets to many tree shapes.
         }

      Collection<ShapeTile> tree_shape_list = new LinkedList<ShapeTile>();
      for (int index = 0; index < convex_shapes.length; ++index)
         {
         ShapeTile curr_convex_shape = convex_shapes[index];

         int offset_width = get_clearance_compensation(p_obstacle_area.clearance_class_no(), p_obstacle_area.get_layer());
         curr_convex_shape = (ShapeTile) curr_convex_shape.enlarge(offset_width);
         ShapeTile[] curr_tree_shapes = curr_convex_shape.divide_into_sections(max_tree_shape_width);
         for (int jndex = 0; jndex < curr_tree_shapes.length; ++jndex)
            {
            tree_shape_list.add(curr_tree_shapes[jndex]);
            }
         }
      
      ShapeTile[] result = new ShapeTile[tree_shape_list.size()];
      Iterator<ShapeTile> it = tree_shape_list.iterator();
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = it.next();
         }
      return result;
      }

   /**
    * This is for a board outline
    */
   public ShapeTile[] calculate_tree_shapes(BrdOutline p_board_outline)
      {
      ShapeTile[] result;
      if (p_board_outline.keepout_outside_outline_generated())
         {
         ShapeTile[] convex_shapes = p_board_outline.get_keepout_area().split_to_convex();

         if (convex_shapes == null) return new ShapeTile[0];

         Collection<ShapeTile> tree_shape_list = new LinkedList<ShapeTile>();
         for (int layer_no = 0; layer_no < r_board.layer_structure.size(); ++layer_no)
            {
            for (int index = 0; index < convex_shapes.length; ++index)
               {
               ShapeTile curr_convex_shape = convex_shapes[index];
               int offset_width = get_clearance_compensation(p_board_outline.clearance_class_no(), 0);
               curr_convex_shape = (ShapeTile) curr_convex_shape.enlarge(offset_width);
               tree_shape_list.add(curr_convex_shape);
               }
            }
         result = new ShapeTile[tree_shape_list.size()];
         Iterator<ShapeTile> it = tree_shape_list.iterator();
         for (int index = 0; index < result.length; ++index)
            {
            result[index] = it.next();
            }
         }
      else
         {
         // Only the line shapes of the outline are inserted as obstales into the tree.
         result = new ShapeTile[p_board_outline.line_count() * r_board.layer_structure.size()];
         int half_width = BrdOutline.HALF_WIDTH;
         PlaLineInt[] curr_line_arr = new PlaLineInt[3];
         int curr_no = 0;
         for (int layer_no = 0; layer_no < r_board.layer_structure.size(); ++layer_no)
            {
            for (int shape_no = 0; shape_no < p_board_outline.shape_count(); ++shape_no)
               {
               ShapePolyline curr_outline_shape = p_board_outline.get_shape(shape_no);
               int border_line_count = curr_outline_shape.border_line_count();
               curr_line_arr[0] = curr_outline_shape.border_line(border_line_count - 1);
               for (int i = 0; i < border_line_count; ++i)
                  {
                  curr_line_arr[1] = curr_outline_shape.border_line(i);
                  curr_line_arr[2] = curr_outline_shape.border_line((i + 1) % border_line_count);
                  Polyline tmp_polyline = new Polyline(curr_line_arr);
                  int cmp_value = get_clearance_compensation(p_board_outline.clearance_class_no(), 0);
                  result[curr_no] = tmp_polyline.offset_shape(half_width + cmp_value, 0);
                  ++curr_no;
                  curr_line_arr[0] = curr_line_arr[1];
                  }
               }
            }
         }
      return result;
      }

   /**
    * Used for creating the shapes of a polyline_trace for this tree. Overwritten in derived classes.
    */
   protected ShapeTile offset_shape(Polyline p_polyline, int p_half_width, int p_no)
      {
      return p_polyline.offset_shape(p_half_width, p_no);
      }

   /**
    * Used for creating the shapes of a polyline_trace for this tree. Overwritten in derived classes.
    */
   protected ShapeTile[] offset_shapes(Polyline p_polyline, int p_half_width, int p_from_no, int p_to_no)
      {
      return p_polyline.offset_shapes(p_half_width, p_from_no, p_to_no);
      }


   /**
    * Makes shure that on each layer there will be more than 1 IncompleteFreeSpaceExpansionRoom, 
    * even if there are no objects on the layer. 
    * Otherwise the maze search algprithm gets problems with vias.
    */
   protected Collection<ExpandRoomFreespaceIncomplete> divide_large_room(Collection<ExpandRoomFreespaceIncomplete> p_room_list, ShapeTileBox p_board_bounding_box)
      {
      if (p_room_list.size() != 1) return p_room_list;

      ExpandRoomFreespaceIncomplete curr_room = p_room_list.iterator().next();
      ShapeTileBox room_bounding_box = curr_room.get_shape().bounding_box();
      if (2 * room_bounding_box.height() <= p_board_bounding_box.height() || 2 * room_bounding_box.width() <= p_board_bounding_box.width())
         {
         return p_room_list;
         }
      double max_section_width = 0.5 * Math.max(p_board_bounding_box.height(), p_board_bounding_box.width());
      ShapeTile[] section_arr = curr_room.get_shape().divide_into_sections(max_section_width);
      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();
      for (ShapeTile curr_section : section_arr)
         {
         ShapeTile curr_shape_to_be_contained = curr_section.intersection(curr_room.get_contained_shape());
         ExpandRoomFreespaceIncomplete curr_section_room = new ExpandRoomFreespaceIncomplete(curr_section, curr_room.get_layer(), curr_shape_to_be_contained);
         result.add(curr_section_room);
         }
      return result;
      }

   /**
    * Valiedate the given item
    * @param p_item
    * @return true if it validates ok
    */
   public final boolean validate_ok (BrdItem p_item)
      {
      ShapeTreeLeaf[] curr_tree_entries = p_item.get_search_tree_entries(this);

      for (int index = 0; index < curr_tree_entries.length; ++index)
         {
         ShapeTreeLeaf curr_leaf = curr_tree_entries[index];

         if (curr_leaf.shape_index_in_object != index)
            {
            System.err.println("tree entry inconsistent for Item");
            return false;
            }
         }
      
      return true;
      }

   }
