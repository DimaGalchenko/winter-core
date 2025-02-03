package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.annotation.Qualifier;

public class BeanWithQualifierAnnotation {

    @Autowired
    public BeanWithQualifierAnnotation(@Qualifier("BeanA") Common common) {

    }
}
