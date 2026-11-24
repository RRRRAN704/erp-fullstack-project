package com.erp.annotation;

import com.erp.enumeration.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts a controller method (or every method of a controller class) to a role.
 * Checked by RoleCheckAspect before the method runs; an annotation on the method wins over one on the class.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    Role value();
}
