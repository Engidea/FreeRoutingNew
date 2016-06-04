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

public final class ArtViaMask
   {
   public final int from_layer;
   public final int to_layer;
   public final boolean attach_smd_allowed;

   public ArtViaMask(int p_from_layer, int p_to_layer, boolean p_attach_smd_allowed)
      {
      from_layer = p_from_layer;
      to_layer = p_to_layer;
      attach_smd_allowed = p_attach_smd_allowed;
      }
   }
