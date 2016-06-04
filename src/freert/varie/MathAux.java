/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

package freert.varie;

import java.math.BigInteger;
import freert.planar.PlaPointFloat;

/**
 *
 * Auxiliary functions with BigInteger Parameters
 * 
 * @author Alfons Wirtz
 */

public final class MathAux
   {
   private MathAux() // disallow instantiation
      {
      }

   
   public static long determinant ( long a_x, long a_y, int b_x, int b_y  )
      {
      return a_x * b_y - a_y * b_x;
      }
   
   public static double determinant ( double a_x, double a_y, double b_x, double b_y  )
      {
      return a_x * b_y - a_y * b_x;
      }

   /**
    * You can use this one to decide if two "directions" are colinear or on the right or left
    * @param p_a
    * @param p_b
    * @return
    */
    public static final double determinant (PlaPointFloat p_a, PlaPointFloat p_b )
       {
       return p_a.v_x * p_b.v_y - p_a.v_y * p_b.v_x;
       }
   
   
   /**
    * calculates the determinant of the vectors (p_x_1, p_y_1) and (p_x_2, p_y_2)
    */
   public static final BigInteger determinant(BigInteger p_x_1, BigInteger p_y_1, BigInteger p_x_2, BigInteger p_y_2)
      {
      BigInteger tmp1 = p_x_1.multiply(p_y_2);
      BigInteger tmp2 = p_x_2.multiply(p_y_1);
      return tmp1.subtract(tmp2);
      }

   /**
    * auxiliary function to implement addition and translation in the classes RationalVector and RationalPoint
    */
   public static final BigInteger[] add_rational_coordinates(BigInteger[] p_first, BigInteger[] p_second)
      {
      BigInteger[] result = new BigInteger[3];
      if (p_first[2].equals(p_second[2]))
      // both rational numbers have the same denominator
         {
         result[2] = p_first[2];
         result[0] = p_first[0].add(p_second[0]);
         result[1] = p_first[1].add(p_second[1]);
         }
      else
         // multiply both denominators for the new denominator
         // to be on the save side:
         // taking the leat common multiple whould be optimal
         {
         result[2] = p_first[2].multiply(p_second[2]);
         BigInteger tmp_1 = p_first[0].multiply(p_second[2]);
         BigInteger tmp_2 = p_second[0].multiply(p_first[2]);
         result[0] = tmp_1.add(tmp_2);
         tmp_1 = p_first[1].multiply(p_second[2]);
         tmp_2 = p_second[1].multiply(p_first[2]);
         result[1] = tmp_1.add(tmp_2);
         }
      return result;
      }


   }