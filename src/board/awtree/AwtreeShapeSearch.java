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
package board.awtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdArea;
import board.items.BrdItem;
import board.items.BrdOutline;
import board.items.BrdTracep;
import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.planar.PlaDimension;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaShape;
import freert.planar.PlaSide;
import freert.planar.Polyline;
import freert.planar.ShapeConvex;
import freert.planar.ShapeSegments;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.planar.ShapeTileRegular;
import freert.planar.ShapeTileSimplex;
import freert.rules.ClearanceMatrix;
import freert.varie.NetNosList;
import freert.varie.UnitMeasure;

/**
 * Elementary geometric search functions making direct use of the MinAreaTree in the package datastructures.
 * this is used are the search tree for free angle routing !!
 * 
 * Binary search tree for shapes in the plane. The shapes are stored in the leafs of the tree. 
 * The algorithm for storing a new shape is as following. 
 * Starting from the root go to the child, so that the increase of the bounding shape of that child is minimal
 * after adding the new shape, until you reach a leaf. 
 * The use of ShapeDirections to calculate the bounding shape is for historical reasons (coming from a Kd-Tree). 
 * Instead any algorithm to calculate a bounding shape of two input shapes can be used. 
 * The algorithm would of course also work for higher dimensions.
 *
 * @author Alfons Wirtz
 */
