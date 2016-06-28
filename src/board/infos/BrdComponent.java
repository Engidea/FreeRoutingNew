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
 * Component.java
 *
 * Created on 27. Mai 2004, 07:23
 */

package board.infos;

import freert.library.LibLogicalPart;
import freert.library.LibPackage;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.varie.UndoObjectStorable;
import gui.varie.ObjectInfoPanel;

/**
 * Describes board components consisting of an array of pins and other stuff like component keepouts.
 *
 * @author Alfons Wirtz
 */
public final class BrdComponent implements UndoObjectStorable, PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // The name of the component
   public final String name;
   // The location of the component
   private PlaPointInt location;
   // The rotation of the library package of the component in degree
   private int rotation_in_degree;
   // Contains information for gate swapping and pin swapping, if != null
   private LibLogicalPart logical_part = null;
   // If false, the component will be placed on the back side of the board
   private boolean on_front;
   // The library package of the component if it is placed on the component side
   private final LibPackage lib_package_front;
   // The library package of the component if it is placed on the solder side
   private final LibPackage lib_package_back;
   // Internal generated unique identification number
   public final int id_no;
   // If true, the component cannot be moved
   public final boolean position_fixed;

   
   /**
    * Creates a new instance of Component with the input parameters. 
    * If p_on_front is false, the component will be placed on the back side.
    */
   public BrdComponent(
         String p_name, 
         PlaPointInt p_location, 
         int p_rotation_in_degree, 
         boolean p_on_front, 
         LibPackage p_package_front, 
         LibPackage p_package_back, 
         int p_no, 
         boolean p_position_fixed)
      {
      id_no = p_no;
      name = p_name;
      location = p_location;
      on_front = p_on_front;
      lib_package_front = p_package_front;
      lib_package_back = p_package_back;
      position_fixed = p_position_fixed;

      rotation_in_degree = normalize_rotation(p_rotation_in_degree);
      }

   /**
    * Copy constructor
    * @param p_other
    */
   private BrdComponent(BrdComponent p_other)
      {
      id_no = p_other.id_no;
      name = p_other.name;
      location = p_other.location;
      on_front = p_other.on_front;
      lib_package_front = p_other.lib_package_front;
      lib_package_back = p_other.lib_package_back;
      position_fixed = p_other.position_fixed;
      logical_part = p_other.logical_part;
      rotation_in_degree = p_other.rotation_in_degree;
      }
   
   /**
    * this is not like the normal copy in the BrdItem, it is used for the ObectStorable
    */
   @Override
   public BrdComponent copy()
      {
      return new BrdComponent(this);
      }

   private int normalize_rotation (int p_rotation_degrees)
      {
      while ( p_rotation_degrees >= 360)
         p_rotation_degrees -= 360;

      while (p_rotation_degrees < 0)
         p_rotation_degrees += 360;
      
      return p_rotation_degrees;
      }
   
   /**
    * Returns the location of this component.
    */
   public final PlaPointInt get_location()
      {
      return location;
      }

   /**
    * Returns the rotation of this component in degree.
    */
   public int get_rotation_in_degree()
      {
      return (int)rotation_in_degree;
      }

   public boolean is_placed()
      {
      return location != null;
      }

   /**
    * If false, the component will be placed on the back side of the board.
    */
   public boolean is_on_front()
      {
      return on_front;
      }

   /**
    * Translates the location of this Component by p_p_vector. The Pins in the board must be moved seperately.
    */
   public void translate_by(PlaVectorInt p_vector)
      {
      if ( p_vector == null ) return;
      
      if (location != null)
         {
         location = location.translate_by(p_vector);
         }
      }

   /**
    * Turns this component by p_factor times 90 degree around p_pole.
    */
   public void rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      if (p_factor == 0) return;

      rotation_in_degree = normalize_rotation (rotation_in_degree + p_factor * 90);
      
      if (location != null)
         {
         location = location.rotate_90_deg(p_factor, p_pole);
         }
      }

   /**
    * Rotates this component by p_angle_in_degree around p_pole.
    */
   public final void rotate_deg(int p_angle_degree, PlaPointInt p_pole, boolean p_flip_style_rotate_first)
      {
      if (p_angle_degree == 0) return;

      if (p_flip_style_rotate_first && !is_on_front())
         {
         // take care of the order of mirroring and rotating on the back side of the board
         p_angle_degree = 360 - p_angle_degree;
         }

      rotation_in_degree = normalize_rotation(rotation_in_degree + p_angle_degree);
      
      if (location != null)
         {
         location = location.to_float().rotate_deg(p_angle_degree, p_pole.to_float()).round();
         }
      }

   /**
    * Changes the placement side of this component and mirrors it at the vertical line through p_pole.
    */
   public void change_side(PlaPointInt p_pole)
      {
      on_front = ! on_front;
      
      if ( location != null ) location = location.mirror_vertical(p_pole);
      }

   /**
    * Compares 2 components by name. Useful for example to display components in alphabetic order.
    */
   public int compareTo(Object p_other)
      {
      if ( p_other == null ) return 1;
      
      if (p_other instanceof BrdComponent)
         {
         return name.compareToIgnoreCase(((BrdComponent) p_other).name);
         }
      
      return 1;
      }


   public String toString()
      {
      return name;
      }

   /**
    * Returns information for pin swap and gate swap, if != null.
    */
   public LibLogicalPart get_logical_part()
      {
      return logical_part;
      }

   /**
    * Sets the infomation for pin swap and gate swap.
    */
   public void set_logical_part(LibLogicalPart p_logical_part)
      {
      logical_part = p_logical_part;
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("component") + " ");
      
      p_window.append_bold(name);
      
      if (location != null)
         {
         p_window.append(" " + resources.getString("at") + " ");
         p_window.append(location.to_float());

         p_window.append(", " + resources.getString("rotation") + " ");
         p_window.append_without_transforming(rotation_in_degree);

         if (on_front)
            {
            p_window.append(", " + resources.getString("front"));
            }
         else
            {
            p_window.append(", " + resources.getString("back"));
            }
         }
      else
         {
         p_window.append(" " + resources.getString("not_yet_placed"));
         }
      p_window.append(", " + resources.getString("package"));
      LibPackage lib_package = get_package();
      p_window.append(lib_package.pkg_name, resources.getString("package_info"), lib_package);
      if (logical_part != null)
         {
         p_window.append(", " + resources.getString("logical_part") + " ");
         p_window.append(logical_part.name, resources.getString("logical_part_info"), logical_part);
         }
      p_window.newline();
      }

   /**
    * @return the library package of this component.
    */
   public LibPackage get_package()
      {
      if (on_front)
         return lib_package_front;
      else
         return lib_package_back;
      }
   }
