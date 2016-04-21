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
 * ItemSelectionFilter.java
 *
 * Created on 14. Dezember 2004, 10:57
 */

package board.varie;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import board.items.BrdItem;

/**
 * Filter for selecting items on the board.
 *
 * @author Alfons Wirtz
 */
public final class ItemSelectionFilter implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // the filter array of the item types
   private final boolean[] sel_array = new boolean[ItemSelectionChoice.values().length];

   /**
    * Creates a new filter with all item types selected.
    */
   public ItemSelectionFilter()
      {
      Arrays.fill(sel_array, true);
      
      sel_array[ItemSelectionChoice.KEEPOUT.ordinal()] = false;
      sel_array[ItemSelectionChoice.VIA_KEEPOUT.ordinal()] = false;
      sel_array[ItemSelectionChoice.COMPONENT_KEEPOUT.ordinal()] = false;
      sel_array[ItemSelectionChoice.CONDUCTION.ordinal()] = false;
      sel_array[ItemSelectionChoice.BOARD_OUTLINE.ordinal()] = false;
      }


   /**
    * Looks, if the input item type is selected.
    */
   public boolean is_selected(ItemSelectionChoice p_choice)
      {
      return sel_array[p_choice.ordinal()];
      }

   public void set_filter ( ItemSelectionFilter from_filter )
      {
      if ( from_filter == null ) return;
      
      for (int index = 0; index < sel_array.length; index++)
         sel_array[index] = from_filter.sel_array[index];
      }

   /**
    * Creates a new filter with only p_item_type selected.
    */
   public ItemSelectionFilter(ItemSelectionChoice p_item_type)
      {
      java.util.Arrays.fill(sel_array, false);
      sel_array[p_item_type.ordinal()] = true;
      sel_array[ItemSelectionChoice.FIXED.ordinal()] = true;
      sel_array[ItemSelectionChoice.UNFIXED.ordinal()] = true;
      }

   /**
    * Creates a new filter with only p_item_types selected.
    */
   public ItemSelectionFilter(ItemSelectionChoice[] p_item_types)
      {
      java.util.Arrays.fill(sel_array, false);
      for (int i = 0; i < p_item_types.length; ++i)
         {
         sel_array[p_item_types[i].ordinal()] = true;
         }
      sel_array[ItemSelectionChoice.FIXED.ordinal()] = true;
      sel_array[ItemSelectionChoice.UNFIXED.ordinal()] = true;
      }

   /**
    * Selects or deselects an item type
    */
   public void set_selected(ItemSelectionChoice p_choice, boolean p_value)
      {
      sel_array[p_choice.ordinal()] = p_value;
      }

   /**
    * Selects all item types.
    */
   public void select_all()
      {
      Arrays.fill(sel_array, true);
      }

   /**
    * Deselects all item types.
    */
   public void deselect_all()
      {
      Arrays.fill(sel_array, false);
      }

   /**
    * Filters a collection of items with this filter.
    */
   public Set<BrdItem> filter(Set<BrdItem> p_items)
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      for (BrdItem curr_item : p_items)
         {
         if (curr_item.is_selected_by_filter(this))
            {
            result.add(curr_item);
            }
         }
      return result;
      }
   }
