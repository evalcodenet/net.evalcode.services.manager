package net.evalcode.services.manager.internal;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.evalcode.services.manager.internal.spi.BundleEventHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;


/**
 * ComponentBundleTracker
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleTracker implements BundleTrackerCustomizer
{
  // MEMBERS
  private final ConcurrentMap<Integer, BundleEventHandler> bundleEventHandlers=
    new ConcurrentHashMap<Integer, BundleEventHandler>();


  // ACCESSORS/MUTATORS
  public void addBundleEventHandler(final int bundleEventMask,
    final BundleEventHandler bundleEventHandler)
  {
    bundleEventHandlers.put(Integer.valueOf(bundleEventMask), bundleEventHandler);
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object addingBundle(final Bundle bundle, final BundleEvent bundleEvent)
  {
    return bundle;
  }

  @Override
  public void modifiedBundle(final Bundle bundle,
    final BundleEvent bundleEvent, final Object object)
  {
    if(ComponentBundleManager.BUNDLE_NAME_OSGI.equals(bundle.getSymbolicName()))
      return;

    for(final Integer bundleEventMask : bundleEventHandlers.keySet())
    {
      if(0<(bundleEvent.getType()&bundleEventMask.intValue()))
        bundleEventHandlers.get(bundleEventMask).handleEvent(this, bundle, bundleEvent);
    }
  }

  @Override
  public void removedBundle(final Bundle bundle, final BundleEvent bundleEvent, final Object object)
  {
    /**
     * Do nothing ...
     */
  }
}
