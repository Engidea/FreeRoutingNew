package gui.varie;


/**
 * TODO merge this info into the BrdLayer
 * @author damiano
 *
 */
public class AutorouteParameterRow
   {
   public static final int PFDIR_horizontal=1;
   public static final int PFDIR_vertical=2;
   
   public final int signal_layer_no;

   public String signal_layer_name;
   public int signal_layer_pfdir;
   public boolean signal_layer_active;
   
   public AutorouteParameterRow(int p_layer_no)
      {
      signal_layer_no = p_layer_no;
      }
   
   public boolean isHorizontal ()
      {
      return signal_layer_pfdir == PFDIR_horizontal;
      }
   }
