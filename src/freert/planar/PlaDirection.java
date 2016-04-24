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
 * Direction.java
 *
 * Created on 3. Februar 2003, 15:36
 */

package freert.planar;

import java.math.BigInteger;
import freert.varie.Signum;

/**
 *
 * Abstract class defining functionality of directions in the plane. 
 * A Direction is an equivalence class of vectors. 
 * Two vectors define the same object of class Direction, if they point into the same direction. 
 * We prefer using directions instead of angles because with angles the arithmetic calculations are in general not exact.
 * A direction is a Vector that cannot be further "shortened"
 * You can define arbitrarily precise "angles" by using bigger and bigger numbers that cannot be "reduced"
 * @author Alfons Wirtz
 */

public final class PlaDirection implements Comparable<PlaDirection>, java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;

   public static final PlaDirection NULL = new PlaDirection(0, 0);
   // the direction to the east
   public static final PlaDirection RIGHT = new PlaDirection(1, 0);
   // the direction to the northeast
   public static final PlaDirection RIGHT45 = new PlaDirection(1, 1);
   // the direction to the north
   public static final PlaDirection UP = new PlaDirection(0, 1);
   // the direction to the northwest
   public static final PlaDirection UP45 = new PlaDirection(-1, 1);
   // the direction to the west
   public static final PlaDirection LEFT = new PlaDirection(-1, 0);
   // the direction to the southwest
   public static final PlaDirection LEFT45 = new PlaDirection(-1, -1);
   // the direction to the south
   public static final PlaDirection DOWN = new PlaDirection(0, -1);
   // the direction to the southeast
   public static final PlaDirection DOWN45 = new PlaDirection(1, -1);

   public final long dir_x;
   public final long dir_y;
   

   /**
    * Use this one only if you know that the values are already gcd
    * @param p_x
    * @param p_y
    */
   PlaDirection(long p_x, long p_y)
      {
      dir_x = p_x;
      dir_y = p_y;
      }
   
   /**
    * Construct a Direction from an IntVector
    * One key point is to "reduce" the points using the gcd
    * @param p_vector
    */
   public PlaDirection(PlaVectorInt p_vector)
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
   
   
   /**
    * creates a Direction from the input Vector
    */
   public static final PlaDirection get_instance(PlaVector p_vector)
      {
      return p_vector.to_normalized_direction();
      }

   /**
    * creates a Direction from the input Vector
    */
   public static final PlaDirection get_instance(PlaVectorInt p_vector)
      {
      return new PlaDirection (p_vector);
      }

   /**
    * Calculates the direction from p_from to p_to. If p_from and p_to are equal, null is returned.
    */
   public static final PlaDirection get_instance(PlaPoint p_from, PlaPoint p_to)
      {
      if (p_from.equals(p_to)) return null;

      return get_instance(p_to.difference_by(p_from));
      }

   /**
    * Creates a Direction whose angle with the x-axis is nearly equal to p_angle
    */
   public static final PlaDirection get_instance_approx(double p_angle)
      {
      final double scale_factor = 10000;
      double x = Math.cos(p_angle) * scale_factor;
      double y = Math.sin(p_angle) * scale_factor;
      return get_instance(new PlaVectorInt(x, y));
      }

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
   
   /**
    * @return true, if the direction is orthogonal or diagonal
    */
   public final boolean is_multiple_of_45_degree()
      {
      return (is_orthogonal() || is_diagonal());
      }



 
  public boolean is_orthogonal()
      {
      return (dir_x == 0 || dir_y == 0);
      }


  public boolean is_diagonal()
     {
     return (Math.abs(dir_x) == Math.abs(dir_y));
     }

  /**
   * return any Vector pointing into this direction
   */
  public PlaVectorInt get_vector()
     {
     return new PlaVectorInt(dir_x, dir_y);
     }

  @Override
  public int compareTo(PlaDirection p_other)
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

  /**
   * returns the opposite direction of this direction
   */
  public PlaDirection opposite()
     {
     return new PlaDirection(-dir_x, -dir_y);
     }

   /**
    * turns the direction by p_factor times 45 degree
    */
  public PlaDirection turn_45_degree(int p_factor)
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
     return new PlaDirection(new_x, new_y);
     }


  
  final double determinant(PlaDirection p_other)
     {
     return (double) dir_x * p_other.dir_y - (double) dir_y * p_other.dir_x;
     }



   
   /**
    * @return true, if p_ob is a Direction and this Direction and p_ob point into the same direction
    */
   public final boolean equals(PlaDirection p_other)
      {
      if (p_other == null) return false;

      if (this == p_other) return true;

      if ( side_of(p_other) != PlaSide.COLLINEAR) return false;
        
      // check, that dir and other_dir do not point into opposite directions
      PlaVector this_vector = get_vector();
      PlaVector other_vector = p_other.get_vector();
      return this_vector.projection(other_vector) == Signum.POSITIVE;
      }

   /**
    * Let L be the line from the Zero Vector to p_other.get_vector(). 
    * The function returns Side.ON_THE_LEFT, if this.get_vector() is on the left of L 
    * Side.ON_THE_RIGHT, if this.get_vector() is on the right of L 
    * Side.COLLINEAR, if this.get_vector() is collinear with L.
    */
   public final PlaSide side_of(PlaDirection p_other)
      {
      return get_vector().side_of(p_other.get_vector());
      }

   /**
    * The function returns Signum.POSITIVE, if the scalar product of of a vector representing this direction and a vector
    * representing p_other is > 0, Signum.NEGATIVE, if the scalar product is < 0, and Signum.ZERO, if the scalar product is equal 0.
    */
   public final Signum projection(PlaDirection p_other)
      {
      return get_vector().projection(p_other.get_vector());
      }

   /**
    * calculates an approximation of the direction in the middle of this direction and p_other
    */
   public final PlaDirection middle_approx(PlaDirection p_other)
      {
      PlaPointFloat v1 = get_vector().to_float();
      PlaPointFloat v2 = p_other.get_vector().to_float();
      double length1 = v1.distance();
      double length2 = v2.distance();
      double x = v1.point_x / length1 + v2.point_x / length2;
      double y = v1.point_y / length1 + v2.point_y / length2;
      final double scale_factor = 1000;
      
      PlaVectorInt vm = new PlaVectorInt(x * scale_factor, y * scale_factor);

      return PlaDirection.get_instance(vm);
      }

   
   /**
    * Returns 1, if the angle between p_1 and this direction is bigger the angle between p_2 and this direction, 0, if p_1 is equal
    * to p_2, * and -1 otherwise.
    */
   public int compare_from(PlaDirection p_1, PlaDirection p_2)
      {
      int result;
      if (p_1.compareTo(this) >= 0)
         {
         if (p_2.compareTo(this) >= 0)
            {
            result = p_1.compareTo(p_2);
            }
         else
            {
            result = -1;
            }
         }
      else
         {
         if (p_2.compareTo(this) >= 0)
            {
            result = 1;
            }
         else
            {
            result = p_1.compareTo(p_2);
            }
         }
      return result;
      }

   /**
    * Returns an approximation of the signed angle corresponding to this dierection.
    */
   public double angle_approx()
      {
      return get_vector().angle_approx();
      }


   }