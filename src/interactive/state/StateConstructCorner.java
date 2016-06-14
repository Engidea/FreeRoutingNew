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
 * CornerItemConstructionState.java
 *
 * Created on 7. November 2003, 09:26
 */

package interactive.state;

import interactive.Actlog;
import interactive.IteraBoard;
import java.util.LinkedList;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;

/**
 * Common class for constructing an obstacle with a polygonal shape.
 *
 * @author Alfons Wirtz
 */
public class StateConstructCorner extends StateInteractive
   {
   // stored corners of the shape of the item under construction
   protected LinkedList<PlaPointInt> corner_list = new LinkedList<PlaPointInt>();

   protected PlaPointFloat snapped_mouse_position;
   protected boolean observers_activated = false;
   
   protected StateConstructCorner(StateInteractive p_parent_state, IteraBoard p_board_handling, Actlog p_logfile)
      {
      super(p_parent_state, p_board_handling, p_logfile);
      p_board_handling.remove_ratsnest(); // Constructing an item may change the connectivity.
      }

   /**
    * adds a corner to the polygon of the item under construction
    */
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return add_corner(p_location);
      }

   /**
    * adds a corner to the polygon of the item under construction
    */
   public StateInteractive add_corner(PlaPointFloat p_location)
      {
      PlaPointInt location = snap_to_restriction(p_location.round());
      // make shure that the coordinates are integer
      corner_list.add(location);
      i_brd.repaint();
      actlog_add_corner(p_location);
      return this;
      }

   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      return add_corner(p_point);
      }

   /**
    * stores the location of the mouse pointer after snapping it to the snap_angle
    */
   public StateInteractive mouse_moved()
      {
      super.mouse_moved();
      PlaPointInt curr_mouse_pos = i_brd.get_current_mouse_position().round();
      snapped_mouse_position = (snap_to_restriction(curr_mouse_pos)).to_float();
      i_brd.repaint();
      return this;
      }

   public javax.swing.JPopupMenu get_popup_menu()
      {
      return i_brd.get_panel().popup_menu_corneritem_construction;
      }

   /**
    * draws the polygon constructed so far as a visual aid
    */
   public void draw(java.awt.Graphics p_graphics)
      {
      int corner_count = corner_list.size();
      if (snapped_mouse_position != null)
         {
         ++corner_count;
         }
      PlaPointFloat[] corners = new PlaPointFloat[corner_count];
      java.util.Iterator<PlaPointInt> it = corner_list.iterator();
      for (int i = 0; i < corners.length - 1; ++i)
         {
         corners[i] = (it.next()).to_float();
         }
      if (snapped_mouse_position == null)
         {
         corners[corners.length - 1] = it.next().to_float();
         }
      else
         {
         corners[corners.length - 1] = snapped_mouse_position;
         }
      i_brd.gdi_context.draw(corners, 300, java.awt.Color.white, p_graphics, 0.5);
      }

   /**
    * add a corner to make the last lines fulfil the snap angle restrictions
    */
   protected void add_corner_for_snap_angle()
      {
      if (r_brd.brd_rules.is_trace_snap_none() ) return;

      PlaPointInt first_corner = corner_list.getFirst();
      PlaPointInt last_corner = corner_list.getLast();
      PlaPointInt add_corner = null;

      if (r_brd.brd_rules.is_trace_snap_45() )
         {
         add_corner = last_corner.fortyfive_degree_corner(first_corner, true);
         }
      
      if (add_corner != null)
         {
         corner_list.add(add_corner);
         }
      }

   /**
    * snaps the line from the last point in the corner_list to the input point according to this.mouse_snap_angle
    */
   private PlaPointInt snap_to_restriction(PlaPointInt p_point)
      {
      PlaPointInt result;
      
      boolean list_empty = (corner_list.size() == 0);
      
      if ( ! list_empty && r_brd.brd_rules.is_trace_snap_45() )
         {
         PlaPointInt last_corner = corner_list.getLast();
         result = p_point.fortyfive_degree_projection(last_corner);
         }
      else
         {
         result = p_point;
         }
      
      return result;
      }
   }
