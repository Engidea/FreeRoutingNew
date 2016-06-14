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
 * BoardOutline.java
 *
 * Created on 18. August 2004, 07:24
 */
package board.items;

import java.awt.Color;
import board.RoutingBoard;
import board.awtree.AwtreeShapeSearch;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.graphics.GdiContext;
import freert.planar.PlaArea;
import freert.planar.PlaAreaLinear;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeSegments;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import gui.varie.ObjectInfoPanel;

/**
 * Class describing a board outline.
 *
 * @author alfons
 */
public final class BrdOutline extends BrdItem implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   public static final int HALF_WIDTH = 100;

   // The board shapes inside the outline curves
   private ShapeSegments[] shapes;
   // The board shape outside the outline curves, where a keepout will be generated The outline curves are holes of the keepout_area.
   private PlaArea keepout_area = null;
   // Used instead of keepout_area if only the line shapes of the outlines are inserted as keepout.
   private ShapeTile[] keepout_lines = null;
   private boolean keepout_outside_outline = false;
   
   public BrdOutline(ShapeSegments[] p_shapes, int p_clearance_class_no, int p_id_no, RoutingBoard p_board)
      {
      super(NetNosList.EMPTY, p_clearance_class_no, p_id_no, 0, ItemFixState.SYSTEM_FIXED, p_board);
      
      shapes = p_shapes;
      }

   private BrdOutline(BrdOutline p_other, int p_id_no)
      {
      super(p_other,p_id_no);
      
      shapes = p_other.shapes;
      }

   @Override
   public BrdOutline copy(int p_id_no)
      {
      return new BrdOutline(this, p_id_no);
      }
   
   @Override
   public int tile_shape_count()
      {
      int result;
      if (keepout_outside_outline)
         {
         ShapeTile[] tile_shapes = get_keepout_area().split_to_convex();
         if (tile_shapes == null)
            {
            // an error accured while dividing the area
            result = 0;
            }
         else
            {
            result = tile_shapes.length * r_board.layer_structure.size();
            }
         }
      else
         {
         result = line_count() * r_board.layer_structure.size();
         }
      return result;
      }

   @Override
   public int shape_layer(int p_index)
      {
      int shape_count = tile_shape_count();
      int result;
      if (shape_count > 0)
         {
         result = p_index * r_board.layer_structure.size() / shape_count;
         }
      else
         {
         result = 0;
         }
      if (result < 0 || result >= r_board.layer_structure.size())
         {
         System.out.println("BoardOutline.shape_layer: p_index out of range");
         }
      return result;
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      return !(p_other instanceof BrdOutline || p_other instanceof BrdArea);
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      ShapeTileBox result = ShapeTileBox.EMPTY;
      for (ShapeSegments curr_shape : shapes)
         {
         result = result.union(curr_shape.bounding_box());
         }
      return result;
      }

   @Override
   public int first_layer()
      {
      return 0;
      }

   @Override
   public int last_layer()
      {
      return r_board.layer_structure.size() - 1;
      }

   @Override
   public boolean is_on_layer(int p_layer)
      {
      return true;
      }

   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      for (ShapeSegments curr_shape : shapes)
         {
         curr_shape = curr_shape.translate_by(p_vector);
         }
      if (keepout_area != null)
         {
         keepout_area = keepout_area.translate_by(p_vector);
         }
      keepout_lines = null;
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      for (ShapeSegments curr_shape : shapes)
         {
         curr_shape = curr_shape.rotate_90_deg(p_factor, p_pole);
         }
      if (keepout_area != null)
         {
         keepout_area = keepout_area.rotate_90_deg(p_factor, p_pole);
         }
      keepout_lines = null;
      }

   @Override
   public void rotate_deg(int p_angle_in_degree, PlaPointFloat p_pole)
      {
      double angle = Math.toRadians(p_angle_in_degree);

      for (ShapeSegments curr_shape : shapes)
         {
         curr_shape = curr_shape.rotate_rad(angle, p_pole);
         }
      
      if (keepout_area != null)
         {
         keepout_area = keepout_area.rotate_rad(angle, p_pole);
         }
      
      keepout_lines = null;
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      for (ShapeSegments curr_shape : shapes)
         {
         curr_shape = curr_shape.mirror_vertical(p_pole);
         }
      
      if (keepout_area != null)
         {
         keepout_area = keepout_area.mirror_vertical(p_pole);
         }
      keepout_lines = null;
      }

   @Override
   public double get_draw_intensity(GdiContext p_graphics_context)
      {
      return 1;
      }

   @Override
   public int get_draw_priority()
      {
      return freert.graphics.GdiDrawable.MAX_DRAW_PRIORITY;
      }

   public int shape_count()
      {
      return shapes.length;
      }

   public ShapeSegments get_shape(int p_index)
      {
      if (p_index < 0 || p_index >= shapes.length)
         {
         System.out.println("BoardOutline.get_shape: p_index out of range");
         return null;
         }

      return shapes[p_index];
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if ( ! is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.BOARD_OUTLINE);
      }

   @Override
   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      Color[] color_arr = new Color[r_board.layer_structure.size()];
      Color draw_color = p_graphics_context.get_outline_color();
      for (int index = 0; index < color_arr.length; ++index)
         {
         color_arr[index] = draw_color;
         }
      return color_arr;
      }

   /**
    * The board shape outside the outline curves, where a keepout will be generated 
    * The outline curves are holes of the keepout_area.
    */
   public PlaArea get_keepout_area()
      {
      if (keepout_area == null)
         {
         ShapeSegments[] hole_arr = new ShapeSegments[shapes.length];
         for (int i = 0; i < hole_arr.length; ++i)
            {
            hole_arr[i] = shapes[i];
            }
         keepout_area = new PlaAreaLinear(r_board.bounding_box, hole_arr);
         }
      
      return keepout_area;
      }

   ShapeTile[] get_keepout_lines()
      {
      if ( keepout_lines == null)
         {
         keepout_lines = new ShapeTile[0];
         }
      return keepout_lines;
      }

   @Override
   public void draw(java.awt.Graphics p_g, GdiContext p_graphics_context, java.awt.Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0) return;

      for (ShapeSegments curr_shape : shapes)
         {
         PlaPointFloat[] draw_corners = curr_shape.corner_approx_arr();
         PlaPointFloat[] closed_draw_corners = new PlaPointFloat[draw_corners.length + 1];
         System.arraycopy(draw_corners, 0, closed_draw_corners, 0, draw_corners.length);
         closed_draw_corners[closed_draw_corners.length - 1] = draw_corners[0];
         p_graphics_context.draw(closed_draw_corners, HALF_WIDTH, p_color_arr[0], p_g, p_intensity);
         }
      }


   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("board_outline"));
      print_clearance_info(p_window, p_locale);
      p_window.newline();
      }

   @Override
   public boolean write(java.io.ObjectOutputStream p_stream)
      {
      try
         {
         p_stream.writeObject(this);
         }
      catch (java.io.IOException e)
         {
         return false;
         }
      return true;
      }

   /**
    * Returns, if keepout is generated outside the board outline. 
    * Otherwise only the line shapes of the outlines are inserted as keepout.
    */
   public boolean keepout_outside_outline_generated()
      {
      return keepout_outside_outline;
      }

   /**
    * Makes the area outside this Outline to Keepout, 
    * Reinserts this Outline into the search trees, if the value changes.
    */
   public void generate_keepout_outside(boolean p_value)
      {
      if (p_value == keepout_outside_outline) return;

      keepout_outside_outline = p_value;
      
      r_board.search_tree_manager.remove(this);
      r_board.search_tree_manager.insert(this);
      }

   /**
    * Returns the sum of the lines of all outline poligons.
    */
   public int line_count()
      {
      int result = 0;
      for (ShapeSegments curr_shape : shapes)
         {
         result += curr_shape.border_line_count();
         }
      return result;
      }

   @Override
   protected final ShapeTile[] calculate_tree_shapes(AwtreeShapeSearch p_search_tree)
      {
      return p_search_tree.calculate_tree_shapes(this);
      }

   }
