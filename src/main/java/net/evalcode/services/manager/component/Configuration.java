package net.evalcode.services.manager.component;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;


/**
 * Configuration
 *
 * @author carsten.schipke@gmail.com
 */
public interface Configuration
{
  // ACCESSORS/MUTATORS
  String get(String key);
  String get(String key, String defaultValue);

  String set(String key, String value);

  Set<String> keySet();

  Path getResourcePath();
  Path getResourcePath(String... subPath);

  Path getLocalConfigurationPath();
  Path getLocalConfigurationPath(String... subPath);

  Path getGlobalConfigurationPath();
  Path getGlobalConfigurationPath(String... subPath);

  URL getConfigurationFileResource(String configurationFileName)
    throws IOException;

  URL getConfigurationFileResource(String resourceName, boolean searchGlobal,
    boolean searchBundleClasspath)
      throws IOException;

  void writeConfigurationFileResource(String resourceName, String content,
    boolean createResource)
      throws IOException;

  String readConfigurationFileResource(String resourceName, boolean searchGlobal)
    throws IOException;
}
