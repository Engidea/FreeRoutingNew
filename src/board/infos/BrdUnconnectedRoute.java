package board.infos;

import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;
import java.util.Collection;
import java.util.Locale;
import board.items.BrdItem;

/**
 * Describes information of a connected set of unconnected traces and vias.
 */
public final class BrdUnconnectedRoute implements Comparable<BrdUnconnectedRoute>,PrintableInfo
   {
   private final GuiResources resources;
   private final rules.RuleNet net;
   private final int id_no;
   private final Integer trace_count;
   private final Integer via_count;

   public final Collection<BrdItem> item_list;

   public BrdUnconnectedRoute(GuiResources resources, rules.RuleNet p_net, Collection<BrdItem> p_item_list, int id_no)
      {
      this.resources = resources;
      this.net = p_net;
      this.item_list = p_item_list;
      this.id_no = id_no;
      int curr_trace_count = 0;
      int curr_via_count = 0;
      for (BrdItem curr_item : p_item_list)
         {
         if (curr_item instanceof board.items.BrdTrace)
            {
            ++curr_trace_count;
            }
         else if (curr_item instanceof board.items.BrdAbitVia)
            {
            ++curr_via_count;
            }
         }
      this.trace_count = curr_trace_count;
      this.via_count = curr_via_count;
      }

   public String toString()
      {

      String result = resources.getString("net") + " " + this.net.name + ": " + resources.getString("trace_count") + " " + this.trace_count.toString() + ", " + resources.getString("via_count")
            + " " + this.via_count.toString();

      return result;
      }

   public int compareTo(BrdUnconnectedRoute p_other)
      {
      int result = this.net.name.compareTo(p_other.net.name);
      if (result == 0)
         {
         result = this.id_no - p_other.id_no;
         }
      return result;
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, Locale p_locale)
      {
      p_window.append_bold(resources.getString("unconnected_route"));
      p_window.append(" " + toString());
      p_window.newline();
      }

   }
