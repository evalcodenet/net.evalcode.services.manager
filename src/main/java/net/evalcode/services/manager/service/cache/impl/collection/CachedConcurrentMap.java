package net.evalcode.services.manager.service.cache.impl.collection;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.evalcode.services.manager.service.cache.spi.collection.MapValuePopulator;
import com.google.common.collect.ForwardingConcurrentMap;


/**
 * CachedConcurrentMap
 *
 * @author evalcode.net
 */
public class CachedConcurrentMap<K, V> extends ForwardingConcurrentMap<K, V>
{
  // MEMBERS
  private ConcurrentMap<K, V> map;
  private MapValuePopulator<K, V> populator;


  // CONSTRUCTION
  public CachedConcurrentMap(final MapValuePopulator<K, V> populator)
  {
    this(populator, new ConcurrentHashMap<K, V>());
  }

  public CachedConcurrentMap(final MapValuePopulator<K, V> populator, final ConcurrentMap<K, V> map)
  {
    this.map=map;
    this.populator=populator;
  }


  // ACCESSORS/MUTATORS
  MapValuePopulator<K, V> populator()
  {
    return populator;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  protected ConcurrentMap<K, V> delegate()
  {
    return map;
  }
}
