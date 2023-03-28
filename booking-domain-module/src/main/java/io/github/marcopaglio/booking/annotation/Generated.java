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
 * This annotation has is used for excluding methods and constructors 
 * from JaCoCo (version 0.8.2 or newer) coverage.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface Generated {

}
