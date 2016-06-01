package freert.rules;

import gui.BoardFrame;
import gui.varie.GuiResources;
import board.RoutingBoard;
import board.infos.BrdViaInfo;

/**
 * Table model of the via table.
 */
public class ViaTableModel extends javax.swing.table.AbstractTableModel
   {
   private static final long serialVersionUID = 1L;

   private final BoardFrame board_frame;
   
   public ViaTableModel(BoardFrame p_board_frame, GuiResources resources)
      {
      board_frame = p_board_frame;
      
      column_names = new String[ViaTableColumnName.values().length];

      for (int i = 0; i < column_names.length; ++i)
         {
         column_names[i] = resources.getString((ViaTableColumnName.values()[i]).toString());
         }
      freert.rules.BoardRules board_rules = board_frame.board_panel.itera_board.get_routing_board().brd_rules;
      data = new Object[board_rules.via_infos.count()][];
      for (int i = 0; i < data.length; ++i)
         {
         data[i] = new Object[ViaTableColumnName.values().length];
         }
      set_values();
      }

   /** 
    * Calculates the the valus in this table 
    */
   public void set_values()
      {
      BoardRules board_rules = board_frame.board_panel.itera_board.get_routing_board().brd_rules;
      
      for (int i = 0; i < data.length; ++i)
         {
         BrdViaInfo curr_via = board_rules.via_infos.get(i);
         data[i][ViaTableColumnName.NAME.ordinal()] = curr_via.get_name();
         data[i][ViaTableColumnName.PADSTACK.ordinal()] = curr_via.get_padstack().pads_name;
         data[i][ViaTableColumnName.CLEARANCE_CLASS.ordinal()] = board_rules.clearance_matrix.get_name(curr_via.get_clearance_class());
         data[i][ViaTableColumnName.ATTACH_SMD.ordinal()] = curr_via.attach_smd_allowed();
         }
      }

   public String getColumnName(int p_col)
      {
      return column_names[p_col];
      }

   public int getRowCount()
      {
      return data.length;
      }

   public int getColumnCount()
      {
      return column_names.length;
      }

   public Object getValueAt(int p_row, int p_col)
      {
      return data[p_row][p_col];
      }

   public void setValueAt(Object p_value, int p_row, int p_col)
      {
      RoutingBoard routing_board = board_frame.board_panel.itera_board.get_routing_board();
      BoardRules board_rules = routing_board.brd_rules;
      Object via_name = getValueAt(p_row, ViaTableColumnName.NAME.ordinal());
      if (!(via_name instanceof String))
         {
         System.out.println("ViaVindow.setValueAt: String expected");
         return;
         }
      BrdViaInfo via_info = board_rules.via_infos.get((String) via_name);
      if (via_info == null)
         {
         System.out.println("ViaVindow.setValueAt: via_info not found");
         return;
         }

      if (p_col == ViaTableColumnName.NAME.ordinal())
         {
         if (!(p_value instanceof String))
            {
            return;
            }
         String new_name = (String) p_value;
         if (board_rules.via_infos.name_exists(new_name))
            {
            return;
            }
         via_info.set_name(new_name);
         board_frame.via_window.refresh();
         }
      else if (p_col == ViaTableColumnName.PADSTACK.ordinal())
         {
         if (!(p_value instanceof String))
            {
            return;
            }
         String new_name = (String) p_value;
         freert.library.LibPadstack new_padstack = routing_board.brd_library.get_via_padstack(new_name);
         if (new_padstack == null)
            {
            System.out.println("ViaVindow.setValueAt: via padstack not found");
            return;
            }
         via_info.set_padstack(new_padstack);
         }
      else if (p_col == ViaTableColumnName.CLEARANCE_CLASS.ordinal())
         {
         if (!(p_value instanceof String))
            {
            return;
            }
         String new_name = (String) p_value;
         int new_cl_class_index = board_rules.clearance_matrix.get_no(new_name);
            {
            if (new_cl_class_index < 0)
               {
               System.out.println("ViaVindow.setValueAt: clearance class not found");
               return;
               }
            }
         via_info.set_clearance_class(new_cl_class_index);
         }
      else if (p_col == ViaTableColumnName.ATTACH_SMD.ordinal())
         {
         if (!(p_value instanceof Boolean))
            {
            System.out.println("ViaVindow.setValueAt: Boolean expected");
            return;
            }
         Boolean attach_smd = (Boolean) p_value;
         via_info.set_attach_smd_allowed(attach_smd);
         }
      data[p_row][p_col] = p_value;
      fireTableCellUpdated(p_row, p_col);
      }

   public boolean isCellEditable(int p_row, int p_col)
      {
      return true;
      }

   public Class<?> getColumnClass(int p_col)
      {
      return getValueAt(0, p_col).getClass();
      }

   private Object[][] data = null;
   private String[] column_names = null;
   }