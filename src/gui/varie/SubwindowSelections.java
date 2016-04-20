package gui.varie;


/**
 * Used for storing the subwindow filters in a snapshot.
 */
public class SubwindowSelections implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public SnapSelection incompletes_selection;
   public SnapSelection packages_selection;
   public SnapSelection nets_selection;
   public SnapSelection components_selection;
   public SnapSelection padstacks_selection;
   }
