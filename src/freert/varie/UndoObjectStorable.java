package freert.varie;
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
 * Condition for an Object to be stored in an UndoableObjects database.
 * An object of class UndoableObjects.Storable must not contain any references.
 */

public interface UndoObjectStorable extends Comparable<Object>
   {
   /**
    * Creates an exact copy of this object
    * To avoid confuzion with java Clone there is a specific method...
    */
   public Object copy();

   }

