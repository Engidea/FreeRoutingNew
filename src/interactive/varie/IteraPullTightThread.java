package interactive.varie;

import interactive.BrdActionThread;
import interactive.IteraBoard;
import interactive.state.StateInteractive;
import interactive.state.StateSelectedItem;

public class IteraPullTightThread extends BrdActionThread
   {
   public IteraPullTightThread(IteraBoard p_board_handling)
      {
      super(p_board_handling,"IteraPullTightThread");
      }

   protected void thread_action()
      {
      if ( ! hdlg.is_StateSelectedItem() ) return;

      StateInteractive return_state = ((StateSelectedItem) hdlg.interactive_state).pull_tight(this);
      hdlg.set_interactive_state(return_state);
      }
   }
