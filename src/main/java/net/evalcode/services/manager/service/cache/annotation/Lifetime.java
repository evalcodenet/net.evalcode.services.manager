package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Lifetime
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lifetime
{
  // PROPERTIES
  int seconds() default 0;

  int hits() default 0;
}
