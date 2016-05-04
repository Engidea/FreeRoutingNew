package autoroute.varie;

import freert.planar.PlaPointInt;

/**
 * Type of a single item in the result list connection_items. Used to create a new PolylineTrace.
 */
public final class ArtLocateResult
   {
   public final int layer;
   public final PlaPointInt[] corners;

   public ArtLocateResult(PlaPointInt[] p_corners, int p_layer)
      {
      corners = p_corners;
      layer = p_layer;
      }
   }
