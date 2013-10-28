package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashSet;
import net.evalcode.services.manager.service.cache.impl.CollectionBacklogProvider;


/**
 * Backlog
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
 *     {@link CollectionBacklog &#064;CollectionBacklog}
 *     Collection<Bar> getValues({@link CollectionBacklog.Keys &#064;CollectionBacklog.Keys} final Collection<Integer> keys)
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
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionBacklog
{
  // PROPERTIES
  @SuppressWarnings("rawtypes")
  /**
   * Concrete collection implementation type
   * the annotated method is expected to return.
   */
  Class<? extends Collection> type() default HashSet.class;
  /**
   * Backlog imlementation.
   */
  Class<? extends CollectionBacklogProvider> provider() default CollectionBacklogProvider.class;


  /**
   * CollectionBacklog.Keys
   *
   * <p>
   * Defines method parameter as set of keys for cached backlog.
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Keys
  {
    // PROPERTIES
    /**
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
     *   {@link Cache &#064;Cache}
     *   {@link CollectionBacklog &#064;CollectionBacklog}
     *   Collection<Foo> getValues({@link CollectionBacklog.Keys &#064;CollectionBacklog.Keys} final Collection<Bar> keys)
     *   {
     *     [..]
     *   }
     * </pre>
     */
    Key[] value() default {@Key};
  }
}
