package net.evalcode.services.manager.service.cache.impl;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;
import net.evalcode.services.manager.service.cache.spi.Cache;
import net.jcip.annotations.ThreadSafe;
import org.aopalliance.intercept.MethodInvocation;


/**
 * CollectionBacklogProvider
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@ThreadSafe
@SuppressWarnings({"rawtypes", "unchecked"})
public class CollectionBacklogProvider implements BacklogProvider
{
  // PREDEFINED PROPERTIES
  static final ConcurrentMap<String, CollectionBacklogMethod> METHODS=new ConcurrentHashMap<>();


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final Cache<?> cache, final Object cacheKey,
    final MethodInvocation methodInvocation)
      throws Throwable
  {
    final Object[] arguments=methodInvocation.getArguments();

    if(0==arguments.length || !(arguments[0] instanceof Collection))
    {
      throw new IllegalArgumentException(
        "Method must be invoked with exactly one parameter of type Collection."
      );
    }

    final Method method=methodInvocation.getMethod();
    final String methodId=method.toGenericString();

    final CollectionBacklogMethod populator=METHODS.get(methodId);

    if(null!=populator)
      return populator.invoke(methodInvocation, (Collection)arguments[0]);

    final CollectionBacklogMethod populatorNew=new CollectionBacklogMethod(cache, cacheKey, method);
    final CollectionBacklogMethod populatorExisting=METHODS.putIfAbsent(methodId, populatorNew);

    if(null==populatorExisting)
      return populatorNew.invoke(methodInvocation, (Collection)arguments[0]);

    return populatorExisting.invoke(methodInvocation, (Collection)arguments[0]);
  }


  /**
   * CollectionBacklogMethod
   *
   * @author carsten.schipke@gmail.com
   */
  static class CollectionBacklogMethod
  {
    // MEMBERS
    final ConcurrentMap<Integer, ValueFuture> backlog=new ConcurrentHashMap<>();

    // TODO Update external cache.
    final Cache<?> cache;
    final Object cacheKey;
    final Class<? extends Collection> returnType;


    // CONSTRUCTION
    CollectionBacklogMethod(final Cache<?> cache, final Object cacheKey, final Method method)
    {
      this.cache=cache;
      this.cacheKey=cacheKey;
      this.returnType=method.getAnnotation(CollectionBacklog.class).type();
    }


    // ACCESSORS
    Object invoke(final MethodInvocation methodInvocation, final Collection<Object> keys)
      throws Throwable
    {
      final Integer[] keysHashed=new Integer[keys.size()];
      int idx=0;

      Thread.currentThread().setName(methodInvocation.getMethod().toGenericString());
      final CountDownLatch methodInvoked=new CountDownLatch(1);

      final Set<Object> missing=new HashSet<>();

      for(final Object key : keys)
      {
        // TODO Respect @Key.Type
        final Integer hash=Integer.valueOf(key.hashCode());

        keysHashed[idx++]=hash;

        final Object value=backlog.get(hash);

        if(null==value)
        {
          if(null==backlog.putIfAbsent(hash, new ValueFuture(methodInvoked)))
            missing.add(key);
        }
      }

      final Collection<Object> arguments=(Collection<Object>)methodInvocation.getArguments()[0];
      arguments.retainAll(missing);

      if(0<arguments.size())
      {
        final Collection<Object> collection=(Collection<Object>)methodInvocation.proceed();

        // TODO Respect @Key.Type
        for(final Object object : collection)
          backlog.get(object.hashCode()).value(object);

        methodInvoked.countDown();
      }

      final Collection<Object> returnValueCollection=returnValueCollection();

      for(final Integer hash : keysHashed)
        returnValueCollection.add(backlog.get(hash).value());

      return returnValueCollection;
    }

    Collection<Object> returnValueCollection() throws InstantiationException, IllegalAccessException
    {
      return (Collection<Object>)returnType.newInstance();
    }


    /**
     * ValueFuture
     *
     * @author carsten.schipke@gmail.com
     */
    static class ValueFuture
    {
      // MEMBERS
      private final AtomicReference<Object> value=new AtomicReference<>(null);
      private final CountDownLatch methodInvoked;


      // CONSTRUCTION
      ValueFuture(final CountDownLatch methodInvoked)
      {
        this.methodInvoked=methodInvoked;
      }


      // ACCESSORS
      Object value()
      {
        try
        {
          methodInvoked.await();
        }
        catch(final InterruptedException e)
        {
          Thread.currentThread().interrupt();
        }

        return value.get();
      }

      void value(final Object value)
      {
        this.value.set(value);
      }
    }
  }
}
