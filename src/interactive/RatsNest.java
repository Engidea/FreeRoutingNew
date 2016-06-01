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
 * RatsNest.java
 *
 * Created on 18. Maerz 2004, 07:30
 */

package interactive;

import freert.graphics.GdiContext;
import freert.main.Stat;
import freert.varie.UndoableObjectNode;
import gui.varie.GuiResources;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import board.BrdConnectable;
import board.RoutingBoard;
import board.infos.AirLineInfo;
import board.items.BrdItem;

/**
 * Creates all Incompletes (Ratsnest) to display them on the screen
 *
 * @author Alfons Wirtz
 */
public final class RatsNest
   {
   private final NetIncompletes[] net_incompletes;
   private final boolean[] is_filtered;

   private final GuiResources resources;

   public boolean hidden = false;

   public RatsNest(RoutingBoard p_board, Stat p_stat )
      {
      resources = new GuiResources(p_stat,"interactive.resources.RatsNest");
      
      int max_net_no = p_board.brd_rules.nets.max_net_no();
      
      // Create the net item lists at once for performance reasons.
      Vector<Collection<BrdItem>> net_item_lists = new Vector<Collection<BrdItem>>(max_net_no);
      
      for (int index = 0; index < max_net_no; ++index)
         {
         net_item_lists.add(new LinkedList<BrdItem>());
         }
      
      Iterator<UndoableObjectNode> it = p_board.undo_items.start_read_object();
      for (;;)
         {
         BrdItem curr_item = (BrdItem) p_board.undo_items.read_object(it);
         
         if (curr_item == null) break;

         if ( ! (curr_item instanceof BrdConnectable)) continue;
         
         for (int i = 0; i < curr_item.net_count(); ++i)
            {
            net_item_lists.get(curr_item.get_net_no(i) - 1).add(curr_item);
            }
         }
      
      net_incompletes = new NetIncompletes[max_net_no];
      
      is_filtered = new boolean[max_net_no];
      
      for (int i = 0; i < net_incompletes.length; ++i)
         {
         net_incompletes[i] = new NetIncompletes(i + 1, net_item_lists.get(i), p_board, resources);
         is_filtered[i] = false;
         }
      }

   /**
    * Recalculates the incomplete connections for the input net
    */
   public void recalculate(int p_net_no, RoutingBoard p_board)
      {
      if (p_net_no >= 1 && p_net_no <= net_incompletes.length)
         {
         Collection<BrdItem> item_list = p_board.get_connectable_items(p_net_no);
         net_incompletes[p_net_no - 1] = new NetIncompletes(p_net_no, item_list, p_board, resources);
         }
      }

   /**
    * Recalculates the incomplete connections for the input net with the input item list.
    */
   public void recalculate(int p_net_no, Collection<BrdItem> p_item_list, RoutingBoard p_board)
      {
      if (p_net_no >= 1 && p_net_no <= net_incompletes.length)
         {
         // copy p_item_list, because it will be changed inside the constructor of NetIncompletes
         Collection<BrdItem> item_list = new LinkedList<BrdItem>(p_item_list);
         net_incompletes[p_net_no - 1] = new NetIncompletes(p_net_no, item_list, p_board, resources);
         }
      }

   public int incomplete_count()
      {
      int result = 0;
      for (int i = 0; i < net_incompletes.length; ++i)
         {
         result += net_incompletes[i].count();
         }
      return result;
      }

   public int incomplete_count(int p_net_no)
      {
      if (p_net_no <= 0 || p_net_no > net_incompletes.length)
         {
         return 0;
         }
      return net_incompletes[p_net_no - 1].count();
      }

   public int length_violation_count()
      {
      int result = 0;
      for (int i = 0; i < net_incompletes.length; ++i)
         {
         if (net_incompletes[i].get_length_violation() != 0)
            {
            ++result;
            }
         }
      return result;
      }

   /**
    * Returns the length of the violation of the length restriction of the net with number p_net_no, > 0, if the cumulative trace
    * length is to big, < 0, if the trace length is to smalll, 0, if the thace length is ok or the net has no length restrictions
    */
   public double get_length_violation(int p_net_no)
      {
      if (p_net_no <= 0 || p_net_no > net_incompletes.length)
         {
         return 0;
         }
      return net_incompletes[p_net_no - 1].get_length_violation();
      }

   /**
    * Returns all airlines of the ratsnest.
    */
   public AirLineInfo[] get_airlines()
      {
      AirLineInfo[] result = new AirLineInfo[incomplete_count()];
      int curr_index = 0;
      for (int i = 0; i < net_incompletes.length; ++i)
         {
         Collection<AirLineInfo> curr_list = net_incompletes[i].get_incompletes();
         for (AirLineInfo curr_line : curr_list)
            {
            result[curr_index] = curr_line;
            ++curr_index;
            }
         }
      return result;
      }

   /**
    * Request for rats nest hide and return previous status
    * @return
    */
   public boolean hide()
      {
      boolean previous = hidden;
      hidden = true;
      return previous;
      }

   public void show()
      {
      hidden = false;
      }

   /**
    * Recalculate the length matching violations. Return false, if the length violations have not changed.
    */
   public boolean recalculate_length_violations()
      {
      boolean result = false;
      for (int i = 0; i < net_incompletes.length; ++i)
         {
         if (net_incompletes[i].calc_length_violation())
            {
            result = true;
            }
         }
      return result;
      }

   /**
    * Used for example to hide the incompletes during interactive routiing.
    */
   public boolean is_hidden()
      {
      return hidden;
      }

   /**
    * Sets the visibility filter for the incompletes of the input net.
    */
   public void set_filter(int p_net_no, boolean p_value)
      {
      if (p_net_no < 1 || p_net_no > is_filtered.length)
         {
         return;
         }
      is_filtered[p_net_no - 1] = p_value;
      }

   public void draw(Graphics p_graphics, GdiContext p_graphics_context)
      {
      boolean draw_length_violations_only = this.hidden;

      for (int i = 0; i < net_incompletes.length; ++i)
         {
         if (!is_filtered[i])
            {
            net_incompletes[i].draw(p_graphics, p_graphics_context, draw_length_violations_only);
            }
         }

      }
   }
