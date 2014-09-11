package net.evalcode.services.manager.service.cache.spi;


/**
 * Cache
 *
 * @author carsten.schipke@gmail.com
 */
public interface Cache<T> extends com.google.common.cache.Cache<Object, Object>
{
  // ACCESSORS/MUTATORS
  boolean contains(Object key);

  Object get(Object key);
}
