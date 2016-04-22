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
 * CoordinateTransform.java
 *
 * Created on 14. Mai 2004, 09:09
 */

package specctra;

import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaVector;
import freert.planar.ShapePolyline;
import freert.planar.ShapeTileBox;

/**
 * Computes transformations between a specctra dsn-file coordinates and board coordinates.
 *
 * @author Alfons Wirtz
 */
public final class DsnCoordinateTransform implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final double scale_factor;
   private final double base_x;
   private final double base_y;
   
   /**
    * Creates a new instance of CoordinateTransform. 
    * The base point of the dsn coordinate system will be translated to zero in the board coordinate system.
    */
   public DsnCoordinateTransform(double p_scale_factor, double p_base_x, double p_base_y)
      {
      scale_factor = p_scale_factor;
      base_x = p_base_x;
      base_y = p_base_y;
      }

   /**
    * Scale a value from the board to the dsn coordinate system
    */
   public double board_to_dsn(double p_val)
      {
      return p_val / scale_factor;
      }

   /**
    * Scale a value from the dsn to the board coordinate system
    */
   public double dsn_to_board(double p_val)
      {
      return p_val * scale_factor;
      }

   /**
    * Transforms a geometry.planar.FloatPoint to a tuple of doubles in the dsn coordinate system.
    */
   public double[] board_to_dsn(PlaPointFloat p_point)
      {
      double[] result = new double[2];
      result[0] = board_to_dsn(p_point.point_x) + base_x;
      result[1] = board_to_dsn(p_point.point_y) + base_y;
      return result;
      }

   /**
    * Transforms a geometry.planar.FloatPoint to a tuple of doubles in the dsn coordinate system in relative (vector) coordinates.
    */
   public double[] board_to_dsn_rel(PlaPointFloat p_point)
      {
      double[] result = new double[2];
      result[0] = board_to_dsn(p_point.point_x);
      result[1] = board_to_dsn(p_point.point_y);
      return result;
      }

   /**
    * Transforms an array of n geometry.planar.FloatPoints to an array of 2*n doubles in the dsn coordinate system.
    */
   public double[] board_to_dsn(PlaPointFloat[] p_points)
      {
      double[] result = new double[2 * p_points.length];
      for (int i = 0; i < p_points.length; ++i)
         {
         result[2 * i] = board_to_dsn(p_points[i].point_x) + base_x;
         result[2 * i + 1] = board_to_dsn(p_points[i].point_y) + base_y;
         }
      return result;
      }

   /**
    * Transforms an array of n geometry.planar.Lines to an array of 4*n doubles in the dsn coordinate system.
    */
   public double[] board_to_dsn(PlaLineInt[] p_lines)
      {
      double[] result = new double[4 * p_lines.length];
      for (int i = 0; i < p_lines.length; ++i)
         {
         PlaPointFloat a = p_lines[i].point_a.to_float();
         PlaPointFloat b = p_lines[i].point_b.to_float();
         result[4 * i] = board_to_dsn(a.point_x) + base_x;
         result[4 * i + 1] = board_to_dsn(a.point_y) + base_y;
         result[4 * i + 2] = board_to_dsn(b.point_x) + base_x;
         result[4 * i + 3] = board_to_dsn(b.point_y) + base_y;
         }
      return result;
      }

   /**
    * Transforms an array of n geometry.planar.FloatPoints to an array of 2*n doubles in the dsn coordinate system in relative
    * (vector) coordinates.
    */
   public double[] board_to_dsn_rel(PlaPointFloat[] p_points)
      {
      double[] result = new double[2 * p_points.length];
      for (int i = 0; i < p_points.length; ++i)
         {
         result[2 * i] = board_to_dsn(p_points[i].point_x);
         result[2 * i + 1] = board_to_dsn(p_points[i].point_y);
         }
      return result;
      }

   /**
    * Transforms a geometry.planar.Vector to a tuple of doubles in the dsn coordinate system.
    */
   public double[] board_to_dsn(PlaVector p_vector)
      {
      double[] result = new double[2];
      PlaPointFloat v = p_vector.to_float();
      result[0] = board_to_dsn(v.point_x);
      result[1] = board_to_dsn(v.point_y);
      return result;
      }

   /**
    * Transforms a dsn tuple to a geometry.planar.FloatPoint
    */
   public PlaPointFloat dsn_to_board(double[] p_tuple)
      {
      double x = dsn_to_board(p_tuple[0] - base_x);
      double y = dsn_to_board(p_tuple[1] - base_y);
      return new PlaPointFloat(x, y);
      }

   /**
    * Transforms a dsn tuple to a geometry.planar.FloatPoint in relative (vector) coordinates.
    */
   public PlaPointFloat dsn_to_board_rel(double[] p_tuple)
      {
      double x = dsn_to_board(p_tuple[0]);
      double y = dsn_to_board(p_tuple[1]);
      return new PlaPointFloat(x, y);
      }

   /**
    * Transforms a geometry.planar.Intbox to the coordinates of a Rectangle.
    */
   public double[] board_to_dsn(ShapeTileBox p_box)
      {
      double[] result = new double[4];
      result[0] = p_box.box_ll.v_x / scale_factor + base_x;
      result[1] = p_box.box_ll.v_y / scale_factor + base_y;
      result[2] = p_box.box_ur.v_x / scale_factor + base_x;
      result[3] = p_box.box_ur.v_y / scale_factor + base_y;
      return result;
      }

   /**
    * Transforms a geometry.planar.Intbox to a Rectangle in relative (vector) coordinates.
    */
   public double[] board_to_dsn_rel(ShapeTileBox p_box)
      {
      double[] result = new double[4];
      result[0] = p_box.box_ll.v_x / scale_factor;
      result[1] = p_box.box_ll.v_y / scale_factor;
      result[2] = p_box.box_ur.v_x / scale_factor;
      result[3] = p_box.box_ur.v_y / scale_factor;
      return result;
      }

   /**
    * Transforms a board shape to a dsn shape.
    */
   public DsnShape board_to_dsn(freert.planar.PlaShape p_board_shape, DsnLayer p_layer)
      {
      DsnShape result;
      if (p_board_shape instanceof ShapeTileBox)
         {
         result = new DsnRectangle(p_layer, board_to_dsn((ShapeTileBox) p_board_shape));
         }
      else if (p_board_shape instanceof ShapePolyline)
         {
         PlaPointFloat[] corners = ((ShapePolyline) p_board_shape).corner_approx_arr();
         double[] coors = board_to_dsn(corners);
         result = new DsnPolygon(p_layer, coors);
         }
      else if (p_board_shape instanceof freert.planar.PlaCircle)
         {
         freert.planar.PlaCircle board_circle = (freert.planar.PlaCircle) p_board_shape;
         double diameter = 2 * board_to_dsn(board_circle.radius);
         double[] center_coor = board_to_dsn(board_circle.center.to_float());
         result = new DsnCircle(p_layer, diameter, center_coor[0], center_coor[1]);
         }
      else
         {
         System.out.println("CoordinateTransform.board_to_dsn not yet implemented for p_board_shape");
         result = null;
         }
      return result;
      }

   /**
    * Transforms the relative (vector) coordinates of a geometry.planar.Shape to a specctra dsn shape.
    */
   public DsnShape board_to_dsn_rel(freert.planar.PlaShape p_board_shape, DsnLayer p_layer)
      {
      DsnShape result;
      if (p_board_shape instanceof ShapeTileBox)
         {
         result = new DsnRectangle(p_layer, board_to_dsn_rel((ShapeTileBox) p_board_shape));
         }
      else if (p_board_shape instanceof ShapePolyline)
         {
         PlaPointFloat[] corners = ((ShapePolyline) p_board_shape).corner_approx_arr();
         double[] coors = board_to_dsn_rel(corners);
         result = new DsnPolygon(p_layer, coors);
         }
      else if (p_board_shape instanceof freert.planar.PlaCircle)
         {
         freert.planar.PlaCircle board_circle = (freert.planar.PlaCircle) p_board_shape;
         double diameter = 2 * board_to_dsn(board_circle.radius);
         double[] center_coor = board_to_dsn_rel(board_circle.center.to_float());
         result = new DsnCircle(p_layer, diameter, center_coor[0], center_coor[1]);
         }
      else
         {
         System.out.println("CoordinateTransform.board_to_dsn not yet implemented for p_board_shape");
         result = null;
         }
      return result;
      }
   }
