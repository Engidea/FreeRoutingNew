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
 * BoxShapeSearchTree.java
 *
 * Created on 20. Mai 2007, 07:33
 *
 */

package board.shape;

import java.util.Collection;
import java.util.LinkedList;
import planar.PlaShape;
import planar.Polyline;
import planar.ShapeBounding_90_Degree;
import planar.ShapeTile;
import planar.ShapeTileBox;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdArea;
import board.items.BrdOutline;
import board.varie.TestLevel;

/**
 * A special simple ShapeSearchtree, where the shapes are of class IntBox. It is used in the 90-degree autorouter algorithm.
 *
 * @author Alfons Wirtz
 */
public final class ShapeSearchTree90Degree extends ShapeSearchTree
   {
   public ShapeSearchTree90Degree(RoutingBoard p_board, int p_compensated_clearance_class_no)
      {
      super(ShapeBounding_90_Degree.INSTANCE, p_board, p_compensated_clearance_class_no);
      }

   /**
    * Calculates a new incomplete room with a maximal TileShape contained in the shape of p_room, which may overlap only with items
    * of the input net on the input layer. p_room.get_contained_shape() will be contained in the shape of the result room. If that
    * is not possible, several rooms are returned with shapes, which intersect with p_room.get_contained_shape(). The result room is
    * not yet complete, because its doors are not yet calculated.
    */
   @Override
   public Collection<ExpandRoomFreespaceIncomplete> complete_shape(ExpandRoomFreespaceIncomplete p_room, int p_net_no, ShapeTreeObject p_ignore_object, ShapeTile p_ignore_shape)
      {
      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();

      if (!(p_room.get_contained_shape() instanceof ShapeTileBox))
         {
         System.out.println("BoxShapeSearchTree.complete_shape: unexpected p_shape_to_be_contained");
         return result;
         }
      
      ShapeTileBox shape_to_be_contained = (ShapeTileBox) p_room.get_contained_shape();

      if (root_node == null) return result;

      ShapeTileBox start_shape = r_board.get_bounding_box();

      if (p_room.get_shape() != null)
         {
         if (!(p_room.get_shape() instanceof ShapeTileBox))
            {
            System.out.println("BoxShapeSearchTree.complete_shape: p_start_shape of type IntBox expected");
            return result;
            }
         start_shape = ((ShapeTileBox) p_room.get_shape()).intersection(start_shape);
         }
      
      ShapeTileBox bounding_shape = start_shape;
      int room_layer = p_room.get_layer();

      result.add(new ExpandRoomFreespaceIncomplete(start_shape, room_layer, shape_to_be_contained));
      
      node_stack.reset();
      node_stack.push(root_node);
      
      ShapeTreeNode curr_node;

      for (;;)
         {
         curr_node = node_stack.pop();

         if (curr_node == null) break;
         
         if ( ! curr_node.bounding_shape.intersects(bounding_shape)) continue;
         
         if ( ! (curr_node instanceof ShapeTreeLeaf))
            {
            node_stack.push(((ShapeTreeNodeInner) curr_node).first_child);
            node_stack.push(((ShapeTreeNodeInner) curr_node).second_child);
            continue;
            }

         ShapeTreeLeaf curr_leaf = (ShapeTreeLeaf) curr_node;
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_leaf.object;
         int shape_index = curr_leaf.shape_index_in_object;

         if (! (curr_object.is_trace_obstacle(p_net_no) && curr_object.shape_layer(shape_index) == room_layer && curr_object != p_ignore_object) ) continue;
         
         ShapeTileBox curr_object_shape = curr_object.get_tree_shape(this, shape_index).bounding_box();
         Collection<ExpandRoomFreespaceIncomplete> new_result = new LinkedList<ExpandRoomFreespaceIncomplete>();
         ShapeTileBox new_bounding_shape = ShapeTileBox.EMPTY;
         for (ExpandRoomFreespaceIncomplete curr_room : result)
            {
            ShapeTileBox curr_shape = (ShapeTileBox) curr_room.get_shape();
            if (curr_shape.overlaps(curr_object_shape))
               {
               if (curr_object instanceof ExpandRoomFreespaceComplete && p_ignore_shape != null)
                  {
                  ShapeTileBox intersection = curr_shape.intersection(curr_object_shape);
                  if (p_ignore_shape.contains(intersection))
                     {
                     // ignore also all objects, whose intersection is contained in the
                     // 2-dim overlap-door with the from_room.
                     continue;
                     }
                  }
               Collection<ExpandRoomFreespaceIncomplete> new_restrained_shapes = restrain_shape(curr_room, curr_object_shape);
               new_result.addAll(new_restrained_shapes);

               for (ExpandRoomFreespaceIncomplete tmp_shape : new_result)
                  {
                  new_bounding_shape = new_bounding_shape.union(tmp_shape.get_shape().bounding_box());
                  }
               }
            else
               {
               new_result.add(curr_room);
               new_bounding_shape = new_bounding_shape.union(curr_shape.bounding_box());
               }
            }
         result = new_result;
         bounding_shape = new_bounding_shape;
         }

      return result;
      }

   /**
    * Restrains the shape of p_incomplete_room to a box shape, which does not intersect with the interiour of p_obstacle_shape.
    * p_incomplete_room.get_contained_shape() must be contained in the shape of the result room.
    */
   private Collection<ExpandRoomFreespaceIncomplete> restrain_shape(ExpandRoomFreespaceIncomplete p_incomplete_room, ShapeTileBox p_obstacle_shape)
      {
      // Search the edge line of p_obstacle_shape, so that p_shape_to_be_contained
      // are on the right side of this line, and that the line segment
      // intersects with the interiour of p_shape.
      // If there are more than 1 such lines take the line which is
      // furthest away from the shape_to_be_contained
      // Then insersect p_shape with the halfplane defined by the
      // opposite of this line.

      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();
      if (p_incomplete_room.get_contained_shape().is_empty())
         {
         if (this.r_board.get_test_level().ordinal() >= TestLevel.ALL_DEBUGGING_OUTPUT.ordinal())
            {
            System.out.println("BoxShapeSearchTree.restrain_shape: p_shape_to_be_contained is empty");
            }
         return result;
         }
      ShapeTileBox room_shape = p_incomplete_room.get_shape().bounding_box();
      ShapeTileBox shape_to_be_contained = p_incomplete_room.get_contained_shape().bounding_box();
      int cut_line_distance = 0;
      ShapeTileBox restrained_shape = null;

      if (room_shape.box_ll.v_x < p_obstacle_shape.box_ur.v_x && room_shape.box_ur.v_x > p_obstacle_shape.box_ur.v_x && room_shape.box_ur.v_y > p_obstacle_shape.box_ll.v_y && room_shape.box_ll.v_y < p_obstacle_shape.box_ur.v_y)
         {
         // The right line segment of the obstacle_shape intersects the interiour of p_shape
         int curr_distance = shape_to_be_contained.box_ll.v_x - p_obstacle_shape.box_ur.v_x;
         if (curr_distance > cut_line_distance)
            {
            cut_line_distance = curr_distance;
            restrained_shape = new ShapeTileBox(p_obstacle_shape.box_ur.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
            }
         }
      if (room_shape.box_ll.v_x < p_obstacle_shape.box_ll.v_x && room_shape.box_ur.v_x > p_obstacle_shape.box_ll.v_x && room_shape.box_ur.v_y > p_obstacle_shape.box_ll.v_y && room_shape.box_ll.v_y < p_obstacle_shape.box_ur.v_y)
         {
         // The left line segment of the obstacle_shape intersects the interiour of p_shape
         int curr_distance = p_obstacle_shape.box_ll.v_x - shape_to_be_contained.box_ur.v_x;
         if (curr_distance > cut_line_distance)
            {
            cut_line_distance = curr_distance;
            restrained_shape = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, p_obstacle_shape.box_ll.v_x, room_shape.box_ur.v_y);
            }
         }
      if (room_shape.box_ll.v_y < p_obstacle_shape.box_ll.v_y && room_shape.box_ur.v_y > p_obstacle_shape.box_ll.v_y && room_shape.box_ur.v_x > p_obstacle_shape.box_ll.v_x && room_shape.box_ll.v_x < p_obstacle_shape.box_ur.v_x)
         {
         // The lower line segment of the obstacle_shape intersects the interiour of p_shape
         int curr_distance = p_obstacle_shape.box_ll.v_y - shape_to_be_contained.box_ur.v_y;
         if (curr_distance > cut_line_distance)
            {
            cut_line_distance = curr_distance;
            restrained_shape = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, p_obstacle_shape.box_ll.v_y);
            }
         }
      if (room_shape.box_ll.v_y < p_obstacle_shape.box_ur.v_y && room_shape.box_ur.v_y > p_obstacle_shape.box_ur.v_y && room_shape.box_ur.v_x > p_obstacle_shape.box_ll.v_x && room_shape.box_ll.v_x < p_obstacle_shape.box_ur.v_x)
         {
         // The upper line segment of the obstacle_shape intersects the interiour of p_shape
         int curr_distance = shape_to_be_contained.box_ll.v_y - p_obstacle_shape.box_ur.v_y;
         if (curr_distance > cut_line_distance)
            {
            cut_line_distance = curr_distance;
            restrained_shape = new ShapeTileBox(room_shape.box_ll.v_x, p_obstacle_shape.box_ur.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
            }
         }
      if (restrained_shape != null)
         {
         result.add(new ExpandRoomFreespaceIncomplete(restrained_shape, p_incomplete_room.get_layer(), shape_to_be_contained));
         return result;
         }

      // Now shape_to_be_contained intersects with the obstacle_shape.
      // shape_to_be_contained and p_shape evtl. need to be divided in two.
      ShapeTileBox is = shape_to_be_contained.intersection(p_obstacle_shape);
      if (is.is_empty())
         {
         System.out.println("BoxShapeSearchTree.restrain_shape: Intersection between obstacle_shape and shape_to_be_contained expected");
         return result;
         }
      ShapeTileBox new_shape_1 = null;
      ShapeTileBox new_shape_2 = null;
      if (is.box_ll.v_x > room_shape.box_ll.v_x && is.box_ll.v_x == p_obstacle_shape.box_ll.v_x && is.box_ll.v_x < room_shape.box_ur.v_x)
         {
         new_shape_1 = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, is.box_ll.v_x, room_shape.box_ur.v_y);
         new_shape_2 = new ShapeTileBox(is.box_ll.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
         }
      else if (is.box_ur.v_x > room_shape.box_ll.v_x && is.box_ur.v_x == p_obstacle_shape.box_ur.v_x && is.box_ur.v_x < room_shape.box_ur.v_x)
         {
         new_shape_2 = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, is.box_ur.v_x, room_shape.box_ur.v_y);
         new_shape_1 = new ShapeTileBox(is.box_ur.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
         }
      else if (is.box_ll.v_y > room_shape.box_ll.v_y && is.box_ll.v_y == p_obstacle_shape.box_ll.v_y && is.box_ll.v_y < room_shape.box_ur.v_y)
         {
         new_shape_1 = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, is.box_ll.v_y);
         new_shape_2 = new ShapeTileBox(room_shape.box_ll.v_x, is.box_ll.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
         }
      else if (is.box_ur.v_y > room_shape.box_ll.v_y && is.box_ur.v_y == p_obstacle_shape.box_ur.v_y && is.box_ur.v_y < room_shape.box_ur.v_y)
         {
         new_shape_2 = new ShapeTileBox(room_shape.box_ll.v_x, room_shape.box_ll.v_y, room_shape.box_ur.v_x, is.box_ur.v_y);
         new_shape_1 = new ShapeTileBox(room_shape.box_ll.v_x, is.box_ur.v_y, room_shape.box_ur.v_x, room_shape.box_ur.v_y);
         }
      if (new_shape_1 != null)
         {
         ShapeTileBox new_shape_to_be_contained = shape_to_be_contained.intersection(new_shape_1);

         // WARNING was > 0 
         if ( ! new_shape_to_be_contained.dimension().is_empty() )
            {
            result.add(new ExpandRoomFreespaceIncomplete(new_shape_1, p_incomplete_room.get_layer(), new_shape_to_be_contained));
            ExpandRoomFreespaceIncomplete new_incomplete_room = new ExpandRoomFreespaceIncomplete(new_shape_2, p_incomplete_room.get_layer(), shape_to_be_contained.intersection(new_shape_2));
            result.addAll(restrain_shape(new_incomplete_room, p_obstacle_shape));
            }
         }
      return result;
      }

   @Override
   public ShapeTile[] calculate_tree_shapes(BrdAbit p_drill_item)
      {
      ShapeTile[] result = new ShapeTile[p_drill_item.tile_shape_count()];
      for (int i = 0; i < result.length; ++i)
         {
         PlaShape curr_shape = p_drill_item.get_shape(i);
         if (curr_shape == null)
            {
            result[i] = null;
            }
         else
            {
            ShapeTileBox curr_tile_shape = curr_shape.bounding_box();
            int offset_width = this.get_clearance_compensation(p_drill_item.clearance_class_no(), p_drill_item.shape_layer(i));
            if (curr_tile_shape == null)
               {
               System.out.println("BoxShapeSearchTree.calculate_tree_shapes: shape is null");
               }
            else
               {
               curr_tile_shape = curr_tile_shape.offset(offset_width);
               }
            result[i] = curr_tile_shape;
            }
         }
      return result;
      }

   @Override
   public ShapeTile[] calculate_tree_shapes(BrdArea p_obstacle_area)
      {
      ShapeTile[] result = super.calculate_tree_shapes(p_obstacle_area);
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = result[i].bounding_box();
         }
      return result;
      }

   public ShapeTile[] calculate_tree_shapes(BrdOutline p_outline)
      {
      ShapeTile[] result = super.calculate_tree_shapes(p_outline);
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = result[i].bounding_box();
         }
      return result;
      }

   /**
    * there was a spelling, an s added and therefore the methof was not inherited
    */
   @Override
   protected ShapeTile offset_shape(Polyline p_polyline, int p_half_width, int p_no)
      {
      return p_polyline.offset_box(p_half_width, p_no);
      }

   @Override
   protected ShapeTile[] offset_shapes(Polyline p_polyline, int p_half_width, int p_from_no, int p_to_no)
      {
      int from_no = Math.max(p_from_no, 0);
      int to_no = Math.min(p_to_no, p_polyline.lines_arr.length - 1);
      int shape_count = Math.max(to_no - from_no - 1, 0);
      ShapeTile[] shape_arr = new ShapeTile[shape_count];
      for (int j = from_no; j < to_no - 1; ++j)
         {
         shape_arr[j - from_no] = p_polyline.offset_box(p_half_width, j);
         }
      return shape_arr;
      }
   }
