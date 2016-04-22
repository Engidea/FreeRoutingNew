package board.printable;

import freert.planar.PlaPointFloat;

public final class PrintableCircle extends PrintableShape
   {
   public final PlaPointFloat center;
   public final double radius;
   
   /**
    * Creates a Circle from the input coordinates.
    */
   public PrintableCircle(PlaPointFloat p_center, double p_radius, java.util.Locale p_locale)
      {
      super(p_locale);
      center = p_center;
      radius = p_radius;
      }

   @Override
   public String toString()
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", this.locale);
      String result = resources.getString("circle") + ": ";
      if (center.point_x != 0 || center.point_y != 0)
         {
         String center_string = resources.getString("center") + " =" + center.to_string(this.locale);
         result += center_string;
         }
      java.text.NumberFormat nf = java.text.NumberFormat.getInstance(this.locale);
      nf.setMaximumFractionDigits(4);
      String radius_string = resources.getString("radius") + " = " + nf.format((float) radius);
      result += radius_string;
      return result;
      }
   }
