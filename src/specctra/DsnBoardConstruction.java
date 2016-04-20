package specctra;

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
