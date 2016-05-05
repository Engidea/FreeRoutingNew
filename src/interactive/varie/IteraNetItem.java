package interactive.varie;

import java.util.Collection;
import board.items.BrdItem;
import freert.planar.PlaPointInt;
import freert.varie.PlaDelTriStorable;

public final class IteraNetItem implements PlaDelTriStorable
   {
   public final BrdItem item;
   public Collection<BrdItem> connected_set;


   public IteraNetItem(BrdItem p_item, Collection<BrdItem> p_connected_set)
      {
      item = p_item;
      connected_set = p_connected_set;
      }

   @Override
   public PlaPointInt[] get_triangulation_corners()
      {
      return item.get_ratsnest_corners();
      }

   }
