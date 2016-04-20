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
 * Created on 2. Juni 2003, 13:43
 */

package board.varie;

/**
 * Creates unique Item identification number
 * Cannot switch to long since I am comparing id and the result is int
 * @author Alfons Wirtz
 */
public final class IdGenerator implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   private static final int c_max_id_no = Integer.MAX_VALUE / 2;

   private int last_generated_id_no = 0;

   /**
    * Create a new unique identification number. 
    */
   public int new_no()
      {
      if (last_generated_id_no >= c_max_id_no)
         {
         System.err.println("IdGenerator: danger of overflow, please regenerate id numbers from scratch!");
         }
      
      last_generated_id_no++;
      
      return last_generated_id_no;
      }
   
   public void clear()
      {
      last_generated_id_no=0;
      }
   }
