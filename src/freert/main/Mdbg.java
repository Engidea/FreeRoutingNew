package freert.main;
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
 * A holder for debug mask
 * It is just a simple and easy way to have a bunch of integers defined as public static final
 * @author damiano
 *
 */
public interface Mdbg
   {
   public int GUI         = 0x00000001;
   public int GUI_KEY     = 0x00000002;
   public int GUI_MENU    = 0x00000004;
   public int GUI_FILE    = 0x00000008;
   public int PLADELTRI   = 0x00000010;
   public int MAZE        = 0x00000020;
   public int PUSH        = 0x00000040;
   public int PUSH_VIA    = 0x00000080;
   public int PUSH_TRACE  = 0x00000100;
   public int CLRVIOL     = 0x00000200;    // Clearance violations
   public int AUTORT      = 0x00000400;    // Autoroute stuff
   public int OPTIMIZE    = 0x00000800;    // Optimize
   public int SHAPE       = 0x00001000;    // 
   public int DSN         = 0x00002000;    //
   public int TRACE_SPLIT = 0x00004000;    // Splitting traces...
   
   public int ALL        = 0xFFFFFFFF;    // all masks
   }
