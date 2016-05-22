package freert.spectra;


public class DsnLogicalPart
   {
   public DsnLogicalPart(String p_name, java.util.Collection<DsnPartPin> p_part_pins)
      {
      name = p_name;
      part_pins = p_part_pins;
      }

   /** The name of the maopping. */
   public final String name;

   /** The pins of this logical part */
   public final java.util.Collection<DsnPartPin> part_pins;
   }
