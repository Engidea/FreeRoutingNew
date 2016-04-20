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
 * BigIntDirection.java
 *
 * Created on 4. Februar 2003, 14:10
 */

package planar;

import java.math.BigInteger;

/**
 * Implements the abstract class Direction as a tuple of infinite precision integers.
 * 
 * @author Alfons Wirtz
 */

public class PlaDirectionBigInt extends PlaDirection implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   final BigInteger dir_x;
   final BigInteger dir_y;

   PlaDirectionBigInt(BigInteger p_x, BigInteger p_y)
      {
      dir_x = p_x;
      dir_y = p_y;
      }

   PlaDirectionBigInt(PlaDirectionInt p_dir)
      {
      dir_x = BigInteger.valueOf(p_dir.dir_x);
      dir_y = BigInteger.valueOf(p_dir.dir_y);
      }
   
   @Override
   public boolean is_orthogonal()
      {
      return (dir_x.signum() == 0 || dir_y.signum() == 0);
      }

   @Override
   public boolean is_diagonal()
      {
      return dir_x.abs().equals(dir_y.abs());
      }

   @Override
   public PlaVectorRational get_vector()
      {
      return new PlaVectorRational(dir_x, dir_y, BigInteger.ONE);
      }

   @Override
   public PlaDirection turn_45_degree(int p_factor)
      {
      throw new IllegalArgumentException("BigIntDirection: turn_45_degree not yet implemented");
      }

   @Override
   public PlaDirectionBigInt opposite()
      {
      return new PlaDirectionBigInt(dir_x.negate(), dir_y.negate());
      }


   /**
    * Implements the Comparable interface. Returns 1, if this direction has a strict bigger angle with the positive x-axis than p_other_direction, 0, if this direction is equal to p_other_direction,
    * and -1 otherwise. Throws an exception, if p_other_direction is not a Direction.
    */
   @Override
   public int compareTo(PlaDirection p_other_direction)
      {
      return -p_other_direction.compareTo(this);
      }

   @Override
   int compareTo(PlaDirectionInt p_other)
      {
      PlaDirectionBigInt other = new PlaDirectionBigInt(p_other);
      return compareTo(other);
      }

   @Override
   int compareTo(PlaDirectionBigInt p_other)
      {
      int x1 = dir_x.signum();
      int y1 = dir_y.signum();
      int x2 = p_other.dir_x.signum();
      int y2 = p_other.dir_y.signum();
      if (y1 > 0)
         {
         if (y2 < 0)
            {
            return -1;
            }
         if (y2 == 0)
            {
            if (x2 > 0)
               {
               return 1;
               }
            return -1;
            }
         }
      else if (y1 < 0)
         {
         if (y2 >= 0)
            {
            return 1;
            }
         }
      else
         // y1 == 0
         {
         if (x1 > 0)
            {
            if (y2 != 0 || x2 < 0)
               {
               return -1;
               }
            return 0;
            }
         // x1 < 0
         if (y2 > 0 || y2 == 0 && x2 > 0)
            {
            return 1;
            }
         if (y2 < 0)
            {
            return -1;
            }
         return 0;
         }

      // now this direction and p_other are located in the same
      // open horizontal half plane

      BigInteger tmp_1 = dir_y.multiply(p_other.dir_x);
      BigInteger tmp_2 = dir_x.multiply(p_other.dir_y);
      BigInteger determinant = tmp_1.subtract(tmp_2);

      return determinant.signum();
      }
   }