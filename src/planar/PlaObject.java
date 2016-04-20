package planar;

/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */


/**
 * There is a need to allow creation of Pla objects that are "null" but still can have their methods called
 * and should return "null" objects. This is because it is quite nice to "concatenate" method calls but all this
 * fails miserably in a maze of Exception handling or in a maze of if null tests if there is no explicit way to 
 * declare a Pla as not a number
 * 
 * At some point in the code you can test the "final" result and decide what to do when things are NaN
 * 
 * @author damiano
 *
 */
public interface PlaObject
   {
   /**
    * If an object is created with invalid params a call to this method will return true
    * any call to the object method that returns other pla object should return objects with is_NaN true
    * @return true if the oject is Not a Number, invalid
    */
   public boolean is_NaN ();
   
   }
