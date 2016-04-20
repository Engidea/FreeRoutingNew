package autoroute.varie;

import autoroute.expand.ExpandObject;
import autoroute.expand.ExpandRoomComplete;


/**
 * Type of the elements of the list returned by this.backtrack(). 
 * Next_room is the common room of the current door and the next door in the backtrack list.
 */
public class ArtBacktrackElement
   {
   public final ExpandObject door;
   public final int section_no_of_door;
   public final ExpandRoomComplete next_room;

   public ArtBacktrackElement(ExpandObject p_door, int p_section_no_of_door, ExpandRoomComplete p_room)
      {
      door = p_door;
      section_no_of_door = p_section_no_of_door;
      next_room = p_room;
      }
   }
