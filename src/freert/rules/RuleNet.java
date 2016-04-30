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
 * Net.java
 *
 * Created on 11. Juni 2004, 08:17
 */
package freert.rules;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import board.BrdConnectable;
import board.infos.PrintableInfo;
import board.items.BrdAbitPin;
import board.items.BrdItem;
import board.items.BrdTrace;
import freert.varie.UndoableObjectNode;
import freert.varie.UndoableObjects;

/**
 * Describes properties for an individual electrical net.
 *
 * @author Alfons Wirtz
 */
public final class RuleNet implements Comparable<RuleNet>, PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // The net list, where this net belongs to
   public final RuleNets parent;
   
   /// The name of the net
   public final String name;
   
   // Used only if a net is divided internally because of fromto rules for example For normal nets it is always 1.
   public final int subnet_number;
   
   /// The unique strict positive number of the net 
   public final int net_number;
   
   // Indicates, if this net contains a power plane
   private boolean contains_plane;
   
   // The routing rule of this net 
   private NetClass net_class;
   
   
   /**
    * Creates a new instance of Net. p_net_list is the net list, where this net belongs to.
    */
   public RuleNet(String p_name, int p_subnet_number, int p_no, RuleNets p_net_list, boolean p_contains_plane)
      {
      parent = p_net_list;
      name = p_name;
      subnet_number = p_subnet_number;
      net_number = p_no;
      contains_plane = p_contains_plane;
      net_class = p_net_list.get_board().brd_rules.get_default_net_class();
      }

   public String toString()
      {
      return name;
      }

   /**
    * Compares 2 nets by name. Useful for example to display nets in alphabetic order.
    */
   @Override
   public int compareTo(RuleNet p_other)
      {
      if ( p_other == null ) return 1;
      
      return name.compareToIgnoreCase(p_other.name);
      }

   /** 
    * Returns the class of this net. 
    */
   public final NetClass get_class()
      {
      return net_class;
      }

   /** 
    * Sets the class of this net 
    */
   public void set_class(NetClass p_rule)
      {
      net_class = p_rule;
      }

   /**
    * Returns the pins and conduction areas of this net
    */
   public LinkedList<BrdItem> get_terminal_items()
      {
      LinkedList<BrdItem> result = new LinkedList<BrdItem>();

      UndoableObjects item_list = parent.get_board().item_list;
      
      Iterator<UndoableObjectNode> it = item_list.start_read_object();
      
      for (;;)
         {
         BrdItem curr_item = (BrdItem) item_list.read_object(it);

         if (curr_item == null) break;

         if ( ! (curr_item instanceof BrdConnectable)) continue;
         
         if (curr_item.contains_net(net_number) && !curr_item.is_route())
            {
            result.add(curr_item);
            }
         }
      return result;
      }

   /**
    * Returns the pins of this net.
    */
   public LinkedList<BrdAbitPin> get_pins()
      {
      LinkedList<BrdAbitPin> result = new LinkedList<board.items.BrdAbitPin>();

      UndoableObjects item_list = parent.get_board().item_list;
      
      Iterator<UndoableObjectNode> it = item_list.start_read_object();

      for (;;)
         {
         BrdItem curr_item = (BrdItem) item_list.read_object(it);

         if (curr_item == null) break;

         if ( ! ( curr_item instanceof BrdAbitPin) ) continue;

         if ( ! curr_item.contains_net(net_number)) continue;

         result.add((BrdAbitPin) curr_item);
         }

      return result;
      }

   /**
    * Returns all items of this net.
    */
   public LinkedList<BrdItem> get_items()
      {
      LinkedList<BrdItem> result = new LinkedList<BrdItem>();
      
      UndoableObjects item_list = parent.get_board().item_list;
      
      Iterator<UndoableObjectNode> iter = item_list.start_read_object();
      
      for (;;)
         {
         BrdItem curr_item = (BrdItem) item_list.read_object(iter);
         
         if (curr_item == null) break;

         if ( ! curr_item.contains_net( net_number)) continue;
         
         result.add(curr_item);
         }
      return result;
      }

   /**
    * Returns the cumulative trace length of all traces on the board belonging to this net
    */
   public double get_trace_length()
      {
      double cumulative_trace_length = 0;
      
      Collection<BrdItem> net_items = parent.get_board().get_connectable_items(net_number);

      for (BrdItem curr_item : net_items)
         {
         if ( ! ( curr_item instanceof BrdTrace) ) continue;

         cumulative_trace_length += ((BrdTrace) curr_item).get_length();
         }
      
      return cumulative_trace_length;
      }


   public void set_contains_plane(boolean p_value)
      {
      contains_plane = p_value;
      }

   /**
    * Indicates, if this net contains a power plane. Used by the autorouter for setting the via costs to the cheap plane via costs.
    * May also be true, if a layer covered with a conduction_area of this net is is a signal layer.
    */
   public boolean contains_plane()
      {
      return contains_plane;
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      Integer via_count = parent.get_board().get_via_count(net_number);
      double cumulative_trace_length = get_trace_length();
      LinkedList<BrdItem> terminal_items = get_terminal_items();
      LinkedList<PrintableInfo> terminals = new LinkedList<PrintableInfo>();
      terminals.addAll(terminal_items);
      Integer terminal_item_count = terminals.size();

      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("net") + " ");
      p_window.append_bold(name);
      p_window.append_bold(": ");
      p_window.append(resources.getString("class") + " ");
      p_window.append(net_class.get_name(), resources.getString("net_class"), net_class);
      p_window.append(", ");
      p_window.append_objects(terminal_item_count.toString(), resources.getString("terminal_items_2"), terminals);
      p_window.append(" " + resources.getString("terminal_items"));
      p_window.append(", " + resources.getString("via_count") + " ");
      p_window.append(via_count.toString());
      p_window.append(", " + resources.getString("trace_length") + " ");
      p_window.append(cumulative_trace_length);
      p_window.newline();

      }

   }
