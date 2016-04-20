package specctra;

import java.util.Collection;

/**
 * Contains the result of the function read_area_scope. area_name or clearance_class_name may be null, which means they are not
 * provided.
 */
public class DsnScopeArea
   {
   String area_name; // may be generated later on, if area_name is null.
   final Collection<DsnShape> shape_list;
   final String clearance_class_name;

   public DsnScopeArea(String p_area_name, Collection<DsnShape> p_shape_list, String p_clearance_class_name)
      {
      area_name = p_area_name;
      shape_list = p_shape_list;
      clearance_class_name = p_clearance_class_name;
      }
   }

