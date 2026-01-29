package com.noadsch12.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NotUpdated {

    String value() default "This element is no longer actively updated.";
}
