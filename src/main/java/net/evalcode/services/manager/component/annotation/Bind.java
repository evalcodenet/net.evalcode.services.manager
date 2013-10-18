package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Bind
 *
 * <p> Declares decorated method as a service binding connector.
 *
 * <p> A service binding connector expects exactly one parameter
 * which specifies the type of service it is connecting when it
 * becomes available.
 *
 * <p> Service types are defined by interfaces that are decorated
 * with the &#064;{@link Service} annotation.
 *
 * <pre>
 *   &#064;Component
 *   class MyComponent
 *   {
 *     /**
 *      * Every started component that implements the service interface FooService
 *      * will get passed to this service binding connector.
 *      *&#047;
 *     &#064;Bind
 *     void bindFooService(final FooService foo)
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
public @interface Bind
{

}
