package net.evalcode.services.manager.service.cache.impl;


import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;
import org.aopalliance.intercept.MethodInvocation;


/**
 * CollectionBacklogProvider
 *
 * @author evalcode.net
 */
public class CollectionBacklogProvider implements BacklogProvider<Object>
{
  // OVERRIDES/IMPLEMENTS
  @Override
  @SuppressWarnings("unchecked")
  public Map<Integer, Object> invoke(final MethodInvocation methodInvocation,
    final Map<Integer, Object> backlog)
      throws Throwable
  {
    final Annotation[][] annotations=methodInvocation.getMethod().getParameterAnnotations();

    int idx=0;

    for(final Annotation[] parameterAnnotations : annotations)
    {
      for(final Annotation annotation : parameterAnnotations)
      {
        if(annotation instanceof CollectionBacklog.Keys)
        {
          return invokeImpl(methodInvocation, backlog,
            (Collection<Object>)methodInvocation.getArguments()[idx]
          );
        }
      }

      idx++;
    }

    return null;
  }


  // IMPLEMENTATION
  @SuppressWarnings("unchecked")
  public Map<Integer, Object> invokeImpl(final MethodInvocation methodInvocation,
    final Map<Integer, Object> backlog, final Collection<Object> keys)
      throws Throwable
  {
    final Object[] aKeysRequested=new Object[keys.size()];
    System.arraycopy(keys.toArray(), 0, aKeysRequested, 0, keys.size());

    keys.removeAll(backlog.values());

    final Object valuesRetrieved=methodInvocation.proceed();

    if(valuesRetrieved instanceof Collection)
    {
      for(final Object object : (Collection<Object>)valuesRetrieved)
        backlog.put(Integer.valueOf(object.hashCode()), object);
    }

    final Map<Integer, Object> valuesRequested=new HashMap<>();

    for(final Object keyRequested : aKeysRequested)
      valuesRequested.put(keyRequested.hashCode(), backlog.get(keyRequested.hashCode()));

    return valuesRequested;
  }
}
