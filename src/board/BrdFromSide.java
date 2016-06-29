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
 * Created on 17. August 2003, 07:36
 */

package board;

import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaSide;
import freert.planar.Polyline;
import freert.planar.ShapeTile;

/**
 * This is kind of a direction, basically p_no indicates a "direction starting from 0 to 
 * @author Alfons Wirtz
 */
public final class BrdFromSide
   {
   public static final BrdFromSide NOT_CALCULATED = new BrdFromSide(-1, null);

   public final int side_no;
   public final PlaPointFloat border_intersection;
   
   /**
    * Values already calculated. Just create an instance from them.
    */
   public BrdFromSide(int p_no, PlaPointFloat p_border_intersection)
      {
      side_no = p_no;
      border_intersection = p_border_intersection;
      }
   
   /**
    * calculates the number of the edge line of p_shape where p_polyline enters. 
    * Used in the push trace algorithm to determine the shove direction. 
    * p_no is expected between 1 and p_polyline.line_count - 2 inclusive.
    */
   BrdFromSide(Polyline p_polyline, int p_no, ShapeTile p_shape)
      {
      int fromside_no = -1;
      PlaPointFloat intersection = null;
      boolean border_intersection_found = false;
      
      // calculate the edge_no of p_shape, where p_polyline enters
      for (int curr_no = p_no; curr_no > 0; --curr_no)
         {
         PlaSegmentInt curr_seg = p_polyline.segment_get(curr_no);
         
         if ( curr_seg == null ) continue;
         
         int[] intersections = curr_seg.border_intersections(p_shape);
         
         if (intersections.length == 0) continue;

         fromside_no = intersections[0];

         intersection = curr_seg.get_line().intersection_approx(p_shape.border_line(fromside_no));
         
         if ( intersection.is_NaN() ) continue;

         border_intersection_found = true;
         break;
         }
      
      if ( ! border_intersection_found)
         {
         // The first corner of p_polyline is inside p_shape.
         // Calculate the nearest intersection point of p_polyline.arr[1] with the border of p_shape to the first corner of p_polyline
         PlaPointFloat from_point = p_polyline.corner_approx(0);
         PlaLineInt check_line = p_polyline.plaline(1);
         double min_dist = Double.MAX_VALUE;
         
         int edge_count = p_shape.border_line_count();

         for (int index = 0; index < edge_count; ++index)
            {
            PlaLineInt curr_line = p_shape.border_line(index);
            
            PlaPointFloat curr_intersection = check_line.intersection_approx(curr_line);
            
            if ( curr_intersection.is_NaN() ) continue;
            
            double curr_dist = Math.abs(curr_intersection.distance(from_point));
            
            if (curr_dist < min_dist)
               {
               fromside_no = index;
               intersection = curr_intersection;
               min_dist = curr_dist;
               }
            }
         }
      
      side_no = fromside_no;
      border_intersection = intersection;
      }

   /**
    * Calculates the nearest border side of p_shape to p_from_point. 
    * Used in the shove_drill_item algorithm to determine the shove direction.
    */
   public BrdFromSide(PlaPointInt p_from_point, ShapeTile p_shape)
      {
      PlaPoint border_projection = p_shape.nearest_border_point(p_from_point);
      
      side_no = p_shape.contains_on_border_line_no(border_projection);
      
      if ( side_no < 0)
         {
         System.out.println("CalcFromSide: side_no >= 0 expected");
         }
      
      border_intersection = border_projection.to_float();
      }

   /**
    * Calculates the Side of p_shape at the start of p_line_segment. 
    * If p_shove_to_the_left, the from_side_no is decremented by 2 else it is increased by 2.
    */
   public BrdFromSide(PlaSegmentInt p_line_segment, ShapeTile p_shape, boolean p_shove_to_the_left)
      {
      PlaPointFloat start_corner = p_line_segment.start_point_approx();
      PlaPointFloat end_corner = p_line_segment.end_point_approx();

      int border_line_count = p_shape.border_line_count();
      PlaLineInt check_line = p_line_segment.get_line();
      PlaPointFloat first_corner = p_shape.corner_approx(0);
      PlaSide prev_side = check_line.side_of(first_corner);
      int front_side_no = -1;

      for (int index = 1; index <= border_line_count; ++index)
         {
         PlaPointFloat next_corner;

         if (index == border_line_count)
            next_corner = first_corner;
         else
            next_corner = p_shape.corner_approx(index);
         
         PlaSide next_side = check_line.side_of(next_corner);
   
         if (prev_side != next_side)
            {
            PlaPointFloat curr_intersection = p_shape.border_line(index - 1).intersection_approx(check_line);
            
            if (! curr_intersection.is_NaN() && (curr_intersection.distance_square(start_corner) < curr_intersection.distance_square(end_corner)) )
               {
               front_side_no = index - 1;
               break;
               }
            }
         
         prev_side = next_side;
         }
      
      
      if (front_side_no < 0)
         {
         System.out.println("CalcFromSide: start corner not found");
         side_no = -1;
         border_intersection = null;
         return;
         }

      if (p_shove_to_the_left)
         {
         side_no = (front_side_no + 2) % border_line_count;
         }
      else
         {
         side_no = (front_side_no + border_line_count - 2) % border_line_count;
         }
      
      PlaPointFloat prev_corner = p_shape.corner_approx(side_no);
      PlaPointFloat next_corner = p_shape.corner_approx((side_no + 1) % border_line_count);
      border_intersection = prev_corner.middle_point(next_corner);
      }

   }
