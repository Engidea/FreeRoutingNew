package board.printable;

import planar.PlaPointFloat;

public final class PrintableRectangle extends PrintableShape
   {
   public final PlaPointFloat lower_left;
   public final PlaPointFloat upper_right;

   public PrintableRectangle(PlaPointFloat p_lower_left, PlaPointFloat p_upper_right, java.util.Locale p_locale)
      {
      super(p_locale);
      lower_left = p_lower_left;
      upper_right = p_upper_right;
      }

   @Override
   public String toString()
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", this.locale);
      String result = resources.getString("rectangle") + ": " + resources.getString("lower_left") + " = " + lower_left.to_string(this.locale) + ", " + resources.getString("upper_right") + " = "
            + upper_right.to_string(this.locale);
      return result;
      }

   }
