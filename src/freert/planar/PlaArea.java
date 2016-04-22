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
 * Area.java
 *
 * Created on 22. Juni 2003, 10:16
 */

package freert.planar;


/**
 * An Area is a not necessarily simply connected Shape, which means, that it may
 * contain holes. The border and the holes of an Area are of class Shape.
 *
 * @author Alfons Wirtz
 */
public interface PlaArea extends PlaObject
   {
   /**
    * returns true, if the area is empty
    */
   boolean is_empty();

   /**
    * returns true, if the area is contained in a sufficiently large box
    */
   boolean is_bounded();

   /**
    * returns 
    * 2, if the area contains 2 dimensional shapes 
    * 1, if it contains curves, 
    * 0, if it is reduced to a points and 
    * -1, if it is empty.
    */
   PlaDimension dimension();

   /**
    * Checks, if this area is completely contained in p_box.
    */
   boolean is_contained_in(ShapeTileBox p_box);

   /**
    * returns the border shape of this area
    */
   PlaShape get_border();

   /**
    * Returns the array of holes, of this area.
    */
   PlaShape[] get_holes();

   /**
    * Returns the smallest surrounding box of the area. If the area is not
    * bounded, some coordinates of the resulting box may be equal
    * Integer.MAX_VALUE
    */
   ShapeTileBox bounding_box();

   /**
    * Returns the smallest surrounding octagon of the area. If the area is not
    * bounded, some coordinates of the resulting octagon may be equal
    * Integer.MAX_VALUE
    */
   ShapeTileOctagon bounding_octagon();

   /**
    * Returns true, if p_point is contained in this area, but not inside a hole.
    * Being on the border is not defined for FloatPoints because of numerical
    * inacurracy.
    */
   boolean contains(PlaPointFloat p_point);

   /**
    * Returns true, if p_point is inside or on the border of this area, but not
    * inside a hole.
    */
   boolean contains(PlaPoint p_point);

   /**
    * Calculates an approximation of the nearest point of the shape to
    * p_from_point
    */
   PlaPointFloat nearest_point_approx(PlaPointFloat p_ftom_point);

   /**
    * Turns this area by p_factor times 90 degree around p_pole.
    */
   PlaArea turn_90_degree(int p_factor, PlaPointInt p_pole);

   /**
    * Rotates the area around p_pole by p_angle. The result may be not exact.
    */
   PlaArea rotate_approx(double p_angle, PlaPointFloat p_pole);

   /**
    * Returns the affine translation of the area by p_vector
    */
   PlaArea translate_by(PlaVector p_vector);

   /**
    * Mirrors this area at the horizontal line through p_pole.
    */
   PlaArea mirror_horizontal(PlaPointInt p_pole);

   /**
    * Mirrors this area at the vertical line through p_pole.
    */
   PlaArea mirror_vertical(PlaPointInt p_pole);

   /**
    * Returns an approximation of the corners of this area.
    */
   PlaPointFloat[] corner_approx_arr();

   /**
    * Returns a division of this area into convex pieces
    */
   ShapeTile[] split_to_convex();

   }
