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
