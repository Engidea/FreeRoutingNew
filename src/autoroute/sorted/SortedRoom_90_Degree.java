package autoroute.sorted;

import planar.ShapeTileBox;


/**
 * Helper class to sort the doors of an expansion room counterclockwise arount the border of the room shape.
 */

public class SortedRoom_90_Degree implements Comparable<SortedRoom_90_Degree>
   {
   private final ShapeTileBox room_shape;
   private final boolean[] edge_interiour_touches_obstacle;

   /** The intersection of tnis ExpansionRoom shape with the neighbour_shape */
   public final ShapeTileBox intersection;

   /** The first side of the room shape, where the neighbour_shape touches */
   public final int first_touching_side;

   /** The last side of the room shape, where the neighbour_shape touches */
   public final int last_touching_side;
   
   public SortedRoom_90_Degree(ShapeTileBox p_room_shape, boolean [] p_eito,  ShapeTileBox p_intersection)
      {
      room_shape = p_room_shape;
      edge_interiour_touches_obstacle = p_eito;
      intersection = p_intersection;

      if (p_intersection.box_ll.v_y == room_shape.box_ll.v_y && p_intersection.box_ur.v_x > room_shape.box_ll.v_x && p_intersection.box_ll.v_x < room_shape.box_ur.v_x)
         {
         edge_interiour_touches_obstacle[0] = true;
         }
      if (p_intersection.box_ur.v_x == room_shape.box_ur.v_x && p_intersection.box_ur.v_y > room_shape.box_ll.v_y && p_intersection.box_ll.v_y < room_shape.box_ur.v_y)
         {
         edge_interiour_touches_obstacle[1] = true;
         }
      if (p_intersection.box_ur.v_y == room_shape.box_ur.v_y && p_intersection.box_ur.v_x > room_shape.box_ll.v_x && p_intersection.box_ll.v_x < room_shape.box_ur.v_x)
         {
         edge_interiour_touches_obstacle[2] = true;
         }
      if (p_intersection.box_ll.v_x == room_shape.box_ll.v_x && p_intersection.box_ur.v_y > room_shape.box_ll.v_y && p_intersection.box_ll.v_y < room_shape.box_ur.v_y)
         {
         edge_interiour_touches_obstacle[3] = true;
         }

      if (p_intersection.box_ll.v_y == room_shape.box_ll.v_y && p_intersection.box_ll.v_x > room_shape.box_ll.v_x)
         {
         this.first_touching_side = 0;
         }
      else if (p_intersection.box_ur.v_x == room_shape.box_ur.v_x && p_intersection.box_ll.v_y > room_shape.box_ll.v_y)
         {
         this.first_touching_side = 1;
         }
      else if (p_intersection.box_ur.v_y == room_shape.box_ur.v_y)
         {
         this.first_touching_side = 2;
         }
      else if (p_intersection.box_ll.v_x == room_shape.box_ll.v_x)
         {
         this.first_touching_side = 3;
         }
      else
         {
         System.out.println("SortedRoomNeighbour: case not expected");
         this.first_touching_side = -1;
         }

      if (p_intersection.box_ll.v_x == room_shape.box_ll.v_x && p_intersection.box_ll.v_y > room_shape.box_ll.v_y)
         {
         this.last_touching_side = 3;
         }
      else if (p_intersection.box_ur.v_y == room_shape.box_ur.v_y && p_intersection.box_ll.v_x > room_shape.box_ll.v_x)
         {
         this.last_touching_side = 2;
         }
      else if (p_intersection.box_ur.v_x == room_shape.box_ur.v_x)
         {
         this.last_touching_side = 1;
         }
      else if (p_intersection.box_ll.v_y == room_shape.box_ll.v_y)
         {
         this.last_touching_side = 0;
         }
      else
         {
         System.out.println("SortedRoomNeighbour: case not expected");
         this.last_touching_side = -1;
         }
      }

   /**
    * Compare function for or sorting the neighbours in counterclock sense around the border of the room shape in ascending
    * order.
    */
   public int compareTo(SortedRoom_90_Degree p_other)
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
      ShapeTileBox is1 = this.intersection;
      ShapeTileBox is2 = p_other.intersection;
      int cmp_value;

      if (first_touching_side == 0)
         {
         cmp_value = is1.box_ll.v_x - is2.box_ll.v_x;
         }
      else if (first_touching_side == 1)
         {
         cmp_value = is1.box_ll.v_y - is2.box_ll.v_y;
         }
      else if (first_touching_side == 2)
         {
         cmp_value = is2.box_ur.v_x - is1.box_ur.v_x;
         }
      else if (first_touching_side == 3)
         {
         cmp_value = is2.box_ur.v_y - is1.box_ur.v_y;
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
         int this_touching_side_diff = (this.last_touching_side - this.first_touching_side + 4) % 4;
         int other_touching_side_diff = (p_other.last_touching_side - p_other.first_touching_side + 4) % 4;
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
            cmp_value = is1.box_ur.v_x - is2.box_ur.v_x;
            }
         else if (last_touching_side == 1)
            {
            cmp_value = is1.box_ur.v_y - is2.box_ur.v_y;
            }
         else if (last_touching_side == 2)
            {
            cmp_value = is2.box_ll.v_x - is1.box_ll.v_x;
            }
         else if (last_touching_side == 3)
            {
            cmp_value = is2.box_ll.v_y - is1.box_ll.v_y;
            }
         else
            {
            System.out.println("SortedRoomNeighbour.compareTo: first_touching_side out of range ");
            return 0;
            }
         }
      return cmp_value;
      }
   }
