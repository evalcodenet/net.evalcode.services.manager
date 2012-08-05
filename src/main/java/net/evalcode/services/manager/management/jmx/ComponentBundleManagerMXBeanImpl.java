package net.evalcode.services.manager.management.jmx;


import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManager;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.management.logging.Log;


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
  private static final String NAME="net.evalcode.services.manager";


  // MEMBERS
  @Inject
  private ComponentBundleManager componentBundleManager;


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
