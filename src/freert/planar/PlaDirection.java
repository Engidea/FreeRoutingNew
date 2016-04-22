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

import datastructures.Signum;

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

public abstract class PlaDirection implements Comparable<PlaDirection>, java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;

   public static final PlaDirectionInt NULL = new PlaDirectionInt(0, 0);
   // the direction to the east
   public static final PlaDirectionInt RIGHT = new PlaDirectionInt(1, 0);
   // the direction to the northeast
   public static final PlaDirectionInt RIGHT45 = new PlaDirectionInt(1, 1);
   // the direction to the north
   public static final PlaDirectionInt UP = new PlaDirectionInt(0, 1);
   // the direction to the northwest
   public static final PlaDirectionInt UP45 = new PlaDirectionInt(-1, 1);
   // the direction to the west
   public static final PlaDirectionInt LEFT = new PlaDirectionInt(-1, 0);
   // the direction to the southwest
   public static final PlaDirectionInt LEFT45 = new PlaDirectionInt(-1, -1);
   // the direction to the south
   public static final PlaDirectionInt DOWN = new PlaDirectionInt(0, -1);
   // the direction to the southeast
   public static final PlaDirectionInt DOWN45 = new PlaDirectionInt(1, -1);

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
   public static final PlaDirectionInt get_instance(PlaVectorInt p_vector)
      {
      return new PlaDirectionInt (p_vector);
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
   public static final PlaDirectionInt get_instance_approx(double p_angle)
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

   /**
    * return any Vector pointing into this direction
    */
   public abstract PlaVector get_vector();

   /**
    * returns true, if the direction is horizontal or vertical
    */
   public abstract boolean is_orthogonal();

   /**
    * returns true, if the direction is diagonal
    */
   public abstract boolean is_diagonal();


   /**
    * turns the direction by p_factor times 45 degree
    */
   public abstract PlaDirection turn_45_degree(int p_factor);

   /**
    * returns the opposite direction of this direction
    */
   public abstract PlaDirection opposite();

   // auxiliary functions needed because the virtual function mechanism does not work in parameter position

   abstract int compareTo(PlaDirectionInt p_other);

   abstract int compareTo(PlaDirectionBigInt p_other);
   
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
      return this.get_vector().projection(p_other.get_vector());
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