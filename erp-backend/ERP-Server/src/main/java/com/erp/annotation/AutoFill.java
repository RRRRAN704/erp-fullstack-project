package com.erp.annotation;





/*Custom annotation used to identify a method that
 requires automatic field filling processing*/


import com.erp.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// @Target defines where this annotation can be applied.
// Here, ElementType.METHOD means it can only be placed on methods.
@Target(ElementType.METHOD)

// @Retention defines how long the annotation is retained.
// RetentionPolicy.RUNTIME means it is available at runtime through reflection.
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    //Database operation type: update or insert
    OperationType value();
}
