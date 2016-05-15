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
 * PullTight90.java
 *
 * Created on 19. Juli 2003, 18:54
 */

package board.algo;

import board.RoutingBoard;
import board.items.BrdTracePolyline;
import board.varie.BrdKeepPoint;
import freert.planar.PlaLineInt;
import freert.planar.PlaPointFloat;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.varie.ThreadStoppable;

/**
 *
 * @author Alfons Wirtz
 */
public final class AlgoPullTight90 extends AlgoPullTight
   {
   public AlgoPullTight90(RoutingBoard p_board, int[] p_only_net_no_arr, ThreadStoppable p_stoppable_thread, BrdKeepPoint p_keep_point)
      {
      super(p_board, p_only_net_no_arr, p_stoppable_thread, p_keep_point );
      }

   @Override
   protected Polyline pull_tight(Polyline p_polyline)
      {
      Polyline new_result = acid_traps_wrap_around(p_polyline);
      Polyline prev_result = null;
      
      while (new_result != prev_result)
         {
         if (is_stop_requested()) break;

         prev_result = new_result;
         Polyline tmp1 = try_skip_second_corner(prev_result);
         Polyline tmp2 = try_skip_corners(tmp1);
         new_result = reposition_lines(tmp2);
         }
      return new_result;
      }

   /**
    * Tries to skip the second corner of p_polyline. Return p_polyline, if nothing was changed.
    */
   private Polyline try_skip_second_corner(Polyline p_polyline)
      {
      if (p_polyline.plalinelen() < 5)
         {
         return p_polyline;
         }
      PlaLineInt[] check_lines = new PlaLineInt[4];
      check_lines[0] = p_polyline.plaline(1);
      check_lines[1] = p_polyline.plaline(0);
      check_lines[2] = p_polyline.plaline(3);
      check_lines[3] = p_polyline.plaline(4);
      Polyline check_polyline = new Polyline(check_lines);
      if (check_polyline.plalinelen() != 4 || curr_clip_shape != null && !curr_clip_shape.contains(check_polyline.corner_approx(1)))
         {
         return p_polyline;
         }
      for (int i = 0; i < 2; ++i)
         {
         ShapeTile shape_to_check = check_polyline.offset_shape(curr_half_width, i);
         if (!r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins))
            {
            return p_polyline;
            }
         }
      // now the second corner can be skipped.
      PlaLineInt[] new_lines = new PlaLineInt[p_polyline.plalinelen(-1)];
      new_lines[0] = p_polyline.plaline(1);
      new_lines[1] = p_polyline.plaline(0);
      for (int index = 2; index < new_lines.length; ++index)
         {
         new_lines[index] = p_polyline.plaline(index + 1);
         }
      return new Polyline(new_lines);
      }

   /**
    * Tries to reduce the amount of corners of p_polyline. Return p_polyline, if nothing was changed.
    */
   private Polyline try_skip_corners(Polyline p_polyline)
      {
      PlaLineInt[] new_lines = new PlaLineInt[p_polyline.plalinelen()];
      new_lines[0] = p_polyline.plaline(0);
      new_lines[1] = p_polyline.plaline(1);
      int new_line_index = 1;
      boolean polyline_changed = false;
      PlaLineInt[] check_lines = new PlaLineInt[4];
      boolean second_last_corner_skipped = false;
      for (int index = 5; index <= p_polyline.plalinelen(); ++index)
         {
         boolean skip_lines = false;
         boolean in_clip_shape = curr_clip_shape == null || curr_clip_shape.contains(p_polyline.corner_approx(index - 3));
         if (in_clip_shape)
            {
            check_lines[0] = new_lines[new_line_index - 1];
            check_lines[1] = new_lines[new_line_index];
            check_lines[2] = p_polyline.plaline(index - 1);
            if (index < p_polyline.plalinelen())
               {
               check_lines[3] = p_polyline.plaline(index);
               }
            else
               {
               // use as concluding line the second last line
               check_lines[3] = p_polyline.plaline(index - 2);
               }
            Polyline check_polyline = new Polyline(check_lines);
            skip_lines = check_polyline.plalinelen() == 4 && (curr_clip_shape == null || curr_clip_shape.contains(check_polyline.corner_approx(1)));
            if (skip_lines)
               {
               ShapeTile shape_to_check = check_polyline.offset_shape(curr_half_width, 0);
               skip_lines = r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins);
               }
            if (skip_lines)
               {
               ShapeTile shape_to_check = check_polyline.offset_shape(curr_half_width, 1);
               skip_lines = r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins);
               }
            }
         if (skip_lines)
            {
            if (index == p_polyline.plalinelen())
               {
               second_last_corner_skipped = true;
               }
            
            if (r_board.changed_area != null)
               {
               PlaPointFloat new_corner = check_lines[1].intersection_approx(check_lines[2]);
               if ( ! new_corner.is_NaN() ) r_board.changed_area.join(new_corner, curr_layer);
               
               PlaPointFloat skipped_corner = p_polyline.plaline(index - 2).intersection_approx(p_polyline.plaline(index - 3));
               if ( ! skipped_corner.is_NaN() ) r_board.changed_area.join(skipped_corner, curr_layer);
               }
            polyline_changed = true;
            ++index;
            }
         else
            {
            ++new_line_index;
            new_lines[new_line_index] = p_polyline.plaline(index - 3);
            }
         }
      if (!polyline_changed)
         {
         return p_polyline;
         }
      if (second_last_corner_skipped)
         {
         // The second last corner of p_polyline was skipped
         ++new_line_index;
         new_lines[new_line_index] = p_polyline.plaline(p_polyline.plalinelen(-1));
         ++new_line_index;
         new_lines[new_line_index] = p_polyline.plaline(p_polyline.plalinelen(-2));
         }
      else
         {
         for (int index = 3; index > 0; --index)
            {
            ++new_line_index;
            new_lines[new_line_index] = p_polyline.plaline(p_polyline.plalinelen(-index));
            }
         }

      PlaLineInt[] cleaned_new_lines = new PlaLineInt[new_line_index + 1];
      System.arraycopy(new_lines, 0, cleaned_new_lines, 0, cleaned_new_lines.length);
      Polyline result = new Polyline(cleaned_new_lines);
      return result;
      }

   @Override
   protected Polyline smoothen_start_corner_at_trace(BrdTracePolyline p_trace)
      {
      return null;
      }

   @Override
   protected Polyline smoothen_end_corner_at_trace(BrdTracePolyline p_trace)
      {
      return null;
      }
   }
