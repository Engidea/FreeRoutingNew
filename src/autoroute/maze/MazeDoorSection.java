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

import autoroute.expand.ExpandDoor;
import freert.planar.PlaSegmentFloat;

public final class MazeDoorSection
   {
   public final ExpandDoor door;
   public final int section_no;
   public final PlaSegmentFloat section_line;

   public MazeDoorSection(ExpandDoor p_door, int p_section_no, PlaSegmentFloat p_section_line)
      {
      door = p_door;
      section_no = p_section_no;
      section_line = p_section_line;
      }
   }
