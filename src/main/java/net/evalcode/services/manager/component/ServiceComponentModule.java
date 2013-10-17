package net.evalcode.services.manager.component;


import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import net.evalcode.services.manager.annotation.Cache;
import net.evalcode.services.manager.configuration.ConfigurationEntityManager;
import net.evalcode.services.manager.configuration.ConfigurationEntityProvider;
import net.evalcode.services.manager.configuration.Environment;
import net.evalcode.services.manager.internal.cache.MethodInvocationCache;
import net.evalcode.services.manager.internal.persistence.EntityManagerFactoryProvider;
import net.evalcode.services.manager.internal.persistence.EntityManagerProvider;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.management.logging.Log;
import net.evalcode.services.manager.management.logging.impl.MethodInvocationLogger;
import net.evalcode.services.manager.management.statistics.Count;
import net.evalcode.services.manager.management.statistics.impl.MethodInvocationCounter;
import net.evalcode.services.manager.misc.FileIO;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;


/**
 * ServiceComponentModule
 *
 * @author carsten.schipke@gmail.com
 */
public class ServiceComponentModule extends AbstractModule
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(ServiceComponentModule.class);


  // MEMBERS
  private ComponentBundleInterface bundle;


  // ACCESSORS/MUTATORS
  public void setComponentBundle(final ComponentBundleInterface componentBundle)
  {
    this.bundle=componentBundle;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void configure()
  {
    final Configuration configuration=this.bundle.getConfiguration();
    final Set<String> configurationKeys=configuration.keySet();

    for(final String key : configurationKeys)
    {
      bind(String.class)
        .annotatedWith(Names.named(key))
        .toProvider(new Provider<String>() {
          @Override
          public String get()
          {
            return configuration.get(key);
          }
        });
    }

    bind(Environment.class)
      .toInstance(SystemProperty.getEnvironment());

    bind(Locale.class)
      .annotatedWith(Names.named("net.evalcode.services.locale"))
      .toInstance(SystemProperty.getLocale());

    bind(Charset.class)
      .annotatedWith(Names.named("net.evalcode.services.charset"))
      .toInstance(SystemProperty.getCharset());

    bind(TimeZone.class)
      .annotatedWith(Names.named("net.evalcode.services.timezone"))
      .toInstance(SystemProperty.getTimeZone());

    bind(FileIO.class)
      .in(Singleton.class);

    for(final Class<?> configurationEntityClazz : this.bundle.getConfigurationEntities())
    {
      final ConfigurationEntityManager configurationEntityManager=new ConfigurationEntityManager(
        binder().getProvider(Injector.class),
        provideObjectMapper(),
        bundle,
        configurationEntityClazz
      );

      bind(ConfigurationEntityManager.class)
        .annotatedWith(Names.named(configurationEntityClazz.getName()))
        .toInstance(configurationEntityManager);

      bind(configurationEntityClazz)
        .toProvider(new ConfigurationEntityProvider(configurationEntityManager));
    }

    bind(ComponentBundleInterface.class)
      .toInstance(this.bundle);

    bind(BundleContext.class)
      .toInstance(this.bundle.getBundleContext());

    for(final ServiceComponentInterface serviceComponent : this.bundle.getServiceComponents())
    {
      bind(ServiceComponentInterface.class)
        .annotatedWith(Names.named(serviceComponent.getName()))
        .toInstance(serviceComponent);
    }

    bind(EntityManagerFactory.class)
      .toProvider(EntityManagerFactoryProvider.class)
      .in(Singleton.class);

    bind(EntityManager.class)
      .toProvider(EntityManagerProvider.class);

    bindInterceptor(Matchers.any(),
      Matchers.annotatedWith(Log.class), new MethodInvocationLogger());

    bindInterceptor(Matchers.any(),
      Matchers.annotatedWith(Count.class), new MethodInvocationCounter());

    bindInterceptor(Matchers.any(),
      Matchers.annotatedWith(Cache.class), new MethodInvocationCache());
  }

  @Provides
  @SuppressWarnings("deprecation")
  ObjectMapper provideObjectMapper()
  {
    final ObjectMapper objectMapper=new ObjectMapper();

    objectMapper.setAnnotationIntrospector(AnnotationIntrospector.pair(
      new JaxbAnnotationIntrospector(), new JacksonAnnotationIntrospector()
    ));

    return objectMapper;
  }
}
