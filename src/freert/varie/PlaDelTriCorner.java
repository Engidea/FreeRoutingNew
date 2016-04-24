package freert.varie;

import freert.planar.PlaPoint;
import freert.planar.PlaSide;

/**
 * Contains a corner point together with the objects this corner belongs to.
 */
public class PlaDelTriCorner
   {
   public PlaDelTriCorner(PlaDelTriStorable p_object, PlaPoint p_coor)
      {
      object = p_object;
      coor = p_coor;
      }

   /**
    * The function returns Side.ON_THE_LEFT, if this corner is on the left of the line from p_1 to p_2; Side.ON_THE_RIGHT, if
    * this corner is on the right of the line from p_1 to p_2; and Side.COLLINEAR, if this corner is collinear with p_1 and p_2.
    */
   public PlaSide side_of(PlaDelTriCorner p_1, PlaDelTriCorner p_2)
      {
      return this.coor.side_of(p_1.coor, p_2.coor);
      }

   public final PlaDelTriStorable object;
   public final PlaPoint coor;
   }
