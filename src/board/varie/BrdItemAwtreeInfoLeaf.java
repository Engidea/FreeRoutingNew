package board.varie;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

import board.awtree.AwtreeNodeLeaf;
import board.awtree.AwtreeShapeSearch;
import freert.planar.ShapeTile;

public final class BrdItemAwtreeInfoLeaf
   {
   public final AwtreeShapeSearch tree;
   public AwtreeNodeLeaf[] entry_arr;
   public ShapeTile[] precalculated_tree_shapes;

   public BrdItemAwtreeInfoLeaf(AwtreeShapeSearch p_tree, ShapeTile[] precalculated_shapes)
      {
      tree = p_tree;
      precalculated_tree_shapes = precalculated_shapes;
      }

   public BrdItemAwtreeInfoLeaf(AwtreeShapeSearch p_tree, AwtreeNodeLeaf[] p_entry_arr)
      {
      tree = p_tree;
      entry_arr = p_entry_arr;
      }

   }
