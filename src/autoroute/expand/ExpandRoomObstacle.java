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
 * ObstacleExpansionRoom.java
 *
 * Created on 17. April 2006, 06:45
 *
 */

package autoroute.expand;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import planar.PlaDimension;
import planar.ShapeTile;
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeObject;

/**
 * Expansion Room used for pushing and ripping obstacles in the autoroute algorithm.
 *
 * @author Alfons Wirtz
 */
public final class ExpandRoomObstacle implements ExpandRoomComplete
   {
   // The list of doors to neighbor expansion rooms 
   private final List<ExpandDoor> doors_list= new LinkedList<ExpandDoor>();

   private final BrdItem item;
   private final int index_in_item;
   private final ShapeTile shape_tile;

   private boolean doors_calculated = false;
   
   public ExpandRoomObstacle(BrdItem p_item, int p_index_in_item, ShapeSearchTree p_shape_tree)
      {
      item = p_item;
      index_in_item = p_index_in_item;
      shape_tile = p_item.get_tree_shape(p_shape_tree, p_index_in_item);
      }

   public int get_index_in_item()
      {
      return index_in_item;
      }

   @Override
   public int get_layer()
      {
      return item.shape_layer(index_in_item);
      }

   @Override
   public ShapeTile get_shape()
      {
      return shape_tile;
      }

   @Override
   public boolean door_exists(ExpandRoom p_other)
      {
      if ( p_other == null ) return false;
      
      for (ExpandDoor curr_door : doors_list)
         {
         if (curr_door.first_room == p_other || curr_door.second_room == p_other) return true;
         }

      return false;
      }

   @Override
   public void add_door(ExpandDoor p_door)
      {
      if ( p_door == null ) return;
      
      doors_list.add(p_door);
      }

   /**
    * Creates a 2-dim door with the other obstacle room, if that is useful for the autoroute algorithm. It is assumed that this room
    * and p_other have a 2-dimensional overlap. Returns false, if no door was created.
    */
   public boolean create_overlap_door(ExpandRoomObstacle p_other)
      {
      if ( p_other == null ) return false;
      
      if ( door_exists(p_other)) return false;
      
      if (!(item.is_route() && p_other.item.is_route())) return false;
      
      if (! item.shares_net(p_other.item)) return false;

      if ( item == p_other.item)
         {
         if (!( item instanceof BrdTracePolyline)) return false;

         // create only doors between consecutive trace segments
         if ( index_in_item != p_other.index_in_item + 1 &&  index_in_item != p_other.index_in_item - 1)
            {
            return false;
            }
         }
      
      ExpandDoor new_door = new ExpandDoor(this, p_other, PlaDimension.AREA);
      
      add_door(new_door);
      
      p_other.add_door(new_door);
      
      return true;
      }

   /**
    * Returns the list of doors of this room to neighbour expansion rooms
    */
   @Override
   public List<ExpandDoor> get_doors()
      {
      return doors_list;
      }

   @Override
   public void clear_doors()
      {
      doors_list.clear();
      }

   @Override
   public void reset_doors()
      {
      for (ExpandObject curr_door : doors_list) curr_door.reset();
      }

   @Override
   public Collection<ExpandDoorItem> get_target_doors()
      {
      return new LinkedList<ExpandDoorItem>();
      }

   public BrdItem get_item()
      {
      return item;
      }

   @Override
   public ShapeTreeObject get_object()
      {
      return item;
      }

   @Override
   public boolean remove_door(ExpandObject p_door)
      {
      if ( p_door == null ) return false;
      
      return doors_list.remove(p_door);
      }

   /**
    * Returns, if all doors to the neighbor rooms are calculated.
    */
   public boolean all_doors_calculated()
      {
      return doors_calculated;
      }

   public void set_doors_calculated(boolean p_value)
      {
      doors_calculated = p_value;
      }

   /**
    * Draws the shape of this room.
    */
   @Override
   public void draw(java.awt.Graphics p_graphics, graphics.GdiContext p_graphics_context, double p_intensity)
      {
      Color draw_color = java.awt.Color.WHITE;

      double layer_visibility = p_graphics_context.get_layer_visibility(get_layer());
      
      p_graphics_context.fill_area(get_shape(), p_graphics, draw_color, p_intensity * layer_visibility);
      
      p_graphics_context.draw_boundary(get_shape(), 0, draw_color, p_graphics, layer_visibility);
      }
   }
