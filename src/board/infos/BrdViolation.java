package board.infos;

import board.items.BrdAbitPin;
import board.items.BrdAbitVia;
import board.items.BrdArea;
import board.items.BrdAreaConduction;
import board.items.BrdAreaObstacleComp;
import board.items.BrdAreaObstacleVia;
import board.items.BrdItem;
import board.items.BrdOutline;
import board.items.BrdTracep;
import freert.planar.PlaPointFloat;
import gui.BoardFrame;
import gui.varie.GuiResources;

public final class BrdViolation implements Comparable<BrdViolation>, PrintableInfo
   {
   public final BrdItemViolation violation;
   public final PlaPointFloat location;
   
   private final BoardFrame board_frame;
   private final GuiResources resources;

   public BrdViolation(BoardFrame p_board_frame, GuiResources p_resources, BrdItemViolation p_violation)
      {
      board_frame = p_board_frame;
      violation = p_violation;
      resources = p_resources;

      PlaPointFloat board_location = p_violation.shape.centre_of_gravity();

      location = board_frame.board_panel.itera_board.coordinate_transform.board_to_user(board_location);
      }

   private String get_item_name(BrdItem p_item)
      {
      String result;
      if (p_item instanceof BrdAbitPin)
         {
         result = resources.getString("pin");
         }
      else if (p_item instanceof BrdAbitVia)
         {
         result = resources.getString("via");
         }
      else if (p_item instanceof BrdTracep)
         {
         result = resources.getString("trace");
         }
      else if (p_item instanceof BrdAreaConduction)
         {
         result = resources.getString("conduction_area");
         }
      else if (p_item instanceof BrdArea)
         {
         result = resources.getString("keepout");
         }
      else if (p_item instanceof BrdAreaObstacleVia)
         {
         result = resources.getString("via_keepout");
         }
      else if (p_item instanceof BrdAreaObstacleComp)
         {
         result = resources.getString("component_keepout");
         }
      else if (p_item instanceof BrdOutline)
         {
         result = resources.getString("board_outline");
         }
      else
         {
         result = resources.getString("unknown");
         }
      return result;
      }

   public String toString()
      {
      board.BrdLayerStructure layer_structure = board_frame.board_panel.itera_board.get_routing_board().layer_structure;
      String result = get_item_name(violation.first_item) + " - " + get_item_name(violation.second_item) + " " + resources.getString("at") + " " + location.to_string(board_frame.get_locale()) + " "
            + resources.getString("on_layer") + " " + layer_structure.get_name(violation.layer_no);
      return result;
      }

   public void print_info(gui.varie.ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      violation.print_info(p_window, p_locale);
      }

   public int compareTo(BrdViolation p_other)
      {
      if (location.v_x > p_other.location.v_x)  return 1;

      if (location.v_x < p_other.location.v_x)  return -1;

      if (location.v_y > p_other.location.v_y)  return 1;

      if (location.v_y < p_other.location.v_y)  return -1;

      return violation.layer_no - p_other.violation.layer_no;
      }

   }