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
package planar;

import datastructures.Signum;

/**
 * Implements functionality for line segments. 
 * The difference between a LineSegment and a Line is, that a Line is possibly infinite but a
 * LineSegment has a start and an endpoint.
 *
 *
 * @author Alfons Wirtz
 */
public final class PlaSegmentInt implements java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;

   private final PlaLineInt start;
   private final PlaLineInt middle;
   private final PlaLineInt end;
   
   transient private PlaPoint precalculated_start_point = null;
   transient private PlaPoint precalculated_end_point = null;
   
   /**
    * Creates a line segment from the 3 input lines.
    *  It starts at the intersection of p_start_line and p_middle_line and ends at the
    * intersection of p_middle_line and p_end_line. p_start_line and p_end_line must not be parallel to p_middle_line.
    */
   public PlaSegmentInt(PlaLineInt p_start_line, PlaLineInt p_middle_line, PlaLineInt p_end_line)
      {
      start = p_start_line;
      middle = p_middle_line;
      end = p_end_line;
      }

   /**
    * creates the p_no-th line segment of p_polyline for p_no between 1 and p_polyline.line_count - 2.
    */
   public PlaSegmentInt(Polyline p_polyline, int p_no)
      {
      if (p_no <= 0 || p_no >= p_polyline.lines_arr.length - 1)
         {
         System.out.println("LineSegment from Polyline: p_no out of range");
         start = null;
         middle = null;
         end = null;
         return;
         }
      
      start = p_polyline.lines_arr[p_no - 1];
      middle = p_polyline.lines_arr[p_no];
      end = p_polyline.lines_arr[p_no + 1];
      }

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
   
   /**
    * Creates the p_no-th line segment of p_shape for p_no between 0 and p_shape.line_count - 1.
    */
   public PlaSegmentInt(ShapePolyline p_shape, int p_no)
      {
      int line_count = p_shape.border_line_count();
      
      if (p_no < 0 || p_no >= line_count)
         {
         System.out.println("LineSegment from TileShape: p_no out of range");
         start = null;
         middle = null;
         end = null;
         return;
         }
      
      if (p_no == 0)
         {
         start = p_shape.border_line(line_count - 1);
         }
      else
         {
         start = p_shape.border_line(p_no - 1);
         }
      
      middle = p_shape.border_line(p_no);
      
      if (p_no == line_count - 1)
         {
         end = p_shape.border_line(0);
         }
      else
         {
         end = p_shape.border_line(p_no + 1);
         }
      }

   /**
    * Returns the intersection of the first 2 lines of this segment
    */
   public PlaPoint start_point()
      {
      if (precalculated_start_point == null)
         {
         precalculated_start_point = middle.intersection(start);
         }
      
      return precalculated_start_point;
      }

   /**
    * Returns the intersection of the last 2 lines of this segment
    */
   public PlaPoint end_point()
      {
      if (precalculated_end_point == null)
         {
         precalculated_end_point = middle.intersection(end);
         }
      return precalculated_end_point;
      }

   /**
    * Returns an approximation of the intersection of the first 2 lines of this segment
    */
   public PlaPointFloat start_point_approx()
      {
      PlaPointFloat result;
      if (precalculated_start_point != null)
         {
         result = precalculated_start_point.to_float();
         }
      else
         {
         result = this.start.intersection_approx(this.middle);
         }
      return result;
      }

   /**
    * Returns an approximation of the intersection of the last 2 lines of this segment
    */
   public PlaPointFloat end_point_approx()
      {
      PlaPointFloat result;
      if (precalculated_end_point != null)
         {
         result = precalculated_end_point.to_float();
         }
      else
         {
         result = this.end.intersection_approx(this.middle);
         }
      return result;
      }

   /**
    * Returns the (infinite) line of this segment.
    */
   public PlaLineInt get_line()
      {
      return middle;
      }

   /**
    * Returns the start closing line of this segment.
    */
   public PlaLineInt get_start_closing_line()
      {
      return start;
      }

   /**
    * Returns the end closing line of this segment.
    */
   public PlaLineInt get_end_closing_line()
      {
      return end;
      }

   /**
    * Returns the line segment with tje opposite direction.
    */
   public PlaSegmentInt opposite()
      {
      return new PlaSegmentInt(end.opposite(), middle.opposite(), start.opposite());
      }

   /**
    * Transforms this LinsSegment into a polyline of length 3
    * Other part of software assume that the length of the array will b three, do not change it
    */
   public final Polyline to_polyline()
      {
      PlaLineInt[] lines = new PlaLineInt[3];
      lines[0] = start;
      lines[1] = middle;
      lines[2] = end;
      return new Polyline(lines);
      }

   /**
    * Creates a 1 dimensional simplex rom this line segment, which has the same shape as the line sgment.
    */
   public ShapeTileSimplex to_simplex()
      {
      PlaLineInt[] line_arr = new PlaLineInt[4];
      if (this.end_point().side_of(this.start) == PlaSide.ON_THE_RIGHT)
         {
         line_arr[0] = this.start.opposite();
         }
      else
         {
         line_arr[0] = this.start;
         }
      line_arr[1] = this.middle;
      line_arr[2] = this.middle.opposite();
      if (this.start_point().side_of(this.end) == PlaSide.ON_THE_RIGHT)
         {
         line_arr[3] = this.end.opposite();
         }
      else
         {
         line_arr[3] = this.end;
         }
      ShapeTileSimplex result = ShapeTileSimplex.get_instance(line_arr);
      return result;
      }

   /**
    * Checks if p_point is contained in this line segment
    */
   public final  boolean contains(PlaPointInt p_point)
      {
      if (middle.side_of(p_point) != PlaSide.COLLINEAR) return false;
      
      // create a perpendicular line at p_point and check, that the two
      // endpoints of this segment are on difcferent sides of that line.
      PlaDirection perpendicular_direction = middle.direction().turn_45_degree(2);
      PlaLineInt perpendicular_line = new PlaLineInt(p_point, perpendicular_direction);
      PlaSide start_point_side = perpendicular_line.side_of(this.start_point());
      PlaSide end_point_side = perpendicular_line.side_of(this.end_point());
      
      if (start_point_side != PlaSide.COLLINEAR && end_point_side != PlaSide.COLLINEAR && start_point_side == end_point_side)
         {
         return false;
         }
      
      return true;
      }

   /**
    * Checks if p_point is contained in this line segment
    */
   public final boolean contains(PlaPoint p_point)
      {
      if (!(p_point instanceof PlaPointInt))
         {
         System.out.println("LineSegments.contains currently only implementet for IntPoints");
         return false;
         }
      
      return contains((PlaPointInt)p_point);
      }

   /**
    * calculates the smallest surrounding box of this line segmant
    */
   public ShapeTileBox bounding_box()
      {
      PlaPointFloat start_corner = middle.intersection_approx(start);
      PlaPointFloat end_corner = middle.intersection_approx(end);
      double llx = Math.min(start_corner.point_x, end_corner.point_x);
      double lly = Math.min(start_corner.point_y, end_corner.point_y);
      double urx = Math.max(start_corner.point_x, end_corner.point_x);
      double ury = Math.max(start_corner.point_y, end_corner.point_y);
      PlaPointInt lower_left = new PlaPointInt(Math.floor(llx), Math.floor(lly));
      PlaPointInt upper_right = new PlaPointInt(Math.ceil(urx), Math.ceil(ury));
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * calculates the smallest surrounding octagon of this line segmant
    */
   public ShapeTileOctagon bounding_octagon()
      {
      PlaPointFloat start_corner = middle.intersection_approx(start);
      PlaPointFloat end_corner = middle.intersection_approx(end);
      double lx = Math.floor(Math.min(start_corner.point_x, end_corner.point_x));
      double ly = Math.floor(Math.min(start_corner.point_y, end_corner.point_y));
      double rx = Math.ceil(Math.max(start_corner.point_x, end_corner.point_x));
      double uy = Math.ceil(Math.max(start_corner.point_y, end_corner.point_y));
      double start_x_minus_y = start_corner.point_x - start_corner.point_y;
      double end_x_minus_y = end_corner.point_x - end_corner.point_y;
      double ulx = Math.floor(Math.min(start_x_minus_y, end_x_minus_y));
      double lrx = Math.ceil(Math.max(start_x_minus_y, end_x_minus_y));
      double start_x_plus_y = start_corner.point_x + start_corner.point_y;
      double end_x_plus_y = end_corner.point_x + end_corner.point_y;
      double llx = Math.floor(Math.min(start_x_plus_y, end_x_plus_y));
      double urx = Math.ceil(Math.max(start_x_plus_y, end_x_plus_y));
      ShapeTileOctagon result = new ShapeTileOctagon((int) lx, (int) ly, (int) rx, (int) uy, (int) ulx, (int) lrx, (int) llx, (int) urx);
      return result.normalize();
      }

   /**
    * Creates a new line segment with the same start and middle line and an end line, so that the length of the new line segment is
    * about p_new_length.
    */
   public PlaSegmentInt change_length_approx(double p_new_length)
      {
      PlaPointFloat new_end_point = start_point_approx().change_length(end_point_approx(), p_new_length);
      PlaDirection perpendicular_direction = this.middle.direction().turn_45_degree(2);
      PlaLineInt new_end_line = new PlaLineInt(new_end_point.round(), perpendicular_direction);
      PlaSegmentInt result = new PlaSegmentInt(this.start, this.middle, new_end_line);
      return result;
      }

   /**
    * Looks up the intersections of this line segment with p_other. 
    * The result array may have length 0, 1 or 2. 
    * If the segments do not intersect the result array will have length 0. 
    * The result lines are so that the intersections of the result lines with this line segment will deliver the intersection points. 
    * If the segments overlap, the result array has length 2 and the intersection points are the first and the last overlap point. 
    * Otherwise the result array has length 1 and the intersection point is the the unique intersection or touching point. 
    * The result is not symmetric in this and p_other, because intersecting lines and not the intersection points are returned.
    */
   public PlaLineInt[] intersection(PlaSegmentInt p_other)
      {
      if ( ! bounding_box().intersects(p_other.bounding_box()))
         {
         return new PlaLineInt[0];
         }
      
      PlaSide start_point_side = start_point().side_of(p_other.middle);
      PlaSide end_point_side = end_point().side_of(p_other.middle);
      if (start_point_side == PlaSide.COLLINEAR && end_point_side == PlaSide.COLLINEAR)
         {
         // there may be an overlap
         PlaSegmentInt this_sorted = this.sort_endpoints_in_x_y();
         PlaSegmentInt other_sorted = p_other.sort_endpoints_in_x_y();
         PlaSegmentInt left_line;
         PlaSegmentInt right_line;
         if (this_sorted.start_point().compare_x_y(other_sorted.start_point()) <= 0)
            {
            left_line = this_sorted;
            right_line = other_sorted;
            }
         else
            {
            left_line = other_sorted;
            right_line = this_sorted;
            }
         int cmp = left_line.end_point().compare_x_y(right_line.start_point());
         if (cmp < 0)
            {
            // end point of the left line is to the lsft of the start point of the right line
            return new PlaLineInt[0];
            }
         if (cmp == 0)
            {
            // end point of the left line is equal to the start point of the right line
            PlaLineInt[] result = new PlaLineInt[1];
            result[0] = left_line.end;
            return result;
            }
         // now there is a real overlap
         PlaLineInt[] result = new PlaLineInt[2];
         result[0] = right_line.start;
         if (right_line.end_point().compare_x_y(left_line.end_point()) >= 0)
            {
            result[1] = left_line.end;
            }
         else
            {
            result[1] = right_line.end;
            }
         return result;
         }
      if (start_point_side == end_point_side || p_other.start_point().side_of(this.middle) == p_other.end_point().side_of(this.middle))
         {
         return new PlaLineInt[0]; // no intersection possible
         }
      // now both start points and both end points are on different sides of the middle
      // line of the other segment.
      PlaLineInt[] result = new PlaLineInt[1];
      result[0] = p_other.middle;
      return result;
      }

   /**
    * Checks if this LineSegment and p_other contain a commen point
    */
   public boolean intersects(PlaSegmentInt p_other)
      {
      PlaLineInt[] intersections = intersection(p_other);
      return intersections.length > 0;
      }

   /**
    * Checks if this LineSegment and p_other contain a common LineSegment, which is not reduced to a point.
    */
   public boolean overlaps(PlaSegmentInt p_other)
      {
      PlaLineInt[] intersections = intersection(p_other);
      return intersections.length > 1;
      }

   /**
    * Constructs an approximation of this line segment by orthogonal stairs with integer coordinates. 
    * The length of the stairs will be at most p_stair_width. 
    * If p_to_the_right, the stairs will be to the right of this line segment, else to the left.
    */
   public PlaPointInt[] stair_approximation_90(double p_width, boolean p_to_the_right)
      {
      PlaPointInt start_point = this.start_point().to_float().round();
      PlaPointInt end_point = this.end_point().to_float().round();
      if (start_point.equals(end_point))
         {
         return new PlaPointInt[0];

         }

      if (start_point.v_x == end_point.v_x || start_point.v_y == end_point.v_y)
         {
         PlaPointInt[] result = new PlaPointInt[2];
         result[0] = start_point;
         result[1] = end_point;
         return result;
         }

      int dx = end_point.v_x - start_point.v_x;
      int dy = end_point.v_y - start_point.v_y;
      int abs_dx = Math.abs(dx);
      int abs_dy = Math.abs(dy);
      boolean function_of_x = abs_dx >= abs_dy;
      // use otherwise function of y for better numerical stability

      int stair_width;
      int stair_count;

      if (function_of_x)
         {
         stair_width = (int) Math.round(((p_width * (double) abs_dx) / (double) abs_dy));
         stair_count = (abs_dx - 1) / stair_width + 1;
         if (end_point.v_x < start_point.v_x)
            {
            stair_width = -stair_width;
            }
         }
      else
         {
         stair_width = (int) Math.round((p_width * (double) abs_dy) / (double) abs_dx);
         stair_count = (abs_dy - 1) / stair_width + 1;
         if (end_point.v_y < start_point.v_y)
            {
            stair_width = -stair_width;
            }
         }
      PlaPointInt[] result = new PlaPointInt[2 * stair_count + 1];

      result[0] = start_point;
      double det = (double) dx * (double) dy;
      boolean change_x_first = p_to_the_right && det > 0 || !p_to_the_right && det < 0;
      int curr_index = 0;

      int prev_line_point_x = start_point.v_x;
      int prev_line_point_y = start_point.v_y;
      for (int i = 1; i < stair_count; ++i)
         {
         int curr_line_point_x;
         int curr_line_point_y;
         if (function_of_x)
            {
            curr_line_point_x = start_point.v_x + i * stair_width;
            curr_line_point_y = (int) Math.round(this.get_line().function_value_approx(curr_line_point_x));
            }
         else
            {
            curr_line_point_y = start_point.v_y + i * stair_width;
            curr_line_point_x = (int) Math.round(this.get_line().function_in_y_value_approx(curr_line_point_y));
            }
         ++curr_index;
         if (change_x_first)
            {
            result[curr_index] = new PlaPointInt(curr_line_point_x, prev_line_point_y);
            }
         else
            {
            result[curr_index] = new PlaPointInt(prev_line_point_x, curr_line_point_y);
            }
         ++curr_index;
         result[curr_index] = new PlaPointInt(curr_line_point_x, curr_line_point_y);
         prev_line_point_x = curr_line_point_x;
         prev_line_point_y = curr_line_point_y;
         }
      ++curr_index;
      if (change_x_first)
         {
         result[curr_index] = new PlaPointInt(end_point.v_x, prev_line_point_y);
         }
      else
         {
         result[curr_index] = new PlaPointInt(prev_line_point_x, end_point.v_y);
         }
      ++curr_index;
      result[curr_index] = end_point;
      return result;
      }

   /**
    * Constructs an approximation of this line segment by 45 degree stairs with integer coordinates. The length of the stairs will
    * be at most p_stair_width. If p_to_the_right, the stairs will be to the right of this line segment, else to the left.
    */
   public PlaPointInt[] stair_approximation_45(double p_width, boolean p_to_the_right)
      {
      PlaPointInt start_point = this.start_point().to_float().round();
      PlaPointInt end_point = this.end_point().to_float().round();
      if (start_point.equals(end_point))
         {
         return new PlaPointInt[0];

         }
      PlaVectorInt delta = end_point.difference_by(start_point);
      if (delta.is_multiple_of_45_degree())
         {
         PlaPointInt[] result = new PlaPointInt[2];
         result[0] = start_point;
         result[1] = end_point;
         return result;
         }
      PlaVectorInt abs_delta = new PlaVectorInt(Math.abs(delta.point_x), Math.abs(delta.point_y));
      boolean function_of_x = abs_delta.point_x >= abs_delta.point_y;
      // use otherwise function of y for better numerical stability
      double det = (double) delta.point_x * (double) delta.point_y;
      int stair_width;
      int stair_count;
      if (function_of_x)
         {
         stair_width = (int) Math.round((p_width * (double) abs_delta.point_x) / (double) abs_delta.point_y);
         stair_count = (abs_delta.point_x - 1) / stair_width + 1;
         if (end_point.v_x < start_point.v_x)
            {
            stair_width = -stair_width;
            }
         }
      else
         {
         stair_width = (int) Math.round((p_width * (double) abs_delta.point_y) / (double) abs_delta.point_x);
         stair_count = (abs_delta.point_y - 1) / stair_width + 1;
         if (end_point.v_y < start_point.v_y)
            {
            stair_width = -stair_width;
            }

         }
      PlaPointInt[] result = new PlaPointInt[2 * stair_count + 1];
      result[0] = start_point;
      PlaPointInt prev_line_point = start_point;
      int curr_index = 0;
      for (int i = 1; i <= stair_count; ++i)
         {
         PlaPointInt curr_line_point;
         int curr_x;
         int curr_y;
         if (i == stair_count)
            {
            curr_line_point = end_point;
            }
         else
            {
            if (function_of_x)
               {
               curr_x = start_point.v_x + i * stair_width;
               curr_y = (int) Math.round(this.get_line().function_value_approx(curr_x));
               }
            else
               {
               curr_y = start_point.v_y + i * stair_width;
               curr_x = (int) Math.round(this.get_line().function_value_approx(curr_y));
               }
            curr_line_point = new PlaPointInt(curr_x, curr_y);
            }
         if (function_of_x)
            {
            boolean diagonal_first = p_to_the_right && det < 0 || !p_to_the_right && det > 0;

            if (diagonal_first)
               {
               curr_x = prev_line_point.v_x + Signum.as_int(stair_width) * Math.abs(curr_line_point.v_y - prev_line_point.v_y);
               curr_y = curr_line_point.v_y;
               }
            else
               // horizontal first
               {
               curr_x = curr_line_point.v_x - Signum.as_int(stair_width) * Math.abs(curr_line_point.v_y - prev_line_point.v_y);
               curr_y = prev_line_point.v_y;
               }
            }
         else
            // function of y
            {
            boolean diagonal_first = p_to_the_right && det > 0 || !p_to_the_right && det < 0;

            if (diagonal_first)
               {
               curr_x = curr_line_point.v_x;
               curr_y = prev_line_point.v_y + Signum.as_int(stair_width) * Math.abs(curr_line_point.v_x - prev_line_point.v_x);
               }
            else
               {
               curr_x = prev_line_point.v_x;
               curr_y = curr_line_point.v_y - Signum.as_int(stair_width) * Math.abs(curr_line_point.v_x - prev_line_point.v_x);
               }

            }
         ++curr_index;
         result[curr_index] = new PlaPointInt(curr_x, curr_y);
         ++curr_index;
         result[curr_index] = curr_line_point;
         prev_line_point = curr_line_point;
         }
      return result;
      }

   /**
    * Returns an array with the borderline numbers of p_shape, which are intersected by this line segment. 
    * Intersections at an endpoint of this line segment are only counted, if the line segment intersects with the interiour of p_shape. 
    * The result array may have lenght 0, 1 or 2. 
    * With 2 intersections the intersection which is nearest to the start point of the line segment comes first.
    */
   public int[] border_intersections(ShapeTile p_shape)
      {
      int[] empty_result = new int[0];
      
      if ( ! bounding_box().intersects(p_shape.bounding_box())) return empty_result;

      int edge_count = p_shape.border_line_count();
      PlaLineInt prev_line = p_shape.border_line(edge_count - 1);
      PlaLineInt curr_line = p_shape.border_line(0);
      int[] result = new int[2];
      PlaPoint[] intersection = new PlaPoint[2];
      int intersection_count = 0;
      PlaPoint line_start = start_point();
      PlaPoint line_end = end_point();

      for (int edge_line_no = 0; edge_line_no < edge_count; ++edge_line_no)
         {
         PlaLineInt next_line;
         if (edge_line_no == edge_count - 1)
            {
            next_line = p_shape.border_line(0);
            }
         else
            {
            next_line = p_shape.border_line(edge_line_no + 1);
            }

         PlaSide start_point_side = curr_line.side_of(line_start);
         PlaSide end_point_side = curr_line.side_of(line_end);
         if (start_point_side == PlaSide.ON_THE_LEFT && end_point_side == PlaSide.ON_THE_LEFT)
            {
            // both endpoints are outside the border_line,
            // no intersection possible
            return empty_result;
            }

         if (start_point_side == PlaSide.COLLINEAR)
            {
            // the start is on curr_line, check that the end point is inside
            // the halfplane, because touches count only, if the interiour
            // is entered
            if (end_point_side != PlaSide.ON_THE_RIGHT)
               {
               return empty_result;
               }

            }

         if (end_point_side == PlaSide.COLLINEAR)
            {
            // the end is on curr_line, check that the start point is inside
            // the halfplane, because touches count only, if the interiour
            // is entered
            if (start_point_side != PlaSide.ON_THE_RIGHT)
               {
               return empty_result;
               }

            }

         if (start_point_side != PlaSide.ON_THE_RIGHT || end_point_side != PlaSide.ON_THE_RIGHT)
            {
            // not both points are inside the halplane defined by curr_line
            PlaPoint is = middle.intersection(curr_line);
            PlaSide prev_line_side_of_is = prev_line.side_of(is);
            PlaSide next_line_side_of_is = next_line.side_of(is);
            if (prev_line_side_of_is != PlaSide.ON_THE_LEFT && next_line_side_of_is != PlaSide.ON_THE_LEFT)
               {
               // this line segment intersects curr_line between the
               // previous and the next corner of p_simplex

               if (prev_line_side_of_is == PlaSide.COLLINEAR)
                  {
                  // this line segment goes through the previous
                  // corner of p_simplex. Check, that the intersection
                  // isn't merely a touch.
                  PlaPoint prev_prev_corner;
                  if (edge_line_no == 0)
                     {
                     prev_prev_corner = p_shape.corner(edge_count - 1);
                     }
                  else
                     {
                     prev_prev_corner = p_shape.corner(edge_line_no - 1);
                     }

                  PlaPoint next_corner;
                  if (edge_line_no == edge_count - 1)
                     {
                     next_corner = p_shape.corner(0);
                     }
                  else
                     {
                     next_corner = p_shape.corner(edge_line_no + 1);
                     }
                  // check, that prev_prev_corner and next_corner
                  // are on different sides of this line segment.
                  PlaSide prev_prev_corner_side = middle.side_of(prev_prev_corner);
                  PlaSide next_corner_side = middle.side_of(next_corner);
                  if (prev_prev_corner_side == PlaSide.COLLINEAR || next_corner_side == PlaSide.COLLINEAR || prev_prev_corner_side == next_corner_side)
                     {
                     return empty_result;
                     }

                  }
               if (next_line_side_of_is == PlaSide.COLLINEAR)
                  {
                  // this line segment goes through the next
                  // corner of p_simplex. Check, that the intersection
                  // isn't merely a touch.
                  PlaPoint prev_corner = p_shape.corner(edge_line_no);
                  PlaPoint next_next_corner;

                  if (edge_line_no == edge_count - 2)
                     {
                     next_next_corner = p_shape.corner(0);
                     }
                  else if (edge_line_no == edge_count - 1)
                     {
                     next_next_corner = p_shape.corner(1);
                     }
                  else
                     {
                     next_next_corner = p_shape.corner(edge_line_no + 2);
                     }
                  // check, that prev_corner and next_next_corner
                  // are on different sides of this line segment.
                  PlaSide prev_corner_side = this.middle.side_of(prev_corner);
                  PlaSide next_next_corner_side = this.middle.side_of(next_next_corner);
                  if (prev_corner_side == PlaSide.COLLINEAR || next_next_corner_side == PlaSide.COLLINEAR || prev_corner_side == next_next_corner_side)
                     {
                     return empty_result;
                     }

                  }
               boolean intersection_already_handeled = false;
               for (int i = 0; i < intersection_count; ++i)
                  {
                  if (is.equals(intersection[i]))
                     {
                     intersection_already_handeled = true;
                     break;

                     }

                  }
               if (!intersection_already_handeled)
                  {
                  if (intersection_count < result.length)
                     {
                     // a new intersection is found
                     result[intersection_count] = edge_line_no;
                     intersection[intersection_count] = is;
                     ++intersection_count;
                     }
                  else
                     {
                     System.out.println("border_intersections: intersection_count to big!");
                     }
                  }
               }
            }

         prev_line = curr_line;
         curr_line = next_line;
         }

      if (intersection_count == 0)
         {
         return empty_result;
         }

      if (intersection_count == 2)
         {
         // assure the correct order
         PlaPointFloat is0 = intersection[0].to_float();
         PlaPointFloat is1 = intersection[1].to_float();
         PlaPointFloat curr_start = line_start.to_float();
         if (curr_start.distance_square(is1) < curr_start.distance_square(is0))
         // swap the result points
            {
            int tmp = result[0];
            result[0] = result[1];
            result[1] = tmp;
            }

         return result;
         }

      if (intersection_count != 1)
         {
         System.out.println("LineSegment.border_intersections: intersection_count 1 expected");
         }

      int[] normalised_result = new int[1];
      normalised_result[0] = result[0];
      return normalised_result;
      }

   /**
    * Inverts the direction of this.middle, if start_point() has a bigger x coordinate than end_point(), or an equal x coordinate
    * and a bigger y coordinate.
    */
   public PlaSegmentInt sort_endpoints_in_x_y()
      {
      boolean swap_endlines = (start_point().compare_x_y(end_point()) > 0);
      PlaSegmentInt result;

      if (swap_endlines)
         {
         result = new PlaSegmentInt(this.end, this.middle, this.start);
         result.precalculated_start_point = this.precalculated_end_point;
         result.precalculated_end_point = this.precalculated_start_point;
         }
      else
         {
         result = this;
         }

      return result;
      }
   }