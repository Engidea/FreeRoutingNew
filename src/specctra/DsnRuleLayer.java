package specctra;

import java.util.Collection;

public class DsnRuleLayer
   {
   DsnRuleLayer(Collection<String> p_layer_names, Collection<DsnRule> p_rules)
      {
      layer_names = p_layer_names;
      rules = p_rules;
      }

   final Collection<String> layer_names;
   final Collection<DsnRule> rules;
   }
