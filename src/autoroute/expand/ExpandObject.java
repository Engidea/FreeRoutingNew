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
 * ExpandableObject.java
 *
 * Created on 6. April 2004, 07:30
 */
package autoroute.expand;

import autoroute.maze.MazeSearchElement;
import freert.planar.PlaDimension;
import freert.planar.ShapeTile;

/**
 * An object, which can be expanded by the maze expansion algorithm.
 *
 * @author alfons
 */
public interface ExpandObject
   {

   /**
    * Calculates the intersection of the shapes of the 2 object belonging to this door.
    */
   ShapeTile get_shape();

   /**
    * Returns the dimension ot the intersection of the shapes of the 2 objects belonging to this door.
    */
   PlaDimension get_dimension();

   /**
    * @return the other room to p_room if this is a door and the other room is a CompleteExpansionRoom. Else null is returned.
    */
   ExpandRoomComplete other_room(ExpandRoomComplete p_room);

   /**
    * Returns the count of MazeSearchElements in this expandable object
    */
   int maze_search_element_count();

   /**
    * Returns the p_no-th MazeSearchElements in this expandable object
    */
   MazeSearchElement get_maze_search_element(int p_no);

   /**
    * Resets this ExpandableObject for autorouting the next connection.
    */
   void reset();
   }
