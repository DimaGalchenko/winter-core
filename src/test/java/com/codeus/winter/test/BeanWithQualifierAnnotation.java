package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.annotation.Qualifier;

public class BeanWithQualifierAnnotation {
    Common common;

    @Autowired
    public BeanWithQualifierAnnotation(@Qualifier("BeanA") Common common) {
        this.common = common;
    }

    public Common getCommon() {
        return common;
    }
}
