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

import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;

/**
 * Type of a single item in the result list connection_items. Used to create a new PolylineTrace.
 */
public final class ArtLocateResult
   {
   private final PlaPointIntAlist corners;

   public final int layer;

   public ArtLocateResult(PlaPointIntAlist p_corners, int p_layer)
      {
      corners = p_corners;
      layer = p_layer;
      }
   
   public PlaPointInt corner (int index )
      {
      return corners.get(index);
      }
   
   public PlaPointInt corner_first()
      {
      return corner(0);
      }
   
   public PlaPointInt corner_last()
      {
      return corner(size(-1));
      }
   
   public int size ()
      {
      return corners.size();
      }
   
   public int size (int offset )
      {
      return size() + offset;
      }
   
   }
