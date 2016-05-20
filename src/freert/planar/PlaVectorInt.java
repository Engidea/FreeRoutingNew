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
 * Created on 1. Februar 2003, 14:47
 */

package freert.planar;


/**
 * Implementation of the interface Vector via a tuple of integers
 * @author Alfons Wirtz
 * @author Damiano Bolla, unwrapped from rationals...
 */

public final class PlaVectorInt  implements java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;
   
   // Standard implementation of the zero vector .
   public static final PlaVectorInt ZERO = new PlaVectorInt(0, 0);

   public final int v_x;
   public final int v_y;
   
   /**
    * creates an IntVector from two integer coordinates
    */
   public PlaVectorInt(int p_x, int p_y)
      {
      // range check omitted for performance reasons
      v_x = p_x;
      v_y = p_y;
      }

   public PlaVectorInt(double p_x, double p_y)
      {
      long along = Math.round(p_x);
      
      if ( along >= Integer.MAX_VALUE ) throw new IllegalArgumentException("IntVector p_x too + big");
      
      if ( along <= Integer.MIN_VALUE ) throw new IllegalArgumentException("IntVector p_x too - big");
      
      v_x = (int)along;
      
      along = Math.round(p_y);
      
      if ( along >= Integer.MAX_VALUE ) throw new IllegalArgumentException("IntVector p_y too + big");
      
      if ( along <= Integer.MIN_VALUE ) throw new IllegalArgumentException("IntVector p_y too - big");
      
      v_y = (int) along;
      }
   
   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
   
   /**
    * returns true, if this IntVector is equal to p_ob
    */
   @Override
   public boolean equals(Object p_ob)
      {
      if (p_ob == null) return false;

      if (this == p_ob) return true;

      if ( !( p_ob instanceof PlaVectorInt) ) return false;

      PlaVectorInt other = (PlaVectorInt) p_ob;

      return v_x == other.v_x && v_y == other.v_y;
      }

   /**
    * returns the Vector such that this plus this.minus() is zero
    */
   public final PlaVectorInt negate()
      {
      return new PlaVectorInt(-v_x, -v_y);
      }

   public boolean is_orthogonal()
      {
      return v_x == 0 || v_y == 0;
      }

   public final boolean is_diagonal()
      {
      return  Math.abs(v_x) == Math.abs(v_y);
      }

   /**
    * Basically the distnce from 0,0 to v_x,v_y
    * @return an approximation of the euclidian length of this vector
    */
   public final double distance()
      {
      return to_float().distance();
      }
   
   /**
    * Calculates the determinant of the matrix consisting of this Vector and p_other
    * it is also the area between the two vectors
    */
   public final long determinant(PlaVectorInt p_other)
      {
      return (long) v_x * p_other.v_y - (long) v_y * p_other.v_x;
      }

   public final PlaVectorInt turn_90_degree(int p_factor)
      {
      while (p_factor < 0)  p_factor += 4;

      while (p_factor >= 4) p_factor -= 4;

      int new_x;
      int new_y;
      switch (p_factor)
         {
         case 0: // 0 degree
            new_x = v_x;
            new_y = v_y;
            break;
         case 1: // 90 degree
            new_x = -v_y;
            new_y = v_x;
            break;
         case 2: // 180 degree
            new_x = -v_x;
            new_y = -v_y;
            break;
         case 3: // 270 degree
            new_x = v_y;
            new_y = -v_x;
            break;
         default:
            new_x = 0;
            new_y = 0;
         }
      return new PlaVectorInt(new_x, new_y);
      }

   public PlaVectorInt mirror_at_y_axis()
      {
      return new PlaVectorInt(-v_x, v_y);
      }

   public final PlaVectorInt mirror_at_x_axis()
      {
      return new PlaVectorInt(v_x, -v_y);
      }


   public final PlaVectorInt add(PlaVectorInt p_other)
      {
      return new PlaVectorInt(v_x + p_other.v_x, v_y + p_other.v_y);
      }

   /**
    * @return the Point, which results from adding this vector to p_point
    */
   public final PlaPointInt add(PlaPointInt p_point)
      {
      return new PlaPointInt(v_x + p_point.v_x, v_y + p_point.v_y);
      }
   
   /**
    * Returns true, if the vector is orthogonal or diagonal
    */
   public boolean is_multiple_of_45_degree()
      {
      return is_orthogonal() || is_diagonal();
      }


   /**
    * Let L be the line from the Zero Vector to p_other. 
    * The function returns Side.ON_THE_LEFT, if this Vector is on the left of L
    * Side.ON_THE_RIGHT, if this Vector is on the right of L 
    * Side.COLLINEAR, if this Vector is collinear with L.
    */
   public PlaSide side_of(PlaVectorInt p_other)
      {
      return PlaSide.get_side_of(determinant(p_other));
      }

   public PlaPointFloat to_float()
      {
      return new PlaPointFloat(v_x, v_y);
      }

   /**
    * Returns an approximation of the signed angle between this vector and p_other.
    */
   public double angle_approx(PlaVectorInt p_other)
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
   public final double angle_approx()
      {
      PlaDirection a_dir = new PlaDirection(this);
      
      return a_dir.angle_approx();
      }

   public double scalar_product(PlaVectorInt p_other)
      {
      return (double) v_x * p_other.v_x + (double) v_y * p_other.v_y;
      }
   
   /**
    * Returns an approximation of the cosinus of the angle between this vector and p_other by a double.
    */
   public final double cos_angle(PlaVectorInt p_other)
      {
      double result = scalar_product(p_other);
      result /= to_float().distance() * p_other.to_float().distance();
      return result;
      }

   /**
    * Returns an approximation vector of this vector with the same direction and length p_length.
    */
   public final PlaVectorInt change_length_approx(double p_length)
      {
      PlaPointFloat new_point = to_float().change_size(p_length);
      
      return new_point.round().to_vector();
      }
   

   }