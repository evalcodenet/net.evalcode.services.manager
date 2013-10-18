package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Service
 *
 * <p> Declares decorated interface as a service.
 *
 * <p> See {@link Component &#064;Component} for further information on how to
 * implement service interfaces.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service
{

}
