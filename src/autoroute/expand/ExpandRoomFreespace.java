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
 * Created on 29. Dezember 2003, 08:37
 */

package autoroute.expand;

import java.util.LinkedList;
import java.util.List;
import planar.ShapeTile;

/**
 * Expansion Areas used by the maze search algorithm.
 *
 * @author Alfons Wirtz
 */
public abstract class ExpandRoomFreespace implements ExpandRoom
   {
   // The list of doors to neighbor expansion rooms 
   private final List<ExpandDoor> doors_list = new LinkedList<ExpandDoor>();
   // The layer of this room 
   private final int layer_no;

   // The shape of this room 
   private ShapeTile room_shape;


   /**
    * Creates a new instance of FreeSpaceExpansionRoom. The shape is normally unbounded at construction time of this room. The final
    * (completed) shape will be a subshape of the start shape, which does not overlap with any obstacle, and is as big as possible.
    * p_contained_points will remain contained in the shape, after it is completed.
    */
   public ExpandRoomFreespace(ShapeTile p_shape, int p_layer)
      {
      room_shape = p_shape;
      layer_no = p_layer;
      }

   /**
    * Adds p_door to the list of doors of this room.
    */
   public void add_door(ExpandDoor p_door)
      {
      if ( p_door == null ) return;
      
      doors_list.add(p_door);
      }

   /**
    * Returns the list of doors of this room to neighbour expansion rooms
    */
   @Override
   public List<ExpandDoor> get_doors()
      {
      return doors_list;
      }

   /**
    * Removes all doors from this room.
    */
   @Override
   public void clear_doors()
      {
      doors_list.clear();
      }

   @Override
   public void reset_doors()
      {
      for (ExpandObject curr_door : doors_list)
         curr_door.reset();
      }

   @Override
   public boolean remove_door(ExpandObject p_door)
      {
      if ( p_door == null ) return false;
      
      return doors_list.remove(p_door);
      }

   @Override
   public ShapeTile get_shape()
      {
      return room_shape;
      }

   public final void set_shape(ShapeTile p_shape)
      {
      room_shape = p_shape;
      }

   @Override
   public int get_layer()
      {
      return layer_no;
      }

   @Override
   public boolean door_exists(ExpandRoom p_other)
      {
      if ( p_other == null ) return false;
      
      for ( ExpandDoor curr_door : doors_list )
         {
         if (curr_door.first_room == p_other || curr_door.second_room == p_other)
            return true;
         }

      return false;
      }
   }
