package autoroute.varie;
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

import autoroute.expand.ExpandObject;
import autoroute.expand.ExpandRoomComplete;


/**
 * Type of the elements of the list returned by this.backtrack(). 
 * Next_room is the common room of the current door and the next door in the backtrack list.
 */
public final class ArtBacktrackElement
   {
   public final ExpandObject door;
   public final int section_no_of_door;
   public final ExpandRoomComplete next_room;

   public ArtBacktrackElement(ExpandObject p_door, int p_section_no_of_door, ExpandRoomComplete p_room)
      {
      door = p_door;
      section_no_of_door = p_section_no_of_door;
      next_room = p_room;
      }
   }
