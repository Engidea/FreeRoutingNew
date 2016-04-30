package main;

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


import java.util.Locale;

/**
 * For all of you wondering on this class, the rationale is this
 * Put in here objects that should have a "global" reach (Stat stands for staus) and should be globally shared
 * It is not a static    class since you may wish to limit the view of it at some point of the code
 * So, just pass it around and use the bits of it when you need
 * @author damiano
 */
public final class Stat
   {
   public java.util.Locale locale;        // The Locale to be used in the whole program
   
   public transient WindowEventsLog log;

   public int debug_mask;                // debug log mask
   public int debug_level;               // debug level mask
   
   
   public Stat ()
      {
      locale      = Locale.ENGLISH;
      debug_mask  = Mdbg.ALL;
      debug_level = Ldbg.RELEASE;
      }
   
   public void userPrintln(String message)
      {
      log.userPrintln(message);
      }

   public void userPrintln(String message, Exception exc)
      {
      log.exceptionPrint(message, exc);
      }
   
   /**
    * Test the given level and mask against current level and mast
    * @param mask
    * @param level
    * @return true if current mask and level match the parameters
    */
   public boolean debug ( int mask, int level )
      {
      return  ((debug_mask & mask) != 0) && ((debug_level & level) != 0);
      }

   
   }
