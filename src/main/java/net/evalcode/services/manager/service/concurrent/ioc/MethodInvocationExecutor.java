package net.evalcode.services.manager.service.concurrent.ioc;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.evalcode.services.manager.service.concurrent.MethodExecutor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MethodInvocationExecutor
 *
 * @author carsten.schipke@gmail.com
 */
// FIXME Define implementation via @Asynchronous#executor
// FIXME Configurable thread pool
// FIXME Manage invocations & return values
public class MethodInvocationExecutor implements MethodExecutor
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(MethodInvocationExecutor.class);


  // MEMBERS
  final ExecutorService executor=Executors.newCachedThreadPool();


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation)
    throws Throwable
  {
    executor.submit(new Callable<Void>() {
      @Override
      public Void call()
      {
        try
        {
          methodInvocation.proceed();
        }
        catch(final Throwable e)
        {
          LOG.error(e.getMessage(), e);
        }

        return null;
      }
    });

    return null;
  }
}
