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
import datastructures.BigIntAux;
import datastructures.Signum;

/**
 *
 * Analog RationalPoint, but implementing the functionality of a Vector instead of the functionality of a Point.
 *
 * @author Alfons Wirtz
 */

public final class PlaVectorRational extends PlaVector
   {
   private static final long serialVersionUID = 1L;

   public final BigInteger x;
   public final BigInteger y;
   public final BigInteger z;
   
   /**
    * creates a RetionalVector from 3 BigIntegers p_x, p_y and p_z. 
    * They represent the 2-dimensional Vector with the rational number
    * Tuple ( p_x / p_z , p_y / p_z).
    */
   public PlaVectorRational(BigInteger p_x, BigInteger p_y, BigInteger p_z)
      {
      if (p_z.signum() >= 0)
         {
         x = p_x;
         y = p_y;
         z = p_z;
         }
      else
         {
         x = p_x.negate();
         y = p_y.negate();
         z = p_z.negate();
         }
      }

   /**
    * creates a RetionalVector from an IntVector
    */
   PlaVectorRational(PlaVectorInt p_vector)
      {
      x = BigInteger.valueOf(p_vector.point_x);
      y = BigInteger.valueOf(p_vector.point_y);
      z = BigInteger.ONE;
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
      return x.signum() == 0 && y.signum() == 0;
      }

   /**
    * returns true, if this RationalVector is equal to p_ob
    */
   public final boolean equals(Object p_ob)
      {
      if (this == p_ob)
         {
         return true;
         }
      if (p_ob == null)
         {
         return false;
         }
      if (getClass() != p_ob.getClass())
         {
         return false;
         }
      PlaPointRational other = (PlaPointRational) p_ob;
      BigInteger det = BigIntAux.determinant(x, other.rp_x, z, other.rp_z);
      if (det.signum() != 0)
         {
         return false;
         }
      det = BigIntAux.determinant(y, other.rp_y, z, other.rp_z);

      return (det.signum() == 0);
      }

   /**
    * returns the Vector such that this plus this.minus() is zero
    */
   @Override
   public PlaVectorRational negate()
      {
      return new PlaVectorRational(x.negate(), y.negate(), z);
      }

   /**
    * adds p_other to this vector
    */
   public final PlaVector add(PlaVector p_other)
      {
      return p_other.add(this);
      }

   /**
    * Let L be the line from the Zero Vector to p_other. The function returns Side.ON_THE_LEFT, if this Vector is on the left of L
    * Side.ON_THE_RIGHT, if this Vector is on the right of L and Side.COLLINEAR, if this Vector is collinear with L.
    */
   public PlaSide side_of(PlaVector p_other)
      {
      PlaSide tmp = p_other.side_of(this);
      return tmp.negate();
      }

   public boolean is_orthogonal()
      {
      return (x.signum() == 0 || y.signum() == 0);
      }

   public boolean is_diagonal()
      {
      return x.abs().equals(y.abs());
      }

   /**
    * The function returns Signum.POSITIVE, if the scalar product of this vector and p_other > 0, Signum.NEGATIVE, if the scalar
    * product is < 0, and Signum.ZERO, if the scalar product is equal 0.
    */
   public Signum projection(PlaVector p_other)
      {
      return p_other.projection(this);
      }

   /**
    * calculates the scalar product of this vector and p_other
    */
   public double scalar_product(PlaVector p_other)
      {
      return p_other.scalar_product(this);
      }

   /**
    * approximates the coordinates of this vector by float coordinates
    */
   public PlaPointFloat to_float()
      {
      double xd = x.doubleValue();
      double yd = y.doubleValue();
      double zd = z.doubleValue();
      return new PlaPointFloat(xd / zd, yd / zd);
      }

   public PlaVector change_length_approx(double p_lenght)
      {
      System.out.println("RationalVector: change_length_approx not yet implemented");
      return this;
      }

   public PlaVector turn_90_degree(int p_factor)
      {
      int n = p_factor;
      while (n < 0)
         {
         n += 4;
         }
      while (n >= 4)
         {
         n -= 4;
         }
      BigInteger new_x;
      BigInteger new_y;
      switch (n)
         {
         case 0: // 0 degree
            new_x = x;
            new_y = y;
            break;
         case 1: // 90 degree
            new_x = y.negate();
            new_y = x;
            break;
         case 2: // 180 degree
            new_x = x.negate();
            new_y = y.negate();
            break;
         case 3: // 270 degree
            new_x = y;
            new_y = x.negate();
            break;
         default:
            return this;
         }
      return new PlaVectorRational(new_x, new_y, this.z);
      }

   public PlaVector mirror_at_y_axis()
      {
      return new PlaVectorRational(this.x.negate(), this.y, this.z);
      }

   public PlaVector mirror_at_x_axis()
      {
      return new PlaVectorRational(this.x, this.y.negate(), this.z);
      }

   @Override
   PlaDirection to_normalized_direction()
      {
      BigInteger dx = x;
      BigInteger dy = y;
      
      BigInteger gcd = dx.gcd(y);
      
      dx = dx.divide(gcd);
      dy = dy.divide(gcd);
      
      BigInteger two = BigInteger.valueOf(2);
      
      while ( PlaLimits.is_critical(dx) || PlaLimits.is_critical(dy) )
         {
         // this really, should never happen, but if it does I just reduce accuracy until things fits
         System.err.println("to_normalize_direction: REDUCING accuracy");
         dx = dx.divide(two);
         dy = dx.divide(two);
         }
      
      return new PlaDirectionLong(dx.longValue(), dy.longValue());
      }

   double scalar_product(PlaVectorInt p_other)
      {
      PlaVector other = new PlaVectorRational(p_other);
      return other.scalar_product(this);
      }

   double scalar_product(PlaVectorRational p_other)
      {
      PlaPointFloat v1 = to_float();
      PlaPointFloat v2 = p_other.to_float();
      return v1.point_x * v2.point_x + v1.point_y * v2.point_y;
      }

   Signum projection(PlaVectorInt p_other)
      {
      PlaVector other = new PlaVectorRational(p_other);
      return other.projection(this);
      }

   Signum projection(PlaVectorRational p_other)
      {
      BigInteger tmp1 = x.multiply(p_other.x);
      BigInteger tmp2 = y.multiply(p_other.y);
      BigInteger tmp3 = tmp1.add(tmp2);
      int result = tmp3.signum();
      return Signum.of(result);
      }

   final PlaVector add(PlaVectorInt p_other)
      {
      PlaVectorRational other = new PlaVectorRational(p_other);
      return add(other);
      }

   final PlaVector add(PlaVectorRational p_other)
      {
      BigInteger v1[] = new BigInteger[3];
      v1[0] = x;
      v1[1] = y;
      v1[2] = z;

      BigInteger v2[] = new BigInteger[3];
      v2[0] = p_other.x;
      v2[1] = p_other.y;
      v2[2] = p_other.z;
      BigInteger[] result = BigIntAux.add_rational_coordinates(v1, v2);
      return new PlaVectorRational(result[0], result[1], result[2]);
      }

   @Override
   PlaPointRational add_to(PlaPointInt p_point)
      {
      BigInteger new_x = z.multiply(BigInteger.valueOf(p_point.v_x));
      new_x = new_x.add(x);
      BigInteger new_y = z.multiply(BigInteger.valueOf(p_point.v_y));
      new_y = new_y.add(y);
      
      return new PlaPointRational(new_x, new_y, z);
      }

   @Override
   PlaPointRational add_to(PlaPointRational p_point)
      {
      BigInteger v1[] = new BigInteger[3];
      v1[0] = x;
      v1[1] = y;
      v1[2] = z;

      BigInteger v2[] = new BigInteger[3];
      v2[0] = p_point.rp_x;
      v2[1] = p_point.rp_y;
      v2[2] = p_point.rp_z;

      BigInteger[] result = BigIntAux.add_rational_coordinates(v1, v2);
      
      return new PlaPointRational(result[0], result[1], result[2]);
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
      BigInteger tmp_1 = y.multiply(p_other.x);
      BigInteger tmp_2 = x.multiply(p_other.y);
      BigInteger determinant = tmp_1.subtract(tmp_2);
      int signum = determinant.signum();
      return PlaSide.new_side_of(signum);
      }
   }