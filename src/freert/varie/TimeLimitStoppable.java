package freert.varie;

/*
 *  Copyright (C) 2016  Damiano Bolla  website www.engidea.com
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


/**
 * There is a need to simplify the logic on time limit and stoppable
 * At the moment there is a bit of confusion and what happens is that algorithm has different behavior if it hit a timeout or not
 * Things are not so easy since in some cases you wish to inherit the TimeLimit and in other cases you do not wish so
 * @author damiano
 *
 */
public class TimeLimitStoppable extends TimeLimit implements ThreadStoppable
   {
   private ThreadStoppable a_stoppable;

   public TimeLimitStoppable(int p_seconds)
      {
      super(p_seconds);
      }
   
   public TimeLimitStoppable(int p_seconds, ThreadStoppable p_stoppable)
      {
      super(p_seconds);
      
      a_stoppable = p_stoppable;
      }

   @Override
   public void request_stop()
      {
      if ( a_stoppable == null ) return;
      
      a_stoppable.request_stop();
      }

   @Override
   public boolean is_stop_requested()
      {
      if ( super.is_stop_requested()) return true;
      
      if ( a_stoppable != null ) return a_stoppable.is_stop_requested();
      
      return false;
      }

   }
