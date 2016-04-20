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
 * Created on 20. Juni 2005, 08:02
 *
 */

package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import javax.swing.JFrame;


/**
 * Sub windows of the board frame.
 *
 * @author Alfons Wirtz
 */
public class GuiSubWindow 
   {
   private boolean visible_before_iconifying = false;
   private final JFrame work_frame = new JFrame();

   public void parent_iconified()
      {
      visible_before_iconifying = isVisible();
      setVisible(false);
      }
   
   public final void setPreferredSize(  Dimension adim )
      {
      work_frame.setPreferredSize(adim);
      }

   public final void setLocationRelativeTo(Component param)
      {
      work_frame.setLocationRelativeTo(param);
      }

   public final void setLocation ( int x, int y)
      {
      work_frame.setLocation(x, y);
      }
      
   public final void setLocation ( Point apoint )
      {
      work_frame.setLocation(apoint);
      }

   
   public final void repaint ()
      {
      work_frame.repaint();
      }
   
   public final void pack ()
      {
      work_frame.pack();
      }

   public final Dimension getSize()
      {
      return work_frame.getSize();
      }

   public final Point getLocation ()
      {
      return work_frame.getLocation();
      }

   public void dispose ()
      {
      work_frame.dispose();
      }

   public final void addWindowListener( WindowAdapter listener )
      {
      work_frame.addWindowListener(listener);
      }
      
   public final JFrame getJFrame ()
      {
      return work_frame;
      }

   public final void add ( Component component )
      {
      work_frame.add(component);
      }
   
   public final void add ( Component component, String params )
      {
      work_frame.add(component, params);
      }

   public final void setDefaultCloseOperation ( int operation )
      {
      work_frame.setDefaultCloseOperation(operation);
      }

   public final void setTitle ( String name )
      {
      work_frame.setTitle(name);
      }

   public final void setBounds (Rectangle bounds)
      {
      work_frame.setBounds(bounds);
      }
   
   public final Rectangle getBounds ()
      {
      return work_frame.getBounds();
      }

   public void setVisible(boolean visible)
      {
      work_frame.setVisible(visible);
      }

   public final boolean isVisible()
      {
      return work_frame.isVisible();
      }
   
   public void parent_deiconified()
      {
      setVisible(visible_before_iconifying);
      }
   }
