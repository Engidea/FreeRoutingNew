/*
 *  Copyright (C) 2014  Alfons Wirtz
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * IncompleteFreeSpaceExpansionRoom.java
 *
 * Created on 10. Februar 2004, 10:13
 */

package autoroute.expand;

import planar.ShapeTile;

/**
 * An expansion room, whose shape is not yet completely calculated.
 *
 * @author Alfons Wirtz
 */
public final class ExpandRoomFreespaceIncomplete extends ExpandRoomFreespace
   {
   // A shape which should be contained in the completed shape
   private ShapeTile contained_shape;
   
   /**
    * If p_shape == null means p_shape is the whole plane.
    */
   public ExpandRoomFreespaceIncomplete(ShapeTile p_shape, int p_layer, ShapeTile p_contained_shape)
      {
      super(p_shape, p_layer);
      
      contained_shape = p_contained_shape;
      }

   public ShapeTile get_contained_shape()
      {
      return contained_shape;
      }

   public void set_contained_shape(ShapeTile p_shape)
      {
      contained_shape = p_shape;
      }

   /*
   public Collection<ExpandDoorItem> get_target_doors()
      {
      return new LinkedList<ExpandDoorItem>();
      }
      */
   }
