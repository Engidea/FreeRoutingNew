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
 * SearchTreeObject.java
 *
 * Created on 10. Januar 2004, 10:08
 */

package board.shape;

import freert.planar.ShapeTile;


/**
 * Common ShapeSearchTree functionality for board.Items and autoroute.ExpansionRooms
 * Merge of ShapeTreeStorable which must be implemented by objects to be stored in a ShapeTree.
 * An object must provide means to be stored in the tree
 * @author Alfons Wirtz
 */
public interface ShapeTreeObject extends Comparable<Object>
   {
   /**
    * @return the number of shapes of an object to store in p_shape_tree
    */
   int tree_shape_count(ShapeSearchTree p_shape_tree);

   /**
    * @return the Shape of this object with index p_index stored in the ShapeTree with index identification number p_tree_id_no
    */
   ShapeTile get_tree_shape(ShapeSearchTree p_tree, int p_index);

   /**
    * Stores the entries in the ShapeTrees of this object for better performance while for example deleting tree entries. 
    * Called only by insert methods of class ShapeTree.
    */
   void set_search_tree_entries(ShapeSearchTree p_tree,ShapeTreeNodeLeaf[] p_entries);
   
   
   /**
    * @return true if this object is an obstacle to objects containing the net number p_net_no
    */
   boolean is_obstacle(int p_net_no);

   /**
    * @return true if this object is an obstacle to traces containing the net number p_net_no
    */
   boolean is_trace_obstacle(int p_net_no);

   /**
    * @return for this object the layer of the shape with index p_index.
    */
   abstract int shape_layer(int p_index);
   }
