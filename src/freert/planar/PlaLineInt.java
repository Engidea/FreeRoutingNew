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
 */

package freert.planar;

import java.math.BigInteger;
import freert.varie.Signum;

/**
 * Implements functionality for lines in the plane.
 * Now, really, the difference between a line and a segment is "hairy"....
 *
 * @author Alfons Wirtz
 */

public final class PlaLineInt implements Comparable<PlaLineInt>, java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="PlaLineInt.";

   public final PlaPointInt point_a;
   public final PlaPointInt point_b;
   
   private final PlaDirection line_dir; 
   
   private transient int cf_q;
   
   boolean is_nan;
   
   /**
    * creates a kind of null line
    */
   public PlaLineInt()
      {
      is_nan   = true;
      point_a  = new PlaPointInt ();
      point_b  = new PlaPointInt ();
      line_dir = new PlaDirection();
      }

   public PlaLineInt(PlaPointInt p_a, PlaPointInt p_b)
      {
      point_a = p_a;
      point_b = p_b;
      
      line_dir = new PlaDirection (point_a,point_b);
      }

   public PlaLineInt(PlaPointInt p_a, PlaDirection p_dir)
      {
      point_a = p_a;
      
      point_b = point_a.translate_by(p_dir);
      
      line_dir = p_dir;
      }

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }

   /**
    * Experimental
    */
   private void calc_m_q ()
      {
      
//      cf_q = point_a.v_y - cf_m * point_a.v_x;
      }

   /**
    * returns true, if this and p_ob define the same line
    */
   public final boolean equals(Object p_ob)
      {
      if (p_ob == null)  return false;

      if (this == p_ob) return true;

      if (!(p_ob instanceof PlaLineInt)) return false;
      
      PlaLineInt other = (PlaLineInt) p_ob;
      
      // point_a should be on the same line
      if (side_of(other.point_a) != PlaSide.COLLINEAR) return false;
      
      // and the line should have the same direction
      return direction().equals(other.direction());
      }

   /**
    * Note that the direction is actually the m coefficient of the line in a plane
    * @return the direction of this directed line
    */
   public PlaDirection direction()
      {
      return line_dir;
      }

   /**
    * The function returns Side.ON_THE_LEFT, if this Line is on the left of p_point, 
    * Side.ON_THE_RIGHT, if this Line is on the right of p_point 
    * Side.COLLINEAR, if this Line contains p_point.
    */
   public final PlaSide side_of(PlaPoint p_point)
      {
      PlaSide result = p_point.side_of(this);
      
      return result.negate();
      }

   /**
    * @param p_point
    * @return
    */
   public final PlaSide side_of(PlaPointInt p_point)
      {
      PlaSide result = p_point.side_of(this);
      
      return result.negate();
      }
   
   
   /**
    * Returns Side.COLLINEAR, if p_point is on the line with tolerance p_tolerance. 
    * Why is this not using direction ? seems a good candidate, no ?
    * Side.ON_THE_LEFT, if this line is on the left of p_point, 
    * Side.ON_THE_RIGHT, if this line is on the right of p_point,
    * TODO try to use direction adn side_of with the same params
    * Occio che è la linea che è a destra o sinistra, NON il punto !!!
    */
   public PlaSide side_of(PlaPointFloat p_point, double p_tolerance)
      {
      // Let's center everything with point_a of this line, once origin changed the point direction is just its coordinates
      PlaPointFloat point_dir = new PlaPointFloat(p_point.v_x - point_a.v_x, p_point.v_y - point_a.v_y);
      
      return direction().side_of(point_dir, p_tolerance) ;
      }

   /**
    * returns Side.ON_THE_LEFT, if this line is on the left of p_point, Side.ON_THE_RIGHT, if this line is on the right of p_point,
    * Side.COLLINEAR otherwise.
    */
   public PlaSide side_of(PlaPointFloat p_point)
      {
      return side_of(p_point, 0);
      }

   /**
    * Returns Side.ON_THE_LEFT, if this line is on the left of the intersection of p_1 and p_2, 
    * Side.ON_THE_RIGHT, if this line is on the right of the intersection
    * Side.COLLINEAR, if all 3 lines intersect in exacly 1 point.
    */
   public PlaSide side_of_intersection(PlaLineInt p_1, PlaLineInt p_2)
      {
      PlaPointFloat intersection_approx = p_1.intersection_approx(p_2);
      
      if ( intersection_approx.is_NaN() )
         {
         System.err.println("side_of_intersection NAN, fix it");
         return PlaSide.ON_THE_LEFT;
         }
      
      PlaSide result = side_of(intersection_approx, 1.0);

      if (result == PlaSide.COLLINEAR)
         {
         // Previous calculation was with FloatPoints and a tolerance for performance reasons. 
         // Make an exact check for collinearity now with class Point instead of FloatPoint.
         PlaPoint intersection = p_1.intersection(p_2, "this should never happen");
         
         return side_of(intersection);
         }
      
      return result;
      }

   /**
    * Looks, if all interiour points of p_tile are on the right side of this line.
    */
   public boolean is_on_the_left(ShapeTile p_tile)
      {
      for (int index = 0; index < p_tile.border_line_count(); ++index)
         {
         if (side_of(p_tile.corner(index)) == PlaSide.ON_THE_RIGHT)
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Looks, if all interiour points of p_tile are on the left side of this line.
    */
   public boolean is_on_the_right(ShapeTile p_tile)
      {
      for (int index = 0; index < p_tile.border_line_count(); ++index)
         {
         if (side_of(p_tile.corner(index)) == PlaSide.ON_THE_LEFT)
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Returns the signed distance of this line from p_point. The result will be positive, if the line is on the left of p_point,
    * else negative.
    */
   public double signed_distance(PlaPointFloat p_point)
      {
      // only implemented for IntPoint lines for performance reasons
      PlaPointInt this_a = (PlaPointInt) point_a;
      PlaPointInt this_b = (PlaPointInt) point_b;
      double dx = this_b.v_x - this_a.v_x;
      double dy = this_b.v_y - this_a.v_y;
      double det = dy * (p_point.v_x - this_a.v_x) - dx * (p_point.v_y - this_a.v_y);
      // area of the parallelogramm spanned by the 3 points
      double length = Math.sqrt(dx * dx + dy * dy);
      return det / length;
      }

   /**
    * returns true, if the 2 lines defins the same set of points, but may have opposite directions
    */
   public boolean overlaps(PlaLineInt p_other)
      {
      return side_of(p_other.point_a) == PlaSide.COLLINEAR && side_of(p_other.point_b) == PlaSide.COLLINEAR;
      }

   /**
    * Returns the line defining the same set of points, but with opposite direction
    */
   public PlaLineInt opposite()
      {
      return new PlaLineInt(point_b, point_a);
      }

   /**
    * Returns the intersection point of the 2 lines. 
    * If the lines are parallel result.is_infinite() will be true.
    */
   public PlaPoint intersection(PlaLineInt p_other, String error_msg )
      {
      
      // Separate handling for orthogonal and 45 degree lines for better performance
      if ( line_dir.is_vertical ) // this line is vertical
         {
         if (p_other.line_dir.is_horizontal ) // other line is horizontal
            {
            return new PlaPointInt(point_a.v_x, p_other.point_a.v_y);
            }
         if (p_other.line_dir.is_diagonal_right()) // other line is right diagonal
            {
            int this_x = point_a.v_x;
            PlaPointInt other_a = p_other.point_a;
            return new PlaPointInt(this_x, other_a.v_y + this_x - other_a.v_x);
            }
         if (p_other.line_dir.is_diagonal_left()) // other line is left diagonal
            {
            int this_x = point_a.v_x;
            PlaPointInt other_a = p_other.point_a;
            return new PlaPointInt(this_x, other_a.v_y + other_a.v_x - this_x);
            }
         }
      else if (line_dir.is_horizontal ) // this line is horizontal
         {
         if (p_other.line_dir.is_vertical ) // other line is vertical
            {
            return new PlaPointInt(p_other.point_a.v_x, point_a.v_y);
            }
         if (p_other.line_dir.is_diagonal_right()) // other line is right diagonal
            {
            int this_y = point_a.v_y;
            PlaPointInt other_a = p_other.point_a;
            return new PlaPointInt(other_a.v_x + this_y - other_a.v_y, this_y);
            }
         if (p_other.line_dir.is_diagonal_left()) // other line is left diagonal
            {
            int this_y = point_a.v_y;
            PlaPointInt other_a = p_other.point_a;
            return new PlaPointInt(other_a.v_x + other_a.v_y - this_y, this_y);
            }
         }
      else if (line_dir.is_diagonal_right()) // this line is right diagonal
         {
         if (p_other.line_dir.is_vertical ) // other line is vertical
            {
            int other_x = p_other.point_a.v_x;
            PlaPointInt this_a = point_a;
            return new PlaPointInt(other_x, this_a.v_y + other_x - this_a.v_x);
            }
         if (p_other.line_dir.is_horizontal ) // other line is horizontal
            {
            int other_y = p_other.point_a.v_y;
            PlaPointInt this_a = point_a;
            return new PlaPointInt(this_a.v_x + other_y - this_a.v_y, other_y);
            }
         }
      else if (line_dir.is_diagonal_left()) // this line is left diagonal
         {
         if (p_other.line_dir.is_vertical ) // other line is vertical
            {
            int other_x = p_other.point_a.v_x;
            PlaPointInt this_a = point_a;
            return new PlaPointInt(other_x, this_a.v_y + this_a.v_x - other_x);
            }
         if (p_other.line_dir.is_horizontal ) // other line is horizontal
            {
            int other_y = p_other.point_a.v_y;
            PlaPointInt this_a = point_a;
            return new PlaPointInt(this_a.v_x + this_a.v_y - other_y, other_y);
            }
         }

      BigInteger det_1 = BigInteger.valueOf(point_a.determinant(point_b));
      BigInteger det_2 = BigInteger.valueOf( p_other.point_a.determinant( p_other.point_b));
      
      PlaVectorInt delta_1 = point_b.difference_by(point_a);
      PlaVectorInt delta_2 = p_other.point_b.difference_by(p_other.point_a);
      
      BigInteger det = BigInteger.valueOf(delta_2.determinant(delta_1));
      BigInteger tmp_1 = det_1.multiply(BigInteger.valueOf(delta_2.point_x));
      BigInteger tmp_2 = det_2.multiply(BigInteger.valueOf(delta_1.point_x));
      BigInteger is_x = tmp_1.subtract(tmp_2);
      tmp_1 = det_1.multiply(BigInteger.valueOf(delta_2.point_y));
      tmp_2 = det_2.multiply(BigInteger.valueOf(delta_1.point_y));
      BigInteger is_y = tmp_1.subtract(tmp_2);
      int signum = det.signum();
      
      if ( signum == 0 )
         {
         // this is the case when the denominator is zero
         
         if ( error_msg != null )
            new IllegalArgumentException(classname+"intersection NAN "+error_msg).printStackTrace();
         
         // this is instead a null rational !!
         return new PlaPointRational(is_x, is_y, det);
         }
      
      if (signum < 0)
         {
         // we wish the denominator to be alsays positive
         det  = det.negate();
         is_x = is_x.negate();
         is_y = is_y.negate();
         }
      
      if ((is_x.mod(det)).signum() == 0 && (is_y.mod(det)).signum() == 0)
         {
         // this means that the result is actually an int point
         is_x = is_x.divide(det);
         is_y = is_y.divide(det);
         // now, if the values are out of range they should be handled as NaN number
         return new PlaPointInt(is_x.longValue(), is_y.longValue());
         }
      
      // this is a standard rational
      return new PlaPointRational(is_x, is_y, det);
      }

   
/*   
   private PlaPointInt intersect ( PlaLineInt line_a, PlaLineInt line_b )
      {
      PlaDirection bottom = line_a.line_dir.subtract(line_b.line_dir);

      
      
      double top_x = line_b.cf_q - line_a.cf_q;
      
      double new_x = top_x / bottom;
      
      double top_y = line_a.cf_m*line_b.cf_q - line_b.cf_m*line_a.cf_q;
      
      double new_y = top_y / bottom;
      
      return new PlaPointInt ( new_x, new_y);
      }
     */ 
   
   /**
    * Returns an approximation of the intersection of the 2 lines by a FloatPoint. 
    * If the lines are parallel the result coordinates will be a NaN PointFloat 
    * Useful in situations where performance is more important than accuracy.
    */
   public final PlaPointFloat intersection_approx(PlaLineInt p_other)
      {
      // this function is at the moment only implemented for lines consisting of IntPoints.
      PlaPointInt other_a =  p_other.point_a;
      PlaPointInt other_b =  p_other.point_b;
      
      double d1x = point_b.v_x - point_a.v_x;
      double d1y = point_b.v_y - point_a.v_y;
      
      double d2x = other_b.v_x - other_a.v_x;
      double d2y = other_b.v_y - other_a.v_y;
      
      double det = d2x * d1y - d2y * d1x;
      
      // this would be an infinite distance since the lines are parallel
      if (det == 0) return new PlaPointFloat();

      double det_1 = (double) point_a.v_x  * point_b.v_y  - (double) point_a.v_y  * point_b.v_x;
      double det_2 = (double) other_a.v_x * other_b.v_y - (double) other_a.v_y * other_b.v_x;

      double is_x = (d2x * det_1 - d1x * det_2) / det;
      double is_y = (d2y * det_1 - d1y * det_2) / det;
      
      return new PlaPointFloat(is_x, is_y);
      }

   /**
    * returns the perpendicular projection of p_point onto this line
    */
   public PlaPoint perpendicular_projection(PlaPoint p_point)
      {
      return p_point.perpendicular_projection(this);
      }

   /**
    * translates the line perpendicular at about p_dist. 
    * If p_dist > 0, the line will be translated to the left, else to the right
    */
   public PlaLineInt translate(double p_dist)
      {
      // this function is at the moment only implemented for lines consisting of IntPoints.

      PlaPointFloat v = direction().to_float();
      
      double lenght = v.distance();
      
      PlaPointInt new_a;
      
      if ( v.v_x_square <= v.v_y_square)
         {
         // translate along the x axis
         int rel_x = (int) Math.round((p_dist * lenght) / v.v_y);
         new_a = new PlaPointInt(point_a.v_x - rel_x, point_a.v_y);
         }
      else
         {
         // translate along the y axis
         int rel_y = (int) Math.round((p_dist * lenght) / v.v_x);
         new_a = new PlaPointInt(point_a.v_x, point_a.v_y + rel_y);
         }
      
      return new PlaLineInt(new_a, direction());
      }

   /**
    * translates the line by p_vector
    */
   public PlaLineInt translate_by(PlaVectorInt p_vector)
      {
      if (p_vector.equals(PlaVectorInt.ZERO)) return this;

      PlaPointInt new_a = point_a.translate_by(p_vector);
      PlaPointInt new_b = point_b.translate_by(p_vector);
      
      return new PlaLineInt(new_a, new_b);
      }

   /**
    * returns true, if the line is axis_parallel
    */
   public boolean is_orthogonal()
      {
      return direction().is_orthogonal();
      }

   /**
    * returns true, if this line is diagonal
    */
   public boolean is_diagonal()
      {
      return direction().is_diagonal();
      }

   /**
    * returns true, if the direction of this line is a multiple of 45 degree
    */
   public boolean is_multiple_of_45_degree()
      {
      return direction().is_multiple_of_45_degree();
      }

   /**
    * checks, if this Line and p_other are parallel
    */
   public boolean is_parallel(PlaLineInt p_other)
      {
      return direction().side_of(p_other.direction()) == PlaSide.COLLINEAR;
      }

   /**
    * checks, if this Line and p_other are perpendicular
    */
   public boolean is_perpendicular(PlaLineInt p_other)
      {
      return direction().projection(p_other.direction()) == Signum.ZERO;
      }

   /**
    * returns true, if this and p_ob define the same line
    */
   public boolean is_equal_or_opposite(PlaLineInt p_other)
      {

      return (side_of(p_other.point_a) == PlaSide.COLLINEAR && side_of(p_other.point_b) == PlaSide.COLLINEAR);
      }

   /**
    * calculates the cosinus of the angle between this line and p_other
    */
   public double cos_angle(PlaLineInt p_other)
      {
      PlaVectorInt v1 = point_b.difference_by(point_a);
      PlaVectorInt v2 = p_other.point_b.difference_by(p_other.point_a);
      return v1.cos_angle(v2);
      }

   /**
    * A line l_1 is defined bigger than a line l_2, if the direction of l_1 is bigger than the direction of l_2. 
    * Implements the comparable interface. 
    * Throws a cast exception, if p_other is not a Line. 
    * Fast implementation only for lines consisting of IntPoints because of critical performance
    */
   public int compareTo(PlaLineInt p_other)
      {
      PlaPointInt other_a = p_other.point_a;
      PlaPointInt other_b = p_other.point_b;

      int dx1 = point_b.v_x - point_a.v_x;
      int dy1 = point_b.v_y - point_a.v_y;
      int dx2 = other_b.v_x - other_a.v_x;
      int dy2 = other_b.v_y - other_a.v_y;
      
      if (dy1 > 0)
         {
         if (dy2 < 0)
            {
            return -1;
            }
         if (dy2 == 0)
            {
            if (dx2 > 0)
               {
               return 1;
               }
            return -1;
            }
         }
      else if (dy1 < 0)
         {
         if (dy2 >= 0)
            {
            return 1;
            }
         }
      else
         // dy1 == 0
         {
         if (dx1 > 0)
            {
            if (dy2 != 0 || dx2 < 0)
               {
               return -1;
               }
            return 0;
            }
         // dx1 < 0
         if (dy2 > 0 || dy2 == 0 && dx2 > 0)
            {
            return 1;
            }
         if (dy2 < 0)
            {
            return -1;
            }
         return 0;
         }

      // now this direction and p_other are located in the same open horizontal half plane

      double determinant = (double) dx2 * dy1 - (double) dy2 * dx1;
      return Signum.as_int(determinant);
      }

   /**
    * Calculates an approximation of the function value of this line at p_x, if the line is not vertical.
    */
   public double function_value_approx(double p_x)
      {
      if ( line_dir.is_vertical )
         {
         System.out.println("function_value_approx: line is vertical");
         return 0;
         }
      
      PlaPointFloat p1 = point_a.to_float();
      PlaPointFloat p2 = point_b.to_float();

      double dx = p2.v_x - p1.v_x;

      double dy = p2.v_y - p1.v_y;
      double det = p1.v_x * p2.v_y - p2.v_x * p1.v_y;
      double result = (dy * p_x - det) / dx;
      return result;
      }

   /**
    * Calculates an approximation of the function value in y of this line at p_y, if the line is not horizontal.
    */
   public double function_in_y_value_approx(double p_y)
      {
      if ( line_dir.is_horizontal )
         {
         System.out.println("function_in_y_value_approx: line is horizontal");
         return 0;
         }

      PlaPointFloat p1 = point_a.to_float();
      PlaPointFloat p2 = point_b.to_float();
      double dy = p2.v_y - p1.v_y;

      double dx = p2.v_x - p1.v_x;
      double det = p1.v_x * p2.v_y - p2.v_x * p1.v_y;
      double result = (dx * p_y + det) / dy;
      return result;
      }

   /**
    * Calculates the direction from p_from_point to the nearest point on this line to p_fro_point. 
    * Returns null, if p_from_point is contained in this line.
    */
   public PlaDirection perpendicular_direction(PlaPointInt p_from_point)
      {
      PlaSide line_side = side_of(p_from_point);

      if (line_side == PlaSide.COLLINEAR) return null;

      PlaDirection dir1 = direction().turn_45_degree(2);
      PlaDirection dir2 = direction().turn_45_degree(6);

      PlaPoint check_point_1 = p_from_point.translate_by(dir1);

      if (side_of(check_point_1) != line_side) return dir1;

      PlaPoint check_point_2 = p_from_point.translate_by(dir2);
      
      if (side_of(check_point_2) != line_side) return dir2;

      PlaPointFloat nearest_line_point = p_from_point.to_float().projection_approx(this);
      
      if (nearest_line_point.length_square(check_point_1.to_float()) <= nearest_line_point.length_square(check_point_2.to_float()))
         return dir1;
      else
         return dir2;
      }

   /**
    * Turns this line by p_factor times 90 degree around p_pole.
    */
   public PlaLineInt turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      PlaPointInt new_a = point_a.turn_90_degree(p_factor, p_pole);
      PlaPointInt new_b = point_b.turn_90_degree(p_factor, p_pole);
      return new PlaLineInt(new_a, new_b);
      }

   /** Mirrors this line at the vertical line through p_pole */
   public PlaLineInt mirror_vertical(PlaPointInt p_pole)
      {
      PlaPointInt new_a = point_b.mirror_vertical(p_pole);
      PlaPointInt new_b = point_a.mirror_vertical(p_pole);
      return new PlaLineInt(new_a, new_b);
      }

   /** Mirrors this line at the horizontal line through p_pole */
   public PlaLineInt mirror_horizontal(PlaPointInt p_pole)
      {
      PlaPointInt new_a = point_b.mirror_horizontal(p_pole);
      PlaPointInt new_b = point_a.mirror_horizontal(p_pole);
      return new PlaLineInt(new_a, new_b);
      }

   }