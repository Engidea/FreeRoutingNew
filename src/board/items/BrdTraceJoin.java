/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

package board.items;

import java.awt.Color;
import java.awt.Graphics;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import board.BrdConnectable;
import board.RoutingBoard;
import board.awtree.AwtreeObject;
import board.awtree.AwtreeShapeSearch;
import board.varie.BrdTraceInfo;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.graphics.GdiContext;
import freert.graphics.GdiDrawable;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.varie.NetNosList;
import gui.varie.ObjectInfoPanel;

/**
 * There is a need to clearly mark the joining point of traces, mostly so I could make sure that start and end trace points
 * are integer points, but the idea could also be that trace join could be "fixed". so specific points stay in place and so on
 * As usual, the idea is simple, the complicated part is to actually do it....
 * This is of course quite similar to a via but it has no padstack and the dimension should be the min trace width of the connected net
 */
public final class BrdTraceJoin extends BrdItem implements BrdConnectable, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   // The center point of the Join 
   private PlaPointInt abit_center;
   
   // pre calculated minimal width of the shapes of this DrillItem on all layers
   private Double min_width = null;

   // a trace join can only be on a specific layer
   private int on_layer = 0;

   public BrdTraceJoin(PlaPointInt p_center, NetNosList p_net_no_arr, int p_clearance_type, int p_id_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(p_net_no_arr, p_clearance_type, p_id_no, 0, p_fixed_state, p_board);

      abit_center = p_center;
      }
   
   /**
    * For the copy constructor
    */
   private BrdTraceJoin ( BrdTraceJoin p_other, int p_id_no )
      {
      super(p_other, p_id_no);
      
      abit_center = p_other.abit_center;
      }
   
   @Override
   public BrdTraceJoin copy(int p_id_no)
      {
      return new BrdTraceJoin(this,p_id_no);
      }
   
   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if (!is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.TRACEJ);
      }
   
   
   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      // this join is an obstacle only if it does NOT share a net with p_other
      return p_other.shares_net(this) == false;
      }

   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      abit_center = abit_center.translate_by(p_vector);
      
      clear_derived_data();
      }

   @Override
   public void rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      abit_center = abit_center.rotate_90_deg(p_factor, p_pole);
      
      clear_derived_data();
      }

   @Override
   public void rotate_deg(int p_angle_in_degree, PlaPointFloat p_pole)
      {
      PlaPointFloat new_center = abit_center.to_float().rotate_deg(p_angle_in_degree, p_pole);

      abit_center = new_center.round();
      
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      abit_center = abit_center.mirror_vertical(p_pole);
      
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
         if ( ! ( curr_contact instanceof BrdTracep) ) continue;

         BrdTracep curr_trace = (BrdTracep) curr_contact;

         BrdTraceInfo curr_trace_info = new BrdTraceInfo(curr_trace.get_layer(), curr_trace.get_half_width(), curr_trace.clearance_idx());
         
         contact_trace_info.add(curr_trace_info);
         }
      
      super.move_by(p_vector);

      // Insert a Trace from the old center to the new center, on all layers, where this DrillItem was connected to a Trace.
      PlaPointIntAlist connect_points = new PlaPointIntAlist(100);
      
      connect_points.add(old_center);
      
      PlaPointInt new_center = center_get();
      
      PlaPointInt add_corner = null;
      
      if (r_board.brd_rules.is_trace_snap_45())
         {
         add_corner = old_center.fortyfive_degree_corner(new_center, true);
         }

      if (add_corner != null)
         {
         connect_points.add(add_corner);
         }
      
      connect_points.add(new_center);
      
      for ( BrdTraceInfo curr_trace_info : contact_trace_info )
         {
         r_board.insert_trace(
               connect_points, 
               curr_trace_info.layer, 
               curr_trace_info.half_width, 
               net_nos, 
               curr_trace_info.clearance_type, 
               ItemFixState.UNFIXED);
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
      return on_layer;
      }

   @Override
   public int last_layer()
      {
      return on_layer;
      }

   /**
    * Need to have the shape...
    */
   public final ShapeTileOctagon get_shape()
      {
      ShapeTileOctagon octa = new ShapeTileOctagon(center_get());

      return octa.enlarge(min_width());
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      PlaShape curr_shape = get_shape();
      return curr_shape.bounding_box();
      }

   @Override
   public int tile_shape_count()
      {
      return 1;
      }

   @Override
   protected final ShapeTile[] calculate_tree_shapes(AwtreeShapeSearch p_search_tree)
      {
      return p_search_tree.calculate_tree_shapes(this);
      }

   /** 
    * Override in subclasses
    * @return the center point of this DrillItem
    */
   public PlaPointInt center_get()
      {
      return abit_center;
      }

   public final void center_set(PlaPointInt p_center)
      {
      abit_center = p_center;
      }

   @Override
   public Set<BrdItem> get_normal_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();

      PlaPointInt drill_center = center_get();

      ShapeTile search_shape = new ShapeTileBox(drill_center);
      
      Set<AwtreeObject> overlaps = r_board.overlapping_objects(search_shape, -1);
      
      Iterator<AwtreeObject> iter = overlaps.iterator();

      while (iter.hasNext())
         {
         AwtreeObject curr_ob = iter.next();

         // skip myself
         if ( curr_ob == this ) continue;
         
         // skip what are not board items
         if (!(curr_ob instanceof BrdItem)) continue;
         
         BrdItem curr_item = (BrdItem) curr_ob;
         
         // skip if current item does not share my net
         if ( ! curr_item.shares_net(this)) continue;

         // skip if current item does not share my layer
         if (  ! curr_item.shares_layer(this)) continue;
         
         if (curr_item instanceof BrdTracep)
            {
            BrdTracep curr_trace = (BrdTracep) curr_item;
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
   protected PlaPointInt normal_contact_point(BrdAbit p_other)
      {
      if ( ! shares_layer(p_other) ) return null;
      
      PlaPointInt my_center = center_get();
      
      if ( my_center.equals(p_other.center_get())) return my_center;
      
      return null;
      }

   @Override
   protected PlaPointInt normal_contact_point(BrdTracep p_trace)
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
   public ShapeTile get_trace_connection_shape(AwtreeShapeSearch p_search_tree, int p_index)
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
      if (min_width != null ) return min_width.doubleValue();
      
      min_width = Double.valueOf(500);

      return min_width.doubleValue();
      }

   
   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      p_window.append_bold("trace_join");
      p_window.append(" center ");
      p_window.append(center_get().to_float());
      p_window.append(r_board.layer_structure.get_name(first_layer()));
      print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }
   
   
   @Override
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
   
   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      }

   @Override
   public int get_draw_priority()
      {
      return GdiDrawable.MIDDLE_DRAW_PRIORITY;
      }

   @Override
   public double get_draw_intensity( GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_color_intensity();
      }

   @Override
   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_colors(is_user_fixed());
      }
   

   
   @Override
   public final void draw( Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0) return;

      int from_layer = first_layer();
      int to_layer = last_layer();
      // Decrease the drawing intensity for items with many layers.
      double visibility_factor = 0;
      
      for (int index = from_layer; index <= to_layer; ++index)
         {
         visibility_factor += p_graphics_context.get_layer_visibility(index);
         }

      if (visibility_factor < 0.001) return;
      
      double intensity = p_intensity / Math.max(visibility_factor, 1);
      for (int index = 0; index <= to_layer - from_layer; ++index)
         {
         PlaShape curr_shape = get_shape();
         
         if (curr_shape == null) continue;

         Color color = p_color_arr[from_layer + index];
         double layer_intensity = intensity * p_graphics_context.get_layer_visibility(from_layer + index);
         p_graphics_context.fill_area(curr_shape, p_g, color, layer_intensity);
         }
      }

   }
