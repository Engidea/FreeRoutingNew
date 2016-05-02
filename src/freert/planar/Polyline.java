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
   private final PlaLineInt[] lines_arr;

   transient private PlaPointFloat[] precalculated_float_corners = null;
   transient private PlaPoint[]      precalculated_corners = null;
   transient private ShapeTileBox    precalculated_bounding_box = null;
   
   /**
    * creates a polyline of length p_polygon.corner_count + 1 from p_polygon, so that the i-th corner of p_polygon will be the
    * intersection of the i-th and the i+1-th lines of the new created p_polyline for 0 <= i < p_point_arr.length. 
    * p_polygon must have at least 2 corners
    */
   public Polyline(PlaPolygon p_polygon)
      {
      PlaPoint[] point_arr = p_polygon.corner_array();
   
      if (point_arr.length < 2)
         throw new IllegalArgumentException(classname+"A must contain at least 2 different points");
      
      lines_arr = new PlaLineInt[point_arr.length + 1];
      
      for (int index = 1; index < point_arr.length; ++index)
         {
         lines_arr[index] = new PlaLineInt(point_arr[index - 1], point_arr[index]);
         }
      
      // construct perpendicular lines at the start and at the end to represent
      // the first and the last point of point_arr as intersection of lines.

      PlaDirection dir = PlaDirection.get_instance(point_arr[0], point_arr[1]);
      lines_arr[0] = new PlaLineInt(point_arr[0], dir.turn_45_degree(2));

      dir = PlaDirection.get_instance(point_arr[point_arr.length - 1], point_arr[point_arr.length - 2]);
      lines_arr[point_arr.length] = new PlaLineInt(point_arr[point_arr.length - 1], dir.turn_45_degree(2));
      }

   public Polyline(PlaPoint[] p_points)
      {
      this(new PlaPolygon(p_points));
      }

   /**
    * creates a polyline consisting of 3 lines
    */
   public Polyline(PlaPoint p_from_corner, PlaPoint p_to_corner)
      {
      if (p_from_corner.equals(p_to_corner))
         throw new IllegalArgumentException(classname+"B must contain at least 2 different points");
      
      lines_arr = new PlaLineInt[3];
      PlaDirection dir = PlaDirection.get_instance(p_from_corner, p_to_corner);
      lines_arr[0] = new PlaLineInt(p_from_corner, dir.turn_45_degree(2));
      lines_arr[1] = new PlaLineInt(p_from_corner, p_to_corner);
      dir = PlaDirection.get_instance(p_from_corner, p_to_corner);
      lines_arr[2] = new PlaLineInt(p_to_corner, dir.turn_45_degree(2));
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
      
      if ( have_len < 3)
         {
         System.err.println(classname+"IntLine[] A < 3");
         lines_arr = new PlaLineInt[0];
         return;
         }
      
      // this part will remove all lines that are colinear with the previous one
      ArrayList<PlaLineInt> lines = new ArrayList<PlaLineInt>(have_len);
      PlaLineInt ref_line = p_line_arr[0];
      lines.add(ref_line);
      
      for (int index = 1; index < have_len; ++index)
         {
         // skip a line that is parallel with the reference line
         if ( ref_line.is_parallel(p_line_arr[index])) continue;
         
         ref_line = p_line_arr[index];
         
         lines.add(ref_line);
         }

      int lines_len = lines.size();

      if (lines.size() < 3)
         {
         System.err.println(classname+"IntLine[] B < 3");
         lines_arr = new PlaLineInt[0];
         return;
         }
      
      // corners are one less than the lines
      precalculated_float_corners = new PlaPointFloat[lines_len - 1];

      for (int index = 0; index < precalculated_float_corners.length; ++index)
         {
         PlaLineInt cur_l = lines.get(index);
         PlaLineInt nxt_l = lines.get(index+1);
         
         precalculated_float_corners[index] = cur_l.intersection_approx(nxt_l);
         }
       
      PlaLineInt[] new_arr = new PlaLineInt[lines_len];
      
      new_arr[0] = lines.get(0);
      
      // turn  the direction of the lines that they point always from the previous corner to the next corner
      // Now, why is not the first and last line checked ? first should point to first and last should point away, no ?
      for (int index = 1; index < lines_len-1; index++)
         {
         PlaLineInt pre_l = lines.get(index-1);
         PlaLineInt cur_l = lines.get(index);
         
         PlaSide side_of_line = pre_l.side_of(precalculated_float_corners[index]);

         if (side_of_line != PlaSide.COLLINEAR)
            {
            PlaDirection d0 = pre_l.direction();
            PlaDirection d1 = cur_l.direction();

            PlaSide side1 = d0.side_of(d1);
         
            if (side1 != side_of_line)
               {
               cur_l = cur_l.opposite();
               }
            }

         new_arr[index] = cur_l;
         }
      
      new_arr[lines_len-1] = lines.get(lines_len-1);
      
      lines_arr = new_arr;
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
      return lines_arr.length >= 3;
      }

   /**
    * Returns the number of lines minus 1
    */
   public int corner_count()
      {
      return lines_arr.length - 1;
      }

   /**
    * Checks, if this polyline is empty or if all corner points are equal
    * This seems weird..... how can a polyline have same points it if has more than three lines non colinear ?
    */
   private boolean is_point()
      {
      PlaPoint first_corner = corner(0);

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
      for (int index = 0; index < lines_arr.length; ++index)
         {
         if ( lines_arr[index].is_orthogonal() ) continue;

         return false;
         }
      return true;
      }

   /**
    * checks, if all lines of this polyline are multiples of 45 degree
    */
   public boolean is_multiple_of_45_degree()
      {
      for (int index = 0; index < lines_arr.length; ++index)
         {
         if (!lines_arr[index].is_multiple_of_45_degree())
            {
            return false;
            }
         }
      return true;
      }

   /**
    * returns the intersection of the first line with the second line
    */
   public PlaPoint corner_first()
      {
      return corner(0);
      }

   /**
    * returns the intersection of the last line with the line before the last line
    */
   public PlaPoint corner_last()
      {
      // corner index go from 0 to n-1 to indicate n corners 
      return corner(corner_count() - 1);
      }

   /**
    * returns the array of the intersection of two consecutive lines approximated by FloatPoint's.
    */
   public PlaPoint[] corner_arr()
      {
      if (lines_arr.length < 2)
         {
         return new PlaPoint[0];
         }
      if (precalculated_corners == null)
         {
         // corner array is not yet allocated
         precalculated_corners = new PlaPoint[corner_count()];
         }

      for (int index = 0; index < precalculated_corners.length; ++index)
         {
         if (precalculated_corners[index] == null)
            precalculated_corners[index] = lines_arr[index].intersection(lines_arr[index + 1]);
         }
      
      return precalculated_corners;
      }

   /**
    * returns the array of the intersection of two consecutive lines approximated by FloatPoint's.
    */
   public PlaPointFloat[] corner_approx_arr()
      {
      if (lines_arr.length < 2)
         {
         return new PlaPointFloat[0];
         }
      
      if (precalculated_float_corners == null)
         {
         // corner array is not yet allocated
         precalculated_float_corners = new PlaPointFloat[corner_count()];
         }

      for (int index = 0; index < precalculated_float_corners.length; ++index)
         {
         if (precalculated_float_corners[index] == null)
            precalculated_float_corners[index] = lines_arr[index].intersection_approx(lines_arr[index + 1]);
         }
      
      return precalculated_float_corners;
      }

   /**
    * Returns an approximation of the intersection of the p_no-th with the (p_no - 1)-th line by a FloatPoint.
    */
   public PlaPointFloat corner_approx(int p_no)
      {
      int corners_count = lines_arr.length -1;

      if (corners_count < 1)
         {
         System.err.println(classname+"corner: corners_count < 1");
         return null;
         }
      
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
         precalculated_float_corners[p_no] = lines_arr[p_no].intersection_approx(lines_arr[p_no + 1]);
         }

      return precalculated_float_corners[p_no];
      }

   /**
    * Returns the intersection of the p_no-th with the (p_no - 1)-th edge line.
    */
   public PlaPoint corner(int p_no)
      {
      int corners_count = lines_arr.length -1;
      
      if (corners_count < 1)
         {
         System.err.println(classname+"corner: corners_count < 1");
         return null;
         }
      
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

      if (precalculated_corners[p_no] == null)
         {
         // corner is not yet calculated
         precalculated_corners[p_no] = lines_arr[p_no].intersection(lines_arr[p_no + 1]);
         }
      
      return precalculated_corners[p_no];
      }

   /**
    * return the polyline with the reversed order of lines
    */
   public Polyline reverse()
      {
      PlaLineInt[] reversed_lines = new PlaLineInt[lines_arr.length];
      
      for (int index = 0; index < lines_arr.length; ++index)
         {
         reversed_lines[index] = lines_arr[lines_arr.length - index - 1].opposite();
         }
      
      return new Polyline(reversed_lines);
      }

   /**
    * Calculates the length of this polyline from p_from_corner to p_to_corner.
    */
   public double length_approx(int p_from_corner, int p_to_corner)
      {
      int from_corner = Math.max(p_from_corner, 0);
      int to_corner = Math.min(p_to_corner, lines_arr.length - 2);
      double result = 0;
      for (int i = from_corner; i < to_corner; ++i)
         {
         result += corner_approx(i + 1).distance(corner_approx(i));
         }
      return result;
      }

   /**
    * Calculates the cumulative distance between consecutive corners of this polyline.
    */
   public double length_approx()
      {
      return length_approx(0, lines_arr.length - 2);
      }

   /**
    * calculates for each line a shape around this line where the right and left edge lines have the distance p_half_width from the
    * center line Returns an array of convex shapes of length line_count - 2
    */
   public ShapeTile[] offset_shapes(int p_half_width)
      {
      return offset_shapes(p_half_width, 0, lines_arr.length - 1);
      }

   /**
    * calculates for each line between p_from_no and p_to_no a shape around this line, where the right and left edge lines have the
    * distance p_half_width from the center line
    */
   public ShapeTile[] offset_shapes(int p_half_width, int p_from_no, int p_to_no)
      {
      int from_no = Math.max(p_from_no, 0);
      int to_no = Math.min(p_to_no, lines_arr.length - 1);
      
      int shape_count = Math.max(to_no - from_no - 1, 0);
      
      ShapeTile[] shape_arr = new ShapeTile[shape_count];
      
      if (shape_count == 0)
         {
         return shape_arr;
         }
      
      PlaVector prev_dir = lines_arr[from_no].direction().get_vector();
      PlaVector curr_dir = lines_arr[from_no + 1].direction().get_vector();
      
      for (int index = from_no + 1; index < to_no; ++index)
         {
         PlaVector next_dir = lines_arr[index + 1].direction().get_vector();

         PlaLineInt[] lines = new PlaLineInt[4];

         lines[0] = lines_arr[index].translate(-p_half_width);
         // current center line translated to the right

         // create the front line of the offset shape
         PlaSide next_dir_from_curr_dir = next_dir.side_of(curr_dir);
         // left turn from curr_line to next_line
         if (next_dir_from_curr_dir == PlaSide.ON_THE_LEFT)
            {
            lines[1] = lines_arr[index + 1].translate(-p_half_width);
            // next right line
            }
         else
            {
            lines[1] = lines_arr[index + 1].opposite().translate(-p_half_width);
            // next left line in opposite direction
            }

         lines[2] = lines_arr[index].opposite().translate(-p_half_width);
         // current left line in opposite direction

         // create the back line of the offset shape
         PlaSide curr_dir_from_prev_dir = curr_dir.side_of(prev_dir);
         // left turn from prev_line to curr_line
         if (curr_dir_from_prev_dir == PlaSide.ON_THE_LEFT)
            {
            lines[3] = lines_arr[index - 1].translate(-p_half_width);
            // previous line translated to the right
            }
         else
            {
            lines[3] = lines_arr[index - 1].opposite().translate(-p_half_width);
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
         PlaVector tmp_curr_dir = next_dir;
         boolean direction_changed = false;
         
         for (int jndex = index + 2; jndex < lines_arr.length - 1; ++jndex)
            {
            if (corner_approx(jndex - 1).length_square(check_distance_corner) > check_dist_square)
               {
               break;
               }
            if (!direction_changed)
               {
               corner_to_check = curr_line.intersection_approx(check_line);
               }
            PlaVector tmp_next_dir = lines_arr[jndex].direction().get_vector();
            PlaLineInt next_border_line = null;
            PlaSide tmp_next_dir_from_tmp_curr_dir = tmp_next_dir.side_of(tmp_curr_dir);
            direction_changed = tmp_next_dir_from_tmp_curr_dir != next_dir_from_curr_dir;
            if (!direction_changed)
               {
               if (tmp_next_dir_from_tmp_curr_dir == PlaSide.ON_THE_LEFT)
                  {
                  next_border_line = lines_arr[jndex].translate(-p_half_width);
                  }
               else
                  {
                  next_border_line = lines_arr[jndex].opposite().translate(-p_half_width);
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
            PlaVector tmp_prev_dir = lines_arr[jndex].direction().get_vector();
            PlaLineInt prev_border_line = null;
            PlaSide tmp_curr_dir_from_tmp_prev_dir = tmp_curr_dir.side_of(tmp_prev_dir);
            direction_changed = tmp_curr_dir_from_tmp_prev_dir != curr_dir_from_prev_dir;
            if (!direction_changed)
               {
               if (tmp_curr_dir.side_of(tmp_prev_dir) == PlaSide.ON_THE_LEFT)
                  {
                  prev_border_line = lines_arr[jndex].translate(-p_half_width);
                  }
               else
                  {
                  prev_border_line = lines_arr[jndex].opposite().translate(-p_half_width);
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
         int curr_shape_no = index - from_no - 1;
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
         shape_arr[curr_shape_no] = bounding_shape.intersection_with_simplify(s1);
         if (shape_arr[curr_shape_no].is_empty())
            {
            System.out.println("offset_shapes: shape is empty");
            }

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
      if (p_no < 0 || p_no > lines_arr.length - 3)
         {
         System.out.println("Polyline.offset_shape: p_no out of range");
         return null;
         }
      ShapeTile[] result = offset_shapes(p_half_width, p_no, p_no + 2);
      return result[0];
      }

   /**
    * Calculates for the p_no-th line segment a box shape around this line where the border lines have the distance p_half_width
    * from the center line. 0 <= p_no <= arr.length - 3
    */
   public ShapeTileBox offset_box(int p_half_width, int p_no)
      {
      PlaSegmentInt curr_line_segment = new PlaSegmentInt(this, p_no + 1);
      ShapeTileBox result = curr_line_segment.bounding_box().offset(p_half_width);
      return result;
      }

   /**
    * Returns the by p_vector translated polyline
    */
   public Polyline translate_by(PlaVector p_vector)
      {
      if (p_vector.equals(PlaVector.ZERO)) return this;
      
      PlaLineInt[] new_arr = new PlaLineInt[lines_arr.length];
      for (int i = 0; i < new_arr.length; ++i)
         {
         new_arr[i] = lines_arr[i].translate_by(p_vector);
         }
      
      return new Polyline(new_arr);
      }

   /**
    * Returns the polyline turned by p_factor times 90 degree around p_pole.
    */
   public Polyline turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[lines_arr.length];
      for (int i = 0; i < new_arr.length; ++i)
         {
         new_arr[i] = lines_arr[i].turn_90_degree(p_factor, p_pole);
         }
      return new Polyline(new_arr);
      }

   public Polyline rotate_approx(double p_angle, PlaPointFloat p_pole)
      {
      if (p_angle == 0)
         {
         return this;
         }
      PlaPointInt[] new_corners = new PlaPointInt[corner_count()];
      for (int i = 0; i < new_corners.length; ++i)
         {

         new_corners[i] = corner_approx(i).rotate(p_angle, p_pole).round();
         }
      return new Polyline(new_corners);
      }

   /** Mirrors this polyline at the vertical line through p_pole */
   public Polyline mirror_vertical(PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[lines_arr.length];
      for (int i = 0; i < new_arr.length; ++i)
         {
         new_arr[i] = lines_arr[i].mirror_vertical(p_pole);
         }
      return new Polyline(new_arr);
      }

   /** Mirrors this polyline at the horizontal line through p_pole */
   public Polyline mirror_horizontal(PlaPointInt p_pole)
      {
      PlaLineInt[] new_arr = new PlaLineInt[lines_arr.length];
      for (int i = 0; i < new_arr.length; ++i)
         {
         new_arr[i] = lines_arr[i].mirror_horizontal(p_pole);
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
      int to_corner_no = Math.min(p_to_corner_no, lines_arr.length - 2);
      double lx = Integer.MAX_VALUE;
      double ly = Integer.MAX_VALUE;
      double rx = Integer.MIN_VALUE;
      double uy = Integer.MIN_VALUE;
      double ulx = Integer.MAX_VALUE;
      double lrx = Integer.MIN_VALUE;
      double llx = Integer.MAX_VALUE;
      double urx = Integer.MIN_VALUE;
      for (int i = from_corner_no; i <= to_corner_no; ++i)
         {
         PlaPointFloat curr = corner_approx(i);
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
      ShapeTileOctagon surrounding_octagon = new ShapeTileOctagon((int) Math.floor(lx), (int) Math.floor(ly), (int) Math.ceil(rx), (int) Math.ceil(uy), (int) Math.floor(ulx), (int) Math.ceil(lrx),
            (int) Math.floor(llx), (int) Math.ceil(urx));
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
      for (int i = 1; i < lines_arr.length - 1; ++i)
         {
         PlaPointFloat projection = p_from_point.projection_approx(lines_arr[i]);
         double curr_distance = projection.distance(p_from_point);
         if (curr_distance < min_distance)
            {
            // look, if the projection is inside the segment
            double segment_length = corners[i].distance(corners[i - 1]);
            if (projection.distance(corners[i]) + projection.distance(corners[i - 1]) < segment_length + c_tolerance)
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
    * Combines the two polylines, if they have a common end corner. The order of lines in this polyline will be preserved. Returns
    * the combined polyline or this polyline, if this polyline and p_other have no common end corner. If there is something to
    * combine at the start of this polyline, p_other is inserted in front of this polyline. If there is something to combine at the
    * end of this polyline, this polyline is inserted in front of p_other.
    */
   public Polyline combine(Polyline p_other)
      {
      if ( p_other == null ) return this;
      
      if ( lines_arr.length < 3 )
         throw new IllegalArgumentException(classname+"what A");
      
      if ( p_other.lines_arr.length < 3)
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
      
      
      
      
      
      
      
      
      PlaLineInt[] line_arr = new PlaLineInt[lines_arr.length + p_other.lines_arr.length - 2];
      if (combine_at_start)
         {
         // insert the lines of p_other in front
         if (combine_other_at_start)
            {
            // insert in reverse order, skip the first line of p_other
            for (int i = 0; i < p_other.lines_arr.length - 1; ++i)
               {
               line_arr[i] = p_other.lines_arr[p_other.lines_arr.length - i - 1].opposite();
               }
            }
         else
            {
            // skip the last line of p_other
            for (int i = 0; i < p_other.lines_arr.length - 1; ++i)
               {
               line_arr[i] = p_other.lines_arr[i];
               }
            }
         // append the lines of this polyline, skip the first line
         for (int i = 1; i < lines_arr.length; ++i)
            {
            line_arr[p_other.lines_arr.length + i - 2] = lines_arr[i];
            }
         }
      else
         {
         // insert the lines of this polyline in front, skip the last line
         for (int i = 0; i < lines_arr.length - 1; ++i)
            {
            line_arr[i] = lines_arr[i];
            }
         if (combine_other_at_start)
            {
            // skip the first line of p_other
            for (int i = 1; i < p_other.lines_arr.length; ++i)
               {
               line_arr[lines_arr.length + i - 2] = p_other.lines_arr[i];
               }
            }
         else
            {
            // insert in reverse order, skip the last line of p_other
            for (int i = 1; i < p_other.lines_arr.length; ++i)
               {
               line_arr[lines_arr.length + i - 2] = p_other.lines_arr[p_other.lines_arr.length - i - 1].opposite();
               }
            }
         }
      
      
      return new Polyline(line_arr);
      }

   /**
    * Splits this polyline at the line with number p_line_no into two by inserting p_endline as concluding line of the first split
    * piece and as the start line of the second split piece. 
    * p_endline and the line with number p_line_no must not be parallel. 
    * The order of the lines ins the two result pieces is preserved. 
    * p_line_no must be bigger than 0 and less then arr.length - 1.
    * Returns null, if nothing was split
    */
   public Polyline[] split(int p_line_no, PlaLineInt p_end_line)
      {
      if (p_line_no < 1 || p_line_no > lines_arr.length - 2)
         {
         System.out.println("Polyline.split: p_line_no out of range");
         return null;
         }
      
      if (lines_arr[p_line_no].is_parallel(p_end_line)) return null;
      
      PlaPoint new_end_corner = lines_arr[p_line_no].intersection(p_end_line);
      
      if ( new_end_corner.is_NaN() ) return null;

      if (p_line_no <= 1 && new_end_corner.equals(corner_first()) || p_line_no >= lines_arr.length - 2 && new_end_corner.equals(corner_last()))
         {
         // No split, if p_end_line does not intersect, but touches only tnis Polyline at an end point.
         return null;
         }
      
      PlaLineInt[] first_piece;
      if (corner(p_line_no - 1).equals(new_end_corner))
         {
         // skip line segment of length 0 at the end of the first piece
         first_piece = new PlaLineInt[p_line_no + 1];
         System.arraycopy(lines_arr, 0, first_piece, 0, first_piece.length);

         }
      else
         {
         first_piece = new PlaLineInt[p_line_no + 2];
         System.arraycopy(lines_arr, 0, first_piece, 0, p_line_no + 1);
         first_piece[p_line_no + 1] = p_end_line;
         }
      PlaLineInt[] second_piece;
      if (corner(p_line_no).equals(new_end_corner))
         {
         // skip line segment of length 0 at the beginning of the second piece
         second_piece = new PlaLineInt[lines_arr.length - p_line_no];
         System.arraycopy(lines_arr, p_line_no, second_piece, 0, second_piece.length);

         }
      else
         {
         second_piece = new PlaLineInt[lines_arr.length - p_line_no + 1];
         second_piece[0] = p_end_line;
         System.arraycopy(lines_arr, p_line_no, second_piece, 1, second_piece.length - 1);
         }
      Polyline[] result = new Polyline[2];
      result[0] = new Polyline(first_piece);
      result[1] = new Polyline(second_piece);
      
      if (result[0].is_point() || result[1].is_point())
         {
         return null;
         }
      
      return result;
      }

   /**
    * create a new Polyline by skipping the lines of this Polyline from p_from_no to p_to_no
    */
   public Polyline skip_lines(int p_from_no, int p_to_no)
      {
      if (p_from_no < 0 || p_to_no > lines_arr.length - 1 || p_from_no > p_to_no)
         {
         return this;
         }
      PlaLineInt[] new_lines = new PlaLineInt[lines_arr.length - (p_to_no - p_from_no + 1)];
      System.arraycopy(lines_arr, 0, new_lines, 0, p_from_no);
      System.arraycopy(lines_arr, p_to_no + 1, new_lines, p_from_no, new_lines.length - p_from_no);
      return new Polyline(new_lines);
      }

   public boolean contains(PlaPointInt p_point)
      {
      for (int index = 1; index < lines_arr.length - 1; ++index)
         {
         PlaSegmentInt curr_segment = new PlaSegmentInt(this, index);
         
         if (curr_segment.contains(p_point)) return true;
         }
      
      return false;
      }

   /**
    * Creates a perpendicular line segment from p_from_point onto the nearest line segment of this polyline to p_from_side. 
    * Returns null, if the perpendicular line does not intersect the neares line segment inside its segment bounds or if p_from_point is
    * contained in this polyline.
    */
   public PlaSegmentInt projection_line(PlaPoint p_from_point)
      {
      if ( p_from_point == null ) return null;
      
      PlaPointFloat from_point = p_from_point.to_float();
      double min_distance = Double.MAX_VALUE;
      PlaLineInt result_line = null;
      PlaLineInt nearest_line = null;
      
      for (int index = 1; index < lines_arr.length - 1; ++index)
         {
         PlaPointFloat projection = from_point.projection_approx(lines_arr[index]);
         double curr_distance = projection.distance(from_point);
         
         if (curr_distance >= min_distance) continue;

         PlaDirection direction_towards_line = lines_arr[index].perpendicular_direction(p_from_point);
        
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
         
         nearest_line = lines_arr[index];
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
      System.arraycopy(lines_arr, 0, new_lines, 0, p_new_line_count - 2);
      // create the last 2 lines of the new polyline
      PlaPoint first_line_point = lines_arr[p_new_line_count - 2].point_a;
      if (first_line_point.equals(new_last_corner))
         {
         first_line_point = lines_arr[p_new_line_count - 2].point_b;
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
      return lines_arr[index];
      }
   
   /**
    * replacement for getting len instead of using direct array
    * @return
    */
   public int plalinelen ( )
      {
      return lines_arr.length;
      }
   
   /**
    * return the plalinelen plus or minus the given offset
    * @param offset
    * @return
    */
   public int plalinelen ( int offset )
      {
      return lines_arr.length + offset;
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
         risul[index] = lines_arr[index];
      
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
         risul[index] = lines_arr[index];
      
      index++; // skip the skip_index
      
      for (; index<src_len; index++)
         risul[index-1] = lines_arr[index];
      
      return risul;
      }

   public void plaline_copy(int src_pos, PlaLineInt [] dest, int dest_pos, int length )
      {
      for (int index=0; index<length; index++)
         dest[dest_pos+index] = lines_arr[src_pos+index];
      }
   
   
   }