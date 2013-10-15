package net.evalcode.services.manager.internal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.evalcode.services.manager.component.Configuration;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.misc.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ComponentBundleConfiguration
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleConfiguration implements Configuration
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(ComponentBundleConfiguration.class);


  // MEMBERS
  private final ConcurrentMap<String, String> configuration=new ConcurrentHashMap<>();
  private final Charset charset=SystemProperty.getCharset();
  private final ComponentBundleInstance bundle;


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
  public Path getResourcePath()
  {
    if(configuration.containsKey(bundle.getName().concat(".resources")))
      return Paths.get(configuration.get(bundle.getName().concat(".resources")));

    return SystemProperty.getResourcesPath(bundle.getName());
  }

  @Override
  public Path getResourcePath(final String... subPath)
  {
    if(configuration.containsKey(bundle.getName().concat(".resources")))
      return Paths.get(configuration.get(bundle.getName().concat(".resources")));

    return Paths.get(SystemProperty.getResourcesPath(bundle.getName()).toString(), subPath);
  }

  @Override
  public URL getConfigurationFileResource(final String configurationFileName)
    throws IOException
  {
    return getConfigurationFileResource(configurationFileName, true, true);
  }

  @Override
  public URL getConfigurationFileResource(final String configurationFileName,
    final boolean searchGlobal, final boolean searchBundleClasspath)
      throws IOException
  {
    // try local configuration path
    File configurationFile=getLocalConfigurationPath(configurationFileName).toFile();

    if(searchGlobal && !configurationFile.exists())
    {
      // try global configuration path
      configurationFile=getGlobalConfigurationPath(configurationFileName).toFile();
    }

    if(!configurationFile.exists())
    {
      if(searchBundleClasspath)
      {
        final URL bundleConfigurationFile=bundle.getInspector()
          .searchResourceInBundleClassPath(configurationFileName);

        if(null!=bundleConfigurationFile)
          return bundleConfigurationFile;
      }

      throw new FileNotFoundException(configurationFileName);
    }

    return configurationFile.toURI().toURL();
  }

  @Override
  public Path getLocalConfigurationPath()
  {
    if(configuration.containsKey(bundle.getName()+".config"))
      Paths.get(configuration.get(bundle.getName()+".config"));

    return SystemProperty.getLocalConfigurationPath(bundle.getName());
  }

  @Override
  public Path getLocalConfigurationPath(final String... subPath)
  {
    if(configuration.containsKey(bundle.getName()+".config"))
      return Paths.get(configuration.get(bundle.getName()+".config"), subPath);

    return Paths.get(SystemProperty.getLocalConfigurationPath(bundle.getName()).toString(), subPath);
  }

  @Override
  public Path getGlobalConfigurationPath()
  {
    if(configuration.containsKey(bundle.getName()+".config"))
      return Paths.get(configuration.get(bundle.getName()+".config"));

    return SystemProperty.getGlobalConfigurationPath(bundle.getName());
  }

  @Override
  public Path getGlobalConfigurationPath(final String... subPath)
  {
    if(configuration.containsKey(bundle.getName()+".config"))
      return Paths.get(configuration.get(bundle.getName()+".config"), subPath);

    return Paths.get(SystemProperty.getGlobalConfigurationPath(bundle.getName()).toString(), subPath);
  }

  @Override
  public String readConfigurationFileResource(final String resourceName,
    final boolean searchGlobal)
      throws IOException
  {
    final URL resource=getConfigurationFileResource(resourceName, searchGlobal, false);
    final FileIO fileIO=new FileIO(charset);

    return fileIO.readResource(resource);
  }

  @Override
  public void writeConfigurationFileResource(final String resourceName,
    final String content, final boolean createResource)
      throws IOException
  {
    URL resource;

    try
    {
      resource=getConfigurationFileResource(resourceName, false, false);
    }
    catch(final FileNotFoundException e)
    {
      if(!createResource)
        throw e;

      resource=getLocalConfigurationPath(resourceName).toUri().toURL();
    }

    final String resourceLocation=resource.toExternalForm();
    final File file=new File(resourceLocation);

    if(!file.exists())
      throw new UnsupportedOperationException("Writing to streams is not supported.");

    (new FileIO(charset)).writeFile(file, content, createResource);
  }
}
