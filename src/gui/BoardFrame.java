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

package gui;

import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.main.Stat;
import gui.config.GuiConfigFile;
import gui.menu.BoardMenuBar;
import gui.varie.FileFilter;
import gui.varie.GuiHelp;
import gui.varie.GuiResources;
import gui.varie.SubwindowSelections;
import gui.win.WindowAbout;
import gui.win.WindowAssignNetClass;
import gui.win.WindowAutorouteParameter;
import gui.win.WindowBeanshell;
import gui.win.WindowClearanceMatrix;
import gui.win.WindowClearanceViolations;
import gui.win.WindowColorManager;
import gui.win.WindowComponents;
import gui.win.WindowDebugConfig;
import gui.win.WindowDisplayMisc;
import gui.win.WindowEditVias;
import gui.win.WindowIncompletes;
import gui.win.WindowLayerVisibility;
import gui.win.WindowLengthViolations;
import gui.win.WindowMoveParameter;
import gui.win.WindowNetClasses;
import gui.win.WindowNets;
import gui.win.WindowObjectVisibility;
import gui.win.WindowPackages;
import gui.win.WindowPadstacks;
import gui.win.WindowRouteParameter;
import gui.win.WindowRouteStubs;
import gui.win.WindowSelectParameter;
import gui.win.WindowUnconnectedRoute;
import gui.win.WindowUnitMeasure;
import gui.win.WindowVia;
import interactive.ScreenMessages;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import board.varie.DesignFile;
import board.varie.IdGenerator;

/**
 * Graphical frame of for interactive editing of a routing board.
 * 
 * @author Alfons Wirtz
 */

