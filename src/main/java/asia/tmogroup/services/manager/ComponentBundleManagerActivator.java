package net.evalcode.services.manager;


import java.util.logging.Handler;
import java.util.logging.LogManager;
import javax.inject.Inject;
import net.evalcode.services.manager.component.Environment;
import net.evalcode.services.manager.internal.ComponentBundleManager;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.internal.ComponentBundleTracker;
import net.evalcode.services.manager.internal.StartingBundleEventHandler;
import net.evalcode.services.manager.internal.StoppingBundleEventHandler;
import net.evalcode.services.manager.internal.util.Messages;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.google.inject.Guice;


/**
 * ComponentBundleManagerActivator
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleManagerActivator implements BundleActivator
{
  // PREDEFINED PROPERTIES
  static final int MASK_TRACKED_BUNDLES=1023;
  static final Logger LOG=LoggerFactory.getLogger(ComponentBundleManagerActivator.class);


  // MEMBERS
  final ComponentBundleTracker componentBundleTracker=new ComponentBundleTracker();
  BundleTracker bundleTracker;

  @Inject
  ComponentBundleManager componentBundleManager;
  @Inject
  StoppingBundleEventHandler stoppingBundleEventHandler;
  @Inject
  StartingBundleEventHandler startingBundleEventHandler;


  // OVERRIDES/IMPLEMENTS
  @Override
  public void start(final BundleContext bundleContext)
  {
    final java.util.logging.Logger rootLogger=LogManager.getLogManager().getLogger("");
    final Handler[] handlers=rootLogger.getHandlers();
    for(int i=0; i<handlers.length; i++)
      rootLogger.removeHandler(handlers[i]);

    SLF4JBridgeHandler.install();

    LOG.debug(Messages.STARTING.get());

    Guice.createInjector(Environment.current().stage(),
      new ComponentBundleManagerModule()
    ).injectMembers(this);

    componentBundleTracker.addBundleEventHandler(BundleEvent.STARTED, startingBundleEventHandler);
    componentBundleTracker.addBundleEventHandler(BundleEvent.STOPPING, stoppingBundleEventHandler);

    bundleTracker=new BundleTracker(bundleContext, MASK_TRACKED_BUNDLES, componentBundleTracker);

    bundleTracker.open();
  }

  @Override
  public void stop(final BundleContext bundleContext)
  {
    LOG.debug(Messages.STOPPING.get());

    componentBundleManager.removeComponentBundles();

    bundleTracker.close();
  }
}
