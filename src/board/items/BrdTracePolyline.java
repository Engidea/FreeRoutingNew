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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import board.RoutingBoard;
import board.algo.AlgoPullTight;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeEntry;
import board.varie.BrdChangedArea;
import board.varie.BrdTraceExitRestriction;
import board.varie.ItemFixState;
import board.varie.TraceAngleRestriction;
import freert.graphics.GdiContext;
import freert.planar.PlaDirection;
import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSegmentInt;
import freert.planar.PlaShape;
import freert.planar.PlaVectorInt;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.planar.ShapeTileOctagon;
import freert.varie.Signum;
import freert.varie.ThreadStoppable;
import freert.varie.TimeLimitStoppable;

/**
 * Objects of class Trace, whose geometry is described by a Polyline
 * @author Alfons Wirtz
 */
public final class BrdTracePolyline extends BrdTrace implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   
   private Polyline polyline;   // the actual line of the trace

   public BrdTracePolyline(Polyline p_polyline, int p_layer, int p_half_width, int[] p_net_no_arr, int p_clearance_type, int p_id_no, int p_group_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(p_layer, p_half_width, p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);

      if ( ! p_polyline.is_valid())
         throw new IllegalArgumentException("PolylineTrace: p_polyline.arr.length >= 3 expected");
      
      polyline = p_polyline;
      }

   @Override
   public BrdItem copy(int p_id_no)
      {
      int[] curr_net_no_arr = new int[net_count()];

      for (int index = 0; index < curr_net_no_arr.length; ++index)
         {
         curr_net_no_arr[index] = get_net_no(index);
         }
      
      return new BrdTracePolyline(polyline, get_layer(), get_half_width(), curr_net_no_arr, clearance_class_no(), p_id_no, get_component_no(), get_fixed_state(), r_board);
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
    * returns the first corner of this trace, which is the intersection of the first and second lines of its polyline
    */
   @Override
   public PlaPoint first_corner()
      {
      return polyline.corner_first();
      }

   /**
    * returns the last corner of this trace, which is the intersection of the last two lines of its polyline
    */
   @Override
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

   @Override
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
      if (p_graphics_context == null)
         {
         return;
         }
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
    * Looks, if other traces can be combined with this trace. Returns true, if
    * Something has been combined. This trace will be the combined trace, so that
    * only other traces may be deleted.
    */
   @Override
   public boolean combine()
      {
      if (! is_on_the_board()) return false;

      boolean something_changed;
      if (combine_at_start(true))
         {
         something_changed = true;
         combine();
         }
      else if (combine_at_end(true))
         {
         something_changed = true;
         combine();
         }
      else
         {
         something_changed = false;
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
    * Returns true, if somthing was combined. 
    * The corners of the other trace will be inserted in front of thie trace. 
    * In case of combine the other trace will be deleted and this trace will remain.
    */
   private boolean combine_at_start(boolean p_ignore_areas)
      {
      PlaPoint start_corner = first_corner();
      Collection<BrdItem> contacts = get_normal_contacts(start_corner, false);
      if (p_ignore_areas)
         {
         // remove conduction areas from the list
         Iterator<BrdItem> it = contacts.iterator();
         while (it.hasNext())
            {
            if (it.next() instanceof BrdAreaConduction)
               {
               it.remove();
               }
            }
         }
      if (contacts.size() != 1)
         {
         return false;
         }
      BrdTracePolyline other_trace = null;
      boolean trace_found = false;
      boolean reverse_order = false;
      Iterator<BrdItem> it = contacts.iterator();
      while (it.hasNext())
         {
         BrdItem curr_ob = it.next();
         if (curr_ob instanceof BrdTracePolyline)
            {
            other_trace = (BrdTracePolyline) curr_ob;
            if (other_trace.get_layer() == get_layer() && other_trace.nets_equal(this) && other_trace.get_half_width() == get_half_width() && other_trace.get_fixed_state() == get_fixed_state())
               {
               if (start_corner.equals(other_trace.corner_last()))
                  {
                  trace_found = true;
                  break;
                  }
               else if (start_corner.equals(other_trace.first_corner()))
                  {
                  reverse_order = true;
                  trace_found = true;
                  break;
                  }
               }
            }
         }
      if (!trace_found)
         {
         return false;
         }

      r_board.item_list.save_for_undo(this);

      // create the lines of the joined polyline
      Polyline other_lines;

      if (reverse_order)
         other_lines = other_trace.polyline.reverse();
      else
         other_lines = other_trace.polyline;
      
      boolean skip_line = other_lines.plaline(other_lines.plalinelen(-2)).is_equal_or_opposite(polyline.plaline(1));
      int new_line_count = polyline.plalinelen() + other_lines.plalinelen() - 2;
      if (skip_line)
         {
         --new_line_count;
         }
      PlaLineInt[] new_lines = new PlaLineInt[new_line_count];
      other_lines.plaline_copy(0, new_lines, 0, other_lines.plalinelen(-1));
      int join_pos = other_lines.plalinelen(-1);
      if (skip_line)
         {
         --join_pos;
         }
      
      polyline.plaline_copy(1, new_lines, join_pos, polyline.plalinelen(-1));
      Polyline joined_polyline = new Polyline(new_lines);
      if (joined_polyline.plalinelen() != new_line_count)
         {
         // consecutive parallel lines where skipped at the join location
         // combine without performance optimation
         r_board.search_tree_manager.remove(this);
         polyline = joined_polyline;
         clear_derived_data();
         r_board.search_tree_manager.insert(this);
         }
      else
         {
         // reuse the tree entries for better performance
         // create the changed line shape at the join location
         int to_no = other_lines.plalinelen();
         if (skip_line)
            {
            --to_no;
            }
         r_board.search_tree_manager.merge_entries_in_front(other_trace, this, joined_polyline, other_lines.plalinelen(-3), to_no);
         other_trace.clear_search_tree_entries();
         polyline = joined_polyline;
         }
      if ( ! polyline.is_valid() )
         {
         r_board.remove_item(this);
         }
      r_board.remove_item(other_trace);

      r_board.join_changed_area(start_corner.to_float(), get_layer());

      return true;
      }

   /**
    * looks, if this trace can be combined at its last point with another trace.
    * Returns true, if somthing was combined. The corners of the other trace
    * will be inserted at the end of thie trace. In case of combine the other
    * trace will be deleted and this trace will remain.
    */
   private boolean combine_at_end(boolean p_ignore_areas)
      {
      PlaPoint end_corner = corner_last();
      Collection<BrdItem> contacts = get_normal_contacts(end_corner, false);
      if (p_ignore_areas)
         {
         // remove conduction areas from the list
         Iterator<BrdItem> it = contacts.iterator();
         while (it.hasNext())
            {
            if (it.next() instanceof BrdAreaConduction)
               {
               it.remove();
               }
            }
         }
      if (contacts.size() != 1)
         {
         return false;
         }
      BrdTracePolyline other_trace = null;
      boolean trace_found = false;
      boolean reverse_order = false;
      Iterator<BrdItem> it = contacts.iterator();
      while (it.hasNext())
         {
         BrdItem curr_ob = it.next();
         if (curr_ob instanceof BrdTracePolyline)
            {
            other_trace = (BrdTracePolyline) curr_ob;
            if (other_trace.get_layer() == get_layer() && other_trace.nets_equal(this) && other_trace.get_half_width() == get_half_width() && other_trace.get_fixed_state() == get_fixed_state())
               {
               if (end_corner.equals(other_trace.first_corner()))
                  {
                  trace_found = true;
                  break;
                  }
               else if (end_corner.equals(other_trace.corner_last()))
                  {
                  reverse_order = true;
                  trace_found = true;
                  break;
                  }
               }
            }
         }
      if (!trace_found)
         {
         return false;
         }

      r_board.item_list.save_for_undo(this);
      
      // create the lines of the joined polyline

      Polyline other_poly;
      
      if (reverse_order)
         {
         other_poly = other_trace.polyline.reverse();
         }
      else
         {
         other_poly = other_trace.polyline;
         }
      
      boolean skip_line = polyline.plaline(polyline.plalinelen(-2)).is_equal_or_opposite(other_poly.plaline(1));
      
      int new_line_count = polyline.plalinelen() + other_poly.plalinelen() - 2;
      
      if (skip_line)
         {
         --new_line_count;
         }
      
      PlaLineInt[] new_lines = new PlaLineInt[new_line_count];
      polyline.plaline_copy( 0, new_lines, 0, polyline.plalinelen(-1));
      int join_pos = polyline.plalinelen(-1);
      
      if (skip_line)
         {
         --join_pos;
         }
      
      other_poly.plaline_copy( 1, new_lines, join_pos, other_poly.plalinelen(-1));
      
      Polyline joined_polyline = new Polyline(new_lines);
      
      if (joined_polyline.plalinelen() != new_line_count)
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
         if (skip_line)
            {
            --to_no;
            }
         r_board.search_tree_manager.merge_entries_at_end(other_trace, this, joined_polyline, polyline.plalinelen(-3), to_no);
         other_trace.clear_search_tree_entries();
         polyline = joined_polyline;
         }
      
      if ( ! polyline.is_valid() )
         {
         r_board.remove_item(this);
         }
      
      r_board.remove_item(other_trace);

      r_board.join_changed_area(end_corner.to_float(), get_layer());

      return true;
      }

   /**
    * Looks up traces intersecting with this trace and splits them at the
    * intersection points. In case of an overlaps, the traces are split at their
    * first and their last common point. Returns the pieces resulting from
    * splitting. Found cycles are removed. If nothing is split, the result will
    * contain just this Trace. If p_clip_shape != null, the split may be
    * resticted to p_clip_shape.
    */
   public Collection<BrdTracePolyline> split(ShapeTileOctagon p_clip_shape)
      {
      Collection<BrdTracePolyline> result = new LinkedList<BrdTracePolyline>();
      if (!is_nets_normal())
         {
         // only normal nets are split
         result.add(this);
         return result;
         }
      
      boolean own_trace_split = false;
      ShapeSearchTree default_tree = r_board.search_tree_manager.get_default_tree();
      for (int index = 0; index < polyline.plalinelen(-2); ++index)
         {
         if (p_clip_shape != null)
            {
            PlaSegmentInt curr_segment = new PlaSegmentInt(polyline, index + 1);
            if (!p_clip_shape.intersects(curr_segment.bounding_box()))
               {
               continue;
               }
            }
         ShapeTile curr_shape = get_tree_shape(default_tree, index);
         
         PlaSegmentInt curr_line_segment = new PlaSegmentInt(polyline, index + 1);
         
         Collection<ShapeTreeEntry> overlapping_tree_entries = new LinkedList<ShapeTreeEntry>();
         // look for intersecting traces with the i-th line segment
         default_tree.calc_overlapping_tree_entries(curr_shape, get_layer(), overlapping_tree_entries);
         Iterator<ShapeTreeEntry> it = overlapping_tree_entries.iterator();
         while (it.hasNext())
            {
            if (!is_on_the_board())
               {
               // this trace has been deleted in a cleanup operation
               return result;
               }
            
            ShapeTreeEntry found_entry = it.next();
            
            if (!(found_entry.object instanceof BrdItem)) continue;
            
            BrdItem found_item = (BrdItem) found_entry.object;

            if (found_item == this)
               {
               if (found_entry.shape_index_in_object >= index - 1 && found_entry.shape_index_in_object <= index + 1)
                  {
                  // don't split own trace at this line or at neighbour lines
                  continue;
                  }

               // try to handle intermediate segments of length 0 by comparing end corners
               if (index < found_entry.shape_index_in_object)
                  {
                  if (polyline.corner(index + 1).equals(polyline.corner(found_entry.shape_index_in_object)))
                     {
                     continue;
                     }
                  }
               else if (found_entry.shape_index_in_object < index)
                  {
                  if (polyline.corner(found_entry.shape_index_in_object + 1).equals(polyline.corner(index)))
                     {
                     continue;
                     }
                  }
               }
            
            if (!found_item.shares_net(this)) continue;
            
            if (found_item instanceof BrdTracePolyline)
               {
               BrdTracePolyline found_trace = (BrdTracePolyline) found_item;
               PlaSegmentInt found_line_segment = new PlaSegmentInt(found_trace.polyline, found_entry.shape_index_in_object + 1);
               PlaLineInt[] intersecting_lines = found_line_segment.intersection(curr_line_segment);
               Collection<BrdTracePolyline> split_pieces = new LinkedList<BrdTracePolyline>();

               // try splitting the found trace first
               boolean found_trace_split = false;

               if (found_trace != this)
                  {
                  for (int jndex = 0; jndex < intersecting_lines.length; ++jndex)
                     {
                     int line_no = found_entry.shape_index_in_object + 1;
                     BrdTracePolyline[] curr_split_pieces = found_trace.split(line_no, intersecting_lines[jndex]);
                     if (curr_split_pieces != null)
                        {

                        for (int k = 0; k < 2; ++k)
                           {
                           if (curr_split_pieces[k] != null)
                              {
                              found_trace_split = true;
                              split_pieces.add(curr_split_pieces[k]);

                              }
                           }
                        if (found_trace_split)
                           {
                           // reread the overlapping tree entries and reset the iterator, because the board has changed
                           default_tree.calc_overlapping_tree_entries(curr_shape, get_layer(), overlapping_tree_entries);
                           it = overlapping_tree_entries.iterator();
                           break;
                           }
                        }
                     }
                  if (!found_trace_split)
                     {
                     split_pieces.add(found_trace);
                     }
                  }
               // now try splitting the own trace

               intersecting_lines = curr_line_segment.intersection(found_line_segment);
               
               for (int jndex = 0; jndex < intersecting_lines.length; ++jndex)
                  {
                  BrdTracePolyline[] curr_split_pieces = split(index + 1, intersecting_lines[jndex]);
                  if (curr_split_pieces != null)
                     {
                     own_trace_split = true;
                     // this trace was split itself into 2.
                     if (curr_split_pieces[0] != null)
                        {
                        result.addAll(curr_split_pieces[0].split(p_clip_shape));
                        }
                     if (curr_split_pieces[1] != null)
                        {
                        result.addAll(curr_split_pieces[1].split(p_clip_shape));
                        }
                     break;
                     }
                  }
               if (found_trace_split || own_trace_split)
                  {
                  // something was split,
                  // remove cycles containing a split piece
                  Iterator<BrdTracePolyline> it2 = split_pieces.iterator();
                  for (int j = 0; j < 2; ++j)
                     {
                     while (it2.hasNext())
                        {
                        BrdTracePolyline curr_piece = it2.next();
                        r_board.remove_if_cycle(curr_piece);
                        }

                     // remove cycles in the own split pieces last
                     // to preserve them, if possible
                     it2 = result.iterator();
                     }
                  }
               if (own_trace_split)
                  {
                  break;
                  }
               }
            else if (found_item instanceof BrdAbit)
               {
               BrdAbit curr_drill_item = (BrdAbit) found_item;
               PlaPoint split_point = curr_drill_item.center_get();
               if (curr_line_segment.contains(split_point))
                  {
                  PlaDirection split_line_direction = curr_line_segment.get_line().direction().turn_45_degree(2);
                  PlaLineInt split_line = new PlaLineInt(split_point, split_line_direction);
                  split(index + 1, split_line);
                  }
               }
            else if (!is_user_fixed() && (found_item instanceof BrdAreaConduction))
               {
               boolean ignore_areas = false;
               if (net_no_arr.length > 0)
                  {
                  freert.rules.RuleNet curr_net = r_board.brd_rules.nets.get(net_no_arr[0]);
                  if (curr_net != null && curr_net.get_class() != null)
                     {
                     ignore_areas = curr_net.get_class().get_ignore_cycles_with_areas();
                     }
                  }
               if (!ignore_areas && get_start_contacts().contains(found_item) && get_end_contacts().contains(found_item))
                  {
                  // this trace can be removed because of cycle with conduction
                  // area
                  r_board.remove_item(this);
                  return result;
                  }
               }
            }
         if (own_trace_split)
            {
            break;
            }
         }
      if (!own_trace_split)
         {
         result.add(this);
         }
      
      if (result.size() > 1)
         {
         for (BrdItem curr_item : result)
            {
            // need to clean up possible autoroute information
            curr_item.art_item_clear(); 
            }
         }
      
      return result;
      }

   /**
    * Checks, if the intersection of the p_line_no-th line of this trace with
    * p_line is inside the pad of a pin. In this case the trace will be split
    * only, if the intersection is at the center of the pin. Extending the
    * function to vias leaded to broken connection problems wenn the autorouter
    * connected to a trace.
    */
   private boolean split_inside_drill_pad_prohibited(int p_line_no, PlaLineInt p_line)
      {
      PlaPoint intersection = polyline.plaline(p_line_no).intersection(p_line);
      
      java.util.Collection<BrdItem> overlap_items = r_board.pick_items(intersection, get_layer() );
      boolean pad_found = false;
      
      for (BrdItem curr_item : overlap_items)
         {
         if (!curr_item.shares_net(this)) continue;

         if (curr_item instanceof BrdAbitPin)
            {
            BrdAbit curr_drill_item = (BrdAbit) curr_item;
            if (curr_drill_item.center_get().equals(intersection))
               {
               return false; // split always at the center of a drill item.
               }
            pad_found = true;
            }
         else if (curr_item instanceof BrdTrace)
            {
            BrdTrace curr_trace = (BrdTrace) curr_item;
            if (curr_trace != this && curr_trace.first_corner().equals(intersection) || curr_trace.corner_last().equals(intersection))
               {
               return false;
               }
            }
         }
      return pad_found;
      }

   public final BrdTrace[] split(PlaPointInt p_point)
      {
      for (int index = 0; index < polyline.plalinelen(-2); index++)
         {
         PlaSegmentInt curr_line_segment = new PlaSegmentInt(polyline, index + 1);
         
         if ( ! curr_line_segment.contains(p_point)) continue;
         
         PlaDirection split_line_direction = curr_line_segment.get_line().direction().turn_45_degree(2);

         PlaLineInt split_line = new PlaLineInt(p_point, split_line_direction);
         
         BrdTrace[] result = split(index + 1, split_line);
         
         if (result != null)  return result;
         }

      return null;
      }
   
   @Override
   public BrdTrace[] split(PlaPoint p_point)
      {
      if ( !( p_point instanceof PlaPointInt ))
         throw new IllegalArgumentException("split, only intpoints...");
      
      return split((PlaPointInt)p_point);
      }

   /**
    * Splits this trace at the line with number p_line_no into two by inserting
    * p_endline as concluding line of the first split piece and as the start
    * line of the second split piece. 
    * Returns the 2 pieces of the splitted trace, or null, if nothing was splitted.
    */
   private BrdTracePolyline[] split(int p_line_no, PlaLineInt p_new_end_line)
      {
      if (!is_on_the_board()) return null;

      Polyline[] split_polylines = polyline.split(p_line_no, p_new_end_line);

      if (split_polylines == null) return null;

      if (split_polylines.length != 2)
         {
         System.out.println("PolylineTrace.split: array of length 2 expected for split_polylines");
         return null;
         }
      
      if (split_inside_drill_pad_prohibited(p_line_no, p_new_end_line))
         {
         return null;
         }
      r_board.remove_item(this);
      BrdTracePolyline[] result = new BrdTracePolyline[2];
      result[0] = r_board.insert_trace_without_cleaning(split_polylines[0], get_layer(), get_half_width(), net_no_arr, clearance_class_no(), get_fixed_state());
      result[1] = r_board.insert_trace_without_cleaning(split_polylines[1], get_layer(), get_half_width(), net_no_arr, clearance_class_no(), get_fixed_state());
      return result;
      }

   /**
    * Splits this trace and overlapping traces, and combines this trace.
    * If p_clip_shape != null, splitting is restricted to p_clip_shape. 
    * @return true, if something was changed. 
    */
   public boolean normalize(ShapeTileOctagon p_clip_shape)
      {
      boolean observers_activated = false;

      // Let the observers know the trace changes.
      observers_activated = ! r_board.observers_active();
      
      if (observers_activated) r_board.start_notify_observers();
      
      Collection<BrdTracePolyline> split_pieces = split(p_clip_shape);
     
      boolean result =  split_pieces.size() != 1;

      Iterator<BrdTracePolyline> it = split_pieces.iterator();
      while (it.hasNext())
         {
         BrdTracePolyline curr_split_trace = it.next();
         if ( ! curr_split_trace.is_on_the_board()) continue;
         
         boolean trace_combined = curr_split_trace.combine();
         if (curr_split_trace.corner_count() == 2 && curr_split_trace.first_corner().equals(curr_split_trace.corner_last()))
            {
            // remove trace with only 1 corner
            r_board.remove_item(curr_split_trace);
            result = true;
            }
         else if (trace_combined)
            {
            curr_split_trace.normalize(p_clip_shape);
            result = true;
            }
         }
      
      if (observers_activated) r_board.end_notify_observers();

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
      
      if (p_pull_tight_algo.only_net_no_arr.length > 0 && ! nets_equal(p_pull_tight_algo.only_net_no_arr))
         {
         return false;
         }

      if ( net_no_arr.length > 0)
         {
         // why only of index 0 ?
         if (! r_board.brd_rules.nets.get( net_no_arr[0]).get_class().can_pull_tight())
            {
            return false;
            }
         }
      
      Polyline new_lines = p_pull_tight_algo.pull_tight(polyline, get_layer(), get_half_width(), net_no_arr, clearance_class_no(), touching_pins_at_end_corners());
      if (new_lines != polyline)
         {
         change(new_lines);
         return true;
         }
      
      TraceAngleRestriction angle_restriction = r_board.brd_rules.get_trace_snap_angle();
      
      if (angle_restriction != TraceAngleRestriction.NINETY_DEGREE && r_board.brd_rules.get_pin_edge_to_turn_dist() > 0)
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
   public final boolean pull_tight(boolean p_own_net_only, int p_pull_tight_accuracy, ThreadStoppable p_stoppable_thread)
      {
      int[] opt_net_no_arr;
      
      if (p_own_net_only)
         {
         opt_net_no_arr = net_no_arr;
         }
      else
         {
         opt_net_no_arr = new int[0];
         }
      
      AlgoPullTight pull_tight_algo = AlgoPullTight.get_instance(r_board, opt_net_no_arr, null, p_pull_tight_accuracy, p_stoppable_thread, null );
      
      return pull_tight(pull_tight_algo);
      }

   /**
    * Tries to smoothen the end corners of this trace, which are at a fork with other traces.
    */
   public boolean smoothen_end_corners_fork(boolean p_own_net_only, int p_pull_tight_accuracy, ThreadStoppable p_stoppable_thread)
      {
      int[] opt_net_no_arr;
      
      if (p_own_net_only)
         {
         opt_net_no_arr = net_no_arr;
         }
      else
         {
         opt_net_no_arr = new int[0];
         }
      
      AlgoPullTight pull_tight_algo = AlgoPullTight.get_instance(r_board, opt_net_no_arr, null, p_pull_tight_accuracy, p_stoppable_thread, null );

      return pull_tight_algo.smoothen_end_corners_at_trace(this);
      }

   public ShapeTile get_trace_connection_shape(ShapeSearchTree p_search_tree, int p_index)
      {
      if (p_index < 0 || p_index >= tile_shape_count())
         {
         System.out.println("PolylineTrace.get_trace_connection_shape p_index out of range");
         return null;
         }
      PlaSegmentInt curr_line_segment = new PlaSegmentInt(polyline, p_index + 1);
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
      if (! is_on_the_board())
         {
         // Just change the polyline of this trace.
         polyline = p_new_polyline;
         return;
         }

      art_item_clear(); // need to clean up possible autoroute item

      // The precalculated tile shapes must not be cleared here here because
      // they are used and modified
      // in ShapeSearchTree.change_entries.

      r_board.item_list.save_for_undo(this);

      // for performance reasons there is some effort to reuse
      // ShapeTree entries of the old trace in the changed trace

      // look for the first line in p_new_polyline different from
      // the lines of the existung trace
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
      if (index_of_first_different_line == last_index)
         {
         return; // both polylines are equal, no change nessesary
         }
      // look for the last line in p_new_polyline different from
      // the lines of the existung trace
      int index_of_last_different_line = -1;

      for (int index = 1; index <= last_index; ++index)
         {
         if (p_new_polyline.plaline(p_new_polyline.plalinelen(-index)) != polyline.plaline(polyline.plalinelen(-index)))
            {
            index_of_last_different_line = p_new_polyline.plalinelen() - index;
            break;
            }
         }
      if (index_of_last_different_line < 0)
         {
         return; // both polylines are equal, no change nessesary
         }
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

      normalize(clip_shape);
      }

   /**
    * checks, that the connection restrictions to the contact pins are
    * satisfied. If p_at_start, the start of this trace is checked, else the
    * end. Returns false, if a pin is at that end, where the connection is
    * checked and the connection is not ok.
    */
   public boolean check_connection_to_pin(boolean p_at_start)
      {
      if (corner_count() < 2)
         {
         return true;
         }
      Collection<BrdItem> contact_list;
      if (p_at_start)
         {
         contact_list = get_start_contacts();
         }
      else
         {
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
      if (contact_pin == null)
         {
         return true;
         }
      Collection<BrdTraceExitRestriction> trace_exit_restrictions = contact_pin.get_trace_exit_restrictions(get_layer());
      if (trace_exit_restrictions.isEmpty())
         {
         return true;
         }
      PlaPoint end_corner;
      PlaPoint prev_end_corner;
      if (p_at_start)
         {
         end_corner = first_corner();
         prev_end_corner = polyline.corner_first_next();
         }
      else
         {
         end_corner = corner_last();
         prev_end_corner = polyline.corner(polyline.corner_count() - 2);
         }
      
      PlaDirection trace_end_direction = PlaDirection.get_instance(end_corner, prev_end_corner);
      
      if (trace_end_direction == null) return true;
      
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

      double end_line_length = end_corner.to_float().distance(prev_end_corner.to_float());
      double curr_clearance = r_board.get_clearance(clearance_class_no(), contact_pin.clearance_class_no(), get_layer());
      double add_width = Math.max(edge_to_turn_dist, curr_clearance + 1);
      double preserve_length = matching_exit_restriction.min_length + get_half_width() + add_width;

      if (preserve_length > end_line_length) return false;

      return true;
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
      
      PlaPoint pin_center = contact_pin.center_get();

      final double edge_to_turn_dist = r_board.brd_rules.get_pin_edge_to_turn_dist();

      // TODO should this be <= 0 and not just < ? as it is now it is mostly never done
      if (edge_to_turn_dist < 0) return false;

      double curr_clearance = r_board.get_clearance(clearance_class_no(), contact_pin.clearance_class_no(), get_layer());
      
      double add_width = Math.max(edge_to_turn_dist, curr_clearance + 1);
      
      ShapeTile offset_pin_shape = (ShapeTile)a_pin_tile.offset(get_half_width() + add_width);
      
      if (p_angle_restriction == TraceAngleRestriction.NINETY_DEGREE || offset_pin_shape.is_IntBox())
         {
         offset_pin_shape = offset_pin_shape.bounding_box();
         }
      else if (p_angle_restriction == TraceAngleRestriction.FORTYFIVE_DEGREE)
         {
         offset_pin_shape = offset_pin_shape.bounding_octagon();
         }
      
      int[][] entries = offset_pin_shape.entrance_points(trace_polyline);
      
      if (entries.length == 0) return false;

      int[] latest_entry_tuple = entries[entries.length - 1];
      
      PlaPointFloat trace_entry_location_approx = trace_polyline.plaline(latest_entry_tuple[0]).intersection_approx(offset_pin_shape.border_line(latest_entry_tuple[1]));

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
         double curr_exit_corner_distance = curr_exit_corner.length_square(trace_entry_location_approx);
         boolean new_nearest_corner_found = false;
         if (curr_exit_corner_distance + TOLERANCE < min_exit_corner_distance)
            {
            new_nearest_corner_found = true;
            }
         else if (curr_exit_corner_distance < min_exit_corner_distance + TOLERANCE)
            {
            // the distances are near equal, compare to the previous corners of
            // p_trace_polyline
            for (int i = 1; i < trace_polyline.corner_count(); ++i)
               {
               PlaPointFloat curr_trace_corner = trace_polyline.corner_approx(i);
               double curr_trace_corner_distance = curr_trace_corner.length_square(curr_exit_corner);
               double old_trace_corner_distance = curr_trace_corner.length_square(nearest_exit_corner);
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
      int clock_wise_side_diff = (nearest_border_line_no - latest_entry_tuple[1] + corner_count) % corner_count;
      int counter_clock_wise_side_diff = (latest_entry_tuple[1] - nearest_border_line_no + corner_count) % corner_count;
      int curr_border_line_no = nearest_border_line_no;
      if (counter_clock_wise_side_diff <= clock_wise_side_diff)
         {
         curr_lines = new PlaLineInt[counter_clock_wise_side_diff + 3];
         for (int i = 0; i <= counter_clock_wise_side_diff; ++i)
            {
            curr_lines[i + 1] = offset_pin_shape.border_line(curr_border_line_no);
            curr_border_line_no = (curr_border_line_no + 1) % corner_count;
            }
         }
      else
         {
         curr_lines = new PlaLineInt[clock_wise_side_diff + 3];
         for (int i = 0; i <= clock_wise_side_diff; ++i)
            {
            curr_lines[i + 1] = offset_pin_shape.border_line(curr_border_line_no);
            curr_border_line_no = (curr_border_line_no - 1 + corner_count) % corner_count;
            }
         }
      curr_lines[0] = nearest_pin_exit_ray;
      curr_lines[curr_lines.length - 1] = trace_polyline.plaline(latest_entry_tuple[0]);

      Polyline border_polyline = new Polyline(curr_lines);
      if (!r_board.check_polyline_trace(border_polyline, get_layer(), get_half_width(), net_no_arr, clearance_class_no()))
         {
         return false;
         }

      PlaLineInt[] cut_lines = new PlaLineInt[trace_polyline.plalinelen( - latest_entry_tuple[0] + 1)];
      cut_lines[0] = curr_lines[curr_lines.length - 2];
      
      for (int index = 1; index < cut_lines.length; ++index)
         {
         cut_lines[index] = trace_polyline.plaline(latest_entry_tuple[0] + index - 1);
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

      // create an shove_fixed exit line.
      curr_lines = new PlaLineInt[3];
      curr_lines[0] = new PlaLineInt(pin_center, pin_exit_direction.turn_45_degree(2));
      curr_lines[1] = nearest_pin_exit_ray;
      curr_lines[2] = offset_pin_shape.border_line(nearest_border_line_no);
      Polyline exit_line_segment = new Polyline(curr_lines);
      r_board.insert_trace(exit_line_segment, get_layer(), get_half_width(), net_no_arr, clearance_class_no(), ItemFixState.SHOVE_FIXED);
      return true;
      }

   /**
    * Looks, if an other pin connection restriction fits better than the current
    * connection restriction and changes this trace in this case. If p_at_start,
    * the start of the trace polygon is changed, else the end. Returns true, if
    * this trace was changed.
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
      if (contact_list.size() != 1)
         {
         return false;
         }
      BrdItem curr_contact = contact_list.iterator().next();
      if (!(curr_contact.get_fixed_state() == ItemFixState.SHOVE_FIXED && (curr_contact instanceof BrdTracePolyline)))
         {
         return false;
         }
      BrdTracePolyline contact_trace = (BrdTracePolyline) curr_contact;
      Polyline contact_polyline = contact_trace.polyline();
      PlaLineInt contact_last_line = contact_polyline.plaline(contact_polyline.plalinelen(-2));
      // look, if this trace has a sharp angle with the contact trace.
      PlaLineInt first_line = trace_polyline.plaline(1);
      // check for sharp angle
      boolean check_swap = contact_last_line.direction().projection(first_line.direction()) == Signum.NEGATIVE;
      if (!check_swap)
         {
         double half_width = get_half_width();
         if (trace_polyline.plalinelen() > 3 && trace_polyline.corner_approx(0).length_square(trace_polyline.corner_approx(1)) <= half_width * half_width)
            {
            // check also for sharp angle with the second line
            check_swap = (contact_last_line.direction().projection(trace_polyline.plaline(2).direction()) == Signum.NEGATIVE);
            }
         }
      if (!check_swap)
         {
         return false;
         }
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
      if (contact_pin == null)
         {
         return false;
         }
      Polyline combined_polyline = contact_polyline.combine(trace_polyline);
      PlaDirection nearest_pin_exit_direction = contact_pin.calc_nearest_exit_restriction_direction(combined_polyline, get_half_width(), get_layer());
      if (nearest_pin_exit_direction == null || nearest_pin_exit_direction.equals(contact_polyline.plaline(1).direction()))
         {
         return false; // direction would not be changed
         }
      contact_trace.set_fixed_state(get_fixed_state());
      combine();
      return true;
      }
   
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(200);
      risul.append(getClass().getName());
      risul.append(" l=");
      risul.append((int)get_length());
      risul.append(" id=");
      risul.append(get_id_no());

      return risul.toString();
      }

   }
