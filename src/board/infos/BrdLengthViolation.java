package board.infos;

import gui.BoardFrame;
import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;
import java.util.Locale;
import rules.NetClass;
import rules.RuleNet;

public final class BrdLengthViolation implements Comparable<BrdLengthViolation>, PrintableInfo
   {
   private GuiResources resources;
   private final BoardFrame board_frame;
   
   public final RuleNet net;
   public final double violation_length;

   public BrdLengthViolation(GuiResources resources, BoardFrame board_frame, RuleNet p_net, double p_violation_length)
      {
      this.resources = resources;
      this.board_frame = board_frame;
      
      net = p_net;
      violation_length = p_violation_length;
      }

   public int compareTo(BrdLengthViolation p_other)
      {
      return net.name.compareToIgnoreCase(p_other.net.name);
      }

   public String toString()
      {
      planar.PlaCoordTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
      NetClass net_class = this.net.get_class();
      Float allowed_length;
      String allowed_string;
      if (violation_length > 0)
         {
         allowed_length = (float) coordinate_transform.board_to_user(net_class.get_maximum_trace_length());
         allowed_string = " " + resources.getString("maximum_allowed") + " ";
         }
      else
         {
         allowed_length = (float) coordinate_transform.board_to_user(net_class.get_minimum_trace_length());
         allowed_string = " " + resources.getString("minimum_allowed") + " ";
         }
      Float length = (float) coordinate_transform.board_to_user(this.net.get_trace_length());
      String result = resources.getString("net") + " " + this.net.name + resources.getString("trace_length") + " " + length.toString() + allowed_string + allowed_length;
      return result;
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, Locale p_locale)
      {
      p_window.append_bold(resources.getString("lenth_violation"));
      p_window.append(" " + toString());
      p_window.newline();
      }
   }
