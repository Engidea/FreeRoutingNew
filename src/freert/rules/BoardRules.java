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
 * BoardRules.java
 *
 * Created on 1. Juni 2004, 07:16
 */

package freert.rules;

import freert.library.LibPadstack;
import freert.planar.ShapeConvex;
import freert.varie.ItemClass;
import interactive.IteraBoard;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import board.BrdLayerStructure;
import board.infos.BrdViaInfo;
import board.infos.BrdViaInfoList;
import board.items.BrdItem;
import board.varie.TraceAngleRestriction;

/**
 * Contains the rules and constraints required for items to be inserted into a routing board
 *
 * @author Alfons Wirtz
 */
public final class BoardRules implements Serializable
   {
   private static final long serialVersionUID = 1L;
   public static final int clearance_null_idx=0;
   public static final int clearance_default_idx=1;

   private final BrdLayerStructure layer_structure;
   
   // The matrix describing the spacing restrictions between item clearance classes.
   public final ClearanceMatrix clearance_matrix;
   // Describes the electrical nets on the board.
   public final RuleNets nets= new RuleNets();
   public final BrdViaInfoList via_infos = new BrdViaInfoList();
   public final Vector<RuleViaInfoList> via_rules = new Vector<RuleViaInfoList>();
   public final NetClasses net_classes;

   
   // If true, the router ignores conduction areas.
   private boolean ignore_conduction = true;
   // The smallest of all default trace half widths 
   private int min_trace_half_width;
   /// The biggest of all default trace half widths 
   private int max_trace_half_width;
   // The minimum distance of the pad border to the first turn of a connected trace to a pin with restricted exit directions. If the
   // value is <= 0, there are no exit restrictions.
   private double pin_edge_to_turn_dist;

   // The angle restriction for traces: 90 degree, 45 degree or none. 
   private transient TraceAngleRestriction trace_angle_restriction;
   private transient IteraBoard itera_board;

   public BoardRules(IteraBoard p_itera_board, BrdLayerStructure p_layer_structure, ClearanceMatrix p_clearance_matrix)
      {
      itera_board = p_itera_board;
      
      net_classes = new NetClasses();
      layer_structure = p_layer_structure;
      clearance_matrix = p_clearance_matrix;
      trace_angle_restriction = TraceAngleRestriction.NONE;
      min_trace_half_width = 100000;
      max_trace_half_width = 100;
      }

   public void set_transient_item ( IteraBoard p_itera_board )
      {
      itera_board = p_itera_board;
      }
   
   /**
    * Returns the trace halfwidth used for routing with the input net on the input layer.
    */
   public int get_trace_half_width(int p_net_no, int p_layer)
      {
      RuleNet curr_net = nets.get(p_net_no);
      return curr_net.get_class().get_trace_half_width(p_layer);
      }

   /**
    * Returns true, if the trace widths used for routing for the input net are equal on all layers. If p_net_no < 0, the default
    * trace widths for all nets are checked.
    */
   public boolean trace_widths_are_layer_dependent(int p_net_no)
      {
      int compare_width = get_trace_half_width(p_net_no, 0);
      for (int iindex = 1; iindex < layer_structure.size(); ++iindex)
         {
         if (get_trace_half_width(p_net_no, iindex) != compare_width)
            {
            return true;
            }
         }
      return false;
      }

   /** Returns he smallest of all default trace half widths */
   public int get_min_trace_half_width()
      {
      return min_trace_half_width;
      }

   /** Returns he biggest of all default trace half widths */
   public int get_max_trace_half_width()
      {
      return max_trace_half_width;
      }

   /**
    * Changes the default trace halfwidth used for routing on the input layer.
    */
   public void set_default_trace_half_width(int p_layer, int p_value)
      {
      get_default_net_class().set_trace_half_width(p_layer, p_value);
      min_trace_half_width = Math.min(min_trace_half_width, p_value);
      max_trace_half_width = Math.max(max_trace_half_width, p_value);
      }

   public int get_default_trace_half_width(int p_layer)
      {
      return get_default_net_class().get_trace_half_width(p_layer);
      }

   /**
    * Changes the default trace halfwidth used for routing on all layers to the input value.
    */
   public void set_default_trace_half_widths(int p_value)
      {
      if (p_value <= 0)
         {
         System.out.println("BoardRules.set_trace_half_widths: p_value out of range");
         return;
         }
      get_default_net_class().set_trace_half_width(p_value);
      min_trace_half_width = Math.min(min_trace_half_width, p_value);
      max_trace_half_width = Math.max(max_trace_half_width, p_value);
      }

   /**
    * Returns the net rule used for all nets, for whichh no special rrule was set.
    */
   public NetClass get_default_net_class()
      {
      if (net_classes.count() <= 0)
         {
         // net rules not yet initialized
         create_default_net_class();
         }
      
      return net_classes.get(0);
      }

   /**
    * Returns an empty new net rule with an internally created name.
    */
   public NetClass get_new_net_class()
      {
      NetClass result = net_classes.append(layer_structure, clearance_matrix, itera_board);
      result.set_trace_clearance_class(get_default_net_class().get_trace_clearance_class());
      result.set_via_rule(get_default_via_rule());
      result.set_trace_half_width(get_default_net_class().get_trace_half_width(0));
      return result;
      }

   /**
    * Returns an empty new net rule with an internally created name.
    */
   public NetClass get_new_net_class(String p_name)
      {
      NetClass result = net_classes.append(p_name, layer_structure, clearance_matrix);
      result.set_trace_clearance_class(get_default_net_class().get_trace_clearance_class());
      result.set_via_rule(get_default_via_rule());
      result.set_trace_half_width(get_default_net_class().get_trace_half_width(0));
      return result;
      }

   /**
    * Create a default via rule for p_net_class with name p_name. If more than one via infos with the same layer range are found,
    * only the via info with the smmallest pad size is inserted.
    */
   public void create_default_via_rule(NetClass p_net_class, String p_name)
      {
      if (via_infos.count() == 0)
         {
         return;
         }
      // Add the rule containing all vias.
      RuleViaInfoList default_rule = new RuleViaInfoList(p_name);
      int default_via_cl_class = p_net_class.default_item_clearance_classes.get(ItemClass.VIA);
      for (int i = 0; i < via_infos.count(); ++i)
         {
         BrdViaInfo curr_via_info = via_infos.get(i);
         if (curr_via_info.get_clearance_class() == default_via_cl_class)
            {
            freert.library.LibPadstack curr_padstack = curr_via_info.get_padstack();
            int curr_from_layer = curr_padstack.from_layer();
            int curr_to_layer = curr_padstack.to_layer();
            BrdViaInfo existing_via = default_rule.get_layer_range(curr_from_layer, curr_to_layer);
            if (existing_via != null)
               {
               ShapeConvex new_shape = curr_padstack.get_shape(curr_from_layer);
               ShapeConvex existing_shape = existing_via.get_padstack().get_shape(curr_from_layer);
               if (new_shape.max_width() < existing_shape.max_width())
                  {
                  // The via with the smallest pad shape is preferred
                  default_rule.remove_via(existing_via);
                  default_rule.append_via(curr_via_info);
                  }
               }
            else
               {
               default_rule.append_via(curr_via_info);
               }
            }
         }
      via_rules.add(default_rule);
      p_net_class.set_via_rule(default_rule);
      }

   private void create_default_net_class()
      {
      // add the default net rule
      NetClass default_net_class = net_classes.append("default", layer_structure, clearance_matrix);
      int default_trace_half_width = 1500;
      default_net_class.set_trace_half_width(default_trace_half_width);
      default_net_class.set_trace_clearance_class(1);
      }

   /**
    * Appends a new net class initialized with default data and a default name.
    */
   public NetClass append_net_class()
      {
      NetClass new_class = net_classes.append(layer_structure, clearance_matrix, itera_board);
      NetClass default_class = net_classes.get(0);
      new_class.set_via_rule(default_class.get_via_rule());
      new_class.set_trace_half_width(default_class.get_trace_half_width(0));
      new_class.set_trace_clearance_class(default_class.get_trace_clearance_class());
      return new_class;
      }

   /**
    * Appends a new net class initialized with default data and returns that class. If a class with p_name exists, this class is
    * returned withoout appending a new class.
    */
   public NetClass append_net_class(String p_name)
      {
      NetClass found_class = net_classes.get(p_name);
      
      if (found_class != null) return found_class;
      
      NetClass new_class = net_classes.append(p_name, layer_structure, clearance_matrix);
      
      NetClass default_class = net_classes.get(0);
      
      new_class.default_item_clearance_classes = new DefaultItemClearanceClasses(default_class.default_item_clearance_classes);
      new_class.set_via_rule(default_class.get_via_rule());
      new_class.set_trace_half_width(default_class.get_trace_half_width(0));
      new_class.set_trace_clearance_class(default_class.get_trace_clearance_class());

      return new_class;
      }

   /**
    * Returns the default via rule for routing or null, if no via rule exists.
    */
   public RuleViaInfoList get_default_via_rule()
      {
      if (via_rules.isEmpty())
         {
         return null;
         }
      return via_rules.get(0);
      }

   /**
    * Returns the via rule wit name p_name, or null, if no such rule exists.
    */
   public RuleViaInfoList get_via_rule(String p_name)
      {
      for (RuleViaInfoList curr_rule : via_rules)
         {
         if (curr_rule.rule_name.equals(p_name))
            {
            return curr_rule;
            }
         }
      return null;
      }

   /**
    * Changes the clearance class index of all objects on the board with index p_from_no to p_to_no.
    */
   public void change_clearance_class_no(int p_from_no, int p_to_no, java.util.Collection<board.items.BrdItem> p_board_items)
      {
      for (board.items.BrdItem curr_item : p_board_items)
         {
         if (curr_item.clearance_idx() == p_from_no)
            {
            curr_item.set_clearance_idx(p_to_no);
            }
         }

      for (NetClass curr_net_class : net_classes )
         {
         if (curr_net_class.get_trace_clearance_class() == p_from_no)
            {
            curr_net_class.set_trace_clearance_class(p_to_no);
            }
         
         for (ItemClass curr_item_class : ItemClass.values())
            {
            if (curr_net_class.default_item_clearance_classes.get(curr_item_class) == p_from_no)
               {
               curr_net_class.default_item_clearance_classes.set(curr_item_class, p_to_no);
               }
            }
         }

      for (int index = 0; index < via_infos.count(); ++index)
         {
         BrdViaInfo curr_via = via_infos.get(index);
         if (curr_via.get_clearance_class() == p_from_no)
            {
            curr_via.set_clearance_class(p_to_no);
            }
         }
      }

   /**
    * Removes the clearance class with number p_index. Returns false, if that was not possible, because there were still items
    * assigned to this class.
    */
   public boolean remove_clearance_class(int p_index, java.util.Collection<board.items.BrdItem> p_board_items)
      {
      for (BrdItem curr_item : p_board_items)
         {
         if (curr_item.clearance_idx() == p_index) return false;
         }
      
      
      for (NetClass curr_net_class : net_classes )
         {
         if (curr_net_class.get_trace_clearance_class() == p_index) return false;

         for (ItemClass curr_item_class : ItemClass.values())
            {
            if (curr_net_class.default_item_clearance_classes.get(curr_item_class) == p_index)
               {
               return false;
               }
            }
         }

      for (BrdViaInfo curr_via : via_infos )
         {
         if (curr_via.get_clearance_class() == p_index) return false;
         }

      for ( BrdItem curr_item : p_board_items)
         {
         if (curr_item.clearance_idx() > p_index)
            {
            curr_item.set_clearance_idx(curr_item.clearance_idx() - 1);
            }
         }

      for (int i = 0; i < net_classes.count(); ++i)
         {
         freert.rules.NetClass curr_net_class = net_classes.get(i);
         if (curr_net_class.get_trace_clearance_class() > p_index)
            {
            curr_net_class.set_trace_clearance_class(curr_net_class.get_trace_clearance_class() - 1);
            }
         for (ItemClass curr_item_class : ItemClass.values())
            {
            int curr_class_no = curr_net_class.default_item_clearance_classes.get(curr_item_class);
            if (curr_class_no > p_index)
               {
               curr_net_class.default_item_clearance_classes.set(curr_item_class, curr_class_no - 1);
               }
            }
         }

      for (int i = 0; i < via_infos.count(); ++i)
         {
         BrdViaInfo curr_via = via_infos.get(i);
         if (curr_via.get_clearance_class() > p_index)
            {
            curr_via.set_clearance_class(curr_via.get_clearance_class() - 1);
            }
         }
      
      clearance_matrix.remove_class(p_index);
      
      return true;
      }

   /**
    * Returns the minimum distance between the pin border and the next corner of a connected trace for a pin with connection
    * restrictions. 
    * If the result is <= 0, there are no exit restrictions.
    */
   public double get_pin_edge_to_turn_dist()
      {
      return pin_edge_to_turn_dist;
      }

   /**
    * Sets he minimum distance between the pin border and the next corner of a connected trace por a pin with connection
    * restrictions. if p_value is <= 0, there are no exit restrictions
    * @return the  previous value so you can save away, if you wish
    */
   public double set_pin_edge_to_turn_dist(double p_value)
      {
      double risul = pin_edge_to_turn_dist; 
      pin_edge_to_turn_dist = p_value;
      return risul;
      }

   /**
    * Tells the router, if conduction areas should be ignored..
    */
   public void set_ignore_conduction(boolean p_value)
      {
      ignore_conduction = p_value;
      }

   /**
    * If true, the router ignores conduction areas.
    */
   public boolean get_ignore_conduction()
      {
      return ignore_conduction;
      }

   public TraceAngleRestriction get_trace_snap_angle()
      {
      return trace_angle_restriction;
      }

   public final boolean is_trace_snap_none ()
      {
      return trace_angle_restriction.is_limit_none();
      }

   public final boolean is_trace_snap_45 ()
      {
      return trace_angle_restriction.is_limit_45();
      }

   public final boolean is_trace_snap_90 ()
      {
      return trace_angle_restriction.is_limit_90();
      }

   public void set_trace_snap_angle(board.varie.TraceAngleRestriction p_angle_restriction)
      {
      // avoid setting a null value
      if ( p_angle_restriction == null ) return;
      
      trace_angle_restriction = p_angle_restriction;
      }

   /**
    * Returns the Maximum of the diameter of the default via on its first and last layer.
    * @return 0 if some sort of invalid situation
    */
   public double get_default_via_diameter()
      {
      RuleViaInfoList default_via_rule = get_default_via_rule();

      if (default_via_rule == null) return 0;

      if (default_via_rule.via_count() <= 0) return 0;

      LibPadstack via_padstack = default_via_rule.get_via(0).get_padstack();
      
      ShapeConvex curr_shape = via_padstack.get_shape(via_padstack.from_layer());
      
      double result = curr_shape.max_width();
      
      curr_shape = via_padstack.get_shape(via_padstack.to_layer());
      
      result = Math.max(result, curr_shape.max_width());
      
      return result;
      }

   /** 
    * Writes an instance of this class to a file 
    */
   private void writeObject(java.io.ObjectOutputStream p_stream) throws java.io.IOException
      {
      p_stream.defaultWriteObject();
      p_stream.writeInt(trace_angle_restriction.get_no());
      }

   /** 
    * Reads an instance of this class from a file 
    */
   private void readObject(ObjectInputStream p_stream) throws IOException, ClassNotFoundException
      {
      p_stream.defaultReadObject();
      int snap_angle_no = p_stream.readInt();
      trace_angle_restriction = TraceAngleRestriction.get_instance(snap_angle_no);
      }

   }
