package board.shape;

import freert.planar.ShapeTile;

/**
 * Interface, which must be implemented by objects to be stored in a ShapeTree.
 */
public interface ShapeTreeStorable extends Comparable<Object>
   {
   /**
    * @return the number of shapes of an object to store in p_shape_tree
    */
   int tree_shape_count(ShapeSearchTree p_shape_tree);

   /**
    * @return the Shape of this object with index p_index stored in the ShapeTree with index identification number p_tree_id_no
    */
   ShapeTile get_tree_shape(ShapeSearchTree p_tree, int p_index);

   /**
    * Stores the entries in the ShapeTrees of this object for better performance while for example deleting tree entries. 
    * Called only by insert methods of class ShapeTree.
    */
   void set_search_tree_entries(ShapeTreeLeaf[] p_entries, ShapeSearchTree p_tree);
   }




