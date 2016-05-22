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
 * Polygon.java
 *
 * Created on 15. Mai 2004, 08:39
 */

package freert.spectra;

import freert.planar.PlaPointInt;
import freert.planar.PlaPointIntAlist;
import freert.planar.PlaShape;
import freert.planar.ShapePolygon;
import gui.varie.IndentFileWriter;

/**
 * Describes a polygon in a Specctra dsn file.
 *
 * @author alfons
 */
public class DsnPolygon extends DsnShape
   {
   /**
    * Creates a new instance of Polygon p_coor is an array of dimension of dimension 2 * point_count and contains x0, y0, x1, y1,
    * ... If the polygon is used as rectangle,
    */
   public DsnPolygon(DsnLayer p_layer, double[] p_coor)
      {
      super(p_layer);
      coor = p_coor;
      }

   public PlaShape transform_to_board(DsnCoordinateTransform p_coordinate_transform)
      {
      int list_len = coor.length / 2;
      
      PlaPointIntAlist corner_arr = new PlaPointIntAlist(list_len);
      double[] curr_point = new double[2];
      
      for (int index = 0; index < list_len; ++index)
         {
         curr_point[0] = coor[2 * index];
         curr_point[1] = coor[2 * index + 1];
         corner_arr.add( p_coordinate_transform.dsn_to_board(curr_point).round());
         }
      
      return new ShapePolygon(corner_arr);
      }

   public freert.planar.PlaShape transform_to_board_rel(DsnCoordinateTransform p_coordinate_transform)
      {
      if (coor.length < 2)
         {
         return freert.planar.ShapeTileSimplex.EMPTY;
         }
      
      int list_len = coor.length / 2;
      
      PlaPointIntAlist corner_arr = new PlaPointIntAlist(list_len);
      for (int i = 0; i < list_len; ++i)
         {
         long curr_x = Math.round(p_coordinate_transform.dsn_to_board(coor[2 * i]));
         long curr_y = Math.round(p_coordinate_transform.dsn_to_board(coor[2 * i + 1]));
         corner_arr.add( new PlaPointInt(curr_x, curr_y));
         }
      
      return new ShapePolygon(corner_arr);
      }

   public DsnRectangle bounding_box()
      {
      double[] bounds = new double[4];
      bounds[0] = Integer.MAX_VALUE;
      bounds[1] = Integer.MAX_VALUE;
      bounds[2] = Integer.MIN_VALUE;
      bounds[3] = Integer.MIN_VALUE;
      for (int i = 0; i < coor.length; ++i)
         {
         if (i % 2 == 0)
            {
            // x coordinate
            bounds[0] = Math.min(bounds[0], coor[i]);
            bounds[2] = Math.max(bounds[2], coor[i]);
            }
         else
            {
            // x coordinate
            bounds[1] = Math.min(bounds[1], coor[i]);
            bounds[3] = Math.max(bounds[3], coor[i]);
            }
         }
      return new DsnRectangle(layer, bounds);
      }

   /**
    * Writes this polygon as a scope to an output dsn-file.
    */
   public void write_scope(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("polygon ");
      p_identifier_type.write(this.layer.name, p_file);
      p_file.write(" ");
      p_file.write((new Integer(0)).toString());
      int corner_count = coor.length / 2;
      for (int i = 0; i < corner_count; ++i)
         {
         p_file.new_line();
         p_file.write(new Double(coor[2 * i]).toString());
         p_file.write(" ");
         p_file.write(new Double(coor[2 * i + 1]).toString());
         }
      p_file.end_scope();
      }

   public void write_scope_int(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("polygon ");
      p_identifier_type.write(this.layer.name, p_file);
      p_file.write(" ");
      p_file.write((new Integer(0)).toString());
      int corner_count = coor.length / 2;
      for (int i = 0; i < corner_count; ++i)
         {
         p_file.new_line();
         Integer curr_coor = (int) Math.round(coor[2 * i]);
         p_file.write(curr_coor.toString());
         p_file.write(" ");
         curr_coor = (int) Math.round(coor[2 * i + 1]);
         p_file.write(curr_coor.toString());
         }
      p_file.end_scope();
      }

   public final double[] coor;
   }
