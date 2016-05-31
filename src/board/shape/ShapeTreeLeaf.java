package board.shape;

import freert.planar.ShapeTileRegular;

/**
 * Description of a leaf of the Tree, where the geometric information is stored.
 */
public final class ShapeTreeLeaf extends ShapeTreeNode implements Comparable<ShapeTreeLeaf>
   {
   /// Actual object stored, may change if you keep the shape but change it... 
   public ShapeTreeObject object;
   // index of the shape in the object, it should really be final but it is not for special performance reuse... mah 
   public int shape_index_in_object;

   public ShapeTreeLeaf(ShapeTreeObject p_object, int p_index, ShapeTreeNodeInner p_parent, ShapeTileRegular p_bounding_shape)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      object = p_object;
      shape_index_in_object = p_index;
      }

   @Override
   public int compareTo(ShapeTreeLeaf p_other)
      {
      int result = object.compareTo(p_other.object);
      
      if (result != 0) return result;
      
      return shape_index_in_object - p_other.shape_index_in_object;
      }

   /** 
    * Returns the number of nodes between this leaf and the root of the tree. 
    */
   public int distance_to_root()
      {
      int result = 1;
      
      ShapeTreeNodeInner curr_parent = parent;

      while ( curr_parent.parent != null)
         {
         curr_parent = curr_parent.parent;
         result++;
         }
      
      return result;
      }
   }
