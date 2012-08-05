package net.evalcode.services.manager.internal.interceptor;


import net.evalcode.services.manager.management.statistics.Count;
import net.evalcode.services.manager.management.statistics.Counter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * MethodInvocationCounter
 *
 * @see net.evalcode.services.manager.management.statistics.Count
 * @see net.evalcode.services.manager.management.statistics.Counter
 *
 * @author carsten.schipke@gmail.com
 */
public class MethodInvocationCounter implements MethodInterceptor
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    final Count count=methodInvocation.getMethod().getAnnotation(Count.class);

    String name=count.value();
    if(null==name || name.isEmpty())
    {
      name=methodInvocation.getThis().getClass().getSimpleName()+"#"+
        methodInvocation.getMethod().getName();
    }

    Counter.increment(name);

    return methodInvocation.proceed();
  }
}
