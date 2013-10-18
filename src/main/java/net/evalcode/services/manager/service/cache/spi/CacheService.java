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
  boolean hasCache(String name);
  Cache<T> getCache(String name);
}
