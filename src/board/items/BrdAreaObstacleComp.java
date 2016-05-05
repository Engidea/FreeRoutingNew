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
 * Created on 8. Mai 2005, 07:28
 */

package board.items;

import java.awt.Color;
import board.RoutingBoard;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.planar.PlaArea;
import freert.planar.PlaVectorInt;
import gui.varie.ObjectInfoPanel;

/**
 * Describes areas of the board, where components are not allowed.
 *
 * @author alfons
 */
public final class BrdAreaObstacleComp extends BrdArea
   {
   private static final long serialVersionUID = 1L;
   
   /**
    * If p_is_obstacle is false, the new instance is not regarded as obstacle and used only for displaying on the screen.
    */
   public BrdAreaObstacleComp(
         PlaArea p_area, 
         int p_layer, 
         PlaVectorInt p_translation, 
         double p_rotation_in_degree, 
         boolean p_side_changed, 
         int p_clearance_type, 
         int p_id_no, 
         int p_component_no, 
         String p_name,
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      super(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, new int[0], p_clearance_type, p_id_no, p_component_no, p_name, p_fixed_state, p_board);
      }

   @Override
   public BrdAreaObstacleComp copy(int p_id_no)
      {
      return new BrdAreaObstacleComp(
            get_relative_area(), 
            get_layer(), 
            get_translation(), 
            get_rotation_in_degree(), 
            get_side_changed(), 
            clearance_class_no(), 
            p_id_no, 
            get_component_no(),
            area_name,
            get_fixed_state(), 
            r_board);
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      return p_other != this && p_other instanceof BrdAreaObstacleComp && p_other.get_component_no() != get_component_no();
      }

   @Override
   public boolean is_trace_obstacle(int p_net_no)
      {
      return false;
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if ( ! is_selected_by_fixed_filter(p_filter)) return false;
      
      return p_filter.is_selected(ItemSelectionChoice.COMPONENT_KEEPOUT);
      }

   @Override
   public Color[] get_draw_colors(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_place_obstacle_colors();
      }

   @Override
   public double get_draw_intensity(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_place_obstacle_color_intensity();
      }
   
   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("component_keepout"));
      print_shape_info(p_window, p_locale);
      print_clearance_info(p_window, p_locale);
      print_clearance_violation_info(p_window, p_locale);
      p_window.newline();
      }
   }
