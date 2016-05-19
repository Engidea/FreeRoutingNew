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
 * Circle.java
 *
 * Created on 20. Mai 2004, 09:22
 */

package specctra;

import freert.planar.PlaPointInt;
import gui.varie.IndentFileWriter;

/**
 * Class for reading and writing circle scopes from dsn-files.
 *
 * @author alfons
 */
public class DsnCircle extends DsnShape
   {
   /**
    * Creates a new circle from the input parameters. p_coor is an array of dimension 3. p_coor [0] is the radius of the circle,
    * p_coor [1] is the x coordinate of the circle, p_coor [2] is the y coordinate of the circle.
    */
   public DsnCircle(DsnLayer p_layer, double[] p_coor)
      {
      super(p_layer);
      coor = p_coor;
      }

   public DsnCircle(DsnLayer p_layer, double p_radius, double p_center_x, double p_center_y)
      {
      super(p_layer);
      coor = new double[3];
      coor[0] = p_radius;
      coor[1] = p_center_x;
      coor[2] = p_center_y;
      }

   public freert.planar.PlaShape transform_to_board(DsnCoordinateTransform p_coordinate_transform)
      {
      double[] location = new double[2];
      location[0] = coor[1];
      location[1] = coor[2];
      PlaPointInt center = p_coordinate_transform.dsn_to_board(location).round();
      int radius = (int) Math.round(p_coordinate_transform.dsn_to_board(coor[0]) / 2);
      return new freert.planar.ShapeCircle(center, radius);
      }

   public freert.planar.PlaShape transform_to_board_rel(DsnCoordinateTransform p_coordinate_transform)
      {
      long[] new_coor = new long[3];
      new_coor[0] = Math.round(p_coordinate_transform.dsn_to_board(coor[0]) / 2);
      for (int i = 1; i < 3; ++i)
         {
         new_coor[i] = Math.round(p_coordinate_transform.dsn_to_board(coor[i]));
         }
      return new freert.planar.ShapeCircle(new PlaPointInt(new_coor[1], new_coor[2]), (int)new_coor[0]);
      }

   public DsnRectangle bounding_box()
      {
      double[] bounds = new double[4];
      bounds[0] = coor[1] - coor[0];
      bounds[1] = coor[2] - coor[0];
      bounds[2] = coor[1] + coor[0];
      bounds[3] = coor[2] + coor[0];
      return new DsnRectangle(layer, bounds);
      }

   public void write_scope(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.new_line();
      p_file.write("(circle ");
      p_identifier_type.write(this.layer.name, p_file);
      for (int i = 0; i < coor.length; ++i)
         {
         p_file.write(" ");
         p_file.write(new Double(coor[i]).toString());
         }
      p_file.write(")");
      }

   public void write_scope_int(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.new_line();
      p_file.write("(circle ");
      p_identifier_type.write(this.layer.name, p_file);
      for (int i = 0; i < coor.length; ++i)
         {
         p_file.write(" ");
         Integer curr_coor = (int) Math.round(coor[i]);
         p_file.write(curr_coor.toString());
         }
      p_file.write(")");
      }

   public final double[] coor;
   }
