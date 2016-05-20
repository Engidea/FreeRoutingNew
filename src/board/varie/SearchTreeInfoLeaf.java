package board.varie;

import board.shape.ShapeTreeLeaf;
import board.shape.ShapeTreeMinArea;
import freert.planar.ShapeTile;

public final class SearchTreeInfoLeaf
   {
   public final ShapeTreeMinArea tree;
   public ShapeTreeLeaf[] entry_arr;
   public ShapeTile[] precalculated_tree_shapes;

   public SearchTreeInfoLeaf(ShapeTreeMinArea p_tree)
      {
      tree = p_tree;
      entry_arr = null;
      precalculated_tree_shapes = null;
      }
   }
