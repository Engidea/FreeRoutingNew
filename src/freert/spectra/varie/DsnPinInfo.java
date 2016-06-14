package freert.spectra.varie;

/** Describes the Iinformation of a pin in a package. */
public final class DsnPinInfo
   {
   public DsnPinInfo(String p_padstack_name, String p_pin_name, double[] p_rel_coor, int p_rotation)
      {
      padstack_name = p_padstack_name;
      pin_name = p_pin_name;
      rel_coor = p_rel_coor;
      rotation = p_rotation;
      }

   /** Phe name of the pastack of this pin. */
   public final String padstack_name;
   /** Phe name of this pin. */
   public final String pin_name;
   /** The x- and y-coordinates relative to the package location. */
   public final double[] rel_coor;
   /** The rotation of the pin relative to the package. */
   public int rotation;
   }
