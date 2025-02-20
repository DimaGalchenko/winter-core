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
     */
    public QualifierAnnotationAutowireCandidateResolver() {
        this.qualifierTypes.add(Qualifier.class);
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
