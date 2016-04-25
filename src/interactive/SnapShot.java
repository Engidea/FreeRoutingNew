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
 * SnapShot.java
 *
 * Created on 8. November 2004, 08:01
 */

package interactive;

import graphics.ColorIntensityTable;
import gui.BoardPanel;
import gui.varie.SnapshotAttributes;
import gui.varie.SubwindowSelections;
import interactive.state.StateInteractive;
import interactive.state.StateMenuDrag;
import interactive.state.StateMenuRoute;
import interactive.state.StateMenuSelect;
import java.awt.Point;

/**
 * Snapshot of the client situation in an interactive session.
 *
 * @author Alfons Wirtz
 */
public final class SnapShot implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final String name;
   public final IteraSettings settings;
   private final int interactive_state_no;
   public final graphics.GdiContext graphics_context;
   private final java.awt.Point viewport_position;
   public final SubwindowSelections subwindow_filters;

   
   /**
    * Returns a new snapshot or null, if the current interactive state is not suitable to generate a snapshot.
    */
   public static SnapShot get_instance(String p_name, IteraBoard p_board_handling)
      {
      if ( ! p_board_handling.is_StateMenu() ) return null;
      
      return new SnapShot(p_name, p_board_handling);
      }

   /** 
    * Creates a SnapShot of the display region and the interactive settings
    */
   private SnapShot(String p_name, IteraBoard p_board_handling)
      {
      BoardPanel b_panel = p_board_handling.get_panel();
      
      name = p_name;
      settings = new IteraSettings(p_board_handling.itera_settings);
      interactive_state_no = get_no(p_board_handling.interactive_state);
      graphics_context = new graphics.GdiContext(p_board_handling.gdi_context);
      viewport_position = new Point(b_panel.board_frame.get_viewport_position());
      subwindow_filters = b_panel.board_frame.get_snapshot_subwindow_selections();
      }

   public String toString()
      {
      return name;
      }

   public java.awt.Point copy_viewport_position()
      {
      if (viewport_position == null) return null;
    
      return new Point(viewport_position);
      }

   /**
    * Goes to this shnapshot in interactive board etiting.
    */
   public void go_to(IteraBoard p_board_handling)
      {
      SnapshotAttributes snapshot_attributes = settings.snapshot_attributes;

      if (snapshot_attributes.object_visibility)
         {
         p_board_handling.gdi_context.color_intensity_table = new ColorIntensityTable(graphics_context.color_intensity_table);
         }
      if (snapshot_attributes.layer_visibility)
         {
         p_board_handling.gdi_context.set_layer_visibility_arr(graphics_context.copy_layer_visibility_arr());
         }

      if (snapshot_attributes.interactive_state)
         {
         p_board_handling.set_interactive_state(get_interactive_state(p_board_handling, p_board_handling.actlog));
         }
      if (snapshot_attributes.selection_layers)
         {
         p_board_handling.itera_settings.select_on_all_visible_layers = settings.select_on_all_visible_layers;
         }
      if (snapshot_attributes.selectable_items)
         {
         p_board_handling.itera_settings.item_selection_filter.set_filter(settings.item_selection_filter);
         }
      if (snapshot_attributes.current_layer)
         {
         p_board_handling.itera_settings.layer_no = settings.layer_no;
         }
      if (snapshot_attributes.rule_selection)
         {
         p_board_handling.itera_settings.manual_rule_selection = settings.manual_rule_selection;
         }
      if (snapshot_attributes.manual_rule_settings)
         {
         p_board_handling.itera_settings.manual_trace_clearance_class = settings.manual_trace_clearance_class;
         p_board_handling.itera_settings.manual_via_rule_index = settings.manual_via_rule_index;
         System.arraycopy(settings.manual_trace_half_width_arr, 0, p_board_handling.itera_settings.manual_trace_half_width_arr, 0, p_board_handling.itera_settings.manual_trace_half_width_arr.length);
         }
      if (snapshot_attributes.push_and_shove_enabled)
         {
         p_board_handling.itera_settings.push_enabled = settings.push_enabled;
         }
      if (snapshot_attributes.drag_components_enabled)
         {
         p_board_handling.itera_settings.drag_components_enabled = settings.drag_components_enabled;
         }
      if (snapshot_attributes.pull_tight_region)
         {
         p_board_handling.itera_settings.trace_pull_tight_region_width = settings.trace_pull_tight_region_width;
         }
      if (snapshot_attributes.component_grid)
         {
         p_board_handling.itera_settings.horizontal_component_grid = settings.horizontal_component_grid;
         p_board_handling.itera_settings.vertical_component_grid = settings.vertical_component_grid;
         }
      if (snapshot_attributes.info_list_selections)
         {
         p_board_handling.get_panel().board_frame.set_snapshot_subwindow_selections(subwindow_filters);
         }
      }

   /**
    * Returns a new InterativeState from the data of this instance.
    */
   public StateInteractive get_interactive_state(IteraBoard p_board_handling, Actlog p_logfile)
      {
      StateInteractive result;
      
      if (interactive_state_no == 1)
         {
         result = new StateMenuRoute(p_board_handling, p_logfile);
         }
      else if (interactive_state_no == 2)
         {
         result = new StateMenuDrag(p_board_handling, p_logfile);
         }
      else
         {
         result = new StateMenuSelect(p_board_handling, p_logfile);
         }
      return result;
      }

   /**
    * Create a number for writing an interactive state to disk. Only MenuStates are saved. The default is SelectState.
    */
   private static int get_no(StateInteractive p_interactive_state)
      {
      int result;
      if (p_interactive_state instanceof StateMenuRoute)
         {
         result = 1;
         }
      else if (p_interactive_state instanceof StateMenuDrag)
         {
         result = 2;
         }
      else
         {
         result = 0;
         }
      return result;
      }

   }