public final class BoardFrame 
   {
   private static final String classname = "BoardFrame.";
   public  static final String GUI_DEFAULTS_FILE_NAME = "gui_defaults.par";
   public  static final String GUI_DEFAULTS_FILE_BACKUP_NAME = "gui_defaults.par.bak";
   public  static final String[] log_file_extensions = { "log" };
   public  static final FileFilter logfile_filter = new FileFilter(log_file_extensions);

   
   public final Stat stat;

   private final JFrame work_frame;
   // The scroll pane for the panel of the routing board. 
   private final JScrollPane scroll_pane;
   // The menu bar of this frame 
   public final BoardMenuBar menubar;
   // The panel with the graphical representation of the board
   public final BoardPanel board_panel;
   // The panel with the tool bars 
   private final BoardToolbar toolbar_panel;
   // Tool bar used in the selected item state. 
   private final JToolBar select_toolbar;
   // The panel with the message line 
   private final BoardPanelStatus message_panel;

   public final ScreenMessages screen_messages;

   public final GuiHelp gui_help;

   private final GuiResources resources;

   private final IdGenerator item_id_no_generator;

   public WindowAbout about_window = null;
   public WindowRouteParameter route_parameter_window = null;
   public WindowAutorouteParameter autoroute_parameter_window = null;
   public WindowSelectParameter select_parameter_window = null;
   public WindowMoveParameter move_parameter_window = null;
   public WindowUnitMeasure unit_parameter_window;
   public WindowClearanceMatrix clearance_matrix_window = null;
   public WindowVia via_window = null;
   public WindowEditVias edit_vias_window = null;
   public WindowNetClasses edit_net_rules_window = null;
   public WindowAssignNetClass assign_net_classes_window = null;
   public WindowPadstacks padstacks_window = null;
   public WindowPackages packages_window = null;
   public WindowIncompletes incompletes_window = null;
   public WindowNets net_info_window = null;
   public WindowClearanceViolations clearance_violations_window = null;
   public WindowLengthViolations length_violations_window = null;
   public WindowUnconnectedRoute unconnected_route_window = null;
   public WindowRouteStubs route_stubs_window = null;
   public WindowComponents components_window = null;
   public WindowLayerVisibility layer_visibility_window = null;
   public WindowObjectVisibility object_visibility_window = null;
   public WindowDisplayMisc display_misc_window = null;
   public WindowBeanshell window_beanshell = null;
   public WindowColorManager color_manager = null;
   public WindowDebugConfig debug_config;

   private final ArrayList<GuiSubWindowSavable> permanent_subwindows = new ArrayList<GuiSubWindowSavable>();
   private final LinkedList<GuiSubWindowTemp> temporary_subwindows = new LinkedList<GuiSubWindowTemp>();

   public DesignFile design_file = null;



   
   
   /**
    * Creates a new board frame with the input design file embedded into a host
    * cad software.
   private static BoardFrame get_embedded_instance(String p_design_file_path_name, BoardObservers p_observers, IdNoGenerator p_id_no_generator, java.util.Locale p_locale)
      {
      final gui.DesignFile design_file = gui.DesignFile.get_instance(p_design_file_path_name);
      if (design_file == null)
         {
         WindowMessage.show("designfile not found");
         return null;
         }
      gui.BoardFrame board_frame = new gui.BoardFrame(design_file, gui.BoardFrame.Option.SINGLE_FRAME, TestLevel.RELEASE_VERSION, p_observers, p_id_no_generator, p_locale, false);

      java.io.InputStream input_stream = design_file.get_input_stream();
      boolean read_ok = board_frame.read(input_stream, true, null);
      if (!read_ok)
         {
         String error_message = "Unable to read design file with pathname " + p_design_file_path_name;
         board_frame.setVisible(true); // to be able to display the status
                                       // message
         board_frame.screen_messages.set_status_message(error_message);
         }
      return board_frame;
      }
    */

   /**
    * Creates new form BoardFrame. The parameters p_item_observers and
    * p_item_id_no_generator are used for synchronizing purposes, if the frame is
    * embedded into a host system,
    */
   public BoardFrame(DesignFile p_design, Stat p_stat )
      {
      stat = p_stat;
      design_file = p_design;

      item_id_no_generator = new IdGenerator();
      resources = new GuiResources(p_stat,"gui.resources.BoardFrame");
      gui_help = new GuiHelp(stat);

      work_frame = new JFrame();
      
      menubar = new BoardMenuBar(p_stat, this);
      
      work_frame.setJMenuBar(menubar);

      toolbar_panel = new BoardToolbar(p_stat, this);
      work_frame.add(toolbar_panel, java.awt.BorderLayout.NORTH);

      message_panel = new BoardPanelStatus(p_stat);
      work_frame.add(message_panel, BorderLayout.SOUTH);

      select_toolbar = new BoardToolbarSelectedItem(p_stat, this);

      screen_messages = new ScreenMessages(message_panel, p_stat);

      scroll_pane = new JScrollPane();
      scroll_pane.setPreferredSize(new Dimension(1150, 800));
      scroll_pane.setVerifyInputWhenFocusTarget(false);
      work_frame.add(scroll_pane, BorderLayout.CENTER);

      board_panel = new BoardPanel(screen_messages, this, p_stat);
      scroll_pane.setViewportView(board_panel);

      work_frame.setTitle(resources.getString("title"));
      work_frame.addWindowListener(new WindowStateListener());

      work_frame.pack();
      }

   /**
    * Reads interactive actions from a logfile.
    */
   public void read_logfile(java.io.InputStream p_input_stream)
      {
      board_panel.itera_board.read_logfile(p_input_stream);
      }
   
   /**
    * open an existing board design from file. 
    * @return false, if the file is invalid.
    */
   public boolean open_design(InputStream p_input_stream)
      {
      java.awt.Point viewport_position = null;
      
      ObjectInputStream object_stream = null;
      try
         {
         object_stream = new ObjectInputStream(p_input_stream);
         board_panel.itera_board.read_design(object_stream);         
         }
      catch (Exception exc)
         {
         stat.userPrintln(classname+"open_design: ", exc);
         return false;
         }

      try
         {
         viewport_position = (Point) object_stream.readObject();

         Point frame_location = (Point) object_stream.readObject();
         work_frame.setLocation(frame_location);

         Rectangle frame_bounds = (Rectangle) object_stream.readObject();
         work_frame.setBounds(frame_bounds);
         }
      catch (Exception e)
         {
         return false;
         }

      allocate_permanent_subwindows();

      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         {
         cur_subwindow.read(object_stream);
         }
      
      try
         {
         p_input_stream.close();
         }
      catch (java.io.IOException e)
         {
         return false;
         }

      Dimension panel_size = board_panel.itera_board.gdi_context.get_panel_size();
      
      board_panel.setSize(panel_size);
      board_panel.setPreferredSize(panel_size);
      set_viewport_position(viewport_position);
      board_panel.create_popup_menus();
      board_panel.init_colors();
      board_panel.itera_board.create_ratsnest();
      hilight_selected_button();
      
      work_frame.setVisible(true);

      return true;
      }
 
  
   /**
    * import a board design from file. 
    * @return false, if the file is invalid.
    */
   public boolean import_design(InputStream p_input_stream )
      {
      if ( ! board_panel.itera_board.import_design(p_input_stream, item_id_no_generator, stat) )
         {
         stat.userPrintln(resources.getString("error_6"));
         return false;
         }

      initialize_windows();
      
      try
         {
         p_input_stream.close();
         }
      catch (java.io.IOException e)
         {
         return false;
         }

      Point viewport_position = new java.awt.Point(0, 0);
      Dimension panel_size = board_panel.itera_board.gdi_context.get_panel_size();
      board_panel.setSize(panel_size);
      board_panel.setPreferredSize(panel_size);
      set_viewport_position(viewport_position);
      board_panel.create_popup_menus();
      board_panel.init_colors();
      board_panel.itera_board.create_ratsnest();
      hilight_selected_button();
      
      work_frame.setVisible(true);
      
      File defaults_file = new File(design_file.get_parent(), GUI_DEFAULTS_FILE_NAME);

      try
         {
         // Read the default GUI settings, if GUI default file exists.
         stat.userPrintln("try open "+defaults_file);
         InputStream input_stream = new FileInputStream(defaults_file);
         GuiConfigFile.read(this, board_panel.itera_board, input_stream);
         input_stream.close();
         }
      catch (Exception exc)
         {
         stat.userPrintln("open defaults exception", exc);
         screen_messages.set_status_message(resources.getString("error_1"));
         return false;
         }
      
      board_panel.zoom_all();
      
      return true;
      }

   
   private void save_win_state (ObjectOutputStream object_stream) throws IOException
      {
      object_stream.writeObject(get_viewport_position());
      
      object_stream.writeObject(work_frame.getLocation());
      object_stream.writeObject(work_frame.getBounds());
      
      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         {
         cur_subwindow.save(object_stream);
         }
      }
   
   /**
    * Saves the interactive settings and the design file to disk. 
    * @return false if the save failed.
    */
   public boolean save()
      {
      if (design_file == null) return false;
      
      try
         {
         File to_file = design_file.get_output_file();
         stat.userPrintln(classname+"save to_file "+to_file);

         java.io.OutputStream  output_stream = new FileOutputStream(to_file);
         java.io.ObjectOutputStream object_stream = new java.io.ObjectOutputStream(output_stream);
         
         board_panel.itera_board.save_design_file(object_stream);
         save_win_state(object_stream);
         
         object_stream.flush();
         output_stream.close();

         stat.userPrintln(classname+"save to_file DONE");

         return true;
         }
      catch (Exception exc)
         {
         stat.userPrintln(classname+"save to_file ", exc);
         return false;
         }
      }

   
   
   public void set_context_sensitive_help(Component awin, String p_help_id)
      {
      gui_help.set_context_sensitive_help(awin,p_help_id);
      }

   public void set_context_sensitive_help(GuiSubWindow awin, String p_help_id)
      {
      gui_help.set_context_sensitive_help(awin.getJFrame(),p_help_id);
      }
   
   /** 
    * Sets the tool bar to the buttons of the selected item state
    */
   public void set_select_toolbar()
      {
      work_frame.getContentPane().remove(toolbar_panel);
      work_frame.getContentPane().add(select_toolbar, BorderLayout.NORTH);
      work_frame.repaint();
      }

   /**
    * Sets the toolbar buttons to the select. route and drag menu buttons of the
    * main menu.
    */
   public void set_menu_toolbar()
      {
      work_frame.getContentPane().remove(select_toolbar);
      work_frame.getContentPane().add(toolbar_panel, BorderLayout.NORTH);
      work_frame.repaint();
      }

   /**
    * Calculates the absolute location of the board frame in his outmost parent frame.
    */
   java.awt.Point absolute_panel_location()
      {
      int x = scroll_pane.getX();
      int y = scroll_pane.getY();
      java.awt.Container curr_parent = scroll_pane.getParent();
      while (curr_parent != null)
         {
         x += curr_parent.getX();
         y += curr_parent.getY();
         curr_parent = curr_parent.getParent();
         }
      return new java.awt.Point(x, y);
      }

   final Rectangle getViewportBorderBounds()
      {
      return scroll_pane.getViewportBorderBounds();
      }
   
   /**
    * Returns the position of the viewport
    */
   public final Point get_viewport_position()
      {
      JViewport viewport = scroll_pane.getViewport();

      return viewport.getViewPosition();
      }

   /**
    * Sets the position of the viewport
    */
   public final void set_viewport_position(Point p_position)
      {
      if ( p_position == null ) return;
      
      JViewport viewport = scroll_pane.getViewport();

      viewport.setViewPosition(p_position);
      }

   
   

   public void add_subwindow ( GuiSubWindowTemp awin )
      {
      if ( awin == null ) return;
      
      temporary_subwindows.add(awin);
      }
   
   public void remove_subwindow ( GuiSubWindowTemp awin )
      {
      if ( awin == null ) return;
      
      temporary_subwindows.remove(awin);
      }
   

   /**
    * Actions to be taken when this frame vanishes.
    */
   public void dispose()
      {
      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         cur_subwindow.dispose();
      
      permanent_subwindows.clear();
      
      for (GuiSubWindowTemp curr_subwindow : temporary_subwindows)
         curr_subwindow.board_frame_disposed();
      
      temporary_subwindows.clear();
      
      board_panel.itera_board.dispose();

      work_frame.dispose();
      }

   private void allocate_permanent_subwindows()
      {
      color_manager = new WindowColorManager(this);
      permanent_subwindows.add(color_manager);
      
      object_visibility_window = WindowObjectVisibility.get_instance(this);
      permanent_subwindows.add(object_visibility_window);
      
      layer_visibility_window = WindowLayerVisibility.get_instance(this);
      permanent_subwindows.add(layer_visibility_window);
      
      display_misc_window = new WindowDisplayMisc(this);
      permanent_subwindows.add(display_misc_window);
      
      window_beanshell = new WindowBeanshell(this);
      permanent_subwindows.add(window_beanshell);
      window_beanshell.initialize();
      
      route_parameter_window = new WindowRouteParameter(stat, this);
      permanent_subwindows.add(route_parameter_window);
      
      select_parameter_window = new WindowSelectParameter(this);
      permanent_subwindows.add(select_parameter_window);
      
      clearance_matrix_window = new WindowClearanceMatrix(this);
      permanent_subwindows.add(clearance_matrix_window);
      
      padstacks_window = new WindowPadstacks(this);
      permanent_subwindows.add(padstacks_window);
      
      packages_window = new WindowPackages(this);
      permanent_subwindows.add(packages_window);
      
      components_window = new WindowComponents(this);
      permanent_subwindows.add(components_window);
      
      incompletes_window = new WindowIncompletes(this);
      permanent_subwindows.add(incompletes_window);
      
      clearance_violations_window = new WindowClearanceViolations(this);
      permanent_subwindows.add(clearance_violations_window);
      
      net_info_window = new WindowNets(this);
      permanent_subwindows.add(net_info_window);
      
      via_window = new WindowVia(this);
      permanent_subwindows.add(via_window);
      
      edit_vias_window = new WindowEditVias(stat, this);
      permanent_subwindows.add(edit_vias_window);
      
      edit_net_rules_window = new WindowNetClasses(this);
      permanent_subwindows.add(edit_net_rules_window);
      
      assign_net_classes_window = new WindowAssignNetClass(this);
      permanent_subwindows.add(assign_net_classes_window);
      
      length_violations_window = new WindowLengthViolations(stat, this);
      permanent_subwindows.add(length_violations_window);
      
      about_window = new WindowAbout(this);
      permanent_subwindows.add(about_window);
      
      move_parameter_window    = new WindowMoveParameter(stat, this);
      permanent_subwindows.add(move_parameter_window);
      
      unit_parameter_window    = new WindowUnitMeasure(this);
      permanent_subwindows.add(unit_parameter_window);
      
      unconnected_route_window = new WindowUnconnectedRoute(stat, this);
      permanent_subwindows.add(unconnected_route_window);
      
      route_stubs_window       = new WindowRouteStubs(stat, this);
      permanent_subwindows.add(route_stubs_window);

      autoroute_parameter_window = new WindowAutorouteParameter(stat, this);
      permanent_subwindows.add(autoroute_parameter_window);
      
      debug_config = new WindowDebugConfig(stat,this);
      permanent_subwindows.add(debug_config);
      
      }

   /**
    * Creates the additional frames of the board frame.
    */
   private void initialize_windows()
      {
      allocate_permanent_subwindows();

      work_frame.setLocation(120, 0);

      select_parameter_window.setLocation(0, 0);
      select_parameter_window.setVisible(true);

      route_parameter_window.setLocation(0, 100);
      autoroute_parameter_window.setLocation(0, 200);
      move_parameter_window.setLocation(0, 50);
      clearance_matrix_window.setLocation(0, 150);
      via_window.setLocation(50, 150);
      edit_vias_window.setLocation(100, 150);
      edit_net_rules_window.setLocation(100, 200);
      assign_net_classes_window.setLocation(100, 250);
      padstacks_window.setLocation(100, 30);
      packages_window.setLocation(200, 30);
      components_window.setLocation(300, 30);
      incompletes_window.setLocation(400, 30);
      clearance_violations_window.setLocation(500, 30);
      length_violations_window.setLocation(550, 30);
      net_info_window.setLocation(350, 30);
      unconnected_route_window.setLocation(650, 30);
      route_stubs_window.setLocation(600, 30);
      window_beanshell.setLocation(0, 250);
      layer_visibility_window.setLocation(0, 450);
      object_visibility_window.setLocation(0, 550);
      display_misc_window.setLocation(0, 350);
      color_manager.setLocation(0, 600);
      about_window.setLocation(200, 200);
      }

   /**
    * Returns the currently used locale for the language dependent output.
    */
   public java.util.Locale get_locale()
      {
      return stat.locale;
      }

   /**
    * Sets the background of the board panel
    */
   public void set_board_background(java.awt.Color p_color)
      {
      board_panel.setBackground(p_color);
      }

   /**
    * Refresh all displayed after the user unit has changed.
    */
   public void refresh_windows()
      {
      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         cur_subwindow.refresh();
      }

   /**
    * Sets the selected button in the menu button button group
    */
   public void hilight_selected_button()
      {
      toolbar_panel.hilight_selected_button();
      }

   /**
    * Used for storing the subwindowfilters in a snapshot.
    */
   public SubwindowSelections get_snapshot_subwindow_selections()
      {
      SubwindowSelections result = new SubwindowSelections();
      result.incompletes_selection = incompletes_window.get_snapshot_info();
      result.packages_selection = packages_window.get_snapshot_info();
      result.nets_selection = net_info_window.get_snapshot_info();
      result.components_selection = components_window.get_snapshot_info();
      result.padstacks_selection = padstacks_window.get_snapshot_info();
      return result;
      }

   /**
    * Used for restoring the subwindowfilters from a snapshot.
    */
   public void set_snapshot_subwindow_selections(SubwindowSelections p_filters)
      {
      incompletes_window.set_snapshot_info(p_filters.incompletes_selection);
      packages_window.set_snapshot_info(p_filters.packages_selection);
      net_info_window.set_snapshot_info(p_filters.nets_selection);
      components_window.set_snapshot_info(p_filters.components_selection);
      padstacks_window.set_snapshot_info(p_filters.padstacks_selection);
      }

   /**
    * Repaints this board frame and all the subwindows of the board.
    */
   public void repaint_all()
      {
      work_frame.repaint();

      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         cur_subwindow.repaint();
      }

   public final GuiResources newGuiResources ( String key )
      {
      return new GuiResources (stat, key);
      }
   
   public final boolean debug ( int mask, int level )
      {
      return stat.debug(mask, level);
      }

   public final void userPrintln ( String message )
      {
      stat.userPrintln(message);
      }

   public final JFrame get_JFrame()
      {
      return work_frame;
      }
   
   public final void setVisible ( boolean visible )
      {
      work_frame.setVisible(visible);
      }
         
   public final void setBounds ( Rectangle bounds )
      {
      work_frame.setBounds(bounds);
      }
   
   public final void repaint ()
      {
      work_frame.repaint();
      }
   
   /**
    * Used to have some meaningful info on this object
    */
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(1000);
      risul.append(classname);
      risul.append(" available object: board_panel");
      
      return risul.toString();
      }
      
   public final void showMessageDialog (Object message, String title )
      {
      JOptionPane.showMessageDialog(work_frame, message, title, JOptionPane.PLAIN_MESSAGE);
      }

   public int showConfirmDialog (Object message, String title )
      {
      return JOptionPane.showConfirmDialog(work_frame, message, title, JOptionPane.YES_NO_OPTION);
      }
   
   
private class WindowStateListener extends java.awt.event.WindowAdapter
   {
   public void windowClosing(java.awt.event.WindowEvent evt)
      {
      work_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      
      if ( debug(Mdbg.GUI, Ldbg.RELEASE))
         {
         int option = JOptionPane.showConfirmDialog(null, resources.getString("confirm_cancel"), null, JOptionPane.YES_NO_OPTION);
         if (option == JOptionPane.NO_OPTION)
            {
            work_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
         }
      }

   public void windowIconified(java.awt.event.WindowEvent evt)
      {
      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         cur_subwindow.parent_iconified();
      
      for (GuiSubWindow curr_subwindow : temporary_subwindows)
         curr_subwindow.parent_iconified();
      }

   public void windowDeiconified(java.awt.event.WindowEvent evt)
      {
      for (GuiSubWindowSavable cur_subwindow : permanent_subwindows )
         cur_subwindow.parent_deiconified();

      for (GuiSubWindow curr_subwindow : temporary_subwindows)
         curr_subwindow.parent_deiconified();
      }
   }

   
   }
