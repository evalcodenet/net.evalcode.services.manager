package net.evalcode.services.manager.internal;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.evalcode.services.manager.component.ComponentBundleInspector;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Configuration;
import net.evalcode.services.manager.component.annotation.Property;
import net.evalcode.services.manager.internal.util.SystemProperty;
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
  static final Logger LOG=LoggerFactory.getLogger(ComponentBundleInspectorImpl.class);


  // MEMBERS
  final Bundle bundle;
  final ComponentBundleManifest bundleManifest;


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

  void appendProperties(final Map<String, String> target, final URL resource)
  {
    final Properties properties=new Properties();

    try
    {
      properties.load(resource.openStream());
    }
    catch(final IOException e)
    {
      LOG.warn("Unable to resolve properties for resource[{}].", resource.toExternalForm());
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
  public Map<String, String> getBundleProperties()
  {
    /**
     * TODO Cache
     */
    return getBundlePropertiesImpl();
  }

  @Override
  public String getBundlePropertiesFileName()
  {
    return bundle.getSymbolicName()+".properties";
  }

  @Override
  public URL searchResourceInBundleClassPath(final String resourceFileName)
  {
    final URL resource=bundle.getResource(resourceFileName);

    if(null!=resource)
      return resource;

    @SuppressWarnings("unchecked")
    final Enumeration<URL> resources=bundle.findEntries("/", resourceFileName, true);

    while(resources.hasMoreElements())
      return resources.nextElement();

    return null;
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
    try
    {
      appendProperties(properties,
        SystemProperty.getGlobalConfigurationPath(getBundlePropertiesFileName()).toUri().toURL()
      );
    }
    catch(final MalformedURLException e)
    {
      LOG.error(e.getMessage(), e);
    }

    // overwrite from local configuration file
    try
    {
      appendProperties(properties,
        SystemProperty.getLocalConfigurationPath(getBundlePropertiesFileName()).toUri().toURL()
      );
    }
    catch(final MalformedURLException e)
    {
      LOG.error(e.getMessage(), e);
    }

    // overwrite from startup parameters
    appendProperties(properties, System.getProperties());

    return properties;
  }
}
