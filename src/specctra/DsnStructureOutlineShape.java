package specctra;

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
