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
 * LogfileScope.java
 *
 * Created on 12. November 2003, 11:10
 */

package interactive;

import interactive.state.StateCircleConstrut;
import interactive.state.StateConstructHole;
import interactive.state.StateConstructPolygon;
import interactive.state.StateConstuctTile;
import interactive.state.StateCopyItem;
import interactive.state.StateDragItem;
import interactive.state.StateDragMakeSpace;
import interactive.state.StateInteractive;
import interactive.state.StateMenu;
import interactive.state.StateMoveItem;
import interactive.state.StateRoute;
import interactive.state.StateSelecRegionCutout;
import interactive.state.StateSelectRegionItems;
import interactive.state.StateSelectedItem;
import board.varie.ItemSelectionChoice;
import freert.planar.PlaPointFloat;
import freert.varie.TimeLimitStoppable;

/**
 * Enumeration class defining scopes in a logfile, Each Object of the class must implement the read_scope method.
 *
 * @author Alfons Wirtz
 */
public abstract class LogfileScope
   {
   /**
    * The only instances of the internal classes:
    */

   // scpopes logging undo and redo
   public static final LogfileScope UNDO = new UndoScope("undo");
   public static final LogfileScope REDO = new RedoScope("redo");
   public static final LogfileScope GENERATE_SNAPSHOT = new GenerateSnapshotScope("generate_snapshot");

   // Scopes for logging changes in the interactive setting:
   public static final LogfileScope SET_CLEARANCE_COMPENSATION = new SetClearanceCompensationScope("set_clearance_compensation");
   public static final LogfileScope SET_DRAG_COMPONENTS_ENABLED = new SetDragComponentsEnabledScope("set_drag_componente_enabled");
   public static final LogfileScope SET_LAYER = new SetLayerScope("set_layer");
   public static final LogfileScope SET_MANUAL_TRACE_CLEARANCE_CLASS = new SetManualTraceClearanceClassScope("set_manual_trace_clearance_class");
   public static final LogfileScope SET_MANUAL_TRACE_HALF_WIDTH = new SetManualTraceHalfWidthScope("set_manual_trace_half_width");
   public static final LogfileScope SET_MANUAL_TRACEWITH_SELECTION = new SetManualTraceWidthSelectionScope("set_manual_tracewidth_selection");
   public static final LogfileScope SET_PULL_TIGHT_ACCURACY = new SetPullTightAccuracyScope("set_pull_tight_accuracy");
   public static final LogfileScope SET_PULL_TIGHT_REGION_WIDTH = new SetPullTightRegionWidthScope("set_pull_tight_region_width");
   public static final LogfileScope SET_PUSH_ENABLED = new SetPushEnabledScope("set_push_enabled");
   public static final LogfileScope SET_SNAP_ANGLE = new SetSnapAngleScope("set_snap_angle");
   public static final LogfileScope SET_SELECTABLE = new SetSelectableScope(" set_selectable");
   public static final LogfileScope SET_SELECT_ON_ALL_LAYER = new SetSelectOnAllLayerScope(" set_select_on_all_layer");
   public static final LogfileScope SET_STITCH_ROUTE = new SetStitchRouteScope(" set_stitch_route");
   public static final LogfileScope SET_TRACE_HALF_WIDTH = new SetTraceHalfWidthScope("set_trace_halfwidth");
   public static final LogfileScope SET_IGNORE_CONDUCTION = new SetIgnoreConductionScope("set_ignore_conduction");

   // scopes for logging changes in the interactively selected set of items:
   public static final LogfileScope START_SELECT = new StartSelectScope("start_select");
   public static final LogfileScope TOGGLE_SELECT = new ToggleSelectScope("toggle_select");
   public static final LogfileScope SELECT_REGION = new SelectRegionScope("select_region");
   public static final LogfileScope EXTEND_TO_WHOLE_CONNECTED_SETS = new ExtendToWholeConnectedSetsScope("extend_to_whole_connected_sets");
   public static final LogfileScope EXTEND_TO_WHOLE_CONNECTIONS = new ExtendToWholeConnectionsScope("extend_to_whole_connections");
   public static final LogfileScope EXTEND_TO_WHOLE_COMPONENTS = new ExtendToWholeComponentsScope("extend_to_whole_components");
   public static final LogfileScope EXTEND_TO_WHOLE_NETS = new ExtendToWholeNetsScope("extend_to_whole_nets");

   // scopes for logging actions on the interactively selected set of items:
   public static final LogfileScope ASSIGN_CLEARANCE_CLASS = new AssignClearanceClassScope("assign_clearance_class");
   public static final LogfileScope ASSIGN_SELECTED_TO_NEW_NET = new AssignSelectedToNewNetScope("assign_selected_to_new_net");
   public static final LogfileScope ASSIGN_SELECTED_TO_NEW_GROUP = new AssignSelectedToNewGroupScope("assign_selected_to_new_group");
   public static final LogfileScope FIX_SELECTED_ITEMS = new FixSelectedScope("fix_selected_items");
   public static final LogfileScope UNFIX_SELECTED_ITEMS = new UnfixSelectedScope("unfix_selected_items");
   public static final LogfileScope DELETE_SELECTED = new DeleteSelectedScope("delete_selected");
   public static final LogfileScope CUTOUT_ROUTE = new CutoutRouteScope("cutout_route");
   public static final LogfileScope OPTIMIZE_SELECTED = new OptimizeSelectedScope("optmize_selected");
   public static final LogfileScope AUTOROUTE_SELECTED = new AutorouteSelectedScope("autoroute_selected");
   public static final LogfileScope FANOUT_SELECTED = new FanoutSelectedScope("fanout_selected");

   // scopes for logging interactive creating or moving items.
   public static final LogfileScope COMPLETE_SCOPE = new CompleteScope("complete_scope");
   public static final LogfileScope CANCEL_SCOPE = new CancelScope("cancel_scope");
   public static final LogfileScope CREATING_TILE = new CreateTileScope("creating_tile");
   public static final LogfileScope CREATING_CIRCLE = new CreateCircleScope("creating_circle");
   public static final LogfileScope CREATING_POLYGONSHAPE = new CreatePolygonShapeScope("creating_polygonshape");
   public static final LogfileScope ADDING_HOLE = new AddHoleScope("adding_hole");
   public static final LogfileScope CREATING_TRACE = new CreateTraceScope("creating_trace");
   public static final LogfileScope CHANGE_LAYER = new ChangeLayerScope("change_layer");
   public static final LogfileScope DRAGGING_ITEMS = new DragItemScope("dragging_items");
   public static final LogfileScope MAKING_SPACE = new MakeSpaceScope("making_space");
   public static final LogfileScope COPYING_ITEMS = new CopyItemScope("copying_items");
   public static final LogfileScope MOVE_ITEMS = new MoveItemScope("moving_items");
   public static final LogfileScope TURN_90_DEGREE = new Turn90DegreeScope("turn_90_degree");
   public static final LogfileScope ROTATE = new RotateScope("rotate");
   public static final LogfileScope CHANGE_PLACEMENT_SIDE = new ChangePlacementSideScope("change_placement_side");
   public static final LogfileScope SET_ZOOM_WITH_WHEEL = new SetZoomWithWheelScope("set_zoom_with_wheel");

   // scopes for logging display changes
   public static final LogfileScope CENTER_DISPLAY = new CenterDisplayScope("center_display");
   public static final LogfileScope ZOOM_FRAME = new ZoomFrameScope("zoom_frame");

   /**
    * This array contains all (above) created objects of this class. Initialializing this static array automatically by the program
    * did not work correctly, so the programmer has to keep it uptodate by hand.
    */
   private static LogfileScope[] arr = { UNDO, REDO, GENERATE_SNAPSHOT, SET_CLEARANCE_COMPENSATION, SET_LAYER, SET_MANUAL_TRACE_CLEARANCE_CLASS, SET_MANUAL_TRACE_HALF_WIDTH,
         SET_MANUAL_TRACEWITH_SELECTION, SET_SNAP_ANGLE, SET_SELECTABLE, SET_SELECT_ON_ALL_LAYER, SET_STITCH_ROUTE, SET_TRACE_HALF_WIDTH, SET_PULL_TIGHT_REGION_WIDTH, SET_PULL_TIGHT_ACCURACY,
         SET_PUSH_ENABLED, SET_IGNORE_CONDUCTION, START_SELECT, TOGGLE_SELECT, SELECT_REGION, EXTEND_TO_WHOLE_CONNECTED_SETS, EXTEND_TO_WHOLE_CONNECTIONS, EXTEND_TO_WHOLE_COMPONENTS,
         EXTEND_TO_WHOLE_NETS, ASSIGN_SELECTED_TO_NEW_NET, ASSIGN_SELECTED_TO_NEW_GROUP, FIX_SELECTED_ITEMS, UNFIX_SELECTED_ITEMS, DELETE_SELECTED, CUTOUT_ROUTE, OPTIMIZE_SELECTED,
         AUTOROUTE_SELECTED, FANOUT_SELECTED, COMPLETE_SCOPE, CANCEL_SCOPE, CREATING_TILE, CREATING_CIRCLE, CREATING_POLYGONSHAPE, ADDING_HOLE, CREATING_TRACE, CHANGE_LAYER, DRAGGING_ITEMS,
         MAKING_SPACE, COPYING_ITEMS, MOVE_ITEMS, TURN_90_DEGREE, ROTATE, CHANGE_PLACEMENT_SIDE, SET_ZOOM_WITH_WHEEL, ASSIGN_CLEARANCE_CLASS, CENTER_DISPLAY, ZOOM_FRAME };

   /**
    * Reads the scope from the input logfile. Returns the active interactive state after reading the scope.
    */
   public abstract StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling);

   /**
    * Returns the LogfileScope with name p_name if it exists, else null.
    */
   public static LogfileScope get_scope(String p_name)
      {
      for (int i = 0; i < arr.length; ++i)
         {
         if (arr[i].name.compareTo(p_name) == 0)
            {
            return arr[i];
            }
         }
      return null;
      }

   /** prevents creating more instances */
   private LogfileScope(String p_name)
      {
      name = p_name;
      }

   /**
    * Scopes marking the end of a cornerlist scope.
    */
   private boolean is_end_scope()
      {
      return this == COMPLETE_SCOPE || this == CANCEL_SCOPE;
      }

   public final String name;

   /**
    * A logfile scope containing a list of points.
    */
   private static abstract class CornerlistScope extends LogfileScope
      {
      public CornerlistScope(String p_name)
         {
         super(p_name);
         }

      /**
       * Reads the next corner list scope togethet with its interiour scopes (layer change for example) from the input logfile.
       * Returns the active interactive state after reading the scope.
       */
      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         PlaPointFloat location = p_logfile.read_corner();
         if (location == null)
            {
            return null;
            }
         StateInteractive interactive_state = start_scope(location, p_return_state, p_board_handling);

         if (interactive_state == null) return null;
         
         p_board_handling.set_interactive_state(interactive_state);
         
         StateInteractive return_state = p_return_state;
         for (;;)
            {
            location = p_logfile.read_corner();
            if (location != null)
               {
               // process corner list
               StateInteractive new_state = interactive_state.process_logfile_point(location);
               if (new_state != interactive_state)
                  {
                  // state ended
                  return_state = new_state;
                  break;
                  }
               }
            else
               {
               // end of corner list, process the next interiour scope
               LogfileScope next_scope = p_logfile.start_read_scope();
               if (next_scope == null)
                  {
                  break; // end of logfile
                  }
               StateInteractive new_state = next_scope.read_scope(p_logfile, interactive_state, p_board_handling);
               if (next_scope.is_end_scope())
                  {
                  return_state = new_state;
                  break;
                  }
               }
            }
         return return_state;
         }

      /**
       * Used for beginning a new CornerlistScope.
       */
      public abstract StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling);
      }

   private static class CreateTraceScope extends CornerlistScope
      {
      public CreateTraceScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateRoute.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class CreateTileScope extends CornerlistScope
      {
      public CreateTileScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return new StateConstuctTile(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class CreateCircleScope extends CornerlistScope
      {
      public CreateCircleScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateCircleConstrut.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class CreatePolygonShapeScope extends CornerlistScope
      {
      public CreatePolygonShapeScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateConstructPolygon.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class AddHoleScope extends CornerlistScope
      {
      public AddHoleScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateConstructHole.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class DragItemScope extends CornerlistScope
      {
      public DragItemScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateDragItem.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class MakeSpaceScope extends CornerlistScope
      {
      public MakeSpaceScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return StateDragMakeSpace.get_instance(p_location, p_return_state, p_board_handling, null);
         }
      }

   private static class CopyItemScope extends CornerlistScope
      {
      public CopyItemScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;
         if (p_return_state instanceof StateSelectedItem)
            {
            java.util.Collection<board.items.BrdItem> item_list = ((StateSelectedItem) p_return_state).get_item_list();
            result = StateCopyItem.get_instance(p_location, item_list, p_return_state.return_state, p_board_handling, null);
            }
         else
            {
            System.out.println("CopyItemScope.start_scope: unexpected p_return_state");
            result = null;

            }
         return result;
         }
      }

   private static class MoveItemScope extends CornerlistScope
      {
      public MoveItemScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive start_scope(PlaPointFloat p_location, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;
         if (p_return_state instanceof StateSelectedItem)
            {
            java.util.Collection<board.items.BrdItem> item_list = ((StateSelectedItem) p_return_state).get_item_list();
            result = StateMoveItem.get_instance(p_location, item_list, p_return_state.return_state, p_board_handling, null);
            }
         else
            {
            System.out.println("MoveComponent.start_scope: unexpected p_return_state");
            result = null;

            }
         return result;
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive new_state = super.read_scope(p_logfile, p_return_state, p_board_handling);
         if (new_state == null)
            {
            return null;
            }
         return new_state.return_state;
         }
      }

   private static class Turn90DegreeScope extends LogfileScope
      {
      public Turn90DegreeScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateMoveItem)
            {
            int factor = p_logfile.read_int();
            ((StateMoveItem) p_return_state).turn_90_degree(factor);
            return p_return_state;
            }

         System.out.println("Turn90DegreeScope.read_scope: unexpected p_return_state");
         return null;
         }
      }

   private static class RotateScope extends LogfileScope
      {
      public RotateScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateMoveItem)
            {
            int angle = p_logfile.read_int();
            ((StateMoveItem) p_return_state).rotate(angle);
            return p_return_state;
            }

         System.out.println("RotateScope.read_scope: unexpected p_return_state");
         return null;
         }
      }

   private static class ChangePlacementSideScope extends LogfileScope
      {
      public ChangePlacementSideScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateMoveItem)
            {
            ((StateMoveItem) p_return_state).change_placement_side();
            return p_return_state;
            }

         System.out.println("ChangePlacementSideScope.read_scope: unexpected p_return_state");
         return null;
         }
      }

   private static class SetZoomWithWheelScope extends LogfileScope
      {
      public SetZoomWithWheelScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateMoveItem)
            {
            int int_value = p_logfile.read_int();
            if (int_value == 0)
               {
               p_board_handling.itera_settings.set_zoom_with_wheel(false);
               }
            else
               {
               p_board_handling.itera_settings.set_zoom_with_wheel(true);
               }
            return p_return_state;
            }

         System.out.println("SetRotateWithWheelScope.read_scope: unexpected p_return_state");
         return null;
         }
      }

   private static class ChangeLayerScope extends LogfileScope
      {
      public ChangeLayerScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_layer = p_logfile.read_int();
         p_return_state.change_layer_action(new_layer);
         return p_return_state;
         }
      }

   private static class StartSelectScope extends LogfileScope
      {
      public StartSelectScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         while (!(p_return_state instanceof StateMenu))
            {
            System.out.println("StartSelectScope.read_scope: menu state expected");
            p_return_state = p_return_state.return_state;
            }
         PlaPointFloat location = p_logfile.read_corner();
         if (location == null)
            {
            System.out.println("StartSelectScope.read_scope: unable to read corner");
            return null;
            }
         return ((StateMenu) p_return_state).select_items(location);
         }
      }

   private static class ToggleSelectScope extends LogfileScope
      {
      public ToggleSelectScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (!(p_return_state instanceof StateSelectedItem))
            {
            System.out.println("ToggleSelectScope.read_scope: SelectedItemState expected");
            return null;
            }
         PlaPointFloat location = p_logfile.read_corner();
         if (location == null)
            {
            System.out.println("ToggleSelectScope.read_scope: unable to read corner");
            return null;
            }
         return ((StateSelectedItem) p_return_state).toggle_select(location);
         }
      }

   private static class SelectRegionScope extends LogfileScope
      {
      public SelectRegionScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (!(p_return_state instanceof StateMenu))
            {
            System.out.println("SelectRegionScope.read_scope: menu state expected");
            }
         PlaPointFloat lower_left = p_logfile.read_corner();
         if (lower_left == null)
            {
            System.out.println("SelectRegionScope.read_scope: unable to read corner");
            return null;
            }
         StateInteractive curr_state = StateSelectRegionItems.get_instance(lower_left, p_return_state, p_board_handling, null);
         PlaPointFloat upper_right = p_logfile.read_corner();
         if (upper_right == null)
            {
            // user may have cancelled the state after the first corner
            return curr_state;
            }
         p_board_handling.set_current_mouse_position(upper_right);
         return curr_state.complete();
         }
      }

   private static class CutoutRouteScope extends LogfileScope
      {
      public CutoutRouteScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (!(p_return_state instanceof StateSelectedItem))
            {
            System.out.println("CutoutRouteScope.read_scope: electedItemState expected");
            }
         java.util.Collection<board.items.BrdItem> item_list = ((StateSelectedItem) p_return_state).get_item_list();
         PlaPointFloat lower_left = p_logfile.read_corner();
         if (lower_left == null)
            {
            System.out.println("CutoutRouteScope.read_scope: unable to read corner");
            return null;
            }
         StateInteractive curr_state = StateSelecRegionCutout.get_instance(item_list, lower_left, p_return_state.return_state, p_board_handling, null);
         PlaPointFloat upper_right = p_logfile.read_corner();
         if (upper_right == null)
            {
            // user may have cancelled the state after the first corner
            return curr_state;
            }
         p_board_handling.set_current_mouse_position(upper_right);
         return curr_state.complete();
         }
      }

   private static class DeleteSelectedScope extends LogfileScope
      {
      public DeleteSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).delete_items();
            }
         else
            {
            System.out.println("DeleteSelectedScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   private static class OptimizeSelectedScope extends LogfileScope
      {
      public OptimizeSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).pull_tight(new TimeLimitStoppable(30));
            }
         else
            {
            System.out.println("DeleteSelectedScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   private static class AutorouteSelectedScope extends LogfileScope
      {
      public AutorouteSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).autoroute(null);
            }
         else
            {
            System.out.println("AutorouteSelectedScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   private static class FanoutSelectedScope extends LogfileScope
      {
      public FanoutSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).fanout(null);
            }
         else
            {
            System.out.println("FanoutSelectedScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   /**
    * Scope calling the single method SelectedItemState.assign_clearance_class
    */
   private static class AssignClearanceClassScope extends LogfileScope
      {
      public AssignClearanceClassScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            int int_value = p_logfile.read_int();
            result = ((StateSelectedItem) p_return_state).assign_clearance_class(int_value);
            }
         else
            {
            System.out.println("AssignSelectedToNewNetScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   /**
    * Scope calling the single method SelectedItemState.assign_items_to_new_net
    */
   private static class AssignSelectedToNewNetScope extends LogfileScope
      {
      public AssignSelectedToNewNetScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).assign_items_to_new_net();
            }
         else
            {
            System.out.println("AssignSelectedToNewNetScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   /**
    * Scope calling the single method SelectedItemState.assign_items_to_new_group
    */
   private static class AssignSelectedToNewGroupScope extends LogfileScope
      {
      public AssignSelectedToNewGroupScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive result;

         if (p_return_state instanceof StateSelectedItem)
            {
            result = ((StateSelectedItem) p_return_state).assign_items_to_new_group();
            }
         else
            {
            System.out.println("AssignSelectedToNewGroupScope.read_scope: SelectedItemState expected");
            result = null;
            }
         return result;
         }
      }

   private static class ExtendToWholeConnectedSetsScope extends LogfileScope
      {
      public ExtendToWholeConnectedSetsScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive return_state = null;
         if (p_return_state instanceof StateSelectedItem)
            {
            return_state = ((StateSelectedItem) p_return_state).extent_to_whole_connected_sets();
            }
         else
            {
            System.out.println("ExtendToWholeConnectedSetsScope.read_scope: SelectedItemState expected");
            }
         return return_state;
         }
      }

   private static class ExtendToWholeComponentsScope extends LogfileScope
      {
      public ExtendToWholeComponentsScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive return_state = null;
         if (p_return_state instanceof StateSelectedItem)
            {
            return_state = ((StateSelectedItem) p_return_state).extent_to_whole_components();
            }
         else
            {
            System.out.println("ExtendToWholeGroupsScope.read_scope: SelectedItemState expected");
            }
         return return_state;
         }
      }

   private static class ExtendToWholeNetsScope extends LogfileScope
      {
      public ExtendToWholeNetsScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive return_state = null;
         if (p_return_state instanceof StateSelectedItem)
            {
            return_state = ((StateSelectedItem) p_return_state).extent_to_whole_nets();
            }
         else
            {
            System.out.println("ExtendToWholeNetsScope.read_scope: SelectedItemState expected");
            }
         return return_state;
         }
      }

   private static class ExtendToWholeConnectionsScope extends LogfileScope
      {
      public ExtendToWholeConnectionsScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         StateInteractive return_state = null;
         if (p_return_state instanceof StateSelectedItem)
            {
            return_state = ((StateSelectedItem) p_return_state).extent_to_whole_connections();
            }
         else
            {
            System.out.println("ExtendToWholeConnectionsScope.read_scope: SelectedItemState expected");
            }
         return return_state;
         }
      }

   private static class FixSelectedScope extends LogfileScope
      {
      public FixSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateSelectedItem)
            {
            ((StateSelectedItem) p_return_state).fix_items();
            }
         else
            {
            System.out.println("FixSelectedScope.read_scope: SelectedItemState expected");
            }
         return p_return_state;
         }
      }

   private static class UnfixSelectedScope extends LogfileScope
      {
      public UnfixSelectedScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         if (p_return_state instanceof StateSelectedItem)
            {
            ((StateSelectedItem) p_return_state).unfix_items();
            }
         else
            {
            System.out.println("UnfixSelectedScope.read_scope: SelectedItemState expected");
            }
         return p_return_state;
         }
      }

   private static class CompleteScope extends LogfileScope
      {
      public CompleteScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return p_return_state.complete();
         }
      }

   private static class CancelScope extends LogfileScope
      {
      public CancelScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         return p_return_state.cancel();
         }
      }

   private static class SetTraceHalfWidthScope extends LogfileScope
      {
      public SetTraceHalfWidthScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int layer = p_logfile.read_int();
         int new_half_width = p_logfile.read_int();
         p_board_handling.get_routing_board().brd_rules.set_default_trace_half_width(layer, new_half_width);
         return p_return_state;
         }
      }

   private static class SetPullTightRegionWidthScope extends LogfileScope
      {
      public SetPullTightRegionWidthScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_tidy_width = p_logfile.read_int();
         p_board_handling.itera_settings.trace_pull_tight_region_width = new_tidy_width;
         return p_return_state;
         }
      }

   private static class SetPushEnabledScope extends LogfileScope
      {
      public SetPushEnabledScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int int_value = p_logfile.read_int();
         if (int_value == 0)
            {
            p_board_handling.itera_settings.push_enabled = false;
            }
         else
            {
            p_board_handling.itera_settings.push_enabled = true;
            }
         return p_return_state;
         }
      }

   private static class SetDragComponentsEnabledScope extends LogfileScope
      {
      public SetDragComponentsEnabledScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int int_value = p_logfile.read_int();
         if (int_value == 0)
            {
            p_board_handling.itera_settings.drag_components_enabled = false;
            }
         else
            {
            p_board_handling.itera_settings.drag_components_enabled = true;
            }
         return p_return_state;
         }
      }

   private static class SetPullTightAccuracyScope extends LogfileScope
      {
      public SetPullTightAccuracyScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_accuracy = p_logfile.read_int();
         p_board_handling.itera_settings.trace_pull_tight_accuracy = new_accuracy;
         return p_return_state;
         }
      }

   private static class SetIgnoreConductionScope extends LogfileScope
      {
      public SetIgnoreConductionScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int int_value = p_logfile.read_int();
         p_board_handling.get_routing_board().set_conduction_is_obstacle(int_value == 0);
         return p_return_state;
         }
      }

   private static class SetLayerScope extends LogfileScope
      {
      public SetLayerScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_layer = p_logfile.read_int();
         p_board_handling.set_layer(new_layer);
         return p_return_state;
         }
      }

   private static class SetClearanceCompensationScope extends LogfileScope
      {
      public SetClearanceCompensationScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_clearance_type = p_logfile.read_int();
         if (new_clearance_type == 0)
            {
            p_board_handling.get_routing_board().search_tree_manager.set_clearance_compensation_used(false);
            }
         else
            {
            p_board_handling.get_routing_board().search_tree_manager.set_clearance_compensation_used(true);
            }
         return p_return_state;
         }
      }

   private static class SetManualTraceHalfWidthScope extends LogfileScope
      {
      public SetManualTraceHalfWidthScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int layer = p_logfile.read_int();
         int half_width = p_logfile.read_int();
         p_board_handling.itera_settings.manual_trace_half_width_arr[layer] = half_width;
         return p_return_state;
         }
      }

   private static class SetManualTraceClearanceClassScope extends LogfileScope
      {
      public SetManualTraceClearanceClassScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int index = p_logfile.read_int();
         p_board_handling.itera_settings.manual_trace_clearance_class = index;
         return p_return_state;
         }
      }

   private static class SetManualTraceWidthSelectionScope extends LogfileScope
      {
      public SetManualTraceWidthSelectionScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int manual_selection = p_logfile.read_int();
         if (manual_selection == 0)
            {
            p_board_handling.itera_settings.manual_rule_selection = false;
            }
         else
            {
            p_board_handling.itera_settings.manual_rule_selection = true;
            }
         return p_return_state;
         }
      }

   private static class SetSnapAngleScope extends LogfileScope
      {
      public SetSnapAngleScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_snap_angle_no = p_logfile.read_int();
         p_board_handling.get_routing_board().brd_rules.set_trace_snap_angle(board.varie.TraceAngleRestriction.arr[new_snap_angle_no]);
         return p_return_state;
         }
      }

   private static class SetSelectOnAllLayerScope extends LogfileScope
      {
      public SetSelectOnAllLayerScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_value = p_logfile.read_int();
         p_board_handling.itera_settings.select_on_all_visible_layers = (new_value != 0);
         return p_return_state;
         }
      }

   private static class SetStitchRouteScope extends LogfileScope
      {
      public SetStitchRouteScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int new_value = p_logfile.read_int();
         p_board_handling.itera_settings.is_stitch_route = (new_value != 0);
         return p_return_state;
         }
      }

   private static class SetSelectableScope extends LogfileScope
      {
      public SetSelectableScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         int item_type_no = p_logfile.read_int();
         int selection = p_logfile.read_int();
         ItemSelectionChoice item_type =ItemSelectionChoice.values()[item_type_no];
         if (selection == 0)
            {
            p_board_handling.itera_settings.item_selection_filter.set_selected(item_type, false);
            if (p_return_state instanceof StateSelectedItem)
               {
               ((StateSelectedItem) p_return_state).filter();
               }
            }
         else
            {
            p_board_handling.itera_settings.item_selection_filter.set_selected(item_type, true);
            }
         return p_return_state;
         }
      }

   private static class UndoScope extends LogfileScope
      {
      public UndoScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         p_board_handling.get_routing_board().undo(null);
         p_board_handling.repaint();
         return p_return_state;
         }
      }

   private static class RedoScope extends LogfileScope
      {
      public RedoScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         p_board_handling.get_routing_board().redo(null);
         p_board_handling.repaint();
         return p_return_state;
         }
      }

   private static class GenerateSnapshotScope extends LogfileScope
      {
      public GenerateSnapshotScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         p_board_handling.get_routing_board().generate_snapshot();
         return p_return_state;
         }
      }

   private static class CenterDisplayScope extends LogfileScope
      {
      public CenterDisplayScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         PlaPointFloat curr_location = p_logfile.read_corner();
         java.awt.geom.Point2D new_center = new java.awt.geom.Point2D.Double(curr_location.v_x, curr_location.v_y);
         p_board_handling.get_panel().center_display(new_center);
         return p_return_state;
         }
      }

   private static class ZoomFrameScope extends LogfileScope
      {
      public ZoomFrameScope(String p_name)
         {
         super(p_name);
         }

      public StateInteractive read_scope(Actlog p_logfile, StateInteractive p_return_state, IteraBoard p_board_handling)
         {
         java.awt.geom.Point2D lower_left = p_board_handling.gdi_context.coordinate_transform.board_to_screen(p_logfile.read_corner());
         java.awt.geom.Point2D upper_right = p_board_handling.gdi_context.coordinate_transform.board_to_screen(p_logfile.read_corner());
         p_board_handling.get_panel().zoom_frame(lower_left, upper_right);
         p_board_handling.repaint();
         return p_return_state;
         }
      }
   }
