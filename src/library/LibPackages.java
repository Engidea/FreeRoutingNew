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
 * Packages.java
 *
 * Created on 3. Juni 2004, 09:29
 */

package library;

import java.util.Iterator;
import java.util.Vector;
import freert.planar.PlaShape;

/**
 * Describes a library of component packages.
 *
 * @author Alfons Wirtz
 */
public final class LibPackages implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private Vector<LibPackage> package_list = new Vector<LibPackage>();

   public final LibPadstacks padstack_list;

   /**
    * Creates a new instance of Packages. p_padstack_list is the list of padstacks used for the pins of the packages in this data
    * structure.
    */
   public LibPackages(LibPadstacks p_padstack_list)
      {
      this.padstack_list = p_padstack_list;
      }

   /**
    * Returns the package with the input name and the input side or null, if no such package exists.
    */
   public LibPackage get(String p_name, boolean p_is_front)
      {
      Iterator<LibPackage> it = package_list.iterator();
      LibPackage other_side_package = null;
      while (it.hasNext())
         {
         LibPackage curr_package = it.next();
         if (curr_package != null && curr_package.pkg_name.compareToIgnoreCase(p_name) == 0)
            {
            if (curr_package.is_front == p_is_front)
               {
               return curr_package;
               }
            other_side_package = curr_package;
            }
         }
      return other_side_package;
      }

   /**
    * Returns the package with index p_package_no. Packages numbers are from 1 to package count.
    */
   public LibPackage get(int p_package_no)
      {
      LibPackage result = package_list.elementAt(p_package_no - 1);
      if (result != null && result.pkg_no != p_package_no)
         {
         System.out.println("Padstacks.get: inconsistent padstack number");
         }
      return result;
      }

   /**
    * Returns the count of packages in this object.
    */
   public int count()
      {
      return package_list.size();
      }

   /**
    * Appends a new package with the input data to this object.
    */
   public LibPackage add(String p_name, LibPackagePin[] p_pin_arr, PlaShape[] p_outline, LibPackageKeepout[] p_keepout_arr, LibPackageKeepout[] p_via_keepout_arr, LibPackageKeepout[] p_place_keepout_arr,
         boolean p_is_front)
      {
      LibPackage new_package = new LibPackage(p_name, package_list.size() + 1, p_pin_arr, p_outline, p_keepout_arr, p_via_keepout_arr, p_place_keepout_arr, p_is_front, this);
      package_list.add(new_package);
      return new_package;
      }

   /**
    * Appends a new package with pins p_pin_arr to this object. The package name is generated internally.
    */
   public LibPackage add(LibPackagePin[] p_pin_arr)
      {
      String package_name = "Package#" + (new Integer(package_list.size() + 1)).toString();

      return add(package_name, p_pin_arr, null, new LibPackageKeepout[0], new LibPackageKeepout[0], new LibPackageKeepout[0], true);
      }
   }
