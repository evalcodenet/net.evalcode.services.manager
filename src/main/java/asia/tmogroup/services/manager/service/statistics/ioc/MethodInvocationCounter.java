package net.evalcode.services.manager.service.statistics.ioc;


import javax.inject.Singleton;
import net.evalcode.services.manager.service.statistics.Count;
import net.evalcode.services.manager.service.statistics.Counter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * MethodInvocationCounter
 *
 * @see net.evalcode.services.manager.service.statistics.Count
 * @see net.evalcode.services.manager.service.statistics.Counter
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
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
