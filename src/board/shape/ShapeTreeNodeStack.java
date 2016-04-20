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
 * ArrayStack.java
 *
 * Created on 11. Maerz 2006, 06:52
 *
 */

package board.shape;

import java.util.LinkedList;

/**
 * Implementation of a stack as an array
 * Since this is used only for ShapeTreeNode I am making a specific class
 * Also, I wonder, if a normal list would be more appropriate....
 *
 * @author Alfons Wirtz
 */
public final class ShapeTreeNodeStack
   {
   private final LinkedList<ShapeTreeNode> node_list = new LinkedList<ShapeTreeNode>();

   /**
    * Sets the stack to empty.
    */
   public void reset()
      {
      node_list.clear();
      }

   /**
    * Pushed p_element onto the stack.
    */
   public void push(ShapeTreeNode p_element)
      {
      if ( p_element == null ) return;
      
      node_list.add(p_element);
      }

   /**
    * Pops the next element from the top of the stack
    * @return null, if the stack is exhausted.
    */
   public ShapeTreeNode pop()
      {
      if (node_list.isEmpty() )
         return null;

      return node_list.removeLast();
      }
   }
