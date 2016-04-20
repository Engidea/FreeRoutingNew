package board.varie;

import board.items.BrdItem;

public final class BrdShoveObstacle
   {
   public BrdItem brd_item = null;
   public int on_layer = -1;

   public BrdShoveObstacle (  )
      {
      }

   public BrdShoveObstacle ( BrdItem p_item, int p_on_layer )
      {
      brd_item = p_item;
      on_layer = p_on_layer;
      }
   
   public void clear ()
      {
      brd_item = null;
      on_layer = -1;
      }
   }
