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

import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import board.infos.BrdComponent;
import board.items.BrdAbitPin;
import freert.planar.PlaPointFloat;

/**
 * Autorouter needs more info on a component
 * @author damiano
 *
 */
public final class ArtComponent implements Comparable<ArtComponent>
   {
   public final BrdComponent brd_component;
   public final int smd_pin_count;
   public final SortedSet<ArtPin> smd_pins = new TreeSet<ArtPin>();
   
   // The center of gravity of all SMD pins of this component
   private final PlaPointFloat gravity_center_of_smd_pins;

   public ArtComponent(BrdComponent p_board_component, Collection<BrdAbitPin> p_board_smd_pin_list)
      {
      brd_component = p_board_component;

      // Calculate the center of gravity of all SMD pins of this component.
      Collection<BrdAbitPin> curr_pin_list = new LinkedList<board.items.BrdAbitPin>();
      
      int cmp_no = p_board_component.id_no;
      
      for (BrdAbitPin curr_board_pin : p_board_smd_pin_list)
         {
         if (curr_board_pin.get_component_no() == cmp_no)
            {
            curr_pin_list.add(curr_board_pin);
            }
         }
      
      double x = 0;
      double y = 0;
      
      for ( BrdAbitPin curr_pin : curr_pin_list)
         {
         PlaPointFloat curr_point = curr_pin.center_get().to_float();
         x += curr_point.v_x;
         y += curr_point.v_y;
         }

      smd_pin_count = curr_pin_list.size();
      x /= smd_pin_count;
      y /= smd_pin_count;
      gravity_center_of_smd_pins = new PlaPointFloat(x, y);

      // calculate the sorted SMD pins of this component

      for ( BrdAbitPin curr_board_pin : curr_pin_list)
         {
         smd_pins.add(new ArtPin(curr_board_pin , gravity_center_of_smd_pins));
         }
      }

   /**
    * Sort the components, so that components with more pins come first
    */
   @Override
   public int compareTo(ArtComponent p_other)
      {
      int me_more = smd_pin_count - p_other.smd_pin_count;

      if (me_more > 0) 
         return 1;
      else if (me_more < 0) 
         return -1;
      else
         return brd_component.id_no - p_other.brd_component.id_no;
      }
   }
