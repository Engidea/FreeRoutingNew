package freert.planar;

import java.util.ArrayList;
import java.util.Iterator;

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
   
   @Override
   public Iterator<PlaLineInt> iterator()
      {
      return a_list.iterator();
      }
   
   }
