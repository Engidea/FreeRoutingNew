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
 * InteractiveState.java
 *
 * Created on 5. November 2003, 12:55
 */

package interactive.state;

import freert.planar.PlaPointFloat;
import gui.varie.GuiResources;
import interactive.Actlog;
import interactive.IteraBoard;
import interactive.LogfileScope;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import javax.swing.JPopupMenu;
import board.RoutingBoard;

/**
 * Common base class of all interaction states with the graphical interface
 *
 * @author Alfons Wirtz
 *
 */
public class StateInteractive
   {
   // board setting access handler for the derived classes
   protected final IteraBoard i_brd;
   // routing board access handler for the derived classes
   protected final RoutingBoard r_brd;
   // The intended state after this state is finished
   public StateInteractive return_state;
   // if logfile != null, the interactive actions are stored in a logfile 
   protected final Actlog actlog;
   // Contains the files with the language dependent messages 
   protected final GuiResources resources;
   
   protected StateInteractive(StateInteractive p_return_state, IteraBoard p_itera_board, Actlog p_logfile)
      {
      return_state = p_return_state;
      i_brd = p_itera_board;
      r_brd = i_brd.get_routing_board();
      actlog = p_logfile;
      resources = p_itera_board.newGuiResources("interactive.resources.InteractiveState");
      }

   protected final void actlog_start_scope (  LogfileScope actevent )
      {
      if ( actlog == null ) return;
      
      actlog.start_scope ( actevent);
      }

   protected final void actlog_add_corner ( PlaPointFloat apoint )
      {
      if ( actlog == null ) return;
      
      actlog.add_corner(apoint);
      }

   protected final void actlog_start_scope (  LogfileScope actevent, PlaPointFloat apoint )
      {
      if ( actlog == null ) return;
      
      actlog.start_scope ( actevent, apoint);
      }
   
   protected final void actlog_start_scope (  LogfileScope actevent, int avalue )
      {
      if ( actlog == null ) return;
      
      actlog.start_scope ( actevent, avalue);
      }

   /**
    * @see stat.debug
    */
   public final boolean debug ( int mask, int level )
      {
      return i_brd.debug(mask, level);
      }

   /**
    * default draw function to be overwritten in derived classes
    */
   public void draw(Graphics p_graphics)
      {
      }

   /**
    * Default function to be overwritten in derived classes. Returns the return_state of this state, if the state is left after the
    * method, or else this state.
    */
   public StateInteractive left_button_clicked(PlaPointFloat p_location)
      {
      return this;
      }

   /*
    * Actions to be taken when a mouse button is released. Default function to be overwritten in derived classes. Returns the
    * return_state of this state, if the state is left after the method, or else this state.
    */
   public StateInteractive button_released()
      {
      return this;
      }

   /**
    * Actions to be taken, when the location of the mouse pointer changes. Default function to be overwritten in derived classes.
    * Returns the return_state of this state, if the state ends after the method, or else this state.
    */
   public StateInteractive mouse_moved()
      {
      PlaPointFloat mouse_position = i_brd.coordinate_transform.board_to_user(i_brd.get_current_mouse_position());
      i_brd.screen_messages.set_mouse_position(mouse_position);
      return this;
      }

   /**
    * Actions to be taken when the mouse moves with a button pressed down. Default function to be overwritten in derived classes.
    * Returns the return_state of this state, if the state is left after the method, or else this state.
    */
   public StateInteractive mouse_dragged(PlaPointFloat p_point)
      {
      return this;
      }

   /**
    * Actions to be taken when the left mouse button is pressed down. Default function to be overwritten in derived classes. Returns
    * the return_state of this state, if the state is left after the method, or else this state.
    */
   public StateInteractive mouse_pressed(PlaPointFloat p_point)
      {
      return this;
      }

   /**
    * Action to be taken, when the mouse wheel was turned..
    */
   public StateInteractive mouse_wheel_moved(int p_rotation)
      {
      Point2D screen_mouse_pos = i_brd.gdi_context.coordinate_transform.board_to_screen(i_brd.get_current_mouse_position());
      
      i_brd.get_panel().zoom_with_mouse_wheel(screen_mouse_pos, p_rotation);
      
      return this;
      }

   /**
    * Default actions when a key shortcut is pressed. Overwritten in derived classes for other key shortcut actions.
    */
   public StateInteractive key_typed(char p_key_char)
      {
      StateInteractive result = this;
      
      Point2D screen_mouse_pos = i_brd.gdi_context.coordinate_transform.board_to_screen(i_brd.get_current_mouse_position());
      
      if (p_key_char == 'a')
         {
         i_brd.get_panel().zoom_all();
         }
      else if (p_key_char == 'c')
         {
         i_brd.get_panel().center_display(screen_mouse_pos);
         }
      else if (p_key_char == 'f')
         {
         result = new StateSelectRegionZoom(this, i_brd, actlog, i_brd.get_current_mouse_position());
         }
      else if (p_key_char == 'h')
         {
         i_brd.get_panel().board_frame.select_previous_snapshot();
         }
      if (p_key_char == 'j')
         {
         i_brd.get_panel().board_frame.goto_selected_snapshot();
         }
      else if (p_key_char == 'k')
         {
         i_brd.get_panel().board_frame.select_next_snapshot();
         }
      else if (p_key_char == 'Z')
         {
         i_brd.get_panel().zoom_out(screen_mouse_pos);
         }
      else if (p_key_char == 'z')
         {
         i_brd.get_panel().zoom_in(screen_mouse_pos);
         }
      else if (p_key_char == ',')
         {
         // toggle the cross hair cursor
         i_brd.get_panel().set_crosshair_cursor(!i_brd.get_panel().is_cross_hair_cursor());
         }
      else if (p_key_char == '\n' || p_key_char == ' ')
         {
         result = complete();
         }
      else if (p_key_char == java.awt.event.KeyEvent.VK_ESCAPE)
         {
         result = cancel();
         }
      else if (Character.isDigit(p_key_char))
         {
         // change the current layer to the p_key_char-ths signal layer
         board.BrdLayerStructure layer_structure = i_brd.get_routing_board().layer_structure;
         int want = Character.digit(p_key_char, 10);
         want = Math.min(want, layer_structure.signal_layer_count());
         // Board layers start at 0, keyboard input for layers starts at 1.
         want = Math.max(want - 1, 0);
         want = layer_structure.get_no(layer_structure.get_signal_layer(want));
         i_brd.set_current_layer(want);
         }
      return result;
      }

   /**
    * Action to be taken, when this state is completed and exited. Default function to be overwritten in derived classes. Returns
    * the return_state of this state.
    */
   public StateInteractive complete()
      {
      if ( return_state != this && actlog != null)
         {
         actlog.start_scope(LogfileScope.COMPLETE_SCOPE);
         }
      return return_state;
      }

   /**
    * Actions to be taken, when this state gets cancelled. Default function to be overwritten in derived classes. 
    * @return the parent state of this state.
    */
   public StateInteractive cancel()
      {
      if (return_state != this && actlog != null)
         {
         actlog.start_scope(LogfileScope.CANCEL_SCOPE);
         }
      return return_state;
      }

   /**
    * Action to be taken, when the current layer is changed. 
    * returns false, if the layer could not be changed, Default function to be overwritten in derived classes.
    */
   public boolean change_layer_action(int p_new_layer)
      {
      i_brd.set_layer(p_new_layer);
      return true;
      }

   /**
    * Used when reading the next point from a Actlog. 
    * Default function to be overwritten in derived classes.
    */
   public StateInteractive process_logfile_point(PlaPointFloat p_point)
      {
      return this;
      }

   /**
    * The default message displayed, when this state is active.
    */
   public void display_default_message()
      {
      }

   /**
    * Gets the identifier for displaying help for the user about this state.
    */
   public String get_help_id()
      {
      return "MenuState";
      }

   /**
    * Returns the popup menu from board_panel, which is used in this interactive state. Default function to be overwritten in
    * derived classes.
    */
   public JPopupMenu get_popup_menu()
      {
      return null;
      }

   /**
    * A state using toolbar must overwrite this function.
    */
   public void set_toolbar()
      {
      }

   }
