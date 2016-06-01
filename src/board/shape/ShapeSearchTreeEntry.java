package board.shape;


/**
 * created for sorting Items according to their clearance to p_cl_type on layer p_layer
 */
public final class ShapeSearchTreeEntry implements Comparable<ShapeSearchTreeEntry>
   {
   static private int last_generated_id_no = 1;

   private final int entry_id_no;

   public final ShapeTreeNodeLeaf leaf;
   public final int clearance;
   
   
   public ShapeSearchTreeEntry(ShapeTreeNodeLeaf p_leaf, int p_clearance)
      {
      leaf = p_leaf;
      clearance = p_clearance;
      entry_id_no = last_generated_id_no++;

      // I wonder what happens when eventually it wraps araound...
      if (last_generated_id_no >= Integer.MAX_VALUE) last_generated_id_no = 1;
      }

   public int compareTo(ShapeSearchTreeEntry p_other)
      {
      if (clearance != p_other.clearance) return clearance - p_other.clearance;

      return entry_id_no - p_other.entry_id_no;
      }
   }
