package board.infos;
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
import board.items.BrdItem;
import board.items.BrdTracep;
import freert.planar.PlaPointFloat;
import freert.rules.RuleNet;
import gui.varie.GuiResources;

/**
 * Describes a single incomplete connection of the rats nest
 */
public final class AirLineInfo implements Comparable<AirLineInfo>, PrintableInfo
   {
   public final RuleNet net;
   public final BrdItem from_item;
   public final PlaPointFloat from_corner;
   public final BrdItem to_item;
   public final PlaPointFloat to_corner;
   
   private final GuiResources resources;

   public AirLineInfo(RuleNet p_net, BrdItem p_from_item, PlaPointFloat p_from_corner, BrdItem p_to_item, PlaPointFloat p_to_corner, GuiResources p_resources)
      {
      net = p_net;
      from_item = p_from_item;
      from_corner = p_from_corner;
      to_item = p_to_item;
      to_corner = p_to_corner;
      resources = p_resources;
      }

   public int compareTo(AirLineInfo p_other)
      {
      return this.net.name.compareTo(p_other.net.name);
      }

   public String toString()
      {
      String result = this.net.name + ": " + item_info(from_item) + " - " + item_info(to_item);
      return result;
      }

   private String item_info(BrdItem p_item)
      {
      if (p_item instanceof BrdAbitPin)
         {
         BrdAbitPin curr_pin = (BrdAbitPin) p_item;
         return curr_pin.component_name() + ", " + curr_pin.get_name();
         }
      else if (p_item instanceof board.items.BrdAbitVia)
         {
         return resources.getString("via");
         }
      else if (p_item instanceof BrdTracep)
         {
         return resources.getString("trace");
         }
      else if (p_item instanceof board.items.BrdAreaConduction)
         {
         return resources.getString("conduction_area");
         }
      else
         {
         return resources.getString("unknown");
         }
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("interactive.resources.RatsNest", p_locale);
      p_window.append_bold(resources.getString("incomplete"));
      p_window.append(" " + resources.getString("net") + " ");
      p_window.append(net.name);
      p_window.append(" " + resources.getString("from") + " ", "Incomplete Start Item", from_item);
      p_window.append(from_corner);
      p_window.append(" " + resources.getString("to") + " ", "Incomplete End Item", to_item);
      p_window.append(to_corner);
      p_window.newline();
      }
   }
