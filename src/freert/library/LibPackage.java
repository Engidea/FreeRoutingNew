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
 * Package.java
 *
 * Created on 27. Mai 2004, 06:53
 */

package freert.library;

import specctra.varie.DsnPackageKeepout;
import board.infos.PrintableInfo;
import freert.planar.PlaShape;

/**
 * Component package templates describing the padstacks and relative locations of the packege pins, and optional other stuff like an
 * outline package keepouts.
 *
 * @author alfons
 */
public final class LibPackage implements Comparable<LibPackage>, PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // The name of the package
   public final String pkg_name;
   // Internally generated package number
   public final int pkg_no;
   // The array of pins of this padstacks
   private final LibPackagePin[] pin_arr;
   private final LibPackages parent_package_list;

   // The outline of the component, which may be null
   public final PlaShape[] outline;
   public final DsnPackageKeepout[] keepout_arr;
   public final DsnPackageKeepout[] via_keepout_arr;
   public final DsnPackageKeepout[] place_keepout_arr;
   // If false, the package is placed on the back side of the board
   public final boolean is_front;
   
   /**
    * Creates a new instance of Package. p_package_list is the list of packages containing this package.
    */
   public LibPackage(String p_name, 
         int p_no, 
         LibPackagePin[] p_pin_arr, 
         PlaShape[] p_outline, 
         DsnPackageKeepout[] p_keepout_arr, 
         DsnPackageKeepout[] p_via_keepout_arr, 
         DsnPackageKeepout[] p_place_keepout_arr, 
         boolean p_is_front,
         LibPackages p_package_list)
      {
      pkg_name = p_name;
      pkg_no = p_no;
      pin_arr = p_pin_arr;
      outline = p_outline;
      keepout_arr = p_keepout_arr;
      via_keepout_arr = p_via_keepout_arr;
      place_keepout_arr = p_place_keepout_arr;
      is_front = p_is_front;
      parent_package_list = p_package_list;
      }

   /**
    * Compare the given name with the package name in case indipendent way
    * @param p_name
    * @return true if they are equal
    */
   public boolean name_is_equal ( String p_name )
      {
      if ( p_name == null ) return false;
      
      return p_name.equalsIgnoreCase(pkg_name);
      }
   
   /**
    * Compares 2 packages by name. Useful for example to display packages in alphabetic order.
    */
   @Override
   public int compareTo(LibPackage p_other)
      {
      return pkg_name.compareToIgnoreCase(p_other.pkg_name);
      }

   /**
    * Returns the pin with the input number from this package.
    */
   public LibPackagePin get_pin(int p_no)
      {
      if (p_no < 0 || p_no >= pin_arr.length)
         {
         System.out.println("Package.get_pin: p_no out of range");
         return null;
         }
      return pin_arr[p_no];
      }

   /**
    * Returns the pin number of the pin with the input name from this package, or -1, if no such pin exists Pin numbers are from 0
    * to pin_count - 1.
    */
   public int get_pin_no(String p_name)
      {
      for (int index = 0; index < pin_arr.length; ++index)
         {
         if (pin_arr[index].name.equals(p_name)) return index;
         }
      return -1;
      }

   /**
    * Returns the pin count of this package.
    */
   public int pin_count()
      {
      return pin_arr.length;
      }

   public String toString()
      {
      return pkg_name;
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("package") + " ");
      p_window.append_bold(pkg_name);
      for (int iindex = 0; iindex < pin_arr.length; ++iindex)
         {
         LibPackagePin curr_pin = pin_arr[iindex];
         p_window.newline();
         p_window.indent();
         p_window.append(resources.getString("pin") + " ");
         p_window.append(curr_pin.name);
         p_window.append(", " + resources.getString("padstack") + " ");
         LibPadstack curr_padstack = this.parent_package_list.padstack_list.get(curr_pin.padstack_no);
         p_window.append(curr_padstack.pads_name, resources.getString("padstack_info"), curr_padstack);
         p_window.append(" " + resources.getString("at") + " ");
         p_window.append(curr_pin.relative_location.to_float());
         p_window.append(", " + resources.getString("rotation") + " ");
         p_window.append_without_transforming(curr_pin.rotation_in_degree);
         }
      p_window.newline();
      }
   }
