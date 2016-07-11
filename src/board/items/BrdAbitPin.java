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
 * Created on 6. Juni 2003, 08:04
 */

package board.items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import board.RoutingBoard;
import board.infos.BrdComponent;
import board.varie.BrdTraceExitRestriction;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.library.LibLogicalPin;
import freert.library.LibPackage;
import freert.library.LibPackagePin;
import freert.library.LibPadstack;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaShape;
import freert.planar.PlaToupleInt;
import freert.planar.PlaVectorInt;
import freert.planar.Polyline;
import freert.planar.ShapeConvex;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import gui.varie.GuiResources;
import gui.varie.ObjectInfoPanel;

/**
 * This is a Pin , a pin can be SMD or PTH and is normally attached to a component
 * @author Alfons Wirtz
 */
public final class BrdAbitPin extends BrdAbit implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // The number of this pin in its component (starting with 0)
   public final int pin_no;
   //  The pin, this pin was changed to by swapping or this pin, if no pin swap accured.
   private BrdAbitPin changed_to;

   private transient PlaShape[] precalculated_shapes = null;
   
   /**
    * Creates a new instance of Pin with the input parameters. (p_to_layer - p_from_layer + 1) shapes must be provided. 
    * p_pin_no is the number of the pin in its component (starting with 0).
    */
   public BrdAbitPin(int p_component_no, int p_pin_no, NetNosList p_net_no_arr, int p_clearance_type, int p_id_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(null, p_net_no_arr, p_clearance_type, p_id_no, p_component_no, p_fixed_state, p_board);

      pin_no = p_pin_no;
      changed_to = this;
      }
   
   private BrdAbitPin ( BrdAbitPin p_other, int p_id_no )
      {
      super(p_other, p_id_no);

      pin_no = p_other.pin_no;
      changed_to = p_other.changed_to;
      }

   @Override
   public BrdAbitPin copy(int p_id_no)
      {
      return new BrdAbitPin(this,p_id_no);
      }
   
   
   
   /**
    * Calculates the relative location of this pin to its component.
    */
   public final PlaVectorInt relative_location()
      {
      BrdComponent component = r_board.brd_components.get(get_component_no());
      
      LibPackage lib_package = component.get_package();
      LibPackagePin package_pin = lib_package.get_pin(pin_no);
      PlaVectorInt rel_location = package_pin.relative_location();
      
      int rotation_deg = component.get_rotation_in_degree();
      if (!component.is_on_front() && !r_board.brd_components.get_flip_style_rotate_first())
         {
         rel_location = package_pin.relative_location().mirror_at_y_axis();
         }
      
      if (rotation_deg % 90 == 0)
         {
         int component_ninety_degree_factor = rotation_deg / 90;
         if (component_ninety_degree_factor != 0)
            {
            rel_location = rel_location.rotate_90_deg(component_ninety_degree_factor);
            }
         }
      else
         {
         // rotation may be not exact
         PlaPointFloat location_approx = rel_location.to_float();
         location_approx = location_approx.rotate_deg(rotation_deg, PlaPointFloat.ZERO);
         rel_location = location_approx.to_vector();
         }
      
      if (!component.is_on_front() && r_board.brd_components.get_flip_style_rotate_first())
         {
         rel_location = rel_location.mirror_at_y_axis();
         }
      
      return rel_location;
      }

   @Override
   public PlaPointInt center_get()
      {
      PlaPointInt a_center = super.center_get();
      
      if ( a_center != null ) return a_center;
      
      // Calculate the pin center.
      BrdComponent component = r_board.brd_components.get(get_component_no());
      
      a_center = component.get_location().translate_by(relative_location());

      // check that the pin center is inside the pin shape and correct it eventually

      LibPadstack padstack = get_padstack();
      
      int from_layer = padstack.from_layer();
      int to_layer = padstack.to_layer();
      int layers_count = to_layer - from_layer + 1;
      
      PlaShape curr_shape = null;
      
      for (int index = 0; index < layers_count; ++index)
         {
         curr_shape = get_shape(index);

         if (curr_shape != null) break;
         }

      if (curr_shape == null)
         {
         System.out.println("Pin: At least 1 shape != null expected");
         return null;
         }
      
      else if ( ! curr_shape.contains_inside(a_center))
         {
         a_center = curr_shape.centre_of_gravity().round();
         }
      
      // this will possibly adjust something
      super.center_set(a_center);
      
      // and this will pick the changes back
      return super.center_get();
      }

   @Override
   public LibPadstack get_padstack()
      {
      BrdComponent component = r_board.brd_components.get(get_component_no());
      
      if (component == null)
         {
         System.out.println("Pin.get_padstack; component not found");
         return null;
         }
      
      int padstack_no = component.get_package().get_pin(pin_no).padstack_no;
      return r_board.brd_library.padstacks.get(padstack_no);
      }


   /**
    * Return the name of this pin in the package of this component.
    */
   public String get_name()
      {
      BrdComponent component = r_board.brd_components.get(get_component_no());

      if (component == null)
         {
         System.out.println("Pin.name: component not found");
         return "???";
         }
      
      return component.get_package().get_pin(pin_no).name;
      }

   /**
    * Gets index of this pin in the library package of the pins component.
    */
   public int get_index_in_package()
      {
      return pin_no;
      }

   @Override
   public PlaShape get_shape(int p_index)
      {
      if ( precalculated_shapes != null) return precalculated_shapes[p_index];

      LibPadstack padstack = get_padstack();
      
      // all shapes have to be calculated at once, because otherwise calculation of from_layer and to_layer may not be correct
      precalculated_shapes = new PlaShape[padstack.to_layer() - padstack.from_layer() + 1];

      BrdComponent component = r_board.brd_components.get(get_component_no());
      if (component == null)
         {
         System.out.println("Pin.get_shape: component not found");
         return null;
         }
      
      LibPackage lib_package = component.get_package();
      if (lib_package == null)
         {
         System.out.println("Pin.get_shape: package not found");
         return null;
         }
      
      LibPackagePin package_pin = lib_package.get_pin(pin_no);
      if (package_pin == null)
         {
         System.out.println("Pin.get_shape: pin_no out of range");
         return null;
         }
      
      PlaVectorInt rel_location = package_pin.relative_location();
      
      int component_rotation = component.get_rotation_in_degree();

      boolean mirror_at_y_axis = !component.is_on_front() && !r_board.brd_components.get_flip_style_rotate_first();

      if (mirror_at_y_axis)
         {
         rel_location = package_pin.relative_location.mirror_at_y_axis();
         }

      PlaVectorInt component_translation = component.get_location().to_vector();

      for (int index = 0; index < precalculated_shapes.length; ++index)
         {
         int padstack_layer = get_padstack_layer(index);

         ShapeConvex curr_shape = padstack.get_shape(padstack_layer);

         if (curr_shape == null)  continue;

         int pin_rotation = package_pin.rotation_in_degree;
         if (pin_rotation % 90 == 0)
            {
            int pin_ninety_degree_factor = pin_rotation/ 90;
            if (pin_ninety_degree_factor != 0)
               {
               curr_shape = curr_shape.rotate_90_deg(pin_ninety_degree_factor, PlaPointInt.ZERO);
               }
            }
         else
            {
            curr_shape = (ShapeConvex) curr_shape.rotate_rad(Math.toRadians(pin_rotation), PlaPointFloat.ZERO);
            }

         if (mirror_at_y_axis)
            {
            curr_shape = (ShapeConvex) curr_shape.mirror_vertical(PlaPointInt.ZERO);
            }

         // translate the shape first relative to the component
         ShapeConvex translated_shape = (ShapeConvex) curr_shape.translate_by(rel_location);

         if (component_rotation % 90 == 0)
            {
            int component_ninety_degree_factor = component_rotation / 90;
            if (component_ninety_degree_factor != 0)
               {
               translated_shape = (ShapeConvex) translated_shape.rotate_90_deg(component_ninety_degree_factor, PlaPointInt.ZERO);
               }
            }
         else
            {
            translated_shape = (ShapeConvex) translated_shape.rotate_rad(Math.toRadians(component_rotation), PlaPointFloat.ZERO);
            }
         if (!component.is_on_front() && r_board.brd_components.get_flip_style_rotate_first())
            {
            translated_shape = (ShapeConvex) translated_shape.mirror_vertical(PlaPointInt.ZERO);
            }
         precalculated_shapes[index] = (ShapeConvex) translated_shape.translate_by(component_translation);
         }

      return precalculated_shapes[p_index];
      }

   /**
    * Returns the layer of the padstack shape corresponding to the shape with index p_index.
    */
   int get_padstack_layer(int p_index)
      {
      LibPadstack padstack = get_padstack();
      BrdComponent component = r_board.brd_components.get(get_component_no());
      int padstack_layer;
      if (component.is_on_front() || padstack.placed_absolute)
         {
         padstack_layer = p_index + first_layer();
         }
      else
         {
         padstack_layer = padstack.board_layer_count() - p_index - first_layer() - 1;
         }
      return padstack_layer;
      }

   /**
    * Calculates the allowed trace exit directions of the shape of this padstack on layer p_layer together with the minimal trace
    * line lengths into thei directions. 
    * Currently only implemented only for box shapes, where traces are allowed to exit the pad only on the small sides
    * @return an empty result if something fails
    */
   public Collection<BrdTraceExitRestriction> get_trace_exit_restrictions(int p_layer)
      {
      Collection<BrdTraceExitRestriction> result = new LinkedList<BrdTraceExitRestriction>();
      int padstack_layer = get_padstack_layer(p_layer - first_layer());
      double pad_xy_factor = 1.5;
      // setting 1.5 to a higher factor may hinder the shove algorithm of the autorouter between
      // the pins of SMD components, because the channels can get blocked by the shove_fixed stubs.

      BrdComponent component = r_board.brd_components.get(get_component_no());
      if (component != null)
         {
         if (component.get_package().pin_count() <= 3)
            {
            pad_xy_factor *= 2; // allow connection to the longer side also for shorter pads.
            }
         }

      Collection<PlaDirection> padstack_exit_directions = get_padstack().get_trace_exit_directions(padstack_layer, pad_xy_factor);

      if (padstack_exit_directions.isEmpty()) return result;

      if (component == null) return result;

      PlaShape curr_shape = get_shape(p_layer - first_layer());

      if (curr_shape == null || !(curr_shape instanceof ShapeTile)) return result;
      
      ShapeTile pad_shape = (ShapeTile) curr_shape;
      double component_rotation = component.get_rotation_in_degree();
      PlaPointInt pin_center = center_get();
      PlaPointFloat center_approx = pin_center.to_float();

      for (PlaDirection curr_padstack_exit_direction : padstack_exit_directions)
         {
         LibPackage lib_package = component.get_package();

         if (lib_package == null)  continue;

         LibPackagePin package_pin = lib_package.get_pin(pin_no);

         if (package_pin == null)  continue;

         double curr_rotation_in_degree = component_rotation + package_pin.rotation_in_degree;
         PlaDirection curr_exit_direction;
         if (curr_rotation_in_degree % 45 == 0)
            {
            int fortyfive_degree_factor = ((int) curr_rotation_in_degree) / 45;
            curr_exit_direction = curr_padstack_exit_direction.rotate_45_deg(fortyfive_degree_factor);
            }
         else
            {
            double curr_angle_in_radian = Math.toRadians(curr_rotation_in_degree) + curr_padstack_exit_direction.angle_approx();
            curr_exit_direction = new PlaDirection(curr_angle_in_radian);
            }
         
         // calculate the minimum line length from the pin center into curr_exit_direction
         int intersecting_border_line_no = pad_shape.intersecting_border_line_no(pin_center, curr_exit_direction);
         
         if (intersecting_border_line_no < 0)
            {
            System.out.println("Pin.get_trace_exit_restrictions: border line not found");
            continue;
            }
         
         PlaLineInt curr_exit_line = new PlaLineInt(pin_center, curr_exit_direction);
         
         PlaPointFloat nearest_border_point = curr_exit_line.intersection_approx(pad_shape.border_line(intersecting_border_line_no));
         
         // here it seems that the right thing to do is continue
         if ( nearest_border_point.is_NaN() ) continue;
         
         BrdTraceExitRestriction curr_exit_restriction = new BrdTraceExitRestriction(curr_exit_direction, center_approx.distance(nearest_border_point));

         result.add(curr_exit_restriction);
         }
      return result;
      }

   /**
    * Returns true, if this pin has exit restrictions on some kayer.
    */
   public boolean has_trace_exit_restrictions()
      {
      for (int index = first_layer(); index <= last_layer(); ++index)
         {
         Collection<BrdTraceExitRestriction> curr_exit_restrictions = get_trace_exit_restrictions(index);
         
         if (curr_exit_restrictions.size() > 0)  return true;
         }
      return false;
      }

   /**
    * Currently drills are allowed to SMD-pins.
    * Returns true, if vias throw the pads of this pins are allowed, false, otherwise.
    */
   public boolean drill_allowed()
      {
      return (first_layer() == last_layer());
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other == this || p_other instanceof BrdArea)
         {
         return false;
         }
      
      if ( ! p_other.shares_net(this))
         {
         // the two items do not share a net, they are obstacles
         return true;
         }
      
      if (p_other instanceof BrdTracep)
         {
         // they share a net and this is a Trace, not an obstacle
         return false;
         }
      
      
      // One issue is whan two pin are nearby or attached, same net
      // What should happen is that it is not flag as error
      
      if ( drill_allowed() )
         {
         if ( (p_other instanceof BrdAbitVia) && ((BrdAbitVia) p_other).attach_allowed)
            {
            // the other is a Via and I am allowed to attach to it
            return false;
            }
         
         if ( (p_other instanceof BrdAbitPin)  )
            {
            // The other is another Pin that is on the same net, it is fine
            return false;
            }
         }
      
      return true;
      }

   @Override
   public void rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      center_clear();
      clear_derived_data();
      }

   @Override
   public void rotate_deg(int p_angle_in_degree, PlaPointFloat p_pole)
      {
      center_clear();
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      center_clear();
      clear_derived_data();
      }

   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      precalculated_shapes = null;
      }

   /**
    * Return all Pins, that can be swapped with this pin.
    */
   public java.util.Set<BrdAbitPin> get_swappable_pins()
      {
      java.util.Set<BrdAbitPin> result = new java.util.TreeSet<BrdAbitPin>();
      BrdComponent component = r_board.brd_components.get(get_component_no());
      if (component == null)
         {
         return result;
         }
      freert.library.LibLogicalPart logical_part = component.get_logical_part();
      if (logical_part == null)
         {
         return result;
         }
      LibLogicalPin this_part_pin = logical_part.get_pin(pin_no);
      if (this_part_pin == null)
         {
         return result;
         }
      if (this_part_pin.gate_pin_swap_code <= 0)
         {
         return result;
         }
      // look up all part pins with the same gate_name and the same gate_pin_swap_code
      for (int i = 0; i < logical_part.pin_count(); ++i)
         {
         if (i == pin_no)
            {
            continue;
            }
         LibLogicalPin curr_part_pin = logical_part.get_pin(i);
         if (curr_part_pin != null && curr_part_pin.gate_pin_swap_code == this_part_pin.gate_pin_swap_code && curr_part_pin.gate_name.equals(this_part_pin.gate_name))
            {
            BrdAbitPin curr_swappeble_pin = r_board.get_pin(get_component_no(), curr_part_pin.pin_no);
            if (curr_swappeble_pin != null)
               {
               result.add(curr_swappeble_pin);
               }
            else
               {
               System.out.println("Pin.get_swappable_pins: swappable pin not found");
               }
            }
         }
      return result;
      }

   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if ( ! is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.PINS);
      }

   @Override
   public Color[] get_draw_colors(freert.graphics.GdiContext p_graphics_context)
      {
      if ( net_count() > 0)
         return p_graphics_context.get_pin_colors();
      else
         {
         // display unconnected pins as obstacles
         return p_graphics_context.get_obstacle_colors();
         }
      }

   @Override
   public double get_draw_intensity(freert.graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_pin_color_intensity();
      }

   /**
    * Swaps the nets of this pin and p_other.
    * @returns false on error.
    */
   public boolean swap(BrdAbitPin p_other)
      {
      if (net_count() > 1 || p_other.net_count() > 1)
         {
         System.out.println("Pin.swap not yet implemented for pins belonging to more than 1 net ");
         return false;
         }
      
      int this_net_no = net_count() > 0 ? get_net_no(0) : 0; 
      
      int other_net_no = p_other.net_count() > 0 ? p_other.get_net_no(0) : 0; 

      set_net_no(other_net_no);

      p_other.set_net_no(this_net_no);
      
      BrdAbitPin tmp = changed_to;
      
      changed_to = p_other.changed_to;
      
      p_other.changed_to = tmp;
      
      return true;
      }

   /**
    * Returns the pin, this pin was changed to by pin swapping, or this pin, if it was not swapped.
    */
   public BrdAbitPin get_changed_to()
      {
      return changed_to;
      }

   @Override
   public boolean write(java.io.ObjectOutputStream p_stream)
      {
      try
         {
         p_stream.writeObject(this);
         }
      catch (java.io.IOException e)
         {
         return false;
         }
      return true;
      }

   /** 
    * @return false if this drillitem is places on the back side of the board 
    */
   @Override
   public boolean is_placed_on_front()
      {
      BrdComponent component = r_board.brd_components.get(get_component_no());

      if (component != null) return component.is_on_front();
      
      return true;
      }

   /**
    * Returns the smallest width of the pin shape on layer p_layer.
    */
   public double get_min_width(int p_layer)
      {
      int padstack_layer = get_padstack_layer(p_layer - first_layer());
      PlaShape padstack_shape = get_padstack().get_shape(padstack_layer);
      if (padstack_shape == null)
         {
         System.out.println("Pin.get_min_width: padstack_shape is null");
         return 0;
         }
      freert.planar.ShapeTileBox padstack_bounding_box = padstack_shape.bounding_box();
      if (padstack_bounding_box == null)
         {
         System.out.println("Pin.get_min_width: padstack_bounding_box is null");
         return 0;
         }
      return padstack_bounding_box.min_width();
      }

   /**
    * The neckdown width is used, when the pin width is smmaller than the trace width to enter or leave the pin with a trace.
    * @return the neckdown half width for traces on p_layer. 
    */
   public int get_trace_neckdown_halfwidth(int p_layer)
      {
      double result = Math.max(0.5 * get_min_width(p_layer) - 1, 1);
      
      return (int) result;
      }

   /**
    * @return the largest width of the pin shape on layer p_layer.
    */
   public double get_max_width(int p_layer)
      {
      int padstack_layer = get_padstack_layer(p_layer - first_layer());
      PlaShape padstack_shape = get_padstack().get_shape(padstack_layer);
      if (padstack_shape == null)
         {
         System.out.println("Pin.get_max_width: padstack_shape is null");
         return 0;
         }
      
      ShapeTileBox padstack_bounding_box = padstack_shape.bounding_box();
      if (padstack_bounding_box == null)
         {
         System.out.println("Pin.get_max_width: padstack_bounding_box is null");
         return 0;
         }
      return padstack_bounding_box.max_width();
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      GuiResources resources = r_board.newGuiResources("board.resources.ObjectInfoPanel");
      p_window.append_bold(resources.getString("pin"));
      p_window.append(" id="+get_id_no());
      p_window.append(" "+resources.getString("component_2") + " ");
      BrdComponent component = r_board.brd_components.get(get_component_no());
      p_window.append(component.name, resources.getString("component_info"), component);
      p_window.append(", " + resources.getString("pin_2") + " ");
      p_window.append(component.get_package().get_pin(pin_no).name);
      p_window.append(", " + resources.getString("padstack") + " ");
      freert.library.LibPadstack padstack = get_padstack();
      p_window.append(padstack.pads_name, resources.getString("padstack_info"), padstack);
      p_window.append(" " + resources.getString("at") + " ");
      p_window.append(center_get().to_float());
      print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }

   /**
    * Calculates the nearest exit restriction direction for changing p_trace_polyline. p_trace_polyline is assumed to start at the
    * pin center. 
    * @return null, if there is no matching exit restrictions.
    */
   public PlaDirection calc_nearest_exit_restriction_direction(Polyline p_trace_polyline, int p_trace_half_width, int p_layer)
      {
      Collection<BrdTraceExitRestriction> trace_exit_restrictions = get_trace_exit_restrictions(p_layer);

      if (trace_exit_restrictions.isEmpty()) return null;

      PlaShape pin_shape = get_shape(p_layer - first_layer());
      PlaPointInt pin_center = center_get();

      if (!(pin_shape instanceof ShapeTile)) return null;

      double edge_to_turn_dist = r_board.brd_rules.get_pin_edge_to_turn_dist();
      
      if (edge_to_turn_dist < 0) return null;

      ShapeTile offset_pin_shape = ((ShapeTile) pin_shape).offset(edge_to_turn_dist + p_trace_half_width);
      ArrayList<PlaToupleInt> entries = offset_pin_shape.entrance_points(p_trace_polyline);

      if (entries.size() == 0) return null;

      PlaToupleInt latest_entry_tuple = entries.get(entries.size() - 1);
      
      PlaPointFloat trace_entry_location_approx = p_trace_polyline.plaline(latest_entry_tuple.v_a).intersection_approx(offset_pin_shape.border_line(latest_entry_tuple.v_b));

      // it means that the location is an impossible one
      if ( trace_entry_location_approx.is_NaN() ) return null;
      
      // calculate the nearest legal pin exit point to trace_entry_location_approx
      double min_exit_corner_distance = Double.MAX_VALUE;
      PlaPointFloat nearest_exit_corner = null;
      PlaDirection pin_exit_direction = null;
      final double TOLERANCE = 1;
      for (BrdTraceExitRestriction curr_exit_restriction : trace_exit_restrictions)
         {
         int curr_intersecting_border_line_no = offset_pin_shape.intersecting_border_line_no(pin_center, curr_exit_restriction.direction);
         PlaLineInt curr_pin_exit_ray = new PlaLineInt(pin_center, curr_exit_restriction.direction);
         PlaPointFloat curr_exit_corner = curr_pin_exit_ray.intersection_approx(offset_pin_shape.border_line(curr_intersecting_border_line_no));
         double curr_exit_corner_distance = curr_exit_corner.distance_square(trace_entry_location_approx);
         boolean new_nearest_corner_found = false;
         if (curr_exit_corner_distance + TOLERANCE < min_exit_corner_distance)
            {
            new_nearest_corner_found = true;
            }
         else if (curr_exit_corner_distance < min_exit_corner_distance + TOLERANCE)
            {
            // the distances are near equal, compare to the previous corners of p_trace_polyline
            for (int i = 1; i < p_trace_polyline.corner_count(); ++i)
               {
               PlaPointFloat curr_trace_corner = p_trace_polyline.corner_approx(i);
               double curr_trace_corner_distance = curr_trace_corner.distance_square(curr_exit_corner);
               double old_trace_corner_distance = curr_trace_corner.distance_square(nearest_exit_corner);
               if (curr_trace_corner_distance + TOLERANCE < old_trace_corner_distance)
                  {
                  new_nearest_corner_found = true;
                  break;
                  }
               else if (curr_trace_corner_distance > old_trace_corner_distance + TOLERANCE)
                  {
                  break;
                  }
               }
            }
         if (new_nearest_corner_found)
            {
            min_exit_corner_distance = curr_exit_corner_distance;
            pin_exit_direction = curr_exit_restriction.direction;
            nearest_exit_corner = curr_exit_corner;
            }
         }
      return pin_exit_direction;
      }

   /**
    * Calculates the nearest trace exit point of the pin on p_layer. 
    * @return null, if the pin has no trace exit restrictions.
    */
   public PlaPointFloat nearest_trace_exit_corner(PlaPointFloat p_from_point, int p_trace_half_width, int p_layer)
      {
      Collection<BrdTraceExitRestriction> trace_exit_restrictions = get_trace_exit_restrictions(p_layer);

      if (trace_exit_restrictions.isEmpty()) return null;
  
      PlaShape pin_shape = get_shape(p_layer - first_layer());
      PlaPointInt pin_center = center_get();

      if (!(pin_shape instanceof ShapeTile)) return null;
      
      final double edge_to_turn_dist = r_board.brd_rules.get_pin_edge_to_turn_dist();
      
      if (edge_to_turn_dist < 0) return null;

      ShapeTile offset_pin_shape = ((ShapeTile) pin_shape).offset(edge_to_turn_dist + p_trace_half_width);

      // calculate the nearest legal pin exit point to trace_entry_location_approx
      double min_exit_corner_distance = Double.MAX_VALUE;
      PlaPointFloat nearest_exit_corner = null;
      
      for (BrdTraceExitRestriction curr_exit_restriction : trace_exit_restrictions)
         {
         int curr_intersecting_border_line_no = offset_pin_shape.intersecting_border_line_no(pin_center, curr_exit_restriction.direction);
         PlaLineInt curr_pin_exit_ray = new PlaLineInt(pin_center, curr_exit_restriction.direction);
         PlaPointFloat curr_exit_corner = curr_pin_exit_ray.intersection_approx(offset_pin_shape.border_line(curr_intersecting_border_line_no));
         
         // here it seems that the right thing to do is continue
         if ( curr_exit_corner.is_NaN() ) continue;
         
         double curr_exit_corner_distance = curr_exit_corner.distance_square(p_from_point);
         if (curr_exit_corner_distance < min_exit_corner_distance)
            {
            min_exit_corner_distance = curr_exit_corner_distance;
            nearest_exit_corner = curr_exit_corner;
            }
         }
      return nearest_exit_corner;
      }
   
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(500);
      risul.append("pin");
      risul.append(" id="+get_id_no());
      return risul.toString();
      }
   
   
   
   }
