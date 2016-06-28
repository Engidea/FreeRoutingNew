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
 * SearchTreeManager.java
 *
 * Created on 9. Januar 2006, 08:28
 *
 */

package board.awtree;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import board.RoutingBoard;
import board.items.BrdItem;
import board.items.BrdTracep;
import freert.planar.Polyline;
import freert.varie.UndoObjectNode;

/**
 *
 * @author Alfons Wirtz
 */
public final class AwtreeManager
   {
   private final RoutingBoard r_board;
   private final Collection<AwtreeShapeSearch> compensated_search_trees;

   private AwtreeShapeSearch default_tree;
   private boolean clearance_compensation_used;   // what does it do ?
   
   public AwtreeManager(RoutingBoard p_board)
      {
      r_board = p_board;
      
      default_tree = new AwtreeShapeSearch(p_board, 0);
      
      compensated_search_trees = new LinkedList<AwtreeShapeSearch>();
      compensated_search_trees.add(default_tree);
      
      clearance_compensation_used = false;
      }

   /**
    * Inserts the tree shapes of p_item into all active search trees.
    * This is possibly the entry point in the search tree system
    */
   public void insert(BrdItem p_item)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         curr_tree.insert(p_item);
         }
      p_item.set_on_the_board(true);
      }

   /**
    * Removes all entries of an item from the search trees.
    */
   public void remove(BrdItem p_item)
      {
      if (!p_item.is_on_the_board()) return;
      
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         AwtreeNodeLeaf[] curr_tree_entries = p_item.get_search_tree_entries(curr_tree);

         if (curr_tree_entries == null) continue;

         curr_tree.remove(curr_tree_entries);
         }

      p_item.clear_search_tree_entries();
      
      p_item.set_on_the_board(false);
      }

   /**
    * @return the default tree used in interactive routing.
    */
   public AwtreeShapeSearch get_default_tree()
      {
      return default_tree;
      }

   /**
    * @return true if all is fine
    * stops validation at first error
    * @param p_item
    * @return
    */
   public final boolean validate_ok (BrdItem p_item)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         if ( ! curr_tree.validate_ok(p_item)) return false;
         }

      return true;
      }

   /**
    * Returns, if clearance compensation is used for the default tree. 
    * This is normally the case, if there exist only the clearance classes null and default in the clearance matrix.
    */
   public boolean is_clearance_compensation_used()
      {
      return clearance_compensation_used;
      }

   /**
    * Sets the usage of clearance compensation to true or false.
    * Maybe this is part of the issue ? Damiano
    */
   public void set_clearance_compensation_used(boolean p_value)
      {
      if ( clearance_compensation_used == p_value) return;
      
      clearance_compensation_used = p_value;
      
      remove_all_board_items();
      
      compensated_search_trees.clear();
      
      int compensated_clearance_class_no;
      
      if (p_value)
         {
         compensated_clearance_class_no = 1;
         }
      else
         {
         compensated_clearance_class_no = 0;
         }
      
      default_tree = new AwtreeShapeSearch(r_board, compensated_clearance_class_no);
      
      compensated_search_trees.add(default_tree);
      
      insert_all_board_items();
      }

   /**
    * Actions to be done, when a value in the clearance matrix is changed interactively.
    */
   public void clearance_value_changed()
      {
      // delete all trees except the default tree
      Iterator<AwtreeShapeSearch> iter = compensated_search_trees.iterator();

      while (iter.hasNext())
         {
         AwtreeShapeSearch curr_tree = iter.next();
      
         if (curr_tree.compensated_clearance_class_no != default_tree.compensated_clearance_class_no)
            {
            iter.remove();
            }
         }
      
      if (clearance_compensation_used)
         {
         remove_all_board_items();
         insert_all_board_items();
         }
      }

   /**
    * Actions to be done, when a new clearance class is removed interactively.
    */
   public void clearance_class_removed(int p_no)
      {
      Iterator<AwtreeShapeSearch> it = compensated_search_trees.iterator();
      
      if (p_no == default_tree.compensated_clearance_class_no)
         {
         System.err.println("SearchtreeManager.clearance_class_removed: unable to remove default tree");
         return;
         }
      
      while (it.hasNext())
         {
         AwtreeShapeSearch curr_tree = it.next();
         if (curr_tree.compensated_clearance_class_no == p_no)
            {
            it.remove();
            }
         }
      }

   /**
    * Returns the tree compensated for the clearance class with number p_clearance_vlass_no. 
    * Initialized the tree, if it is not yet allocated.
    */
   public AwtreeShapeSearch get_autoroute_tree(int p_clearance_class_no)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         if (curr_tree.compensated_clearance_class_no == p_clearance_class_no)
            {
            return curr_tree;
            }
         }

      AwtreeShapeSearch curr_autoroute_tree = new AwtreeShapeSearch( r_board, p_clearance_class_no);
      
      compensated_search_trees.add(curr_autoroute_tree);
      
      Iterator<UndoObjectNode> iter = r_board.undo_items.start_read_object();

      for (;;)
         {
         BrdItem curr_item = (BrdItem) r_board.undo_items.read_next(iter);
      
         if (curr_item == null) break;

         curr_autoroute_tree.insert(curr_item);
         }
      
      return curr_autoroute_tree;
      }

   /**
    * Clears all compensated trees used in the autoroute algorithm apart from the default tree.
    */
   public void reset_compensated_trees()
      {
      Iterator<AwtreeShapeSearch> iter = compensated_search_trees.iterator();

      while (iter.hasNext())
         {
         AwtreeShapeSearch curr_tree = iter.next();
      
         if (curr_tree != default_tree) iter.remove();
         }
      }

   /** 
    * Reinsert all items into the search trees 
    */
   public void reinsert_tree_items()
      {
      remove_all_board_items();
      insert_all_board_items();
      }

   private void remove_all_board_items()
      {
      Iterator<UndoObjectNode> it = r_board.undo_items.start_read_object();

      for (;;)
         {
         BrdItem curr_item = (BrdItem) r_board.undo_items.read_next(it);
        
         if (curr_item == null)  break;

         remove(curr_item);
         }
      }

   private void insert_all_board_items()
      {
      Iterator<UndoObjectNode> it = r_board.undo_items.start_read_object();
      
      for (;;)
         {
         BrdItem curr_item = (BrdItem) r_board.undo_items.read_next(it);
      
         if (curr_item == null) break;

         curr_item.clear_derived_data();

         insert(curr_item);
         }
      }

   // The following functions are used internally for performance improvement.

   /**
    * Merges the tree entries from p_from_trace in front of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public void merge_entries_in_front(BrdTracep p_from_trace, BrdTracep p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         curr_tree.merge_entries_in_front(p_from_trace, p_to_trace, p_joined_polyline, p_from_entry_no, p_to_entry_no);
         }
      }

   /**
    * Merges the tree entries from p_from_trace to the end of p_to_trace. 
    * Special implementation for combine trace for performance reasons.
    */
   public void merge_entries_at_end(BrdTracep p_from_trace, BrdTracep p_to_trace, Polyline p_joined_polyline, int p_from_entry_no, int p_to_entry_no)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         curr_tree.merge_entries_at_end(p_from_trace, p_to_trace, p_joined_polyline, p_from_entry_no, p_to_entry_no);
         }
      }

   /**
    * Changes the tree entries from p_keep_at_start_count + 1 to new_shape_count - 1 - keep_at_end_count to p_changed_entries.
    * Special implementation for change_trace for performance reasons
    */
   public void change_entries(BrdTracep p_obj, Polyline p_new_polyline, int p_keep_at_start_count, int p_keep_at_end_count)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         curr_tree.change_entries(p_obj, p_new_polyline, p_keep_at_start_count, p_keep_at_end_count);
         }
      }

   /**
    * Trannsfers tree entries from p_from_trace to p_start and p_end_piece after a moddle piece was cut out. Special implementation
    * for ShapeTraceEntries.fast_cutout_trace for performance reasoms.
    */
   public void reuse_entries_after_cutout(BrdTracep p_from_trace, BrdTracep p_start_piece, BrdTracep p_end_piece)
      {
      for (AwtreeShapeSearch curr_tree : compensated_search_trees)
         {
         curr_tree.reuse_entries_after_cutout(p_from_trace, p_start_piece, p_end_piece);
         }
      }

   /**
    * Used to have some meaningful info on this object
    * Mostly used for beanshell
    */
   @Override
   public String toString()
      {
      StringBuilder risul = new StringBuilder(1000);
      risul.append("SearchTreeManager /n");
      risul.append("methods: get_default_tree() \n");
      
      return risul.toString();
      }

   }
