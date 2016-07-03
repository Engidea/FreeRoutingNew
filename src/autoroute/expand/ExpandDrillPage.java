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
 * DrillPage.java
 *
 * Created on 26. Maerz 2006, 10:46
 *
 */

package autoroute.expand;

import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import autoroute.ArtEngine;
import autoroute.maze.MazeSearchElement;
import board.RoutingBoard;
import board.awtree.AwtreeFindEntry;
import board.awtree.AwtreeShapeSearch;
import board.items.BrdAbitPin;
import board.items.BrdItem;
import freert.graphics.GdiContext;
import freert.planar.PlaAreaLinear;
import freert.planar.PlaDimension;
import freert.planar.PlaPointInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;

/**
 *
 * @author Alfons Wirtz
 */
public final class ExpandDrillPage implements ExpandObject
   {
   private final RoutingBoard r_board;
   // The shape of the page 
   public final ShapeTileBox page_shape;

   private final MazeSearchElement[] maze_search_info_arr;
   
   // The list of expansion drills on this page. can be empty if not calculated
   private final Collection<ExpandDrill> drill_list = new LinkedList<ExpandDrill>();
   // The number of the net, for which the drills are calculated 
   private int net_no = -1;
   
   
   public ExpandDrillPage(ShapeTileBox p_shape, RoutingBoard p_board)
      {
      r_board = p_board;
      page_shape = p_shape;
      maze_search_info_arr = new MazeSearchElement[p_board.get_layer_count()];
      
      for (int index = 0; index < maze_search_info_arr.length; ++index)
         {
         maze_search_info_arr[index] = new MazeSearchElement();
         }
      }

   /**
    * Gets and possibly recalculates drill bits
    * Returns the drills on this page. If p_atttach_smd, drilling to SMD pins is allowed.
    */
   public Collection<ExpandDrill> get_drills(ArtEngine p_art_engine, boolean p_attach_smd)
      {
      // if the net I am handling is this net return the current drills
      if ( p_art_engine.get_net_no() == net_no) return drill_list;
      
      // now, it happens that I recalculate the parameters...
      net_no = p_art_engine.get_net_no();
      drill_list.clear();
      
      // Use the search tree from the autoroute, it is adjusted with compensation
      AwtreeShapeSearch search_tree = p_art_engine.art_search_tree;
      
      Collection<AwtreeFindEntry> overlaps = search_tree.find_overlap_tree_entries(page_shape, -1);
      
      Collection<ShapeTile> cutout_shapes = new LinkedList<ShapeTile>();

      // drills on top of existing vias are used in the ripup algorithm
      ShapeTile prev_obstacle_shape = ShapeTileBox.EMPTY;
      
      for (AwtreeFindEntry curr_entry : overlaps)
         {
         if (!(curr_entry.object instanceof BrdItem)) continue;
      
         BrdItem curr_item = (BrdItem) curr_entry.object;
         
         if (curr_item.is_drillable(net_no)) continue;
         
         if (p_attach_smd && (curr_item instanceof BrdAbitPin) && ((BrdAbitPin) curr_item).drill_allowed()) continue;
         
         ShapeTile curr_obstacle_shape = curr_item.get_tree_shape(search_tree, curr_entry.shape_index_in_object);

         if (!prev_obstacle_shape.contains(curr_obstacle_shape))
            {
            // Checked to avoid multiple cutout for example for vias with the same shape on all layers.
            ShapeTile curr_cutout_shape = curr_obstacle_shape.intersection(page_shape);

            if (curr_cutout_shape.dimension() == PlaDimension.AREA)
               {
               cutout_shapes.add(curr_cutout_shape);
               }
            }
         
         prev_obstacle_shape = curr_obstacle_shape;
         }
      
      ShapeTile[] holes = new ShapeTile[cutout_shapes.size()];

      Iterator<ShapeTile> iter = cutout_shapes.iterator();
      
      for (int index = 0; index < holes.length; ++index)
         {
         holes[index] = iter.next();
         }
      
      PlaAreaLinear shape_with_holes = new PlaAreaLinear(page_shape, holes);
      ShapeTile[] drill_shapes = shape_with_holes.split_to_convex();

      // Use the center points of these drill shapes to try making a via.
      int drill_first_layer = 0;
      int drill_last_layer = r_board.get_layer_count() - 1;
      
      for (int i = 0; i < drill_shapes.length; ++i)
         {
         ShapeTile curr_drill_shape = drill_shapes[i];
         PlaPointInt curr_drill_location = null;
         if (p_attach_smd)
            {
            curr_drill_location = calc_pin_center_in_drill(curr_drill_shape, drill_first_layer, p_art_engine.r_board);
            if (curr_drill_location == null)
               {
               curr_drill_location = calc_pin_center_in_drill(curr_drill_shape, drill_last_layer, p_art_engine.r_board);
               }
            }
         if (curr_drill_location == null)
            {
            curr_drill_location = curr_drill_shape.centre_of_gravity().round();
            }
         ExpandDrill new_drill = new ExpandDrill(curr_drill_shape, curr_drill_location, drill_first_layer, drill_last_layer);
         if (new_drill.calculate_expansion_rooms(p_art_engine))
            {
            drill_list.add(new_drill);
            }
         }

      return drill_list;
      }

   @Override
   public ShapeTile get_shape()
      {
      return page_shape;
      }

   @Override
   public PlaDimension get_dimension()
      {
      return PlaDimension.AREA;
      }

   @Override
   public int maze_search_element_count()
      {
      return maze_search_info_arr.length;
      }

   @Override
   public MazeSearchElement get_maze_search_element(int p_no)
      {
      return maze_search_info_arr[p_no];
      }

   /**
    * Resets all drills of this page for autorouting the next connection.
    */
   @Override
   public void reset()
      {
      for (ExpandDrill curr_drill : drill_list)
         curr_drill.reset();
      
      for (MazeSearchElement curr_info : maze_search_info_arr)
         curr_info.reset();
      }

   /**
    * Invalidates the drills of this page so that they are recalculated at the
    * next call of get_drills().
    */
   public void invalidate()
      {
      net_no = -1;  // this is the real invalidate
      drill_list.clear();  // this helps the GC 
      }

   
   /*
    * Test draw of the drills on this page.
    */
   public void draw(Graphics p_graphics, GdiContext p_graphics_context, double p_intensity)
      {
      for (ExpandDrill curr_drill : drill_list)
         {
         curr_drill.draw(p_graphics, p_graphics_context, p_intensity);
         }
      }

   @Override
   public ExpandRoomComplete other_room_complete(ExpandRoomComplete p_room)
      {
      return null;
      }

   /**
    * Looks if p_drill_shape contains the center of a drillable Pin on p_layer.
    * @return null, if no such Pin was found.
    */
   private PlaPointInt calc_pin_center_in_drill(ShapeTile p_drill_shape, int p_layer, RoutingBoard p_board)
      {
      Collection<BrdItem> overlapping_items = p_board.overlapping_items(p_drill_shape, p_layer);
      
      for (BrdItem curr_item : overlapping_items)
         {
         if ( ! (curr_item instanceof board.items.BrdAbitPin) ) continue;

         BrdAbitPin curr_pin = (BrdAbitPin) curr_item;
      
         if (curr_pin.drill_allowed() && p_drill_shape.contains_inside(curr_pin.center_get()))
            return curr_pin.center_get();
         }

      return null;
      }

   }
