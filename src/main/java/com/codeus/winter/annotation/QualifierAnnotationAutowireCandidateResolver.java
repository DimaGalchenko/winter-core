package com.codeus.winter.annotation;

import com.codeus.winter.config.DependencyDescriptor;
import com.codeus.winter.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class QualifierAnnotationAutowireCandidateResolver {

    private final Set<Class<? extends Annotation>> qualifierTypes = new HashSet<>();

    /**
     * Create a new {@code QualifierAnnotationAutowireCandidateResolver} for Winter's
     * standard {@link com.codeus.winter.annotation.Qualifier} annotation.
     * <p>Also supports JSR-330's {@link jakarta.inject.Qualifier} annotation (as well as
     * its pre-Jakarta {@code javax.inject.Qualifier} equivalent), if available.
     */
    @SuppressWarnings("unchecked")
    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
        try {
            this.qualifierTypes.add((Class<? extends Annotation>) Class.forName("jakarta.inject.Qualifier"));
        } catch (ClassNotFoundException ex) {
            // JSR-330 API (as included in Jakarta EE) not available - simply skip.
        }
        try {
            this.qualifierTypes.add((Class<? extends Annotation>) Class.forName("javax.inject.Qualifier"));
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - simply skip.
        }
    }

    public String getSuggestedName(DependencyDescriptor descriptor) {
        for (Annotation annotation : descriptor.getAnnotations()) {
            if (isQualifier(annotation.annotationType())) {
                Object value = AnnotationUtils.getValue(annotation);
                if (value instanceof String str) {
                    return str;
                }
            }
        }
        return null;
    }


    private boolean isQualifier(Class<? extends Annotation> annotationType) {
        for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
            if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
                return true;
            }
        }
        return false;
    }
}
