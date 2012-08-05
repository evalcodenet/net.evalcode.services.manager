package net.evalcode.services.manager.internal;


import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.evalcode.services.manager.component.Configuration;
import net.evalcode.services.manager.internal.util.SystemProperty;


/**
 * ComponentBundleConfiguration
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleConfiguration implements Configuration
{
  // MEMBERS
  private final ComponentBundleInstance bundle;
  private final ConcurrentMap<String, String> configuration=
    new ConcurrentHashMap<String, String>();


  // CONSTRUCTION
  ComponentBundleConfiguration(final ComponentBundleInstance bundle)
  {
    this.bundle=bundle;

    reload();
  }


  // ACCESSORS/MUTATORS
  void reload()
  {
    configuration.clear();

    configuration.putAll(bundle.getInspector().getBundleProperties());
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String get(final String key)
  {
    final String value=SystemProperty.get(key);

    if(null!=value)
      return value;

    return configuration.get(key);
  }

  @Override
  public String get(final String key, final String defaultValue)
  {
    final String value=get(key);

    if(null==value)
      return defaultValue;

    return value;
  }

  @Override
  public String set(final String key, final String value)
  {
    synchronized(this)
    {
      final String previousValue=get(key);

      configuration.put(key, value);

      return previousValue;
    }
  }

  @Override
  public Set<String> keySet()
  {
    final Set<String> keySet=new HashSet<String>();

    keySet.addAll(SystemProperty.keySet());
    keySet.addAll(configuration.keySet());

    return keySet;
  }

  @Override
  public File getConfigurationFile(final String configurationFileName)
  {
    File configurationFile=new File(
      SystemProperty.getLocalConfigurationPath()+File.separator+
      bundle.getName()+File.separator+
      configurationFileName
    );

    if(!configurationFile.exists())
    {
      configurationFile=new File(
        SystemProperty.getGlobalConfigurationPath()+File.separator+
        bundle.getName()+File.separator+
        configurationFileName
      );
    }

    if(!configurationFile.exists()&&null!=bundle.getBundle().getResource(configurationFileName))
    {
      configurationFile=new File(
        bundle.getBundle().getResource(configurationFileName).toExternalForm()
      );
    }

    return configurationFile;
  }

  @Override
  public String getResourcePath()
  {
    return SystemProperty.getResourcesPath(bundle.getName());
  }
}
