package net.evalcode.services.manager.internal.cache;


import javax.inject.Singleton;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * MethodInvocationCache
 *
 * @author evalcode.net
 */
@Singleton
public class MethodInvocationCache implements MethodInterceptor
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    return methodInvocation.proceed();
  }
}
