package specctra;

import java.util.SortedSet;

public class DsnLogicalPartMapping
   {
   // The name of the mapping
   public final String name;

   // The components belonging to the mapping
   public final SortedSet<String> components;

   public DsnLogicalPartMapping(String p_name, SortedSet<String> p_components)
      {
      name = p_name;
      components = p_components;
      }

   }