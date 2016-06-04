package autoroute.expand;
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
 * horizontal and vertical costs for traces on a board layer 
 * Need to find out what the number means
 */
public final class ExpandCostFactor
   {
   // The horizontal expansion cost factor on a layer of the board 
   public final double horizontal;
   // The vertical expansion cost factor on a layer of the board 
   public final double vertical;

   public ExpandCostFactor(double p_horizontal, double p_vertical)
      {
      horizontal = p_horizontal;
      vertical = p_vertical;
      }
   }
