package net.evalcode.services.manager.management.jmx;


import java.util.Map;
import javax.inject.Singleton;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.management.statistics.Counter;


/**
 * ComponentBundleStatisticsMXBeanImpl
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class ComponentBundleStatisticsMXBeanImpl
  implements ServiceMBean, ComponentBundleStatisticsMXBean
{
  // PREDEFINED PROPERTIES
  private static final String NAME="net.evalcode.services.statistics";


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public Map<String, Long> getCounterStatistics()
  {
    return Counter.getAll();
  }
}
