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
   
   public PlaPointInt add(PlaPointInt a_point)
      {
      if ( a_point == null ) return null;
      
      a_list.add(a_point);
      
      return a_point;
      }
   
   public PlaPointInt get(int index)
      {
      return a_list.get(index);
      }
   
   public PlaPointInt get_last()
      {
      if ( size() < 1 ) return null;
      
      return get(size(-1));
      }

   public int size ()
      {
      return a_list.size();
      }
   
   public int size ( int offset )
      {
      return size()+offset;
      }

   public void remove ( int index )
      {
      a_list.remove(index);
      }

   /**
    * Returns true if the given point is equal to the last point in the list
    * @param a_point
    * @return
    */
   public boolean is_equal_last ( PlaPointInt a_point )
      {
      if ( a_point == null ) return false;
      
      PlaPointInt last = get_last();

      if ( last == null ) return false;
      
      return last.equals(a_point);
      }
   
   @Override
   public Iterator<PlaPointInt> iterator()
      {
      return a_list.iterator();
      }
   
   }
