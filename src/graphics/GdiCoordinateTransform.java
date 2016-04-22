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

package graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import freert.planar.PlaLimits;
import freert.planar.PlaPointFloat;
import freert.planar.ShapeTileBox;

/**
 * Transformation function between the board and the screen coordinate systems.
 *
 * @author Alfons Wirtz
 */

public final class GdiCoordinateTransform implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   final ShapeTileBox design_box;
   final ShapeTileBox design_box_with_offset;
   final Dimension    screen_bounds;

   private final double scale_factor;
   private final double display_x_offset;
   private final double display_y_offset;

   // Left side and right side of the board are swapped.
   private boolean mirror_left_right = false;

   // Top side and bottom side of the board are swapped.
   private boolean mirror_top_bottom = true;

   private double rotation_radiants = 0;

   private PlaPointFloat rotation_pole;   

   public GdiCoordinateTransform(ShapeTileBox p_design_box, Dimension p_panel_bounds)
      {
      screen_bounds = p_panel_bounds;
      design_box = p_design_box;
      rotation_pole = p_design_box.centre_of_gravity();

      int min_ll = Math.min(p_design_box.box_ll.v_x, p_design_box.box_ll.v_y);
      int max_ur = Math.max(p_design_box.box_ur.v_x, p_design_box.box_ur.v_y);
      if (Math.max(Math.abs(min_ll), Math.abs(max_ur)) <= 0.3 * PlaLimits.CRIT_INT)
         {
         // create an offset to p_design_box to enable deep zoom out
         double design_offset = Math.max(p_design_box.width(), p_design_box.height());
         design_box_with_offset = p_design_box.offset(design_offset);
         }
      else
         {
         // no offset because of danger of integer overflow
         design_box_with_offset = p_design_box;
         }

      double x_scale_factor = screen_bounds.getWidth() / design_box_with_offset.width();
      double y_scale_factor = screen_bounds.getHeight() / design_box_with_offset.height();

      scale_factor = Math.min(x_scale_factor, y_scale_factor);
      display_x_offset = scale_factor * design_box_with_offset.box_ll.v_x;
      display_y_offset = scale_factor * design_box_with_offset.box_ll.v_y;
      }

   public GdiCoordinateTransform(GdiCoordinateTransform p_coordinate_transform)
      {
      screen_bounds = new Dimension(p_coordinate_transform.screen_bounds);
      design_box = new ShapeTileBox(p_coordinate_transform.design_box.box_ll, p_coordinate_transform.design_box.box_ur);
      rotation_pole = new PlaPointFloat(p_coordinate_transform.rotation_pole.point_x, p_coordinate_transform.rotation_pole.point_y);
      design_box_with_offset = new ShapeTileBox(p_coordinate_transform.design_box_with_offset.box_ll, p_coordinate_transform.design_box_with_offset.box_ur);
      scale_factor = p_coordinate_transform.scale_factor;
      display_x_offset = p_coordinate_transform.display_x_offset;
      display_y_offset = p_coordinate_transform.display_y_offset;
      mirror_left_right = p_coordinate_transform.mirror_left_right;
      mirror_top_bottom = p_coordinate_transform.mirror_top_bottom;
      rotation_radiants = p_coordinate_transform.rotation_radiants;
      }

   /**
    * scale a value from the board to the screen coordinate system
    */
   public double board_to_screen(double p_val)
      {
      return p_val * scale_factor;
      }

   /**
    * scale a value the screen to the board coordinate system
    */
   public double screen_to_board(double p_val)
      {
      return p_val / scale_factor;
      }

   /**
    * transform a geometry.planar.FloatPoint to a java.awt.geom.Point2D
    */
   public Point2D board_to_screen(PlaPointFloat p_point)
      {
      PlaPointFloat rotated_point = p_point.rotate(this.rotation_radiants, this.rotation_pole);

      double x, y;
      if (this.mirror_left_right)
         {
         x = (design_box_with_offset.width() - rotated_point.point_x - 1) * scale_factor + display_x_offset;
         }
      else
         {
         x = rotated_point.point_x * scale_factor - display_x_offset;
         }
      if (this.mirror_top_bottom)
         {
         y = (design_box_with_offset.height() - rotated_point.point_y - 1) * scale_factor + display_y_offset;
         }
      else
         {
         y = rotated_point.point_y * scale_factor - display_y_offset;
         }
      return new Point2D.Double(x, y);
      }

   /**
    * Transform a java.awt.geom.Point2D to a geometry.planar.FloatPoint
    */
   public PlaPointFloat screen_to_board(Point2D p_point)
      {
      double x, y;
      
      if ( mirror_left_right )
         {
         x = design_box_with_offset.width() - (p_point.getX() - display_x_offset) / scale_factor - 1;
         }
      else
         {
         x = (p_point.getX() + display_x_offset) / scale_factor;
         }
      
      if ( mirror_top_bottom)
         {
         y = design_box_with_offset.height() - (p_point.getY() - display_y_offset) / scale_factor - 1;
         }
      else
         {
         y = (p_point.getY() + display_y_offset) / scale_factor;
         }
      
      PlaPointFloat result = new PlaPointFloat(x, y);
      
      return result.rotate(-rotation_radiants, rotation_pole);
      }

   /**
    * Transforms an angle in radian on the board to an angle on the screen.
    */
   public double board_to_screen_angle(double p_angle)
      {
      double result = p_angle + this.rotation_radiants;
      if (this.mirror_left_right)
         {
         result = Math.PI - result;
         }
      if (this.mirror_top_bottom)
         {
         result = -result;
         }
      while (result >= 2 * Math.PI)
         {
         result -= 2 * Math.PI;
         }
      while (result < 0)
         {
         result += 2 * Math.PI;
         }
      return result;
      }

   /**
    * Transform a geometry.planar.IntBox to a java.awt.Rectangle If the internal rotation is not a multiple of Pi/2, a bounding
    * rectangle of the rotated rectangular shape is returned.
    */
   public java.awt.Rectangle board_to_screen(ShapeTileBox p_box)
      {
      Point2D corner_1 = board_to_screen(p_box.box_ll.to_float());
      Point2D corner_2 = board_to_screen(p_box.box_ur.to_float());
      double ll_x = Math.min(corner_1.getX(), corner_2.getX());
      double ll_y = Math.min(corner_1.getY(), corner_2.getY());
      double dx = Math.abs(corner_2.getX() - corner_1.getX());
      double dy = Math.abs(corner_2.getY() - corner_1.getY());
      java.awt.Rectangle result = new java.awt.Rectangle((int) Math.floor(ll_x), (int) Math.floor(ll_y), (int) Math.ceil(dx), (int) Math.ceil(dy));
      return result;
      }

   /**
    * Transform a java.awt.Rectangle to a geometry.planar.IntBox If the internal rotation is not a multiple of Pi/2, a bounding box
    * of the rotated rectangular shape is returned.
    */
   public ShapeTileBox screen_to_board(java.awt.Rectangle p_rect)
      {
      PlaPointFloat corner_1 = screen_to_board(new Point2D.Double(p_rect.getX(), p_rect.getY()));
      PlaPointFloat corner_2 = screen_to_board(new Point2D.Double(p_rect.getX() + p_rect.getWidth(), p_rect.getY() + p_rect.getHeight()));
      int llx = (int) Math.floor(Math.min(corner_1.point_x, corner_2.point_x));
      int lly = (int) Math.floor(Math.min(corner_1.point_y, corner_2.point_y));
      int urx = (int) Math.ceil(Math.max(corner_1.point_x, corner_2.point_x));
      int ury = (int) Math.ceil(Math.max(corner_1.point_y, corner_2.point_y));
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   /**
    * If p_value is true, the left side and the right side of the board will be swapped.
    */
   public void set_mirror_left_right(boolean p_value)
      {
      mirror_left_right = p_value;
      }

   /**
    * Returns, if the left side and the right side of the board are swapped.
    */
   public boolean is_mirror_left_right()
      {
      return mirror_left_right;
      }

   /**
    * If p_value is true, the top side and the botton side of the board will be swapped.
    */
   public void set_mirror_top_bottom(boolean p_value)
      {
      // Because the origin of display is the upper left corner, the internal value
      // will be opposite to the input value of this function.
      mirror_top_bottom = !p_value;
      }

   /**
    * Returns, if the top side and the botton side of the board are swapped.
    */
   public boolean is_mirror_top_bottom()
      {
      // Because the origin of display is the upper left corner, the internal value
      // is opposite to the result of this function.
      return !mirror_top_bottom;
      }

   /**
    * Sets the rotation of the displayed board to p_value.
    */
   public void set_rotation(double p_value)
      {
      rotation_radiants = p_value;
      }

   /**
    * Returns the rotation of the displayed board.
    */
   public double get_rotation()
      {
      return rotation_radiants;
      }

   /**
    * Returns the internal rotation snapped to the nearest multiple of 90 degree. The result will be 0, 1, 2 or 3.
    */
   public int get_90_degree_rotation()
      {
      int multiple = (int) Math.round(Math.toDegrees(rotation_radiants) / 90.0);
      while (multiple < 0)
         {
         multiple += 4;
         }
      while (multiple >= 4)
         {
         multiple -= 4;
         }
      return multiple;
      }

   }