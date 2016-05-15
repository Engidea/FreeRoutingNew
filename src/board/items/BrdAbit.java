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
 * DrillItem.java
 *
 * Created on 27. Juni 2003, 11:38
 */

package board.items;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import board.BrdConnectable;
import board.RoutingBoard;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeObject;
import board.varie.BrdTraceInfo;
import board.varie.ItemFixState;
import freert.graphics.GdiContext;
import freert.graphics.GdiDrawable;
import freert.library.LibPadstack;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 * Common superclass for Pins and Vias
 * The name is just some kind of reasonable that means some piece of a board...
 * @author Alfons Wirtz
 */
public abstract class BrdAbit extends BrdItem implements BrdConnectable, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   // The center point of the drill item, can be null, what is the reasons for a rational ??? 
   private PlaPointInt abit_center;
   // pre calculated minimal width of the shapes of this DrillItem on all layers
   private Double precalculated_min_width = null;
   // Pre calculated first layer, where this DrillItem contains a pad shape. If < 0, the value is not yet calculated
   private int precalculated_first_layer = -1;
   // Pre calculated last layer, where this DrillItem contains a pad shape. If < 0, the value is not yet calculated
   private int precalculated_last_layer = -1;

   public BrdAbit(PlaPointInt p_center, int[] p_net_no_arr, int p_clearance_type, int p_id_no, int p_group_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);

      abit_center = p_center;
      }

   /**
    * Works only for symmetric DrillItems
    */
   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      if (abit_center != null)
         {
         abit_center = abit_center.translate_by(p_vector);
         }
      
      clear_derived_data();
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      if (abit_center != null)
         {
         abit_center = abit_center.turn_90_degree(p_factor, p_pole);
         }
      
      clear_derived_data();
      }

   @Override
   public void rotate_approx(double p_angle_in_degree, PlaPointFloat p_pole)
      {
      if (abit_center != null)
         {
         PlaPointFloat new_center = abit_center.to_float().rotate(Math.toRadians(p_angle_in_degree), p_pole);
         abit_center = new_center.round();
         }
      
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      if (abit_center != null)
         {
         abit_center = abit_center.mirror_vertical(p_pole);
         }
      
      clear_derived_data();
      }

   @Override
   public void move_by(PlaVectorInt p_vector)
      {
      PlaPointInt old_center = center_get();
      
      // remember the contact situation of this drillitem to traces on each layer
      Set<BrdTraceInfo> contact_trace_info = new TreeSet<BrdTraceInfo>();

      for ( BrdItem curr_contact : get_normal_contacts() )
         {
         if ( ! ( curr_contact instanceof BrdTrace) ) continue;

         BrdTrace curr_trace = (BrdTrace) curr_contact;

         BrdTraceInfo curr_trace_info = new BrdTraceInfo(curr_trace.get_layer(), curr_trace.get_half_width(), curr_trace.clearance_class_no());
         
         contact_trace_info.add(curr_trace_info);
         }
      
      super.move_by(p_vector);

      // Insert a Trace from the old center to the new center, on all layers, where this DrillItem was connected to a Trace.
      LinkedList<PlaPointInt> connect_point_list = new LinkedList<PlaPointInt>();
      
      connect_point_list.add(old_center);
      
      PlaPointInt new_center = center_get();
      
      PlaPointInt add_corner = null;
      
      if (r_board.brd_rules.is_trace_snap_90() )
         {
         // Make shure, that the traces will remain 90- or 45-degree.
         add_corner = old_center.ninety_degree_corner(new_center, true);
         }
      else if (r_board.brd_rules.is_trace_snap_45())
         {
         add_corner = old_center.fortyfive_degree_corner(new_center, true);
         }

      if (add_corner != null)
         {
         connect_point_list.add(add_corner);
         }
      
      connect_point_list.add(new_center);
      
      PlaPointInt[] connect_points = connect_point_list.toArray(new PlaPointInt[connect_point_list.size()]);
      
      for ( BrdTraceInfo curr_trace_info : contact_trace_info )
         {
         r_board.insert_trace(connect_points, curr_trace_info.layer, curr_trace_info.half_width, net_no_arr, curr_trace_info.clearance_type, ItemFixState.UNFIXED);
         }
      }

   @Override
   public int shape_layer(int p_index)
      {
      int index = Math.max(p_index, 0);
      int from_layer = first_layer();
      int to_layer = last_layer();
      index = Math.min(index, to_layer - from_layer);
      return from_layer + index;
      }

   @Override
   public boolean is_on_layer(int p_layer)
      {
      return p_layer >= first_layer() && p_layer <= last_layer();
      }

   @Override
   public int first_layer()
      {
      if (precalculated_first_layer < 0)
         {
         LibPadstack padstack = get_padstack();
         if (is_placed_on_front() || padstack.placed_absolute)
            {
            precalculated_first_layer = padstack.from_layer();
            }
         else
            {
            precalculated_first_layer = padstack.board_layer_count() - padstack.to_layer() - 1;
            }
         }
      
      return precalculated_first_layer;
      }

   @Override
   public int last_layer()
      {
      if (precalculated_last_layer < 0)
         {
         LibPadstack padstack = get_padstack();
         if (is_placed_on_front() || padstack.placed_absolute)
            {
            precalculated_last_layer = padstack.to_layer();
            }
         else
            {
            precalculated_last_layer = padstack.board_layer_count() - padstack.from_layer() - 1;
            }
         }
      return precalculated_last_layer;
      }

   public abstract PlaShape get_shape(int p_index);

   @Override
   public ShapeTileBox bounding_box()
      {
      ShapeTileBox result = ShapeTileBox.EMPTY;
      for (int i = 0; i < tile_shape_count(); ++i)
         {
         PlaShape curr_shape = get_shape(i);
         if (curr_shape != null)
            {
            result = result.union(curr_shape.bounding_box());
            }
         }
      return result;
      }

   @Override
   public int tile_shape_count()
      {
      LibPadstack padstack = get_padstack();
      int from_layer = padstack.from_layer();
      int to_layer = padstack.to_layer();
      return to_layer - from_layer + 1;
      }

   @Override
   protected final ShapeTile[] calculate_tree_shapes(ShapeSearchTree p_search_tree)
      {
      return p_search_tree.calculate_tree_shapes(this);
      }

   /**
    * Returns the smallest distance from the center to the border of the shape on any layer.
    */
   public final double smallest_radius()
      {
      double result = Double.MAX_VALUE;
      PlaPointFloat c = center_get().to_float();
      for (int i = 0; i < tile_shape_count(); ++i)
         {
         PlaShape curr_shape = get_shape(i);
         if (curr_shape != null)
            {
            result = Math.min(result, curr_shape.border_distance(c));
            }
         }
      return result;
      }

   /** 
    * @return the center point of this DrillItem
    */
   public PlaPointInt center_get()
      {
      return abit_center;
      }

   protected final void center_set(PlaPointInt p_center)
      {
      abit_center = p_center;
      }

   protected final void center_clear()
      {
      abit_center = null;
      }
   
   
   /**
    * Returns the padstack of this drillitem.
    */
   public abstract LibPadstack get_padstack();

   public ShapeTile get_tree_shape_on_layer(ShapeSearchTree p_tree, int p_layer)
      {
      int from_layer = first_layer();
      int to_layer = last_layer();
      if (p_layer < from_layer || p_layer > to_layer)
         {
         System.out.println("DrillItem.get_tree_shape_on_layer: p_layer out of range");
         return null;
         }
      return get_tree_shape(p_tree, p_layer - from_layer);
      }

   public ShapeTile get_tile_shape_on_layer(int p_layer)
      {
      int from_layer = first_layer();
      int to_layer = last_layer();
      if (p_layer < from_layer || p_layer > to_layer)
         {
         System.out.println("DrillItem.get_tile_shape_on_layer: p_layer out of range");
         return null;
         }
      return get_tile_shape(p_layer - from_layer);
      }

   public PlaShape get_shape_on_layer(int p_layer)
      {
      int from_layer = first_layer();
      int to_layer = last_layer();
      if (p_layer < from_layer || p_layer > to_layer)
         {
         System.out.println("DrillItem.get_shape_on_layer: p_layer out of range");
         return null;
         }
      return get_shape(p_layer - from_layer);
      }

   @Override
   public Set<BrdItem> get_normal_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();

      PlaPointInt drill_center = center_get();

      ShapeTile search_shape = new ShapeTileBox(drill_center);
      
      Set<ShapeTreeObject> overlaps = r_board.overlapping_objects(search_shape, -1);
      
      Iterator<ShapeTreeObject> iter = overlaps.iterator();

      while (iter.hasNext())
         {
         ShapeTreeObject curr_ob = iter.next();

         // skip myself
         if ( curr_ob == this ) continue;
         
         // skip what are not board items
         if (!(curr_ob instanceof BrdItem)) continue;
         
         BrdItem curr_item = (BrdItem) curr_ob;
         
         // skip if current item does not share my net
         if ( ! curr_item.shares_net(this)) continue;

         // skip if current item does not share my layer
         if (  ! curr_item.shares_layer(this)) continue;
         
         if (curr_item instanceof BrdTrace)
            {
            BrdTrace curr_trace = (BrdTrace) curr_item;
            if (drill_center.equals(curr_trace.corner_first()) || drill_center.equals(curr_trace.corner_last()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) curr_item;
            if (drill_center.equals(curr_drill_item.center_get()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAreaConduction)
            {
            BrdAreaConduction curr_area = (BrdAreaConduction) curr_item;
            if (curr_area.get_area().contains(drill_center))
               {
               result.add(curr_item);
               }
            }
         }
      return result;
      }

   @Override
   public PlaPointInt normal_contact_point(BrdAbit p_other)
      {
      if ( ! shares_layer(p_other) ) return null;
      
      PlaPointInt my_center = center_get();
      
      if ( my_center.equals(p_other.center_get())) return my_center;
      
      return null;
      }

   @Override
   public PlaPointInt normal_contact_point(BrdTrace p_trace)
      {
      if ( ! shares_layer(p_trace)) return null;

      PlaPointInt drill_center = center_get();

      if (drill_center.equals(p_trace.corner_first()) || drill_center.equals(p_trace.corner_last()))
         {
         return drill_center;
         }
      
      return null;
      }

   @Override
   public ArrayList<PlaPointInt> get_ratsnest_corners()
      {
      ArrayList<PlaPointInt> result = new ArrayList<PlaPointInt>(1);
      result.add (center_get());
      return result;
      }

   @Override
   public ShapeTile get_trace_connection_shape(ShapeSearchTree p_search_tree, int p_index)
      {
      return new ShapeTileBox(center_get());
      }

   /** 
    * False, if this drillitem is places on the back side of the board 
    */
   public boolean is_placed_on_front()
      {
      return true;
      }

   /**
    * Return the mininal width of the shapes of this DrillItem on all signal layers.
    */
   public double min_width()
      {
      if (precalculated_min_width != null ) return precalculated_min_width.doubleValue();
      
      double min_width = Integer.MAX_VALUE;
      int begin_layer = first_layer();
      int end_layer = last_layer();

      for (int curr_layer = begin_layer; curr_layer <= end_layer; ++curr_layer)
         {
         if ( ! r_board.layer_structure.is_signal(curr_layer)) continue;
         
         PlaShape curr_shape = get_shape_on_layer(curr_layer);
         
         if (curr_shape == null) continue;
         
         ShapeTileBox curr_bounding_box = curr_shape.bounding_box();
         min_width = Math.min(min_width, curr_bounding_box.width());
         min_width = Math.min(min_width, curr_bounding_box.height());
         }

      precalculated_min_width = Double.valueOf(min_width);

      return precalculated_min_width.doubleValue();
      }

   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      
      precalculated_first_layer = -1;
      precalculated_last_layer = -1;
      }

   @Override
   public int get_draw_priority()
      {
      return GdiDrawable.MIDDLE_DRAW_PRIORITY;
      }

   @Override
   public void draw( Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0) return;

      int from_layer = first_layer();
      int to_layer = last_layer();
      // Decrease the drawing intensity for items with many layers.
      double visibility_factor = 0;
      for (int i = from_layer; i <= to_layer; ++i)
         {
         visibility_factor += p_graphics_context.get_layer_visibility(i);
         }

      if (visibility_factor < 0.001) return;
      
      double intensity = p_intensity / Math.max(visibility_factor, 1);
      for (int i = 0; i <= to_layer - from_layer; ++i)
         {
         PlaShape curr_shape = get_shape(i);
         if (curr_shape == null)
            {
            continue;
            }
         java.awt.Color color = p_color_arr[from_layer + i];
         double layer_intensity = intensity * p_graphics_context.get_layer_visibility(from_layer + i);
         p_graphics_context.fill_area(curr_shape, p_g, color, layer_intensity);
         }
      }

   }
