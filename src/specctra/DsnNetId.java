package specctra;


public class DsnNetId implements Comparable<DsnNetId>
   {
   public DsnNetId(String p_name, int p_subnet_number)
      {
      name = p_name;
      subnet_number = p_subnet_number;
      }

   public int compareTo(DsnNetId p_other)
      {
      int result = this.name.compareTo(p_other.name);
      if (result == 0)
         {
         result = this.subnet_number - p_other.subnet_number;
         }
      return result;
      }

   public final String name;
   public final int subnet_number;
   }
