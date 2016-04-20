package board.infos;

import gui.varie.GuiResources;
import planar.PlaPointFloat;
import rules.RuleNet;
import board.items.BrdItem;

/**
 * Describes a single incomplete connection of the rats nest
 */
public final class AirLineInfo implements Comparable<AirLineInfo>, PrintableInfo
   {
   public final RuleNet net;
   public final BrdItem from_item;
   public final PlaPointFloat from_corner;
   public final BrdItem to_item;
   public final PlaPointFloat to_corner;
   
   private final GuiResources resources;

   public AirLineInfo(RuleNet p_net, BrdItem p_from_item, PlaPointFloat p_from_corner, BrdItem p_to_item, PlaPointFloat p_to_corner, GuiResources p_resources)
      {
      net = p_net;
      from_item = p_from_item;
      from_corner = p_from_corner;
      to_item = p_to_item;
      to_corner = p_to_corner;
      resources = p_resources;
      }

   public int compareTo(AirLineInfo p_other)
      {
      return this.net.name.compareTo(p_other.net.name);
      }

   public String toString()
      {
      String result = this.net.name + ": " + item_info(from_item) + " - " + item_info(to_item);
      return result;
      }

   private String item_info(BrdItem p_item)
      {
      String result;
      if (p_item instanceof board.items.BrdAbitPin)
         {
         board.items.BrdAbitPin curr_pin = (board.items.BrdAbitPin) p_item;
         result = curr_pin.component_name() + ", " + curr_pin.name();
         }
      else if (p_item instanceof board.items.BrdAbitVia)
         {
         result = resources.getString("via");
         }
      else if (p_item instanceof board.items.BrdTrace)
         {
         result = resources.getString("trace");
         }
      else if (p_item instanceof board.items.BrdAreaConduction)
         {
         result = resources.getString("conduction_area");
         }
      else
         {
         result = resources.getString("unknown");
         }
      return result;
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("interactive.resources.RatsNest", p_locale);
      p_window.append_bold(resources.getString("incomplete"));
      p_window.append(" " + resources.getString("net") + " ");
      p_window.append(net.name);
      p_window.append(" " + resources.getString("from") + " ", "Incomplete Start Item", from_item);
      p_window.append(from_corner);
      p_window.append(" " + resources.getString("to") + " ", "Incomplete End Item", to_item);
      p_window.append(to_corner);
      p_window.newline();
      }
   }
