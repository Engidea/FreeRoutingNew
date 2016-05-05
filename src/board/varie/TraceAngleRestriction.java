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
 * Really, just make it serializabe and be done with it, no ?
 *
 * @author Alfons Wirtz
 */
public final class TraceAngleRestriction
   {
   private static final int ANGLE_NONE=0;
   private static final int ANGLE_45D=1;
   private static final int ANGLE_90D=2;
   
   public static final TraceAngleRestriction NONE = new TraceAngleRestriction("none", ANGLE_NONE);
   public static final TraceAngleRestriction FORTYFIVE_DEGREE = new TraceAngleRestriction("45 degree", ANGLE_45D);
   public static final TraceAngleRestriction NINETY_DEGREE = new TraceAngleRestriction("90 degree", ANGLE_90D);

   public static final TraceAngleRestriction[] arr = { NONE, FORTYFIVE_DEGREE, NINETY_DEGREE };

   private final String name;
   private final int index;

   private TraceAngleRestriction(String p_name, int p_no)
      {
      name = p_name;
      index = p_no;
      }
   
   public boolean is_limit_none()
      {
      return index==ANGLE_NONE;
      }

   public boolean is_limit_45()
      {
      return index==ANGLE_45D;
      }

   public boolean is_limit_90()
      {
      return index==ANGLE_90D;
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