package net.evalcode.services.manager.service.jmx;


import java.util.Map;
import javax.management.MXBean;


/**
 * ComponentBundleStatisticsMXBean
 *
 * @author carsten.schipke@gmail.com
 */
@MXBean
public interface ComponentBundleStatisticsMXBean
{
  // ACCESSORS/MUTATORS
  Map<String, Long> getCounterStatistics();
}
