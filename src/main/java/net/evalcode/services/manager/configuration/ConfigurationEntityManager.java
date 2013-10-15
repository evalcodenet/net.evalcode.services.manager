package net.evalcode.services.manager.configuration;


import java.io.IOException;
import java.net.URL;
import net.evalcode.services.manager.annotation.Configuration;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.misc.FileIO;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.google.inject.Provider;


/**
 * ConfigurationEntityProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class ConfigurationEntityManager<T>
{
  // PREDEFINED PROPERTIES
  private static final Logger LOG=LoggerFactory.getLogger(ConfigurationEntityManager.class);


  // MEMBERS
  private final Class<T> clazz;
  private final Class<? extends T> fallback;
  private final Configuration configuration;
  private final ComponentBundleInterface bundle;
  private final ObjectMapper objectMapper;
  private final Provider<Injector> providerInjector;


  // CONSTRUCTION
  @SuppressWarnings("unchecked")
  public ConfigurationEntityManager(final Provider<Injector> providerInjector,
    final ObjectMapper objectMapper, final ComponentBundleInterface bundle, final Class<T> clazz)
  {
    this.providerInjector=providerInjector;
    this.objectMapper=objectMapper;

    this.bundle=bundle;

    this.clazz=clazz;
    this.configuration=clazz.getAnnotation(Configuration.class);

    if(!Void.class.equals(configuration.fallback()) &&
      clazz.isAssignableFrom(configuration.fallback()))
    {
      fallback=(Class<? extends T>)configuration.fallback();
    }
    else
    {
      fallback=null;

      if(!Void.class.equals(configuration.fallback()))
      {
        throw new RuntimeException(String.format(
          "Configuration entity fallback must be a sub-type of its "+
          "configuration entity [entity: %1$s, fallback: %2$s].",
            clazz, configuration.fallback()
        ));
      }
    }
  }


  // ACCESSORS/MUTATORS
  public T get()
  {
    LOG.debug("Invoke {}.", this);

    URL configurationResource=null;

    try
    {
      configurationResource=getResource();
    }
    catch(final IOException e)
    {
      LOG.warn("Configuration entity file not found [entity: {}, file: {}].",
        new Object[] {clazz, configuration.value(), e}
      );

      if(null!=fallback)
      {
        LOG.debug("Trying fallback [entity: {}, fallback: {}].", clazz, fallback);

        return getFallback();
      }
    }

    final FileIO fileIo=providerInjector.get().getInstance(FileIO.class);
    String configurationFileContent=fileIo.readResource(configurationResource);

    if(null!=configurationFileContent)
    {
      for(final String key : bundle.getConfiguration().keySet())
      {
        final String value=JSONObject.quote(bundle.getConfiguration().get(key));

        configurationFileContent=StringUtils.replace(
          configurationFileContent, "${"+key+"}", value.substring(1, value.length()-1)
        );
      }
    }

    try
    {
      final T configuration=objectMapper.readValue(configurationFileContent, clazz);

      providerInjector.get().injectMembers(configuration);

      return configuration;
    }
    catch(final IOException e)
    {
      LOG.warn("Invalid configuration entity file [entity: {}, file: {}].",
        new Object[] {clazz, configurationResource, e}
      );

      if(null!=fallback)
      {
        LOG.debug("Trying fallback [entity: {}, fallback: {}].", clazz, fallback);

        return getFallback();
      }
    }

    return null;
  }

  public void set(final T entity, final boolean createResource) throws IOException
  {
    bundle.getConfiguration().writeConfigurationFileResource(
      configuration.value(), objectMapper.writeValueAsString(entity), createResource
    );
  }

  public URL getResource() throws IOException
  {
    return bundle.getConfiguration().getConfigurationFileResource(
      configuration.value(), true, true
    );
  }

  public T getFallback()
  {
    try
    {
      final T instance=fallback.newInstance();

      providerInjector.get().injectMembers(instance);

      return instance;
    }
    catch(final InstantiationException | IllegalAccessException e)
    {
      throw new RuntimeException(String.format(
        "Failed to instantiate configuration entity fallback [entity: %1$s, fallback: %2$s].",
          clazz,
          fallback
      ));
    }
  }
}
