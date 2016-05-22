package freert.spectra.varie;

import board.items.BrdAbitPin;


public class EaglePinInfo
   {
   public final BrdAbitPin pin;
   public BrdAbitPin curr_changed_to;

   public EaglePinInfo(BrdAbitPin p_pin)
      {
      pin = p_pin;
      curr_changed_to = p_pin;
      }
   }
