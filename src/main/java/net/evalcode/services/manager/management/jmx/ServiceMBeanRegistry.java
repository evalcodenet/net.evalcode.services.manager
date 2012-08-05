package net.evalcode.services.manager.management.jmx;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import net.evalcode.services.manager.annotation.Bind;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.annotation.Unbind;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.management.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ServiceMBeanRegistry
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class ServiceMBeanRegistry
{
  // PREDEFINED PROPERTIES
  private static final Logger LOG=LoggerFactory.getLogger(ServiceMBeanRegistry.class);


  // MEMBERS
  private final MBeanServer mBeanServer;
  private final String mBeanRootNode;


  // CONSTRUCTION
  @Inject
  public ServiceMBeanRegistry(final MBeanServer mBeanServer,
    @Named("net.evalcode.services.jmx.node") final String mBeanRootNode)
  {
    super();

    this.mBeanServer=mBeanServer;
    this.mBeanRootNode=mBeanRootNode;
  }


  // ACCESSORS/MUTATORS
  @Log
  @Bind
  public void bind(final ServiceMBean mBean)
  {
    try
    {
      mBeanServer.registerMBean(mBean, getObjectName(mBean));
    }
    catch(final JMException e)
    {
      LOG.error(e.getMessage(), e);
    }
  }

  @Log
  @Unbind
  public void unbind(final ServiceMBean mBean)
  {
    try
    {
      mBeanServer.unregisterMBean(getObjectName(mBean));
    }
    catch(final JMException e)
    {
      LOG.error(e.getMessage(), e);
    }
  }

  public ObjectName getObjectName(final ServiceMBean mBean)
    throws MalformedObjectNameException
  {
    return new ObjectName(String.format("%1$s:type=%2$s", mBeanRootNode, mBean.getName()));
  }
}
