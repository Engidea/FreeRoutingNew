package board.awtree;
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
 * Description of a leaf of the Tree, where the geometric information is stored.
 */
public final class AwtreeNodeLeaf extends AwtreeNode implements Comparable<AwtreeNodeLeaf>
   {
   /// Actual object stored, may change if you keep the shape but change it... 
   public AwtreeObject object;
   // index of the shape in the object, it should really be final but it is not for special performance reuse... mah 
   public int shape_index_in_object;

   public AwtreeNodeLeaf(AwtreeObject p_object, int p_index, AwtreeNodeFork p_parent, ShapeTileRegular p_bounding_shape)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      object = p_object;
      shape_index_in_object = p_index;
      }

   @Override
   public int compareTo(AwtreeNodeLeaf p_other)
      {
      int result = object.compareTo(p_other.object);
      
      if (result != 0) return result;
      
      return shape_index_in_object - p_other.shape_index_in_object;
      }

   /** 
    * @return the number of nodes between this leaf and the root of the tree. 
    */
   public int distance_to_root()
      {
      int result = 1;
      
      AwtreeNodeFork curr_parent = parent;

      while ( curr_parent.parent != null)
         {
         curr_parent = curr_parent.parent;
         result++;
         }
      
      return result;
      }
   }
