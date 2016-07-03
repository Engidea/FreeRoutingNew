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
 * GuiConfigFile.java
 *
 * Created on 26. Dezember 2004, 08:29
 */

package gui.config;

import gui.BoardFrame;
import gui.GuiSubWindow;
import gui.varie.IndentFileWriter;
import interactive.IteraBoard;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JFrame;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 * Description of a text file, where the board independent interactive settings are stored.
 *
 * @author Alfons Wirtz
 */
public final class GuiConfigFile 
   {
   private final BoardFrame board_frame;
   private final interactive.IteraBoard board_handling;
   // Used, when reading a defaults file, null otherwise. 
   private final GuiConfigScanner scanner;
   // Used, when writing a defaults file; null otherwise
   private final IndentFileWriter out_file;

   /**
    * Writes the GUI setting of p_board_frame as default to p_output_stream
    * @throws IOException 
    */
   public static void write( BoardFrame p_board_frame, IteraBoard p_board_handling, java.io.OutputStream p_output_stream) throws IOException
      {
      IndentFileWriter output_file = new IndentFileWriter(p_output_stream);

      GuiConfigFile result = new GuiConfigFile(p_board_frame, p_board_handling, null, output_file);

      result.write_defaults_scope();
      
      output_file.close();
      }

   /**
    * Reads the GUI setting of p_board_frame from file.
    * @throws IOException 
    */
   public static void read(BoardFrame p_board_frame, IteraBoard p_board_handling, InputStream p_input_stream) throws IOException
      {
      GuiConfigScanner scanner = new GuiConfigScanner(new InputStreamReader(p_input_stream));

      GuiConfigFile config_file = new GuiConfigFile(p_board_frame, p_board_handling, scanner, null);

      config_file.read_defaults_scope();
      }

   private GuiConfigFile(BoardFrame p_board_frame, interactive.IteraBoard p_board_handling, GuiConfigScanner p_scanner, IndentFileWriter p_output_file)
      {
      board_frame = p_board_frame;
      board_handling = p_board_handling;
      scanner = p_scanner;
      out_file = p_output_file;
      }

   private void write_defaults_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("gui_defaults");
      write_windows_scope();
      write_colors_scope();
      write_parameter_scope();
      out_file.end_scope();
      }

   private boolean read_defaults_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();

      if (next_token != GuiConfigKeyword.OPEN_BRACKET)
         {
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.GUI_DEFAULTS)
         {
         return false;
         }

      // read the direct subscopes of the gui_defaults scope
      for (;;)
         {
         Object prev_token = next_token;
         next_token = scanner.next_token();
         if (next_token == null)
            {
            // end of file
            return true;
            }
         if (next_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == GuiConfigKeyword.OPEN_BRACKET)
            {
            if (next_token == GuiConfigKeyword.COLORS)
               {
               if (!read_colors_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.WINDOWS)
               {
               if (!read_windows_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.PARAMETER)
               {
               if (!read_parameter_scope())
                  {
                  return false;
                  }
               }
            else
               {
               // overread all scopes except the routes scope for the time being
               skip_scope(scanner);
               }
            }
         }
      
      board_frame.refresh_windows();
      
      return true;
      }

   private boolean read_windows_scope() throws java.io.IOException
      {
      // read the direct subscopes of the windows scope
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         next_token = scanner.next_token();
         if (next_token == null)
            {
            // unexpected end of file
            return false;
            }
         if (next_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == GuiConfigKeyword.OPEN_BRACKET)
            {
            if (!(next_token instanceof GuiConfigKeyword))
               {
               System.out.println("GuiConfigFile.windows: Keyword expected, next_token="+next_token);
               return false;
               }
            if (!read_frame_scope((GuiConfigKeyword) next_token))
               {
               return false;
               }
            }
         }
      return true;
      }

   private void write_windows_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("windows");
      write_frame_scope(board_frame.get_JFrame(), "board_frame");
      write_frame_scope(board_frame.color_manager, "color_manager");
      write_frame_scope(board_frame.layer_visibility_window, "layer_visibility");
      write_frame_scope(board_frame.object_visibility_window, "object_visibility");
      write_frame_scope(board_frame.display_misc_window, "display_miscellanious");
      write_frame_scope(board_frame.window_beanshell, "snapshots");
      write_frame_scope(board_frame.select_parameter_window, "select_parameter");
      write_frame_scope(board_frame.route_parameter_window, "route_parameter");
      write_frame_scope(board_frame.route_parameter_window.manual_rule_window, "manual_rules");
      write_frame_scope(board_frame.autoroute_parameter_window, "autoroute_parameter");
      write_frame_scope(board_frame.move_parameter_window, "move_parameter");
      write_frame_scope(board_frame.clearance_matrix_window, "clearance_matrix");
      write_frame_scope(board_frame.via_window, "via_rules");
      write_frame_scope(board_frame.edit_vias_window, "edit_vias");
      write_frame_scope(board_frame.edit_net_rules_window, "edit_net_rules");
      write_frame_scope(board_frame.assign_net_classes_window, "assign_net_rules");
      write_frame_scope(board_frame.padstacks_window, "padstack_info");
      write_frame_scope(board_frame.packages_window, "package_info");
      write_frame_scope(board_frame.components_window, "component_info");
      write_frame_scope(board_frame.net_info_window, "net_info");
      write_frame_scope(board_frame.incompletes_window, "incompletes_info");
      write_frame_scope(board_frame.clearance_violations_window, "violations_info");
      out_file.end_scope();
      }

   private boolean read_frame_scope(GuiConfigKeyword p_frame) throws java.io.IOException
      {
      boolean is_visible;
      Object next_token = scanner.next_token();
      if (next_token == GuiConfigKeyword.VISIBLE)
         {
         is_visible = true;
         }
      else if (next_token == GuiConfigKeyword.NOT_VISIBLE)
         {
         is_visible = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_frame_scope: visible or not_visible expected");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.OPEN_BRACKET)
         {
         System.out.println("GuiConfigFile.read_frame_scope: open_bracket expected");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.BOUNDS)
         {
         System.out.println("GuiConfigFile.read_frame_scope: bounds expected");
         return false;
         }
      java.awt.Rectangle bounds = read_rectangle();
      if (bounds == null)
         {
         return false;
         }
      for (int i = 0; i < 2; ++i)
         {
         next_token = scanner.next_token();
         if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
            {
            System.out.println("GuiConfigFile.read_frame_scope: closing bracket expected");
            return false;
            }
         }

      GuiSubWindow sub_window = null;
      
      if (p_frame == GuiConfigKeyword.BOARD_FRAME)
         {
         board_frame.setVisible(is_visible);
         board_frame.setBounds(bounds);
         }
      else if (p_frame == GuiConfigKeyword.COLOR_MANAGER)
         {
         sub_window = board_frame.color_manager;
         }
      else if (p_frame == GuiConfigKeyword.OBJECT_VISIBILITY)
         {
         sub_window = board_frame.object_visibility_window;
         }
      else if (p_frame == GuiConfigKeyword.LAYER_VISIBILITY)
         {
         sub_window = board_frame.layer_visibility_window;
         }
      else if (p_frame == GuiConfigKeyword.DISPLAY_MISCELLANIOUS)
         {
         sub_window = board_frame.display_misc_window;
         }
      else if (p_frame == GuiConfigKeyword.SNAPSHOTS)
         {
         sub_window = board_frame.window_beanshell;
         }
      else if (p_frame == GuiConfigKeyword.SELECT_PARAMETER)
         {
         sub_window = board_frame.select_parameter_window;
         }
      else if (p_frame == GuiConfigKeyword.ROUTE_PARAMETER)
         {
         sub_window = board_frame.route_parameter_window;
         }
      else if (p_frame == GuiConfigKeyword.MANUAL_RULES)
         {
         sub_window = board_frame.route_parameter_window.manual_rule_window;
         }
      else if (p_frame == GuiConfigKeyword.AUTOROUTE_PARAMETER)
         {
         sub_window = board_frame.autoroute_parameter_window;
         }
      else if (p_frame == GuiConfigKeyword.MOVE_PARAMETER)
         {
         sub_window = board_frame.move_parameter_window;
         }
      else if (p_frame == GuiConfigKeyword.CLEARANCE_MATRIX)
         {
         sub_window = board_frame.clearance_matrix_window;
         }
      else if (p_frame == GuiConfigKeyword.VIA_RULES)
         {
         sub_window = board_frame.via_window;
         }
      else if (p_frame == GuiConfigKeyword.EDIT_VIAS)
         {
         sub_window = board_frame.edit_vias_window;
         }
      else if (p_frame == GuiConfigKeyword.EDIT_NET_RULES)
         {
         sub_window = board_frame.edit_net_rules_window;
         }
      else if (p_frame == GuiConfigKeyword.ASSIGN_NET_RULES)
         {
         sub_window = board_frame.assign_net_classes_window;
         }
      else if (p_frame == GuiConfigKeyword.PADSTACK_INFO)
         {
         sub_window = board_frame.padstacks_window;
         }
      else if (p_frame == GuiConfigKeyword.PACKAGE_INFO)
         {
         sub_window = board_frame.packages_window;
         }
      else if (p_frame == GuiConfigKeyword.COMPONENT_INFO)
         {
         sub_window = board_frame.components_window;
         }
      else if (p_frame == GuiConfigKeyword.NET_INFO)
         {
         sub_window = board_frame.net_info_window;
         }
      else if (p_frame == GuiConfigKeyword.INCOMPLETES_INFO)
         {
         sub_window = board_frame.incompletes_window;
         }
      else if (p_frame == GuiConfigKeyword.VIOLATIONS_INFO)
         {
         sub_window = board_frame.clearance_violations_window;
         }
      else
         {
         System.out.println("GuiConfigFile.read_frame_scope: unknown frame "+p_frame);
         return false;
         }
      
      if ( sub_window != null )
         {
         sub_window.setVisible(is_visible);
         // Set only the location, Do not change the size of the frame because it depends on the layer count.
         sub_window.setLocation(bounds.getLocation());
         }
      
      return true;
      }
   
   

   private java.awt.Rectangle read_rectangle() throws java.io.IOException
      {
      int[] coor = new int[4];
      for (int i = 0; i < 4; ++i)
         {
         Object next_token = scanner.next_token();
         if (!(next_token instanceof Integer))
            {
            System.out.println("GuiConfigFile.read_rectangle: Integer expected");
            return null;
            }
         coor[i] = (Integer) next_token;
         }
      return new java.awt.Rectangle(coor[0], coor[1], coor[2], coor[3]);
      }

   private void write_frame_scope(GuiSubWindow subwin, String p_frame_name) throws java.io.IOException
      {
      write_frame_scope(subwin.getJFrame(),p_frame_name);
      }

   private void write_frame_scope(JFrame p_frame, String p_frame_name) throws IOException
      {
      out_file.start_scope();
      out_file.write(p_frame_name);
      out_file.new_line();
      if (p_frame.isVisible())
         {
         out_file.write("visible");
         }
      else
         {
         out_file.write("not_visible");
         }
      write_bounds(p_frame.getBounds());
      out_file.end_scope();
      }

   private void write_bounds(java.awt.Rectangle p_bounds) throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("bounds");
      out_file.new_line();
      Integer x = (int) p_bounds.getX();
      out_file.write(x.toString());
      Integer y = (int) p_bounds.getY();
      out_file.write(" ");
      out_file.write(y.toString());
      Integer width = (int) p_bounds.getWidth();
      out_file.write(" ");
      out_file.write(width.toString());
      Integer height = (int) p_bounds.getHeight();
      out_file.write(" ");
      out_file.write(height.toString());
      out_file.end_scope();
      }

   private boolean read_colors_scope() throws java.io.IOException
      {
      // read the direct subscopes of the colors scope
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         next_token = scanner.next_token();
         if (next_token == null)
            {
            // unexpected end of file
            return false;
            }
         if (next_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == GuiConfigKeyword.OPEN_BRACKET)
            {

            if (next_token == GuiConfigKeyword.BACKGROUND)
               {
               if (!read_background_color())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.CONDUCTION)
               {
               if (!read_conduction_colors())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.HILIGHT)
               {
               if (!read_hilight_color())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.INCOMPLETES)
               {
               if (!read_incompletes_color())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.KEEPOUT)
               {
               if (!read_keepout_colors())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.OUTLINE)
               {
               if (!read_outline_color())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.COMPONENT_FRONT)
               {
               if (!read_component_color(true))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.COMPONENT_BACK)
               {
               if (!read_component_color(false))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.LENGTH_MATCHING)
               {
               if (!read_length_matching_color())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.PINS)
               {
               if (!read_pin_colors())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.TRACES)
               {
               if (!read_trace_colors(false))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.FIXED_TRACES)
               {
               if (!read_trace_colors(true))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.VIA_KEEPOUT)
               {
               if (!read_via_keepout_colors())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.VIAS)
               {
               if (!read_via_colors(false))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.FIXED_VIAS)
               {
               if (!read_via_colors(true))
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.VIOLATIONS)
               {
               if (!read_violations_color())
                  {
                  return false;
                  }
               }
            else
               {
               // skip unknown scope
               skip_scope(scanner);
               }
            }
         }
      return true;
      }

   private boolean read_trace_colors(boolean p_fixed) throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_trace_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_trace_colors(curr_colors, p_fixed);
      return true;
      }

   private boolean read_via_colors(boolean p_fixed) throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_via_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_via_colors(curr_colors, p_fixed);
      return true;
      }

   private boolean read_pin_colors() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_pin_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_pin_colors(curr_colors);
      return true;
      }

   private boolean read_conduction_colors() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_conduction_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_conduction_colors(curr_colors);
      return true;
      }

   private boolean read_keepout_colors() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_obstacle_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_keepout_colors(curr_colors);
      return true;
      }

   private boolean read_via_keepout_colors() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_via_obstacle_color_intensity(intensity);
      java.awt.Color[] curr_colors = read_color_array();
      if (curr_colors.length < 1)
         {
         return false;
         }
      board_handling.gdi_context.item_color_table.set_via_keepout_colors(curr_colors);
      return true;
      }

   private boolean read_background_color() throws java.io.IOException
      {
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_background_color(curr_color);
      board_frame.set_board_background(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_background_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_hilight_color() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_hilight_color_intensity(intensity);
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_hilight_color(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_higlight_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_incompletes_color() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_incomplete_color_intensity(intensity);
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_incomplete_color(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_incompletes_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_length_matching_color() throws java.io.IOException
      {
      double intensity = read_color_intensity();
      if (intensity < 0)
         {
         return false;
         }
      board_handling.gdi_context.set_length_matching_area_color_intensity(intensity);
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_length_matching_area_color(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_length_matching_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_violations_color() throws java.io.IOException
      {
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_violations_color(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_violations_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_outline_color() throws java.io.IOException
      {
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_outline_color(curr_color);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_outline_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private boolean read_component_color(boolean p_front) throws java.io.IOException
      {
      java.awt.Color curr_color = read_color();
      if (curr_color == null)
         {
         return false;
         }
      board_handling.gdi_context.other_color_table.set_component_color(curr_color, p_front);
      Object next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_component_color: closing bracket expected");
         return false;
         }
      return true;
      }

   private double read_color_intensity() throws java.io.IOException
      {
      double result;
      Object next_token = scanner.next_token();
      if (next_token instanceof Double)
         {
         result = (Double) next_token;
         }
      else if (next_token instanceof Integer)
         {
         result = (Integer) next_token;
         }
      else
         {
         System.out.println("GuiConfigFile.read_color_intensity: Number expected");
         result = -1;
         }
      return result;
      }

   /**
    * reads a java.awt.Color from the defaults file. Returns null, if no valid
    * color was found.
    */
   private java.awt.Color read_color() throws java.io.IOException
      {
      int[] rgb_color_arr = new int[3];
      for (int i = 0; i < 3; ++i)
         {
         Object next_token = scanner.next_token();
         if (!(next_token instanceof Integer))
            {
            if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
               {
               System.out.println("GuiConfigFile.read_color: closing bracket expected");
               }
            return null;
            }
         rgb_color_arr[i] = (Integer) next_token;
         }
      return new java.awt.Color(rgb_color_arr[0], rgb_color_arr[1], rgb_color_arr[2]);
      }

   /**
    * reads a n array java.awt.Color from the defaults file. Returns null, if no
    * valid colors were found.
    */
   private java.awt.Color[] read_color_array() throws java.io.IOException
      {
      java.util.Collection<java.awt.Color> color_list = new java.util.LinkedList<java.awt.Color>();
      for (;;)
         {
         java.awt.Color curr_color = read_color();
         if (curr_color == null)
            {
            break;
            }
         color_list.add(curr_color);
         }
      java.awt.Color[] result = new java.awt.Color[color_list.size()];
      java.util.Iterator<java.awt.Color> it = color_list.iterator();
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = it.next();
         }
      return result;
      }

   private void write_colors_scope() throws java.io.IOException
      {
      freert.graphics.GdiContext graphics_context = board_handling.gdi_context;
      out_file.start_scope();
      out_file.write("colors");
      out_file.start_scope();
      out_file.write("background");
      write_color_scope(graphics_context.get_background_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("hilight");
      write_color_intensity(graphics_context.get_hilight_color_intensity());
      write_color_scope(graphics_context.get_hilight_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("incompletes");
      write_color_intensity(graphics_context.get_incomplete_color_intensity());
      write_color_scope(graphics_context.get_incomplete_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("outline");
      write_color_scope(graphics_context.get_outline_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("component_front");
      write_color_scope(graphics_context.get_component_color(true));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("component_back");
      write_color_scope(graphics_context.get_component_color(false));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("violations");
      write_color_scope(graphics_context.get_violations_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("length_matching");
      write_color_intensity(graphics_context.get_length_matching_area_color_intensity());
      write_color_scope(graphics_context.get_length_matching_area_color());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("traces");
      write_color_intensity(graphics_context.get_trace_color_intensity());
      write_color(graphics_context.get_trace_colors(false));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("fixed_traces");
      write_color_intensity(graphics_context.get_trace_color_intensity());
      write_color(graphics_context.get_trace_colors(true));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("vias");
      write_color_intensity(graphics_context.get_via_color_intensity());
      write_color(graphics_context.get_via_colors(false));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("fixed_vias");
      write_color_intensity(graphics_context.get_via_color_intensity());
      write_color(graphics_context.get_via_colors(true));
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("pins");
      write_color_intensity(graphics_context.get_pin_color_intensity());
      write_color(graphics_context.get_pin_colors());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("conduction");
      write_color_intensity(graphics_context.get_conduction_color_intensity());
      write_color(graphics_context.get_conduction_colors());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("keepout");
      write_color_intensity(graphics_context.get_obstacle_color_intensity());
      write_color(graphics_context.get_obstacle_colors());
      out_file.end_scope();
      out_file.start_scope();
      out_file.write("via_keepout");
      write_color_intensity(graphics_context.get_via_obstacle_color_intensity());
      write_color(graphics_context.get_via_obstacle_colors());
      out_file.end_scope();
      out_file.end_scope();
      }

   private void write_color_intensity(double p_value) throws java.io.IOException
      {
      out_file.write(" ");
      Float value = (float) p_value;
      out_file.write(value.toString());
      }

   private void write_color_scope(java.awt.Color p_color) throws java.io.IOException
      {
      out_file.new_line();
      Integer red = p_color.getRed();
      out_file.write(red.toString());
      out_file.write(" ");
      Integer green = p_color.getGreen();
      out_file.write(green.toString());
      out_file.write(" ");
      Integer blue = p_color.getBlue();
      out_file.write(blue.toString());
      }

   private void write_color(java.awt.Color[] p_colors) throws java.io.IOException
      {
      for (int i = 0; i < p_colors.length; ++i)
         {
         write_color_scope(p_colors[i]);
         }
      }

   private boolean read_parameter_scope() throws java.io.IOException
      {
      // read the subscopes of the parameter scope
      Object next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         next_token = scanner.next_token();
         if (next_token == null)
            {
            // unexpected end of file
            return false;
            }
         if (next_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == GuiConfigKeyword.OPEN_BRACKET)
            {

            if (next_token == GuiConfigKeyword.SELECTION_LAYERS)
               {
               if (!read_selection_layer_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.VIA_SNAP_TO_SMD_CENTER)
               {
               if (!read_via_snap_to_smd_center_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.SHOVE_ENABLED)
               {
               if (!read_shove_enabled_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.DRAG_COMPONENTS_ENABLED)
               {
               if (!read_drag_components_enabled_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.ROUTE_MODE)
               {
               if (!read_route_mode_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.PULL_TIGHT_REGION)
               {
               if (!read_pull_tight_region_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.PULL_TIGHT_ACCURACY)
               {
               if (!read_pull_tight_accuracy_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.IGNORE_CONDUCTION_AREAS)
               {
               if (!read_ignore_conduction_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.AUTOMATIC_LAYER_DIMMING)
               {
               if (!read_automatic_layer_dimming_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.CLEARANCE_COMPENSATION)
               {
               if (!read_clearance_compensation_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.HILIGHT_ROUTING_OBSTACLE)
               {
               if (!read_hilight_routing_obstacle_scope())
                  {
                  return false;
                  }
               }
            else if (next_token == GuiConfigKeyword.SELECTABLE_ITEMS)
               {
               if (!read_selectable_item_scope())
                  {
                  return false;
                  }
               }
            else
               {
               // skip unknown scope
               skip_scope(scanner);
               }
            }
         }
      return true;
      }

   private void write_parameter_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("parameter");
      write_selection_layer_scope();
      write_selectable_item_scope();
      write_via_snap_to_smd_center_scope();
      write_route_mode_scope();
      write_shove_enabled_scope();
      write_drag_components_enabled_scope();
      write_hilight_routing_obstacle_scope();
      write_pull_tight_accuracy_scope();
      write_clearance_compensation_scope();
      write_ignore_conduction_scope();
      write_automatic_layer_dimming_scope();
      out_file.end_scope();
      }

   private boolean read_selection_layer_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean select_on_all_layers;
      if (next_token == GuiConfigKeyword.ALL_VISIBLE)
         {
         select_on_all_layers = true;
         }
      else if (next_token == GuiConfigKeyword.CURRENT_ONLY)
         {
         select_on_all_layers = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_selection_layer_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_selection_layer_scop: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_select_on_all_visible_layers(select_on_all_layers);
      return true;
      }

   private boolean read_shove_enabled_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean shove_enabled;
      if (next_token == GuiConfigKeyword.ON)
         {
         shove_enabled = true;
         }
      else if (next_token == GuiConfigKeyword.OFF)
         {
         shove_enabled = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_shove_enabled_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_shove_enabled_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_push_enabled(shove_enabled);
      return true;
      }

   private boolean read_drag_components_enabled_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean drag_components_enabled;
      if (next_token == GuiConfigKeyword.ON)
         {
         drag_components_enabled = true;
         }
      else if (next_token == GuiConfigKeyword.OFF)
         {
         drag_components_enabled = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_drag_components_enabled_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_drag_components_enabled_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_drag_components_enabled(drag_components_enabled);
      return true;
      }

   private boolean read_ignore_conduction_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean ignore_conduction;
      if (next_token == GuiConfigKeyword.ON)
         {
         ignore_conduction = true;
         }
      else if (next_token == GuiConfigKeyword.OFF)
         {
         ignore_conduction = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_ignore_conduction_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_ignore_conduction_scope: closing bracket expected");
         return false;
         }
      board_handling.set_ignore_conduction(ignore_conduction);
      return true;
      }

   private void write_shove_enabled_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("shove_enabled ");
      out_file.new_line();
      if (board_handling.itera_settings.is_push_enabled())
         {
         out_file.write("on");
         }
      else
         {
         out_file.write("off");
         }
      out_file.end_scope();
      }

   private void write_drag_components_enabled_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("drag_components_enabled ");
      out_file.new_line();
      if (board_handling.itera_settings.get_drag_components_enabled())
         {
         out_file.write("on");
         }
      else
         {
         out_file.write("off");
         }
      out_file.end_scope();
      }

   private void write_ignore_conduction_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("ignore_conduction_areas ");
      out_file.new_line();
      if (board_handling.get_routing_board().brd_rules.get_ignore_conduction())
         {
         out_file.write("on");
         }
      else
         {
         out_file.write("off");
         }
      out_file.end_scope();
      }

   private void write_selection_layer_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("selection_layers ");
      out_file.new_line();
      if (board_handling.itera_settings.get_select_on_all_visible_layers())
         {
         out_file.write("all_visible");
         }
      else
         {
         out_file.write("current_only");
         }
      out_file.end_scope();
      }

   private boolean read_route_mode_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean is_stitch_mode;
      if (next_token == GuiConfigKeyword.STITCHING)
         {
         is_stitch_mode = true;
         }
      else if (next_token == GuiConfigKeyword.DYNAMIC)
         {
         is_stitch_mode = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_roude_mode_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_selection_layer_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_stitch_route(is_stitch_mode);
      return true;
      }

   private void write_route_mode_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("route_mode ");
      out_file.new_line();
      if (board_handling.itera_settings.is_stitch_route())
         {
         out_file.write("stitching");
         }
      else
         {
         out_file.write("dynamic");
         }
      out_file.end_scope();
      }

   /**
    * TODO delete this
    */
   private boolean read_pull_tight_region_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      if (!(next_token instanceof Integer))
         {
         System.out.println("GuiConfigFile.read_pull_tight_region_scope: Integer expected");
         return false;
         }

      System.out.println("GuiConfigFile.read_pull_tight_region_scope: SHOULD NOT BE CALLED");

      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_pull_tight_region_scope: closing bracket expected");
         return false;
         }
      return true;
      }


   private boolean read_pull_tight_accuracy_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      if (!(next_token instanceof Integer))
         {
         System.out.println("GuiConfigFile.read_pull_tight_accuracy_scope: Integer expected");
         return false;
         }
      int pull_tight_accuracy = (Integer) next_token;
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_pull_tight_accuracy_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.pull_tight_accuracy_set(pull_tight_accuracy);
      return true;
      }

   private void write_pull_tight_accuracy_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("pull_tight_accuracy ");
      out_file.new_line();
      Integer pullt_min_move = board_handling.itera_settings.trace_pullt_min_move;
      out_file.write(pullt_min_move.toString());
      out_file.end_scope();
      }

   private boolean read_automatic_layer_dimming_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      double intensity;
      if (next_token instanceof Double)
         {
         intensity = (Double) next_token;
         }
      else if (next_token instanceof Integer)
         {
         intensity = (Integer) next_token;
         }
      else
         {
         System.out.println("GuiConfigFile.read_automatic_layer_dimming_scope: Integer expected");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_automatic_layer_dimming_scope: closing bracket expected");
         return false;
         }
      board_handling.gdi_context.set_auto_layer_dim_factor(intensity);
      return true;
      }

   private void write_automatic_layer_dimming_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("automatic_layer_dimming ");
      out_file.new_line();
      Float layer_dimming = (float) board_handling.gdi_context.get_auto_layer_dim_factor();
      out_file.write(layer_dimming.toString());
      out_file.end_scope();
      }

   private boolean read_hilight_routing_obstacle_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean hilight_obstacle;
      if (next_token == GuiConfigKeyword.ON)
         {
         hilight_obstacle = true;
         }
      else if (next_token == GuiConfigKeyword.OFF)
         {
         hilight_obstacle = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_hilight_routing_obstacle_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_hilight_routing_obstacle_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_hilight_routing_obstacle(hilight_obstacle);
      return true;
      }

   private void write_hilight_routing_obstacle_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("hilight_routing_obstacle ");
      out_file.new_line();
      if (board_handling.itera_settings.is_hilight_routing_obstacle())
         {
         out_file.write("on");
         }
      else
         {
         out_file.write("off");
         }
      out_file.end_scope();
      }

   /**
    * @deprecated
    */
   private boolean read_clearance_compensation_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();

      next_token = scanner.next_token();

      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_clearance_compensation_scope: closing bracket expected");
         return false;
         }

      return true;
      }

   /**
    * @Deprecated
    * @throws java.io.IOException
    */
   private void write_clearance_compensation_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("clearance_compensation ");
      out_file.new_line();
      out_file.write("off");
      out_file.end_scope();
      }

   private boolean read_via_snap_to_smd_center_scope() throws java.io.IOException
      {
      Object next_token = scanner.next_token();
      boolean snap;
      if (next_token == GuiConfigKeyword.ON)
         {
         snap = true;
         }
      else if (next_token == GuiConfigKeyword.OFF)
         {
         snap = false;
         }
      else
         {
         System.out.println("GuiConfigFile.read_via_snap_to_smd_center_scope: unexpected token");
         return false;
         }
      next_token = scanner.next_token();
      if (next_token != GuiConfigKeyword.CLOSED_BRACKET)
         {
         System.out.println("GuiConfigFile.read_via_snap_to_smd_center_scope: closing bracket expected");
         return false;
         }
      board_handling.itera_settings.set_via_snap_to_smd_center(snap);
      return true;
      }

   private void write_via_snap_to_smd_center_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("via_snap_to_smd_center ");
      out_file.new_line();
      if (board_handling.itera_settings.is_via_snap_to_smd_center())
         {
         out_file.write("on");
         }
      else
         {
         out_file.write("off");
         }
      out_file.end_scope();
      }

   private boolean read_selectable_item_scope() throws java.io.IOException
      {
      ItemSelectionFilter item_selection_filter = board_handling.itera_settings.get_item_selection_filter();
      item_selection_filter.deselect_all();
      for (;;)
         {
         Object next_token = scanner.next_token();
         if (next_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            break;
            }
         if (next_token == GuiConfigKeyword.TRACES)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.TRACES, true);
            }
         else if (next_token == GuiConfigKeyword.VIAS)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.VIAS, true);
            }
         else if (next_token == GuiConfigKeyword.PINS)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.PINS, true);
            }
         else if (next_token == GuiConfigKeyword.CONDUCTION)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.CONDUCTION, true);
            }
         else if (next_token == GuiConfigKeyword.KEEPOUT)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.KEEPOUT, true);
            }
         else if (next_token == GuiConfigKeyword.VIA_KEEPOUT)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.VIA_KEEPOUT, true);
            }
         else if (next_token == GuiConfigKeyword.FIXED)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.FIXED, true);
            }
         else if (next_token == GuiConfigKeyword.UNFIXED)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.UNFIXED, true);
            }
         else if (next_token == GuiConfigKeyword.VIAS)
            {
            item_selection_filter.set_selected(ItemSelectionChoice.VIAS, true);
            }
         else
            {
            System.out.println("GuiConfigFile.read_selectable_item_scope: unexpected token");
            return false;
            }
         }
      return true;
      }

   private void write_selectable_item_scope() throws java.io.IOException
      {
      out_file.start_scope();
      out_file.write("selectable_items ");
      out_file.new_line();
      ItemSelectionFilter item_selection_filter = board_handling.itera_settings.get_item_selection_filter();
      ItemSelectionChoice[] selectable_choices = ItemSelectionChoice.values();
      for (int i = 0; i < selectable_choices.length; ++i)
         {
         if (item_selection_filter.is_selected(selectable_choices[i]))
            {
            out_file.write(selectable_choices[i].toString());
            out_file.write(" ");
            }
         }
      out_file.end_scope();
      }



   /**
    * Skips the current scope. Returns false, if no legal scope was found.
    */
   private static boolean skip_scope(GuiConfigScanner p_scanner)
      {
      int open_bracked_count = 1;
      while (open_bracked_count > 0)
         {
         Object curr_token = null;
         try
            {
            curr_token = p_scanner.next_token();
            }
         catch (Exception e)
            {
            System.out.println("GuiConfigFile.skip_scope: Error while scanning file");
            System.out.println(e);
            return false;
            }
         if (curr_token == null)
            {
            return false; // end of file
            }
         if (curr_token == GuiConfigKeyword.OPEN_BRACKET)
            {
            ++open_bracked_count;
            }
         else if (curr_token == GuiConfigKeyword.CLOSED_BRACKET)
            {
            --open_bracked_count;
            }
         }
      System.out.println("GuiConfigFile.skip_spope: unknown scope skipped");
      return true;
      }
   }
