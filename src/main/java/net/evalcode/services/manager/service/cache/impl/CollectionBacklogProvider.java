package net.evalcode.services.manager.service.cache.impl;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.annotation.Cache;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.annotation.KeySegment;
import net.evalcode.services.manager.service.cache.annotation.Lifetime;
import net.evalcode.services.manager.service.cache.impl.CollectionBacklogProvider.CollectionBacklogMethod.Metadata;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;
import net.jcip.annotations.ThreadSafe;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheBuilder;


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
  static final Logger LOG=LoggerFactory.getLogger(CollectionBacklogProvider.class);
  static final ConcurrentMap<String, CollectionBacklogMethod> METHODS=new ConcurrentHashMap<>();


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation)
    throws Throwable
  {
    final Method method=methodInvocation.getMethod();
    final Metadata metadata=Metadata.get(method);

    final Object[] arguments=methodInvocation.getArguments();
    final Collection<Object> keys=(Collection)arguments[metadata.collectionKeysIdx()];

    final CollectionBacklogMethod populator=METHODS.get(metadata.cacheKeyImpl(methodInvocation));

    if(null==populator)
    {
      final CollectionBacklogMethod populatorNew=new CollectionBacklogMethod(method);
      final CollectionBacklogMethod populatorExisting=METHODS.putIfAbsent(
        metadata.cacheKeyImpl(methodInvocation), populatorNew
      );

      if(null==populatorExisting)
        return populatorNew.invoke(methodInvocation, keys);

      return populatorExisting.invoke(methodInvocation, keys);
    }

    return populator.invoke(methodInvocation, keys);
  }


  /**
   * CollectionBacklogMethod
   *
   * @author carsten.schipke@gmail.com
   */
  static class CollectionBacklogMethod
  {
    // MEMBERS
    final ConcurrentMap<Integer, Element> backlog;
    final Metadata metadata;


    // CONSTRUCTION
    CollectionBacklogMethod(final Method method)
    {
      metadata=Metadata.get(method);

      final Lifetime lifetime=metadata.collectionLifetime();

      final com.google.common.cache.Cache<Integer, Element> cache=
        CacheBuilder.newBuilder()
          .expireAfterWrite(lifetime.value(), lifetime.unit())
          .build();

      backlog=cache.asMap();
    }


    // ACCESSORS
    Object invoke(final MethodInvocation methodInvocation, final Collection<Object> keys)
      throws Throwable
    {
      final Integer[] keysHashed=new Integer[keys.size()];
      int idx=0;

      final CountDownLatch methodInvoked=new CountDownLatch(1);
      final Map<Integer, Element> ofInterest=new HashMap<>();
      final Set<Object> missingValues=new HashSet<>();

      for(final Object key : keys)
      {
        // TODO Respect @Key.Type
        // TODO Provide interface to allow cache elements to define a comparator.
        final Integer hash=Integer.valueOf(key.hashCode());

        keysHashed[idx++]=hash;

        final Element element=backlog.get(hash);

        if(null==element)
        {
          final Element elementNew=new ElementFuture(methodInvoked);
          final Element elementExisting=backlog.putIfAbsent(hash, elementNew);

          if(null==elementExisting)
          {
            ofInterest.put(hash, elementNew);

            missingValues.add(key);
          }
          else
          {
            ofInterest.put(hash, elementExisting);
          }
        }
        else
        {
          if(null==element.get())
          {
            final Element elementNew=new ElementFuture(methodInvoked);

            if(backlog.replace(hash, element, elementNew))
            {
              ofInterest.put(hash,  elementNew);

              missingValues.add(key);
            }
            else
            {
              ofInterest.put(hash, backlog.get(hash));
            }
          }
          else
          {
            ofInterest.put(hash, element);
          }
        }
      }

      if(0<missingValues.size())
      {
        final Collection<Object> arguments=
          (Collection<Object>)methodInvocation.getArguments()[metadata.collectionKeysIdx()];

        arguments.retainAll(missingValues);

        if(0<arguments.size())
        {
          try
          {
            final Collection<Object> collection=(Collection<Object>)methodInvocation.proceed();

            // TODO Respect @Key.Type
            for(final Object object : collection)
            {
              final Element element=ofInterest.get(object.hashCode());

              if(null==element)
              {
                LOG.warn("Key/value hashcode mismatch - object will be added to cache but can not be returned to caller [method: {}, hash: {}, object: {}].",
                  metadata.method, object.hashCode(), object
                );

                backlog.putIfAbsent(object.hashCode(), new Element(object));
              }
              else
              {
                element.set(object);
              }
            }
          }
          finally
          {
            methodInvoked.countDown();
          }
        }
      }

      final Collection<Object> returnValueCollection=
        (Collection<Object>)metadata.collectionType().newInstance();

      for(final Integer hash : keysHashed)
      {
        final Element element=ofInterest.get(hash);

        if(null==element)
        {
          LOG.warn("Value expired [{}].", hash);
        }
        else
        {
          final Object value=element.get();

          if(null!=value)
            returnValueCollection.add(value);
        }
      }

      return returnValueCollection;
    }


    /**
     * Element
     *
     * @author carsten.schipke@gmail.com
     */
    static class Element
    {
      // MEMBERS
      private volatile Object value;


      // CONSTRUCTION
      Element()
      {
        super();
      }

      Element(final Object value)
      {
        super();

        this.value=value;
      }


      // ACCESSORS/MUTATORS
      Object get()
      {
        return value;
      }

      void set(final Object object)
      {
        value=object;
      }
    }


    /**
     * ElementFuture
     *
     * @author carsten.schipke@gmail.com
     */
    static class ElementFuture extends Element
    {
      // MEMBERS
      private final CountDownLatch monitor;


      // CONSTRUCTION
      ElementFuture(final CountDownLatch monitor)
      {
        super();

        this.monitor=monitor;
      }


      // ACCESSORS
      Object get()
      {
        final Object value=super.get();

        if(null==value)
        {
          try
          {
            monitor.await();
          }
          catch(final InterruptedException e)
          {
            Thread.currentThread().interrupt();
          }

          return super.get();
        }

        return value;
      }
    }


    /**
     * Metadata
     *
     * @author carsten.schipke@gmail.com
     */
    static class Metadata
    {
      // PREDEFINED PROPERTIES
      private static final ConcurrentMap<String, Metadata> INSTANCES=new ConcurrentHashMap<>();


      // MEMBERS
      private final String name;
      private final Method method;

      private volatile Class<? extends Collection> collectionType;
      private volatile Lifetime collectionLifetime;
      private volatile int collectionKeysIdx=-1;

      private volatile boolean collectionKeySegmentIndicesInitialized=false;
      private int collectionKeySegmentIndices[]=new int[0];


      // CONSTRUCTION
      Metadata(final String name, final Method method)
      {
        this.name=name;
        this.method=method;
      }


      // STATIC ACCESSORS
      static Metadata get(final Method method)
      {
        final String name=method.toGenericString();

        if(!INSTANCES.containsKey(name))
        {
          final Metadata metadata=new Metadata(name, method);
          final Metadata metadataExisting=INSTANCES.putIfAbsent(name, metadata);

          if(null==metadataExisting)
            return metadata;

          return metadataExisting;
        }

        return INSTANCES.get(name);
      }


      // IMPLEMENTATION
      String cacheKeyImpl(final MethodInvocation methodInvocation)
      {
        final StringBuffer keySegments=new StringBuffer(name);
        keySegments.append(name);

        for(final int idx : cacheKeySegments())
        {
          keySegments.append("-");
          keySegments.append(methodInvocation.getArguments()[idx].hashCode());
        }

        return keySegments.toString();
      }

      int[] cacheKeySegments()
      {
        if(!collectionKeySegmentIndicesInitialized)
        {
          final Annotation[][] annotations=method.getParameterAnnotations();
          int idx[]=new int[annotations.length];

          int i=0, j=0;

          for(final Annotation[] parameterAnnotations : annotations)
          {
            for(final Annotation parameterAnnotation : parameterAnnotations)
            {
              if(KeySegment.class.equals(parameterAnnotation.annotationType()))
                idx[j++]=i;
            }

            i++;
          }

          int tmp[]=new int[j];
          System.arraycopy(idx, 0, tmp, 0, j);

          collectionKeySegmentIndices=tmp;
          collectionKeySegmentIndicesInitialized=true;
        }

        return collectionKeySegmentIndices;
      }

      int collectionKeysIdx()
      {
        if(-1==collectionKeysIdx)
        {
          int idx=0;

          if(1==method.getParameterTypes().length
            && Collection.class.isAssignableFrom(method.getParameterTypes()[0]))
            return collectionKeysIdx=0;

          for(final Annotation[] annotations : method.getParameterAnnotations())
          {
            for(final Annotation annotation : annotations)
            {
              if(CollectionBacklog.Keys.class.equals(annotation.annotationType()))
                return collectionKeysIdx=idx;
            }

            idx++;
          }

          throw new IllegalArgumentException("Method must have exactly one argument of type "
            + "collection annotated with @CollectionBacklog.Keys."
          );
        }

        return collectionKeysIdx;
      }

      Lifetime collectionLifetime()
      {
        if(null==collectionLifetime)
          collectionLifetime=method.getAnnotation(Cache.class).lifetime();

        return collectionLifetime;
      }

      Class<? extends Collection> collectionType()
      {
        if(null==collectionType)
          collectionType=method.getAnnotation(CollectionBacklog.class).type();

        return collectionType;
      }
    }
  }
}
