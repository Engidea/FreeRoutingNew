package board.awtree;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */


/**
 * Information returned by the find methods when searching in the tree
 */
public final class AwtreeFindEntry implements Comparable<AwtreeFindEntry>
   {
   public final AwtreeObject object;
   public final int shape_index_in_object;

   public AwtreeFindEntry(AwtreeObject p_object, int p_shape_index_in_object)
      {
      // do not let this possible error to go unnoticed
      if ( p_object == null ) throw new IllegalArgumentException("null p_object");
      
      object = p_object;
      shape_index_in_object = p_shape_index_in_object;
      }

   @Override
   public int compareTo(AwtreeFindEntry other)
      {
      return object.compareTo(other.object);
      }
   
   @Override
   public boolean equals ( Object other) 
      {
      if ( other == null ) return false;
      
      if (  ! (other instanceof AwtreeFindEntry)) return false;
      
      return compareTo((AwtreeFindEntry)other) != 0;
      }
   }
