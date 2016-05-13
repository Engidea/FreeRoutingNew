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
 *
 * Created on 8. Januar 2005, 10:12
 */

package freert.varie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import board.varie.IdGenerator;
import freert.planar.PlaLimits;
import freert.planar.PlaPointInt;
import freert.planar.PlaSide;

/**
 * Creates a Delaunay triangulation in the plane for the input objects. 
 * The objects in the input list must implement the interface PlanarDelaunayTriangulation.Storable
 * which consists of the the method get_triangulation_corners(). 
 * The result can be read by the function get_edge_lines(). 
 * The algorithm is from Chapter 9.3. of the book Computational Geometry, Algorithms and Applications
 * from M. de Berg, M. van Kreveld, M Overmars and O Schwarzkopf.
 * 
 * This is used in the ratsnest to connect the "dots" with the most efficient path
 * 
 * @author Alfons Wirtz
 */
public final class PlaDelTri
   {
   private static final int seed = 99;
   // Randum generatur to shuffle the input corners. A fixed seed is used to make the results reproduceble.
   private static Random random_generator = new Random(seed);

   // The structure for searching the triangle containing a given input corner.
   private final PlaDelTriTriangleGraph search_graph;
   // This list contain the edges of the trinangulation, where the start corner and end corner are equal.
   private Collection<PlaDelTriEdge> degenerate_edges;
   // id numbers are for implementing an ordering on the Edges so that they can be used in a set for example
   private final IdGenerator last_edge_id_no = new IdGenerator();
   
   public PlaDelTri(Collection<PlaDelTriStorable> p_object_list)
      {
      List<PlaDelTriCorner> corner_list = new LinkedList<PlaDelTriCorner>();
      for (PlaDelTriStorable curr_object : p_object_list)
         {
         ArrayList<PlaPointInt> curr_corners = curr_object.get_triangulation_corners();

         for (PlaPointInt curr_corner : curr_corners)
            {
            corner_list.add(new PlaDelTriCorner(curr_object, curr_corner));
            }
         }

      // create a random permutation of the corners. use a fixed seed to get reproducible result
      random_generator.setSeed(seed);
      Collections.shuffle(corner_list, random_generator);

      // create a big triangle containing all corners in the list to start with.

      int bounding_coor = PlaLimits.CRIT_INT;
      PlaDelTriCorner[] bounding_corners = new PlaDelTriCorner[3];
      bounding_corners[0] = new PlaDelTriCorner(null, new PlaPointInt(bounding_coor, 0));
      bounding_corners[1] = new PlaDelTriCorner(null, new PlaPointInt(0, bounding_coor));
      bounding_corners[2] = new PlaDelTriCorner(null, new PlaPointInt(-bounding_coor, -bounding_coor));

      PlaDelTriEdge[] edge_lines = new PlaDelTriEdge[3];
      for (int i = 0; i < 2; ++i)
         {
         edge_lines[i] = new PlaDelTriEdge(last_edge_id_no, bounding_corners[i], bounding_corners[i + 1]);
         }
      edge_lines[2] = new PlaDelTriEdge(last_edge_id_no, bounding_corners[2], bounding_corners[0]);

      PlaDelTriTriangle start_triangle = new PlaDelTriTriangle(last_edge_id_no, edge_lines, null);

      // Set the left triangle of the edge lines to start_triangle. The right triangles remains null.
      for (PlaDelTriEdge curr_edge : edge_lines)
         {
         curr_edge.set_left_triangle(start_triangle);
         }

      // Initialize the search graph.

      search_graph     = new PlaDelTriTriangleGraph(start_triangle);
      degenerate_edges = new LinkedList<PlaDelTriEdge>();

      // Insert the corners in the corner list into the search graph.

      for (PlaDelTriCorner curr_corner : corner_list)
         {
         PlaDelTriTriangle triangle_to_split = search_graph.position_locate(curr_corner);
         split(triangle_to_split, curr_corner);
         }
      }

   /**
    * Returns all edge lines of the result of the Delaunay Triangulation.
    */
   public Collection<PlaDelTriResultEdge> get_edge_lines()
      {
      Collection<PlaDelTriResultEdge> result = new LinkedList<PlaDelTriResultEdge>();

      for (PlaDelTriEdge curr_edge : degenerate_edges)
         {
         result.add(new PlaDelTriResultEdge(curr_edge.start_corner.coor, curr_edge.start_corner.object, curr_edge.end_corner.coor, curr_edge.end_corner.object));
         }
      
      if (search_graph.anchor != null)
         {
         Set<PlaDelTriEdge> result_edges = new TreeSet<PlaDelTriEdge>();
         
         search_graph.anchor.get_leaf_edges(result_edges);
         
         for (PlaDelTriEdge curr_edge : result_edges)
            {
            result.add(new PlaDelTriResultEdge(curr_edge.start_corner.coor, curr_edge.start_corner.object, curr_edge.end_corner.coor, curr_edge.end_corner.object));
            }
         }
      return result;
      }

   /**
    * Splits p_triangle into 3 new triangles at p_corner, if p_corner lies in the interiour. If p_corner lies on the border,
    * p_triangle and the corresponding neighbour are split into 2 new triangles each at p_corner. If p_corner lies outside this
    * triangle or on a corner, nothing is split. In this case the function returns false.
    */
   private boolean split(PlaDelTriTriangle p_triangle, PlaDelTriCorner p_corner)
      {
      // check, if p_corner is in the interiour of this triangle or if p_corner is contained in an edge line.

      PlaDelTriEdge containing_edge = null;
      for (int index = 0; index < 3; ++index)
         {
         PlaDelTriEdge curr_edge = p_triangle.edge_lines[index];
         PlaSide curr_side;
         if (curr_edge.left_triangle == p_triangle)
            {
            curr_side = p_corner.side_of(curr_edge.start_corner, curr_edge.end_corner);
            }
         else
            {
            curr_side = p_corner.side_of(curr_edge.end_corner, curr_edge.start_corner);
            }
         if (curr_side == PlaSide.ON_THE_RIGHT)
            {
            // p_corner is outside this triangle
            System.out.println("PlanarDelaunayTriangulation.split: p_corner is outside");
            return false;
            }
         
         else if (curr_side == PlaSide.COLLINEAR)
            {
            if (containing_edge != null)
               {
               // p_corner is equal to a corner of this triangle

               PlaDelTriCorner common_corner = curr_edge.common_corner(containing_edge);
               if (common_corner == null)
                  {
                  System.out.println("PlanarDelaunayTriangulation.split: common corner expected");
                  return false;
                  }
               if (p_corner.object == common_corner.object)
                  {
                  return false;
                  }
               degenerate_edges.add(new PlaDelTriEdge(last_edge_id_no,p_corner, common_corner));
               return true;
               }
            containing_edge = curr_edge;
            }
         }

      if (containing_edge == null)
         {
         // split p_triangle into 3 new triangles by adding edges from the corners of p_triangle to p_corner.

         PlaDelTriTriangle[] new_triangles = p_triangle.split_at_inner_point(p_corner);

         if (new_triangles == null) return false;

         for (PlaDelTriTriangle curr_triangle : new_triangles)
            {
            search_graph.insert(curr_triangle, p_triangle);
            }

         for (int index = 0; index < 3; ++index)
            {
            legalize_edge(p_corner, p_triangle.edge_lines[index]);
            }
         }
      else
         {
         // split this triangle and the neighbour triangle into 4 new triangles by adding edges from
         // the corners of the triangles to p_corner.

         PlaDelTriTriangle neighbour_to_split = containing_edge.other_neighbour(p_triangle);

         PlaDelTriTriangle[] new_triangles = p_triangle.split_at_border_point(p_corner, neighbour_to_split);

         if (new_triangles == null) return false;

         // There are exact four new triangles with the first 2 dividing p_triangle and the last 2 dividing neighbour_to_split.
         search_graph.insert(new_triangles[0], p_triangle);
         search_graph.insert(new_triangles[1], p_triangle);
         search_graph.insert(new_triangles[2], neighbour_to_split);
         search_graph.insert(new_triangles[3], neighbour_to_split);

         for (int index = 0; index < 3; ++index)
            {
            PlaDelTriEdge curr_edge = p_triangle.edge_lines[index];
            if (curr_edge != containing_edge)
               {
               legalize_edge(p_corner, curr_edge);
               }
            }
         
         for (int index = 0; index < 3; ++index)
            {
            PlaDelTriEdge curr_edge = neighbour_to_split.edge_lines[index];
            if (curr_edge != containing_edge)
               {
               legalize_edge(p_corner, curr_edge);
               }
            }
         }
      return true;
      }

   /**
    * Flips p_edge, if it is no legal edge of the Delaunay Triangulation. 
    * p_corner is the last inserted corner of the triangulation
    * @return true, if the triangulation was changed.
    */
   private boolean legalize_edge(PlaDelTriCorner p_corner, PlaDelTriEdge p_edge)
      {
      if ( p_edge.is_legal() ) return false;

      PlaDelTriTriangle triangle_to_change;
      if (p_edge.left_triangle.opposite_corner(p_edge) == p_corner)
         {
         triangle_to_change = p_edge.right_triangle;
         }
      else if (p_edge.right_triangle.opposite_corner(p_edge) == p_corner)
         {
         triangle_to_change = p_edge.left_triangle;
         }
      else
         {
         System.out.println("PlanarDelaunayTriangulation.legalize_edge: edge lines inconsistant");
         return false;
         }
      
      PlaDelTriEdge flipped_edge = p_edge.flip();

      // Update the search graph.
      search_graph.insert(flipped_edge.left_triangle, p_edge.left_triangle);
      search_graph.insert(flipped_edge.right_triangle, p_edge.left_triangle);
      search_graph.insert(flipped_edge.left_triangle, p_edge.right_triangle);
      search_graph.insert(flipped_edge.right_triangle, p_edge.right_triangle);

      // Call this function recursively for the other edge lines of triangle_to_change.
      for (int index = 0; index < 3; ++index)
         {
         PlaDelTriEdge curr_edge = triangle_to_change.edge_lines[index];
         if (curr_edge != p_edge)
            {
            legalize_edge(p_corner, curr_edge);
            }
         }
      return true;
      }

   /**
    * Checks the consistancy of the triangles in this triagulation. Used for debugging purposes.
    */
   public boolean validate()
      {
      boolean result = search_graph.anchor.validate();
      if (result == true)
         {
         System.out.println("Delauny triangulation check passed ok");
         }
      else
         {
         System.out.println("Delauny triangulation check has detected problems");
         }
      return result;
      }
   }
