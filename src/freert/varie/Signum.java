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
 */

package freert.varie;

/**
 * Implements the mathematical signum function.
 * Used to have a clear understanding of what is a return code of a method
 * @author Alfons Wirtz
 */

public final class Signum
   {
   public static final Signum POSITIVE = new Signum("positive");
   public static final Signum NEGATIVE = new Signum("negative");
   public static final Signum ZERO = new Signum("zero");

   private final String name;

   /**
    * By making it private there is the guarantee that only three objects are available
    * @param p_name
    */
   private Signum(String p_name)
      {
      name = p_name;
      }
   
   /**
    * Returns the signum of p_value. Values are Signum.POSITIVE, Signum.NEGATIVE and Signum.ZERO
    */
   public static final Signum of(double p_value)
      {
      if (p_value > 0)
         {
         return POSITIVE;
         }
      else if (p_value < 0)
         {
         return NEGATIVE;
         }
      else
         {
         return ZERO;
         }
      }

   /**
    * Returns the signum of p_value as an int. Values are +1, 0 and -1
    */
   public static final int as_int(double p_value)
      {
      if (p_value > 0) 
         return 1;
      else if (p_value < 0) 
         return -1;
      else
         return 0;
      }

   /**
    * Returns the string of this instance
    */
   public String to_string()
      {
      return name;
      }

   /**
    * Returns the opposite Signum of this Signum
    */
   public final Signum negate()
      {
      if (this == POSITIVE)
         {
         return NEGATIVE;
         }
      else if (this == NEGATIVE)
         {
         return POSITIVE;
         }
      else
         {
         return this;
         }
      }

   }