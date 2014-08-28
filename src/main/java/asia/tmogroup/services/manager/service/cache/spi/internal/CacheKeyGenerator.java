package net.evalcode.services.manager.service.cache.spi.internal;


import net.evalcode.services.manager.service.cache.annotation.Key;


/**
 * CacheKeyGenerator
 *
 * @author carsten.schipke@gmail.com
 */
public interface CacheKeyGenerator
{
  // ACCESSORS/MUTATORS
  Object createKey(Key key);
}
