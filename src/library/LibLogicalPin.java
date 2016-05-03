package library;

import java.io.Serializable;

public final class LibLogicalPin implements Comparable<LibLogicalPin>, Serializable
   {
   private static final long serialVersionUID = 1L;

   // The number of the part pin. Must be the same number as in the componnents library package.
   public final int pin_no;
   // The name of the part pin. Must be the same name as in the componnents library package.
   public final String pin_name;
   // The name of the gate this pin belongs to
   public final String gate_name;
   // The gate swap code. Gates with the same gate swap code can be swapped. Gates with swap code <= 0 are not swappable.
   public final int gate_swap_code;
   // The identifier of the pin in the gate
   public final String gate_pin_name;
   // The pin swap code of the gate. Pins with the same pin swap code can be swapped inside a gate. Pins with swap code <= 0 are not swappable.
   public final int gate_pin_swap_code;
   
   public LibLogicalPin(int p_pin_no, String p_pin_name, String p_gate_name, int p_gate_swap_code, String p_gate_pin_name, int p_gate_pin_swap_code)
      {
      pin_no = p_pin_no;
      pin_name = p_pin_name;
      gate_name = p_gate_name;
      gate_swap_code = p_gate_swap_code;
      gate_pin_name = p_gate_pin_name;
      gate_pin_swap_code = p_gate_pin_swap_code;
      }

   public int compareTo(LibLogicalPin p_other)
      {
      return pin_no - p_other.pin_no;
      }

   }
