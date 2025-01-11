package com.codeus.winter.exception;

import jakarta.annotation.Nullable;

/**
 * Exception for not unique BeanDefinition.
 */
public class NotUniqueBeanDefinitionException extends RuntimeException {
    /**
     * Constructor with a message.
     *
     * @param message message.
     */
    public NotUniqueBeanDefinitionException(final String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message message.
     * @param cause   cause.
     */
    public NotUniqueBeanDefinitionException(@Nullable final String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
