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
 * MoveItemGroup.java
 *
 * Created on 25. Oktober 2003, 09:03
 */
package board;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import board.infos.BrdComponent;
import board.items.BrdAbit;
import board.items.BrdArea;
import board.items.BrdComponentOutline;
import board.items.BrdItem;
import board.varie.SortedItemDouble;
import datastructures.TimeLimit;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVector;

/**
 * Class for moving a group of items on the board
 * 
 * @author Alfons Wirtz
 */
public final class MoveComponent
   {
   private static final int s_TIGHT_TIME_ms = 2;

   private final PlaVector translate_vector;
   private final int max_recursion_depth;
   private final int max_via_recursion_depth;
   private final RoutingBoard r_board;
   private boolean all_items_movable = true;
   private SortedItemDouble[] item_group_arr;
   private BrdComponent component = null;

   public MoveComponent(BrdItem p_item, PlaVector p_translate_vector, int p_max_recursion_depth, int p_max_via_recursion_depth)
      {
      r_board = p_item.r_board;
      translate_vector = p_translate_vector;
      max_recursion_depth = p_max_recursion_depth;
      max_via_recursion_depth = p_max_via_recursion_depth;

      Collection<BrdItem> item_group_list;
      int component_no = p_item.get_component_no();
      if (component_no > 0)
         {
         item_group_list = r_board.get_component_items(component_no);
         component = r_board.brd_components.get(component_no);
         }
      else
         {
         item_group_list = new LinkedList<BrdItem>();
         item_group_list.add(p_item);
         }
      
      Collection<PlaPointFloat> item_centers = new LinkedList<PlaPointFloat>();
      
      for (BrdItem curr_item : item_group_list)
         {
         boolean curr_item_movable = !curr_item.is_user_fixed() && ((curr_item instanceof BrdAbit) || (curr_item instanceof BrdArea) || (curr_item instanceof BrdComponentOutline));
         if (!curr_item_movable)
            {
            // MoveItemGroup currently only implemented for DrillItems
            all_items_movable = false;
            return;
            }
         if (curr_item instanceof BrdAbit)
            {
            item_centers.add(((BrdAbit) curr_item).get_center().to_float());
            }
         }
      
      
      // calculate the gravity point of all item centers
      double gravity_x = 0;
      double gravity_y = 0;
      for (PlaPointFloat curr_center : item_centers)
         {
         gravity_x += curr_center.point_x;
         gravity_y += curr_center.point_y;
         }
      
      gravity_x /= item_centers.size();
      gravity_y /= item_centers.size();
      PlaPoint gravity_point = new PlaPointInt(Math.round(gravity_x), Math.round(gravity_y));
      item_group_arr = new SortedItemDouble[item_group_list.size()];
      Iterator<BrdItem> it = item_group_list.iterator();
      
      for (int i = 0; i < item_group_arr.length; ++i)
         {
         BrdItem curr_item = it.next();
         PlaPoint item_center;
         if (curr_item instanceof BrdAbit)
            {
            item_center = ((BrdAbit) curr_item).get_center();
            }
         else
            {
            item_center = curr_item.bounding_box().centre_of_gravity().round();
            }
      
         PlaVector compare_vector = gravity_point.difference_by(item_center);
         double curr_projection = compare_vector.scalar_product(translate_vector);
         item_group_arr[i] = new SortedItemDouble(curr_item, curr_projection);
         }

      // sort the items, in the direction of p_translate_vector, so that the items in front come first.
      Arrays.sort(item_group_arr);
      }

   /**
    * Checks, if all items in the group can be moved by shoving obstacle trace aside without creating clearance violations.
    */
   public boolean check_move()
      {
      if ( ! all_items_movable) return false;
      
      TimeLimit time_limit = new TimeLimit(3);
      
      Collection<BrdItem> ignore_items = new LinkedList<BrdItem>();

      for (int index = 0; index < item_group_arr.length; ++index)
         {
         boolean move_ok;
         if (item_group_arr[index].item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) item_group_arr[index].item;
            if (translate_vector.length_approx() >= curr_drill_item.min_width())
               {
               // a clearance violation with a connecting trace may occur
               move_ok = false;
               }
            else
               {
               move_ok = r_board.move_drill_algo.check(curr_drill_item, translate_vector, max_recursion_depth, max_via_recursion_depth, ignore_items, time_limit);
               }
            }
         else
            {
            move_ok = r_board.check_item_move(item_group_arr[index].item, this.translate_vector, ignore_items);
            }
         
         if (!move_ok) return false;
         }

      return true;
      }

   /**
    * Moves all items in the group by this.translate_vector and shoves aside obstacle traces.
    * @return false, if that was not possible without creating clearance violations. In this case an undo may be necessary.
    */
   public boolean insert(int p_tidy_width, int p_pull_tight_accuracy)
      {
      if ( ! all_items_movable) return false;
      
      if ( component != null)
         {
         // component must be moved first, so that the new pin shapes are calculated correctly
         r_board.brd_components.move(component.id_no, translate_vector);
         // let the observers synchronize the moving
         r_board.observers.notify_moved(component);
         }
      
      for (int index = 0; index < item_group_arr.length; ++index)
         {
         if (item_group_arr[index].item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) item_group_arr[index].item;
            boolean move_ok = r_board.move_drill_item(curr_drill_item, translate_vector, max_recursion_depth, max_via_recursion_depth, p_tidy_width, p_pull_tight_accuracy, s_TIGHT_TIME_ms);
            if (!move_ok)
               {
               if (this.component != null)
                  {
                  this.component.translate_by(translate_vector.negate());
                  // Otherwise the component outline is not restored correctly by the undo algorithm.
                  }
               return false;
               }
            }
         else
            {
            item_group_arr[index].item.move_by(translate_vector);
            }
         }
      return true;
      }

   }
