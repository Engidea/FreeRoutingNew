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
 * ConductionArea.java
 *
 * Created on 29. Juni 2003, 11:49
 */

package board.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import board.BrdConnectable;
import board.RoutingBoard;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeObject;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.planar.PlaArea;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import gui.varie.ObjectInfoPanel;

/**
 * A ObstacleArea, which can be electrically connected to other items.
 *
 * @author Alfons Wirtz
 */
public final class BrdAreaConduction extends BrdArea implements BrdConnectable
   {
   private static final long serialVersionUID = 1L;

   private boolean is_obstacle;
   
   public BrdAreaConduction(
         PlaArea p_area, 
         int p_layer, 
         PlaVectorInt p_translation, 
         double p_rotation_in_degree, 
         boolean p_side_changed, 
         int[] p_net_no_arr, 
         int p_clearance_class, 
         int p_id_no, 
         int p_group_no,
         String p_name, 
         boolean p_is_obstacle, 
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      super(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, p_net_no_arr, p_clearance_class, p_id_no, p_group_no, p_name, p_fixed_state, p_board);
      is_obstacle = p_is_obstacle;
      }

   @Override
   public BrdAreaConduction copy(int p_id_no)
      {
      if ( net_count() != 1)
         {
         System.err.println("ConductionArea.copy not yet implemented for areas with more than 1 net");
         return null;
         }
      
      return new BrdAreaConduction(
            get_relative_area(), 
            get_layer(), 
            get_translation(), 
            get_rotation_in_degree(), 
            get_side_changed(), 
            net_nos.net_nos_arr, 
            clearance_class_no(), 
            p_id_no, 
            get_component_no(),
            area_name, 
            is_obstacle, 
            get_fixed_state(), r_board);
      }

   @Override
   public Set<BrdItem> get_normal_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      for (int i = 0; i < tile_shape_count(); ++i)
         {
         ShapeTile curr_shape = get_tile_shape(i);
         Set<ShapeTreeObject> overlaps = r_board.overlapping_objects(curr_shape, get_layer());
         Iterator<ShapeTreeObject> it = overlaps.iterator();
         while (it.hasNext())
            {
            ShapeTreeObject curr_ob = it.next();
            if (!(curr_ob instanceof BrdItem))
               {
               continue;
               }
            BrdItem curr_item = (BrdItem) curr_ob;
            if (curr_item != this && curr_item.shares_net(this) && curr_item.shares_layer(this))
               {
               if (curr_item instanceof BrdTrace)
                  {
                  BrdTrace curr_trace = (BrdTrace) curr_item;
                  if (curr_shape.contains(curr_trace.corner_first()) || curr_shape.contains(curr_trace.corner_last()))
                     {
                     result.add(curr_item);
                     }
                  }
               else if (curr_item instanceof BrdAbit)
                  {
                  BrdAbit curr_drill_item = (BrdAbit) curr_item;
                  if (curr_shape.contains(curr_drill_item.center_get()))
                     {
                     result.add(curr_item);
                     }
                  }
               }
            }
         }
      return result;
      }

   @Override
   public ShapeTile get_trace_connection_shape(ShapeSearchTree p_search_tree, int p_index)
      {
      if (p_index < 0 || p_index >= tree_shape_count(p_search_tree))
         {
         System.out.println("ConductionArea.get_trace_connection_shape p_index out of range");
         return null;
         }
      
      return get_tree_shape(p_search_tree, p_index);
      }

   @Override
   public ArrayList<PlaPointInt> get_ratsnest_corners()
      {
      PlaPointFloat[] corners = get_area().corner_approx_arr();

      ArrayList<PlaPointInt> result = new ArrayList<PlaPointInt>(corners.length);
      
      for (int index = 0; index < corners.length; ++index)
         {
         result.add(corners[index].round());
         }

      return result;
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if ( is_obstacle) return super.is_obstacle(p_other);

      return false;
      }

   /**
    * Returns if this conduction area is regarded as obstacle to traces of foreign nets.
    */
   public boolean get_is_obstacle()
      {
      return is_obstacle;
      }

   /**
    * Sets, if this conduction area is regarded as obstacle to traces and vias of foreign nets.
    */
   public void set_is_obstacle(boolean p_value)
      {
      is_obstacle = p_value;
      }

   @Override
   public boolean is_trace_obstacle(int p_net_no)
      {
      return is_obstacle && ! contains_net(p_net_no);
      }

   @Override
   public boolean is_drillable(int p_net_no)
      {
      return ! is_obstacle || contains_net(p_net_no);
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if ( ! is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.CONDUCTION);
      }

   @Override
   public java.awt.Color[] get_draw_colors(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_conduction_colors();
      }

   @Override
   public double get_draw_intensity(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_conduction_color_intensity();
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("conduction_area"));
      print_shape_info(p_window, p_locale);
      print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }
   }
