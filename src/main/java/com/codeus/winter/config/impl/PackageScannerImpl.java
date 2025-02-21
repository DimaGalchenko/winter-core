package com.codeus.winter.config.impl;

import com.codeus.winter.config.PackageScanner;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import org.reflections.Reflections;

public class PackageScannerImpl implements PackageScanner {

    @Override
    public final Set<Class<?>> findClassesWithAnnotations(String packageName,
                                                          Set<Class<? extends Annotation>> annotations) {
        Set<Class<?>> annotatedClasses = new HashSet<>();

        Reflections reflections = new Reflections(packageName);

        for (Class<? extends Annotation> annotation : annotations) {
            annotatedClasses.addAll(reflections.getTypesAnnotatedWith(annotation));
        }

        return annotatedClasses;
    }

}
