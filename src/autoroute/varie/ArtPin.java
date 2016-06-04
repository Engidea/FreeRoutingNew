package autoroute.varie;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 */

import board.items.BrdAbitPin;
import freert.planar.PlaPointFloat;

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
      PlaPointFloat pin_location = p_board_pin.center_get().to_float();
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
