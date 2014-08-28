package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Activate
 *
 * <p> Decorated method will be invoked by the component manager as soon as
 * its OSGi bundle as well as all its dependency bundles are started.
 *
 * <pre>
 *   &#064;Component
 *   class MyComponent
 *   {
 *     &#064;Activate
 *     void startup()
 *     {
 *       [..]
 *     }
 *   }
 * </pre>
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Activate
{

}
