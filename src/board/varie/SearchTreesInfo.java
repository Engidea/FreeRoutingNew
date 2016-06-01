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
 * ItemSearchTreesInfo.java
 *
 * Created on 10. Januar 2006, 08:46
 *
 */

package board.varie;

import java.util.Collection;
import java.util.LinkedList;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeNodeLeaf;
import freert.planar.ShapeTile;

/**
 * Stores information about the search trees of the board items, which is pre calculated for performance reasons.
 * @author Alfons Wirtz
 */
public final class SearchTreesInfo
   {
   private final Collection<SearchTreeInfoLeaf> leaves_list = new LinkedList<SearchTreeInfoLeaf>();

   /**
    * Returns the tree entries for the given tree  or null, if for this tree no entries of this item
    * are inserted.
    */
   public ShapeTreeNodeLeaf[] get_tree_entries(ShapeSearchTree p_tree)
      {
      for (SearchTreeInfoLeaf curr_tree_info : leaves_list)
         {
         if (curr_tree_info.tree == p_tree)
            {
            return curr_tree_info.entry_arr;
            }
         }
      return null;
      }

   public void clear ()
      {
      leaves_list.clear();
      }

   /**
    * Sets the item tree entries for the given tree
    * If the tree does not exist it is created
    */
   public void set_tree_entries( ShapeSearchTree p_tree , ShapeTreeNodeLeaf[] p_tree_entries)
      {
      for (SearchTreeInfoLeaf curr_tree_info : leaves_list)
         {
         if (curr_tree_info.tree == p_tree)
            {
            curr_tree_info.entry_arr = p_tree_entries;
            return;
            }
         }
      
      SearchTreeInfoLeaf new_tree_info = new SearchTreeInfoLeaf(p_tree);
      new_tree_info.entry_arr = p_tree_entries;
      leaves_list.add(new_tree_info);
      }

   /**
    * Returns the precalculated tiles hapes for the tree with identification number p_tree_no, or null, if the tile shapes of this
    * tree are nnot yet precalculated.
    */
   public ShapeTile[] get_precalculated_tree_shapes(ShapeSearchTree p_tree)
      {
      for (SearchTreeInfoLeaf curr_tree_info : leaves_list)
         {
         if (curr_tree_info.tree == p_tree)
            {
            return curr_tree_info.precalculated_tree_shapes;
            }
         }
      return null;
      }

   /**
    * Sets the item tree entries for the tree with identification number p_tree_no.
    */
   public void set_precalculated_tree_shapes(ShapeTile[] p_tile_shapes, ShapeSearchTree p_tree)
      {
      for (SearchTreeInfoLeaf curr_tree_info : leaves_list)
         {
         if (curr_tree_info.tree == p_tree)
            {
            curr_tree_info.precalculated_tree_shapes = p_tile_shapes;
            return;
            }
         }
      
      SearchTreeInfoLeaf new_tree_info = new SearchTreeInfoLeaf(p_tree);
      new_tree_info.precalculated_tree_shapes = p_tile_shapes;
      leaves_list.add(new_tree_info);
      }

   /**
    * clears the stored information about the precalculated tree shapes for all search trees.
    */
   public void clear_precalculated_tree_shapes()
      {
      for (SearchTreeInfoLeaf curr_tree_info : leaves_list)
         {
         curr_tree_info.precalculated_tree_shapes = null;
         }
      }

   }
