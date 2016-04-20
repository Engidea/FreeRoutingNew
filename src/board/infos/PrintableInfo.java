package board.infos;

import gui.varie.ObjectInfoPanel;


/**
 * Functionality needed for objects to print information into an ObjectInfoWindow
 */
public interface PrintableInfo
   {
   /**
    * Prints information about an ObjectInfoWindow.Printable object into the input window.
    */
   void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale);
   }
