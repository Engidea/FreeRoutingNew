package board.shape;

import planar.ShapeTile;

/**
 * Interface, which must be implemented by objects to be stored in a ShapeTree.
 */
public interface ShapeTreeStorable extends Comparable<Object>
   {
   /**
    * Number of shapes of an object to store in p_shape_tree
    */
   int tree_shape_count(ShapeTree p_shape_tree);

   /**
    * Get the Shape of this object with index p_index stored in the ShapeTree with index identification number p_tree_id_no
    */
   ShapeTile get_tree_shape(ShapeTree p_tree, int p_index);

   /**
    * Stores the entries in the ShapeTrees of this object for better performance while for example deleting tree entries. 
    * Called only by insert methods of class ShapeTree.
    */
   void set_search_tree_entries(ShapeTreeLeaf[] p_entries, ShapeTree p_tree);
   }




