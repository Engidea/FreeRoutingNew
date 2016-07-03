package freert.varie;

import freert.planar.PlaPointInt;

/**
 * Utililty class to make code simpler to understand
 * @author damiano
 *
 */
public final class PlaPointIntDist implements Comparable<PlaPointIntDist>
   {
   public final PlaPointInt i_point;
   public final double point_dist;
   
   public PlaPointIntDist(PlaPointInt p_point, double p_dist)
      {
      i_point = p_point;
      point_dist = p_dist;
      }

   @Override
   public int compareTo(PlaPointIntDist o)
      {
      return Signum.as_int(point_dist - o.point_dist);
      }

   }
