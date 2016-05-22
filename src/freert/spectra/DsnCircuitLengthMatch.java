package freert.spectra;


/** A max_length of -1 indicates, that no maximum length is defined. */
class DsnCircuitLengthMatch
   {
   public DsnCircuitLengthMatch(double p_max_length, double p_min_length)
      {
      max_length = p_max_length;
      min_length = p_min_length;
      }

   public final double max_length;
   public final double min_length;
   }
