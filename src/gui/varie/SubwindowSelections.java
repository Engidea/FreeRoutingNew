package gui.varie;

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
 * Used for storing the subwindow filters in a snapshot.
 */
public class SubwindowSelections implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public SnapSelection incompletes_selection;
   public SnapSelection packages_selection;
   public SnapSelection nets_selection;
   public SnapSelection components_selection;
   public SnapSelection padstacks_selection;
   }
