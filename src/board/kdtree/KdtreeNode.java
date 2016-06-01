package board.kdtree;

import freert.planar.ShapeTileRegular;

/**
 * Common functionality of inner nodes and leaf nodes
 * Should not be used by itself
 */
public abstract class KdtreeNode
   {
   public KdtreeNodeFork parent;
   public ShapeTileRegular bounding_shape;
   }
