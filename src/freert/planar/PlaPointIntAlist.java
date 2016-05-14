package freert.planar;

import java.util.ArrayList;

/**
 * Needed simply because erasure makes impossible to differentiate a constructor with ArrayList <xxx> and ArrayList <yyy>
 * @author damiano
 */
public final class PlaPointIntAlist
   {
   private final ArrayList<PlaPointInt>a_list;
   
   public PlaPointIntAlist(int size)
      {
      a_list = new ArrayList<PlaPointInt>(size);
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
   }
