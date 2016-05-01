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
 * FloatPoint.java
 *
 * Created on 2. Februar 2003, 09:14
 */

package freert.planar;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 *
 * Implements a point in the plane as a touple of double's. Because arithmetic calculations with double's are in general not exact,
 * FloatPoint is not derived from the abstract class Point.
 *
 *
 * @author Alfons Wirtz
 */

public final class PlaPointFloat   /*extends PlaPoint*/   implements Serializable,PlaObject
   {
   private static final long serialVersionUID = 1L;
   
   public static final PlaPointFloat ZERO = new PlaPointFloat(0, 0);

   public  final double v_x;
   public  final double v_y;

   private final double dist_square;  // calculated on constructor
   
   private boolean is_nan = false;
   
   public PlaPointFloat()
      {
      is_nan = true;
      v_x = Integer.MAX_VALUE;   // this will retain most of the previous behavior
      v_y = Integer.MAX_VALUE;   // but I also have a clear indication that this is a NaN
      dist_square = 0;
      }
   
   public boolean is_NaN ()
      {
      return is_nan;
      }

   public PlaPointFloat(double p_x, double p_y)
      {
      v_x = p_x;
      v_y = p_y;
      
      dist_square = v_x * v_x + v_y * v_y;
      }

   public PlaPointFloat(PlaPointInt p_pt)
      {
      v_x = p_pt.v_x;
      v_y = p_pt.v_y;
      
      dist_square = v_x * v_x + v_y * v_y;
      }

   /**
    * Returns the distance from 0,0 to the point, squared
    * @return
    */
   public final double distance_square()
      {
      return dist_square;
      }

   /**
    * The "size" of the line from 0,0 to x,y 
    * @return
    */
   public final double distance()
      {
      return Math.sqrt(distance_square());
      }

   /**
    * returns the square of the distance from this Point to the Point p_other
    */
   public final double length_square(PlaPointFloat p_other)
      {
      double dx = p_other.v_x - v_x;
      double dy = p_other.v_y - v_y;
      
      return dx * dx + dy * dy;
      }

   /**
    * returns the distance from this point to the point p_other
    */
   public final double distance(PlaPointFloat p_other)
      {
      return Math.sqrt(length_square(p_other));
      }

   /**
    * Computes the weighted distance to p_other.
    */
   public double weighted_distance(PlaPointFloat p_other, double p_horizontal_weight, double p_vertical_weight)
      {
      double delta_x = v_x - p_other.v_x;
      double delta_y = v_y - p_other.v_y;
      delta_x *= p_horizontal_weight;
      delta_y *= p_vertical_weight;
      double result = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
      return result;
      }

   /**
    * rounds the coordinates from an object of class Point_double to an object of class IntPoint
    */
   public PlaPointInt round()
      {
      return new PlaPointInt(Math.round(v_x), Math.round(v_y));
      }

   /**
    * Rounds this point, so that if this point is on the right side of any directed line with direction p_dir, the result point will
    * also be on the right side.
    */
   public PlaPointInt round_to_the_right(PlaDirection p_dir)
      {
      PlaPointFloat dir = p_dir.to_float();

      double rounded_x;
      if (dir.v_y > 0)
         {
         rounded_x = Math.ceil(v_x);
         }
      else if (dir.v_y < 0)
         {
         rounded_x = Math.floor(v_x);
         }
      else
         {
         rounded_x = Math.round(v_x);
         }

      double rounded_y;

      if (dir.v_x > 0)
         {
         rounded_y = Math.floor(v_y);
         }
      else if (dir.v_x < 0)
         {
         rounded_y = Math.ceil(v_y);
         }
      else
         {
         rounded_y = Math.round(v_y);
         }
      return new PlaPointInt(rounded_x, rounded_y);
      }

   /**
    * Round this Point so the x coordinate of the result will be a multiple of p_horizontal_grid and the y coordinate a multiple of
    * p_vertical_grid.
    */
   public PlaPointInt round_to_grid(int p_horizontal_grid, int p_vertical_grid)
      {
      double rounded_x;
      if (p_horizontal_grid > 0)
         {
         rounded_x = Math.rint(v_x / p_horizontal_grid) * p_horizontal_grid;
         }
      else
         {
         rounded_x = v_x;
         }
      
      double rounded_y;
      if (p_vertical_grid > 0)
         {
         rounded_y = Math.rint(v_y / p_vertical_grid) * p_vertical_grid;
         }
      else
         {
         rounded_y = v_y;
         }
      
      return new PlaPointInt(rounded_x, rounded_y);
      }

   /**
    * Rounds this point, so that if this point is on the left side of any directed line with direction p_dir, the result point will
    * also be on the left side.
    */
   public PlaPointInt round_to_the_left(PlaDirection p_dir)
      {
      PlaPointFloat dir = p_dir.to_float();
      double rounded_x;

      if (dir.v_y > 0)
         {
         rounded_x = Math.floor(v_x);
         }
      else if (dir.v_y < 0)
         {
         rounded_x = Math.ceil(v_x);
         }
      else
         {
         rounded_x = Math.round(v_x);
         }

      double rounded_y;

      if (dir.v_x > 0)
         {
         rounded_y = Math.ceil(v_y);
         }
      else if (dir.v_x < 0)
         {
         rounded_y = Math.floor(v_y);
         }
      else
         {
         rounded_y = Math.round(v_y);
         }
      
      return new PlaPointInt(rounded_x, rounded_y);
      }

   /**
    * Adds the coordinates of this FloatPoint and p_other.
    */
   public PlaPointFloat add(PlaPointFloat p_other)
      {
      return new PlaPointFloat(v_x + p_other.v_x, v_y + p_other.v_y);
      }

   /**
    * Substracts the coordinates of p_other from this FloatPoint.
    */
   public PlaPointFloat substract(PlaPointFloat p_other)
      {
      return new PlaPointFloat(v_x - p_other.v_x, v_y - p_other.v_y);
      }

   /**
    * Returns an approximation of the perpendicular projection of this point onto p_line
    */
   public PlaPointFloat projection_approx(PlaLineInt p_line)
      {
      PlaSegmentFloat line = new PlaSegmentFloat(p_line.point_a.to_float(), p_line.point_b.to_float());
      
      return line.perpendicular_projection(this);
      }

   /**
    * Calculates the scalar product of (p_1 - this). with (p_2 - this).
    */
   public double scalar_product(PlaPointFloat p_1, PlaPointFloat p_2)
      {
      if (p_1 == null || p_2 == null)
         {
         System.out.println("FloatPoint.scalar_product: parameter point is null");
         return 0;
         }
      
      double dx_1 = p_1.v_x - v_x;
      double dx_2 = p_2.v_x - v_x;
      
      double dy_1 = p_1.v_y - v_y;
      double dy_2 = p_2.v_y - v_y;
      
      return (dx_1 * dx_2 + dy_1 * dy_2);
      }

   /**
    * Approximates a FloatPoint on the line from zero to this point with distance p_new_length from zero.
    */
   public PlaPointFloat change_size(double p_new_size)
      {
      if (v_x == 0 && v_y == 0)
         {
         // the size of the zero point cannot be changed
         return this;
         }
      
      double length = Math.sqrt(v_x * v_x + v_y * v_y);
      double new_x = (v_x * p_new_size) / length;
      double new_y = (v_y * p_new_size) / length;
      
      return new PlaPointFloat(new_x, new_y);
      }

   /**
    * Approximates a FloatPoint on the line from this point to p_to_point with distance p_new_length from this point.
    */
   public PlaPointFloat change_length(PlaPointFloat p_to_point, double p_new_length)
      {
      double dx = p_to_point.v_x - v_x;
      double dy = p_to_point.v_y - v_y;
      
      if (dx == 0 && dy == 0)
         {
         System.out.println("IntPoint.change_length: Points are equal");
         return p_to_point;
         }
      
      double length = Math.sqrt(dx * dx + dy * dy);
      double new_x = v_x + (dx * p_new_length) / length;
      double new_y = v_y + (dy * p_new_length) / length;
      
      return new PlaPointFloat(new_x, new_y);
      }

   /**
    * Returns the middle point between this point and p_to_point.
    */
   public PlaPointFloat middle_point(PlaPointFloat p_to_point)
      {
      if (p_to_point == this) return this;

      double middle_x = 0.5 * (v_x + p_to_point.v_x);
      double middle_y = 0.5 * (v_y + p_to_point.v_y);

      return new PlaPointFloat(middle_x, middle_y);
      }

   /**
    * The function returns 
    * Side.ON_THE_LEFT, if this Point is on the left of the line from p_1 to p_2 
    * Side.ON_THE_RIGHT, if this Point is on the right of the line from p_1 to p_2
    * Collinearity is not defined, becouse numerical calculations ar not exact for FloatPoints.
    */
   public PlaSide side_of(PlaPointFloat p_1, PlaPointFloat p_2)
      {
      double line_dir_x  = p_2.v_x - p_1.v_x;   // this is equivalent to direction x for the line
      double line_dir_y  = p_2.v_y - p_1.v_y;   // this is equivalent to direction y for the line

      double point_dir_x = v_x - p_1.v_x;   // this is equivalent to the direction x for this point
      double point_dir_y = v_y - p_1.v_y;   // this is equivalent to the direction y for this point
      
      double determinant = line_dir_x * point_dir_y - line_dir_y * point_dir_x;
      
      return PlaSide.get_side_of(determinant);
      }

   /**
    * Rotates this FloatPoints by p_angle ( in radian ) around the p_pole.
    */
   public PlaPointFloat rotate(double p_angle, PlaPointFloat p_pole)
      {
      if (p_angle == 0)  return this;
      
      double dx = v_x - p_pole.v_x;
      double dy = v_y - p_pole.v_y;
      double sin_angle = Math.sin(p_angle);
      double cos_angle = Math.cos(p_angle);
      double new_dx = dx * cos_angle - dy * sin_angle;
      double new_dy = dx * sin_angle + dy * cos_angle;
      return new PlaPointFloat(p_pole.v_x + new_dx, p_pole.v_y + new_dy);
      }

   /**
    * @return a new point by p_factor times 90 degree around ZERO.
    */
   public PlaPointFloat turn_90_degree(int p_factor)
      {
      while (p_factor < 0)  p_factor += 4;

      while (p_factor >= 4)  p_factor -= 4;

      double new_x;
      double new_y;

      switch (p_factor)
         {
         case 0: // 0 degree
            new_x = v_x;
            new_y = v_y;
            break;
         case 1: // 90 degree
            new_x = -v_y;
            new_y = v_x;
            break;
         case 2: // 180 degree
            new_x = -v_x;
            new_y = -v_y;
            break;
         case 3: // 270 degree
            new_x = v_y;
            new_y = -v_x;
            break;
         default:
            new_x = 0;
            new_y = 0;
         }
      
      return new PlaPointFloat(new_x, new_y);
      }

   /**
    * Turns this FloatPoint by p_factor times 90 degree around p_pole.
    */
   public PlaPointFloat turn_90_degree(int p_factor, PlaPointFloat p_pole)
      {
      PlaPointFloat v = substract(p_pole);
      v = v.turn_90_degree(p_factor);
      return p_pole.add(v);
      }

   /**
    * Checks, if this point is contained in the box spanned by p_1 and p_2 with the input tolerance.
    */
   public boolean is_contained_in_box(PlaPointFloat p_1, PlaPointFloat p_2, double p_tolerance)
      {
      double min_x;
      double max_x;
      
      if (p_1.v_x < p_2.v_x)
         {
         min_x = p_1.v_x;
         max_x = p_2.v_x;
         }
      else
         {
         min_x = p_2.v_x;
         max_x = p_1.v_x;
         }
      
      if (v_x < min_x - p_tolerance || v_x > max_x + p_tolerance)
         {
         return false;
         }
      
      double min_y;
      double max_y;
      
      if (p_1.v_y < p_2.v_y)
         {
         min_y = p_1.v_y;
         max_y = p_2.v_y;
         }
      else
         {
         min_y = p_2.v_y;
         max_y = p_1.v_y;
         }
      
      return (v_y >= min_y - p_tolerance && v_y <= max_y + p_tolerance);
      }

   /**
    * Creates the smallest IntBox containing this point.
    */
   public ShapeTileBox bounding_box()
      {
      PlaPointInt lower_left = new PlaPointInt(Math.floor(v_x), Math.floor(v_y));
      PlaPointInt upper_right = new PlaPointInt(Math.ceil(v_x), Math.ceil(v_y));
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * Calculates the touching points of the tangents from this point to a circle around p_to_point with radius p_distance. Solves
    * the quadratic equation, which results by substituting x by the term in y from the equation of the polar line of a circle with
    * center p_to_point and radius p_distance and putting it into the circle equation. The polar line is the line through the 2
    * tangential points of the circle looked at from from this point and has the equation (this.x - p_to_point.x) * (x -
    * p_to_point.x) + (this.y - p_to_point.y) * (y - p_to_point.y) = p_distance **2
    */
   public PlaPointFloat[] tangential_points(PlaPointFloat p_to_point, double p_distance)
      {
      // turn the situation 90 degree if the x difference is smaller
      // than the y difference for better numerical stability

      double dx = Math.abs(v_x - p_to_point.v_x);
      double dy = Math.abs(v_y - p_to_point.v_y);
      boolean situation_turned = (dy > dx);
      PlaPointFloat pole;
      PlaPointFloat circle_center;

      if (situation_turned)
         {
         // turn the situation by 90 degree
         pole = new PlaPointFloat(-v_y, v_x);
         circle_center = new PlaPointFloat(-p_to_point.v_y, p_to_point.v_x);
         }
      else
         {
         pole = this;
         circle_center = p_to_point;
         }

      dx = pole.v_x - circle_center.v_x;
      dy = pole.v_y - circle_center.v_y;
      double dx_square = dx * dx;
      double dy_square = dy * dy;
      double dist_square = dx_square + dy_square;
      double radius_square = p_distance * p_distance;
      double discriminant = radius_square * dy_square - (radius_square - dx_square) * dist_square;

      if (discriminant <= 0)
         {
         // pole is inside the circle.
         return new PlaPointFloat[0];
         }
      double square_root = Math.sqrt(discriminant);

      PlaPointFloat[] result = new PlaPointFloat[2];

      double a1 = radius_square * dy;
      double dy1 = (a1 + p_distance * square_root) / dist_square;
      double dy2 = (a1 - p_distance * square_root) / dist_square;

      double first_point_y = dy1 + circle_center.v_y;
      double first_point_x = (radius_square - dy * dy1) / dx + circle_center.v_x;
      double second_point_y = dy2 + circle_center.v_y;
      double second_point_x = (radius_square - dy * dy2) / dx + circle_center.v_x;

      if (situation_turned)
         {
         // turn the result by 270 degree
         result[0] = new PlaPointFloat(first_point_y, -first_point_x);
         result[1] = new PlaPointFloat(second_point_y, -second_point_x);
         }
      else
         {
         result[0] = new PlaPointFloat(first_point_x, first_point_y);
         result[1] = new PlaPointFloat(second_point_x, second_point_y);
         }
      return result;
      }

   /**
    * Calculates the left tangential point of the line from this point to a circle around p_to_point with radius p_distance. 
    * @return null, if this point is inside this circle.
    */
   public PlaPointFloat left_tangential_point(PlaPointFloat p_to_point, double p_distance)
      {
      if (p_to_point == null) return null;

      PlaPointFloat[] tangent_points = tangential_points(p_to_point, p_distance);

      if (tangent_points.length < 2)   return null;

      if (p_to_point.side_of(this, tangent_points[0]) == PlaSide.ON_THE_LEFT)
         return tangent_points[0];
      else
         return tangent_points[1];
      }

   /**
    * Calculates the right tangential point of the line from this point to a circle around p_to_point with radius p_distance.
    * Returns null, if this point is inside this circle.
    */
   public PlaPointFloat right_tangential_point(PlaPointFloat p_to_point, double p_distance)
      {
      if (p_to_point == null)         return null;

      PlaPointFloat[] tangent_points = tangential_points(p_to_point, p_distance);

      if (tangent_points.length < 2) return null;

      if (p_to_point.side_of(this, tangent_points[0]) == PlaSide.ON_THE_RIGHT)
         return tangent_points[0];
      else
         return tangent_points[1];
      }

   /**
    * Calculates the center of the circle through this point, p_1 and p_2 by calculating the intersection of the two lines
    * perpendicular to and passing through the midpoints of the lines (this, p_1) and (p_1, p_2).
    */
   public PlaPointFloat circle_center(PlaPointFloat p_1, PlaPointFloat p_2)
      {
      double slope_1 = (p_1.v_y - v_y) / (p_1.v_x - v_x);
      double slope_2 = (p_2.v_y - p_1.v_y) / (p_2.v_x - p_1.v_x);
      double x_center = (slope_1 * slope_2 * (v_y - p_2.v_y) + slope_2 * (v_x + p_1.v_x) - slope_1 * (p_1.v_x + p_2.v_x)) / (2 * (slope_2 - slope_1));
      double y_center = (0.5 * (v_x + p_1.v_x) - x_center) / slope_1 + 0.5 * (v_y + p_1.v_y);
      return new PlaPointFloat(x_center, y_center);
      }

   /**
    * Returns true, if this point is contained in the circle through p_1, p_2 and p_3.
    */
   public boolean inside_circle(PlaPointFloat p_1, PlaPointFloat p_2, PlaPointFloat p_3)
      {
      PlaPointFloat center = p_1.circle_center(p_2, p_3);
      double radius_square = center.length_square(p_1);
      return (length_square(center) < radius_square - 1); // - 1 is a tolerance for numerical stability.
      }

   public String to_string(java.util.Locale p_locale)
      {
      NumberFormat nf = NumberFormat.getInstance(p_locale);
      nf.setMaximumFractionDigits(4);
      return (" (" + nf.format(v_x) + " , " + nf.format(v_y) + ") ");
      }

   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(100);
      risul.append("("+v_x);
      risul.append(","+v_y);
      risul.append(')');
      return risul.toString();
      }


   private final int compare ( double first, double second )
      {
      double compare = first - second; 
      if ( compare > 0 ) 
         return 1;
      else if ( compare < 0 ) 
         return -1;
      else 
         return 0;
      }

   //   @Override
   protected int compare_x(PlaPointInt p_other)
      {
      return compare (v_x,p_other.v_x); 
      }
   
   protected int compare_x(PlaPointRational p_other)
      {
      return compare (v_x,p_other.to_float().v_x); 
      }

   
   protected int compare_y(PlaPointInt p_other)
      {
      return compare (v_y,p_other.v_y); 
      }

   protected int compare_y(PlaPointRational p_other)
      {
      return compare (v_y,p_other.to_float().v_y); 
      }
   
   }