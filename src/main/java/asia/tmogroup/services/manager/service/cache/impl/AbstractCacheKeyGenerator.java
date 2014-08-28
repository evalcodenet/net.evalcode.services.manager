package net.evalcode.services.manager.service.cache.impl;


import net.evalcode.services.manager.service.cache.annotation.Key;
import net.evalcode.services.manager.service.cache.spi.internal.CacheKeyGenerator;


/**
 * AbstractCacheKeyGenerator
 *
 * @author carsten.schipke@gmail.com
 */
public abstract class AbstractCacheKeyGenerator implements CacheKeyGenerator
{
  // OVERRIDES/IMPLEMENTS
  @Override
  public abstract Object createKey(final Key key);
}
