package specctra;

import specctra.varie.DsnWriteResolution;

/**
 * Information from the parser scope in a Specctra-dsn-file. The fields are optional and may be null.
 */
public class DsnParserInfo implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // Character for quoting strings in a dsn-File. 
   public final String string_quote;
   public final String host_cad;
   public final String host_version;
   public final java.util.Collection<String[]> constants;
   public final DsnWriteResolution write_resolution;
   public final boolean dsn_file_generated_by_host;

   public DsnParserInfo(String p_string_quote, String p_host_cad, String p_host_version, java.util.Collection<String[]> p_constants, DsnWriteResolution p_write_resolution,
         boolean p_dsn_file_generated_by_host)
      {
      string_quote = p_string_quote;
      host_cad = p_host_cad;
      host_version = p_host_version;
      constants = p_constants;
      write_resolution = p_write_resolution;
      dsn_file_generated_by_host = p_dsn_file_generated_by_host;
      }

   }
