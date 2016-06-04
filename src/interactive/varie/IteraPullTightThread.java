package interactive.varie;

/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

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
