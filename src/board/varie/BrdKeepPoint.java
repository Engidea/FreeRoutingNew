package board.varie;

import planar.PlaPoint;

public final class BrdKeepPoint
   {
   // the point to keep 
   public final PlaPoint keep_point;
   // keep point layer....
   public final int on_layer;

   /**
    * If you use it then the values MUST be non null
    * @param p_keep_point
    * @param p_on_layer
    */
   public BrdKeepPoint ( PlaPoint p_keep_point, int p_on_layer )
      {
      if ( p_keep_point == null )
         throw new IllegalArgumentException("p_keep_point == null");

      keep_point = p_keep_point;
      on_layer = p_on_layer;
      }
   }
