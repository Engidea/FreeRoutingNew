package gui.varie;

import interactive.SnapShot;
import javax.swing.DefaultListModel;

/**
 * Type for attributes of this class, which are saved to an Objectstream.
 */
public class SnapSavedAttributes implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public SnapSavedAttributes(DefaultListModel<SnapShot> p_list_model, int p_snapshot_count, java.awt.Point p_location, boolean p_is_visible)
      {
      list_model = p_list_model;
      snapshot_count = p_snapshot_count;
      location = p_location;
      is_visible = p_is_visible;

      }

   public final DefaultListModel<SnapShot> list_model;
   public final int snapshot_count;
   public final java.awt.Point location;
   public final boolean is_visible;
   }
