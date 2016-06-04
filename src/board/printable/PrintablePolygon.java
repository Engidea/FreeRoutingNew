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
