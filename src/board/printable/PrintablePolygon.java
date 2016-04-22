package board.printable;

import freert.planar.PlaPointFloat;

public final class PrintablePolygon extends PrintableShape
   {
   public final PlaPointFloat[] corner_arr;

   public PrintablePolygon(PlaPointFloat[] p_corners, java.util.Locale p_locale)
      {
      super(p_locale);
      corner_arr = p_corners;
      }

   @Override
   public String toString()
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", this.locale);
      String result = resources.getString("polygon") + ": ";
      for (int i = 0; i < corner_arr.length; ++i)
         {
         if (i > 0)
            {
            result += ", ";
            }
         result += corner_arr[i].to_string(this.locale);
         }
      return result;
      }
   }
