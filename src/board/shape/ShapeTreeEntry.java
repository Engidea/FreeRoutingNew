package board.shape;


/**
 * Information of a single object stored in a tree
 */
public class ShapeTreeEntry
   {
   public final ShapeTreeStorable object;
   public final int shape_index_in_object;

   public ShapeTreeEntry(ShapeTreeStorable p_object, int p_shape_index_in_object)
      {
      object = p_object;
      shape_index_in_object = p_shape_index_in_object;
      }
   }
