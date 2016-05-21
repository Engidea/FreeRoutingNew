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
 * DrillPageArray.java
 *
 * Created on 26. Maerz 2006, 06:54
 *
 */

package autoroute;

import java.awt.Graphics;
import java.util.Collection;
import java.util.LinkedList;
import autoroute.expand.ExpandDrillPage;
import board.RoutingBoard;
import freert.graphics.GdiContext;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 * Describes the 2 dimensional array of pages of ExpansionDrill`s used in the maze search algorithm. 
 * The pages are rectangles of about equal width and height covering the bounding box of the board area.
 *
 * @author Alfons Wirtz
 */
public final class DrillPageArray
   {
   private final ShapeTileBox bounding_box;
   // The number of columns in the array.
   private final int column_count;
   // The number of rows in the array.
   private final int row_count;
   // The width of a single page in this array.
   private final int page_width;
   // The height of a single page in this array.
   private final int page_height;
   // as far as I can tell the whole array is initialized, no null holes
   private final ExpandDrillPage[][] page_arr;
   
   public DrillPageArray(RoutingBoard p_board, double p_max_page_width)
      {
      bounding_box = p_board.bounding_box;
      
      double length = bounding_box.box_ur.v_x - bounding_box.box_ll.v_x;
      double height = bounding_box.box_ur.v_y - bounding_box.box_ll.v_y;
      
      column_count = (int) Math.ceil(length / p_max_page_width);
      row_count = (int) Math.ceil(height / p_max_page_width);
      page_width = (int) Math.ceil(length / column_count);
      page_height = (int) Math.ceil(height / row_count);
      page_arr = new ExpandDrillPage[row_count][column_count];
      
      for (int row_idx = 0; row_idx < row_count; ++row_idx)
         {
         for (int col_idx = 0; col_idx < column_count; ++col_idx)
            {
            int ll_x = bounding_box.box_ll.v_x + col_idx * page_width;
            int ur_x = ll_x + page_width; // normally this

            if (col_idx == column_count - 1)
               {
               ur_x = bounding_box.box_ur.v_x;
               }
            
            int ll_y = bounding_box.box_ll.v_y + row_idx * page_height;
            
            int ur_y = ll_y + page_height; // normally this

            if (row_idx == row_count - 1)
               {
               ur_y = bounding_box.box_ur.v_y;
               }

            page_arr[row_idx][col_idx] = new ExpandDrillPage(new ShapeTileBox(ll_x, ll_y, ur_x, ur_y), p_board);
            }
         }
      }

   /**
    * Invalidates all drill pages intersecting with p_shape, so the they must be recalculated at the next call of get_ddrills()
    */
   public void invalidate(ShapeTile p_shape)
      {
      Collection<ExpandDrillPage> overlaps = overlapping_pages(p_shape);
      for (ExpandDrillPage curr_page : overlaps)
         {
         curr_page.invalidate();
         }
      }

   /**
    * Collects all drill pages with a 2-dimensional overlap with p_shape.
    */
   public Collection<ExpandDrillPage> overlapping_pages(ShapeTile p_shape)
      {
      Collection<ExpandDrillPage> result = new LinkedList<ExpandDrillPage>();

      ShapeTileBox shape_box = p_shape.bounding_box().intersection(this.bounding_box);

      int min_j = (int) Math.floor(((double) (shape_box.box_ll.v_y - bounding_box.box_ll.v_y)) / (double) page_height);
      double max_j = ((double) (shape_box.box_ur.v_y - bounding_box.box_ll.v_y)) / (double) page_height;

      int min_i = (int) Math.floor(((double) (shape_box.box_ll.v_x - bounding_box.box_ll.v_x)) / (double) page_width);
      double max_i = ((double) (shape_box.box_ur.v_x - bounding_box.box_ll.v_x)) / (double) page_width;

      for (int j = min_j; j < max_j; ++j)
         {
         for (int i = min_i; i < max_i; ++i)
            {
            ExpandDrillPage curr_page = page_arr[j][i];
            
            ShapeTile intersection = p_shape.intersection(curr_page.page_shape);
            
            if (intersection.dimension().is_area())
               {
               result.add(page_arr[j][i]);
               }
            }
         }
      return result;
      }

   /**
    * Resets all drill pages for autorouting the next connection.
    * this is also never caller, weird/.....
   private void reset()
      {
      for (int row_idx = 0; row_idx < page_arr.length; ++row_idx)
         {
         ExpandDrillPage[] curr_row = page_arr[row_idx];
         
         for (int col_idx = 0; col_idx < curr_row.length; ++col_idx)
            {
            curr_row[col_idx].reset();
            }
         }
      }
    */
   
   /*
    * Test draw of the all drills, apparently never called ?
    * damiano maybe it is useful to see them
    */
   public void draw(Graphics p_graphics, GdiContext p_graphics_context, double p_intensity)
      {
      for (int j = 0; j < page_arr.length; ++j)
         {
         ExpandDrillPage[] curr_row = page_arr[j];
         
         for (int i = 0; i < curr_row.length; ++i)
            {
            curr_row[i].draw(p_graphics, p_graphics_context, p_intensity);
            }
         }
      }
   }
