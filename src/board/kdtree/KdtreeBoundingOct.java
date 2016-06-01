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

package board.kdtree;

import freert.planar.ShapeCircle;
import freert.planar.ShapeConvex;
import freert.planar.ShapePolygon;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.planar.ShapeTileRegular;
import freert.planar.ShapeTileSimplex;

/**
 * Describing the functionality for the fixed directions of a RegularTileShape.
 * Basically how to constrain a shape to have lines that have a certain "fixed" direction relative to each other 
 * Implements the abstract class ShapeBoundingDirections as the 8 directions, which are multiples of 45 degree. 
 * The class is a singleton with the only instantiation INSTANCE.
 * It is called Oct since the bounding end up into an octagon, or at 45 degrees angle, if you wish
 * @author Alfons Wirtz
 */
public final class KdtreeBoundingOct
   {
   public static final KdtreeBoundingOct INSTANCE = new KdtreeBoundingOct();

   /**
    * @return the count of the fixed directions.
    */
   public int count()
      {
      return 8;
      }

   /**
    * Calculates for an arbitrary ConvexShape a surrounding RegularTileShape with this fixed directions. 
    * Is used in the implementation of the search trees.
    */
   public ShapeTileRegular bounds(ShapeConvex p_shape)
      {
      return p_shape.bounding_shape(this);
      }

   /**
    * Auxiliary function to implement the same function with parameter type ShapeTileBox
    */
   public ShapeTileRegular bounds(ShapeTileBox p_box)
      {
      return p_box.to_IntOctagon();
      }

   /**
    * Auxiliary function to implement the same function with parameter type ShapeTileOctagon
    */
   public ShapeTileRegular bounds(ShapeTileOctagon p_oct)
      {
      return p_oct;
      }

   /**
    * Auxiliary function to implement the same function with parameter type ShapeTileSimplex
    */
   public ShapeTileRegular bounds(ShapeTileSimplex p_simplex)
      {
      return p_simplex.bounding_octagon();
      }

   public ShapeTileRegular bounds(ShapeCircle p_circle)
      {
      return p_circle.bounding_octagon();
      }

   public ShapeTileRegular bounds(ShapePolygon p_polygon)
      {
      return p_polygon.bounding_octagon();
      }

   }