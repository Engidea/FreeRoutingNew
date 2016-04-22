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

package freert.planar;

/**
 * Describing the functionality for the fixed directions of a RegularTileShape.
 * Basically how to constrain a shape to have lines that have a certain "fixed" direction relative to each other 
 * @author Alfons Wirtz
 */

public interface ShapeBounding
   {

   /**
    * @return the count of the fixed directions.
    */
   int count();

   /**
    * Calculates for an arbitrary ConvexShape a surrounding RegularTileShape with this fixed directions. 
    * Is used in the implementation of the search trees.
    */
   ShapeTileRegular bounds(ShapeConvex p_shape);

   /**
    * Auxiliary function to implement the same function with parameter type ConvexShape.
    */
   ShapeTileRegular bounds(ShapeTileBox p_box);

   /**
    * Auxiliary function to implement the same function with parameter type ConvexShape.
    */
   ShapeTileRegular bounds(ShapeTileOctagon p_oct);

   /**
    * Auxiliary function to implement the same function with parameter type ConvexShape.
    */
   ShapeTileRegular bounds(ShapeTileSimplex p_simplex);

   /**
    * Auxiliary function to implement the same function with parameter type ConvexShape.
    */
   ShapeTileRegular bounds(PlaCircle p_circle);

   /**
    * Auxiliary function to implement the same function with parameter type ConvexShape.
    */
   ShapeTileRegular bounds(ShapePolygon p_polygon);
   }