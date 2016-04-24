package interactive.varie;

import java.util.Collection;
import board.items.BrdItem;
import freert.planar.PlaPoint;
import freert.varie.PlaDelTriStorable;

public class IteraNetItem implements PlaDelTriStorable
   {
   public IteraNetItem(BrdItem p_item, Collection<BrdItem> p_connected_set)
      {
      item = p_item;
      connected_set = p_connected_set;
      }

   public PlaPoint[] get_triangulation_corners()
      {
      return this.item.get_ratsnest_corners();
      }

   public final BrdItem item;
   public Collection<BrdItem> connected_set;

   }
