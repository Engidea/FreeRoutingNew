package board.varie;

import freert.planar.PlaPointFloat;
import board.items.BrdTracep;

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
