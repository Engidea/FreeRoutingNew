package planar;

/**
 * mutable octagon with double coordinates (see geometry.planar.IntOctagon)
 */
public final class OctagonMutable implements PlaObject
   {
   public double lx;
   public double ly;
   public double rx;
   public double uy;
   public double ulx;
   public double lrx;
   public double llx;
   public double urx;

   public OctagonMutable()
      {
      set_empty();
      }
   
   @Override
   public final boolean is_NaN ()
      {
      return false;
      }
   
   public void set_empty()
      {
      lx = Integer.MAX_VALUE;
      ly = Integer.MAX_VALUE;
      rx = Integer.MIN_VALUE;
      uy = Integer.MIN_VALUE;
      ulx = Integer.MAX_VALUE;
      lrx = Integer.MIN_VALUE;
      llx = Integer.MAX_VALUE;
      urx = Integer.MIN_VALUE;
      }

   /**
    * calculates the smallest IntOctagon containing this octagon.
    */
   public ShapeTileOctagon to_int()
      {
      if (rx < lx || uy < ly || lrx < ulx || urx < llx)
         {
         return ShapeTileOctagon.EMPTY;
         }
      
      return new ShapeTileOctagon((int) Math.floor(lx), (int) Math.floor(ly), (int) Math.ceil(rx), (int) Math.ceil(uy), (int) Math.floor(ulx), (int) Math.ceil(lrx), (int) Math.floor(llx),
            (int) Math.ceil(urx));
      }
   }