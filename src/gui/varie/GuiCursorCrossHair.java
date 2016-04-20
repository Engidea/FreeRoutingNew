package gui.varie;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;


/**
 * 
 * @author damiano
 *
 */
public final class GuiCursorCrossHair extends GuiCursor
   {
   private static final double MAX_COOR = 1000;
   
   private final GeneralPath draw_path;
   
   public GuiCursorCrossHair()
      {
      super();
      Line2D VERTICAL_LINE = new Line2D.Double(0, -MAX_COOR, 0, MAX_COOR);
      Line2D HORIZONTAL_LINE = new Line2D.Double(-MAX_COOR, 0, MAX_COOR, 0);
      Line2D RIGHT_DIAGONAL_LINE = new Line2D.Double(-MAX_COOR, -MAX_COOR, MAX_COOR, MAX_COOR);
      Line2D LEFT_DIAGONAL_LINE = new Line2D.Double(-MAX_COOR, MAX_COOR, MAX_COOR, -MAX_COOR);
      
      draw_path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
      draw_path.append(VERTICAL_LINE, false);
      draw_path.append(HORIZONTAL_LINE, false);
      draw_path.append(RIGHT_DIAGONAL_LINE, false);
      draw_path.append(LEFT_DIAGONAL_LINE, false);
      }
   
   @Override
   public void draw(Graphics p_graphics)
      {
      if (!location_initialized) return;

      Graphics2D g2 = (Graphics2D) p_graphics;
      init_graphics(g2);
      g2.translate(cursor_x_coor, cursor_y_coor);
      g2.draw(draw_path);
      }
   }
