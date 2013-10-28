package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Region
 *
 * @author evalcode.net
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Region
{
  // PROPERTIES
  String value() default "";

  String defaultConfig() default "";
}
