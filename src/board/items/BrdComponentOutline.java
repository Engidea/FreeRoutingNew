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
 * ComponentOutline.java
 *
 * Created on 28. November 2005, 06:42
 *
 */
package board.items;

import java.awt.Color;
import board.RoutingBoard;
import board.shape.ShapeSearchTree;
import board.varie.ItemFixState;
import board.varie.ItemSelectionFilter;
import datastructures.UnitMeasure;
import freert.planar.PlaArea;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVector;
import freert.planar.ShapeTile;
import graphics.GdiContext;
import gui.varie.ObjectInfoPanel;

/**
 *
 * @author Alfons Wirtz
 */
public final class BrdComponentOutline extends BrdItem implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private PlaArea relative_area;
   private PlaVector translation;
   private double rotation_in_degree;
   private boolean is_front;

   private transient PlaArea precalculated_absolute_area = null;
   

   public BrdComponentOutline(PlaArea p_area, boolean p_is_front, PlaVector p_translation, double p_rotation_in_degree, int p_component_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(new int[0], 0, 0, p_component_no, p_fixed_state, p_board);
      relative_area = p_area;
      is_front = p_is_front;
      translation = p_translation;
      rotation_in_degree = p_rotation_in_degree;
      }

   @Override
   public BrdComponentOutline copy(int p_id_no)
      {
      return new BrdComponentOutline(relative_area, is_front, translation, rotation_in_degree, get_component_no(), get_fixed_state(), r_board);
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      return false;
      }

   public int get_layer()
      {
      int result;
      if (is_front)
         {
         result = 0;
         }
      else
         {
         result = r_board.get_layer_count() - 1;
         }
      return result;
      }

   public int first_layer()
      {
      return get_layer();
      }

   public int last_layer()
      {
      return get_layer();
      }

   public boolean is_on_layer(int p_layer)
      {
      return get_layer() == p_layer;
      }

   @Override
   public boolean is_obstacle(BrdItem p_item)
      {
      return false;
      }

   @Override
   public int shape_layer(int p_index)
      {
      return get_layer();
      }

   public int tile_shape_count()
      {
      return 0;
      }

   /**
    * A component outline does not have any shape to be stored in the search tree
    */
   @Override
   protected final ShapeTile[] calculate_tree_shapes(ShapeSearchTree p_search_tree)
      {
      return new ShapeTile[0];
      }

   public double get_draw_intensity(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_component_outline_color_intensity();
      }

   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      Color[] color_arr = new Color[r_board.layer_structure.size()];
      Color front_draw_color = p_graphics_context.get_component_color(true);
      for (int i = 0; i < color_arr.length - 1; ++i)
         {
         color_arr[i] = front_draw_color;
         }
      if (color_arr.length > 1)
         {
         color_arr[color_arr.length - 1] = p_graphics_context.get_component_color(false);
         }
      return color_arr;
      }

   public int get_draw_priority()
      {
      return graphics.GdiDrawable.MIDDLE_DRAW_PRIORITY;
      }

   public void draw(java.awt.Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0)
         {
         return;
         }
      Color color = p_color_arr[get_layer()];
      double intensity = p_graphics_context.get_layer_visibility(get_layer()) * p_intensity;

      double draw_width = Math.min(r_board.host_com.get_resolution(UnitMeasure.MIL), 100); // problem with low resolution on Kicad
      p_graphics_context.draw_boundary(get_area(), draw_width, color, p_g, intensity);
      }

   public freert.planar.ShapeTileBox bounding_box()
      {
      return get_area().bounding_box();
      }

   public void translate_by(PlaVector p_vector)
      {
      translation = translation.add(p_vector);
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      is_front = !is_front;
      PlaPoint rel_location = PlaPoint.ZERO.translate_by(translation);
      translation = rel_location.mirror_vertical(p_pole).difference_by(PlaPoint.ZERO);
      clear_derived_data();
      }

   public void rotate_approx(double p_angle_in_degree, PlaPointFloat p_pole)
      {
      double turn_angle = p_angle_in_degree;
      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         turn_angle = 360 - p_angle_in_degree;
         }
      rotation_in_degree += turn_angle;
      while (rotation_in_degree >= 360)
         {
         rotation_in_degree -= 360;
         }
      while (rotation_in_degree < 0)
         {
         rotation_in_degree += 360;
         }
      PlaPointFloat new_translation = translation.to_float().rotate(Math.toRadians(p_angle_in_degree), p_pole);
      translation = new_translation.round().difference_by(PlaPoint.ZERO);
      clear_derived_data();
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      rotation_in_degree += p_factor * 90;
      while (rotation_in_degree >= 360)
         {
         rotation_in_degree -= 360;
         }
      while (rotation_in_degree < 0)
         {
         rotation_in_degree += 360;
         }
      PlaPoint rel_location = PlaPoint.ZERO.translate_by(translation);
      translation = rel_location.turn_90_degree(p_factor, p_pole).difference_by(PlaPoint.ZERO);
      clear_derived_data();
      }

   public PlaArea get_area()
      {
      if ( precalculated_absolute_area != null) return precalculated_absolute_area;

      PlaArea turned_area = relative_area;

      if (!is_front && !r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPoint.ZERO);
         }
      if (rotation_in_degree != 0)
         {
         double rotation = rotation_in_degree;
         if (rotation % 90 == 0)
            {
            turned_area = turned_area.turn_90_degree(((int) rotation) / 90, PlaPoint.ZERO);
            }
         else
            {
            turned_area = turned_area.rotate_approx(Math.toRadians(rotation), PlaPointFloat.ZERO);
            }

         }
      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPoint.ZERO);
         }
      precalculated_absolute_area = turned_area.translate_by(translation);

      return precalculated_absolute_area;
      }

   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      
      precalculated_absolute_area = null;
      }

   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      }

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
   }
