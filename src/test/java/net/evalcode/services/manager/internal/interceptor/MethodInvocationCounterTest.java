package net.evalcode.services.manager.internal.interceptor;


import static org.junit.Assert.assertEquals;
import java.lang.reflect.Method;
import net.evalcode.services.manager.internal.interceptor.MethodInvocationCounter;
import net.evalcode.services.manager.management.statistics.Count;
import net.evalcode.services.manager.management.statistics.Counter;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Test {@link MethodInvocationCounter}
 *
 * @author carsten.schipke@gmail.com
 */
public class MethodInvocationCounterTest
{
  // PREDEFINED PROPERTIES
  static MethodInvocationCounter methodInvocationCounter;


  // SETUP
  @BeforeClass
  public static void setUp()
  {
    methodInvocationCounter=new MethodInvocationCounter();
  }


  // TESTS
  @Test
  public void testGetBundle() throws Throwable
  {
    final Method method=MethodInvocationCounterTestClazz.class.getMethod("foo", new Class<?>[] {});
    final MethodInvocation methodInvocation=Mockito.mock(MethodInvocation.class);

    Mockito.when(methodInvocation.getMethod()).thenReturn(method);

    methodInvocationCounter.invoke(methodInvocation);

    assertEquals(1L, Counter.get("foo"));
  }


  /**
   * MethodInvocationCounterTestClazz
   *
   * @author carsten.schipke@gmail.com
   */
  static class MethodInvocationCounterTestClazz
  {
    @Count("foo")
    public void foo()
    {

    }
  }
}
