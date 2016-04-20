/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
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
 * PrintableShape.java
 *
 * Created on 5. Januar 2005, 08:02
 */

package board.printable;


/**
 * Shape class used for printing a geometry.planar.Shape after transforming it to user coordinates.
 *
 * @author Alfons Wirtz
 */
public abstract class PrintableShape
   {
   protected final java.util.Locale locale;

   protected PrintableShape(java.util.Locale p_locale)
      {
      this.locale = p_locale;
      }

   /**
    * Returns text information about the PrintableShape.
    */
   public abstract String toString();



   }
