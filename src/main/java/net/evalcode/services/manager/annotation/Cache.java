package net.evalcode.services.manager.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Cache
 *
 * <p> Cache invocation of decorated methods.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
  // PROPERTIES
  String region() default "";
  String key() default "";

  int ttl() default 0;
}
