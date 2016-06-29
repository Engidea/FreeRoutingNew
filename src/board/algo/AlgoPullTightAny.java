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

package board.algo;

import java.util.Collection;
import java.util.TreeSet;
import board.RoutingBoard;
import board.items.BrdItem;
import board.items.BrdTracep;
import board.varie.BrdKeepPoint;
import freert.planar.PlaDirection;
import freert.planar.PlaLimits;
import freert.planar.PlaLineInt;
import freert.planar.PlaLineIntAlist;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSide;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.varie.NetNosList;
import freert.varie.Signum;
import freert.varie.ThreadStoppable;

/**
 * Auxiliary class containing internal functions for pulling any angle traces tight.
 * 
 * @author Alfons Wirtz
 */

public final class AlgoPullTightAny extends AlgoPullTight
   {
   private static final double SMOOTH_LENGTH = 10.0;
   
   // with angles to close to 180 degree the algorithm becomes numerically unstable
   private static final double COS_ANGLE_MAX = 0.999;
   
   public AlgoPullTightAny(
         RoutingBoard p_board, 
         NetNosList p_only_net_no_arr, 
         ThreadStoppable p_stoppable_thread, 
         BrdKeepPoint p_keep_point,
         int p_min_move_dist)
      {
      super(p_board, p_only_net_no_arr, p_stoppable_thread, p_keep_point, p_min_move_dist);
      }

   /**
    * This may loop forever, can be interrupted, but should not, really
    */
   @Override
   protected Polyline pull_tight(Polyline p_polyline)
      {
      Polyline new_result = acid_traps_wrap_around(p_polyline);
      
      Polyline prev_result = null;
      
      while (new_result != prev_result)
         {
         if (is_stop_requested()) break;
         
         prev_result = new_result;
         
         new_result = skip_segments_of_length_0(prev_result);
         new_result = reduce_lines(new_result);
         new_result = skip_lines(new_result);

         // I intended to replace reduce_corners by the previous 2 functions, because with consecutive corners closer than
         // 1 grid point reduce_corners may loop with smoothen_corners because of changing directions heavily.
         // Unlike reduce_corners, the above 2 functions do not introduce new directions

//         new_result = reduce_corners(new_result);
//         new_result = reduce_rationals(new_result);
         new_result = reposition_lines(new_result);
         new_result = smoothen_corners(new_result);
         }
      
      return new_result;
      }


   
   
   

   /**
    * tries to smoothen p_polyline by cutting of corners, if possible
    */
   private Polyline smoothen_corners(Polyline p_polyline)
      {
      if (p_polyline.plaline_len() < 4) return p_polyline;

      boolean polyline_changed = false;

      // create a storage with extra capacity
      PlaLineIntAlist line_arr = p_polyline.alist_copy(10);
      
      for (int index = 0; index < line_arr.size(-3); ++index)
         {
         PlaLineInt new_line = smoothen_corner(line_arr, index);
         
         if (new_line == null) continue;

         polyline_changed = true;
         
         line_arr.add(index + 2,new_line);
         
         ++index;
         }

      if ( ! polyline_changed ) return p_polyline;
      
      return new Polyline(line_arr);
      }

   @Override
   protected Polyline reposition_lines(Polyline p_polyline)
      {
      if (p_polyline.plaline_len() < 5) return p_polyline;

      boolean polyline_changed = false;
      
      PlaLineInt[] line_arr = p_polyline.alist_to_array();
      
      for (int index = 0; index < line_arr.length - 4; ++index)
         {
         PlaLineInt new_line = reposition_line(line_arr, index);
         
         if (new_line == null) continue;

         polyline_changed = true;
         
         line_arr[index + 2] = new_line;
         
         if (line_arr[index + 2].is_parallel(line_arr[index + 1]) || line_arr[index + 2].is_parallel(line_arr[index + 3]))
            {
            // calculation of corners not possible before skipping parallel lines
            break;
            }
         }
      
      if ( polyline_changed ) return new Polyline(line_arr);
      
      return p_polyline;
      }

   /**
    * tries to reduce the number of lines of p_polyline by moving lines parallel beyond the intersection of the next or previous lines.
    */
   private Polyline reduce_lines(Polyline p_polyline)
      {
      if (p_polyline.plaline_len() < 6) return p_polyline;

      for (int index = 2; index < p_polyline.plaline_len(-2); ++index)
         {
         PlaPointFloat prev_corner = p_polyline.corner_approx(index-2);
         
         // surely if a corner is a NaN it cannot be in the clip shape
         if ( prev_corner.is_NaN() ) continue;
         
         PlaPointFloat next_corner = p_polyline.corner_approx(index+1);

         if ( next_corner.is_NaN() ) continue;

         PlaLineInt translate_line = p_polyline.plaline(index);
         
         double prev_dist = translate_line.distance_signed(prev_corner);
         double next_dist = translate_line.distance_signed(next_corner);
         
         // the 2 corners are on different sides of the translate_line
         if (Signum.of(prev_dist) != Signum.of(next_dist))  continue;

         double translate_dist;
         if (Math.abs(prev_dist) < Math.abs(next_dist))
            {
            translate_dist = prev_dist;
            }
         else
            {
            translate_dist = next_dist;
            }
         
         // line segment may have length 0
         if (translate_dist == 0)  continue;

         PlaSide line_side = translate_line.side_of(prev_corner);
         PlaLineInt new_line = translate_line.translate(-translate_dist);
         // make shure, we have crossed the nearest_corner;
         int sign = Signum.as_int(translate_dist);
         PlaSide new_line_side_of_prev_corner = new_line.side_of(prev_corner);
         PlaSide new_line_side_of_next_corner = new_line.side_of(next_corner);
         while (new_line_side_of_prev_corner == line_side && new_line_side_of_next_corner == line_side)
            {
            translate_dist += sign * 0.5;
            new_line = translate_line.translate(-translate_dist);
            new_line_side_of_prev_corner = new_line.side_of(prev_corner);
            new_line_side_of_next_corner = new_line.side_of(next_corner);
            }
         
         int crossed_corners_before_count = 0;
         int crossed_corners_after_count = 0;
         
         if (new_line_side_of_prev_corner != line_side) crossed_corners_before_count++;
         
         if (new_line_side_of_next_corner != line_side) crossed_corners_after_count++;

         // check, that we havent crossed both corners
         if (crossed_corners_before_count > 1 || crossed_corners_after_count > 1)
            {
            continue;
            }
         
         // check, that next_nearest_corner and nearest_corner are on different sides of new_line;
         if (crossed_corners_before_count > 0)
            {
            if (index < 3) continue;
            
            PlaPointFloat prev_prev_corner = p_polyline.corner_approx(index - 3);

            if (new_line.side_of(prev_prev_corner) != line_side) continue;
            }

         if (crossed_corners_after_count > 0)
            {
            if (index >= p_polyline.plaline_len(-3) ) continue;

            PlaPointFloat next_next_corner = p_polyline.corner_approx(index + 2);

            if (new_line.side_of(next_next_corner) != line_side) continue;
            }
         
         PlaLineIntAlist curr_lines = new PlaLineIntAlist(p_polyline.plaline_len());
         
         int keep_before_ind = index - crossed_corners_before_count;

         p_polyline.alist_append_to(curr_lines, 0, keep_before_ind);
         
         curr_lines.add(new_line);
         
         p_polyline.alist_append_to(curr_lines, index + 1 + crossed_corners_after_count );
         
         Polyline try_poly = new Polyline(curr_lines);

         boolean check_ok = false;
         
         if (try_poly.plaline_len() == curr_lines.size())
            {
            ShapeTile shape_to_check = try_poly.offset_shape(curr_half_width, keep_before_ind - 1);
            check_ok = r_board.check_trace(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins);
            }
         
         if ( ! check_ok) continue;
         
         r_board.changed_area.join(prev_corner, curr_layer);
         r_board.changed_area.join(next_corner, curr_layer);

         p_polyline = try_poly;

         // There was here a --index, basically, the idea is to try to look again from the beginnig.
         // let's try to make it simple and not do it, this makes the algorithm surely terminating
         }
      
      return p_polyline;
      }

   /**
    * Try to smothen the corner by adding an extra line and check if it is ok
    * @param p_line_arr
    * @param p_start_no
    * @return
    */
   private PlaLineInt smoothen_corner(PlaLineIntAlist p_line_arr, int p_start_no)
      {
      if (p_line_arr.size() - p_start_no < 4) return null;

      PlaLineInt cur_line = p_line_arr.get(p_start_no);
      PlaLineInt a_line   = p_line_arr.get(p_start_no + 1);
      PlaLineInt b_line   = p_line_arr.get(p_start_no + 2);
      PlaLineInt d_line   = p_line_arr.get(p_start_no + 3);
      
      PlaPointFloat curr_corner = a_line.intersection_approx(b_line);

      // cannot smoothen if is not a number
      if ( curr_corner.is_NaN() ) return null;

      double cosinus_angle = a_line.cos_angle(b_line);

      // lines are already nearly parallel, don't divide angle any further because of problems with numerical stability
      if (cosinus_angle > COS_ANGLE_MAX) return null;

      PlaPointFloat prev_corner = cur_line.intersection_approx(a_line);
      
      if ( prev_corner.is_NaN() ) return null;
      
      PlaPointFloat next_corner = b_line.intersection_approx(d_line);

      if ( next_corner.is_NaN() ) return null;
      
      // create a line approximately through curr_corner, whose direction is about the middle of the directions of the
      // previous and the next line. Translations of this line are used to cut off the corner.
      PlaDirection prev_dir = a_line.direction();
      PlaDirection next_dir = b_line.direction();
      PlaDirection middle_dir = prev_dir.middle_approx(next_dir);
      
      // this is the line that will be "translated
      PlaLineInt translate_line = new PlaLineInt(curr_corner.round(), middle_dir);
      
      double prev_dist = translate_line.distance_signed(prev_corner);
      double next_dist = translate_line.distance_signed(next_corner);
      
      PlaPointFloat nearest_point;
      double max_translate_dist;
      
      if (Math.abs(prev_dist) < Math.abs(next_dist))
         {
         nearest_point = prev_corner;
         max_translate_dist = prev_dist;
         }
      else
         {
         nearest_point = next_corner;
         max_translate_dist = next_dist;
         }
      
      if (Math.abs(max_translate_dist) < 1) return null;
      
      PlaLineIntAlist curr_lines = new PlaLineIntAlist(p_line_arr.size(1));
      // copy the whole list
      p_line_arr.append_to(curr_lines, 0);

      // then reserve a slot to put the new line
      curr_lines.add_null(p_start_no + 2);

      double translate_dist = max_translate_dist;
      double delta_dist = max_translate_dist;
      PlaSide side_of_nearest_point = translate_line.side_of(nearest_point);
      int sign = Signum.as_int(max_translate_dist);
      PlaLineInt result = null;
      
      while (Math.abs(delta_dist) > min_move_dist)
         {
         boolean check_ok = false;
         PlaLineInt new_line = translate_line.translate(-translate_dist);
         PlaSide new_line_side_of_nearest_point = new_line.side_of(nearest_point);
         if (new_line_side_of_nearest_point == side_of_nearest_point || new_line_side_of_nearest_point == PlaSide.COLLINEAR)
            {
            curr_lines.set(p_start_no + 2, new_line);
            Polyline tmp = new Polyline(curr_lines);

            if (tmp.plaline_len() == curr_lines.size())
               {
               ShapeTile shape_to_check = tmp.offset_shape(curr_half_width, p_start_no + 1);
               check_ok = r_board.check_trace(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins);
               }
            delta_dist /= 2;
            if (check_ok)
               {
               result = new_line;

               if (translate_dist == max_translate_dist)
                  {
                  // biggest possible change
                  break;
                  }
               
               translate_dist += delta_dist;
               }
            else
               {
               translate_dist -= delta_dist;
               }
            }
         else
            // moved a little bit to far at the first time because of numerical inaccuracy
            {
            double shorten_value = sign * 0.5;
            max_translate_dist -= shorten_value;
            translate_dist -= shorten_value;
            delta_dist -= shorten_value;
            }
         }
      
      if (result == null) return null;

      curr_lines.changed_area_join_corner(r_board.changed_area, p_start_no, curr_layer);
      curr_lines.changed_area_join_corner(r_board.changed_area, p_start_no+3, curr_layer);

      return result;
      }
   
   
   
   /**
    * Tries to reposition the line with index p_no to make the polyline consisting of p_line_arr shorter
    * @return null if it fails to shorten
    */
   @Override
   protected PlaLineInt reposition_line(PlaLineInt[] p_line_arr, int p_start_no)
      {
      if (p_line_arr.length - p_start_no < 5) return null;

      PlaLineInt translate_line = p_line_arr[p_start_no + 2];
      
      PlaPointFloat prev_corner = p_line_arr[p_start_no].intersection_approx(p_line_arr[p_start_no + 1]);
      
      PlaPointFloat next_corner = p_line_arr[p_start_no + 3].intersection_approx(p_line_arr[p_start_no + 4]);
      
      double prev_dist = translate_line.distance_signed(prev_corner);
      
      int corners_skipped_before = 0;
      
      int corners_skipped_after = 0;
      
      final double c_epsilon = 0.001;
      
      while (Math.abs(prev_dist) < c_epsilon)
         {
         // move also all lines trough the start corner of the line to translate
         ++corners_skipped_before;
         int curr_no = p_start_no - corners_skipped_before;
      
         // the first corner is on the line to translate
         if (curr_no < 0) return null;

         prev_corner = p_line_arr[curr_no].intersection_approx(p_line_arr[curr_no + 1]);
         
         prev_dist = translate_line.distance_signed(prev_corner);
         }
      
      double next_dist = translate_line.distance_signed(next_corner);

      while (Math.abs(next_dist) < c_epsilon)
         {
         // move also all lines trough the end corner of the line to translate
         ++corners_skipped_after;
         
         int curr_no = p_start_no + 3 + corners_skipped_after;
         if (curr_no >= p_line_arr.length - 2)
            {
            // the last corner is on the line to translate
            return null;
            }
         next_corner = p_line_arr[curr_no].intersection_approx(p_line_arr[curr_no + 1]);
         next_dist = translate_line.distance_signed(next_corner);
         }
      
      
      if (Signum.of(prev_dist) != Signum.of(next_dist))
         {
         // the 2 corners are at different sides of translate_line
         return null;
         }
      
      PlaPointFloat nearest_point;
      double max_translate_dist;
      if (Math.abs(prev_dist) < Math.abs(next_dist))
         {
         nearest_point = prev_corner;
         max_translate_dist = prev_dist;
         }
      else
         {
         nearest_point = next_corner;
         max_translate_dist = next_dist;
         }

      PlaLineInt[] curr_lines = new PlaLineInt[p_line_arr.length];
      
      System.arraycopy(p_line_arr, 0, curr_lines, 0, p_start_no + 2);
      
      System.arraycopy(p_line_arr, p_start_no + 3, curr_lines, p_start_no + 3, curr_lines.length - p_start_no - 3);
      
      double translate_dist = max_translate_dist;
      
      double delta_dist = max_translate_dist;
      
      PlaSide side_of_nearest_point = translate_line.side_of(nearest_point);
      
      int sign = Signum.as_int(max_translate_dist);
      
      PlaLineInt result = null;
      
      boolean first_time = true;
      
      while (first_time || Math.abs(delta_dist) > min_move_dist)
         {
         boolean check_ok = false;
         
         PlaLineInt new_line = translate_line.translate(-translate_dist);
         
         if (first_time && Math.abs(translate_dist) < 1)
            {
            if (new_line.equals(translate_line))
               {
               // try the parallel line through the nearest_point
               PlaPointInt rounded_nearest_point = nearest_point.round();
               if (nearest_point.distance(rounded_nearest_point.to_float()) < Math.abs(translate_dist))
                  {
                  new_line = new PlaLineInt(rounded_nearest_point, translate_line.direction());
                  }
               first_time = false;
               }
            if (new_line.equals(translate_line))
               {
               return null;
               }
            }
         
         PlaSide new_line_side_of_nearest_point = new_line.side_of(nearest_point);
         
         if (new_line_side_of_nearest_point == side_of_nearest_point || new_line_side_of_nearest_point == PlaSide.COLLINEAR)
            {
            first_time = false;
            curr_lines[p_start_no + 2] = new_line;
            // corners_skipped_before > 0 or corners_skipped_after > 0
            // happens very rarely. But this handling seems to be important because there are situations which no other
            // tightening function can solve. For example when 3 ore more consecutive corners are equal.
            PlaLineInt prev_translated_line = new_line;
         
            for (int index = 0; index < corners_skipped_before; ++index)
               {
               // Translate the previous lines onto or past the intersection of new_line with the first untranslated line.
               int prev_line_no = p_start_no + 1 - corners_skipped_before;
               
               PlaPointFloat curr_prev_corner = prev_translated_line.intersection_approx(curr_lines[prev_line_no]);
               
               // apparently this is a somewhat correct  thing to do
               if ( curr_prev_corner.is_NaN() ) return null;
               
               PlaLineInt curr_translate_line = p_line_arr[p_start_no + 1 - index];
               double curr_translate_dist = curr_translate_line.distance_signed(curr_prev_corner);
               prev_translated_line = curr_translate_line.translate(-curr_translate_dist);
               curr_lines[p_start_no + 1 - index] = prev_translated_line;
               }
            
            prev_translated_line = new_line;
            for (int index = 0; index < corners_skipped_after; ++index)
               {
               // Translate the next lines onto or past the intersection of new_line with the first untranslated line.
               int next_line_no = p_start_no + 3 + corners_skipped_after;
               
               PlaPointFloat curr_next_corner = prev_translated_line.intersection_approx(curr_lines[next_line_no]);
               
               // apparently this is a somewhat correct  thing to do
               if ( curr_next_corner.is_NaN() ) return null;
               
               PlaLineInt curr_translate_line = p_line_arr[p_start_no + 3 + index];
               double curr_translate_dist = curr_translate_line.distance_signed(curr_next_corner);
               prev_translated_line = curr_translate_line.translate(-curr_translate_dist);
               curr_lines[p_start_no + 3 + index] = prev_translated_line;
               }
            
            Polyline tmp = new Polyline(curr_lines);

            if (tmp.plaline_len() == curr_lines.length)
               {
               ShapeTile shape_to_check = tmp.offset_shape(curr_half_width, p_start_no + 1);
               check_ok = r_board.check_trace(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins);
               }
            
            delta_dist /= 2;
            
            if (check_ok)
               {
               result = curr_lines[p_start_no + 2];
               
               // biggest possible change
               if (translate_dist == max_translate_dist) break;
               
               translate_dist += delta_dist;
               }
            else
               {
               translate_dist -= delta_dist;
               }
            }
         else
            {
            // moved a little bit to far at the first time because of numerical inaccuracy
            double shorten_value = sign * 0.5;
            max_translate_dist -= shorten_value;
            translate_dist -= shorten_value;
            delta_dist -= shorten_value;
            }
         }

      if (result == null) return null;

      PlaPointFloat new_prev_corner = curr_lines[p_start_no].intersection_approx(curr_lines[p_start_no + 1]);
      PlaPointFloat new_next_corner = curr_lines[p_start_no + 3].intersection_approx(curr_lines[p_start_no + 4]);
      r_board.changed_area.join(new_prev_corner, curr_layer);
      r_board.changed_area.join(new_next_corner, curr_layer);

      return result;
      }

   /**
    * Try to skip some lines in the trace and see if it is still good....
    * @param p_polyline
    * @return
    */
   private Polyline skip_lines(Polyline p_polyline)
      {
      for (int index = 1; index < p_polyline.plaline_len(-3); ++index)
         {
         for (int jndex = 0; jndex <= 1; ++jndex)
            {
            PlaPointFloat corner1;
            PlaPointFloat corner2;
            PlaLineInt curr_line;
            
            if (jndex == 0) // try to skip the line before the i+2-th line
               {
               curr_line = p_polyline.plaline(index + 2);
               corner1 = p_polyline.corner_approx(index);
               corner2 = p_polyline.corner_approx(index - 1);
               }
            else
               // try to skip the line after i-th line
               {
               curr_line = p_polyline.plaline(index);
               corner1 = p_polyline.corner_approx(index + 1);
               corner2 = p_polyline.corner_approx(index + 2);
               }

            PlaSide side1 = curr_line.side_of(corner1);
            PlaSide side2 = curr_line.side_of(corner2);
            
            if (side1 != side2)
               {
               // the two corners are on different sides of the line
               Polyline reduced_polyline = new Polyline(p_polyline.plaline_skip(index + 1, index + 1));
               
               if ( reduced_polyline.plaline_len() == p_polyline.plaline_len(-1) )
                  {
                  int shape_no = index - 1;
                  if (jndex == 0)
                     {
                     ++shape_no;
                     }
                  ShapeTile shape_to_check = reduced_polyline.offset_shape(curr_half_width, shape_no);
                  
                  if (r_board.check_trace(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins))
                     {
                     r_board.changed_area.join(corner1, curr_layer);
                     r_board.changed_area.join(corner2, curr_layer);
                     return reduced_polyline;
                     }
                  }
               }
            
            // now try skipping 2 lines
            if (index >= p_polyline.plaline_len(-4))  break;
            
            PlaPointFloat corner3;
            if (jndex == 1)
               {
               corner3 = p_polyline.corner_approx(index + 3);
               }
            else
               {
               corner3 = p_polyline.corner_approx(index + 1);
               }
            
            if (jndex == 0)
               {
               // curr_line is 1 line later than in the case skipping 1 line when coming from behind
               curr_line = p_polyline.plaline(index + 3);
               side1 = curr_line.side_of(corner1);
               side2 = curr_line.side_of(corner2);
               }
            else
               {
               side1 = curr_line.side_of(corner3);
               }
            
            if (side1 != side2)
               {
               // the two corners are on different sides of the line
               Polyline reduced_polyline = new Polyline(p_polyline.plaline_skip(index + 1, index + 2));
               
               if (reduced_polyline.plaline_len() == p_polyline.plaline_len(-2) )
                  {
                  int shape_no = index - 1;

                  if (jndex == 0) ++shape_no;

                  ShapeTile shape_to_check = reduced_polyline.offset_shape(curr_half_width, shape_no);
                  if (r_board.check_trace(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, contact_pins))
                     {
                     r_board.changed_area.join(corner1, curr_layer);
                     r_board.changed_area.join(corner2, curr_layer);
                     r_board.changed_area.join(corner3, curr_layer);
                     return reduced_polyline;
                     }
                  }
               }
            }
         }
      return p_polyline;
      }

   @Override
   protected Polyline smoothen_start_corner_at_trace(BrdTracep p_trace)
      {
      boolean acute_angle = false;
      boolean bend = false;
      PlaPointFloat other_trace_corner_approx = null;
      PlaLineInt other_trace_line = null;
      PlaLineInt other_prev_trace_line = null;
      Polyline trace_polyline = p_trace.polyline();
      PlaPoint curr_end_corner = trace_polyline.corner_first();

      PlaPoint curr_prev_end_corner = trace_polyline.corner_first_next();

      boolean skip_short_segment = !(curr_end_corner instanceof PlaPointInt) && curr_end_corner.to_float().distance_square(curr_prev_end_corner.to_float()) < SMOOTH_LENGTH;
      
      int start_line_no = 1;

      if (skip_short_segment)
         {
         if (trace_polyline.corner_count() < 3) return null;

         curr_prev_end_corner = trace_polyline.corner(2);

         ++start_line_no;
         }
      
      
      PlaSide prev_corner_side = null;
      PlaDirection line_direction = trace_polyline.plaline(start_line_no).direction();
      PlaDirection prev_line_direction = trace_polyline.plaline(start_line_no + 1).direction();

      Collection<BrdItem> contact_list = p_trace.get_start_contacts();
      
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdTracep && !curr_contact.is_shove_fixed())
            {
            Polyline contact_trace_polyline = ((BrdTracep) curr_contact).polyline();
            PlaPointFloat curr_other_trace_corner_approx;
            PlaLineInt curr_other_trace_line;
            PlaLineInt curr_other_prev_trace_line;
            if (contact_trace_polyline.corner_first().equals(curr_end_corner))
               {
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(1);
               curr_other_trace_line = contact_trace_polyline.plaline(1);
               curr_other_prev_trace_line = contact_trace_polyline.plaline(2);
               }
            else
               {
               int curr_corner_no = contact_trace_polyline.corner_count() - 2;
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(curr_corner_no);
               curr_other_trace_line = contact_trace_polyline.plaline(curr_corner_no + 1).opposite();
               curr_other_prev_trace_line = contact_trace_polyline.plaline(curr_corner_no);
               }
            PlaSide curr_prev_corner_side = curr_prev_end_corner.side_of(curr_other_trace_line);
            Signum curr_projection = line_direction.projection(curr_other_trace_line.direction());
            boolean other_trace_found = false;
            if (curr_projection == Signum.POSITIVE && curr_prev_corner_side != PlaSide.COLLINEAR)
               {
               acute_angle = true;
               other_trace_found = true;

               }
            else if (curr_projection == Signum.ZERO && trace_polyline.corner_count() > 2)
               {
               if (prev_line_direction.projection(curr_other_trace_line.direction()) == Signum.POSITIVE)
                  {
                  bend = true;
                  other_trace_found = true;
                  }
               }
            if (other_trace_found)
               {
               other_trace_corner_approx = curr_other_trace_corner_approx;
               other_trace_line = curr_other_trace_line;
               prev_corner_side = curr_prev_corner_side;
               other_prev_trace_line = curr_other_prev_trace_line;
               }
            }
         else
            {
            return null;
            }
         }
      
      
      
      int new_line_count = trace_polyline.plaline_len(1);
      int diff = 1;
      if (skip_short_segment)
         {
         --new_line_count;
         --diff;
         }
      
      if (acute_angle)
         {
         PlaDirection new_line_dir;
         if (prev_corner_side == PlaSide.ON_THE_LEFT)
            {
            new_line_dir = other_trace_line.direction().rotate_45_deg(2);
            }
         else
            {
            new_line_dir = other_trace_line.direction().rotate_45_deg(6);
            }
         PlaLineInt translate_line = new PlaLineInt(curr_end_corner.to_float().round(), new_line_dir);
         double translate_dist = (PlaLimits.sqrt2 - 1) * curr_half_width;
         double prev_corner_dist = Math.abs(translate_line.distance_signed(curr_prev_end_corner.to_float()));
         double other_dist = Math.abs(translate_line.distance_signed(other_trace_corner_approx));
         translate_dist = Math.min(translate_dist, prev_corner_dist);
         translate_dist = Math.min(translate_dist, other_dist);
         if (translate_dist >= 0.99)
            {

            translate_dist = Math.max(translate_dist - 1, 1);
            if (translate_line.side_of(curr_prev_end_corner) == PlaSide.ON_THE_LEFT)
               {
               translate_dist = -translate_dist;
               }
            PlaLineInt add_line = translate_line.translate(translate_dist);
            // constract the new trace polyline.
            PlaLineInt[] new_lines = new PlaLineInt[new_line_count];
            new_lines[0] = other_trace_line;
            new_lines[1] = add_line;
            for (int index = 2; index < new_lines.length; ++index)
               {
               new_lines[index] = trace_polyline.plaline(index - diff);
               }
            return new Polyline(new_lines);
            }
         }
      else if (bend)
         {
         PlaLineInt[] check_line_arr = new PlaLineInt[new_line_count];
         check_line_arr[0] = other_prev_trace_line;
         check_line_arr[1] = other_trace_line;
         for (int index = 2; index < check_line_arr.length; ++index)
            {
            check_line_arr[index] = trace_polyline.plaline(index - diff);
            }
         PlaLineInt new_line = reposition_line(check_line_arr, 0);
         if (new_line != null)
            {
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.plaline_len()];
            new_lines[0] = other_trace_line;
            new_lines[1] = new_line;
            for (int index = 2; index < new_lines.length; ++index)
               {
               new_lines[index] = trace_polyline.plaline(index);
               }
            return new Polyline(new_lines);
            }
         }
      
      return null;
      }

   protected Polyline smoothen_end_corner_at_trace(BrdTracep p_trace)
      {
      boolean acute_angle = false;
      boolean bend = false;
      PlaPointFloat other_trace_corner_approx = null;
      PlaLineInt other_trace_line = null;
      PlaLineInt other_prev_trace_line = null;
      Polyline trace_polyline = p_trace.polyline();
      
      PlaPoint curr_end_corner = trace_polyline.corner_last();

      PlaPoint curr_prev_end_corner = trace_polyline.corner_last_prev();

      boolean skip_short_segment = !(curr_end_corner instanceof PlaPointInt) && curr_end_corner.to_float().distance_square(curr_prev_end_corner.to_float()) < SMOOTH_LENGTH;
      
      int end_line_no = trace_polyline.plaline_len(-2);
      if (skip_short_segment)
         {
         if (trace_polyline.corner_count() < 3) return null;

         curr_prev_end_corner = trace_polyline.corner(trace_polyline.corner_count() - 3);
         --end_line_no;
         }

      PlaSide prev_corner_side = null;
      PlaDirection line_direction = trace_polyline.plaline(end_line_no).direction().opposite();
      PlaDirection prev_line_direction = trace_polyline.plaline(end_line_no).direction().opposite();

      TreeSet<BrdItem> contact_list = p_trace.get_end_contacts();
      
      for (BrdItem curr_contact : contact_list)
         {
         if ( !(curr_contact instanceof BrdTracep) ) return null;
         
         if ( curr_contact.is_shove_fixed()) return null;

         BrdTracep a_trace = (BrdTracep)curr_contact;
         
         Polyline c_trace_poly = a_trace.polyline();
         
         // cannot reduce anything if the trace is already at minimum
         if (c_trace_poly.corner_count() <= 2) continue;
         
         PlaPointFloat curr_other_trace_corner_approx;
         PlaLineInt curr_other_trace_line;
         PlaLineInt curr_other_prev_trace_line;

         if (c_trace_poly.corner_first().equals(curr_end_corner))
            {
            curr_other_trace_corner_approx = c_trace_poly.corner_approx(1);
            curr_other_trace_line = c_trace_poly.plaline(1);
            curr_other_prev_trace_line = c_trace_poly.plaline(2);
            }
         else
            {
            int curr_corner_no = c_trace_poly.corner_count() - 2;
            curr_other_trace_corner_approx = c_trace_poly.corner_approx(curr_corner_no);
            curr_other_trace_line = c_trace_poly.plaline(curr_corner_no + 1).opposite();
            curr_other_prev_trace_line = c_trace_poly.plaline(curr_corner_no);
            }
         
         PlaSide curr_prev_corner_side = curr_prev_end_corner.side_of(curr_other_trace_line);
         Signum curr_projection = line_direction.projection(curr_other_trace_line.direction());
         boolean other_trace_found = false;
         if (curr_projection == Signum.POSITIVE && curr_prev_corner_side != PlaSide.COLLINEAR)
            {
            acute_angle = true;
            other_trace_found = true;
            }
         else if (curr_projection == Signum.ZERO && trace_polyline.corner_count() > 2)
            {
            if (prev_line_direction.projection(curr_other_trace_line.direction()) == Signum.POSITIVE)
               {
               bend = true;
               other_trace_found = true;
               }
            }
         if (other_trace_found)
            {
            other_trace_corner_approx = curr_other_trace_corner_approx;
            other_trace_line = curr_other_trace_line;
            prev_corner_side = curr_prev_corner_side;
            other_prev_trace_line = curr_other_prev_trace_line;
            }
         }

      int new_line_count = trace_polyline.plaline_len(+1);
      int diff = 0;
      if (skip_short_segment)
         {
         --new_line_count;
         ++diff;
         }

      if (acute_angle)
         {
         PlaDirection new_line_dir;
         if (prev_corner_side == PlaSide.ON_THE_LEFT)
            {
            new_line_dir = other_trace_line.direction().rotate_45_deg(6);
            }
         else
            {
            new_line_dir = other_trace_line.direction().rotate_45_deg(2);
            }
         PlaLineInt translate_line = new PlaLineInt(curr_end_corner.to_float().round(), new_line_dir);
         double translate_dist = (PlaLimits.sqrt2 - 1) * curr_half_width;
         double prev_corner_dist = Math.abs(translate_line.distance_signed(curr_prev_end_corner.to_float()));
         double other_dist = Math.abs(translate_line.distance_signed(other_trace_corner_approx));
         translate_dist = Math.min(translate_dist, prev_corner_dist);
         translate_dist = Math.min(translate_dist, other_dist);
         
         if (translate_dist >= 0.99)
            {
            translate_dist = Math.max(translate_dist - 1, 1);
            if (translate_line.side_of(curr_prev_end_corner) == PlaSide.ON_THE_LEFT)
               {
               translate_dist = -translate_dist;
               }

            PlaLineInt add_line = translate_line.translate(translate_dist);
            // constract the new trace polyline.
            
            PlaLineIntAlist new_lines = new PlaLineIntAlist(new_line_count);
            
            int l_count = trace_polyline.plaline_len(-1); // copy until before the end line
            
            for (int index = 0; index < l_count; ++index)
               {
               new_lines.add( trace_polyline.plaline(index));
               }
            
            new_lines.add( add_line );
            new_lines.add( other_trace_line);
            
            return new Polyline(new_lines);
            }
         }
      else if (bend)
         {
         PlaLineInt[] check_line_arr = new PlaLineInt[new_line_count];
         for (int index = 0; index < check_line_arr.length - 2; ++index)
            {
            check_line_arr[index] = trace_polyline.plaline(index + diff);
            }
         
         check_line_arr[check_line_arr.length - 2] = other_trace_line;
         check_line_arr[check_line_arr.length - 1] = other_prev_trace_line;
         PlaLineInt new_line = reposition_line(check_line_arr, check_line_arr.length - 5);
         if (new_line != null)
            {
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.plaline_len()];
            
            for (int index = 0; index < new_lines.length - 2; ++index)
               {
               new_lines[index] = trace_polyline.plaline(index);
               }
            
            new_lines[new_lines.length - 2] = new_line;
            new_lines[new_lines.length - 1] = other_trace_line;
            
            return new Polyline(new_lines);
            }
         }
      return null;
      }

   }



