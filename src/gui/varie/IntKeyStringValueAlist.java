package gui.varie;

import java.util.ArrayList;
import java.util.Iterator;

public final class IntKeyStringValueAlist implements Iterable<IntKeyStringValue>
   {
   public final ArrayList<IntKeyStringValue>a_list;
   
   public IntKeyStringValueAlist ( int initial_capacity )
      {
      a_list = new ArrayList<IntKeyStringValue>(initial_capacity);
      }
   
   public IntKeyStringValue get( int p_key )
      {
      for ( IntKeyStringValue a_row : a_list )
         if ( a_row.key == p_key ) return a_row;
      
      return null;
      }

   public void add ( int p_key, String p_value )
      {
      a_list.add(new IntKeyStringValue(p_key, p_value));
      }

   @Override
   public Iterator<IntKeyStringValue> iterator()
      {
      return a_list.iterator();
      }
   
   }
