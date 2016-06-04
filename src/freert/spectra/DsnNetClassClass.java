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
 * To be renamed into DsnNetClassClass
 */
public class DsnNetClassClass
   {
   public final Collection<String> class_names;
   public final Collection<DsnRule> rules;
   public final Collection<DsnRuleLayer> layer_rules;

   public DsnNetClassClass(Collection<String> p_class_names, Collection<DsnRule> p_rules, Collection<DsnRuleLayer> p_layer_rules)
      {
      class_names = p_class_names;
      rules = p_rules;
      layer_rules = p_layer_rules;
      }
   }
