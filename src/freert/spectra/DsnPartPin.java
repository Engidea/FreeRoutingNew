package freert.spectra;
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

public class DsnPartPin
   {
   public final String pin_name;
   public final String gate_name;
   public final int gate_swap_code;
   public final String gate_pin_name;
   public final int gate_pin_swap_code;

   public DsnPartPin(String p_pin_name, String p_gate_name, int p_gate_swap_code, String p_gate_pin_name, int p_gate_pin_swap_code)
      {
      pin_name = p_pin_name;
      gate_name = p_gate_name;
      gate_swap_code = p_gate_swap_code;
      gate_pin_name = p_gate_pin_name;
      gate_pin_swap_code = p_gate_pin_swap_code;
      }
   }
