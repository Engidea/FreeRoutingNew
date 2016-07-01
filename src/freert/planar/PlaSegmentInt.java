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

import java.util.ArrayList;



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
   private static final String classname="PlaSegmentInt.";

   private final PlaLineInt start;
   private final PlaLineInt middle;
   private final PlaLineInt end;
   
   private final PlaPoint start_point;
   private final PlaPoint end_point;
   
   private transient ShapeTileBox bounding_box;
   private transient ShapeTileOctagon bounding_octagon;
   
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
      
      start_point = middle.intersection(start, "should never happen");
      end_point   = middle.intersection(end, "should never happen");
      }

   public PlaSegmentInt(PlaPointInt p_from_corner, PlaPointInt p_to_corner)
      {
      if (p_from_corner.equals(p_to_corner))
         throw new IllegalArgumentException(classname+"C must contain at least 2 different points");

      PlaDirection dir = new PlaDirection(p_from_corner, p_to_corner);
      
      start  = new PlaLineInt(p_from_corner, dir.rotate_45_deg(2));
      middle = new PlaLineInt(p_from_corner, p_to_corner);
      end    = new PlaLineInt(p_to_corner, dir.rotate_45_deg(2));
      
      start_point = p_from_corner;
      end_point   = p_to_corner;
      }
   
   
   /**
    * Creates the p_no-th line segment of p_shape for p_no between 0 and p_shape.line_count - 1.
    */
   public PlaSegmentInt(ShapeSegments p_shape, int p_no)
      {
      int line_count = p_shape.border_line_count();
      
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
      
      start_point = middle.intersection(start, "should never happen");
      end_point = middle.intersection(end, "should never happen");
      }

   
   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
 
   
   /**
    * Returns the intersection of the first 2 lines of this segment
    */
   public PlaPoint start_point()
      {
      return start_point;
      }

   /**
    * Returns the intersection of the last 2 lines of this segment
    */
   public PlaPoint end_point()
      {
      return end_point;
      }

   /**
    * Returns an approximation of the intersection of the first 2 lines of this segment
    */
   public PlaPointFloat start_point_approx()
      {
      return start_point.to_float();
      }

   /**
    * Returns an approximation of the intersection of the last 2 lines of this segment
    */
   public PlaPointFloat end_point_approx()
      {
      return end_point.to_float();
      }

   /**
    * Returns the (infinite) line of this segment.
    */
   public PlaLineInt get_line()
      {
      return middle;
      }

   /**
    * Returns the line segment with the opposite direction.
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
      PlaLineIntAlist lines = new PlaLineIntAlist(3);
      lines.add( start );
      lines.add( middle );
      lines.add( end );
      return new Polyline(lines);
      }

   /**
    * Creates a 1 dimensional simplex rom this line segment, which has the same shape as the line sgment.
    */
   public ShapeTileSimplex to_simplex()
      {
      ArrayList<PlaLineInt> line_arr = new ArrayList<PlaLineInt>(4);
      
      if (end_point().side_of(start) == PlaSide.ON_THE_RIGHT)
         line_arr.add( start.opposite());
      else
         line_arr.add( start);
      
      line_arr.add( middle );
      line_arr.add( middle.opposite() );
      
      if (start_point().side_of(end) == PlaSide.ON_THE_RIGHT)
         line_arr.add( end.opposite() );
      else
         line_arr.add( end );
      
      return new ShapeTileSimplex(line_arr);
      }

   /**
    * Checks if p_point is contained in this line segment
    */
   public final  boolean contains(PlaPointInt p_point)
      {
      if ( p_point == null ) return false;
      
      if (middle.side_of(p_point) != PlaSide.COLLINEAR) return false;
      
      // create a perpendicular line at p_point and check, that the two
      // endpoints of this segment are on difcferent sides of that line.
      
      PlaDirection perpendicular_direction = middle.direction().rotate_45_deg(2);
      PlaLineInt perpendicular_line = new PlaLineInt(p_point, perpendicular_direction);
      PlaSide start_point_side = perpendicular_line.side_of(start_point());
      PlaSide end_point_side = perpendicular_line.side_of(end_point());
      
      if (start_point_side != PlaSide.COLLINEAR && end_point_side != PlaSide.COLLINEAR && start_point_side == end_point_side)
         {
         return false;
         }
      
      return true;
      }
   
   
   /**
    * calculates the smallest surrounding box of this line segmant
    */
   public ShapeTileBox bounding_box()
      {
      if ( bounding_box != null ) return bounding_box;
      
      PlaPointFloat start_corner = start_point_approx();
      PlaPointFloat end_corner   = end_point_approx();
      double llx = Math.min(start_corner.v_x, end_corner.v_x);
      double lly = Math.min(start_corner.v_y, end_corner.v_y);
      double urx = Math.max(start_corner.v_x, end_corner.v_x);
      double ury = Math.max(start_corner.v_y, end_corner.v_y);
      PlaPointInt lower_left = new PlaPointInt(Math.floor(llx), Math.floor(lly));
      PlaPointInt upper_right = new PlaPointInt(Math.ceil(urx), Math.ceil(ury));
      bounding_box = new ShapeTileBox(lower_left, upper_right);
      
      return bounding_box;
      }

   /**
    * calculates the smallest surrounding octagon of this line segmant
    */
   public ShapeTileOctagon bounding_octagon()
      {
      if ( bounding_octagon != null ) return bounding_octagon;
      
      PlaPointFloat start_corner = start_point_approx();
      PlaPointFloat end_corner = end_point_approx();
      double lx = Math.floor(Math.min(start_corner.v_x, end_corner.v_x));
      double ly = Math.floor(Math.min(start_corner.v_y, end_corner.v_y));
      double rx = Math.ceil(Math.max(start_corner.v_x, end_corner.v_x));
      double uy = Math.ceil(Math.max(start_corner.v_y, end_corner.v_y));
      double start_x_minus_y = start_corner.v_x - start_corner.v_y;
      double end_x_minus_y = end_corner.v_x - end_corner.v_y;
      double ulx = Math.floor(Math.min(start_x_minus_y, end_x_minus_y));
      double lrx = Math.ceil(Math.max(start_x_minus_y, end_x_minus_y));
      double start_x_plus_y = start_corner.v_x + start_corner.v_y;
      double end_x_plus_y = end_corner.v_x + end_corner.v_y;
      double llx = Math.floor(Math.min(start_x_plus_y, end_x_plus_y));
      double urx = Math.ceil(Math.max(start_x_plus_y, end_x_plus_y));
      ShapeTileOctagon result = new ShapeTileOctagon(
            lx, 
            ly, 
            rx, 
            uy, 
            ulx,
            lrx, 
            llx, 
            urx);
      bounding_octagon = result.normalize();
      
      return bounding_octagon;
      }

   /**
    * Creates a new line segment with the same start and middle line and an end line, so that the length of the new line segment is
    * about p_new_length.
    */
   public PlaSegmentInt change_length_approx(double p_new_length)
      {
/*      
      PlaPointFloat new_end_point = start_point_approx().change_length(end_point_approx(), p_new_length);
      PlaDirection perpendicular_direction = middle.direction().turn_45_degree(2);
      PlaLineInt new_end_line = new PlaLineInt(new_end_point.round(), perpendicular_direction);
      PlaSegmentInt result = new PlaSegmentInt(start, middle, new_end_line);
*/      
      
      System.err.println("testing, the original is the commented out");
      
      PlaPointFloat start_approx = start_point_approx();
      
      PlaPointInt start = start_point.round();

      PlaPointFloat new_end_point = start_approx.change_length(end_point_approx(), p_new_length);
      
      PlaPointInt end = new_end_point.round();

      return new PlaSegmentInt(start,end);
      }

   /**
    * Return a list of intersection points where the segments "touch"
    * The difficult part is when the two segments overlap...
    * @param p_other
    * @return
    */
   public ArrayList<PlaPointInt> intersection_points(PlaSegmentInt p_other)
      {
      ArrayList<PlaPointInt> risul = new ArrayList<PlaPointInt> (2);
      
      PlaPointInt myi_start = start_point.round();
      PlaPointInt myi_end = end_point.round();
      
      if ( ! middle.is_parallel(p_other.middle))
         {
         PlaPointFloat f_intersect = middle.intersection_approx(p_other.middle);
         if ( f_intersect.is_NaN() )
            {
            System.err.println("intersection_points: How did this happen ?");
            return risul;
            }
         
         PlaPointInt i_intersect = f_intersect.round();
         
         // if the intersect point is actually inside the segment I can actually add it
         if ( i_intersect.is_inside(myi_start, myi_end)) risul.add(i_intersect);
         
         return risul;
         }
      
      // now, segments are parallel... so I will be returing two points... which ones ?
      // remember that I am splitting this segment against another one... I should NOT go outside the boundary of this !!
      PlaPointInt oti_start = p_other.start_point.round();

      if ( oti_start.is_inside(myi_start, myi_end)) risul.add(oti_start);

      PlaPointInt oti_end = p_other.start_point.round();

      if ( oti_end.is_inside(myi_start, myi_end)) risul.add(oti_end);
      
      return risul;
      }

   
   
   /**
    * Looks up the intersections of this line segment with p_other. 
    * The result array may have length 0, 1 or 2. 
    * If the segments do not intersect the result array will have length 0. 
    * The result lines are so that the intersections of the result lines with this line segment will deliver the intersection points
    * If the segments overlap, the result array has length 2 and the intersection points are the first and the last overlap point. 
    * Otherwise the result array has length 1 and the intersection point is the the unique intersection or touching point. 
    * The result is not symmetric in this and p_other, because intersecting lines and not the intersection points are returned.
    */
   public ArrayList<PlaLineInt> intersection(PlaSegmentInt p_other)
      {
      ArrayList<PlaLineInt> risul = new ArrayList<PlaLineInt> (3);
      
      if ( ! bounding_box().intersects(p_other.bounding_box()))
         {
         // this is actually an empty risul
         return risul;
         }
      
      PlaSide start_point_side = start_point().side_of(p_other.middle);
      PlaSide end_point_side = end_point().side_of(p_other.middle);
      
      if (start_point_side == PlaSide.COLLINEAR && end_point_side == PlaSide.COLLINEAR)
         {
         // there may be an overlap
         PlaSegmentInt this_sorted = sort_endpoints_in_x_y();
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
            // end point of the left line is to the lsft of the start point of the right line. Empty
            return risul;
            }
         
         if (cmp == 0)
            {
            // end point of the left line is equal to the start point of the right line
            risul.add(left_line.end);
            return risul;
            }
         
         // now there is a real overlap
         risul.add ( right_line.start );

         if (right_line.end_point().compare_x_y(left_line.end_point()) >= 0)
            risul.add ( left_line.end );
         else
            risul.add ( right_line.end );

         return risul;
         }
      
      if (start_point_side == end_point_side || p_other.start_point().side_of(middle) == p_other.end_point().side_of(middle))
         {
         // no intersection possible
         return risul; 
         }

      // now both start points and both end points are on different sides of the middle line of the other segment.
      risul.add ( p_other.middle );

      return risul;
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
            // both endpoints are outside the border_line, no intersection possible
            return empty_result;
            }

         if (start_point_side == PlaSide.COLLINEAR)
            {
            // the start is on curr_line, check that the end point is inside
            // the halfplane, because touches count only, if the interiour is entered
            if (end_point_side != PlaSide.ON_THE_RIGHT) return empty_result;
            }

         if (end_point_side == PlaSide.COLLINEAR)
            {
            // the end is on curr_line, check that the start point is inside
            // the halfplane, because touches count only, if the interiour is entered
            if (start_point_side != PlaSide.ON_THE_RIGHT) return empty_result;
            }

         if (start_point_side != PlaSide.ON_THE_RIGHT || end_point_side != PlaSide.ON_THE_RIGHT)
            {
            // not both points are inside the halplane defined by curr_line
            PlaPoint is = middle.intersection(curr_line, "what does this do ?");
            PlaSide prev_line_side_of_is = prev_line.side_of(is);
            PlaSide next_line_side_of_is = next_line.side_of(is);

            if (prev_line_side_of_is != PlaSide.ON_THE_LEFT && next_line_side_of_is != PlaSide.ON_THE_LEFT)
               {
               // this line segment intersects curr_line between the previous and the next corner of p_simplex

               if (prev_line_side_of_is == PlaSide.COLLINEAR)
                  {
                  // this line segment goes through the previous corner of p_simplex. 
                  // Check, that the intersection isn't merely a touch.
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
                  
                  // check, that prev_prev_corner and next_corner are on different sides of this line segment.
                  PlaSide prev_prev_corner_side = middle.side_of(prev_prev_corner);
                  PlaSide next_corner_side = middle.side_of(next_corner);
                  if (prev_prev_corner_side == PlaSide.COLLINEAR || next_corner_side == PlaSide.COLLINEAR || prev_prev_corner_side == next_corner_side)
                     {
                     return empty_result;
                     }

                  }
               
               if (next_line_side_of_is == PlaSide.COLLINEAR)
                  {
                  // this line segment goes through the next corner of p_simplex. 
                  // Check, that the intersection isn't merely a touch.
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
                  
                  // check, that prev_corner and next_next_corner are on different sides of this line segment.
                  PlaSide prev_corner_side = middle.side_of(prev_corner);
                  PlaSide next_next_corner_side = middle.side_of(next_next_corner);
                  if (prev_corner_side == PlaSide.COLLINEAR || next_next_corner_side == PlaSide.COLLINEAR || prev_corner_side == next_next_corner_side)
                     {
                     return empty_result;
                     }
                  }

               boolean intersection_already_handeled = false;
               for (int index = 0; index < intersection_count; ++index)
                  {
                  if (is.equals(intersection[index]))
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
            {
            // swap the result points
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
      boolean swap_endlines = start_point().compare_x_y(end_point()) > 0;

      if (swap_endlines)
         return new PlaSegmentInt(end, middle, start);
      else
         return this;
      }
   }