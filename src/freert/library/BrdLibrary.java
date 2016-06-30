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
 * BoardLibrary.java
 *
 * Created on 4. Juni 2004, 06:37
 */

package freert.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import board.RoutingBoard;
import freert.varie.UndoObjectNode;
import freert.varie.UndoObjectStorable;

/**
 * Describes a board library of packages and padstacks.
 *
 * @author alfons
 */
public final class BrdLibrary implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   public LibPadstacks padstacks = null;
   public LibPackages packages = null;
   
   // Containes information for gate swap and pin swap in the Specctra-dsn format
   public final LibLogicalParts logical_parts = new LibLogicalParts();

   // The subset of padstacks in the board library, which can be used in routing for inserting vias
   private final List<LibPadstack> via_padstacks = new Vector<LibPadstack>();


   /** 
    * Sets the subset of padstacks from this.padstacks, which can be used in routing for inserting vias
    */
   public void set_via_padstacks(ArrayList<LibPadstack> p_padstacks)
      {
      via_padstacks.clear();
      
      via_padstacks.addAll(p_padstacks);
      }

   /** 
    * The count of padstacks from this.padstacks, which can be used in routing 
    */
   public int via_padstack_count()
      {
      return via_padstacks.size();
      }

   /** 
    * Gets the via padstack for routing with index p_no 
    */
   public LibPadstack get_via_padstack(int p_no)
      {
      if ( p_no < 0 || p_no >= via_padstacks.size()) return null;

      return via_padstacks.get(p_no);
      }

   /** 
    * Gets the via padstack with name p_name, or null, if no such padstack exists. 
    */
   public LibPadstack get_via_padstack(String p_name)
      {
      if ( p_name == null ) return null;
      
      for (LibPadstack curr_padstack : via_padstacks)
         {
         if (curr_padstack.pads_name.equals(p_name)) return curr_padstack;
         }

      return null;
      }

   /**
    * Returns the via padstacks, which can be used for routing.
    */
   public LibPadstack[] get_via_padstacks()
      {
      LibPadstack[] result = new LibPadstack[via_padstacks.size()];

      for (int index = 0; index < result.length; ++index)
         result[index] = via_padstacks.get(index);
      
      return result;
      }

   /**
    * Apppends p_padstack to the list of via padstacks. Returns false, if the list contains already a padstack with p_padstack.name.
    */
   public boolean add_via_padstack(LibPadstack p_padstack)
      {
      if ( p_padstack == null ) return false;
      
      if (get_via_padstack(p_padstack.pads_name) != null) return false;

      via_padstacks.add(p_padstack);
      
      return true;
      }

   /**
    * Removes p_padstack from the via padstack list. Returns false, if p_padstack was not found in the list
    */
   public boolean remove_via_padstack(LibPadstack p_padstack )
      {
      if ( p_padstack == null ) return true;
      
      return via_padstacks.remove(p_padstack);
      }

   /**
    * Gets the via padstack mirrored to the back side of the board. Returns null, if no such via padstack exists.
    */
   public LibPadstack get_mirrored_via_padstack(LibPadstack p_via_padstack)
      {
      int layer_count = padstacks.board_layer_structure.size();
      if (p_via_padstack.from_layer() == 0 && p_via_padstack.to_layer() == layer_count - 1)
         {
         return p_via_padstack;
         }
      int new_from_layer = layer_count - p_via_padstack.to_layer() - 1;
      int new_to_layer = layer_count - p_via_padstack.from_layer() - 1;
      for (LibPadstack curr_via_padstack : via_padstacks)
         {
         if (curr_via_padstack.from_layer() == new_from_layer && curr_via_padstack.to_layer() == new_to_layer)
            {
            return curr_via_padstack;
            }
         }
      return null;
      }

   /**
    * Looks, if the input padstack is used on p_board in a Package or in drill.
    */
   public boolean is_used(LibPadstack p_padstack, RoutingBoard p_board)
      {
      Iterator<UndoObjectNode> it = p_board.undo_items.start_read_object();

      for (;;)
         {
         UndoObjectStorable curr_item = p_board.undo_items.read_next(it);
      
         if (curr_item == null) break;

         if (curr_item instanceof board.items.BrdAbit)
            {
            if (((board.items.BrdAbit) curr_item).get_padstack() == p_padstack)
               {
               return true;
               }
            }
         }
      
      for (int index = 1; index <= packages.pkg_count(); ++index)
         {
         LibPackage curr_package = packages.pkg_get(index);
         
         for (int jndex = 0; jndex < curr_package.pin_count(); ++jndex)
            {
            if (curr_package.get_pin(jndex).padstack_no == p_padstack.pads_no)
               {
               return true;
               }
            }
         }
      return false;
      }
   }
