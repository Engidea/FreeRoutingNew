package autoroute.maze;

import autoroute.expand.ExpandObject;


/**
 * The result type of ArtMazeSearch.find_connection
 */
public class MazeSearchResult
   {
   public final ExpandObject destination_door;
   public final int section_no_of_door;

   public MazeSearchResult(ExpandObject p_destination_door, int p_section_no_of_door)
      {
      destination_door = p_destination_door;
      section_no_of_door = p_section_no_of_door;
      }
   }
