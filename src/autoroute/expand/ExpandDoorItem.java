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
 * TargetItemExpansionDoor.java
 *
 * Created on 2. Februar 2004, 12:59
 */
package autoroute.expand;

import freert.planar.PlaDimension;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileSimplex;
import autoroute.ArtItem;
import autoroute.maze.MazeSearchElement;
import board.items.BrdItem;
import board.kdtree.KdtreeShapeSearch;

/**
 * An expansion door leading to a start or destination item of the autoroute algorithm.
 *
 * @author Alfons Wirtz
 */
public final class ExpandDoorItem implements ExpandObject
   {
   public  final BrdItem item;
   public  final int tree_entry_no;
   public  final ExpandRoomComplete room;
   private final ShapeTile shape_tile;
   private final MazeSearchElement maze_search_info;
   

   public ExpandDoorItem(BrdItem p_item, int p_tree_entry_no, ExpandRoomComplete p_room, KdtreeShapeSearch p_search_tree)
      {
      item = p_item;
      tree_entry_no = p_tree_entry_no;
      room = p_room;
      if (room == null)
         {
         shape_tile = ShapeTileSimplex.EMPTY;
         }
      else
         {
         ShapeTile item_shape = item.get_tree_shape(p_search_tree, tree_entry_no);
         shape_tile = item_shape.intersection(room.get_shape());
         }
      maze_search_info = new MazeSearchElement();
      }

   @Override
   public ShapeTile get_shape()
      {
      return shape_tile;
      }

   @Override
   public PlaDimension get_dimension()
      {
      return PlaDimension.AREA;
      }

   public boolean is_destination_door()
      {
      ArtItem item_info = item.art_item_get();
      return !item_info.is_start_info();
      }

   @Override
   public ExpandRoomComplete other_room(ExpandRoomComplete p_room)
      {
      return null;
      }

   @Override
   public MazeSearchElement get_maze_search_element(int p_no)
      {
      return maze_search_info;
      }

   @Override
   public int maze_search_element_count()
      {
      return 1;
      }

   @Override
   public void reset()
      {
      maze_search_info.reset();
      }
   }
