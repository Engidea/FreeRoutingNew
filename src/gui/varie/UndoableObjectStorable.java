package gui.varie;

/**
 * Condition for an Object to be stored in an UndoableObjects database.
 * An object of class UndoableObjects.Storable must not contain any references.
 */

public interface UndoableObjectStorable extends Comparable<Object>
   {
   /**
    * Creates an exact copy of this object
    * Public overwriting of the protected clone method in java.lang.Object,
    */
   Object clone();

   }

