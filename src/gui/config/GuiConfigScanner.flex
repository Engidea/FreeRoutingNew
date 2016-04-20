package gui.config;
@SuppressWarnings("all")
%%

%class GuiConfigScanner
%public
%unicode
%ignorecase 
%function next_token
%type Object
/* %debug */

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]


/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "#" {InputCharacter}* {LineTerminator}

Letter=[A-Za-z]
Digit=[0-9]

DecIntegerLiteral =  ([+-]? (0 | [1-9][0-9]*))

Mantissa = ([+-]? [0-9]+ ("." [0-9]+)?)

Exponent = ([Ee] {DecIntegerLiteral})

DecFloatLiteral = {Mantissa} {Exponent}?

SpecChar = _


Identifier = ({Letter}|{SpecChar})({Letter}|{Digit}|{SpecChar})* 

%%

/* keywords */
<YYINITIAL> {
   "all_visible"               { return GuiConfigKeyword.ALL_VISIBLE; }
   "assign_net_rules"          { return GuiConfigKeyword.ASSIGN_NET_RULES; }
   "automatic_layer_dimming"   { return GuiConfigKeyword.AUTOMATIC_LAYER_DIMMING; }
   "background"                { return GuiConfigKeyword.BACKGROUND; }
   "board_frame"               { return GuiConfigKeyword.BOARD_FRAME; }
   "bounds"                    { return GuiConfigKeyword.BOUNDS; }
   "clearance_compensation"    { return GuiConfigKeyword.CLEARANCE_COMPENSATION; }
   "clearance_matrix"          { return GuiConfigKeyword.CLEARANCE_MATRIX; }
   "colors"                    { return GuiConfigKeyword.COLORS; }
   "color_manager"             { return GuiConfigKeyword.COLOR_MANAGER; }
   "component_back"            { return GuiConfigKeyword.COMPONENT_BACK; }
   "component_front"           { return GuiConfigKeyword.COMPONENT_FRONT; }
   "component_grid"            { return GuiConfigKeyword.COMPONENT_GRID; }
   "component_info"            { return GuiConfigKeyword.COMPONENT_INFO; }
   "conduction"                { return GuiConfigKeyword.CONDUCTION; }
   "current_layer"             { return GuiConfigKeyword.CURRENT_LAYER; }
   "current_only"              { return GuiConfigKeyword.CURRENT_ONLY; }
   "deselected_snapshot_attributes" { return GuiConfigKeyword.DESELECTED_SNAPSHOT_ATTRIBUTES; }
   "display_miscellanious"     { return GuiConfigKeyword.DISPLAY_MISCELLANIOUS; }
   "display_region"            { return GuiConfigKeyword.DISPLAY_REGION; }
   "drag_components_enabled"   { return GuiConfigKeyword.DRAG_COMPONENTS_ENABLED; }
   "dynamic"                   { return GuiConfigKeyword.DYNAMIC; }
   "edit_net_rules"            { return GuiConfigKeyword.EDIT_NET_RULES; }
   "edit_vias"                 { return GuiConfigKeyword.EDIT_VIAS; }
   "fixed"                     { return GuiConfigKeyword.FIXED; }
   "fixed_traces"              { return GuiConfigKeyword.FIXED_TRACES; }
   "fixed_vias"                { return GuiConfigKeyword.FIXED_VIAS; }
   "fortyfive_degree"          { return GuiConfigKeyword.FORTYFIVE_DEGREE; }
   "gui_defaults"              { return GuiConfigKeyword.GUI_DEFAULTS; }
   "hilight"                   { return GuiConfigKeyword.HILIGHT; }
   "hilight_routing_obstacle"  { return GuiConfigKeyword.HILIGHT_ROUTING_OBSTACLE; }
   "ignore_conduction_areas"   { return GuiConfigKeyword.IGNORE_CONDUCTION_AREAS; }
   "incompletes"               { return GuiConfigKeyword.INCOMPLETES; }
   "incompletes_info"          { return GuiConfigKeyword.INCOMPLETES_INFO; }
   "interactive_state"         { return GuiConfigKeyword.INTERACTIVE_STATE; }
   "keepout"                   { return GuiConfigKeyword.KEEPOUT; }
   "layer_visibility"          { return GuiConfigKeyword.LAYER_VISIBILITY; }
   "length_matching"           { return GuiConfigKeyword.LENGTH_MATCHING; }
   "manual_rules"              { return GuiConfigKeyword.MANUAL_RULES; }
   "manual_rule_settings"      { return GuiConfigKeyword.MANUAL_RULE_SETTINGS; }
   "move_parameter"            { return GuiConfigKeyword.MOVE_PARAMETER; }
   "net_info"                  { return GuiConfigKeyword.NET_INFO; }
   "ninety_degree"             { return GuiConfigKeyword.NINETY_DEGREE; }
   "none"                      { return GuiConfigKeyword.NONE; }
   "not_visible"               { return GuiConfigKeyword.NOT_VISIBLE; }
   "object_colors"             { return GuiConfigKeyword.OBJECT_COLORS; }
   "object_visibility"         { return GuiConfigKeyword.OBJECT_VISIBILITY; }
   "off"                       { return GuiConfigKeyword.OFF; }
   "on"                        { return GuiConfigKeyword.ON; }
   "outline"                   { return GuiConfigKeyword.OUTLINE; }
   "package_info"              { return GuiConfigKeyword.PACKAGE_INFO; }
   "padstack_info"             { return GuiConfigKeyword.PADSTACK_INFO; }
   "parameter"                 { return GuiConfigKeyword.PARAMETER; }
   "pins"                      { return GuiConfigKeyword.PINS; }
   "pull_tight_accuracy"       { return GuiConfigKeyword.PULL_TIGHT_ACCURACY; }
   "pull_tight_region"         { return GuiConfigKeyword.PULL_TIGHT_REGION; }
   "push_and_shove_enabled"    { return GuiConfigKeyword.PUSH_AND_SHOVE_ENABLED; }
   "route_details"             { return GuiConfigKeyword.ROUTE_DETAILS; }
   "route_mode"                { return GuiConfigKeyword.ROUTE_MODE; }
   "route_parameter"           { return GuiConfigKeyword.ROUTE_PARAMETER; }
   "autoroute_parameter"       { return GuiConfigKeyword.AUTOROUTE_PARAMETER; }
   "rule_selection"            { return GuiConfigKeyword.RULE_SELECTION; }
   "select_parameter"          { return GuiConfigKeyword.SELECT_PARAMETER; }
   "selectable_items"          { return GuiConfigKeyword.SELECTABLE_ITEMS; }
   "selection_layers"          { return GuiConfigKeyword.SELECTION_LAYERS; }
   "snapshots"                 { return GuiConfigKeyword.SNAPSHOTS; }
   "shove_enabled"             { return GuiConfigKeyword.SHOVE_ENABLED; }
   "stitching"                 { return GuiConfigKeyword.STITCHING; }
   "traces"                    { return GuiConfigKeyword.TRACES; }
   "unfixed"                   { return GuiConfigKeyword.UNFIXED; }
   "via_keepout"               { return GuiConfigKeyword.VIA_KEEPOUT; }
   "vias"                      { return GuiConfigKeyword.VIAS; }
   "via_rules"                 { return GuiConfigKeyword.VIA_RULES; }
   "via_snap_to_smd_center"    { return GuiConfigKeyword.VIA_SNAP_TO_SMD_CENTER; }
   "violations"                { return GuiConfigKeyword.VIOLATIONS; }
   "violations_info"           { return GuiConfigKeyword.VIOLATIONS_INFO; }
   "visible"                   { return GuiConfigKeyword.VISIBLE; }
   "windows"                   { return GuiConfigKeyword.WINDOWS; }
   "("                         { return GuiConfigKeyword.OPEN_BRACKET; }
   ")"                         { return GuiConfigKeyword.CLOSED_BRACKET; }
}

<YYINITIAL> {
  /* identifiers */ 
  {Identifier}                   { return yytext(); }
 
  /* literals */
  {DecIntegerLiteral}            { return new Integer(yytext()); }
  {DecFloatLiteral}              { return new Double(yytext()); }

  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

/* error fallback */
.|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+">"); }
