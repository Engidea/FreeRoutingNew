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

import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;
import java.util.Collection;
import java.util.Locale;
import board.items.BrdAbitVia;
import board.items.BrdItem;
import board.items.BrdTracep;

/**
 * Describes information of a connected set of unconnected traces and vias.
 */
public final class BrdUnconnectedRoute implements Comparable<BrdUnconnectedRoute>,PrintableInfo
   {
   private final GuiResources resources;
   private final freert.rules.RuleNet net;
   private final int id_no;
   private final Integer trace_count;
   private final Integer via_count;

   public final Collection<BrdItem> item_list;

   public BrdUnconnectedRoute(GuiResources p_resources, freert.rules.RuleNet p_net, Collection<BrdItem> p_item_list, int p_id_no)
      {
      resources = p_resources;
      net = p_net;
      item_list = p_item_list;
      id_no = p_id_no;
      
      int curr_trace_count = 0;
      int curr_via_count = 0;
      for (BrdItem curr_item : p_item_list)
         {
         if (curr_item instanceof BrdTracep)
            {
            ++curr_trace_count;
            }
         else if (curr_item instanceof BrdAbitVia)
            {
            ++curr_via_count;
            }
         }
      trace_count = curr_trace_count;
      via_count = curr_via_count;
      }

   public String toString()
      {

      String result = resources.getString("net") + " " + net.name + ": " + resources.getString("trace_count") + " " + trace_count.toString() + ", " + resources.getString("via_count")
            + " " + via_count.toString();

      return result;
      }

   public int compareTo(BrdUnconnectedRoute p_other)
      {
      int result = net.name.compareTo(p_other.net.name);
      if (result == 0)
         {
         result = id_no - p_other.id_no;
         }
      return result;
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, Locale p_locale)
      {
      p_window.append_bold(resources.getString("unconnected_route"));
      p_window.append(" " + toString());
      p_window.newline();
      }

   }
