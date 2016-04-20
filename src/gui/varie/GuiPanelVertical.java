package gui.varie;

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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Quite often there is a need to have a bunch of stuff in vertical, in a nice way
 * @author damiano
 *
 */
public final class GuiPanelVertical
   {
   private final JPanel work_panel;
   
   private final GridBagLayout gbl;
   private final GridBagConstraints gbc;
   
   public GuiPanelVertical( )
      {
      this(new Insets(2,2,2,2));
      }

   public GuiPanelVertical(Insets gbc_insets )
      {
      work_panel = new JPanel();
      gbl = new GridBagLayout();
      gbc = new GridBagConstraints();
      
      work_panel.setLayout(gbl);
      
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = gbc_insets;
      gbc.gridx = 0;
      gbc.gridy = GridBagConstraints.RELATIVE;
      }

   public void add ( Component component )
      {
      if ( component == null ) return;
      work_panel.add(component,gbc);
      }
   
   public JPanel getJPanel ()
      {
      return work_panel;
      }
         
   public void setBorder ( Border border )
      {
      work_panel.setBorder(border);
      }
   
   public void setToolTipText ( String text )
      {
      work_panel.setToolTipText(text);
      }
   }
