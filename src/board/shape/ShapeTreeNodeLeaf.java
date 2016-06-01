package board.shape;

import freert.planar.ShapeTileRegular;

/**
 * Description of a leaf of the Tree, where the geometric information is stored.
 */
public final class ShapeTreeNodeLeaf extends ShapeTreeNode implements Comparable<ShapeTreeNodeLeaf>
   {
   /// Actual object stored, may change if you keep the shape but change it... 
   public ShapeTreeObject object;
   // index of the shape in the object, it should really be final but it is not for special performance reuse... mah 
   public int shape_index_in_object;

   public ShapeTreeNodeLeaf(ShapeTreeObject p_object, int p_index, ShapeTreeNodeFork p_parent, ShapeTileRegular p_bounding_shape)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      object = p_object;
      shape_index_in_object = p_index;
      }

   @Override
   public int compareTo(ShapeTreeNodeLeaf p_other)
      {
      int result = object.compareTo(p_other.object);
      
      if (result != 0) return result;
      
      return shape_index_in_object - p_other.shape_index_in_object;
      }

   /** 
    * @return the number of nodes between this leaf and the root of the tree. 
    */
   public int distance_to_root()
      {
      int result = 1;
      
      ShapeTreeNodeFork curr_parent = parent;

      while ( curr_parent.parent != null)
         {
         curr_parent = curr_parent.parent;
         result++;
         }
      
      return result;
      }
   }
