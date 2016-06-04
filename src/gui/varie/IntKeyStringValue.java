package gui.varie;

public class IntKeyStringValue
   {
   public final Integer key;
   public final String value;
   
   public IntKeyStringValue(int p_key, String p_value)
      {
      key = Integer.valueOf(p_key);
      value = p_value;
      }
   
   public String toString()
      {
      return value;
      }
   }
