package freert.planar;

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
 * This wish to make sure that types on dimensions are handled correctly
 * @author damiano
 *
 */
public final class PlaDimension
   {
   private static final int PLAD_AREA=2;
   private static final int PLAD_LINE=1;
   private static final int PLAD_POINT=0;
   private static final int PLAD_EMPTY=-1;
   
   public static final PlaDimension AREA  = new PlaDimension(PLAD_AREA,"Area");
   public static final PlaDimension LINE  = new PlaDimension(PLAD_LINE,"Line");
   public static final PlaDimension POINT = new PlaDimension(PLAD_POINT,"Point");
   public static final PlaDimension EMPTY = new PlaDimension(PLAD_EMPTY,"Empty");
   
   public final String name;

   public  final int value;
   
   private PlaDimension ( int p_value, String p_name )
      {
      value = p_value;
      name = p_name;
      }
   
   public boolean is_line ()
      {
      return value == PLAD_LINE;
      }

   public boolean is_area ()
      {
      return value >= PLAD_AREA;
      }
   
   /**
    * A point is considered an empty dimension
    * @return
    */
   public boolean is_empty ()
      {
      return value <= PLAD_POINT;
      }
   
   
   public boolean less ( PlaDimension p_other )
      {
      return value < p_other.value;
      }
   }
