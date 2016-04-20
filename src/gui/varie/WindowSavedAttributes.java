package gui.varie;

import java.awt.Rectangle;

/**
 * Type for attributes of this class, which are saved to an Objectstream.
 */
public class WindowSavedAttributes implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final Rectangle bounds;
   public final boolean is_visible;
   
   public WindowSavedAttributes(java.awt.Rectangle p_bounds, boolean p_is_visible)
      {
      bounds = p_bounds;
      is_visible = p_is_visible;
      }
   }
