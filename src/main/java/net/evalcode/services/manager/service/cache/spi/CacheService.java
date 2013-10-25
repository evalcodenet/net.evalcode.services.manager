package net.evalcode.services.manager.service.cache.spi;


import net.evalcode.services.manager.component.annotation.Service;


/**
 * CacheService
 *
 * @author carsten.schipke@gmail.com
 */
@Service
public interface CacheService<T>
{
  // ACCESSORS/MUTATORS
  Cache<T> cache(String regionName);
  Cache<T> cache(String regionName, String defaultConfig);

  boolean cacheExists(String regionName);

  int vote(String regionName);
}
