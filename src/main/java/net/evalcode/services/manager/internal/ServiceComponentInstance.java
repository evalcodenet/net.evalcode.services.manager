package net.evalcode.services.manager.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.component.ServiceComponentInterface;
import net.evalcode.services.manager.component.annotation.Activate;
import net.evalcode.services.manager.component.annotation.Deactivate;
import net.evalcode.services.manager.internal.util.Messages;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * ServiceComponentInstance
 *
 * @author carsten.schipke@gmail.com
 */
class ServiceComponentInstance implements ServiceComponentInterface, ServiceListener, Callable<Void>
{
  // PREDEFINED TASKS
  static enum Task
  {
    ACTIVATE_COMPONENT,
    DEACTIVATE_COMPONENT,
    UPDATE_SERVICE_BINDINGS
  }


  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(ServiceComponentInstance.class);


  // MEMBERS
  private final ComponentBundleInstance componentBundleInstance;

  final LinkedBlockingQueue<Task> tasks=new LinkedBlockingQueue<Task>();
  final ServiceComponentInspector inspector;
  final Class<?> clazz;

  final List<ServiceRegistration> serviceRegistrations=
    new ArrayList<ServiceRegistration>();
  final ConcurrentMap<ServiceReference, ServiceComponentInstance> boundServiceReferences=
    new ConcurrentHashMap<ServiceReference, ServiceComponentInstance>();
  final Set<ServiceComponentInstance> boundServiceProviders=
    new ConcurrentHashSet<ServiceComponentInstance>();


  // CONSTRUCTION
  ServiceComponentInstance(final ComponentBundleInstance componentBundleInstance,
    final Class<?> componentClazz)
  {
    this.clazz=componentClazz;
    this.componentBundleInstance=componentBundleInstance;

    inspector=new ServiceComponentInspector(clazz);
  }


  // ACCESSORS/MUTATORS
  void activate()
  {
    tasks.offer(Task.ACTIVATE_COMPONENT);
  }

  void deactivate()
  {
    tasks.offer(Task.DEACTIVATE_COMPONENT);
  }

  void updateServiceProviderBindings()
  {
    tasks.offer(Task.UPDATE_SERVICE_BINDINGS);
  }

  ComponentBundleManager getManager()
  {
    return componentBundleInstance.getManager();
  }

