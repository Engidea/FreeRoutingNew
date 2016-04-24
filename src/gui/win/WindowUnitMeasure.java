package gui.win;

import freert.varie.UnitMeasure;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiPanelVertical;
import gui.varie.GuiResources;
import interactive.IteraBoard;
import java.awt.Insets;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

/**
 * It is far easier to handle a sub window than some strange bits in the main bar
 * Need to add some sort of feedback for changed values
 * @author damiano
 *
 */
public final class WindowUnitMeasure extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;
   private final IteraBoard board_handling;
   private JFormattedTextField unit_factor_field;
   private JComboBox <UnitMeasure>unit_combo_box;
   private boolean key_input_completed = true;

   private GuiResources resources;
   
   public WindowUnitMeasure(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      board_handling = p_board_frame.board_panel.board_handling;
      resources = new GuiResources(board_frame.stat, "gui.resources.WindowUnitParameter");
      
      setTitle(resources.getString("title"));

      GuiPanelVertical main_panel = new GuiPanelVertical(new Insets(3,3,3,3));

      NumberFormat number_format = NumberFormat.getInstance(p_board_frame.get_locale());
      number_format.setMaximumFractionDigits(7);
      unit_factor_field = new JFormattedTextField(number_format);
      unit_factor_field.setColumns(5);
      unit_factor_field.setValue(1);
      unit_factor_field.addKeyListener(new UnitFactorKeyListener());
      unit_factor_field.addFocusListener(new UnitFactorFocusListener());
      
      main_panel.add(newUnitFactor());

      unit_combo_box = new JComboBox<UnitMeasure>();
      unit_combo_box.setModel(new DefaultComboBoxModel<UnitMeasure>(UnitMeasure.values()));
      unit_combo_box.setFocusTraversalPolicyProvider(true);
      unit_combo_box.setInheritsPopupMenu(true);

      unit_combo_box.addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               freert.varie.UnitMeasure new_unit = (UnitMeasure) unit_combo_box.getSelectedItem();
               board_frame.board_panel.board_handling.change_user_unit(new_unit);
               board_frame.refresh_windows();
               }
         });

      main_panel.add(newUnitMeasure());

      add(main_panel.getJPanel());
      
      p_board_frame.set_context_sensitive_help(this, "WindowUnitParameter");

      refresh();
      pack();
      setLocationRelativeTo(null);
      }

   private JPanel newUnitFactor ()
      {
      JPanel risul = new JPanel();
      
      risul.add(resources.newJLabel("Unit_Factor"));
      risul.add(unit_factor_field);
      
      return risul;
      }
   

   private JPanel newUnitMeasure ()
      {
      JPanel risul = new JPanel();
      
      risul.add(resources.newJLabel("Unit_Measure"));
      risul.add(unit_combo_box);
      
      return risul;
      }
   
   private void unit_factor_field_set(double p_value)
      {
      if (p_value <= 0)
         {
         unit_factor_field.setValue(0);
         }
      else
         {
         double grid_width = board_handling.coordinate_transform.board_to_user(p_value);
         unit_factor_field.setValue(grid_width);
         }
      }

   
   @Override
   public void refresh()
      {
      unit_factor_field_set(board_handling.coordinate_transform.user_unit_factor);
      unit_combo_box_set(board_handling.coordinate_transform.user_unit);
      }
   
   private void unit_combo_box_set(UnitMeasure p_value)
      {
      if (p_value == null) return;

      unit_combo_box.setSelectedItem(p_value);
      }

   private class UnitFactorKeyListener extends java.awt.event.KeyAdapter
      {
      public void keyTyped(java.awt.event.KeyEvent p_evt)
         {
         if (p_evt.getKeyChar() == '\n')
            {
            Object input = unit_factor_field.getValue();
            if (input instanceof Number)
               {
               double input_value = ((Number) input).doubleValue();
               if (input_value > 0)
                  {
                  board_handling.change_user_unit_factor(input_value);
                  }
               }
            double unit_factor = board_handling.coordinate_transform.user_unit_factor;
            unit_factor_field.setValue(unit_factor);

            board_frame.refresh_windows();
            }
         }
      }

   private class UnitFactorFocusListener implements FocusListener
      {
      public void focusLost(java.awt.event.FocusEvent p_evt)
         {
         if (!key_input_completed)
            {
            // restore the text field.
            unit_factor_field_set(board_handling.coordinate_transform.user_unit_factor);
            key_input_completed = true;
            }
         }

      public void focusGained(java.awt.event.FocusEvent p_evt)
         {
         }
      }
   }
