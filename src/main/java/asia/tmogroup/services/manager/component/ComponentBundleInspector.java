package net.evalcode.services.manager.component;


import java.net.URL;
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
  Set<Class<?>> getExportedJpaEntities();
  Set<Class<?>> getExportedJaxbEntities();

  Map<String, String> getBundleProperties();
  String getBundlePropertiesFileName();

  URL searchResourceInBundleClassPath(String resourceFileName);
}
