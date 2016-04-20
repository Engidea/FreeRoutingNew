package board.infos;

import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;
import interactive.IteraBoard;
import main.Stat;
import planar.PlaPointFloat;
import board.items.BrdItem;
import board.items.BrdTrace;
import datastructures.Signum;

/**
 * Describes information of a route stub in the list.
 */
public final class BrdRouteStub implements Comparable<BrdRouteStub>, PrintableInfo
   {
   private final Stat stat;
   private final rules.RuleNet net;
   private final PlaPointFloat location;
   private final int layer_no;
   private final IteraBoard board_handling;
   private final GuiResources resources;

   public  final BrdItem stub_item;
   
   public BrdRouteStub(Stat p_stat, IteraBoard p_board_handling, BrdItem p_stub, PlaPointFloat p_location, int p_layer_no)
      {
      stat = p_stat;
      board_handling = p_board_handling;
      resources = new GuiResources(p_stat,"gui.resources.RouteStubInfo");
      stub_item = p_stub;
      location = board_handling.coordinate_transform.board_to_user(p_location);
      layer_no = p_layer_no;
      int net_no = p_stub.get_net_no(0);
      net = board_handling.get_routing_board().brd_rules.nets.get(net_no);
      }

   public String toString()
      {
      String item_string;
      if (stub_item instanceof BrdTrace)
         {
         item_string = resources.getString("trace");
         }
      else
         {
         item_string = resources.getString("via");
         }
      
      String layer_name = board_handling.get_routing_board().layer_structure.get_name(layer_no);
      String result = item_string + " " + resources.getString("stub_net") + " " + net.name + " " + resources.getString("at") + " " + location.to_string(stat.locale) + " "
            + resources.getString("on_layer") + " " + layer_name;
      return result;
      }

   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      p_window.append_bold(resources.getString("stub"));
      p_window.append(" " + toString());
      p_window.newline();
      }
   
   
   public int compareTo(BrdRouteStub p_other)
      {
      int result = net.name.compareTo(p_other.net.name);
      if (result == 0)
         {
         result = Signum.as_int(location.point_x - p_other.location.point_x);
         }
      if (result == 0)
         {
         result = Signum.as_int(location.point_y - p_other.location.point_y);
         }
      if (result == 0)
         {
         result = layer_no - p_other.layer_no;
         }
      return result;
      }
   }
