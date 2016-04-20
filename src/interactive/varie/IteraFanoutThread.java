package interactive.varie;

import interactive.BrdActionThread;
import interactive.IteraBoard;
import interactive.state.StateInteractive;
import interactive.state.StateSelectedItem;

public class IteraFanoutThread extends BrdActionThread
   {
   public IteraFanoutThread(IteraBoard p_board_handling)
      {
      super(p_board_handling,"IteraFanoutThread");
      }

   protected void thread_action()
      {
      if ( ! hdlg.is_StateSelectedItem() ) return;

      StateInteractive return_state = ((StateSelectedItem) hdlg.interactive_state).fanout(this);
      hdlg.set_interactive_state(return_state);
      }
   }
