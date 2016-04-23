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

import java.math.BigInteger;
import datastructures.Signum;

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
   public abstract PlaVector add(PlaVector p_other);

   /**
    * Let L be the line from the Zero Vector to p_other. 
    * The function returns Side.ON_THE_LEFT, if this Vector is on the left of L
    * Side.ON_THE_RIGHT, if this Vector is on the right of L 
    * Side.COLLINEAR, if this Vector is collinear with L.
    */
   public abstract PlaSide side_of(PlaVector p_other);
   
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
    * The function returns Signum.POSITIVE, if the scalar product of this vector and p_other > 0, Signum.NEGATIVE, if the scalar
    * product Vector is < 0, and Signum.ZERO, if the scalar product is equal 0.
    */
   public abstract Signum projection(PlaVector p_other);

   /**
    * Returns an approximation of the scalar product of this vector with p_other by a double.
    */
   public abstract double scalar_product(PlaVector p_other);

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
    * Creates a 2-dimensional Vector from the 3 input values. If p_z != 0 it correspondents to the Vector in the plane with rational
    * number coordinates (p_x / p_z, p_y / p_z).
    */
   public static PlaVector get_instance(BigInteger p_x, BigInteger p_y, BigInteger p_z)
      {
      if (p_z.signum() < 0)
         {
         // the dominator z of a RationalVector is expected to be positive
         p_x = p_x.negate();
         p_y = p_y.negate();
         p_z = p_z.negate();

         }
      if ((p_x.mod(p_z)).signum() == 0 && (p_x.mod(p_z)).signum() == 0)
         {
         // p_x and p_y can be divided by p_z
         p_x = p_x.divide(p_z);
         p_y = p_y.divide(p_z);
         p_z = BigInteger.ONE;
         }
      if (p_z.equals(BigInteger.ONE))
         {
         if ((p_x.abs()).compareTo(PlaLimits.CRIT_INT_BIG) <= 0 && (p_y.abs()).compareTo(PlaLimits.CRIT_INT_BIG) <= 0)
            {
            // the Point fits into an IntPoint
            return new PlaVectorInt(p_x.intValue(), p_y.intValue());
            }
         }
      return new PlaVectorRational(p_x, p_y, p_z);
      }

   /**
    * returns an approximation of the euclidian length of this vector
    */
   public double length_approx()
      {
      return to_float().distance();
      }

   /**
    * Returns an approximation of the cosinus of the angle between this vector and p_other by a double.
    */
   public double cos_angle(PlaVector p_other)
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
   public abstract PlaVector change_length_approx(double p_lenght);

   abstract PlaDirection to_normalized_direction();

   // auxiliary functions needed because the virtual function mechanism
   // does not work in parameter position

   abstract PlaVector add(PlaVectorInt p_other);

   abstract PlaVector add(PlaVectorRational p_other);

   abstract PlaPoint add_to(PlaPointInt p_point);

   abstract PlaPoint add_to(PlaPointRational p_point);


   /**
    * The function returns Signum.POSITIVE, if the scalar product of this vector and p_other > 0, Signum.NEGATIVE, if the scalar
    * product Vector is < 0, and Signum.ZERO, if the scalar product is equal 0.
    */
   abstract Signum projection(PlaVectorInt p_other);

   abstract Signum projection(PlaVectorRational p_other);

   abstract double scalar_product(PlaVectorInt p_other);

   abstract double scalar_product(PlaVectorRational p_other);

   }