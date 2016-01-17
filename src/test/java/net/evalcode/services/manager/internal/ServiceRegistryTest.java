package net.evalcode.services.manager.internal;


import java.util.Collections;
import java.util.Set;
import net.evalcode.services.manager.internal.ServiceComponentInstance;
import net.evalcode.services.manager.internal.ServiceRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.ServiceRegistration;


/**
 * Test {@link ServiceRegistry}
 *
 * @author carsten.schipke@gmail.com
 */
public class ServiceRegistryTest
{
  // PREDEFINED PROPERTIES
  static ServiceRegistry serviceRegistry;


  // SETUP
  @BeforeClass
  public static void setUp()
  {
    serviceRegistry=new ServiceRegistry();
  }


  // TESTS
  @Test
  public void testNotifyServiceProviders()
  {
    final ServiceComponentInstance serviceComponent=Mockito.mock(ServiceComponentInstance.class);
    final Set<ServiceRegistration> serviceRegistrations=Collections.emptySet();

    serviceRegistry.registerServiceProvider(serviceComponent, serviceRegistrations);
    Mockito.verify(serviceComponent, Mockito.atLeast(1)).updateServiceProviderBindings();

    serviceRegistry.removeServiceProvider(serviceComponent);
    Mockito.verify(serviceComponent, Mockito.atLeast(2)).updateServiceProviderBindings();
  }
}
