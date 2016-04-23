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
 * Created on 3. Februar 2003, 08:17
 */

package freert.planar;

import java.math.BigInteger;
import datastructures.Signum;

/**
 * Implements an abstract class Direction using long
 * By using longs I really wish to nail down this miplementation, the possible error using longs is really small
 * Direction is supposed to indicate an angle, a long is 2 to 64 bits, really small
 * @author Alfons Wirtz
 */

public final class PlaDirectionLong extends PlaDirection implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final long dir_x;
   public final long dir_y;
   

   PlaDirectionLong(long p_x, long p_y)
      {
      dir_x = p_x;
      dir_y = p_y;
      }

   /**
    * Construct a Direction from an IntVector
    * @param p_vector
    */
   public PlaDirectionLong(PlaVectorInt p_vector)
      {
      long a_x = p_vector.point_x;
      long a_y = p_vector.point_y;

      // need to "reduce" the points if necessary
      BigInteger b1 = BigInteger.valueOf(a_x);
      BigInteger b2 = BigInteger.valueOf(a_y);
      BigInteger gcd = b1.gcd(b2);

      long gcdlong = gcd.longValue();      
      
      if (gcdlong > 1)
         {
         a_x /= gcdlong;
         a_y /= gcdlong;
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
   int compareTo(PlaDirectionLong p_other)
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
   public PlaDirectionLong opposite()
      {
      return new PlaDirectionLong(-dir_x, -dir_y);
      }

   @Override
   public PlaDirectionLong turn_45_degree(int p_factor)
      {
      int n = p_factor % 8;
      long new_x;
      long new_y;
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
      return new PlaDirectionLong(new_x, new_y);
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

   
   final double determinant(PlaDirectionLong p_other)
      {
      return (double) dir_x * p_other.dir_y - (double) dir_y * p_other.dir_x;
      }

   }