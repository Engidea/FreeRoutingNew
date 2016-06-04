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
import java.util.LinkedList;

public class DsnBoardConstruction
   {
   String outline_clearance_class_name = null;
   DsnShape bounding_shape;
   int found_layer_count = 0;

   Collection<DsnLayer> layer_info = new LinkedList<DsnLayer>();
   LinkedList<DsnShape> outline_shapes = new LinkedList<DsnShape>();
   Collection<DsnRule> default_rules = new LinkedList<DsnRule>();
   Collection<DsnLayerRule> layer_dependent_rules = new LinkedList<DsnLayerRule>();
   }
