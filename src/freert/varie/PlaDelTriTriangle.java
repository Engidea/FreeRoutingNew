package freert.varie;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import freert.planar.PlaSide;
import board.varie.IdGenerator;

/**
 * Describes a triangle in the triagulation. 
 * edge_lines is an array of dimension 3
 * The edge lines are sorted in counter clock sense around the border of this triangle. 
 * The list children points to the children of this triangle, when used as a node in the search graph.
 */
public final class PlaDelTriTriangle
   {
   private final IdGenerator id_generator;
   
   // The 3 edge lines of this triangle sorted in counter clock sense around the border
   final PlaDelTriEdge[] edge_lines;

   /**
    * Indicates, if this triangle is on the left of the i-th edge line for i = 0 to 2. Must be set, if this triagngle is an inner
    * node because left_triangle and right_triangle of edge lines point only to leaf nodes.
    */
   private boolean[] is_on_the_left_of_edge_line = null;

   // The children of this triangle when used as a node in the triangle search graph
   final Collection<PlaDelTriTriangle> children = new LinkedList<PlaDelTriTriangle>();

   /**
    * Triangles resulting from an edge flip have 2 parents, all other triangles have 1 parent. first parent is used when
    * traversing the graph sequentially to avoid visiting children nodes more than once.
    */
   private final PlaDelTriTriangle first_parent;
   
   public PlaDelTriTriangle(IdGenerator p_id_generator, PlaDelTriEdge[] p_edge_lines, PlaDelTriTriangle p_first_parent)
      {
      id_generator = p_id_generator;
      edge_lines = p_edge_lines;
      first_parent = p_first_parent;
      }

   /**
    * Returns true, if this triangle node is a leaf, and false, if it is an inner node.
    */
   public boolean is_leaf()
      {
      return children.isEmpty();
      }

   /**
    * Gets the corner with index p_no.
    */
   public PlaDelTriCorner get_corner(int p_no)
      {
      if (p_no < 0 || p_no >= 3)
         {
         System.out.println("Triangle.get_corner: p_no out of range");
         return null;
         }
      
      PlaDelTriEdge curr_edge = edge_lines[p_no];
      
      if (curr_edge.left_triangle == this)
         return curr_edge.start_corner;

      if (curr_edge.right_triangle == this)
         return curr_edge.end_corner;

      System.out.println("Triangle.get_corner: inconsistant edge lines");

      return null;
      }

   /**
    * Calculates the opposite corner of this triangle to p_edge_line. 
    * @return null, if p_edge_line is nor an edge line of this triangle.
    */
   public PlaDelTriCorner opposite_corner(PlaDelTriEdge p_edge_line)
      {
      int edge_line_no = -1;
      
      for (int index = 0; index < 3; ++index)
         {
         if (edge_lines[index] == p_edge_line)
            {
            edge_line_no = index;
            break;
            }
         }
      
      
      if (edge_line_no < 0)
         {
         System.out.println("Triangle.opposite_corner: p_edge_line not found");
         return null;
         }
      
      PlaDelTriEdge next_edge = edge_lines[(edge_line_no + 1) % 3];

      if (next_edge.left_triangle == this)
         return next_edge.end_corner;
      else
         return next_edge.start_corner;
      }

   /**
    * Checks if p_point is inside or on the border of this triangle.
    */
   boolean contains(PlaDelTriCorner p_corner)
      {
      if (is_on_the_left_of_edge_line == null)
         {
         System.out.println("Triangle.contains: array is_on_the_left_of_edge_line not initialized");
         return false;
         }
      
      for (int index = 0; index < 3; ++index)
         {
         PlaDelTriEdge curr_edge = edge_lines[index];
         PlaSide curr_side = p_corner.side_of(curr_edge.start_corner, curr_edge.end_corner);
         if (is_on_the_left_of_edge_line[index])
            {
            // checking curr_edge.left_triangle == this instead will not work, if this triangle is an inner node.
            if (curr_side == PlaSide.ON_THE_RIGHT)
               {
               return false;
               }
            }
         else
            {
            if (curr_side == PlaSide.ON_THE_LEFT)
               {
               return false;
               }
            }
         }
      return true;
      }

   /**
    * Puts the edges of all leafs below this node into the list p_result_edges
    */
   public void get_leaf_edges(Set<PlaDelTriEdge> p_result_edges)
      {
      if (is_leaf())
         {
         for (int i = 0; i < 3; ++i)
            {
            PlaDelTriEdge curr_edge = edge_lines[i];
            if (curr_edge.start_corner.object != null && curr_edge.end_corner.object != null)
               {
               // Skip edges containing a bounding corner.
               p_result_edges.add(curr_edge);
               }
            }
         }
      else
         {
         for (PlaDelTriTriangle curr_child : children)
            {
            if (curr_child.first_parent == this) // to prevent traversing nodes more than once
               {
               curr_child.get_leaf_edges(p_result_edges);
               }
            }
         }
      }

   /**
    * Split this triangle into 3 new triangles by adding edges from the corners of this triangle to p_corner, p_corner has to be
    * located in the interiour of this triangle.
    */

   public PlaDelTriTriangle[] split_at_inner_point(PlaDelTriCorner p_corner)
      {
      PlaDelTriTriangle[] new_triangles = new PlaDelTriTriangle[3];

      PlaDelTriEdge[] new_edges = new PlaDelTriEdge[3];
      for (int i = 0; i < 3; ++i)
         {
         new_edges[i] = new PlaDelTriEdge(id_generator, get_corner(i), p_corner);
         }

      // construct the 3 new triangles.
      PlaDelTriEdge[] curr_edge_lines = new PlaDelTriEdge[3];

      curr_edge_lines[0] = edge_lines[0];
      curr_edge_lines[1] = new PlaDelTriEdge(id_generator, get_corner(1), p_corner);
      curr_edge_lines[2] = new PlaDelTriEdge(id_generator, p_corner, get_corner(0));
      new_triangles[0] = new PlaDelTriTriangle(id_generator, curr_edge_lines, this);

      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = edge_lines[1];
      curr_edge_lines[1] = new PlaDelTriEdge(id_generator, get_corner(2), p_corner);
      curr_edge_lines[2] = new_triangles[0].edge_lines[1];
      new_triangles[1] = new PlaDelTriTriangle(id_generator, curr_edge_lines, this);

      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = edge_lines[2];
      curr_edge_lines[1] = new_triangles[0].edge_lines[2];
      curr_edge_lines[2] = new_triangles[1].edge_lines[1];
      new_triangles[2] = new PlaDelTriTriangle(id_generator, curr_edge_lines, this);

      // Set the new neigbour triangles of the edge lines.
      for (int index = 0; index < 3; ++index)
         {
         PlaDelTriEdge curr_edge = new_triangles[index].edge_lines[0];
         if (curr_edge.left_triangle == this)
            {
            curr_edge.set_left_triangle(new_triangles[index]);
            }
         else
            {
            curr_edge.set_right_triangle(new_triangles[index]);
            }
         // The other neighbour triangle remains valid.
         }

      PlaDelTriEdge curr_edge = new_triangles[0].edge_lines[1];
      curr_edge.set_left_triangle(new_triangles[0]);
      curr_edge.set_right_triangle(new_triangles[1]);

      curr_edge = new_triangles[1].edge_lines[1];
      curr_edge.set_left_triangle(new_triangles[1]);
      curr_edge.set_right_triangle(new_triangles[2]);

      curr_edge = new_triangles[2].edge_lines[1];
      curr_edge.set_left_triangle(new_triangles[0]);
      curr_edge.set_right_triangle(new_triangles[2]);
      return new_triangles;
      }

   /**
    * Split this triangle and p_neighbour_to_split into 4 new triangles by adding edges from the corners of the triangles to
    * p_corner. p_corner is assumed to be loacated on the common edge line of this triangle and p_neigbour_to_split. If that is
    * not true, the function returns null. The first 2 result triangles are from splitting this triangle, and the last 2 result
    * triangles are from splitting p_neighbour_to_split.
    */
   public PlaDelTriTriangle[] split_at_border_point(PlaDelTriCorner p_corner, PlaDelTriTriangle p_neighbour_to_split)
      {
      PlaDelTriTriangle[] new_triangles = new PlaDelTriTriangle[4];
      // look for the triangle edge of this and the neighbour triangle containing p_point;
      int this_touching_edge_no = -1;
      int neigbbour_touching_edge_no = -1;
      PlaDelTriEdge touching_edge = null;
      PlaDelTriEdge other_touching_edge = null;
      for (int index = 0; index < 3; ++index)
         {
         PlaDelTriEdge curr_edge = edge_lines[index];
         if (p_corner.side_of(curr_edge.start_corner, curr_edge.end_corner) == PlaSide.COLLINEAR)
            {
            this_touching_edge_no = index;
            touching_edge = curr_edge;
            }
         curr_edge = p_neighbour_to_split.edge_lines[index];
         if (p_corner.side_of(curr_edge.start_corner, curr_edge.end_corner) == PlaSide.COLLINEAR)
            {
            neigbbour_touching_edge_no = index;
            other_touching_edge = curr_edge;
            }
         }
      if (this_touching_edge_no < 0 || neigbbour_touching_edge_no < 0)
         {
         System.out.println("Triangle.split_at_border_point: touching edge not found");
         return null;
         }
      if (touching_edge != other_touching_edge)
         {
         System.out.println("Triangle.split_at_border_point: edges inconsistent");
         return null;
         }

      PlaDelTriEdge first_common_new_edge;
      PlaDelTriEdge second_common_new_edge;
      // Construct the new edge lines that 2 split triangles of this triangle
      // will be on the left side of the new common touching edges.
      if (this == touching_edge.left_triangle)
         {
         first_common_new_edge = new PlaDelTriEdge(id_generator, touching_edge.start_corner, p_corner);
         second_common_new_edge = new PlaDelTriEdge(id_generator, p_corner, touching_edge.end_corner);
         }
      else
         {
         first_common_new_edge = new PlaDelTriEdge(id_generator, touching_edge.end_corner, p_corner);
         second_common_new_edge = new PlaDelTriEdge(id_generator, p_corner, touching_edge.start_corner);
         }

      // Construct the first split triangle of this triangle.

      PlaDelTriEdge prev_edge = edge_lines[(this_touching_edge_no + 2) % 3];
      PlaDelTriEdge this_splitting_edge;
      // construct the splitting edge line of this triangle, so that the first split
      // triangle lies on the left side, and the second split triangle on the right side.
      if (this == prev_edge.left_triangle)
         {
         this_splitting_edge = new PlaDelTriEdge(id_generator, p_corner, prev_edge.start_corner);
         }
      else
         {
         this_splitting_edge = new PlaDelTriEdge(id_generator, p_corner, prev_edge.end_corner);
         }
      PlaDelTriEdge[] curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = prev_edge;
      curr_edge_lines[1] = first_common_new_edge;
      curr_edge_lines[2] = this_splitting_edge;
      new_triangles[0] = new PlaDelTriTriangle(id_generator, curr_edge_lines, this);
      if (this == prev_edge.left_triangle)
         {
         prev_edge.set_left_triangle(new_triangles[0]);
         }
      else
         {
         prev_edge.set_right_triangle(new_triangles[0]);
         }
      first_common_new_edge.set_left_triangle(new_triangles[0]);
      this_splitting_edge.set_left_triangle(new_triangles[0]);

      // Construct the second split triangle of this triangle.

      PlaDelTriEdge next_edge = edge_lines[(this_touching_edge_no + 1) % 3];
      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = this_splitting_edge;
      curr_edge_lines[1] = second_common_new_edge;
      curr_edge_lines[2] = next_edge;
      new_triangles[1] = new PlaDelTriTriangle(id_generator, curr_edge_lines, this);
      this_splitting_edge.set_right_triangle(new_triangles[1]);
      second_common_new_edge.set_left_triangle(new_triangles[1]);
      if (this == next_edge.left_triangle)
         {
         next_edge.set_left_triangle(new_triangles[1]);
         }
      else
         {
         next_edge.set_right_triangle(new_triangles[1]);
         }

      // construct the first split triangle of p_neighbour_to_split
      next_edge = p_neighbour_to_split.edge_lines[(neigbbour_touching_edge_no + 1) % 3];
      PlaDelTriEdge neighbour_splitting_edge;
      // construct the splitting edge line of p_neighbour_to_split, so that the first split
      // triangle lies on the left side, and the second split triangle on the right side.
      if (p_neighbour_to_split == next_edge.left_triangle)
         {
         neighbour_splitting_edge = new PlaDelTriEdge(id_generator, next_edge.end_corner, p_corner);
         }
      else
         {
         neighbour_splitting_edge = new PlaDelTriEdge(id_generator, next_edge.start_corner, p_corner);
         }
      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = neighbour_splitting_edge;
      curr_edge_lines[1] = first_common_new_edge;
      curr_edge_lines[2] = next_edge;
      new_triangles[2] = new PlaDelTriTriangle(id_generator, curr_edge_lines, p_neighbour_to_split);
      neighbour_splitting_edge.set_left_triangle(new_triangles[2]);
      first_common_new_edge.set_right_triangle(new_triangles[2]);
      if (p_neighbour_to_split == next_edge.left_triangle)
         {
         next_edge.set_left_triangle(new_triangles[2]);
         }
      else
         {
         next_edge.set_right_triangle(new_triangles[2]);

         }

      // construct the second split triangle of p_neighbour_to_split
      prev_edge = p_neighbour_to_split.edge_lines[(neigbbour_touching_edge_no + 2) % 3];
      curr_edge_lines = new PlaDelTriEdge[3];
      curr_edge_lines[0] = prev_edge;
      curr_edge_lines[1] = second_common_new_edge;
      curr_edge_lines[2] = neighbour_splitting_edge;
      new_triangles[3] = new PlaDelTriTriangle(id_generator, curr_edge_lines, p_neighbour_to_split);
      if (p_neighbour_to_split == prev_edge.left_triangle)
         {
         prev_edge.set_left_triangle(new_triangles[3]);
         }
      else
         {
         prev_edge.set_right_triangle(new_triangles[3]);

         }
      second_common_new_edge.set_right_triangle(new_triangles[3]);
      neighbour_splitting_edge.set_right_triangle(new_triangles[3]);

      return new_triangles;
      }

   /**
    * Checks the consistency of this triangle and its children. Used for debugging purposes.
    */
   public boolean validate()
      {
      boolean result = true;
      if (is_leaf())
         {
         PlaDelTriEdge prev_edge = edge_lines[2];
         for (int i = 0; i < 3; ++i)
            {
            PlaDelTriEdge curr_edge = edge_lines[i];
            if (!curr_edge.validate())
               {
               result = false;
               }
            // Check, if the ens corner of the previous line equals to the start corner of this line.
            PlaDelTriCorner prev_end_corner;
            if (prev_edge.left_triangle == this)
               {
               prev_end_corner = prev_edge.end_corner;
               }
            else
               {
               prev_end_corner = prev_edge.start_corner;
               }
            PlaDelTriCorner curr_start_corner;
            if (curr_edge.left_triangle == this)
               {
               curr_start_corner = curr_edge.start_corner;
               }
            else if (curr_edge.right_triangle == this)
               {
               curr_start_corner = curr_edge.end_corner;
               }
            else
               {
               System.out.println("Triangle.validate: edge inconsistent");
               return false;
               }
            if (curr_start_corner != prev_end_corner)
               {
               System.out.println("Triangle.validate: corner inconsistent");
               result = false;
               }
            prev_edge = curr_edge;
            }
         }
      else
         {
         for (PlaDelTriTriangle curr_child : children)
            {
            if (curr_child.first_parent == this) // to avoid traversing nodes more than once.
               {
               curr_child.validate();
               }
            }
         }
      return result;
      }

   /**
    * Must be done as long as this triangle node is a leaf and after for all its edge lines the left_triangle or the
    * right_triangle reference is set to this triangle.
    */
   void initialize_is_on_the_left_of_edge_line_array()
      {
      if ( is_on_the_left_of_edge_line != null)
         {
         return; // already initialized
         }
      
      is_on_the_left_of_edge_line = new boolean[3];
      for (int i = 0; i < 3; ++i)
         {
         is_on_the_left_of_edge_line[i] = (edge_lines[i].left_triangle == this);
         }
      }

   }
