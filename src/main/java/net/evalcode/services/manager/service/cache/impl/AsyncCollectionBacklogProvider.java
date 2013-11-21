package net.evalcode.services.manager.service.cache.impl;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.annotation.Lifetime;
import net.evalcode.services.manager.service.cache.impl.CollectionBacklogProvider.CollectionBacklogMethod.Metadata;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Injector;


/**
 * AsyncCollectionBacklogProvider
 *
 * @author carsten.schipke@gmail.com
 */
// FIXME Support @KeySegment
// FIXME Wrong behavior with server runtime / runtime optimizations.
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class AsyncCollectionBacklogProvider implements BacklogProvider
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(AsyncCollectionBacklogProvider.class);
  static final ConcurrentMap<String, CollectionBacklogPopulator<Object>> METHODS=
    new ConcurrentHashMap<>();
  static final ExecutorService EXECUTOR=Executors.newCachedThreadPool();


  @Inject
  Injector injector;


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation)
    throws Throwable
  {
    final Object[] arguments=methodInvocation.getArguments();

    final Object instance=injector.getInstance(methodInvocation.getThis().getClass());
    final Method method=methodInvocation.getMethod();
    final Metadata metadata=Metadata.get(method);

    final Collection<Object> keys=(Collection)arguments[metadata.collectionKeysIdx()];

    final CollectionBacklogPopulator<Object> populator=METHODS.get(
      metadata.cacheKeyImpl(methodInvocation)
    );

    if(null==populator)
    {
      final CollectionBacklogPopulator<Object> populatorNew=
        new CollectionBacklogPopulator(instance, method);
      final CollectionBacklogPopulator<Object> populatorExisting=METHODS.putIfAbsent(
        metadata.cacheKeyImpl(methodInvocation), populatorNew
      );

      if(null==populatorExisting)
      {
        populatorNew.queue(keys);

        EXECUTOR.submit(populatorNew);

        return populatorNew.result(keys);
      }

      populatorExisting.queue(keys);

      return populatorExisting.result(keys);
    }

    populator.queue(keys);

    return populator.result(keys);
  }


  /**
   * CollectionBacklogPopulator
   *
   * @author carsten.schipke@gmail.com
   */
  static class CollectionBacklogPopulator<V> implements Callable<Void>
  {
    // MEMBERS
    final ConcurrentLinkedQueue<V> queue=new ConcurrentLinkedQueue<>();
    final ConcurrentMap<Integer, TaskFutureValue<V>> backlog;
    final Method method;
    final Object instance;
    final Class<? extends Collection<V>> collectionType;


    // CONSTRUCTION
    CollectionBacklogPopulator(final Object instance, final Method method)
    {
      this.method=method;
      this.instance=instance;

      collectionType=(Class<? extends Collection<V>>)
        method.getAnnotation(CollectionBacklog.Asynchronous.class).type();

      final Lifetime lifetime=Metadata.get(method).collectionLifetime();
      final com.google.common.cache.Cache<Integer, TaskFutureValue<V>> cache=
        CacheBuilder.newBuilder()
          .expireAfterWrite(lifetime.value(), lifetime.unit())
          .build();

      backlog=cache.asMap();
    }


    // ACCESSORS
    void queue(final Collection<V> keys)
    {
      for(final V key : keys)
      {
        final Integer hash=Integer.valueOf(key.hashCode());

        if(!backlog.containsKey(hash))
        {
          if(null==backlog.putIfAbsent(hash, new TaskFuture<V>()))
            queue.offer(key);
        }
      }
    }

    Collection<V> result(final Collection<V> keys) throws Exception
    {
      final Collection<V> returnValueCollection=collectionType.newInstance();

      for(final V key : keys)
      {
        final Integer hash=Integer.valueOf(key.hashCode());
        final TaskFutureValue<V> future=backlog.get(hash);

        final V value=future.value();

        if(null==value)
          backlog.remove(hash);
        else
          returnValueCollection.add(value);
      }

      return returnValueCollection;
    }


    // OVERRIDES/IMPLEMENTS
    // FIXME Recursive fork/join spin-up/down on demand.
    @Override
    public Void call()
    {
      for(;;)
      {
        if(Thread.interrupted())
        {
          Thread.currentThread().interrupt();

          break;
        }

        Collection<V> keys;

        try
        {
          keys=collectionType.newInstance();
        }
        catch(final InstantiationException | IllegalAccessException e)
        {
          throw new IllegalArgumentException(e);
        }

        // FIXME THRESHOLD
        while(50>keys.size())
        {
          final V v=queue.poll();

          if(null==v)
            break;

          keys.add(v);
        }

        try
        {
          if(0<keys.size())
          {
            final Method method=instance.getClass().getMethod(
              this.method.getName(), this.method.getParameterTypes()
            );

            for(final V value : (Collection<V>)method.invoke(instance, keys))
            {
              final Integer hash=value.hashCode();
              final TaskFutureValue<V> future=backlog.get(hash);

              if(null==future)
                backlog.putIfAbsent(hash, new TaskFutureValue<V>(value));
              else
                future.value(value);
            }
          }
        }
        catch(final ReflectiveOperationException e)
        {
          LOG.error(e.getMessage(), e);
        }

        try
        {
          Thread.sleep(10L);
        }
        catch(final InterruptedException e)
        {
          Thread.currentThread().interrupt();

          break;
        }
      }

      return null;
    }


    static class TaskFutureValue<V>
    {
      // MEMBERS
      protected final AtomicReference<V> refValue=new AtomicReference<>(null);


      // CONSTRUCTION
      TaskFutureValue()
      {
        super();
      }

      TaskFutureValue(final V value)
      {
        super();

        refValue.set(value);
      }


      // ACCESSORS/MUTATORS
      V value()
      {
        return refValue.get();
      }

      void value(final V value)
      {
        refValue.set(value);
      }
    }


    /**
     * TaskFuture
     *
     * @author evalcode.net
     */
    static class TaskFuture<V> extends TaskFutureValue<V>
    {
      // MEMBERS
      private final CountDownLatch resolved=new CountDownLatch(1);


      // OVERRIDES/IMPLEMENTS
      @Override
      V value()
      {
        final V value=refValue.get();

        if(null==value)
        {
          try
          {
            resolved.await(1, TimeUnit.MINUTES);
          }
          catch(final InterruptedException e)
          {
            Thread.currentThread().interrupt();
          }

          return refValue.get();
        }

        return value;
      }

      @Override
      void value(final V value)
      {
        refValue.set(value);

        resolved.countDown();
      }
    }
  }
}
