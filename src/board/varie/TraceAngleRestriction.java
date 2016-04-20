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
 * SnapAngle.java
 *
 * Created on 14. Juli 2003, 07:40
 */

package board.varie;

/**
 * Enum for angle restrictions none, 45 degree and 90 degree.
 *
 * @author Alfons Wirtz
 */
public final class TraceAngleRestriction
   {
   public static final TraceAngleRestriction NONE = new TraceAngleRestriction("none", 0);
   public static final TraceAngleRestriction FORTYFIVE_DEGREE = new TraceAngleRestriction("45 degree", 1);
   public static final TraceAngleRestriction NINETY_DEGREE = new TraceAngleRestriction("90 degree", 2);

   public static final TraceAngleRestriction[] arr = { NONE, FORTYFIVE_DEGREE, NINETY_DEGREE };

   private final String name;
   private final int index;

   private TraceAngleRestriction(String p_name, int p_no)
      {
      name = p_name;
      index = p_no;
      }
   
   public boolean is_no_restriction()
      {
      return index==0;
      }

   /**
    * Returns the string of this instance
    */
   public String to_string()
      {
      return name;
      }

   /**
    * Returns the number of this instance
    */
   public int get_no()
      {
      return index;
      }
   }