package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Cache
 *
 * <p>
 * Cache invocation of decorated methods.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
  // PROPERTIES
  Region region() default @Region;

  Key key() default @Key;

  Lifetime lifetime() default @Lifetime;

  CollectionBacklog backlog() default @CollectionBacklog;
}
