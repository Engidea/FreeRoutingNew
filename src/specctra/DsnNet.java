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
 * Net.java
 *
 * Created on 19. Mai 2004, 08:58
 */

package specctra;

import gui.varie.IndentFileWriter;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * Class for reading and writing net scopes from dsn-files.
 *
 * @author alfons
 */
public class DsnNet
   {
   public final DsnNetId id;

   /** List of elements of type Pin. */
   private Set<DsnNetPin> pin_list = null;
   
   public DsnNet(DsnNetId p_net_id)
      {
      id = p_net_id;
      }

   public static void write_scope(DsnWriteScopeParameter p_par, freert.rules.RuleNet p_net, Collection<board.items.BrdAbitPin> p_pin_list) throws java.io.IOException
      {
      p_par.file.start_scope();
      write_net_id(p_net, p_par.file, p_par.identifier_type);
      // write the pins scope
      p_par.file.start_scope();
      p_par.file.write("pins");
      Iterator<board.items.BrdAbitPin> it = p_pin_list.iterator();
      while (it.hasNext())
         {
         board.items.BrdAbitPin curr_pin = it.next();
         if (curr_pin.contains_net(p_net.net_number))
            {
            write_pin(p_par, curr_pin);
            }
         }
      p_par.file.end_scope();
      p_par.file.end_scope();
      }

   public static void write_net_id(freert.rules.RuleNet p_net, IndentFileWriter p_file, DsnIdentifier p_identifier_type) throws java.io.IOException
      {
      p_file.write("net ");
      p_identifier_type.write(p_net.name, p_file);
      p_file.write(" ");
      Integer subnet_number = p_net.subnet_number;
      p_file.write(subnet_number.toString());
      }

   public static void write_pin(DsnWriteScopeParameter p_par, board.items.BrdAbitPin p_pin) throws java.io.IOException
      {
      board.infos.BrdComponent curr_component = p_par.board.brd_components.get(p_pin.get_component_no());
      if (curr_component == null)
         {
         System.out.println("Net.write_scope: component not found");
         return;
         }
      freert.library.LibPackagePin lib_pin = curr_component.get_package().get_pin(p_pin.get_index_in_package());
      if (lib_pin == null)
         {
         System.out.println("Net.write_scope:  pin number out of range");
         return;
         }
      p_par.file.new_line();
      p_par.identifier_type.write(curr_component.name, p_par.file);
      p_par.file.write("-");
      p_par.identifier_type.write(lib_pin.name, p_par.file);

      }

   public void set_pins(Collection<DsnNetPin> p_pin_list)
      {
      pin_list = new TreeSet<DsnNetPin>();
      for (DsnNetPin curr_pin : p_pin_list)
         {
         pin_list.add(curr_pin);
         }
      }

   public Set<DsnNetPin> get_pins()
      {
      return pin_list;
      }

   }