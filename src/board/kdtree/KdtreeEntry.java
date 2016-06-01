package board.kdtree;


/**
 * Information of a single object stored in a tree
 */
public final class KdtreeEntry
   {
   public final KdtreeObject object;
   public final int shape_index_in_object;

   public KdtreeEntry(KdtreeObject p_object, int p_shape_index_in_object)
      {
      object = p_object;
      shape_index_in_object = p_shape_index_in_object;
      }
   }
