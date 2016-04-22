package freert.planar;


final class ShapePolygonDivisionPoint implements PlaObject
   {
   public final PlaPoint[] corners;

   final int corner_no_after_projection;
   final PlaPointFloat projection;

   /**
    * At a concave corner of the closed polygon, a minimal axis parallel division line is constructed, to divide the closed polygon
    * into two.
    */
   ShapePolygonDivisionPoint(PlaPoint[] p_corners, int p_concave_corner_no)
      {
      corners = p_corners;

      PlaPointFloat concave_corner = corners[p_concave_corner_no].to_float();
      PlaPointFloat before_concave_corner;

      if (p_concave_corner_no != 0)
         before_concave_corner = corners[p_concave_corner_no - 1].to_float();
      else
         before_concave_corner = corners[corners.length - 1].to_float();

      PlaPointFloat after_concave_corner;

      if (p_concave_corner_no == corners.length - 1)
         after_concave_corner = corners[0].to_float();
      else
         after_concave_corner = corners[p_concave_corner_no + 1].to_float();

      boolean search_right = before_concave_corner.point_y > concave_corner.point_y || concave_corner.point_y > after_concave_corner.point_y;

      boolean search_left = before_concave_corner.point_y < concave_corner.point_y || concave_corner.point_y < after_concave_corner.point_y;

      boolean search_up = before_concave_corner.point_x < concave_corner.point_x || concave_corner.point_x < after_concave_corner.point_x;

      boolean search_down = before_concave_corner.point_x > concave_corner.point_x || concave_corner.point_x > after_concave_corner.point_x;

      double min_projection_dist = Integer.MAX_VALUE;
      PlaPointFloat min_projection = null;
      int corner_no_after_min_projection = 0;

      int corner_no_after_curr_projection = (p_concave_corner_no + 2) % corners.length;

      PlaPoint corner_before_curr_projection;
      if (corner_no_after_curr_projection != 0)
         corner_before_curr_projection = corners[corner_no_after_curr_projection - 1];
      else
         corner_before_curr_projection = corners[corners.length - 1];
      PlaPointFloat corner_before_projection_approx = corner_before_curr_projection.to_float();

      double curr_dist;
      int loop_end = corners.length - 2;

      for (int i = 0; i < loop_end; ++i)
         {
         PlaPoint corner_after_curr_projection = corners[corner_no_after_curr_projection];
         PlaPointFloat corner_after_projection_approx = corner_after_curr_projection.to_float();
         if (corner_before_projection_approx.point_y != corner_after_projection_approx.point_y)
         // try a horizontal division
            {
            double min_y;
            double max_y;

            if (corner_after_projection_approx.point_y > corner_before_projection_approx.point_y)
               {
               min_y = corner_before_projection_approx.point_y;
               max_y = corner_after_projection_approx.point_y;
               }
            else
               {
               min_y = corner_after_projection_approx.point_y;
               max_y = corner_before_projection_approx.point_y;
               }

            if (concave_corner.point_y >= min_y && concave_corner.point_y <= max_y)
               {
               PlaLineInt curr_line = new PlaLineInt(corner_before_curr_projection, corner_after_curr_projection);
               double x_intersect = curr_line.function_in_y_value_approx(concave_corner.point_y);
               curr_dist = Math.abs(x_intersect - concave_corner.point_x);
               // Make shure, that the new shape will not be concave at the projection point.
               // That might happen, if the boundary curve runs back in itself.
               boolean projection_ok = curr_dist < min_projection_dist
                     && (search_right && x_intersect > concave_corner.point_x && concave_corner.point_y <= corner_after_projection_approx.point_y || search_left
                           && x_intersect < concave_corner.point_x && concave_corner.point_y >= corner_after_projection_approx.point_y);
               if (projection_ok)
                  {
                  min_projection_dist = curr_dist;
                  corner_no_after_min_projection = corner_no_after_curr_projection;
                  min_projection = new PlaPointFloat(x_intersect, concave_corner.point_y);
                  }
               }
            }

         if (corner_before_projection_approx.point_x != corner_after_projection_approx.point_x)
         // try a vertical division
            {
            double min_x;
            double max_x;
            if (corner_after_projection_approx.point_x > corner_before_projection_approx.point_x)
               {
               min_x = corner_before_projection_approx.point_x;
               max_x = corner_after_projection_approx.point_x;
               }
            else
               {
               min_x = corner_after_projection_approx.point_x;
               max_x = corner_before_projection_approx.point_x;
               }
            if (concave_corner.point_x >= min_x && concave_corner.point_x <= max_x)
               {
               PlaLineInt curr_line = new PlaLineInt(corner_before_curr_projection, corner_after_curr_projection);
               double y_intersect = curr_line.function_value_approx(concave_corner.point_x);
               curr_dist = Math.abs(y_intersect - concave_corner.point_y);
               // make shure, that the new shape will be convex at the projection point
               boolean projection_ok = curr_dist < min_projection_dist
                     && (search_up && y_intersect > concave_corner.point_y && concave_corner.point_x >= corner_after_projection_approx.point_x || search_down && y_intersect < concave_corner.point_y
                           && concave_corner.point_x <= corner_after_projection_approx.point_x);

               if (projection_ok)
                  {
                  min_projection_dist = curr_dist;
                  corner_no_after_min_projection = corner_no_after_curr_projection;
                  min_projection = new PlaPointFloat(concave_corner.point_x, y_intersect);
                  }
               }
            }
         corner_before_curr_projection = corner_after_curr_projection;
         corner_before_projection_approx = corner_after_projection_approx;
         if (corner_no_after_curr_projection == corners.length - 1)
            {
            corner_no_after_curr_projection = 0;
            }
         else
            {
            ++corner_no_after_curr_projection;
            }
         }
      if (min_projection_dist == Integer.MAX_VALUE)
         {
         System.out.println("PolygonShape.DivisionPoint: projection not found");
         }

      projection = min_projection;
      corner_no_after_projection = corner_no_after_min_projection;
      }

   @Override
   public boolean is_NaN()
      {
      return false;
      }
   
   
   
   
   
   
   
   
   
   }
