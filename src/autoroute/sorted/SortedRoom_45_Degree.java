package autoroute.sorted;

import planar.ShapeTileOctagon;


/**
 * Helper class to sort the doors of an expansion room counterclockwise arount the border of the room shape.
 */

public class SortedRoom_45_Degree implements Comparable<SortedRoom_45_Degree>
   {
   private final ShapeTileOctagon room_shape;
   private final boolean[] edge_interiour_touches_obstacle;

   // The intersection of tnis ExpansionRoom shape with the neighbour_shape
   public final ShapeTileOctagon intersection;
   // The first side of the room shape, where the neighbour_shape touches
   public final int first_touching_side;
   // The last side of the room shape, where the neighbour_shape touches
   public final int last_touching_side;

   /**
    * Creates a new instance of SortedRoomNeighbour and calculates the first and last touching sides with the room shape.
    * this.last_touching_side will be -1, if sorting did not work because the room_shape is contained in the neighbour shape.
    */
   public SortedRoom_45_Degree(ShapeTileOctagon p_room_shape, boolean []p_eito, ShapeTileOctagon p_intersection)
      {
      room_shape = p_room_shape;
      edge_interiour_touches_obstacle = p_eito;  
      intersection = p_intersection;

      if (intersection.oct_ly == room_shape.oct_ly && intersection.oct_llx > room_shape.oct_llx)
         {
         this.first_touching_side = 0;
         }
      else if (intersection.oct_lrx == room_shape.oct_lrx && intersection.oct_ly > room_shape.oct_ly)
         {
         this.first_touching_side = 1;
         }
      else if (intersection.oct_rx == room_shape.oct_rx && intersection.oct_lrx < room_shape.oct_lrx)
         {
         this.first_touching_side = 2;
         }
      else if (intersection.oct_urx == room_shape.oct_urx && intersection.oct_rx < room_shape.oct_rx)
         {
         this.first_touching_side = 3;
         }
      else if (intersection.oct_uy == room_shape.oct_uy && intersection.oct_urx < room_shape.oct_urx)
         {
         this.first_touching_side = 4;
         }
      else if (intersection.oct_ulx == room_shape.oct_ulx && intersection.oct_uy < room_shape.oct_uy)
         {
         this.first_touching_side = 5;
         }
      else if (intersection.oct_lx == room_shape.oct_lx && intersection.oct_ulx > room_shape.oct_ulx)
         {
         this.first_touching_side = 6;
         }
      else if (intersection.oct_llx == room_shape.oct_llx && intersection.oct_lx > room_shape.oct_lx)
         {
         this.first_touching_side = 7;
         }
      else
         {
         // the room_shape may be contained in the neighbour_shape
         this.first_touching_side = -1;
         this.last_touching_side = -1;
         return;
         }

      if (intersection.oct_llx == room_shape.oct_llx && intersection.oct_ly > room_shape.oct_ly)
         {
         this.last_touching_side = 7;
         }
      else if (intersection.oct_lx == room_shape.oct_lx && intersection.oct_llx > room_shape.oct_llx)
         {
         this.last_touching_side = 6;
         }
      else if (intersection.oct_ulx == room_shape.oct_ulx && intersection.oct_lx > room_shape.oct_lx)
         {
         this.last_touching_side = 5;
         }
      else if (intersection.oct_uy == room_shape.oct_uy && intersection.oct_ulx > room_shape.oct_ulx)
         {
         this.last_touching_side = 4;
         }
      else if (intersection.oct_urx == room_shape.oct_urx && intersection.oct_uy < room_shape.oct_uy)
         {
         this.last_touching_side = 3;
         }
      else if (intersection.oct_rx == room_shape.oct_rx && intersection.oct_urx < room_shape.oct_urx)
         {
         this.last_touching_side = 2;
         }
      else if (intersection.oct_lrx == room_shape.oct_lrx && intersection.oct_rx < room_shape.oct_rx)
         {
         this.last_touching_side = 1;
         }
      else if (intersection.oct_ly == room_shape.oct_ly && intersection.oct_lrx < room_shape.oct_lrx)
         {
         this.last_touching_side = 0;
         }
      else
         {
         // the room_shape may be contained in the neighbour_shape
         this.last_touching_side = -1;
         return;
         }

      int next_side_no = this.first_touching_side;
      for (;;)
         {
         int curr_side_no = next_side_no;
         next_side_no = (next_side_no + 1) % 8;
         if (!edge_interiour_touches_obstacle[curr_side_no])
            {
            boolean touch_only_at_corner = false;
            if (curr_side_no == this.first_touching_side)
               {
               if (intersection.corner(curr_side_no).equals(room_shape.corner(next_side_no)))
                  {
                  touch_only_at_corner = true;
                  }
               }
            if (curr_side_no == this.last_touching_side)
               {
               if (intersection.corner(next_side_no).equals(room_shape.corner(curr_side_no)))
                  {
                  touch_only_at_corner = true;
                  }
               }
            if (!touch_only_at_corner)
               {
               edge_interiour_touches_obstacle[curr_side_no] = true;
               }
            }
         if (curr_side_no == this.last_touching_side)
            {
            break;
            }

         }
      }

   /**
    * Compare function for or sorting the neighbours in counterclock sense around the border of the room shape in ascending
    * order.
    */
   public int compareTo(SortedRoom_45_Degree p_other)
      {
      if (this.first_touching_side > p_other.first_touching_side)
         {
         return 1;
         }
      if (this.first_touching_side < p_other.first_touching_side)
         {
         return -1;
         }

      // now the first touch of this and p_other is at the same side
      ShapeTileOctagon is1 = this.intersection;
      ShapeTileOctagon is2 = p_other.intersection;
      int cmp_value;

      if (first_touching_side == 0)
         {
         cmp_value = is1.corner(0).v_x - is2.corner(0).v_x;
         }
      else if (first_touching_side == 1)
         {
         cmp_value = is1.corner(1).v_x - is2.corner(1).v_x;
         }
      else if (first_touching_side == 2)
         {
         cmp_value = is1.corner(2).v_y - is2.corner(2).v_y;
         }
      else if (first_touching_side == 3)
         {
         cmp_value = is1.corner(3).v_y - is2.corner(3).v_y;
         }
      else if (first_touching_side == 4)
         {
         cmp_value = is2.corner(4).v_x - is1.corner(4).v_x;
         }
      else if (first_touching_side == 5)
         {
         cmp_value = is2.corner(5).v_x - is1.corner(5).v_x;
         }
      else if (first_touching_side == 6)
         {
         cmp_value = is2.corner(6).v_y - is1.corner(6).v_y;
         }
      else if (first_touching_side == 7)
         {
         cmp_value = is2.corner(7).v_y - is1.corner(7).v_y;
         }
      else
         {
         System.out.println("SortedRoomNeighbour.compareTo: first_touching_side out of range ");
         return 0;
         }

      if (cmp_value == 0)
         {
         // The first touching points of this neighbour and p_other with the room shape are equal.
         // Compare the last touching points.
         int this_touching_side_diff = (this.last_touching_side - this.first_touching_side + 8) % 8;
         int other_touching_side_diff = (p_other.last_touching_side - p_other.first_touching_side + 8) % 8;
         if (this_touching_side_diff > other_touching_side_diff)
            {
            return 1;
            }
         if (this_touching_side_diff < other_touching_side_diff)
            {
            return -1;
            }
         // now the last touch of this and p_other is at the same side
         if (last_touching_side == 0)
            {
            cmp_value = is1.corner(1).v_x - is2.corner(1).v_x;
            }
         else if (last_touching_side == 1)
            {
            cmp_value = is1.corner(2).v_x - is2.corner(2).v_x;
            }
         else if (last_touching_side == 2)
            {
            cmp_value = is1.corner(3).v_y - is2.corner(3).v_y;
            }
         else if (last_touching_side == 3)
            {
            cmp_value = is1.corner(4).v_y - is2.corner(4).v_y;
            }
         else if (last_touching_side == 4)
            {
            cmp_value = is2.corner(5).v_x - is1.corner(5).v_x;
            }
         else if (last_touching_side == 5)
            {
            cmp_value = is2.corner(6).v_x - is1.corner(6).v_x;
            }
         else if (last_touching_side == 6)
            {
            cmp_value = is2.corner(7).v_y - is1.corner(7).v_y;
            }
         else if (last_touching_side == 7)
            {
            cmp_value = is2.corner(0).v_y - is1.corner(0).v_y;
            }
         }
      return cmp_value;
      }
   }
