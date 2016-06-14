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
 * Enum for angle restrictions none, 45 degree
 * Really, just make it serializabe and be done with it, no ?
 *
 * @author Alfons Wirtz
 */
public final class TraceAngleRestriction
   {
   private static final int ANGLE_NONE=0;
   private static final int ANGLE_45D=1;
   
   public static final TraceAngleRestriction NONE      = new TraceAngleRestriction(ANGLE_NONE, "None");
   public static final TraceAngleRestriction FORTYFIVE = new TraceAngleRestriction(ANGLE_45D, "45 degree");

   private final String name;
   private final int index;

   public static final TraceAngleRestriction get_instance ( int index )
      {
      switch (index ) 
         {
         case ANGLE_NONE: return NONE;
         case ANGLE_45D: return FORTYFIVE;
         default:
            System.err.println("TraceAngleRestriction: ERROR bad index="+index);
            return NONE;
         }
      }
   
   private TraceAngleRestriction(int p_no, String p_name)
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