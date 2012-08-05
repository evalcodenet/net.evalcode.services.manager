package net.evalcode.services.manager.component;


import java.io.File;
import java.util.Set;


/**
 * Configuration
 *
 * @author carsten.schipke@gmail.com
 */
public interface Configuration
{
  // ACCESSORS/MUTATORS
  String get(final String key);
  String get(final String key, final String defaultValue);

  String set(final String key, final String value);

  Set<String> keySet();

  File getConfigurationFile(final String configurationFileName);

  String getResourcePath();
}
