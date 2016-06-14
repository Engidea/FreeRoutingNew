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
 * Created on 1. Februar 2003, 10:31
 */

package freert.planar;

import java.math.BigInteger;
import freert.varie.MathAux;

/**
 * Implementation of the abstract class Point as a tuple of int
 * @author Alfons Wirtz
 */

public class PlaPointInt extends PlaPoint implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
//   private static final String classname="PlaPointInt.";
   
   // Standard implementation of the zero point
   public static final PlaPointInt ZERO = new PlaPointInt(0, 0);

   public final int v_x;
   public final int v_y;

   private boolean is_nan;
   
   public PlaPointInt()
      {
      is_nan = true;
      v_x = PlaLimits.CRIT_INT;
      v_y = PlaLimits.CRIT_INT;
      }
   
   public PlaPointInt(int p_x, int p_y)
      {
      if (PlaLimits.is_critical (p_x))
         System.out.println("Warning in PlaPointInt(int): too big p_x="+p_x);

      if (PlaLimits.is_critical (p_y))
         System.out.println("Warning in PlaPointInt(int): too big p_y="+p_y);
      
      v_x = p_x;
      v_y = p_y;
      }

   public PlaPointInt(long p_x, long p_y)
      {
      if (PlaLimits.is_critical (p_x))
         System.out.println("Warning in PlaPointInt(long): too big p_x="+p_x);

      if (PlaLimits.is_critical (p_y))
         System.out.println("Warning in PlaPointInt(long): too big p_y="+p_y);
      
      v_x = (int)p_x;
      v_y = (int)p_y;
      }

   public PlaPointInt(double p_x, double p_y)
      {
      if (PlaLimits.is_critical (p_x))
         System.out.println("Warning in PlaPointInt(double): too big p_x="+p_x);

      if (PlaLimits.is_critical (p_y))
         System.out.println("Warning in PlaPointInt(double): too big p_y="+p_y);
      
      v_x = (int)p_x;
      v_y = (int)p_y;
      }
   
   @Override
   public final PlaPointInt round()
      {
      return this;
      }
   
   @Override 
   public final boolean is_rational()
      {
      return false;
      }
   
   public final boolean is_NaN ()
      {
      return is_nan;
      }

   @Override
   public final boolean equals(PlaPointInt p_ob)
      {
      if ( p_ob == null ) return false;
      
      if (this == p_ob) return true;
      
      return (v_x == p_ob.v_x && v_y == p_ob.v_y);
      }

   /**
    * This is actually correct, the points are surely not the same point
    */
   @Override
   public final boolean equals(PlaPointRational p_ob)
      {
      return false;
      }

   /**
    * This is a good candidate for optimization
    * @param p_1
    * @param p_2
    * @return
    */
   public final PlaSide side_of(PlaPointInt p_1, PlaPointInt p_2)
      {
      PlaVectorInt v1 = difference_by(p_1);
      PlaVectorInt v2 = p_2.difference_by(p_1);
      return v1.side_of(v2);
      }   
   

   /**
    * The function returns Side.ON_THE_LEFT, if this Point is on the left of the line from p_1 to p_2; 
    * Side.ON_THE_RIGHT, if this Point is on the right of the line from p_1 to p_2; 
    * Side.COLLINEAR, if this Point is collinear with p_1 and p_2.
    */
   @Override   
   public final PlaSide side_of(PlaPoint p_1, PlaPoint p_2)
      {
      // now, another way to calculate this, consider the line p_1 -> p_2 as reference and set origins on p_1
      PlaPointFloat pf_1 = p_1.to_float();
      PlaPointFloat pf_2 = p_2.to_float();
      
      // center the point to pf_1, and this becomes a "direction"
      PlaPointFloat point_dir = new PlaPointFloat(v_x - pf_1.v_x, v_y - pf_1.v_y);
      
      // center the second point to pf_1, and this becomes a "direction" of the line
      PlaPointFloat line_dir = new PlaPointFloat(pf_2.v_x - pf_1.v_x, pf_2.v_y - pf_1.v_y);
      
      double determinant = MathAux.determinant(point_dir, line_dir);
      
      PlaSide b_risul = PlaSide.get_side_of(determinant);

      return b_risul;
      }
   
   
   
   
   @Override
   public boolean is_contained_in(ShapeTileBox p_box)
      {
      return v_x >= p_box.box_ll.v_x && v_y >= p_box.box_ll.v_y && v_x <= p_box.box_ur.v_x && v_y <= p_box.box_ur.v_y;
      }

   @Override
   public PlaPointInt translate_by(PlaVectorInt p_vector)
      {
      return new PlaPointInt(v_x + p_vector.v_x, v_y + p_vector.v_y);
      }

   public final PlaPointInt translate_by(PlaDirection p_dir)
      {
      PlaVectorInt move = p_dir.to_vector();
      
      return new PlaPointInt(v_x + move.v_x, v_y + move.v_y);
      }

   /**
    * Turns this point by p_factor times 90 degree around p_pole.
    */
   public final PlaPointInt rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      PlaVectorInt v = difference_by(p_pole);
      v = v.rotate_90_deg(p_factor);
      return p_pole.translate_by(v);
      }
   
   /**
    * Mirrors this point at the vertical line through p_pole.
    */
   public final PlaPointInt mirror_vertical(PlaPointInt p_pole)
      {
      PlaVectorInt v = difference_by(p_pole);
      v = v.mirror_at_y_axis();
      return p_pole.translate_by(v);
      }
   
   /**
    * Mirrors this point at the horizontal line through p_pole
    */
   public final PlaPointInt mirror_horizontal(PlaPointInt p_pole)
      {
      PlaVectorInt v = difference_by(p_pole);
      v = v.mirror_at_x_axis();
      return p_pole.translate_by(v);
      }
   

   public final PlaVectorInt difference_by(PlaPointInt p_other)
      {
      return new PlaVectorInt(v_x - p_other.v_x, v_y - p_other.v_y);
      }

   public final PlaVectorInt to_vector()
      {
      return new PlaVectorInt(v_x, v_y);
      }

   @Override
   public PlaSide side_of(PlaLineInt p_line)
      {
      PlaVectorInt v1 = difference_by(p_line.point_a);
      
      PlaVectorInt v2 = p_line.point_b.difference_by(p_line.point_a);

      return v1.side_of(v2);
      }

   @Override
   public PlaPointFloat to_float()
      {
      return new PlaPointFloat(v_x, v_y);
      }

   /**
    * returns the determinant of the vectors (x, y) and (p_other.x, p_other.y)
    */
   public final long determinant(PlaPointInt p_other)
      {
      return (long) v_x * p_other.v_y - (long) v_y * p_other.v_x;
      }

   @Override
   public PlaPoint perpendicular_projection(PlaLineInt p_line)
      {
      PlaVectorInt v  = p_line.point_b.difference_by(p_line.point_a);
      BigInteger vxvx = BigInteger.valueOf((long) v.v_x * v.v_x);
      BigInteger vyvy = BigInteger.valueOf((long) v.v_y * v.v_y);
      BigInteger vxvy = BigInteger.valueOf((long) v.v_x * v.v_y);
      BigInteger denominator = vxvx.add(vyvy);
      
      BigInteger det = BigInteger.valueOf(p_line.point_a.determinant(p_line.point_b));
      BigInteger point_x = BigInteger.valueOf(v_x);
      BigInteger point_y = BigInteger.valueOf(v_y);

      BigInteger tmp1 = vxvx.multiply(point_x);
      BigInteger tmp2 = vxvy.multiply(point_y);
      tmp1 = tmp1.add(tmp2);
      tmp2 = det.multiply(BigInteger.valueOf(v.v_y));
      BigInteger proj_x = tmp1.add(tmp2);

      tmp1 = vxvy.multiply(point_x);
      tmp2 = vyvy.multiply(point_y);
      tmp1 = tmp1.add(tmp2);
      tmp2 = det.multiply(BigInteger.valueOf(v.v_x));
      BigInteger proj_y = tmp1.subtract(tmp2);

      int d_signum = denominator.signum();

      if (d_signum == 0)
         {
         new IllegalArgumentException("Should NEVER happen").printStackTrace();
         return new PlaPointRational(proj_x, proj_y, denominator);
         }
         
      if (d_signum < 0)
         {
         denominator = denominator.negate();
         proj_x = proj_x.negate();
         proj_y = proj_y.negate();
         }

      if ((proj_x.mod(denominator)).signum() == 0 && (proj_y.mod(denominator)).signum() == 0)
         {
         proj_x = proj_x.divide(denominator);
         proj_y = proj_y.divide(denominator);
         return new PlaPointInt(proj_x.intValue(), proj_y.intValue());
         }
      
      return new PlaPointRational(proj_x, proj_y, denominator);
      }

   /**
    * Returns the signed area of the parallelogramm spanned by the vectors p_2 - p_1 and this - p_1
    */
   public double signed_area(PlaPointInt p_1, PlaPointInt p_2)
      {
      PlaVectorInt d21 = p_2.difference_by(p_1);
      PlaVectorInt d01 = difference_by(p_1);
      return d21.determinant(d01);
      }

   /**
    * calculates the square of the distance between this point and p_to_point
    */
   public double distance_square(PlaPointInt p_to_point)
      {
      double dx = p_to_point.v_x - v_x;
      double dy = p_to_point.v_y - v_y;
      
      return dx * dx + dy * dy;
      }

   /**
    * calculates the distance between this point and p_to_point
    */
   public double distance(PlaPointInt p_to_point)
      {
      return Math.sqrt(distance_square(p_to_point));
      }

   /**
    * Calculates the nearest point to this point on the horizontal or vertical line through p_other (Snaps this point to on
    * ortogonal line through p_other).
    */
   public PlaPointInt orthogonal_projection(PlaPointInt p_other)
      {
      PlaPointInt result;
      int horizontal_distance = Math.abs(this.v_x - p_other.v_x);
      int vertical_distance = Math.abs(this.v_y - p_other.v_y);
      if (horizontal_distance <= vertical_distance)
         {
         // projection onto the vertical line through p_other
         result = new PlaPointInt(p_other.v_x, this.v_y);
         }
      else
         {
         // projection onto the horizontal line through p_other
         result = new PlaPointInt(this.v_x, p_other.v_y);
         }
      return result;
      }

   /**
    * Calculates the nearest point to this point on an orthogonal or diagonal line through p_other (Snaps this point to on 45 degree
    * line through p_other).
    */
   public PlaPointInt fortyfive_degree_projection(PlaPointInt p_other)
      {
      int dx = this.v_x - p_other.v_x;
      int dy = this.v_y - p_other.v_y;
      double[] dist_arr = new double[4];
      dist_arr[0] = Math.abs(dx);
      dist_arr[1] = Math.abs(dy);
      double diagonal_1 = ((double) dy - (double) dx) / 2;
      double diagonal_2 = ((double) dy + (double) dx) / 2;
      dist_arr[2] = Math.abs(diagonal_1);
      dist_arr[3] = Math.abs(diagonal_2);
      double min_dist = dist_arr[0];
      for (int i = 1; i < 4; ++i)
         {
         if (dist_arr[i] < min_dist)
            {
            min_dist = dist_arr[i];
            }
         }
      PlaPointInt result;
      if (min_dist == dist_arr[0])
         {
         // projection onto the vertical line through p_other
         result = new PlaPointInt(p_other.v_x, this.v_y);
         }
      else if (min_dist == dist_arr[1])
         {
         // projection onto the horizontal line through p_other
         result = new PlaPointInt(this.v_x, p_other.v_y);
         }
      else if (min_dist == dist_arr[2])
         {
         // projection onto the right diagonal line through p_other
         int diagonal_value = (int) diagonal_2;
         result = new PlaPointInt(p_other.v_x + diagonal_value, p_other.v_y + diagonal_value);
         }
      else
         {
         // projection onto the left diagonal line through p_other
         int diagonal_value = (int) diagonal_1;
         result = new PlaPointInt(p_other.v_x - diagonal_value, p_other.v_y + diagonal_value);
         }
      return result;
      }

   /**
    * Calculates a corner point p so that the lines through this point and p and from p to p_to_point are multiples of 45 degree,
    * and that the angle at p will be 45 degree. If p_left_turn, p_to_point will be on the left of the line from this point to p,
    * else on the right. Returns null, if the line from this point to p_to_point is already a multiple of 45 degree.
    */
   public PlaPointInt fortyfive_degree_corner(PlaPointInt p_to_point, boolean p_left_turn)
      {
      int dx = p_to_point.v_x - this.v_x;
      int dy = p_to_point.v_y - this.v_y;
      PlaPointInt result;

      // handle the 8 sections between the 45 degree lines

      if (dy > 0 && dy < dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(p_to_point.v_x - dy, this.v_y);
            }
         else
            {
            result = new PlaPointInt(this.v_x + dy, p_to_point.v_y);
            }
         }
      else if (dx > 0 && dy > dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(p_to_point.v_x, this.v_y + dx);
            }
         else
            {
            result = new PlaPointInt(this.v_x, p_to_point.v_y - dx);
            }
         }
      else if (dx < 0 && dy > -dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(this.v_x, p_to_point.v_y + dx);
            }
         else
            {
            result = new PlaPointInt(p_to_point.v_x, this.v_y - dx);
            }
         }
      else if (dy > 0 && dy < -dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(this.v_x - dy, p_to_point.v_y);
            }
         else
            {
            result = new PlaPointInt(p_to_point.v_x + dy, this.v_y);
            }
         }
      else if (dy < 0 && dy > dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(p_to_point.v_x - dy, this.v_y);
            }
         else
            {
            result = new PlaPointInt(this.v_x + dy, p_to_point.v_y);
            }
         }
      else if (dx < 0 && dy < dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(p_to_point.v_x, this.v_y + dx);
            }
         else
            {
            result = new PlaPointInt(this.v_x, p_to_point.v_y - dx);
            }
         }
      else if (dx > 0 && dy < -dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(this.v_x, p_to_point.v_y + dx);
            }
         else
            {
            result = new PlaPointInt(p_to_point.v_x, this.v_y - dx);
            }
         }
      else if (dy < 0 && dy > -dx)
         {
         if (p_left_turn)
            {
            result = new PlaPointInt(this.v_x - dy, p_to_point.v_y);
            }
         else
            {
            result = new PlaPointInt(p_to_point.v_x + dy, this.v_y);
            }
         }
      else
         {
         // the line from this point to p_to_point is already a multiple of 45 degree
         result = null;
         }
      return result;
      }


   @Override
   public int compare_x_y(PlaPointRational p_other)
      {
      BigInteger my_x_tmp = p_other.rp_z.multiply(BigInteger.valueOf(v_x));
      int risul = my_x_tmp.compareTo(p_other.rp_x);
      
      if ( risul != 0 ) return risul;
      
      BigInteger my_y_tmp = p_other.rp_z.multiply(BigInteger.valueOf(v_y));
      
      return my_y_tmp.compareTo(p_other.rp_y);
      }

   @Override
   public  int compare_x_y(PlaPointInt p_other)
      {
      int risul = v_x - p_other.v_x;
      
      if ( risul != 0 ) return risul;
      
      return v_y - p_other.v_y;
      }
   
   @Override 
   public final String toString ()
      {
      StringBuilder risul = new StringBuilder(100);
      risul.append("("+v_x);
      risul.append(","+v_y);
      risul.append(')');
      return risul.toString();
      }
   
   /**
    * Calculates the perpendicular direction froma this point to p_line. Returns Direction.NULL, if this point lies on p_line.
    */
   public final PlaDirection perpendicular_direction(PlaLineInt p_line)
      {
      PlaSide side = side_of(p_line);
      
      if (side == PlaSide.COLLINEAR) return PlaDirection.NULL;
      
      if (side == PlaSide.ON_THE_RIGHT)
         return p_line.direction().rotate_45_deg(2);
      else
         return p_line.direction().rotate_45_deg(6);
      }
   
   
   }
