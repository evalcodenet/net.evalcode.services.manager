package net.evalcode.services.manager.service.cache.spi;


import org.aopalliance.intercept.MethodInvocation;


/**
 * BacklogProvider
 *
 * @author carsten.schipke@gmail.com
 */
public interface BacklogProvider
{
  // ACCCESSORS
  Object invoke(Cache<?> cache, Object cacheKey, MethodInvocation methodInvocation)
    throws Throwable;
}
