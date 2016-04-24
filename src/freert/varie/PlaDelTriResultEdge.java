package freert.varie;

import freert.planar.PlaPoint;


/**
 * Describes a line segment in the result of the Delaunay Triangulation.
 */
public final class PlaDelTriResultEdge
   {
   // The start point of the line segment
   public final PlaPoint start_point;
   // The object at the start point of the line segment
   public final PlaDelTriStorable start_object;
   // The end point of the line segment
   public final PlaPoint end_point;
   // The object at the end point of the line segment 
   public final PlaDelTriStorable end_object;

   public PlaDelTriResultEdge(PlaPoint p_start_point, PlaDelTriStorable p_start_object, PlaPoint p_end_point, PlaDelTriStorable p_end_object)
      {
      start_point = p_start_point;
      start_object = p_start_object;
      end_point = p_end_point;
      end_object = p_end_object;
      }
   }
