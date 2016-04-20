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
 * ShapeSearchTree45Degree.java
 *
 * Created on 15. Juli 2007, 07:26
 *
 */
package board.shape;

import java.util.Collection;
import java.util.LinkedList;
import planar.PlaLineInt;
import planar.PlaShape;
import planar.PlaSide;
import planar.ShapeBounding_45_Degree;
import planar.ShapeTile;
import planar.ShapeTileBox;
import planar.ShapeTileOctagon;
import autoroute.expand.ExpandRoomFreespaceComplete;
import autoroute.expand.ExpandRoomFreespaceIncomplete;
import board.RoutingBoard;
import board.items.BrdAbit;
import board.items.BrdArea;
import board.items.BrdOutline;
import board.varie.TestLevel;

/**
 * A special simple ShapeSearchtree, where the shapes are of class IntOctagon. It is used in the 45-degree autorouter algorithm.
 *
 * @author Alfons Wirtz
 */
public final class ShapeSearchTree45Degree extends ShapeSearchTree
   {
   public ShapeSearchTree45Degree(RoutingBoard p_board, int p_compensated_clearance_class_no)
      {
      super(ShapeBounding_45_Degree.INSTANCE, p_board, p_compensated_clearance_class_no);
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
      // let's start with an empty result
      Collection<ExpandRoomFreespaceIncomplete> result = new LinkedList<ExpandRoomFreespaceIncomplete>();
      
      if (! p_room.get_contained_shape().is_IntOctagon())
         {
         System.out.println("ShapeSearchTree45Degree.complete_shape: unexpected p_shape_to_be_contained");
         return result;
         }
      
      ShapeTileOctagon shape_to_be_contained = p_room.get_contained_shape().bounding_octagon();

      if (root_node == null) return result;
      
      ShapeTileOctagon start_shape = r_board.get_bounding_box().bounding_octagon();

      if (p_room.get_shape() != null)
         {
         if (!(p_room.get_shape() instanceof ShapeTileOctagon))
            {
            System.out.println("ShapeSearchTree45Degree.complete_shape: p_start_shape of type IntOctagon expected");
            return result;
            }
         
         start_shape = p_room.get_shape().bounding_octagon().intersection(start_shape);
         }
      
      
      ShapeTileOctagon bounding_shape = start_shape;
      int room_layer = p_room.get_layer();
     
      result.add(new ExpandRoomFreespaceIncomplete(start_shape, room_layer, shape_to_be_contained));
      
      node_stack.reset();
      node_stack.push(root_node);
      ShapeTreeNode curr_node;

      for (;;)
         {
         curr_node = node_stack.pop();

         if (curr_node == null)  break;

         if ( ! curr_node.bounding_shape.intersects(bounding_shape)) continue;
         
         if ( ! (curr_node instanceof ShapeTreeLeaf) )
            {
            node_stack.push(((ShapeTreeNodeInner) curr_node).first_child);
            node_stack.push(((ShapeTreeNodeInner) curr_node).second_child);
            continue;
            }
            
         ShapeTreeLeaf curr_leaf = (ShapeTreeLeaf) curr_node;
         ShapeTreeObject curr_object = (ShapeTreeObject) curr_leaf.object;
         boolean is_obstacle = curr_object.is_trace_obstacle(p_net_no);

         int shape_index = curr_leaf.shape_index_in_object;
         
         if ( ! ( is_obstacle && curr_object.shape_layer(shape_index) == room_layer && curr_object != p_ignore_object) ) continue;
         
         ShapeTileOctagon curr_object_shape = curr_object.get_tree_shape(this, shape_index).bounding_octagon();
         
         Collection<ExpandRoomFreespaceIncomplete> new_result = new LinkedList<ExpandRoomFreespaceIncomplete>();
         
         ShapeTileOctagon new_bounding_shape = ShapeTileOctagon.EMPTY;
         
         for (ExpandRoomFreespaceIncomplete curr_room : result)
            {
            ShapeTileOctagon curr_shape = (ShapeTileOctagon) curr_room.get_shape();

            if ( ! curr_shape.overlaps(curr_object_shape) )
               {
               new_result.add(curr_room);
               new_bounding_shape = new_bounding_shape.union(curr_shape.bounding_box());
               continue;
               }

            if (curr_object instanceof ExpandRoomFreespaceComplete && p_ignore_shape != null)
               {
               ShapeTileOctagon intersection = curr_shape.intersection(curr_object_shape);
               if (p_ignore_shape.contains(intersection))
                  {
                  // ignore also all objects, whose intersection is contained in the 2-dim overlap-door with the from_room.
                  if (!p_ignore_shape.contains(curr_shape))
                     {
                     new_result.add(curr_room);
                     new_bounding_shape = new_bounding_shape.union(curr_shape.bounding_box());
                     }
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
         
         
         result = new_result;
         bounding_shape = new_bounding_shape;
         
         }
      
      
      result = divide_large_room(result, r_board.get_bounding_box());
      // remove rooms with shapes equal to the contained shape to prevent endless loop.
      java.util.Iterator<ExpandRoomFreespaceIncomplete> it = result.iterator();
      while (it.hasNext())
         {
         ExpandRoomFreespaceIncomplete curr_room = it.next();
         if (curr_room.get_contained_shape().contains(curr_room.get_shape()))
            {
            it.remove();
            }
         }
      return result;
      }

   @Override
   protected Collection<ExpandRoomFreespaceIncomplete> divide_large_room(Collection<ExpandRoomFreespaceIncomplete> p_room_list, ShapeTileBox p_board_bounding_box)
      {
      Collection<ExpandRoomFreespaceIncomplete> result = super.divide_large_room(p_room_list, p_board_bounding_box);
      for (ExpandRoomFreespaceIncomplete curr_room : result)
         {
         curr_room.set_shape(curr_room.get_shape().bounding_octagon());
         curr_room.set_contained_shape(curr_room.get_contained_shape().bounding_octagon());
         }
      return result;
      }

   /**
    * Checks, if the border line segment with index p_obstacle_border_line_no intersects with the inside of p_room_shape.
    */
   private static boolean obstacle_segment_touches_inside(ShapeTileOctagon p_obstacle_shape, int p_obstacle_border_line_no, ShapeTileOctagon p_room_shape)
      {
      int curr_border_line_no = p_obstacle_border_line_no;
      int curr_obstacle_corner_x = p_obstacle_shape.corner_x(p_obstacle_border_line_no);
      int curr_obstacle_corner_y = p_obstacle_shape.corner_y(p_obstacle_border_line_no);
      for (int j = 0; j < 5; ++j)
         {

         if (p_room_shape.side_of_border_line(curr_obstacle_corner_x, curr_obstacle_corner_y, curr_border_line_no) != PlaSide.ON_THE_LEFT)
            {
            return false;
            }
         curr_border_line_no = (curr_border_line_no + 1) % 8;
         }

      int next_obstacle_border_line_no = (p_obstacle_border_line_no + 1) % 8;
      int next_obstacle_corner_x = p_obstacle_shape.corner_x(next_obstacle_border_line_no);
      int next_obstacle_corner_y = p_obstacle_shape.corner_y(next_obstacle_border_line_no);
      curr_border_line_no = (p_obstacle_border_line_no + 5) % 8;
      for (int j = 0; j < 3; ++j)
         {
         if (p_room_shape.side_of_border_line(next_obstacle_corner_x, next_obstacle_corner_y, curr_border_line_no) != PlaSide.ON_THE_LEFT)
            {
            return false;
            }
         curr_border_line_no = (curr_border_line_no + 1) % 8;
         }
      return true;
      }

   /**
    * Restrains the shape of p_incomplete_room to a octagon shape, which does not intersect with the interiour of p_obstacle_shape.
    * p_incomplete_room.get_contained_shape() must be contained in the shape of the result room.
    */
   private Collection<ExpandRoomFreespaceIncomplete> restrain_shape(ExpandRoomFreespaceIncomplete p_incomplete_room, ShapeTileOctagon p_obstacle_shape)
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
            System.out.println("ShapeSearchTree45Degree.restrain_shape: p_shape_to_be_contained is empty");
            }
         return result;
         }
      ShapeTileOctagon room_shape = p_incomplete_room.get_shape().bounding_octagon();
      ShapeTileOctagon shape_to_be_contained = p_incomplete_room.get_contained_shape().bounding_octagon();
      double cut_line_distance = -1;
      int restraining_line_no = -1;

      for (int obstacle_line_no = 0; obstacle_line_no < 8; ++obstacle_line_no)
         {
         double curr_distance = signed_line_distance(p_obstacle_shape, obstacle_line_no, shape_to_be_contained);
         if (curr_distance > cut_line_distance)
            {
            if (obstacle_segment_touches_inside(p_obstacle_shape, obstacle_line_no, room_shape))
               {
               cut_line_distance = curr_distance;
               restraining_line_no = obstacle_line_no;
               }
            }
         }
      if (cut_line_distance >= 0)
         {
         ShapeTileOctagon restrained_shape = calc_outside_restrained_shape(p_obstacle_shape, restraining_line_no, room_shape);
         result.add(new ExpandRoomFreespaceIncomplete(restrained_shape, p_incomplete_room.get_layer(), shape_to_be_contained));
         return result;
         }

      // There is no cut line, so that all p_shape_to_be_contained is completely on the right side of that line. Search a cut line, so that
      // at least part of p_shape_to_be_contained is on the right side.
      if (shape_to_be_contained.dimension().is_empty() )
         {
         // There is already a completed expansion room around p_shape_to_be_contained.
         return result;
         }

      restraining_line_no = -1;
      for (int obstacle_line_no = 0; obstacle_line_no < 8; ++obstacle_line_no)
         {
         if (obstacle_segment_touches_inside(p_obstacle_shape, obstacle_line_no, room_shape))
            {
            PlaLineInt curr_line = p_obstacle_shape.border_line(obstacle_line_no);
            if (shape_to_be_contained.side_of(curr_line) == PlaSide.COLLINEAR)
               {
               // curr_line intersects with the interiour of p_shape_to_be_contained
               restraining_line_no = obstacle_line_no;
               break;
               }
            }
         }
      if (restraining_line_no < 0)
         {
         // cut line not found, parts or the whole of p_shape may be already
         // occupied from somewhere else.
         return result;
         }
      ShapeTileOctagon restrained_shape = calc_outside_restrained_shape(p_obstacle_shape, restraining_line_no, room_shape);
      if (restrained_shape.dimension().is_area() )
         {
         ShapeTileOctagon new_shape_to_be_contained = shape_to_be_contained.intersection(restrained_shape);
         if ( ! new_shape_to_be_contained.dimension().is_empty() )
            {
            result.add(new ExpandRoomFreespaceIncomplete(restrained_shape, p_incomplete_room.get_layer(), new_shape_to_be_contained));
            }
         }

      ShapeTileOctagon rest_piece = calc_inside_restrained_shape(p_obstacle_shape, restraining_line_no, room_shape);
      if ( rest_piece.dimension().is_area() )
         {
         ShapeTile rest_shape_to_be_contained = shape_to_be_contained.intersection(rest_piece);
         
         if ( ! rest_shape_to_be_contained.dimension().is_empty() )
            {
            ExpandRoomFreespaceIncomplete rest_incomplete_room = new ExpandRoomFreespaceIncomplete(rest_piece, p_incomplete_room.get_layer(), rest_shape_to_be_contained);
            result.addAll(restrain_shape(rest_incomplete_room, p_obstacle_shape));
            }
         }
      return result;
      }

   private static double signed_line_distance(ShapeTileOctagon p_obstacle_shape, int p_obstacle_line_no, ShapeTileOctagon p_contained_shape)
      {
      double result;
      if (p_obstacle_line_no == 0)
         {
         result = p_obstacle_shape.oct_ly - p_contained_shape.oct_uy;
         }
      else if (p_obstacle_line_no == 2)
         {
         result = p_contained_shape.oct_lx - p_obstacle_shape.oct_rx;
         }
      else if (p_obstacle_line_no == 4)
         {
         result = p_contained_shape.oct_ly - p_obstacle_shape.oct_uy;
         }
      else if (p_obstacle_line_no == 6)
         {
         result = p_obstacle_shape.oct_lx - p_contained_shape.oct_rx;
         }
      // factor 0.5 used instead to 1 / sqrt(2) to prefer orthogonal lines slightly to diagonal restraining lines.
      else if (p_obstacle_line_no == 1)
         {
         result = 0.5 * (p_contained_shape.oct_ulx - p_obstacle_shape.oct_lrx);
         }
      else if (p_obstacle_line_no == 3)
         {
         result = 0.5 * (p_contained_shape.oct_llx - p_obstacle_shape.oct_urx);
         }
      else if (p_obstacle_line_no == 5)
         {
         result = 0.5 * (p_obstacle_shape.oct_ulx - p_contained_shape.oct_lrx);
         }
      else if (p_obstacle_line_no == 7)
         {
         result = 0.5 * (p_obstacle_shape.oct_llx - p_contained_shape.oct_urx);
         }
      else
         {
         System.out.println("ShapeSearchTree45Degree.signed_line_distance: p_obstacle_line_no out of range");
         result = 0;
         }
      return result;
      }

   /**
    * Intersects p_room_shape with the half plane defined by the outside of the borderline with index p_obstacle_line_no of
    * p_obstacle_shape.
    */
   ShapeTileOctagon calc_outside_restrained_shape(ShapeTileOctagon p_obstacle_shape, int p_obstacle_line_no, ShapeTileOctagon p_room_shape)
      {
      int lx = p_room_shape.oct_lx;
      int ly = p_room_shape.oct_ly;
      int rx = p_room_shape.oct_rx;
      int uy = p_room_shape.oct_uy;
      int ulx = p_room_shape.oct_ulx;
      int lrx = p_room_shape.oct_lrx;
      int llx = p_room_shape.oct_llx;
      int urx = p_room_shape.oct_urx;

      if (p_obstacle_line_no == 0)
         {
         uy = p_obstacle_shape.oct_ly;
         }
      else if (p_obstacle_line_no == 2)
         {
         lx = p_obstacle_shape.oct_rx;
         }
      else if (p_obstacle_line_no == 4)
         {
         ly = p_obstacle_shape.oct_uy;
         }
      else if (p_obstacle_line_no == 6)
         {
         rx = p_obstacle_shape.oct_lx;
         }
      else if (p_obstacle_line_no == 1)
         {
         ulx = p_obstacle_shape.oct_lrx;
         }
      else if (p_obstacle_line_no == 3)
         {
         llx = p_obstacle_shape.oct_urx;
         }
      else if (p_obstacle_line_no == 5)
         {
         lrx = p_obstacle_shape.oct_ulx;
         }
      else if (p_obstacle_line_no == 7)
         {
         urx = p_obstacle_shape.oct_llx;
         }
      else
         {
         System.out.println("ShapeSearchTree45Degree.calc_outside_restrained_shape: p_obstacle_line_no out of range");
         }

      ShapeTileOctagon result = new ShapeTileOctagon(lx, ly, rx, uy, ulx, lrx, llx, urx);
      return result.normalize();
      }

   /**
    * Intersects p_room_shape with the half plane defined by the inside of the borderline with index p_obstacle_line_no of
    * p_obstacle_shape.
    */
   ShapeTileOctagon calc_inside_restrained_shape(ShapeTileOctagon p_obstacle_shape, int p_obstacle_line_no, ShapeTileOctagon p_room_shape)
      {
      int lx = p_room_shape.oct_lx;
      int ly = p_room_shape.oct_ly;
      int rx = p_room_shape.oct_rx;
      int uy = p_room_shape.oct_uy;
      int ulx = p_room_shape.oct_ulx;
      int lrx = p_room_shape.oct_lrx;
      int llx = p_room_shape.oct_llx;
      int urx = p_room_shape.oct_urx;

      if (p_obstacle_line_no == 0)
         {
         ly = p_obstacle_shape.oct_ly;
         }
      else if (p_obstacle_line_no == 2)
         {
         rx = p_obstacle_shape.oct_rx;
         }
      else if (p_obstacle_line_no == 4)
         {
         uy = p_obstacle_shape.oct_uy;
         }
      else if (p_obstacle_line_no == 6)
         {
         lx = p_obstacle_shape.oct_lx;
         }
      else if (p_obstacle_line_no == 1)
         {
         lrx = p_obstacle_shape.oct_lrx;
         }
      else if (p_obstacle_line_no == 3)
         {
         urx = p_obstacle_shape.oct_urx;
         }
      else if (p_obstacle_line_no == 5)
         {
         ulx = p_obstacle_shape.oct_ulx;
         }
      else if (p_obstacle_line_no == 7)
         {
         llx = p_obstacle_shape.oct_llx;
         }
      else
         {
         System.out.println("ShapeSearchTree45Degree.calc_inside_restrained_shape: p_obstacle_line_no out of range");
         }

      ShapeTileOctagon result = new ShapeTileOctagon(lx, ly, rx, uy, ulx, lrx, llx, urx);
      return result.normalize();
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
            ShapeTile curr_tile_shape = curr_shape.bounding_octagon();
            if (curr_tile_shape.is_IntBox())
               {
               curr_tile_shape = curr_shape.bounding_box();

               // To avoid small corner cutoffs when taking the offset as an octagon.
               // That may complicate the room division in the maze expand algorithm unnecessesary.
               }

            int offset_width = this.get_clearance_compensation(p_drill_item.clearance_class_no(), p_drill_item.shape_layer(i));
            curr_tile_shape = (ShapeTile) curr_tile_shape.offset(offset_width);
            result[i] = curr_tile_shape.bounding_octagon();
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
         result[i] = result[i].bounding_octagon();
         }
      return result;
      }

   public ShapeTile[] calculate_tree_shapes(BrdOutline p_outline)
      {
      ShapeTile[] result = super.calculate_tree_shapes(p_outline);
      for (int i = 0; i < result.length; ++i)
         {
         result[i] = result[i].bounding_octagon();
         }
      return result;
      }
   }
