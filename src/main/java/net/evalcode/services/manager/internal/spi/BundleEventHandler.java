package net.evalcode.services.manager.internal.spi;


import net.evalcode.services.manager.internal.ComponentBundleTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;


/**
 * BundleEventHandler
 *
 * @author carsten.schipke@gmail.com
 */
public interface BundleEventHandler
{
  // ACCESSORS/MUTATORS
  void handleEvent(final ComponentBundleTracker bundleTracker,
    final Bundle bundle, final BundleEvent bundleEvent);
}
