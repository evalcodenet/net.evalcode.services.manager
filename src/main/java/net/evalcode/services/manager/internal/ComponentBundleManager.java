package net.evalcode.services.manager.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import org.osgi.framework.Bundle;
import com.google.inject.Injector;


/**
 * ComponentBundleManager
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleManager
{
  // PREDEFINED PROPERTIES
  public static final String BUNDLE_NAME_OSGI="org.eclipse.osgi";
  public static final String BUNDLE_NAME_MANAGER="net.evalcode.services.manager";


  // MEMBERS
  private final ConcurrentMap<String, ComponentBundleInstance> componentBundles=
    new ConcurrentHashMap<String, ComponentBundleInstance>();

  @Inject
  private Injector injector;
  @Inject
  private ServiceRegistry serviceRegistry;


  // ACCESSORS/MUTATORS
  public ComponentBundleInstance getManagerBundle()
  {
    return componentBundles.get(BUNDLE_NAME_MANAGER);
  }

  public ComponentBundleInstance addComponentBundle(final Bundle bundle)
  {
    boolean isManagerBundle=false;
    if(BUNDLE_NAME_MANAGER.equals(bundle.getSymbolicName()))
      isManagerBundle=true;

    synchronized(this)
    {
      if(!componentBundles.containsKey(bundle.getSymbolicName()))
      {
        if(isManagerBundle)
        {
          componentBundles.put(bundle.getSymbolicName(),
            new ManagerBundleInstance(this, bundle, injector)
          );
        }
        else
        {
          componentBundles.put(bundle.getSymbolicName(),
            new ComponentBundleInstance(this, bundle)
          );
        }
      }
    }

    return componentBundles.get(bundle.getSymbolicName());
  }

  public Collection<ComponentBundleInstance> getComponentBundles()
  {
    return Collections.unmodifiableCollection(componentBundles.values());
  }

  public Set<String> getComponentBundleNames()
  {
    final Set<String> componentBundleNames=new HashSet<>();

    for(final ComponentBundleInstance componentBundle : componentBundles.values())
      componentBundleNames.add(componentBundle.getBundle().getSymbolicName());

    return componentBundleNames;
  }

  public void removeComponentBundle(final Bundle bundle)
  {
    final ComponentBundleInstance componentBundleInstance=
      componentBundles.remove(bundle.getSymbolicName());

    if(null!=componentBundleInstance)
      componentBundleInstance.deactivateServiceComponents();
  }

  public void removeComponentBundles()
  {
    for(final ComponentBundleInstance componentBundle : componentBundles.values())
      removeComponentBundle(componentBundle.getBundle());
  }

  ServiceRegistry getServiceRegistry()
  {
    return serviceRegistry;
  }
}
