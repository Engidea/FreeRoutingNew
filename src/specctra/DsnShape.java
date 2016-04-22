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
 * Shape.java
 *
 * Created on 16. Mai 2004, 11:09
 */
package specctra;

import gui.varie.IndentFileWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import planar.PlaArea;
import planar.PlaShape;
import planar.PolylineArea;
import planar.ShapePolyline;
import specctra.varie.DsnReadUtils;

/**
 * Describes a shape in a Specctra dsn file.
 *
 * @author alfons
 */
public abstract class DsnShape
   {

   /**
    * Writes a shape scope to a Specctra dsn file.
    */
   public abstract void write_scope(IndentFileWriter p_file, DsnIdentifier p_identifier) throws java.io.IOException;

   /**
    * Writes a shape scope to a Specctra session file. In a session file all coordinates must be integer.
    */
   public abstract void write_scope_int(IndentFileWriter p_file, DsnIdentifier p_identifier) throws java.io.IOException;

   /**
    * Reads shape scope from a Specctra dsn file. If p_layer_structure == null, only Layer.PCB and Layer.Signal are expected, no
    * induvidual layers.
    */
   public static final DsnShape read_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      DsnShape result = null;
      try
         {
         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.OPEN_BRACKET)
            {
            // overread the open bracket
            next_token = p_scanner.next_token();
            }

         if (next_token == DsnKeyword.RECTANGLE)
            {

            result = DsnShape.read_rectangle_scope(p_scanner, p_layer_structure);
            }
         else if (next_token == DsnKeyword.POLYGON)
            {

            result = DsnShape.read_polygon_scope(p_scanner, p_layer_structure);
            }
         else if (next_token == DsnKeyword.CIRCLE)
            {

            result = DsnShape.read_circle_scope(p_scanner, p_layer_structure);
            }
         else if (next_token == DsnKeyword.POLYGON_PATH)
            {
            result = DsnShape.read_polygon_path_scope(p_scanner, p_layer_structure);
            }
         else
            {
            // not a shape scope, skip it.
            DsnKeywordScope.skip_scope(p_scanner);
            }
         }
      catch (java.io.IOException e)
         {
         System.out.println("Shape.read_scope: IO error scanning file");
         System.out.println(e);
         return result;
         }
      return result;
      }

   /**
    * Reads an object of type PolylinePath from the dsn-file.
    */
   public static DsnPolylinePath read_polyline_path_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      try
         {
         DsnLayer layer = null;
         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.PCB_SCOPE)
            {
            layer = DsnLayer.PCB;
            }
         else if (next_token == DsnKeyword.SIGNAL)
            {
            layer = DsnLayer.SIGNAL;
            }
         else
            {
            if (p_layer_structure == null)
               {
               System.out.println("PolylinePath.read_scope: only layer types pcb or signal expected");
               return null;
               }
            if (!(next_token instanceof String))
               {
               System.out.println("PolylinePath.read_scope: layer name string expected");
               return null;
               }
            int layer_no = p_layer_structure.get_no((String) next_token);
            if (layer_no < 0 || layer_no >= p_layer_structure.arr.length)
               {
               System.out.print("Shape.read_polyline_path_scope: layer name ");
               System.out.print((String) next_token);
               System.out.println(" not found in layer structure ");
               return null;
               }
            layer = p_layer_structure.arr[layer_no];
            }
         Collection<Object> corner_list = new LinkedList<Object>();

         // read the width and the corners of the path
         for (;;)
            {
            next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            corner_list.add(next_token);
            }
         if (corner_list.size() < 5)
            {
            System.out.println("PolylinePath.read_scope: to few numbers in scope");
            return null;
            }
         Iterator<Object> it = corner_list.iterator();
         double width = 0;
         Object next_object = it.next();
         if (next_object instanceof Double)
            {
            width = ((Double) next_object).doubleValue();
            }
         else if (next_object instanceof Integer)
            {
            width = ((Integer) next_object).intValue();
            }
         else
            {
            System.out.println("PolylinePath.read_scope: number expected");
            return null;
            }
         double[] corner_arr = new double[corner_list.size() - 1];
         for (int i = 0; i < corner_arr.length; ++i)
            {
            next_object = it.next();
            if (next_object instanceof Double)
               {
               corner_arr[i] = ((Double) next_object).doubleValue();
               }
            else if (next_object instanceof Integer)
               {
               corner_arr[i] = ((Integer) next_object).intValue();
               }
            else
               {
               System.out.println("Shape.read_polygon_path_scope: number expected");
               return null;
               }

            }
         return new DsnPolylinePath(layer, width, corner_arr);
         }
      catch (java.io.IOException e)
         {
         System.out.println("PolylinePath.read_scope: IO error scanning file");
         System.out.println(e);
         return null;
         }
      }

   /**
    * Reads a shape , which may contain holes from a specctra dsn-file. The first shape in the shape_list of the result is the
    * border of the area. The other shapes in the shape_list are holes (windows).
    */
   public static final DsnScopeArea read_area_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure, boolean p_skip_window_scopes)
      {
      Collection<DsnShape> shape_list = new LinkedList<DsnShape>();
      String clearance_class_name = null;
      String area_name = null;
      boolean result_ok = true;
      Object next_token = null;
      try
         {
         next_token = p_scanner.next_token();
         }
      catch (java.io.IOException e)
         {
         System.out.println("Shape.read_area_scope: IO error scanning file");
         return null;
         }
      if (next_token instanceof String)
         {
         String curr_name = (String) next_token;
         if (!curr_name.isEmpty())
            {
            area_name = curr_name;
            }
         }
      DsnShape curr_shape = DsnShape.read_scope(p_scanner, p_layer_structure);
      if (curr_shape == null)
         {
         result_ok = false;
         }
      shape_list.add(curr_shape);
      next_token = null;
      for (;;)
         {
         Object prev_token = next_token;
         try
            {
            next_token = p_scanner.next_token();
            }
         catch (java.io.IOException e)
            {
            System.out.println("Shape.read_area_scope: IO error scanning file");
            return null;
            }
         if (next_token == null)
            {
            System.out.println("Shape.read_area_scope: unexpected end of file");
            return null;
            }
         if (next_token == DsnKeyword.CLOSED_BRACKET)
            {
            // end of scope
            break;
            }

         if (prev_token == DsnKeyword.OPEN_BRACKET)
            {
            // a new scope is expected
            if (next_token == DsnKeyword.WINDOW && !p_skip_window_scopes)
               {
               DsnShape hole_shape = DsnShape.read_scope(p_scanner, p_layer_structure);
               shape_list.add(hole_shape);
               // overread closing bracket
               try
                  {
                  next_token = p_scanner.next_token();
                  }
               catch (java.io.IOException e)
                  {
                  System.out.println("Shape.read_area_scope: IO error scanning file");
                  return null;
                  }
               if (next_token != DsnKeyword.CLOSED_BRACKET)
                  {
                  System.out.println("Shape.read_area_scope: closed bracket expected");
                  return null;
                  }

               }
            else if (next_token == DsnKeyword.CLEARANCE_CLASS)
               {
               clearance_class_name = DsnReadUtils.read_string_scope(p_scanner);
               }
            else
               {
               // skip unknown scope
               DsnKeywordScope.skip_scope(p_scanner);
               }
            }
         }
      if (!result_ok)
         {
         return null;
         }
      return new DsnScopeArea(area_name, shape_list, clearance_class_name);
      }

   /**
    * Reads a rectangle scope from a Specctra dsn file. If p_layer_structure == null, only Layer.PCB and Layer.Signal are expected,
    * no induvidual layers.
    */
   public static DsnRectangle read_rectangle_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      try
         {
         DsnLayer rect_layer = null;
         double rect_coor[] = new double[4];

         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.PCB_SCOPE)
            {
            rect_layer = DsnLayer.PCB;
            }
         else if (next_token == DsnKeyword.SIGNAL)
            {
            rect_layer = DsnLayer.SIGNAL;
            }
         else if (p_layer_structure != null)
            {
            if (!(next_token instanceof String))
               {
               System.out.println("Shape.read_rectangle_scope: layer name string expected");
               return null;
               }
            String layer_name = (String) next_token;
            int layer_no = p_layer_structure.get_no(layer_name);
            if (layer_no < 0 || layer_no >= p_layer_structure.arr.length)
               {
               System.out.println("Shape.read_rectangle_scope: layer name " + layer_name + " not found in layer structure ");
               }
            else
               {
               rect_layer = p_layer_structure.arr[layer_no];
               }
            }
         else
            {
            rect_layer = DsnLayer.SIGNAL;
            }
         // fill the the rectangle
         for (int i = 0; i < 4; ++i)
            {
            next_token = p_scanner.next_token();
            if (next_token instanceof Double)
               {
               rect_coor[i] = ((Double) next_token).doubleValue();
               }
            else if (next_token instanceof Integer)
               {
               rect_coor[i] = ((Integer) next_token).intValue();
               }
            else
               {
               System.out.println("Shape.read_rectangle_scope: number expected");
               return null;
               }
            }
         // overread the closing bracket

         next_token = p_scanner.next_token();
         if (next_token != DsnKeyword.CLOSED_BRACKET)
            {
            System.out.println("Shape.read_rectangle_scope ) expected");
            return null;
            }
         if (rect_layer == null)
            {
            return null;
            }
         return new DsnRectangle(rect_layer, rect_coor);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Shape.read_rectangle_scope: IO error scanning file");
         System.out.println(e);
         return null;
         }
      }

   /**
    * Reads a closed polygon scope from a Specctra dsn file. If p_layer_structure == null, only Layer.PCB and Layer.Signal are
    * expected, no induvidual layers.
    */
   public static DsnPolygon read_polygon_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      try
         {
         DsnLayer polygon_layer = null;
         boolean layer_ok = true;
         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.PCB_SCOPE)
            {
            polygon_layer = DsnLayer.PCB;
            }
         else if (next_token == DsnKeyword.SIGNAL)
            {
            polygon_layer = DsnLayer.SIGNAL;
            }
         else
            {
            if (p_layer_structure == null)
               {
               System.out.println("Shape.read_polygon_scope: only layer types pcb or signal expected");
               return null;
               }
            if (!(next_token instanceof String))
               {
               System.out.println("Shape.read_polygon_scope: layer name string expected");
               return null;
               }
            int layer_no = p_layer_structure.get_no((String) next_token);
            if (layer_no < 0 || layer_no >= p_layer_structure.arr.length)
               {
               System.out.print("Shape.read_polygon_scope: layer name ");
               System.out.print((String) next_token);
               System.out.println(" not found in layer structure ");
               layer_ok = false;
               }
            else
               {
               polygon_layer = p_layer_structure.arr[layer_no];
               }
            }

         // overread the aperture width
         next_token = p_scanner.next_token();

         Collection<Object> coor_list = new LinkedList<Object>();

         // read the coordinates of the polygon
         for (;;)
            {
            next_token = p_scanner.next_token();
            if (next_token == null)
               {
               System.out.println("Shape.read_polygon_scope: unexpected end of file");
               return null;
               }
            if (next_token == DsnKeyword.OPEN_BRACKET)
               {
               // unknown scope
               DsnKeywordScope.skip_scope(p_scanner);
               next_token = p_scanner.next_token();
               }
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            coor_list.add(next_token);
            }
         if (!layer_ok)
            {
            return null;
            }
         double[] coor_arr = new double[coor_list.size()];
         Iterator<Object> it = coor_list.iterator();
         for (int i = 0; i < coor_arr.length; ++i)
            {
            Object next_object = it.next();
            if (next_object instanceof Double)
               {
               coor_arr[i] = ((Double) next_object).doubleValue();
               }
            else if (next_object instanceof Integer)
               {
               coor_arr[i] = ((Integer) next_object).intValue();
               }
            else
               {
               System.out.println("Shape.read_polygon_scope: number expected");
               return null;
               }

            }
         return new DsnPolygon(polygon_layer, coor_arr);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Rectangle.read_scope: IO error scanning file");
         System.out.println(e);
         return null;
         }
      }

   /**
    * Reads a circle scope from a Specctra dsn file.
    */
   public static DsnCircle read_circle_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      try
         {
         DsnLayer circle_layer = null;
         boolean layer_ok = true;
         double circle_coor[] = new double[3];

         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.PCB_SCOPE)
            {
            circle_layer = DsnLayer.PCB;
            }
         else if (next_token == DsnKeyword.SIGNAL)
            {
            circle_layer = DsnLayer.SIGNAL;
            }
         else
            {
            if (p_layer_structure == null)
               {
               System.out.println("Shape.read_circle_scope: p_layer_structure != null expected");
               return null;
               }
            if (!(next_token instanceof String))
               {
               System.out.println("Shape.read_circle_scope: string for layer_name expected");
               return null;
               }
            int layer_no = p_layer_structure.get_no((String) next_token);
            if (layer_no < 0 || layer_no >= p_layer_structure.arr.length)
               {
               System.out.print("Shape.read_circle_scope: layer with name ");
               System.out.print((String) next_token);
               System.out.println(" not found in layer stracture ");
               layer_ok = false;
               }
            else
               {
               circle_layer = p_layer_structure.arr[layer_no];
               }
            }
         // fill the the the coordinates
         int curr_index = 0;
         for (;;)
            {
            next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            if (curr_index > 2)
               {
               System.out.println("Shape.read_circle_scope: closed bracket expected");
               return null;
               }
            if (next_token instanceof Double)
               {
               circle_coor[curr_index] = ((Double) next_token).doubleValue();
               }
            else if (next_token instanceof Integer)
               {
               circle_coor[curr_index] = ((Integer) next_token).intValue();
               }
            else
               {
               System.out.println("Shape.read_circle_scope: number expected");
               return null;
               }
            ++curr_index;
            }
         if (!layer_ok)
            {
            return null;
            }
         return new DsnCircle(circle_layer, circle_coor);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Shape.read_rectangle_scope: IO error scanning file");
         System.out.println(e);
         return null;
         }
      }

   /**
    * Reads an object of type Path from the dsn-file.
    */
   public static DsnPolygonPath read_polygon_path_scope(JflexScanner p_scanner, DsnLayerStructure p_layer_structure)
      {
      try
         {
         DsnLayer layer = null;
         boolean layer_ok = true;
         Object next_token = p_scanner.next_token();
         if (next_token == DsnKeyword.PCB_SCOPE)
            {
            layer = DsnLayer.PCB;
            }
         else if (next_token == DsnKeyword.SIGNAL)
            {
            layer = DsnLayer.SIGNAL;
            }
         else
            {
            if (p_layer_structure == null)
               {
               System.out.println("Shape.read_polygon_path_scope: only layer types pcb or signal expected");
               return null;
               }
            if (!(next_token instanceof String))
               {
               System.out.println("Path.read_scope: layer name string expected");
               return null;
               }
            int layer_no = p_layer_structure.get_no((String) next_token);
            if (layer_no < 0 || layer_no >= p_layer_structure.arr.length)
               {
               System.out.print("Shape.read_polygon_path_scope: layer with name ");
               System.out.print((String) next_token);
               System.out.println(" not found in layer structure ");
               layer_ok = false;
               }
            else
               {
               layer = p_layer_structure.arr[layer_no];
               }
            }
         Collection<Object> corner_list = new LinkedList<Object>();

         // read the width and the corners of the path
         for (;;)
            {
            next_token = p_scanner.next_token();
            if (next_token == DsnKeyword.OPEN_BRACKET)
               {
               // unknown scope
               DsnKeywordScope.skip_scope(p_scanner);
               next_token = p_scanner.next_token();
               }
            if (next_token == DsnKeyword.CLOSED_BRACKET)
               {
               break;
               }
            corner_list.add(next_token);
            }
         if (corner_list.size() < 5)
            {
            System.out.println("Shape.read_polygon_path_scope: to few numbers in scope");
            return null;
            }
         if (!layer_ok)
            {
            return null;
            }
         Iterator<Object> it = corner_list.iterator();
         double width = 0;
         Object next_object = it.next();
         if (next_object instanceof Double)
            {
            width = ((Double) next_object).doubleValue();
            }
         else if (next_object instanceof Integer)
            {
            width = ((Integer) next_object).intValue();
            }
         else
            {
            System.out.println("Shape.read_polygon_path_scope: number expected");
            return null;
            }
         double[] coordinate_arr = new double[corner_list.size() - 1];
         for (int i = 0; i < coordinate_arr.length; ++i)
            {
            next_object = it.next();
            if (next_object instanceof Double)
               {
               coordinate_arr[i] = ((Double) next_object).doubleValue();
               }
            else if (next_object instanceof Integer)
               {
               coordinate_arr[i] = ((Integer) next_object).intValue();
               }
            else
               {
               System.out.println("Shape.read_polygon_path_scope: number expected");
               return null;
               }

            }
         return new DsnPolygonPath(layer, width, coordinate_arr);
         }
      catch (java.io.IOException e)
         {
         System.out.println("Shape.read_polygon_path_scope: IO error scanning file");
         System.out.println(e);
         return null;
         }
      }

   /**
    * Transforms a shape with holes to the board coordinate system. 
    * The first shape in the Collection p_area is the border, the other shapes are holes of the area.
    */
   public static PlaArea transform_area_to_board(Collection<DsnShape> p_area, DsnCoordinateTransform p_coordinate_transform)
      {
      int hole_count = p_area.size() - 1;
      if (hole_count <= -1)
         {
         System.out.println("Shape.transform_area_to_board: p_area.size() > 0 expected");
         return null;
         }
      Iterator<DsnShape> it = p_area.iterator();
      DsnShape boundary = it.next();
      PlaShape boundary_shape = boundary.transform_to_board(p_coordinate_transform);
      
      PlaArea result;
      if (hole_count == 0)
         {
         result = boundary_shape;
         }
      else
         {
         // Area with holes
         if (!(boundary_shape instanceof ShapePolyline))
            {
            System.out.println("Shape.transform_area_to_board: PolylineShape expected");
            return null;
            }
         ShapePolyline border = (ShapePolyline) boundary_shape;
         ShapePolyline[] holes = new ShapePolyline[hole_count];
         for (int i = 0; i < holes.length; ++i)
            {
            PlaShape hole_shape = it.next().transform_to_board(p_coordinate_transform);
            if (!(hole_shape instanceof ShapePolyline))
               {
               System.out.println("Shape.transform_area_to_board: PolylineShape expected");
               return null;
               }
            holes[i] = (ShapePolyline) hole_shape;
            }
         result = new PolylineArea(border, holes);
         }
      return result;
      }

   /**
    * Transforms the relative coordinates of a shape with holes to the board coordinate system. The first shape in the Collection
    * p_area is the border, the other shapes are holes of the area.
    */
   public static PlaArea transform_area_to_board_rel(Collection<DsnShape> p_area, DsnCoordinateTransform p_coordinate_transform)
      {
      int hole_count = p_area.size() - 1;
      if (hole_count <= -1)
         {
         System.out.println("Shape.transform_area_to_board_rel: p_area.size() > 0 expected");
         return null;
         }
      Iterator<DsnShape> it = p_area.iterator();
      DsnShape boundary = it.next();
      PlaShape boundary_shape = boundary.transform_to_board_rel(p_coordinate_transform);
      PlaArea result;
      if (hole_count == 0)
         {
         result = boundary_shape;
         }
      else
         {
         // Area with holes
         if (!(boundary_shape instanceof ShapePolyline))
            {
            System.out.println("Shape.transform_area_to_board_rel: PolylineShape expected");
            return null;
            }
         ShapePolyline border = (ShapePolyline) boundary_shape;
         ShapePolyline[] holes = new ShapePolyline[hole_count];
         for (int i = 0; i < holes.length; ++i)
            {
            PlaShape hole_shape = it.next().transform_to_board_rel(p_coordinate_transform);
            if (!(hole_shape instanceof ShapePolyline))
               {
               System.out.println("Shape.transform_area_to_board: PolylineShape expected");
               return null;
               }
            holes[i] = (ShapePolyline) hole_shape;
            }
         result = new PolylineArea(border, holes);
         }
      return result;
      }

   public void write_hole_scope(IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.start_scope();
      p_file.write("window");
      write_scope(p_file, p_identifier_type);
      p_file.end_scope();
      }

   /**
    * Transforms a specctra dsn shape to a geometry.planar.Shape.
    */
   public abstract PlaShape transform_to_board(DsnCoordinateTransform p_coordinate_transform);

   /**
    * Returns the smallest axis parallel rectangle containing this shape.
    */
   public abstract DsnRectangle bounding_box();

   /**
    * Transforms the relative (vector) coordinates of a specctra dsn shape to a geometry.planar.Shape.
    */
   public abstract PlaShape transform_to_board_rel(DsnCoordinateTransform p_coordinate_transform);

   protected DsnShape(DsnLayer p_layer)
      {
      layer = p_layer;
      }

   public final DsnLayer layer;

   }