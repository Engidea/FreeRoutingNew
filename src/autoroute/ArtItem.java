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
 * ItemAutorouteInfo.java
 *
 * Created on 22. Februar 2004, 12:09
 */

package autoroute;

import java.awt.Graphics;
import java.util.ArrayList;
import autoroute.expand.ExpandRoomObstacle;
import board.items.BrdItem;
import board.kdtree.KdtreeShapeSearch;
import freert.graphics.GdiContext;

/**
 * Temporary data stored in board Items used in the autoroute algorithm
 *
 * @author Alfons Wirtz
 */

public final class ArtItem
   {
   private final BrdItem parent_item;
   private ArtConnection precalculated_connnection = null;

   // Defines, if this item belongs to the start or destination set of the maze search algorithm
   private boolean start_info;
   
   // ExpansionRoom for pushing or ripping the this object for each tree shape.
   private final ArrayList<ExpandRoomObstacle> expansion_room_arr = new ArrayList<ExpandRoomObstacle>();
   
   public ArtItem(BrdItem p_item)
      {
      parent_item = p_item;
      }

   /**
    * Looks, if the corresponding item belongs to the start or destination set of the autoroute algorithm. 
    * Only used, if the item belongs to the net, which will be currently routed.
    */
   public boolean is_start_info()
      {
      return start_info;
      }

   /**
    * Sets, if the corresponding item belongs to the start or destination set of the autoroute algorithm.
    *  Only used, if the item belongs to the net, which will be currently routed.
    */
   public void set_start_info(boolean p_value)
      {
      start_info = p_value;
      }

   /**
    * Returns the pre calculated connection of this item or null, if it is not yet pre calculated.
    */
   public ArtConnection get_precalculated_connection()
      {
      return precalculated_connnection;
      }

   /**
    * Sets the pre calculated connection of this item.
    */
   public void set_precalculated_connection(ArtConnection p_connection)
      {
      precalculated_connnection = p_connection;
      }

   /**
    * Gets the ExpansionRoom of of index p_index. Creates it, if it is not yet existing.
    */
   public ExpandRoomObstacle get_expansion_room(int p_index, KdtreeShapeSearch p_autoroute_tree)
      {
      int want_size = parent_item.tree_shape_count(p_autoroute_tree);
      
      expansion_room_arr.ensureCapacity(want_size);
      
      while (expansion_room_arr.size() < want_size ) expansion_room_arr.add(null);
      
      try
         {
         ExpandRoomObstacle risul = expansion_room_arr.get(p_index);
         
         if ( risul != null ) return risul;

         risul = new ExpandRoomObstacle(parent_item, p_index, p_autoroute_tree);
         
         expansion_room_arr.set(p_index, risul);
         
         return risul;
         }
      catch ( Exception exc )
         {
         System.out.println("ArtItem.get_expansion_room: p_index out of range");
         return null;
         }
      }

   /**
    * Resets the expansion rooms for autorouting the next connnection.
    */
   public void reset_doors()
      {
      for (ExpandRoomObstacle curr_room : expansion_room_arr)
         {
         if (curr_room == null) continue;

         curr_room.reset_doors();
         }
      }

   /**
    * Draws the shapes of the expansion rooms of this info for testing purposes.
    */
   public void draw( Graphics p_graphics, GdiContext p_graphics_context, double p_intensity)
      {
      if (expansion_room_arr == null) return;

      for (ExpandRoomObstacle curr_room : expansion_room_arr)
         {
         if (curr_room == null) continue;

         curr_room.draw(p_graphics, p_graphics_context, p_intensity);
         }
      }
   }
