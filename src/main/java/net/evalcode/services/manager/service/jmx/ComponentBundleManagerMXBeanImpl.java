package net.evalcode.services.manager.service.jmx;


import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManager;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.logging.Log;


/**
 * ComponentBundleManagerMXBeanImpl
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class ComponentBundleManagerMXBeanImpl implements ServiceMBean, ComponentBundleManagerMXBean
{
  // PREDEFINED PROPERTIES
  static final String NAME="net.evalcode.services.manager";


  // MEMBERS
  final ComponentBundleManager componentBundleManager;


  // CONSTRUCTION
  @Inject
  public ComponentBundleManagerMXBeanImpl(final ComponentBundleManager componentBundleManager)
  {
    super();

    this.componentBundleManager=componentBundleManager;
  }


  // OVERRIDES/IMPLEMENTS
  @Log
  @Override
  public String getName()
  {
    return NAME;
  }

  @Log
  @Override
  public Set<String> getComponentBundles()
  {
    return componentBundleManager.getComponentBundleNames();
  }
}
