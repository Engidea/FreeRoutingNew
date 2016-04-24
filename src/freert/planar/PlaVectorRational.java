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

   public final BigInteger v_x;
   public final BigInteger v_y;
   public final BigInteger v_z;
   
   /**
    * creates a RetionalVector from 3 BigIntegers p_x, p_y and p_z. 
    * They represent the 2-dimensional Vector with the rational number
    * Tuple ( p_x / p_z , p_y / p_z).
    */
   public PlaVectorRational(BigInteger p_x, BigInteger p_y, BigInteger p_z)
      {
      if (p_z.signum() >= 0)
         {
         v_x = p_x;
         v_y = p_y;
         v_z = p_z;
         }
      else
         {
         v_x = p_x.negate();
         v_y = p_y.negate();
         v_z = p_z.negate();
         }
      }

   /**
    * creates a RetionalVector from an IntVector
    */
   PlaVectorRational(PlaVectorInt p_vector)
      {
      v_x = BigInteger.valueOf(p_vector.point_x);
      v_y = BigInteger.valueOf(p_vector.point_y);
      v_z = BigInteger.ONE;
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
      return v_x.signum() == 0 && v_y.signum() == 0;
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
      BigInteger det = BigIntAux.determinant(v_x, other.rp_x, v_z, other.rp_z);
      if (det.signum() != 0)
         {
         return false;
         }
      det = BigIntAux.determinant(v_y, other.rp_y, v_z, other.rp_z);

      return (det.signum() == 0);
      }

   /**
    * returns the Vector such that this plus this.minus() is zero
    */
   @Override
   public PlaVectorRational negate()
      {
      return new PlaVectorRational(v_x.negate(), v_y.negate(), v_z);
      }

   public boolean is_orthogonal()
      {
      return (v_x.signum() == 0 || v_y.signum() == 0);
      }

   public boolean is_diagonal()
      {
      return v_x.abs().equals(v_y.abs());
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
    * approximates the coordinates of this vector by float coordinates
    */
   public PlaPointFloat to_float()
      {
      double xd = v_x.doubleValue();
      double yd = v_y.doubleValue();
      double zd = v_z.doubleValue();
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
            new_x = v_x;
            new_y = v_y;
            break;
         case 1: // 90 degree
            new_x = v_y.negate();
            new_y = v_x;
            break;
         case 2: // 180 degree
            new_x = v_x.negate();
            new_y = v_y.negate();
            break;
         case 3: // 270 degree
            new_x = v_y;
            new_y = v_x.negate();
            break;
         default:
            return this;
         }
      return new PlaVectorRational(new_x, new_y, this.v_z);
      }

   public PlaVector mirror_at_y_axis()
      {
      return new PlaVectorRational(this.v_x.negate(), this.v_y, this.v_z);
      }

   public PlaVector mirror_at_x_axis()
      {
      return new PlaVectorRational(this.v_x, this.v_y.negate(), this.v_z);
      }

   @Override
   PlaDirection to_normalized_direction()
      {
      BigInteger dx = v_x;
      BigInteger dy = v_y;
      
      BigInteger gcd = dx.gcd(v_y);
      
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
      
      return new PlaDirection(dx.longValue(), dy.longValue());
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
      return v1.v_x * v2.v_x + v1.v_y * v2.v_y;
      }

   Signum projection(PlaVectorInt p_other)
      {
      PlaVector other = new PlaVectorRational(p_other);
      return other.projection(this);
      }

   Signum projection(PlaVectorRational p_other)
      {
      BigInteger tmp1 = v_x.multiply(p_other.v_x);
      BigInteger tmp2 = v_y.multiply(p_other.v_y);
      BigInteger tmp3 = tmp1.add(tmp2);
      int result = tmp3.signum();
      return Signum.of(result);
      }

   protected final PlaVector add(PlaVectorInt p_other)
      {
      PlaVectorRational other = new PlaVectorRational(p_other);
      return add(other);
      }

   protected  final PlaVector add(PlaVectorRational p_other)
      {
      BigInteger v1[] = new BigInteger[3];
      v1[0] = v_x;
      v1[1] = v_y;
      v1[2] = v_z;

      BigInteger v2[] = new BigInteger[3];
      v2[0] = p_other.v_x;
      v2[1] = p_other.v_y;
      v2[2] = p_other.v_z;
      BigInteger[] result = BigIntAux.add_rational_coordinates(v1, v2);
      return new PlaVectorRational(result[0], result[1], result[2]);
      }

   @Override
   PlaPointRational add_to(PlaPointInt p_point)
      {
      BigInteger new_x = v_z.multiply(BigInteger.valueOf(p_point.v_x));
      new_x = new_x.add(v_x);
      BigInteger new_y = v_z.multiply(BigInteger.valueOf(p_point.v_y));
      new_y = new_y.add(v_y);
      
      return new PlaPointRational(new_x, new_y, v_z);
      }

   /*
   @Override
   PlaPointRational add_to(PlaPointRational p_point)
      {
      BigInteger v1[] = new BigInteger[3];
      v1[0] = v_x;
      v1[1] = v_y;
      v1[2] = v_z;

      BigInteger v2[] = new BigInteger[3];
      v2[0] = p_point.rp_x;
      v2[1] = p_point.rp_y;
      v2[2] = p_point.rp_z;

      BigInteger[] result = BigIntAux.add_rational_coordinates(v1, v2);
      
      return new PlaPointRational(result[0], result[1], result[2]);
      }
*/
   
   @Override
   public PlaSide side_of(PlaVectorInt p_other)
      {
      PlaVectorRational other = new PlaVectorRational(p_other);
      
      return side_of(other);
      }

   @Override
   public PlaSide side_of(PlaVectorRational p_other)
      {
      BigInteger tmp_1 = v_y.multiply(p_other.v_x);
      BigInteger tmp_2 = v_x.multiply(p_other.v_y);
      BigInteger determinant = tmp_1.subtract(tmp_2);
      int signum = determinant.signum();
      return PlaSide.get_side_of(signum);
      }
   }