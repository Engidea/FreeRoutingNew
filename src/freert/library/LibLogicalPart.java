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
 * LogicalPart.java
 *
 * Created on 26. Maerz 2005, 06:14
 */

package freert.library;

import board.infos.PrintableInfo;

/**
 * Contains contain information for gate swap and pin swap for a single component.
 *
 * @author Alfons Wirtz
 */
public class LibLogicalPart implements PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String name;
   public final int part_no;
   
   private final LibLogicalPin[] part_pin_arr;

   
   /**
    * Creates a new instance of LogicalPart. The part pins are sorted by pin_no.
    * The pin_no's of the part pins must be the same number as in the
    * componnents library package.
    */
   public LibLogicalPart(String p_name, int p_no, LibLogicalPin[] p_part_pin_arr)
      {
      name = p_name;
      part_no = p_no;
      part_pin_arr = p_part_pin_arr;
      }

   public int pin_count()
      {
      return part_pin_arr.length;
      }

   /** 
    * @return the pim with index p_no. Pin numbers are from 0 to pin_count - 1 
    */
   public LibLogicalPin get_pin(int p_no)
      {
      if (p_no < 0 || p_no >= part_pin_arr.length)
         {
         System.out.println("LogicalPart.get_pin: p_no out of range");
         return null;
         }
    
      return part_pin_arr[p_no];
      }

   @Override
   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("logical_part_2") + " ");
      p_window.append_bold(name);
      for (int i = 0; i < part_pin_arr.length; ++i)
         {
         LibLogicalPin curr_pin = part_pin_arr[i];
         p_window.newline();
         p_window.indent();
         p_window.append(resources.getString("pin") + " ");
         p_window.append(curr_pin.pin_name);
         p_window.append(", " + resources.getString("gate") + " ");
         p_window.append(curr_pin.gate_name);
         p_window.append(", " + resources.getString("swap_code") + " ");
         Integer gate_swap_code = curr_pin.gate_swap_code;
         p_window.append(gate_swap_code.toString());
         p_window.append(", " + resources.getString("gate_pin") + " ");
         p_window.append(curr_pin.gate_pin_name);
         p_window.append(", " + resources.getString("swap_code") + " ");
         Integer pin_swap_code = curr_pin.gate_pin_swap_code;
         p_window.append(pin_swap_code.toString());
         }
      p_window.newline();
      p_window.newline();
      }
   }
