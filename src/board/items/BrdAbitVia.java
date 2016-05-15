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
 * Via.java
 *
 * Created on 5. Juni 2003, 10:36
 */
package board.items;

import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import autoroute.ArtItem;
import autoroute.expand.ExpandDrill;
import board.RoutingBoard;
import board.shape.ShapeSearchTree;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.library.LibPadstack;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import gui.varie.ObjectInfoPanel;

/**
 * Electrical Item on the board, which may have a shape on several layer, whose geometry is
 * described by a padstack
 *
 * @author Alfons Wirtz
 */
public final class BrdAbitVia extends BrdAbit implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private LibPadstack padstack;
   // True, if copper sharing of this via with SMD pins of the same net is allowed
   public final boolean attach_allowed;
   
   // Temporary data used in the autoroute algorithm
   transient private PlaShape[] precalculated_shapes = null;
   transient private ExpandDrill autoroute_drill_info = null;
   
   public BrdAbitVia(LibPadstack p_padstack, PlaPointInt p_center, NetNosList p_net_no_arr, int p_clearance_type, int p_id_no, int p_group_no, ItemFixState p_fixed_state, boolean p_attach_allowed, RoutingBoard p_board)
      {
      super(p_center, p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);
      padstack = p_padstack;
      attach_allowed = p_attach_allowed;
      }

   @Override
   public BrdAbitVia copy(int p_id_no)
      {
      return new BrdAbitVia(
            padstack, 
            center_get(), 
            net_nos, 
            clearance_idx(), 
            p_id_no, 
            get_component_no(), 
            get_fixed_state(), 
            attach_allowed, 
            r_board);
      }

   @Override
   public PlaShape get_shape(int p_index)
      {
      if ( precalculated_shapes != null) return precalculated_shapes[p_index];
      
      // will throw exception if padstack is null, good
      precalculated_shapes = new PlaShape[padstack.to_layer() - padstack.from_layer() + 1];
      
      for (int index = 0; index < precalculated_shapes.length; ++index)
         {
         int padstack_layer = index + first_layer();
         PlaVectorInt translate_vector = center_get().difference_by(PlaPointInt.ZERO);
         PlaShape curr_shape = padstack.get_shape(padstack_layer);

         if (curr_shape == null)
            {
            precalculated_shapes[index] = null;
            }
         else
            {
            precalculated_shapes[index] = (PlaShape) curr_shape.translate_by(translate_vector);
            }
         }

      return precalculated_shapes[p_index];
      }

   @Override
   public LibPadstack get_padstack()
      {
      return padstack;
      }

   public void set_padstack(LibPadstack p_padstack)
      {
      padstack = p_padstack;
      }

   @Override
   public boolean is_route()
      {
      return !is_user_fixed() && this.net_count() > 0;
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other == this || p_other instanceof BrdAreaObstacleComp)
         {
         return false;
         }
      
      if ((p_other instanceof BrdAreaConduction) && !((BrdAreaConduction) p_other).get_is_obstacle())
         {
         return false;
         }
      
      if (! p_other.shares_net(this))
         {
         // if me and the other do not share a net then other is an obstacle
         return true;
         }
      
      if (p_other instanceof BrdTrace)
         {
         // This is a via and the other is a net, not an obstacle
         return false;
         }
      
      if ( attach_allowed )
         {
         // I am allowed to attach
         
         if ( p_other instanceof BrdAbitPin && ((BrdAbitPin) p_other).drill_allowed())
            {
            return false;
            }
         }

      return true;
      }

   /**
    * Checks, if the Via has contacts on at most 1 layer.
    */
   @Override
   public boolean is_tail()
      {
      Collection<BrdItem> contact_list = get_normal_contacts();
      
      if (contact_list.size() <= 1)
         {
         return true;
         }
      
      Iterator<BrdItem> iter = contact_list.iterator();
      BrdItem curr_contact_item = iter.next();
      
      int first_contact_first_layer = curr_contact_item.first_layer();
      int first_contact_last_layer = curr_contact_item.last_layer();
      
      while (iter.hasNext())
         {
         curr_contact_item = iter.next();
         
         if (curr_contact_item.first_layer() != first_contact_first_layer || curr_contact_item.last_layer() != first_contact_last_layer)
            {
            return false;
            }
      
         }
      return true;
      }

   public void change_placement_side(PlaPointInt p_pole)
      {
      LibPadstack new_padstack = r_board.library.get_mirrored_via_padstack(padstack);

      if (new_padstack == null) return;

      padstack = new_padstack;
      
      super.change_placement_side(p_pole);
      
      clear_derived_data();
      }

   public ExpandDrill get_autoroute_drill_info(ShapeSearchTree p_autoroute_tree)
      {
      if ( autoroute_drill_info != null) return autoroute_drill_info;
      
      ArtItem via_autoroute_info = art_item_get();
      
      ShapeTile curr_drill_shape = new ShapeTileBox(center_get());
      
      autoroute_drill_info = new ExpandDrill(curr_drill_shape, center_get(), first_layer(), last_layer());
      
      int via_layer_count = last_layer() - first_layer() + 1;
      
      for (int index = 0; index < via_layer_count; ++index)
         {
         autoroute_drill_info.room_arr[index] = via_autoroute_info.get_expansion_room(index, p_autoroute_tree);
         }

      return this.autoroute_drill_info;
      }

   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      
      precalculated_shapes = null;
      autoroute_drill_info = null;
      }

   @Override
   public void art_item_clear()
      {
      super.art_item_clear();
      
      autoroute_drill_info = null;
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if (!is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.VIAS);
      }

   public java.awt.Color[] get_draw_colors(freert.graphics.GdiContext p_graphics_context)
      {
      if ( net_count() == 0)
         {
         // display unconnected vias as obstacles
         return p_graphics_context.get_obstacle_colors();
         }
      else if (this.first_layer() >= this.last_layer())
         {
         // display vias with only one layer as pins
         return p_graphics_context.get_pin_colors();
         }
      else
         {
         return p_graphics_context.get_via_colors(this.is_user_fixed());
         }
      }

   public double get_draw_intensity(freert.graphics.GdiContext p_graphics_context)
      {
      double result;
      if (this.net_count() == 0)
         {
         // display unconnected vias as obstacles
         result = p_graphics_context.get_obstacle_color_intensity();

         }
      else if (this.first_layer() >= this.last_layer())
         {
         // display vias with only one layer as pins
         result = p_graphics_context.get_pin_color_intensity();
         }
      else
         {
         result = p_graphics_context.get_via_color_intensity();
         }
      return result;
      }

   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("via"));
      p_window.append(" " + resources.getString("at"));
      p_window.append(this.center_get().to_float());
      p_window.append(", " + resources.getString("padstack"));
      p_window.append(padstack.pads_name, resources.getString("padstack_info"), padstack);
      this.print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }

   public boolean write(ObjectOutputStream p_stream)
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
