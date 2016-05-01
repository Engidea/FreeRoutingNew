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
 * Created on 19. Juli 2003, 18:59
 */
package board.algo;

import board.RoutingBoard;
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.varie.BrdKeepPoint;
import freert.planar.PlaDirection;
import freert.planar.PlaLimits;
import freert.planar.PlaLineInt;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaSide;
import freert.planar.PlaVector;
import freert.planar.Polyline;
import freert.planar.ShapeTile;
import freert.varie.Signum;
import freert.varie.ThreadStoppable;

/**
 *
 * @author Alfons Wirtz
 */
public final class AlgoPullTight45 extends AlgoPullTight
   {
   public AlgoPullTight45(RoutingBoard p_board, int[] p_only_net_no_arr, ThreadStoppable p_stoppable_thread, BrdKeepPoint p_keep_point)
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
         if ( is_stop_requested())  break;

         prev_result = new_result;
         Polyline tmp1 = reduce_corners(prev_result);
         Polyline tmp2 = smoothen_corners(tmp1);
         new_result = reposition_lines(tmp2);
         }
      return new_result;
      }

   /**
    * Tries to reduce the amount of corners of p_polyline. Return p_polyline, if nothing was changed.
    */
   private Polyline reduce_corners(Polyline p_polyline)
      {
      if (p_polyline.lines_arr.length <= 4)
         {
         return p_polyline;
         }
      int new_corner_count = 1;
      PlaPoint[] curr_corner = new PlaPoint[4];
      for (int i = 0; i < 4; ++i)
         {
         curr_corner[i] = p_polyline.corner(i);
         if (!(curr_corner[i] instanceof PlaPointInt))
            {
            return p_polyline;
            }
         }
      boolean[] curr_corner_in_clip_shape = new boolean[4];

      for (int i = 0; i < 4; ++i)
         {
         if (curr_clip_shape == null)
            {
            curr_corner_in_clip_shape[i] = true;
            }
         else
            {
            curr_corner_in_clip_shape[i] = !curr_clip_shape.is_outside(curr_corner[i]);
            }
         }

      boolean polyline_changed = false;
      PlaPoint[] new_corners = new PlaPoint[p_polyline.lines_arr.length - 3];
      new_corners[0] = curr_corner[0];
      PlaPoint[] curr_check_points = new PlaPoint[2];
      PlaPoint new_corner = null;
      int corner_no = 3;
      while (corner_no < p_polyline.lines_arr.length - 1)
         {
         boolean corner_removed = false;
         curr_corner[3] = p_polyline.corner(corner_no);
         if (!(curr_corner[3] instanceof PlaPointInt))
            {
            return p_polyline;
            }
         if (curr_corner[1].equals(curr_corner[2]) || corner_no < p_polyline.lines_arr.length - 2 && curr_corner[3].side_of(curr_corner[1], curr_corner[2]) == PlaSide.COLLINEAR)
            {
            // corners in the middle af a line can be skipped
            ++corner_no;
            curr_corner[2] = curr_corner[3];
            curr_corner_in_clip_shape[2] = curr_corner_in_clip_shape[3];
            if (corner_no < p_polyline.lines_arr.length - 1)
               {
               curr_corner[3] = p_polyline.corner(corner_no);
               if (!(curr_corner[3] instanceof PlaPointInt))
                  {
                  return p_polyline;
                  }
               }
            polyline_changed = true;
            }
         curr_corner_in_clip_shape[3] = curr_clip_shape == null || !curr_clip_shape.is_outside(curr_corner[3]);
         if (curr_corner_in_clip_shape[1] && curr_corner_in_clip_shape[2] && curr_corner_in_clip_shape[3])
            {
            // translate the line from curr_corner[2] to curr_corner[1] to curr_corner[3]
            PlaVector delta = curr_corner[3].difference_by(curr_corner[2]);
            new_corner = curr_corner[1].translate_by(delta);
            if (curr_corner[3].equals(curr_corner[2]))
               {
               // just remove multiple corner
               corner_removed = true;
               }
            else if (new_corner.side_of(curr_corner[0], curr_corner[1]) == PlaSide.COLLINEAR)
               {
               curr_check_points[0] = new_corner;
               curr_check_points[1] = curr_corner[1];
               Polyline check_polyline = new Polyline(curr_check_points);
               if (check_polyline.lines_arr.length == 3)
                  {
                  ShapeTile shape_to_check = check_polyline.offset_shape(curr_half_width, 0);
                  if (r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins))
                     {
                     curr_check_points[1] = curr_corner[3];
                     if (curr_check_points[0].equals(curr_check_points[1]))
                        {
                        corner_removed = true;
                        }
                     else
                        {
                        check_polyline = new Polyline(curr_check_points);
                        if (check_polyline.lines_arr.length == 3)
                           {
                           shape_to_check = check_polyline.offset_shape(curr_half_width, 0);
                           corner_removed = r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins);
                           }
                        else
                           {
                           corner_removed = true;
                           }
                        }
                     }
                  }
               else
                  {
                  corner_removed = true;
                  }
               }
            }
         if (!corner_removed && curr_corner_in_clip_shape[0] && curr_corner_in_clip_shape[1] && curr_corner_in_clip_shape[2])
            {
            // the first try has failed. Try to translate the line from
            // corner_2 to corner_1 to corner_0
            PlaVector delta = curr_corner[0].difference_by(curr_corner[1]);
            new_corner = curr_corner[2].translate_by(delta);
            if (curr_corner[0].equals(curr_corner[1]))
               {
               // just remove multiple corner
               corner_removed = true;
               }
            else if (new_corner.side_of(curr_corner[2], curr_corner[3]) == PlaSide.COLLINEAR)
               {
               curr_check_points[0] = new_corner;
               curr_check_points[1] = curr_corner[0];
               Polyline check_polyline = new Polyline(curr_check_points);
               if (check_polyline.lines_arr.length == 3)
                  {
                  ShapeTile shape_to_check = check_polyline.offset_shape(curr_half_width, 0);
                  if (r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins))
                     {
                     curr_check_points[1] = curr_corner[2];
                     check_polyline = new Polyline(curr_check_points);
                     if (check_polyline.lines_arr.length == 3)
                        {
                        shape_to_check = check_polyline.offset_shape(curr_half_width, 0);
                        corner_removed = r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins);
                        }
                     else
                        {
                        corner_removed = true;
                        }
                     }
                  }
               else
                  {
                  corner_removed = true;
                  }
               }
            }
         if (corner_removed)
            {
            polyline_changed = true;
            curr_corner[1] = new_corner;
            curr_corner_in_clip_shape[1] = curr_clip_shape == null || !curr_clip_shape.is_outside(curr_corner[1]);
            if (r_board.changed_area != null)
               {
               r_board.changed_area.join(new_corner.to_float(), curr_layer);
               r_board.changed_area.join(curr_corner[1].to_float(), curr_layer);
               r_board.changed_area.join(curr_corner[2].to_float(), curr_layer);
               }
            }
         else
            {
            new_corners[new_corner_count] = curr_corner[1];
            ++new_corner_count;
            curr_corner[0] = curr_corner[1];
            curr_corner[1] = curr_corner[2];
            curr_corner_in_clip_shape[0] = curr_corner_in_clip_shape[1];
            curr_corner_in_clip_shape[1] = curr_corner_in_clip_shape[2];
            }
         curr_corner[2] = curr_corner[3];
         curr_corner_in_clip_shape[2] = curr_corner_in_clip_shape[3];
         ++corner_no;
         }
      if (!polyline_changed)
         {
         return p_polyline;
         }
      PlaPoint adjusted_corners[] = new PlaPoint[new_corner_count + 2];
      for (int i = 0; i < new_corner_count; ++i)
         {
         adjusted_corners[i] = new_corners[i];
         }
      adjusted_corners[new_corner_count] = curr_corner[1];
      adjusted_corners[new_corner_count + 1] = curr_corner[2];
      Polyline result = new Polyline(adjusted_corners);
      return result;
      }

   /**
    * Smoothens the 90 degree corners of p_polyline to 45 degree by cutting of the 90 degree corner. The cutting of is so small,
    * that no check is needed
    */
   private Polyline smoothen_corners(Polyline p_polyline)
      {
      Polyline result = p_polyline;
      boolean polyline_changed = true;
      while (polyline_changed)
         {
         if (result.lines_arr.length < 4)
            {
            return result;
            }
         polyline_changed = false;
         PlaLineInt[] line_arr = new PlaLineInt[result.lines_arr.length];
         System.arraycopy(result.lines_arr, 0, line_arr, 0, line_arr.length);

         for (int i = 1; i < line_arr.length - 2; ++i)
            {
            PlaDirection d1 = line_arr[i].direction();
            PlaDirection d2 = line_arr[i + 1].direction();
            if (d1.is_multiple_of_45_degree() && d2.is_multiple_of_45_degree() && d1.projection(d2) != Signum.POSITIVE)
               {
               // there is a 90 degree or sharper angle
               PlaLineInt new_line = smoothen_corner(line_arr, i);
               if (new_line == null)
                  {
                  // the greedy smoothening couldn't change the polyline
                  new_line = smoothen_sharp_corner(line_arr, i);
                  }
               if (new_line != null)
                  {
                  polyline_changed = true;
                  // add the new line into the line array
                  PlaLineInt[] tmp_lines = new PlaLineInt[line_arr.length + 1];
                  System.arraycopy(line_arr, 0, tmp_lines, 0, i + 1);
                  tmp_lines[i + 1] = new_line;
                  System.arraycopy(line_arr, i + 1, tmp_lines, i + 2, tmp_lines.length - (i + 2));
                  line_arr = tmp_lines;
                  ++i;
                  }
               }
            }
         if (polyline_changed)
            {
            result = new Polyline(line_arr);
            }
         }
      return result;
      }

   /**
    * adds a line between at p_no to smoothen a 90 degree corner between p_line_1 and p_line_2 to 45 degree. 
    * The distance of the new line to the corner will be so small that no clearance check is necessary.
    */
   private PlaLineInt smoothen_sharp_corner(PlaLineInt[] p_line_arr, int p_no)
      {
      PlaPointFloat curr_corner = p_line_arr[p_no].intersection_approx(p_line_arr[p_no + 1]);
      if (curr_corner.v_x != (int) curr_corner.v_x)
         {
         // intersection of 2 diagonal lines is not integer
         PlaLineInt result = smoothen_non_integer_corner(p_line_arr, p_no);
            {
            if (result != null)
               {
               return result;
               }
            }
         }
      PlaPointFloat prev_corner = p_line_arr[p_no].intersection_approx(p_line_arr[p_no - 1]);
      PlaPointFloat next_corner = p_line_arr[p_no + 1].intersection_approx(p_line_arr[p_no + 2]);

      PlaDirection prev_dir = p_line_arr[p_no].direction();
      PlaDirection next_dir = p_line_arr[p_no + 1].direction();
      
//      PlaDirection new_line_dir = new PlaDirection(prev_dir.get_vector().add(next_dir.get_vector()));
      PlaDirection new_line_dir = prev_dir.add(next_dir);
      
      PlaLineInt translate_line = new PlaLineInt(curr_corner.round(), new_line_dir);
      double translate_dist = (PlaLimits.sqrt2 - 1) * this.curr_half_width;
      double prev_dist = Math.abs(translate_line.signed_distance(prev_corner));
      double next_dist = Math.abs(translate_line.signed_distance(next_corner));
      translate_dist = Math.min(translate_dist, prev_dist);
      translate_dist = Math.min(translate_dist, next_dist);
      if (translate_dist < 0.99)
         {
         return null;
         }
      translate_dist = Math.max(translate_dist - 1, 1);
      if (translate_line.side_of(next_corner) == PlaSide.ON_THE_LEFT)
         {
         translate_dist = -translate_dist;
         }
      PlaLineInt result = translate_line.translate(translate_dist);
      if (r_board.changed_area != null)
         {
         r_board.changed_area.join(curr_corner, curr_layer);
         }
      return result;
      }

   /**
    * Smoothens with a short axis parrallel line to remove a non integer corner of two intersecting diagonal lines. Returns null, if
    * that is not possible.
    */
   private PlaLineInt smoothen_non_integer_corner(PlaLineInt[] p_line_arr, int p_no)
      {
      PlaLineInt prev_line = p_line_arr[p_no];
      PlaLineInt next_line = p_line_arr[p_no + 1];
      if (prev_line.is_equal_or_opposite(next_line))
         {
         return null;
         }
      if (!(prev_line.is_diagonal() && next_line.is_diagonal()))
         {
         return null;
         }
      PlaPointFloat curr_corner = prev_line.intersection_approx(next_line);
      PlaPointFloat prev_corner = prev_line.intersection_approx(p_line_arr[p_no - 1]);
      PlaPointFloat next_corner = next_line.intersection_approx(p_line_arr[p_no + 2]);
      PlaLineInt result = null;
      int new_x = 0;
      int new_y = 0;
      boolean new_line_is_vertical = false;
      boolean new_line_is_horizontal = false;
      if (prev_corner.v_x > curr_corner.v_x && next_corner.v_x > curr_corner.v_x)
         {
         new_x = (int) Math.ceil(curr_corner.v_x);
         new_y = (int) Math.ceil(curr_corner.v_y);
         new_line_is_vertical = true;
         }
      else if (prev_corner.v_x < curr_corner.v_x && next_corner.v_x < curr_corner.v_x)
         {
         new_x = (int) Math.floor(curr_corner.v_x);
         new_y = (int) Math.floor(curr_corner.v_y);
         new_line_is_vertical = true;
         }
      else if (prev_corner.v_y > curr_corner.v_y && next_corner.v_y > curr_corner.v_y)
         {
         new_x = (int) Math.ceil(curr_corner.v_x);
         new_y = (int) Math.ceil(curr_corner.v_y);
         new_line_is_horizontal = true;
         }
      else if (prev_corner.v_y < curr_corner.v_y && next_corner.v_y < curr_corner.v_y)
         {
         new_x = (int) Math.floor(curr_corner.v_x);
         new_y = (int) Math.floor(curr_corner.v_y);
         new_line_is_horizontal = true;
         }
      PlaDirection new_line_dir = null;
      if (new_line_is_vertical)
         {
         if (prev_corner.v_y < next_corner.v_y)
            {
            new_line_dir = PlaDirection.UP;
            }
         else
            {
            new_line_dir = PlaDirection.DOWN;
            }
         }
      else if (new_line_is_horizontal)
         {
         if (prev_corner.v_x < next_corner.v_x)
            {
            new_line_dir = PlaDirection.RIGHT;
            }
         else
            {
            new_line_dir = PlaDirection.LEFT;
            }
         }
      else
         {
         return null;
         }

      PlaPointInt line_a = new PlaPointInt(new_x, new_y);
      result = new PlaLineInt(line_a, new_line_dir);
      return result;
      }

   /**
    * adds a line between at p_no to smoothen a 90 degree corner between p_line_1 and p_line_2 to 45 degree. 
    * The distance of the new line to the corner will be so big that a clearance check is necessary.
    */
   private PlaLineInt smoothen_corner(PlaLineInt[] p_line_arr, int p_no)
      {
      PlaPointFloat prev_corner = p_line_arr[p_no].intersection_approx(p_line_arr[p_no - 1]);
      if ( prev_corner.is_NaN()) return null;
      
      PlaPointFloat curr_corner = p_line_arr[p_no].intersection_approx(p_line_arr[p_no + 1]);
      if ( curr_corner.is_NaN()) return null;
      
      PlaPointFloat next_corner = p_line_arr[p_no + 1].intersection_approx(p_line_arr[p_no + 2]);
      if ( next_corner.is_NaN()) return null;

      PlaDirection prev_dir = p_line_arr[p_no].direction();
      PlaDirection next_dir = p_line_arr[p_no + 1].direction();
      
//      PlaDirection new_line_dir = new PlaDirection(prev_dir.get_vector().add(next_dir.get_vector()));
      PlaDirection new_line_dir = prev_dir.add(next_dir);
      
      PlaLineInt translate_line = new PlaLineInt(curr_corner.round(), new_line_dir);
      
      double prev_dist = Math.abs(translate_line.signed_distance(prev_corner));
      double next_dist = Math.abs(translate_line.signed_distance(next_corner));

      if (prev_dist == 0 || next_dist == 0) return null;
 
     double max_translate_dist;
      PlaPointFloat nearest_corner;
      if (prev_dist <= next_dist)
         {
         max_translate_dist = prev_dist;
         nearest_corner = prev_corner;
         }
      else
         {
         max_translate_dist = next_dist;
         nearest_corner = next_corner;
         }
      
      if (max_translate_dist < 1) return null;
      
      max_translate_dist = Math.max(max_translate_dist - 1, 1);
      if (translate_line.side_of(next_corner) == PlaSide.ON_THE_LEFT)
         {
         max_translate_dist = -max_translate_dist;
         }
      PlaLineInt[] check_lines = new PlaLineInt[3];
      check_lines[0] = p_line_arr[p_no];
      check_lines[2] = p_line_arr[p_no + 1];
      double translate_dist = max_translate_dist;
      double delta_dist = max_translate_dist;
      PlaSide side_of_nearest_corner = translate_line.side_of(nearest_corner);
      int sign = Signum.as_int(max_translate_dist);
      PlaLineInt result = null;
      while (Math.abs(delta_dist) > this.min_translate_dist)
         {
         boolean check_ok = false;
         PlaLineInt new_line = translate_line.translate(translate_dist);
         PlaSide new_line_side_of_nearest_corner = new_line.side_of(nearest_corner);
         if (new_line_side_of_nearest_corner == side_of_nearest_corner || new_line_side_of_nearest_corner == PlaSide.COLLINEAR)
            {
            check_lines[1] = new_line;
            Polyline tmp = new Polyline(check_lines);

            if (tmp.lines_arr.length == 3)
               {
               ShapeTile shape_to_check = tmp.offset_shape(curr_half_width, 0);
               check_ok = r_board.check_trace_shape(shape_to_check, curr_layer, curr_net_no_arr, curr_cl_type, this.contact_pins);

               }
            delta_dist /= 2;
            if (check_ok)
               {
               result = check_lines[1];
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
            // moved a little bit to far at the first time
            // because of numerical inaccuracy
            {
            double shorten_value = sign * 0.5;
            max_translate_dist -= shorten_value;
            translate_dist -= shorten_value;
            delta_dist -= shorten_value;
            }
         }
      if (result != null && r_board.changed_area != null)
         {
         PlaPointFloat new_prev_corner = check_lines[0].intersection_approx(result);
         PlaPointFloat new_next_corner = check_lines[2].intersection_approx(result);
         r_board.changed_area.join(new_prev_corner, curr_layer);
         r_board.changed_area.join(new_next_corner, curr_layer);
         r_board.changed_area.join(curr_corner, curr_layer);
         }
      return result;
      }

   protected Polyline smoothen_start_corner_at_trace(BrdTracePolyline p_trace)
      {
      boolean acute_angle = false;
      boolean bend = false;
      PlaPointFloat other_trace_corner_approx = null;
      PlaLineInt other_trace_line = null;
      PlaLineInt other_prev_trace_line = null;
      Polyline trace_polyline = p_trace.polyline();
      PlaPoint curr_end_corner = trace_polyline.corner(0);

      if (this.curr_clip_shape != null && this.curr_clip_shape.is_outside(curr_end_corner))
         {
         return null;
         }

      PlaPoint curr_prev_end_corner = trace_polyline.corner(1);
      PlaSide prev_corner_side = null;
      PlaDirection line_direction = trace_polyline.lines_arr[1].direction();
      PlaDirection prev_line_direction = trace_polyline.lines_arr[2].direction();

      java.util.Collection<BrdItem> contact_list = p_trace.get_start_contacts();
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdTracePolyline && !curr_contact.is_shove_fixed())
            {
            Polyline contact_trace_polyline = ((BrdTracePolyline) curr_contact).polyline();
            PlaPointFloat curr_other_trace_corner_approx;
            PlaLineInt curr_other_trace_line;
            PlaLineInt curr_other_prev_trace_line;
            if (contact_trace_polyline.corner_first().equals(curr_end_corner))
               {
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(1);
               curr_other_trace_line = contact_trace_polyline.lines_arr[1];
               curr_other_prev_trace_line = contact_trace_polyline.lines_arr[2];
               }
            else
               {
               int curr_corner_no = contact_trace_polyline.corner_count() - 2;
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(curr_corner_no);
               curr_other_trace_line = contact_trace_polyline.lines_arr[curr_corner_no + 1].opposite();
               curr_other_prev_trace_line = contact_trace_polyline.lines_arr[curr_corner_no];
               }
            PlaSide curr_prev_corner_side = curr_prev_end_corner.side_of(curr_other_trace_line);
            Signum curr_projection = line_direction.projection(curr_other_trace_line.direction());
            boolean other_trace_found = false;
            if (curr_projection == Signum.POSITIVE && curr_prev_corner_side != PlaSide.COLLINEAR)
               {
               if (curr_other_trace_line.direction().is_orthogonal())
                  {
                  acute_angle = true;
                  other_trace_found = true;
                  }
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

      if (acute_angle)
         {
         PlaDirection new_line_dir;
         if (prev_corner_side == PlaSide.ON_THE_LEFT)
            {
            new_line_dir = other_trace_line.direction().turn_45_degree(2);
            }
         else
            {
            new_line_dir = other_trace_line.direction().turn_45_degree(6);
            }
         PlaLineInt translate_line = new PlaLineInt(curr_end_corner.to_float().round(), new_line_dir);
         double translate_dist = (PlaLimits.sqrt2 - 1) * this.curr_half_width;
         double prev_corner_dist = Math.abs(translate_line.signed_distance(curr_prev_end_corner.to_float()));
         double other_dist = Math.abs(translate_line.signed_distance(other_trace_corner_approx));
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
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.lines_arr.length + 1];
            new_lines[0] = other_trace_line;
            new_lines[1] = add_line;
            for (int i = 2; i < new_lines.length; ++i)
               {
               new_lines[i] = trace_polyline.lines_arr[i - 1];
               }
            return new Polyline(new_lines);
            }
         }
      else if (bend)
         {
         PlaLineInt[] check_line_arr = new PlaLineInt[trace_polyline.lines_arr.length + 1];
         check_line_arr[0] = other_prev_trace_line;
         check_line_arr[1] = other_trace_line;
         for (int i = 2; i < check_line_arr.length; ++i)
            {
            check_line_arr[i] = trace_polyline.lines_arr[i - 1];
            }
         PlaLineInt new_line = reposition_line(check_line_arr, 2);
         if (new_line != null)
            {
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.lines_arr.length];
            new_lines[0] = other_trace_line;
            new_lines[1] = new_line;
            for (int i = 2; i < new_lines.length; ++i)
               {
               new_lines[i] = trace_polyline.lines_arr[i];
               }
            return new Polyline(new_lines);
            }
         }
      return null;
      }

   protected Polyline smoothen_end_corner_at_trace(BrdTracePolyline p_trace)
      {
      boolean acute_angle = false;
      boolean bend = false;
      PlaPointFloat other_trace_corner_approx = null;
      PlaLineInt other_trace_line = null;
      PlaLineInt other_prev_trace_line = null;
      Polyline trace_polyline = p_trace.polyline();
      PlaPoint curr_end_corner = trace_polyline.corner_last();

      if (this.curr_clip_shape != null && this.curr_clip_shape.is_outside(curr_end_corner))
         {
         return null;
         }

      PlaPoint curr_prev_end_corner = trace_polyline.corner(trace_polyline.corner_count() - 2);
      PlaSide prev_corner_side = null;
      PlaDirection line_direction = trace_polyline.lines_arr[trace_polyline.lines_arr.length - 2].direction().opposite();
      PlaDirection prev_line_direction = trace_polyline.lines_arr[trace_polyline.lines_arr.length - 3].direction().opposite();

      java.util.Collection<BrdItem> contact_list = p_trace.get_end_contacts();
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdTracePolyline && !curr_contact.is_shove_fixed())
            {
            Polyline contact_trace_polyline = ((BrdTracePolyline) curr_contact).polyline();
            PlaPointFloat curr_other_trace_corner_approx;
            PlaLineInt curr_other_trace_line;
            PlaLineInt curr_other_prev_trace_line;
            if (contact_trace_polyline.corner_first().equals(curr_end_corner))
               {
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(1);
               curr_other_trace_line = contact_trace_polyline.lines_arr[1];
               curr_other_prev_trace_line = contact_trace_polyline.lines_arr[2];
               }
            else
               {
               int curr_corner_no = contact_trace_polyline.corner_count() - 2;
               curr_other_trace_corner_approx = contact_trace_polyline.corner_approx(curr_corner_no);
               curr_other_trace_line = contact_trace_polyline.lines_arr[curr_corner_no + 1].opposite();
               curr_other_prev_trace_line = contact_trace_polyline.lines_arr[curr_corner_no];
               }
            PlaSide curr_prev_corner_side = curr_prev_end_corner.side_of(curr_other_trace_line);
            Signum curr_projection = line_direction.projection(curr_other_trace_line.direction());
            boolean other_trace_found = false;
            if (curr_projection == Signum.POSITIVE && curr_prev_corner_side != PlaSide.COLLINEAR)
               {
               if (curr_other_trace_line.direction().is_orthogonal())
                  {
                  acute_angle = true;
                  other_trace_found = true;
                  }
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

      if (acute_angle)
         {
         PlaDirection new_line_dir;
         if (prev_corner_side == PlaSide.ON_THE_LEFT)
            {
            new_line_dir = other_trace_line.direction().turn_45_degree(6);
            }
         else
            {
            new_line_dir = other_trace_line.direction().turn_45_degree(2);
            }
         PlaLineInt translate_line = new PlaLineInt(curr_end_corner.to_float().round(), new_line_dir);
         double translate_dist = (PlaLimits.sqrt2 - 1) * this.curr_half_width;
         double prev_corner_dist = Math.abs(translate_line.signed_distance(curr_prev_end_corner.to_float()));
         double other_dist = Math.abs(translate_line.signed_distance(other_trace_corner_approx));
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
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.lines_arr.length + 1];
            for (int i = 0; i < trace_polyline.lines_arr.length - 1; ++i)
               {
               new_lines[i] = trace_polyline.lines_arr[i];
               }
            new_lines[new_lines.length - 2] = add_line;
            new_lines[new_lines.length - 1] = other_trace_line;
            return new Polyline(new_lines);
            }
         }
      else if (bend)
         {
         PlaLineInt[] check_line_arr = new PlaLineInt[trace_polyline.lines_arr.length + 1];
         for (int i = 0; i < trace_polyline.lines_arr.length - 1; ++i)
            {
            check_line_arr[i] = trace_polyline.lines_arr[i];
            }
         check_line_arr[check_line_arr.length - 2] = other_trace_line;
         check_line_arr[check_line_arr.length - 1] = other_prev_trace_line;
         PlaLineInt new_line = reposition_line(check_line_arr, trace_polyline.lines_arr.length - 2);
         if (new_line != null)
            {
            PlaLineInt[] new_lines = new PlaLineInt[trace_polyline.lines_arr.length];
            for (int i = 0; i < new_lines.length - 2; ++i)
               {
               new_lines[i] = trace_polyline.lines_arr[i];
               }
            new_lines[new_lines.length - 2] = new_line;
            new_lines[new_lines.length - 1] = other_trace_line;
            return new Polyline(new_lines);
            }
         }
      return null;
      }
   }
