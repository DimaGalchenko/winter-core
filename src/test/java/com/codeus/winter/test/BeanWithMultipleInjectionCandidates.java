package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;

public class BeanWithMultipleInjectionCandidates {
    private final Common common;

    @Autowired
    public BeanWithMultipleInjectionCandidates(Common common) {
        this.common = common;
    }

    public Common getCommon() {
        return common;
    }
}
