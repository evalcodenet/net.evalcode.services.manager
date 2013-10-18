package net.evalcode.services.manager.service.jmx;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.evalcode.services.manager.service.jmx.ServiceMBean;
import net.evalcode.services.manager.service.jmx.ServiceMBeanRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Test {@link ServiceMBeanRegistry}
 *
 * @author carsten.schipke@gmail.com
 */
public class ServiceMBeanRegistryTest
{
  // PREDEFINED PROPERTIES
  static MBeanServer mbeanServer;
  static String mbeanRootNode;
  static ServiceMBeanRegistry mbeanRegistry;


  // SETUP
  @BeforeClass
  public static void setUp()
  {
    mbeanServer=Mockito.mock(MBeanServer.class);
    mbeanRootNode=ServiceMBeanRegistryTest.class.getPackage().getName();
    mbeanRegistry=new ServiceMBeanRegistry(mbeanServer, mbeanRootNode);
  }


  // TESTS
  @Test
  public void testGetObjectName() throws MalformedObjectNameException
  {
    final ServiceMBean serviceMBean=Mockito.mock(ServiceMBean.class);
    Mockito.when(serviceMBean.getName()).thenReturn(ServiceMBean.class.getSimpleName());

    final ObjectName mbeanObjectName=mbeanRegistry.getObjectName(serviceMBean);

    assertEquals(mbeanRootNode, mbeanObjectName.getDomain());
    assertTrue(-1<mbeanObjectName.getCanonicalName().indexOf(ServiceMBean.class.getSimpleName()));
  }

  @Test
  public void testBindServiceMBean() throws MalformedObjectNameException,
    InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
  {
    final ServiceMBean serviceMBean=Mockito.mock(ServiceMBean.class);
    Mockito.when(serviceMBean.getName()).thenReturn(ServiceMBean.class.getSimpleName());

    final ObjectName serviceMBeanObjectName=mbeanRegistry.getObjectName(serviceMBean);

    mbeanRegistry.bind(serviceMBean);

    Mockito.verify(mbeanServer, Mockito.atLeastOnce())
      .registerMBean(serviceMBean, serviceMBeanObjectName);
  }

  @Test
  public void testUnbindServiceMBean() throws MalformedObjectNameException,
    MBeanRegistrationException, InstanceNotFoundException
  {
    final ServiceMBean serviceMBean=Mockito.mock(ServiceMBean.class);
    Mockito.when(serviceMBean.getName()).thenReturn(ServiceMBean.class.getSimpleName());

    final ObjectName serviceMBeanObjectName=mbeanRegistry.getObjectName(serviceMBean);

    mbeanRegistry.unbind(serviceMBean);

    Mockito.verify(mbeanServer, Mockito.atLeastOnce())
      .unregisterMBean(serviceMBeanObjectName);
  }
}
