package net.evalcode.services.manager.internal;


import javax.inject.Inject;
import net.evalcode.services.manager.internal.spi.BundleEventHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;


/**
 * StoppingBundleEventHandler
 *
 * @author carsten.schipke@gmail.com
 */
public class StoppingBundleEventHandler implements BundleEventHandler
{
  // MEMBERS
  private final ComponentBundleManager componentBundleManager;


  // CONSTRUCTION
  @Inject
  public StoppingBundleEventHandler(final ComponentBundleManager componentBundleManager)
  {
    this.componentBundleManager=componentBundleManager;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public void handleEvent(final ComponentBundleTracker bundleTracker,
    final Bundle bundle, final BundleEvent bundleEvent)
  {
    componentBundleManager.removeComponentBundle(bundle);
  }
}
