package board.kdtree;


/**
 * created for sorting Items according to their clearance to p_cl_type on layer p_layer
 */
public final class KdtreeNodeLeafSorted implements Comparable<KdtreeNodeLeafSorted>
   {
   static private int last_generated_id_no = 1;

   private final int entry_id_no;

   public final KdtreeNodeLeaf leaf;
   public final int clearance;
   
   
   public KdtreeNodeLeafSorted(KdtreeNodeLeaf p_leaf, int p_clearance)
      {
      leaf = p_leaf;
      clearance = p_clearance;
      entry_id_no = last_generated_id_no++;

      // I wonder what happens when eventually it wraps araound...
      if (last_generated_id_no >= Integer.MAX_VALUE) last_generated_id_no = 1;
      }

   public int compareTo(KdtreeNodeLeafSorted p_other)
      {
      if (clearance != p_other.clearance) return clearance - p_other.clearance;

      return entry_id_no - p_other.entry_id_no;
      }
   }
