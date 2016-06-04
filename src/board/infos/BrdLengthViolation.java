package board.infos;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

import freert.rules.NetClass;
import freert.rules.RuleNet;
import gui.BoardFrame;
import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;
import java.util.Locale;

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
      freert.planar.PlaCoordTransform coordinate_transform = board_frame.board_panel.itera_board.coordinate_transform;
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
