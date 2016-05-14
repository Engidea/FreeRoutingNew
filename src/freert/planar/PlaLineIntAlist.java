package freert.planar;

import java.util.ArrayList;

/**
 * Needed simply because erasure makes impossible to differentiate a constructor with ArrayList <xxx> and ArrayList <yyy>
 * @author damiano
 */
public final class PlaLineIntAlist
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
      
      if ( avalue != null ) a_list.add(avalue);
      
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
   }
