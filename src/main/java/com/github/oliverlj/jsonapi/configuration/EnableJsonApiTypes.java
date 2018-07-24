package com.github.oliverlj.jsonapi.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation to enable json api via annotation configuration.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JsonApiHttpMessageConvertersConfiguration.class)
public @interface EnableJsonApiTypes {

    /**
     * Alias for {@link #basePackages}.
     * <p>
     * Allows for more concise annotation declarations if no other attributes are needed &mdash; for example, {@code @ComponentScan("org.my.pkg")} instead of
     * {@code @ComponentScan(basePackages = "org.my.pkg")}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated json api types.
     * <p>
     * {@link #value} is an alias for (and mutually exclusive with) this attribute.
     * <p>
     * Use {@link #basePackageClasses} for a type-safe alternative to String-based package names.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages to scan for annotated annotated json api types. The package of each class
     * specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

}
