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
 * NetRules.java
 *
 * Created on 7. April 2005, 07:52
 */

package freert.rules;

import gui.varie.GuiResources;
import interactive.IteraBoard;
import java.util.Iterator;
import java.util.Vector;
import board.BrdLayerStructure;

/**
 * Contains the array of net classes for interactive routing.
 *
 * @author Alfons Wirtz
 */
public class NetClasses implements java.io.Serializable, Iterable<NetClass>
   {
   private static final long serialVersionUID = 1L;

   private final Vector<NetClass> class_list = new Vector<NetClass>();
   
   /**
    * Returns the number of classes in this array.
    */
   public int count()
      {
      return class_list.size();
      }

   public Iterator<NetClass> iterator ()
      {
      return class_list.iterator();
      }
   /**
    * Returns the net class with index p_index.
    */
   public NetClass get(int p_index)
      {
      assert p_index >= 0 && p_index <= class_list.size() - 1;
      
      return class_list.get(p_index);
      }

   /**
    * Returns the net class with name p_name, or null, if no such class exists.
    */
   public NetClass get(String p_name)
      {
      for (NetClass curr_class : class_list)
         {
         if (curr_class.get_name().equals(p_name)) return curr_class;
         }

      return null;
      }

   /**
    * Appends a new empty class with name p_name to the class array
    */
   NetClass append(String p_name, board.BrdLayerStructure p_layer_structure, ClearanceMatrix p_clearance_matrix)
      {
      NetClass new_class = new NetClass(p_name, p_layer_structure, p_clearance_matrix);
      class_list.add(new_class);
      return new_class;
      }

   /**
    * Appends a new empty class to the class array. A name for the class is created internally
    */
   NetClass append(BrdLayerStructure p_layer_structure, ClearanceMatrix p_clearance_matrix, IteraBoard itera_board)
      {
      GuiResources resources = itera_board.newGuiResources("rules.resources.Default");
      String name_front = resources.getString("class");
      String new_name = null;
      Integer index = 0;
      for (;;)
         {
         ++index;
         new_name = name_front + index.toString();
         if (get(new_name) == null)  break;
         }

      return append(new_name, p_layer_structure, p_clearance_matrix);
      }

   /**
    * Looks, if the list contains a net class with trace half widths all equal to p_trace_half_width, 
    * trace clearance class equal to p_trace_clearance_class and 
    * via rule equal to p_cia_rule. 
    * @return null, if no such net class was found.
    */
   public NetClass find(int p_trace_half_width, int p_trace_clearance_class, RuleViaInfoList p_via_rule)
      {
      for ( NetClass curr_class : class_list)
         {
         if (curr_class.get_trace_clearance_class() != p_trace_clearance_class ) continue;
         
         if ( curr_class.get_via_rule() != p_via_rule ) continue;
         
         if ( curr_class.has_trace_half_width(p_trace_half_width)) return curr_class;
         }

      return null;
      }

   /**
    * Looks, if the list contains a net class with trace half width[i] all equal to p_trace_half_width_arr[i] for 0 <= i <
    * layer_count, trace clearance class equal to p_trace_clearance_class and via rule equal to p_via_rule. Returns null, if no such
    * net class was found.
   public NetClass find(int[] p_trace_half_width_arr, int p_trace_clearance_class, RuleViaInfoList p_via_rule)
      {
      for (NetClass curr_class : class_list)
         {
         if (curr_class.get_trace_clearance_class() == p_trace_clearance_class && curr_class.get_via_rule() == p_via_rule && p_trace_half_width_arr.length == curr_class.layer_count())
            {
            boolean trace_widths_equal = true;
            for (int index = 0; index < curr_class.layer_count(); ++index)
               {
               if (curr_class.get_trace_half_width(index) != p_trace_half_width_arr[index])
                  {
                  trace_widths_equal = false;
                  break;
                  }
               }
            if (trace_widths_equal)
               {
               return curr_class;
               }
            }
         }
      return null;
      }

    */

   /**
    * Removes p_net_class from this list. Returns false, if p_net_class was not contained in the list.
    */
   public boolean remove(NetClass p_net_class)
      {
      return class_list.remove(p_net_class);
      }
   }
