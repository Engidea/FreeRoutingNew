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
 * Cursor.java
 *
 * Created on 17. Maerz 2006, 06:52
 *
 */

package gui.varie;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/**
 *
 * @author Alfons Wirtz
 */
public abstract class GuiCursor
   {
   protected boolean location_initialized = false;
   protected double cursor_x_coor;
   protected double cursor_y_coor;

   private final BasicStroke init_basic;
   private final AlphaComposite init_alpha; 
   
   public GuiCursor ()
      {
      init_basic =  new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);;
      init_alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
      }
   
   public abstract void draw(Graphics p_graphics);

   public final void set_location(Point2D p_location)
      {
      cursor_x_coor = p_location.getX();
      cursor_y_coor = p_location.getY();
      location_initialized = true;
      }

   protected void init_graphics(Graphics2D p_graphics)
      {
      p_graphics.setStroke(init_basic);
      p_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      p_graphics.setColor(Color.WHITE);
      p_graphics.setComposite(init_alpha);
      }
   }
