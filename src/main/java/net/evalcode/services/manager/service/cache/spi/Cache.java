package net.evalcode.services.manager.service.cache.spi;


/**
 * Cache
 *
 * @author carsten.schipke@gmail.com
 */
public interface Cache<T>
{
  // ACCESSORS/MUTATORS
  String getName();

  boolean contains(Object key);

  Object get(Object key);
  Object put(Object key, Object value);

  void remove(Object key);
  void removeAll();
}
