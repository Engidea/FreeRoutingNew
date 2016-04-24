package freert.varie;


/**
 * Directed acyclic graph for finding the triangle containing a search point p. 
 * The leaves contain the trianngles of the current triangulation. 
 * The internal nodes are triangles, that were part of the triangulationn at some earlier stage, but have been replaced their children.
 */
public final class PlaDelTriTriangleGraph
   {
   PlaDelTriTriangle anchor = null;
   
   public PlaDelTriTriangleGraph(PlaDelTriTriangle p_triangle)
      {
      insert(p_triangle, null);
      }

   public void insert(PlaDelTriTriangle p_triangle, PlaDelTriTriangle p_parent)
      {
      if ( p_triangle == null ) return;
      
      p_triangle.initialize_is_on_the_left_of_edge_line_array();
      
      if (p_parent == null)
         {
         anchor = p_triangle;
         }
      else
         {
         p_parent.children.add(p_triangle);
         }
      }

   /**
    * Search for the leaf triangle containing p_corner. 
    * It will not be unique, if p_corner lies on a triangle edge.
    */
   public PlaDelTriTriangle position_locate(PlaDelTriCorner p_corner)
      {
      if (anchor == null)  return null;

      if (anchor.children.isEmpty()) return anchor;

      for (PlaDelTriTriangle curr_child : anchor.children)
         {
         PlaDelTriTriangle result = position_locate_recu(p_corner, curr_child);

         if (result != null) return result;
         }

      System.out.println("TriangleGraph.position_locate: containing triangle not found");
      
      return null;
      }

   /**
    * Recursive part of position_locate.
    */
   private PlaDelTriTriangle position_locate_recu(PlaDelTriCorner p_corner, PlaDelTriTriangle p_triangle)
      {
      if (!p_triangle.contains(p_corner)) return null;

      if (p_triangle.is_leaf()) return p_triangle;

      for (PlaDelTriTriangle curr_child : p_triangle.children)
         {
         PlaDelTriTriangle result = position_locate_recu(p_corner, curr_child);

         if (result != null) return result;
         }

      System.out.println("TriangleGraph.position_locate_reku: containing triangle not found");
      return null;
      }
   
   }
