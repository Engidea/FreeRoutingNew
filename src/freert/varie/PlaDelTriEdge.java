package freert.varie;

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

import board.varie.IdGenerator;


/**
 * Describes an edge between two triangles in the triangulation. The unique id_nos are for making edges comparable.
 */
public class PlaDelTriEdge implements Comparable<PlaDelTriEdge>
   {
   // The unique id number of this triangle
   private final int id_no;

   private final IdGenerator id_generator;
   
   public final PlaDelTriCorner start_corner;
   public final PlaDelTriCorner end_corner;

   // The triangle on the left side of this edge
   PlaDelTriTriangle left_triangle = null;
   // The triangle on the right side of this edge
   PlaDelTriTriangle right_triangle = null;
   
   
   public PlaDelTriEdge(IdGenerator p_id_generator, PlaDelTriCorner p_start_corner, PlaDelTriCorner p_end_corner)
      {
      start_corner = p_start_corner;
      end_corner = p_end_corner;
      id_generator = p_id_generator;
      id_no = id_generator.new_no();
      }

   public int compareTo(PlaDelTriEdge p_other)
      {
      return id_no - p_other.id_no;
      }

   public void set_left_triangle(PlaDelTriTriangle p_triangle)
      {
      left_triangle = p_triangle;
      }

   public void set_right_triangle(PlaDelTriTriangle p_triangle)
      {
      right_triangle = p_triangle;
      }

   /**
    * Returns the common corner of this edge and p_other, or null, if no commen corner exists.
    */
   public PlaDelTriCorner common_corner(PlaDelTriEdge p_other)
      {
      PlaDelTriCorner result = null;
      
      if (p_other.start_corner.equals(start_corner) || p_other.end_corner.equals(start_corner))
         {
         result = start_corner;
         }
      else if (p_other.start_corner.equals(this.end_corner) || p_other.end_corner.equals(end_corner))
         {
         result = end_corner;
         }
      
      return result;
      }

   /**
    * Returns the neighbour triangle of this edge, which is different from p_triangle. If p_triangle is not a neighbour of this
    * edge, null is returned.
    */
   public PlaDelTriTriangle other_neighbour(PlaDelTriTriangle p_triangle)
      {
      PlaDelTriTriangle result;
      if (p_triangle == left_triangle)
         {
         result = right_triangle;
         }
      else if (p_triangle == right_triangle)
         {
         result = left_triangle;
         }
      else
         {
         System.out.println("Edge.other_neighbour: inconsistant neigbour triangle");
         result = null;
         }
      
      return result;
      }

   /**
    * Returns true, if this is a legal edge of the Delaunay Triangulation.
    */
   public boolean is_legal()
      {
      if (left_triangle == null || right_triangle == null)
         {
         return true;
         }
      
      PlaDelTriCorner left_opposite_corner = left_triangle.opposite_corner(this);
      PlaDelTriCorner right_opposite_corner = right_triangle.opposite_corner(this);

      boolean inside_circle = right_opposite_corner.coor.to_float().inside_circle(this.start_corner.coor.to_float(), left_opposite_corner.coor.to_float(), this.end_corner.coor.to_float());
      return !inside_circle;
      }

   /**
    * Flips this edge line to the edge line between the opposite corners of the adjacent triangles. 
    * Returns the new constructed Edge.
    */
   public PlaDelTriEdge flip()
      {
      // Create the flipped edge, so that the start corner of this edge is on the left
      // and the end corner of this edge on the right.
      PlaDelTriEdge flipped_edge = new PlaDelTriEdge(id_generator, right_triangle.opposite_corner(this), left_triangle.opposite_corner(this));

      PlaDelTriTriangle first_parent = this.left_triangle;

      // Calculate the index of this edge line in the left and right adjacent triangles.

      int left_index = -1;
      int right_index = -1;
      for (int i = 0; i < 3; ++i)
         {
         if (left_triangle.edge_lines[i] == this)
            {
            left_index = i;
            }
         if (right_triangle.edge_lines[i] == this)
            {
            right_index = i;
            }
         }
      if (left_index < 0 || right_index < 0)
         {
         System.out.println("Edge.flip: edge line inconsistant");
         return null;
         }
      PlaDelTriEdge left_prev_edge = left_triangle.edge_lines[(left_index + 2) % 3];
      PlaDelTriEdge left_next_edge = left_triangle.edge_lines[(left_index + 1) % 3];
      PlaDelTriEdge right_prev_edge = right_triangle.edge_lines[(right_index + 2) % 3];
      PlaDelTriEdge right_next_edge = right_triangle.edge_lines[(right_index + 1) % 3];

      // Create the left triangle of the flipped edge.

      PlaDelTriEdge[] curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = flipped_edge;
      curr_edge_lines[1] = left_prev_edge;
      curr_edge_lines[2] = right_next_edge;
      PlaDelTriTriangle new_left_triangle = new PlaDelTriTriangle(id_generator, curr_edge_lines, first_parent);
      flipped_edge.left_triangle = new_left_triangle;
      if (left_prev_edge.left_triangle == this.left_triangle)
         {
         left_prev_edge.left_triangle = new_left_triangle;
         }
      else
         {
         left_prev_edge.right_triangle = new_left_triangle;
         }
      if (right_next_edge.left_triangle == this.right_triangle)
         {
         right_next_edge.left_triangle = new_left_triangle;
         }
      else
         {
         right_next_edge.right_triangle = new_left_triangle;
         }

      // Create the right triangle of the flipped edge.

      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = flipped_edge;
      curr_edge_lines[1] = right_prev_edge;
      curr_edge_lines[2] = left_next_edge;
      PlaDelTriTriangle new_right_triangle = new PlaDelTriTriangle(id_generator ,curr_edge_lines, first_parent);
      flipped_edge.right_triangle = new_right_triangle;
      if (right_prev_edge.left_triangle == this.right_triangle)
         {
         right_prev_edge.left_triangle = new_right_triangle;
         }
      else
         {
         right_prev_edge.right_triangle = new_right_triangle;
         }
      if (left_next_edge.left_triangle == this.left_triangle)
         {
         left_next_edge.left_triangle = new_right_triangle;
         }
      else
         {
         left_next_edge.right_triangle = new_right_triangle;
         }

      return flipped_edge;
      }

   /**
    * Checks the consistancy of this edge in its database. Used for debugging purposes.
    */
   public boolean validate()
      {
      boolean result = true;
      if (this.left_triangle == null)
         {
         if (this.start_corner.object != null || this.end_corner.object != null)
            {
            System.out.println("Edge.validate: left triangle may be null only for bounding edges");
            result = false;
            }
         }
      else
         {
         // check if the left triangle contains this edge
         boolean found = false;
         for (int i = 0; i < 3; ++i)
            {
            if (left_triangle.edge_lines[i] == this)
               {
               found = true;
               break;
               }
            }
         if (!found)
            {
            System.out.println("Edge.validate: left triangle does not contain this edge");
            result = false;
            }
         }
      if (this.right_triangle == null)
         {
         if (this.start_corner.object != null || this.end_corner.object != null)
            {
            System.out.println("Edge.validate: right triangle may be null only for bounding edges");
            result = false;
            }
         }
      else
         {
         // check if the left triangle contains this edge
         boolean found = false;
         for (int i = 0; i < 3; ++i)
            {
            if (right_triangle.edge_lines[i] == this)
               {
               found = true;
               break;
               }
            }
         if (!found)
            {
            System.out.println("Edge.validate: right triangle does not contain this edge");
            result = false;
            }
         }

      return result;
      }

   }
