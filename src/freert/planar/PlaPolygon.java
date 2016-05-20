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
 * A Polygon is a list of points in the plane, where no 2 consecutive points may be equal and no 3 consecutive points collinear.
 * That is kind of a polyline, but they do different things... still really, I wish we could use polyline or polygons, not both
 * @author Alfons Wirtz#
 */

public final class PlaPolygon implements java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;

   private final ArrayList<PlaPointInt> corners; 

   public PlaPolygon(PlaPointIntAlist p_point_list)
      {
      corners = new ArrayList<PlaPointInt>(p_point_list.size());

      for (PlaPointInt a_point : p_point_list )
         {
         // if this point is already in the list
         if ( has_point(a_point) ) continue;
         
         // if this point is "colinear" with some points in the list
         if ( has_colinear(a_point)) continue;
         
         corners.add(a_point);
         }
      }

   
   @Override
   public final boolean is_NaN ()
      {
      return false;
      }

   /**
    * Return true if the given point is colinear with two points in the list and should NOT be added
    * Now, the issue is that this point may be colinear (on the same line) but further away, so, it should be the one
    * being kept, not the one currently in the list....
    * @param a_point
    * @return
    */
   private boolean has_colinear (PlaPointInt a_point)
      {
      int count = corners.size();
      
      // I need at least two points in the corners for algorithm to work
      if ( count < 2 ) return false;
      
      for (int index=0; index<count-1; index++)
         {
         PlaPointInt start = corners.get(index);
         PlaPointInt end   = corners.get(index+1);
         
         // the given point is not on the same line as start end
         if (a_point.side_of(start, end) != PlaSide.COLLINEAR) continue;

         // use distance square instread of distance to avoid a square root calculation
         double d_start_p   = start.distance_square(a_point);
         double d_p_end     = a_point.distance_square(end);
         double d_start_end = start.distance_square(end);

         if ( d_start_end >= d_start_p )
            {
            if ( d_start_end >= d_p_end )
               {
               // simplest case, the new point is in the middle of start end
               return true; 
               }
            else
               {
               // new point is on the left of start point, close to it
               corners.set(index, a_point);
               return true;
               }
            }
         else
            {
            if ( d_start_end >= d_p_end )
               {
               // new point is on the right of end, close to it
               corners.set(index+1, a_point);
               return true;
               }
            else
               {
               // new point is on the left, far away
               corners.set(index, a_point);
               return true;
               }
            }
         }
      
      return false;
      }
   
   /**
    * @return true if there is already this exact point in the list
    */
   private boolean has_point (PlaPointInt a_point)
      {
      int count = corners.size();
      
      for (int index=0; index<count; index++)
         {
         PlaPointInt b_point = corners.get(index);
         
         if ( b_point.equals(a_point)) return true;
         }
      
      return false;
      }
   
   /**
    * @return the array of corners of this polygon
    */
   public PlaPointInt[] corner_array()
      {
      int corner_count = corners.size();
      
      PlaPointInt[] result = new PlaPointInt[corner_count];
      
      for (int index = 0; index < corner_count; ++index)
         {
         result[index] = corners.get(index);
         }
      
      return result;
      }

   public PlaPointInt corner ( int index )
      {
      return corners.get(index);
      }

   public int corner_size ()
      {
      return corners.size();
      }
   
   /**
    * Reverts the order of the corners of this polygon.
    */
   public PlaPolygon revert_corners()
      {
      int corner_count = corners.size();
      
      int from_idx = corner_count-1;
      
      PlaPointIntAlist reverse_corner_arr = new PlaPointIntAlist(corner_count);

      for (int index = 0; index < corner_count; ++index)
         reverse_corner_arr.add( corners.get(from_idx--) );
      
      return new PlaPolygon(reverse_corner_arr);
      }

   /**
    * Returns the winding number of this polygon, treated as closed. 
    * It will be > 0, if the corners are in countercock sense
    * < 0 if the corners are in clockwise sense.
    */
   public int winding_number_after_closing()
      {
      if (corner_size() < 2) return 0;

      PlaVectorInt first_side_vector = corner(1).difference_by(corner(0));
      
      PlaVectorInt prev_side_vector = first_side_vector;
      
      int corner_count = corner_size();
      // Skip the last corner, if it is equal to the first corner.
      if (corner(0).equals(corner(corner_count - 1)))
         {
         --corner_count;
         }
      
      double angle_sum = 0;
      
      for (int index = 1; index <= corner_count; ++index)
         {
         PlaVectorInt next_side_vector;
         if (index == corner_count - 1)
            {
            next_side_vector = corner(0).difference_by(corner(index));
            }
         
         else if (index == corner_count)
            {
            next_side_vector = first_side_vector;
            }
         else
            {
            next_side_vector = corner(index + 1).difference_by(corner(index));
            }
         
         angle_sum += prev_side_vector.angle_approx(next_side_vector);
         
         prev_side_vector = next_side_vector;
         }
      angle_sum /= 2.0 * Math.PI;
      
      if (Math.abs(angle_sum) < 0.5)
         {
         System.err.println("Polygon.winding_number_after_closing: winding number != 0 expected");
         }
      
      return (int) Math.round(angle_sum);
      }
   }