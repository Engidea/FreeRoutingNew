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
 * IntDirection.java
 *
 * Created on 3. Februar 2003, 08:17
 */

package freert.planar;

import datastructures.Signum;

/**
 * Implements an abstract class Direction as an equivalence class of IntVector's.
 *
 * @author Alfons Wirtz
 */

public final class PlaDirectionInt extends PlaDirection implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final int dir_x;
   public final int dir_y;
   

   PlaDirectionInt(int p_x, int p_y)
      {
      dir_x = p_x;
      dir_y = p_y;
      }

   /**
    * Construct a Direction from an IntVector
    * @param p_vector
    */
   public PlaDirectionInt(PlaVectorInt p_vector)
      {
      // need to "reduce" the points if necessary
      int a_x = p_vector.point_x;
      int a_y = p_vector.point_y;

      int gcd = binaryGcd(Math.abs(a_x), Math.abs(a_y));
      
      if (gcd > 1)
         {
         a_x /= gcd;
         a_y /= gcd;
         }
      
      dir_x = a_x;
      dir_y = a_y;
      }

   
   
   @Override
   public boolean is_orthogonal()
      {
      return (dir_x == 0 || dir_y == 0);
      }

   @Override
   public boolean is_diagonal()
      {
      return (Math.abs(dir_x) == Math.abs(dir_y));
      }

   @Override
   public PlaVectorInt get_vector()
      {
      return new PlaVectorInt(dir_x, dir_y);
      }

   @Override
   int compareTo(PlaDirectionInt p_other)
      {
      if (dir_y > 0)
         {
         if (p_other.dir_y < 0)
            {
            return -1;
            }
         if (p_other.dir_y == 0)
            {
            if (p_other.dir_x > 0)
               {
               return 1;
               }
            return -1;
            }
         }
      else if (dir_y < 0)
         {
         if (p_other.dir_y >= 0)
            {
            return 1;
            }
         }
      else
         // y == 0
         {
         if (dir_x > 0)
            {
            if (p_other.dir_y != 0 || p_other.dir_x < 0)
               {
               return -1;
               }
            return 0;
            }
         // x < 0
         if (p_other.dir_y > 0 || p_other.dir_y == 0 && p_other.dir_x > 0)
            {
            return 1;
            }
         if (p_other.dir_y < 0)
            {
            return -1;
            }
         return 0;
         }

      // now this direction and p_other are located in the same
      // open horizontal half plane

      double determinant = (double) p_other.dir_x * dir_y - (double) p_other.dir_y * dir_x;
      
      return Signum.as_int(determinant);
      }

   @Override
   public PlaDirectionInt opposite()
      {
      return new PlaDirectionInt(-dir_x, -dir_y);
      }

   @Override
   public PlaDirectionInt turn_45_degree(int p_factor)
      {
      int n = p_factor % 8;
      int new_x;
      int new_y;
      switch (n)
         {
         case 0: // 0 degree
            new_x = dir_x;
            new_y = dir_y;
            break;
         case 1: // 45 degree
            new_x = dir_x - dir_y;
            new_y = dir_x + dir_y;
            break;
         case 2: // 90 degree
            new_x = -dir_y;
            new_y = dir_x;
            break;
         case 3: // 135 degree
            new_x = -dir_x - dir_y;
            new_y = dir_x - dir_y;
            break;
         case 4: // 180 degree
            new_x = -dir_x;
            new_y = -dir_y;
            break;
         case 5: // 225 degree
            new_x = dir_y - dir_x;
            new_y = -dir_x - dir_y;
            break;
         case 6: // 270 degree
            new_x = dir_y;
            new_y = -dir_x;
            break;
         case 7: // 315 degree
            new_x = dir_x + dir_y;
            new_y = dir_y - dir_x;
            break;
         default:
            new_x = 0;
            new_y = 0;
         }
      return new PlaDirectionInt(new_x, new_y);
      }

   /**
    * Implements the Comparable interface. 
    * Returns 
    * 1, if this direction has a strict bigger angle with the positive x-axis than p_other_direction, 
    * 0, if this direction is equal to p_other_direction, and -1 otherwise. 
    * Throws an exception, if p_other_direction is not a Direction.
    */
   @Override
   public int compareTo(PlaDirection p_other_direction)
      {
      return -p_other_direction.compareTo(this);
      }

   @Override
   int compareTo(PlaDirectionBigInt p_other)
      {
      return -(p_other.compareTo(this));
      }

   final double determinant(PlaDirectionInt p_other)
      {
      return (double) dir_x * p_other.dir_y - (double) dir_y * p_other.dir_x;
      }
   
   
   
   
   
   
   
   
   
   // the following function binaryGcd is copied from private parts of java.math
   // because we need it public.

   /*
    * trailingZeroTable[i] is the number of trailing zero bits in the binary representaion of i.
    */
   private static final byte trailingZeroTable[] = { -25, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1,
         0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0,
         1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4,
         0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
         4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0 };

   /**
    * Calculate GCD of a and b interpreted as unsigned integers.
    */
   private int binaryGcd(int a, int b)
      {
      if (b == 0)  return a;

      if (a == 0)  return b;

      int x;
      int aZeros = 0;
      while ((x = a & 0xff) == 0)
         {
         a >>>= 8;
         aZeros += 8;
         }
      int y = trailingZeroTable[x];
      aZeros += y;
      a >>>= y;

      int bZeros = 0;
      while ((x = b & 0xff) == 0)
         {
         b >>>= 8;
         bZeros += 8;
         }
      y = trailingZeroTable[x];
      bZeros += y;
      b >>>= y;

      int t = (aZeros < bZeros ? aZeros : bZeros);

      while (a != b)
         {
         if ((a + 0x80000000) > (b + 0x80000000))
            { // a > b as unsigned
            a -= b;

            while ((x = a & 0xff) == 0)
               a >>>= 8;
            a >>>= trailingZeroTable[x];
            }
         else
            {
            b -= a;

            while ((x = b & 0xff) == 0)
               b >>>= 8;
            b >>>= trailingZeroTable[x];
            }
         }
      return a << t;
      }
   
   
   
   
   
   }