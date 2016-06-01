package board.shape;

import freert.planar.ShapeTileRegular;

/**
 * Common functionality of inner nodes and leaf nodes
 * Should not be used by itself
 */
public abstract class ShapeTreeNode
   {
   public ShapeTreeNodeFork parent;
   public ShapeTileRegular bounding_shape;
   }
