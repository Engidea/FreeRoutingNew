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
 * PolylineShape.java
 *
 * Created on 16. November 2002, 09:34
 */

package freert.planar;


/**
 * Abstract class with functions for shapes, whose borders consist of straight lines.
 *
 * @author Alfons Wirtz
 */
public abstract class ShapePolyline implements PlaShape, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   protected boolean is_nan;
   
   @Override
   public final boolean is_NaN ()
      {
      return is_nan;
      }
   
   /**
    * returns true, if the the shape has no infinite part at this corner
    */
   public abstract boolean corner_is_bounded(int p_no);

   /**
    * Returns the number of border lines of the shape
    */
   public abstract int border_line_count();

   /**
    * Returns the p_no-th corner of this shape for p_no between 0 and border_line_count() - 1. 
    * The corners are sorted starting with the smallest y-coordinate in counterclock sense arount the shape. 
    * If there are several corners with the smallest y-coordinate, the corner with the smallest x-coordinate comes first. 
    * Consecutive corners may be equal.
    */
   public abstract PlaPointInt corner(int p_no);

   /**
    * Turns this shape by p_factor times 90 degree around p_pole.
    */
   @Override
   public abstract ShapePolyline turn_90_degree(int p_factor, PlaPointInt p_pole);

   /**
    * Rotates this shape around p_pole by p_angle. The result may be not exact.
    */
   @Override
   public abstract ShapePolyline rotate_approx(double p_angle, PlaPointFloat p_pole);

   /**
    * Mirrors this shape at the horizontal line through p_pole.
    */
   @Override
   public abstract ShapePolyline mirror_horizontal(PlaPointInt p_pole);

   /**
    * Mirrors this shape at the vertical line through p_pole.
    */
   @Override
   public abstract ShapePolyline mirror_vertical(PlaPointInt p_pole);

   /**
    * Returns the affine translation of the area by p_vector
    */
   @Override
   public abstract ShapePolyline translate_by(PlaVectorInt p_vector);

   /**
    * Returns an approximation of the p_no-th corner of this shape for p_no between 0 and border_line_count() - 1. If the shape is
    * not bounded at this corner, the coordinates of the result will be set to Integer.MAX_VALUE.
    */
   public PlaPointFloat corner_approx(int p_no)
      {
      return corner(p_no).to_float();
      }

   /**
    * Returns an approximation of the all corners of this shape. 
    * If the shape is not bounded at a corner, the coordinates will be set to Integer.MAX_VALUE.
    */
   @Override
   public PlaPointFloat[] corner_approx_arr()
      {
      int corner_count = border_line_count();
      
      PlaPointFloat[] result = new PlaPointFloat[corner_count];
      
      for (int index = 0; index < corner_count; ++index)
         {
         result[index] = corner_approx(index);
         }
      
      return result;
      }

   /**
    * If p_point is equal to a corner of this shape, the number of that corner is returned; -1 otherwise.
    */
   public final int equals_corner(PlaPoint p_point)
      {
      for (int index = 0; index < border_line_count(); ++index)
         {
         if (p_point.equals(corner(index))) return index;
         }

      return -1;
      }

   /**
    * Returns the cumulative border line length of the shape. If the shape is unbounded, Integer.MAX_VALUE is returned.
    */
   @Override
   public double circumference()
      {
      if (!is_bounded()) return Integer.MAX_VALUE;

      int corner_count = border_line_count();
      double result = 0;
      PlaPointFloat prev_corner = corner_approx(corner_count - 1);
      for (int i = 0; i < corner_count; ++i)
         {
         PlaPointFloat curr_corner = corner_approx(i);
         result += curr_corner.distance(prev_corner);
         prev_corner = curr_corner;
         }
      return result;
      }

   /**
    * Returns the arithmetic middle of the corners of this shape
    */
   @Override
   public PlaPointFloat centre_of_gravity()
      {
      int corner_count = border_line_count();
      double x = 0;
      double y = 0;
      for (int index = 0; index < corner_count; ++index)
         {
         PlaPointFloat curr_point = corner_approx(index);
         x += curr_point.v_x;
         y += curr_point.v_y;
         }
      x /= corner_count;
      y /= corner_count;
      return new PlaPointFloat(x, y);
      }

   /**
    * checks, if this shape is completely contained in p_box.
    */
   @Override
   public  boolean is_contained_in(ShapeTileBox p_box)
      {
      return p_box.contains(bounding_box());
      }

   /**
    * Returns the index of the corner of the shape, so that all other points of the shape are to the right of the line from
    * p_from_point to this corner
    */
   public final int index_of_left_most_corner(PlaPointFloat p_from_point)
      {
      PlaPointFloat left_most_corner = corner_approx(0);
      int corner_count = border_line_count();
      int result = 0;
      
      for (int index = 1; index < corner_count; ++index)
         {
         PlaPointFloat curr_corner = corner_approx(index);
         
         if (curr_corner.side_of(p_from_point, left_most_corner) == PlaSide.ON_THE_LEFT)
            {
            left_most_corner = curr_corner;
            result = index;
            }
         }
      
      return result;
      }

   /**
    * Returns the index of the corner of the shape, so that all other points of the shape are to the right of the line from
    * p_from_point to this corner
    */
   public final int index_of_right_most_corner(PlaPointFloat p_from_point)
      {
      PlaPointFloat right_most_corner = corner_approx(0);
      int corner_count = border_line_count();
      int result = 0;
      
      for (int index = 1; index < corner_count; ++index)
         {
         PlaPointFloat curr_corner = corner_approx(index);
      
         if (curr_corner.side_of(p_from_point, right_most_corner) == PlaSide.ON_THE_RIGHT)
            {
            right_most_corner = curr_corner;
            result = index;
            }
         }
      return result;
      }

   /**
    * Returns a FloatLine so that result.a is an approximation of the left most corner of this shape when viewed from
    * p_from_point, and result.b is an approximation of the right most corner.
    */
   public final PlaSegmentFloat polar_line_segment(PlaPointFloat p_from_point)
      {
      if (is_empty())
         {
         System.out.println("PolylineShape.polar_line_segment: shape is empty");
         return null;
         }
      
      PlaPointFloat left_most_corner = corner_approx(0);
      PlaPointFloat right_most_corner = corner_approx(0);
      int corner_count = border_line_count();
      for (int index = 1; index < corner_count; ++index)
         {
         PlaPointFloat curr_corner = corner_approx(index);
         if (curr_corner.side_of(p_from_point, right_most_corner) == PlaSide.ON_THE_RIGHT)
            right_most_corner = curr_corner;

         if (curr_corner.side_of(p_from_point, left_most_corner) == PlaSide.ON_THE_LEFT)
            left_most_corner = curr_corner;

         }

      return new PlaSegmentFloat(left_most_corner, right_most_corner);
      }

   /**
    * Returns the p_no-th border line of this shape.
    */
   public abstract PlaLineInt border_line(int p_no);

   /**
    * @return the previous border line or corner number of this shape.
    */
   public final int prev_no(int p_no)
      {
      if ( p_no <= 0 )
         return border_line_count() - 1;
      else
         return p_no - 1;
      }

   /**
    * @return the next border line or corner number of this shape.
    */
   public final int next_no(int p_no)
      {
      if (p_no >= border_line_count() - 1)
         return 0;
      else
         return p_no + 1;
      }

   @Override
   public ShapePolyline get_border()
      {
      return this;
      }

   @Override
   public PlaShape[] get_holes()
      {
      return new PlaShape[0];
      }

   /**
    * Checks, if this shape and p_line have a common point.
    */
   public final boolean intersects(PlaLineInt p_line)
      {
      PlaSide side_of_first_corner = p_line.side_of(corner(0));

      if (side_of_first_corner == PlaSide.COLLINEAR) return true;

      for (int index = 1; index < border_line_count(); ++index)
         {
         if (p_line.side_of(corner(index)) != side_of_first_corner) return true;
         }

      return false;
      }

   /**
    * Calculates the left most corner of this shape, when looked at from p_from_point.
    */
   public final PlaPoint left_most_corner(PlaPoint p_from_point)
      {
      if (is_empty()) return p_from_point;

      PlaPoint result = corner(0);
      int corner_count = border_line_count();
      for (int index = 1; index < corner_count; ++index)
         {
         PlaPoint curr_corner = corner(index);
         
         if (curr_corner.side_of(p_from_point, result) == PlaSide.ON_THE_LEFT)
            {
            result = curr_corner;
            }
         }
      return result;
      }

   /**
    * Calculates the left most corner of this shape, when looked at from p_from_point.
    */
   public PlaPoint right_most_corner(PlaPoint p_from_point)
      {
      if ( is_empty()) return p_from_point;

      PlaPoint result = corner(0);
      int corner_count = border_line_count();
      for (int i = 1; i < corner_count; ++i)
         {
         PlaPoint curr_corner = this.corner(i);
         if (curr_corner.side_of(p_from_point, result) == PlaSide.ON_THE_RIGHT)
            {
            result = curr_corner;
            }
         }
      return result;
      }
   }
