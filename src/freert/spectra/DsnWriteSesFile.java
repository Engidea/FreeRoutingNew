package freert.spectra;

import gui.varie.IndentFileWriter;
import interactive.IteraBoard;
import java.io.OutputStream;
import board.RoutingBoard;

public final class DsnWriteSesFile
   {
   private static final String classname = "DsnWriteSesFile.";
   
   private final IteraBoard board_handling;
   private final IndentFileWriter output_file;

   
   public DsnWriteSesFile ( IteraBoard p_board_handling, OutputStream p_output_stream )
      {
      board_handling = p_board_handling;
      output_file = new IndentFileWriter(p_output_stream);
      }
   
   
   /**
    * Writes p_board to a text file in the Specctra dsn format. 
    * Returns false, if the write failed. 
    * If p_compat_mode is true, only standard speecctra dsn scopes are written, so that any host system with an specctra interface can read them.
    */
   public boolean write( String p_design_name, boolean p_compat_mode)
      {
      try
         {
         write_dsn_scope( p_design_name, p_compat_mode);
         output_file.close();
         board_handling.userPrintln(classname+"write DONE");
         }
      catch (java.io.IOException exc)
         {
         board_handling.userPrintln("unable to write dsn file", exc);
         return false;
         }

      return true;
      }

   private void write_dsn_scope( String p_design_name, boolean p_compat_mode) throws java.io.IOException
      {
      RoutingBoard routing_board = board_handling.get_routing_board();
      DsnWriteScopeParameter write_scope_parameter = new DsnWriteScopeParameter(routing_board, board_handling.itera_settings.autoroute_settings, output_file,
            routing_board.host_com.specctra_parser_info.string_quote, routing_board.host_com.coordinate_transform, p_compat_mode);

      output_file.start_scope();
      output_file.write("PCB ");
      write_scope_parameter.identifier_type.write(p_design_name, output_file);
      DsnKeywordParser.write_scope(write_scope_parameter.file, write_scope_parameter.board.host_com.specctra_parser_info, write_scope_parameter.identifier_type, false);
      DsnKeywordResolution.write_scope(output_file, routing_board.host_com);
      DsnKeywordStructure.write_scope(write_scope_parameter);
      DsnKeywordPlacement.write_scope(write_scope_parameter);
      DsnKeywordLibrary.write_scope(write_scope_parameter);
      DsnKeywordPartLibrary.write_scope(write_scope_parameter);
      DsnKeywordNetwork.write_scope(write_scope_parameter);
      DsnKeywordWiring.write_scope(write_scope_parameter);
      output_file.end_scope();
      }

   }
