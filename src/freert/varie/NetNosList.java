package freert.varie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import board.shape.ShapeTreeObject;

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
 * QUite a few of the functions require a list of net numbers to check on It is for various use, not necessarly for deny access
 * Using the raw type makes parameter checking quite cumbersome, so, let's wrap it up
 * Note that inner object visibility is temporary, untils the whole mess is fixed.
 * 
 * @author damiano
 *
 */
public final class NetNosList implements Iterable<Integer>
   {
   public final int[] net_nos_arr;

   public static final NetNosList EMPTY = new NetNosList();

   /**
    * If you want an empty net list just use the EMPTY
    */
   private NetNosList()
      {
      net_nos_arr = new int[0];
      }

   public NetNosList( int first_value)
      {
      net_nos_arr = new int[1];
      net_nos_arr[0] = first_value;
      }

   /**
    * It is ok just to "link" the given array and not copy it
    * @param p_nets_no
    */
   public NetNosList(int[] p_nets_no)
      {
      net_nos_arr = p_nets_no;
      }

   public NetNosList(ArrayList<Integer> net_nos)
      {
      net_nos_arr = new int[net_nos.size()];

      int index = 0;

      for (Integer an_int : net_nos)
         net_nos_arr[index++] = an_int.intValue();
      }
   
   public boolean is_empty()
      {
      return size() < 1;
      }

   public int size()
      {
      return net_nos_arr.length;
      }

   public int get ( int index )
      {
      return net_nos_arr[index];
      }
   
   public int first ()
      {
      return get(0);
      }
   
   
   @Override
   public Iterator<Integer> iterator()
      {
      return new RangeIterator();
      }

   /**
    * If any of the net is considered an obstacle then return true 
    * @param p_obj
    * @return
    */
   public boolean is_obstacle(ShapeTreeObject p_obj)
      {
      for (int index = 0; index < net_nos_arr.length; ++index)
         {
         if (p_obj.is_obstacle(net_nos_arr[index])) return true;
         }
      
      return false;
      }

   /**
    * If there is at least one net that is not an obstacle then return true
    * @param p_obj
    * @return
    */
   public boolean is_connectable(ShapeTreeObject p_obj)
      {
      for (int index = 0; index < net_nos_arr.length; ++index)
         {
         if ( ! p_obj.is_obstacle(net_nos_arr[index])) return true;
         }
      
      return false;
      }

   /**
    * Look int the array if it has the given net no
    * @param p_net_no
    * @return
    */
   public boolean has_net_no(int p_net_no)
      {
      if ( p_net_no < 0 ) return false;
      
      int array_len = net_nos_arr.length;
      
      for (int index = 0; index < array_len; ++index)
         {
         if (net_nos_arr[index] == p_net_no) return true;
         }

      return false;
      }

   /**
    * checks if the list of nets is in my list of nets
    * @param p_net_nos_b
    * @return
    */
   public boolean net_nos_equal( int[] p_net_nos_b)
      {
      if (net_nos_arr.length != p_net_nos_b.length ) return false;
      
      for (int cur_net_b : p_net_nos_b )
         {
         if ( ! has_net_no( cur_net_b) ) return false;
         }
      
      return true;
      }

   public boolean net_nos_equal( NetNosList p_net_nos )
      {
      return net_nos_equal(p_net_nos.net_nos_arr);
      }
   
   /**
    * Returns true if there is at least one net common
    * @param p_net_no_arr
    * @return
    */
   public final boolean shares_net_no(int[] p_net_no_arr)
      {
      for (int want_net_no : p_net_no_arr)
         {
         if ( has_net_no (want_net_no )) return true;
         }

      return false;
      }
   
   /**
    * If the given net_no is foundin the list returns a new object with that element less, otherwise null
    * 
    * @param net_no
    * @return
    */
   public NetNosList remove_from_net(int net_no)
      {
      int current_len = net_nos_arr.length;

      ArrayList<Integer> risul = new ArrayList<Integer>(current_len);

      for (int index = 0; index < current_len; index++)
         {
         int a_net_no = net_nos_arr[index];

         if (a_net_no == net_no) continue;

         risul.add(a_net_no);
         }

      // we did not remove anything, therefore the value is not in the list
      if (risul.size() == current_len) return null;

      return new NetNosList(risul);
      }

   public int [] to_array ()
      {
      int []risul = new int[net_nos_arr.length];
      
      for (int index = 0; index < net_nos_arr.length; index++)
         risul[index] = net_nos_arr[index];

      return risul;
      }
   
   
   
private class RangeIterator implements Iterator<Integer>
   {
   private int cursor;

   public RangeIterator()
      {
      cursor = 0;
      }

   public boolean hasNext()
      {
      return cursor < net_nos_arr.length;
      }

   public Integer next()
      {
      if ( ! hasNext()) throw new NoSuchElementException();

      return cursor++;
      }

   public void remove()
      {
      throw new UnsupportedOperationException();
      }
   }

   }
