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
 * Created on 3. Februar 2003, 15:36
 */

package freert.planar;

import java.math.BigInteger;
import freert.varie.Signum;

/**
 * A Direction is an equivalence class of vectors. 
 * Two vectors define the same object of class Direction, if they point into the same direction. 
 * We prefer using directions instead of angles because with angles the arithmetic calculations are in general not exact.
 * A direction is a Vector that cannot be further "shortened"
 * You can define arbitrarily precise "angles" by using bigger and bigger numbers that cannot be "reduced"
 * Now, it also happens that this could be the basis for the "m" coefficient of a line, the idea being that by having
 * the y and x part you end up handling the situations where "m" would go to infinity in a nice way
 * So, I am adding "m" support with the aim to get rid, sooner or later of the rationals... 
 * @author Alfons Wirtz
 * @author Damiano Bolla 2016
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

   public final long dir_y;   // this is normally the top part of the m coefficient
   public final long dir_x;   // this is the bottom part
   
   public final boolean is_vertical;
   public final boolean is_horizontal;
   
   private boolean is_NaN;
   
   public PlaDirection()
      {
      is_NaN = true;
      dir_x = 0;
      dir_y = 0;
      is_vertical = true;
      is_horizontal = true;
      }
   
   
   /**
    * Use this one only if you know that the values are already gcd
    */
   private PlaDirection(long p_x, long p_y)
      {
      dir_x = p_x;
      dir_y = p_y;
      is_vertical   = dir_x == 0;
      is_horizontal = dir_y == 0;
      }

   /**
    * Used whan you ahve a rational vector
    * @param dx
    * @param dy
    */
   PlaDirection (BigInteger dx, BigInteger dy )
      {
      BigInteger gcd = dx.gcd(dy);
      
      if ( gcd.signum() != 0 )
         {
         dx = dx.divide(gcd);
         dy = dy.divide(gcd);
         }
      
      BigInteger two = BigInteger.valueOf(2);
      
      while ( PlaLimits.is_critical(dx.longValue()) || PlaLimits.is_critical(dy.longValue()) )
         {
         // this really, should never happen, but if it does I just reduce accuracy until things fits
         System.err.println("PlaDirection: INteger REDUCING accuracy");
         dx = dx.divide(two);
         dy = dx.divide(two);
         }
   
      dir_x = dx.intValue();
      dir_y = dy.intValue();

      is_vertical   = dir_x == 0;
      is_horizontal = dir_y == 0;
      }
   
   /**
    * The two points define a "line" and I want a direction of that
    * To get it, you simply "move p_b so it is actually centered at zero with reference p_a
    * The resulting value should be "reduced"
\   */
   public PlaDirection(PlaPointInt p_a, PlaPointInt p_b)
      {
      this ( BigInteger.valueOf(p_b.v_x - p_a.v_x),BigInteger.valueOf(p_b.v_y - p_a.v_y));
      }
   
   
   /**
    * Construct a Direction from an IntVector
    * One key point is to "reduce" the points using the gcd
    * @param p_vector
    */
   public PlaDirection(PlaVectorInt p_vector)
      {
      this ( BigInteger.valueOf(p_vector.point_x),BigInteger.valueOf(p_vector.point_y));
      }
   
   /**
    * Creates a Direction whose angle with the x-axis is nearly equal to p_angle
    */
   public PlaDirection (double p_angle)
      {
      this(vector_from_angle(p_angle));
      }
   
   private static PlaVectorInt vector_from_angle (double p_angle )
      {
      double scale_factor = 10000;
      
      double x = Math.cos(p_angle) * scale_factor;
      double y = Math.sin(p_angle) * scale_factor;
      
      return new PlaVectorInt(x, y);
      }

   /**
    * Calculates the direction from p_from to p_to. 
    * If p_from and p_to are equal, null is returned.
    */
   public static final PlaDirection get_instance(PlaPointInt p_from, PlaPointInt p_to)
      {
      if (p_from.equals(p_to)) return null;

      PlaVectorInt p_vector = p_to.difference_by(p_from);
      
      return p_vector.to_direction();
      }

   
   @Override
   public final boolean is_NaN ()
      {
      return is_NaN;
      }
   
   /**
    * @return true, if the direction is orthogonal or diagonal
    */
   public final boolean is_multiple_of_45_degree()
      {
      return is_orthogonal() || is_diagonal();
      }



 
  public boolean is_orthogonal()
      {
      return dir_x == 0 || dir_y == 0;
      }


  public boolean is_diagonal()
     {
     return is_diagonal_right() || is_diagonal_left();
     }

  /**
   * The line is 45 degrees diagonal on the right 
   * @return
   */
  public boolean is_diagonal_right()
     {
     return dir_x == dir_y;
     }

  /**
   * The line is 45 degrees diagonal on the right 
   * @return
   */
  public boolean is_diagonal_left()
     {
     return dir_x == -dir_y;
     }

  
  /**
   * Return a new PlaDirection that is the sum of this direction plus the other one
   * @param p_oter
   * @return
   */
  public PlaDirection add ( PlaDirection p_other )
     {
     long new_x = dir_x + p_other.dir_x;
     long new_y = dir_y + p_other.dir_y;
     
     return new PlaDirection(BigInteger.valueOf(new_x),BigInteger.valueOf(new_y));
     }
  
  /**
   * Used while trying to calculate intersection of thwo lines
   * @param p_other
   * @return
   */
  public PlaDirection subtract ( PlaDirection p_other )
     {
     if ( dir_x == p_other.dir_x )
        {
        // if the denominator is the same, then it is just a difference
        return new PlaDirection(dir_y - p_other.dir_y, dir_x);
        }
     
     
     long denom = dir_x * p_other.dir_x;
     
     long my_y    = dir_y * p_other.dir_x;
     long other_y = p_other.dir_y * dir_x;

     long numerator = my_y - other_y;
     
     return new PlaDirection(BigInteger.valueOf(denom),BigInteger.valueOf(numerator) );
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

     // now this direction and p_other are located in the same open horizontal half plane

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
     long new_x;
     long new_y;

     switch (p_factor % 8)
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


  /**
   * Consider the two direction vectors written as 
   *  Xa Xb
   *  Ya Yb
   * The determinant is zero if they are "colinear" or > 0 if on right or < 0 if on left
   * There is some chance of overflow here, small one, in theory
   * TODO I should check that dir_x " dir_Y are never > 32 bits                          
   * @param p_other
   * @return
   */
  final long determinant(PlaDirection p_other)
     {
     return dir_x * p_other.dir_y - dir_y * p_other.dir_x;
     }


  private final double determinant(PlaPointFloat p_other)
     {
     return dir_x * p_other.v_y - dir_y * p_other.v_x;
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
    * @return true, if p_ob is a Direction and this Direction and p_ob point into the same direction
    */
   public final boolean equals(PlaDirection p_other)
      {
      if (p_other == null) return false;

      if (this == p_other) return true;

      if ( side_of(p_other) != PlaSide.COLLINEAR) return false;
        
      // check, that dir and other_dir do not point into opposite directions

      return projection(p_other) == Signum.POSITIVE;
      }

   /**
    * The function returns 
    * Side.ON_THE_LEFT, if this.get_vector() is on the left of L 
    * Side.ON_THE_RIGHT, if this.get_vector() is on the right of L 
    * Side.COLLINEAR, if this.get_vector() is collinear with L.
    */
   public final PlaSide side_of(PlaDirection p_other)
      {
      return PlaSide.get_side_of(determinant(p_other));
      }

   /**
    * Important: p_other represent a "direction" in a floating point mode
    * @param p_other
    * @param p_tolerance
    * @return
    */
   public PlaSide side_of(PlaPointFloat p_other, double p_tolerance)
      {
      return PlaSide.get_side_of(determinant(p_other),p_tolerance);
      }
   
   
   /**
    * Projection is defined as finding the component of one vector in the direction of another
    * So the projection of b onto a can be found by taking the scalar product of b and a unit vector
    * in the direction of a, i.e. l = b · a 
    * It happens that a Direction is a unit vector, by definition, so, the projection of b into a is just the scalar product
    * of this direction by the other direction
    * @param p_other
    * @return
    */
   final double projection_value(PlaDirection p_other)
      {
      return (double) dir_x * p_other.dir_x + (double) dir_y * p_other.dir_y;
      }
   
   /**
    * The function returns Signum.POSITIVE, if the scalar product of of a vector representing this direction and a vector
    * representing p_other is > 0, 
    * Signum.NEGATIVE, if the scalar product is < 0, and 
    * Signum.ZERO, if the scalar product is equal 0.
    */
   public final Signum projection(PlaDirection p_other)
      {
      return Signum.of(projection_value(p_other));
      }

   /**
    * calculates an approximation of the direction in the middle of this direction and p_other
    * Really, this could now be more accurate, right ?
    */
   public final PlaDirection middle_approx(PlaDirection p_other)
      {
      PlaPointFloat v1 = to_float();
      PlaPointFloat v2 = p_other.to_float();
      
      double length1 = v1.distance();
      double length2 = v2.distance();
      
      double x = v1.v_x / length1 + v2.v_x / length2;
      
      double y = v1.v_y / length1 + v2.v_y / length2;
      
      final double scale_factor = 1000;
      
      PlaVectorInt vm = new PlaVectorInt(x * scale_factor, y * scale_factor);

      return new PlaDirection(vm);
      }

   public final PlaPointFloat to_float ()
      {
      return new PlaPointFloat(dir_x, dir_y);
      }
   
   /**
    * Returns 1, if the angle between p_1 and this direction is bigger the angle between p_2 and this direction, 
    * 0, if p_1 is equal to p_2, 
    * -1 otherwise.
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
    * Returns an approximation of the signed angle corresponding to this dierection
    * Now, for compatibility reason, the angle logic returned is as follows
    * The top part (for y > 0 ) increases from 0 to PI 
    * Is always negative and foes from -0 to -PI
    */
   public double angle_approx()
      {
      if ( is_vertical )
         {
         if ( dir_y >= 0 ) 
            return Math.PI / 2;
         else 
            return -Math.PI / 2;
         }
      else if ( is_horizontal )
         {
         if ( dir_x >= 0 ) 
            return 0;
         else
            return Math.PI;
         }

      // dir_x is the cosine and dir_y is the sine
      double ratio = (double) dir_y / (double) dir_x;
      
      // atan goes from -PI/2 to PI/2 where it starts from 0 to PI/2 jumps to -PI/2 and goes on to close to -0
      double atan_rad = Math.atan(ratio);

      // Atan behaviour is already correct for the "right" side of the plane
      if ( dir_x >= 0 ) return atan_rad;

      if ( dir_y >= 0 )
         return Math.PI + atan_rad;
      else
         return atan_rad - Math.PI;
      }

   }