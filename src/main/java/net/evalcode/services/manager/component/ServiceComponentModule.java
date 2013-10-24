package net.evalcode.services.manager.component;


import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import net.evalcode.services.manager.component.configuration.ConfigurationEntityManager;
import net.evalcode.services.manager.component.configuration.ConfigurationEntityProvider;
import net.evalcode.services.manager.internal.persistence.EntityManagerFactoryProvider;
import net.evalcode.services.manager.internal.persistence.EntityManagerProvider;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.service.cache.Cache;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.ioc.MethodInvocationCache;
import net.evalcode.services.manager.service.logging.Log;
import net.evalcode.services.manager.service.logging.ioc.MethodInvocationLogger;
import net.evalcode.services.manager.service.statistics.Count;
import net.evalcode.services.manager.service.statistics.ioc.MethodInvocationCounter;
import net.evalcode.services.manager.util.io.FileIO;
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
  ComponentBundleInterface bundle;


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
    configureCommon();

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
  }

  protected void configureCommon()
  {
    bind(Environment.class)
      .toInstance(Environment.current());

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

    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Log.class),
      new MethodInvocationLogger());

    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Count.class),
      new MethodInvocationCounter());

    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cache.class),
      new MethodInvocationCache(getProvider(Injector.class)));
  }

  @Provides
  @Singleton
  CacheServiceRegistry provideCacheServiceRegistry()
  {
    return CacheServiceRegistry.get();
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
