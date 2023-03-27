package io.github.marcopaglio.booking.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Generated annotation is used to mark source code that has been generated.
 * It can also be used to differentiate user written code from generated code in a single file.
 * This annotation is a light version of {@code javax.annotation.Generated} 
 * without values and applicable only on methods and constructors.
 * 
 * @see 
 *   <a href="https://docs.oracle.com/javase/8/docs/api/javax/annotation/Generated.html">
 *     javax.annotation.Generated
 *   </a>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface Generated {

}
