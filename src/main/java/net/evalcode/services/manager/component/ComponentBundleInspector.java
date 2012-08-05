package net.evalcode.services.manager.component;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


/**
 * ComponentBundleInspector
 *
 * @author carsten.schipke@gmail.com
 */
public interface ComponentBundleInspector
{
  // ACCESSORS/MUTATORS
  String getPropertiesFileName();
  Map<String, String> getBundleProperties();

  String getLocalConfigurationFilePath(String configurationFileName);
  String getGlobalConfigurationFilePath(String configurationFileName);

  File getConfigurationFile(String configurationFileName, boolean searchGlobal,
    boolean searchBundleClasspath) throws FileNotFoundException;

  void writeConfigurationFile(String configurationFileName, String content, boolean createFile)
    throws IOException;
  String readConfigurationFile(String configurationFileName, boolean searchGlobal)
    throws FileNotFoundException;

  Set<Class<?>> getExportedJpaEntities();
  Set<Class<?>> getExportedJaxbEntities();
}
