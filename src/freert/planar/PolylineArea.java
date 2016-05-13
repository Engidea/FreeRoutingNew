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
 * PolylineArea.java
 *
 * Created on 19. Juni 2003, 07:58
 */
package freert.planar;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A PolylineArea is an Area, where the outside border curve and the hole borders consist of straight lines.
 *
 * @author Alfons Wirtz
 */
public final class PolylineArea implements PlaArea, Serializable
   {
   private static final long serialVersionUID = 1L;

   final ShapePolyline border_shape;
   final ShapePolyline[] hole_arr;
   transient private ShapeTile[] precalculated_convex_pieces = null;

   public PolylineArea(ShapePolyline p_border_shape, ShapePolyline[] p_hole_arr)
      {
      border_shape = p_border_shape;
      hole_arr = p_hole_arr;
      }

   @Override
   public final boolean is_NaN ()
      {
      return false;
      }

   @Override
   public PlaDimension dimension()
      {
      return border_shape.dimension();
      }

   @Override
   public boolean is_bounded()
      {
      return border_shape.is_bounded();
      }

   @Override
   public boolean is_empty()
      {
      return border_shape.is_empty();
      }

   @Override
   public boolean is_contained_in(ShapeTileBox p_box)
      {
      return border_shape.is_contained_in(p_box);
      }

   @Override
   public ShapePolyline get_border()
      {
      return border_shape;
      }

   @Override
   public ShapePolyline[] get_holes()
      {
      return hole_arr;
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      return border_shape.bounding_box();
      }

   @Override
   public ShapeTileOctagon bounding_octagon()
      {
      return border_shape.bounding_octagon();
      }

   @Override
   public boolean contains(PlaPointFloat p_point)
      {
      if (!border_shape.contains(p_point))
         {
         return false;
         }
      for (int i = 0; i < hole_arr.length; ++i)
         {
         if (hole_arr[i].contains(p_point))
            {
            return false;
            }
         }
      return true;
      }

   @Override
   public boolean contains(PlaPoint p_point)
      {
      if (!border_shape.contains(p_point))
         {
         return false;
         }
      for (int i = 0; i < hole_arr.length; ++i)
         {
         if (hole_arr[i].contains_inside(p_point))
            {
            return false;
            }
         }
      return true;
      }

   @Override
   public PlaPointFloat nearest_point_approx(PlaPointFloat p_from_point)
      {
      double min_dist = Double.MAX_VALUE;
      PlaPointFloat result = null;
      ShapeTile[] convex_shapes = split_to_convex();
      for (int i = 0; i < convex_shapes.length; ++i)
         {
         PlaPointFloat curr_nearest_point = convex_shapes[i].nearest_point_approx(p_from_point);
         double curr_dist = curr_nearest_point.length_square(p_from_point);
         if (curr_dist < min_dist)
            {
            min_dist = curr_dist;
            result = curr_nearest_point;
            }
         }
      return result;
      }

   @Override
   public PolylineArea translate_by(PlaVectorInt p_vector)
      {
      if (p_vector.equals(PlaVectorInt.ZERO)) return this;

      ShapePolyline translated_border = border_shape.translate_by(p_vector);

      ShapePolyline[] translated_holes = new ShapePolyline[hole_arr.length];
      
      for (int index = 0; index < hole_arr.length; ++index)
         {
         translated_holes[index] = hole_arr[index].translate_by(p_vector);
         }
      
      return new PolylineArea(translated_border, translated_holes);
      }

   @Override
   public PlaPointFloat[] corner_approx_arr()
      {
      int corner_count = border_shape.border_line_count();
      for (int i = 0; i < hole_arr.length; ++i)
         {
         corner_count += hole_arr[i].border_line_count();
         }
      PlaPointFloat[] result = new PlaPointFloat[corner_count];
      PlaPointFloat[] curr_corner_arr = border_shape.corner_approx_arr();
      System.arraycopy(curr_corner_arr, 0, result, 0, curr_corner_arr.length);
      int dest_pos = curr_corner_arr.length;
      for (int i = 0; i < hole_arr.length; ++i)
         {
         curr_corner_arr = hole_arr[i].corner_approx_arr();
         System.arraycopy(curr_corner_arr, 0, result, dest_pos, curr_corner_arr.length);
         dest_pos += curr_corner_arr.length;
         }
      return result;
      }

   /**
    * Splits this polygon shape with holes into convex pieces. 
    * The result is not exact, because rounded intersections of lines are used in the result pieces. 
    * It can be made exact, if Polylines are returned instead of Polygons, so that no intersection points
    * are needed in the result. 
    * @return an empty array if some sort of error, and a message
    */
   @Override
   public ShapeTile[] split_to_convex()
      {
      if (precalculated_convex_pieces != null)
         {
         // if result already available
         return precalculated_convex_pieces;
         }
         
      ShapeTile[] convex_border_pieces = border_shape.split_to_convex();

      if (convex_border_pieces == null)
         {
         System.err.println("PolylineArea. split_to_convex: convex_border_pieces==null");
         precalculated_convex_pieces = new ShapeTile[0];
         return precalculated_convex_pieces;
         }
      
      List<ShapeTile> curr_piece_list = new LinkedList<ShapeTile>();
      for (int index = 0; index < convex_border_pieces.length; ++index)
         {
         curr_piece_list.add(convex_border_pieces[index]);
         }
      
      for (int index = 0; index < hole_arr.length; ++index)
         {
         if ( ! hole_arr[index].dimension().is_area() )
            {
            System.out.println("PolylineArea. split_to_convex: dimennsion 2 for hole expected");
            continue;
            }
         
         ShapeTile[] convex_hole_pieces = hole_arr[index].split_to_convex();

         if (convex_hole_pieces == null)
            {
            System.err.println("PolylineArea. split_to_convex: convex_hole_pieces==null");
            precalculated_convex_pieces = new ShapeTile[0];
            return precalculated_convex_pieces;
            }
         
         for (int j = 0; j < convex_hole_pieces.length; ++j)
            {
            ShapeTile curr_hole_piece = convex_hole_pieces[j];
            List<ShapeTile> new_piece_list = new LinkedList<ShapeTile>();
            Iterator<ShapeTile> it = curr_piece_list.iterator();
            while (it.hasNext())
               {
               ShapeTile curr_divide_piece = it.next();
               cutout_hole_piece(curr_divide_piece, curr_hole_piece, new_piece_list);
               }
            curr_piece_list = new_piece_list;
            }
         }
      
      precalculated_convex_pieces = new ShapeTile[curr_piece_list.size()];
      Iterator<ShapeTile> iter = curr_piece_list.iterator();
      for (int index = 0; index < precalculated_convex_pieces.length; ++index)
         {
         precalculated_convex_pieces[index] = iter.next();
         }
      
      return precalculated_convex_pieces;
      }

   @Override
   public PolylineArea turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      ShapePolyline new_border = border_shape.turn_90_degree(p_factor, p_pole);
      ShapePolyline[] new_hole_arr = new ShapePolyline[hole_arr.length];
      for (int i = 0; i < new_hole_arr.length; ++i)
         {
         new_hole_arr[i] = hole_arr[i].turn_90_degree(p_factor, p_pole);
         }
      return new PolylineArea(new_border, new_hole_arr);
      }

   @Override
   public PolylineArea rotate_approx(double p_angle, PlaPointFloat p_pole)
      {
      ShapePolyline new_border = border_shape.rotate_approx(p_angle, p_pole);
      ShapePolyline[] new_hole_arr = new ShapePolyline[hole_arr.length];
      for (int i = 0; i < new_hole_arr.length; ++i)
         {
         new_hole_arr[i] = hole_arr[i].rotate_approx(p_angle, p_pole);
         }
      return new PolylineArea(new_border, new_hole_arr);
      }

   @Override
   public PolylineArea mirror_vertical(PlaPointInt p_pole)
      {
      ShapePolyline new_border = border_shape.mirror_vertical(p_pole);
      ShapePolyline[] new_hole_arr = new ShapePolyline[hole_arr.length];
      for (int i = 0; i < new_hole_arr.length; ++i)
         {
         new_hole_arr[i] = hole_arr[i].mirror_vertical(p_pole);
         }
      return new PolylineArea(new_border, new_hole_arr);

      }

   @Override
   public PolylineArea mirror_horizontal(PlaPointInt p_pole)
      {
      ShapePolyline new_border = border_shape.mirror_horizontal(p_pole);
      ShapePolyline[] new_hole_arr = new ShapePolyline[hole_arr.length];
      for (int i = 0; i < new_hole_arr.length; ++i)
         {
         new_hole_arr[i] = hole_arr[i].mirror_horizontal(p_pole);
         }
      return new PolylineArea(new_border, new_hole_arr);

      }

   static private void cutout_hole_piece(ShapeTile p_divide_piece, ShapeTile p_hole_piece, Collection<ShapeTile> p_result_pieces)
      {
      ShapeTile[] result_pieces = p_divide_piece.cutout(p_hole_piece);
      for (int i = 0; i < result_pieces.length; ++i)
         {
         ShapeTile curr_piece = result_pieces[i];
         if (curr_piece.dimension() == PlaDimension.AREA)
            {
            p_result_pieces.add(curr_piece);
            }
         }
      }
   }
