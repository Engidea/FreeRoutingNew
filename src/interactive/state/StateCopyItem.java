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
 * CopyItemState.java
 *
 * Created on 11. November 2003, 08:23
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPopupMenu;
import board.RoutingBoard;
import board.infos.BrdComponent;
import board.items.BrdAbit;
import board.items.BrdAbitVia;
import board.items.BrdArea;
import board.items.BrdItem;
import freert.library.LibPackage;
import freert.library.LibPackagePin;
import freert.library.LibPadstack;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeConvex;

/**
 * Interactive copying of items.
 *
 * @author Alfons Wirtz
 */
public class StateCopyItem extends StateInteractive
   {
   private Collection<BrdItem> item_list;
   private PlaPointInt start_position,current_position,previous_position;
   private int current_layer;
   private boolean layer_changed;
   
   
   /**
    * Returns a new instance of CopyItemState or null, if p_item_list is empty.
    */
   public static StateCopyItem get_instance(PlaPointFloat p_location, Collection<BrdItem> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      if (p_item_list.size() == 0)
         {
         return null;
         }
      p_board_handling.remove_ratsnest(); // copying an item may change the connectivity.
      return new StateCopyItem(p_location, p_item_list, p_parent_state, p_board_handling, p_logfile);
      }

   private StateCopyItem(PlaPointFloat p_location, Collection<BrdItem> p_item_list, StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      item_list = new LinkedList<BrdItem>();

      start_position = p_location.round();
      current_layer = p_board_handling.itera_settings.layer_no;
      layer_changed = false;
      current_position = start_position;
      previous_position = current_position;
      
      Iterator<BrdItem> it = p_item_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         if (curr_item instanceof BrdAbit || curr_item instanceof BrdArea)
            {
            BrdItem new_item = curr_item.copy(0);
            item_list.add(new_item);
            }
         }
      

      actlog_start_scope(LogfileScope.COPYING_ITEMS, p_location);
      }

   public StateInteractive mouse_moved()
      {
      super.mouse_moved();
      change_position(i_brd.get_current_mouse_position());
      return this;
      }

   /**
    * Changes the position for inserting the copied items to p_new_location.
    */
   private void change_position(PlaPointFloat p_new_position)
      {
      current_position = p_new_position.round();
      if (!current_position.equals(previous_position))
         {
         PlaVectorInt translate_vector = current_position.difference_by(previous_position);
         Iterator<board.items.BrdItem> it = item_list.iterator();
         while (it.hasNext())
            {
            board.items.BrdItem curr_item = it.next();
            curr_item.translate_by(translate_vector);
            }
         previous_position = current_position;
         i_brd.repaint();
         }
      }

   /**
    * Changes the first layer of the items in the copy list to p_new_layer.
    */
   @Override
   public boolean change_layer_action(int p_new_layer)
      {
      int r_layer = i_brd.set_layer(p_new_layer);

      // save what actually has been set by ths system
      actlog_start_scope(LogfileScope.CHANGE_LAYER, r_layer);

      layer_changed = r_layer == p_new_layer;
      
      return layer_changed;
      }

   /**
    * Inserts the items in the copy list into the board. 
    * Items, which would produce a clearance violation, are not inserted.
    */
   public void insert()
      {
      if (item_list == null) return;

      // Contains old and new padstacks after layer change.
      Map<LibPadstack, LibPadstack> padstack_pairs = new TreeMap<LibPadstack, LibPadstack>(); 

      if (layer_changed)
         {
         // create new via padstacks
         Iterator<BrdItem> it = item_list.iterator();
         while (it.hasNext())
            {
            BrdItem curr_ob = it.next();
            if (curr_ob instanceof BrdAbitVia)
               {
               BrdAbitVia curr_via = (BrdAbitVia) curr_ob;
               LibPadstack new_padstack = change_padstack_layers(curr_via.get_padstack(), current_layer, r_brd, padstack_pairs);
               curr_via.set_padstack(new_padstack);
               }
            }
         }
      // Copy the components of the old items and assign the new items to the copied components.

      /** Contailns the old and new id no of a copied component. */
      Map<Integer, Integer> cmp_no_pairs = new TreeMap<Integer, Integer>();

      /** Contains the new created components after copying. */
      Collection<BrdComponent> copied_components = new LinkedList<BrdComponent>();

      PlaVectorInt translate_vector = current_position.difference_by(start_position);
      Iterator<BrdItem> it = item_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         int curr_cmp_no = curr_item.get_component_no();
         if (curr_cmp_no > 0)
            {
            // This item belongs to a component
            int new_cmp_no;
            Integer curr_key = new Integer(curr_cmp_no);
            if (cmp_no_pairs.containsKey(curr_key))
               {
               // the new component for this pin is already created
               Integer curr_value = cmp_no_pairs.get(curr_key);
               new_cmp_no = curr_value.intValue();
               }
            else
               {
               BrdComponent old_component = r_brd.brd_components.get(curr_cmp_no);
               if (old_component == null)
                  {
                  System.out.println("CopyItemState: component not found");
                  continue;
                  }
               
               PlaPointInt new_location = old_component.get_location().translate_by(translate_vector);
               
               LibPackage new_package;
               if (layer_changed)
                  {
                  // create a new package with changed layers of the padstacks.
                  LibPackagePin[] new_pin_arr = new LibPackagePin[old_component.get_package().pin_count()];
                  for (int i = 0; i < new_pin_arr.length; ++i)
                     {
                     LibPackagePin old_pin = old_component.get_package().get_pin(i);
                     LibPadstack old_padstack = r_brd.brd_library.padstacks.get(old_pin.padstack_no);
                     if (old_padstack == null)
                        {
                        System.out.println("CopyItemState.insert: package padstack not found");
                        return;
                        }
                     LibPadstack new_padstack = change_padstack_layers(old_padstack, current_layer, r_brd, padstack_pairs);
                     new_pin_arr[i] = new LibPackagePin(old_pin.name, new_padstack.pads_no, old_pin.relative_location, old_pin.rotation_in_degree);
                     }
                  new_package = r_brd.brd_library.packages.add(new_pin_arr);
                  }
               else
                  {
                  new_package = old_component.get_package();
                  }
               BrdComponent new_component = r_brd.brd_components.add(new_location, old_component.get_rotation_in_degree(), old_component.is_on_front(), new_package);
               copied_components.add(new_component);
               new_cmp_no = new_component.id_no;
               cmp_no_pairs.put(new Integer(curr_cmp_no), new Integer(new_cmp_no));
               }
            curr_item.set_component_no(new_cmp_no);
            }
         }
      
      boolean all_items_inserted = true;
      boolean first_time = true;
      
      for ( BrdItem curr_item : item_list )
         {
         if (curr_item.r_board != null && curr_item.clearance_violation_count() == 0)
            {
            if (first_time)
               {
               // make the current situation restorable by undo
               r_brd.generate_snapshot();
               first_time = false;
               }
            r_brd.insert_item(curr_item.copy(0));
            }
         else
            {
            all_items_inserted = false;
            }
         }
      
      if (all_items_inserted)
         {
         i_brd.screen_messages.set_status_message(resources.getString("all_items_inserted"));
         }
      else
         {
         i_brd.screen_messages.set_status_message(resources.getString("some_items_not_inserted_because_of_obstacles"));
         }

      actlog_add_corner(this.current_position.to_float());
 
      start_position = current_position;
      layer_changed = false;
      i_brd.repaint();
      }

   @Override
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      insert();
      return this;
      }

   @Override
   public StateInteractive process_logfile_point(PlaPointFloat p_location)
      {
      change_position(p_location);
      insert();
      return this;
      }

   @Override
   public void draw(Graphics p_graphics)
      {
      if (item_list == null) return;

      for ( BrdItem curr_item : item_list )

         curr_item.draw(p_graphics, i_brd.gdi_context, i_brd.gdi_context.get_hilight_color(), i_brd.gdi_context.get_hilight_color_intensity());
      }

   @Override
   public JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_copy;
      }

   /**
    * Creates a new padstack from p_old_pastack with a layer range starting at p_new_layer.
    */
   private static LibPadstack change_padstack_layers(LibPadstack p_old_padstack, int p_new_layer, RoutingBoard p_board, Map<LibPadstack, LibPadstack> p_padstack_pairs)
      {
      LibPadstack new_padstack;
      int old_layer = p_old_padstack.from_layer();
      if (old_layer == p_new_layer)
         {
         new_padstack = p_old_padstack;
         }
      else if (p_padstack_pairs.containsKey(p_old_padstack))
         {
         // New padstack already created, assign it to the via.
         new_padstack = p_padstack_pairs.get(p_old_padstack);
         }
      else
         {
         // Create a new padstack.
         ShapeConvex[] new_shapes = new ShapeConvex[p_board.get_layer_count()];
         int layer_diff = old_layer - p_new_layer;
         for (int i = 0; i < new_shapes.length; ++i)
            {
            int new_layer_no = i + layer_diff;
            if (new_layer_no >= 0 && new_layer_no < new_shapes.length)
               {
               new_shapes[i] = p_old_padstack.get_shape(i + layer_diff);
               }
            }
         new_padstack = p_board.brd_library.padstacks.add(new_shapes);
         p_padstack_pairs.put(p_old_padstack, new_padstack);
         }
      return new_padstack;
      }
   }
