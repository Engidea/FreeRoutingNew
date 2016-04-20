package board.shape;

import datastructures.Signum;

/**
 * created for sorting Items according to their clearance to p_cl_type on layer p_layer
 */
public class ShapeSearchTreeEntry implements Comparable<ShapeSearchTreeEntry>
   {
   /** used in objects of class EntrySortedByClearance */
   static private int last_generated_id_no = 0;

   private final int entry_id_no;

   public ShapeTreeLeaf leaf;
   public int clearance;
   
   
   public ShapeSearchTreeEntry(ShapeTreeLeaf p_leaf, int p_clearance)
      {
      leaf = p_leaf;
      clearance = p_clearance;
      if (last_generated_id_no >= Integer.MAX_VALUE)
         {
         last_generated_id_no = 0;
         }
      else
         {
         ++last_generated_id_no;
         }
      entry_id_no = last_generated_id_no;

      }

   public int compareTo(ShapeSearchTreeEntry p_other)
      {
      if (clearance != p_other.clearance)
         {
         return Signum.as_int(clearance - p_other.clearance);
         }
      return entry_id_no - p_other.entry_id_no;
      }
   }
