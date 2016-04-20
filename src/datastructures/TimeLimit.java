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
 * TimeLimit.java
 *
 * Created on 15. Maerz 2006, 09:27
 *
 */

package datastructures;

/**
 * Class used to cancel a performance critical algorithm after a time limit is exceeded.
 * It can be argued that if zero is provided stop condition should never be met
 * This is generally speaking not a wise definition
 * @author Alfons Wirtz
 */
public class TimeLimit
   {
   private final long target_time_ms;
   
   /**
    * Creates a new instance with a time limit of p_seconds
    * Minimum value is clipped at 1, max value throws an exception
    * The idea is that I wish to pick up the situations where autoroute fails due to some timing limitations
    */
   public TimeLimit(int p_seconds)
      {
      long time_now = System.currentTimeMillis(); 
      
      if ( p_seconds < 1 ) p_seconds = 1;
      
      if ( p_seconds > 60 )
         throw new IllegalArgumentException("p_seconds too big = "+p_seconds);
      
      target_time_ms = time_now + p_seconds * 1000;
      }

   /**
    * Returns true, if the time limit provided in the constructor of this class is exceeded.
    */
   public boolean is_stop_requested()
      {
      return System.currentTimeMillis() > target_time_ms;
      }
   }
