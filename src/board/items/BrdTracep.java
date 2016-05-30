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
 */
package board.items;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import board.BrdConnectable;
import board.RoutingBoard;
import board.algo.AlgoPullTight;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeEntry;
import board.shape.ShapeTreeObject;
import board.varie.BrdChangedArea;
import board.varie.BrdTraceExitRestriction;
import board.varie.BrdTracepCombineFound;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import board.varie.TraceAngleRestriction;
import freert.graphics.GdiContext;
import freert.graphics.GdiDrawable;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaLineIntAlist;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaShape;
import freert.planar.PlaToupleInt;
import freert.planar.PlaVectorInt;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.rules.RuleNet;
import freert.rules.RuleNets;
import freert.varie.NetNosList;
import freert.varie.Signum;
import freert.varie.ThreadStoppable;
import freert.varie.TimeLimitStoppable;
import gui.varie.ObjectInfoPanel;

/**
 * Traces are just described by a Polyline, life is too short to immagine to implement two different "description" that are actually
 * equivalent, yes, it is teoretically possible... but really.
 * @author Alfons Wirtz
 */
public final class BrdTracep extends BrdItem implements BrdConnectable, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="BrdTracep.";
   
   private Polyline polyline;   // the actual line of the trace
 
   private final int trace_half_width; // half width of the trace pen
   private int layer_no; // board layer of the trace
   
   
   public BrdTracep(
         Polyline p_polyline, 
         int p_layer, 
         int p_half_width, 
         NetNosList p_net_no_arr, 
         int p_clearance_type, 
         int p_id_no, 
         int p_group_no, 
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      super(p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);
      
      trace_half_width = p_half_width;

      if ( p_layer <= 0)
         layer_no = 0;
      else if ( p_layer >= p_board.get_layer_count() )
         layer_no = p_board.get_layer_count() - 1;
      else
         layer_no = p_layer;

      if ( ! p_polyline.is_valid())
         throw new IllegalArgumentException("PolylineTrace: p_polyline.arr.length >= 3 expected");
      
      polyline = p_polyline;
      }

   private BrdTracep(BrdTracep p_other, int p_id_no)
      {
      super(p_other,p_id_no);
      
      trace_half_width = p_other.trace_half_width;
      layer_no = p_other.layer_no;
      polyline = p_other.polyline.copy();
      }

   @Override
   public BrdTracep copy(int p_id_no)
      {
      return new BrdTracep(this, p_id_no);
      }

   public int first_layer()
      {
      return layer_no;
      }

   public int last_layer()
      {
      return layer_no;
      }

   public int get_layer()
      {
      return layer_no;
      }

   public void set_layer(int p_layer)
      {
      layer_no = p_layer;
      }

   public final int get_half_width()
      {
      return trace_half_width;
      }


   /**
    * Returns the half with enlarged by the clearance compensation value for the
    * tree with id number p_ttree_id_no Equals get_half_width(), if no clearance
    * compensation is used in this tree.
    */
   public final int get_compensated_half_width(ShapeSearchTree p_search_tree)
      {
      return trace_half_width + p_search_tree.get_clearance_compensation(clearance_idx(), layer_no);
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other == this || p_other instanceof BrdAreaObstacleVia || p_other instanceof BrdAreaObstacleComp)
         {
         return false;
         }
      
      if (p_other instanceof BrdAreaConduction && !((BrdAreaConduction) p_other).get_is_obstacle())
         {
         return false;
         }

      if (! p_other.shares_net(this)) return true;
      
      return false;
      }

   /**
    * Get a list of all items with a connection point on the layer of this trace equal to its first corner.
    */
   public final Set<BrdItem> get_start_contacts()
      {
      return get_normal_contacts(corner_first(), false);
      }

   /**
    * Get a list of all items with a connection point on the layer of this trace equal to its last corner.
    */
   public final Set<BrdItem> get_end_contacts()
      {
      return get_normal_contacts(corner_last(), false);
      }
   
   @Override
   public Set<BrdItem> get_normal_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      PlaPoint start_corner = corner_first();
      
      if (start_corner != null)
         {
         result.addAll(get_normal_contacts(start_corner, false));
         }
      
      PlaPoint end_corner = corner_last();
      
      if (end_corner != null)
         {
         result.addAll(get_normal_contacts(end_corner, false));
         }
      
      return result;
      }

   @Override
   public boolean is_route()
      {
      return ! is_user_fixed() && net_count() > 0;
      }

   /**
    * @return true, if this trace is not contacted at its first or at its last point.
    */
   public boolean is_tail()
      {
      Collection<BrdItem> contact_list = get_start_contacts();
      
      if (contact_list.size() == 0) return true;

      contact_list = get_end_contacts();

      return (contact_list.size() == 0);
      }

   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_colors(is_user_fixed());
      }

   public int get_draw_priority()
      {
      return GdiDrawable.MAX_DRAW_PRIORITY;
      }

   public double get_draw_intensity( GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_color_intensity();
      }

   /**
    * Get a list of all items having a connection point at p_point on the layer of this trace. 
    * If p_ignore_net is false, only contacts to items sharing a net with this trace are calculated. This is the normal case.
    * @param p_skip_areas if true the skip elements of type BrdAreaConduction
    */
   public TreeSet<BrdItem> get_normal_contacts(PlaPoint p_point, boolean p_ignore_net, boolean p_skip_areas )
      {
      TreeSet<BrdItem> result = new TreeSet<BrdItem>();

      if ( p_point == null ) return result;

      // point should land on either first or last corner
      if ( !(p_point.equals(corner_first()) || p_point.equals(corner_last()))) return result;

      ShapeTile search_shape = new ShapeTileBox(p_point);

      Set<ShapeTreeObject> overlaps = r_board.overlapping_objects(search_shape, layer_no);

      for (ShapeTreeObject curr_ob : overlaps)
         {
         // skip myself
         if ( curr_ob == this ) continue;
         
         if (!(curr_ob instanceof BrdItem)) continue;
  
         BrdItem curr_item = (BrdItem) curr_ob;
         
         // skip items that are on different layers
         if ( ! curr_item.shares_layer(this) ) continue;
         
         // skip there is no net sharing
         if ( ! (p_ignore_net || curr_item.shares_net(this))) continue;
         
         if (curr_item instanceof BrdTracep)
            {
            BrdTracep curr_trace = (BrdTracep) curr_item;
            if (p_point.equals(curr_trace.corner_first()) || p_point.equals(curr_trace.corner_last()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) curr_item;
            if (p_point.equals(curr_drill_item.center_get()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAreaConduction)
            {
            BrdAreaConduction curr_area = (BrdAreaConduction) curr_item;
            
            if ( ! p_skip_areas  && curr_area.get_area().contains(p_point))
               {
               result.add(curr_item);
               }
            }
         }
      return result;
      }
   
   public TreeSet<BrdItem> get_normal_contacts(PlaPoint p_point, boolean p_ignore_net)
      {
      return get_normal_contacts(p_point,p_ignore_net, false);
      }

   @Override
   public PlaPointInt normal_contact_point(BrdAbit p_drill_item)
      {
      return p_drill_item.normal_contact_point(this);
      }

   @Override
   public PlaPointInt normal_contact_point(BrdTracep p_other)
      {
      if ( layer_no != p_other.layer_no) return null;

      boolean contact_at_first_corner = corner_first().equals(p_other.corner_first()) || corner_first().equals(p_other.corner_last());
      boolean contact_at_last_corner = corner_last().equals(p_other.corner_first()) || corner_last().equals(p_other.corner_last());
      
      // more than 1 contact point, it is an invalid situation
      if ( contact_at_first_corner && contact_at_last_corner) return null;

      // added rounding, it should be an IntPoint in any case, NO, they may be rationals, really... WARNING
      if (contact_at_first_corner) return corner_first().round();

      if (contact_at_last_corner)  return corner_last().round();

      return null;
      }

   @Override
   public boolean is_drillable(int p_net_no)
      {
      return contains_net(p_net_no);
      }

   /**
    * looks, if this trace is connected to the same object at its start and its end point
    */
   public boolean is_overlap()
      {
      Set<BrdItem> start_contacts = get_start_contacts();
      
      Set<BrdItem> end_contacts = get_end_contacts();
      
      for ( BrdItem end_contact : end_contacts )
         {
         if (start_contacts.contains(end_contact))  return true;
         }

      return false;
      }

   /**
    * Returns true, if it is not allowed to change the location of this item by the push algorithm.
    */
   public final boolean is_shove_fixed()
      {
      if ( super.is_shove_fixed())  return true;

      // check, if the trace belongs to a net, which is not shovable.
      RuleNets nets = r_board.brd_rules.nets;
      
      for (int curr_net_no : net_nos)
         {
         // do not check special nets
         if ( ! RuleNets.is_normal_net_no(curr_net_no)) continue;
         
         // trace is fixed if the net is shove fixed
         if (nets.get(curr_net_no).get_class().is_shove_fixed()) return true;
         }
      
      return false;
      }

   /**
    * @return the endpoint of this trace with the shortest distance to p_from_point
    */
   public PlaPoint nearest_end_point(PlaPointInt p_from_point)
      {
      PlaPoint p1 = corner_first();
      
      PlaPoint p2 = corner_last();
      
      PlaPointFloat from_point = p_from_point.to_float();

      double d1 = from_point.distance(p1.to_float());
      
      double d2 = from_point.distance(p2.to_float());

      if (d1 < d2)
         return p1;
      else
         return p2;
      }

   
   
   
   
   /**
    * Checks, if this trace can be reached by other items via more than one path
    * a cycle exists if through expanding the start contact we reach this trace again via an end contact
    */
   public boolean has_cycle()
      {
      if ( is_overlap() ) return true;

      Set<BrdItem> visited_items = new TreeSet<BrdItem>();
      
      Collection<BrdItem> start_contacts = get_start_contacts();

      for (BrdItem curr_contact : start_contacts)
         {
         // make shure, that all direct neighbours are expanded from here, to block coming back to this trace via a start contact.
         visited_items.add(curr_contact);
         }
      
      boolean ignore_areas = false;
      
      if ( ! net_nos.is_empty() )
         {
         RuleNet curr_net = r_board.brd_rules.nets.get(net_nos.first());

         if (curr_net != null && curr_net.get_class() != null)
            {
            ignore_areas = curr_net.get_class().get_ignore_cycles_with_areas();
            }
         }
      
      for (BrdItem curr_contact : start_contacts)
         {
         if ( curr_contact.has_cycle_recu(visited_items, this, this, ignore_areas)) return true;
         }

      return false;
      }

   @Override
   public int shape_layer(int p_index)
      {
      return layer_no;
      }

   @Override
   public ArrayList<PlaPointInt> get_ratsnest_corners()
      {
      // Use only uncontacted enpoints of the trace.
      // Otherwise the allocated memory in the calculation of the incompletes might become very big.

      ArrayList<PlaPointInt> result = new ArrayList<PlaPointInt>(2);

      if (get_start_contacts().isEmpty())
         result.add( corner_first().round() );   
      
      if (get_end_contacts().isEmpty())
         result.add( corner_last().round() );   
      
      return result;
      }


   @Override
   public final boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if ( ! is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.TRACES);
      }

   
   private final void touching_pins_at_end_corners(TreeSet<BrdAbitPin>result, PlaPoint curr_end_point)
      {
      ShapeTileOctagon curr_oct;
      
      if ( curr_end_point.is_rational() )
         curr_oct = new ShapeTileOctagon(curr_end_point.to_float());
      else
         curr_oct = new ShapeTileOctagon(curr_end_point.round());
      
      curr_oct = curr_oct.enlarge(trace_half_width);
      
      Set<BrdItem> curr_overlaps = r_board.overlapping_items_with_clearance(curr_oct, layer_no, NetNosList.EMPTY, clearance_idx());
   
      for (BrdItem curr_item : curr_overlaps)
         {
         if ((curr_item instanceof BrdAbitPin) && curr_item.shares_net(this))
            {
            result.add((BrdAbitPin) curr_item);
            }
         }
      }
   
   /**
    * Looks up touching pins at the first corner and the last corner of the trace. 
    * Used to avoid acid traps.
    */
   public final TreeSet<BrdAbitPin> touching_pins_at_end_corners()
      {
      TreeSet<BrdAbitPin> result = new TreeSet<BrdAbitPin>();

      touching_pins_at_end_corners(result, corner_first());
      touching_pins_at_end_corners(result, corner_last());
      
      return result;
      }

   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("trace"));
      p_window.append(" " + resources.getString("from"));
      p_window.append(corner_first().to_float());
      p_window.append(resources.getString("to"));
      p_window.append(corner_last().to_float());
      p_window.append(resources.getString("on_layer") + " ");
      p_window.append(r_board.layer_structure.get_name(layer_no));
      p_window.append(", " + resources.getString("width") + " ");
      p_window.append(2 * trace_half_width);
      p_window.append(", " + resources.getString("length") + " ");
      p_window.append(get_length());
      print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }

   @Override
   public boolean validate_ok()
      {
      boolean result = super.validate_ok();

      if (corner_first().equals( corner_last()))
         {
         System.out.println("Trace.validate: first and last corner are equal");
         result = false;
         }
      
      return result;
      }
   
   

   /**
    * checks, if this trace is on layer p_layer
    */
   @Override
   public boolean is_on_layer(int p_layer)
      {
      return get_layer() == p_layer;
      }

   /**
    * returns the first corner of the trace
    * returns the first corner of this trace, which is the intersection of the first and second lines of its polyline
    * It MUST return an int point otherwise I will notbe able to connect to a pin !!!
    */
   public PlaPoint corner_first()
      {
      return polyline.corner_first();
      }

   /**
    * returns the last corner of the trace
    * returns the last corner of this trace, which is the intersection of the last two lines of its polyline
    * It MUST return an int point otherwise I will notbe able to connect to a pin !!!
    */
   public PlaPoint corner_last()
      {
      return polyline.corner_last();
      }

   /**
    * returns the number of corners of this trace
    */
   public int corner_count()
      {
      return polyline.corner_count();
      }

   /**
    * Returns the length of this trace.
    */
   public double get_length()
      {
      return polyline.length_approx();
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      ShapeTileBox result = polyline.bounding_box();
      
      return result.offset(get_half_width());
      }

   @Override
   public void draw(Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null) return;

      int layer = get_layer();
      Color color = p_color_arr[layer];
      double display_width = get_half_width();
      double intensity = p_intensity * p_graphics_context.get_layer_visibility(layer);
      
      p_graphics_context.draw(polyline.corner_approx_arr(), display_width, color, p_g, intensity);
      }

   /**
    * Returns the polyline of this trace.
    */
   public Polyline polyline()
      {
      return polyline;
      }

   @Override
   protected final ShapeTile[] calculate_tree_shapes(ShapeSearchTree p_search_tree)
      {
      return p_search_tree.calculate_tree_shapes(this);
      }

   /**
    * returns the count of tile shapes of this polyline
    */
   @Override
   public int tile_shape_count()
      {
      return Math.max(polyline.plalinelen(-2), 0);
      }

   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      polyline = polyline.translate_by(p_vector);
      
      clear_derived_data();
      }

   @Override
   public void turn_90_degree(int p_factor, PlaPointInt p_pole)
      {
      polyline = polyline.turn_90_degree(p_factor, p_pole);
      
      clear_derived_data();
      }

   @Override
   public void rotate_approx(double p_angle_in_degree, PlaPointFloat p_pole)
      {
      polyline = polyline.rotate_approx(Math.toRadians(p_angle_in_degree), p_pole);
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      polyline = polyline.mirror_vertical(p_pole);

      set_layer(r_board.get_layer_count() - get_layer() - 1);

      clear_derived_data();
      }

   /**
    * Looks, if other traces can be combined with this trace. 
    * This trace will be the combined trace, so that only other traces may be deleted.
    * WARNING: This is a recursive call function, are we really sure that it is "good" ?
    * @return true, if Something has been combined.
    */
   public boolean combine(int recursion_depth)
      {
      if (! is_on_the_board()) return false;

      // we have exausted the recursion depth
      if ( recursion_depth <= 0 )
         {
         System.out.println(classname+"combine: recursion exausted trace "+this);
//         new IllegalArgumentException("combine: recursion exausted").printStackTrace();
         return false;
         }
      
      recursion_depth--;
      
      boolean something_changed = false;
      
      if (combine_at_start())
         {
         something_changed = true;
         // note that now I do not care of return code
         combine(recursion_depth);
         }
      else if (combine_at_end())
         {
         something_changed = true;
         // note that now I do not care of return code
         combine(recursion_depth);
         }
      
      if (something_changed)
         {
         // let the observers synchronize the changes
         r_board.observers.notify_changed(this);
         art_item_clear(); // need to clean up possible autoroute item;
         }
      
      return something_changed;
      }

   /**
    * looks, if this trace can be combined at its first point with an other trace. 
    * The corners of the other trace will be inserted in front of thie trace. 
    * In case of combine the other trace will be deleted and this trace will remain.
    * @returns true, if somthing was combined. 
    */
   private boolean combine_at_start()
      {
      PlaPoint end_corner = corner_first();

      BrdTracepCombineFound other = search_end(end_corner, false);
      
      if (other.other_trace == null ) return false;

      r_board.undo_items.save_for_undo(this);

      // create the lines of the joined polyline
      Polyline other_poly = other.other_trace.polyline;
      
      if (other.reverse_order) other_poly = other_poly.reverse();
      
      boolean skip_line = other_poly.plaline_last_prev().is_equal_or_opposite(polyline.plaline_first_next());
      
      // assume I want to copy other up until the end line
      int my_copy_count = other_poly.plalinelen(-1);
      
      // but if I need to stip the previous one, copy one less
      if (skip_line) my_copy_count--;
      
      PlaLineIntAlist new_lines = new PlaLineIntAlist(polyline.plalinelen() + other_poly.plalinelen());
      
      // append other up until the last line or less, if requested
      other_poly.plaline_append(new_lines, 0, my_copy_count);
      
      // then append myself, after the first line to the end
      polyline.plaline_append(new_lines, 1);

      Polyline joined_polyline = new Polyline(new_lines);

      if (joined_polyline.plalinelen() != new_lines.size())
         {
         // consecutive parallel lines where skipped at the join location combine without performance optimation
         r_board.search_tree_manager.remove(this);
         polyline = joined_polyline;
         clear_derived_data();
         r_board.search_tree_manager.insert(this);
         }
      else
         {
         // reuse the tree entries for better performance create the changed line shape at the join location
         int to_no = other_poly.plalinelen();

         if (skip_line) --to_no;

         r_board.search_tree_manager.merge_entries_in_front(other.other_trace, this, joined_polyline, other_poly.plalinelen(-3), to_no);
         other.other_trace.clear_search_tree_entries();
         polyline = joined_polyline;
         }
      
      if ( ! polyline.is_valid() )
         {
         r_board.remove_item(this);
         }
      
      r_board.remove_item(other.other_trace);

      r_board.join_changed_area(end_corner.to_float(), get_layer());

      return true;
      }

   private BrdTracepCombineFound search_end (PlaPoint end_corner, boolean at_end )
      {
      PlaPoint a_corner,b_corner;
      
      BrdTracepCombineFound risul = new BrdTracepCombineFound();
      
      TreeSet<BrdItem> contacts = get_normal_contacts(end_corner, false, true);
      
      // combine cannot work with more than one contact...
      if ( contacts.size() != 1 ) return risul;
      
      for ( BrdItem curr_ob : contacts )
         {
         if ( ! ( curr_ob instanceof BrdTracep) ) continue;
         
         BrdTracep other_trace = (BrdTracep) curr_ob;

         if ( other_trace.get_layer() != get_layer() ) continue; 
               
         if ( ! other_trace.nets_equal(this) ) continue;
         
         if ( other_trace.get_half_width() != get_half_width() ) continue;
         
         // maybe this could be different ?
         if ( other_trace.get_fixed_state() != get_fixed_state() ) continue;

         if ( at_end )
            {
            a_corner = other_trace.corner_first();
            b_corner = other_trace.corner_last();
            }
         else
            {
            a_corner = other_trace.corner_last();
            b_corner = other_trace.corner_first();
            }

         if (end_corner.equals(a_corner))
            {
            risul.other_trace = other_trace;
            return risul;
            }
         
         if (end_corner.equals(b_corner))
            {
            risul.other_trace = other_trace;
            risul.reverse_order = true;
            return risul;
            }
         }
      
      return risul;
      }
   
   /**
    * looks, if this trace can be combined at its last point with another trace.
    * Returns true, if somthing was combined. The corners of the other trace
    * will be inserted at the end of thie trace. In case of combine the other
    * trace will be deleted and this trace will remain.
    */
   private boolean combine_at_end()
      {
      PlaPoint end_corner = corner_last();

      BrdTracepCombineFound other = search_end(end_corner, true);
      
      if (other.other_trace == null ) return false;

      r_board.undo_items.save_for_undo(this);
      
      // create the lines of the joined polyline

      Polyline other_poly = other.other_trace.polyline;
      
      if (other.reverse_order) other_poly = other_poly.reverse();
      
      boolean skip_line = polyline.plaline_last_prev().is_equal_or_opposite(other_poly.plaline_first_next());
      
      // assume I want to copy myself up until the end line
      int my_copy_count = polyline.plalinelen(-1);
      
      // but if I need to stip the previous one, copy one less
      if (skip_line) my_copy_count--;
      
      PlaLineIntAlist new_lines = new PlaLineIntAlist(polyline.plalinelen() + other_poly.plalinelen());
      
      // append myself up until the last line or less, if requested
      polyline.plaline_append(new_lines, 0, my_copy_count);
      
      // then append other, after the first line to the end
      other_poly.plaline_append(new_lines, 1);
      
      Polyline joined_polyline = new Polyline(new_lines);
      
      if (joined_polyline.plalinelen() != new_lines.size())
         {
         // consecutive parallel lines where skipped at the join location combine without performance optimation
         r_board.search_tree_manager.remove(this);
         clear_search_tree_entries();
         polyline = joined_polyline;
         clear_derived_data();
         r_board.search_tree_manager.insert(this);
         }
      else
         {
         // reuse tree entries for better performance create the changed line shape at the join location
         int to_no = polyline.plalinelen();

         if (skip_line) --to_no;
         
         r_board.search_tree_manager.merge_entries_at_end(other.other_trace, this, joined_polyline, polyline.plalinelen(-3), to_no);
         other.other_trace.clear_search_tree_entries();
         polyline = joined_polyline;
         }
      
      if ( ! polyline.is_valid() )
         {
         r_board.remove_item(this);
         }
      
      r_board.remove_item(other.other_trace);

      r_board.join_changed_area(end_corner.to_float(), get_layer());

      return true;
      }

   /**
    * Looks up traces intersecting with this trace and splits them at the intersection points. 
    * In case of an overlaps, the traces are split at their first and their last common point. 
    * Found cycles are removed. 
    * If nothing is split, the result will contain just this Trace. 
    * If p_clip_shape != null, the split may be resticted to p_clip_shape.
    * @return the pieces resulting from splitting
    */
   public LinkedList<BrdTracep> split(ShapeTileOctagon p_clip_shape)
      {
      LinkedList<BrdTracep> result = new LinkedList<BrdTracep>();

      if ( ! is_nets_normal())
         {
         // only normal nets are split
         result.add(this);
         return result;
         }
      
      boolean own_trace_split = false;
      
      ShapeSearchTree default_tree = r_board.search_tree_manager.get_default_tree();

      for (int index = 0; index < polyline.plalinelen(-2); ++index)
         {
         PlaSegmentInt curr_segment = polyline.segment_get(index + 1);

         if (p_clip_shape != null)
            {
            if ( ! p_clip_shape.intersects(curr_segment.bounding_box())) continue;
            }

         ShapeTile curr_shape = get_tree_shape(default_tree, index);
         
         LinkedList<ShapeTreeEntry> over_tree_entries = new LinkedList<ShapeTreeEntry>();

         // look for intersecting traces with the i-th line segment
         
         default_tree.calc_overlapping_tree_entries(curr_shape, get_layer(), over_tree_entries);
         
         Iterator<ShapeTreeEntry> over_tree_iter = over_tree_entries.iterator();
         
         while (over_tree_iter.hasNext())
            {
            // this trace has been deleted in a cleanup operation
            if (!is_on_the_board()) return result;
            
            ShapeTreeEntry overlap_tentry = over_tree_iter.next();
            
            if (!(overlap_tentry.object instanceof BrdItem)) continue;
            
            BrdItem overlap_item = (BrdItem) overlap_tentry.object;

            if ( split_avoid_this_item(index, overlap_tentry, overlap_item)) continue;
            
            if (!overlap_item.shares_net(this)) continue;
               
            if (overlap_item instanceof BrdTracep)
               {
               BrdTracep found_trace = (BrdTracep) overlap_item;
               
               PlaSegmentInt found_line_segment = found_trace.polyline.segment_get(overlap_tentry.shape_index_in_object + 1);
               
               ArrayList<PlaLineInt> intersecting_lines = found_line_segment.intersection(curr_segment);
               
               LinkedList<BrdTracep> split_pieces = new LinkedList<BrdTracep>();

               // try splitting the found trace first
               boolean found_trace_split = split_tracep_other (found_trace, split_pieces, intersecting_lines, overlap_tentry);

               if (found_trace_split)
                  {
                  // reread the overlapping tree entries and reset the iterator, because the board has changed
                  default_tree.calc_overlapping_tree_entries(curr_shape, get_layer(), over_tree_entries);
                  over_tree_iter = over_tree_entries.iterator();
                  }
               
               // now try splitting the own trace
               intersecting_lines = curr_segment.intersection(found_line_segment);
               
               // no need to readjust the iterator since we are actually exiting
               own_trace_split = split_tracep_own (index,result,intersecting_lines, p_clip_shape);
     
               if ( found_trace_split ) split_tracep_remove_cycles (split_pieces);

               // do this last to preserve traces if possible
               if ( own_trace_split ) split_tracep_remove_cycles (result);
               
               if (own_trace_split) break;
               }
            else if (overlap_item instanceof BrdAbit)
               {
               split_abit (index,  (BrdAbit)overlap_item, curr_segment);
               }
            else if ( overlap_item instanceof BrdAreaConduction )
               {
               if ( split_conduction ( (BrdAreaConduction)overlap_item ) ) return result;
               }
            }
         
         if ( own_trace_split )  break;
         }
      
      if ( ! own_trace_split ) result.add(this);
      
      if (result.size() > 1)
         {
         // need to clean up possible autoroute information
         for (BrdItem curr_item : result)  curr_item.art_item_clear(); 
         }
      
      return result;
      }
   
   /**
    * check if I should aboid testing this segment
    * @return
    */
   private boolean split_avoid_this_item (int index, ShapeTreeEntry found_entry, BrdItem overlap_item)
      {
      if (overlap_item != this) return false;
      
      int line_index_in_object = found_entry.shape_index_in_object; 
      
      if ( line_index_in_object >= index - 1 && line_index_in_object <= index + 1)
         {
         // don't split own trace at this line or at neighbour lines
         return true;
         }

      // try to handle intermediate segments of length 0 by comparing end corners
      if (index < line_index_in_object)
         {
         if (polyline.corner(index + 1).equals(polyline.corner(line_index_in_object)))
            {
            return true;
            }
         }
      else if (line_index_in_object < index)
         {
         if (polyline.corner(line_index_in_object + 1).equals(polyline.corner(index)))
            {
            return true;
            }
         }
      
      return false;
      }

   
   private boolean split_conduction ( BrdAreaConduction c_area )
      {
      boolean ignore_areas = false;
      
      if ( is_user_fixed() ) return false;
      
      RuleNet curr_net = r_board.brd_rules.nets.get(net_nos.first());
      
      if (curr_net != null && curr_net.get_class() != null)
         {
         ignore_areas = curr_net.get_class().get_ignore_cycles_with_areas();
         }

      if ( ignore_areas ) return false;
      
      if ( get_start_contacts().contains(c_area) && get_end_contacts().contains(c_area))
         {
         // this trace can be removed because of cycle with conduction area
         // Hmmm, surely I should split up until where conduction area begins ?
         r_board.remove_item(this);
         return true;
         }
      
      return false;
      }
   
   private void split_abit (int index,  BrdAbit curr_drill_item, PlaSegmentInt curr_line_segment )
      {
      PlaPointInt split_point = curr_drill_item.center_get();
   
      if ( ! curr_line_segment.contains(split_point) ) return;
      
      PlaDirection split_line_direction = curr_line_segment.get_line().direction().turn_45_degree(2);
      
      PlaLineInt split_line = new PlaLineInt(split_point, split_line_direction);
   
      // Icould have a split with int point parameter, it is then known that I am splitting at int point...
      split_with_end_line(index + 1, split_line);
      }

   private void split_tracep_remove_cycles ( LinkedList<BrdTracep> a_collection )
      {
      for ( BrdTracep curr_piece : a_collection ) r_board.remove_if_cycle(curr_piece);
      }

   private boolean split_tracep_own (int index, LinkedList<BrdTracep> result, ArrayList<PlaLineInt> intersecting_lines, ShapeTileOctagon p_clip_shape )
      {
      boolean have_trace_split = false;
      
      for ( PlaLineInt inter_line : intersecting_lines )
         {
         if ( have_trace_split ) break;
         
         ArrayList<BrdTracep> curr_split_pieces = split_with_end_line(index + 1, inter_line);

         if (curr_split_pieces.size() < 1 ) continue;

         // yes, we have a trace split
         have_trace_split = true;
         
         result.addAll(curr_split_pieces.get(0).split(p_clip_shape));

         if (curr_split_pieces.size() < 2 ) continue;

         result.addAll(curr_split_pieces.get(1).split(p_clip_shape));
         }
      
      return have_trace_split;
      }
   
   /**
    * return true if some other trace was split
    */
   private boolean split_tracep_other (BrdTracep found_trace, Collection<BrdTracep> split_pieces, ArrayList<PlaLineInt> intersecting_lines, ShapeTreeEntry found_entry )
      {
      if ( found_trace == this ) return false;
      
      boolean have_trace_split = false;
      
      for (PlaLineInt inter_line : intersecting_lines )
         {
         if ( have_trace_split ) break;
         
         int line_no = found_entry.shape_index_in_object + 1;
         
         ArrayList<BrdTracep> curr_split_pieces = found_trace.split_with_end_line(line_no, inter_line);

         if (curr_split_pieces.size() < 1 ) continue;
         
         split_pieces.add(curr_split_pieces.get(0));

         have_trace_split = true;

         if (curr_split_pieces.size() < 2 ) continue;

         split_pieces.add(curr_split_pieces.get(1));
         }
   
      if ( ! have_trace_split) split_pieces.add(found_trace);

      return have_trace_split;
      }
   
   
   /**
    * Checks, if the intersection of the p_line_no-th line of this trace with p_line is inside the pad of a pin. 
    * In this case the trace will be split only, if the intersection is at the center of the pin. 
    * Extending the function to vias leaded to broken connection problems wenn the autorouter connected to a trace.
    * @return true if a split is allowed
    */
   private boolean split_inside_drill_pad_allowed(int p_line_no, PlaLineInt p_line)
      {
      PlaPoint intersection = polyline.plaline(p_line_no).intersection(p_line, null);

      // it is kind of OK if intersecion is a NaN since it will NOT match a point
      if ( intersection.is_NaN() ) return true;
      
      Collection<BrdItem> overlap_items = r_board.pick_items(intersection, get_layer() );
      
      boolean pad_found = false;
      
      for (BrdItem curr_item : overlap_items)
         {
         if ( ! curr_item.shares_net(this)) continue;

         if (curr_item instanceof BrdAbitPin)
            {
            BrdAbit curr_drill_item = (BrdAbit) curr_item;
            
            pad_found = true;  // remember that I have found a pad here
            
            if (curr_drill_item.center_get().equals(intersection))
               {
               // split always allowed at the center of a drill item.
               return true; 
               }
            }
         else if (curr_item instanceof BrdTracep)
            {
            BrdTracep curr_trace = (BrdTracep) curr_item;
            
            if (curr_trace != this && curr_trace.corner_first().equals(intersection) || curr_trace.corner_last().equals(intersection))
               {
               return true;
               }
            }
         }
      
      // a split is allowed if we are not inside a pad
      return pad_found == false;
      }
   
   /**
    * Splits this trace into two at p_point. 
    * can return null i for example p_point is not located on this trace.
    * @return true if the trace has been split 
    */
   public final boolean split_at_point(PlaPointInt p_point)
      {
      for (int index = 1; index < polyline.plalinelen(-1); index++)
         {
         PlaSegmentInt curr_line_segment = polyline.segment_get(index);
         
         // The split point (an integer) is within the current line segment
         if ( ! curr_line_segment.contains(p_point)) continue;
         
         PlaDirection split_direction = curr_line_segment.get_line().direction().turn_45_degree(2);

         PlaLineInt split_line = new PlaLineInt(p_point, split_direction);
         
         ArrayList<BrdTracep> result = split_with_end_line(index, split_line);
         
         if (result.size() > 0 )  return true;
         }

      return false;
      }

   /**
    * Splits this trace at the line with number p_line_no into two 
    * by inserting p_endline as concluding line of the first split piece and as the start line of the second split piece
    * NOTE that this actually changes the items on the board !!!
    * @return the splitted traces or nothing if no split
    */
   private ArrayList<BrdTracep> split_with_end_line(int p_line_no, PlaLineInt p_new_end_line)
      {
      ArrayList<BrdTracep> risul = new  ArrayList<BrdTracep>(2);
      
      if (!is_on_the_board()) return risul;

      // if split prohibited do nothing
      if ( ! split_inside_drill_pad_allowed(p_line_no, p_new_end_line)) return risul;

      ArrayList<Polyline> split_polylines = polyline.split(p_line_no, p_new_end_line);

      if (split_polylines.size() < 2) return risul;
      
      r_board.remove_item(this);

      BrdTracep a_trace = r_board.insert_trace_without_cleaning(split_polylines.get(0), get_layer(), get_half_width(), net_nos, clearance_idx(), get_fixed_state());
      
      if ( a_trace != null ) risul.add( a_trace );
      
      a_trace = r_board.insert_trace_without_cleaning(split_polylines.get(1), get_layer(), get_half_width(), net_nos, clearance_idx(), get_fixed_state());
      
      if ( a_trace != null ) risul.add( a_trace );

      return risul;
      }

   
   /**
    * Splits this trace and overlapping traces, and combines this trace.
    * If p_clip_shape != null, splitting is restricted to p_clip_shape. 
    * @return true, if something was changed. 
    */
   public boolean normalize(ShapeTileOctagon p_clip_shape)
      {
      return normalize_recu(p_clip_shape, 10);
      }
   
   private boolean normalize_recu(ShapeTileOctagon p_clip_shape, int loop_countdown)
      {
      r_board.start_notify_observers();
      
      if ( loop_countdown <= 0 )
         {
         System.err.println(classname+"normalize_recu: countdown exceeded");
         return false;
         }
      
      loop_countdown--;
      
      LinkedList<BrdTracep> split_traces_list = split(p_clip_shape);
     
      boolean result = split_traces_list.size() != 1;

      for ( BrdTracep split_trace : split_traces_list )
         {
         if ( ! split_trace.is_on_the_board()) continue;
         
         boolean trace_combined = split_trace.combine(30);
         
         if (split_trace.corner_count() == 2 && split_trace.corner_first().equals(split_trace.corner_last()))
            {
            // This should not be even possible !!! remove trace with only 1 corner
            r_board.remove_item(split_trace);
            result = true;
            }
         else if (trace_combined)
            {
            split_trace.normalize_recu(p_clip_shape,loop_countdown);
            result = true;
            }
         }
      
      r_board.end_notify_observers();

      return result;
      }

   /**
    * Tries to shorten this trace without creating clearance violations
    * It may as well check and fix any possible current clearance violations, no ? damiano 
    * @returns true if the trace was changed.
    */
   public boolean pull_tight(AlgoPullTight p_pull_tight_algo)
      {
      if ( p_pull_tight_algo.is_stop_requested() ) return false;
      
      // This trace may have been deleted in a trace split for example
      if (! is_on_the_board())  return false;
      
      if ( is_shove_fixed()) return false;

      if ( ! is_nets_normal()) return false;
      
      if (p_pull_tight_algo.only_net_no_arr.size() > 0 && ! nets_equal(p_pull_tight_algo.only_net_no_arr))
         {
         return false;
         }

      if ( ! net_nos.is_empty() )
         {
         // why only of index 0 ?
         if (! r_board.brd_rules.nets.get( net_nos.first()).get_class().can_pull_tight())
            {
            return false;
            }
         }
      
      Polyline new_lines = p_pull_tight_algo.pull_tight(polyline, get_layer(), get_half_width(), net_nos, clearance_idx(), touching_pins_at_end_corners());
      if (new_lines != polyline)
         {
         change(new_lines);
         return true;
         }
      
      TraceAngleRestriction angle_restriction = r_board.brd_rules.get_trace_snap_angle();
      
      if (angle_restriction != TraceAngleRestriction.NINETY && r_board.brd_rules.get_pin_edge_to_turn_dist() > 0)
         {
         if (swap_connection_to_pin(true))
            {
            pull_tight(p_pull_tight_algo);
            return true;
            }
         
         if (swap_connection_to_pin(false))
            {
            pull_tight(p_pull_tight_algo);
            return true;
            }
         
         // optimize algorithm could not improve the trace, try to remove acid traps
         
         if (correct_connection_to_pin(true, angle_restriction))
            {
            pull_tight(p_pull_tight_algo);
            return true;
            }
         
         if (correct_connection_to_pin(false, angle_restriction))
            {
            pull_tight(p_pull_tight_algo);
            return true;
            }
         
         }
      
      return false;
      }

   /**
    * pippo
    * @param p_own_net_only
    * @param p_pull_tight_accuracy
    * @return
    */
   public final boolean pull_tight(boolean p_own_net_only, int p_pull_tight_accuracy )
      {
      return pull_tight(p_own_net_only, p_pull_tight_accuracy, new TimeLimitStoppable(20) );
      }
   
   /**
    * Tries to pull this trace tight without creating clearance violations
    * @return true, if the trace was changed.
    */
   public final boolean pull_tight(boolean p_own_net_only, int p_pullt_min_move, ThreadStoppable p_stoppable_thread)
      {
      NetNosList opt_net_no_arr;
      
      if (p_own_net_only)
         {
         opt_net_no_arr = net_nos;
         }
      else
         {
         opt_net_no_arr = NetNosList.EMPTY;
         }
      
      AlgoPullTight pull_tight_algo = AlgoPullTight.get_instance(r_board, opt_net_no_arr, null, p_pullt_min_move, p_stoppable_thread, null );
      
      return pull_tight(pull_tight_algo);
      }

   /**
    * Tries to smoothen the end corners of this trace, which are at a fork with other traces.
    */
   public boolean smoothen_end_corners_fork(boolean p_own_net_only, int p_pullt_min_move, ThreadStoppable p_stoppable_thread)
      {
      NetNosList opt_net_no_arr;
      
      if (p_own_net_only)
         {
         opt_net_no_arr = net_nos;
         }
      else
         {
         opt_net_no_arr = NetNosList.EMPTY;
         }
      
      AlgoPullTight pull_tight_algo = AlgoPullTight.get_instance(r_board, opt_net_no_arr, null, p_pullt_min_move, p_stoppable_thread, null );

      return pull_tight_algo.smoothen_end_corners_at_trace(this);
      }

   @Override
   public ShapeTile get_trace_connection_shape(ShapeSearchTree p_search_tree, int p_index)
      {
      if (p_index < 0 || p_index >= tile_shape_count())
         {
         System.out.println("PolylineTrace.get_trace_connection_shape p_index out of range");
         return null;
         }
      
      PlaSegmentInt curr_line_segment = polyline.segment_get( p_index + 1);
      
      ShapeTile result = curr_line_segment.to_simplex().simplify();
      
      return result;
      }

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
    * changes the geometry of this trace to p_new_polyline
    */
   public void change(Polyline p_new_polyline)
      {
      if ( p_new_polyline == null ) return;

      art_item_clear(); // need to clean up possible autoroute item
      
      if (! is_on_the_board())
         {
         // Just change the polyline of this trace.
         polyline = p_new_polyline;
         return;
         }

      // The precalculated tile shapes must not be cleared here here because they are used and modified
      // in ShapeSearchTree.change_entries.

      r_board.undo_items.save_for_undo(this);

      // for performance reasons there is some effort to reuse ShapeTree entries of the old trace in the changed trace

      // look for the first line in p_new_polyline different from the lines of the existung trace
      int last_index = Math.min(p_new_polyline.plalinelen(), polyline.plalinelen());
      
      int index_of_first_different_line = last_index;

      for (int index = 0; index < last_index; ++index)
         {
         if (p_new_polyline.plaline(index) != polyline.plaline(index) )
            {
            index_of_first_different_line = index;
            break;
            }
         }
      
      // both polylines are equal, no change nessesary, does this ever happen ?
      if (index_of_first_different_line == last_index)  return; 

      // look for the last line in p_new_polyline different from the lines of the existung trace
      int index_of_last_different_line = -1;

      for (int index = 1; index <= last_index; ++index)
         {
         if (p_new_polyline.plaline(p_new_polyline.plalinelen(-index)) != polyline.plaline(polyline.plalinelen(-index)))
            {
            index_of_last_different_line = p_new_polyline.plalinelen() - index;
            break;
            }
         }
      
      /// both polylines are equal, no change nessesary      
      if ( index_of_last_different_line < 0) return; 
      
      int keep_at_start_count = Math.max(index_of_first_different_line - 2, 0);
      int keep_at_end_count = Math.max(p_new_polyline.plalinelen() - index_of_last_different_line - 3, 0);
      r_board.search_tree_manager.change_entries(this, p_new_polyline, keep_at_start_count, keep_at_end_count);
      polyline = p_new_polyline;

      // let the observers syncronize the changes
      r_board.observers.notify_changed(this);

      ShapeTileOctagon clip_shape = null;

      BrdChangedArea changed_area = r_board.changed_area;

      if (changed_area != null)
         {
         clip_shape = changed_area.get_area(get_layer());
         }

      normalize_recu(clip_shape, 11);
      }

   /**
    * checks, that the connection restrictions to the contact pins are satisfied. 
    * If p_at_start, the start of this trace is checked, else the end. 
    * @return false, if a pin is at that end, where the connection is checked and the connection is not ok.
    */
   private boolean check_connection_to_pin(boolean p_at_start)
      {
      if (corner_count() < 2) return true;

      Collection<BrdItem> contact_list;

      if (p_at_start)
         contact_list = get_start_contacts();
      else
         contact_list = get_end_contacts();
      
      BrdAbitPin contact_pin = null;
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdAbitPin)
            {
            contact_pin = (BrdAbitPin) curr_contact;
            break;
            }
         }
      
      if (contact_pin == null) return true;

      Collection<BrdTraceExitRestriction> trace_exit_restrictions = contact_pin.get_trace_exit_restrictions(get_layer());

      if (trace_exit_restrictions.isEmpty()) return true;

      PlaPointInt end_corner;
      PlaPointInt prev_end_corner;
      
      if (p_at_start)
         {
         // As far as the use is concerned, we are not looking for a perfect match
         end_corner = corner_first().round();
         prev_end_corner = polyline.corner_first_next().round();
         }
      else
         {
         // As far as the use is concerned, we are not looking for a perfect match
         end_corner = corner_last().round();
         prev_end_corner = polyline.corner_last_prev().round();
         }
      
      PlaDirection trace_end_direction = new PlaDirection(prev_end_corner, end_corner);
      
      if ( trace_end_direction.is_NaN() ) return true;
      
      BrdTraceExitRestriction matching_exit_restriction = null;
      for (BrdTraceExitRestriction curr_exit_restriction : trace_exit_restrictions)
         {
         if (curr_exit_restriction.direction.equals(trace_end_direction))
            {
            matching_exit_restriction = curr_exit_restriction;
            break;
            }
         }

      if (matching_exit_restriction == null) return false;

      final double edge_to_turn_dist = r_board.brd_rules.get_pin_edge_to_turn_dist();
      
      if (edge_to_turn_dist < 0) return false;

      double end_line_length = end_corner.distance(prev_end_corner);
      
      double curr_clearance = r_board.get_clearance(clearance_idx(), contact_pin.clearance_idx(), get_layer());
      double add_width = Math.max(edge_to_turn_dist, curr_clearance + 1);
      double preserve_length = matching_exit_restriction.min_length + get_half_width() + add_width;

      return preserve_length <= end_line_length;
      }

   /**
    * Tries to correct a connection restriction of this trace.
    * If p_at_start, the start of the trace polygon is corrected, else the end. 
    * @return true if this trace was changed.
    */
   public boolean correct_connection_to_pin(boolean p_at_start, TraceAngleRestriction p_angle_restriction)
      {
      if ( check_connection_to_pin(p_at_start)) return false;

      Polyline trace_polyline;
      Collection<BrdItem> contact_list;
      if (p_at_start)
         {
         trace_polyline = polyline();
         contact_list = get_start_contacts();
         }
      else
         {
         trace_polyline = polyline().reverse();
         contact_list = get_end_contacts();
         }
      
      BrdAbitPin contact_pin = null;
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdAbitPin)
            {
            contact_pin = (BrdAbitPin) curr_contact;
            break;
            }
         }
      
      if (contact_pin == null) return false;
      
      Collection<BrdTraceExitRestriction> trace_exit_restrictions = contact_pin.get_trace_exit_restrictions( get_layer());

      if (trace_exit_restrictions.isEmpty()) return false;
      
      PlaShape pin_shape = contact_pin.get_shape(get_layer() - contact_pin.first_layer());

      if (!(pin_shape instanceof ShapeTile)) return false;
      
      ShapeTile a_pin_tile = (ShapeTile)pin_shape;
      
      PlaPointInt pin_center = contact_pin.center_get();

      final double edge_to_turn_dist = r_board.brd_rules.get_pin_edge_to_turn_dist();

      // TODO should this be <= 0 and not just < ? as it is now it is mostly never done
      if (edge_to_turn_dist < 0) return false;

      double curr_clearance = r_board.get_clearance(clearance_idx(), contact_pin.clearance_idx(), get_layer());
      
      double add_width = Math.max(edge_to_turn_dist, curr_clearance + 1);
      
      ShapeTile offset_pin_shape = (ShapeTile)a_pin_tile.offset(get_half_width() + add_width);
      
      if (p_angle_restriction.is_limit_90() || offset_pin_shape.is_IntBox())
         {
         offset_pin_shape = offset_pin_shape.bounding_box();
         }
      else if (p_angle_restriction.is_limit_45() )
         {
         offset_pin_shape = offset_pin_shape.bounding_octagon();
         }
      
      ArrayList<PlaToupleInt> entries = offset_pin_shape.entrance_points(trace_polyline);
      
      if (entries.size() == 0) return false;

      PlaToupleInt latest_entry_tuple = entries.get(entries.size() - 1);
      
      PlaPointFloat trace_entry_location_approx = trace_polyline.plaline(latest_entry_tuple.v_a).intersection_approx(offset_pin_shape.border_line(latest_entry_tuple.v_b));

      if ( trace_entry_location_approx.is_NaN() ) return false;
      
      // calculate the nearest legal pin exit point to trace_entry_location_approx
      double min_exit_corner_distance = Double.MAX_VALUE;
      PlaLineInt nearest_pin_exit_ray = null;
      int nearest_border_line_no = -1;
      PlaDirection pin_exit_direction = null;
      PlaPointFloat nearest_exit_corner = null;
      final double TOLERANCE = 1;
      for (BrdTraceExitRestriction curr_exit_restriction : trace_exit_restrictions)
         {
         int curr_intersecting_border_line_no = offset_pin_shape.intersecting_border_line_no(pin_center, curr_exit_restriction.direction);
         PlaLineInt curr_pin_exit_ray = new PlaLineInt(pin_center, curr_exit_restriction.direction);
         PlaPointFloat curr_exit_corner = curr_pin_exit_ray.intersection_approx(offset_pin_shape.border_line(curr_intersecting_border_line_no));
         double curr_exit_corner_distance = curr_exit_corner.dustance_square(trace_entry_location_approx);
         boolean new_nearest_corner_found = false;
         if (curr_exit_corner_distance + TOLERANCE < min_exit_corner_distance)
            {
            new_nearest_corner_found = true;
            }
         else if (curr_exit_corner_distance < min_exit_corner_distance + TOLERANCE)
            {
            // the distances are near equal, compare to the previous corners of p_trace_polyline
            for (int index = 1; index < trace_polyline.corner_count(); ++index)
               {
               PlaPointFloat curr_trace_corner = trace_polyline.corner_approx(index);
               double curr_trace_corner_distance = curr_trace_corner.dustance_square(curr_exit_corner);
               double old_trace_corner_distance = curr_trace_corner.dustance_square(nearest_exit_corner);
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
            nearest_pin_exit_ray = curr_pin_exit_ray;
            nearest_border_line_no = curr_intersecting_border_line_no;
            pin_exit_direction = curr_exit_restriction.direction;
            nearest_exit_corner = curr_exit_corner;
            }
         }

      // append the polygon piece around the border of the pin shape.

      PlaLineInt[] curr_lines;

      int corner_count = offset_pin_shape.border_line_count();
      int clock_wise_side_diff = (nearest_border_line_no - latest_entry_tuple.v_b + corner_count) % corner_count;
      int counter_clock_wise_side_diff = (latest_entry_tuple.v_b - nearest_border_line_no + corner_count) % corner_count;
      int curr_border_line_no = nearest_border_line_no;
      
      if (counter_clock_wise_side_diff <= clock_wise_side_diff)
         {
         curr_lines = new PlaLineInt[counter_clock_wise_side_diff + 3];
         for (int index = 0; index <= counter_clock_wise_side_diff; ++index)
            {
            curr_lines[index + 1] = offset_pin_shape.border_line(curr_border_line_no);
            curr_border_line_no = (curr_border_line_no + 1) % corner_count;
            }
         }
      else
         {
         curr_lines = new PlaLineInt[clock_wise_side_diff + 3];
         for (int index = 0; index <= clock_wise_side_diff; ++index)
            {
            curr_lines[index + 1] = offset_pin_shape.border_line(curr_border_line_no);
            curr_border_line_no = (curr_border_line_no - 1 + corner_count) % corner_count;
            }
         }
      curr_lines[0] = nearest_pin_exit_ray;
      curr_lines[curr_lines.length - 1] = trace_polyline.plaline(latest_entry_tuple.v_a);

      Polyline border_polyline = new Polyline(curr_lines);
      
      if (!r_board.check_polyline_trace(border_polyline, get_layer(), get_half_width(), net_nos, clearance_idx()))
         {
         return false;
         }

      PlaLineInt[] cut_lines = new PlaLineInt[trace_polyline.plalinelen( - latest_entry_tuple.v_a + 1)];
      cut_lines[0] = curr_lines[curr_lines.length - 2];
      
      for (int index = 1; index < cut_lines.length; ++index)
         {
         cut_lines[index] = trace_polyline.plaline(latest_entry_tuple.v_a + index - 1);
         }

      Polyline cut_polyline = new Polyline(cut_lines);
      Polyline changed_polyline;
      if (cut_polyline.corner_first().equals(cut_polyline.corner_last()))
         {
         changed_polyline = border_polyline;
         }
      else
         {
         changed_polyline = border_polyline.combine(cut_polyline);
         }
      if (!p_at_start)
         {
         changed_polyline = changed_polyline.reverse();
         }
      
      change(changed_polyline);

      // create an shove_fixed exit line. Interesting...
      
      PlaLineIntAlist s_lines = new PlaLineIntAlist(3);
      
      s_lines.add( new PlaLineInt(pin_center, pin_exit_direction.turn_45_degree(2)));
      s_lines.add( nearest_pin_exit_ray );
      s_lines.add( offset_pin_shape.border_line(nearest_border_line_no) );

      Polyline exit_line_segment = new Polyline(s_lines);
      
      r_board.insert_trace(exit_line_segment, get_layer(), get_half_width(), net_nos, clearance_idx(), ItemFixState.SHOVE_FIXED);
      
      return true;
      }

   /**
    * Looks, if an other pin connection restriction fits better than the current connection restriction 
    * changes this trace in this case. 
    * If p_at_start, the start of the trace polygon is changed, else the end. 
    * @returns true, if this trace was changed.
    */
   public boolean swap_connection_to_pin(boolean p_at_start)
      {
      Polyline trace_polyline;
      Collection<BrdItem> contact_list;

      if (p_at_start)
         {
         trace_polyline = polyline();
         contact_list = get_start_contacts();
         }
      else
         {
         trace_polyline = polyline().reverse();
         contact_list = get_end_contacts();
         }
      
      
      if (contact_list.size() != 1) return false;

      BrdItem curr_contact = contact_list.iterator().next();
      
      if (!(curr_contact.get_fixed_state() == ItemFixState.SHOVE_FIXED && (curr_contact instanceof BrdTracep)))
         {
         return false;
         }
      
      BrdTracep contact_trace = (BrdTracep) curr_contact;
      Polyline contact_polyline = contact_trace.polyline();
      
      PlaLineInt contact_last_line = contact_polyline.plaline_last_prev();
      
      // look, if this trace has a sharp angle with the contact trace.
      PlaLineInt first_line = trace_polyline.plaline_first_next();
      
      // check for sharp angle
      boolean check_swap = contact_last_line.direction().projection(first_line.direction()) == Signum.NEGATIVE;
      
      if (!check_swap)
         {
         double half_width = get_half_width();
         if (trace_polyline.plalinelen() > 3 && trace_polyline.corner_approx(0).dustance_square(trace_polyline.corner_approx(1)) <= half_width * half_width)
            {
            // check also for sharp angle with the second line
            check_swap = (contact_last_line.direction().projection(trace_polyline.plaline(2).direction()) == Signum.NEGATIVE);
            }
         }
      
      if (!check_swap) return false;

      BrdAbitPin contact_pin = null;
      
      Collection<BrdItem> curr_contacts = contact_trace.get_start_contacts();
      
      for (BrdItem tmp_contact : curr_contacts)
         {
         if (tmp_contact instanceof BrdAbitPin)
            {
            contact_pin = (BrdAbitPin) tmp_contact;
            break;
            }
         }
      
      if (contact_pin == null) return false;

      Polyline combined_polyline = contact_polyline.combine(trace_polyline);
      
      PlaDirection nearest_pin_exit_direction = contact_pin.calc_nearest_exit_restriction_direction(combined_polyline, get_half_width(), get_layer());
      
      if (nearest_pin_exit_direction == null || nearest_pin_exit_direction.equals(contact_polyline.plaline_first_next().direction()))
         {
         return false; // direction would not be changed
         }
      
      contact_trace.set_fixed_state(get_fixed_state());
      
      combine(20);
      
      return true;
      }
   
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(200);
      risul.append(classname);
      risul.append("trace l=");
      risul.append((int)get_length());
      risul.append(" id=");
      risul.append(get_id_no());

      return risul.toString();
      }

   }
