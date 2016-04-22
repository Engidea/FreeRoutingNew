package specctra;

import specctra.varie.DsnWriteResolution;

/**
 * Information from the parser scope in a Specctra-dsn-file. The fields are optional and may be null.
 */
public final class DsnParserInfo implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   // Character for quoting strings in a dsn-File. 
   public final String string_quote;
   public final String host_cad;
   public final String host_version;
   public final java.util.Collection<String[]> constants;
   public final DsnWriteResolution write_resolution;
   public final boolean dsn_file_generated_by_host;

   public DsnParserInfo ( DsnReadScopeParameters p_par )
      {
      string_quote = p_par.string_quote;
      host_cad = p_par.host_cad;
      host_version = p_par.host_version;
      constants = p_par.constants;
      write_resolution = p_par.write_resolution;
      dsn_file_generated_by_host = p_par.dsn_file_generated_by_host;
      }
   }
