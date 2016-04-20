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
 */

package board.varie;

import planar.OctagonMutable;
import planar.PlaPointFloat;
import planar.ShapeTileBox;
import planar.ShapeTileOctagon;

/**
 * Used internally for marking changed areas on the board after shoving and optimizing items.
 * @author Alfons Wirtz
 */

public final class BrdChangedArea
   {
   private final int layer_count;
   private OctagonMutable[] arr;
   
   public BrdChangedArea(int p_layer_count)
      {
      layer_count = p_layer_count;
      arr = new OctagonMutable[layer_count];

      for (int i = 0; i < layer_count; ++i)
         {
         // Initialize all octagons to empty
         arr[i] = new OctagonMutable();
         }
      }

   /**
    * enlarges the octagon on p_layer, so that it contains p_point
    */
   public void join(PlaPointFloat p_point, int p_layer)
      {
      OctagonMutable curr = arr[p_layer];
      curr.lx = Math.min(p_point.point_x, curr.lx);
      curr.ly = Math.min(p_point.point_y, curr.ly);
      curr.rx = Math.max(curr.rx, p_point.point_x);
      curr.uy = Math.max(curr.uy, p_point.point_y);

      double tmp = p_point.point_x - p_point.point_y;
      curr.ulx = Math.min(curr.ulx, tmp);
      curr.lrx = Math.max(curr.lrx, tmp);

      tmp = p_point.point_x + p_point.point_y;
      curr.llx = Math.min(curr.llx, tmp);
      curr.urx = Math.max(curr.urx, tmp);
      }

   /**
    * enlarges the octagon on p_layer, so that it contains p_shape
    */
   public void join(planar.ShapeTile p_shape, int p_layer)
      {
      if (p_shape == null) return;

      int corner_count = p_shape.border_line_count();
      for (int i = 0; i < corner_count; ++i)
         {
         join(p_shape.corner_approx(i), p_layer);
         }
      }

   /**
    * get the marking octagon on layer p_layer
    */
   public ShapeTileOctagon get_area(int p_layer)
      {
      return arr[p_layer].to_int();
      }

   public ShapeTileBox surrounding_box()
      {
      int llx = Integer.MAX_VALUE;
      int lly = Integer.MAX_VALUE;
      int urx = Integer.MIN_VALUE;
      int ury = Integer.MIN_VALUE;

      for (int i = 0; i < layer_count; ++i)
         {
         OctagonMutable curr = arr[i];
         llx = Math.min(llx, (int) Math.floor(curr.lx));
         lly = Math.min(lly, (int) Math.floor(curr.ly));
         urx = Math.max(urx, (int) Math.ceil(curr.rx));
         ury = Math.max(ury, (int) Math.ceil(curr.uy));
         }
      if (llx > urx || lly > ury)
         {
         return ShapeTileBox.EMPTY;
         }
      return new ShapeTileBox(llx, lly, urx, ury);
      }

   /**
    * Initializes the marking octagon on p_layer to empty
    */
   public void set_empty(int p_layer)
      {
      arr[p_layer].set_empty();
      }
   }