package net.evalcode.services.manager.service.cache.impl;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    final ConcurrentMap<Integer, ValueFuture> futures=new ConcurrentHashMap<>();
    final ConcurrentMap<Integer, Object> backlog=new ConcurrentHashMap<>();

    // TODO Update external cache.
    final Cache<?> cache;
    final Object cacheKey;
    final Method method;

    volatile Class<? extends Collection> returnType;


    // CONSTRUCTION
    CollectionBacklogMethod(final Cache<?> cache, final Object cacheKey, final Method method)
    {
      this.cache=cache;
      this.cacheKey=cacheKey;
      this.method=method;
    }


    // ACCESSORS
    Object invoke(final MethodInvocation methodInvocation, final Collection<Object> keys)
      throws Throwable
    {
      final Integer[] keysHashed=new Integer[keys.size()];
      int idx=0;

      for(final Object key : keys)
      {
        // TODO Respect @Key.Type
        final Integer hash=Integer.valueOf(key.hashCode());

        keysHashed[idx++]=hash;

        final Object value=futures.get(hash);

        if(null==value)
          futures.putIfAbsent(hash, new ValueFuture(backlog, methodInvocation, hash, key));
      }

      final Collection<Object> returnValueCollection=returnValueCollection();

      for(final Integer hash : keysHashed)
        returnValueCollection.add(futures.get(hash).getValue());

      return returnValueCollection;
    }

    Class<? extends Collection> returnType()
    {
      if(null==returnType)
      {
        final CollectionBacklog annotation=method.getAnnotation(CollectionBacklog.class);

        returnType=annotation.type();

        return returnType;
      }

      return returnType;
    }

    Collection<Object> returnValueCollection() throws InstantiationException, IllegalAccessException
    {
      return (Collection<Object>)returnType().newInstance();
    }


    /**
     * PopulatorValue
     *
     * @author carsten.schipke@gmail.com
     */
    static class ValueFuture
    {
      // PREDEFINED PROPERTIES
      static final ConcurrentMap<Integer, ConcurrentMap<Integer, Object>> KEYS=
        new ConcurrentHashMap<>();


      // MEMBERS
      final ConcurrentMap<Integer, Object> backlog;
      final MethodInvocation methodInvocation;
      final Integer methodInvocationHash;
      final Integer hash;


      // CONSTRUCTION
      ValueFuture(final ConcurrentMap<Integer, Object> backlog,
        final MethodInvocation methodInvocation, final Integer hash, final Object key)
      {
        this.backlog=backlog;
        this.hash=hash;

        this.methodInvocation=methodInvocation;
        this.methodInvocationHash=methodInvocation.hashCode();

        if(!KEYS.containsKey(methodInvocationHash))
          KEYS.putIfAbsent(methodInvocationHash, new ConcurrentHashMap<Integer, Object>());

        KEYS.get(methodInvocationHash).putIfAbsent(hash, key);
      }


      // ACCESSORS
      Object getValue() throws Throwable
      {
        final Object value=backlog.get(hash);

        if(null==value)
        {
          final Collection<Object> arguments=(Collection<Object>)methodInvocation.getArguments()[0];
          arguments.removeAll(backlog.values());

          if(0<arguments.size())
          {
            final Map<Integer, Object> keys=KEYS.get(methodInvocationHash);
            arguments.retainAll(keys.values());

            final Collection<Object> collection=(Collection<Object>)methodInvocation.proceed();

            // TODO Respect @Key.Type
            for(final Object object : collection)
              backlog.put(object.hashCode(), object);
          }

          return backlog.get(hash);
        }

        return value;
      }
    }
  }
}
