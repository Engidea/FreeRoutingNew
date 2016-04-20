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
 * BoardPanel.java
 *
 * Created on 3. Oktober 2002, 18:47
 */

package gui;

import gui.menu.PopupMenuCopy;
import gui.menu.PopupMenuDynamicRoute;
import gui.menu.PopupMenuInsertCancel;
import gui.menu.PopupMenuMain;
import gui.menu.PopupMenuMove;
import gui.menu.PopupMenuSelectedItems;
import gui.menu.PopupMenuStitchRoute;
import gui.menu.PupupMenuCornerItemConstruction;
import gui.varie.GuiCursor;
import gui.varie.GuiCursorCrossHair;
import interactive.IteraBoard;
import interactive.ScreenMessages;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import main.Stat;
import planar.PlaPointFloat;

/**
 * Panel containing the graphical representation of a routing board.
 * You cannot hide the JPanel implementationsince we override the PaintComponent
 * @author Alfons Wirtz
 */
public final class BoardPanel extends JPanel
   {
   private static final long serialVersionUID = 1L;
   private static final double ZOOM_FACTOR = 2.0;

   private final Stat stat;
   private final java.awt.Robot mouse_robot;
  
   public  final BoardFrame board_frame;
   public  final ScreenMessages screen_messages;
   public  final IteraBoard board_handling;       // initialized on constructor, not null
   
   
   public JPopupMenu popup_menu_insert_cancel;
   public PopupMenuCopy popup_menu_copy;
   public PopupMenuMove popup_menu_move;
   public JPopupMenu popup_menu_corneritem_construction;
   public JPopupMenu popup_menu_main;
   public PopupMenuDynamicRoute popup_menu_dynamic_route;
   public PopupMenuStitchRoute popup_menu_stitch_route;
   public JPopupMenu popup_menu_select;

   public Point2D right_button_click_location = null;
   private Point middle_drag_position = null;

   // Defines the appearance of the custom custom_cursor in the board panel.
   private GuiCursor custom_cursor = null;
   

   public BoardPanel(ScreenMessages p_screen_messages, BoardFrame p_board_frame,  Stat p_stat)
      {
      stat = p_stat;
      board_frame = p_board_frame;
      screen_messages = p_screen_messages;

      mouse_robot = getMouseRobot();

      setLayout(new BorderLayout());

      setBackground(new Color(0, 0, 0));
      setMaximumSize(new Dimension(30000, 20000));
      setMinimumSize(new Dimension(90, 60));
      setPreferredSize(new Dimension(1200, 900));
      addMouseMotionListener(new MouseMotionAdapter()
         {
            public void mouseDragged(MouseEvent evt)
               {
               mouse_dragged_action(evt);
               }

            public void mouseMoved(MouseEvent evt)
               {
               mouse_moved_action(evt);
               }
         });
      
      addKeyListener(new KeyAdapter()
         {
         public void keyTyped(KeyEvent evt)
            {
            board_handling.key_typed_action(evt.getKeyChar());
            }
         });
      addMouseListener(new MouseAdapter()
         {
         public void mouseClicked(MouseEvent evt)
            {
            mouse_clicked_action(evt);
            }

         public void mousePressed(MouseEvent evt)
            {
            mouse_pressed_action(evt);
            }

         public void mouseReleased(MouseEvent evt)
            {
            board_handling.button_released();
            middle_drag_position = null;
            }
         });
      
      addMouseWheelListener(new MouseWheelListener()
         {
         public void mouseWheelMoved(MouseWheelEvent evt)
            {
            board_handling.mouse_wheel_moved(evt.getWheelRotation());
            }
         });
      
      board_handling = new IteraBoard(this, stat);
      
      setAutoscrolls(true);

      setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }

   private java.awt.Robot getMouseRobot ()
      {
      try
         {
         // used to be able to change the location of the mouse pointer
         return new java.awt.Robot();
         }
      catch (AWTException exc)
         {
         stat.userPrintln("unable to create robot",exc);
         return null;
         }
      }
   
   void create_popup_menus()
      {
      popup_menu_main = new PopupMenuMain(stat, board_frame);
      popup_menu_dynamic_route = new PopupMenuDynamicRoute(stat, board_frame);
      popup_menu_stitch_route = new PopupMenuStitchRoute(stat,board_frame);
      popup_menu_corneritem_construction = new PupupMenuCornerItemConstruction(board_frame);
      popup_menu_select = new PopupMenuSelectedItems(stat, board_frame);
      popup_menu_insert_cancel = new PopupMenuInsertCancel(board_frame);
      popup_menu_copy = new PopupMenuCopy(stat,board_frame);
      popup_menu_move = new PopupMenuMove(stat, board_frame);
      }

   public void zoom_with_mouse_wheel(Point2D p_point, int p_wheel_rotation)
      {
      if (middle_drag_position != null || p_wheel_rotation == 0)
         {
         return; // scrolling with the middle mouse butten in progress
         }
      
      double zoom_factor = 1 - 0.1 * p_wheel_rotation;
      
      zoom_factor = Math.max(zoom_factor, 0.5);
      
      zoom(zoom_factor, p_point);
      }

   private void mouse_pressed_action(MouseEvent evt)
      {
      if (evt.getButton() == 1)
         {
         board_handling.mouse_pressed(evt.getPoint());
         }
      else if (evt.getButton() == 2 && middle_drag_position == null)
         {
         middle_drag_position = new Point(evt.getPoint());
         }
      }

   private void mouse_dragged_action(MouseEvent evt)
      {
      if (middle_drag_position != null)
         {
         scroll_middle_mouse(evt);
         }
      else
         {
         board_handling.mouse_dragged(evt.getPoint());
         scroll_near_border(evt);
         }
      }

   private void mouse_moved_action(MouseEvent p_evt)
      {
      requestFocusInWindow(); // to enable keyboard aliases
      
      if (board_handling != null)
         {
         board_handling.mouse_moved(p_evt.getPoint());
         }
      
      if (custom_cursor != null)
         {
         custom_cursor.set_location(p_evt.getPoint());
         repaint();
         }
      }

   private void mouse_clicked_action(MouseEvent evt)
      {
      if (evt.getButton() == 1)
         {
         board_handling.left_button_clicked(evt.getPoint());
         }
      else if (evt.getButton() == 3)
         {
         JPopupMenu curr_menu = board_handling.get_current_popup_menu();
         if (curr_menu != null)
            {
            int curr_x = evt.getX();
            int curr_y = evt.getY();
            if (curr_menu == popup_menu_dynamic_route)
               {
               int dx = curr_menu.getWidth();
               if (dx <= 0)
                  {
                  // force the width to be calculated
                  curr_menu.show(this, curr_x, curr_y);
                  dx = curr_menu.getWidth();
                  }
               curr_x -= dx;
               }
            curr_menu.show(this, curr_x, curr_y);
            }
         right_button_click_location = evt.getPoint();
         }
      }

   /**
    * overwrites the paintComponent method to draw the routing board
    */
   @Override
   public void paintComponent(Graphics p_g)
      {
      super.paintComponent(p_g);
      
      if (board_handling != null) board_handling.draw(p_g);
      
      if (custom_cursor != null)  custom_cursor.draw(p_g);
      }


   /**
    * zooms in at p_position
    */
   public void zoom_in(Point2D p_position)
      {
      zoom(ZOOM_FACTOR, p_position);
      }

   /**
    * zooms out at p_position
    */
   public void zoom_out(Point2D p_position)
      {
      double zoom_factor = 1 / ZOOM_FACTOR;
      zoom(zoom_factor, p_position);
      }

   /**
    * zooms to frame
    */
   public void zoom_frame(Point2D p_position1, Point2D p_position2)
      {
      double width_of_zoom_frame = Math.abs(p_position1.getX() - p_position2.getX());
      double height_of_zoom_frame = Math.abs(p_position1.getY() - p_position2.getY());

      double center_x = Math.min(p_position1.getX(), p_position2.getX()) + (width_of_zoom_frame / 2);
      double center_y = Math.min(p_position1.getY(), p_position2.getY()) + (height_of_zoom_frame / 2);

      Point2D center_point = new Point2D.Double(center_x, center_y);

      Rectangle display_rect = board_frame.getViewportBorderBounds();

      double width_factor = display_rect.getWidth() / width_of_zoom_frame;
      double height_factor = display_rect.getHeight() / height_of_zoom_frame;

      Point2D changed_location = zoom(Math.min(width_factor, height_factor), center_point);
      set_viewport_center(changed_location);
      }

   public void center_display(Point2D p_new_center)
      {
      Point delta = set_viewport_center(p_new_center);
      Point2D new_center = get_viewport_center();
      Point new_mouse_location = new Point((int) (new_center.getX() - delta.getX()), (int) (new_center.getY() - delta.getY()));

      move_mouse(new_mouse_location);
      repaint();
      
      board_handling.actlog.start_scope(interactive.LogfileScope.CENTER_DISPLAY);
      PlaPointFloat curr_corner = new PlaPointFloat(p_new_center.getX(), p_new_center.getY());
      board_handling.actlog.add_corner(curr_corner);
      }

   public Point2D get_viewport_center()
      {
      Point pos = board_frame.get_viewport_position();
      Rectangle display_rect = board_frame.getViewportBorderBounds();
      return new Point2D.Double(pos.getX() + display_rect.getCenterX(), pos.getY() + display_rect.getCenterY());
      }

   /**
    * zooms the content of the board by p_factor 
    * @return the change of the cursor location
    */
   private Point2D zoom(double p_factor, Point2D p_location)
      {
      final int max_panel_size = 10000000;
      Dimension old_size = getSize();
      Point2D old_center = get_viewport_center();

      if (p_factor > 1 && Math.max(old_size.getWidth(), old_size.getHeight()) >= max_panel_size)
         {
         // to prevent an sun.dc.pr.PRException, which I do not know, how to handle; maybe a bug in Java.         
         return p_location; 
         }
      
      int new_width = (int) Math.round(p_factor * old_size.getWidth());
      int new_height = (int) Math.round(p_factor * old_size.getHeight());
      Dimension new_size = new Dimension(new_width, new_height);
      
      board_handling.gdi_context.change_panel_size(new_size);
      
      setPreferredSize(new_size);
      
      setSize(new_size);
      
      revalidate();

      Point2D new_cursor = new Point2D.Double(p_location.getX() * p_factor, p_location.getY() * p_factor);
      double dx = new_cursor.getX() - p_location.getX();
      double dy = new_cursor.getY() - p_location.getY();
      
      Point2D new_center = new Point2D.Double(old_center.getX() + dx, old_center.getY() + dy);
      Point2D adjustment_vector = set_viewport_center(new_center);
      
      repaint();
      
      Point2D adjusted_new_cursor = new Point2D.Double(new_cursor.getX() + adjustment_vector.getX()
            + 0.5, new_cursor.getY() + adjustment_vector.getY() + 0.5);
      
      return adjusted_new_cursor;
      }

   /** 
    * Sets the displayed region to the whole board. 
    */
   public void zoom_all()
      {
      board_handling.adjust_design_bounds();
      Rectangle display_rect = board_frame.getViewportBorderBounds();
      Rectangle design_bounds = board_handling.gdi_context.get_design_bounds();
      double width_factor = display_rect.getWidth() / design_bounds.getWidth();
      double height_factor = display_rect.getHeight() / design_bounds.getHeight();
      double zoom_factor = Math.min(width_factor, height_factor);
      Point2D zoom_center = board_handling.gdi_context.get_design_center();
      zoom(zoom_factor, zoom_center);
      Point2D new_vieport_center = board_handling.gdi_context.get_design_center();
      set_viewport_center(new_vieport_center);

      }

   /**
    * Sets the viewport center to p_point. 
    * Adjust the result, if p_point is near the border of the viewport. 
    * @return the adjustmed point
    */
   public Point set_viewport_center(Point2D p_point)
      {
      Rectangle display_rect = board_frame.getViewportBorderBounds();
      double x_corner = p_point.getX() - display_rect.getWidth() / 2;
      double y_corner = p_point.getY() - display_rect.getHeight() / 2;
      Dimension panel_size = getSize();
      double adjusted_x_corner = Math.min(x_corner, panel_size.getWidth());
      adjusted_x_corner = Math.max(x_corner, 0);
      double adjusted_y_corner = Math.min(y_corner, panel_size.getHeight());
      adjusted_y_corner = Math.max(y_corner, 0);
      Point new_position = new Point((int) adjusted_x_corner, (int) adjusted_y_corner);
      board_frame.set_viewport_position(new_position);
      Point adjustment_vector = new Point((int) (adjusted_x_corner - x_corner),(int) (adjusted_y_corner - y_corner));
      return adjustment_vector;
      }

   /**
    * Selects the p_signal_layer_no-th layer in the select_parameter_window.
    */
   public void set_selected_signal_layer(int p_signal_layer_no)
      {
      // this is actually called before windows are set up...
      if ( board_frame.select_parameter_window == null ) return;
      
      board_frame.select_parameter_window.select_signal_layer(p_signal_layer_no);
      popup_menu_dynamic_route.disable_layer_item(p_signal_layer_no);
      popup_menu_stitch_route.disable_layer_item(p_signal_layer_no);
      popup_menu_copy.disable_layer_item(p_signal_layer_no);
      }

   void init_colors()
      {
      board_handling.gdi_context.item_color_table.addTableModelListener(new ColorTableListener());
      board_handling.gdi_context.other_color_table.addTableModelListener(new ColorTableListener());
      setBackground(board_handling.gdi_context.get_background_color());
      }

   private void scroll_near_border(MouseEvent p_evt)
      {
      final int border_dist = 50;
      Rectangle r = new Rectangle(p_evt.getX() - border_dist, p_evt.getY() - border_dist, 2 * border_dist, 2 * border_dist);
      ((JPanel) p_evt.getSource()).scrollRectToVisible(r);
      }

   // damiano, hey, this is what I am lookin for !!!
   private void scroll_middle_mouse(MouseEvent p_evt)
      {
      double delta_x = middle_drag_position.x - p_evt.getX();
      double delta_y = middle_drag_position.y - p_evt.getY();

      Point view_position = board_frame.get_viewport_position();

      double x = (view_position.x + delta_x);
      double y = (view_position.y + delta_y);

      Dimension panel_size = getSize();
      x = Math.min(x, panel_size.getWidth() - board_frame.getViewportBorderBounds().getWidth());
      y = Math.min(y, panel_size.getHeight() - board_frame.getViewportBorderBounds().getHeight());

      x = Math.max(x, 0);
      y = Math.max(y, 0);

      Point p = new Point((int) x, (int) y);
      
      board_frame.set_viewport_position(p);
      }

   public void move_mouse(Point2D p_location)
      {
      if (mouse_robot == null) return;

      Point absolute_panel_location = board_frame.absolute_panel_location();
      Point view_position = board_frame.get_viewport_position();
      int x = (int) Math.round(absolute_panel_location.getX() - view_position.getX() + p_location.getX()) + 1;
      int y = (int) Math.round(absolute_panel_location.getY() - view_position.getY() + p_location.getY() + 1);
      
      mouse_robot.mouseMove(x, y);
      }

   /**
    * If p_value is true, the custom crosshair cursor will be used in display.
    * Otherwise the standard Cursor will be used. Using the custom cursor may
    * slow down the display performance a lot.
    */
   public void set_crosshair_cursor(boolean p_value)
      {
      custom_cursor = p_value ? new GuiCursorCrossHair() : null;
      
      board_frame.refresh_windows();

      repaint();
      }

   /**
    * If the result is true, the custom crosshair cursor will be used in
    * display. Otherwise the standard Cursor will be used. Using the custom
    * cursor may slow down the display performance a lot.
    */
   public boolean is_cross_hair_cursor()
      {
      return custom_cursor != null;
      }

   private class ColorTableListener implements TableModelListener
      {
      public void tableChanged(TableModelEvent p_event)
         {
         // redisplay board because some colors have changed.
         setBackground(board_handling.gdi_context.get_background_color());
         repaint();
         }
      }
   }
