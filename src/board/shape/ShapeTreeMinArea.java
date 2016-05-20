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
 * MinAreaTree.java
 *
 * Created on 1. September 2004, 08:29
 */

package board.shape;

import java.util.Set;
import java.util.TreeSet;
import freert.planar.PlaShape;
import freert.planar.ShapeBounding;
import freert.planar.ShapeTileRegular;

/**
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
public abstract class ShapeTreeMinArea
   {
   protected ShapeTreeNodeStack node_stack = new ShapeTreeNodeStack();
   
   // the fixed directions for calculating bounding RegularTileShapes of shapes to store in this tree.
   protected final ShapeBounding bounding_directions;
   // Root node - initially null 
   protected ShapeTreeNode root_node = null;
   // The number of entries stored in the tree
   protected int leaf_count = 0;

   /**
    * Constructor with a fixed set of directions defining the keys and and the surrounding shapes
    */
   public ShapeTreeMinArea(ShapeBounding p_directions)
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
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   /**
    * Calculates the objects in this tree, which overlap with p_shape
    */
   public final Set<ShapeTreeLeaf> get_overlaps(ShapeTileRegular p_shape)
      {
      Set<ShapeTreeLeaf> found_overlaps = new TreeSet<ShapeTreeLeaf>();

      if (root_node == null) return found_overlaps;

      node_stack.reset();
      node_stack.push(root_node);
      
      ShapeTreeNode curr_node;
      
      for (;;)
         {
         curr_node = node_stack.pop();

         if (curr_node == null) break;

         if ( ! curr_node.bounding_shape.intersects(p_shape)) continue;
         
         if (curr_node instanceof ShapeTreeLeaf)
            {
            found_overlaps.add((ShapeTreeLeaf) curr_node);
            }
         else
            {
            node_stack.push(((ShapeTreeNodeInner) curr_node).first_child);
            node_stack.push(((ShapeTreeNodeInner) curr_node).second_child);
            }
         }
      return found_overlaps;
      }

   public final void insert_leaf(ShapeTreeLeaf p_leaf)
      {
      leaf_count++;

      if (root_node == null)
         {
         // Tree is empty - just insert the new leaf
         root_node = p_leaf;
         return;
         }

      // Non-empty tree - do a recursive location for leaf replacement
      ShapeTreeLeaf leaf_to_replace = position_locate(root_node, p_leaf);

      // Construct a new node - whenever a leaf is added so is a new node
      ShapeTileRegular new_bounds = p_leaf.bounding_shape.union(leaf_to_replace.bounding_shape);
      ShapeTreeNodeInner curr_parent = leaf_to_replace.parent;
      ShapeTreeNodeInner new_node = new ShapeTreeNodeInner(new_bounds, curr_parent);

      if (leaf_to_replace.parent != null)
         {
         // Replace the pointer from the parent to the leaf with our new node
         if (leaf_to_replace == curr_parent.first_child)
            {
            curr_parent.first_child = new_node;
            }
         else
            {
            curr_parent.second_child = new_node;
            }
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

   private final ShapeTreeLeaf position_locate(ShapeTreeNode p_curr_node, ShapeTreeLeaf p_leaf_to_insert)
      {
      ShapeTreeNode curr_node = p_curr_node;

      while (!(curr_node instanceof ShapeTreeLeaf))
         {
         ShapeTreeNodeInner curr_inner_node = (ShapeTreeNodeInner) curr_node;
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
      return (ShapeTreeLeaf) curr_node;
      }


   public final void remove_leaf(ShapeTreeLeaf p_leaf)
      {
      if (p_leaf == null) return;

      // remove the leaf node
      ShapeTreeNodeInner parent = p_leaf.parent;
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
      ShapeTreeNode other_leaf;
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
      ShapeTreeNodeInner grand_parent = parent.parent;
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
      ShapeTreeNodeInner node_to_recalculate = grand_parent;
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

   }
