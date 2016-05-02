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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * Abstract class defining functionality for convex shapes, whose borders consists of straight lines.
 *
 * @author Alfons Wirtz
 */
public abstract class ShapeTile extends ShapePolyline implements ShapeConvex
   {
   private static final long serialVersionUID = 1L;

   /**
    * creates a Simplex as intersection of the halfplanes defined by an array of directed lines
    */
   public static ShapeTile get_instance(PlaLineInt[] p_line_arr)
      {
      ShapeTileSimplex result = ShapeTileSimplex.get_instance(p_line_arr);
      return result.simplify();
      }

   /**
    * Creates a TileShape from a Point array, who forms the corners of the shape of a convex polygon. May work only for IntPoints.
    */
   public static ShapeTile get_instance(PlaPoint[] p_convex_polygon)
      {
      PlaLineInt[] line_arr = new PlaLineInt[p_convex_polygon.length];
      for (int j = 0; j < line_arr.length - 1; ++j)
         {
         line_arr[j] = new PlaLineInt(p_convex_polygon[j], p_convex_polygon[j + 1]);
         }
      line_arr[line_arr.length - 1] = new PlaLineInt(p_convex_polygon[line_arr.length - 1], p_convex_polygon[0]);
      return get_instance(line_arr);
      }

   /**
    * creates a half_plane from a directed line
    */
   public static ShapeTile get_instance(PlaLineInt p_line)
      {
      PlaLineInt[] lines = new PlaLineInt[1];
      lines[0] = p_line;
      return ShapeTileSimplex.get_instance(lines);
      }

   /**
    * Creates a normalized IntOctagon from the input values. For the meaning of the parameter shortcuts see class IntOctagon.
    */
   public static ShapeTileOctagon get_instance(int p_lx, int p_ly, int p_rx, int p_uy, int p_ulx, int p_lrx, int p_llx, int p_urx)
      {
      ShapeTileOctagon oct = new ShapeTileOctagon(p_lx, p_ly, p_rx, p_uy, p_ulx, p_lrx, p_llx, p_urx);
      return oct.normalize();
      }

   /**
    * creates a boxlike convex shape
    */
   public static ShapeTileOctagon get_instance(int p_lower_left_x, int p_lower_left_y, int p_upper_right_x, int p_upper_right_y)
      {
      ShapeTileBox box = new ShapeTileBox(p_lower_left_x, p_lower_left_y, p_upper_right_x, p_upper_right_y);
      return box.to_IntOctagon();
      }

   /**
    * creates the smallest IntOctagon containing p_point
   public static ShapeTileBox get_instance(PlaPoint p_point)
      {
      return p_point.surrounding_box();
      }
    */

   /**
    * Tries to simplify the result shape to a simpler shape. Simplifying always in the intersection function may cause performance
    * problems.
    */
   public ShapeTile intersection_with_simplify(ShapeTile p_other)
      {
      ShapeTile result = intersection(p_other);
      return result.simplify();
      }

   /**
    * Converts the physical instance of this shape to a simpler physical instance, if possible.
    */
   public abstract ShapeTile simplify();

   /**
    * checks if this TileShape is an IntBox or can be converted into an IntBox
    */
   public abstract boolean is_IntBox();

   /**
    * checks if this TileShape is an IntOctagon or can be converted into an IntOctagon
    */
   public abstract boolean is_IntOctagon();

   /**
    * Returns the intersection of this shape with p_other
    */
   public abstract ShapeTile intersection(ShapeTile p_other);

   /**
    * Returns the p_no-th edge line of this shape for p_no between 0 and edge_line_count() - 1. The edge lines are sorted in
    * counterclock sense around the shape starting with the edge with the smallest direction.
    */
   public abstract PlaLineInt border_line(int p_no);

   /**
    * if p_line is a borderline of this shape the number of that edge is returned, otherwise -1
    */
   public abstract int border_line_index(PlaLineInt p_line);

   /**
    * Converts the internal representation of this TieShape to a Simplex
    */
   public abstract ShapeTileSimplex to_Simplex();

   /**
    * Returns the content of the area of the shape. If the shape is unbounded, Double.MAX_VALUE is returned.
    */
   public double area()
      {
      if (!is_bounded()) return Double.MAX_VALUE;

      if ( ! dimension().is_area() ) return 0;

      // calculate half of the absolute value of
      // x0 (y1 - yn-1) + x1 (y2 - y0) + x2 (y3 - y1) + ...+ xn-1( y0 - yn-2)
      // where xi, yi are the coordinates of the i-th corner of this TileShape.

      double result = 0;
      int corner_count = border_line_count();
      PlaPointFloat prev_corner = corner_approx(corner_count - 2);
      PlaPointFloat curr_corner = corner_approx(corner_count - 1);
      
      for (int index = 0; index < corner_count; ++index)
         {
         PlaPointFloat next_corner = corner_approx(index);
         result += curr_corner.v_x * (next_corner.v_y - prev_corner.v_y);
         prev_corner = curr_corner;
         curr_corner = next_corner;
         }
      
      result = 0.5 * Math.abs(result);
      
      return result;
      }

   /**
    * Returns true, if p_point is not contained in the inside or the edge of the shape
    */
   public boolean is_outside(PlaPoint p_point)
      {
      int line_count = border_line_count();

      if (line_count == 0) return true;

      for (int i = 0; i < line_count; ++i)
         {
         if (border_line(i).side_of(p_point) == PlaSide.ON_THE_LEFT)
            {
            return true;
            }
         }
      return false;
      }

   public boolean contains(PlaPoint p_point)
      {
      return !is_outside(p_point);
      }

   /**
    * Returns true, if p_point is contained in this shape, but not on an edge line
    */
   public boolean contains_inside(PlaPoint p_point)
      {
      int line_count = border_line_count();

      if (line_count == 0) return false;

      for (int i = 0; i < line_count; ++i)
         {
         if (border_line(i).side_of(p_point) != PlaSide.ON_THE_RIGHT)
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Returns true, if p_point is contained in this shape.
    */
   public boolean contains(PlaPointFloat p_point)
      {
      return contains(p_point, 0);
      }

   /**
    * Returns true, if p_point is contained in this shape with tolerance p_tolerance. p_tolerance is used when determing, if a point
    * is on the left side of a border line. It is used there in calculating a determinant and is not the distance of p_point to the
    * border.
    */
   public boolean contains(PlaPointFloat p_point, double p_tolerance)
      {
      int line_count = border_line_count();

      if (line_count == 0) return false;

      for (int i = 0; i < line_count; ++i)
         {
         if (border_line(i).side_of(p_point, p_tolerance) != PlaSide.ON_THE_RIGHT)
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Returns Side.COLLINEAR if p_point is on the border of this shape with tolerance p_tolerence. p_tolerance is used when
    * determing, if a point is on the right side of a border line. It is used there in calculating a determinant and is not the
    * distance of p_point to the border. Otherwise the function returns Side.ON_THE_LEFT if p_point is outside of this shape, and
    * Side.ON_THE_RIGTH if p_point is inside this shape.
    */
   public PlaSide side_of_border(PlaPointFloat p_point, double p_tolerance)
      {
      int line_count = border_line_count();
      if (line_count == 0)
         {
         return PlaSide.COLLINEAR;
         }
      PlaSide result = PlaSide.ON_THE_RIGHT; // point is inside
      for (int i = 0; i < line_count; ++i)
         {
         PlaSide curr_side = border_line(i).side_of(p_point, p_tolerance);
         if (curr_side == PlaSide.ON_THE_LEFT)
            {
            return PlaSide.ON_THE_LEFT; // point is outside
            }
         else if (curr_side == PlaSide.COLLINEAR)
            {
            result = curr_side;
            }

         }
      return result;
      }

   /**
    * If p_point lies on the border of this shape, the number of the edge line segment containing p_point is returned, otherwise -1
    * is returned.
    */
   public int contains_on_border_line_no(PlaPoint p_point)
      {
      int line_count = border_line_count();
      if (line_count == 0)
         {
         return -1;
         }
      int containing_line_no = -1;
      for (int i = 0; i < line_count; ++i)
         {
         PlaSide side_of = border_line(i).side_of(p_point);
         if (side_of == PlaSide.ON_THE_LEFT)
            {
            // p_point outside the convex shape
            return -1;
            }
         if (side_of == PlaSide.COLLINEAR)
            {
            containing_line_no = i;
            }
         }
      return containing_line_no;
      }

   /**
    * Returns true, if p_point lies exact on the boundary of the shape
    */
   public boolean contains_on_border(PlaPoint p_point)
      {
      return (contains_on_border_line_no(p_point) >= 0);
      }

   /**
    * Returns true, if this shape contains p_other completely. THere may be some numerical inaccurracy.
    */
   public boolean contains_approx(ShapeTile p_other)
      {
      PlaPointFloat[] corners = p_other.corner_approx_arr();
      for (PlaPointFloat curr_corner : corners)
         {
         if (!contains(curr_corner))
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Returns true, if this shape contains p_other completely.
    */
   public boolean contains(ShapeTile p_other)
      {
      for (int i = 0; i < p_other.border_line_count(); ++i)
         {
         if (!contains(p_other.corner(i)))
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Returns the distance between p_point and its nearest point on the shape. 0, if p_point is contained in this shape
    */
   public double distance(PlaPointFloat p_point)
      {
      PlaPointFloat nearest_point = nearest_point_approx(p_point);
      return nearest_point.distance(p_point);
      }

   /**
    * Returns the distance between p_point and its nearest point on the edge of the shape.
    */
   public double border_distance(PlaPointFloat p_point)
      {
      PlaPointFloat nearest_point = nearest_border_point_approx(p_point);
      return nearest_point.distance(p_point);
      }

   public double smallest_radius()
      {
      return border_distance(centre_of_gravity());
      }

   /**
    * Returns the point in this shape, which has the smallest distance to p_from_point. p_from_point, if that point is contained in
    * this shape
    */
   public PlaPoint nearest_point(PlaPoint p_from_point)
      {
      if (!is_outside(p_from_point))
         {
         return p_from_point;
         }
      return nearest_border_point(p_from_point);
      }

   public PlaPointFloat nearest_point_approx(PlaPointFloat p_from_point)
      {
      if (contains(p_from_point))
         {
         return p_from_point;
         }
      return nearest_border_point_approx(p_from_point);
      }

   /**
    * Returns a nearest point to p_from_point on the edge of the shape
    */
   public PlaPoint nearest_border_point(PlaPoint p_from_point)
      {
      int line_count = border_line_count();
      
      if (line_count == 0) return null;

      PlaPointFloat from_point_f = p_from_point.to_float();
      
      if (line_count == 1)
         {
         return border_line(0).perpendicular_projection(p_from_point);
         }
      
      PlaPoint nearest_point = null;
      double min_dist = Double.MAX_VALUE;
      int min_dist_ind = 0;

      // calculate the distance to the nearest corner first
      for (int i = 0; i < line_count; ++i)
         {
         PlaPointFloat curr_corner_f = corner_approx(i);
         double curr_dist = curr_corner_f.length_square(from_point_f);
         if (curr_dist < min_dist)
            {
            min_dist = curr_dist;
            min_dist_ind = i;
            }
         }

      nearest_point = corner(min_dist_ind);

      int prev_ind = line_count - 2;
      int curr_ind = line_count - 1;

      for (int next_ind = 0; next_ind < line_count; ++next_ind)
         {
         PlaPoint projection = border_line(curr_ind).perpendicular_projection(p_from_point);
         if ((!corner_is_bounded(curr_ind) || border_line(prev_ind).side_of(projection) == PlaSide.ON_THE_RIGHT)
               && (!corner_is_bounded(next_ind) || border_line(next_ind).side_of(projection) == PlaSide.ON_THE_RIGHT))
            {
            PlaPointFloat projection_f = projection.to_float();
            double curr_dist = projection_f.length_square(from_point_f);
            if (curr_dist < min_dist)
               {
               min_dist = curr_dist;
               nearest_point = projection;
               }
            }
         prev_ind = curr_ind;
         curr_ind = next_ind;
         }
      return nearest_point;
      }

   /**
    * Returns an approximation of the nearest point to p_from_point on the border of the this shape
    */
   public PlaPointFloat nearest_border_point_approx(PlaPointFloat p_from_point)
      {
      PlaPointFloat[] nearest_points = nearest_border_points_approx(p_from_point, 1);
      if (nearest_points.length <= 0)
         {
         return null;
         }
      return nearest_points[0];
      }

   /**
    * Returns an approximation of the p_count nearest points to p_from_point on the border of the this shape. 
    * The result points must be located on different border lines and are sorted in ascending order (the nearest point comes first).
    */
   public PlaPointFloat[] nearest_border_points_approx(PlaPointFloat p_from_point, int p_count)
      {
      // TODO should the return for impossible params be aa NaN ?
      if (p_count <= 0) return new PlaPointFloat[0];

      int line_count = border_line_count();
      int result_count = Math.min(p_count, line_count);
      if (line_count == 0)
         {
         return new PlaPointFloat[0];
         }
      if (line_count == 1)
         {
         PlaPointFloat[] result = new PlaPointFloat[1];
         result[0] = p_from_point.projection_approx(border_line(0));
         return result;
         }
      if (dimension() == PlaDimension.POINT)
         {
         PlaPointFloat[] result = new PlaPointFloat[1];
         result[0] = corner_approx(0);
         return result;
         }
      
      PlaPointFloat[] nearest_points = new PlaPointFloat[result_count];
      double[] min_dists = new double[result_count];
      for (int i = 0; i < result_count; ++i)
         {
         min_dists[i] = Double.MAX_VALUE;
         }

      // calculate the distances to the nearest corners first
      for (int i = 0; i < line_count; ++i)
         {
         if (corner_is_bounded(i))
            {
            PlaPointFloat curr_corner = corner_approx(i);
            double curr_dist = curr_corner.length_square(p_from_point);
            for (int j = 0; j < result_count; ++j)
               {
               if (curr_dist < min_dists[j])
                  {
                  for (int k = j + 1; k < result_count; ++k)
                     {
                     min_dists[k] = min_dists[k - 1];
                     nearest_points[k] = nearest_points[k - 1];
                     }
                  min_dists[j] = curr_dist;
                  nearest_points[j] = curr_corner;
                  break;
                  }
               }
            }
         }

      int prev_ind = line_count - 2;
      int curr_ind = line_count - 1;

      for (int next_ind = 0; next_ind < line_count; ++next_ind)
         {
         PlaPointFloat projection = p_from_point.projection_approx(border_line(curr_ind));
         if ((!corner_is_bounded(curr_ind) || border_line(prev_ind).side_of(projection) == PlaSide.ON_THE_RIGHT)
               && (!corner_is_bounded(next_ind) || border_line(next_ind).side_of(projection) == PlaSide.ON_THE_RIGHT))
            {
            double curr_dist = projection.length_square(p_from_point);
            for (int j = 0; j < result_count; ++j)
               {
               if (curr_dist < min_dists[j])
                  {
                  for (int k = j + 1; k < result_count; ++k)
                     {
                     min_dists[k] = min_dists[k - 1];
                     nearest_points[k] = nearest_points[k - 1];
                     }
                  min_dists[j] = curr_dist;
                  nearest_points[j] = projection;
                  break;
                  }
               }
            }
         prev_ind = curr_ind;
         curr_ind = next_ind;
         }
      return nearest_points;
      }

   /**
    * Returns the number of a nearest corner of the shape to p_from_point
    */
   public int index_of_nearest_corner(PlaPoint p_from_point)
      {
      PlaPointFloat from_point_f = p_from_point.to_float();
      int result = 0;
      int corner_count = border_line_count();
      double min_dist = Double.MIN_VALUE;
      for (int i = 0; i < corner_count; ++i)
         {
         double curr_dist = corner_approx(i).distance(from_point_f);
         if (curr_dist < min_dist)
            {
            min_dist = curr_dist;
            result = i;
            }
         }
      return result;
      }

   /**
    * Returns a line segment consisting of an approximations of the corners with index 0 and corner_count / 2.
    */
   public PlaSegmentFloat diagonal_corner_segment()
      {
      if (is_empty())
         {
         return null;
         }
      PlaPointFloat first_corner = corner_approx(0);
      PlaPointFloat last_corner = corner_approx(border_line_count() / 2);
      return new PlaSegmentFloat(first_corner, last_corner);
      }

   /**
    * Returns an approximation of the p_count nearest relative outside locations of p_shape in the direction of different border
    * lines of this shape. These relative locations are sorted in ascending order (the shortest comes first).
    */
   public PlaPointFloat[] nearest_relative_outside_locations(ShapeTile p_shape, int p_count)
      {
      int line_count = border_line_count();
      if (p_count <= 0 || line_count < 3 || !intersects(p_shape))
         {
         return new PlaPointFloat[0];
         }

      int result_count = Math.min(p_count, line_count);

      PlaPointFloat[] translate_coors = new PlaPointFloat[result_count];
      double[] min_dists = new double[result_count];
      for (int i = 0; i < result_count; ++i)
         {
         min_dists[i] = Double.MAX_VALUE;
         }

      int curr_ind = line_count - 1;

      int other_line_count = p_shape.border_line_count();

      for (int next_ind = 0; next_ind < line_count; ++next_ind)
         {
         double curr_max_dist = 0;
         PlaPointFloat curr_translate_coor = PlaPointFloat.ZERO;
         for (int corner_no = 0; corner_no < other_line_count; ++corner_no)
            {
            PlaPointFloat curr_corner = p_shape.corner_approx(corner_no);
            if (border_line(curr_ind).side_of(curr_corner) == PlaSide.ON_THE_RIGHT)
               {
               PlaPointFloat projection = curr_corner.projection_approx(border_line(curr_ind));
               double curr_dist = projection.length_square(curr_corner);
               if (curr_dist > curr_max_dist)
                  {
                  curr_max_dist = curr_dist;
                  curr_translate_coor = projection.substract(curr_corner);
                  }
               }
            }

         for (int j = 0; j < result_count; ++j)
            {
            if (curr_max_dist < min_dists[j])
               {
               for (int k = j + 1; k < result_count; ++k)
                  {
                  min_dists[k] = min_dists[k - 1];
                  translate_coors[k] = translate_coors[k - 1];
                  }
               min_dists[j] = curr_max_dist;
               translate_coors[j] = curr_translate_coor;
               break;
               }
            }
         curr_ind = next_ind;
         }
      return translate_coors;
      }

   public ShapeConvex shrink(double p_offset)
      {
      ShapeConvex result = offset(-p_offset);
      if (result.is_empty())
         {
         ShapeTileBox centre_box = centre_of_gravity().bounding_box();
         result = intersection(centre_box);
         }
      return result;
      }

   /**
    * Returns the maximum of the edge widths of the shape. Only defined when the shape is bounded.
    */
   public double length()
      {
      if ( ! is_bounded())
         {
         return Integer.MAX_VALUE;
         }
      
      if (dimension().is_empty())
         {
         return 0;
         }
      
      if (dimension().is_line())
         {
         return circumference() / 2;
         }
      
      // now the shape is 2-dimensional
      double max_distance = -1;
      double max_distance_2 = -1;
      PlaPointFloat gravity_point = centre_of_gravity();
      for (int i = 0; i < border_line_count(); ++i)
         {
         double curr_distance = Math.abs(border_line(i).signed_distance(gravity_point));
         if (curr_distance > max_distance)
            {
            max_distance_2 = max_distance;
            max_distance = curr_distance;
            }
         else if (curr_distance > max_distance_2)
            {
            max_distance_2 = curr_distance;
            }
         }
      return max_distance + max_distance_2;
      }

   /**
    * Calculates, if this Shape and p_other habe a common border piece and returns an 2 dimensional array with the indices in this
    * shape and p_other of the touching edge lines in this case. Otherwise an array of dimension 0 is returned. Used if the
    * intersection shape is 1-dimensional.
    */
   public int[] touching_sides(ShapeTile p_other)
      {
      // search the first edge line of p_other with reverse direction >= right

      int side_no_2 = -1;
      PlaDirection dir2 = null;
      for (int i = 0; i < p_other.border_line_count(); ++i)
         {
         PlaDirection curr_dir = p_other.border_line(i).direction();
         if (curr_dir.compareTo(PlaDirection.LEFT) >= 0)
            {
            side_no_2 = i;
            dir2 = curr_dir.opposite();
            break;
            }
         }
      if (dir2 == null)
         {
         System.out.println("touching_side : dir2 not found");
         return new int[0];
         }
      int side_no_1 = 0;
      PlaDirection dir1 = border_line(0).direction();
      final int max_ind = border_line_count() + p_other.border_line_count();

      for (int i = 0; i < max_ind; ++i)
         {
         int compare = dir2.compareTo(dir1);
         if (compare == 0)
            {
            if (border_line(side_no_1).is_equal_or_opposite(p_other.border_line(side_no_2)))
               {
               int[] result = new int[2];
               result[0] = side_no_1;
               result[1] = side_no_2;
               return result;
               }
            }
         if (compare >= 0) // dir2 is bigger than dir1
            {
            side_no_1 = (side_no_1 + 1) % border_line_count();
            dir1 = border_line(side_no_1).direction();
            }
         else
            // dir1 is bigger than dir2
            {
            side_no_2 = (side_no_2 + 1) % p_other.border_line_count();
            dir2 = p_other.border_line(side_no_2).direction().opposite();
            }
         }
      return new int[0];
      }

   /**
    * Calculates the minimal distance of p_line to this shape, assuming, that p_line is on the left of this shape. Returns -1, if
    * p_line is on the right of this shape or intersects with the interiour of this shape.
    */
   public double distance_to_the_left(PlaLineInt p_line)
      {
      double result = Integer.MAX_VALUE;
      for (int i = 0; i < border_line_count(); ++i)
         {
         PlaPointFloat curr_corner = corner_approx(i);
         PlaSide line_side = p_line.side_of(curr_corner, 1);
         if (line_side == PlaSide.COLLINEAR)
            {
            line_side = p_line.side_of(corner(i));
            }
         if (line_side == PlaSide.ON_THE_RIGHT)
            {
            // curr_point would be outside the result shape
            result = -1;
            break;
            }
         result = Math.min(result, p_line.signed_distance(curr_corner));
         }
      return result;
      }

   /**
    * Returns Side.COLLINEAR, if p_line intersects with the interiour of this shape, Side.ON_THE_LEFT, if this shape is completely
    * on the left of p_line or Side.ON_THE_RIGHT, if this shape is completely on the right of p_line.
    */
   public PlaSide side_of(PlaLineInt p_line)
      {
      boolean on_the_left = false;
      boolean on_the_right = false;
      for (int i = 0; i < border_line_count(); ++i)
         {
         PlaSide curr_side = p_line.side_of(corner(i));
         if (curr_side == PlaSide.ON_THE_LEFT)
            {
            on_the_right = true;
            }
         else if (curr_side == PlaSide.ON_THE_RIGHT)
            {
            on_the_left = true;
            }
         if (on_the_left && on_the_right)
            {
            return PlaSide.COLLINEAR;
            }
         }
      PlaSide result;
      if (on_the_left)
         {
         result = PlaSide.ON_THE_LEFT;
         }
      else
         {
         result = PlaSide.ON_THE_RIGHT;
         }
      return result;
      }

   public ShapeTile turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      PlaLineInt[] new_lines = new PlaLineInt[border_line_count()];
      for (int i = 0; i < new_lines.length; ++i)
         {
         new_lines[i] = border_line(i).turn_90_degree(p_factor, p_pole);
         }
      return get_instance(new_lines);
      }

   public ShapeTile rotate_approx(double p_angle, PlaPointFloat p_pole)
      {
      if (p_angle == 0)
         {
         return this;
         }
      PlaPointInt[] new_corners = new PlaPointInt[border_line_count()];
      for (int i = 0; i < new_corners.length; ++i)
         {

         new_corners[i] = corner_approx(i).rotate(p_angle, p_pole).round();
         }
      PlaPolygon corner_polygon = new PlaPolygon(new_corners);
      PlaPoint[] polygon_corners = corner_polygon.corner_array();
      ShapeTile result;
      if (polygon_corners.length >= 3)
         {
         result = get_instance(polygon_corners);
         }
      else if (polygon_corners.length == 2)
         {
         Polyline curr_polyline = new Polyline(polygon_corners);
         PlaSegmentInt curr_segment = new PlaSegmentInt(curr_polyline, 0);
         result = curr_segment.to_simplex();
         }
      else if (polygon_corners.length == 1)
         {
         result = new ShapeTileBox(polygon_corners[0]);
         }
      else
         {
         result = ShapeTileSimplex.EMPTY;
         }
      return result;
      }

   public ShapeTile mirror_vertical(PlaPointInt p_pole)
      {
      PlaLineInt[] new_lines = new PlaLineInt[border_line_count()];
      for (int i = 0; i < new_lines.length; ++i)
         {
         new_lines[i] = border_line(i).mirror_vertical(p_pole);
         }
      return get_instance(new_lines);
      }

   public ShapeTile mirror_horizontal(PlaPointInt p_pole)
      {
      PlaLineInt[] new_lines = new PlaLineInt[border_line_count()];
      for (int i = 0; i < new_lines.length; ++i)
         {
         new_lines[i] = border_line(i).mirror_horizontal(p_pole);
         }
      return get_instance(new_lines);
      }

   /**
    * Calculates the border line of this shape intersecting the ray from p_from_point into the direction p_direction. p_from_point
    * is assumed to be inside this shape, otherwise -1 is returned.
    */
   public int intersecting_border_line_no(PlaPoint p_from_point, PlaDirection p_direction)
      {
      if (! contains(p_from_point)) return -1;
      
      PlaPointFloat from_point = p_from_point.to_float();
      PlaLineInt intersection_line = new PlaLineInt(p_from_point, p_direction);
      PlaPointFloat second_line_point = intersection_line.point_b.to_float();
      int result = -1;
      double min_distance = Float.MAX_VALUE;
      for (int index = 0; index < border_line_count(); ++index)
         {
         PlaLineInt curr_border_line = border_line(index);
         
         PlaPointFloat curr_intersection = curr_border_line.intersection_approx(intersection_line);
      
         if (curr_intersection.is_NaN()) continue; // lines are parallel
         
         double curr_distence = curr_intersection.length_square(from_point);
         if (curr_distence < min_distance)
            {
            boolean direction_ok = curr_border_line.side_of(second_line_point) == PlaSide.ON_THE_LEFT || second_line_point.length_square(curr_intersection) < curr_distence;
            if (direction_ok)
               {
               result = index;
               min_distance = curr_distence;
               }
            }
         }
      return result;
      }

   /**
    * Cuts p_shape out of this shape and divides the result into convex pieces
    */
   public abstract ShapeTile[] cutout(ShapeTile p_shape);

   /**
    * Returns an arry of tuples of integers. The length of the array is the number of points, where p_polyline enters or leaves the
    * interiour of this shape. The first coordinate of the tuple is the number of the line segment of p_polyline, which enters the
    * simplex and the second coordinate of the tuple is the number of the edge_line of the simplex, which is crossed there. That
    * means that the entrance point is the intersection of this 2 lines.
    */
   public int[][] entrance_points(Polyline p_polyline)
      {
      int[][] result = new int[2 * p_polyline.plalinelen()][2];
      int intersection_count = 0;
      int prev_intersection_line_no = -1;
      int prev_intersection_edge_no = -1;
      for (int line_no = 1; line_no < p_polyline.plalinelen(-1); ++line_no)
         {
         PlaSegmentInt curr_line_seg = new PlaSegmentInt(p_polyline, line_no);
         int[] curr_intersections = curr_line_seg.border_intersections(this);
         for (int i = 0; i < curr_intersections.length; ++i)
            {
            int edge_no = curr_intersections[i];
            if (line_no != prev_intersection_line_no || edge_no != prev_intersection_edge_no)
               {
               result[intersection_count][0] = line_no;
               result[intersection_count][1] = edge_no;
               ++intersection_count;
               prev_intersection_line_no = line_no;
               prev_intersection_edge_no = edge_no;
               }
            }
         }
      int[][] normalized_result = new int[intersection_count][2];
      for (int j = 0; j < intersection_count; ++j)
         {
         for (int i = 0; i < 2; ++i)
            {
            normalized_result[j][i] = result[j][i];
            }
         }
      return normalized_result;
      }

   /**
    * Cuts out the parts of p_polyline in the interiour of this shape and returns a list of the remaining pieces of p_polyline.
    * Pieces completely contained in the border of this shape are not returned.
    */
   public Polyline[] cutout(Polyline p_polyline)
      {
      int[][] intersection_no = entrance_points(p_polyline);
      PlaPoint first_corner = p_polyline.corner_first();
      boolean first_corner_is_inside = contains_inside(first_corner);
      if (intersection_no.length == 0)
      // no intersections
         {
         if (first_corner_is_inside)
         // p_polyline is contained completely in this shape
            {
            return new Polyline[0];
            }
         // p_polyline is completely outside
         Polyline[] result = new Polyline[1];
         result[0] = p_polyline;
         return result;
         }
      Collection<Polyline> pieces = new LinkedList<Polyline>();
      int curr_intersection_no = 0;
      int[] curr_intersection_tuple = intersection_no[curr_intersection_no];
      PlaPoint first_intersection = p_polyline.plaline(curr_intersection_tuple[0]).intersection(border_line(curr_intersection_tuple[1]));
      if (!first_corner_is_inside)
         {
         // calculate outside piece at start
         if (!first_corner.equals(first_intersection))
            {
            // otherwise skip 1 point outside polyline at the start
            int curr_polyline_intersection_no = curr_intersection_tuple[0];
            
            PlaLineInt[] curr_lines = new PlaLineInt[curr_polyline_intersection_no + 2];
            p_polyline.plaline_copy(0, curr_lines, 0, curr_polyline_intersection_no + 1);
            
            // close the polyline piece with the intersected edge line.
            curr_lines[curr_polyline_intersection_no + 1] = border_line(curr_intersection_tuple[1]);

            try
               {
               Polyline curr_piece = new Polyline(curr_lines);
               pieces.add(curr_piece);
               }
            catch ( Exception exc )
               {
               
               }
            }
         ++curr_intersection_no;
         }
      while (curr_intersection_no < intersection_no.length - 1)
      // calculate the next outside polyline piece
         {
         curr_intersection_tuple = intersection_no[curr_intersection_no];
         int[] next_intersection_tuple = intersection_no[curr_intersection_no + 1];
         int curr_intersection_no_of_polyline = curr_intersection_tuple[0];
         int next_intersection_no_of_polyline = next_intersection_tuple[0];
         // check that at least 1 corner of p_polyline with number betweencurr_intersection_no_of_polyline and
         // next_intersection_no_of_polyline is not contained in this shape. Otherwise the part of p_polyline
         // between this intersections is completely contained in the border and can be ignored
         boolean insert_piece = false;
         for (int index = curr_intersection_no_of_polyline + 1; index < next_intersection_no_of_polyline; ++index)
            {
            if (is_outside(p_polyline.corner(index)))
               {
               insert_piece = true;
               break;
               }
            }

         if (insert_piece)
            {
            PlaLineInt[] curr_lines = new PlaLineInt[next_intersection_no_of_polyline - curr_intersection_no_of_polyline + 3];
            curr_lines[0] = border_line(curr_intersection_tuple[1]);
            
            System.arraycopy(p_polyline.lines_arr, curr_intersection_no_of_polyline, curr_lines, 1, curr_lines.length - 2);
            
            curr_lines[curr_lines.length - 1] = border_line(next_intersection_tuple[1]);

            try
               {
               Polyline curr_piece = new Polyline(curr_lines);
               pieces.add(curr_piece);
               }
            catch ( Exception exc )
               {
               System.err.println("poly empty1");
               }
            }
         curr_intersection_no += 2;
         }
      if (curr_intersection_no <= intersection_no.length - 1)
      // calculate outside piece at end
         {
         curr_intersection_tuple = intersection_no[curr_intersection_no];
         int curr_polyline_intersection_no = curr_intersection_tuple[0];
         PlaLineInt[] curr_lines = new PlaLineInt[p_polyline.plalinelen(-curr_polyline_intersection_no + 1)];

         curr_lines[0] = border_line(curr_intersection_tuple[1]);
         
         System.arraycopy(p_polyline.lines_arr, curr_polyline_intersection_no, curr_lines, 1, curr_lines.length - 1);

         try
            {
            Polyline curr_piece = new Polyline(curr_lines);
            pieces.add(curr_piece);
            }
         catch ( Exception exc )
            {
            System.err.println("poly empty1");
            }
         }
      Polyline[] result = new Polyline[pieces.size()];
      Iterator<Polyline> it = pieces.iterator();
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = it.next();
         }
      return result;
      }

   public ShapeTile[] split_to_convex()
      {
      ShapeTile[] result = new ShapeTile[1];
      result[0] = this;
      return result;
      }

   /**
    * Divides this shape into sections with width and height at most p_max_section_width of about equal size.
    */
   public ShapeTile[] divide_into_sections(double p_max_section_width)
      {
      if (is_empty())
         {
         ShapeTile[] result = new ShapeTile[1];
         result[0] = this;
         return result;
         }
      ShapeTile[] section_boxes = bounding_box().divide_into_sections(p_max_section_width);
      Collection<ShapeTile> section_list = new LinkedList<ShapeTile>();
      for (int i = 0; i < section_boxes.length; ++i)
         {
         ShapeTile curr_section = intersection_with_simplify(section_boxes[i]);
         if (curr_section.dimension() == PlaDimension.AREA)
            {
            section_list.add(curr_section);
            }
         }
      ShapeTile[] result = new ShapeTile[section_list.size()];
      Iterator<ShapeTile> it = section_list.iterator();
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = it.next();
         }
      return result;
      }

   /**
    * Checks, if p_line_segment has a common point with the interiour of this shape.
    */
   public boolean is_intersected_interiour_by(PlaSegmentInt p_line_segment)
      {
      PlaPointFloat float_start_point = p_line_segment.start_point_approx();
      PlaPointFloat float_end_point = p_line_segment.end_point_approx();

      PlaSide[] border_line_side_of_start_point_arr = new PlaSide[border_line_count()];
      PlaSide[] border_line_side_of_end_point_arr = new PlaSide[border_line_side_of_start_point_arr.length];
      for (int i = 0; i < border_line_side_of_start_point_arr.length; ++i)
         {
         PlaLineInt curr_border_line = border_line(i);
         PlaSide border_line_side_of_start_point = curr_border_line.side_of(float_start_point, 1);
         if (border_line_side_of_start_point == PlaSide.COLLINEAR)
            {
            border_line_side_of_start_point = curr_border_line.side_of(p_line_segment.start_point());
            }
         PlaSide border_line_side_of_end_point = curr_border_line.side_of(float_end_point, 1);
         if (border_line_side_of_end_point == PlaSide.COLLINEAR)
            {
            border_line_side_of_end_point = curr_border_line.side_of(p_line_segment.end_point());
            }
         if (border_line_side_of_start_point != PlaSide.ON_THE_RIGHT && border_line_side_of_end_point != PlaSide.ON_THE_RIGHT)
            {
            // both endpoints are outside the border_line,
            // no intersection possible
            return false;
            }
         border_line_side_of_start_point_arr[i] = border_line_side_of_start_point;
         border_line_side_of_end_point_arr[i] = border_line_side_of_end_point;
         }
      boolean start_point_is_inside = true;
      for (int i = 0; i < border_line_side_of_start_point_arr.length; ++i)
         {
         if (border_line_side_of_start_point_arr[i] != PlaSide.ON_THE_RIGHT)
            {
            start_point_is_inside = false;
            break;
            }
         }
      if (start_point_is_inside)
         {
         return true;
         }
      boolean end_point_is_inside = true;
      for (int i = 0; i < border_line_side_of_end_point_arr.length; ++i)
         {
         if (border_line_side_of_end_point_arr[i] != PlaSide.ON_THE_RIGHT)
            {
            end_point_is_inside = false;
            break;
            }
         }
      if (end_point_is_inside)
         {
         return true;
         }
      PlaLineInt segment_line = p_line_segment.get_line();
      // Check, if this line segments intersect a border line of p_shape.
      for (int i = 0; i < border_line_side_of_start_point_arr.length; ++i)
         {
         PlaSide border_line_side_of_start_point = border_line_side_of_start_point_arr[i];
         PlaSide border_line_side_of_end_point = border_line_side_of_end_point_arr[i];
         if (border_line_side_of_start_point != border_line_side_of_end_point)
            {
            if (border_line_side_of_start_point == PlaSide.COLLINEAR && border_line_side_of_end_point == PlaSide.ON_THE_LEFT || border_line_side_of_end_point == PlaSide.COLLINEAR
                  && border_line_side_of_start_point == PlaSide.ON_THE_LEFT)
               {
               // the interiour of p_shape is not intersected.
               continue;
               }
            PlaSide prev_corner_side = segment_line.side_of(corner_approx(i), 1);
            if (prev_corner_side == PlaSide.COLLINEAR)
               {
               prev_corner_side = segment_line.side_of(corner(i));
               }
            int next_corner_index;
            if (i == border_line_side_of_start_point_arr.length - 1)
               {
               next_corner_index = 0;
               }
            else
               {
               next_corner_index = i + 1;
               }
            PlaSide next_corner_side = segment_line.side_of(corner_approx(next_corner_index), 1);
            if (next_corner_side == PlaSide.COLLINEAR)
               {
               next_corner_side = segment_line.side_of(corner(next_corner_index));
               }
            if (prev_corner_side == PlaSide.ON_THE_LEFT && next_corner_side == PlaSide.ON_THE_RIGHT || prev_corner_side == PlaSide.ON_THE_RIGHT && next_corner_side == PlaSide.ON_THE_LEFT)
               {
               // this line segment crosses a border line of p_shape
               return true;
               }
            }
         }
      return false;
      }

   // auxiliary functions needed because the virtual function mechanism does
   // not work in parameter position
   abstract ShapeTile intersection(ShapeTileSimplex p_other);

   abstract ShapeTile intersection(ShapeTileOctagon p_other);

   abstract ShapeTile intersection(ShapeTileBox p_other);

   /**
    * Auxiliary function to implement the public function cutout(TileShape p_shape)
    */
   abstract ShapeTile[] cutout_from(ShapeTileBox p_shape);

   /**
    * Auxiliary function to implement the public function cutout(TileShape p_shape)
    */
   abstract ShapeTile[] cutout_from(ShapeTileOctagon p_shape);

   /**
    * Auxiliary function to implement the public function cutout(TileShape p_shape)
    */
   abstract ShapeTile[] cutout_from(ShapeTileSimplex p_shape);
   }
