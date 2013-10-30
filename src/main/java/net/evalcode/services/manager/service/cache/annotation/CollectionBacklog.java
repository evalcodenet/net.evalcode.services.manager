package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashSet;
import net.evalcode.services.manager.service.cache.impl.CollectionBacklogProvider;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;


/**
 * CacheBacklog
 *
 * <p>
 * Caches possible map values for methods returning subsets for given input keys.
 * <p>
 * Define one or multiple keys to influence handling of passed
 * collection keys or to provide default keys as fallback.
 *
 * <p>
 * Default, use Bar.hashCode() of each key to check existance
 * of corresponding cached value:
 *
 * <pre>
 *   package net.evalcode.services;
 *
 *
 *   class Foo
 *   {
 *     {@link Cache &#064;Cache}
 *     {@link CollectionBacklog &#064;CacheBacklog}
 *     Collection<Bar> getValues(final Collection<Integer> keys)
 *     {
 *       [..]
 *     }
 *   }
 *
 *   // 1st invocation: Invokes original method with parameters {1, 2, 3},
 *   // caches & returns corresponding method's return values.
 *   getValues({1, 2, 3});
 *
 *   // 2nd invocation: Loads values for {1, 2, 3} from cache & returns.
 *   getValues({1, 2, 3});
 *
 *   // Invokes original method with keys for un-cached values {4, 5}.
 *   // Merges method's return values into cache and returns requested
 *   // collection values for keys {1, 2, 3, 4, 5}.
 *   getValues({1, 2, 3, 4, 5});
 * </pre>
 *
 * <p>
 * Annotated method must have exactly one parameter containing a collection of key(s)
 * for requested values.
 *
 * <p>
 * Parameter/key can be annotated with {@link Key &#064;Key} for further customization.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionBacklog
{
  // PROPERTIES
  @SuppressWarnings("rawtypes")
  Class<? extends Collection> type() default HashSet.class;
  /**
   * Cache backlog imlementation.
   */
  Class<? extends BacklogProvider> provider() default CollectionBacklogProvider.class;
}
