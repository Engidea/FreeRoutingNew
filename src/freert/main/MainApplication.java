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
 * MainApplication.java
 *
 * Created on 19. Oktober 2002, 17:58
 *
 */
package freert.main;

import gui.BoardFrame;
import gui.varie.FileFilter;
import gui.varie.GuiResources;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import board.varie.DesignFile;

/**
 * Main application for creating frames with new or existing board designs.
 * @author Alfons Wirtz
 */
public class MainApplication extends JFrame
   {
   private static final long serialVersionUID = 1L;

   // Change this string when creating a new version
   public static final String VERSION_NUMBER_STRING = "1.2.46";
   
   private final MainApplicationListener listener = new MainApplicationListener();
   private final Stat stat = new Stat();

   private final GuiResources resources;
   private JButton open_board_button;
   private javax.swing.JPanel main_panel;

   private Collection<BoardFrame> board_frames = new LinkedList<BoardFrame>();

   private MainOptions main_options;
   
   /**
    * Main function of the Application
    */
   public static void main(String p_args[])
      {
      new MainApplication(p_args);
      }

   public MainApplication(String p_args[])
      {
      main_options = new MainOptions(p_args);

      Stat.instance = stat;

      decideUILookAndFeel(main_options);

      stat.log = new WindowEventsLog(true);

      resources = new GuiResources(stat,"gui.resources.MainApplication");
      
      setTitle(resources.getString("title"));
      
      ImageIcon icon = new ImageIcon (getClass().getResource("freert_icon.png"));
      setIconImage(icon.getImage());
      
      addWindowListener(new WindowStateListener());
      
      mainApplication();
      setVisible(true);

      stat.log.setVisible(false);
      }

   
   /**
    * NOrmally the look and feel should be the standard one, java, allow to set the systemplaf
    * to have the system look and feel
    */
   private void decideUILookAndFeel (MainOptions options)
     {
     try
       {
       boolean systemPlaf=false;
       
       systemPlaf = options.system_plaf;
       
       String systemPlafReq = System.getenv("systemplaf");
       if ( systemPlafReq != null && systemPlafReq.equalsIgnoreCase("true") )  systemPlaf = true;
       
       if ( systemPlaf )
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       }
     catch ( Exception exc )
       {
       exc.printStackTrace();
       }
     }
      
   
   
   /**
    * Creates new form MainApplication
    */
   private void mainApplication()
      {
      main_panel = new JPanel(new BorderLayout());
      getContentPane().add(main_panel);

      open_board_button = resources.newJButton("open_own_design", "open_own_design_tooltip", listener);

      main_panel.add(open_board_button,BorderLayout.NORTH);

      main_panel.add(stat.log.getComponentToDisplay(),BorderLayout.CENTER);
      
      pack();
      
      setLocationRelativeTo(null);
      }

   /** 
    * opens a board design from a binary file or a specctra dsn file
    * Need to detach from swing thread since messages are not printed... 
    */
   private void open_board_design_action()
      {
      DesignFile design_file = open_dialog(stat, main_options.design_dir_name);
      
      if (design_file == null)
         {
         stat.userPrintln(resources.getString("message_3"));
         return;
         }

      String message = resources.getString("loading_design") + " " + design_file.get_name();

      stat.userPrintln(message); 

      BoardFrame board_frame = new BoardFrame(design_file, stat);

      ImportDesign d_import = new ImportDesign(board_frame);
      
      d_import.execute();
      }

   /**
    * Shows a file chooser for opening a design file
    */
   private DesignFile open_dialog(Stat stat, String p_design_dir_name)
      {
      JFileChooser file_chooser = new JFileChooser(p_design_dir_name);
      FileFilter file_filter = new FileFilter(DesignFile.all_file_extensions);
      file_chooser.setFileFilter(file_filter);
      file_chooser.showOpenDialog(null);
      
      File curr_design_file = file_chooser.getSelectedFile();
      
      if (curr_design_file == null) return null;
      
      return new DesignFile(stat, curr_design_file, file_chooser);
      }
   

   private boolean open_board_file (BoardFrame b_frame)
      {
      InputStream input_stream = b_frame.design_file.get_input_stream();
      
      if (input_stream == null)
         {
         stat.userPrintln(resources.getString("message_8") + " " + b_frame.design_file.get_name());
         return false;
         }

      boolean read_ok;
      
      if ( b_frame.design_file.is_created_from_text_file() )
         read_ok = b_frame.import_design(input_stream);
      else
         read_ok = b_frame.open_design(input_stream);
      
      if (!read_ok) return false;
      
      if (b_frame.design_file.is_created_from_text_file())
         {
         // Read the file with the saved rules, if it is existing.
   
         String file_name = b_frame.design_file.get_name();
         String[] name_parts = file_name.split("\\.");
         String confirm_import_rules_message = resources.getString("confirm_import_rules");
         b_frame.design_file.read_rules_file(name_parts[0], b_frame.design_file.get_parent(), b_frame.board_panel.itera_board, confirm_import_rules_message);
         }
      
      return true;
      }
   
   private void open_board_file_gui (BoardFrame b_frame, boolean read_ok )
      {
      
      if ( read_ok )
         {
         b_frame.refresh_windows();
         stat.userPrintln(resources.getString("message_4") + " " + b_frame.design_file.get_name() + " " + resources.getString("message_5"));
  
         board_frames.add(b_frame);
         
         b_frame.get_JFrame().addWindowListener(new BoardFrameWindowListener(b_frame));
         }
      
      }
   
   
private class ImportDesign extends SwingWorker<Boolean, Integer>
   {
   private final BoardFrame b_frame;
   
   ImportDesign ( BoardFrame p_b_frame )
      {
      b_frame = p_b_frame;
      }
   
   protected Boolean doInBackground() throws Exception
      {
      return open_board_file ( b_frame );
      }

    protected void done()
       {
       try
          {
          open_board_file_gui ( b_frame, get());
          }
       catch ( Exception exc )
          {
          stat.log.exceptionShow("ImportDesign", exc);
          }
       }
    }
   
   
   private class BoardFrameWindowListener extends WindowAdapter
      {
      private BoardFrame board_frame;

      public BoardFrameWindowListener(BoardFrame p_board_frame)
         {
         board_frame = p_board_frame;
         }

      public void windowClosed(WindowEvent evt)
         {
         if (board_frame != null)
            {
            // remove this board_frame from the list of board frames
            board_frame.dispose();
            board_frames.remove(board_frame);
            board_frame = null;
            }
         }
      }

   private class WindowStateListener extends java.awt.event.WindowAdapter
      {
      private boolean wantExit ()
         {
         if ( board_frames.size() <= 0 ) return true;
         
         int option = javax.swing.JOptionPane.showConfirmDialog(null, resources.getString("confirm_cancel"), null, javax.swing.JOptionPane.YES_NO_OPTION);
         
         if (option == javax.swing.JOptionPane.NO_OPTION) return false;
         
         return true;
         }

      public void windowClosing(java.awt.event.WindowEvent evt)
         {
         if ( wantExit())
            {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            // it should be possible to exit by simply closing all windwso 
            System.exit(0);
            }
         else
            {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            }
         }

      }
   
   private class MainApplicationListener implements ActionListener
      {
      @Override
      public void actionPerformed(ActionEvent event)
         {
         Object source = event.getSource();
         
         if ( source == open_board_button )
            open_board_design_action();
         }
      }
   
   }