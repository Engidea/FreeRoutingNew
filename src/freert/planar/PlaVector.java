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

   // Standard implementation of the zero vector .
   public static final PlaVectorInt ZERO = new PlaVectorInt(0, 0);
   
   /**
    * @return true, if this vector is equal to the zero vector.
    */
   public abstract boolean is_zero();

   /**
    * returns the Vector such that this plus negate() is zero
    */
   public abstract PlaVector negate();

   /**
    * adds p_other to this vector
    */
   public final PlaVector add(PlaVector p_other)
      {
      if ( p_other == null ) return null;

      if ( p_other instanceof PlaVectorInt )
         return add((PlaVectorInt)p_other);
      else if ( p_other instanceof PlaVectorRational )
         return add((PlaVectorRational)p_other);
      else 
         return null;
      }

   public abstract PlaVector add(PlaVectorInt p_other);
   public abstract PlaVector add(PlaVectorRational p_other);

   
   
   /**
    * Let L be the line from the Zero Vector to p_other. 
    * The function returns Side.ON_THE_LEFT, if this Vector is on the left of L
    * Side.ON_THE_RIGHT, if this Vector is on the right of L 
    * Side.COLLINEAR, if this Vector is collinear with L.
    */
   public final PlaSide side_of(PlaVector p_other)
      {
      if ( p_other == null ) return null;
      
      if ( p_other instanceof PlaVectorInt )
         return side_of((PlaVectorInt)p_other);
      else if ( p_other instanceof PlaVectorRational )
         return side_of((PlaVectorRational)p_other);
      else 
         return null;
      }
   
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

   
   /**
    * Returns an approximation of the scalar product of this vector with p_other by a double.
    */
   public final double scalar_product(PlaVector p_other)
      {
      if ( p_other == null ) throw new IllegalArgumentException("p_other is null");
      
      if ( p_other instanceof PlaVectorInt )
         return scalar_product((PlaVectorInt)p_other);
      else if ( p_other instanceof PlaVectorRational )
         return scalar_product((PlaVectorRational)p_other);
      else 
         throw new IllegalArgumentException("p_other is unsupported");
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
    * Returns an approximation of the cosinus of the angle between this vector and p_other by a double.
    */
   private double cos_angle(PlaVector p_other)
      {
      double result = scalar_product(p_other);
      result /= to_float().distance() * p_other.to_float().distance();
      return result;
      }

   /**
    * Returns an approximation of the signed angle between this vector and p_other.
    */
   public double angle_approx(PlaVector p_other)
      {
      double result = Math.acos(cos_angle(p_other));

      if (side_of(p_other) == PlaSide.ON_THE_LEFT)
         {
         result = -result;
         }
      
      return result;
      }

   /**
    * Returns an approximation of the signed angle between this vector and the x axis.
    */
   public double angle_approx()
      {
      PlaVector other = new PlaVectorInt(1, 0);
      
      return other.angle_approx(this);
      }

   /**
    * Returns an approximation vector of this vector with the same direction and length p_length.
    */
   public final PlaVectorInt change_length_approx(double p_length)
      {
      PlaPointFloat new_point = to_float().change_size(p_length);
      return new_point.round().difference_by(PlaPoint.ZERO);
      }

   abstract PlaDirection to_direction();

   
   abstract PlaPoint add_to(PlaPointInt p_point);





   }