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
 * Settings.java
 *
 * Created on 29. August 2003, 11:33
 */

package interactive;

import java.io.IOException;
import java.io.ObjectInputStream;
import autoroute.ArtSettings;
import board.BrdLayer;
import board.RoutingBoard;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Contains the values of the interactive settings of the board handling.
 *
 * @author Alfons Wirtz
 */
public final class IteraSettings implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // the current layer TODO transfor this in BrdLayer, a real object not an indice
   // Not as easy beacuse of serialization...
   public int layer_no;
   
   // allows pushing obstacles aside 
   public boolean push_enabled;
   // allows dragging components with the route 
   public boolean drag_components_enabled;
   // indicates if interactive selections are made on all visible layers or only on the current layer. 
   public boolean select_on_all_visible_layers;
   // Route mode: stitching or dynamic 
   public boolean is_stitch_route;
   /// The accuracy of the pull tight algorithm, the minimum move that will be done 
   public int trace_pullt_min_move;
   // Via snaps to smd center, if attach smd is allowed.
   private boolean via_snap_to_smd_center;

   // The horizontal placement grid when moving components, if > 0.
   public int horizontal_component_grid;
   // The vertical placement grid when moving components, if > 0.
   public int vertical_component_grid;

   // the trace width at static pins smaller the the trace width will be lowered automatically to the pin with, if necessary.
   private boolean automatic_neckdown;
   // Indicates if the routing rule selection is manual by the user or automatic by the net rules
   public boolean manual_rule_selection;
   // If true, the current routing obstacle is highlighted in dynamic routing. 
   private boolean hilight_routing_obstacle;

   // The index of the clearance class used for traces in interactive routing in the clearance matrix, if manual_route_selection is on.
   int manual_trace_clearance_class;
   // The index of the via rule used in routing in the board via rules if manual_route_selection is on.
   int manual_via_rule_index;
   // If true, the mouse wheel is used for zooming. 
   public boolean zoom_with_wheel;
   // The array of manual trace half widths, initially equal to the automatic trace half widths. 
   final int[] manual_trace_half_width_arr;

   public ArtSettings autoroute_settings;

   // The filter used in interactive selection of board items I am now sure it is never null
   public final ItemSelectionFilter item_selection_filter = new ItemSelectionFilter();


   // true if the data of this class are not allowed to be changed in interactive board editing. 
   private transient boolean settings_read_only = false;
   // The file used for logging interactive action, so that they can be replayed later
   private transient Actlog act_log;
   // routing board is useful to pick things from system
   private transient RoutingBoard r_board;
   
   public IteraSettings(RoutingBoard p_board, Actlog p_logfile)
      {
      r_board = p_board;
      act_log = p_logfile;

      layer_no = 0;
      push_enabled = true;
      drag_components_enabled = true;
      select_on_all_visible_layers = true; // else selection is only on the current layer
      is_stitch_route = false; // else interactive routing is dynamic
      trace_pullt_min_move = 500;
      via_snap_to_smd_center = true;
      horizontal_component_grid = 0;   // pippo
      vertical_component_grid = 0;
      automatic_neckdown = true;
      manual_rule_selection = false;
      hilight_routing_obstacle = true;
      manual_trace_clearance_class = 1;
      manual_via_rule_index = 0;
      zoom_with_wheel = true;
      
      manual_trace_half_width_arr = new int[p_board.get_layer_count()];
      
      for (int index = 0; index < manual_trace_half_width_arr.length; ++index)
         {
         manual_trace_half_width_arr[index] = 1000;
         }
      
      autoroute_settings = new ArtSettings(p_board);
      }

   public int get_layer_no()
      {
      return layer_no;
      }

   public final BrdLayer get_layer( )
      {
      return r_board.layer_structure.get(layer_no);
      }
   
   /** 
    * allows pushing obstacles aside 
    */
   public boolean is_push_enabled()
      {
      return push_enabled;
      }

   /** 
    * Route mode: stitching or dynamic 
    */
   public boolean is_stitch_route()
      {
      return is_stitch_route;
      }

   /** 
    * allows dragging components with the route 
    */
   public boolean get_drag_components_enabled()
      {
      return drag_components_enabled;
      }

   /** 
    * indicates if interactive selections are made on all visible layers or only on the current layer 
    */
   public boolean get_select_on_all_visible_layers()
      {
      return select_on_all_visible_layers;
      }

   /** 
    * Indicates if the routing rule selection is manual by the user or automatic by the net rules. 
    */
   public boolean get_manual_rule_selection()
      {
      return manual_rule_selection;
      }

   /**
    * Via snaps to smd center, if attach smd is alllowed.
    */
   public boolean is_via_snap_to_smd_center()
      {
      return via_snap_to_smd_center;
      }

   /** 
    * If true, the current routing obstacle is hilightet in dynamic routing 
    */
   public boolean is_hilight_routing_obstacle()
      {
      return hilight_routing_obstacle;
      }

   /**
    * If true, the trace width at static pins smaller the the trace width will be lowered automatically to the pin with, if
    * necessary.
    */
   public boolean is_automatic_neckdown()
      {
      return automatic_neckdown;
      }

   /** 
    * If true, the mouse wheel is used for zooming 
    */
   public boolean get_zoom_with_wheel()
      {
      return zoom_with_wheel;
      }

   /** 
    * The filter used in interactive selection of board items 
    */
   public ItemSelectionFilter get_item_selection_filter()
      {
      return item_selection_filter;
      }

   /**
    * The horizontal placement grid when moving components, if > 0.
    */
   public int get_horizontal_component_grid()
      {
      return horizontal_component_grid;
      }

   /**
    * The vertical placement grid when moving components, if > 0.
    */
   public int get_vertical_component_grid()
      {
      return vertical_component_grid;
      }

   /**
    * The index of the clearance class used for traces in interactive routing in the clearance matrix
    * if manual_route_selection is on.
    */
   public int get_manual_trace_clearance_class()
      {
      return manual_trace_clearance_class;
      }

   /**
    * The index of the via rule used in routing in the board via rules if manual_route_selection is on.
    */
   public int get_manual_via_rule_index()
      {
      return manual_via_rule_index;
      }

   /** 
    * Get the trace half width in manual routing mode on layer p_layer_no 
    */
   public int get_manual_trace_half_width(int p_layer_no)
      {
      if (p_layer_no < 0 || p_layer_no >= manual_trace_half_width_arr.length)
         {
         System.out.println("Settings.get_manual_trace_half_width p_layer_no out of range");
         return 0;
         }
      return manual_trace_half_width_arr[p_layer_no];
      }

   /**
    * The index of the via rule used in routing in the board via rules if manual_route_selection is on.
    */
   public void set_manual_via_rule_index(int p_value)
      {
      if (settings_read_only) return;

      manual_via_rule_index = p_value;
      }

   /**
    * The horizontal placement grid when moving components, if > 0.
    */
   public void set_horizontal_component_grid(int p_value)
      {
      if (settings_read_only) return;

      horizontal_component_grid = p_value;
      }

   /**
    * The vertical placement grid when moving components, if > 0.
    */
   public void set_vertical_component_grid(int p_value)
      {
      if (settings_read_only) return;

      vertical_component_grid = p_value;
      }

   /** 
    * If true, the current routing obstacle is hilightet in dynamic routing 
    */
   public void set_hilight_routing_obstacle(boolean p_value)
      {
      if (settings_read_only) return;

      hilight_routing_obstacle = p_value;
      }

   /**
    * If true, the trace width at static pins smaller the the trace width will be lowered automatically to the pin with, if
    * necessary.
    */
   public void set_automatic_neckdown(boolean p_value)
      {
      if (settings_read_only) return;

      automatic_neckdown = p_value;
      }


   /**
    * Enables or disables pushing obstacles in interactive routing
    */
   public void set_push_enabled(boolean p_value)
      {
      if (settings_read_only) return;

      push_enabled = p_value;
      
      act_log.start_scope(LogfileScope.SET_PUSH_ENABLED, p_value);
      }

   /**
    * Enables or disables dragging components
    */
   public void set_drag_components_enabled(boolean p_value)
      {
      if (settings_read_only) return;

      drag_components_enabled = p_value;
      act_log.start_scope(LogfileScope.SET_DRAG_COMPONENTS_ENABLED, p_value);
      }

   /**
    * Sets, if item selection is on all board layers or only on the current layer.
    */
   public void set_select_on_all_visible_layers(boolean p_value)
      {
      if (settings_read_only) return;

      select_on_all_visible_layers = p_value;
      act_log.start_scope(LogfileScope.SET_SELECT_ON_ALL_LAYER, p_value);
      }

   /** 
    * Route mode: stitching or dynamic 
    */
   public void set_stitch_route(boolean p_value)
      {
      if (settings_read_only) return;

      is_stitch_route = p_value;

      act_log.start_scope(LogfileScope.SET_STITCH_ROUTE, p_value);
      }

   /**
    * Changes the current width of the pull tight accuracy for traces.
    */
   public final void pull_tight_accuracy_set(int p_value)
      {
      if (settings_read_only) return;

      trace_pullt_min_move = p_value;
      act_log.start_scope(LogfileScope.SET_PULL_TIGHT_ACCURACY, p_value);
      }

   /**
    * Changes, if vias snap to smd center, if attach smd is allowed.
    */
   public void set_via_snap_to_smd_center(boolean p_value)
      {
      if (settings_read_only) return;

      via_snap_to_smd_center = p_value;
      }

   /**
    * Sets the current trace width selection to manual or automatic.
    */
   public void set_manual_tracewidth_selection(boolean p_value)
      {
      if (settings_read_only) return;

      manual_rule_selection = p_value;
      act_log.start_scope(LogfileScope.SET_MANUAL_TRACEWITH_SELECTION, p_value);
      }

   /**
    * Sets the manual trace half width used in interactive routing.
    */
   public void set_manual_trace_half_width(int p_layer_no, int p_value)
      {
      if (settings_read_only) return;

      manual_trace_half_width_arr[p_layer_no] = p_value;
      act_log.start_scope(LogfileScope.SET_MANUAL_TRACE_HALF_WIDTH, p_layer_no);
      act_log.add_int(p_value);
      }

   /**
    * The index of the clearance class used for traces in interactive routing in the clearance matrix, if manual_route_selection is
    * on.
    */
   public void set_manual_trace_clearance_class(int p_index)
      {
      if (settings_read_only) return;

      manual_trace_clearance_class = p_index;
      act_log.start_scope(LogfileScope.SET_MANUAL_TRACE_CLEARANCE_CLASS, p_index);
      }

   /**
    * If true, the wheel is used for zooming.
    */
   public void set_zoom_with_wheel(boolean p_value)
      {
      if (settings_read_only) return;

      if (zoom_with_wheel == p_value) return;
      
      zoom_with_wheel = p_value;

      act_log.start_scope(LogfileScope.SET_ZOOM_WITH_WHEEL, p_value);
      }

   /**
    * Changes the interactive selectability of p_item_type.
    */
   public void set_selectable(ItemSelectionChoice p_item_type, boolean p_value)
      {
      if (settings_read_only) return;

      item_selection_filter.set_selected(p_item_type, p_value);

      act_log.start_scope(LogfileScope.SET_SELECTABLE, p_item_type.ordinal());
      
      act_log.add(p_value);
      }

   /**
    * Defines, if the setting attributes are allowed to be changed interactively or not.
    */
   public void set_read_only(boolean p_value)
      {
      settings_read_only = p_value;
      }

   /**
    * Used to set proper value of act_log when reading objects
    * @param p_act_log
    */
   void set_transient_fields(RoutingBoard p_board, Actlog p_act_log)
      {
      r_board = p_board;
      act_log = p_act_log;
      }

   /** 
    * Reads an instance of this class from a file
    * restore transient files to a proper value 
    * note that act_log is updated using set_log method
    */
   private void readObject(ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      p_stream.defaultReadObject();
      
      settings_read_only = false;
      }

   }
