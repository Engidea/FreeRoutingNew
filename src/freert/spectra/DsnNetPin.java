package freert.spectra;


/**
 * Sorted tuple of component name and pin name.
 */
public class DsnNetPin implements Comparable<DsnNetPin>
   {
   public DsnNetPin(String p_component_name, String p_pin_name)
      {
      component_name = p_component_name;
      pin_name = p_pin_name;
      }

   public int compareTo(DsnNetPin p_other)
      {
      int result = this.component_name.compareTo(p_other.component_name);
      if (result == 0)
         {
         result = this.pin_name.compareTo(p_other.pin_name);
         }
      return result;
      }

   public final String component_name;
   public final String pin_name;
   }