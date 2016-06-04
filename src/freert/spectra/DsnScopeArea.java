package freert.spectra;
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

