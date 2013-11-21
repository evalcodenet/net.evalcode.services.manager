package net.evalcode.services.manager.service.concurrent.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.service.concurrent.MethodExecutor;
import net.evalcode.services.manager.service.concurrent.ioc.MethodInvocationExecutor;


/**
 * Asynchronous
 *
 * <p>
 * Invoke method asynchronously
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Asynchronous
{
  Class<? extends MethodExecutor> executor() default MethodInvocationExecutor.class;
}
