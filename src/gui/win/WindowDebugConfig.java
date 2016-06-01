package gui.win;

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

import freert.main.Ldbg;
import freert.main.Mdbg;
import freert.main.Stat;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiPanelVertical;
import gui.varie.GuiResources;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public final class WindowDebugConfig extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="WindowDebugConfig.";
   
   private final PrivateActionListener actionListener = new PrivateActionListener();
   private final ArrayList<CheckboxAndBitmask> mask_list = new ArrayList<CheckboxAndBitmask>(32); 
   private final ArrayList<CheckboxAndBitmask> level_list = new ArrayList<CheckboxAndBitmask>(32); 
   
   private final Stat stat;
   
   public WindowDebugConfig(Stat p_stat, BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      stat = p_stat;

      GuiResources resources = new GuiResources(board_frame.stat, "gui.resources.WindowUnitParameter");
      
      setTitle(resources.getString("title"));

      JPanel main_panel = new JPanel();
      
      main_panel.add(newMaskSelect(resources));

      main_panel.add(newLevelSelect(resources));

      add(main_panel);
      
      p_board_frame.set_context_sensitive_help(this, "WindowUnitParameter");

      refresh();
      pack();
      setLocationRelativeTo(null);
      }

   private JCheckBox newCheckbox (GuiResources resources, ArrayList<CheckboxAndBitmask> alist, String name, int bitmask )
      {
      JCheckBox acheck = resources.newJCheckBox(name,null,actionListener);
      CheckboxAndBitmask anitem = new CheckboxAndBitmask(acheck, bitmask);
      alist.add(anitem);
      return acheck;
      }
   
   private JPanel newMaskSelect (GuiResources resources)
      {
      GuiPanelVertical risul = new GuiPanelVertical(new Insets(3,3,3,3));
      
      risul.setBorder(resources.newTitledBorder("Mask"));
      
      risul.add(newCheckbox(resources, mask_list, "Gui", Mdbg.GUI));
      risul.add(newCheckbox(resources, mask_list, "Planar delaunay", Mdbg.PLADELTRI));
      risul.add(newCheckbox(resources, mask_list, "Maze", Mdbg.MAZE));
      risul.add(newCheckbox(resources, mask_list, "Push", Mdbg.PUSH));
      risul.add(newCheckbox(resources, mask_list, "Push Trace", Mdbg.PUSH_TRACE));
      risul.add(newCheckbox(resources, mask_list, "Clearance Violation", Mdbg.CLRVIOL));
      
      return risul.getJPanel();
      }
   

   private JPanel newLevelSelect (GuiResources resources)
      {
      GuiPanelVertical risul = new GuiPanelVertical(new Insets(3,3,3,3));

      risul.setBorder(resources.newTitledBorder("Level"));
 
      risul.add(newCheckbox(resources, level_list, "Fine", Ldbg.FINE));
      risul.add(newCheckbox(resources, level_list, "Trace", Ldbg.TRACE));
      risul.add(newCheckbox(resources, level_list, "Debug", Ldbg.DEBUG));
      risul.add(newCheckbox(resources, level_list, "Notice", Ldbg.NOTICE));
      risul.add(newCheckbox(resources, level_list, "Warning", Ldbg.WARNING));
      risul.add(newCheckbox(resources, level_list, "Error", Ldbg.ERROR));
      risul.add(newCheckbox(resources, level_list, "SPC_A", Ldbg.SPC_A));
      
      return risul.getJPanel();
      }
   
   private void refresh_checkboxes (ArrayList<CheckboxAndBitmask> alist, int mask )
      {
      for ( CheckboxAndBitmask arow : alist )
         {
         arow.checkbox.setSelected( (arow.bitmask & mask) != 0 );
         }
      }

   
   @Override
   public void refresh()
      {
      refresh_checkboxes (mask_list, stat.debug_mask );
      refresh_checkboxes (level_list, stat.debug_level );
      }

   /**
    * Scan all action checkboxes list and see if one match
    * if it does it will handle the toggling and return true
    * @param src
    * @return true if the given source matches in any of the mask handlers
    */
   private boolean mask_handled ( Object src )
      {
      for ( CheckboxAndBitmask arow : mask_list )
         {
         if ( arow.checkbox != src ) continue;
         
         if ( (stat.debug_mask & arow.bitmask) != 0 )
            stat.debug_mask &= ~arow.bitmask;
         else
            stat.debug_mask |= arow.bitmask;
         
         return true;
         }
      
      return false;
      }
   
   /**
    * Scan all action checkboxes list and see if one match
    * if it does it will handle the toggling
    * @param src
    * @return true if the given source matches in any of the mask handlers
    */
   private boolean level_handled ( Object src )
      {
      for ( CheckboxAndBitmask arow : level_list )
         {
         if ( arow.checkbox != src ) continue;
         
         if ( (stat.debug_level & arow.bitmask) != 0 )
            stat.debug_level &= ~arow.bitmask;
         else
            stat.debug_level |= arow.bitmask;
         
         return true;
         }
      
      return false;
      }

   
   private class PrivateActionListener implements ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         Object src = p_evt.getSource();
         
         if ( mask_handled(src)) return;
         
         if ( level_handled(src)) return;
         
         stat.userPrintln(classname+"error no matching for checkbox");
         }
      }
   
   /**
    * Ahhhhh, cannot believe I am having a static class here :-)
    * @author damiano
    */
   private static class CheckboxAndBitmask
      {
      final JCheckBox checkbox;
      final int bitmask;

      CheckboxAndBitmask (JCheckBox p_checkbox, int p_bitmask )
         {
         checkbox = p_checkbox;
         bitmask = p_bitmask;
         }
      }
   }
