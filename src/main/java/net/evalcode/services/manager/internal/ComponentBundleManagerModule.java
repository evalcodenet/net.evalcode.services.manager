package net.evalcode.services.manager.internal;


import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import net.evalcode.services.manager.component.ServiceComponentModule;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.service.jmx.ComponentBundleManagerMXBeanImpl;
import net.evalcode.services.manager.service.jmx.ServiceMBeanRegistry;
import com.google.inject.Provider;
import com.google.inject.name.Names;


/**
 * ComponentBundleManagerModule
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleManagerModule extends ServiceComponentModule
{
  // OVERRIDES/IMPLEMENTS
  @Override
  protected void configure()
  {
    configureCommon();

    final Set<String> configurationKeys=SystemProperty.keySet();

    for(final String key : configurationKeys)
    {
      bind(String.class)
        .annotatedWith(Names.named(key))
        .toProvider(new Provider<String>()
        {
          @Override
          public String get()
          {
            return SystemProperty.get(key);
          }
        });
    }

    bind(MBeanServer.class)
      .toInstance(ManagementFactory.getPlatformMBeanServer());

    bind(StartingBundleEventHandler.class)
      .in(Singleton.class);
    bind(StoppingBundleEventHandler.class)
      .in(Singleton.class);

    bind(ServiceRegistry.class)
      .in(Singleton.class);
    bind(ServiceMBeanRegistry.class)
      .in(Singleton.class);

    bind(ComponentBundleManager.class)
      .in(Singleton.class);
    bind(ComponentBundleManagerMXBeanImpl.class)
      .in(Singleton.class);
  }
}
