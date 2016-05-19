package freert.planar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Needed simply because erasure makes impossible to differentiate a constructor with ArrayList <xxx> and ArrayList <yyy>
 * @author damiano
 */
public final class PlaPointIntAlist implements Iterable<PlaPointInt>
   {
   private final ArrayList<PlaPointInt>a_list;
   
   public PlaPointIntAlist(int size)
      {
      a_list = new ArrayList<PlaPointInt>(size);
      }
   
   
   public PlaPointIntAlist(Collection<PlaPointInt> p_list )
      {
      a_list = new ArrayList<PlaPointInt>(p_list.size());
      
      a_list.addAll(p_list);
      }
   
   
   public PlaPointIntAlist(PlaPointInt [] point_arr )
      {
      a_list = new ArrayList<PlaPointInt>(point_arr.length);
      
      for (int index=0; index<point_arr.length; index++)
         a_list.add(point_arr[index]);
      }
   
   public PlaPointInt add(PlaPointInt avalue)
      {
      
      if ( avalue != null ) a_list.add(avalue);
      
      return avalue;
      }
   
   public PlaPointInt get(int index)
      {
      return a_list.get(index);
      }
   
   public int size ()
      {
      return a_list.size();
      }
   
   public void remove ( int index )
      {
      a_list.remove(index);
      }
   
   
   @Override
   public Iterator<PlaPointInt> iterator()
      {
      return a_list.iterator();
      }
   
   }
