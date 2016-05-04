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
 * IntVector.java
 *
 * Created on 1. Februar 2003, 14:47
 */

package freert.planar;


/**
 *
 * Implementation of the interface Vector via a tuple of integers
 *
 * @author Alfons Wirtz
 */

public final class PlaVectorInt extends PlaVector
   {
   private static final long serialVersionUID = 1L;

   public final int point_x;
   public final int point_y;
   
   /**
    * creates an IntVector from two integer coordinates
    */
   public PlaVectorInt(int p_x, int p_y)
      {
      // range check omitted for performance reasons
      point_x = p_x;
      point_y = p_y;
      }

   public PlaVectorInt(double p_x, double p_y)
      {
      long along = Math.round(p_x);
      
      if ( along >= Integer.MAX_VALUE ) throw new IllegalArgumentException("IntVector p_x too + big");
      
      if ( along <= Integer.MIN_VALUE ) throw new IllegalArgumentException("IntVector p_x too - big");
      
      point_x = (int)along;
      
      along = Math.round(p_y);
      
      if ( along >= Integer.MAX_VALUE ) throw new IllegalArgumentException("IntVector p_y too + big");
      
      if ( along <= Integer.MIN_VALUE ) throw new IllegalArgumentException("IntVector p_y too - big");
      
      point_y = (int) along;
      }
   
   
   public final PlaVectorInt round()
      {
      return this;
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

      return (point_x == other.point_x && point_y == other.point_y);
      }

   /**
    * returns true, if both coordinates of this vector are 0
    */
   @Override
   public final boolean is_zero()
      {
      return point_x == 0 && point_y == 0;
      }

   /**
    * returns the Vector such that this plus this.minus() is zero
    */
   @Override
   public PlaVectorInt negate()
      {
      return new PlaVectorInt(-point_x, -point_y);
      }

   @Override
   public boolean is_orthogonal()
      {
      return (point_x == 0 || point_y == 0);
      }

   @Override
   public boolean is_diagonal()
      {
      return (Math.abs(point_x) == Math.abs(point_y));
      }

   /**
    * Calculates the determinant of the matrix consisting of this Vector and p_other
    * it is also the area between the two vectors
    */
   public final long determinant(PlaVectorInt p_other)
      {
      return (long) point_x * p_other.point_y - (long) point_y * p_other.point_x;
      }

   @Override
   public PlaVectorInt turn_90_degree(int p_factor)
      {
      while (p_factor < 0)
         p_factor += 4;

      while (p_factor >= 4)
         p_factor -= 4;

      int new_x;
      int new_y;
      switch (p_factor)
         {
         case 0: // 0 degree
            new_x = point_x;
            new_y = point_y;
            break;
         case 1: // 90 degree
            new_x = -point_y;
            new_y = point_x;
            break;
         case 2: // 180 degree
            new_x = -point_x;
            new_y = -point_y;
            break;
         case 3: // 270 degree
            new_x = point_y;
            new_y = -point_x;
            break;
         default:
            new_x = 0;
            new_y = 0;
         }
      return new PlaVectorInt(new_x, new_y);
      }

   @Override
   public PlaVectorInt mirror_at_y_axis()
      {
      return new PlaVectorInt(-point_x, point_y);
      }

   @Override
   public PlaVector mirror_at_x_axis()
      {
      return new PlaVectorInt(point_x, -point_y);
      }


   @Override
   public PlaVectorInt add(PlaVectorInt p_other)
      {
      return new PlaVectorInt(point_x + p_other.point_x, point_y + p_other.point_y);
      }

   @Override
   public PlaVectorRational add(PlaVectorRational p_other)
      {
      return p_other.add(this);
      }

   /**
    * returns the Point, which results from adding this vector to p_point
    */
   @Override
   public PlaPointInt add_to(PlaPointInt p_point)
      {
      return new PlaPointInt(p_point.v_x + point_x, p_point.v_y + point_y);
      }

   PlaPoint add_to(PlaPointRational p_point)
      {
      return p_point.translate_by(this);
      }

   @Override
   public PlaSide side_of(PlaVectorInt p_other)
      {
      return PlaSide.get_side_of(determinant(p_other));
      }

   @Override
   public PlaSide side_of(PlaVectorRational p_other)
      {
      PlaSide tmp = p_other.side_of(this);
      
      return tmp.negate();
      }


   @Override
   public PlaPointFloat to_float()
      {
      return new PlaPointFloat(point_x, point_y);
      }

   @Override
   PlaDirection to_direction()
      {
      return new PlaDirection(this);
      }

   /*
   @Override
   public Signum projection(PlaVectorInt p_other)
      {
      double tmp = (double) point_x * p_other.point_x + (double) point_y * p_other.point_y;
      return Signum.of(tmp);
      }
*/
   @Override
   public double scalar_product(PlaVectorInt p_other)
      {
      return (double) point_x * p_other.point_x + (double) point_y * p_other.point_y;
      }

   @Override
   public double scalar_product(PlaVectorRational p_other)
      {
      return p_other.scalar_product(this);
      }
   
   /**
    * Returns an approximation of the cosinus of the angle between this vector and p_other by a double.
    */
   public double cos_angle(PlaVectorInt p_other)
      {
      double result = scalar_product(p_other);
      result /= to_float().distance() * p_other.to_float().distance();
      return result;
      }
   

   }