package interactive.varie;

import planar.PlaPointFloat;
import board.items.BrdItem;

public class IteraTargetPoint
   {
   public final PlaPointFloat location;
   public final BrdItem item;

   public IteraTargetPoint(PlaPointFloat p_location, BrdItem p_item)
      {
      location = p_location;
      item = p_item;
      }
   }
