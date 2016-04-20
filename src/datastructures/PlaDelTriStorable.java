package datastructures;

import planar.PlaPoint;

/**
 * Interface with functionality required for objects to be used in a planar triangulation.
 */
public interface PlaDelTriStorable
   {
   /**
    * Returns an array of corners, which can be used in a planar triangulation.
    */
   PlaPoint[] get_triangulation_corners();
   }
