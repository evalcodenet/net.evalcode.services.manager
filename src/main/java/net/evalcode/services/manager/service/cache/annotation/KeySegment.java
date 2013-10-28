package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * KeySegment
 *
 * @author evalcode.net
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface KeySegment
{
  // PROPERTIES
  Key.Type type() default Key.Type.HASHCODE;

  String value() default "";
}
