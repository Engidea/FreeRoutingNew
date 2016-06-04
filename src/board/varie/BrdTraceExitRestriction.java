package board.varie;
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

import freert.planar.PlaDirection;


/**
 * Describes an exit restriction from a trace from a pin pad.
 */
public class BrdTraceExitRestriction
   {
   public final PlaDirection direction;
   public final double min_length;

   public BrdTraceExitRestriction(PlaDirection p_direction, double p_min_length)
      {
      direction = p_direction;
      min_length = p_min_length;
      }
   }
