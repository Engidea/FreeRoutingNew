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
 * BoardHandling.java
 *
 * Created on 5. November 2003, 13:02
 *
 */
package interactive;

import freert.graphics.GdiContext;
import freert.host.HostCom;
import freert.planar.PlaCoordTransform;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.ShapePolyline;
import freert.planar.ShapeTileBox;
import freert.rules.BoardRules;
import freert.rules.NetClass;
import freert.rules.RuleNet;
import freert.varie.ItemClass;
import freert.varie.UnitMeasure;
import gui.BoardPanel;
import gui.varie.GuiResources;
import interactive.state.StateCircleConstrut;
import interactive.state.StateConstructHole;
import interactive.state.StateConstructPolygon;
import interactive.state.StateConstuctTile;
import interactive.state.StateCopyItem;
import interactive.state.StateInteractive;
import interactive.state.StateMenu;
import interactive.state.StateMenuDrag;
import interactive.state.StateMenuRoute;
import interactive.state.StateMenuSelect;
import interactive.state.StateMoveItem;
import interactive.state.StateRoute;
import interactive.state.StateSelectRegionItems;
import interactive.state.StateSelectRegionZoom;
import interactive.state.StateSelectedItem;
import interactive.varie.IteraAutorouteThread;
import interactive.varie.IteraFanoutThread;
import interactive.varie.IteraPullTightThread;
import interactive.varie.ReadActlogThread;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import main.Ldbg;
import main.Mdbg;
import main.Stat;
import specctra.DsnReadFile;
import specctra.DsnWriteSesFile;
import specctra.SpectraSesToEagle;
import specctra.SpectraWriteSesFile;
import specctra.varie.DsnReadResult;
import autoroute.batch.BatchAutorouteThread;
import board.BrdLayer;
import board.BrdLayerStructure;
import board.RoutingBoard;
import board.items.BrdAbitPin;
import board.items.BrdItem;
import board.items.BrdTracePolyline;
import board.varie.IdGenerator;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Central connection class between the graphical user interface and the board database.
 * Note that this is not serialized
 * @author Alfons Wirtz
 */
