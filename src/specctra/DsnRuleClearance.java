package specctra;

import java.util.Collection;

public class DsnRuleClearance extends DsnRule
   {
   final double value;
   final Collection<String> clearance_class_pairs;

   public DsnRuleClearance(double p_value, Collection<String> p_class_pairs)
      {
      value = p_value;
      clearance_class_pairs = p_class_pairs;
      }
   }
