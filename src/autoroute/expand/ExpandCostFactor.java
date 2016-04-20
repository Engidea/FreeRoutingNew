package autoroute.expand;

/** 
 * horizontal and vertical costs for traces on a board layer 
 * Need to find out what the number means
 */
public final class ExpandCostFactor
   {
   // The horizontal expansion cost factor on a layer of the board 
   public final double horizontal;
   // The vertical expansion cost factor on a layer of the board 
   public final double vertical;

   public ExpandCostFactor(double p_horizontal, double p_vertical)
      {
      horizontal = p_horizontal;
      vertical = p_vertical;
      }
   }
