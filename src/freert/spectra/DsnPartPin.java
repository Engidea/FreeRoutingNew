package freert.spectra;

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
