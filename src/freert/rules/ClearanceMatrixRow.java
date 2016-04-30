package freert.rules;

import gui.varie.ObjectInfoPanel;
import board.infos.PrintableInfo;


/**
 * contains a row of entries of the clearance matrix
 */
public class ClearanceMatrixRow implements PrintableInfo, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final ClearanceMatrix cm_parent;

   final String name;

   int[] max_value;
   final ClearanceMatrixEntry[] column;
   
   public ClearanceMatrixRow(ClearanceMatrix p_parent, String p_name)
      {
      cm_parent = p_parent;
      
      name = p_name;
      
      column = new ClearanceMatrixEntry[cm_parent.get_class_count()];
      for (int i = 0; i < cm_parent.get_class_count(); ++i)
         {
         column[i] = new ClearanceMatrixEntry(cm_parent.get_layer_structure());
         }
      
      max_value = new int[cm_parent.get_layer_structure().size()];
      }

   public void print_info( ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("freert.board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("spacing_from_clearance_class") + " ");
      p_window.append_bold(name);
      for (int i = 1; i < column.length; ++i)
         {
         p_window.newline();
         p_window.indent();
         p_window.append(" " + resources.getString("to_class") + " ");
         p_window.append(cm_parent.get_row(i).name);
         ClearanceMatrixEntry curr_column = column[i];
         if (curr_column.is_layer_dependent())
            {
            p_window.append(" " + resources.getString("on_layer") + " ");
            for (int j = 0; j < cm_parent.get_layer_structure().size(); ++j)
               {
               p_window.newline();
               p_window.indent();
               p_window.indent();
               p_window.append(cm_parent.get_layer_structure().get_name(j));
               p_window.append(" = ");
               p_window.append(curr_column.layer[j]);
               }
            }
         else
            {
            p_window.append(" = ");
            p_window.append(curr_column.layer[0]);
            }
         }
      }

   }

