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
 * IntBox.java
 *
 * Created on 2. Februar 2003, 14:09
 */

package freert.planar;

/**
 * Implements functionality of orthogonal rectangles in the plane with integer coordinates.
 * @author Alfons Wirtz
 */

public final class ShapeTileBox extends ShapeTileRegular
   {
   private static final long serialVersionUID = 1L;

   public static final ShapeTileBox EMPTY = new ShapeTileBox(PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT);

   // coordinates of the lower left corner
   public final PlaPointInt box_ll;
   // coordinates of the upper right corner
   public final PlaPointInt box_ur;   
   
   public ShapeTileBox (PlaPointInt a_point)
      {
      box_ll = a_point;
      box_ur = a_point;
      }
   
   public ShapeTileBox (PlaPoint a_point)
      {
      if ( a_point.is_rational() )
         {
         PlaPointFloat fp = a_point.to_float();
         double p_ll_x = Math.floor(fp.v_x);
         double p_ll_y = Math.floor(fp.v_y);
         double p_ur_x = Math.ceil(fp.v_x);
         double p_ur_y = Math.ceil(fp.v_y);
         
         box_ll = new PlaPointInt(p_ll_x, p_ll_y);
         box_ur = new PlaPointInt(p_ur_x, p_ur_y);
         }
      else 
         {
         PlaPointInt i_point = a_point.round();
         box_ll = i_point;
         box_ur = i_point;
         }
      }

   /**
    * Creates an IntBox from its lower left and upper right corners.
    */
   public ShapeTileBox(PlaPointInt p_ll, PlaPointInt p_ur)
      {
      box_ll = p_ll;
      box_ur = p_ur;
      }

   /**
    * creates an IntBox from the coordinates of its lower left and upper right corners.
    */
   public ShapeTileBox(int p_ll_x, int p_ll_y, int p_ur_x, int p_ur_y)
      {
      box_ll = new PlaPointInt(p_ll_x, p_ll_y);
      box_ur = new PlaPointInt(p_ur_x, p_ur_y);
      }
   
   @Override
   public boolean is_IntOctagon()
      {
      return true;
      }

   @Override
   public boolean is_empty()
      {
      return (box_ll.v_x > box_ur.v_x || box_ll.v_y > box_ur.v_y);
      }

   @Override
   public int border_line_count()
      {
      return 4;
      }

   /**
    * returns the horizontal extension of the box.
    */
   public int width()
      {
      return box_ur.v_x - box_ll.v_x;
      }

   /**
    * Returns the vertical extension of the box.
    */
   public int height()
      {
      return box_ur.v_y - box_ll.v_y;
      }

   @Override
   public double max_width()
      {
      return Math.max(box_ur.v_x - box_ll.v_x, box_ur.v_y - box_ll.v_y);
      }

   @Override
   public double min_width()
      {
      return Math.min(box_ur.v_x - box_ll.v_x, box_ur.v_y - box_ll.v_y);
      }

   @Override
   public double area()
      {
      return ((double) (box_ur.v_x - box_ll.v_x)) * ((double) (box_ur.v_y - box_ll.v_y));
      }

   @Override
   public double circumference()
      {
      return 2 * ((box_ur.v_x - box_ll.v_x) + (box_ur.v_y - box_ll.v_y));
      }

   @Override
   public PlaPointInt corner(int p_no)
      {
      if (p_no == 0)
         {
         return box_ll;
         }
      if (p_no == 1)
         {
         return new PlaPointInt(box_ur.v_x, box_ll.v_y);
         }
      if (p_no == 2)
         {
         return box_ur;
         }
      if (p_no == 3)
         {
         return new PlaPointInt(box_ll.v_x, box_ur.v_y);
         }
      
      throw new IllegalArgumentException("IntBox.corner: p_no out of range");
      }

   @Override
   public PlaDimension dimension()
      {
      if (is_empty()) return PlaDimension.EMPTY;

      if (box_ll.equals(box_ur)) return PlaDimension.POINT;

      if (box_ur.v_x == box_ll.v_x || box_ll.v_y == box_ur.v_y) return PlaDimension.LINE;

      return PlaDimension.AREA;
      }

   /**
    * Chechs, if p_point is located in the interiour of this box.
    */
   public boolean contains_inside(PlaPointInt p_point)
      {
      return p_point.v_x > box_ll.v_x && p_point.v_x < box_ur.v_x && p_point.v_y > box_ll.v_y && p_point.v_y < box_ur.v_y;
      }

   @Override
   public boolean is_IntBox()
      {
      return true;
      }

   @Override
   public ShapeTile simplify()
      {
      return this;
      }

   /**
    * Calculates the nearest point of this box to p_from_point.
    */
   public PlaPointFloat nearest_point(PlaPointFloat p_from_point)
      {
      double x;
      if (p_from_point.v_x <= box_ll.v_x)
         x = box_ll.v_x;
      else if (p_from_point.v_x >= box_ur.v_x)
         x = box_ur.v_x;
      else
         x = p_from_point.v_x;

      double y;
      if (p_from_point.v_y <= box_ll.v_y)
         y = box_ll.v_y;
      else if (p_from_point.v_y >= box_ur.v_y)
         y = box_ur.v_y;
      else
         y = p_from_point.v_y;

      return new PlaPointFloat(x, y);
      }

   /**
    * Calculates the sorted p_max_result_points nearest points on the border of this box. p_point is assumed to be located in the
    * interiour of this nox. The funtion is only imoplemented for p_max_result_points <= 2;
    */
   public PlaPointInt[] nearest_border_projections(PlaPointInt p_point, int p_max_result_points)
      {
      if (p_max_result_points <= 0)
         {
         return new PlaPointInt[0];
         }
      p_max_result_points = Math.min(p_max_result_points, 2);
      PlaPointInt[] result = new PlaPointInt[p_max_result_points];

      int lower_x_diff = p_point.v_x - box_ll.v_x;
      int upper_x_diff = box_ur.v_x - p_point.v_x;
      int lower_y_diff = p_point.v_y - box_ll.v_y;
      int upper_y_diff = box_ur.v_y - p_point.v_y;

      int min_diff;
      int second_min_diff;

      int nearest_projection_x = p_point.v_x;
      int nearest_projection_y = p_point.v_y;
      int second_nearest_projection_x = p_point.v_x;
      int second_nearest_projection_y = p_point.v_y;
      if (lower_x_diff <= upper_x_diff)
         {
         min_diff = lower_x_diff;
         second_min_diff = upper_x_diff;
         nearest_projection_x = box_ll.v_x;
         second_nearest_projection_x = box_ur.v_x;
         }
      else
         {
         min_diff = upper_x_diff;
         second_min_diff = lower_x_diff;
         nearest_projection_x = box_ur.v_x;
         second_nearest_projection_x = box_ll.v_x;
         }
      if (lower_y_diff < min_diff)
         {
         second_min_diff = min_diff;
         min_diff = lower_y_diff;
         second_nearest_projection_x = nearest_projection_x;
         second_nearest_projection_y = nearest_projection_y;
         nearest_projection_x = p_point.v_x;
         nearest_projection_y = box_ll.v_y;
         }
      else if (lower_y_diff < second_min_diff)
         {
         second_min_diff = lower_y_diff;
         second_nearest_projection_x = p_point.v_x;
         second_nearest_projection_y = box_ll.v_y;
         }
      if (upper_y_diff < min_diff)
         {
         second_min_diff = min_diff;
         min_diff = upper_y_diff;
         second_nearest_projection_x = nearest_projection_x;
         second_nearest_projection_y = nearest_projection_y;
         nearest_projection_x = p_point.v_x;
         nearest_projection_y = box_ur.v_y;
         }
      else if (upper_y_diff < second_min_diff)
         {
         second_min_diff = upper_y_diff;
         second_nearest_projection_x = p_point.v_x;
         second_nearest_projection_y = box_ur.v_y;
         }
      result[0] = new PlaPointInt(nearest_projection_x, nearest_projection_y);
      if (result.length > 1)
         {
         result[1] = new PlaPointInt(second_nearest_projection_x, second_nearest_projection_y);
         }

      return result;
      }

   /**
    * Calculates distance of this box to p_from_point.
    */
   @Override
   public double distance(PlaPointFloat p_from_point)
      {
      return p_from_point.distance(nearest_point(p_from_point));
      }

   /**
    * Computes the weighted distance to the box p_other.
    */
   public double weighted_distance(ShapeTileBox p_other, double p_horizontal_weight, double p_vertical_weight)
      {
      double result;

      double max_ll_x = Math.max(box_ll.v_x, p_other.box_ll.v_x);
      double max_ll_y = Math.max(box_ll.v_y, p_other.box_ll.v_y);
      double min_ur_x = Math.min(box_ur.v_x, p_other.box_ur.v_x);
      double min_ur_y = Math.min(box_ur.v_y, p_other.box_ur.v_y);

      if (min_ur_x >= max_ll_x)
         {
         result = Math.max(p_vertical_weight * (max_ll_y - min_ur_y), 0);
         }
      else if (min_ur_y >= max_ll_y)
         {
         result = Math.max(p_horizontal_weight * (max_ll_x - min_ur_x), 0);
         }
      else
         {
         double delta_x = max_ll_x - min_ur_x;
         double delta_y = max_ll_y - min_ur_y;
         delta_x *= p_horizontal_weight;
         delta_y *= p_vertical_weight;
         result = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
         }
      return result;
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      return this;
      }

   @Override
   public ShapeTileOctagon bounding_octagon()
      {
      return to_IntOctagon();
      }

   @Override
   public boolean is_bounded()
      {
      return true;
      }

   @Override
   public ShapeTileBox bounding_tile()
      {
      return this;
      }

   @Override
   public boolean corner_is_bounded(int p_no)
      {
      return true;
      }

   @Override
   public ShapeTileRegular union(ShapeTileRegular p_other)
      {
      return p_other.union(this);
      }

   @Override
   public ShapeTileBox union(ShapeTileBox p_other)
      {
      int llx = Math.min(box_ll.v_x, p_other.box_ll.v_x);
      int lly = Math.min(box_ll.v_y, p_other.box_ll.v_y);
      int urx = Math.max(box_ur.v_x, p_other.box_ur.v_x);
      int ury = Math.max(box_ur.v_y, p_other.box_ur.v_y);
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   @Override
   public ShapeTileBox intersection(ShapeTileBox p_other)
      {
      if (p_other.box_ll.v_x > box_ur.v_x)
         {
         return EMPTY;
         }
      if (p_other.box_ll.v_y > box_ur.v_y)
         {
         return EMPTY;
         }
      if (box_ll.v_x > p_other.box_ur.v_x)
         {
         return EMPTY;
         }
      if (box_ll.v_y > p_other.box_ur.v_y)
         {
         return EMPTY;
         }
      int llx = Math.max(box_ll.v_x, p_other.box_ll.v_x);
      int urx = Math.min(box_ur.v_x, p_other.box_ur.v_x);
      int lly = Math.max(box_ll.v_y, p_other.box_ll.v_y);
      int ury = Math.min(box_ur.v_y, p_other.box_ur.v_y);
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   /**
    * returns the intersection of this box with a ConvexShape
    */
   @Override
   public ShapeTile intersection(ShapeTile p_other)
      {
      return p_other.intersection(this);
      }

   @Override
   ShapeTileOctagon intersection(ShapeTileOctagon p_other)
      {
      return p_other.intersection(to_IntOctagon());
      }

   @Override
   ShapeTileSimplex intersection(ShapeTileSimplex p_other)
      {
      return p_other.intersection(to_Simplex());
      }

   @Override
   public boolean intersects(PlaShape p_other)
      {
      return p_other.intersects(this);
      }

   @Override
   public boolean intersects(ShapeTileBox p_other)
      {
      if (p_other.box_ll.v_x > box_ur.v_x)
         return false;
      if (p_other.box_ll.v_y > box_ur.v_y)
         return false;
      if (box_ll.v_x > p_other.box_ur.v_x)
         return false;
      if (box_ll.v_y > p_other.box_ur.v_y)
         return false;
      return true;
      }

   /**
    * Returns true, if this box intersects with p_other and the intersection is 2-dimensional.
    */
   public boolean overlaps(ShapeTileBox p_other)
      {
      if (p_other.box_ll.v_x >= box_ur.v_x)
         return false;
      if (p_other.box_ll.v_y >= box_ur.v_y)
         return false;
      if (box_ll.v_x >= p_other.box_ur.v_x)
         return false;
      if (box_ll.v_y >= p_other.box_ur.v_y)
         return false;
      return true;
      }

   @Override
   public boolean contains(ShapeTileRegular p_other)
      {
      return p_other.is_contained_in(this);
      }

   @Override
   public ShapeTileRegular bounding_shape(ShapeBoundingOct p_dirs)
      {
      return p_dirs.bounds(this);
      }

   /**
    * Enlarges the box by p_offset. Contrary to the offset() method the result is an IntOctagon, not an IntBox.
    */
   @Override
   public ShapeTileOctagon enlarge(double p_offset)
      {
      return bounding_octagon().offset(p_offset);
      }

   @Override
   public ShapeTileBox translate_by(PlaVectorInt p_rel_coor)
      {
      if (p_rel_coor.equals(PlaVectorInt.ZERO)) return this;

      PlaPointInt new_ll = box_ll.translate_by(p_rel_coor);
      PlaPointInt new_ur = box_ur.translate_by(p_rel_coor);

      return new ShapeTileBox(new_ll, new_ur);
      }

   @Override
   public ShapeTileBox turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      PlaPointInt p1 = box_ll.turn_90_degree(p_factor, p_pole);
      PlaPointInt p2 = box_ur.turn_90_degree(p_factor, p_pole);

      int llx = Math.min(p1.v_x, p2.v_x);
      int lly = Math.min(p1.v_y, p2.v_y);
      int urx = Math.max(p1.v_x, p2.v_x);
      int ury = Math.max(p1.v_y, p2.v_y);
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   @Override
   public PlaLineInt border_line(int p_no)
      {
      PlaPointInt p_a;
      PlaPointInt p_b;

      switch (p_no)
         {
         case 0:
            // lower boundary line
            p_a = new PlaPointInt(0,box_ll.v_y);
            p_b = new PlaPointInt(1, box_ll.v_y);
            break;
         case 1:
            // right boundary line
            p_a = new PlaPointInt( box_ur.v_x, 0);
            p_b = new PlaPointInt( box_ur.v_x, 1);
            break;
         case 2:
            // upper boundary line
            p_a = new PlaPointInt( 0, box_ur.v_y);
            p_b = new PlaPointInt( -1, box_ur.v_y);
            break;
         case 3:
            // left boundary line
            p_a = new PlaPointInt( box_ll.v_x, 0);
            p_b = new PlaPointInt( box_ll.v_x, -1);
            break;
         default:
            throw new IllegalArgumentException("IntBox.edge_line: p_no out of range");
         }
      
      return new PlaLineInt(p_a,p_b);
      }

   @Override
   public int border_line_index(PlaLineInt p_line)
      {
      System.out.println("edge_index_of_line not yet implemented for IntBoxes");
      return -1;
      }

   /**
    * Returns the box offseted by p_dist. If p_dist > 0, the offset is to the outside, else to the inside.
    */
   @Override
   public ShapeTileBox offset(double p_dist)
      {
      if (p_dist == 0 || is_empty())
         {
         return this;
         }
      int dist = (int) Math.round(p_dist);
      PlaPointInt lower_left = new PlaPointInt(box_ll.v_x - dist, box_ll.v_y - dist);
      PlaPointInt upper_right = new PlaPointInt(box_ur.v_x + dist, box_ur.v_y + dist);
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * Returns the box, where the horizontal boundary is offseted by p_dist. If p_dist > 0, the offset is to the outside, else to the
    * inside.
    */
   public ShapeTileBox horizontal_offset(double p_dist)
      {
      if (p_dist == 0 || is_empty())
         {
         return this;
         }
      int dist = (int) Math.round(p_dist);
      PlaPointInt lower_left = new PlaPointInt(box_ll.v_x - dist, box_ll.v_y);
      PlaPointInt upper_right = new PlaPointInt(box_ur.v_x + dist, box_ur.v_y);
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * Returns the box, where the vertical boundary is offseted by p_dist. If p_dist > 0, the offset is to the outside, else to the
    * inside.
    */
   public ShapeTileBox vertical_offset(double p_dist)
      {
      if (p_dist == 0 || is_empty())
         {
         return this;
         }
      int dist = (int) Math.round(p_dist);
      PlaPointInt lower_left = new PlaPointInt(box_ll.v_x, box_ll.v_y - dist);
      PlaPointInt upper_right = new PlaPointInt(box_ur.v_x, box_ur.v_y + dist);
      return new ShapeTileBox(lower_left, upper_right);
      }

   /**
    * Shrinks the width and height of the box by the input width. The box will not vanish completely.
    */
   public ShapeTileBox shrink(int p_width)
      {
      int ll_x;
      int ur_x;
      if (2 * p_width <= box_ur.v_x - box_ll.v_x)
         {
         ll_x = box_ll.v_x + p_width;
         ur_x = box_ur.v_x - p_width;
         }
      else
         {
         ll_x = (box_ll.v_x + box_ur.v_x) / 2;
         ur_x = ll_x;
         }
      int ll_y;
      int ur_y;
      if (2 * p_width <= box_ur.v_y - box_ll.v_y)
         {
         ll_y = box_ll.v_y + p_width;
         ur_y = box_ur.v_y - p_width;
         }
      else
         {
         ll_y = (box_ll.v_y + box_ur.v_y) / 2;
         ur_y = ll_y;
         }
      return new ShapeTileBox(ll_x, ll_y, ur_x, ur_y);
      }

   @Override
   public PlaSide compare(ShapeTileRegular p_other, int p_edge_no)
      {
      PlaSide result = p_other.compare(this, p_edge_no);
      return result.negate();
      }

   @Override
   public PlaSide compare(ShapeTileBox p_other, int p_edge_no)
      {
      PlaSide result;
      switch (p_edge_no)
         {
         case 0:
            // compare the lower edge line
            if (box_ll.v_y > p_other.box_ll.v_y)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (box_ll.v_y < p_other.box_ll.v_y)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 1:
            // compare the right edge line
            if (box_ur.v_x < p_other.box_ur.v_x)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (box_ur.v_x > p_other.box_ur.v_x)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 2:
            // compare the upper edge line
            if (box_ur.v_y < p_other.box_ur.v_y)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (box_ur.v_y > p_other.box_ur.v_y)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 3:
            // compare the left edge line
            if (box_ll.v_x > p_other.box_ll.v_x)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (box_ll.v_x < p_other.box_ll.v_x)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;
         default:
            throw new IllegalArgumentException("IntBox.compare: p_edge_no out of range");

         }
      return result;
      }

   /**
    * Returns an object of class IntOctagon defining the same shape
    */
   public ShapeTileOctagon to_IntOctagon()
      {
      return new ShapeTileOctagon(box_ll.v_x, box_ll.v_y, box_ur.v_x, box_ur.v_y, box_ll.v_x - box_ur.v_y, box_ur.v_x - box_ll.v_y, box_ll.v_x + box_ll.v_y, box_ur.v_x + box_ur.v_y);
      }

   /**
    * Returns an object of class Simplex defining the same shape
    */
   @Override
   public ShapeTileSimplex to_Simplex()
      {
      PlaLineInt[] line_arr;
      if (is_empty())
         {
         line_arr = new PlaLineInt[0];
         }
      else
         {
         line_arr = new PlaLineInt[4];
         line_arr[0] = new PlaLineInt(box_ll, PlaDirection.RIGHT);
         line_arr[1] = new PlaLineInt(box_ur, PlaDirection.UP);
         line_arr[2] = new PlaLineInt(box_ur, PlaDirection.LEFT);
         line_arr[3] = new PlaLineInt(box_ll, PlaDirection.DOWN);
         }
      return new ShapeTileSimplex(line_arr);
      }

   @Override
   public boolean is_contained_in(ShapeTileBox p_other)
      {
      if (is_empty() || this == p_other)
         {
         return true;
         }
      if (box_ll.v_x < p_other.box_ll.v_x || box_ll.v_y < p_other.box_ll.v_y || box_ur.v_x > p_other.box_ur.v_x || box_ur.v_y > p_other.box_ur.v_y)
         {
         return false;
         }
      return true;
      }

   /**
    * Return true, if p_other is contained in the interiour of this box.
    */
   public boolean contains_in_interiour(ShapeTileBox p_other)
      {
      if (p_other.is_empty())
         {
         return true;
         }
      if (p_other.box_ll.v_x <= box_ll.v_x || p_other.box_ll.v_y <= box_ll.v_y || p_other.box_ur.v_x >= box_ur.v_x || p_other.box_ur.v_y >= box_ur.v_y)
         {
         return false;
         }
      return true;
      }

   /**
    * Calculates the part of p_from_box, which has minimal distance to this box.
    */
   public ShapeTileBox nearest_part(ShapeTileBox p_from_box)
      {
      int ll_x;

      if (p_from_box.box_ll.v_x >= box_ll.v_x)
         {
         ll_x = p_from_box.box_ll.v_x;
         }
      else if (p_from_box.box_ur.v_x >= box_ll.v_x)
         {
         ll_x = box_ll.v_x;
         }
      else
         {
         ll_x = p_from_box.box_ur.v_x;
         }

      int ur_x;

      if (p_from_box.box_ur.v_x <= box_ur.v_x)
         {
         ur_x = p_from_box.box_ur.v_x;
         }
      else if (p_from_box.box_ll.v_x <= box_ur.v_x)
         {
         ur_x = box_ur.v_x;
         }
      else
         {
         ur_x = p_from_box.box_ll.v_x;
         }

      int ll_y;

      if (p_from_box.box_ll.v_y >= box_ll.v_y)
         {
         ll_y = p_from_box.box_ll.v_y;
         }
      else if (p_from_box.box_ur.v_y >= box_ll.v_y)
         {
         ll_y = box_ll.v_y;
         }
      else
         {
         ll_y = p_from_box.box_ur.v_y;
         }

      int ur_y;

      if (p_from_box.box_ur.v_y <= box_ur.v_y)
         {
         ur_y = p_from_box.box_ur.v_y;
         }
      else if (p_from_box.box_ll.v_y <= box_ur.v_y)
         {
         ur_y = box_ur.v_y;
         }
      else
         {
         ur_y = p_from_box.box_ll.v_y;
         }
      return new ShapeTileBox(ll_x, ll_y, ur_x, ur_y);
      }

   @Override
   public boolean is_contained_in(ShapeTileOctagon p_other)
      {
      return p_other.contains(to_IntOctagon());
      }

   @Override
   public boolean intersects(ShapeTileOctagon p_other)
      {
      return p_other.intersects(to_IntOctagon());
      }

   @Override
   public boolean intersects(ShapeTileSimplex p_other)
      {
      return p_other.intersects(to_Simplex());
      }

   @Override
   public boolean intersects(ShapeCircle p_other)
      {
      return p_other.intersects(this);
      }

   @Override
   public ShapeTileOctagon union(ShapeTileOctagon p_other)
      {
      return p_other.union(to_IntOctagon());
      }

   @Override
   public PlaSide compare(ShapeTileOctagon p_other, int p_edge_no)
      {
      return to_IntOctagon().compare(p_other, p_edge_no);
      }

   /**
    * Divides this box into sections with width and height at most p_max_section_width of about equal size.
    */
   @Override
   public ShapeTileBox[] divide_into_sections(double p_max_section_width)
      {
      if (p_max_section_width <= 0)
         {
         return new ShapeTileBox[0];
         }
      double length = box_ur.v_x - box_ll.v_x;
      double height = box_ur.v_y - box_ll.v_y;
      int x_count = (int) Math.ceil(length / p_max_section_width);
      int y_count = (int) Math.ceil(height / p_max_section_width);
      int section_length_x = (int) Math.ceil(length / x_count);
      int section_length_y = (int) Math.ceil(height / y_count);
      ShapeTileBox[] result = new ShapeTileBox[x_count * y_count];
      int curr_index = 0;
      for (int j = 0; j < y_count; ++j)
         {
         int curr_lly = box_ll.v_y + j * section_length_y;
         int curr_ury;
         if (j == (y_count - 1))
            {
            curr_ury = box_ur.v_y;
            }
         else
            {
            curr_ury = curr_lly + section_length_y;
            }
         for (int i = 0; i < x_count; ++i)
            {
            int curr_llx = box_ll.v_x + i * section_length_x;
            int curr_urx;
            if (i == (x_count - 1))
               {
               curr_urx = box_ur.v_x;
               }
            else
               {
               curr_urx = curr_llx + section_length_x;
               }
            result[curr_index] = new ShapeTileBox(curr_llx, curr_lly, curr_urx, curr_ury);
            ++curr_index;
            }
         }
      return result;
      }

   @Override
   public ShapeTile[] cutout(ShapeTile p_shape)
      {
      ShapeTile[] tmp_result = p_shape.cutout_from(this);
      ShapeTile[] result = new ShapeTile[tmp_result.length];
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = tmp_result[i].simplify();
         }
      return result;
      }

   @Override
   ShapeTileBox[] cutout_from(ShapeTileBox p_d)
      {
      ShapeTileBox c = intersection(p_d);
      
      if (is_empty() || c.dimension().less(dimension()) )
         {
         // there is only an overlap at the border
         ShapeTileBox[] result = new ShapeTileBox[1];
         result[0] = p_d;
         return result;
         }

      ShapeTileBox[] result = new ShapeTileBox[4];

      result[0] = new ShapeTileBox(p_d.box_ll.v_x, p_d.box_ll.v_y, c.box_ur.v_x, c.box_ll.v_y);

      result[1] = new ShapeTileBox(p_d.box_ll.v_x, c.box_ll.v_y, c.box_ll.v_x, p_d.box_ur.v_y);

      result[2] = new ShapeTileBox(c.box_ur.v_x, p_d.box_ll.v_y, p_d.box_ur.v_x, c.box_ur.v_y);

      result[3] = new ShapeTileBox(c.box_ll.v_x, c.box_ur.v_y, p_d.box_ur.v_x, p_d.box_ur.v_y);

      // now the division will be optimised, so that the cumulative
      // circumference will be minimal.

      ShapeTileBox b = null;

      if (c.box_ll.v_x - p_d.box_ll.v_x > c.box_ll.v_y - p_d.box_ll.v_y)
         {
         // switch left dividing line to lower
         b = result[0];
         result[0] = new ShapeTileBox(c.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         b = result[1];
         result[1] = new ShapeTileBox(b.box_ll.v_x, p_d.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         }
      if (p_d.box_ur.v_y - c.box_ur.v_y > c.box_ll.v_x - p_d.box_ll.v_x)
         {
         // switch upper dividing line to the left
         b = result[1];
         result[1] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, c.box_ur.v_y);
         b = result[3];
         result[3] = new ShapeTileBox(p_d.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         }
      if (p_d.box_ur.v_x - c.box_ur.v_x > p_d.box_ur.v_y - c.box_ur.v_y)
         {
         // switch right dividing line to upper
         b = result[2];
         result[2] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, p_d.box_ur.v_y);
         b = result[3];
         result[3] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, c.box_ur.v_x, b.box_ur.v_y);
         }
      if (c.box_ll.v_y - p_d.box_ll.v_y > p_d.box_ur.v_x - c.box_ur.v_x)
         {
         // switch lower dividing line to the left
         b = result[0];
         result[0] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, p_d.box_ur.v_x, b.box_ur.v_y);
         b = result[2];
         result[2] = new ShapeTileBox(b.box_ll.v_x, c.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         }
      return result;
      }

   @Override
   ShapeTileSimplex[] cutout_from(ShapeTileSimplex p_simplex)
      {
      return to_Simplex().cutout_from(p_simplex);
      }

   @Override
   ShapeTileOctagon[] cutout_from(ShapeTileOctagon p_oct)
      {
      return to_IntOctagon().cutout_from(p_oct);
      }


   }