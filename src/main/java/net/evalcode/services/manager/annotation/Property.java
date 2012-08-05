package net.evalcode.services.manager.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Property
 *
 * <p> Define system properties used by components.
 *
 * <p> Reasons for using this, rather than accessing System.getProperty directly:
 *
 * <ul>
 *   <li> API Documentation</li>
 *   <li> Component manager automatically provides
 *     <ul>
 *       <li> Configuration and re-initialization of components/services at runtime</li>
 *       <li> Start - configure - crash & recover</li>
 *       <li> Configuration input validation</li>
 *       <li> Configuration consistency checks</li>
 *       <li> Dependency injection of configuration</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <pre>
 *   &#064;Component(properties={
 *     &#064;Property(name="my.foo.property", defaultValue="bar"),
 *     &#064;Property(name="my.bar.property", defaultValue="foo")
 *   })
 *   class MyComponent
 *   {
 *     &#064;Inject
 *     &#064;Named("my.foo.property")
 *     String fooProperty;
 *
 *     &#064;Inject
 *     &#064;Named("my.bar.property")
 *     String barProperty;
 *
 *     [..]
 *   }
 * </pre>
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property
{
  // PROPERTIES
  /**
   * @return Literal name of system property.
   */
  String name();

  /**
   * @return Default value to be used if system property is not specified by user.
   */
  String defaultValue() default "";
}
