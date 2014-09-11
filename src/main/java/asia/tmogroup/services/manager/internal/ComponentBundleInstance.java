package net.evalcode.services.manager.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.persistence.EntityManagerFactory;
import net.evalcode.services.manager.component.ComponentBundleInspector;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.component.Configuration;
import net.evalcode.services.manager.component.ServiceComponentInterface;
import net.evalcode.services.manager.component.ServiceComponentModule;
import net.evalcode.services.manager.internal.persistence.PersistenceNamingStrategy;
import net.evalcode.services.manager.internal.persistence.PersistenceXml;
import net.evalcode.services.manager.internal.persistence.PersistenceXmlMetadata;
import net.evalcode.services.manager.internal.util.Messages;
import net.evalcode.services.manager.internal.util.SystemProperty;
import org.hibernate.ejb.Ejb3Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.evalcode.javax.xml.bind.MapperFactory;


/**
 * ComponentBundleInstance
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleInstance implements ComponentBundleInterface
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(ComponentBundleInstance.class);


  // MEMBERS
  final Bundle bundle;
  final ComponentBundleInspectorImpl inspector;
  final ComponentBundleManager componentBundleManager;
  final ComponentBundleConfiguration configuration;

  final Set<ServiceComponentInstance> componentInstances=new HashSet<>();

  final Map<Class<? extends Module>, Module> componentModules=new HashMap<>();
  final Map<Class<? extends Module>, Injector> componentInjectors=new HashMap<>();

  final ExecutorService componentExecutor=Executors.newCachedThreadPool();
  final List<Future<Void>> componentFutures=new ArrayList<>();

  EntityManagerFactory entityManagerFactory;
  Ejb3Configuration entityManagerConfiguration;


  // CONSTRUCTION
  ComponentBundleInstance(final ComponentBundleManager componentBundleManager, final Bundle bundle)
  {
    this.bundle=bundle;
    this.componentBundleManager=componentBundleManager;

    this.inspector=new ComponentBundleInspectorImpl(bundle);
    this.configuration=new ComponentBundleConfiguration(this);

    for(final Class<?> component : this.inspector.getExportedServiceComponents())
      addServiceComponent(component);
  }


  // ACCESSORS/MUTATORS
  String getName()
  {
    return bundle.getSymbolicName();
  }

  ComponentBundleManager getManager()
  {
    return componentBundleManager;
  }

  Module getComponentModule(final ServiceComponentInstance serviceComponent)
  {
    final Class<? extends Module> componentModuleClazz=
      serviceComponent.getInspector().getComponentModuleClass();

    synchronized(this)
    {
      if(!componentModules.containsKey(componentModuleClazz))
      {
        try
        {
          final Module componentModule=componentModuleClazz.newInstance();

          if(componentModule instanceof ServiceComponentModule)
            ((ServiceComponentModule)componentModule).setComponentBundle(this);

          componentModules.put(componentModuleClazz, componentModule);
        }
        catch(final InstantiationException e)
        {
          LOG.error(Messages.UNABLE_TO_INSTANTIATE_COMPONENT_MODULE.get(),
            new Object[] {serviceComponent.getName(), componentModuleClazz, e}
          );

          return null;
        }
        catch(final IllegalAccessException e)
        {
          LOG.error(Messages.UNABLE_TO_ACCESS_COMPONENT_MODULE.get(),
            new Object[] {serviceComponent.getName(), componentModuleClazz, e}
          );

          return null;
        }
      }
    }

    return componentModules.get(componentModuleClazz);
  }

  Injector getComponentInjector(final ServiceComponentInstance serviceComponent)
  {
    final Class<? extends Module> componentModuleClazz=
      serviceComponent.getInspector().getComponentModuleClass();

    synchronized(this)
    {
      if(!componentInjectors.containsKey(componentModuleClazz))
      {
        componentInjectors.put(componentModuleClazz,
          Guice.createInjector(getComponentModule(serviceComponent))
        );
      }
    }

    return componentInjectors.get(componentModuleClazz);
  }

  public void activateServiceComponents()
  {
    for(final ServiceComponentInstance component : componentInstances)
    {
      synchronized(this)
      {
        componentFutures.add(componentExecutor.submit(component));
      }

      component.activate();
    }
  }

  public void deactivateServiceComponents()
  {
    for(final ServiceComponentInstance component : componentInstances)
      component.deactivate();

    componentExecutor.shutdown();

    for(final Future<Void> componentFuture : componentFutures)
    {
      try
      {
        componentFuture.get();
      }
      catch(final InterruptedException e)
      {
        LOG.warn(e.getMessage(), e);

        Thread.currentThread().interrupt();
      }
      catch(final ExecutionException e)
      {
        LOG.error(e.getMessage(), e);
      }
    }

    componentFutures.clear();

    synchronized(this)
    {
      if(null!=entityManagerFactory && entityManagerFactory.isOpen())
        entityManagerFactory.close();
    }
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Bundle getBundle()
  {
    return bundle;
  }

  @Override
  public BundleContext getBundleContext()
  {
    return getBundle().getBundleContext();
  }

  @Override
  public ComponentBundleInspector getInspector()
  {
    return inspector;
  }

  @Override
  public Configuration getConfiguration()
  {
    return configuration;
  }

  @Override
  public Set<Class<?>> getConfigurationEntities()
  {
    return inspector.getExportedConfigurationEntities();
  }

  @Override
  public Set<ServiceComponentInterface> getServiceComponents()
  {
    final Set<ServiceComponentInterface> serviceComponentInterfaces=
      new HashSet<ServiceComponentInterface>();

    serviceComponentInterfaces.addAll(componentInstances);

    return serviceComponentInterfaces;
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory()
  {
    synchronized(this)
    {
      if(null==entityManagerFactory)
        entityManagerFactory=getEntityManagerConfiguration().buildEntityManagerFactory();

      return entityManagerFactory;
    }
  }

  @Override
  public Ejb3Configuration getEntityManagerConfiguration()
  {
    synchronized(this)
    {
      if(null==entityManagerConfiguration)
        entityManagerConfiguration=createEntityManagerConfiguration();

      return entityManagerConfiguration;
    }
  }


  // IMPLEMENTATION
  private void addServiceComponent(final Class<?> component)
  {
    componentInstances.add(new ServiceComponentInstance(this, component));
  }

  private Ejb3Configuration createEntityManagerConfiguration()
  {
    PersistenceXml persistenceXml=null;

    try
    {
      // TODO Bundle-scoped persistence.xml.
      persistenceXml=MapperFactory.create(MapperFactory.Impl.JAXB).unmarshal(
        PersistenceXml.class, SystemProperty.getConfigurationFilePath("persistence.xml").toFile()
      );
    }
    catch(final IOException e)
    {
      LOG.error(e.getMessage(), e);
    }

    final Ejb3Configuration entityManagerConfiguration=new Ejb3Configuration();
    entityManagerConfiguration.setNamingStrategy(new PersistenceNamingStrategy());
    entityManagerConfiguration.configure(new PersistenceXmlMetadata(persistenceXml), null);

    final Set<Class<?>> exportedEntities=inspector.getExportedJpaEntities();

    for(final Class<?> entity : exportedEntities)
      entityManagerConfiguration.addAnnotatedClass(entity);

    return entityManagerConfiguration;
  }
}
