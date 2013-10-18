package net.evalcode.services.manager.internal;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.evalcode.services.manager.internal.util.Messages;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ServiceRegistry
 *
 * @author carsten.schipke@gmail.com
 */
class ServiceRegistry
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(ServiceRegistry.class);


  // MEMBERS
  final ConcurrentMap<ServiceComponentInstance, Set<ServiceRegistration>> providers=
    new ConcurrentHashMap<>();


  // ACCESSORS/MUTATORS
  Set<ServiceComponentInstance> getServiceProviders()
  {
    return providers.keySet();
  }

  Set<ServiceRegistration> getServiceRegistrations()
  {
    final Set<ServiceRegistration> serviceRegistrations=new HashSet<ServiceRegistration>();

    for(final Set<ServiceRegistration> componentServiceRegistrations : providers.values())
      serviceRegistrations.addAll(componentServiceRegistrations);

    return serviceRegistrations;
  }

  Set<ServiceRegistration> getServiceRegistrations(final ServiceComponentInstance provider)
  {
    if(!providers.containsKey(provider))
      return new HashSet<ServiceRegistration>();

    return providers.get(provider);
  }

  void registerServiceProvider(final ServiceComponentInstance serviceProvider,
    final Set<ServiceRegistration> serviceRegistrations)
  {
    providers.putIfAbsent(serviceProvider, serviceRegistrations);

    notifyServiceProviders(serviceProvider);
  }

  void removeServiceProvider(final ServiceComponentInstance serviceProvider)
  {
    providers.remove(serviceProvider);

    notifyServiceProviders(serviceProvider);
  }

  void notifyServiceProviders(final ServiceComponentInstance changedByServiceProvider)
  {
    LOG.debug(Messages.SERVICE_REGISTRY_UPDATE.get());

    if(!providers.containsKey(changedByServiceProvider))
      changedByServiceProvider.updateServiceProviderBindings();

    for(final ServiceComponentInstance serviceProvider : providers.keySet())
      serviceProvider.updateServiceProviderBindings();
  }
}
