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
 * ExpansionDrill.java
 *
 * Created on 19. April 2004, 08:00
 */
package autoroute.expand;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import autoroute.ArtEngine;
import autoroute.maze.MazeSearchElement;
import board.shape.ShapeTreeObject;
import freert.planar.PlaDimension;
import freert.planar.PlaPointInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 * Layer change expansion object in the maze search algorithm.
 *
 * @author alfons
 */
public final class ExpandDrill implements ExpandObject
   {
   private final ArrayList<MazeSearchElement> maze_search_info_list;
   
   // Array of dimension last_layer - first_layer + 1
   public final ExpandRoomComplete[] room_arr;

   // The shape of the drill
   private final ShapeTile shape;
   // The location, where the drill is checked
   public final PlaPointInt location;
   // The first layer of the drill 
   public final int first_layer_no;
   // The last layer of the drill 
   public final int last_layer_no;
   
   public ExpandDrill(ShapeTile p_shape, PlaPointInt p_location, int p_first_layer_no, int p_last_layer_no)
      {
      shape = p_shape;
      location = p_location;
      first_layer_no = p_first_layer_no;
      last_layer_no = p_last_layer_no;
      
      int layer_count = p_last_layer_no - p_first_layer_no + 1;
      
      room_arr = new ExpandRoomComplete[layer_count];
      
      maze_search_info_list = new ArrayList<MazeSearchElement>(layer_count);
      
      for (int index=0; index < layer_count; ++index)  maze_search_info_list.add(new MazeSearchElement());
      }

   /**
    * Looks for the expansion room of this drill on each layer. Creates a CompleteFreeSpaceExpansionRoom, if no expansion room is
    * found. Returns false, if that was not possible because of an obstacle at this.location on some layer in the compensated search
    * tree.
    */
   public boolean calculate_expansion_rooms(ArtEngine p_autoroute_engine)
      {
      ShapeTile search_shape = new ShapeTileBox(location);
      
      Collection<ShapeTreeObject> overlaps = p_autoroute_engine.autoroute_search_tree.find_overlap_objects(search_shape, -1);

      for (int index = first_layer_no; index <= last_layer_no; ++index)
         {
         ExpandRoomComplete found_room = null;
         Iterator<ShapeTreeObject> iter = overlaps.iterator();
         while (iter.hasNext())
            {
            board.shape.ShapeTreeObject curr_ob = iter.next();
            
            if (!(curr_ob instanceof ExpandRoomComplete))
               {
               iter.remove();
               continue;
               }
            
            ExpandRoomComplete curr_room = (ExpandRoomComplete) curr_ob;
            
            if (curr_room.get_layer() == index)
               {
               found_room = curr_room;
               iter.remove();
               break;
               }
            }
         
         if (found_room == null)
            {
            // create a new expansion room on this layer
            ExpandRoomFreespaceIncomplete new_incomplete_room = new ExpandRoomFreespaceIncomplete(null, index, search_shape);
            Collection<ExpandRoomFreespaceComplete> new_rooms = p_autoroute_engine.complete_expansion_room(new_incomplete_room);
            if (new_rooms.size() != 1)
               {
               // the size may be 0 because of an obstacle in the compensated tree at this.location
               return false;
               }
            Iterator<ExpandRoomFreespaceComplete> iterb = new_rooms.iterator();

            if (iterb.hasNext()) found_room = iterb.next();
            }
         
         room_arr[index - first_layer_no] = found_room;
         }
      
      return true;
      }

   @Override
   public ShapeTile get_shape()
      {
      return shape;
      }

   @Override
   public PlaDimension get_dimension()
      {
      return PlaDimension.AREA;
      }

   @Override
   public ExpandRoomComplete other_room(ExpandRoomComplete p_room)
      {
      return null;
      }

   @Override
   public int maze_search_element_count()
      {
      return maze_search_info_list.size();
      }

   @Override
   public MazeSearchElement get_maze_search_element(int p_no)
      {
      return maze_search_info_list.get(p_no);
      }

   @Override
   public void reset()
      {
      for (MazeSearchElement curr_info : maze_search_info_list)
         curr_info.reset();
      }

   /*
    * Test draw of the the shape of this drill.
    */
   public void draw(java.awt.Graphics p_graphics, freert.graphics.GdiContext p_graphics_context, double p_intensity)
      {
      Color draw_color = p_graphics_context.get_hilight_color();

      p_graphics_context.fill_area( shape, p_graphics, draw_color, p_intensity);
      
      p_graphics_context.draw_boundary(shape, 0, draw_color, p_graphics, 1);
      }
   }
