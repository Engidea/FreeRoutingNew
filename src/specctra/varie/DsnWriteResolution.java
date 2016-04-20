package specctra.varie;

public class DsnWriteResolution implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   public final String char_name;
   public final int positive_int;

   public DsnWriteResolution(String p_char_name, int p_positive_int)
      {
      char_name = p_char_name;
      positive_int = p_positive_int;
      }

   }
