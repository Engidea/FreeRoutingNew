package gui.varie;

/**
 * Contains the name of a clearance class and its index in the clearance matrix.
 */
public class GuiClearanceClass
   {
   public GuiClearanceClass(String p_name, int p_index)
      {
      this.name = p_name;
      this.index = p_index;
      }

   public String toString()
      {
      return name;
      }

   public final String name;
   public final int index;
   }
