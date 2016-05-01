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
 * RationalVector.java
 *
 * Created on 1. Februar 2003, 09:16
 */

package freert.planar;

import java.math.BigInteger;
import freert.varie.BigIntAux;
import freert.varie.Signum;

/**
 *
 * Analog RationalPoint, but implementing the functionality of a Vector instead of the functionality of a Point.
 *
 * @author Alfons Wirtz
 */

public final class PlaVectorRational extends PlaVector
   {
   private static final long serialVersionUID = 1L;

   public final BigInteger rp_x;
   public final BigInteger rp_y;
   public final BigInteger rp_z;
   
   /**
    * creates a RetionalVector from 3 BigIntegers p_x, p_y and p_z. 
    * They represent the 2-dimensional Vector with the rational number
    * Tuple ( p_x / p_z , p_y / p_z).
    */
   public PlaVectorRational(BigInteger p_x, BigInteger p_y, BigInteger p_z)
      {
      if (p_z.signum() >= 0)
         {
         rp_x = p_x;
         rp_y = p_y;
         rp_z = p_z;
         }
      else
         {
         rp_x = p_x.negate();
         rp_y = p_y.negate();
         rp_z = p_z.negate();
         }
      }

   /**
    * creates a RetionalVector from an IntVector
    */
   PlaVectorRational(PlaVectorInt p_vector)
      {
      rp_x = BigInteger.valueOf(p_vector.point_x);
      rp_y = BigInteger.valueOf(p_vector.point_y);
      rp_z = BigInteger.ONE;
      }

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }

   /**
    * returns true, if the x and y coordinates of this vector are 0
    */
   public final boolean is_zero()
      {
      return rp_x.signum() == 0 && rp_y.signum() == 0;
      }

   /**
    * returns true, if this RationalVector is equal to p_ob
    */
   @Override
   public final boolean equals(Object p_other)
      {
      if (p_other == null) return false;

      if (this == p_other) return true;

      if ( ! ( p_other instanceof PlaVectorRational ) ) return false;

      PlaVectorRational other = (PlaVectorRational) p_other;
      
      BigInteger det = BigIntAux.determinant(rp_x, other.rp_x, rp_z, other.rp_z);

      if (det.signum() != 0)
         {
         return false;
         }
      
      det = BigIntAux.determinant(rp_y, other.rp_y, rp_z, other.rp_z);

      return (det.signum() == 0);
      }

   /**
    * returns the Vector such that this plus minus() is zero
    */
   @Override
   public PlaVectorRational negate()
      {
      return new PlaVectorRational(rp_x.negate(), rp_y.negate(), rp_z);
      }

   public boolean is_orthogonal()
      {
      return (rp_x.signum() == 0 || rp_y.signum() == 0);
      }

   public boolean is_diagonal()
      {
      return rp_x.abs().equals(rp_y.abs());
      }

   /**
    * approximates the coordinates of this vector by float coordinates
    */
   public PlaPointFloat to_float()
      {
      double xd = rp_x.doubleValue();
      double yd = rp_y.doubleValue();
      double zd = rp_z.doubleValue();
      return new PlaPointFloat(xd / zd, yd / zd);
      }

   public PlaVector turn_90_degree(int p_factor)
      {
      while (p_factor < 0) p_factor += 4;

      while (p_factor >= 4)  p_factor -= 4;

      BigInteger new_x;
      BigInteger new_y;
      switch (p_factor)
         {
         case 0: // 0 degree
            new_x = rp_x;
            new_y = rp_y;
            break;
         case 1: // 90 degree
            new_x = rp_y.negate();
            new_y = rp_x;
            break;
         case 2: // 180 degree
            new_x = rp_x.negate();
            new_y = rp_y.negate();
            break;
         case 3: // 270 degree
            new_x = rp_y;
            new_y = rp_x.negate();
            break;
         default:
            return this;
         }
      return new PlaVectorRational(new_x, new_y, rp_z);
      }

   public PlaVector mirror_at_y_axis()
      {
      return new PlaVectorRational(rp_x.negate(), rp_y, rp_z);
      }

   public PlaVector mirror_at_x_axis()
      {
      return new PlaVectorRational(rp_x, rp_y.negate(), rp_z);
      }

   @Override
   PlaDirection to_normalized_direction()
      {
      return new PlaDirection(rp_x, rp_y);
      }

   @Override
   public double scalar_product(PlaVectorInt p_other)
      {
      PlaVector other = new PlaVectorRational(p_other);
      return other.scalar_product(this);
      }

   @Override
   public double scalar_product(PlaVectorRational p_other)
      {
      PlaPointFloat v1 = to_float();
      PlaPointFloat v2 = p_other.to_float();
      return v1.v_x * v2.v_x + v1.v_y * v2.v_y;
      }

   @Override
   public Signum projection(PlaVectorInt p_other)
      {
      PlaVector other = new PlaVectorRational(p_other);
      return other.projection(this);
      }

   @Override
   public Signum projection(PlaVectorRational p_other)
      {
      BigInteger tmp1 = rp_x.multiply(p_other.rp_x);
      BigInteger tmp2 = rp_y.multiply(p_other.rp_y);
      BigInteger tmp3 = tmp1.add(tmp2);
      int result = tmp3.signum();
      return Signum.of(result);
      }

   @Override
   public final PlaVectorRational add(PlaVectorInt p_other)
      {
      PlaVectorRational other = new PlaVectorRational(p_other);
      return add(other);
      }

   @Override
   public  final PlaVectorRational add(PlaVectorRational p_other)
      {
      BigInteger v1[] = new BigInteger[3];
      v1[0] = rp_x;
      v1[1] = rp_y;
      v1[2] = rp_z;

      BigInteger v2[] = new BigInteger[3];
      v2[0] = p_other.rp_x;
      v2[1] = p_other.rp_y;
      v2[2] = p_other.rp_z;
      BigInteger[] result = BigIntAux.add_rational_coordinates(v1, v2);
      return new PlaVectorRational(result[0], result[1], result[2]);
      }

   @Override
   PlaPointRational add_to(PlaPointInt p_point)
      {
      BigInteger new_x = rp_z.multiply(BigInteger.valueOf(p_point.v_x));
      new_x = new_x.add(rp_x);
      BigInteger new_y = rp_z.multiply(BigInteger.valueOf(p_point.v_y));
      new_y = new_y.add(rp_y);
      
      return new PlaPointRational(new_x, new_y, rp_z);
      }
   
   @Override
   public PlaSide side_of(PlaVectorInt p_other)
      {
      PlaVectorRational other = new PlaVectorRational(p_other);
      
      return side_of(other);
      }

   @Override
   public PlaSide side_of(PlaVectorRational p_other)
      {
      BigInteger tmp_1 = rp_x.multiply(p_other.rp_y);

      BigInteger tmp_2 = rp_y.multiply(p_other.rp_x);
      
      BigInteger determinant = tmp_1.subtract(tmp_2);

      int signum = determinant.signum();
      
      return PlaSide.get_side_of(signum);
      }
   }