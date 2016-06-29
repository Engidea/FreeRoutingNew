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
 * Circle.java
 *
 * Created on 4. Juni 2003, 07:29
 */

package freert.planar;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Describes functionality of a circle shape in the plane
 * So, if we look at the class hierarchy.... this should not be a Shape, since is is a curved one...
 *
 * @author Alfons Wirtz
 */
public final class ShapeCircle implements ShapeConvex , Serializable
   {
   private static final long serialVersionUID = 1L;

   public final PlaPointInt center;
   public final int radius;
   
   public ShapeCircle(PlaPointInt p_center, int p_radius)
      {
      center = p_center;
      if (p_radius < 0)
         {
         System.out.println("Circle: unexpected negative radius");
         radius = -p_radius;
         }
      else
         {
         radius = p_radius;
         }
      }
   
   public ShapeCircle(PlaPointInt p_center, double p_radius)
      {
      this(p_center, (int)Math.round(p_radius));
      }
   

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }

   @Override
   public boolean is_empty()
      {
      return false;
      }

   @Override
   public boolean is_bounded()
      {
      return true;
      }

   @Override
   public PlaDimension dimension()
      {
      // circle is reduced to a point
      if (radius == 0) return PlaDimension.POINT;
    
      return PlaDimension.AREA;
      }

   @Override
   public double circumference()
      {
      return 2.0 * Math.PI * radius;
      }

   @Override
   public double area()
      {
      return (Math.PI * radius) * radius;
      }

   @Override
   public PlaPointFloat centre_of_gravity()
      {
      return center.to_float();
      }

   @Override
   public boolean is_outside(PlaPoint p_point)
      {
      PlaPointFloat fp = p_point.to_float();
      return fp.distance_square(center.to_float()) > (double) radius * radius;
      }

   @Override
   public boolean contains(PlaPoint p_point)
      {
      return !is_outside(p_point);
      }

   @Override
   public boolean contains_inside(PlaPoint p_point)
      {
      PlaPointFloat fp = p_point.to_float();
      return fp.distance_square(center.to_float()) < (double) radius * radius;
      }

   @Override
   public boolean contains_on_border(PlaPoint p_point)
      {
      PlaPointFloat fp = p_point.to_float();
      return fp.distance_square(center.to_float()) == (double) radius * radius;
      }

   @Override
   public boolean contains(PlaPointFloat p_point)
      {
      return p_point.distance_square(center.to_float()) <= (double) radius * radius;
      }

   @Override
   public double distance(PlaPointFloat p_point)
      {
      double d = p_point.distance(center.to_float()) - radius;
      return Math.max(d, 0.0);
      }

   @Override
   public double smallest_radius()
      {
      return radius;
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      int llx = center.v_x - radius;
      int urx = center.v_x + radius;
      int lly = center.v_y - radius;
      int ury = center.v_y + radius;
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   @Override
   public ShapeTileOctagon bounding_octagon()
      {
      int lx = center.v_x - radius;
      int rx = center.v_x + radius;
      int ly = center.v_y - radius;
      int uy = center.v_y + radius;

      double sqrt2_minus_1 = Math.sqrt(2) - 1;
      int ceil_corner_value = (int) Math.ceil(sqrt2_minus_1 * radius);
      int floor_corner_value = (int) Math.floor(sqrt2_minus_1 * radius);

      int ulx = lx - (center.v_y + floor_corner_value);
      int lrx = rx - (center.v_y - ceil_corner_value);
      int llx = lx + (center.v_y - floor_corner_value);
      int urx = rx + (center.v_y + ceil_corner_value);
      return new ShapeTileOctagon(lx, ly, rx, uy, ulx, lrx, llx, urx);
      }

   @Override
   public ShapeTile bounding_tile()
      {
      return bounding_octagon();
      // the following caused problems with the spring_over algorithm in routing.
      /*
       * if (precalculated_bounding_tile == null) { precalculated_bounding_tile = bounding_tile(c_max_approximation_segment_length); } return precalculated_bounding_tile;
       */
      }

   /**
    * Creates a bounding tile shape around this circle, so that the length of the line segments of the tile is at most p_max_segment_length.
   public ShapeTile bounding_tile(int p_max_segment_length)
      {
      int quadrant_division_count = radius / p_max_segment_length + 1;
      
      if (quadrant_division_count <= 2)
         {
         return bounding_octagon();
         }
      
      PlaLineInt[] tangent_line_arr = new PlaLineInt[quadrant_division_count * 4];
      for (int i = 0; i < quadrant_division_count; ++i)
         {
         // calculate the tangential points in the first quadrant
         PlaVector border_delta;
         if (i == 0)
            {
            border_delta = new PlaVectorInt(radius, 0);
            }
         else
            {
            double curr_angle = i * Math.PI / (2.0 * quadrant_division_count);
            int curr_x = (int) Math.ceil(Math.sin(curr_angle) * radius);
            int curr_y = (int) Math.ceil(Math.cos(curr_angle) * radius);
            border_delta = new PlaVectorInt(curr_x, curr_y);
            }
         PlaPoint curr_a = center.translate_by(border_delta);
         PlaPoint curr_b = curr_a.turn_90_degree(1, center);
         PlaDirection curr_dir = PlaDirection.get_instance(curr_b.difference_by(center));
         PlaLineInt curr_tangent = new PlaLineInt(curr_a, curr_dir);
         tangent_line_arr[quadrant_division_count + i] = curr_tangent;
         tangent_line_arr[2 * quadrant_division_count + i] = curr_tangent.turn_90_degree(1, center);
         tangent_line_arr[3 * quadrant_division_count + i] = curr_tangent.turn_90_degree(2, center);
         tangent_line_arr[i] = curr_tangent.turn_90_degree(3, center);
         }
      return ShapeTile.get_instance(tangent_line_arr);
      }
    */

   @Override
   public boolean is_contained_in(ShapeTileBox p_box)
      {
      if (p_box.box_ll.v_x > center.v_x - radius)
         {
         return false;
         }
      if (p_box.box_ll.v_y > center.v_y - radius)
         {
         return false;
         }
      if (p_box.box_ur.v_x < center.v_x + radius)
         {
         return false;
         }
      if (p_box.box_ur.v_y < center.v_y + radius)
         {
         return false;
         }
      return true;
      }

   @Override
   public ShapeCircle rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      PlaPointInt new_center = center.rotate_90_deg(p_factor, p_pole);
      return new ShapeCircle(new_center, radius);
      }

   @Override
   public ShapeCircle rotate_rad(double p_angle, PlaPointFloat p_pole)
      {
      PlaPointInt new_center = center.to_float().rotate_rad(p_angle, p_pole).round();
      return new ShapeCircle(new_center, radius);
      }

   @Override
   public ShapeCircle mirror_vertical(PlaPointInt p_pole)
      {
      PlaPointInt new_center = center.mirror_vertical(p_pole);
      
      return new ShapeCircle(new_center, radius);
      }

   @Override
   public ShapeCircle mirror_horizontal(PlaPointInt p_pole)
      {
      PlaPointInt new_center = center.mirror_horizontal(p_pole);
      return new ShapeCircle(new_center, radius);
      }

   @Override
   public double max_width()
      {
      return 2 * radius;
      }

   @Override
   public double min_width()
      {
      return 2 * radius;
      }

   @Override
   public ShapeTileRegular bounding_shape()
      {
      return bounding_octagon();
      }

   @Override
   public ShapeCircle offset(double p_offset)
      {
      double new_radius = radius + p_offset;
      int r = (int) Math.round(new_radius);
      return new ShapeCircle(center, r);
      }

   @Override
   public ShapeCircle shrink(double p_offset)
      {
      double new_radius = radius - p_offset;
      int r = Math.max((int) Math.round(new_radius), 1);
      return new ShapeCircle(center, r);
      }


   @Override
   public ShapeCircle translate_by(PlaVectorInt p_vector)
      {
      if (p_vector.equals(PlaVectorInt.ZERO)) return this;

      PlaPointInt new_center = center.translate_by(p_vector);
      
      return new ShapeCircle(new_center, radius);
      }

   @Override
   public PlaPointFloat nearest_point_approx(PlaPointFloat p_point)
      {
      System.out.println("Circle.nearest_point_approx not yet implemented");
      return null;
      }

   @Override
   public double border_distance(PlaPointFloat p_point)
      {
      double d = p_point.distance(center.to_float()) - radius;
      return Math.abs(d);
      }

   @Override
   public ShapeCircle enlarge(double p_offset)
      {
      if (p_offset == 0) return this;

      int new_radius = radius + (int) Math.round(p_offset);
      return new ShapeCircle(center, new_radius);
      }

   @Override
   public boolean intersects(PlaShape p_other)
      {
      return p_other.intersects(this);
      }

   @Override
   public ArrayList<Polyline> cutout(Polyline p_polyline)
      {
      System.out.println("Circle.cutout not yet implemented");
      return null;
      }

   @Override
   public boolean intersects(ShapeCircle p_other)
      {
      double d_square = radius + p_other.radius;
      d_square *= d_square;
      return center.distance_square(p_other.center) <= d_square;
      }

   @Override
   public boolean intersects(ShapeTileBox p_box)
      {
      return p_box.distance(center.to_float()) <= radius;
      }

   @Override
   public boolean intersects(ShapeTileOctagon p_oct)
      {
      return p_oct.distance(center.to_float()) <= radius;
      }

   @Override
   public boolean intersects(ShapeTileSimplex p_simplex)
      {
      return p_simplex.distance(center.to_float()) <= radius;
      }

   @Override
   public ShapeTile[] split_to_convex()
      {
      ShapeTile[] result = new ShapeTile[1];
      result[0] = bounding_tile();
      return result;
      }

   @Override
   public ShapeCircle get_border()
      {
      return this;
      }

   @Override
   public PlaShape[] get_holes()
      {
      return new PlaShape[0];
      }

   @Override
   public PlaPointFloat[] corner_approx_arr()
      {
      return new PlaPointFloat[0];
      }

   @Override
   public String toString()
      {
      return to_string(java.util.Locale.ENGLISH);
      }

   public String to_string(java.util.Locale p_locale)
      {
      String result = "Circle: ";
      
      String center_string = "center " + center.toString();
      result += center_string;
      
      java.text.NumberFormat nf = java.text.NumberFormat.getInstance(p_locale);
      String radius_string = "radius " + nf.format(radius);
      result += radius_string;
      return result;
      }

   }
