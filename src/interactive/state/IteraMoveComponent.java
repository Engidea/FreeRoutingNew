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
package interactive.state;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import board.RoutingBoard;
import board.infos.BrdComponent;
import board.items.BrdAbit;
import board.items.BrdArea;
import board.items.BrdComponentOutline;
import board.items.BrdItem;
import board.varie.SortedItemDouble;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.varie.TimeLimit;

/**
 * Class for moving a group of items on the board
 * Used by StateDrag only
 * 
 * @author Alfons Wirtz
 */
public final class IteraMoveComponent
   {
   private static final int s_TIGHT_TIME_ms = 2;

   private final PlaVectorInt translate_vector;
   private final int max_recursion_depth;
   private final int max_via_recursion_depth;
   private final RoutingBoard r_board;
   private final TreeSet<SortedItemDouble> item_group_arr = new TreeSet<SortedItemDouble>();

   private BrdComponent component = null;
   private boolean all_items_movable = true;

   public IteraMoveComponent(BrdItem p_item, PlaVectorInt p_translate_vector, int p_max_recursion_depth, int p_max_via_recursion_depth)
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
            item_centers.add(((BrdAbit) curr_item).center_get().to_float());
            }
         }
      
      
      // calculate the gravity point of all item centers
      double gravity_x = 0;
      double gravity_y = 0;
      for (PlaPointFloat curr_center : item_centers)
         {
         gravity_x += curr_center.v_x;
         gravity_y += curr_center.v_y;
         }
      
      gravity_x /= item_centers.size();
      gravity_y /= item_centers.size();
      PlaPointInt gravity_point = new PlaPointInt(Math.round(gravity_x), Math.round(gravity_y));
      
      for (BrdItem curr_item : item_group_list )
         {
         PlaPointInt item_center;

         if (curr_item instanceof BrdAbit)
            {
            item_center = ((BrdAbit) curr_item).center_get();
            }
         else
            {
            item_center = curr_item.bounding_box().centre_of_gravity().round();
            }
      
         PlaVectorInt compare_vector = gravity_point.difference_by(item_center);
         
         double curr_projection = compare_vector.scalar_product(translate_vector);

         // sort the items, in the direction of p_translate_vector, so that the items in front come first.
         item_group_arr.add( new SortedItemDouble(curr_item, curr_projection) );
         }
      }

   /**
    * Checks, if all items in the group can be moved by shoving obstacle trace aside without creating clearance violations.
    */
   public boolean check_move()
      {
      if ( ! all_items_movable) return false;
      
      TimeLimit time_limit = new TimeLimit(3);
      
      Collection<BrdItem> ignore_items = new LinkedList<BrdItem>();
      
      for (SortedItemDouble an_item : item_group_arr )
         {
         boolean move_ok;

         if (an_item.item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) an_item.item;
            
            if (translate_vector.distance() >= curr_drill_item.min_width())
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
            move_ok = r_board.check_item_move(an_item.item, translate_vector, ignore_items);
            }
         
         if (!move_ok) return false;
         }

      return true;
      }

   /**
    * Moves all items in the group by translate_vector and shoves aside obstacle traces.
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
      
      for (SortedItemDouble an_item : item_group_arr )
         {
         if (an_item.item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) an_item.item;

            boolean move_ok = r_board.move_drill_item(curr_drill_item, translate_vector, max_recursion_depth, max_via_recursion_depth, p_tidy_width, p_pull_tight_accuracy, s_TIGHT_TIME_ms);

            if ( move_ok) continue;

            if (component != null)
               {
               // Otherwise the component outline is not restored correctly by the undo algorithm.
               component.translate_by(translate_vector.negate());
               }

            return false;
            }
         else
            {
            an_item.item.move_by(translate_vector);
            }
         }
      
      return true;
      }

   }
