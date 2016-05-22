package freert.spectra;

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
