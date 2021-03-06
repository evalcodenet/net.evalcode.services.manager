package net.evalcode.services.manager.service.logging.ioc;


import javax.inject.Singleton;
import net.evalcode.services.manager.service.logging.Log;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.LoggerFactory;


/**
 * MethodInvocationLogger
 *
 * @see net.evalcode.services.manager.service.logging.Log
 * @see net.evalcode.services.manager.service.logging.Log.Level
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
public class MethodInvocationLogger implements MethodInterceptor
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    final Log log=methodInvocation.getMethod().getAnnotation(Log.class);
    final Log.Level level=log.level();
    final String pattern=log.pattern();

    Class<?> clazz=methodInvocation.getThis().getClass().getSuperclass();

    if(null==clazz)
      clazz=methodInvocation.getThis().getClass();

    if(Log.Level.INFO==level)
    {
      LoggerFactory.getLogger(clazz).info(pattern, new Object[] {
        clazz.getSimpleName()+"@"+Integer.toHexString(methodInvocation.getThis().hashCode()),
        methodInvocation.getMethod().getName(),
        methodInvocation.getArguments()
      });
    }
    else
    {
      LoggerFactory.getLogger(clazz).debug(pattern, new Object[] {
        clazz.getSimpleName()+"@"+Integer.toHexString(methodInvocation.getThis().hashCode()),
        methodInvocation.getMethod().getName(),
        methodInvocation.getArguments()
      });
    }

    return methodInvocation.proceed();
  }
}
