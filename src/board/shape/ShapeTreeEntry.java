package board.shape;


/**
 * Information of a single object stored in a tree
 */
public final class ShapeTreeEntry
   {
   public final ShapeTreeObject object;
   public final int shape_index_in_object;

   public ShapeTreeEntry(ShapeTreeObject p_object, int p_shape_index_in_object)
      {
      object = p_object;
      shape_index_in_object = p_shape_index_in_object;
      }
   }
