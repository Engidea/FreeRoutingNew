package board.varie;

import freert.planar.PlaDirectionLong;


/**
 * Describes an exit restriction from a trace from a pin pad.
 */
public class BrdTraceExitRestriction
   {
   public final PlaDirectionLong direction;
   public final double min_length;

   public BrdTraceExitRestriction(PlaDirectionLong p_direction, double p_min_length)
      {
      direction = p_direction;
      min_length = p_min_length;
      }
   }
