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
import java.awt.Graphics;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Locale;
import board.RoutingBoard;
import board.shape.ShapeSearchTree;
import board.varie.ItemFixState;
import board.varie.ItemSelectionFilter;
import freert.graphics.GdiContext;
import freert.planar.PlaArea;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import freert.varie.UnitMeasure;
import gui.varie.ObjectInfoPanel;

/**
 *
 * @author Alfons Wirtz
 */
public final class BrdComponentOutline extends BrdItem implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final PlaShape original_shape;
   private final double draw_width;
   
   
   private PlaVectorInt translation;
   private double rotation_in_degree;
   private boolean is_front;

   private transient PlaArea precalculated_absolute_area = null;

   public BrdComponentOutline(PlaShape p_shape, boolean p_is_front, PlaVectorInt p_translation, double p_rotation_in_degree, int p_component_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(NetNosList.EMPTY, 0, 0, p_component_no, p_fixed_state, p_board);
      
      original_shape = p_shape;
      is_front = p_is_front;
      translation = p_translation;
      rotation_in_degree = p_rotation_in_degree;
      
      draw_width = Math.min(r_board.host_com.get_resolution(UnitMeasure.INCH), 100); // problem with low resolution on Kicad
      }

   @Override
   public BrdComponentOutline copy(int p_id_no)
      {
      return new BrdComponentOutline(original_shape, is_front, translation, rotation_in_degree, get_component_no(), get_fixed_state(), r_board);
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      return false;
      }

   private int get_layer()
      {
      return is_front ?  0 : r_board.get_layer_count() - 1;
      }

   @Override
   public int first_layer()
      {
      return get_layer();
      }

   @Override
   public int last_layer()
      {
      return get_layer();
      }

   @Override
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

   @Override
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

   @Override
   public double get_draw_intensity(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_component_outline_color_intensity();
      }

   @Override
   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      Color[] color_arr = new Color[r_board.layer_structure.size()];
      Color front_draw_color = p_graphics_context.get_component_color(true);
      
      for (int index = 0; index < color_arr.length - 1; ++index)  color_arr[index] = front_draw_color;
      
      if (color_arr.length > 1)
         {
         color_arr[color_arr.length - 1] = p_graphics_context.get_component_color(false);
         }
      
      return color_arr;
      }

   @Override
   public int get_draw_priority()
      {
      return freert.graphics.GdiDrawable.MIDDLE_DRAW_PRIORITY;
      }

   @Override
   public void draw(Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0)  return;

      Color color = p_color_arr[get_layer()];
      
      double intensity = p_graphics_context.get_layer_visibility(get_layer()) * p_intensity;
      
      p_graphics_context.draw_boundary(get_area(), draw_width, color, p_g, intensity);
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      return get_area().bounding_box();
      }

   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      translation = translation.add(p_vector);
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      is_front = ! is_front;
      PlaPointInt rel_location = PlaPointInt.ZERO.translate_by(translation);
      
      translation = rel_location.mirror_vertical(p_pole).to_vector();
      
      clear_derived_data();
      }

   private void set_rotation ( double p_rotation )
      {
      rotation_in_degree += p_rotation;
      
      while (rotation_in_degree >= 360) rotation_in_degree -= 360;

      while (rotation_in_degree < 0) rotation_in_degree += 360;
      }
   
   @Override
   public void rotate_approx(double p_angle_in_degree, PlaPointFloat p_pole)
      {
      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         p_angle_in_degree = 360 - p_angle_in_degree;
         }
      
      set_rotation ( p_angle_in_degree );

      PlaPointFloat new_translation = translation.to_float().rotate(Math.toRadians(rotation_in_degree), p_pole);

      translation = new_translation.to_vector();
      
      clear_derived_data();
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      set_rotation ( rotation_in_degree += p_factor * 90);
      
      PlaPointInt rel_location = PlaPointInt.ZERO.translate_by(translation);
      
      translation = rel_location.turn_90_degree(p_factor, p_pole).to_vector();
      
      clear_derived_data();
      }

   private PlaArea get_area()
      {
      if ( precalculated_absolute_area != null) return precalculated_absolute_area;

      PlaArea turned_area = original_shape;
      
      if (!is_front && !r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = original_shape.mirror_vertical(PlaPointInt.ZERO);
         }
      
      if (rotation_in_degree != 0)
         {
         if (rotation_in_degree % 90 == 0)
            {
            turned_area = original_shape.turn_90_degree(((int) rotation_in_degree) / 90, PlaPointInt.ZERO);
            }
         else
            {
            turned_area = original_shape.rotate_approx(Math.toRadians(rotation_in_degree), PlaPointFloat.ZERO);
            }
         }

      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = original_shape.mirror_vertical(PlaPointInt.ZERO);
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

   @Override
   public void print_info(ObjectInfoPanel p_window, Locale p_locale)
      {
      }

   @Override
   public boolean write(ObjectOutputStream p_stream)
      {
      try
         {
         p_stream.writeObject(this);
         }
      catch (IOException e)
         {
         return false;
         }
      return true;
      }
   }
