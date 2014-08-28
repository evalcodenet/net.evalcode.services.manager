package net.evalcode.services.manager.internal;


import static org.junit.Assert.assertEquals;
import net.evalcode.services.manager.internal.ComponentBundleInspectorImpl;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;


/**
 * Test {@link ComponentBundleInspectorImpl}
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleInspectorTest
{
  // TESTS
  @Test
  public void testGetBundle()
  {
    final long bundleId=1L;
    final String bundleSymbolicName="net.evalcode.services.component";

    final Bundle bundle=Mockito.mock(Bundle.class);

    Mockito.when(bundle.getBundleId()).thenReturn(bundleId);
    Mockito.when(bundle.getSymbolicName()).thenReturn(bundleSymbolicName);

    final ComponentBundleInspectorImpl componentBundleInspector=new ComponentBundleInspectorImpl(bundle);

    assertEquals(bundleId, componentBundleInspector.getBundle().getBundleId());
    assertEquals(bundleSymbolicName, componentBundleInspector.getBundle().getSymbolicName());
  }

  @Test
  public void testPackageNameToPath()
  {
    final Bundle bundle=Mockito.mock(Bundle.class);
    final ComponentBundleInspectorImpl componentBundleInspector=new ComponentBundleInspectorImpl(bundle);

    assertEquals("asia/tmogroup/services/component",
      componentBundleInspector.packageNameToPath("net.evalcode.services.component")
    );

    assertEquals("asia/tmogroup/services/component/entity",
      componentBundleInspector.packageNameToPath("net.evalcode.services.component.entity")
    );

    assertEquals("asia/tmogroup/services/component/internal",
      componentBundleInspector.packageNameToPath("net.evalcode.services.component.internal")
    );

    assertEquals("asia/tmogroup/services/component/service",
      componentBundleInspector.packageNameToPath("net.evalcode.services.component.service")
    );
  }

  @Test
  public void testClassPathToName()
  {
    final Bundle bundle=Mockito.mock(Bundle.class);
    final ComponentBundleInspectorImpl componentBundleInspector=new ComponentBundleInspectorImpl(bundle);

    assertEquals("net.evalcode.services.component.Component",
      componentBundleInspector.classPathToName("net.evalcode.services.component",
        "asia/tmogroup/services/component/Component.class"
    ));

    assertEquals("net.evalcode.services.component.internal.ComponentImpl",
      componentBundleInspector.classPathToName("net.evalcode.services.component.internal",
        "asia/tmogroup/services/component/internal/ComponentImpl.class"
    ));

    assertEquals("net.evalcode.services.component.internal.ComponentImpl.Foo",
      componentBundleInspector.classPathToName("net.evalcode.services.component.internal",
        "asia/tmogroup/services/component/internal/ComponentImpl$Foo.class"
    ));

    assertEquals("net.evalcode.services.component.internal.ComponentImpl.Foo.1",
      componentBundleInspector.classPathToName("net.evalcode.services.component.internal",
        "asia/tmogroup/services/component/internal/ComponentImpl$Foo$1.class"
    ));
  }
}
