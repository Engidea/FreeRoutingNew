package autoroute.varie;

import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;

/**
 * Type of a single item in the result list connection_items. Used to create a new PolylineTrace.
 */
public final class ArtLocateResult
   {
   private final PlaPointIntAlist corners;

   public final int layer;

   public ArtLocateResult(PlaPointIntAlist p_corners, int p_layer)
      {
      corners = p_corners;
      layer = p_layer;
      }
   
   public PlaPointInt corner (int index )
      {
      return corners.get(index);
      }
   
   public PlaPointInt corner_first()
      {
      return corner(0);
      }
   
   public PlaPointInt corner_last()
      {
      return corner(size(-1));
      }
   
   public int size ()
      {
      return corners.size();
      }
   
   public int size (int offset )
      {
      return size() + offset;
      }
   
   }
