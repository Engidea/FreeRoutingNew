package autoroute.varie;

public final class ArtViaMask
   {
   public final int from_layer;
   public final int to_layer;
   public final boolean attach_smd_allowed;

   public ArtViaMask(int p_from_layer, int p_to_layer, boolean p_attach_smd_allowed)
      {
      from_layer = p_from_layer;
      to_layer = p_to_layer;
      attach_smd_allowed = p_attach_smd_allowed;
      }
   }
