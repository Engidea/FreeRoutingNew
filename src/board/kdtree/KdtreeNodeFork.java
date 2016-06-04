package board.kdtree;
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

import freert.planar.ShapeTileRegular;

/**
 * Description of an inner node of the tree, which implements a fork to its two children.
 */
public final class KdtreeNodeFork extends KdtreeNode
   {
   public KdtreeNode first_child;
   public KdtreeNode second_child;

   public KdtreeNodeFork(ShapeTileRegular p_bounding_shape, KdtreeNodeFork p_parent)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      first_child = null;
      second_child = null;
      }
   }
