package com.noadsch12.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NotFinished {

    String value() default "This element is not finished yet.";
}
