package gui.varie;

/**
 * Layers of the board layer structure plus layer "all". 
 * Index is the layer number in the board layer structure or -1 for layer "all".
 */
public class GuiLayer
   {
   public final String name;

   // The index in the board layer_structure, -1 for the layers with name "all" or "inner"
   public final int index;

   public GuiLayer(String p_name, int p_index)
      {
      name = p_name;
      index = p_index;
      }

   public String toString()
      {
      return name;
      }
   }
