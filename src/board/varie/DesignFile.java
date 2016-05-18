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
 * DesignFile.java
 *
 * Created on 25. Oktober 2006, 07:48
 *
 */
package board.varie;

import gui.BoardFrame;
import gui.varie.FileFilter;
import gui.varie.GuiResources;
import gui.win.WindowMessage;
import interactive.IteraBoard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import main.Stat;

/**
 * File functionality
 * 
 * @author Alfons Wirtz
 */
public final class DesignFile
   {
   private final Stat stat;

   public static final String[] all_file_extensions = { "bin", "dsn" };
   public static final String[] text_file_extensions = { "dsn" };
   public static final String binary_file_extension = "bin";
   public static final String RULES_FILE_EXTENSION = ".rules";

   private final GuiResources resources;
   
   private File output_file;
   private final File input_file;

   private JFileChooser file_chooser;


   /**
    * Creates a new instance of DesignFile. If p_is_webstart, the application was opened with Java Web Start.
    */
   public DesignFile(Stat p_stat, File p_design_file, JFileChooser p_file_chooser)
      {
      stat = p_stat;
      
      resources = new GuiResources(stat, "gui.resources.BoardMenuFile");      

      file_chooser = p_file_chooser;
      input_file = p_design_file;
      output_file = p_design_file;

      if (p_design_file != null)
         {
         String file_name = p_design_file.getName();
         String[] name_parts = file_name.split("\\.");
         if (name_parts[name_parts.length - 1].compareToIgnoreCase(binary_file_extension) != 0)
            {
            String binfile_name = name_parts[0] + "." + binary_file_extension;
            output_file = new File(p_design_file.getParent(), binfile_name);
            }
         }
      }

   /**
    * Gets an InputStream from the file. Returns null, if the algorithm failed.
    */
   public java.io.InputStream get_input_stream()
      {
      if (input_file == null)
         {
         return null;
         }

      try
         {
         return new FileInputStream(input_file);
         }
      catch (Exception e)
         {
         return null;
         }
      }

   /**
    * Gets the file name as a String. Returns null on failure.
    */
   public String get_name()
      {
      if (input_file != null)
         {
         return input_file.getName();
         }
      else
         {
         return null;
         }
      }

   /**
    * @param p_parent
    * @param p_board_frame
    */
   public void save_as_dialog(java.awt.Component p_parent, BoardFrame p_board_frame)
      {
      String[] file_name_parts = get_name().split("\\.", 2);
      String design_name = file_name_parts[0];

      if (file_chooser == null)
         {
         String design_dir_name;
         if (output_file == null)
            {
            design_dir_name = null;
            }
         else
            {
            design_dir_name = output_file.getParent();
            }
         file_chooser = new JFileChooser(design_dir_name);
         FileFilter file_filter = new FileFilter(all_file_extensions);
         file_chooser.setFileFilter(file_filter);
         }

      file_chooser.showSaveDialog(p_parent);
      File new_file = file_chooser.getSelectedFile();
      if (new_file == null)
         {
         p_board_frame.screen_messages.set_status_message(resources.getString("message_1"));
         return;
         }
      String new_file_name = new_file.getName();
      String[] new_name_parts = new_file_name.split("\\.");
      String found_file_extension = new_name_parts[new_name_parts.length - 1];
      if (found_file_extension.compareToIgnoreCase(binary_file_extension) == 0)
         {
         p_board_frame.screen_messages.set_status_message(resources.getString("message_2") + " " + new_file.getName());
         output_file = new_file;
         p_board_frame.save();
         }
      else
         {
         if (found_file_extension.compareToIgnoreCase("dsn") != 0)
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_3"));
            return;
            }
         java.io.OutputStream output_stream;
         try
            {
            output_stream = new FileOutputStream(new_file);
            }
         catch (Exception e)
            {
            output_stream = null;
            }
         
         if (p_board_frame.board_panel.board_handling.export_to_dsn_file(output_stream, design_name, false))
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_4") + " " + new_file_name + " " + resources.getString("message_5"));
            }
         else
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_6") + " " + new_file_name + " " + resources.getString("message_7"));
            }
         }
      }

   /**
    * Writes a Specctra Session File to update the design file in the host system. Returns false, if the write failed
    */
   public boolean write_specctra_ses_file(BoardFrame p_board_frame)
      {
      String design_file_name = get_name();
      String[] file_name_parts = design_file_name.split("\\.", 2);
      String design_name = file_name_parts[0];
         {
         String output_file_name = design_name + ".ses";
         File curr_output_file = new File(get_parent(), output_file_name);
         java.io.OutputStream output_stream;
         try
            {
            output_stream = new FileOutputStream(curr_output_file);
            }
         catch (Exception e)
            {
            output_stream = null;
            }

         if (p_board_frame.board_panel.board_handling.export_specctra_session_file(design_file_name, output_stream))
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_11") + " " + output_file_name + " " + resources.getString("message_12"));
            }
         else
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_13") + " " + output_file_name + " " + resources.getString("message_7"));
            return false;
            }
         }
      if (WindowMessage.confirm(resources.getString("confirm")))
         {
         return write_rules_file(design_name, p_board_frame.board_panel.board_handling);
         }
      return true;
      }

   /**
    * Saves the board rule to file, so that they can be reused later on.
    * pippo
    */
   private boolean write_rules_file(String p_design_name, interactive.IteraBoard p_board_handling)
      {
      String rules_file_name = p_design_name + RULES_FILE_EXTENSION;

      File rules_file = new File(get_parent(), rules_file_name);
      
      stat.userPrintln("board write rule to "+rules_file);
      
      try
         {
         OutputStream output_stream = new FileOutputStream(rules_file);
         RulesFile.write(p_board_handling, output_stream, p_design_name);
         }
      catch (java.io.IOException e)
         {
         System.out.println("unable to create rules file");
         return false;
         }

      
      return true;
      }

   public boolean read_rules_file(String p_design_name, String p_parent_name, IteraBoard p_board_handling, String p_confirm_message)
      {
      boolean result = true;
      String rule_file_name = p_design_name + ".rules";
      boolean dsn_file_generated_by_host = p_board_handling.get_routing_board().host_com.specctra_parser_info.dsn_file_generated_by_host;

      try
         {
         File rules_file = new File(p_parent_name, rule_file_name);
         java.io.InputStream input_stream = new FileInputStream(rules_file);
         if (input_stream != null && dsn_file_generated_by_host && WindowMessage.confirm(p_confirm_message))
            {
            result = RulesFile.read(input_stream, p_design_name, p_board_handling);
            }
         else
            {
            result = false;
            }
         try
            {
            if (input_stream != null)
               {
               input_stream.close();
               }
            rules_file.delete();
            }
         catch (java.io.IOException e)
            {
            result = false;
            }
         }
      catch (FileNotFoundException e)
         {
         result = false;
         }

      return result;
      }

   public void update_eagle(BoardFrame p_board_frame)
      {
      String design_file_name = get_name();
      java.io.ByteArrayOutputStream session_output_stream = new java.io.ByteArrayOutputStream();
      if (!p_board_frame.board_panel.board_handling.export_specctra_session_file(design_file_name, session_output_stream))
         {
         return;
         }
      java.io.InputStream input_stream = new java.io.ByteArrayInputStream(session_output_stream.toByteArray());

      String[] file_name_parts = design_file_name.split("\\.", 2);
      String design_name = file_name_parts[0];
      String output_file_name = design_name + ".scr";
         {
         File curr_output_file = new File(get_parent(), output_file_name);
         java.io.OutputStream output_stream;
         try
            {
            output_stream = new FileOutputStream(curr_output_file);
            }
         catch (Exception e)
            {
            output_stream = null;
            }

         if (p_board_frame.board_panel.board_handling.export_eagle_session_file(input_stream, output_stream))
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_14") + " " + output_file_name + " " + resources.getString("message_15"));
            }
         else
            {
            p_board_frame.screen_messages.set_status_message(resources.getString("message_16") + " " + output_file_name + " " + resources.getString("message_7"));
            }
         }
      if (WindowMessage.confirm(resources.getString("confirm")))
         {
         write_rules_file(design_name, p_board_frame.board_panel.board_handling);
         }
      }

   /**
    * Gets the binary file for saving or null, if the design file is not available because the application is run with Java Web
    * Start.
    */
   public File get_output_file()
      {
      return output_file;
      }

   public File get_input_file()
      {
      return input_file;
      }

   public String get_parent()
      {
      if (input_file != null)
         {
         return input_file.getParent();
         }
      return null;
      }

   public File get_parent_file()
      {
      if (input_file != null)
         {
         return input_file.getParentFile();
         }
      return null;
      }

   public boolean is_created_from_text_file()
      {
      return input_file != output_file;
      }

   }
