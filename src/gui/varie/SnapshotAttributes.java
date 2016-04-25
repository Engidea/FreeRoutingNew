package gui.varie;

/**
 * Defines the data of the snapshot selected for restoring.
 */
public final class SnapshotAttributes implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public boolean object_colors;
   public boolean object_visibility;
   public boolean layer_visibility;
   public boolean display_region;
   public boolean interactive_state;
   public boolean selection_layers;
   public boolean selectable_items;
   public boolean current_layer;
   public boolean rule_selection;
   public boolean manual_rule_settings;
   public boolean push_and_shove_enabled;
   public boolean drag_components_enabled;
   public boolean pull_tight_region;
   public boolean component_grid;
   public boolean info_list_selections;
   
   public SnapshotAttributes()
      {
      object_colors = true;
      object_visibility = true;
      layer_visibility = true;
      display_region = true;
      interactive_state = true;
      selection_layers = true;
      selectable_items = true;
      current_layer = true;
      rule_selection = true;
      manual_rule_settings = true;
      push_and_shove_enabled = true;
      drag_components_enabled = true;
      pull_tight_region = true;
      component_grid = true;
      info_list_selections = true;
      }

   public void copy(SnapshotAttributes p_attributes)
      {
      object_colors = p_attributes.object_colors;
      object_visibility = p_attributes.object_visibility;
      layer_visibility = p_attributes.layer_visibility;
      display_region = p_attributes.display_region;
      interactive_state = p_attributes.interactive_state;
      selection_layers = p_attributes.selection_layers;
      selectable_items = p_attributes.selectable_items;
      current_layer = p_attributes.current_layer;
      rule_selection = p_attributes.rule_selection;
      manual_rule_settings = p_attributes.manual_rule_settings;
      push_and_shove_enabled = p_attributes.push_and_shove_enabled;
      drag_components_enabled = p_attributes.drag_components_enabled;
      pull_tight_region = p_attributes.pull_tight_region;
      component_grid = p_attributes.component_grid;
      info_list_selections = p_attributes.info_list_selections;
      }
   }
