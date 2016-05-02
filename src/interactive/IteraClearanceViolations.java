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
 * ClearanceViolations.java
 *
 * Created on 3. Oktober 2004, 09:13
 */

package interactive;

import freert.graphics.GdiContext;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.LinkedList;
import board.infos.BrdItemViolation;
import board.items.BrdItem;

/**
 * To display the clearance violations between items on the screen.
 * @author alfons
 */
public final class IteraClearanceViolations
   {
   public final Collection<BrdItemViolation> violation_list = new LinkedList<BrdItemViolation>();
   
   public IteraClearanceViolations(Collection<BrdItem> p_item_list)
      {
      for ( BrdItem curr_item : p_item_list )
         {
         violation_list.addAll(curr_item.clearance_violations());
         }
      }

   public void draw(Graphics p_graphics, GdiContext p_graphics_context)
      {
      Color draw_color = p_graphics_context.get_violations_color();

      for ( BrdItemViolation curr_violation : violation_list )
         {
         double intensity = p_graphics_context.get_layer_visibility(curr_violation.layer_no);

         p_graphics_context.fill_area(curr_violation.shape, p_graphics, draw_color, intensity);
         
         // draw a circle around the violation.
         double draw_radius = curr_violation.first_item.r_board.brd_rules.get_min_trace_half_width() * 5;

         p_graphics_context.draw_circle(curr_violation.shape.centre_of_gravity(), draw_radius, 0.1 * draw_radius, draw_color, p_graphics, intensity);
         }
      }

   }
