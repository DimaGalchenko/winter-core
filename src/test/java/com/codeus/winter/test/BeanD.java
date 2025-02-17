package com.codeus.winter.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class BeanD {
    private final List<Common> list;
    private final Set<Common> set;
    private final Map<String, Common> map;

    public BeanD(List<Common> list, Set<Common> set, Map<String, Common> map) {
        this.list = list;
        this.set = set;
        this.map = map;
    }

    public List<Common> getList() {
        return list;
    }

    public Set<Common> getSet() {
        return set;
    }

    public Map<String, Common> getMap() {
        return map;
    }
}
