package gui.varie;

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

import java.util.ArrayList;
import java.util.Iterator;

public final class IntKeyStringValueAlist implements Iterable<IntKeyStringValue>
   {
   public final ArrayList<IntKeyStringValue>a_list;
   
   public IntKeyStringValueAlist ( int initial_capacity )
      {
      a_list = new ArrayList<IntKeyStringValue>(initial_capacity);
      }
   
   public IntKeyStringValue get( int p_key )
      {
      for ( IntKeyStringValue a_row : a_list )
         if ( a_row.key == p_key ) return a_row;
      
      return null;
      }

   public void add ( int p_key, String p_value )
      {
      a_list.add(new IntKeyStringValue(p_key, p_value));
      }

   @Override
   public Iterator<IntKeyStringValue> iterator()
      {
      return a_list.iterator();
      }
   
   }
