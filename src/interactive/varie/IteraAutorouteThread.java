package interactive.varie;

import interactive.BrdActionThread;
import interactive.IteraBoard;
import interactive.state.StateInteractive;
import interactive.state.StateSelectedItem;

public class IteraAutorouteThread extends BrdActionThread
   {
   public IteraAutorouteThread(IteraBoard p_board_handling)
      {
      super(p_board_handling,"IteraAutorouteThread");
      }

   @Override
   protected void thread_action()
      {
      if ( ! hdlg.is_StateSelectedItem() ) return;

      StateInteractive return_state = ((StateSelectedItem) hdlg.interactive_state).autoroute(this);

      hdlg.set_interactive_state(return_state);
      }
   }
