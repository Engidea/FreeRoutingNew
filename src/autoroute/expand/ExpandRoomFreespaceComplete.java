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
 * CompleteFreeSpaceExpansionRoom.java
 *
 * Created on 10. Februar 2004, 10:12
 */

package autoroute.expand;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import autoroute.ArtEngine;
import board.BrdConnectable;
import board.items.BrdItem;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTree;
import board.shape.ShapeTreeEntry;
import board.shape.ShapeTreeLeaf;
import board.shape.ShapeTreeObject;
import freert.planar.ShapeTile;
import freert.varie.NetNosList;

/**
 * An expansion room, whose shape is completely calculated, so that it can be stored in a shape tree.
 *
 * @author Alfons Wirtz
 */
public final class ExpandRoomFreespaceComplete extends ExpandRoomFreespace implements ExpandRoomComplete, ShapeTreeObject
   {
   // The list of doors to items of the own net
   private final Collection<ExpandDoorItem> target_doors = new LinkedList<ExpandDoorItem>();
   // The array of entries in the SearchTree. Consists of just one element
   private ShapeTreeLeaf[] tree_entries = null;
   // identification number for implementing the Comparable interface
   private final int id_no;

   private boolean room_is_net_dependent = false;
   
   public ExpandRoomFreespaceComplete(ShapeTile p_shape, int p_layer, int p_id_no)
      {
      super(p_shape, p_layer);

      id_no = p_id_no;
      }

   @Override
   public void set_search_tree_entries(ShapeTreeLeaf[] p_entries, ShapeTree p_tree)
      {
      tree_entries = p_entries;
      }

   @Override
   public int compareTo(Object p_other)
      {
      if (p_other instanceof ExpandRoomFreespace)
         return ((ExpandRoomFreespaceComplete) p_other).id_no - this.id_no;
      else
         return -1;
      }

   /**
    * Removes the tree entries of this roomm from p_shape_tree.
    */
   public final void remove_from_tree(ShapeTree p_shape_tree)
      {
      if ( p_shape_tree == null ) return;
      
      p_shape_tree.remove(tree_entries);
      }

   @Override
   public int tree_shape_count(ShapeTree p_shape_tree)
      {
      return 1;
      }

   @Override
   public ShapeTile get_tree_shape(ShapeTree p_shape_tree, int p_index)
      {
      return get_shape();
      }

   @Override
   public int shape_layer(int p_index)
      {
      return get_layer();
      }

   @Override
   public boolean is_obstacle(int p_net_no)
      {
      return true;
      }

   @Override
   public boolean is_trace_obstacle(int p_net_no)
      {
      return true;
      }

   /**
    * Will be called, when the room overlaps with net dependent objects.
    */
   public final void set_net_dependent()
      {
      room_is_net_dependent = true;
      }

   /**
    * Returns, if the room overlaps with net dependent objects.
    *  In this case it cannot be retained, when the net number changes in autorouting.
    */
   public final boolean is_net_dependent()
      {
      return room_is_net_dependent;
      }

   /**
    * Returns the list doors to target items of this room
    */
   @Override
   public Collection<ExpandDoorItem> get_target_doors()
      {
      return target_doors;
      }

   /**
    * Adds p_door to the list of target doors of this room.
    */
   public final void add_target_door(ExpandDoorItem p_door)
      {
      if ( p_door == null ) return;
      
      target_doors.add(p_door);
      }

   @Override
   public boolean remove_door(ExpandObject p_door)
      {
      if ( p_door == null ) return false;
      
      if (p_door instanceof ExpandDoorItem)
         return target_doors.remove(p_door);
      else
         return super.remove_door(p_door);
      }

   @Override
   public ShapeTreeObject get_object()
      {
      return this;
      }

   /**
    * Calculates the doors to the start and destination items of the autoroute algorithm.
    */
   public final void calculate_target_doors(ShapeTreeEntry p_own_net_object, int p_net_no, ShapeSearchTree p_autoroute_search_tree)
      {
      set_net_dependent();

      if ( ! (p_own_net_object.object instanceof BrdConnectable)) return;
      
      BrdConnectable curr_object = (BrdConnectable) p_own_net_object.object;

      if ( ! curr_object.contains_net(p_net_no)) return;
      
      ShapeTile curr_connection_shape = curr_object.get_trace_connection_shape(p_autoroute_search_tree, p_own_net_object.shape_index_in_object);

      if ( curr_connection_shape == null ) return;
      
      if ( ! get_shape().intersects(curr_connection_shape)) return;

      BrdItem curr_item = (BrdItem) curr_object;
      
      ExpandDoorItem new_target_door = new ExpandDoorItem(curr_item, p_own_net_object.shape_index_in_object, this, p_autoroute_search_tree);
      
      add_target_door(new_target_door);
      }

   /**
    * Draws the shape of this room.
    */
   @Override
   public void draw(java.awt.Graphics p_graphics, freert.graphics.GdiContext p_graphics_context, double p_intensity)
      {
      Color draw_color = p_graphics_context.get_trace_colors(false)[get_layer()];
      
      double layer_visibility = p_graphics_context.get_layer_visibility(get_layer());
      
      p_graphics_context.fill_area( get_shape(), p_graphics, draw_color, p_intensity * layer_visibility);
      
      p_graphics_context.draw_boundary( get_shape(), 0, draw_color, p_graphics, layer_visibility);
      }

   /**
    * Check, if this FreeSpaceExpansionRoom is valid.
    */
   public final boolean validate(ArtEngine p_autoroute_engine)
      {
      boolean result = true;
      Collection<ShapeTreeEntry> overlapping_objects = new LinkedList<ShapeTreeEntry>();

      NetNosList net_no_arr = new NetNosList(1,p_autoroute_engine.get_net_no() );
      
      p_autoroute_engine.autoroute_search_tree.calc_overlapping_tree_entries(get_shape(), get_layer(), net_no_arr, overlapping_objects);

      for (ShapeTreeEntry curr_entry : overlapping_objects )
         {
         if (curr_entry.object == this) continue;

         ShapeTreeObject curr_object = (ShapeTreeObject) curr_entry.object;

         if (!curr_object.is_trace_obstacle(p_autoroute_engine.get_net_no())) continue;

         if (curr_object.shape_layer(curr_entry.shape_index_in_object) != get_layer()) continue;

         ShapeTile curr_shape = curr_object.get_tree_shape(p_autoroute_engine.autoroute_search_tree, curr_entry.shape_index_in_object);

         ShapeTile intersection = get_shape().intersection(curr_shape);

         if ( ! intersection.dimension().is_empty() )
            {
            System.out.println("ExpansionRoom overlap conflict");
            result = false;
            }
         
         }
      return result;
      }

   /**
    * Removes all doors and target doors from this room.
    */
   @Override
   public void clear_doors()
      {
      super.clear_doors();

      target_doors.clear();
      }

   @Override
   public void reset_doors()
      {
      super.reset_doors();
      
      for (ExpandObject curr_door : target_doors)
         curr_door.reset();

      }
   }
