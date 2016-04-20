package interactive.varie;

import interactive.BrdActionThread;
import interactive.IteraBoard;
import interactive.LogfileScope;
import interactive.state.StateInteractive;

public class ReadActlogThread extends BrdActionThread
   {
   private final java.io.InputStream input_stream;
   
   public ReadActlogThread(IteraBoard p_board_handling, java.io.InputStream p_input_stream)
      {
      super(p_board_handling,"ReadActlogThread");
      
      input_stream = p_input_stream;
      }

   protected void thread_action()
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("interactive.resources.InteractiveState", hdlg.get_locale());
      boolean saved_board_read_only = hdlg.is_board_read_only();
      hdlg.set_board_read_only(true);
      String start_message = resources.getString("logfile") + " " + resources.getString("stop_message");
      hdlg.screen_messages.set_status_message(start_message);
      hdlg.screen_messages.set_write_protected(true);
      boolean done = false;
      StateInteractive previous_state = hdlg.interactive_state;

      if (!hdlg.actlog.start_read(input_stream))
         {
         done = true;
         }
      
      boolean interrupted = false;
      
      @SuppressWarnings("unused")
      int debug_counter = 0;
      hdlg.get_panel().board_frame.refresh_windows();
      hdlg.paint_immediately = true;
      while (!done)
         {
         if (is_stop_requested())
            {
            interrupted = true;
            done = true;
            }
         ++debug_counter;
         LogfileScope logfile_scope = hdlg.actlog.start_read_scope();

         if (logfile_scope == null)
            {
            done = true; // end of logfile
            }
         
         if (!done)
            {
            try
               {
               StateInteractive new_state = logfile_scope.read_scope(hdlg.actlog, hdlg.interactive_state, hdlg);
               if (new_state == null)
                  {
                  System.out.println("BoardHandling:read_logfile: inconsistent logfile scope");
                  new_state = previous_state;
                  }
               hdlg.repaint();
               hdlg.set_interactive_state(new_state);
               }
            catch (Exception e)
               {
               done = true;
               }

            }
         }
      hdlg.paint_immediately = false;
      try
         {
         input_stream.close();
         }
      catch (java.io.IOException e)
         {
         System.out.println("ReadLogfileThread: unable to close input stream");
         }
      hdlg.get_panel().board_frame.refresh_windows();
      hdlg.screen_messages.set_write_protected(false);
      String curr_message;
      if (interrupted)
         {
         curr_message = resources.getString("interrupted");
         }
      else
         {
         curr_message = resources.getString("completed");
         }
      String end_message = resources.getString("logfile") + " " + curr_message;
      hdlg.screen_messages.set_status_message(end_message);
      hdlg.set_board_read_only(saved_board_read_only);
      hdlg.get_panel().board_frame.repaint_all();
      }
   }
