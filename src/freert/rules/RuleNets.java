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
 * Nets.java
 *
 * Created on 9. Juni 2004, 10:24
 */
package freert.rules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import board.RoutingBoard;

/**
 * Describes the electrical Nets on a board.
 *
 * @author alfons
 */
public final class RuleNets implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   // The maximum legal net number for nets
   public static final int max_legal_net_no = 9999999;
   // auxiliary net number for internal use 
   public static final int HIDDEN_NET_NO = 10000001;
   
   // The list of electrical nets on the board
   private final Vector<RuleNet> net_list = new Vector<RuleNet>();
   
   private RoutingBoard r_board;

   /**
    * Returns the biggest net number on the board.
    */
   public int max_net_no()
      {
      return net_list.size();
      }

   /**
    * @return the net with the input name and subnet_number , or null, if no such net exists. 
    */
   public RuleNet get(String p_name, int p_subnet_number)
      {
      if ( p_name == null ) return null;
      
      for (RuleNet curr_net : net_list)
         {
         if ( curr_net.name.compareToIgnoreCase(p_name) != 0) continue;
         
         if (curr_net.subnet_number == p_subnet_number) return curr_net;
         }
      
      return null;
      }

   /**
    * @return all subnets with the input name.
    */
   public Collection<RuleNet> get(String p_name)
      {
      Collection<RuleNet> result = new LinkedList<RuleNet>();
      
      if ( p_name == null ) return result;
      
      for (RuleNet curr_net : net_list)
         {
         if ( curr_net.name.compareToIgnoreCase(p_name) == 0)  result.add(curr_net);
         }
      
      return result;
      }

   /**
    * Returns the net with the input net number or null, if no such net exists.
    */
   public RuleNet get(int p_net_no)
      {
      if (p_net_no < 1 || p_net_no > net_list.size())
         {
         return null;
         }
      
      RuleNet result = net_list.elementAt(p_net_no - 1);
      
      if ( result.net_number != p_net_no)
         {
         throw new IllegalArgumentException("Nets.get: inconsistent net_no");
         }
      
      return result;
      }

   /**
    * Generates a new net number
    * Forget about translating the new#}
    */
   public RuleNet new_net()
      {
      int net_idx = net_list.size() + 1;
      String net_name = "net#" + net_idx;
      return add(net_name, 1, false);
      }

   /**
    * Adds a new net with default properties with the input name. 
    * p_subnet_number is used only if a net is divided internally
    * because of fromto rules for example. For normal nets it is always 1.
    */
   public RuleNet add(String p_name, int p_subnet_number, boolean p_contains_plane)
      {
      int new_net_no = net_list.size() + 1;

      if (new_net_no >= max_legal_net_no)
         throw new IllegalArgumentException("Nets.add_net: max_net_no out of range");
      
      RuleNet new_net = new RuleNet(p_name, p_subnet_number, new_net_no, this, p_contains_plane);

      net_list.add(new_net);
      
      return new_net;
      }

   /**
    * @return false, if p_net_no belongs to a net internally used for special purposes.
    */
   public static boolean is_normal_net_no(int p_net_no)
      {
      return (p_net_no > 0 && p_net_no <= max_legal_net_no);
      }

   /**
    * Sets the Board of this net list. Used for example to get access to the Items of the net.
    */
   public void set_board(RoutingBoard p_board)
      {
      r_board = p_board;
      }

   /**
    * Gets the Board of this net list. Used for example to get access to the Items of the net.
    */
   public RoutingBoard get_board()
      {
      return r_board;
      }
   }
