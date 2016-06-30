package freert.planar;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import board.varie.BrdChangedArea;

/**
 * Needed simply because erasure makes impossible to differentiate a constructor with ArrayList <xxx> and ArrayList <yyy>
 * @author damiano
 */
public final class PlaLineIntAlist implements Iterable<PlaLineInt>
   {
   private final ArrayList<PlaLineInt>a_list;
   
   public PlaLineIntAlist(int size)
      {
      a_list = new ArrayList<PlaLineInt>(size);
      }
   
   public PlaLineIntAlist(PlaLineInt l1, PlaLineInt l2, PlaLineInt l3)
      {
      a_list = new ArrayList<PlaLineInt>(3);
      add(l1);
      add(l2);
      add(l3);
      }
   
   
   /**
    * Create a new instance by copying the collection to this Alist
    * @param p_list
    */
   public PlaLineIntAlist(Collection<PlaLineInt> p_list)
      {
      int list_len = p_list.size();
      a_list = new ArrayList<PlaLineInt>(list_len);
      addAll(p_list);
      }

   public void addAll(Collection<PlaLineInt> p_list )
      {
      a_list.addAll(p_list);
      }
   
   public PlaLineIntAlist(PlaLineInt [] point_arr )
      {
      a_list = new ArrayList<PlaLineInt>(point_arr.length);
      
      for (int index=0; index<point_arr.length; index++)
         a_list.add(point_arr[index]);
      }
   
   public PlaLineInt add(PlaLineInt avalue)
      {
      if ( avalue == null ) return null;
      
      a_list.add(avalue);
      
      return avalue;
      }
   
   public PlaLineInt add(int index, PlaLineInt avalue)
      {
      if ( avalue == null ) return null;
      
      a_list.add(index, avalue);
      
      return avalue;
      }
   

   public void add_null (int index)
      {
      a_list.add(index, null);
      }

   public PlaLineInt set(int index, PlaLineInt avalue)
      {
      if ( avalue == null ) return null;
      
      a_list.set(index, avalue);
      
      return avalue;
      }

   
   
   public PlaLineInt get(int index)
      {
      return a_list.get(index);
      }
   
   public int size ()
      {
      return a_list.size();
      }
   
   public int size (int offset)
      {
      return size()+offset;
      }

   public PlaLineInt[] to_array()
      {
      return a_list.toArray(new PlaLineInt[size()]);
      }
   
   /**
    * Return the actual back storage, it is not a copy
    * @return
    */
   public ArrayList<PlaLineInt>to_alist()
      {
      return a_list;
      }
   
   @Override
   public Iterator<PlaLineInt> iterator()
      {
      return a_list.iterator();
      }
   
   /**
    * Append to dest the remaining lines starting from pos
    * If src_pos is zero and dest is empty this actually copy the list
    * @param dest
    * @param src_pos
    */
   public void append_to(PlaLineIntAlist dest, int src_pos )
      {
      int poly_len = size();
      
      for (int index=src_pos; index<poly_len; index++)
         dest.add(get(index));
      }
   
   public void append_to(PlaLineIntAlist dest, int src_pos, int length )
      {
      for (int index=0; index<length; index++)
         dest.add( get(src_pos+index));
      }
   
   /**
    * Add to the p_area the corner that results from crossing line p_index with p_index+1
    * @param p_area
    * @param p_index
    * @param p_layer
    */
   public void changed_area_join_corner (BrdChangedArea p_area, int p_index, int p_layer )
      {
      PlaPointFloat corner = get(p_index).intersection_approx(get(p_index + 1));
      
      if ( corner.is_NaN() ) return;
      
      p_area.join(corner, p_layer);
      }

   }
