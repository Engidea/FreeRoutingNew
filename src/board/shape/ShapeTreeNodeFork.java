package board.shape;

import freert.planar.ShapeTileRegular;

/**
 * Description of an inner node of the tree, which implements a fork to its two children.
 */
public final class ShapeTreeNodeFork extends ShapeTreeNode
   {
   public ShapeTreeNode first_child;
   public ShapeTreeNode second_child;

   public ShapeTreeNodeFork(ShapeTileRegular p_bounding_shape, ShapeTreeNodeFork p_parent)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      first_child = null;
      second_child = null;
      }
   }
