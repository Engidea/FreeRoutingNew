package freert.varie;

import java.util.ArrayList;
import freert.planar.PlaPointInt;

/**
 * Interface with functionality required for objects to be used in a planar triangulation.
 */
public interface PlaDelTriStorable
   {
   /**
    * Returns an array of corners, which can be used in a planar triangulation.
    */
   ArrayList<PlaPointInt> get_triangulation_corners();
   }
