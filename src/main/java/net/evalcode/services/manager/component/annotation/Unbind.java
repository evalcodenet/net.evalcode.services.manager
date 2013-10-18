package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Unbind
 *
 * <p> Declares decorated method as a service binding dis-connector.
 *
 * <p> A service binding dis-connector needs to expect exactly one parameter
 * which specifies the type of service component it is dis-connecting when
 * it becomes unavailable.
 *
 * <p> Service component types are defined by corresponding service interfaces,
 * decorated with the &#064;Service annotation - any other (sub-)types are not valid
 * and will be ignored / not recognized as services.
 *
 * <p> As soon as a service component's bundle is getting requested to be stopped,
 * the component manager will invoke all un-binding method(s) that await a parameter
 * of type of a service interface implemented by the stopping component and pass
 * the component as this parameter.
 *
 * <pre>
 *   &#064;Component
 *   class MyComponent
 *   {
 *     /**
 *      * Every stopping component that implements the service interface FooService
 *      * will get passed to this service binding dis-connector.
 *      *&#047;
 *     &#064;Unbind
 *     void unbindFooService(final FooService foo)
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
public @interface Unbind
{

}
