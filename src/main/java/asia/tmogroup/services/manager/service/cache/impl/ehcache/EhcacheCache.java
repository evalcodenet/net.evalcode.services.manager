package net.evalcode.services.manager.service.cache.impl.ehcache;


import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.evalcode.services.manager.service.cache.spi.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


/**
 * EhcacheCache
 *
 * @author carsten.schipke@gmail.com
 */
// FIXME Hide implementation.
public class EhcacheCache implements Cache<Ehcache>
{
  // MEMBERS
  final Ehcache impl;


  // CONSTRUCTION
  public EhcacheCache(final Ehcache impl)
  {
    this.impl=impl;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public boolean contains(final Object key)
  {
    final Element value=impl.get(key);

    return null!=value;
  }

  @Override
  public Object get(final Object key)
  {
    final Element value=impl.get(key);

    if(null==value)
      return null;

    return value.getObjectValue();
  }

  @Override
  public Object getIfPresent(final Object key)
  {
    return get(key);
  }

  @Override
  public Object get(final Object key, final Callable<? extends Object> callable)
    throws ExecutionException
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }

  @Override
  public ImmutableMap<Object, Object> getAllPresent(final Iterable<?> iterable)
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }

  @Override
  public void put(final Object key, final Object value)
  {
    impl.put(new Element(key, value));
  }

  @Override
  public void putAll(final Map<? extends Object, ? extends Object> map)
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }

  @Override
  public void invalidate(final Object object)
  {
    impl.remove(object);
  }

  @Override
  public void invalidateAll(final Iterable<?> iterable)
  {
    impl.removeAll(Lists.newArrayList(iterable));
  }

  @Override
  public void invalidateAll()
  {
    impl.removeAll();
  }

  @Override
  public long size()
  {
    return Long.valueOf(impl.getSize());
  }

  @Override
  public CacheStats stats()
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }

  @Override
  public ConcurrentMap<Object, Object> asMap()
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanUp()
  {
    // TODO Implement.
    throw new UnsupportedOperationException();
  }
}
