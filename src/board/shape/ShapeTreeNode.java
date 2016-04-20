package board.shape;

import planar.ShapeTileRegular;

/**
 * Common functionality of inner nodes and leaf nodes.
 */
public class ShapeTreeNode
   {
   public ShapeTreeNodeInner parent;
   public ShapeTileRegular bounding_shape;
   }
