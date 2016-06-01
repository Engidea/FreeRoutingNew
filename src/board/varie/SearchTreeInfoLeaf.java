package board.varie;

import board.kdtree.KdtreeShapeSearch;
import board.kdtree.KdtreeNodeLeaf;
import freert.planar.ShapeTile;

public final class SearchTreeInfoLeaf
   {
   public final KdtreeShapeSearch tree;
   public KdtreeNodeLeaf[] entry_arr;
   public ShapeTile[] precalculated_tree_shapes;

   public SearchTreeInfoLeaf(KdtreeShapeSearch p_tree)
      {
      tree = p_tree;
      entry_arr = null;
      precalculated_tree_shapes = null;
      }
   }
