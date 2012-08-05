package net.evalcode.services.manager.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.annotation.Configuration;
import net.evalcode.services.manager.annotation.Property;
import net.evalcode.services.manager.component.ComponentBundleInspector;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.misc.FileIO;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ComponentBundleInspectorImpl
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleInspectorImpl implements ComponentBundleInspector
{
  // PREDEFINED PROPERTIES
  private static final Logger LOG=LoggerFactory.getLogger(ComponentBundleInspectorImpl.class);


  // MEMBERS
  private final Bundle bundle;
  private final ComponentBundleManifest bundleManifest;
  private final Charset charset=SystemProperty.getCharset();


  // CONSTRUCTION
  ComponentBundleInspectorImpl(final Bundle bundle)
  {
    this.bundle=bundle;
    this.bundleManifest=new ComponentBundleManifest(bundle);
  }


  // ACCESSORS/MUTATORS
  Bundle getBundle()
  {
    return bundle;
  }

  ComponentBundleManifest getBundleManifest()
  {
    return bundleManifest;
  }

  Set<Class<?>> getExportedServiceComponents()
  {
    return getExportedAnnotatedClasses(Component.class);
  }

  Set<Class<?>> getExportedConfigurationEntities()
  {
    return getExportedAnnotatedClasses(Configuration.class);
  }

  Set<Class<?>> getExportedAnnotatedClasses(final Class<? extends Annotation> annotation)
  {
    final Set<Class<?>> exportedClasses=getExportedClasses();
    final Set<Class<?>> exportedAnnotatedClasses=new HashSet<>();

    for(final Class<?> exportedClazz : exportedClasses)
    {
      if(exportedClazz.isAnnotationPresent(annotation) &&
        !exportedAnnotatedClasses.contains(exportedClazz))
        exportedAnnotatedClasses.add(exportedClazz);
    }

    return exportedAnnotatedClasses;
  }

  Set<Class<?>> getExportedClasses()
  {
    final Set<Class<?>> classes=new HashSet<>();
    final Set<String> packageNames=getBundleManifest().getEntry(
      ComponentBundleManifest.Header.EXPORT_PACKAGE
    );

    for(final String packageName : packageNames)
    {
      final Enumeration<?> classPaths=bundle.findEntries("/", "*.class", true);

      if(null==classPaths)
        continue;

      while(classPaths.hasMoreElements())
      {
        final String name=classPathToName(packageName, classPaths.nextElement().toString());

        if(null==name)
          continue;

        Class<?> clazz;

        try
        {
          clazz=bundle.loadClass(name);
        }
        catch(final ClassNotFoundException e)
        {
          continue;
        }

        if(!classes.contains(clazz))
          classes.add(clazz);
      }
    }

    return classes;
  }

  void appendProperties(final Map<String, String> target, final File propertiesFile)
  {
    final Properties properties=new Properties();

    try
    {
      properties.load(new FileInputStream(propertiesFile));
    }
    catch(final FileNotFoundException e)
    {
      LOG.debug("Properties file not found [{}].", propertiesFile.getAbsolutePath());
    }
    catch(IOException e)
    {
      LOG.error("Unable to access properties file [{}].", propertiesFile.getAbsolutePath(), e);
    }

    appendProperties(target, properties);
  }

  void appendProperties(final Map<String, String> target, final Properties properties)
  {
    for(final Object key : properties.keySet())
    {
      if(target.containsKey(key))
        target.put((String)key, (String)properties.get(key));
    }
  }

  String getConfigurationFilePath(final String configurationFilePath,
    final String configurationFileName)
  {
    return configurationFilePath+
      File.separator+
      bundle.getSymbolicName()+
      File.separator+
      configurationFileName;
  }

  String classPathToName(final String packageName, final String classPath)
  {
    final String packagePath=packageNameToPath(packageName);

    if(!StringUtils.contains(classPath, packagePath))
      return null;

    final String relClassPath=StringUtils.substringAfterLast(
      classPath, packageNameToPath(packageName)
    );

    final String simpleClassName=StringUtils.replaceChars(relClassPath, "/$", "..");
    final String className=packageName.concat(StringUtils.remove(simpleClassName, ".class"));

    return className;
  }

  String packageNameToPath(final String packageName)
  {
    return StringUtils.replace(packageName, ".", "/");
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Map<String, String> getBundleProperties()
  {
    /**
     * TODO Cache
     */
    return getBundlePropertiesImpl();
  }

  @Override
  public Set<Class<?>> getExportedJpaEntities()
  {
    /**
     * TODO Cache
     */
    return getExportedAnnotatedClasses(Entity.class);
  }

  @Override
  public Set<Class<?>> getExportedJaxbEntities()
  {
    /**
     * TODO Cache
     */
    return new HashSet<Class<?>>() {{
        addAll(getExportedAnnotatedClasses(XmlType.class));
        addAll(getExportedAnnotatedClasses(XmlRootElement.class));
      }
      private static final long serialVersionUID=1L;
    };
  }

  @Override
  public String readConfigurationFile(final String configurationFileName,
    final boolean searchGlobal)
      throws FileNotFoundException
  {
    final File file=getConfigurationFile(configurationFileName, searchGlobal, false);
    final FileIO fileIO=new FileIO(charset);

    return fileIO.readFile(file);
  }

  @Override
  public void writeConfigurationFile(final String configurationFileName,
    final String content, final boolean createFile)
      throws IOException
  {
    File file;

    try
    {
      file=getConfigurationFile(configurationFileName, false, false);
    }
    catch(final FileNotFoundException e)
    {
      if(!createFile)
        throw e;

      file=new File(getLocalConfigurationFilePath(configurationFileName));
    }

    (new FileIO(charset)).writeFile(file, content, createFile);
  }

  @Override
  public File getConfigurationFile(final String configurationFileName,
    final boolean searchGlobal, final boolean searchBundleClasspath)
      throws FileNotFoundException
  {
    // try local configuration path
    File configurationFile=new File(getLocalConfigurationFilePath(configurationFileName));

    if(searchGlobal && !configurationFile.exists())
    {
      // try global configuration path
      configurationFile=new File(getGlobalConfigurationFilePath(configurationFileName));
    }

    if(searchBundleClasspath && !configurationFile.exists())
    {
      // try bundle resource path
      if(null!=bundle.getResource(configurationFileName))
        configurationFile=new File(bundle.getResource(configurationFileName).toExternalForm());
    }

    if(!configurationFile.exists())
      throw new FileNotFoundException(configurationFileName);

    return configurationFile;
  }

  @Override
  public String getGlobalConfigurationFilePath(final String configurationFileName)
  {
    return getConfigurationFilePath(SystemProperty.getGlobalConfigurationPath(),
      configurationFileName
    );
  }

  @Override
  public String getLocalConfigurationFilePath(final String configurationFileName)
  {
    return getConfigurationFilePath(SystemProperty.getLocalConfigurationPath(),
      configurationFileName
    );
  }

  @Override
  public String getPropertiesFileName()
  {
    return bundle.getSymbolicName()+".properties";
  }


  // IMPLEMENTATION
  /**
   * <p> Loads system properties in given priority (last overwrites first).
   *
   * <ul>
   *   <li>Declared properties: @Component(properties={})</li>
   *   <li>Global configuration: config/net.evalcode.services.bundle.properties</li>
   *   <li>Local configuration: config/environment/net.evalcode.services.bundle.properties</li>
   *   <li>Startup parameters: java -Dnet.evalcode.services.property=foo...</li>
   * </ul>
   */
  private Map<String, String> getBundlePropertiesImpl()
  {
    final Map<String, String> properties=new HashMap<>();

    // initialize with declared component properties
    for(final Class<?> component : getExportedServiceComponents())
    {
      for(final Property property : component.getAnnotation(Component.class).properties())
        properties.put(property.name(), property.defaultValue());
    }

    // overwrite from global configuration file
    appendProperties(properties,
      SystemProperty.getGlobalConfigurationFile(getPropertiesFileName())
    );

    // overwrite from local configuration file
    appendProperties(properties,
      SystemProperty.getLocalConfigurationFile(getPropertiesFileName())
    );

    // overwrite from startup parameters
    appendProperties(properties, System.getProperties());

    return properties;
  }
}
