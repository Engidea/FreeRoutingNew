package board.varie;

import planar.PlaDirectionInt;


/**
 * Describes an exit restriction from a trace from a pin pad.
 */
public class BrdTraceExitRestriction
   {
   public final PlaDirectionInt direction;
   public final double min_length;

   public BrdTraceExitRestriction(PlaDirectionInt p_direction, double p_min_length)
      {
      direction = p_direction;
      min_length = p_min_length;
      }
   }
