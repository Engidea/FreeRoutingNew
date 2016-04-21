package autoroute.varie;

/** 
 * Array of via costs from one layer to the other layers 
 */
public final class ArtViaCost
   {
   public final int[] to_layer;

   public ArtViaCost(int p_layer_count)
      {
      to_layer = new int[p_layer_count];
      }
   }
