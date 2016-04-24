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
 * Created on 17. Dezember 2004, 07:34
 */

package freert.planar;

import board.printable.PrintableCircle;
import board.printable.PrintablePolygon;
import board.printable.PrintableRectangle;
import board.printable.PrintableShape;
import freert.varie.UnitMeasure;

/**
 * Class for transforming objects between user coordinate space and board coordinate space.
 *
 * @author Alfons Wirtz
 */
public final class PlaCoordTransform implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // The unit used for user coordinates 
   public final UnitMeasure user_unit;
   // The factor of the user unit 
   public final double user_unit_factor;
   // The unit used for board coordinates 
   public final UnitMeasure board_unit;
   // The factor of the board unit 
   public final double board_unit_factor;
   // The factor used for transforming coordinates between user coordinate space and board coordinate space
   private final double scale_factor;
   
   public PlaCoordTransform(double p_user_unit_factor, UnitMeasure p_user_unit, double p_board_unit_factor, UnitMeasure p_board_unit)
      {
      user_unit = p_user_unit;
      board_unit = p_board_unit;
      user_unit_factor = p_user_unit_factor;
      board_unit_factor = p_board_unit_factor;
      scale_factor = board_unit_factor / user_unit_factor;
      }

   /**
    * Scale a value from the board to the user coordinate system.
    */
   public double board_to_user(double p_value)
      {
      return UnitMeasure.scale(p_value * scale_factor, board_unit, user_unit);
      }

   /**
    * Scale a value from the user to the board coordinate system.
    */
   public double user_to_board(double p_value)
      {
      return UnitMeasure.scale(p_value / scale_factor, user_unit, board_unit);
      }

   /**
    * Transforms a geometry.planar.FloatPoint from the board coordinate space to
    * the user coordinate space.
    */
   public PlaPointFloat board_to_user(PlaPointFloat p_point)
      {
      return new PlaPointFloat(board_to_user(p_point.v_x), board_to_user(p_point.v_y));
      }

   /**
    * Transforms a geometry.planar.FloatPoint from the user coordinate space. to
    * the board coordinate space.
    */
   public PlaPointFloat user_to_board(PlaPointFloat p_point)
      {
      return new PlaPointFloat(user_to_board(p_point.v_x), user_to_board(p_point.v_y));
      }

   public PrintableShape board_to_user(PlaShape p_shape, java.util.Locale p_locale)
      {
      PrintableShape result;
      if (p_shape instanceof PlaCircle)
         {
         result = board_to_user((PlaCircle) p_shape, p_locale);
         }
      else if (p_shape instanceof ShapeTileBox)
         {
         result = board_to_user((ShapeTileBox) p_shape, p_locale);
         }
      else if (p_shape instanceof ShapePolyline)
         {
         result = board_to_user((ShapePolyline) p_shape, p_locale);
         }
      else
         {
         System.out.println("CoordinateTransform.board_to_user not yet implemented for p_shape");
         result = null;
         }
      return result;
      }

   public PrintableCircle board_to_user(freert.planar.PlaCircle p_circle, java.util.Locale p_locale)
      {
      return new PrintableCircle(board_to_user(p_circle.center.to_float()), board_to_user(p_circle.radius), p_locale);
      }

   public PrintableRectangle board_to_user(freert.planar.ShapeTileBox p_box, java.util.Locale p_locale)
      {
      return new PrintableRectangle(board_to_user(p_box.box_ll.to_float()), board_to_user(p_box.box_ur.to_float()), p_locale);
      }

   public PrintablePolygon board_to_user(freert.planar.ShapePolyline p_shape, java.util.Locale p_locale)
      {
      PlaPointFloat[] corners = p_shape.corner_approx_arr();
      PlaPointFloat[] transformed_corners = new PlaPointFloat[corners.length];
      for (int i = 0; i < corners.length; ++i)
         {
         transformed_corners[i] = board_to_user(corners[i]);
         }
      return new PrintablePolygon(transformed_corners, p_locale);
      }
   }
