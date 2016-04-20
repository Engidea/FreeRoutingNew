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
 * BoardFileMenu.java
 *
 * Created on 11. Februar 2005, 11:26
 */
package gui.menu;

import gui.BoardFrame;
import gui.config.GuiConfigFile;
import gui.varie.GuiResources;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import main.Stat;

/**
 * Creates the file menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public final class BoardMenuFile extends JMenu
   {
   private static final long serialVersionUID = 1L;
   private static final String classname = "BoardMenuFile.";

   private final BoardMenuFileListener listener = new BoardMenuFileListener();

   private final Stat stat;
   private final BoardFrame board_frame;
   private final GuiResources resources;

   private JMenuItem save_item;
   private JMenuItem cancel_and_exit_item;
   private JMenuItem save_as_item;
   private JMenuItem save_settings_item;
   private JMenuItem write_spectra_file_item;
   private JMenuItem write_eagle_script_item;

   public BoardMenuFile(Stat stat, BoardFrame p_board_frame )
      {
      this.stat = stat;

      board_frame = p_board_frame;

      resources = new GuiResources(stat, "gui.resources.BoardMenuFile");

      setText(resources.getString("file"));

      save_item = resources.newJMenuItem("save", "save_tooltip", listener);
      add(save_item);

      cancel_and_exit_item = resources.newJMenuItem("cancel_and_exit", "cancel_and_exit_tooltip", listener);
      add(cancel_and_exit_item);

      save_as_item = resources.newJMenuItem("save_as", "save_as_tooltip", listener);
      add(save_as_item);

      save_settings_item = resources.newJMenuItem("settings", "settings_tooltip", listener);
      add(save_settings_item);
      }

   public void add_design_dependent_items()
      {
      write_spectra_file_item = resources.newJMenuItem("session_file", "session_file_tooltip", listener);

      this.add(write_spectra_file_item);

      write_eagle_script_item = resources.newJMenuItem("eagle_script", "eagle_script_tooltip", listener);

      add(write_eagle_script_item);
      }

   private void save_as_action()
      {
      if (this.board_frame.design_file != null)
         {
         this.board_frame.design_file.save_as_dialog(this, this.board_frame);
         }
      }

   @SuppressWarnings("unused")
   private void write_logfile_action()
      {
      JFileChooser file_chooser = new JFileChooser();
      java.io.File logfile_dir = board_frame.design_file.get_parent_file();
      file_chooser.setCurrentDirectory(logfile_dir);
      file_chooser.setFileFilter(BoardFrame.logfile_filter);
      file_chooser.showOpenDialog(this);
      java.io.File filename = file_chooser.getSelectedFile();
      if (filename == null)
         {
         board_frame.screen_messages.set_status_message(resources.getString("message_8"));
         }
      else
         {
         board_frame.screen_messages.set_status_message(resources.getString("message_9"));
         board_frame.board_panel.board_handling.start_logfile(filename);
         }
      }

   @SuppressWarnings("unused")
   private void read_logfile_action()
      {
      JFileChooser file_chooser = new JFileChooser();
      java.io.File logfile_dir = board_frame.design_file.get_parent_file();
      file_chooser.setCurrentDirectory(logfile_dir);
      file_chooser.setFileFilter(BoardFrame.logfile_filter);
      file_chooser.showOpenDialog(this);
      java.io.File filename = file_chooser.getSelectedFile();
      if (filename == null)
         {
         board_frame.screen_messages.set_status_message(resources.getString("message_10"));
         }
      else
         {
         java.io.InputStream input_stream = null;
         try
            {
            input_stream = new java.io.FileInputStream(filename);
            }
         catch (java.io.FileNotFoundException e)
            {
            return;
            }
         board_frame.read_logfile(input_stream);
         }
      }

   private void save_defaults_action()
      {
      java.io.File config_file = new java.io.File(board_frame.design_file.get_parent(), BoardFrame.GUI_DEFAULTS_FILE_NAME);

      if (config_file.exists())
         {
         // Make a backup copy of the old defaults file.
         java.io.File defaults_file_backup = new java.io.File(board_frame.design_file.get_parent(), BoardFrame.GUI_DEFAULTS_FILE_BACKUP_NAME);
         if (defaults_file_backup.exists())
            {
            defaults_file_backup.delete();
            }
         config_file.renameTo(defaults_file_backup);
         }
      
      stat.log.userPrintln("try save config "+config_file);
      
      try
         {
         OutputStream output_stream = new FileOutputStream(config_file);
         GuiConfigFile.write(board_frame, board_frame.board_panel.board_handling, output_stream);
         board_frame.screen_messages.set_status_message(resources.getString("message_17"));
         }
      catch (Exception exc)
         {
         stat.log.exceptionPrint(classname+"save_defaults_action", exc);
         board_frame.screen_messages.set_status_message(resources.getString("message_18"));
         }
      }

   private void save_item_fun()
      {
      boolean save_ok = board_frame.save();
      board_frame.board_panel.board_handling.close_files();
      if (save_ok)
         {
         board_frame.screen_messages.set_status_message(resources.getString("save_message"));
         }
      }

   private class BoardMenuFileListener implements ActionListener
      {
      @Override
      public void actionPerformed(ActionEvent event)
         {
         Object source = event.getSource();

         if (source == save_item)
            save_item_fun();
         else if (source == cancel_and_exit_item)
            board_frame.dispose();
         else if (source == save_as_item)
            save_as_action();
         else if (source == save_settings_item)
            save_defaults_action();
         else if (source == write_spectra_file_item)
            board_frame.design_file.write_specctra_ses_file(board_frame);
         else if (source == write_eagle_script_item)
            board_frame.design_file.update_eagle(board_frame);

         }

      }

   }
