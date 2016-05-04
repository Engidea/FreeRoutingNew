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
 */
package board.items;

import java.awt.Color;
import java.awt.Graphics;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import autoroute.ArtItem;
import board.BrdConnectable;
import board.RoutingBoard;
import board.infos.BrdItemViolation;
import board.infos.PrintableInfo;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTree;
import board.shape.ShapeTreeEntry;
import board.shape.ShapeTreeLeaf;
import board.shape.ShapeTreeObject;
import board.varie.BrdStopConnection;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import board.varie.SearchTreesInfo;
import freert.graphics.GdiContext;
import freert.graphics.GdiDrawable;
import freert.planar.PlaDimension;
import freert.planar.PlaPoint;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVector;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.rules.RuleNets;
import gui.varie.ObjectInfoPanel;
import gui.varie.UndoableObjectStorable;

/**
 * Basic class of the items on a board.
 * @author Alfons Wirtz
 */
public abstract class BrdItem implements GdiDrawable, ShapeTreeObject, PrintableInfo, UndoableObjectStorable, Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final double PROTECT_FANOUT_LENGTH = 400;

   private final int id_no;
   // the index in the clearance matrix describing the required spacing to other items
   private int clearance_class;
   // The nets, to which this item belongs 
   public  int[] net_no_arr;
   // if an item is fixed or not
   private ItemFixState fixed_state;
   // not 0, if this item belongs to a component 
   private int component_no = 0;
    // false, if the item is deleted or not inserted into the board
   private boolean on_the_board = false;
   
   // The board this Item is on 
   public transient RoutingBoard r_board;
   // Temporary data used in the auto route algorithm. 
   private transient ArtItem art_item;
   // points to the entries of this item in the ShapeSearchTrees 
   private transient SearchTreesInfo search_trees_info = new SearchTreesInfo();

   
   public BrdItem(int[] p_net_no_arr, int p_clearance_type, int p_id_no, int p_component_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      r_board = p_board;
   
      if (p_net_no_arr == null)
         {
         net_no_arr = new int[0];
         }
      else
         {
         net_no_arr = new int[p_net_no_arr.length];
         System.arraycopy(p_net_no_arr, 0, net_no_arr, 0, p_net_no_arr.length);
         }

      if (p_id_no <= 0)
         id_no = r_board.host_com.id_no_generator.new_no();
      else
         id_no = p_id_no;
      
      clearance_class = p_clearance_type;
      component_no = p_component_no;
      fixed_state = p_fixed_state;
      }
   
   /**
    * When you read back from object strem you MUST recreate the transient objects
    * @param p_board
    */
   public final void set_transient_field ( RoutingBoard p_board )
      {
      r_board = p_board;
      search_trees_info = new SearchTreesInfo();
      }
   
   
   /**
    * Implements the comparable interface
    * Should it throw exception if non comparable ? Damiano
    */
   @Override
   public int compareTo(Object p_other)
      {
      if (p_other instanceof BrdItem)
         {
         return ((BrdItem) p_other).id_no - id_no;
         }
      else
         {
         return 1;
         }
      }

   /**
    * returns the unique identification number of this item
    */
   public final int get_id_no()
      {
      return id_no;
      }

   /**
    * Check for contains net considering a p_net_no == -1 as a wildcard
    * @param p_net_no
    * @return
    */
   public final boolean contains_net_wildcard (int p_net_no )
      {
      if ( p_net_no <= 0 ) return true;
      
      return contains_net(p_net_no);
      }

   /**
    * Returns true if the net number array of this item contains p_net_no.
    */
   public final boolean contains_net(int p_net_no)
      {
      if (p_net_no <= 0) return false;
      
      for (int index = 0; index < net_no_arr.length; ++index)
         {
         if (net_no_arr[index] == p_net_no) return true;
         }

      return false;
      }

   @Override
   public final boolean is_obstacle(int p_net_no)
      {
      return ! contains_net(p_net_no);
      }

   @Override
   public  boolean is_trace_obstacle(int p_net_no)
      {
      return ! contains_net(p_net_no);
      }

   /**
    * Returns, if this item in not allowed to overlap with p_other.
    */
   public abstract boolean is_obstacle(BrdItem p_other);

   /**
    * Returns true if the net number arrays of this and p_other have a common number.
    */
   public final boolean shares_net(BrdItem p_other)
      {
      return shares_net_no(p_other.net_no_arr);
      }

   /**
    * @return true if the net number array of this and p_net_no_arr have a common number
    */
   public final boolean shares_net_no(int[] p_net_no_arr)
      {
      for (int i = 0; i < net_no_arr.length; ++i)
         {
         int want_net_no = net_no_arr[i];
         
         for (int j = 0; j < p_net_no_arr.length; ++j)
            {
            if (want_net_no == p_net_no_arr[j])
               {
               return true;
               }
            }
         }
      return false;
      }

   /**
    * Returns the number of shapes of this item after decomposition into convex polygonal shapes
    */
   public abstract int tile_shape_count();

   /**
    * Returns the p_index-throws shape of this item after decomposition into convex polygonal shapes
    * Overriden in subclasses
    */
   public ShapeTile get_tile_shape(int p_index)
      {
      return get_tree_shape(r_board.search_tree_manager.get_default_tree(), p_index);
      }

   @Override
   public final int tree_shape_count(ShapeTree p_tree)
      {
      ShapeTile[] precalculated_tree_shapes = get_precalculated_tree_shapes(p_tree);
      
      return precalculated_tree_shapes.length;
      }

   @Override
   public final ShapeTile get_tree_shape(ShapeTree p_tree, int p_index)
      {
      ShapeTile[] precalculated_tree_shapes = get_precalculated_tree_shapes(p_tree);
      
      return precalculated_tree_shapes[p_index];
      }

   private ShapeTile[] get_precalculated_tree_shapes(ShapeTree p_tree)
      {
      ShapeTile[] precalculated_tree_shapes = search_trees_info.get_precalculated_tree_shapes(p_tree);

      if (precalculated_tree_shapes == null)
         {
         precalculated_tree_shapes = calculate_tree_shapes((ShapeSearchTree) p_tree);
         search_trees_info.set_precalculated_tree_shapes(precalculated_tree_shapes, p_tree);
         }
      
      return precalculated_tree_shapes;
      }

   /**
    * Calculates the tree shapes for this item for p_search_tree.
    */
   protected abstract ShapeTile[] calculate_tree_shapes(ShapeSearchTree p_search_tree);

   /**
    * Returns false, if this item is deleted oor not inserted into the board.
    */
   public final boolean is_on_the_board()
      {
      return on_the_board;
      }

   public final void set_on_the_board(boolean p_value)
      {
      on_the_board = p_value;
      }

   /**
    * Creates a copy of this item with id number p_id_no. If p_id_no <= 0, the id_no of the new item is generated internally
    */
   public abstract BrdItem copy(int p_id_no);

   @Override
   public final Object clone()
      {
      return copy(get_id_no());
      }

   /**
    * returns true, if the layer range of this item contains p_layer
    */
   public abstract boolean is_on_layer(int p_layer);

   /**
    * Returns the number of the first layer containing geometry of this item.
    */
   public abstract int first_layer();

   /**
    * Returns the number of the last layer containing geometry of this item.
    */
   public abstract int last_layer();

   /**
    * write this item to an output stream
    */
   public abstract boolean write(ObjectOutputStream p_stream);

   /**
    * Translates the shapes of this item by p_vector. Does not move the item in the board.
    */
   public abstract void translate_by(PlaVector p_vector);

   /**
    * Turns this Item by p_factor times 90 degree around p_pole. Does not update the item in the board.
    */
   public abstract void turn_90_degree(int p_factor, PlaPointInt p_pole);

   /**
    * Rotates this Item by p_angle_in_degree around p_pole. Does not update the item in the board.
    */
   public abstract void rotate_approx(double p_angle_in_degree, PlaPointFloat p_pole);

   /**
    * Changes the placement side of this Item and mirrors it at the vertical line through p_pole. Does not update the item in the
    * board.
    */
   public abstract void change_placement_side(PlaPointInt p_pole);

   /**
    * Returns a box containing the geometry of this item.
    */
   public abstract ShapeTileBox bounding_box();

   /**
    * Translates this item by p_vector in the board.
    * Override in subclasses
    */
   public void move_by(PlaVectorInt p_vector)
      {
      r_board.item_list.save_for_undo(this);
      r_board.search_tree_manager.remove(this);
      translate_by(p_vector);
      r_board.search_tree_manager.insert(this);
      // let the observers synchronize the changes
      r_board.observers.notify_changed(this);
      }

   /**
    * Returns true, if some shapes of this item and p_other are on the same layer.
    */
   public final boolean shares_layer(BrdItem p_other)
      {
      int max_first_layer = Math.max(this.first_layer(), p_other.first_layer());
      int min_last_layer = Math.min(this.last_layer(), p_other.last_layer());
      return max_first_layer <= min_last_layer;
      }

   /**
    * Returns the first layer, where both this item and p_other have a shape. 
    * Returns -1, if such a layer does not exisr.
    */
   public final int first_common_layer(BrdItem p_other)
      {
      int max_first_layer = Math.max(this.first_layer(), p_other.first_layer());
      int min_last_layer = Math.min(this.last_layer(), p_other.last_layer());
      
      if (max_first_layer > min_last_layer)
         {
         return -1;
         }
      return max_first_layer;
      }

   /**
    * Returns the last layer, where both this item and p_other have a shape. Returns -1, if such a layer does not exisr.
    */
   public final int last_common_layer(BrdItem p_other)
      {
      int max_first_layer = Math.max(first_layer(), p_other.first_layer());
      int min_last_layer = Math.min(last_layer(), p_other.last_layer());

      if (max_first_layer > min_last_layer) return -1;

      return min_last_layer;
      }

   /**
    * Return the name of the component of this item or null, if this item does not belong to a component.
    */
   public final String component_name()
      {
      if ( component_no <= 0 ) return null;

      return r_board.brd_components.get(component_no).name;
      }

   /**
    * Returns the count of clearance violations of this item with other items.
    */
   public final int clearance_violation_count()
      {
      Collection<BrdItemViolation> violations = clearance_violations();
      return violations.size();
      }

   /**
    * Returns true if curr_item is an obstacle as clearance violation
    * @param curr_item
    * @return
    */
   private boolean clearance_violation_obstacle (BrdItem curr_item )
      {
      boolean is_obstacle = curr_item.is_obstacle(this);
      
      if ( ! is_obstacle ) return false;
      
      if ( this instanceof BrdTrace && curr_item instanceof BrdTrace)
         {
         // Look, if both traces are connected to the same tie pin.
         // In this case they are allowed to overlap without sharing a net.
         BrdTrace this_trace = (BrdTrace) this;
         PlaPoint contact_point = this_trace.first_corner();
         boolean contact_found = false;
         Collection<BrdItem> curr_contacts = this_trace.get_normal_contacts(contact_point, true);
            {
            if (curr_contacts.contains(curr_item))
               {
               contact_found = true;
               }
            }
            
         if (!contact_found)
            {
            contact_point = this_trace.corner_last();
            curr_contacts = this_trace.get_normal_contacts(contact_point, true);
               {
               if (curr_contacts.contains(curr_item))
                  {
                  contact_found = true;
                  }
               }
            }
         if (contact_found)
            {
            for (BrdItem curr_contact : curr_contacts)
               {
               if (curr_contact instanceof BrdAbitPin)
                  {
                  if (curr_contact.shares_net(this) && curr_contact.shares_net(curr_item))
                     {
                     is_obstacle = false;
                     break;
                     }
                  }
               }
            }
         }
      
      return is_obstacle;   
      }

   
   /**
    * Returns a list of all clearance violations of this item with other items. 
    * The first_item in such an object is always this item.
    */
   public final Collection<BrdItemViolation> clearance_violations()
      {
      Collection<BrdItemViolation> result = new LinkedList<BrdItemViolation>();
      
      ShapeSearchTree default_tree = r_board.search_tree_manager.get_default_tree();
      
      for (int index = 0; index < tile_shape_count(); ++index)
         {
         ShapeTile curr_tile_shape = get_tile_shape(index);
         
         Collection<ShapeTreeEntry> curr_overlapping_items = default_tree.find_overlap_tree_entries_with_clearance(curr_tile_shape, shape_layer(index), new int[0], clearance_class);
      
         Iterator<ShapeTreeEntry> iter = curr_overlapping_items.iterator();
         
         while (iter.hasNext())
            {
            ShapeTreeEntry curr_entry = iter.next();
            
            // skip objects that are not board items
            if ( !(curr_entry.object instanceof BrdItem)) continue;
            
            // skip myself, obviously
            if ( curr_entry.object == this ) continue;
         
            BrdItem curr_item = (BrdItem) curr_entry.object;
            
            // if current item is not an obstacle for this item, no check to do
            if (! clearance_violation_obstacle(curr_item)) continue;

            ShapeTile shape_1 = curr_tile_shape;
            ShapeTile shape_2 = curr_item.get_tree_shape(default_tree, curr_entry.shape_index_in_object);

            if (shape_1 == null || shape_2 == null)
               {
               System.err.println("Item.clearance_violations: unexpected  null shape");
               continue;
               }
            
            if (!r_board.search_tree_manager.is_clearance_compensation_used())
               {
               double cl_offset = 0.5 * r_board.brd_rules.clearance_matrix.value_at(curr_item.clearance_class, this.clearance_class, shape_layer(index));
               shape_1 = (ShapeTile) shape_1.enlarge(cl_offset);
               shape_2 = (ShapeTile) shape_2.enlarge(cl_offset);
               }

            ShapeTile intersection = shape_1.intersection(shape_2);
            if (intersection.dimension() == PlaDimension.AREA )
               {
               BrdItemViolation curr_violation = new BrdItemViolation(this, curr_item, intersection, shape_layer(index));
               result.add(curr_violation);
               }
            }
         }
      return result;
      }

   /**
    * Returns all connectable Items with a direct contacts to this item. 
    * The result will be empty, if this item is not connectable.
    */
   public final Set<BrdItem> get_all_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      if (!(this instanceof BrdConnectable)) return result;
      
      for (int index = 0; index < tile_shape_count(); ++index)
         {
         Collection<ShapeTreeObject> overlapping_items = r_board.overlapping_objects(get_tile_shape(index), shape_layer(index));
         Iterator<ShapeTreeObject> it = overlapping_items.iterator();
         while (it.hasNext())
            {
            ShapeTreeObject curr_ob = it.next();
            
            if (!(curr_ob instanceof BrdItem)) continue;

            BrdItem curr_item = (BrdItem) curr_ob;

            if (curr_item != this && curr_item instanceof BrdConnectable && curr_item.shares_net(this))
               {
               result.add(curr_item);
               }
            }
         }
      
      return result;
      }

   /**
    * Returns all connectable Items with a direct contacts to this item on the input layer. The result will be empty, if this item
    * is not connectable.
    */
   public final Set<BrdItem> get_all_contacts(int p_layer)
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();

      if (!(this instanceof BrdConnectable)) return result;

      for (int index = 0; index < tile_shape_count(); ++index)
         {
         if ( shape_layer(index) != p_layer) continue;

         Collection<ShapeTreeObject> overlapping_items = r_board.overlapping_objects(get_tile_shape(index), p_layer);
         Iterator<ShapeTreeObject> it = overlapping_items.iterator();

         while (it.hasNext())
            {
            ShapeTreeObject curr_ob = it.next();
         
            if (!(curr_ob instanceof BrdItem)) continue;

            BrdItem curr_item = (BrdItem) curr_ob;
            if (curr_item != this && curr_item instanceof BrdConnectable && curr_item.shares_net(this))
               {
               result.add(curr_item);
               }
            }
         }
      return result;
      }

   /**
    * Checks, if this item is electrically connected to another connectable item. 
    * Returns false for items, which are not connectable.
    */
   public final boolean is_connected()
      {
      Collection<BrdItem> contacts = get_all_contacts();
      return (contacts.size() > 0);
      }

   /**
    * Checks, if this item is electrically connected to another connectable item on the input layer. Returns false for items, which
    * are not connectable.
    */
   public final boolean is_connected_on_layer(int p_layer)
      {
      Collection<BrdItem> contacts_on_layer = this.get_all_contacts(p_layer);
      return (contacts_on_layer.size() > 0);
      }

   /**
    * default implementation to be overwritten in the Connectable subclasses
    */
   public Set<BrdItem> get_normal_contacts()
      {
      return new TreeSet<BrdItem>();
      }

   /**
    * Returns the contact point, if this item and p_other are Connectable and have a unique normal contact. 
    * Returns null otherwise
    */
   public PlaPoint normal_contact_point(BrdItem p_other)
      {
      return null;
      }

   /**
    * auxiliary function
    */
   public PlaPoint normal_contact_point(BrdTrace p_other)
      {
      return null;
      }

   /**
    * auxiliary function
    */
   public PlaPoint normal_contact_point(BrdAbit p_other)
      {
      return null;
      }

   /**
    * Returns the set of all Connectable items of the net with number p_net_no which can be reached recursively via normal contacts
    * from this item. If p_net_no <= 0, the net number is ignored.
    */
   public final Set<BrdItem> get_connected_set(int p_net_no)
      {
      return get_connected_set(p_net_no, false);
      }

   /**
    * Returns the set of all Connectable items of the net with number p_net_no which can be reached recursively via normal contacts
    * from this item. If p_net_no <= 0, the net number is ignored. If p_stop_at_plane, the recursive algorithm stops, when a
    * conduction area is reached, which does not belong to a component.
    */
   public final Set<BrdItem> get_connected_set(int p_net_no, boolean p_stop_at_plane)
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      if (p_net_no > 0 && ! contains_net(p_net_no)) return result;

      result.add(this);
      
      get_connected_set_recu(result, p_net_no, p_stop_at_plane);
      
      return result;
      }

   /**
    * recursive part of get_connected_set
    */
   private void get_connected_set_recu(Set<BrdItem> p_result, int p_net_no, boolean p_stop_at_plane)
      {
      Collection<BrdItem> contact_list = get_normal_contacts();

      if (contact_list == null) return;

      for (BrdItem curr_contact : contact_list)
         {
         if (p_stop_at_plane && curr_contact instanceof BrdAreaConduction && curr_contact.get_component_no() <= 0)
            {
            continue;
            }

         if (p_net_no > 0 && !curr_contact.contains_net(p_net_no))
            {
            continue;
            }
         
         if (p_result.add(curr_contact))
            {
            curr_contact.get_connected_set_recu(p_result, p_net_no, p_stop_at_plane);
            }
         }
      }

   /**
    * Returns true, if this item contains some overlap to be cleaned.
    * To be overriden in subclasses
    */
   public boolean is_overlap()
      {
      return false;
      }

   /**
    * Recursive part of Trace.is_cycle. If p_ignore_areas is true, cycles where conduction areas are involved are ignored.
    */
   protected final boolean is_cycle_recu(Set<BrdItem> p_visited_items, BrdItem p_search_item, BrdItem p_come_from_item, boolean p_ignore_areas)
      {
      if (p_ignore_areas && this instanceof BrdAreaConduction)
         {
         return false;
         }
      Collection<BrdItem> contact_list = get_normal_contacts();
      if (contact_list == null)
         {
         return false;
         }
      Iterator<BrdItem> it = contact_list.iterator();
      while (it.hasNext())
         {
         BrdItem curr_contact = it.next();
         if (curr_contact == p_come_from_item)
            {
            continue;
            }
         if (curr_contact == p_search_item)
            {
            return true;
            }
         if (p_visited_items.add(curr_contact))
            {
            if (curr_contact.is_cycle_recu(p_visited_items, p_search_item, this, p_ignore_areas))
               {
               return true;
               }
            }
         }
      return false;
      }

   /**
    * Returns the set of all Connectable items belonging to the net with number p_net_no, which are not in the connected set of this
    * item. If p_net_no <= 0, the net numbers contained in this items are used instead of p_net_no.
    */
   public final Set<BrdItem> get_unconnected_set(int p_net_no)
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      
      if (p_net_no > 0 && ! contains_net(p_net_no))
         {
         return result;
         }
      
      if (p_net_no > 0)
         {
         result.addAll(r_board.get_connectable_items(p_net_no));
         }
      else
         {
         for (int curr_net_no : this.net_no_arr)
            {
            result.addAll(r_board.get_connectable_items(curr_net_no));
            }
         }
      
      result.removeAll( get_connected_set(p_net_no));
      
      return result;
      }

   /**
    * Returns all traces and vias from this item until the next fork or terminal item.
    */
   public final Set<BrdItem> get_connection_items()
      {
      return get_connection_items(BrdStopConnection.NONE);
      }

   /**
    * Returns all traces and vias from this item until the next fork or terminal item. If p_stop_option ==
    * StopConnectionOption.FANOUT_VIA, the algorithm will stop at the next fanout via, If p_stop_option == StopConnectionOption.VIA,
    * the algorithm will stop at any via.
    */
   public final Set<BrdItem> get_connection_items(BrdStopConnection p_stop_option)
      {
      Set<BrdItem> contacts = get_normal_contacts();
      Set<BrdItem> result = new TreeSet<BrdItem>();

      if (is_route()) result.add(this);

      Iterator<BrdItem> it = contacts.iterator();
      while (it.hasNext())
         {
         BrdItem curr_item = it.next();
         PlaPoint prev_contact_point = this.normal_contact_point(curr_item);
         if (prev_contact_point == null)
            {
            // no unique contact point
            continue;
            }
         int prev_contact_layer = this.first_common_layer(curr_item);
         if (this instanceof BrdTrace)
            {
            // Check, that there is only 1 contact at this location.
            // Only for pins and vias items of more than 1 connection
            // are collected
            BrdTrace start_trace = (BrdTrace) this;
            Collection<BrdItem> check_contacts = start_trace.get_normal_contacts(prev_contact_point, false);
            if (check_contacts.size() != 1)
               {
               continue;
               }
            }
         // Search from curr_item along the contacts
         // until the next fork or nonroute item.
         for (;;)
            {
            if (!curr_item.is_route())
               {
               // connection ends
               break;
               }
            if (curr_item instanceof BrdAbitVia)
               {
               if (p_stop_option == BrdStopConnection.VIA)
                  {
                  break;
                  }
               if (p_stop_option == BrdStopConnection.FANOUT_VIA)
                  {
                  if (curr_item.is_fanout_via(result))
                     {
                     break;
                     }
                  }
               }
            result.add(curr_item);
            Collection<BrdItem> curr_ob_contacts = curr_item.get_normal_contacts();
            // filter the contacts at the previous contact point,
            // because we were already there.
            // If then there is not exactly 1 new contact left, there is
            // a stub or a fork.
            PlaPoint next_contact_point = null;
            int next_contact_layer = -1;
            BrdItem next_contact = null;
            boolean fork_found = false;
            Iterator<BrdItem> curr_it = curr_ob_contacts.iterator();
            while (curr_it.hasNext())
               {
               BrdItem tmp_contact = curr_it.next();
               int tmp_contact_layer = curr_item.first_common_layer(tmp_contact);
               if (tmp_contact_layer >= 0)
                  {
                  PlaPoint tmp_contact_point = curr_item.normal_contact_point(tmp_contact);
                  if (tmp_contact_point == null)
                     {
                     // no unique contact point
                     fork_found = true;
                     break;
                     }
                  if (prev_contact_layer != tmp_contact_layer || !prev_contact_point.equals(tmp_contact_point))
                     {
                     if (next_contact != null)
                        {
                        // second new contact found
                        fork_found = true;
                        break;
                        }
                     next_contact = tmp_contact;
                     next_contact_point = tmp_contact_point;
                     next_contact_layer = tmp_contact_layer;
                     }
                  }
               }
            if (next_contact == null || fork_found)
               {
               break;
               }
            curr_item = next_contact;
            prev_contact_point = next_contact_point;
            prev_contact_layer = next_contact_layer;
            }
         }
      return result;
      }

   /**
    * Function o be overwritten by classes Trace and Via
    */
   public boolean is_tail()
      {
      return false;
      }

   /**
    * Returns all corners of this item, which are used for displaying the ratsnest. 
    * To be overwritten in derived classes implementing the Connectable interface.
    */
   public PlaPoint[] get_ratsnest_corners()
      {
      return new PlaPoint[0];
      }

   @Override
   public final void draw(Graphics p_g, GdiContext p_graphics_context, Color p_color, double p_intensity)
      {
      Color[] color_arr = new Color[r_board.get_layer_count()];

      for (int index = 0; index < color_arr.length; ++index) color_arr[index] = p_color;
      
      draw(p_g, p_graphics_context, color_arr, p_intensity);
      }

   /**
    * Draws this item whith its draw colors from p_graphics_context. 
    */
   public final void draw(Graphics p_g, GdiContext p_graphics_context)
      {
      Color[] layer_colors = get_draw_colors(p_graphics_context);
      
      draw(p_g, p_graphics_context, layer_colors, get_draw_intensity(p_graphics_context));
      }

   /**
    * Test function checking the item for inconsistencies.
    * return tru if all is fine or false if it fails, stops at first error
    */
   public boolean validate_ok()
      {
      if (!r_board.search_tree_manager.validate_ok(this)) return false;
      
      for (int index = 0; index < tile_shape_count(); ++index)
         {
         ShapeTile curr_shape = get_tile_shape(index);
         if (curr_shape.is_empty())
            {
            System.out.println("Item.validate: shape is empty");
            return false;
            }
         }
      
      return true;
      }


   /**
    * Test if it is possible to delete this item
    * @param delete_forced if true deletion is "forced"
    * @return
    */
   public final boolean can_delete ( boolean delete_forced )
      {
      if ( delete_forced ) return true;
      
      if ( is_delete_fixed() ) return false;
      
      if ( is_user_fixed() ) return false;
      
      return true;
      }

   /**
    * Returns true, if it is not allowed to change this item except evtl. shoving the item
    */
   public final boolean is_user_fixed()
      {
      return (fixed_state.ordinal() >= ItemFixState.USER_FIXED.ordinal());
      }

   /**
    * Returns true, if it is not allowed to delete this item.
    */
   public final boolean is_delete_fixed()
      {
      // Items belonging to a component are delete_fixed.
      if (component_no > 0 ) return true;
      
      if ( is_user_fixed()) return true;

      if ( !( this instanceof BrdAreaConduction) ) return false;
      
      BrdAreaConduction conduct = (BrdAreaConduction)this;

      int my_layer_no = conduct.get_layer();
      
      // Also power planes are delete_fixed
      if ( ! r_board.layer_structure.is_signal(my_layer_no)) return true;
      
      return false;
      }

   /**
    * Returns true, if it is not allowed to change the location of this item by the push algorithm.
    * Override in classes
    */
   public boolean is_shove_fixed()
      {
      return (fixed_state.ordinal() >= ItemFixState.SHOVE_FIXED.ordinal());
      }

   /**
    * @return the fixed state of this Item.
    */
   public final ItemFixState get_fixed_state()
      {
      return fixed_state;
      }

   /**
    * Overridden in subclasses
    * @return false, if this item is an obstacle for vias with the input net number.
    */
   public boolean is_drillable(int p_net_no)
      {
      return false;
      }

   /**
    * Fixes the item.
    */
   public final void set_fixed_state(ItemFixState p_fixed_state)
      {
      fixed_state = p_fixed_state;
      }

   /**
    * Unfixes the item, if it is not fixed by the system.
    */
   public final void unfix()
      {
      if (fixed_state != ItemFixState.SYSTEM_FIXED)
         {
         fixed_state = ItemFixState.UNFIXED;
         }
      }

   /**
    * @return true, if this item is an unfixed trace or via
    * To be overriden
    */
   public boolean is_route()
      {
      return false;
      }

   /**
    * @return if this item can be routed to
    */
   public final boolean is_connectable()
      {
      return ((this instanceof BrdConnectable) && net_count() > 0);
      }

   /**
    * Returns the count of nets this item belongs to.
    */
   public final int net_count()
      {
      return net_no_arr.length;
      }

   /**
    * gets the p_no-the net number of this item for 0 <= p_no < this.net_count().
    */
   public final int get_net_no(int p_no)
      {
      return net_no_arr[p_no];
      }

   /**
    * Return the component number of this item or 0, if it does not belong to a component.
    */
   public final int get_component_no()
      {
      return component_no;
      }

   /**
    * Removes p_net_no from the net number array. Returns false, if p_net_no was not contained in this array.
    */
   public final boolean remove_from_net(int p_net_no)
      {
      int found_index = -1;
      for (int i = 0; i < net_no_arr.length; ++i)
         {
         if ( net_no_arr[i] == p_net_no)
            {
            found_index = i;
            }
         }
      
      if (found_index < 0) return false;
      
      int[] new_net_no_arr = new int[net_no_arr.length - 1];
      
      for (int i = 0; i < found_index; ++i)
         {
         new_net_no_arr[i] = net_no_arr[i];
         }
      
      for (int i = found_index; i < new_net_no_arr.length; ++i)
         {
         new_net_no_arr[i] = net_no_arr[i + 1];
         }
      
      this.net_no_arr = new_net_no_arr;
      
      return true;
      }

   /**
    * Returns the index in the clearance matrix describing the required spacing of this item to other items
    */
   public final int clearance_class_no()
      {
      return clearance_class;
      }

   /**
    * Sets the index in the clearance matrix describing the required spacing of this item to other items.
    */
   public final void set_clearance_class_no(int p_index)
      {
      if (p_index < 0 || p_index >= r_board.brd_rules.clearance_matrix.get_class_count())
         {
         System.err.println("Item.set_clearance_class_no: p_index out of range");
         return;
         }
      
      clearance_class = p_index;
      }

   /**
    * Changes the clearance class of this item and updates the search tree.
    */
   public final void change_clearance_class(int p_index)
      {
      if (p_index < 0 || p_index >= r_board.brd_rules.clearance_matrix.get_class_count())
         {
         System.err.println("Item.set_clearance_class_no: p_index out of range");
         return;
         }
      
      clearance_class = p_index;
      
      clear_derived_data();
      
      if ( r_board.search_tree_manager.is_clearance_compensation_used())
         {
         // reinsert the item into the search tree, because the compensated shape has changed.
         r_board.search_tree_manager.remove(this);
         r_board.search_tree_manager.insert(this);
         }
      }

   /**
    * Set this item to the component with the input component number.
    */
   public final void set_component_no(int p_no)
      {
      component_no = p_no;
      }

   /**
    * Makes this item connectable and assigns it to the input net. 
    * If p_net_no < 0, the net items net number will be removed and the item will no longer be connectable.
    */
   public final void set_net_no(int p_net_no)
      {
      if (! RuleNets.is_normal_net_no(p_net_no)) return;
      
      if (p_net_no > r_board.brd_rules.nets.max_net_no())
         {
         System.out.println("Item.assign_net_no: p_net_no to big");
         return;
         }
      
      r_board.item_list.save_for_undo(this);

      if (p_net_no <= 0)
         {
         net_no_arr = new int[0];
         }
      else
         {
         if (net_no_arr.length == 0)
            {
            net_no_arr = new int[1];
            }
         else if (net_no_arr.length > 1)
            {
            System.out.println("Item.assign_net_no: unexpected net_count > 1");
            }
         
         net_no_arr[0] = p_net_no;
         }
      }

   /**
    * Returns true, if p_item is contained in the input filter.
    */
   public abstract boolean is_selected_by_filter(ItemSelectionFilter p_filter);

   /**
    * Internally used for implementing the function is_selectrd_by_filter
    */
   protected final boolean is_selected_by_fixed_filter(ItemSelectionFilter p_filter)
      {
      if ( is_user_fixed() )
         return p_filter.is_selected(ItemSelectionChoice.FIXED);
      else
         return p_filter.is_selected(ItemSelectionChoice.UNFIXED);
      }

   /**
    * Sets the item tree entries for the tree with identification number p_tree_no.
    */
   public final void set_search_tree_entries(ShapeTreeLeaf[] p_tree_entries, ShapeTree p_tree)
      {
      search_trees_info.set_tree_entries(p_tree_entries, p_tree);
      }

   /**
    * Returns the tree entries for the tree with identification number p_tree_no, or null, if for this tree no entries of this item
    * are inserted.
    */
   public final ShapeTreeLeaf[] get_search_tree_entries(ShapeSearchTree p_tree)
      {
      return search_trees_info.get_tree_entries(p_tree);
      }

   /**
    * Sets the precalculated tree shapes tree entries for the tree with identification number p_tree_no.
    */
   public final void set_precalculated_tree_shapes(ShapeTile[] p_shapes, ShapeSearchTree p_tree)
      {
      search_trees_info.set_precalculated_tree_shapes(p_shapes, p_tree);
      }

   /**
    * Sets the search tree entries of this item to empty
    */
   public final void clear_search_tree_entries()
      {
      search_trees_info.clear();
      }

   /**
    * Gets the information for the autoroute algorithm. 
    * Creates it, if it does not yet exist.
    */
   public final ArtItem art_item_get()
      {
      if (art_item != null) return art_item; 
         
      art_item = new ArtItem(this);

      return art_item;
      }

   /**
    * Clears the data allocated for the autoroute algorithm.
    * Cannot make it final since a subclass override it
    */
   public void art_item_clear()
      {
      art_item = null;
      }

   /**
    * Clear all cached or derived data. so that they have to be recalculated, when they are used next time.
    * Cannot make it final since subclass override it
    */
   public void clear_derived_data()
      {
      search_trees_info.clear_precalculated_tree_shapes();
      
      art_item_clear();
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_net_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      for (int i = 0; i < this.net_count(); ++i)
         {
         p_window.append(", " + resources.getString("net") + " ");
         freert.rules.RuleNet curr_net = r_board.brd_rules.nets.get(this.get_net_no(i));
         p_window.append(curr_net.name, resources.getString("net_info"), curr_net);
         }
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_clearance_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      if (this.clearance_class > 0)
         {
         java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
         p_window.append(", " + resources.getString("clearance_class") + " ");
         String name = r_board.brd_rules.clearance_matrix.get_name(clearance_class);
         p_window.append(name, resources.getString("clearance_info"), r_board.brd_rules.clearance_matrix.get_row(clearance_class));
         }
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_fixed_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      if (fixed_state != ItemFixState.UNFIXED)
         {
         java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.FixedState", p_locale);
         p_window.append(", ");
         p_window.append(resources.getString(this.fixed_state.toString()));
         }
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_contact_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      Collection<BrdItem> contacts = this.get_normal_contacts();
      if (!contacts.isEmpty())
         {
         java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
         p_window.append(", " + resources.getString("contacts") + " ");
         Integer contact_count = contacts.size();
         p_window.append_items(contact_count.toString(), resources.getString("contact_info"), contacts);
         }
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_clearance_violation_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      Collection<BrdItemViolation> clearance_violations = this.clearance_violations();
      
      if ( clearance_violations.isEmpty() ) return;
      
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append(", ");
      Integer violation_count = clearance_violations.size();
      Collection<PrintableInfo> violations = new java.util.LinkedList<PrintableInfo>();
      violations.addAll(clearance_violations);
      p_window.append_objects(violation_count.toString(), resources.getString("violation_info"), violations);
      if (violation_count == 1)
         {
         p_window.append(" " + resources.getString("clearance_violation"));
         }
      else
         {
         p_window.append(" " + resources.getString("clearance_violations"));
         }
      }

   /**
    * Internal function used in the implementation of print_info
    */
   protected final void print_connectable_item_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      this.print_clearance_info(p_window, p_locale);
      this.print_fixed_info(p_window, p_locale);
      this.print_net_info(p_window, p_locale);
      this.print_contact_info(p_window, p_locale);
      this.print_clearance_violation_info(p_window, p_locale);
      }

   /**
    * Internal funktion used in the implementation of print_info
    */
   protected final void print_item_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      this.print_clearance_info(p_window, p_locale);
      this.print_fixed_info(p_window, p_locale);
      this.print_clearance_violation_info(p_window, p_locale);
      }

   /**
    * Checks, if all nets of this items are normal.
    */
   public final boolean is_nets_normal()
      {
      for (int index = 0; index < net_no_arr.length; ++index)
         {
         if (!RuleNets.is_normal_net_no(net_no_arr[index]))
            {
            return false;
            }
         }
      return true;
      }

   /**
    * Checks, if this item and p_other contain exactly the same net numbers.
    */
   public final boolean nets_equal(BrdItem p_other)
      {
      return nets_equal(p_other.net_no_arr);
      }

   /**
    * Checks, if this item contains exactly the nets in p_net_no_arr
    */
   public final boolean nets_equal(int[] p_net_no_arr)
      {
      if (net_no_arr.length != p_net_no_arr.length) return false;

      for (int curr_net_no : p_net_no_arr)
         {
         if ( ! contains_net(curr_net_no)) return false;
         }
      
      return true;
      }

   /**
    * Returns true, if the via is directly ob by a trace connected to a nearby SMD-pin. 
    * If p_ignore_items != null, contact traces in P-ignore_items are ignored.
    */
   public final boolean is_fanout_via(Set<BrdItem> p_ignore_items)
      {
      Collection<BrdItem> contact_list = this.get_normal_contacts();
      for (BrdItem curr_contact : contact_list)
         {
         if (curr_contact instanceof BrdAbitPin && curr_contact.first_layer() == curr_contact.last_layer() && curr_contact.get_normal_contacts().size() <= 1)
            {
            return true;
            }
         if (! (curr_contact instanceof BrdTrace)) continue;
         
         if (p_ignore_items != null && p_ignore_items.contains(curr_contact)) continue;

         BrdTrace curr_trace = (BrdTrace) curr_contact;
         
         if (curr_trace.get_length() >= PROTECT_FANOUT_LENGTH * curr_trace.get_half_width())
            continue;

         Collection<BrdItem> trace_contact_list = curr_trace.get_normal_contacts();
         for (BrdItem tmp_contact : trace_contact_list)
            {
            if (tmp_contact instanceof BrdAbitPin && curr_contact.first_layer() == curr_contact.last_layer() && tmp_contact.get_normal_contacts().size() <= 1)
               {
               return true;
               }
            if (tmp_contact instanceof BrdTracePolyline && tmp_contact.get_fixed_state() == ItemFixState.SHOVE_FIXED)
               {
               // look for shove fixed exit traces of SMD-pins
               BrdTracePolyline contact_trace = (BrdTracePolyline) tmp_contact;
               if (contact_trace.corner_count() == 2)
                  {
                  return true;
                  }
               }
            }
         }
      return false;
      }
   }
