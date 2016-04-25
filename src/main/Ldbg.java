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

/**
 * A holder for debug levels
 * It is just a simple and easy way to have a bunch of integers defined as public static final
 * @author damiano
 *
 */
public interface Ldbg
   {
   public int FINE    = 0x00000001;
   public int TRACE   = 0x00000002;
   public int DEBUG   = 0x00000004;
   public int NOTICE  = 0x00000008;
   public int WARNING = 0x00000010;
   public int ERROR   = 0x00000020;
   public int SPC_A   = 0x00000040;
   public int SPC_B   = 0x00000080;
   public int SPC_C   = 0x00000100;  // used in autoroute
   public int RELEASE = 0x00000200;  // A bunch of things in freeroute are bound to a "release" concept
   
   
   }
