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
 *
 * Implements functionality for convex shapes, whose borderline directions are multiples of 45 degree and defined with integer
 * coordinates.
 *
 * @author Alfons Wirtz
 */

public final class ShapeTileOctagon extends ShapeTileRegular 
   {
   private static final long serialVersionUID = 1L;

   // Reusable instance of an empty octagon.
   public static final ShapeTileOctagon EMPTY = new ShapeTileOctagon(PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT);

   // x coordinate of the left border line
   public final int oct_lx;
   // y coordinate of the lower border line
   public final int oct_ly;
   // x coordinate of the right border line
   public final int oct_rx;
   // y coordinate of the upper border line
   public final int oct_uy;
   // x axis intersection of the upper left border line
   public final int oct_ulx;
   // x axis intersection of the lower right border line
   public final int oct_lrx;
   // x axis intersection of the lower left border line
   public final int oct_llx;
   // x axis intersection of the upper right border line
   public final int oct_urx;

   // Result of to_simplex() memorized for performance reasons.
   private ShapeTileSimplex precalculated_to_simplex = null;

   /**
    * Construct n octagon around the given point
    * @param a_point
    */
   public ShapeTileOctagon ( PlaPoint a_point )
      {
      if ( a_point instanceof PlaPointInt )
         {
         PlaPointInt pint = (PlaPointInt) a_point;
         int tmp_1 = pint.v_x - pint.v_y;
         int tmp_2 = pint.v_x + pint.v_y;

         oct_lx  = pint.v_x;
         oct_ly  = pint.v_y;
         oct_rx  = pint.v_x;;
         oct_uy  = pint.v_y;
         oct_ulx = tmp_1;
         oct_lrx = tmp_1;
         oct_llx = tmp_2;
         oct_urx = tmp_2;
         }
      else if ( a_point instanceof PlaPointRational )
         {
         PlaPointFloat fp = ((PlaPointRational)a_point).to_float();
         int lx = (int) Math.floor(fp.point_x);
         int ly = (int) Math.floor(fp.point_y);
         int rx = (int) Math.ceil(fp.point_x);
         int uy = (int) Math.ceil(fp.point_y);

         double tmp = fp.point_x - fp.point_y;
         int ulx = (int) Math.floor(tmp);
         int lrx = (int) Math.ceil(tmp);

         tmp = fp.point_x + fp.point_y;
         int llx = (int) Math.floor(tmp);
         int urx = (int) Math.ceil(tmp);
         
         oct_lx  = lx;
         oct_ly  = ly;
         oct_rx  = rx;
         oct_uy  = uy;
         oct_ulx = ulx;
         oct_lrx = lrx;
         oct_llx = llx;
         oct_urx = urx;
         }
      else
         {
         is_nan  = true;
         oct_lx  = 0;
         oct_ly  = 0;
         oct_rx  = 0;
         oct_uy  = 0;
         oct_ulx = 0;
         oct_lrx = 0;
         oct_llx = 0;
         oct_urx = 0;
         }
      }
   
   /**
    * Creates an IntOctagon from 8 integer values. 
    * p_lx is the smallest x value of the shape. 
    * p_ly is the smallest y value of the shape. 
    * p_rx is the biggest x value of the shape. 
    * p_uy is the biggest y value of the shape. 
    * p_ulx is the intersection of the upper left diagonal boundary line with the x axis. 
    * p_lrx is the intersection of the lower right diagonal boundary line with the x axis. 
    * p_llx is the intersection of the lower left diagonal boundary line with the x axis. 
    * p_urx is the intersection of the upper right diagonal boundary line with the x axis.
    */
   public ShapeTileOctagon(int p_lx, int p_ly, int p_rx, int p_uy, int p_ulx, int p_lrx, int p_llx, int p_urx)
      {
      oct_lx  = p_lx;
      oct_ly  = p_ly;
      oct_rx  = p_rx;
      oct_uy  = p_uy;
      oct_ulx = p_ulx;
      oct_lrx = p_lrx;
      oct_llx = p_llx;
      oct_urx = p_urx;
      }

   // WARNING thisimplementation is not strictly always correct
   @Override   
   public boolean is_empty()
      {
      return this == EMPTY;
      }

   @Override   
   public boolean is_IntOctagon()
      {
      return true;
      }

   @Override
   public boolean is_bounded()
      {
      return true;
      }

   @Override
   public boolean corner_is_bounded(int p_no)
      {
      return true;
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      return new ShapeTileBox(oct_lx, oct_ly, oct_rx, oct_uy);
      }

   @Override
   public ShapeTileOctagon bounding_octagon()
      {
      return this;
      }

   @Override
   public ShapeTileOctagon bounding_tile()
      {
      return this;
      }

   @Override
   public PlaDimension dimension()
      {
      if (this == EMPTY)  return PlaDimension.EMPTY;

      if (oct_rx > oct_lx && oct_uy > oct_ly && oct_lrx > oct_ulx && oct_urx > oct_llx)
         return PlaDimension.AREA;
      
      if (oct_rx == oct_lx && oct_uy == oct_ly)  return  PlaDimension.POINT;

      return PlaDimension.LINE;
      }

   @Override   
   public PlaPointInt corner(int p_no)
      {

      int x;
      int y;
      switch (p_no)
         {
         case 0:
            x = oct_llx - oct_ly;
            y = oct_ly;
            break;
         case 1:
            x = oct_lrx + oct_ly;
            y = oct_ly;
            break;
         case 2:
            x = oct_rx;
            y = oct_rx - oct_lrx;
            break;
         case 3:
            x = oct_rx;
            y = oct_urx - oct_rx;
            break;
         case 4:
            x = oct_urx - oct_uy;
            y = oct_uy;
            break;
         case 5:
            x = oct_ulx + oct_uy;
            y = oct_uy;
            break;
         case 6:
            x = oct_lx;
            y = oct_lx - oct_ulx;
            break;
         case 7:
            x = oct_lx;
            y = oct_llx - oct_lx;
            break;
         default:
            throw new IllegalArgumentException("IntOctagon.corner: p_no out of range");
         }
      return new PlaPointInt(x, y);
      }

   /**
    * Additional to the function corner() for performance reasons to avoid allocation of an IntPoint.
    */
   public int corner_y(int p_no)
      {
      int y;
      switch (p_no)
         {
         case 0:
            y = oct_ly;
            break;
         case 1:
            y = oct_ly;
            break;
         case 2:
            y = oct_rx - oct_lrx;
            break;
         case 3:
            y = oct_urx - oct_rx;
            break;
         case 4:
            y = oct_uy;
            break;
         case 5:
            y = oct_uy;
            break;
         case 6:
            y = oct_lx - oct_ulx;
            break;
         case 7:
            y = oct_llx - oct_lx;
            break;
         default:
            throw new IllegalArgumentException("IntOctagon.corner: p_no out of range");
         }
      return y;
      }

   /**
    * Additional to the function corner() for performance reasons to avoid allocation of an IntPoint.
    */
   public int corner_x(int p_no)
      {

      int x;
      switch (p_no)
         {
         case 0:
            x = oct_llx - oct_ly;
            break;
         case 1:
            x = oct_lrx + oct_ly;
            break;
         case 2:
            x = oct_rx;
            break;
         case 3:
            x = oct_rx;
            break;
         case 4:
            x = oct_urx - oct_uy;
            break;
         case 5:
            x = oct_ulx + oct_uy;
            break;
         case 6:
            x = oct_lx;
            break;
         case 7:
            x = oct_lx;
            break;
         default:
            throw new IllegalArgumentException("IntOctagon.corner: p_no out of range");
         }
      return x;
      }

   @Override   
   public double area()
      {

      // calculate half of the absolute value of
      // x0 (y1 - y7) + x1 (y2 - y0) + x2 (y3 - y1) + ...+ x7( y0 - y6)
      // where xi, yi are the coordinates of the i-th corner of this Octagon.

      // Overwrites the same implementation in TileShape for performence
      // reasons to avoid Point allocation.

      double result = (double) (oct_llx - oct_ly) * (double) (oct_ly - oct_llx + oct_lx);
      result += (double) (oct_lrx + oct_ly) * (double) (oct_rx - oct_lrx - oct_ly);
      result += (double) oct_rx * (double) (oct_urx - 2 * oct_rx - oct_ly + oct_uy + oct_lrx);
      result += (double) (oct_urx - oct_uy) * (double) (oct_uy - oct_urx + oct_rx);
      result += (double) (oct_ulx + oct_uy) * (double) (oct_lx - oct_ulx - oct_uy);
      result += (double) oct_lx * (double) (oct_llx - 2 * oct_lx - oct_uy + oct_ly + oct_ulx);

      return 0.5 * Math.abs(result);
      }

   @Override   
   public int border_line_count()
      {
      return 8;
      }

   @Override   
   public PlaLineInt border_line(int p_no)
      {
      PlaPointInt p_a;
      PlaPointInt p_b;
      
      switch (p_no)
         {
         case 0:
            // lower boundary line
            p_a = new PlaPointInt(0,oct_ly);
            p_b = new PlaPointInt(1,oct_ly);
            break;
         case 1:
            // lower right boundary line
            p_a = new PlaPointInt(oct_lrx,0);
            p_b = new PlaPointInt(oct_lrx + 1,1);
            break;
         case 2:
            // right boundary line
            p_a = new PlaPointInt(oct_rx,0);
            p_b = new PlaPointInt(oct_rx,1);
            break;
         case 3:
            // upper right boundary line
            p_a = new PlaPointInt(oct_urx,0);
            p_b = new PlaPointInt(oct_urx - 1,1);
            break;
         case 4:
            // upper boundary line
            p_a = new PlaPointInt(0,oct_uy);
            p_b = new PlaPointInt(-1, oct_uy);
            break;
         case 5:
            // upper left boundary line
            p_a = new PlaPointInt( oct_ulx,0);
            p_b = new PlaPointInt( oct_ulx - 1, -1);
            break;
         case 6:
            // left boundary line
            p_a = new PlaPointInt( oct_lx, 0);
            p_b = new PlaPointInt( oct_lx, -1);
            break;
         case 7:
            // lower left boundary line
            p_a = new PlaPointInt( oct_llx, 0);
            p_b = new PlaPointInt( oct_llx + 1, -1);
            break;
         default:
            throw new IllegalArgumentException("IntOctagon.edge_line: p_no out of range");
         }
      
      return new PlaLineInt(p_a, p_b);
      }

   @Override   
   public ShapeTileOctagon translate_by(PlaVector p_rel_coor)
      {
      // This function is at the moment only implemented for Vectors
      // with integer coordinates.
      // The general implementation is still missing.

      if (p_rel_coor.equals(PlaVector.ZERO))
         {
         return this;
         }
      PlaVectorInt rel_coor = (PlaVectorInt) p_rel_coor;
      return new ShapeTileOctagon(oct_lx + rel_coor.point_x, oct_ly + rel_coor.point_y, oct_rx + rel_coor.point_x, oct_uy + rel_coor.point_y, oct_ulx + rel_coor.point_x - rel_coor.point_y, oct_lrx + rel_coor.point_x
            - rel_coor.point_y, oct_llx + rel_coor.point_x + rel_coor.point_y, oct_urx + rel_coor.point_x + rel_coor.point_y);
      }

   @Override   
   public double max_width()
      {
      double width_1 = Math.max(oct_rx - oct_lx, oct_uy - oct_ly);
      double width2 = Math.max(oct_urx - oct_llx, oct_lrx - oct_ulx);
      double result = Math.max(width_1, width2 / PlaLimits.sqrt2);
      return result;
      }

   @Override   
   public double min_width()
      {
      double width_1 = Math.min(oct_rx - oct_lx, oct_uy - oct_ly);
      double width2 = Math.min(oct_urx - oct_llx, oct_lrx - oct_ulx);
      double result = Math.min(width_1, width2 / PlaLimits.sqrt2);
      return result;
      }

   @Override   
   public ShapeTileOctagon offset(double p_distance)
      {
      int width = (int) Math.round(p_distance);
      if (width == 0)
         {
         return this;
         }
      int dia_width = (int) Math.round(PlaLimits.sqrt2 * p_distance);
      ShapeTileOctagon result = new ShapeTileOctagon(oct_lx - width, oct_ly - width, oct_rx + width, oct_uy + width, oct_ulx - dia_width, oct_lrx + dia_width, oct_llx - dia_width, oct_urx + dia_width);
      return result.normalize();
      }

   @Override   
   public ShapeTileOctagon enlarge(double p_offset)
      {
      return offset(p_offset);
      }

   @Override   
   public boolean contains(ShapeTileRegular p_other)
      {
      return p_other.is_contained_in(this);
      }

   @Override   
   public ShapeTileRegular union(ShapeTileRegular p_other)
      {
      return p_other.union(this);
      }

   @Override   
   public ShapeTile intersection(ShapeTile p_other)
      {
      return p_other.intersection(this);
      }

   public ShapeTileOctagon normalize()
      {
      if (oct_lx > oct_rx || oct_ly > oct_uy || oct_llx > oct_urx || oct_ulx > oct_lrx)
         {
         return EMPTY;
         }
      int new_lx = oct_lx;
      int new_rx = oct_rx;
      int new_ly = oct_ly;
      int new_uy = oct_uy;
      int new_llx = oct_llx;
      int new_ulx = oct_ulx;
      int new_lrx = oct_lrx;
      int new_urx = oct_urx;

      if (new_lx < new_llx - new_uy)
      // the point new_lx, new_uy is the the lower left border line of
      // this octagon
      // change new_lx , that the the lower left border line runs through
      // this point
         {
         new_lx = new_llx - new_uy;
         }

      if (new_lx < new_ulx + new_ly)
      // the point new_lx, new_ly is above the the upper left border line of
      // this octagon
      // change new_lx , that the the upper left border line runs through
      // this point
         {
         new_lx = new_ulx + new_ly;
         }

      if (new_rx > new_urx - new_ly)
      // the point new_rx, new_ly is above the the upper right border line of
      // this octagon
      // change new_rx , that the the upper right border line runs through
      // this point
         {
         new_rx = new_urx - new_ly;
         }

      if (new_rx > new_lrx + new_uy)
      // the point new_rx, new_uy is below the the lower right border line of
      // this octagon
      // change rx , that the the lower right border line runs through
      // this point

         {
         new_rx = new_lrx + new_uy;
         }

      if (new_ly < new_lx - new_lrx)
      // the point lx, ly is below the lower right border line of this
      // octagon
      // change ly, so that the lower right border line runs through
      // this point
         {
         new_ly = new_lx - new_lrx;
         }

      if (new_ly < new_llx - new_rx)
      // the point rx, ly is below the lower left border line of
      // this octagon.
      // change ly, so that the lower left border line runs through
      // this point
         {
         new_ly = new_llx - new_rx;
         }

      if (new_uy > new_urx - new_lx)
      // the point lx, uy is above the upper right border line of
      // this octagon.
      // Change the uy, so that the upper right border line runs through
      // this point.
         {
         new_uy = new_urx - new_lx;
         }

      if (new_uy > new_rx - new_ulx)
      // the point rx, uy is above the upper left border line of
      // this octagon.
      // Change the uy, so that the upper left border line runs through
      // this point.
         {
         new_uy = new_rx - new_ulx;
         }

      if (new_llx - new_lx < new_ly)
      // The point lx, ly is above the lower left border line of
      // this octagon.
      // Change the lower left line, so that it runs through this point.
         {
         new_llx = new_lx + new_ly;
         }

      if (new_rx - new_lrx < new_ly)
      // the point rx, ly is above the lower right border line of
      // this octagon.
      // Change the lower right line, so that it runs through this point.
         {
         new_lrx = new_rx - new_ly;
         }

      if (new_urx - new_rx > new_uy)
      // the point rx, uy is below the upper right border line of p_oct.
      // Change the upper right line, so that it runs through this point.
         {
         new_urx = new_uy + new_rx;
         }

      if (new_lx - new_ulx > new_uy)
      // the point lx, uy is below the upper left border line of
      // this octagon.
      // Change the upper left line, so that it runs through this point.
         {
         new_ulx = new_lx - new_uy;
         }

      int diag_upper_y = (int) Math.ceil((new_urx - new_ulx) / 2.0);

      if (new_uy > diag_upper_y)
      // the intersection of the upper right and the upper left border
      // line is below new_uy. Adjust new_uy to diag_upper_y.
         {
         new_uy = diag_upper_y;
         }

      int diag_lower_y = (int) Math.floor((new_llx - new_lrx) / 2.0);

      if (new_ly < diag_lower_y)
      // the intersection of the lower right and the lower left border
      // line is above new_ly. Adjust new_ly to diag_lower_y.
         {
         new_ly = diag_lower_y;
         }

      int diag_right_x = (int) Math.ceil((new_urx + new_lrx) / 2.0);

      if (new_rx > diag_right_x)
      // the intersection of the upper right and the lower right border
      // line is to the left of right x. Adjust new_rx to diag_right_x.
         {
         new_rx = diag_right_x;
         }

      int diag_left_x = (int) Math.floor((new_llx + new_ulx) / 2.0);

      if (new_lx < diag_left_x)
      // the intersection of the lower left and the upper left border
      // line is to the right of left x. Ajust new_lx to diag_left_x.
         {
         new_lx = diag_left_x;
         }
      if (new_lx > new_rx || new_ly > new_uy || new_llx > new_urx || new_ulx > new_lrx)
         {
         return EMPTY;
         }
      return new ShapeTileOctagon(new_lx, new_ly, new_rx, new_uy, new_ulx, new_lrx, new_llx, new_urx);
      }

   /**
    * Checks, if this IntOctagon is normalized.
    */
   public boolean is_normalized()
      {
      ShapeTileOctagon on = this.normalize();
      boolean result = oct_lx == on.oct_lx && oct_ly == on.oct_ly && oct_rx == on.oct_rx && oct_uy == on.oct_uy && oct_llx == on.oct_llx && oct_lrx == on.oct_lrx && oct_ulx == on.oct_ulx && oct_urx == on.oct_urx;
      return result;
      }

   @Override   
   public ShapeTileSimplex to_Simplex()
      {
      if (is_empty())
         {
         return ShapeTileSimplex.EMPTY;
         }
      if (precalculated_to_simplex == null)
         {
         PlaLineInt[] line_arr = new PlaLineInt[8];
         for (int i = 0; i < 8; ++i)
            {
            line_arr[i] = border_line(i);
            }
         ShapeTileSimplex curr_simplex = new ShapeTileSimplex(line_arr);
         precalculated_to_simplex = curr_simplex.remove_redundant_lines();
         }
      return precalculated_to_simplex;
      }

   @Override   
   public ShapeTileRegular bounding_shape(ShapeBounding p_dirs)
      {
      return p_dirs.bounds(this);
      }

   @Override   
   public boolean intersects(PlaShape p_other)
      {
      return p_other.intersects(this);
      }

   /**
    * Returns true, if p_point is contained in this octagon. Because of the parameter type FloatPoint, the function may not be exact
    * close to the border.
    */
   @Override   
   public boolean contains(PlaPointFloat p_point)
      {
      if (oct_lx > p_point.point_x || oct_ly > p_point.point_y || oct_rx < p_point.point_x || oct_uy < p_point.point_y)
         {
         return false;
         }
      double tmp_1 = p_point.point_x - p_point.point_y;
      double tmp_2 = p_point.point_x + p_point.point_y;
      if (oct_ulx > tmp_1 || oct_lrx < tmp_1 || oct_llx > tmp_2 || oct_urx < tmp_2)
         {
         return false;
         }
      return true;
      }

   /**
    * Calculates the side of the point (p_x, p_y) of the border line with index p_border_line_no. The border lines are located in
    * counterclock sense around this octagon.
    */
   public PlaSide side_of_border_line(int p_x, int p_y, int p_border_line_no)
      {

      int tmp;
      if (p_border_line_no == 0)
         {
         tmp = this.oct_ly - p_y;
         }
      else if (p_border_line_no == 2)
         {
         tmp = p_x - this.oct_rx;
         }
      else if (p_border_line_no == 4)
         {
         tmp = p_y - this.oct_uy;
         }
      else if (p_border_line_no == 6)
         {
         tmp = this.oct_lx - p_x;
         }
      else if (p_border_line_no == 1)
         {
         tmp = p_x - p_y - this.oct_lrx;
         }
      else if (p_border_line_no == 3)
         {
         tmp = p_x + p_y - this.oct_urx;
         }
      else if (p_border_line_no == 5)
         {
         tmp = this.oct_ulx + p_y - p_x;
         }
      else if (p_border_line_no == 7)
         {
         tmp = this.oct_llx - p_x - p_y;
         }
      else
         {
         System.out.println("IntOctagon.side_of_border_line: p_border_line_no out of range");
         tmp = 0;
         }
      PlaSide result;
      if (tmp < 0)
         {
         result = PlaSide.ON_THE_LEFT;
         }
      else if (tmp > 0)
         {
         result = PlaSide.ON_THE_RIGHT;
         }
      else
         {
         result = PlaSide.COLLINEAR;
         }
      return result;
      }

   @Override   
   ShapeTileSimplex intersection(ShapeTileSimplex p_other)
      {
      return p_other.intersection(this);
      }

   @Override   
   public ShapeTileOctagon intersection(ShapeTileOctagon p_other)
      {
      ShapeTileOctagon result = new ShapeTileOctagon(Math.max(oct_lx, p_other.oct_lx), Math.max(oct_ly, p_other.oct_ly), Math.min(oct_rx, p_other.oct_rx), Math.min(oct_uy, p_other.oct_uy), Math.max(oct_ulx, p_other.oct_ulx), Math.min(oct_lrx,
            p_other.oct_lrx), Math.max(oct_llx, p_other.oct_llx), Math.min(oct_urx, p_other.oct_urx));
      return result.normalize();
      }

   @Override   
   ShapeTileOctagon intersection(ShapeTileBox p_other)
      {
      return intersection(p_other.to_IntOctagon());
      }

   /**
    * checkes if this (normalized) octagon is contained in p_box
    */
   @Override   
   public boolean is_contained_in(ShapeTileBox p_box)
      {
      return (oct_lx >= p_box.box_ll.v_x && oct_ly >= p_box.box_ll.v_y && oct_rx <= p_box.box_ur.v_x && oct_uy <= p_box.box_ur.v_y);
      }

   @Override   
   public boolean is_contained_in(ShapeTileOctagon p_other)
      {
      boolean result = oct_lx >= p_other.oct_lx && oct_ly >= p_other.oct_ly && oct_rx <= p_other.oct_rx && oct_uy <= p_other.oct_uy && oct_llx >= p_other.oct_llx && oct_ulx >= p_other.oct_ulx && oct_lrx <= p_other.oct_lrx && oct_urx <= p_other.oct_urx;

      return result;
      }

   @Override   
   public ShapeTileOctagon union(ShapeTileOctagon p_other)
      {
      ShapeTileOctagon result = new ShapeTileOctagon(Math.min(oct_lx, p_other.oct_lx), Math.min(oct_ly, p_other.oct_ly), Math.max(oct_rx, p_other.oct_rx), Math.max(oct_uy, p_other.oct_uy), Math.min(oct_ulx, p_other.oct_ulx), Math.max(oct_lrx,
            p_other.oct_lrx), Math.min(oct_llx, p_other.oct_llx), Math.max(oct_urx, p_other.oct_urx));
      return result;
      }

   @Override   
   public boolean intersects(ShapeTileBox p_other)
      {
      return intersects(p_other.to_IntOctagon());
      }

   /**
    * checks, if two normalized Octagons intersect.
    */
   @Override   
   public boolean intersects(ShapeTileOctagon p_other)
      {
      int is_lx;
      int is_rx;
      if (p_other.oct_lx > this.oct_lx)
         {
         is_lx = p_other.oct_lx;
         }
      else
         {
         is_lx = this.oct_lx;
         }
      if (p_other.oct_rx < this.oct_rx)
         {
         is_rx = p_other.oct_rx;
         }
      else
         {
         is_rx = this.oct_rx;
         }
      if (is_lx > is_rx)
         {
         return false;
         }

      int is_ly;
      int is_uy;
      if (p_other.oct_ly > this.oct_ly)
         {
         is_ly = p_other.oct_ly;
         }
      else
         {
         is_ly = this.oct_ly;
         }
      if (p_other.oct_uy < this.oct_uy)
         {
         is_uy = p_other.oct_uy;
         }
      else
         {
         is_uy = this.oct_uy;
         }
      if (is_ly > is_uy)
         {
         return false;
         }

      int is_llx;
      int is_urx;
      if (p_other.oct_llx > this.oct_llx)
         {
         is_llx = p_other.oct_llx;
         }
      else
         {
         is_llx = this.oct_llx;
         }
      if (p_other.oct_urx < this.oct_urx)
         {
         is_urx = p_other.oct_urx;
         }
      else
         {
         is_urx = this.oct_urx;
         }
      if (is_llx > is_urx)
         {
         return false;
         }

      int is_ulx;
      int is_lrx;
      if (p_other.oct_ulx > this.oct_ulx)
         {
         is_ulx = p_other.oct_ulx;
         }
      else
         {
         is_ulx = this.oct_ulx;
         }
      if (p_other.oct_lrx < this.oct_lrx)
         {
         is_lrx = p_other.oct_lrx;
         }
      else
         {
         is_lrx = this.oct_lrx;
         }
      if (is_ulx > is_lrx)
         {
         return false;
         }
      return true;
      }

   /**
    * Returns true, if this octagon intersects with p_other and the intersection is 2-dimensional.
    */
   public boolean overlaps(ShapeTileOctagon p_other)
      {
      int is_lx;
      int is_rx;
      if (p_other.oct_lx > this.oct_lx)
         {
         is_lx = p_other.oct_lx;
         }
      else
         {
         is_lx = this.oct_lx;
         }
      if (p_other.oct_rx < this.oct_rx)
         {
         is_rx = p_other.oct_rx;
         }
      else
         {
         is_rx = this.oct_rx;
         }
      if (is_lx >= is_rx)
         {
         return false;
         }

      int is_ly;
      int is_uy;
      if (p_other.oct_ly > this.oct_ly)
         {
         is_ly = p_other.oct_ly;
         }
      else
         {
         is_ly = this.oct_ly;
         }
      if (p_other.oct_uy < this.oct_uy)
         {
         is_uy = p_other.oct_uy;
         }
      else
         {
         is_uy = this.oct_uy;
         }
      if (is_ly >= is_uy)
         {
         return false;
         }

      int is_llx;
      int is_urx;
      if (p_other.oct_llx > this.oct_llx)
         {
         is_llx = p_other.oct_llx;
         }
      else
         {
         is_llx = this.oct_llx;
         }
      if (p_other.oct_urx < this.oct_urx)
         {
         is_urx = p_other.oct_urx;
         }
      else
         {
         is_urx = this.oct_urx;
         }
      if (is_llx >= is_urx)
         {
         return false;
         }

      int is_ulx;
      int is_lrx;
      if (p_other.oct_ulx > this.oct_ulx)
         {
         is_ulx = p_other.oct_ulx;
         }
      else
         {
         is_ulx = this.oct_ulx;
         }
      if (p_other.oct_lrx < this.oct_lrx)
         {
         is_lrx = p_other.oct_lrx;
         }
      else
         {
         is_lrx = this.oct_lrx;
         }
      if (is_ulx >= is_lrx)
         {
         return false;
         }
      return true;
      }

   @Override   
   public boolean intersects(ShapeTileSimplex p_other)
      {
      return p_other.intersects(this);
      }

   @Override   
   public boolean intersects(PlaCircle p_other)
      {
      return p_other.intersects(this);
      }

   @Override   
   public ShapeTileOctagon union(ShapeTileBox p_other)
      {
      return union(p_other.to_IntOctagon());
      }

   /**
    * computes the x value of the left boundary of this Octagon at p_y
    */
   public int left_x_value(int p_y)
      {
      int result = Math.max(oct_lx, oct_ulx + p_y);
      return Math.max(result, oct_llx - p_y);
      }

   /**
    * computes the x value of the right boundary of this Octagon at p_y
    */
   public int right_x_value(int p_y)
      {
      int result = Math.min(oct_rx, oct_urx - p_y);
      return Math.min(result, oct_lrx + p_y);
      }

   /**
    * computes the y value of the lower boundary of this Octagon at p_x
    */
   public int lower_y_value(int p_x)
      {
      int result = Math.max(oct_ly, oct_llx - p_x);
      return Math.max(result, p_x - oct_lrx);
      }

   /**
    * computes the y value of the upper boundary of this Octagon at p_x
    */
   public int upper_y_value(int p_x)
      {
      int result = Math.min(oct_uy, p_x - oct_ulx);
      return Math.min(result, oct_urx - p_x);
      }

   @Override   
   public PlaSide compare(ShapeTileRegular p_other, int p_edge_no)
      {
      PlaSide result = p_other.compare(this, p_edge_no);
      return result.negate();
      }

   @Override   
   public PlaSide compare(ShapeTileOctagon p_other, int p_edge_no)
      {
      PlaSide result;
      switch (p_edge_no)
         {
         case 0:
            // compare the lower edge line
            if (oct_ly > p_other.oct_ly)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_ly < p_other.oct_ly)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 1:
            // compare the lower right edge line
            if (oct_lrx < p_other.oct_lrx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_lrx > p_other.oct_lrx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 2:
            // compare the right edge line
            if (oct_rx < p_other.oct_rx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_rx > p_other.oct_rx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 3:
            // compare the upper right edge line
            if (oct_urx < p_other.oct_urx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_urx > p_other.oct_urx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 4:
            // compare the upper edge line
            if (oct_uy < p_other.oct_uy)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_uy > p_other.oct_uy)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 5:
            // compare the upper left edge line
            if (oct_ulx > p_other.oct_ulx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_ulx < p_other.oct_ulx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 6:
            // compare the left edge line
            if (oct_lx > p_other.oct_lx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_lx < p_other.oct_lx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;

         case 7:
            // compare the lower left edge line
            if (oct_llx > p_other.oct_llx)
               {
               result = PlaSide.ON_THE_LEFT;
               }
            else if (oct_llx < p_other.oct_llx)
               {
               result = PlaSide.ON_THE_RIGHT;
               }
            else
               {
               result = PlaSide.COLLINEAR;
               }
            break;
         default:
            throw new IllegalArgumentException("IntBox.compare: p_edge_no out of range");

         }
      return result;
      }

   @Override
   public PlaSide compare(ShapeTileBox p_other, int p_edge_no)
      {
      return compare(p_other.to_IntOctagon(), p_edge_no);
      }

   @Override
   public int border_line_index(PlaLineInt p_line)
      {
      throw new IllegalArgumentException("edge_index_of_line not yet implemented for octagons");
      }

   /**
    * Calculates the border point of this octagon from p_point into the 45 degree direction p_dir. If this border point is not an
    * IntPoint the nearest outside IntPoint of the octagon is returned.
    */
   private PlaPointInt border_point(PlaPointInt p_point, PlaDirection p_dir)
      {
      int result_x;
      int result_y;
      
      if (p_dir == PlaDirection.RIGHT)
         {
         result_x = Math.min(oct_rx, oct_urx - p_point.v_y);
         result_x = Math.min(result_x, oct_lrx + p_point.v_y);
         result_y = p_point.v_y;
         }
      else if (p_dir == PlaDirection.LEFT)
         {
         result_x = Math.max(oct_lx, oct_ulx + p_point.v_y);
         result_x = Math.max(result_x, oct_llx - p_point.v_y);
         result_y = p_point.v_y;
         }
      else if (p_dir == PlaDirection.UP)
         {
         result_x = p_point.v_x;
         result_y = Math.min(oct_uy, p_point.v_x - oct_ulx);
         result_y = Math.min(result_y, oct_urx - p_point.v_x);
         }
      else if (p_dir == PlaDirection.DOWN)
         {
         result_x = p_point.v_x;
         result_y = Math.max(oct_ly, oct_llx - p_point.v_x);
         result_y = Math.max(result_y, p_point.v_x - oct_lrx);
         }
      else if (p_dir == PlaDirection.RIGHT45)
         {
         result_x = (int) (Math.ceil(0.5 * (p_point.v_x - p_point.v_y + oct_urx)));
         result_x = Math.min(result_x, oct_rx);
         result_x = Math.min(result_x, p_point.v_x - p_point.v_x + oct_uy);
         result_y = p_point.v_y - p_point.v_x + result_x;
         }
      else if (p_dir == PlaDirection.UP45)
         {
         result_x = (int) (Math.floor(0.5 * (p_point.v_x + p_point.v_y + oct_ulx)));
         result_x = Math.max(result_x, oct_lx);
         result_x = Math.max(result_x, p_point.v_x + p_point.v_y - oct_uy);
         result_y = p_point.v_y + p_point.v_x - result_x;
         }
      else if (p_dir == PlaDirection.LEFT45)
         {
         result_x = (int) (Math.floor(0.5 * (p_point.v_x - p_point.v_y + oct_llx)));
         result_x = Math.max(result_x, oct_lx);
         result_x = Math.max(result_x, p_point.v_x - p_point.v_y + oct_ly);
         result_y = p_point.v_y - p_point.v_x + result_x;
         }
      else if (p_dir == PlaDirection.DOWN45)
         {
         result_x = (int) (Math.ceil(0.5 * (p_point.v_x + p_point.v_y + oct_lrx)));
         result_x = Math.min(result_x, oct_rx);
         result_x = Math.min(result_x, p_point.v_x + p_point.v_y - oct_ly);
         result_y = p_point.v_y + p_point.v_x - result_x;
         }
      else
         {
         throw new IllegalArgumentException("IntOctagon.border_point: unexpected 45 degree direction");
         }
      
      return new PlaPointInt(result_x, result_y);
      }

   static final PlaDirection[] eight_45_degree = new PlaDirection[] { PlaDirection.RIGHT, PlaDirection.RIGHT45, PlaDirection.UP, PlaDirection.UP45, PlaDirection.LEFT, PlaDirection.LEFT45,
         PlaDirection.DOWN, PlaDirection.DOWN45 };

   /**
    * Calculates the sorted p_max_result_points nearest points on the border of this octagon in the 45-degree directions. p_point is
    * assumed to be located in the inside of this octagon.
    * 
    * @return and empty array if conditions are not met
    */
   public PlaPointInt[] nearest_border_projections(PlaPointInt p_point, int p_max_result_points)
      {
      if (!contains(p_point)) return new PlaPointInt[0];

      if (p_max_result_points <= 0) return new PlaPointInt[0];

      p_max_result_points = Math.min(p_max_result_points, 8);

      PlaPointInt[] result = new PlaPointInt[p_max_result_points];

      double[] min_dist = new double[p_max_result_points];

      for (int index = 0; index < p_max_result_points; ++index)
         min_dist[index] = Double.MAX_VALUE;

      PlaPointFloat inside_point = p_point.to_float();

      for (PlaDirection curr_dir : eight_45_degree)
         {
         PlaPointInt curr_border_point = border_point(p_point, curr_dir);
         double curr_dist = inside_point.distance_square(curr_border_point.to_float());
         for (int i = 0; i < p_max_result_points; ++i)
            {
            if (curr_dist < min_dist[i])
               {
               for (int k = p_max_result_points - 1; k > i; --k)
                  {
                  min_dist[k] = min_dist[k - 1];
                  result[k] = result[k - 1];
                  }
               min_dist[i] = curr_dist;
               result[i] = curr_border_point;
               break;
               }
            }
         }

      return result;
      }

   /**
    * Checks, if this octagon can be converted to an IntBox.
    */
   @Override   
   public boolean is_IntBox()
      {
      if (oct_llx != oct_lx + oct_ly) return false;

      if (oct_lrx != oct_rx - oct_ly) return false;

      if (oct_urx != oct_rx + oct_uy) return false;

      if (oct_ulx != oct_lx - oct_uy) return false;

      return true;
      }

   @Override
   public ShapeTile simplify()
      {
      if (is_IntBox()) return this.bounding_box();

      return this;
      }

   @Override
   public ShapeTile[] cutout(ShapeTile p_shape)
      {
      return p_shape.cutout_from(this);
      }

   /**
    * Divide p_d minus this octagon into 8 convex pieces, from which 4 have cut off a corner.
    */
   @Override
   ShapeTileOctagon[] cutout_from(ShapeTileBox p_d)
      {
      ShapeTileOctagon c = this.intersection(p_d);

      if ( is_empty() || c.dimension().less(dimension()) )
         {
         // there is only an overlap at the border
         ShapeTileOctagon[] result = new ShapeTileOctagon[1];
         result[0] = p_d.to_IntOctagon();
         return result;
         }

      ShapeTileBox[] boxes = new ShapeTileBox[4];

      // construct left box

      boxes[0] = new ShapeTileBox(p_d.box_ll.v_x, c.oct_llx - c.oct_lx, c.oct_lx, c.oct_lx - c.oct_ulx);

      // construct right box

      boxes[1] = new ShapeTileBox(c.oct_rx, c.oct_rx - c.oct_lrx, p_d.box_ur.v_x, c.oct_urx - c.oct_rx);

      // construct lower box

      boxes[2] = new ShapeTileBox(c.oct_llx - c.oct_ly, p_d.box_ll.v_y, c.oct_lrx + c.oct_ly, c.oct_ly);

      // construct upper box

      boxes[3] = new ShapeTileBox(c.oct_ulx + c.oct_uy, c.oct_uy, c.oct_urx - c.oct_uy, p_d.box_ur.v_y);

      ShapeTileOctagon[] octagons = new ShapeTileOctagon[4];

      // construct upper left octagon

      ShapeTileOctagon curr_oct = new ShapeTileOctagon(p_d.box_ll.v_x, boxes[0].box_ur.v_y, boxes[3].box_ll.v_x, p_d.box_ur.v_y, -PlaLimits.CRIT_INT, c.oct_ulx, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT);
      octagons[0] = curr_oct.normalize();

      // construct lower left octagon

      curr_oct = new ShapeTileOctagon(p_d.box_ll.v_x, p_d.box_ll.v_y, boxes[2].box_ll.v_x, boxes[0].box_ll.v_y, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, c.oct_llx);
      octagons[1] = curr_oct.normalize();

      // construct lower right octagon

      curr_oct = new ShapeTileOctagon(boxes[2].box_ur.v_x, p_d.box_ll.v_y, p_d.box_ur.v_x, boxes[1].box_ll.v_y, c.oct_lrx, PlaLimits.CRIT_INT, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT);
      octagons[2] = curr_oct.normalize();

      // construct upper right octagon

      curr_oct = new ShapeTileOctagon(boxes[3].box_ur.v_x, boxes[1].box_ur.v_y, p_d.box_ur.v_x, p_d.box_ur.v_y, -PlaLimits.CRIT_INT, PlaLimits.CRIT_INT, c.oct_urx, PlaLimits.CRIT_INT);
      octagons[3] = curr_oct.normalize();

      // optimise the result to minimum cumulative circumference

      ShapeTileBox b = boxes[0];
      ShapeTileOctagon o = octagons[0];
      if (b.box_ur.v_x - b.box_ll.v_x > o.oct_uy - o.oct_ly)
         {
         // switch the horizontal upper left divide line to vertical

         boxes[0] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, o.oct_uy);
         curr_oct = new ShapeTileOctagon(b.box_ur.v_x, o.oct_ly, o.oct_rx, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[0] = curr_oct.normalize();
         }

      b = boxes[3];
      o = octagons[0];
      if (b.box_ur.v_y - b.box_ll.v_y > o.oct_rx - o.oct_lx)
         {
         // switch the vertical upper left divide line to horizontal

         boxes[3] = new ShapeTileBox(o.oct_lx, b.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(o.oct_lx, o.oct_ly, o.oct_rx, b.box_ll.v_y, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[0] = curr_oct.normalize();
         }
      b = boxes[3];
      o = octagons[3];
      if (b.box_ur.v_y - b.box_ll.v_y > o.oct_rx - o.oct_lx)
         {
         // switch the vertical upper right divide line to horizontal

         boxes[3] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, o.oct_rx, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(o.oct_lx, o.oct_ly, o.oct_rx, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[3] = curr_oct.normalize();
         }
      b = boxes[1];
      o = octagons[3];
      if (b.box_ur.v_x - b.box_ll.v_x > o.oct_uy - o.oct_ly)
         {
         // switch the horizontal upper right divide line to vertical

         boxes[1] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, b.box_ur.v_x, o.oct_uy);
         curr_oct = new ShapeTileOctagon(o.oct_lx, o.oct_ly, b.box_ll.v_x, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[3] = curr_oct.normalize();
         }
      b = boxes[1];
      o = octagons[2];
      if (b.box_ur.v_x - b.box_ll.v_x > o.oct_uy - o.oct_ly)
         {
         // switch the horizontal lower right divide line to vertical

         boxes[1] = new ShapeTileBox(b.box_ll.v_x, o.oct_ly, b.box_ur.v_x, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(o.oct_lx, o.oct_ly, b.box_ll.v_x, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[2] = curr_oct.normalize();
         }
      b = boxes[2];
      o = octagons[2];
      if (b.box_ur.v_y - b.box_ll.v_y > o.oct_rx - o.oct_lx)
         {
         // switch the vertical lower right divide line to horizontal

         boxes[2] = new ShapeTileBox(b.box_ll.v_x, b.box_ll.v_y, o.oct_rx, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(o.oct_lx, b.box_ur.v_y, o.oct_rx, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[2] = curr_oct.normalize();
         }
      b = boxes[2];
      o = octagons[1];
      if (b.box_ur.v_y - b.box_ll.v_y > o.oct_rx - o.oct_lx)
         {
         // switch the vertical lower left divide line to horizontal

         boxes[2] = new ShapeTileBox(o.oct_lx, b.box_ll.v_y, b.box_ur.v_x, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(o.oct_lx, b.box_ur.v_y, o.oct_rx, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[1] = curr_oct.normalize();
         }
      b = boxes[0];
      o = octagons[1];
      if (b.box_ur.v_x - b.box_ll.v_x > o.oct_uy - o.oct_ly)
         {
         // switch the horizontal lower left divide line to vertical
         boxes[0] = new ShapeTileBox(b.box_ll.v_x, o.oct_ly, b.box_ur.v_x, b.box_ur.v_y);
         curr_oct = new ShapeTileOctagon(b.box_ur.v_x, o.oct_ly, o.oct_rx, o.oct_uy, o.oct_ulx, o.oct_lrx, o.oct_llx, o.oct_urx);
         octagons[1] = curr_oct.normalize();
         }

      ShapeTileOctagon[] result = new ShapeTileOctagon[8];

      // add the 4 boxes to the result
      for (int i = 0; i < 4; ++i)
         {
         result[i] = boxes[i].to_IntOctagon();
         }

      // add the 4 octagons to the result
      for (int i = 0; i < 4; ++i)
         {
         result[4 + i] = octagons[i];
         }
      return result;
      }

   /**
    * Divide p_divide_octagon minus cut_octagon into 8 convex pieces without sharp angles.
    */
   @Override
   ShapeTileOctagon[] cutout_from(ShapeTileOctagon p_d)
      {
      ShapeTileOctagon c = this.intersection(p_d);

      if ( is_empty() || c.dimension().less(dimension()) )
         {
         // there is only an overlap at the border
         ShapeTileOctagon[] result = new ShapeTileOctagon[1];
         result[0] = p_d;
         return result;
         }

      ShapeTileOctagon[] result = new ShapeTileOctagon[8];

      int tmp = c.oct_llx - c.oct_lx;

      result[0] = new ShapeTileOctagon(p_d.oct_lx, tmp, c.oct_lx, c.oct_lx - c.oct_ulx, p_d.oct_ulx, p_d.oct_lrx, p_d.oct_llx, p_d.oct_urx);

      int tmp2 = c.oct_llx - c.oct_ly;

      result[1] = new ShapeTileOctagon(p_d.oct_lx, p_d.oct_ly, tmp2, tmp, p_d.oct_ulx, p_d.oct_lrx, p_d.oct_llx, c.oct_llx);

      tmp = c.oct_lrx + c.oct_ly;

      result[2] = new ShapeTileOctagon(tmp2, p_d.oct_ly, tmp, c.oct_ly, p_d.oct_ulx, p_d.oct_lrx, p_d.oct_llx, p_d.oct_urx);

      tmp2 = c.oct_rx - c.oct_lrx;

      result[3] = new ShapeTileOctagon(tmp, p_d.oct_ly, p_d.oct_rx, tmp2, c.oct_lrx, p_d.oct_lrx, p_d.oct_llx, p_d.oct_urx);

      tmp = c.oct_urx - c.oct_rx;

      result[4] = new ShapeTileOctagon(c.oct_rx, tmp2, p_d.oct_rx, tmp, p_d.oct_ulx, p_d.oct_lrx, p_d.oct_llx, p_d.oct_urx);

      tmp2 = c.oct_urx - c.oct_uy;

      result[5] = new ShapeTileOctagon(tmp2, tmp, p_d.oct_rx, p_d.oct_uy, p_d.oct_ulx, p_d.oct_lrx, c.oct_urx, p_d.oct_urx);

      tmp = c.oct_ulx + c.oct_uy;

      result[6] = new ShapeTileOctagon(tmp, c.oct_uy, tmp2, p_d.oct_uy, p_d.oct_ulx, p_d.oct_lrx, p_d.oct_llx, p_d.oct_urx);

      tmp2 = c.oct_lx - c.oct_ulx;

      result[7] = new ShapeTileOctagon(p_d.oct_lx, tmp2, tmp, p_d.oct_uy, p_d.oct_ulx, c.oct_ulx, p_d.oct_llx, p_d.oct_urx);

      for (int i = 0; i < 8; ++i)
         {
         result[i] = result[i].normalize();
         }

      ShapeTileOctagon curr_1 = result[0];
      ShapeTileOctagon curr_2 = result[7];

      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_1.oct_rx - curr_1.left_x_value(curr_1.oct_uy) > curr_2.upper_y_value(curr_1.oct_rx) - curr_2.oct_ly)
         {
         // switch the horizontal upper left divide line to vertical
         curr_1 = new ShapeTileOctagon(Math.min(curr_1.oct_lx, curr_2.oct_lx), curr_1.oct_ly, curr_1.oct_rx, curr_2.oct_uy, curr_2.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_2.oct_urx);

         curr_2 = new ShapeTileOctagon(curr_1.oct_rx, curr_2.oct_ly, curr_2.oct_rx, curr_2.oct_uy, curr_2.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_2.oct_urx);

         result[0] = curr_1.normalize();
         result[7] = curr_2.normalize();
         }
      curr_1 = result[7];
      curr_2 = result[6];
      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_2.upper_y_value(curr_1.oct_rx) - curr_2.oct_ly > curr_1.oct_rx - curr_1.left_x_value(curr_2.oct_ly))
      // switch the vertical upper left divide line to horizontal
         {
         curr_2 = new ShapeTileOctagon(curr_1.oct_lx, curr_2.oct_ly, curr_2.oct_rx, Math.max(curr_2.oct_uy, curr_1.oct_uy), curr_1.oct_ulx, curr_2.oct_lrx, curr_1.oct_llx, curr_2.oct_urx);

         curr_1 = new ShapeTileOctagon(curr_1.oct_lx, curr_1.oct_ly, curr_1.oct_rx, curr_2.oct_ly, curr_1.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_1.oct_urx);

         result[7] = curr_1.normalize();
         result[6] = curr_2.normalize();
         }
      curr_1 = result[6];
      curr_2 = result[5];
      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_2.upper_y_value(curr_1.oct_rx) - curr_1.oct_ly > curr_2.right_x_value(curr_1.oct_ly) - curr_2.oct_lx)
      // switch the vertical upper right divide line to horizontal
         {
         curr_1 = new ShapeTileOctagon(curr_1.oct_lx, curr_1.oct_ly, curr_2.oct_rx, Math.max(curr_2.oct_uy, curr_1.oct_uy), curr_1.oct_ulx, curr_2.oct_lrx, curr_1.oct_llx, curr_2.oct_urx);

         curr_2 = new ShapeTileOctagon(curr_2.oct_lx, curr_2.oct_ly, curr_2.oct_rx, curr_1.oct_ly, curr_2.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_2.oct_urx);

         result[6] = curr_1.normalize();
         result[5] = curr_2.normalize();
         }
      curr_1 = result[5];
      curr_2 = result[4];
      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_2.right_x_value(curr_2.oct_uy) - curr_2.oct_lx > curr_1.upper_y_value(curr_2.oct_lx) - curr_2.oct_uy)
      // switch the horizontal upper right divide line to vertical
         {
         curr_2 = new ShapeTileOctagon(curr_2.oct_lx, curr_2.oct_ly, Math.max(curr_2.oct_rx, curr_1.oct_rx), curr_1.oct_uy, curr_1.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_1.oct_urx);

         curr_1 = new ShapeTileOctagon(curr_1.oct_lx, curr_1.oct_ly, curr_2.oct_lx, curr_1.oct_uy, curr_1.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_1.oct_urx);

         result[5] = curr_1.normalize();
         result[4] = curr_2.normalize();
         }
      curr_1 = result[4];
      curr_2 = result[3];
      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_1.right_x_value(curr_1.oct_ly) - curr_1.oct_lx > curr_1.oct_ly - curr_2.lower_y_value(curr_1.oct_lx))
      // switch the horizontal lower right divide line to vertical
         {
         curr_1 = new ShapeTileOctagon(curr_1.oct_lx, curr_2.oct_ly, Math.max(curr_2.oct_rx, curr_1.oct_rx), curr_1.oct_uy, curr_1.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_1.oct_urx);

         curr_2 = new ShapeTileOctagon(curr_2.oct_lx, curr_2.oct_ly, curr_1.oct_lx, curr_2.oct_uy, curr_2.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_2.oct_urx);

         result[4] = curr_1.normalize();
         result[3] = curr_2.normalize();
         }

      curr_1 = result[3];
      curr_2 = result[2];

      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_2.oct_uy - curr_2.lower_y_value(curr_2.oct_rx) > curr_1.right_x_value(curr_2.oct_uy) - curr_2.oct_rx)
      // switch the vertical lower right divide line to horizontal
         {
         curr_2 = new ShapeTileOctagon(curr_2.oct_lx, Math.min(curr_1.oct_ly, curr_2.oct_ly), curr_1.oct_rx, curr_2.oct_uy, curr_2.oct_ulx, curr_1.oct_lrx, curr_2.oct_llx, curr_1.oct_urx);

         curr_1 = new ShapeTileOctagon(curr_1.oct_lx, curr_2.oct_uy, curr_1.oct_rx, curr_1.oct_uy, curr_1.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_1.oct_urx);

         result[3] = curr_1.normalize();
         result[2] = curr_2.normalize();
         }

      curr_1 = result[2];
      curr_2 = result[1];

      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_1.oct_uy - curr_1.lower_y_value(curr_1.oct_lx) > curr_1.oct_lx - curr_2.left_x_value(curr_1.oct_uy))
      // switch the vertical lower left divide line to horizontal
         {
         curr_1 = new ShapeTileOctagon(curr_2.oct_lx, Math.min(curr_1.oct_ly, curr_2.oct_ly), curr_1.oct_rx, curr_1.oct_uy, curr_2.oct_ulx, curr_1.oct_lrx, curr_2.oct_llx, curr_1.oct_urx);

         curr_2 = new ShapeTileOctagon(curr_2.oct_lx, curr_1.oct_uy, curr_2.oct_rx, curr_2.oct_uy, curr_2.oct_ulx, curr_2.oct_lrx, curr_2.oct_llx, curr_2.oct_urx);

         result[2] = curr_1.normalize();
         result[1] = curr_2.normalize();
         }

      curr_1 = result[1];
      curr_2 = result[0];

      if (!(curr_1.is_empty() || curr_2.is_empty()) && curr_2.oct_rx - curr_2.left_x_value(curr_2.oct_ly) > curr_2.oct_ly - curr_1.lower_y_value(curr_2.oct_rx))
      // switch the horizontal lower left divide line to vertical
         {
         curr_2 = new ShapeTileOctagon(Math.min(curr_2.oct_lx, curr_1.oct_lx), curr_1.oct_ly, curr_2.oct_rx, curr_2.oct_uy, curr_2.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_2.oct_urx);

         curr_1 = new ShapeTileOctagon(curr_2.oct_rx, curr_1.oct_ly, curr_1.oct_rx, curr_1.oct_uy, curr_1.oct_ulx, curr_1.oct_lrx, curr_1.oct_llx, curr_1.oct_urx);

         result[1] = curr_1.normalize();
         result[0] = curr_2.normalize();
         }

      return result;
      }

   @Override
   ShapeTileSimplex[] cutout_from(ShapeTileSimplex p_simplex)
      {
      return this.to_Simplex().cutout_from(p_simplex);
      }

   }