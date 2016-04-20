package specctra;


/** A max_length of -1 indicates, tha no maximum length is defined. */
public class DsnCircuitReadScopeResult
   {
   public DsnCircuitReadScopeResult(double p_max_length, double p_min_length, java.util.Collection<String> p_use_via, java.util.Collection<String> p_use_layer)
      {
      max_length = p_max_length;
      min_length = p_min_length;
      use_via = p_use_via;
      use_layer = p_use_layer;
      }

   public final double max_length;
   public final double min_length;
   public final java.util.Collection<String> use_via;
   public final java.util.Collection<String> use_layer;
   }
