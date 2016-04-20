package gui.varie;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class AutorouteParameterRow
   {
   public final int signal_layer_no;
   public final JLabel signal_layer_name;
   public final JComboBox<String> signal_layer_combo;
   public final JCheckBox signal_layer_active;
   
   public AutorouteParameterRow(int p_layer_no)
      {
      signal_layer_no = p_layer_no;
      signal_layer_name = new JLabel();
      signal_layer_active = new JCheckBox();
      signal_layer_combo = new JComboBox<String>();
      }
   
   public void setName ( String name )
      {
      signal_layer_name.setText(name);
      }
   }
