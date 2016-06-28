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
 * Stores informations for correct restoring or canceling an object in an undo or redo operation.
 * p_level is the level in the Undo stack, where this object was inserted.
 */
public final class UndoObjectNode implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   final UndoObjectStorable object; // the object in the node
   
   int undo_level; // the level in the Undo stack, where this node was inserted
   UndoObjectNode undo_object; // the object to restore in an undo or null.
   UndoObjectNode redo_object; // the object to restore in a redo or null.

   UndoObjectNode(UndoObjectStorable p_object, int p_level)
      {
      object = p_object;
      undo_level = p_level;
      undo_object = null;
      redo_object = null;
      }

   }
