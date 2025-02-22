package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;

public class BeanWithMultiplyInjectionCandidates {
    private final Common common;

    @Autowired
    public BeanWithMultiplyInjectionCandidates(Common common) {
        this.common = common;
    }

    public Common getCommon() {
        return common;
    }
}
