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
 * Path.java
 *
 * Created on 24. Mai 2004, 08:10
 */

package specctra;

import freert.planar.PlaPointFloat;
import freert.planar.PlaPointIntAlist;
import freert.planar.PlaShape;
import freert.planar.ShapePolygon;
import freert.planar.ShapeTileOctagon;
import gui.varie.IndentFileWriter;

/**
 * Class for reading and writing path scopes consisting of a polygon from dsn-files.
 *
 * @author Alfons Wirtz
 */
public class DsnPolygonPath extends DsnPath
   {

   /** Creates a new instance of PolygonPath */
   public DsnPolygonPath(DsnLayer p_layer, double p_width, double[] p_coordinate_arr)
      {
      super(p_layer, p_width, p_coordinate_arr);
      }

   /**
    * Writes this path as a scope to an output dsn-file.
    */
   public void write_scope(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("path ");
      p_identifier_type.write(layer.name, p_file);
      p_file.write(" ");
      p_file.write((new Double(width)).toString());
      int corner_count = coordinate_arr.length / 2;
      for (int i = 0; i < corner_count; ++i)
         {
         p_file.new_line();
         p_file.write(new Double(coordinate_arr[2 * i]).toString());
         p_file.write(" ");
         p_file.write(new Double(coordinate_arr[2 * i + 1]).toString());
         }
      p_file.end_scope();
      }

   public void write_scope_int(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("path ");
      p_identifier_type.write(layer.name, p_file);
      p_file.write(" ");
      p_file.write((new Double(width)).toString());
      int corner_count = coordinate_arr.length / 2;
      for (int i = 0; i < corner_count; ++i)
         {
         p_file.new_line();
         Integer curr_coor = (int) Math.round(coordinate_arr[2 * i]);
         p_file.write(curr_coor.toString());
         p_file.write(" ");
         curr_coor = (int) Math.round(coordinate_arr[2 * i + 1]);
         p_file.write(curr_coor.toString());
         }
      p_file.end_scope();
      }

   public PlaShape transform_to_board(DsnCoordinateTransform p_coordinate_transform)
      {
      PlaPointFloat[] corner_arr = new PlaPointFloat[coordinate_arr.length / 2];
      double[] curr_point = new double[2];
      for (int index = 0; index < corner_arr.length; ++index)
         {
         curr_point[0] = coordinate_arr[2 * index];
         curr_point[1] = coordinate_arr[2 * index + 1];
         corner_arr[index] = p_coordinate_transform.dsn_to_board(curr_point);
         }
      
      double offset = p_coordinate_transform.dsn_to_board(width) / 2;
      if (corner_arr.length <= 2)
         {
         ShapeTileOctagon bounding_oct = bounding_octagon(corner_arr);
         return bounding_oct.enlarge(offset);
         }
      
      PlaPointIntAlist rounded_corner_arr = new PlaPointIntAlist(corner_arr.length);
      for (int i = 0; i < corner_arr.length; ++i)
         {
         rounded_corner_arr.add( corner_arr[i].round());
         }
      
      PlaShape result = new ShapePolygon(rounded_corner_arr);
      if (offset > 0)
         {
         result = result.bounding_tile().enlarge(offset);
         }
      return result;
      }

   public freert.planar.PlaShape transform_to_board_rel(DsnCoordinateTransform p_coordinate_transform)
      {
      PlaPointFloat[] corner_arr = new PlaPointFloat[coordinate_arr.length / 2];
      double[] curr_point = new double[2];
      for (int i = 0; i < corner_arr.length; ++i)
         {
         curr_point[0] = coordinate_arr[2 * i];
         curr_point[1] = coordinate_arr[2 * i + 1];
         corner_arr[i] = p_coordinate_transform.dsn_to_board_rel(curr_point);
         }
      double offset = p_coordinate_transform.dsn_to_board(width) / 2;
      if (corner_arr.length <= 2)
         {
         ShapeTileOctagon bounding_oct = bounding_octagon(corner_arr);
         return bounding_oct.enlarge(offset);
         }
      
      PlaPointIntAlist rounded_corner_arr = new PlaPointIntAlist(corner_arr.length);
      for (int i = 0; i < corner_arr.length; ++i)
         {
         rounded_corner_arr.add(corner_arr[i].round());
         }
      
      PlaShape result = new freert.planar.ShapePolygon(rounded_corner_arr);
      
      if (offset > 0)
         {
         result = result.bounding_tile().enlarge(offset);
         }
      return result;
      }

   public DsnRectangle bounding_box()
      {
      double offset = width / 2;
      double[] bounds = new double[4];
      bounds[0] = Integer.MAX_VALUE;
      bounds[1] = Integer.MAX_VALUE;
      bounds[2] = Integer.MIN_VALUE;
      bounds[3] = Integer.MIN_VALUE;
      for (int i = 0; i < coordinate_arr.length; ++i)
         {
         if (i % 2 == 0)
            {
            // x coordinate
            bounds[0] = Math.min(bounds[0], coordinate_arr[i] - offset);
            bounds[2] = Math.max(bounds[2], coordinate_arr[i]) + offset;
            }
         else
            {
            // x coordinate
            bounds[1] = Math.min(bounds[1], coordinate_arr[i] - offset);
            bounds[3] = Math.max(bounds[3], coordinate_arr[i] + offset);
            }
         }
      return new DsnRectangle(layer, bounds);
      }
   
   
   
   
   /**
    * Calculates the smallest IntOctagon containing all the input points
    */
   public ShapeTileOctagon bounding_octagon(PlaPointFloat[] p_point_arr)
      {
      double lx = Integer.MAX_VALUE;
      double ly = Integer.MAX_VALUE;
      double rx = Integer.MIN_VALUE;
      double uy = Integer.MIN_VALUE;
      double ulx = Integer.MAX_VALUE;
      double lrx = Integer.MIN_VALUE;
      double llx = Integer.MAX_VALUE;
      double urx = Integer.MIN_VALUE;
      for (int i = 0; i < p_point_arr.length; ++i)
         {
         PlaPointFloat curr = p_point_arr[i];
         lx = Math.min(lx, curr.v_x);
         ly = Math.min(ly, curr.v_y);
         rx = Math.max(rx, curr.v_x);
         uy = Math.max(uy, curr.v_y);
         double tmp = curr.v_x - curr.v_y;
         ulx = Math.min(ulx, tmp);
         lrx = Math.max(lrx, tmp);
         tmp = curr.v_x + curr.v_y;
         llx = Math.min(llx, tmp);
         urx = Math.max(urx, tmp);
         }
      ShapeTileOctagon surrounding_octagon = new ShapeTileOctagon((int) Math.floor(lx), (int) Math.floor(ly), (int) Math.ceil(rx), (int) Math.ceil(uy), (int) Math.floor(ulx), (int) Math.ceil(lrx),
            (int) Math.floor(llx), (int) Math.ceil(urx));
      return surrounding_octagon;
      }

   
   
   
   
   
   
   
   
   
   
   
   
   }
