package board.kdtree;

import freert.planar.ShapeTileRegular;

/**
 * Description of an inner node of the tree, which implements a fork to its two children.
 */
public final class KdtreeNodeFork extends KdtreeNode
   {
   public KdtreeNode first_child;
   public KdtreeNode second_child;

   public KdtreeNodeFork(ShapeTileRegular p_bounding_shape, KdtreeNodeFork p_parent)
      {
      bounding_shape = p_bounding_shape;
      parent = p_parent;
      first_child = null;
      second_child = null;
      }
   }
