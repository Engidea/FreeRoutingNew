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
 * UndoableObjects.java
 *
 * Created on 24. August 2003, 06:41
 */
package freert.varie;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Database of objects, for which Undo and Redo operations are made possible. 
 * The algorithm works only for objects containing no references.
 *
 * @author Alfons Wirtz
 */
public final class UndoableObjects implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final String classname="UndoableObjects.";

   // The entries of this map are of type UnduableObject, the keys of type UndoableObjects.Storable
   private final ConcurrentMap<UndoableObjectStorable, UndoableObjectNode> objects_map;
   // the lists of deleted objects on each undo level, which where already existing before the previous snapshot.
   private final Vector<Collection<UndoableObjectNode>> deleted_objects_stack;

   // the current undo level
   private int stack_level;

   private boolean redo_possible = false;
   
   public UndoableObjects()
      {
      stack_level = 0;
      objects_map = new ConcurrentSkipListMap<UndoableObjectStorable, UndoableObjectNode>();
      deleted_objects_stack = new Vector<Collection<UndoableObjectNode>>();
      }

   /**
    * Returns an iterator for sequential reading of the object list. 
    * Use it together with this.read_object().
    */
   public Iterator<UndoableObjectNode> start_read_object()
      {
      Collection<UndoableObjectNode> object_list = objects_map.values();
      
      return object_list.iterator();
      }

   /**
    * Reads the next object in this list. 
    * @return null, if the list is exhausted. p_it must be created by start_read_object.
    */
   public UndoableObjectStorable read_object(Iterator<UndoableObjectNode> p_iter)
      {
      while (p_iter.hasNext())
         {
         UndoableObjectNode curr_node = p_iter.next();
    
         // skip objects getting alive only by redo
         
         if ( curr_node.undo_level <= stack_level) return (curr_node.object);
         }

      return null;
      }

   /**
    * Adds p_object to the UndoableObjectsList
    * A check is made to make sure that p_object is non null
    * By doing this I am guarantee that a get will always return a non null object
    */
   public void insert(UndoableObjectStorable p_object)
      {
      if ( p_object == null ) throw new IllegalArgumentException(classname+"insert p_object is null");
      
      disable_redo();
      
      UndoableObjectNode undoable_object = new UndoableObjectNode(p_object, stack_level);
      
      objects_map.put(p_object, undoable_object);
      }

   /**
    * Removes p_object from the top level of the UndoableObjectsList. Returns false, if p_object was not found in the list.
    */
   public boolean delete(UndoableObjectStorable p_object)
      {
      disable_redo();
      Collection<UndoableObjectNode> curr_delete_list;
      
      if (deleted_objects_stack.isEmpty())
         {
         // stack_level 0
         curr_delete_list = null;
         }
      else
         {
         curr_delete_list = deleted_objects_stack.lastElement();
         }
      
      // search p_object in the list
      UndoableObjectNode object_node = objects_map.get(p_object);
      
      if (object_node == null) return false;

      if (object_node.object != p_object)
         {
         System.out.println("UndoableObjectList.delete: Object inconsistent");
         return false;
         }

      if (curr_delete_list != null)
         {
         if (object_node.undo_level < this.stack_level)
            {
            // add curr_ob to the current delete list to make Undo possible.
            curr_delete_list.add(object_node);
            }
         else if (object_node.undo_object != null)
            {
            // add curr_ob.undo_object to the current delete list to make Undo possible.

            curr_delete_list.add(object_node.undo_object);
            }
         }
      objects_map.remove(p_object);
      return true;
      }

   /**
    * Makes the current state of the list restorable by Undo.
    */
   public void generate_snapshot()
      {
      disable_redo();
      Collection<UndoableObjectNode> curr_deleted_objects_list = new LinkedList<UndoableObjectNode>();
      deleted_objects_stack.add(curr_deleted_objects_list);
      ++stack_level;
      }

   /**
    * Restores the situation before the last snapshot. 
    * Outputs the cancelled and the restored objects (if != null) to enable the
    * calling function to take additional actions needed for these objects. 
    * @returns false, if no more undo is possible
    */
   public boolean undo(Collection<UndoableObjectStorable> p_cancelled_objects, Collection<UndoableObjectStorable> p_restored_objects)
      {
      if (stack_level == 0) return false; // no more undo possible
      
      Iterator<UndoableObjectNode> it = objects_map.values().iterator();

      while (it.hasNext())
         {
         UndoableObjectNode curr_node = it.next();
         
         if (curr_node.undo_level != stack_level) continue;
         
         if (curr_node.undo_object != null)
            {
            // replace the current object by its previous state.
            curr_node.undo_object.redo_object = curr_node;
            objects_map.put(curr_node.object, curr_node.undo_object);
            
            if (p_restored_objects != null)  p_restored_objects.add(curr_node.undo_object.object);
            }

         if (p_cancelled_objects != null)  p_cancelled_objects.add(curr_node.object);
         }
 
     
      // restore the deleted objects
      Collection<UndoableObjectNode> curr_delete_list = deleted_objects_stack.elementAt(stack_level - 1);

      Iterator<UndoableObjectNode> it2 = curr_delete_list.iterator();
      
      while (it2.hasNext())
         {
         UndoableObjectNode curr_deleted_node = it2.next();
         
         objects_map.put(curr_deleted_node.object, curr_deleted_node);
         
         if (p_restored_objects != null) p_restored_objects.add(curr_deleted_node.object);
         }
      
      stack_level--;
      
      redo_possible = true;
      
      return true;
      }

   /**
    * Restores the situation before the last undo. Outputs the cancelled and the restored objects (if != null) to enable the calling
    * function to take additional actions needed for these objects. Returns false, if no more redo is possible.
    */
   public boolean redo(Collection<UndoableObjectStorable> p_cancelled_objects, Collection<UndoableObjectStorable> p_restored_objects)
      {
      if (stack_level >= deleted_objects_stack.size()) return false; // Already at the top level

      stack_level++;
      
      Iterator<UndoableObjectNode> it = objects_map.values().iterator();
      
      while (it.hasNext())
         {
         UndoableObjectNode curr_node = it.next();
      
         if ( curr_node.redo_object != null && curr_node.redo_object.undo_level == stack_level)
            {
            // Object was created on a lower level and changed on the currenzt level,
            // replace the lower level object by the object on the current layer.
            objects_map.put(curr_node.object, curr_node.redo_object);
            if (p_cancelled_objects != null)
               {
               p_cancelled_objects.add(curr_node.object);
               }
            if (p_restored_objects != null)
               {
               p_restored_objects.add(curr_node.redo_object.object);
               // else the redo_object was deleted on the redo level
               }
            }
         else if (curr_node.undo_level == stack_level)
            {
            // Object was created on the current level, allow it to be restored.
            p_restored_objects.add(curr_node.object);
            }
         }
      
      // Delete the objects, which were deleted on the current level, again.
      Collection<UndoableObjectNode> curr_delete_list = deleted_objects_stack.elementAt(stack_level - 1);
      Iterator<UndoableObjectNode> it2 = curr_delete_list.iterator();
      while (it2.hasNext())
         {
         UndoableObjectNode curr_deleted_node = it2.next();
         while (curr_deleted_node.redo_object != null && curr_deleted_node.redo_object.undo_level <= this.stack_level)
            {
            curr_deleted_node = curr_deleted_node.redo_object;
            }
         if (this.objects_map.remove(curr_deleted_node.object) == null)
            {
            System.out.println("previous deleted object not found");
            }
         if (p_restored_objects == null || !p_restored_objects.remove(curr_deleted_node.object))
            {
            // the object needs only be cancelled if it is already in the board
            if (p_cancelled_objects != null)
               {
               p_cancelled_objects.add(curr_deleted_node.object);
               }
            }
         }
      return true;
      }

   /**
    * Removes the top snapshot from the undo stack, so that its situation cannot be restored any more. Returns false, if no more
    * snapshot could be popped.
    */
   public boolean pop_snapshot()
      {
      disable_redo();

      if (stack_level == 0) return false;

      Iterator<UndoableObjectNode> it = objects_map.values().iterator();
      while (it.hasNext())
         {
         UndoableObjectNode curr_node = it.next();
         if (curr_node.undo_level == stack_level - 1)
            {
            if (curr_node.redo_object != null && curr_node.redo_object.undo_level == stack_level)
               {
               curr_node.redo_object.undo_object = curr_node.undo_object;
               if (curr_node.undo_object != null)
                  {
                  curr_node.undo_object.redo_object = curr_node.redo_object;

                  }
               }
            }
         else if (curr_node.undo_level >= stack_level)
            {
            --curr_node.undo_level;
            }

         }

      int deleted_objects_stack_size = deleted_objects_stack.size();
      if (deleted_objects_stack_size >= 2)
         {
         // join the top delete list with the delete list of the second top level
         Collection<UndoableObjectNode> from_delete_list = deleted_objects_stack.elementAt(deleted_objects_stack_size - 1);
         Collection<UndoableObjectNode> to_delete_list = deleted_objects_stack.elementAt(deleted_objects_stack_size - 2);
         for (UndoableObjectNode curr_deleted_node : from_delete_list)
            {
            if (curr_deleted_node.undo_level < this.stack_level - 1)
               {
               to_delete_list.add(curr_deleted_node);
               }
            else if (curr_deleted_node.undo_object != null)
               {
               to_delete_list.add(curr_deleted_node.undo_object);
               }
            }
         }
      deleted_objects_stack.remove(deleted_objects_stack_size - 1);
      --stack_level;
      return true;
      }

   /**
    * Must be called before p_object will be modified after a snapshot for the first time, if it may have existed before that
    * snapshot.
    */
   public void save_for_undo(UndoableObjectStorable p_object)
      {
      disable_redo();
      
      // search p_object in the map
      UndoableObjectNode curr_node = objects_map.get(p_object);
      
      if (curr_node == null)
         {
         System.out.println("UndoableObjects.save_for_undo: object node not found");
         return;
         }

      if (curr_node.undo_level < stack_level)
         {
         UndoableObjectNode old_node = new UndoableObjectNode((UndoableObjectStorable) p_object.copy(), curr_node.undo_level);
         old_node.undo_object  = curr_node.undo_object;
         old_node.redo_object  = curr_node;
         curr_node.undo_object = old_node;
         curr_node.undo_level  = stack_level;
         return;
         }
      }

   /**
    * Must be called, if objects are changed for the first time after undo.
    */
   private void disable_redo()
      {
      if (!redo_possible) return;

      redo_possible = false;

      // shorten the size of the deleted_objects_stack to this.stack_level
      for (int i = deleted_objects_stack.size() - 1; i >= this.stack_level; --i)
         {
         deleted_objects_stack.remove(i);
         }
      
      Iterator<UndoableObjectNode> it = objects_map.values().iterator();
      while (it.hasNext())
         {
         UndoableObjectNode curr_node = it.next();
         if (curr_node.undo_level > this.stack_level)
            {
            it.remove();
            }
         else if (curr_node.undo_level == this.stack_level)
            {
            curr_node.redo_object = null;
            }
         }
      }
   }
