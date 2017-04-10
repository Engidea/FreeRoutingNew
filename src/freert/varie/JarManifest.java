package freert.varie;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This is useful as a reference to get all attributes from Manifest
 * I wanted to find out the compile date of the main class but there is a simple way
 * Search for Implementation-Version and there is a neat way to find out the implementation version of a class
 * without rumaging by hand in a Manifest (also look at the build.xml)
 * Apparently you also have to define the Specification-Version otherwise it result in null...
 * This class anyway parses ALL manifests packet up and put all attributes together, yes, not really useful
 * @author damiano
 */
public class JarManifest
   {
   private final HashMap<String,String> attrMap = new HashMap<String,String>();
   
   public JarManifest ()
      {
      try
         {
         parse ();
         }
      catch ( Exception exc )
         {
         exc.printStackTrace();
         }
      }
   
   public String getValue (String key )
      {
      return attrMap.get(key);
      }
   
   public Set<String> getKeys ()
      {
      return attrMap.keySet();
      }
   
   private void parseManifest (URL url ) throws IOException
      {
      InputStream is = url.openStream();
      
      if (is == null) return;
      
      Manifest manifest = new Manifest(is);
      
      Attributes attrs = manifest.getMainAttributes();
      
      for ( Object key : attrs.keySet() )
         {
         if ( key instanceof Attributes.Name )
            {
            Attributes.Name nam = (Attributes.Name)key;
            String skey = nam.toString();
            String value = attrs.getValue(skey);
            attrMap.put(skey, value);
            }
         }
      
      is.close();
      }
   
   private void parse () throws IOException
      {
      Enumeration<URL> resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
      
      while (resEnum.hasMoreElements()) 
         {
         try 
            {
            URL url = resEnum.nextElement();
            parseManifest (url);
            }
         catch ( Exception exc )
            {
            exc.printStackTrace();
            }
         }
      }
   
   }
