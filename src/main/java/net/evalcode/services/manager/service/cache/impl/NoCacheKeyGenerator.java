package net.evalcode.services.manager.service.cache.impl;


import net.evalcode.services.manager.service.cache.spi.CacheKeyGenerator;
import org.aopalliance.intercept.MethodInvocation;


/**
 * NoCacheKeyGenerator
 *
 * @author evalcode.net
 */
public class NoCacheKeyGenerator implements CacheKeyGenerator
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public String createKey(final MethodInvocation methodInvocation)
  {
    // Do nothing.

    return null;
  }
}
