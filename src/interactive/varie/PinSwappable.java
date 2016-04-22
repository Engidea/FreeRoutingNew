package interactive.varie;

import java.util.Collection;
import freert.planar.PlaPointFloat;
import freert.planar.PlaSegmentFloat;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdAbitPin;
import board.items.BrdItem;

public class PinSwappable implements Comparable<PinSwappable>
   {
   private final RoutingBoard board;

   public final BrdAbitPin pin;
   public  PlaSegmentFloat incomplete;
   
   public PinSwappable(RoutingBoard p_board, BrdAbitPin p_pin)
      {
      board = p_board;
      pin = p_pin;
      incomplete = null;
      if (p_pin.is_connected() || p_pin.net_count() != 1)
         {
         return;
         }
      // calculate the incomplete of p_pin
      PlaPointFloat pin_center = p_pin.get_center().to_float();
      double min_dist = Double.MAX_VALUE;
      PlaPointFloat nearest_point = null;
      Collection<BrdItem> net_items = board.get_connectable_items(p_pin.get_net_no(0));
      for (BrdItem curr_item : net_items)
         {
         if (curr_item == this.pin || !(curr_item instanceof BrdAbit))
            {
            continue;
            }
         PlaPointFloat curr_point = ((BrdAbit) curr_item).get_center().to_float();
         double curr_dist = pin_center.distance_square(curr_point);
         if (curr_dist < min_dist)
            {
            min_dist = curr_dist;
            nearest_point = curr_point;
            }
         }

      if (nearest_point != null)
         incomplete = new freert.planar.PlaSegmentFloat(pin_center, nearest_point);
      }

   public int compareTo(PinSwappable p_other)
      {
      return this.pin.compareTo(p_other.pin);
      }

   }