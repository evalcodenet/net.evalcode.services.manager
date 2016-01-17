package net.evalcode.services.manager.internal;


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
  void handleEvent(ComponentBundleTracker bundleTracker, Bundle bundle, BundleEvent bundleEvent);
}
