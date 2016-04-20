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
 * InteractiveActionThread.java
 *
 * Created on 2. Maerz 2006, 07:23
 *
 */
package interactive;

import datastructures.ThreadStoppable;




/**
 * Used for running an action in a separate Thread, that can be stopped by the user.
 *
 * @author Alfons Wirtz
 */
public abstract class BrdActionThread extends Thread implements ThreadStoppable
   {
   private boolean stop_requested = false;

   public final IteraBoard hdlg;

   protected BrdActionThread(IteraBoard p_handling, String task_name)
      {
      hdlg = p_handling;
      super.setName(task_name);
      }

   protected abstract void thread_action();

   public void run()
      {
      hdlg.userPrintln("Thread START "+getName());
      thread_action();
      hdlg.repaint();
      hdlg.userPrintln("Thread END "+getName());
      }

   public void request_stop()
      {
      stop_requested = true;
      }

   public boolean is_stop_requested()
      {
      return stop_requested;
      }

   public synchronized void draw(java.awt.Graphics p_graphics)
      {
      // Can be overwritten in derived classes.
      }
   }
