package board.varie;
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

import board.items.BrdTracep;
import freert.planar.PlaPointFloat;

/**
 * Information about an entry point of p_trace into the shape. The entry points are sorted around the border of the shape
 */
public class ShapeTraceEntryPoint
   {
   public ShapeTraceEntryPoint(BrdTracep p_trace, int p_trace_line_no, int p_edge_no, PlaPointFloat p_entry_approx)
      {
      trace = p_trace;
      edge_no = p_edge_no;
      trace_line_no = p_trace_line_no;
      entry_approx = p_entry_approx;
      stack_level = -1; // not yet calculated
      }

   public final BrdTracep trace;
   public final int trace_line_no;
   public int edge_no;
   public final PlaPointFloat entry_approx;
   public int stack_level;
   public ShapeTraceEntryPoint next;
   }
