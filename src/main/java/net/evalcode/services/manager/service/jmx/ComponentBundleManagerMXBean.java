package net.evalcode.services.manager.service.jmx;


import java.util.Set;
import javax.management.MXBean;


/**
 * ComponentBundleManagerMXBean
 *
 * @author carsten.schipke@gmail.com
 */
@MXBean
public interface ComponentBundleManagerMXBean
{
  // ACCESSORS/MUTATORS
  Set<String> getComponentBundles();
}
