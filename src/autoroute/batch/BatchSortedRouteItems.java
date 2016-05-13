package autoroute.batch;

import freert.planar.PlaPointFloat;
import freert.varie.UndoableObjectNode;
import gui.varie.UndoableObjectStorable;
import java.util.Iterator;
import java.util.Set;
import board.RoutingBoard;
import board.items.BrdAbitVia;
import board.items.BrdItem;
import board.items.BrdTrace;

/**
 * Reads the vias and traces on the board in ascending x order. 
 * Because the vias and traces on the board change while optimizing
 * the item list of the board is read from scratch each time the next route item is returned.
 */
public class BatchSortedRouteItems
   {
   private final RoutingBoard r_board;

   private PlaPointFloat min_item_coor;
   private int min_item_layer;
   
   public BatchSortedRouteItems(RoutingBoard p_board)
      {
      r_board = p_board;

      clear();
      }

   public void clear ()
      {
      min_item_coor = new PlaPointFloat(Integer.MIN_VALUE, Integer.MIN_VALUE);
      min_item_layer = -1;
      }
   
   public PlaPointFloat get_current_position()
      {
      return min_item_coor;
      }
   
   public BrdItem next()
      {
      BrdItem result = null;
      PlaPointFloat curr_min_coor = new PlaPointFloat(Integer.MAX_VALUE, Integer.MAX_VALUE);
      int curr_min_layer = Integer.MAX_VALUE;
      Iterator<UndoableObjectNode> it = r_board.item_list.start_read_object();
      for (;;)
         {
         UndoableObjectStorable curr_item = r_board.item_list.read_object(it);
         if (curr_item == null)
            {
            break;
            }
         if (curr_item instanceof BrdAbitVia)
            {
            BrdAbitVia curr_via = (BrdAbitVia) curr_item;
            if (!curr_via.is_user_fixed())
               {
               PlaPointFloat curr_via_center = curr_via.center_get().to_float();
               int curr_via_min_layer = curr_via.first_layer();
               if (curr_via_center.v_x > min_item_coor.v_x || curr_via_center.v_x == min_item_coor.v_x
                     && (curr_via_center.v_y > min_item_coor.v_y || curr_via_center.v_y == min_item_coor.v_y && curr_via_min_layer > min_item_layer))
                  {
                  if (curr_via_center.v_x < curr_min_coor.v_x || curr_via_center.v_x == curr_min_coor.v_x
                        && (curr_via_center.v_y < curr_min_coor.v_y || curr_via_center.v_y == curr_min_coor.v_y && curr_via_min_layer < curr_min_layer))
                     {
                     curr_min_coor = curr_via_center;
                     curr_min_layer = curr_via_min_layer;
                     result = curr_via;
                     }
                  }
               }
            }
         }
      // Read traces last to prefer vias to traces at the same location
      it = r_board.item_list.start_read_object();
      for (;;)
         {
         UndoableObjectStorable curr_item = r_board.item_list.read_object(it);
         if (curr_item == null)
            {
            break;
            }
         if (curr_item instanceof BrdTrace)
            {
            BrdTrace curr_trace = (BrdTrace) curr_item;
            if (!curr_trace.is_shove_fixed())
               {
               PlaPointFloat first_corner = curr_trace.corner_first().to_float();
               PlaPointFloat last_corner = curr_trace.corner_last().to_float();
               PlaPointFloat compare_corner;
               if (first_corner.v_x < last_corner.v_x || first_corner.v_x == last_corner.v_x && first_corner.v_y < last_corner.v_y)
                  {
                  compare_corner = last_corner;
                  }
               else
                  {
                  compare_corner = first_corner;
                  }
               int curr_trace_layer = curr_trace.get_layer();
               if (compare_corner.v_x > min_item_coor.v_x || compare_corner.v_x == min_item_coor.v_x
                     && (compare_corner.v_y > min_item_coor.v_y || compare_corner.v_y == min_item_coor.v_y && curr_trace_layer > min_item_layer))
                  {
                  if (compare_corner.v_x < curr_min_coor.v_x || compare_corner.v_x == curr_min_coor.v_x
                        && (compare_corner.v_y < curr_min_coor.v_y || compare_corner.v_y == curr_min_coor.v_y && curr_trace_layer < curr_min_layer))
                     {
                     boolean is_connected_to_via = false;
                     Set<BrdItem> trace_contacts = curr_trace.get_normal_contacts();
                     for (BrdItem curr_contact : trace_contacts)
                        {
                        if (curr_contact instanceof BrdAbitVia && !curr_contact.is_user_fixed())
                           {
                           is_connected_to_via = true;
                           break;
                           }
                        }
                     if (!is_connected_to_via)
                        {
                        curr_min_coor = compare_corner;
                        curr_min_layer = curr_trace_layer;
                        result = curr_trace;
                        }
                     }
                  }
               }
            }
         }
      min_item_coor = curr_min_coor;
      min_item_layer = curr_min_layer;
      return result;

      }

   }
