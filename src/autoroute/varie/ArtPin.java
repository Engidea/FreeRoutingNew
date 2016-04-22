package autoroute.varie;

import freert.planar.PlaPointFloat;
import board.items.BrdAbitPin;

/**
 * Autoroute needs a specific pin
 * @author damiano
 *
 */
public final class ArtPin implements Comparable<ArtPin>
   {
   public final BrdAbitPin board_pin;

   private final double distance_to_component_center;

   ArtPin(BrdAbitPin p_board_pin, PlaPointFloat gravity_center_of_smd_pins)
      {
      board_pin = p_board_pin;
      PlaPointFloat pin_location = p_board_pin.get_center().to_float();
      distance_to_component_center = pin_location.distance(gravity_center_of_smd_pins);
      }

   public int compareTo(ArtPin p_other)
      {
      double delta_dist = distance_to_component_center - p_other.distance_to_component_center;
      
      if (delta_dist > 0) 
         return 1;
      else if (delta_dist < 0) 
         return -1;
      else 
         return board_pin.pin_no - p_other.board_pin.pin_no;
      }
   }
