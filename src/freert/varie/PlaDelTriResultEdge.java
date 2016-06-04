package freert.varie;

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

import freert.planar.PlaPointInt;


/**
 * Describes a line segment in the result of the Delaunay Triangulation.
 */
public final class PlaDelTriResultEdge
   {
   // The start point of the line segment
   public final PlaPointInt start_point;
   // The object at the start point of the line segment
   public final PlaDelTriStorable start_object;
   // The end point of the line segment
   public final PlaPointInt end_point;
   // The object at the end point of the line segment 
   public final PlaDelTriStorable end_object;

   public PlaDelTriResultEdge(PlaPointInt p_start_point, PlaDelTriStorable p_start_object, PlaPointInt p_end_point, PlaDelTriStorable p_end_object)
      {
      start_point = p_start_point;
      start_object = p_start_object;
      end_point = p_end_point;
      end_object = p_end_object;
      }
   }
