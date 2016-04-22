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
import freert.planar.ShapeBounding;
import freert.planar.ShapeTileRegular;

/**
 * Binary search tree for shapes in the plane. The shapes are stored in the leafs of the tree. 
 * The algorithm for storing a new shape is as following. 
 * Starting from the root go to the child, so that the increase of the bounding shape of that child is minimal
 * after adding the new shape, until you reach a leaf. 
 * The use of ShapeDirections to calculate the bounding shape is for historicalreasons (coming from a Kd-Tree). 
 * Instead any algorithm to calculate a bounding shape of two input shapes can be used. 
 * The algorithm would of course also work for higher dimensions.
 *
 * @author Alfons Wirtz
 */
public class ShapeTreeMinArea extends ShapeTree
   {
   protected ShapeTreeNodeStack node_stack = new ShapeTreeNodeStack();

   /**
    * Constructor with a fixed set of directions defining the keys and and the surrounding shapes
    */
   public ShapeTreeMinArea(ShapeBounding p_directions)
      {
      super(p_directions);
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

   @Override
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


   @Override
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
