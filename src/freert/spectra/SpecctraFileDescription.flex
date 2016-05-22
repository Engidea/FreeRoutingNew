package designformats.specctra;
@SuppressWarnings("all")
%%

%class DsnFileScanner
%implements JflexScanner
%unicode
%ignorecase 
%function next_token
%type Object
/* %debug */

%{
  StringBuffer string = new StringBuffer();
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "#" {InputCharacter}* {LineTerminator}

Letter=[A-Za-z]
Digit=[0-9]

/* Character used for quoting string */
QuoteChar1 = \"
QuoteChar2 = '

SpecCharASCII = _|\.|\/|\\|:|#|\$|&|>|<|,|;|=|@|\[|\]||\~|\*|\?|\!|\%|\^

SpecCharANSI1 = €|‚|ƒ|„|…|†|‡|ˆ|‰|Š|‹|Œ|Ž|‘|’|“|”|•|–|—|˜|™|š|›|œ|ž|Ÿ
SpecCharANSI2 = [¡-ÿ]
SpecCharANSI = {SpecCharANSI1}|{SpecCharANSI2}


SpecChar1 = {SpecCharASCII}|{SpecCharANSI}

SpecChar2 = {SpecChar1}|-|\+

SpecChar3 = {SpecChar2}|{QuoteChar1}|{QuoteChar2}

SpecChar4 = {SpecChar1}|\+

SpecChar5 = {SpecChar4}|{QuoteChar1}|{QuoteChar2}


DecIntegerLiteral =  ([+-]? (0 | [1-9][0-9]*))

Mantissa = ([+-]? [0-9]+ ("." [0-9]+)?)

Exponent = ([Ee] {DecIntegerLiteral})

DecFloatLiteral = {Mantissa} {Exponent}?

Identifier = ({Letter}|{SpecChar1})({Letter}|{Digit}|{SpecChar3})* 

NameIdentifier = ({Letter}|{Digit}|{SpecChar2})({Letter}|{Digit}|{SpecChar3})*

IdentifierIgnoringQuotes = ({Letter}|{Digit}|{SpecChar3})*

/* to divide the component name from the pin name with the character "-" */
ComponentIdentifier = ({Letter}|{Digit}|{SpecChar4})({Letter}|{Digit}|{SpecChar5})*

/* States used for qouting strings */
%state STRING1
%state STRING2

/* The state NAME is used if the next token has to be interpreted as string, even if it is a number */
%state NAME

/* The state LAYER_NAME is used if the next token has to be interpreted as a layer name */
%state LAYER_NAME

/* To divide a component name from the pin name with the charracter "-" */
%state COMPONENT_NAME

/* Returns the next character */
%state SPEC_CHAR

/* Reads the next identifier while handling the quote characters as normal characters */
%state IGNORE_QUOTE

%%


<YYINITIAL> {
   /* keywords */
   "absolute"      { return DsnKeyword.ABSOLUTE; }
   "active"        { return DsnKeyword.ACTIVE; }
   "against_preferred_direction_trace_costs" { return DsnKeyword.AGAINST_PREFERRED_DIRECTION_TRACE_COSTS; }
   "against_prefered_direction_trace_costs" { return DsnKeyword.AGAINST_PREFERRED_DIRECTION_TRACE_COSTS; }
   "attach"        { return DsnKeyword.ATTACH; }
   "autoroute"     { return DsnKeyword.AUTOROUTE; }
   "autoroute_settings" { return DsnKeyword.AUTOROUTE_SETTINGS; }
   "back"          { return DsnKeyword.BACK; }
   "boundary"      { return DsnKeyword.BOUNDARY; }
   "circ"          { yybegin(LAYER_NAME); return DsnKeyword.CIRCLE; }
   "circle"        { yybegin(LAYER_NAME); return DsnKeyword.CIRCLE; }
   "circuit"       { return DsnKeyword.CIRCUIT; }
   "class"         { yybegin(NAME); return DsnKeyword.CLASS; }
   "class_class"   { return DsnKeyword.CLASS_CLASS; }
   "classes"       { return DsnKeyword.CLASSES; }
   "clear"         { return DsnKeyword.CLEARANCE; }
   "clearance"     { return DsnKeyword.CLEARANCE; }
   "clearance_class" { yybegin(NAME); return DsnKeyword.CLEARANCE_CLASS; }
   "comp"          { yybegin(NAME); return DsnKeyword.COMPONENT_SCOPE; }
   "component"     { yybegin(NAME); return DsnKeyword.COMPONENT_SCOPE; }
   "constant"      { return DsnKeyword.CONSTANT; }
   "control"       { return DsnKeyword.CONTROL; }
   "fanout"        { return DsnKeyword.FANOUT; }
   "fix"           { return DsnKeyword.FIX; }
   "fortyfive_degree" { return DsnKeyword.FORTYFIVE_DEGREE; }
   "flip_style"    { return DsnKeyword.FLIP_STYLE; }
   "fromto"        { return DsnKeyword.FROMTO; }
   "front"         { return DsnKeyword.FRONT; }
   "generated_by_freeroute" {return DsnKeyword.GENERATED_BY_FREEROUTE; }
   "horizontal"    { return DsnKeyword.HORIZONTAL; }
   "image"         { yybegin(NAME); return DsnKeyword.IMAGE; }
   "host_cad"      { yybegin(NAME); return DsnKeyword.HOST_CAD; }
   "host_version"  { yybegin(NAME); return DsnKeyword.HOST_VERSION; }
   "keepout"       { yybegin(NAME); return DsnKeyword.KEEPOUT; }
   "layer"         { yybegin(NAME); return DsnKeyword.LAYER; }
   "layer_rule"    { yybegin(NAME); return DsnKeyword.LAYER_RULE; }
   "length"        { return DsnKeyword.LENGTH; }
   "library"       { return DsnKeyword.LIBRARY_SCOPE; }
   "lock_type"     { return DsnKeyword.LOCK_TYPE; }
   "protect"       { return DsnKeyword.PROTECT; }
   "logical_part"  { yybegin(NAME); return DsnKeyword.LOGICAL_PART; }
   "logical_part_mapping"  { yybegin(NAME); return DsnKeyword.LOGICAL_PART_MAPPING; }
   "net"           { yybegin(NAME); return DsnKeyword.NET; }
   "network"       { return DsnKeyword.NETWORK_SCOPE; }
   "network_out"   { return DsnKeyword.NETWORK_OUT; }
   "ninety_degree" { return DsnKeyword.NINETY_DEGREE; }
   "none"          { return DsnKeyword.NONE; }
   "normal"        { return DsnKeyword.NORMAL; }
   "off"           { return DsnKeyword.OFF; }
   "on"            { return DsnKeyword.ON; }
   "order"         { return DsnKeyword.ORDER; }
   "outline"       { return DsnKeyword.OUTLINE; }
   "padstack"      { yybegin(NAME); return DsnKeyword.PADSTACK; }
   "parser"        { return DsnKeyword.PARSER_SCOPE; }
   "part_library"  { return DsnKeyword.PART_LIBRARY_SCOPE; }
   "path"          { yybegin(LAYER_NAME); return DsnKeyword.POLYGON_PATH; }
   "pcb"           { return DsnKeyword.PCB_SCOPE; }
   "pin"           { return DsnKeyword.PIN; }
   "pins"          { return DsnKeyword.PINS; }
   "place"         { yybegin(NAME); return DsnKeyword.PLACE; }
   "place_control" { return DsnKeyword.PLACE_CONTROL; } 
   "place_keepout" { yybegin(NAME); return DsnKeyword.PLACE_KEEPOUT; }
   "placement"     { return DsnKeyword.PLACEMENT_SCOPE; }
   "plane"         { yybegin(NAME); return DsnKeyword.PLANE_SCOPE; }
   "plane_via_costs" { return DsnKeyword.PLANE_VIA_COSTS; }
   "poly"          { yybegin(LAYER_NAME); return DsnKeyword.POLYGON; }
   "polygon"       { yybegin(LAYER_NAME); return DsnKeyword.POLYGON; }
   "polyline_path" { yybegin(LAYER_NAME); return DsnKeyword.POLYLINE_PATH; }
   "position"      { return DsnKeyword.POSITION; }
   "postroute"      { return DsnKeyword.POSTROUTE; }
   "power"         { return DsnKeyword.POWER; }
   "preferred_direction" { return DsnKeyword.PREFERRED_DIRECTION; }
   "prefered_direction" { return DsnKeyword.PREFERRED_DIRECTION; }
   "preferred_direction_trace_costs" { return DsnKeyword.PREFERRED_DIRECTION_TRACE_COSTS; }
   "prefered_direction_trace_costs" { return DsnKeyword.PREFERRED_DIRECTION_TRACE_COSTS; }
   "pull_tight"    { return DsnKeyword.PULL_TIGHT; }
   "rect"          { yybegin(LAYER_NAME); return DsnKeyword.RECTANGLE; }
   "rectangle"     { yybegin(LAYER_NAME); return DsnKeyword.RECTANGLE; }
   "resolution"    { return DsnKeyword.RESOLUTION_SCOPE; }
   "rotate"        { return DsnKeyword.ROTATE; }
   "rotate_first"  { return DsnKeyword.ROTATE_FIRST; }
   "routes"        { return DsnKeyword.ROUTES; }
   "rule"          { return DsnKeyword.RULE; }
   "rules"         { return DsnKeyword.RULES; }
   "session"       { return DsnKeyword.SESSION; }
   "shape"         { return DsnKeyword.SHAPE; }
   "shove_fixed"   { return DsnKeyword.SHOVE_FIXED; }
   "side"          { return DsnKeyword.SIDE; }
   "signal"        { return DsnKeyword.SIGNAL; }
   "snap_angle"    { return DsnKeyword.SNAP_ANGLE; }
   "spare"         { return DsnKeyword.SPARE; }
   "start_pass_no" { return DsnKeyword.START_PASS_NO; }
   "start_ripup_costs" { return DsnKeyword.START_RIPUP_COSTS; }
   "string_quote"  { yybegin(IGNORE_QUOTE); return DsnKeyword.STRING_QUOTE; }
   "structure"     { return DsnKeyword.STRUCTURE_SCOPE; }
   "type"          { return DsnKeyword.TYPE; }
   "use_layer"     { yybegin(NAME); return DsnKeyword.USE_LAYER; }
   "use_net"       { yybegin(NAME); return DsnKeyword.USE_NET; }
   "use_via"       { yybegin(NAME); return DsnKeyword.USE_VIA; }
   "vertical"      { return DsnKeyword.VERTICAL; }
   "via"           { yybegin(NAME); return DsnKeyword.VIA; }
   "vias"          { return DsnKeyword.VIAS; }
   "via_at_smd"    { return DsnKeyword.VIA_AT_SMD; }
   "via_costs"     { return DsnKeyword.VIA_COSTS; }
   "via_keepout"   { yybegin(NAME); return DsnKeyword.VIA_KEEPOUT; }
   "via_rule"      { return DsnKeyword.VIA_RULE; }
   "width"         { return DsnKeyword.WIDTH; }
   "window"        { return DsnKeyword.WINDOW; }
   "wire"          { yybegin(NAME); return DsnKeyword.WIRE; }
   "wire_keepout"  { return DsnKeyword.KEEPOUT; }
   "wiring"        { return DsnKeyword.WIRING_SCOPE; }
   "write_resolution" { return DsnKeyword.WRITE_RESOLUTION; }
   "("             { return DsnKeyword.OPEN_BRACKET; }
   ")"             { return DsnKeyword.CLOSED_BRACKET; }

  /* identifiers */ 
  {Identifier}                   { return yytext(); }

  /* Characters for quoting strings */
  {QuoteChar1}                    { string.setLength(0); yybegin(STRING1); }
  {QuoteChar2}                    { string.setLength(0); yybegin(STRING2); }
 
  /* literals */
  {DecIntegerLiteral}            { return new Integer(yytext()); }
  {DecFloatLiteral}              { return new Double(yytext()); }

  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

/* Strings quoted with " */
<STRING1> {
  [^\"\\]+                   { string.append( yytext() ); }
  \\                             { string.append('\\'); }
  \"                             { yybegin(YYINITIAL); return string.toString(); }
}

/* Strings quotet with ' */
<STRING2> {
  [^\'\\]+                   { string.append( yytext() ); }
  \\                             { string.append('\\'); }
  '                              { yybegin(YYINITIAL); return string.toString(); }
}


<NAME> {
   /* keywords */
   "("             { yybegin(YYINITIAL); return DsnKeyword.OPEN_BRACKET;}
   ")"             { yybegin(YYINITIAL); return DsnKeyword.CLOSED_BRACKET;}

  /* identifiers */ 
  {NameIdentifier}               { yybegin(YYINITIAL); return yytext(); }


  /* Characters for quoting strings */
  {QuoteChar1}                    { string.setLength(0); yybegin(STRING1); }
  {QuoteChar2}                    { string.setLength(0); yybegin(STRING2); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

/* Reads the next identifier while handling the quote characters as normal characters */
<IGNORE_QUOTE> {
   /* keywords */
   "("             { yybegin(YYINITIAL); return DsnKeyword.OPEN_BRACKET;}
   ")"             { yybegin(YYINITIAL); return DsnKeyword.CLOSED_BRACKET;}

   /* identifiers */  
   {IdentifierIgnoringQuotes}     { yybegin(YYINITIAL); return yytext(); }
   {WhiteSpace}                   { /* ignore */ }
}
    

<LAYER_NAME> {
   /* keywords */
   "pcb"           { yybegin(YYINITIAL); return DsnKeyword.PCB_SCOPE; }
   "signal"        { yybegin(YYINITIAL); return DsnKeyword.SIGNAL; }
   "("             { yybegin(YYINITIAL); return DsnKeyword.OPEN_BRACKET;}
   ")"             { yybegin(YYINITIAL); return DsnKeyword.CLOSED_BRACKET;}

  /* identifiers */ 
  {NameIdentifier}               { yybegin(YYINITIAL); return yytext(); }
 
  /* Characters for quoting strings */
  {QuoteChar1}                    { string.setLength(0); yybegin(STRING1); }
  {QuoteChar2}                    { string.setLength(0); yybegin(STRING2); }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

/* to divide a component name from the pin name with the charracter "-" */
<COMPONENT_NAME> {
   /* keywords */
   "("             { yybegin(YYINITIAL); return DsnKeyword.OPEN_BRACKET;}
   ")"             { yybegin(YYINITIAL); return DsnKeyword.CLOSED_BRACKET;}

  /* identifiers */ 
  {ComponentIdentifier}               { yybegin(YYINITIAL); return yytext(); }


  /* Characters for quoting strings */
  {QuoteChar1}                    { string.setLength(0); yybegin(STRING1); }
  {QuoteChar2}                    { string.setLength(0); yybegin(STRING2); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<SPEC_CHAR> {
   {SpecChar2} {return yytext();}
}

/* error fallback */
.|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+">"); }
