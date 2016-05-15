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
 * Created on 19. August 2004, 07:34
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
 * Describes Areas on the board, where vias are not allowed.
 *
 * @author alfons
 */
public final class BrdAreaObstacleVia extends BrdArea
   {
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new area item which may belong to several nets
    */
   public BrdAreaObstacleVia(
         PlaArea p_area, 
         int p_layer, 
         PlaVectorInt p_translation, 
         double p_rotation_in_degree, 
         boolean p_side_changed, 
         int[] p_net_no_arr, 
         int p_clearance_type, 
         int p_id_no, 
         int p_group_no,
         String p_name, 
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      super(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_name, p_fixed_state, p_board);
      }

   /**
    * Creates a new area item without net
    */
   public BrdAreaObstacleVia(
         PlaArea p_area, 
         int p_layer, 
         PlaVectorInt p_translation, 
         double p_rotation_in_degree, 
         boolean p_side_changed, 
         int p_clearance_type, 
         int p_id_no, 
         int p_group_no, 
         String p_name,
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      this(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, new int[0], p_clearance_type, p_id_no, p_group_no, p_name, p_fixed_state, p_board);
      }

   @Override
   public BrdAreaObstacleVia copy(int p_id_no)
      {
      int[] copied_net_nos =  net_nos.net_nos_arr.clone();

      return new BrdAreaObstacleVia(
            get_relative_area(), 
            get_layer(), 
            get_translation(), 
            get_rotation_in_degree(), 
            get_side_changed(), 
            copied_net_nos, 
            clearance_class_no(), 
            p_id_no,
            get_component_no(), 
            area_name, get_fixed_state(), r_board);
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other.shares_net(this)) return false;
      
      return p_other instanceof BrdAbitVia;
      }

   @Override
   public boolean is_trace_obstacle(int p_net_no)
      {
      return false;
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if (! is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.VIA_KEEPOUT);
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("via_keepout"));
      print_shape_info(p_window, p_locale);
      print_clearance_info(p_window, p_locale);
      print_clearance_violation_info(p_window, p_locale);
      p_window.newline();
      }

   @Override
   public Color[] get_draw_colors(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_via_obstacle_colors();
      }

   @Override
   public double get_draw_intensity(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_via_obstacle_color_intensity();
      }

   }
