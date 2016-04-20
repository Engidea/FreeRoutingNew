package gui.varie;

import java.util.Collection;
import board.items.BrdItem;

public class IteraNetItems
   {
   public IteraNetItems(int p_net_no, Collection<BrdItem> p_items)
      {
      net_no = p_net_no;
      items = p_items;
      }

   public final int net_no;
   public final Collection<BrdItem> items;
   }