public final class AwtreeShapeSearch
   {
   private final RoutingBoard r_board;

   private final AwtreeNodeStack node_stack = new AwtreeNodeStack();
   // the fixed directions for calculating bounding RegularTileShapes of shapes to store in this tree.
   private final AwtreeBoundingOct bounding_directions = AwtreeBoundingOct.INSTANCE;
   // The clearance class number for which the shapes of this tree is compensated, if 0 shapes are not compensated 
   public final int compensated_clearance_class_no;

   // Root node - initially null 
   private AwtreeNode root_node = null;
   // The number of entries stored in the tree
   private int leaf_count = 0;

   /**
    * Creates a new ShapeSearchTree. 
    * p_compensated_clearance_class_no is the clearance class number for which the shapes of this tree is compensated. 
    * If p_compensated_clearance_class_no = 0, the shapes are not compensated
    * Note that for the free angle routing this is the actual class that is created
    */
   public AwtreeShapeSearch(RoutingBoard p_board, int p_compensated_clearance_class_no)
      {
      r_board = p_board;
      compensated_clearance_class_no = p_compensated_clearance_class_no;
      }

   /**
    * Inserts all shapes of p_obj into the tree
    */
   public final void insert(AwtreeObject p_obj)
      {
      int shape_count = p_obj.tree_shape_count(this);
      
      if (shape_count <= 0)  return;

      AwtreeNodeLeaf[] leaf_arr = new AwtreeNodeLeaf[shape_count];

      for (int index = 0; index < shape_count; ++index)
         {
         leaf_arr[index] = insert(p_obj, index);
         }
      
      p_obj.set_search_tree_entries(this, leaf_arr );
      }

   /**
    * Insert a shape - creates a new node with a bounding shape
    * This is possibly the entry point to understand the whole search tree mechanism
    */
   protected final AwtreeNodeLeaf insert(AwtreeObject p_object, int p_index)
      {
      PlaShape object_shape = p_object.get_tree_shape(this, p_index);
      
      if (object_shape == null) 
         {
         System.err.println("ShapeTree.insert: object_shape is null");
         return null;
         }

      ShapeTileRegular bounding_shape = object_shape.bounding_shape(bounding_directions);

      if (bounding_shape == null)
         {
         System.err.println("ShapeTree.insert: bounding_shape is null");
         return null;
         }
      
      // Construct a new KdLeaf and set it up
      AwtreeNodeLeaf new_leaf = new AwtreeNodeLeaf(p_object, p_index, null, bounding_shape);
      
      insert_leaf(new_leaf);
      
      return new_leaf;
      }

   /** 
    * Inserts the leaves of this tree into an array list
    */
   private final ArrayList<AwtreeNodeLeaf> to_array()
      {
      ArrayList<AwtreeNodeLeaf> result = new ArrayList<AwtreeNodeLeaf>(leaf_count);
      
      AwtreeNode curr_node = root_node;
      
      if ( curr_node == null ) return result;
      
      for (;;)
         {
         // go down from curr_node to the left most leaf
         while (curr_node instanceof AwtreeNodeFork)
            {
            curr_node = ((AwtreeNodeFork) curr_node).first_child;
            }
         
         result.add( (AwtreeNodeLeaf) curr_node );

         // go up until parent.second_child != curr_node, which means we came from first_child
         AwtreeNodeFork curr_parent = curr_node.parent;

         while (curr_parent != null && curr_parent.second_child == curr_node)
            {
            curr_node = curr_parent;
            curr_parent = curr_node.parent;
            }
         
         if (curr_parent == null) break;

         curr_node = curr_parent.second_child;
         }
      
      return result;
      }

   /**
    * removes all entries of p_obj in the tree.
    */
   public final void remove(AwtreeNodeLeaf[] p_entries)
      {
      if (p_entries == null) return;

      for (int index = 0; index < p_entries.length; ++index)
         {
         remove_leaf(p_entries[index]);
         }
      }

   /** 
    * @return some statistic information about the tree
    */
   public String statistics()
      {
      ArrayList<AwtreeNodeLeaf> leaf_arr = to_array();
      double cumulative_depth = 0;
      int maximum_depth = 0;
      
      for (AwtreeNodeLeaf a_leaf : leaf_arr )
         {
         if (a_leaf == null ) continue;

         int distance_to_root = a_leaf.distance_to_root();
         cumulative_depth += distance_to_root;
         maximum_depth = Math.max(maximum_depth, distance_to_root);
         }
      
      
      StringBuilder risul = new StringBuilder(1000);
      
      double everage_depth = cumulative_depth / leaf_arr.size();

      risul.append("MinAreaTree: Entry count: "+leaf_arr.size()+"\n");
      risul.append("log: "+Math.round(Math.log(leaf_arr.size()))+"\n");
      risul.append("Everage depth: "+Math.round(everage_depth)+"\n");
      risul.append("Maximum depth: "+maximum_depth+"\n");
      
      return risul.toString();
      }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   /**
    * Calculates the objects in this tree, which overlap with p_shape
    */
   public final Set<AwtreeNodeLeaf> get_overlaps(ShapeTile p_shape)
      {
      Set<AwtreeNodeLeaf> found_overlaps = new TreeSet<AwtreeNodeLeaf>();

      if (root_node == null) return found_overlaps;

      node_stack.reset();
      node_stack.push(root_node);
      
      AwtreeNode curr_node;
      
      for (;;)
         {
         curr_node = node_stack.pop();

         if (curr_node == null) break;

         if ( ! curr_node.bounding_shape.intersects(p_shape)) continue;
         
         if (curr_node instanceof AwtreeNodeLeaf)
            {
            found_overlaps.add((AwtreeNodeLeaf) curr_node);
            }
         else
            {
            node_stack.push(((AwtreeNodeFork) curr_node).first_child);
            node_stack.push(((AwtreeNodeFork) curr_node).second_child);
            }
         }
      
      return found_overlaps;
      }

   private final void insert_leaf(AwtreeNodeLeaf p_leaf)
      {
      leaf_count++;

      if (root_node == null)
         {
         // Tree is empty - just insert the new leaf
         root_node = p_leaf;
         return;
         }

      // Non-empty tree - do a recursive location for leaf replacement
      AwtreeNodeLeaf leaf_to_replace = position_locate(root_node, p_leaf);

      // Construct a new node - whenever a leaf is added so is a new node
      ShapeTileRegular new_bounds = p_leaf.bounding_shape.union(leaf_to_replace.bounding_shape);
      AwtreeNodeFork curr_parent = leaf_to_replace.parent;
      AwtreeNodeFork new_node = new AwtreeNodeFork(new_bounds, curr_parent);

      if (leaf_to_replace.parent != null)
         {
         // Replace the pointer from the parent to the leaf with our new node
         if (leaf_to_replace == curr_parent.first_child)
            curr_parent.first_child = new_node;
         else
            curr_parent.second_child = new_node;
         }
      
      // Update the parent pointers of the old leaf and new leaf to point to new node
      leaf_to_replace.parent = new_node;
      p_leaf.parent = new_node;

      // Insert the children in any order.
      new_node.first_child = leaf_to_replace;
      new_node.second_child = p_leaf;

      if (root_node == leaf_to_replace)
         {
         root_node = new_node;
         }
      }

   private final AwtreeNodeLeaf position_locate(AwtreeNode p_curr_node, AwtreeNodeLeaf p_leaf_to_insert)
      {
      AwtreeNode curr_node = p_curr_node;

      while (!(curr_node instanceof AwtreeNodeLeaf))
         {
         AwtreeNodeFork curr_inner_node = (AwtreeNodeFork) curr_node;
         curr_inner_node.bounding_shape = p_leaf_to_insert.bounding_shape.union(curr_inner_node.bounding_shape);

         // Choose the the child, so that the area increase of that child after taking the union
         // with the shape of p_leaf_to_insert is minimal.

         ShapeTileRegular first_child_shape = curr_inner_node.first_child.bounding_shape;
         ShapeTileRegular union_with_first_child_shape = p_leaf_to_insert.bounding_shape.union(first_child_shape);
         double first_area_increase = union_with_first_child_shape.area() - first_child_shape.area();

         ShapeTileRegular second_child_shape = curr_inner_node.second_child.bounding_shape;
         ShapeTileRegular union_with_second_child_shape = p_leaf_to_insert.bounding_shape.union(second_child_shape);
         double second_area_increase = union_with_second_child_shape.area() - second_child_shape.area();

         if (first_area_increase <= second_area_increase)
            {
            curr_node = curr_inner_node.first_child;
            }
         else
            {
            curr_node = curr_inner_node.second_child;
            }
         }
      return (AwtreeNodeLeaf) curr_node;
      }


   public final void remove_leaf(AwtreeNodeLeaf p_leaf)
      {
      if (p_leaf == null) return;

      // remove the leaf node
      AwtreeNodeFork parent = p_leaf.parent;
      p_leaf.bounding_shape = null;
      p_leaf.parent = null;
      p_leaf.object = null;

      leaf_count--;
      
      if (parent == null)
         {
         // tree gets empty
         root_node = null;
         return;
         }

      // find the other leaf of the parent
      AwtreeNode other_leaf;
      if (parent.second_child == p_leaf)
         {
         other_leaf = parent.first_child;
         }
      else if (parent.first_child == p_leaf)
         {
         other_leaf = parent.second_child;
         }
      else
         {
         System.out.println("MinAreaTree.remove_leaf: parent inconsistent");
         other_leaf = null;
         }
      // link the other leaf to the grand_parent and remove the parent node
      AwtreeNodeFork grand_parent = parent.parent;
      other_leaf.parent = grand_parent;
      if (grand_parent == null)
         {
         // only one leaf left in the tree
         root_node = other_leaf;
         }
      else
         {
         if (grand_parent.second_child == parent)
            {
            grand_parent.second_child = other_leaf;
            }
         else if (grand_parent.first_child == parent)
            {
            grand_parent.first_child = other_leaf;
            }
         else
            {
            System.out.println("MinAreaTree.remove_leaf: grand_parent inconsistent");
            }
         }
      parent.parent = null;
      parent.first_child = null;
      parent.second_child = null;
      parent.bounding_shape = null;

      // recalculate the bounding shapes of the ancestors
      // as long as it gets smaller after removing p_leaf
      AwtreeNodeFork node_to_recalculate = grand_parent;
      while (node_to_recalculate != null)
         {
         ShapeTileRegular new_bounds = node_to_recalculate.second_child.bounding_shape.union(node_to_recalculate.first_child.bounding_shape);
         if (new_bounds.contains(node_to_recalculate.bounding_shape))
            {
            // the new bounds are not smaller, no further recalculate nessesary
            break;
            }
         node_to_recalculate.bounding_shape = new_bounds;
         node_to_recalculate = node_to_recalculate.parent;
         }
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
   public final void change_entries(BrdTracep p_obj, Polyline p_new_polyline, int p_keep_at_start_count, int p_keep_at_end_count)
      {
      // calculate the shapes of p_new_polyline from keep_at_start_count to new_shape_count - keep_at_end_count - 1;
      int compensated_half_width = p_obj.get_half_width() + get_clearance_compensation(p_obj.clearance_idx(), p_obj.get_layer());
      ArrayList<ShapeTile> changed_shapes = offset_shapes(p_new_polyline, compensated_half_width, p_keep_at_start_count, p_new_polyline.plaline_len(-1) - p_keep_at_end_count);
      int old_shape_count = p_obj.tree_shape_count(this);
      int new_shape_count = changed_shapes.size() + p_keep_at_start_count + p_keep_at_end_count;
      AwtreeNodeLeaf[] new_leaf_arr = new AwtreeNodeLeaf[new_shape_count];
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[new_shape_count];
      AwtreeNodeLeaf[] old_entries = p_obj.get_search_tree_entries(this);
      
      for (int index = 0; index < p_keep_at_start_count; ++index)
         {
         new_leaf_arr[index] = old_entries[index];
         new_precalculated_tree_shapes[index] = p_obj.get_tree_shape(this, index);
         }
      
      for (int index = p_keep_at_start_count; index < old_shape_count - p_keep_at_end_count; ++index)
         {
         remove_leaf(old_entries[index]);
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
      for (int index = p_keep_at_start_count; index < new_shape_count - p_keep_at_end_count; ++index)
         {
         new_precalculated_tree_shapes[index] = changed_shapes.get(index - p_keep_at_start_count);
         }
      
      p_obj.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      for (int index = p_keep_at_start_count; index < new_shape_count - p_keep_at_end_count; ++index)
         {
         new_leaf_arr[index] = insert(p_obj, index);
         }
      p_obj.set_search_tree_entries(this, new_leaf_arr );
      }

   /**
    * Merges the tree entries from p_from_trace in front of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public final void merge_entries_in_front(BrdTracep p_from_trace, BrdTracep p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      int compensated_half_width = p_to_trace.get_half_width() + get_clearance_compensation(p_to_trace.clearance_idx(), p_to_trace.get_layer());
      ArrayList<ShapeTile> link_shapes = offset_shapes(p_joined_polyline, compensated_half_width, p_from_entry_no, p_to_entry_no);
      boolean change_order = p_from_trace.corner_first().equals(p_to_trace.corner_first());
      // remove the last or first tree entry from p_from_trace and the first tree entry from p_to_trace, because they will be replaced by the new link entries.
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
      AwtreeNodeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
      AwtreeNodeLeaf[] to_trace_entries = p_to_trace.get_search_tree_entries(this);
      remove_leaf(from_trace_entries[remove_no]);
      remove_leaf(to_trace_entries[0]);
      
      final int link_shapes_count = link_shapes.size();
      
      int new_shape_count = from_trace_entries.length + link_shapes_count + to_trace_entries.length - 2;
      AwtreeNodeLeaf[] new_leaf_arr = new AwtreeNodeLeaf[new_shape_count];
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
      
      for (int index = 1; index < old_to_shape_count; ++index)
         {
         int curr_ind = from_shape_count_minus_1 + link_shapes_count + index - 1;
         new_precalculated_tree_shapes[curr_ind] = p_to_trace.get_tree_shape(this, index);
         new_leaf_arr[curr_ind] = to_trace_entries[index];
         new_leaf_arr[curr_ind].shape_index_in_object = curr_ind;
         }

      // correct the precalculated tree shapes first, because it is used in this.insert
      for (int index = 0; index < link_shapes_count; ++index)
         {
         int curr_ind = from_shape_count_minus_1 + index;
         new_precalculated_tree_shapes[curr_ind] = link_shapes.get(index);
         }
      p_to_trace.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      // create the new link entries
      for (int i = 0; i < link_shapes_count; ++i)
         {
         int curr_ind = from_shape_count_minus_1 + i;
         new_leaf_arr[curr_ind] = insert(p_to_trace, curr_ind);
         }

      p_to_trace.set_search_tree_entries(this, new_leaf_arr);
      }

   /**
    * Merges the tree entries from p_from_trace to the end of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public final void merge_entries_at_end(BrdTracep p_from_trace, BrdTracep p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      int compensated_half_width = p_to_trace.get_half_width() + get_clearance_compensation(p_to_trace.clearance_idx(), p_to_trace.get_layer());
      ArrayList<ShapeTile> link_shapes = offset_shapes(p_joined_polyline, compensated_half_width, p_from_entry_no, p_to_entry_no);
      boolean change_order = p_from_trace.corner_last().equals(p_to_trace.corner_last());
      AwtreeNodeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
      AwtreeNodeLeaf[] to_trace_entries = p_to_trace.get_search_tree_entries(this);
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
      
      final int link_shapes_count = link_shapes.size();
      
      int new_shape_count = from_trace_entries.length + link_shapes_count + to_trace_entries.length - 2;
      AwtreeNodeLeaf[] new_leaf_arr = new AwtreeNodeLeaf[new_shape_count];
      ShapeTile[] new_precalculated_tree_shapes = new ShapeTile[new_shape_count];
      // transfer the tree entries except the last from the old shapes
      // of p_to_trace to the new shapes of p_to_trace
      for (int i = 0; i < to_shape_count_minus_1; ++i)
         {
         new_precalculated_tree_shapes[i] = p_to_trace.get_tree_shape(this, i);
         new_leaf_arr[i] = to_trace_entries[i];
         }

      for (int index = 1; index < from_trace_entries.length; ++index)
         {
         int curr_ind = to_shape_count_minus_1 + link_shapes_count + index - 1;
         int from_no;
         if (change_order)
            {
            from_no = from_trace_entries.length - index - 1;
            }
         else
            {
            from_no = index;
            }
         new_precalculated_tree_shapes[curr_ind] = p_from_trace.get_tree_shape(this, from_no);
         new_leaf_arr[curr_ind] = from_trace_entries[from_no];
         new_leaf_arr[curr_ind].object = p_to_trace;
         new_leaf_arr[curr_ind].shape_index_in_object = curr_ind;
         }

      // correct the precalculated tree shapes first, because it is used in this.insert
      for (int index = 0; index < link_shapes_count; ++index)
         {
         int curr_ind = to_shape_count_minus_1 + index;
         new_precalculated_tree_shapes[curr_ind] = link_shapes.get(index);
         }
      
      p_to_trace.set_precalculated_tree_shapes(new_precalculated_tree_shapes, this);

      // create the new link entries
      for (int index = 0; index < link_shapes_count; ++index)
         {
         int curr_ind = to_shape_count_minus_1 + index;
         new_leaf_arr[curr_ind] = insert(p_to_trace, curr_ind);
         }
      p_to_trace.set_search_tree_entries(this, new_leaf_arr);
      }

   /**
    * Trannsfers tree entries from p_from_trace to p_start and p_end_piece after a moddle piece was cut out. 
    * Special implementation for ShapeTraceEntries.fast_cutout_trace for performance reasoms.
    */
   public final void reuse_entries_after_cutout(BrdTracep p_from_trace, BrdTracep p_start_piece, BrdTracep p_end_piece)
      {
      AwtreeNodeLeaf[] start_piece_leaf_arr = new AwtreeNodeLeaf[p_start_piece.polyline().plaline_len(-2)];
      AwtreeNodeLeaf[] from_trace_entries = p_from_trace.get_search_tree_entries(this);
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

      AwtreeNodeLeaf[] end_piece_leaf_arr = new AwtreeNodeLeaf[p_end_piece.polyline().plaline_len(-2)];

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

      p_start_piece.set_search_tree_entries(this, start_piece_leaf_arr );
      p_end_piece.set_search_tree_entries(this, end_piece_leaf_arr );
      }

   /**
    * Puts all items in the tree overlapping with p_shape on layer p_layer into p_obstacles. 
    * If p_layer < 0, the layer is ignored.
    */
   public final TreeSet<AwtreeObject> find_overlap_objects(ShapeConvex p_shape, int p_layer, NetNosList p_ignore_net_nos)
      {
      TreeSet<AwtreeObject> risul_obstacles = new TreeSet<AwtreeObject>();

      Collection<AwtreeEntry> tree_entries = new LinkedList<AwtreeEntry>();

      calc_overlapping_tree_entries(p_shape, p_layer, p_ignore_net_nos, tree_entries);

      Iterator<AwtreeEntry> it = tree_entries.iterator();
      
      while (it.hasNext())
         {
         AwtreeEntry curr_entry = it.next();

         risul_obstacles.add(curr_entry.object);
         }
      
      return risul_obstacles;
      }

   /**
    * Returns all SearchTreeObjects on layer p_layer, which overlap with p_shape. If p_layer < 0, the layer is ignored
    */
   public final Set<AwtreeObject> find_overlap_objects(ShapeConvex p_shape, int p_layer)
      {
      return find_overlap_objects(p_shape, p_layer, NetNosList.EMPTY );
      }

   /**
    * Puts all tree entries overlapping with p_shape on layer p_layer into the list p_obstacles. 
    * If p_layer < 0, the layer is ignored.
    */
   public final void calc_overlapping_tree_entries(ShapeConvex p_shape, int p_layer, Collection<AwtreeEntry> p_risul_tree)
      {
      calc_overlapping_tree_entries(p_shape, p_layer, NetNosList.EMPTY, p_risul_tree);
      }

   /**
    * Puts all tree entries overlapping with p_shape on layer p_layer into the list p_obstacles. 
    * If p_layer < 0, the layer is ignored. 
    * tree_entries with object containing a net number of p_ignore_net_nos are ignored.
    */
   public final void calc_overlapping_tree_entries(ShapeConvex p_shape, int p_layer, NetNosList p_ignore_net_nos, Collection<AwtreeEntry> p_risul_tree)
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
      
      Collection<AwtreeNodeLeaf> tmp_list = get_overlaps(bounds);
      Iterator<AwtreeNodeLeaf> it = tmp_list.iterator();

      boolean is_45_degree = p_shape instanceof ShapeTileOctagon;

      while (it.hasNext())
         {
         AwtreeNodeLeaf curr_leaf = it.next();
         AwtreeObject curr_object = curr_leaf.object;
         int shape_index = curr_leaf.shape_index_in_object;
         
         // ignore object if it is on a different layer
         boolean ignore_object = p_layer >= 0 && curr_object.shape_layer(shape_index) != p_layer;
         
         if ( ignore_object ) continue;

         // ingore if the given object is somewhat connectable to the net nos
         ignore_object = p_ignore_net_nos.is_connectable(curr_object); 
         
         if ( ignore_object ) continue;
         
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
            AwtreeEntry new_entry = new AwtreeEntry(curr_object, shape_index);
            p_risul_tree.add(new_entry);
            }
         }
      }

   /**
    * Looks up all entries in the search tree, so that inserting an item with shape p_shape, net number p_net_no, 
    * clearance type p_cl_type and layer p_layer whould produce a clearance violation
    * puts them into the set p_obstacle_entries. 
    * The elements in p_obstacle_entries are of type TreeEntry. 
    * if p_layer < 0, the layer is ignored. 
    * Used only internally, because the clearance compensation is not taken innto account.
    */
   public final void find_overlap_tree_entries_with_clearance(ShapeTile p_shape, int p_layer, NetNosList p_ignore_net_nos, int p_cl_type, Collection<AwtreeEntry> p_result)
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
      // search with the bounds enlarged by the maximum clearance to get all candidates for overlap
      // a factor less than sqr2 has evtl. be added because enlarging is not symmetric.
      ShapeTile offset_bounds = bounds.offset(max_clearance);

      Collection<AwtreeNodeLeaf> tmp_list = get_overlaps(offset_bounds);
      
      // sort the found items by its clearances tp p_cl_type on layer p_layer
      Set<AwtreeNodeLeafSorted> sorted_items = new TreeSet<AwtreeNodeLeafSorted>();

      for ( AwtreeNodeLeaf curr_leaf : tmp_list )
         {
         BrdItem curr_item = (BrdItem) curr_leaf.object;
         
         int shape_index = curr_leaf.shape_index_in_object;
         
         boolean ignore_item = p_layer >= 0 && curr_item.shape_layer(shape_index) != p_layer;
         
         if ( ignore_item ) continue;

         ignore_item = p_ignore_net_nos.is_connectable(curr_item);
         
         if (!ignore_item)
            {
            int curr_clearance = cl_matrix.value_at(p_cl_type, curr_item.clearance_idx(), p_layer);
            AwtreeNodeLeafSorted sorted_ob = new AwtreeNodeLeafSorted(curr_leaf, curr_clearance);
            sorted_items.add(sorted_ob);
            }
         }
      
      int curr_half_clearance = 0;
      
      ShapeTile curr_offset_shape = p_shape;
      
      for ( AwtreeNodeLeafSorted tmp_entry : sorted_items )
         {
         int tmp_half_clearance = tmp_entry.clearance / 2;

         if (tmp_half_clearance != curr_half_clearance)
            {
            curr_half_clearance = tmp_half_clearance;
            curr_offset_shape = p_shape.enlarge(curr_half_clearance);
            }
         
         ShapeTile tmp_shape = tmp_entry.leaf.object.get_tree_shape(this, tmp_entry.leaf.shape_index_in_object);
         // enlarge both item shapes by the half clearance to create symmetry.
         ShapeConvex tmp_offset_shape = (ShapeConvex) tmp_shape.enlarge(curr_half_clearance);
         
         if (curr_offset_shape.intersects(tmp_offset_shape))
            {
            p_result.add(new AwtreeEntry(tmp_entry.leaf.object, tmp_entry.leaf.shape_index_in_object));
            }
         }
      }

   /**
    * Puts all items in the tree overlapping with p_shape on layer p_layer into p_result
    * If p_layer < 0 the layer is ignored.
    * @param p_result a non null Set
    */
   public final void find_overlap_objects_with_clearance(ShapeTile p_shape, int p_layer, NetNosList p_ignore_net_nos, int p_cl_type, Set<AwtreeObject> p_result)
      {
      Collection<AwtreeEntry> res_tree_entries = new LinkedList<AwtreeEntry>();
      
      if (is_clearance_compensation_used())
         {
         calc_overlapping_tree_entries(p_shape, p_layer, p_ignore_net_nos, res_tree_entries);
         }
      else
         {
         find_overlap_tree_entries_with_clearance(p_shape, p_layer, p_ignore_net_nos, p_cl_type, res_tree_entries);
         }
      
      for (AwtreeEntry curr_entry : res_tree_entries )
         {
         p_result.add(curr_entry.object);
         }
      }

   /**
    * Returns items, which overlap with p_shape on layer p_layer inclusive clearance. 
    * p_clearance_class is the index in the clearance matrix, which describes the required clearance restrictions to other items. 
    * The function may also return items which are nearly overlapping, but do not overlap with exact calculation. 
    * If p_layer < 0, the layer is ignored.
    */
   public final Set<BrdItem> find_overlap_items_with_clearance(ShapeTile p_shape, int p_layer, NetNosList p_ignore_net_nos, int p_clearance_class)
      {
      Set<AwtreeObject> overlaps = new TreeSet<AwtreeObject>();

      find_overlap_objects_with_clearance(p_shape, p_layer, p_ignore_net_nos, p_clearance_class, overlaps);
      
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      for (AwtreeObject curr_object : overlaps)
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
   public final LinkedList<AwtreeEntry> find_overlap_tree_entries_with_clearance(ShapeTile p_shape, int p_layer, NetNosList p_ignore_net_nos, int p_clearance_class)
      {
      LinkedList<AwtreeEntry> result = new LinkedList<AwtreeEntry>();
      
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
   public Collection<ExpandRoomFreespaceIncomplete> complete_shape(ExpandRoomFreespaceIncomplete p_room, int p_net_no, AwtreeObject p_ignore_object, ShapeTile p_ignore_shape)
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
      AwtreeNode curr_node;
      int room_layer = p_room.get_layer();

      for (;;)
         {
         curr_node = node_stack.pop();
         
         if (curr_node == null) break;
         
         if ( ! curr_node.bounding_shape.intersects(bounding_shape)) continue;
         
         if ( ! (curr_node instanceof AwtreeNodeLeaf) )
            {
            node_stack.push(((AwtreeNodeFork) curr_node).first_child);
            node_stack.push(((AwtreeNodeFork) curr_node).second_child);
            continue;
            }

         AwtreeNodeLeaf curr_leaf = (AwtreeNodeLeaf) curr_node;
         AwtreeObject curr_object = curr_leaf.object;
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
               // cannot happen in free angle roouting, because then expansion_rooms may not overlap. 
               // Therefore that can be removed as soon as special function for 45-degree routing is used.
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
      for (int index = 0; index < obstacle_simplex.border_line_count(); ++index)
         {
         PlaSegmentInt curr_line_segment = new PlaSegmentInt(obstacle_simplex, index);
         if (room_shape.is_intersected_interiour_by(curr_line_segment))
            {
            // otherwise curr_object may not touch the intersection of p_shape with the half_plane defined by the cut_line.
            // That may lead to problems when creating the ExpansionRooms.
            PlaLineInt curr_line = obstacle_simplex.border_line(index);

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
         if (shape_to_be_contained.dimension().is_lt_point() )
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
   public final void reduce_trace_shape_at_tie_pin(BrdAbitPin p_tie_pin, BrdTracep p_trace)
      {
      ShapeTile pin_shape = p_tie_pin.get_tree_shape_on_layer(this, p_trace.get_layer());
      PlaPointFloat compare_corner;
      int trace_shape_no;
      if (p_trace.corner_first().equals(p_tie_pin.center_get()))
         {
         trace_shape_no = 0;
         compare_corner = p_trace.polyline().corner_approx(1);

         }
      else if (p_trace.corner_last().equals(p_tie_pin.center_get()))
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
      AwtreeNodeLeaf[] old_entries = p_item.get_search_tree_entries(this);
      AwtreeNodeLeaf[] new_leaf_arr = new AwtreeNodeLeaf[old_entries.length];
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
      p_item.set_search_tree_entries(this, new_leaf_arr );
      }

   /**
    * This is for a board trace
    */
   public final ShapeTile[] calculate_tree_shapes(BrdTracep p_trace)
      {
      int offset_width = p_trace.get_half_width() + get_clearance_compensation(p_trace.clearance_idx(), p_trace.get_layer());
      
      ShapeTile[] result = new ShapeTile[p_trace.tile_shape_count()];

      for (int index = 0; index < result.length; ++index)
         {
         result[index] = offset_shape(p_trace.polyline(), offset_width, index);
         }
      return result;
      }

   /**
    * This is for Drill items
    */
   public final ShapeTile[] calculate_tree_shapes(BrdAbit p_drill_item)
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
            if (r_board.brd_rules.is_trace_snap_90() )
               {
               curr_tile_shape = curr_shape.bounding_box();
               }
            else if (r_board.brd_rules.is_trace_snap_45())
               {
               curr_tile_shape = curr_shape.bounding_octagon();
               }
            else
               {
               curr_tile_shape = curr_shape.bounding_tile();
               }
            int offset_width = get_clearance_compensation(p_drill_item.clearance_idx(), p_drill_item.shape_layer(index));
            if (curr_tile_shape == null)
               {
               System.out.println("ShapeSearchTree.calculate_tree_shapes: shape is null");
               }
            else
               {
               curr_tile_shape = curr_tile_shape.enlarge(offset_width);
               }
            result[index] = curr_tile_shape;
            }
         }
      return result;
      }

   /**
    * this is for BoardArea
    */
   public final ShapeTile[] calculate_tree_shapes(BrdArea p_obstacle_area)
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

         int offset_width = get_clearance_compensation(p_obstacle_area.clearance_idx(), p_obstacle_area.get_layer());
         curr_convex_shape = curr_convex_shape.enlarge(offset_width);
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
               int offset_width = get_clearance_compensation(p_board_outline.clearance_idx(), 0);
               curr_convex_shape = curr_convex_shape.enlarge(offset_width);
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
               ShapeSegments curr_outline_shape = p_board_outline.get_shape(shape_no);
               int border_line_count = curr_outline_shape.border_line_count();
               curr_line_arr[0] = curr_outline_shape.border_line(border_line_count - 1);
               
               for (int iindex = 0; iindex < border_line_count; ++iindex)
                  {
                  curr_line_arr[1] = curr_outline_shape.border_line(iindex);
                  curr_line_arr[2] = curr_outline_shape.border_line((iindex + 1) % border_line_count);
               
                  Polyline tmp_polyline = new Polyline(curr_line_arr);
                  
                  int cmp_value = get_clearance_compensation(p_board_outline.clearance_idx(), 0);
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
   protected ArrayList<ShapeTile> offset_shapes(Polyline p_polyline, int p_half_width, int p_from_no, int p_to_no)
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
      AwtreeNodeLeaf[] curr_tree_entries = p_item.get_search_tree_entries(this);

      for (int index = 0; index < curr_tree_entries.length; ++index)
         {
         AwtreeNodeLeaf curr_leaf = curr_tree_entries[index];

         if (curr_leaf.shape_index_in_object != index)
            {
            System.err.println("tree entry inconsistent for Item");
            return false;
            }
         }
      
      return true;
      }

   /**
    * Used to have some meaningful info on this object
    * Mostly used for beanshell
    */
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(1000);
      risul.append("ShapeSearchTree \n");
      risul.append("objects: ... \n");
      risul.append("methods: statistics() \n");
      
      return risul.toString();
      }
   
   }
