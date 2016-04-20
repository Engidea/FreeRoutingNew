package gui.varie;

/**
 * Information to be stored in a SnapShot.
 */
public final class SnapSelection implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String filter;
   public final int[] selected_indices;

   public SnapSelection(String p_filter, int[] p_selected_indices)
      {
      filter = p_filter;
      selected_indices = p_selected_indices;
      }

   }
