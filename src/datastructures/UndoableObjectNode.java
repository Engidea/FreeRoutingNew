package datastructures;

import gui.varie.UndoableObjectStorable;

/**
 * Stores informations for correct restoring or canceling an object in an undo or redo operation. p_level is the level in the
 * Undo stack, where this object was inserted.
 */
public final class UndoableObjectNode implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   final UndoableObjectStorable object; // the object in the node
   int undo_level; // the level in the Undo stack, where this node was inserted
   UndoableObjectNode undo_object; // the object to restore in an undo or null.
   UndoableObjectNode redo_object; // the object to restore in a redo or null.

   UndoableObjectNode(UndoableObjectStorable p_object, int p_level)
      {
      object = p_object;
      undo_level = p_level;
      undo_object = null;
      redo_object = null;
      }

   }