  ServiceComponentInspector getInspector()
  {
    return inspector;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getName()
  {
    return clazz.getName();
  }

  @Override
  public Class<?> getType()
  {
    return clazz;
  }

  @Override
  public Module getModule()
  {
    return componentBundleInstance.getComponentModule(this);
  }

  @Override
  public Injector getInjector()
  {
    return componentBundleInstance.getComponentInjector(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getInstance()
  {
    return (T)getInjector().getInstance(getType());
  }

  @Override
  public ComponentBundleInterface getComponentBundle()
  {
    return componentBundleInstance;
  }

  @Override
  public Void call() throws InterruptedException
  {
    for(;;)
    {
      if(Thread.currentThread().isInterrupted())
        break;

      final Task task=tasks.take();

      if(Task.ACTIVATE_COMPONENT.equals(task))
      {
        activateImpl();
      }
      else if(Task.DEACTIVATE_COMPONENT.equals(task))
      {
        deactivateImpl();

        return null;
      }
      else
      {
        final ServiceRegistry serviceRegistry=getManager().getServiceRegistry();
        final Set<ServiceComponentInstance> serviceProviders=
          serviceRegistry.getServiceProviders();

        for(final ServiceComponentInstance serviceProvider : serviceProviders)
        {
          for(final Class<?> requestedService : getRequestedServices())
          {
            if(requestedService.isInstance(serviceProvider.getInstance()) &&
              !boundServiceProviders.contains(serviceProvider))
              connectServiceProvider(serviceProvider);
          }
        }

        for(final ServiceComponentInstance boundServiceProvider : boundServiceProviders)
        {
          if(!serviceRegistry.getServiceProviders().contains(boundServiceProvider))
            disconnectServiceProvider(boundServiceProvider);
        }
      }
    }

    return null;
  }

  @Override
  public void serviceChanged(final ServiceEvent serviceEvent)
  {
    updateServiceProviderBindings();
  }


  // IMPLEMENTATION
  private void connectServiceProvider(final ServiceComponentInstance serviceProvider)
  {
    final Set<ServiceRegistration> serviceRegistrations=
      getManager().getServiceRegistry().getServiceRegistrations(serviceProvider);

    for(final ServiceRegistration serviceRegistration : serviceRegistrations)
    {
      for(final Class<?> requestedService : getRequestedServices())
      {
        /**
         * TODO implement dynamic proxy
         * Combine interface <requestedService> + ServiceComponentInterface
         */
        if(requestedService.isInstance(serviceProvider.getInstance()))
        {
          final Object service=getComponentBundle().getBundleContext().getService(
            serviceRegistration.getReference()
          );

          if(!boundServiceProviders.contains(serviceProvider))
          {
            invokeMethod(getInspector().getServiceConnectors().get(requestedService),
              Arrays.asList(service).toArray()
            );

            boundServiceProviders.add(serviceProvider);
          }

          boundServiceReferences.put(serviceRegistration.getReference(), serviceProvider);
        }
      }
    }
  }

  private void disconnectServiceProvider(final ServiceComponentInstance serviceProvider)
  {
    for(final Class<?> requestedService : getRequestedServices())
    {
      if(requestedService.isInstance(serviceProvider.getInstance()))
      {
        invokeMethod(getInspector().getServiceDisconnectors().get(requestedService),
          Arrays.asList(serviceProvider.getInstance()).toArray());
      }
    }

    for(final ServiceReference serviceReference : boundServiceReferences.keySet())
    {
      if(boundServiceReferences.get(serviceReference).equals(serviceProvider))
      {
        boundServiceReferences.remove(serviceReference);

        getComponentBundle().getBundleContext().ungetService(serviceReference);
      }
    }

    boundServiceProviders.remove(serviceProvider);
  }

  private void activateImpl()
  {
    Thread.currentThread().setName(getType().getSimpleName());

    getComponentBundle().getBundleContext().addServiceListener(this);

    final Method methodActivate=getInspector().getAnnotatedMethod(Activate.class);

    if(null!=methodActivate)
      invokeMethod(methodActivate);

    registerServicesImpl();
  }

  private void deactivateImpl()
  {
    getComponentBundle().getBundleContext().removeServiceListener(this);

    unregisterServicesImpl();

    final Method methodDeactivate=getInspector().getAnnotatedMethod(Deactivate.class);

    if(null!=methodDeactivate)
      invokeMethod(methodDeactivate);
  }

  private void registerServicesImpl()
  {
    final Set<ServiceRegistration> serviceRegistrations=new HashSet<ServiceRegistration>();

    for(final Class<?> providedService : getInspector().getProvidedServices())
    {
      final ServiceRegistration serviceRegistration=
        getComponentBundle().getBundleContext().registerService(
          providedService.getName(), getInstance(), null
        );

      serviceRegistrations.add(serviceRegistration);

      LOG.debug(Messages.REGISTERED_SERVICE.get(),
        clazz.getName(), serviceRegistration.getReference()
      );
    }

    getManager().getServiceRegistry().registerServiceProvider(this, serviceRegistrations);
  }

  private void unregisterServicesImpl()
  {
    getManager().getServiceRegistry().removeServiceProvider(this);

    for(final ServiceRegistration serviceRegistration : serviceRegistrations)
    {
      LOG.debug(Messages.UNEGISTER_SERVICE.get(),
        clazz.getName(), serviceRegistration.getReference()
      );

      serviceRegistration.unregister();
    }

    serviceRegistrations.clear();
  }

  private void invokeMethod(final Method method)
  {
    try
    {
      method.invoke(getInstance());
    }
    catch(final IllegalArgumentException e)
    {
      LOG.error(Messages.UNABLE_TO_INVOKE_COMPONENT_METHOD.get(), e);
    }
    catch(final IllegalAccessException e)
    {
      LOG.error(Messages.UNABLE_TO_INVOKE_COMPONENT_METHOD.get(), e);
    }
    catch(final InvocationTargetException e)
    {
      LOG.error(Messages.UNABLE_TO_ACCESS_COMPONENT_METHOD.get(), e);
    }
  }

  private void invokeMethod(final Method method, final Object[] arguments)
  {
    try
    {
      method.invoke(getInstance(), arguments);
    }
    catch(final IllegalArgumentException e)
    {
      LOG.error(Messages.UNABLE_TO_INVOKE_COMPONENT_METHOD.get(), e);
    }
    catch(final IllegalAccessException e)
    {
      LOG.error(Messages.UNABLE_TO_ACCESS_COMPONENT_METHOD.get(), e);
    }
    catch(final InvocationTargetException e)
    {
      LOG.error(Messages.UNABLE_TO_INVOKE_COMPONENT_METHOD.get(), e);
    }
  }

  private Set<Class<?>> getRequestedServices()
  {
    return getInspector().getServiceConnectors().keySet();
  }
}
