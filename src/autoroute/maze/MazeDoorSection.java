package autoroute.maze;

import freert.planar.PlaSegmentFloat;
import autoroute.expand.ExpandDoor;

public final class MazeDoorSection
   {
   public final ExpandDoor door;
   public final int section_no;
   public final PlaSegmentFloat section_line;

   public MazeDoorSection(ExpandDoor p_door, int p_section_no, PlaSegmentFloat p_section_line)
      {
      door = p_door;
      section_no = p_section_no;
      section_line = p_section_line;
      }
   }
