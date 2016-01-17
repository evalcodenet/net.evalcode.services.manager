package net.evalcode.services.manager.service.jmx;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import net.evalcode.services.manager.component.annotation.Bind;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Unbind;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.logging.Log;
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
  static final Logger LOG=LoggerFactory.getLogger(ServiceMBeanRegistry.class);


  // MEMBERS
  final MBeanServer mBeanServer;
  final String mBeanRootNode;


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


  // IMPLEMENTATION
  ObjectName getObjectName(final ServiceMBean mBean)
    throws MalformedObjectNameException
  {
    return new ObjectName(String.format("%1$s:type=%2$s", mBeanRootNode, mBean.getName()));
  }
}
