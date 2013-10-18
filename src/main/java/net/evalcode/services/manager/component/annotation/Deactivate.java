package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Deactivate
 *
 * <p> Decorated method will be invoked by the component manager as soon as
 * its OSGi bundle or one of its dependency bundles is requested to stop.
 *
 * <pre>
 *   &#064;Component
 *   class MyComponent
 *   {
 *     &#064;Deactivate
 *     void shutdown()
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
public @interface Deactivate
{

}
