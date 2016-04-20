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
 * RegularTileShape.java
 *
 * Created on 16. November 2002, 17:11
 */

package planar;

/**
 * TileShapes whose border lines may have only directions out of a fixed set, as for example orthoganal directions, which define
 * axis parallel box shapes.
 * 
 * @author Alfons Wirtz
 */
public abstract class ShapeTileRegular extends ShapeTile
   {
   private static final long serialVersionUID = 1L;

   /**
    * Compares the edglines of index p_edge_no of this regular TileShape and p_other. returns Side.ON_THE_LEFT, if the edgeline of
    * this simplex is to the left of the edgeline of p_other; Side.COLLINEAR, if the edlines are equal, and Side.ON_THE_RIGHT, if
    * this edgeline is to the right of the edgeline of p_other.
    */
   public abstract PlaSide compare(ShapeTileRegular p_other, int p_edge_no);

   /**
    * calculates the smallest RegularTileShape containing this shape and p_other.
    */
   public abstract ShapeTileRegular union(ShapeTileRegular p_other);

   /**
    * returns true, if p_other is completely contained in this shape
    */
   public abstract boolean contains(ShapeTileRegular p_other);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   abstract PlaSide compare(ShapeTileBox p_other, int p_edge_no);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   abstract PlaSide compare(ShapeTileOctagon p_other, int p_edge_no);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   abstract ShapeTileRegular union(ShapeTileBox p_other);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   abstract ShapeTileRegular union(ShapeTileOctagon p_other);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   public abstract boolean is_contained_in(ShapeTileBox p_other);

   /**
    * Auxiliary function to implement the same function with parameter type RegularTileShape.
    */
   abstract boolean is_contained_in(ShapeTileOctagon p_other);
   }
