package freert.spectra;
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
import freert.planar.ShapeSegments;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 * Used to separate the holes in the outline.
 */
public final class DsnStructureOutlineShape
   {
   final ShapeSegments shape;
   final ShapeTileBox bounding_box;
   final ShapeTile[] convex_shapes;
   boolean is_hole;

   public DsnStructureOutlineShape(ShapeSegments p_shape)
      {
      shape = p_shape;
      bounding_box = p_shape.bounding_box();
      convex_shapes = p_shape.split_to_convex();
      is_hole = false;
      }

   /**
    * Returns true, if this shape contains all corners of p_other_shape.
    */
   boolean contains_all_corners(DsnStructureOutlineShape p_other_shape)
      {
      if ( convex_shapes == null)
         {
         // calculation of the convex shapes failed
         return false;
         }
      
      int corner_count = p_other_shape.shape.border_line_count();
      
      for (int index = 0; index < corner_count; ++index)
         {
         PlaPointInt curr_corner = p_other_shape.shape.corner(index);

         boolean is_contained = false;
         
         for (int jndex = 0; jndex < convex_shapes.length; ++jndex)
            {
            if ( ! convex_shapes[jndex].contains(curr_corner)) continue;

            is_contained = true;
            break;
            }
         
         if ( ! is_contained) return false;
         }
      return true;
      }
   }
