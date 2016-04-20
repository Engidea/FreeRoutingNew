package gui.menu;

import gui.BoardFrame;
import gui.BoardPanel;
import gui.varie.GuiResources;
import javax.swing.JMenuItem;

public final class PopupLayerMenuItem extends JMenuItem
   {
   private static final long serialVersionUID = 1L;
   
   private final BoardFrame board_frame;
   private final int layer_no;
   private final String message1;
   
   PopupLayerMenuItem(BoardFrame p_board_frame, int p_layer_no)
      {
      board_frame = p_board_frame;
      
      GuiResources resources = board_frame.newGuiResources("gui.resources.Default");
      message1 = resources.getString("layer_changed_to") + " ";
      layer_no = p_layer_no;
      addActionListener(new java.awt.event.ActionListener()
         {
            public void actionPerformed(java.awt.event.ActionEvent evt)
               {
               final BoardPanel board_panel = board_frame.board_panel;
               if (board_panel.board_handling.change_layer_action(layer_no))
                  {
                  String layer_name = board_panel.board_handling.get_routing_board().layer_structure.get_name(layer_no);
                  board_panel.screen_messages.set_status_message(message1 + layer_name);
                  }
               // If change_layer failed the status message is set inside
               // change_layer_action
               // because the information of the cause of the failing is
               // missing here.
               board_panel.move_mouse(board_panel.right_button_click_location);
               }
         });
      }

   }
