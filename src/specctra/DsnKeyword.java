/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
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
 * Keyword.java
 *
 * Created on 8. Mai 2004, 10:23
 */
package specctra;


/**
 * Enumeration class for keywords of the specctra dsn file format
 *
 * @author alfons
 */
public class DsnKeyword
   {
   /**
    * The only instances of the internal classes:
    * ScopeKeywords with an individual read_scope method are defined in an extra class,
    */
   public static final DsnKeyword ABSOLUTE = new DsnKeyword("absolute");
   public static final DsnKeyword ACTIVE = new DsnKeyword("active");
   public static final DsnKeyword AGAINST_PREFERRED_DIRECTION_TRACE_COSTS = new DsnKeyword("against_preferred_direction_trace_costs");
   public static final DsnKeyword ATTACH = new DsnKeyword("attach");
   public static final DsnKeyword AUTOROUTE = new DsnKeyword("autoroute");
   public static final DsnKeyword AUTOROUTE_SETTINGS = new DsnKeyword("autoroute_settings");
   public static final DsnKeyword BACK = new DsnKeyword("back");
   public static final DsnKeyword BOUNDARY = new DsnKeyword("boundary");
   public static final DsnKeyword CIRCUIT = new DsnKeyword("circuit");
   public static final DsnKeyword CIRCLE = new DsnKeyword("circle");
   public static final DsnKeyword CLASS = new DsnKeyword("class");
   public static final DsnKeyword CLASS_CLASS = new DsnKeyword("class_class");
   public static final DsnKeyword CLASSES = new DsnKeyword("classes");
   public static final DsnKeywordScope COMPONENT_SCOPE = new DsnKeywordComponent();
   public static final DsnKeyword CONSTANT = new DsnKeyword("constant");
   public static final DsnKeyword CONTROL = new DsnKeyword("control");
   public static final DsnKeyword CLEARANCE = new DsnKeyword("clearance");
   public static final DsnKeyword CLEARANCE_CLASS = new DsnKeyword("clearance_class");
   public static final DsnKeyword CLOSED_BRACKET = new DsnKeyword(")");
   public static final DsnKeyword FANOUT = new DsnKeyword("fanout");
   public static final DsnKeyword FLIP_STYLE = new DsnKeyword("flip_style");
   public static final DsnKeyword FIX = new DsnKeyword("fix");
   public static final DsnKeyword FORTYFIVE_DEGREE = new DsnKeyword("fortyfive_degree");
   public static final DsnKeyword FROMTO = new DsnKeyword("fromto");
   public static final DsnKeyword FRONT = new DsnKeyword("front");
   public static final DsnKeyword GENERATED_BY_FREEROUTE = new DsnKeyword("generated_by_freeroute");
   public static final DsnKeyword HORIZONTAL = new DsnKeyword("horizontal");
   public static final DsnKeyword HOST_CAD = new DsnKeyword("host_cad");
   public static final DsnKeyword HOST_VERSION = new DsnKeyword("host_version");
   public static final DsnKeyword IMAGE = new DsnKeyword("image");
   public static final DsnKeyword KEEPOUT = new DsnKeyword("keepout");
   public static final DsnKeyword LAYER = new DsnKeyword("layer");
   public static final DsnKeyword LAYER_RULE = new DsnKeyword("layer_rule");
   public static final DsnKeyword LENGTH = new DsnKeyword("length");
   public static final DsnKeywordScope LIBRARY_SCOPE = new DsnKeywordLibrary();
   public static final DsnKeyword LOCK_TYPE = new DsnKeyword("lock_type");
   public static final DsnKeyword LOGICAL_PART = new DsnKeyword("logical_part");
   public static final DsnKeyword LOGICAL_PART_MAPPING = new DsnKeyword("logical_part_mapping");
   public static final DsnKeyword NET = new DsnKeyword("net");
   public static final DsnKeyword NETWORK_OUT = new DsnKeyword("network_out");
   public static final DsnKeywordScope NETWORK_SCOPE = new DsnKeywordNetwork();
   public static final DsnKeyword NINETY_DEGREE = new DsnKeyword("ninety_degree");
   public static final DsnKeyword NONE = new DsnKeyword("none");
   public static final DsnKeyword NORMAL = new DsnKeyword("normal");
   public static final DsnKeyword OFF = new DsnKeyword("off");
   public static final DsnKeyword ON = new DsnKeyword("on");
   public static final DsnKeyword OPEN_BRACKET = new DsnKeyword("(");
   public static final DsnKeyword ORDER = new DsnKeyword("order");
   public static final DsnKeyword OUTLINE = new DsnKeyword("outline");
   public static final DsnKeyword PADSTACK = new DsnKeyword("padstack");
   public static final DsnKeywordScope PART_LIBRARY_SCOPE = new DsnKeywordPartLibrary();
   public static final DsnKeywordScope PARSER_SCOPE = new DsnKeywordParser();
   public static final DsnKeywordScope PCB_SCOPE = new DsnKeywordScope("pcb");
   public static final DsnKeyword PIN = new DsnKeyword("pin");
   public static final DsnKeyword PINS = new DsnKeyword("pins");
   public static final DsnKeyword PLACE = new DsnKeyword("place");
   public static final DsnKeywordScope PLACE_CONTROL = new DsnKeywordPlaceControl();
   public static final DsnKeyword PLACE_KEEPOUT = new DsnKeyword("place_keepout");
   public static final DsnKeywordScope PLACEMENT_SCOPE = new DsnKeywordPlacement();
   public static final DsnKeywordScope PLANE_SCOPE = new DsnKeywordPlane();
   public static final DsnKeyword PLANE_VIA_COSTS = new DsnKeyword("plane_via_costs");
   public static final DsnKeyword PREFERRED_DIRECTION = new DsnKeyword("preferred_direction");
   public static final DsnKeyword PREFERRED_DIRECTION_TRACE_COSTS = new DsnKeyword("preferred_direction_trace_costs");
   public static final DsnKeyword SNAP_ANGLE = new DsnKeyword("snap_angle");
   public static final DsnKeyword POLYGON = new DsnKeyword("polygon");
   public static final DsnKeyword POLYGON_PATH = new DsnKeyword("polygon_path");
   public static final DsnKeyword POLYLINE_PATH = new DsnKeyword("polyline_path");
   public static final DsnKeyword POSITION = new DsnKeyword("position");
   public static final DsnKeyword POSTROUTE = new DsnKeyword("postroute");
   public static final DsnKeyword POWER = new DsnKeyword("power");
   public static final DsnKeyword PULL_TIGHT = new DsnKeyword("pull_tight");
   public static final DsnKeyword RECTANGLE = new DsnKeyword("rectangle");
   public static final DsnKeyword RESOLUTION_SCOPE = new DsnKeywordResolution();
   public static final DsnKeyword ROTATE = new DsnKeyword("rotate");
   public static final DsnKeyword ROTATE_FIRST = new DsnKeyword("rotate_first");
   public static final DsnKeyword ROUTES = new DsnKeyword("routes");
   public static final DsnKeyword RULE = new DsnKeyword("rule");
   public static final DsnKeyword RULES = new DsnKeyword("rules");
   public static final DsnKeyword SESSION = new DsnKeyword("session");
   public static final DsnKeyword SHAPE = new DsnKeyword("shape");
   public static final DsnKeyword SHOVE_FIXED = new DsnKeyword("shove_fixed");
   public static final DsnKeyword SIDE = new DsnKeyword("side");
   public static final DsnKeyword SIGNAL = new DsnKeyword("signal");
   public static final DsnKeyword SPARE = new DsnKeyword("spare");
   public static final DsnKeyword START_PASS_NO = new DsnKeyword("start_pass_no");
   public static final DsnKeyword START_RIPUP_COSTS = new DsnKeyword("start_ripup_costs");
   public static final DsnKeyword STRING_QUOTE = new DsnKeyword("string_quote");
   public static final DsnKeywordScope STRUCTURE_SCOPE = new DsnKeywordStructure();
   public static final DsnKeyword TYPE = new DsnKeyword("type");
   public static final DsnKeyword USE_LAYER = new DsnKeyword("use_layer");
   public static final DsnKeyword USE_NET = new DsnKeyword("use_net");
   public static final DsnKeyword USE_VIA = new DsnKeyword("use_via");
   public static final DsnKeyword VERTICAL = new DsnKeyword("vertical");
   public static final DsnKeyword VIA = new DsnKeyword("via");
   public static final DsnKeyword VIAS = new DsnKeyword("vias");
   public static final DsnKeyword VIA_AT_SMD = new DsnKeyword("via_at_smd");
   public static final DsnKeyword VIA_COSTS = new DsnKeyword("via_costs");
   public static final DsnKeyword VIA_KEEPOUT = new DsnKeyword("via_keepout");
   public static final DsnKeyword VIA_RULE = new DsnKeyword("via_rule");
   public static final DsnKeyword WIDTH = new DsnKeyword("width");
   public static final DsnKeyword WINDOW = new DsnKeyword("window");
   public static final DsnKeyword WIRE = new DsnKeyword("wire");
   public static final DsnKeywordScope WIRING_SCOPE = new DsnKeywordWiring();
   public static final DsnKeyword WRITE_RESOLUTION = new DsnKeyword("write_resolution");
   public static final DsnKeyword PROTECT = new DsnKeyword("protect");

   private final String name;

   /** 
    * prevents creating more instances 
    */
   protected DsnKeyword(String p_name)
      {
      name = p_name;
      }

   /**
    * Returns the name string of this Keyword. The name is used for debugging purposes.
    */
   public String get_name()
      {
      return name;
      }


   }