public final class IteraBoard
   {
   private static final String classname="BoardHandling.";

   private final Stat stat;
   // The board database used in this interactive handling
   private RoutingBoard r_board = null;
   // The graphical context for drawing the board. */
   public GdiContext gdi_context = null;
   // For transforming coordinates between the user and the board coordinate space */
   public PlaCoordTransform coordinate_transform = null;
   // The text message fields displayed on the screen */
   public final ScreenMessages screen_messages;
   // The current settings for interactive actions on the board */
   public IteraSettings itera_settings = null;
   // The currently active interactive state
   public StateInteractive interactive_state = null;
   // Used for running an interactive action in a separate thread.
   private BrdActionThread interactive_action_thread = null;
   // To display all incomplete connections on the screen. */
   private RatsNest ratsnest = null;
   // To display all clearance violations between items on the screen. */
   private IteraClearanceViolations clearance_violations = null;
   // The graphical panel used for displaying the board
   private final BoardPanel board_panel;
   // The file used for logging interactive action, so that they can be replayed later
   public final Actlog actlog = new Actlog();
   // True if currently a logfile is being processed. Used to prevent interactive changes of the board database in this case.
   private boolean board_is_read_only = false;
   // The current position of the mouse pointer
   private PlaPointFloat current_mouse_position = null;
   // To repaint the board immediately for example when reading a logfile.
   public boolean paint_immediately = false;
   // Pick up labels and gui pieces from here
   private final GuiResources resources;
   
   public IteraBoard(BoardPanel p_panel, Stat p_stat)
      {
      stat = p_stat;
      board_panel = p_panel;
      screen_messages = p_panel.screen_messages;
      set_interactive_state(new StateMenuSelect(this, actlog));
      resources = new GuiResources(p_stat, "interactive.resources.BoardHandling");
      }

   public Stat get_stat()
      {
      return stat;
      }
   
   /**
    * Sets the board to read only for example when running a separate action thread to avoid unsynchronized change of the board.
    * @return the previous state
    */
   public boolean set_board_read_only(boolean p_value)
      {
      boolean previous = board_is_read_only;
      
      board_is_read_only = p_value;

      itera_settings.set_read_only(p_value);
      
      return previous;
      }

   /**
    * Return true, if the board is set to read only.
    */
   public boolean is_board_read_only()
      {
      return board_is_read_only;
      }

   /**
    * Return the current language for the GUI messages.
    */
   public java.util.Locale get_locale()
      {
      return stat.locale;
      }

   /**
    * returns the number of layers of the board design.
    */
   public int get_layer_count()
      {
      if (r_board == null) return 0;

      return r_board.get_layer_count();
      }

   /**
    * Gets the routing board of this board handling.
    */
   public RoutingBoard get_routing_board()
      {
      return r_board;
      }

   /**
    * Returns the current position of the mouse pointer.
    */
   public PlaPointFloat get_current_mouse_position()
      {
      return current_mouse_position;
      }

   /**
    * Sets the current mouse position to the input point. Used while reading a logfile.
    */
   void set_current_mouse_position(PlaPointFloat p_point)
      {
      current_mouse_position = p_point;
      }

   /**
    * Tells the router, if conduction areas should be ignored..
    */
   public void set_ignore_conduction(boolean p_value)
      {
      if (board_is_read_only) return;

      r_board.set_conduction_is_obstacle(!p_value);

      actlog.start_scope(LogfileScope.SET_IGNORE_CONDUCTION, p_value);
      }

   public void set_pin_edge_to_turn_dist(double p_value)
      {
      if (board_is_read_only) return;

      double edge_to_turn_dist = coordinate_transform.user_to_board(p_value);

      double o_val = r_board.brd_rules.set_pin_edge_to_turn_dist(edge_to_turn_dist);

      // if the old value is the same as new value
      if (edge_to_turn_dist == o_val) return;

      // unfix the pin exit stubs

      for (BrdAbitPin curr_pin : r_board.get_pins())
         {
         if ( ! curr_pin.has_trace_exit_restrictions() ) continue;
         
         for (BrdItem curr_contact : curr_pin.get_normal_contacts())
            {
            if ( ! (curr_contact instanceof BrdTracePolyline) ) continue;

            BrdTracePolyline a_trace = (BrdTracePolyline) curr_contact;
            
            if ( a_trace.get_fixed_state() != ItemFixState.SHOVE_FIXED) continue;

            if (a_trace.corner_count() != 2) continue;

            curr_contact.set_fixed_state(ItemFixState.UNFIXED);
            }
         }
      }

   /**
    * Gets the trace half width used in interactive routing for the input net on the input layer.
    */
   public int get_trace_halfwidth(int p_net_no, int p_layer)
      {
      int result;
      if (itera_settings.manual_rule_selection)
         {
         result = itera_settings.manual_trace_half_width_arr[p_layer];
         }
      else
         {
         result = r_board.brd_rules.get_trace_half_width(p_net_no, p_layer);
         }
      return result;
      }

   /**
    * Returns if p_layer is active for interactive routing of traces.
    */
   public boolean is_active_routing_layer(int p_net_no, int p_layer)
      {
      if (itera_settings.manual_rule_selection) return true;
      
      RuleNet curr_net = r_board.brd_rules.nets.get(p_net_no);
      if (curr_net == null)
         {
         return true;
         }
      
      NetClass curr_net_class = curr_net.get_class();
      if (curr_net_class == null)
         {
         return true;
         }
      return curr_net_class.is_active_routing_layer(p_layer);
      }

   /** Gets the trace clearance class used in interactive routing. */
   public int get_trace_clearance_class(int p_net_no)
      {
      int result;
      if (itera_settings.manual_rule_selection)
         {
         result = itera_settings.manual_trace_clearance_class;
         }
      else
         {
         result = r_board.brd_rules.nets.get(p_net_no).get_class().get_trace_clearance_class();
         }
      return result;
      }

   /** Gets the via rule used in interactive routing. */
   public freert.rules.RuleViaInfoList get_via_rule(int p_net_no)
      {
      freert.rules.RuleViaInfoList result = null;
      if (itera_settings.manual_rule_selection)
         {
         result = r_board.brd_rules.via_rules.get(itera_settings.manual_via_rule_index);
         }
      if (result == null)
         {
         result = r_board.brd_rules.nets.get(p_net_no).get_class().get_via_rule();
         }
      return result;
      }

   /**
    * Changes the default trace halfwidth currently used in interactive routing on the input layer.
    */
   public void set_default_trace_halfwidth(int p_layer, int p_value)
      {
      if (board_is_read_only)
         {
         return;
         }
      if (p_layer >= 0 && p_layer <= r_board.get_layer_count())
         {
         r_board.brd_rules.set_default_trace_half_width(p_layer, p_value);
         actlog.start_scope(LogfileScope.SET_TRACE_HALF_WIDTH, p_layer);
         actlog.add_int(p_value);
         }
      }

   /**
    * Switches clearance compensation on or off.
    */
   public void set_clearance_compensation(boolean p_value)
      {
      if (board_is_read_only) return;

      r_board.search_tree_manager.set_clearance_compensation_used(p_value);
      
      actlog.start_scope(LogfileScope.SET_CLEARANCE_COMPENSATION, p_value);
      }

   /**
    * Changes the current snap angle in the interactive board handling.
    */
   public void set_trace_snap_angle(board.varie.TraceAngleRestriction p_snap_angle)
      {
      if (board_is_read_only) return;

      r_board.brd_rules.set_trace_snap_angle(p_snap_angle);
      
      actlog.start_scope(LogfileScope.SET_SNAP_ANGLE, p_snap_angle.get_no());
      }

   /**
    * Changes the current layer in the interactive board handling.
    * This logs the action to the actlog
    */
   public void set_current_layer(int p_layer)
      {
      if (board_is_read_only) return;

      p_layer = set_layer(p_layer);

      actlog.start_scope(LogfileScope.SET_LAYER, p_layer);
      }

   /**
    * Changes the current layer without saving the change to logfile
    * @return the actual value stored after having passed all checks
    */
   public int set_layer(int p_layer_no)
      {
      if ( p_layer_no < 0 )  p_layer_no  = 0;
      
      if ( p_layer_no >= r_board.get_layer_count() ) p_layer_no = r_board.get_layer_count() -1;
      
      // current setting is the same as new one, do nothing
      if ( itera_settings.layer_no == p_layer_no ) return p_layer_no;
      
      BrdLayer curr_layer = r_board.layer_structure.get(p_layer_no);
      
      screen_messages.show_layer_name(curr_layer);
      
      itera_settings.layer_no = p_layer_no;

      // Change the selected layer in the select parameter window.
      int signal_layer_no = r_board.layer_structure.get_signal_layer_no(curr_layer);
      
      if ( ! board_is_read_only)
         {
         board_panel.set_selected_signal_layer(signal_layer_no);
         }

      // make the layer visible, if it is invisible
      if (gdi_context.get_layer_visibility(p_layer_no) == 0)
         {
         gdi_context.set_layer_visibility(p_layer_no, 1);
         board_panel.board_frame.refresh_windows();
         }
      
      gdi_context.set_fully_visible_layer(p_layer_no);
      
      repaint();
      
      return p_layer_no;
      }

   /**
    * Changes the visibility of the input layer to the input value. p_value is expected between 0 and 1
    * @return the given layer or a different one if the given layer is invisible ir invalid
    */
   public int set_layer_visibility(int p_layer_no, double p_visibility)
      {
      if ( ! gdi_context.is_valid_layer(p_layer_no) ) 
         return gdi_context.get_layer_visibility_best();

      // here layer is valid, let me set the visibility
      gdi_context.set_layer_visibility(p_layer_no, p_visibility);

      if ( p_visibility != 0 || itera_settings.layer_no != p_layer_no) return p_layer_no;
      
      // current layer has become invisible, return the best visible layer
      return gdi_context.get_layer_visibility_best();
      }
   
   
   /**
    * Displays the current layer in the layer message field, and clears the field for the additional message.
    */
   public void display_layer_messsage()
      {
      screen_messages.clear_add_field();
      BrdLayer curr_layer = itera_settings.get_layer();
      screen_messages.show_layer_name(curr_layer);
      }

   /**
    * Initializes the manual trace widths from the default trace widths in the board rules.
    */
   public void initialize_manual_trace_half_widths()
      {
      for (int i = 0; i < itera_settings.manual_trace_half_width_arr.length; ++i)
         {
         itera_settings.manual_trace_half_width_arr[i] = r_board.brd_rules.get_default_net_class().get_trace_half_width(i);
         }
      }

   /**
    * Sets the manual trace half width used in interactive routing. If p_layer_no < 0, the manual trace half width is changed on all
    * layers.
    */
   public void set_manual_trace_half_width(int p_layer_no, int p_value)
      {
      if (p_layer_no == gui.ComboBoxLayer.ALL_LAYER_INDEX)
         {
         for (int i = 0; i < itera_settings.manual_trace_half_width_arr.length; ++i)
            {
            itera_settings.set_manual_trace_half_width(i, p_value);
            }
         }
      else if (p_layer_no == gui.ComboBoxLayer.INNER_LAYER_INDEX)
         {
         for (int i = 1; i < itera_settings.manual_trace_half_width_arr.length - 1; ++i)
            {
            itera_settings.set_manual_trace_half_width(i, p_value);
            }
         }
      else
         {
         itera_settings.set_manual_trace_half_width(p_layer_no, p_value);
         }
      }

   /**
    * Changes the interactive selectability of p_item_type.
    */
   public void set_selectable(ItemSelectionChoice p_item_type, boolean p_selectable)
      {
      itera_settings.set_selectable(p_item_type, p_selectable);
      
      if ( p_selectable ) return;
      
      if ( is_StateSelectedItem() )
         {
         // if the item is not selectable filter it out
         set_interactive_state(((StateSelectedItem) interactive_state).filter());
         }
      }

   /**
    * Displays all incomplete connections, if they are not visible, or hides them, if they are visible.
    */
   public void toggle_ratsnest()
      {
      if (ratsnest == null || ratsnest.is_hidden())
         {
         create_ratsnest();
         }
      else
         {
         ratsnest = null;
         }
      repaint();
      }

   public void toggle_clearance_violations()
      {
      if ( debug (Mdbg.CLRVIOL, Ldbg.TRACE )) userPrintln(classname+"toggle_clearance_violations: start");

      if (clearance_violations == null)
         {
         clearance_violations = new IteraClearanceViolations(r_board.get_items());
         Integer violation_count = new Integer((clearance_violations.violation_list.size() + 1) / 2);
         String curr_message = violation_count.toString() + " " + resources.getString("clearance_violations_found");
         screen_messages.set_status_message(curr_message);
         }
      else
         {
         clearance_violations = null;
         screen_messages.set_status_message("");
         }
      
      repaint();
      }

   /**
    * Displays all incomplete connections.
    */
   public void create_ratsnest()
      {
      ratsnest = new RatsNest(r_board, stat);
      Integer incomplete_count = ratsnest.incomplete_count();
      Integer length_violation_count = ratsnest.length_violation_count();
      String curr_message;
      if (length_violation_count == 0)
         {
         curr_message = incomplete_count.toString() + " " + resources.getString("incomplete_connections_to_route");
         }
      else
         {
         curr_message = incomplete_count.toString() + " " + resources.getString("incompletes") + " " + length_violation_count.toString() + " " + resources.getString("length_violations");
         }
      screen_messages.set_status_message(curr_message);
      }

   /**
    * Recalculates the incomplete connections for the input net.
    */
   public void update_ratsnest(int p_net_no)
      {
      if (ratsnest != null && p_net_no > 0)
         {
         ratsnest.recalculate(p_net_no, r_board);
         ratsnest.show();
         }
      }

   /**
    * Recalculates the incomplete connections for the input net for the items in p_item_list.
    */
   public void update_ratsnest(int p_net_no, Collection<BrdItem> p_item_list)
      {
      if (ratsnest != null && p_net_no > 0)
         {
         ratsnest.recalculate(p_net_no, p_item_list, r_board);
         ratsnest.show();
         }
      }

   /**
    * Recalculates the incomplete connections, if the ratsnest is active.
    */
   public void update_ratsnest()
      {
      if (ratsnest != null)
         {
         ratsnest = new RatsNest(r_board, stat);
         }
      }

   /**
    * Hides the incomplete connections on the screen.
    */
   public void hide_ratsnest()
      {
      if (ratsnest != null)
         {
         ratsnest.hide();
         }
      }

   /**
    * Shows the incomplete connections on the screen, if the ratsnest is active.
    */
   public void show_ratsnest()
      {
      if (ratsnest != null)
         {
         ratsnest.show();
         }
      }

   /**
    * Removes the incomplete connections.
    */
   public void remove_ratsnest()
      {
      ratsnest = null;
      }

   /**
    * Returns the ratsnest with the information about the incomplete connections.
    */
   public RatsNest get_ratsnest()
      {
      if (ratsnest == null) ratsnest = new RatsNest(r_board, stat);
      
      return ratsnest;
      }

   public void recalculate_length_violations()
      {
      if (ratsnest != null)
         {
         if (ratsnest.recalculate_length_violations())
            {
            if (!ratsnest.is_hidden())
               {
               repaint();
               }
            }
         }
      }

   /**
    * Sets the visibility filter for the incomplete of the input net.
    */
   public void set_incompletes_filter(int p_net_no, boolean p_value)
      {
      if (ratsnest != null)
         {
         ratsnest.set_filter(p_net_no, p_value);
         }
      }

   /**
    * Creates the Routingboard, the graphic context and the interactive settings
    */
   public void create_board(
         ShapeTileBox p_bounding_box, 
         BrdLayerStructure p_layer_structure, 
         ShapePolyline[] p_outline_shapes, 
         String p_outline_clearance_class_name, 
         BoardRules p_rules,
         HostCom p_communication )
      {
      if ( r_board != null)
         {
         stat.userPrintln(classname+"create_board: board already created"); 
         return;
         }
      
      int outline_cl_class_no = 0;

      if (p_rules != null)
         {
         if (p_outline_clearance_class_name != null && p_rules.clearance_matrix != null)
            {
            outline_cl_class_no = p_rules.clearance_matrix.get_no(p_outline_clearance_class_name);
            outline_cl_class_no = Math.max(outline_cl_class_no, 0);
            }
         else
            {
            outline_cl_class_no = p_rules.get_default_net_class().default_item_clearance_classes.get(ItemClass.AREA);
            }
         }
      
      r_board = new RoutingBoard(p_bounding_box, p_layer_structure, p_outline_shapes, outline_cl_class_no, p_rules, p_communication, stat);

      // create the interactive settings with default
      double unit_factor = p_communication.coordinate_transform.board_to_dsn(1);
      coordinate_transform = new PlaCoordTransform(1, p_communication.unit, unit_factor, p_communication.unit);
      itera_settings = new IteraSettings(r_board, actlog);

      // create a graphics context for the board
      Dimension panel_size = board_panel.getPreferredSize();
      gdi_context = new GdiContext(p_bounding_box, panel_size, p_layer_structure, stat);
      
      
      
      }

   /**
    * Changes the factor of the user unit.
    */
   public void change_user_unit_factor(double p_new_factor)
      {
      PlaCoordTransform old_transform = coordinate_transform;
      coordinate_transform = new PlaCoordTransform(p_new_factor, old_transform.user_unit, old_transform.board_unit_factor, old_transform.board_unit);
      }

   /**
    * Changes the user unit.
    */
   public void change_user_unit(UnitMeasure p_unit)
      {
      PlaCoordTransform old_transform = coordinate_transform;
      coordinate_transform = new PlaCoordTransform(old_transform.user_unit_factor, p_unit, old_transform.board_unit_factor, old_transform.board_unit);
      }

   /**
    * From here on the interactive actions are written to a logfile.
    */
   public void start_logfile(File p_filename)
      {
      if (board_is_read_only)
         {
         return;
         }
      actlog.start_write(p_filename);
      }

   /**
    * Repaints the board panel on the screen.
    */
   public void repaint()
      {
      if (paint_immediately)
         {
         final Rectangle MAX_RECTAMGLE = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
         board_panel.paintImmediately(MAX_RECTAMGLE);
         }
      else
         {
         board_panel.repaint();
         }
      }

   /**
    * Repaints a rectangle of board panel on the screen.
    */
   public void repaint(Rectangle p_rect)
      {
      if (paint_immediately)
         {
         board_panel.paintImmediately(p_rect);
         }
      else
         {
         board_panel.repaint(p_rect);
         }
      }

   /**
    * Gets the panel for graphical display of the board.
    */
   public BoardPanel get_panel()
      {
      return board_panel;
      }

   /**
    * Gets the popup menu used in the current interactive state. Returns null, if the current state uses no popup menu.
    */
   public javax.swing.JPopupMenu get_current_popup_menu()
      {
      javax.swing.JPopupMenu result;
      if (interactive_state != null)
         {
         result = interactive_state.get_popup_menu();
         }
      else
         {
         result = null;
         }
      return result;
      }

   /**
    * Draws the board and all temporary construction graphics in the current interactive state.
    */
   public void draw(Graphics p_graphics)
      {
      if (r_board == null)
         {
         return;
         }
      r_board.draw(p_graphics, gdi_context);

      if (ratsnest != null)
         {
         ratsnest.draw(p_graphics, gdi_context);
         }
      if (clearance_violations != null)
         {
         clearance_violations.draw(p_graphics, gdi_context);
         }
      if (interactive_state != null)
         {
         interactive_state.draw(p_graphics);
         }
      if (interactive_action_thread != null)
         {
         interactive_action_thread.draw(p_graphics);
         }
      }

   public void generate_snapshot()
      {
      if (board_is_read_only)
         {
         return;
         }
      r_board.generate_snapshot();
      actlog.start_scope(LogfileScope.GENERATE_SNAPSHOT);
      }

   /**
    * Restores the situation before the previous snapshot.
    */
   public void undo()
      {
      if (board_is_read_only || !(interactive_state instanceof StateMenu))
         {
         return;
         }
      java.util.Set<Integer> changed_nets = new java.util.TreeSet<Integer>();
      if (r_board.undo(changed_nets))
         {
         for (Integer changed_net : changed_nets)
            {
            update_ratsnest(changed_net);
            }
         if (changed_nets.size() > 0)
            {
            // reset the start pass number in the autorouter in case
            // a batch autorouter is undone.
            itera_settings.autoroute_settings.pass_no_set(1);
            }
         screen_messages.set_status_message(resources.getString("undo"));
         }
      else
         {
         screen_messages.set_status_message(resources.getString("no_more_undo_possible"));
         }
      actlog.start_scope(LogfileScope.UNDO);
      repaint();
      }

   /**
    * Restores the sitiation before the last undo.
    */
   public void redo()
      {
      if (board_is_read_only || !(interactive_state instanceof StateMenu))
         {
         return;
         }
      java.util.Set<Integer> changed_nets = new java.util.TreeSet<Integer>();
      if (r_board.redo(changed_nets))
         {
         for (Integer changed_net : changed_nets)
            {
            update_ratsnest(changed_net);
            }
         screen_messages.set_status_message(resources.getString("redo"));
         }
      else
         {
         screen_messages.set_status_message(resources.getString("no_more_redo_possible"));
         }
      actlog.start_scope(LogfileScope.REDO);
      repaint();
      }

   /**
    * Actions to be taken in the current interactive state when the left mouse butten is clicked.
    */
   public void left_button_clicked(Point2D p_point)
      {
      if (board_is_read_only)
         {
         if ( interactive_action_thread != null)
            {
            // The left button is used to stop the interactive action thread.
            interactive_action_thread.request_stop();
            }
         return;
         }
      
      if ( interactive_state == null ) return;
      
      if ( gdi_context == null ) return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      
      StateInteractive return_state = interactive_state.left_button_clicked(location);
      
      set_interactive_state(return_state);
      }

   /**
    * Actions to be taken in the current interactive state when the mouse pointer has moved.
    */
   public void mouse_moved(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;
      
      if (interactive_state != null && gdi_context != null)
         {
         current_mouse_position = gdi_context.coordinate_transform.screen_to_board(p_point);
         StateInteractive return_state = interactive_state.mouse_moved();
         // An automatic repaint here would slow down the display
         // performance in interactive route.
         // If a repaint is necessary, it should be done in the individual mouse_moved
         // method of the class derived from InteractiveState
         if (return_state != interactive_state)
            {
            set_interactive_state(return_state);
            repaint();
            }
         }
      }

   /**
    * Actions to be taken when the mouse button is pressed.
    */
   public void mouse_pressed(Point2D p_point)
      {
      if (interactive_state != null && gdi_context != null)
         {
         current_mouse_position = gdi_context.coordinate_transform.screen_to_board(p_point);
         set_interactive_state(interactive_state.mouse_pressed(current_mouse_position));
         }
      }

   /**
    * Actions to be taken in the current interactive state when the mouse is dragged.
    */
   public void mouse_dragged(Point2D p_point)
      {
      if (interactive_state != null && gdi_context != null)
         {
         current_mouse_position = gdi_context.coordinate_transform.screen_to_board(p_point);
         StateInteractive return_state = interactive_state.mouse_dragged(current_mouse_position);
         if (return_state != interactive_state)
            {
            set_interactive_state(return_state);
            repaint();
            }
         }
      }

   /**
    * Actions to be taken in the current interactive state when a mouse button is released.
    */
   public void button_released()
      {
      if (interactive_state != null)
         {
         StateInteractive return_state = interactive_state.button_released();
         if (return_state != interactive_state)
            {
            set_interactive_state(return_state);
            repaint();
            }
         }
      }

   /**
    * Actions to be taken in the current interactive state when the mouse wheel is moved
    * This is called by the GUI callback
    */
   public void mouse_wheel_moved(int p_rotation)
      {
      // this calls the handler, the appropriate one
      StateInteractive return_state = interactive_state.mouse_wheel_moved(p_rotation);

      if (return_state != interactive_state)
         {
         set_interactive_state(return_state);
         repaint();
         }
      }

   /**
    * Action to be taken in the current interactive state when a key on the keyboard is typed.
    */
   public void key_typed_action(char p_key_char)
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;

      StateInteractive return_state = interactive_state.key_typed(p_key_char);
      
      if (return_state != null && return_state != interactive_state)
         {
         set_interactive_state(return_state);
         board_panel.board_frame.hilight_selected_button();
         repaint();
         }

      }

   /**
    * Completes the curreent interactive state and returns to its return state.
    */
   public void return_from_state()
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;

      StateInteractive new_state = interactive_state.complete();

      if (new_state != interactive_state)
         {
         set_interactive_state(new_state);
         repaint();
         }
      }

   /**
    * Cancels the current interactive state.
    */
   public void cancel_state()
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;

      StateInteractive new_state = interactive_state.cancel();

      if (new_state != interactive_state)
         {
         set_interactive_state(new_state);
         repaint();
         }
      }

   /**
    * Actions to be taken in the current interactive state when the current board layer is changed. Returns false, if the layer
    * change failed.
    */
   public boolean change_layer_action(int p_new_layer)
      {
      boolean result = true;
      if (interactive_state != null && !board_is_read_only)
         {
         result = interactive_state.change_layer_action(p_new_layer);
         }
      return result;
      }

   /**
    * Sets the interactive state to SelectMenuState
    */
   public void set_select_menu_state()
      {
      interactive_state = new StateMenuSelect(this, actlog);
      screen_messages.set_status_message(resources.getString("select_menu"));
      }

   /**
    * Sets the interactive state to RouteMenuState
    */
   public void set_route_menu_state()
      {
      interactive_state = new StateMenuRoute(this, actlog);
      screen_messages.set_status_message(resources.getString("route_menu"));
      }

   /**
    * Sets the interactive state to DragMenuState
    */
   public void set_drag_menu_state()
      {
      interactive_state = new StateMenuDrag(this, actlog);
      screen_messages.set_status_message(resources.getString("drag_menu"));
      }


   /**
    * Imports a board design from a Specctra dsn-file. 
    * The parameters  p_item_id_no_generator are used, in case the board is embedded into a host system. 
    * Returns false, if the dsn-file is currupted.
    * @return true if all is fine
    */
   public boolean import_design( InputStream p_design, IdGenerator p_item_id_no_generator, Stat p_stat)
      {
      if (p_design == null)
         throw new IllegalArgumentException("import_design: p_design == null");
      
      DsnReadResult read_result;
      
      try
         {
         DsnReadFile reader = new DsnReadFile(this, p_design );
         read_result = reader.read( p_item_id_no_generator );
         p_design.close();
         }
      catch (Exception exc)
         {
         stat.userPrintln("import_design", exc);
         return false;
         }

      if (read_result != DsnReadResult.OK) return false;
      
      r_board.reduce_nets_of_route_items();
      
      set_layer(0);
      
      for (int index = 0; index < r_board.get_layer_count(); ++index)
         {
         if (!itera_settings.autoroute_settings.get_layer_active(index))
            {
            gdi_context.set_layer_visibility(index, 0);
            }
         }
      
      return true;
      }

   /**
    * Writes the currently edited board design to a text file in the Specctra dsn format. If p_compat_mode is true, only standard
    * speecctra dsn scopes are written, so that any host system with an specctra interface can read them.
    */
   public boolean export_to_dsn_file(OutputStream p_output_stream, String p_design_name, boolean p_compat_mode)
      {
      if (board_is_read_only || p_output_stream == null) return false;
      
      DsnWriteSesFile writer = new DsnWriteSesFile(this, p_output_stream );

      return writer.write( p_design_name, p_compat_mode);
      }

   /**
    * Writes a session file ins the Eaglea scr format.
    */
   public boolean export_eagle_session_file(java.io.InputStream p_input_stream, OutputStream p_output_stream)
      {
      if (board_is_read_only) return false;

      return SpectraSesToEagle.get_instance(p_input_stream, p_output_stream, r_board);
      }

   /**
    * Writes a session file ins the Specctra ses-format.
    */
   public boolean export_specctra_session_file(String p_design_name, OutputStream p_output_stream)
      {
      if (board_is_read_only) return false;
      
      SpectraWriteSesFile writer = new SpectraWriteSesFile(stat, r_board, p_output_stream); 
      
      return writer.write(p_design_name);
      
      }

   /**
    * Saves the currently edited board design to p_design_file.
    * @throws IOException 
    */
   public void save_design_file(java.io.ObjectOutputStream p_object_stream) throws IOException
      {
      p_object_stream.writeObject(r_board);
      p_object_stream.writeObject(itera_settings);
      p_object_stream.writeObject(coordinate_transform);
      p_object_stream.writeObject(gdi_context);
      }

   /**
    * Reads an existing board design from the input stream
    * @throws IOException 
    * @throws ClassNotFoundException 
    * @throws some exception if loading fails
    */
   public void read_design(ObjectInputStream p_design) throws ClassNotFoundException, IOException
      {
      r_board = (RoutingBoard) p_design.readObject();

      r_board.set_transient_item(this);
      
      itera_settings = (IteraSettings) p_design.readObject();
      itera_settings.set_transient_fields(r_board, actlog);
      coordinate_transform = (PlaCoordTransform) p_design.readObject();
      gdi_context = (GdiContext) p_design.readObject();

      gdi_context.set_transient_field(stat);
      
      screen_messages.show_layer_name(itera_settings.get_layer());
      }
   
   /**
    * Processes the actions stored in the input logfile.
    */
   public void read_logfile(InputStream p_input_stream)
      {
      if (board_is_read_only ) return;
      
      if ( ! (interactive_state instanceof StateMenu)) return;

      interactive_action_thread = new ReadActlogThread(this, p_input_stream);
      interactive_action_thread.start();
      }

   /**
    * Closes all currently used files so that the file buffers are written to disk.
    */
   public void close_files()
      {
      if (actlog != null)
         {
         actlog.close_output();
         }
      }

   /**
    * Starts interactive routing at the input location.
    */
   public void start_route(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;
      
      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      
      StateInteractive new_state = StateRoute.get_instance(location, interactive_state, this, actlog);
      
      set_interactive_state(new_state);
      }

   /**
    * Selects board items at the input location.
    */
   public void select_items(Point2D p_point)
      {
      if (board_is_read_only ) return;
      
      if ( ! (interactive_state instanceof StateMenu) ) return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      StateInteractive return_state = ((StateMenu) interactive_state).select_items(location);
      set_interactive_state(return_state);
      }

   /**
    * Selects all items in an interactive defined rectangle.
    */
   public void select_items_in_region()
      {
      if (board_is_read_only ) return;
      
      if (!( interactive_state instanceof StateMenu)) return;

      set_interactive_state(StateSelectRegionItems.get_instance(interactive_state, this, actlog));
      }

   /**
    * Selects all items in the input collection.
    */
   public void select_items(Set<BrdItem> p_items)
      {
      // no interactive action when logfile is running
      if (board_is_read_only) return;
      
      display_layer_messsage();
      
      if (interactive_state instanceof StateMenu)
         {
         set_interactive_state(StateSelectedItem.get_instance(p_items, interactive_state, this, actlog));
         }
      else if (interactive_state instanceof StateSelectedItem)
         {
         ((StateSelectedItem) interactive_state).get_item_list().addAll(p_items);
         repaint();
         }
      }

   /**
    * Looks for a swappable pin at p_location. Prepares for pin swap if a swappable pin was found.
    */
   public void swap_pin(Point2D p_location)
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateMenu() ) return;
      
      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_location);
      StateInteractive return_state = ((StateMenu) interactive_state).swap_pin(location);
      set_interactive_state(return_state);
      }

   /**
    * Displays a window containing all selected items.
    */
   public void zoom_selection()
      {
      if (! is_StateSelectedItem() ) return;

      ShapeTileBox bounding_box = r_board.get_bounding_box(((StateSelectedItem) interactive_state).get_item_list());
      bounding_box = bounding_box.offset(r_board.brd_rules.get_max_trace_half_width());
      Point2D lower_left = gdi_context.coordinate_transform.board_to_screen(bounding_box.box_ll.to_float());
      Point2D upper_right = gdi_context.coordinate_transform.board_to_screen(bounding_box.box_ur.to_float());
      board_panel.zoom_frame(lower_left, upper_right);
      }

   /**
    * Picks item at p_point. Removes it from the selected_items list, if it is already in there, otherwise adds it to the list.
    * Changes to the selected items state, if something was selected.
    */
   public void toggle_select_action(Point2D p_point)
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      StateInteractive return_state = ((StateSelectedItem) interactive_state).toggle_select(location);
      if (return_state != interactive_state)
         {
         set_interactive_state(return_state);
         repaint();
         }
      }

   /**
    * Fixes the selected items.
    */
   public void fix_selected_items()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      ((StateSelectedItem) interactive_state).fix_items();
      }

   /**
    * Unfixes the selected items.
    */
   public void unfix_selected_items()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      ((StateSelectedItem) interactive_state).unfix_items();
      }

   /**
    * Displays information about the selected item into a graphical text window.
    */
   public void display_selected_item_info()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      ((StateSelectedItem) interactive_state).info_display();
      }

   /**
    * Makes all selected items connectable and assigns them to a new net.
    */
   public void assign_selected_to_new_net()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateInteractive new_state = ((StateSelectedItem) interactive_state).assign_items_to_new_net();
      set_interactive_state(new_state);
      }

   /**
    * Assigns all selected items to a new group ( new component for example)
    */
   public void assign_selected_to_new_group()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateInteractive new_state = ((StateSelectedItem) interactive_state).assign_items_to_new_group();
      set_interactive_state(new_state);
      }

   /**
    * Deletes all unfixed selected items.
    */
   public void delete_selected_items()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateInteractive new_state = ((StateSelectedItem) interactive_state).delete_items();
      set_interactive_state(new_state);
      }

   /**
    * Deletes all unfixed selected traces and vias inside a rectangle.
    */
   public void cutout_selected_items()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateInteractive new_state = ((StateSelectedItem) interactive_state).cutout_items();
      set_interactive_state(new_state);
      }

   /**
    * Assigns the input clearance class to the selected items
    */
   public void assign_clearance_classs_to_selected_items(int p_cl_class_index)
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateInteractive new_state = ((StateSelectedItem) interactive_state).assign_clearance_class(p_cl_class_index);
      set_interactive_state(new_state);
      }

   /**
    * Moves or rotates the selected items
    */
   public void move_selected_items(Point2D p_from_location)
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateSelectedItem curr_state = (StateSelectedItem) interactive_state;
      Collection<BrdItem> item_list = curr_state.get_item_list();
      PlaPointFloat from_location = gdi_context.coordinate_transform.screen_to_board(p_from_location);
      StateInteractive new_state = StateMoveItem.get_instance(from_location, item_list, interactive_state, this, actlog);
      set_interactive_state(new_state);
      repaint();
      }

   /**
    * Copies all selected items.
    */
   public void copy_selected_items(Point2D p_from_location)
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      StateSelectedItem curr_state = (StateSelectedItem) interactive_state;
      curr_state.extent_to_whole_components();
      Collection<BrdItem> item_list = curr_state.get_item_list();
      PlaPointFloat from_location = gdi_context.coordinate_transform.screen_to_board(p_from_location);
      StateInteractive new_state = StateCopyItem.get_instance(from_location, item_list, interactive_state.return_state, this, actlog);
      set_interactive_state(new_state);
      }

   /**
    * Optimizes the selected items.
    * May be called by a key typed in the selected state or by a menu
    */
   public void optimize_selected_items()
      {
      if ( board_is_read_only ) return;
      
      if ( !(interactive_state instanceof StateSelectedItem)) return;
      
      r_board.generate_snapshot();
      interactive_action_thread = new IteraPullTightThread(this);
      interactive_action_thread.start();
      }

   /**
    * Autoroute the selected items.
    */
   public void autoroute_selected_items()
      {
      if (board_is_read_only ) return;
      
      // basically nothing is selected
      if ( !(interactive_state instanceof StateSelectedItem)) return;
      
      r_board.generate_snapshot();
      
      interactive_action_thread = new IteraAutorouteThread(this);
      interactive_action_thread.start();
      }

   /**
    * Fanouts the selected items.
    */
   public void fanout_selected_items()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      r_board.generate_snapshot();
      interactive_action_thread = new IteraFanoutThread(this);
      interactive_action_thread.start();
      }

   /**
    * Start the batch autorouter on the whole Board
    */
   public void start_batch_autorouter()
      {
      if (board_is_read_only)
         {
         stat.userPrintln(classname+"start_batch_autorouter: FAIL board is read only");
         return;
         }
      
      r_board.generate_snapshot();
      interactive_action_thread =  new BatchAutorouteThread(this);
      interactive_action_thread.start();
      }

   /**
    * Selects also all items belonging to a net of a currently selecte item.
    */
   public void extend_selection_to_whole_nets()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      set_interactive_state(((StateSelectedItem) interactive_state).extent_to_whole_nets());
      }

   /**
    * Selects also all items belonging to a component of a currently selecte item.
    */
   public void extend_selection_to_whole_components()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      set_interactive_state(((StateSelectedItem) interactive_state).extent_to_whole_components());
      }

   /**
    * Selects also all items belonging to a connected set of a currently selecte item.
    */
   public void extend_selection_to_whole_connected_sets()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      set_interactive_state(((StateSelectedItem) interactive_state).extent_to_whole_connected_sets());
      }

   /**
    * Selects also all items belonging to a connection of a currently selecte item.
    */
   public void extend_selection_to_whole_connections()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      set_interactive_state(((StateSelectedItem) interactive_state).extent_to_whole_connections());
      }

   /**
    * Shows or hides the clearance violations of the selected items.
    */
   public void toggle_selected_item_violations()
      {
      if (board_is_read_only ) return;
      
      if ( ! is_StateSelectedItem()) return;

      ((StateSelectedItem) interactive_state).toggle_clearance_violations();
      }

   public void turn_45_degree(int p_factor)
      {
      if (board_is_read_only || !(interactive_state instanceof StateMoveItem))
         {
         // no interactive action when logfile is running
         return;
         }
      ((StateMoveItem) interactive_state).turn_45_degree(p_factor);
      }

   public void change_placement_side()
      {
      if (board_is_read_only || !(interactive_state instanceof StateMoveItem))
         {
         // no interactive action when logfile is running
         return;
         }
      ((StateMoveItem) interactive_state).change_placement_side();
      }

   /**
    * Zooms display to an interactive defined rectangle.
    */
   public void zoom_region()
      {
      interactive_state = new StateSelectRegionZoom(interactive_state, this, actlog, null);
      }

   /**
    * Start interactively creating a circle obstacle.
    */
   public void start_circle(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only)  return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      set_interactive_state(StateCircleConstrut.get_instance(location, interactive_state, this, actlog));
      }

   /**
    * Start interactively creating a tile shaped obstacle.
    */
   public void start_tile(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only)  return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      set_interactive_state(new StateConstuctTile(location, interactive_state, this, actlog));
      }

   /**
    * Start interactively creating a polygon shaped obstacle.
    */
   public void start_polygonshape_item(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only)  return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      set_interactive_state(StateConstructPolygon.get_instance(location, interactive_state, this, actlog));
      }

   /**
    * Actions to be taken, when adding a hole to an existing obstacle shape on the board is started.
    */
   public void start_adding_hole(Point2D p_point)
      {
      // no interactive action when logfile is running
      if (board_is_read_only)  return;

      PlaPointFloat location = gdi_context.coordinate_transform.screen_to_board(p_point);
      StateInteractive new_state = StateConstructHole.get_instance(location, interactive_state, this, actlog);
      set_interactive_state(new_state);
      }

   /**
    * Gets a surrounding rectangle of the area, where an update of the graphics is needed caused by the previous interactive
    * actions.
    */
   public Rectangle get_graphics_update_rectangle()
      {
      Rectangle result;
      ShapeTileBox update_box = r_board.get_graphics_update_box();
      if (update_box == null || update_box.is_empty())
         {
         result = new Rectangle(0, 0, 0, 0);
         }
      else
         {
         ShapeTileBox offset_box = update_box.offset(r_board.get_max_trace_half_width());
         result = gdi_context.coordinate_transform.board_to_screen(offset_box);
         }
      return result;
      }

   /**
    * Gets all items at p_location on the active board layer. 
    * If nothing is found on the active layer and settings.select_on_all_layers is true, all layers are selected.
    */
   public Set<BrdItem> pick_items(PlaPointFloat p_location)
      {
      return pick_items(p_location, itera_settings.item_selection_filter);
      }

   /**
    * Gets all items at p_location on the active board layer with the inputt item filter. 
    * If nothing is found on the active layer and settings.select_on_all_layers is true, all layers are selected.
    */
   public Set<BrdItem> pick_items(PlaPointFloat p_location, ItemSelectionFilter p_item_filter)
      {
      PlaPointInt location = p_location.round();
      
      Set<BrdItem> result = r_board.pick_items(location, itera_settings.get_layer_no(), p_item_filter);
      
      if ( ! ( result.size() == 0 && itera_settings.select_on_all_visible_layers ) ) return result;
      
      for (int index = 0; index < gdi_context.layer_count(); ++index)
         {
         if (index == itera_settings.layer_no || gdi_context.get_layer_visibility(index) <= 0)  continue;
         
         result.addAll(r_board.pick_items(location, index, p_item_filter));
         }

      return result;
      }

   /**
    * Moves the mouse pointer to p_to_location.
    */
   public void move_mouse(PlaPointFloat p_to_location)
      {
      if ( board_is_read_only) return;

      board_panel.move_mouse(gdi_context.coordinate_transform.board_to_screen(p_to_location));
      }

   /**
    * Gets the current interactive state.
    */
   public StateInteractive get_interactive_state()
      {
      return interactive_state;
      }

   public void set_interactive_state(StateInteractive p_state)
      {
      if ( p_state == null ) return;
      
      if ( p_state == interactive_state ) return;
      
      interactive_state = p_state;

      if (!board_is_read_only)
         {
         p_state.set_toolbar();
         board_panel.board_frame.set_context_sensitive_help(board_panel, p_state.get_help_id());
         }
      }

   /**
    * returns true if the iteractive_State is an istance of StateSelectedItem
    * @return 
    */
   public boolean is_StateSelectedItem ( )
      {
      if ( interactive_state == null ) return false;
      
      return interactive_state instanceof StateSelectedItem;
      }
   
   public boolean is_StateMenu ( )
      {
      if ( interactive_state == null ) return false;
      
      return interactive_state instanceof StateMenu;
      }
   
   
   
   
   /**
    * Adjust the design bounds, so that also all items being still placed outside the board outline are contained in the new bounds.
    */
   public void adjust_design_bounds()
      {
      ShapeTileBox new_bounding_box = r_board.get_bounding_box();
      Collection<BrdItem> board_items = r_board.get_items();
      for (BrdItem curr_item : board_items)
         {
         ShapeTileBox curr_bounding_box = curr_item.bounding_box();
         if (curr_bounding_box.box_ur.v_x < Integer.MAX_VALUE)
            {
            new_bounding_box = new_bounding_box.union(curr_bounding_box);
            }
         }
      gdi_context.change_design_bounds(new_bounding_box);
      }

   /**
    * Sets all references inside this class to null, so that it can be recycled by the garbage collector.
    */
   public void dispose()
      {
      close_files();
      gdi_context = null;
      coordinate_transform = null;
      itera_settings = null;
      interactive_state = null;
      ratsnest = null;
      clearance_violations = null;
      r_board = null;
      }
   
   public final void userPrintln ( String message )
      {
      stat.userPrintln(message);      
      }
   
   public final void userPrintln ( String message, Exception exc )
      {
      stat.userPrintln(message, exc);      
      }
   
   /**
    * @see stat.debug
    */
   public final boolean debug ( int mask, int level )
      {
      return stat.debug(mask, level);
      }
   
   
   public final GuiResources newGuiResources ( String key )
      {
      return new GuiResources (stat, key);
      }

   }
