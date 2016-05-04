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
 * NetIncompletes.java
 *
 * Created on 16. Maerz 2004, 06:47
 */

package interactive;

import freert.graphics.GdiContext;
import freert.planar.PlaPointFloat;
import freert.rules.RuleNet;
import freert.varie.PlaDelTri;
import freert.varie.PlaDelTriResultEdge;
import freert.varie.PlaDelTriStorable;
import gui.varie.GuiResources;
import interactive.varie.IteraEdge;
import interactive.varie.IteraNetItem;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import board.RoutingBoard;
import board.infos.AirLineInfo;
import board.items.BrdAbitPin;
import board.items.BrdItem;

/**
 * Creates the Incompletes (Ratsnest) of one net to display them on the screen.
 *
 * @author Alfons Wirtz
 */
public final class NetIncompletes
   {
   // the net the incompletes are bound to
   private final RuleNet rule_net;  

   private final LinkedList<AirLineInfo> incomplete_list = new LinkedList<AirLineInfo>();

   private final double draw_marker_radius;

   /**
    * The length of the violation of the length restriction of the net, > 0, if the cumulative trace length is to big, < 0, if the
    * trace length is to smalll, 0, if the thace length is ok or the net has no length restrictions
    */
   private double length_violation = 0;

   
   public NetIncompletes(int p_net_no, Collection<BrdItem> p_net_items, RoutingBoard p_board, GuiResources p_resources)
      {
      draw_marker_radius = p_board.brd_rules.get_min_trace_half_width() * 2;
      rule_net = p_board.brd_rules.nets.get(p_net_no);

      // Create an array of Item-connected_set pairs.
      IteraNetItem[] net_items = calculate_net_items(p_net_items);
      if (net_items.length <= 1)
         {
         return;
         }

      // create a Delauny Triangulation for the net_items
      Collection<PlaDelTriStorable> triangulation_objects = new LinkedList<PlaDelTriStorable>();
      for (PlaDelTriStorable curr_object : net_items)
         {
         triangulation_objects.add(curr_object);
         }
      PlaDelTri triangulation = new PlaDelTri(triangulation_objects);

      // sort the result edges of the triangulation by length in ascending order.
      Collection<PlaDelTriResultEdge> triangulation_lines = triangulation.get_edge_lines();
      SortedSet<IteraEdge> sorted_edges = new TreeSet<IteraEdge>();

      for (PlaDelTriResultEdge curr_line : triangulation_lines)
         {
         IteraEdge new_edge = new IteraEdge((IteraNetItem) curr_line.start_object, curr_line.start_point.to_float(), (IteraNetItem) curr_line.end_object, curr_line.end_point.to_float());
         sorted_edges.add(new_edge);
         }

      // Create the Airlines. Skip edges, whose from_item and to_item are already in the same connected set
      // or whose connected sets have already an airline.
      RuleNet curr_net = p_board.brd_rules.nets.get(p_net_no);
      Iterator<IteraEdge> it = sorted_edges.iterator();
      while (it.hasNext())
         {
         IteraEdge curr_edge = it.next();
         if (curr_edge.from_item.connected_set == curr_edge.to_item.connected_set)
            {
            continue; // airline exists already
            }
         incomplete_list.add(new AirLineInfo(curr_net, curr_edge.from_item.item, curr_edge.from_corner, curr_edge.to_item.item, curr_edge.to_corner, p_resources));
         join_connected_sets(net_items, curr_edge.from_item.connected_set, curr_edge.to_item.connected_set);
         }
      calc_length_violation();
      }

   /**
    * Returns the number of incomplete of this net.
    */
   public int count()
      {
      return incomplete_list.size();
      }
   
   public LinkedList<AirLineInfo> get_incompletes()
      {
      return incomplete_list;
      }

   /**
    * Recalculates the length violations. Return false, if the lenght violation has not changed.
    */
   boolean calc_length_violation()
      {
      double old_violation = length_violation;
      double max_length = rule_net.get_class().get_maximum_trace_length();
      double min_length = rule_net.get_class().get_minimum_trace_length();
      if (max_length <= 0 && min_length <= 0)
         {
         length_violation = 0;
         return false;
         }
      double new_violation = 0;
      double trace_length = rule_net.get_trace_length();
      if (max_length > 0 && trace_length > max_length)
         {
         new_violation = trace_length - max_length;
         }
      if (min_length > 0 && trace_length < min_length && this.incomplete_list.size() == 0)
         {
         new_violation = trace_length - min_length;
         }
      length_violation = new_violation;
      boolean result = Math.abs(new_violation - old_violation) > 0.1;
      return result;
      }

   /**
    * Returns the length of the violation of the length restriction of the net, > 0, if the cumulative trace length is to big, < 0,
    * if the trace length is to smalll, 0, if the thace length is ok or the net has no length restrictions
    */
   double get_length_violation()
      {
      return length_violation;
      }

   public void draw(Graphics p_graphics, GdiContext p_graphics_context, boolean p_length_violations_only)
      {
      if (!p_length_violations_only)
         {
         java.awt.Color draw_color = p_graphics_context.get_incomplete_color();
         double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
         if (draw_intensity <= 0)
            {
            return;
            }
         PlaPointFloat[] draw_points = new PlaPointFloat[2];
         int draw_width = 1;
         Iterator<AirLineInfo> it = incomplete_list.iterator();
         while (it.hasNext())
            {
            AirLineInfo curr_incomplete = it.next();
            draw_points[0] = curr_incomplete.from_corner;
            draw_points[1] = curr_incomplete.to_corner;
            p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
            if (!curr_incomplete.from_item.shares_layer(curr_incomplete.to_item))
               {
               draw_layer_change_marker(curr_incomplete.from_corner, this.draw_marker_radius, p_graphics, p_graphics_context);
               draw_layer_change_marker(curr_incomplete.to_corner, this.draw_marker_radius, p_graphics, p_graphics_context);
               }
            }
         }
      
      if (length_violation == 0) return;

      // draw the length violation around every Pin of the net.
      Collection<BrdAbitPin> net_pins = rule_net.get_pins();
      for ( BrdAbitPin curr_pin : net_pins)
         {
         draw_length_violation_marker(curr_pin.center_get().to_float(), length_violation, p_graphics, p_graphics_context);
         }
      }

   static void draw_layer_change_marker(PlaPointFloat p_location, double p_radius, Graphics p_graphics, GdiContext p_graphics_context)
      {
      final int draw_width = 1;
      Color draw_color = p_graphics_context.get_incomplete_color();
      
      double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
      
      PlaPointFloat[] draw_points = new PlaPointFloat[2];
      draw_points[0] = new PlaPointFloat(p_location.v_x - p_radius, p_location.v_y - p_radius);
      draw_points[1] = new PlaPointFloat(p_location.v_x + p_radius, p_location.v_y + p_radius);
      
      p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
      
      draw_points[0] = new PlaPointFloat(p_location.v_x + p_radius, p_location.v_y - p_radius);
      draw_points[1] = new PlaPointFloat(p_location.v_x - p_radius, p_location.v_y + p_radius);
      
      p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
      }

   private void draw_length_violation_marker(PlaPointFloat p_location, double p_diameter, Graphics p_graphics, GdiContext p_graphics_context)
      {
      final int draw_width = 1;
      java.awt.Color draw_color = p_graphics_context.get_incomplete_color();
      double draw_intensity = p_graphics_context.get_incomplete_color_intensity();
      double circle_radius = 0.5 * Math.abs(p_diameter);
      p_graphics_context.draw_circle(p_location, circle_radius, draw_width, draw_color, p_graphics, draw_intensity);
      PlaPointFloat[] draw_points = new PlaPointFloat[2];
      draw_points[0] = new PlaPointFloat(p_location.v_x - circle_radius, p_location.v_y);
      draw_points[1] = new PlaPointFloat(p_location.v_x + circle_radius, p_location.v_y);
      p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
      if (p_diameter > 0)
         {
         // draw also the vertical diameter to create a "+"
         draw_points[0] = new PlaPointFloat(p_location.v_x, p_location.v_y - circle_radius);
         draw_points[1] = new PlaPointFloat(p_location.v_x, p_location.v_y + circle_radius);
         p_graphics_context.draw(draw_points, draw_width, draw_color, p_graphics, draw_intensity);
         }
      }

   /**
    * Calculates an array of Item-connected_set pairs for the items of this net. Pairs belonging to the same connected set are
    * located next to each other.
    */
   private IteraNetItem[] calculate_net_items(Collection<BrdItem> p_item_list)
      {
      IteraNetItem[] result = new IteraNetItem[p_item_list.size()];
      Collection<BrdItem> handeled_items = new LinkedList<BrdItem>();
      int curr_index = 0;
      while (!p_item_list.isEmpty())
         {
         BrdItem start_item = p_item_list.iterator().next();
         Collection<BrdItem> curr_connected_set = start_item.get_connected_set(this.rule_net.net_number);
         handeled_items.addAll(curr_connected_set);
         p_item_list.removeAll(curr_connected_set);
         Iterator<BrdItem> it = curr_connected_set.iterator();
         while (it.hasNext())
            {
            BrdItem curr_item = it.next();
            if (curr_index >= result.length)
               {
               System.out.println("NetIncompletes.calculate_net_items: to many items");
               return result;
               }
            result[curr_index] = new IteraNetItem(curr_item, curr_connected_set);
            ++curr_index;
            }
         }
      if (curr_index < result.length)
         {
         System.out.println("NetIncompletes.calculate_net_items: to few items");
         }
      return result;
      }

   /**
    * Joins p_from_connected_set to p_to_connected_set and updates the connected sets of the items in p_net_items.
    */
   private void join_connected_sets(IteraNetItem[] p_net_items, Collection<BrdItem> p_from_connected_set, Collection<BrdItem> p_to_connected_set)
      {
      for (int i = 0; i < p_net_items.length; ++i)
         {
         IteraNetItem curr_item = p_net_items[i];
         if (curr_item.connected_set == p_from_connected_set)
            {
            p_to_connected_set.add(curr_item.item);
            curr_item.connected_set = p_to_connected_set;
            }
         }
      }
   }
