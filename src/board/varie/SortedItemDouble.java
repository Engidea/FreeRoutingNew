package board.varie;

import board.items.BrdItem;
import freert.varie.Signum;

/**
 * Used to sort the group items in the direction of translate_vector, so that the front items can be moved first.
 * It is kind of generic, when you want to sort Items by some kind of value
 */
public final class SortedItemDouble implements Comparable<SortedItemDouble>
   {
   public final BrdItem item;
   public final double projection;

   public SortedItemDouble(BrdItem p_item, double p_projection)
      {
      item = p_item;
      projection = p_projection;
      }

   public int compareTo(SortedItemDouble p_other)
      {
      return Signum.as_int(projection - p_other.projection);
      }
   }
