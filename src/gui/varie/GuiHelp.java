package gui.varie;

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
 * BoardMenuHelp.java
 *
 * Created on 19. Oktober 2005, 08:15
 *
 */

import java.awt.Component;
import java.net.URL;
import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import freert.main.Stat;

/**
 *
 * @author Alfons Wirtz
 */
public final class GuiHelp
   {
   private javax.help.HelpSet help_set = null;
   private javax.help.HelpBroker help_broker = null;
   
   private CSH.DisplayHelpFromSource contents_help = null;
   private CSH.DisplayHelpAfterTracking direct_help = null;

   private GuiResources resources;
   
   public GuiHelp(Stat p_stat)
      {
      resources = new GuiResources(p_stat,"gui.resources.BoardMenuHelp");
      
      String language = p_stat.locale.getLanguage();

      // default value if no other match
      String helpset_name = "helpset/en/Help.hs";
      
      if (language.equalsIgnoreCase("de"))
         {
         helpset_name = "helpset/de/Help.hs";
         }

      try
         {
         URL hsURL = HelpSet.findHelpSet(getClass().getClassLoader(), helpset_name);
         if (hsURL == null)
            {
            System.out.println("HelpSet " + helpset_name + " not found.");
            }
         else
            {
            help_set = new HelpSet(null, hsURL);
            }
         }
      catch (HelpSetException ee)
         {
         System.out.println("HelpSet " + helpset_name + " could not be opened.");
         System.out.println(ee.getMessage());
         }

      help_broker = help_set.createHelpBroker();
      
      // CSH.DisplayHelpFromSource is a convenience class to display the helpset
      contents_help = new CSH.DisplayHelpFromSource(help_broker);
      direct_help = new CSH.DisplayHelpAfterTracking(help_broker);
      }

   
   public void add_menu_items ( JMenu to_menu )
      {
      JMenuItem direct_help_item = resources.newJMenuItem("direct_help",null,direct_help);

      to_menu.add(direct_help_item);
      
      JMenuItem contents_item = resources.newJMenuItem("contents",null,contents_help);

      to_menu.add(contents_item);
      }
   
   
   /**
    * Sets contexts sensitive help for the input component
    * TODO enable this feature again
    */
   public void set_context_sensitive_help(Component p_component, String p_help_id)
      {
      Component curr_component;

      if (p_component instanceof JFrame)
         {
         curr_component = ((JFrame) p_component).getRootPane();
         }
      else
         {
         curr_component = p_component;
         }
      
      
      String help_id = "html_files." + p_help_id;
      
      javax.help.CSH.setHelpIDString(curr_component, help_id);
      
      help_broker.enableHelpKey(curr_component, help_id, help_set);
      }
   
   }
