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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A Polyline is a sequence of lines, where no 2 consecutive lines may be parallel. 
 * A Polyline of n lines defines a Polygon of n-1 intersection points of consecutive lines. 
 * The lines of the objects of class Polyline are normally defined by points with integer coordinates, 
 * where the intersections of Lines can be represented in general only by infinite precision rational points.
 * We use polyline with integer coordinates instead of polygons with infinite precision rational coordinates because of 
 * better performance in geometric calculations.
 *
 * @author Alfons Wirtz
 */

public final class Polyline implements java.io.Serializable, PlaObject
   {
   private static final long serialVersionUID = 1L;
   private static final boolean USE_BOUNDING_OCTAGON_FOR_OFFSET_SHAPES = true;
   private static final String classname="Polyline.";

   // the array of lines of this Polyline.
   private final ArrayList<PlaLineInt> lines_arr;

   private transient PlaPointFloat[] precalculated_float_corners = null;
   private transient PlaPoint[]      precalculated_corners = null;
   private transient ShapeTileBox    precalculated_bounding_box = null;

   private  PlaPointInt corner_first;
   private  PlaPointInt corner_last;
   
   /**
    * creates a polyline of length p_polygon.corner_count + 1 from p_polygon, so that the i-th corner of p_polygon will be the
    * intersection of the i-th and the i+1-th lines of the new created p_polyline for 0 <= i < p_point_arr.length. 
    * p_polygon must have at least 2 corners
    */
   private Polyline(PlaPointIntAlist point_arr)
      {
      int initial_len = point_arr.size(); 
      
      if ( initial_len < 2)
         throw new IllegalArgumentException(classname+"A must contain at least 2 different points");

      ArrayList<PlaPointInt> corners_list = new ArrayList<PlaPointInt>(initial_len);
      
      for ( int index=0; index< initial_len; index++ )
         {
         PlaPointInt a_point = point_arr.get(index);
         
         if ( a_point == null ) continue;
         
         // if this point is already in the list
         if ( has_point(corners_list, a_point) ) continue;
         
         // if this point is "colinear" with some points in the list
         if ( has_colinear(corners_list, a_point)) continue;
         
         corners_list.add(a_point);
         }
      
      int input_len = corners_list.size(); 
      
      lines_arr = new ArrayList<PlaLineInt>(input_len + 1);

      corner_first = corners_list.get(0);
      
      // construct perpendicular lines at the start and at the end to represent
      PlaDirection dir = PlaDirection.get_instance(corner_first, corners_list.get(1));
      lines_arr.add(new PlaLineInt(corner_first, dir.turn_45_degree(2)) );

      for (int index = 1; index < input_len; ++index)
         lines_arr.add( new PlaLineInt(corners_list.get(index - 1), corners_list.get(index) ) );
      
      corner_last = corners_list.get(input_len - 1);
      
      // the first and the last point of point_arr as intersection of lines.
      dir = PlaDirection.get_instance(corner_last, corners_list.get(input_len - 2));
      lines_arr.add( new PlaLineInt(corner_last, dir.turn_45_degree(2) ) );
      
      precalculated_corners = new PlaPoint[corner_count()];
      precalculated_corners[0] = corner_first;
      precalculated_corners[corner_count()-1] = corner_last;
      }

   public Polyline(PlaPointInt[] p_points)
      {
      this(new PlaPointIntAlist(p_points));
      }

   private static boolean has_point (ArrayList<PlaPointInt> corners_list, PlaPointInt a_point)
      {
      for (PlaPointInt b_point : corners_list )
         {
         if ( b_point.equals(a_point)) return true;
         }
      
      return false;
      }
   
   private static boolean has_colinear (ArrayList<PlaPointInt> corners_list, PlaPointInt a_point)
      {
      int count = corners_list.size();
      
      // I need at least two points in the corners for algorithm to work
      if ( count < 2 ) return false;
      
      for (int index=0; index<count-1; index++)
         {
         PlaPointInt start = corners_list.get(index);
         PlaPointInt end   = corners_list.get(index+1);
         
         // the given point is not on the same line as start end
         if (a_point.side_of(start, end) != PlaSide.COLLINEAR) continue;

         double d_start_p   = start.distance(a_point);
         double d_p_end     = a_point.distance(end);
         double d_start_end = start.distance(end);

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
               corners_list.set(index, a_point);
               return true;
               }
            }
         else
            {
            if ( d_start_end >= d_p_end )
               {
               // new point is on the right of end, close to it
               corners_list.set(index+1, a_point);
               return true;
               }
            else
               {
               // new point is on the left, far away
               corners_list.set(index, a_point);
               return true;
               }
            }
         }
      
      return false;
      }
   
   
   /**
    * creates a polyline consisting of 3 lines
    */
   public Polyline(PlaPointInt p_from_corner, PlaPointInt p_to_corner)
      {
      if (p_from_corner.equals(p_to_corner))
         throw new IllegalArgumentException(classname+"C must contain at least 2 different points");
      
      corner_first = p_from_corner;
      
      lines_arr = new ArrayList<PlaLineInt>(3);
      PlaDirection dir = PlaDirection.get_instance(p_from_corner, p_to_corner);
      lines_arr.add( new PlaLineInt(p_from_corner, dir.turn_45_degree(2)) );
      lines_arr.add( new PlaLineInt(p_from_corner, p_to_corner) );
      dir = PlaDirection.get_instance(p_from_corner, p_to_corner);
      lines_arr.add( new PlaLineInt(p_to_corner, dir.turn_45_degree(2)) );
      
      precalculated_corners = new PlaPoint[corner_count()];
      precalculated_corners[0] = p_from_corner;
      precalculated_corners[1] = p_to_corner;
      }

   /**
    * Creates a polyline from an array of lines. 
    * Lines, which are parallel to the previous line are skipped. 
    * The directed lines are normalized, so that they intersect the previous line before the next line
    * Now, it happens that there is a request to create a polyline with wrong corners... however, the code then checks the validity
    * and it is not quite easy to wrap all of it in try catch
    */
   public Polyline(PlaLineInt[] p_line_arr)
      {
      int have_len = p_line_arr.length;
      
      lines_arr = new ArrayList<PlaLineInt>(have_len);
      
      if ( have_len < 3)
         {
         System.err.println(classname+"IntLine[] A < 3");
         return;
         }
      
      // this part will remove all lines that are colinear with the previous one
      PlaLineInt ref_line = p_line_arr[0];
      lines_arr.add(ref_line);
      
      for (int index = 1; index < have_len; ++index)
         {
         // skip a line that is parallel with the reference line
         if ( ref_line.is_parallel(p_line_arr[index])) continue;
         
         ref_line = p_line_arr[index];
         
         lines_arr.add(ref_line);
         }

      if (plalinelen() < 3)
         {
         System.err.println(classname+"IntLine[] B < 3");
         return;
         }

      complete_constructor();
      }

   public Polyline(ArrayList<PlaLineInt> p_line_arr)
      {
      int have_len = p_line_arr.size();
      
      lines_arr = new ArrayList<PlaLineInt>(have_len);
      
      if ( have_len < 3)
         {
         System.err.println(classname+"IntLine<> A < 3");
         return;
         }
      
      // this part will remove all lines that are colinear with the previous one
      PlaLineInt ref_line = p_line_arr.get(0);
      lines_arr.add(ref_line);
      
      for (int index = 1; index < have_len; ++index)
         {
         PlaLineInt a_line = p_line_arr.get(index);
         
         // skip a line that is parallel with the reference line
         if ( ref_line.is_parallel(a_line) ) continue;
         
         ref_line = a_line;
         
         lines_arr.add(ref_line);
         }

      if (plalinelen() < 3)
         {
         System.err.println(classname+"IntLine<> B < 3");
         return;
         }

      complete_constructor();
      }

   private void complete_constructor()
      {
      // this will calculate the float corners
      corner_approx_arr();
      
      adjust_direction();
      
      precalculated_corners = new PlaPoint[corner_count()];
      
      precalculated_corners[0] = plaline(0).intersection(plaline(1), "should never happen");
      }
   
   
   
   private void adjust_direction ()
      {
      // turn  the direction of the lines that they point always from the previous corner to the next corner
      // Now, why is not the first and last line checked ? first should point to first and last should point away, no ?
      for (int index = 1; index < plalinelen(-1); index++)
         {
         PlaLineInt pre_l = plaline(index-1);
         PlaLineInt cur_l = plaline(index);
         
         PlaSide side_of_line = pre_l.side_of(precalculated_float_corners[index]);
   
         if (side_of_line == PlaSide.COLLINEAR) continue;
   
         PlaDirection d0 = pre_l.direction();
         PlaDirection d1 = cur_l.direction();
   
         PlaSide side1 = d0.side_of(d1);
      
         if (side1 == side_of_line) continue;
         
         lines_arr.set(index, cur_l.opposite() );
         }
      }
   
   
   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
   
   /**
    * On construction polyline should check that given lines are non colinear
    * @return true if there are more or equal than three lines in a polyline
    */
   public boolean is_valid ()
      {
      return plalinelen() >= 3;
      }

   /**
    * Returns the number of lines minus 1
    * This MUST use the base datastructure since it is used to build the actualcorners
    */
   public int corner_count()
      {
      return plalinelen() - 1;
      }

   /**
    * Checks is any corner of the polyline goes back to the first point
    * It does happen if you are not careful while splitting, should not happen, really...
    */
   private boolean has_corner_loopt()
      {
      PlaPoint first_corner = corner_first();

      for (int index = 1; index < corner_count(); ++index)
         {
         if ( corner(index).equals(first_corner) ) continue;

         return false;
         }
      
      return true;
      }

   /**
    * checks, if all lines of this polyline are orthogonal
    */
   public boolean is_orthogonal()
      {
      int lmax = plalinelen();
      
      for (int index = 0; index < lmax; ++index)
         {
         if ( plaline(index).is_orthogonal() ) continue;

         return false;
         }
      
      return true;
      }

   /**
    * checks, if all lines of this polyline are multiples of 45 degree
    */
   public boolean is_multiple_of_45_degree()
      {
      int lmax = plalinelen();

      for (int index = 0; index < lmax; ++index)
         {
         if ( plaline(index).is_multiple_of_45_degree()) continue;

         return false;
         }

      return true;
      }

   /**
    * returns the intersection of the first line with the second line
    */
   public PlaPoint corner_first()
      {
      PlaPoint a_point = corner(0);
      
      //if ( a_point.is_rational() )
      //   System.err.println(classname+"corner_first: RATIONAL");
      
      return a_point;
      }

   /**
    * Equivalente to cornet(1)
    * @return the corner after the first one
    */
   public PlaPoint corner_first_next()
      {
      return corner(1);
      }

   
   /**
    * @return the intersection of the last line with the line before the last line
    */
   public PlaPoint corner_last()
      {
      // corner index go from 0 to n-1 to indicate n corners 
      return corner(corner_count() - 1);
      }

   /**
    * Equivalent to corner(corner_count() - 2)
    * @return the corner before the last one
    */
   public PlaPoint corner_last_prev()
      {
      // corner index go from 0 to n-1 to indicate n corners 
      return corner(corner_count() - 2);
      }

   /**
    * returns the array of the intersection of two consecutive lines approximated by FloatPoint's.
    */
   public PlaPointFloat[] corner_approx_arr()
      {
      int corner_max = corner_count();
      
      if (precalculated_float_corners == null)
         {
         // corner array is not yet allocated
         precalculated_float_corners = new PlaPointFloat[corner_max];
         }

      for (int index = 0; index < corner_max; ++index)
         {
         if (precalculated_float_corners[index] != null) continue;
         
         PlaLineInt cur_l = plaline(index);
         PlaLineInt nxt_l = plaline(index+1);
         
         precalculated_float_corners[index] = cur_l.intersection_approx(nxt_l);
         }
      
      return precalculated_float_corners;
      }

   /**
    * Returns an approximation of the intersection of the p_no-th with the (p_no - 1)-th line by a FloatPoint.
    */
   public PlaPointFloat corner_approx(int p_no)
      {
      int corners_count = corner_count();
      
      if (p_no < 0)
         {
         System.err.println(classname+"corner_approx: p_no is < 0");
         p_no = 0;
         }
      else if (p_no >= corners_count )
         {
         System.err.println(classname+"corner_approx: p_no must be less than arr.length - 1");
         p_no = corners_count - 1;
         }
      
      if (precalculated_float_corners == null)
         {
         // corner array is not yet allocated
         precalculated_float_corners = new PlaPointFloat[corners_count];
         }

      if (precalculated_float_corners[p_no] == null)
         {
         // corner is not yet calculated
         precalculated_float_corners[p_no] = plaline(p_no).intersection_approx(plaline(p_no + 1));
         }

      return precalculated_float_corners[p_no];
      }

   /**
    * Returns the intersection of the p_no-th with the (p_no - 1)-th edge line.
    */
   public PlaPoint corner(int p_no)
      {
      int corners_count = corner_count();
      
      if (p_no < 0)
         {
         System.err.println(classname+"corner: p_no is < 0 adjusted to 0");
         p_no = 0;
         }
      else if (p_no >= corners_count)
         {
         System.out.println(classname+"corner: p_no must be less than arr.length - 1");
         p_no = corners_count - 1;
         }
      
      if (precalculated_corners == null)
         {
         // corner array is not yet allocated
         precalculated_corners = new PlaPoint[corners_count];
         }

      if (precalculated_corners[p_no] != null) return precalculated_corners[p_no];

      // calculate the new corner to this polyline, make sure that first and last are integers
      precalculated_corners[p_no] = plaline(p_no).intersection(plaline(p_no + 1), "should never happen");
      
/* Ahhh, not yet, it is not possible, yet to round first and last point to int points, not yet....     
      if ( p_no == 0 ) 
         precalculated_corners[p_no] = precalculated_corners[p_no].round();
      else if ( p_no == corners_count -1 )
         precalculated_corners[p_no] = precalculated_corners[p_no].round();
*/
      
      return precalculated_corners[p_no];
      }

   /**
    * return the polyline with the reversed order of lines
    */
   public Polyline reverse()
      {
      int lines_max = plalinelen();
            
      PlaLineInt[] reversed_lines = new PlaLineInt[lines_max];
      
      int index_down = lines_max-1;
      
      for (int index = 0; index < lines_max; ++index)
         {
         reversed_lines[index] = plaline(index_down--).opposite();
         }
      
      return new Polyline(reversed_lines);
      }

   /**
    * Calculates the length of this polyline from p_from_corner to p_to_corner.
    */
   public double length_approx(int p_from_corner, int p_to_corner)
      {
      int from_corner = Math.max(p_from_corner, 0);
      int to_corner = Math.min(p_to_corner, plalinelen(-2));
      double result = 0;
      for (int iindex = from_corner; iindex < to_corner; ++iindex)
         {
         result += corner_approx(iindex + 1).distance(corner_approx(iindex));
         }
      return result;
      }

   /**
    * Calculates the cumulative distance between consecutive corners of this polyline.
    */
   public double length_approx()
      {
      return length_approx(0, plalinelen(-2));
      }

   /**
    * calculates for each line a shape around this line where the right and left edge lines have the distance p_half_width from the
    * center line Returns an array of convex shapes of length line_count - 2
    */
   public ArrayList<ShapeTile> offset_shapes(int p_half_width)
      {
      return offset_shapes(p_half_width, 0, plalinelen(-1));
      }

   /**
    * calculates for each line between p_from_no and p_to_no a shape around this line, where the right and left edge lines have the
    * distance p_half_width from the center line
    */
   public ArrayList<ShapeTile> offset_shapes(int p_half_width, int p_from_no, int p_to_no)
      {
      int from_no = Math.max(p_from_no, 0);
      int to_no = Math.min(p_to_no, plalinelen(-1));
      
      int shape_count = Math.max(to_no - from_no - 1, 0);
      
      ArrayList<ShapeTile> shape_arr = new ArrayList<ShapeTile>(shape_count);
      
      if (shape_count == 0) return shape_arr;
      
      PlaDirection prev_dir = plaline(from_no).direction();
      PlaDirection curr_dir = plaline(from_no + 1).direction();
      
      for (int index = from_no + 1; index < to_no; ++index)
         {
         PlaDirection next_dir = plaline(index + 1).direction();

         PlaLineInt[] lines = new PlaLineInt[4];

         lines[0] = plaline(index).translate(-p_half_width);
         // current center line translated to the right

         // create the front line of the offset shape
         PlaSide next_dir_from_curr_dir = next_dir.side_of(curr_dir);
         // left turn from curr_line to next_line
         if (next_dir_from_curr_dir == PlaSide.ON_THE_LEFT)
            {
            lines[1] = plaline(index + 1).translate(-p_half_width);
            // next right line
            }
         else
            {
            lines[1] = plaline(index + 1).opposite().translate(-p_half_width);
            // next left line in opposite direction
            }

         lines[2] = plaline(index).opposite().translate(-p_half_width);
         // current left line in opposite direction

         // create the back line of the offset shape
         PlaSide curr_dir_from_prev_dir = curr_dir.side_of(prev_dir);
         // left turn from prev_line to curr_line
         if (curr_dir_from_prev_dir == PlaSide.ON_THE_LEFT)
            {
            lines[3] = plaline(index - 1).translate(-p_half_width);
            // previous line translated to the right
            }
         else
            {
            lines[3] = plaline(index - 1).opposite().translate(-p_half_width);
            // previous left line in opposite direction
            }
         // cut off outstanding corners with following shapes
         PlaPointFloat corner_to_check = null;
         PlaLineInt curr_line = lines[1];
         PlaLineInt check_line = null;
         if (next_dir_from_curr_dir == PlaSide.ON_THE_LEFT)
            {
            check_line = lines[2];
            }
         else
            {
            check_line = lines[0];
            }
         PlaPointFloat check_distance_corner = corner_approx(index);
         final double check_dist_square = 2.0 * p_half_width * p_half_width;
         Collection<PlaLineInt> cut_dog_ear_lines = new LinkedList<PlaLineInt>();
         PlaDirection tmp_curr_dir = next_dir;
         boolean direction_changed = false;
         
         for (int jndex = index + 2; jndex < plalinelen(-1); ++jndex)
            {
            if (corner_approx(jndex - 1).length_square(check_distance_corner) > check_dist_square)
               {
               break;
               }
            if (!direction_changed)
               {
               corner_to_check = curr_line.intersection_approx(check_line);
               }
            PlaDirection tmp_next_dir = plaline(jndex).direction();
            PlaLineInt next_border_line = null;
            PlaSide tmp_next_dir_from_tmp_curr_dir = tmp_next_dir.side_of(tmp_curr_dir);
            direction_changed = tmp_next_dir_from_tmp_curr_dir != next_dir_from_curr_dir;
            if (!direction_changed)
               {
               if (tmp_next_dir_from_tmp_curr_dir == PlaSide.ON_THE_LEFT)
                  {
                  next_border_line = plaline(jndex).translate(-p_half_width);
                  }
               else
                  {
                  next_border_line = plaline(jndex).opposite().translate(-p_half_width);
                  }

               if (next_border_line.side_of(corner_to_check) == PlaSide.ON_THE_LEFT && next_border_line.side_of(corner(index)) == PlaSide.ON_THE_RIGHT
                     && next_border_line.side_of(corner(index - 1)) == PlaSide.ON_THE_RIGHT)
               // an outstanding corner
                  {
                  cut_dog_ear_lines.add(next_border_line);
                  }
               tmp_curr_dir = tmp_next_dir;
               curr_line = next_border_line;
               }
            }
         // cut off outstanding corners with previous shapes
         check_distance_corner = corner_approx(index - 1);
         if (curr_dir_from_prev_dir == PlaSide.ON_THE_LEFT)
            {
            check_line = lines[2];
            }
         else
            {
            check_line = lines[0];
            }
         curr_line = lines[3];
         tmp_curr_dir = prev_dir;
         direction_changed = false;
         for (int jndex = index - 2; jndex >= 1; --jndex)
            {
            if (corner_approx(jndex).length_square(check_distance_corner) > check_dist_square)
               {
               break;
               }
            if (!direction_changed)
               {
               corner_to_check = curr_line.intersection_approx(check_line);
               }
            PlaDirection tmp_prev_dir = plaline(jndex).direction();
            PlaLineInt prev_border_line = null;
            PlaSide tmp_curr_dir_from_tmp_prev_dir = tmp_curr_dir.side_of(tmp_prev_dir);
            direction_changed = tmp_curr_dir_from_tmp_prev_dir != curr_dir_from_prev_dir;
            if (!direction_changed)
               {
               if (tmp_curr_dir.side_of(tmp_prev_dir) == PlaSide.ON_THE_LEFT)
                  {
                  prev_border_line = plaline(jndex).translate(-p_half_width);
                  }
               else
                  {
                  prev_border_line = plaline(jndex).opposite().translate(-p_half_width);
                  }
               if (prev_border_line.side_of(corner_to_check) == PlaSide.ON_THE_LEFT && prev_border_line.side_of(corner(index)) == PlaSide.ON_THE_RIGHT
                     && prev_border_line.side_of(corner(index - 1)) == PlaSide.ON_THE_RIGHT)
               // an outstanding corner
                  {
                  cut_dog_ear_lines.add(prev_border_line);
                  }
               tmp_curr_dir = tmp_prev_dir;
               curr_line = prev_border_line;
               }
            }
         ShapeTile s1 = ShapeTile.get_instance(lines);
         int cut_line_count = cut_dog_ear_lines.size();
         if (cut_line_count > 0)
            {
            PlaLineInt[] cut_lines = new PlaLineInt[cut_line_count];
            Iterator<PlaLineInt> it = cut_dog_ear_lines.iterator();
            for (int j = 0; j < cut_line_count; ++j)
               {
               cut_lines[j] = it.next();
               }
            s1 = s1.intersection(ShapeTile.get_instance(cut_lines));
            }

         ShapeTile bounding_shape;

         if (USE_BOUNDING_OCTAGON_FOR_OFFSET_SHAPES)
            {
            // intersect with the bounding octagon
            ShapeTileOctagon surr_oct = bounding_octagon(index - 1, index);
            bounding_shape = surr_oct.offset(p_half_width);
            }
         else
            {
            // intersect with the bounding box
            ShapeTileBox surr_box = bounding_box(index - 1, index);
            ShapeTileBox offset_box = surr_box.offset(p_half_width);
            bounding_shape = offset_box.to_Simplex();
            }
      
         ShapeTile a_risul = bounding_shape.intersection_with_simplify(s1);
         
         if ( a_risul.is_empty() )
            System.err.println(classname+"offset_shapes: shape is empty");
         else
            shape_arr.add(a_risul);
         
         prev_dir = curr_dir;
         curr_dir = next_dir;
         }

      return shape_arr;
      }

   /**
    * Calculates for the p_no-th line segment a shape around this line where the right and left edge lines have the distance
    * p_half_width from the center line. 0 <= p_no <= arr.length - 3
    */
   public ShapeTile offset_shape(int p_half_width, int p_no)
      {
      if (p_no < 0 || p_no > plalinelen(-3) )
         {
         System.out.println("Polyline.offset_shape: p_no out of range");
         return null;
         }
      
      ArrayList<ShapeTile> result = offset_shapes(p_half_width, p_no, p_no + 2);

      return result.get(0);
      }

   /**
    * Calculates for the p_no-th line segment a box shape around this line 
    * where the border lines have the distance p_half_width
    * from the center line. 0 <= p_no <= arr.length - 3
    */
   public ShapeTileBox offset_box(int p_half_width, int p_no)
      {
      PlaSegmentInt curr_line_segment = segment_get(p_no + 1);
      
      ShapeTileBox result = curr_line_segment.bounding_box().offset(p_half_width);
      
      return result;
      }

   /**
    * Returns the by p_vector translated polyline pippo
    */
   public Polyline translate_by(PlaVectorInt p_vector)
      {
      if (p_vector.equals(PlaVectorInt.ZERO)) return this;
      
      PlaLineInt[] new_arr = new PlaLineInt[plalinelen()];
      
      for (int index = 0; index < new_arr.length; ++index)
         {
         new_arr[index] = plaline(index).translate_by(p_vector);
         }
      
      return new Polyline(new_arr);
      }

   /**
    * Returns the polyline turned by p_factor times 90 degree around p_pole.
    */
   public Polyline turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[plalinelen()];
      for (int index = 0; index < new_arr.length; ++index)
         {
         new_arr[index] = plaline(index).turn_90_degree(p_factor, p_pole);
         }
      
      return new Polyline(new_arr);
      }

   public Polyline rotate_approx(double p_angle, PlaPointFloat p_pole)
      {
      if (p_angle == 0) return this;

      PlaPointInt[] new_corners = new PlaPointInt[corner_count()];
      for (int i = 0; i < new_corners.length; ++i)
         {
         new_corners[i] = corner_approx(i).rotate(p_angle, p_pole).round();
         }

      return new Polyline(new_corners);
      }

   /** 
    * Mirrors this polyline at the vertical line through p_pole 
    */
   public Polyline mirror_vertical(PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[plalinelen()];
    
      for (int index = 0; index < new_arr.length; ++index)
         {
         new_arr[index] = plaline(index).mirror_vertical(p_pole);
         }
      return new Polyline(new_arr);
      }

   /** 
    * Mirrors this polyline at the horizontal line through p_pole 
    */
   public Polyline mirror_horizontal(PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[plalinelen()];
      for (int index = 0; index < new_arr.length; ++index)
         {
         new_arr[index] = plaline(index).mirror_horizontal(p_pole);
         }
      return new Polyline(new_arr);
      }

   /**
    * Returns the smallest box containing the intersection points from index p_from_corner_no to index p_to_corner_no of the lines
    * of this polyline
    */
   public ShapeTileBox bounding_box(int p_from_corner_no, int p_to_corner_no)
      {
      int from_corner_no = Math.max(p_from_corner_no, 0);
      int to_corner_no = Math.min(p_to_corner_no, plalinelen(-2));
      double llx = Integer.MAX_VALUE;
      double lly = llx;
      double urx = Integer.MIN_VALUE;
      double ury = urx;
      for (int i = from_corner_no; i <= to_corner_no; ++i)
         {
         PlaPointFloat curr_corner = corner_approx(i);
         llx = Math.min(llx, curr_corner.v_x);
         lly = Math.min(lly, curr_corner.v_y);
         urx = Math.max(urx, curr_corner.v_x);
         ury = Math.max(ury, curr_corner.v_y);
         }
      PlaPointInt lower_left = new PlaPointInt(Math.floor(llx), Math.floor(lly));
      PlaPointInt upper_right = new PlaPointInt(Math.ceil(urx), Math.ceil(ury));
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * Returns the smallest box containing the intersection points of the lines of this polyline
    */
   public ShapeTileBox bounding_box()
      {
      if (precalculated_bounding_box == null)
         {
         precalculated_bounding_box = bounding_box(0, corner_count() - 1);
         }
      return precalculated_bounding_box;
      }

   /**
    * Returns the smallest octagon containing the intersection points from index p_from_corner_no to index p_to_corner_no of the
    * lines of this polyline
    */
   public ShapeTileOctagon bounding_octagon(int p_from_corner_no, int p_to_corner_no)
      {
      int from_corner_no = Math.max(p_from_corner_no, 0);
      int to_corner_no = Math.min(p_to_corner_no, plalinelen(-2));
      double lx = Integer.MAX_VALUE;
      double ly = Integer.MAX_VALUE;
      double rx = Integer.MIN_VALUE;
      double uy = Integer.MIN_VALUE;
      double ulx = Integer.MAX_VALUE;
      double lrx = Integer.MIN_VALUE;
      double llx = Integer.MAX_VALUE;
      double urx = Integer.MIN_VALUE;
      for (int index = from_corner_no; index <= to_corner_no; ++index)
         {
         PlaPointFloat curr = corner_approx(index);
         lx = Math.min(lx, curr.v_x);
         ly = Math.min(ly, curr.v_y);
         rx = Math.max(rx, curr.v_x);
         uy = Math.max(uy, curr.v_y);
         double tmp = curr.v_x - curr.v_y;
         ulx = Math.min(ulx, tmp);
         lrx = Math.max(lrx, tmp);
         tmp = curr.v_x + curr.v_y;
         llx = Math.min(llx, tmp);
         urx = Math.max(urx, tmp);
         }
      ShapeTileOctagon surrounding_octagon = new ShapeTileOctagon(
            Math.floor(lx), 
            Math.floor(ly), 
            Math.ceil(rx), 
            Math.ceil(uy), 
            Math.floor(ulx), 
            Math.ceil(lrx),
            Math.floor(llx), 
            Math.ceil(urx));
      return surrounding_octagon;
      }

   /**
    * Calculates an aproximation of the nearest point on this polyline to p_from_point.
    */
   public PlaPointFloat nearest_point_approx(PlaPointFloat p_from_point)
      {
      double min_distance = Double.MAX_VALUE;
      PlaPointFloat nearest_point = null;
      // calculate the nearest corner point
      PlaPointFloat[] corners = corner_approx_arr();
      for (int i = 0; i < corners.length; ++i)
         {
         double curr_distance = corners[i].distance(p_from_point);
         if (curr_distance < min_distance)
            {
            min_distance = curr_distance;
            nearest_point = corners[i];
            }
         }
      final double c_tolerance = 1;
      for (int index = 1; index < plalinelen(-1); ++index)
         {
         PlaPointFloat projection = p_from_point.projection_approx(plaline(index));
         double curr_distance = projection.distance(p_from_point);
         if (curr_distance < min_distance)
            {
            // look, if the projection is inside the segment
            double segment_length = corners[index].distance(corners[index - 1]);
            if (projection.distance(corners[index]) + projection.distance(corners[index - 1]) < segment_length + c_tolerance)
               {
               min_distance = curr_distance;
               nearest_point = projection;
               }
            }
         }
      return nearest_point;
      }

   /**
    * Calculates the distance of p_from_point to the the nearest point on this polyline
    */
   public double distance(PlaPointFloat p_from_point)
      {
      double result = p_from_point.distance(nearest_point_approx(p_from_point));
      return result;
      }

   /**
    * Combines the two polylines, if they have a common end corner. 
    * The order of lines in this polyline will be preserved. 
    * @returns the combined polyline or this polyline, if this polyline and p_other have no common end corner. 
    * If there is something to combine at the start of this polyline, p_other is inserted in front of this polyline. 
    * If there is something to combine at the end of this polyline, this polyline is inserted in front of p_other.
    */
   public Polyline combine(Polyline p_other)
      {
      if ( p_other == null ) return this;
      
      if ( plalinelen() < 3 )
         throw new IllegalArgumentException(classname+"what A");
      
      if ( p_other.plalinelen() < 3)
         throw new IllegalArgumentException(classname+"what B");
      
      boolean combine_at_start;
      boolean combine_other_at_start;
      
      if (corner_first().equals(p_other.corner_first()))
         {
         combine_at_start = true;
         combine_other_at_start = true;
         }
      else if (corner_first().equals(p_other.corner_last()))
         {
         combine_at_start = true;
         combine_other_at_start = false;
         }
      else if (corner_last().equals(p_other.corner_first()))
         {
         combine_at_start = false;
         combine_other_at_start = true;
         }
      else if (corner_last().equals(p_other.corner_last()))
         {
         combine_at_start = false;
         combine_other_at_start = false;
         }
      else
         {
         return this; // no common end point
         }
      
      ArrayList<PlaLineInt> lines_list = new ArrayList<PlaLineInt>(plalinelen() + p_other.plalinelen());
      
      if (combine_at_start)
         {
         // insert the lines of p_other in front
         if (combine_other_at_start)
            {
            // insert in reverse order, skip the first line of p_other
            for (int index = 0; index < p_other.plalinelen(-1); ++index)
               {
               lines_list.add( p_other.plaline(p_other.plalinelen() - index - 1).opposite());
               }
            }
         else
            {
            // skip the last line of p_other
            for (int index = 0; index < p_other.plalinelen(-1); ++index)
               {
               lines_list.add(p_other.plaline(index));
               }
            }
         
         // append the lines of this polyline, skip the first line
         for (int iindex = 1; iindex < plalinelen(); ++iindex)
            {
            lines_list.add( plaline(iindex));
            }
         }
      else
         {
         // insert the lines of this polyline in front, skip the last line
         for (int index = 0; index < plalinelen(-1); ++index)
            {
            lines_list.add( plaline(index));
            }
         
         if (combine_other_at_start)
            {
            // skip the first line of p_other
            for (int index = 1; index < p_other.plalinelen(); ++index)
               {
               lines_list.add( p_other.plaline(index));
               }
            }
         else
            {
            // insert in reverse order, skip the last line of p_other
            for (int index = 1; index < p_other.plalinelen(); ++index)
               {
               lines_list.add( p_other.plaline(p_other.plalinelen() - index - 1).opposite());
               }
            }
         }
      
      return new Polyline(lines_list);
      }

   /**
    * Splits this polyline at the line with number p_line_no into two 
    * by inserting p_endline as concluding line of the first split piece and as the start line of the second split piece. 
    * p_endline and the line with number p_line_no must not be parallel. 
    * The order of the lines ins the two result pieces is preserved. 
    * p_line_no must be bigger than 0 and less then arr.length - 1.
    * Damiano ok, let me try to split ending into an int point, since the "old" trace will be deleted...
    * @return null, if nothing was split
    */
   public Polyline[] split(int p_line_no, PlaLineInt p_end_line)
      {
      if (p_line_no < 1 || p_line_no > plalinelen(-2) )
         {
         System.out.println("Polyline.split: p_line_no out of range");
         return null;
         }
      
      if (plaline(p_line_no).is_parallel(p_end_line)) return null;
      
      PlaPointFloat a_corner = plaline(p_line_no).intersection_approx(p_end_line);
      
      if ( a_corner.is_NaN() ) return null;
      
      // Yes, it happens a lot that the end corner is rational, however, test shows that it is ok to round it
      // to an int point since it actually must fall into a drawable board
      PlaPointInt new_end_corner = a_corner.round();

      // No split, if p_end_line does not intersect, but touches only tnis Polyline at an end point.
      if (p_line_no == 1 && new_end_corner.equals(corner_first()) ) return null;
      
      if ( p_line_no == plalinelen(-2) && new_end_corner.equals(corner_last()) ) return null;
      
      PlaLineInt[] first_piece;
      if (corner(p_line_no - 1).equals(new_end_corner))
         {
         // skip line segment of length 0 at the end of the first piece
         first_piece = new PlaLineInt[p_line_no + 1];
         plaline_copy(0, first_piece, 0, first_piece.length);
         }
      else
         {
         first_piece = new PlaLineInt[p_line_no + 2];
         plaline_copy(0, first_piece, 0, p_line_no + 1);
         first_piece[p_line_no + 1] = p_end_line;
         }
      
      PlaLineInt[] second_piece;
      if (corner(p_line_no).equals(new_end_corner))
         {
         // skip line segment of length 0 at the beginning of the second piece
         second_piece = new PlaLineInt[plalinelen() - p_line_no];
         plaline_copy(p_line_no, second_piece, 0, second_piece.length);
         }
      else
         {
         second_piece = new PlaLineInt[plalinelen() - p_line_no + 1];
         second_piece[0] = p_end_line;
         plaline_copy(p_line_no, second_piece, 1, second_piece.length - 1);
         }
      
      Polyline[] result = new Polyline[2];
      result[0] = new Polyline(first_piece);

      if (result[0].has_corner_loopt() ) return null;
      
      result[1] = new Polyline(second_piece);
      
      if ( result[1].has_corner_loopt() )  return null;

      return result;
      }

   /**
    * create a new Polyline by skipping the lines of this Polyline from p_from_no to p_to_no
    */
   public Polyline skip_lines(int p_from_no, int p_to_no)
      {
      if (p_from_no < 0 || p_to_no > plalinelen(-1) || p_from_no > p_to_no)
         {
         return this;
         }
      
      PlaLineInt[] new_lines = new PlaLineInt[plalinelen() - (p_to_no - p_from_no + 1)];
      plaline_copy(0, new_lines, 0, p_from_no);
      plaline_copy(p_to_no + 1, new_lines, p_from_no, new_lines.length - p_from_no);
      return new Polyline(new_lines);
      }

   public boolean contains(PlaPointInt p_point)
      {
      for (int index = 1; index < plalinelen(-1); ++index)
         {
         PlaSegmentInt curr_segment = segment_get(index);
         
         if (curr_segment.contains(p_point)) return true;
         }
      
      return false;
      }

   /**
    * Creates a perpendicular line segment from p_from_point onto the nearest line segment of this polyline to p_from_side. 
    * Returns null, if the perpendicular line does not intersect the neares line segment inside its segment bounds or if p_from_point is
    * contained in this polyline.
    */
   public PlaSegmentInt projection_line(PlaPointInt p_from_point)
      {
      if ( p_from_point == null ) return null;
      
      PlaPointFloat from_point = p_from_point.to_float();
      double min_distance = Double.MAX_VALUE;
      PlaLineInt result_line = null;
      PlaLineInt nearest_line = null;
      
      for (int index = 1; index < plalinelen(-1); ++index)
         {
         PlaPointFloat projection = from_point.projection_approx(plaline(index));
         double curr_distance = projection.distance(from_point);
         
         if (curr_distance >= min_distance) continue;

         PlaDirection direction_towards_line = plaline(index).perpendicular_direction(p_from_point);
        
         if (direction_towards_line == null) continue;

         PlaLineInt curr_result_line = new PlaLineInt(p_from_point, direction_towards_line);
         PlaPoint prev_corner = corner(index - 1);
         PlaPoint next_corner = corner(index);
         PlaSide prev_corner_side = curr_result_line.side_of(prev_corner);
         PlaSide next_corner_side = curr_result_line.side_of(next_corner);
         
         if (prev_corner_side != PlaSide.COLLINEAR && next_corner_side != PlaSide.COLLINEAR && prev_corner_side == next_corner_side)
            {
            // the projection point is outside the line segment
            continue;
            }
         
         nearest_line = plaline(index);
         min_distance = curr_distance;
         result_line = curr_result_line;
         }

      if (nearest_line == null) return null;

      PlaLineInt start_line = new PlaLineInt(p_from_point, nearest_line.direction());

      PlaSegmentInt result = new PlaSegmentInt(start_line, result_line, nearest_line);
      
      return result;
      }

   /**
    * Shortens this polyline to p_new_line_count lines. 
    * Additioanally the last line segment will be approximately shortened to p_new_length. 
    * The last corner of the new polyline will be an IntPoint.
    */
   public Polyline shorten(int p_new_line_count, double p_last_segment_length)
      {
      PlaPointFloat last_corner = corner_approx(p_new_line_count - 2);
      PlaPointFloat prev_last_corner = corner_approx(p_new_line_count - 3);
      PlaPointInt new_last_corner = prev_last_corner.change_length(last_corner, p_last_segment_length).round();
      
      if (new_last_corner.equals(corner(corner_count() - 2)))
         {
         // skip the last line
         return skip_lines(p_new_line_count - 1, p_new_line_count - 1);
         }
      
      PlaLineInt[] new_lines = new PlaLineInt[p_new_line_count];
      plaline_copy(0, new_lines, 0, p_new_line_count - 2);
      // create the last 2 lines of the new polyline
      PlaPointInt first_line_point = plaline(p_new_line_count - 2).point_a;
      if (first_line_point.equals(new_last_corner))
         {
         first_line_point = plaline(p_new_line_count - 2).point_b;
         }
      PlaLineInt new_prev_last_line = new PlaLineInt(first_line_point, new_last_corner);
      new_lines[p_new_line_count - 2] = new_prev_last_line;
      new_lines[p_new_line_count - 1] = new PlaLineInt(new_last_corner, new_prev_last_line.direction().turn_45_degree(6));
      return new Polyline(new_lines);
      }
   
   /**
    * Replacement for direct indexing in the array
    * @param index
    * @return
    */
   public PlaLineInt plaline ( int index )
      {
      return lines_arr.get(index);
      }
   
   /**
    * replacement for getting len instead of using direct array
    * @return
    */
   public int plalinelen ( )
      {
      return lines_arr.size();
      }
   
   /**
    * return the plalinelen plus or minus the given offset
    * @param offset
    * @return
    */
   public int plalinelen ( int offset )
      {
      return lines_arr.size() + offset;
      }
   
   /**
    * Copy current plaline array into a new one with the same len
    * Content is copied
    * @return
    */
   public PlaLineInt [] plaline_copy()
      {
      int arr_len = plalinelen();
      
      PlaLineInt [] risul = new PlaLineInt[arr_len];
      
      for (int index=0; index<arr_len; index++)
         risul[index] = plaline(index);
      
      return risul;
      }
   
   /**
    * Copy current plaline array into a new one skipping index given
    * The resulting copy will be one line short, obviously
    * Content is copied
    * @return
    */
   public PlaLineInt [] plaline_copy(int skip_index)
      {
      int src_len   = plalinelen();
      int risul_len = src_len-1;
      int index;
      
      PlaLineInt [] risul = new PlaLineInt[risul_len];
      
      for (index=0; index<skip_index; index++)
         risul[index] = plaline(index);
      
      index++; // skip the skip_index
      
      for (; index<src_len; index++)
         risul[index-1] = plaline(index);
      
      return risul;
      }

   public void plaline_copy(int src_pos, PlaLineInt [] dest, int dest_pos, int length )
      {
      for (int index=0; index<length; index++)
         dest[dest_pos+index] = plaline(src_pos+index);
      }
   
   
   public PlaSegmentInt segment_get ( int index )
      {
      if ( index <= 0 || index >= plalinelen(-1) )
         {
         System.out.println(classname+"segment_get BAD index="+index);
         return null;
         }
      
      return new PlaSegmentInt(plaline(index - 1),plaline(index),plaline(index + 1) );
      }
   
   }