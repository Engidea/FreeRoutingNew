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
 */

package board;

import java.util.Set;
import board.awtree.AwtreeShapeSearch;
import board.items.BrdItem;
import freert.planar.ShapeTile;

/**
 * Functionality required for items, which can be electrical connected to other items.
 * @author Alfons Wirtz
 */

public interface BrdConnectable
   {
   /**
    * Returns the list of all contacts of a connectable item located at defined connection points. Connection points of traces are
    * there endpoints, connection points of drill_items there center points, and connection points of conduction areas are points on
    * there border.
    */
   Set<BrdItem> get_normal_contacts();

   /**
    * Returns all connectable items of the net with number p_net_no, which can be reached recursively from this item via normal
    * contacts. if (p_net_no <= 0, the net number is ignored.
    */
   Set<BrdItem> get_connected_set(int p_net_no);

   /**
    * Returns for each convex shape of a connectable item the subshape of points, where traces can be connected to that item.
    */
   ShapeTile get_trace_connection_shape(AwtreeShapeSearch p_tree, int p_index);
   }
