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
 * Point.java
 *
 * Created on 1. Februar 2003, 11:38
 */

package freert.planar;

import java.io.Serializable;


/**
 * Abstract class describing functionality for Points in the plane.
 *
 * @author Alfons Wirtz
 */

public abstract class PlaPoint implements PlaObject, Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="PlaPoint.";
   
   public abstract PlaPointInt round();
   
   /**
    * returns the translation of this point by p_vector
    */
   public final PlaPoint translate_by(PlaVector p_vector)
      {
      if ( p_vector == null ) return this;
      
      if ( p_vector instanceof PlaVectorInt )
         return translate_by((PlaVectorInt)p_vector);
      else if ( p_vector instanceof PlaVectorRational )
         return translate_by((PlaVectorRational)p_vector);
      else 
         return null;
      }
   
   public abstract PlaPoint translate_by(PlaVectorInt p_vector);
   public abstract PlaPoint translate_by(PlaVectorRational p_vector);
   

   /**
    * returns the difference vector of this point and p_other
    */
   public final PlaVector difference_by(PlaPoint p_other)
      {
      if ( p_other == null ) return null;
      
      if ( p_other instanceof PlaPointInt )
         return difference_by((PlaPointInt)p_other);
      else if ( p_other instanceof PlaPointRational )
         return difference_by((PlaPointRational)p_other);
      else 
         return null;
      }

   
   public abstract PlaVector difference_by(PlaPointInt p_other);
   public abstract PlaVector difference_by(PlaPointRational p_other);
   
   @Override
   public boolean equals (Object p_other )
      {
      if ( p_other == null ) return false;
      
      if ( p_other instanceof PlaPointInt )
         return equals((PlaPointInt)p_other);
      else if ( p_other instanceof PlaPointRational )
         return equals((PlaPointRational)p_other);
      else 
         {
         // this is really quite serious, better pick it up
         System.err.println(classname+"equals: BAD class="+p_other.getClass());
         return false;
         }
      }
   
   public abstract boolean equals(PlaPointInt p_other);
   public abstract boolean equals(PlaPointRational p_other);

   
   
   /**
    * approximates the coordinates of this point by float coordinates
    */
   public abstract PlaPointFloat to_float();

   /**
    * Returns true, if this point lies in the interiour or on the border of p_box.
    */
   public abstract boolean is_contained_in(ShapeTileBox p_box);

   public abstract PlaSide side_of(PlaLineInt p_line);

   /**
    * returns the nearest point to this point on p_line
    */
   public abstract PlaPoint perpendicular_projection(PlaLineInt p_line);



   /**
    * The function returns Side.ON_THE_LEFT, if this Point is on the left of the line from p_1 to p_2; 
    * Side.ON_THE_RIGHT, if this Point is on the right of the line from p_1 to p_2; 
    * Side.COLLINEAR, if this Point is collinear with p_1 and p_2.
    */
   public final PlaSide side_of(PlaPoint p_1, PlaPoint p_2)
      {
      PlaVector v1 = difference_by(p_1);
      PlaVector v2 = p_2.difference_by(p_1);
      return v1.side_of(v2);
      }

   /**
    * Calculates the perpendicular direction froma this point to p_line. Returns Direction.NULL, if this point lies on p_line.
    */
   public PlaDirection perpendicular_direction(PlaLineInt p_line)
      {
      PlaSide side = side_of(p_line);
      
      if (side == PlaSide.COLLINEAR) return PlaDirection.NULL;
      
      if (side == PlaSide.ON_THE_RIGHT)
         return p_line.direction().turn_45_degree(2);
      else
         return p_line.direction().turn_45_degree(6);
      }

   /**
    * The function returns compare_x (p_other), if the result is not 0. Otherwise it returns compare_y (p_other).
    */
   public int compare_x_y(PlaPoint p_other)
      {
      if ( p_other instanceof PlaPointInt )
         return compare_x_y((PlaPointInt)p_other);
      else
         return compare_x_y((PlaPointRational)p_other);
      }

   public abstract int compare_x_y(PlaPointInt p_other);
   public abstract int compare_x_y(PlaPointRational p_other);

   /**
    * Turns this point by p_factor times 90 degree around p_pole.
    */
   public final PlaPoint turn_90_degree(int p_factor, PlaPoint p_pole)
      {
      PlaVector v = difference_by(p_pole);
      v = v.turn_90_degree(p_factor);
      return p_pole.translate_by(v);
      }

   public abstract PlaPointInt turn_90_degree(int p_factor, PlaPointInt p_pole);

   /**
    * Mirrors this point at the vertical line through p_pole.
    */
   public abstract PlaPointInt mirror_vertical(PlaPointInt p_pole);

   /**
    * Mirrors this point at the horizontal line through p_pole
    */
   public abstract PlaPointInt mirror_horizontal(PlaPointInt p_pole);
   
   public abstract boolean is_rational ();
   


   }