package autoroute.maze;
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


/**
 * The result type of ArtMazeSearch.find_connection
 */
public class MazeSearchResult
   {
   public final ExpandObject destination_door;
   public final int section_no_of_door;

   public MazeSearchResult(ExpandObject p_destination_door, int p_section_no_of_door)
      {
      destination_door = p_destination_door;
      section_no_of_door = p_section_no_of_door;
      }
   }
