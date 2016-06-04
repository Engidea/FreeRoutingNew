package board.printable;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 */

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
      if (center.v_x != 0 || center.v_y != 0)
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
