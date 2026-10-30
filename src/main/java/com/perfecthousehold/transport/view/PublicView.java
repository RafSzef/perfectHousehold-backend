package com.perfecthousehold.transport.view;

import com.fasterxml.jackson.annotation.JsonView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for controller endpoints to serialize only public entity fields,
 * automatically hiding sensitive fields like password hashes.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JsonView(Views.Public.class)
public @interface PublicView {
}
