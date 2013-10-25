package net.evalcode.services.manager.service.cache.spi.collection;


import java.util.Collection;


/**
 * MapValuePopulator
 *
 * @author evalcode.net
 */
public interface MapValuePopulator<K, V>
{
  Collection<V> values(Collection<K> keys);
}
