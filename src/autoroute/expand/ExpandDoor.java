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
 * ExpansionDoor.java
 *
 * Created on 6. Januar 2004, 07:23
 */
package autoroute.expand;

import java.util.ArrayList;
import autoroute.ArtEngine;
import autoroute.maze.MazeSearchElement;
import freert.planar.PlaDimension;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentFloat;
import freert.planar.ShapeTile;

/**
 * An ExpansionDoor is a common edge between two ExpansionRooms
 * @author Alfons Wirtz
 */
public final class ExpandDoor implements ExpandObject
   {
   // The first room of this door
   public final ExpandRoom first_room;
   // The second room of this door
   public final ExpandRoom second_room;
   // The dimension of a door may be 1 or 2.
   public final PlaDimension dimension;

   // each section of the following array can be expanded separately by the maze search algorithm
   private final ArrayList<MazeSearchElement> section_list = new ArrayList<MazeSearchElement>();

   
   public ExpandDoor(ExpandRoom p_first_room, ExpandRoom p_second_room, PlaDimension p_dimension)
      {
      first_room = p_first_room;
      second_room = p_second_room;
      dimension = p_dimension;
      }

   public ExpandDoor(ExpandRoom p_first_room, ExpandRoom p_second_room)
      {
      first_room = p_first_room;
      second_room = p_second_room;
      dimension = first_room.get_shape().intersection(second_room.get_shape()).dimension();
      }

   /**
    * Calculates the intersection of the shapes of the 2 rooms belonging to this door.
    */
   @Override
   public ShapeTile get_shape()
      {
      ShapeTile first_shape = first_room.get_shape();
      
      ShapeTile second_shape = second_room.get_shape();
      
      return first_shape.intersection(second_shape);
      }

   /**
    * The dimension of a door may be 1 or 2. 2-dimensional doors can only exist between ObstacleExpansionRooms
    */
   @Override
   public PlaDimension get_dimension()
      {
      return dimension;
      }

   /**
    * Returns the other room of this door, or null, if p_roon is neither equal to this.first_room nor to this.second_room.
    */
   public ExpandRoom other_room(ExpandRoom p_room)
      {
      if (p_room == first_room)
         return second_room;
      else if (p_room == second_room)
         return first_room;
      else
         return null;
      }

   /**
    * Returns the other room of this door, or null, 
    * if p_roon is neither equal to this.first_room nor to this.second_room, or if the
    * other room is not a CompleteExpansionRoom.
    */
   public ExpandRoomComplete other_room(ExpandRoomComplete p_room)
      {
      ExpandRoom result;
      
      if (p_room == first_room)
         {
         result = second_room;
         }
      else if (p_room == second_room)
         {
         result = first_room;
         }
      else
         {
         return null;
         }
      
      if ( result instanceof ExpandRoomComplete )
         {
         return (ExpandRoomComplete) result;
         }
      
      return null;
      }

   @Override
   public int maze_search_element_count()
      {
      return section_list.size();
      }

   @Override
   public MazeSearchElement get_maze_search_element(int p_no)
      {
      return section_list.get(p_no);
      }

   /**
    * Calculates the Line segments of the sections of this door.
    */
   public PlaSegmentFloat[] get_section_segments(double p_offset)
      {
      double offset = p_offset + ArtEngine.TRACE_WIDTH_TOLERANCE;

      ShapeTile door_shape = get_shape();

      if (door_shape.is_empty()) return new PlaSegmentFloat[0];
         
      PlaSegmentFloat door_line_segment;
      PlaSegmentFloat shrinked_line_segment;
      
      if ( dimension.is_line() )
         {
         door_line_segment = door_shape.diagonal_corner_segment();
         shrinked_line_segment = door_line_segment.shrink_segment(offset);
         }
      else if ( dimension.is_area() &&  (first_room instanceof ExpandRoomFreespaceComplete) && (second_room instanceof ExpandRoomFreespaceComplete) )
         {
         // Overlapping doors at a corner possible in case of 90- or 45-degree routing.
         // In case of freeangle routing the corners are cut off.
         door_line_segment = calc_door_line_segment(door_shape);
         if (door_line_segment == null)
            {
            // CompleteFreeSpaceExpansionRoom inside other room
            return new PlaSegmentFloat[0];
            }
         
         if (door_line_segment.point_b.dustance_square(door_line_segment.point_a) < 4 * offset * offset)
            {
            // door is small, 2 dimensional small doors are not yet expanded.
            return new PlaSegmentFloat[0];
            }
         shrinked_line_segment = door_line_segment.shrink_segment(offset);
         }
      else
         {
         PlaPointFloat gravity_point = door_shape.centre_of_gravity();
         door_line_segment = new PlaSegmentFloat(gravity_point, gravity_point);
         shrinked_line_segment = door_line_segment;
         }
      double c_max_door_section_width = 10 * offset;
      
      int section_count = (int) (door_line_segment.point_b.distance(door_line_segment.point_a) / c_max_door_section_width) + 1;
      
      allocate_sections(section_count);
      
      PlaSegmentFloat[] result = shrinked_line_segment.divide_segment_into_sections(section_count);
      
      return result;
      }

   /** 
    * allocates and initializes p_section_count sections 
    */
   private void allocate_sections(int p_section_count)
      {
      section_list.ensureCapacity(p_section_count);
      
      while ( section_list.size() < p_section_count ) 
         {
         section_list.add(new MazeSearchElement());
         }
      }
   
   /**
    * Calculates a diagonal line of the 2-dimensional p_door_shape which represents the restraint line between the shapes of
    * this.first_room and this.second_room.
    */
   private PlaSegmentFloat calc_door_line_segment(ShapeTile p_door_shape)
      {
      ShapeTile first_room_shape = first_room.get_shape();
      ShapeTile second_room_shape = second_room.get_shape();
      PlaPointInt first_corner = null;
      PlaPointInt second_corner = null;
      int corner_count = p_door_shape.border_line_count();
      
      for (int index = 0; index < corner_count; ++index)
         {
         PlaPointInt curr_corner = p_door_shape.corner(index);
         
         if ( ! ( ! first_room_shape.contains_inside(curr_corner) && !second_room_shape.contains_inside(curr_corner) ) ) continue;
         
         // curr_corner is on the border of both room shapes.
         if (first_corner == null)
            {
            first_corner = curr_corner;
            }
         else if (second_corner == null && !first_corner.equals(curr_corner))
            {
            second_corner = curr_corner;
            break;
            }
         }
      
      if (first_corner == null || second_corner == null) return null;

      return new PlaSegmentFloat(first_corner.to_float(), second_corner.to_float());
      }

   @Override
   public void reset()
      {
      for (MazeSearchElement curr_section : section_list)
         curr_section.reset();
      }

   }
