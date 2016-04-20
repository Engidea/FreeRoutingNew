package board.varie;


/**
 * Auxiliary class used in the method move_by
 */
public class BrdTraceInfo implements Comparable<BrdTraceInfo>
   {
   public final int layer;
   public final int half_width;
   public final int clearance_type;

   public BrdTraceInfo(int p_layer, int p_half_width, int p_clearance_type)
      {
      layer = p_layer;
      half_width = p_half_width;
      clearance_type = p_clearance_type;
      }

   /**
    * Implements the comparable interface.
    */
   public int compareTo(BrdTraceInfo p_other)
      {
      return p_other.layer - this.layer;
      }

   }
