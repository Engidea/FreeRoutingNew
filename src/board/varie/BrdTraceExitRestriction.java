package board.varie;

import freert.planar.PlaDirection;


/**
 * Describes an exit restriction from a trace from a pin pad.
 */
public class BrdTraceExitRestriction
   {
   public final PlaDirection direction;
   public final double min_length;

   public BrdTraceExitRestriction(PlaDirection p_direction, double p_min_length)
      {
      direction = p_direction;
      min_length = p_min_length;
      }
   }
