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
 * Vector.java
 *
 * Created on 1. Februar 2003, 14:28
 */

package freert.planar;


/**
 * Abstract class describing functionality of Vectors. Vectors are used for translating Points in the plane.
 *
 * @author Alfons Wirtz
 */

public abstract class PlaVector implements java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;

   
   /**
    * @return true, if this vector is equal to the zero vector.
    */
   public abstract boolean is_zero();

   /**
    * returns the Vector such that this plus negate() is zero
    */
   public abstract PlaVector negate();
   
   
   public abstract PlaVectorInt round ();

   public abstract PlaVector add(PlaVectorInt p_other);

   public abstract PlaVector add(PlaVectorRational p_other);
   
   /**
    * Let L be the line from the Zero Vector to p_other. 
    * The function returns Side.ON_THE_LEFT, if this Vector is on the left of L
    * Side.ON_THE_RIGHT, if this Vector is on the right of L 
    * Side.COLLINEAR, if this Vector is collinear with L.
    */
   
   public abstract PlaSide side_of(PlaVectorInt p_other);
   public abstract PlaSide side_of(PlaVectorRational p_other);
   

   /**
    * returns true, if the vector is horizontal or vertical
    */
   public abstract boolean is_orthogonal();

   /**
    * returns true, if the vector is diagonal
    */
   public abstract boolean is_diagonal();

   /**
    * Returns true, if the vector is orthogonal or diagonal
    */
   public boolean is_multiple_of_45_degree()
      {
      return is_orthogonal() || is_diagonal();
      }


   public abstract double scalar_product(PlaVectorInt p_other);
   public abstract double scalar_product(PlaVectorRational p_other);
   

   /**
    * approximates the coordinates of this vector by float coordinates
    */
   public abstract PlaPointFloat to_float();

   /**
    * Turns this vector by p_factor times 90 degree.
    */
   public abstract PlaVector turn_90_degree(int p_factor);

   /**
    * Mirrors this vector at the x axis.
    */
   public abstract PlaVector mirror_at_x_axis();

   /**
    * Mirrors this vector at the y axis.
    */
   public abstract PlaVector mirror_at_y_axis();

   /**
    * Basically the distnce from 0,0 to v_x,v_y
    * @return an approximation of the euclidian length of this vector
    */
   public final double distance()
      {
      return to_float().distance();
      }

   /**
    * Returns an approximation vector of this vector with the same direction and length p_length.
    */
   public final PlaVectorInt change_length_approx(double p_length)
      {
      PlaPointFloat new_point = to_float().change_size(p_length);
      
      return new_point.round().difference_by(PlaPointInt.ZERO);
      }

   abstract PlaDirection to_direction();

   
   abstract PlaPoint add_to(PlaPointInt p_point);





   }