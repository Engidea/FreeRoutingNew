/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
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
 * ViaRule.java
 *
 * Created on 31. Maerz 2005, 06:44
 */

package freert.rules;

import gui.varie.ObjectInfoPanel;
import java.util.LinkedList;
import java.util.List;
import board.infos.BrdViaInfo;
import board.infos.PrintableInfo;

/**
 * Contains an array of vias used for routing. 
 * Vias at the beginning of the array are preferred to later vias.
 * This is quite very much similar to BrdViaInfoList, wonder why two things so similar
 * @author Alfons Wirtz
 */
public final class RuleViaInfoList implements java.io.Serializable, PrintableInfo
   {
   private static final long serialVersionUID = 1L;

   // Empty via rule. Must not be changed
   public static final RuleViaInfoList EMPTY = new RuleViaInfoList("empty");

   public final String rule_name;

   private final List<BrdViaInfo> vinfo_list = new LinkedList<BrdViaInfo>();
   
   public RuleViaInfoList(String p_name)
      {
      rule_name = p_name;
      }

   public void append_via(BrdViaInfo p_via)
      {
      vinfo_list.add(p_via);
      }

   /**
    * Removes p_via from the rule. Returns false, if p_via was not contained in the rule.
    */
   public boolean remove_via(BrdViaInfo p_via)
      {
      return vinfo_list.remove(p_via);
      }

   public int via_count()
      {
      return vinfo_list.size();
      }

   public BrdViaInfo get_via(int p_index)
      {
      assert p_index >= 0 && p_index < vinfo_list.size();
      
      return vinfo_list.get(p_index);
      }

   public String toString()
      {
      return rule_name;
      }

   /**
    * Returns true, if p_via_info is contained in the via list of this rule.
    */
   public boolean contains(BrdViaInfo p_via_info)
      {
      for (BrdViaInfo curr_info : this.vinfo_list)
         {
         if (p_via_info == curr_info)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * Returns true, if this rule contains a via with padstack p_padstack
    */
   public boolean contains_padstack(library.LibPadstack p_padstack)
      {
      for (BrdViaInfo curr_info : this.vinfo_list)
         {
         if (curr_info.get_padstack() == p_padstack)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * Searchs a via in this rule with first layer = p_from_layer and last layer = p_to_layer. Returns null, if no such via exists.
    */
   public BrdViaInfo get_layer_range(int p_from_layer, int p_to_layer)
      {
      for (BrdViaInfo curr_info : this.vinfo_list)
         {
         if (curr_info.get_padstack().from_layer() == p_from_layer && curr_info.get_padstack().to_layer() == p_to_layer)
            {
            return curr_info;
            }
         }
      return null;
      }

   /**
    * Swaps the locations of p_1 and p_2 in the rule. Returns false, if p_1 or p_2 were not found in the list.
    */
   public boolean swap(BrdViaInfo p_1, BrdViaInfo p_2)
      {
      int index_1 = vinfo_list.indexOf(p_1);
      int index_2 = vinfo_list.indexOf(p_2);

      if (index_1 < 0 || index_2 < 0)
         {
         return false;
         }
      if (index_1 == index_2)
         {
         return true;
         }
      this.vinfo_list.set(index_1, p_2);
      this.vinfo_list.set(index_2, p_1);
      return true;
      }

   public void print_info( ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("via_rule_2") + " ");
      p_window.append_bold(this.rule_name);
      p_window.append_bold(":");
      int counter = 0;
      boolean first_time = true;
      final int max_vias_per_row = 5;
      for (BrdViaInfo curr_via : this.vinfo_list)
         {
         if (first_time)
            {
            first_time = false;
            }
         else
            {
            p_window.append(", ");
            }
         if (counter == 0)
            {
            p_window.newline();
            p_window.indent();
            }
         p_window.append(curr_via.get_name(), resources.getString("via_info"), curr_via);
         counter = (counter + 1) % max_vias_per_row;
         }
      }
   }
