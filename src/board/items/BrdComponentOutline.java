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
import board.awtree.AwtreeShapeSearch;
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
   
   private boolean is_front;
   private PlaVectorInt translation;
   private int rotate_degree;   // this is basically used only when back exporting, so, keep it in sync

   private transient PlaArea precalculated_absolute_area = null;

   public BrdComponentOutline(PlaShape p_shape, boolean p_is_front, PlaVectorInt p_translation, int p_rotate_degree, int p_component_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(NetNosList.EMPTY, 0, 0, p_component_no, p_fixed_state, p_board);
      
      original_shape = p_shape;
      draw_width     = Math.min(r_board.host_com.get_resolution(UnitMeasure.INCH), 100); // problem with low resolution on Kicad

      is_front       = p_is_front;
      translation    = p_translation;
      rotate_degree  = p_rotate_degree;
      
      }

   private BrdComponentOutline ( BrdComponentOutline p_other, int p_id_no )
      {
      super(p_other, p_id_no);
      
      original_shape = p_other.original_shape;
      draw_width     = p_other.draw_width;

      is_front       = p_other.is_front;
      translation    = p_other.translation;
      rotate_degree  = p_other.rotate_degree;
      }
   
   @Override
   public BrdComponentOutline copy(int p_id_no)
      {
      return new BrdComponentOutline(this, p_id_no);
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
   protected final ShapeTile[] calculate_tree_shapes(AwtreeShapeSearch p_search_tree)
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
      
      PlaPointInt rel_location = translation.to_int();
      
      translation = rel_location.mirror_vertical(p_pole).to_vector();
      
      clear_derived_data();
      }

   private int rotation_reduce ( int p_rotation )
      {
      while (p_rotation >= 360) p_rotation -= 360;

      while (p_rotation < 0) p_rotation += 360;
      
      return p_rotation;
      }
   
   @Override
   public void rotate_approx(int p_angle_in_degree, PlaPointFloat p_pole)
      {
      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         p_angle_in_degree = 360 - p_angle_in_degree;
         }
      
      rotate_degree = rotation_reduce ( rotate_degree + p_angle_in_degree );

      PlaPointFloat rel_location = translation.to_float();
      
      PlaPointFloat new_translation = rel_location.rotate_rad(Math.toRadians(p_angle_in_degree), p_pole);

      translation = new_translation.to_vector();
      
      clear_derived_data();
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      rotate_degree = rotation_reduce ( rotate_degree + p_factor * 90 );
      
      PlaPointInt rel_location = translation.to_int();
      
      translation = rel_location.turn_90_degree(p_factor, p_pole).to_vector();
      
      clear_derived_data();
      }

   private PlaArea get_area()
      {
      if ( precalculated_absolute_area != null) return precalculated_absolute_area;

      PlaArea turned_area = original_shape;
      
      if (!is_front && !r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPointInt.ZERO);
         }
      
      if (rotate_degree != 0)
         {
         if (rotate_degree % 90 == 0)
            turned_area = turned_area.rotate_90_deg(rotate_degree / 90, PlaPointInt.ZERO);
         else
            turned_area = turned_area.rotate_rad(Math.toRadians(rotate_degree), PlaPointFloat.ZERO);
         }

      if (!is_front && r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPointInt.ZERO);
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
