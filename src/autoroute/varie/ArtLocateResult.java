package autoroute.varie;

import freert.planar.PlaPointInt;

/**
 * Type of a single item in the result list connection_items. Used to create a new PolylineTrace.
 */
public final class ArtLocateResult
   {
   public final int layer;
   private final PlaPointInt[] corners;

   public ArtLocateResult(PlaPointInt[] p_corners, int p_layer)
      {
      corners = p_corners;
      layer = p_layer;
      }
   
   public PlaPointInt corner (int index )
      {
      return corners[index];
      }
   
   public PlaPointInt first()
      {
      return corner(0);
      }
   
   public PlaPointInt last()
      {
      return corner(size(-1));
      }
   
   public int size ()
      {
      return corners.length;
      }
   
   public int size (int offset )
      {
      return corners.length + offset;
      }
   
   }
