package board.varie;

import planar.ShapeTile;
import board.shape.ShapeTree;
import board.shape.ShapeTreeLeaf;

public final class SearchTreeInfoLeaf
   {
   public final ShapeTree tree;
   public ShapeTreeLeaf[] entry_arr;
   public ShapeTile[] precalculated_tree_shapes;

   public SearchTreeInfoLeaf(ShapeTree p_tree)
      {
      tree = p_tree;
      entry_arr = null;
      precalculated_tree_shapes = null;
      }
   }
