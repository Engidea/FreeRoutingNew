package freert.varie;

/**
 * Condition for an Object to be stored in an UndoableObjects database.
 * An object of class UndoableObjects.Storable must not contain any references.
 */

public interface UndoableObjectStorable extends Comparable<Object>
   {
   /**
    * Creates an exact copy of this object
    * To avoid confuzion with java Clone there is a specific method...
    */
   public Object copy();

   }

