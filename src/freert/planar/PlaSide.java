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

package freert.planar;

/**
 * Implementation of an enum class Side with the three values ON_THE_LEFT, ON_THE_RIGHT, COLLINEAR.
 * Ok, the bigg issue is colinearity, that is the reason for the rationals, to be able to "know" whan a point is on a line...
 * 
 * @author Alfons Wirtz
 */

public final class PlaSide
   {
   public static final PlaSide ON_THE_LEFT  = new PlaSide("on_the_left");
   public static final PlaSide ON_THE_RIGHT = new PlaSide("on_the_right");
   public static final PlaSide COLLINEAR    = new PlaSide("collinear");

   private final String name;

   private PlaSide(String p_name)
      {
      name = p_name;
      }
   
   @Override
   public String toString()
      {
      return name;
      }

   /**
    * returns the opposite side of this side
    */
   public PlaSide negate()
      {
      if (this == ON_THE_LEFT)
         return ON_THE_RIGHT;
      else if (this == ON_THE_RIGHT)
         return ON_THE_LEFT;
      else
         return this;
      }

   /**
    * @return ON_THE_LEFT, if p_value < 0, ON_THE_RIGHT, if p_value > 0 and COLLINEAR, if p_value == 0
    */
   static PlaSide get_side_of(double p_value)
      {
      if (p_value > 0)
          return ON_THE_RIGHT;
      else if (p_value < 0)
          return ON_THE_LEFT;
      else
         return COLLINEAR;
      }
   
   /**
    * This uses the correct logic for determinant
    * @param p_value
    * @param tolerance a positive number for tolerance
    * @return
    */
   static PlaSide get_side_of(double p_value, double tolerance)
      {
      if (p_value > tolerance)
          return ON_THE_RIGHT;
      else if (p_value < -tolerance)
          return ON_THE_LEFT;
      else
         return COLLINEAR;
      }

   /**
    * Depending on how you calculate the determinant you use this one or the previous one
    * @param p_value
    * @return
    */
   static PlaSide get_side_of(long p_value)
      {
      if (p_value > 0)
          return ON_THE_RIGHT;
      else if (p_value < 0)
          return ON_THE_LEFT;
      else
         return COLLINEAR;
      }

   }