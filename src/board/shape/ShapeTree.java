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
 * ShapeTree.java
 *
 * Created on 1. September 2004, 10:50
 */

package board.shape;

import freert.planar.PlaShape;
import freert.planar.ShapeBounding;
import freert.planar.ShapeTileRegular;

/**
 * Abstract binary search tree for shapes in the plane. 
 * The shapes are stored in the leafs of the tree. 
 * Objects to be stored in the tree must implement the interface ShapeTreeStorable.
 *
 * @author Alfons Wirtz
 */
public abstract class ShapeTree
   {
   // the fixed directions for calculating bounding RegularTileShapes of shapes to store in this tree.
   protected final ShapeBounding bounding_directions;
   // Root node - initially null 
   protected ShapeTreeNode root_node = null;
   // The number of entries stored in the tree
   protected int leaf_count = 0;

   public ShapeTree(ShapeBounding p_directions)
      {
      bounding_directions = p_directions;
      }

   /**
    * Inserts all shapes of p_obj into the tree
    */
   public final void insert(ShapeTreeStorable p_obj)
      {
      int shape_count = p_obj.tree_shape_count(this);
      
      if (shape_count <= 0)  return;

      ShapeTreeLeaf[] leaf_arr = new ShapeTreeLeaf[shape_count];

      for (int index = 0; index < shape_count; ++index)
         {
         leaf_arr[index] = insert(p_obj, index);
         }
      
      p_obj.set_search_tree_entries(leaf_arr, this);
      }

   /**
    * Insert a shape - creates a new node with a bounding shape
    * This is possibly the entry point to understand the whole search tree mechanism
    */
   protected final ShapeTreeLeaf insert(ShapeTreeStorable p_object, int p_index)
      {
      PlaShape object_shape = p_object.get_tree_shape(this, p_index);
      
      if (object_shape == null) return null;

      ShapeTileRegular bounding_shape = object_shape.bounding_shape(bounding_directions);

      if (bounding_shape == null)
         {
         System.err.println("ShapeTree.insert: bounding shape of TreeObject is null");
         return null;
         }
      
      // Construct a new KdLeaf and set it up
      ShapeTreeLeaf new_leaf = new ShapeTreeLeaf(p_object, p_index, null, bounding_shape);
      
      insert_leaf(new_leaf);
      
      return new_leaf;
      }

   /** 
    * Inserts the leaves of this tree into an array
    */
   public final ShapeTreeLeaf[] to_array()
      {
      ShapeTreeLeaf[] result = new ShapeTreeLeaf[leaf_count];

      if (result.length == 0) return result;
      
      ShapeTreeNode curr_node = root_node;

      int curr_index = 0;
      
      for (;;)
         {
         // go down from curr_node to the left most leaf
         while (curr_node instanceof ShapeTreeNodeInner)
            {
            curr_node = ((ShapeTreeNodeInner) curr_node).first_child;
            }
         result[curr_index] = (ShapeTreeLeaf) curr_node;

         ++curr_index;
         // go up until parent.second_child != curr_node, which means we came from first_child
         ShapeTreeNodeInner curr_parent = curr_node.parent;
         while (curr_parent != null && curr_parent.second_child == curr_node)
            {
            curr_node = curr_parent;
            curr_parent = curr_node.parent;
            }
         if (curr_parent == null)
            {
            break;
            }
         curr_node = curr_parent.second_child;
         }
      return result;
      }

   public abstract void insert_leaf(ShapeTreeLeaf p_leaf);

   public abstract void remove_leaf(ShapeTreeLeaf p_leaf);

   /**
    * removes all entries of p_obj in the tree.
    */
   public final void remove(ShapeTreeLeaf[] p_entries)
      {
      if (p_entries == null) return;

      for (int index = 0; index < p_entries.length; ++index)
         {
         remove_leaf(p_entries[index]);
         }
      }

   /** 
    * Returns the number of entries stored in the tree
    */
   public final int size()
      {
      return leaf_count;
      }

   /** 
    * Outputs some statistic information about the tree
    */
   public final void statistics(String p_message)
      {
      ShapeTreeLeaf[] leaf_arr = this.to_array();
      double cumulative_depth = 0;
      int maximum_depth = 0;
      for (int i = 0; i < leaf_arr.length; ++i)
         {
         if (leaf_arr[i] != null)
            {
            int distance_to_root = leaf_arr[i].distance_to_root();
            cumulative_depth += distance_to_root;
            maximum_depth = Math.max(maximum_depth, distance_to_root);
            }
         }
      double everage_depth = cumulative_depth / leaf_arr.length;
      System.out.print("MinAreaTree: Entry count: ");
      System.out.print(leaf_arr.length);
      System.out.print(" log: ");
      System.out.print(Math.round(Math.log(leaf_arr.length)));
      System.out.print(" Everage depth: ");
      System.out.print(Math.round(everage_depth));
      System.out.print(" ");
      System.out.print(" Maximum depth: ");
      System.out.print(maximum_depth);
      System.out.print(" ");
      System.out.println(p_message);
      }
   }
