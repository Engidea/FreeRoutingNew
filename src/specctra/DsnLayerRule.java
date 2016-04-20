package specctra;

import java.util.Collection;

public class DsnLayerRule
   {
   DsnLayerRule(String p_layer_name, Collection<DsnRule> p_rule)
      {
      layer_name = p_layer_name;
      rule = p_rule;
      }

   final String layer_name;
   final Collection<DsnRule> rule;
   }
