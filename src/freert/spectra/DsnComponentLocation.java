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

import java.util.Map;

/** 
 * The structure of an entry in the list locations 
 */
public class DsnComponentLocation
   {
   public final String name;
   // the x- and the y-coordinate of the location
   public final double[] coor;

   // True, if the component is placed at the component side. Else the component is placed at the solder side.
   public final boolean is_front;

   // The rotation of the component in degree
   public final double rotation;

   // If true, the component cannot be moved
   public final boolean position_fixed;

   // The entries of this map are of type ItemClearanceInfo, the keys are the pin names.
   public final Map<String, DsnClearanceInfo> pin_infos;

   public final Map<String, DsnClearanceInfo> keepout_infos;

   public final Map<String, DsnClearanceInfo> via_keepout_infos;

   public final Map<String, DsnClearanceInfo> place_keepout_infos;

   public DsnComponentLocation(String p_name, double[] p_coor, boolean p_is_front, double p_rotation, boolean p_position_fixed, Map<String, DsnClearanceInfo> p_pin_infos,
         Map<String, DsnClearanceInfo> p_keepout_infos, Map<String, DsnClearanceInfo> p_via_keepout_infos, Map<String, DsnClearanceInfo> p_place_keepout_infos)
      {
      name = p_name;
      coor = p_coor;
      is_front = p_is_front;
      rotation = p_rotation;
      position_fixed = p_position_fixed;
      pin_infos = p_pin_infos;
      keepout_infos = p_keepout_infos;
      via_keepout_infos = p_via_keepout_infos;
      place_keepout_infos = p_place_keepout_infos;
      }
   }
