package freert.library;

import freert.planar.PlaVectorInt;

/**
 * Describes a pin padstack of a package.
 */
public final class LibPackagePin implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   /// The name of the pin. 
   public final String name;
   // The number of the padstack mask of the pin
   public final int padstack_no;
   // The location of the pin relative to its package. 
   public final PlaVectorInt relative_location;
   // the rotation of the pin padstack 
   public final double rotation_in_degree;
   
   /**
    * Creates a new package pin with the input coordinates relative to the package location.
    */
   public LibPackagePin(String p_name, int p_padstack_no, PlaVectorInt p_relative_location, double p_rotation_in_degree)
      {
      name = p_name;
      padstack_no = p_padstack_no;
      relative_location = p_relative_location;
      rotation_in_degree = p_rotation_in_degree;
      }
   
   public PlaVectorInt relative_location()
      {
      return relative_location;
      }
   
   }

