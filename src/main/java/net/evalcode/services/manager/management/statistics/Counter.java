package net.evalcode.services.manager.management.statistics;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Counter
 *
 * @author carsten.schipke@gmail.com
 */
public final class Counter
{
  // MEMBERS
  private static final ConcurrentMap<String, AtomicLong> COUNTER=
    new ConcurrentHashMap<String, AtomicLong>();


  // CONSTRUCTION
  private Counter()
  {
    super();
  }


  // STATIC ACCESSORS
  public static long get(final String name)
  {
    COUNTER.putIfAbsent(name, new AtomicLong(0));

    return COUNTER.get(name).get();
  }

  @SuppressWarnings("boxing")
  public static Map<String, Long> getAll()
  {
    final Map<String, Long> counter=new HashMap<String, Long>();

    for(final String name : COUNTER.keySet())
      counter.put(name, COUNTER.get(name).longValue());

    return counter;
  }

  public static Set<String> getKeys()
  {
    return COUNTER.keySet();
  }

  public static long increment(final String name)
  {
    COUNTER.putIfAbsent(name, new AtomicLong(0));

    return COUNTER.get(name).incrementAndGet();
  }
}
